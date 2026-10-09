/*
 * Copyright Â© 2010 Intel Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next
 * paragraph) shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/**
 * \file linker.cpp
 * GLSL linker implementation
 *
 * Given a set of shaders that are to be linked to generate a final program,
 * there are three distinct stages.
 *
 * In the first stage shaders are partitioned into groups based on the shader
 * type.  All shaders of a particular type (e.g., vertex shaders) are linked
 * together.
 *
 *   - Undefined references in each shader are resolve to definitions in
 *     another shader.
 *   - Types and qualifiers of uniforms, outputs, and global variables defined
 *     in multiple shaders with the same name are verified to be the same.
 *   - Initializers for uniforms and global variables defined
 *     in multiple shaders with the same name are verified to be the same.
 *
 * The result, in the terminology of the GLSL spec, is a set of shader
 * executables for each processing unit.
 *
 * After the first stage is complete, a series of semantic checks are performed
 * on each of the shader executables.
 *
 *   - Each shader executable must define a \c main function.
 *   - Each vertex shader executable must write to \c gl_Position.
 *   - Each fragment shader executable must write to either \c gl_FragData or
 *     \c gl_FragColor.
 *
 * In the final stage individual shader executables are linked to create a
 * complete exectuable.
 *
 *   - Types of uniforms defined in multiple shader stages with the same name
 *     are verified to be the same.
 *   - Initializers for uniforms defined in multiple shader stages with the
 *     same name are verified to be the same.
 *   - Types and qualifiers of outputs defined in one stage are verified to
 *     be the same as the types and qualifiers of inputs defined with the same
 *     name in a later stage.
 *
 * \author Ian Romanick <ian.d.romanick@intel.com>
 */

#include <ctype.h>
#include "../../util/strndup.h"
#include "glsl_symbol_table.h"
#include "glsl_parser_extras.h"
#include "ir.h"
#include "../nir/nir.h"
#include "program.h"
#include "../../mesa/program/prog_instruction.h"
#include "program.h"
#include "../../util/mesa-sha1.h"
#include "../../util/set.h"
#include "string_to_uint_map.h"
#include "linker.h"
#include "linker_util.h"
#include "link_varyings.h"
#include "ir_optimization.h"
#include "ir_rvalue_visitor.h"
#include "ir_uniform.h"
#include "builtin_functions.h"

#include "../../util/u_string.h"
#include "../../util/u_math.h"


#include "../../mesa/main/shaderobj.h"
#include "../../mesa/main/enums.h"
#include "../../mesa/main/mtypes.h"
#include "../../mesa/main/context.h"


namespace {

struct find_variable {
   const char *name;
   bool found;

   find_variable(const char *name) : name(name), found(false) {}
};

/**
 * Visitor that determines whether or not a variable is ever written.
 * Note: this is only considering if the variable is statically written
 * (= regardless of the runtime flow of control)
 *
 * Use \ref find_assignments for convenience.
 */
class find_assignment_visitor : public ir_hierarchical_visitor {
public:
   find_assignment_visitor(unsigned num_vars,
                           find_variable * const *vars)
      : num_variables(num_vars), num_found(0), variables(vars)
   {
   }

   virtual ir_visitor_status visit_enter(ir_assignment *ir)
   {
      ir_variable *const var = ir->lhs->variable_referenced();

      return check_variable_name(var->name);
   }

   virtual ir_visitor_status visit_enter(ir_call *ir)
   {
      foreach_two_lists(formal_node, &ir->callee->parameters,
                        actual_node, &ir->actual_parameters) {
         ir_rvalue *param_rval = (ir_rvalue *) actual_node;
         ir_variable *sig_param = (ir_variable *) formal_node;

         if (sig_param->data.mode == ir_var_function_out ||
             sig_param->data.mode == ir_var_function_inout) {
            ir_variable *var = param_rval->variable_referenced();
            if (var && check_variable_name(var->name) == visit_stop)
               return visit_stop;
         }
      }

      if (ir->return_deref != NULL) {
         ir_variable *const var = ir->return_deref->variable_referenced();

         if (check_variable_name(var->name) == visit_stop)
            return visit_stop;
      }

      return visit_continue_with_parent;
   }

private:
   ir_visitor_status check_variable_name(const char *name)
   {
      for (unsigned i = 0; i < num_variables; ++i) {
         if (strcmp(variables[i]->name, name) == 0) {
            if (!variables[i]->found) {
               variables[i]->found = true;

               assert(num_found < num_variables);
               if (++num_found == num_variables)
                  return visit_stop;
            }
            break;
         }
      }

      return visit_continue_with_parent;
   }

private:
   unsigned num_variables;           /**< Number of variables to find */
   unsigned num_found;               /**< Number of variables already found */
   find_variable * const *variables; /**< Variables to find */
};

/**
 * Determine whether or not any of NULL-terminated list of variables is ever
 * written to.
 */
static void
find_assignments(exec_list *ir, find_variable * const *vars)
{
   unsigned num_variables = 0;

   for (find_variable * const *v = vars; *v; ++v)
      num_variables++;

   find_assignment_visitor visitor(num_variables, vars);
   visitor.run(ir);
}

/**
 * Determine whether or not the given variable is ever written to.
 */
static void
find_assignments(exec_list *ir, find_variable *var)
{
   find_assignment_visitor visitor(1, &var);
   visitor.run(ir);
}

/**
 * Visitor that determines whether or not a variable is ever read.
 */
class find_deref_visitor : public ir_hierarchical_visitor {
public:
   find_deref_visitor(const char *name)
      : name(name), found(false)
   {
      /* empty */
   }

   virtual ir_visitor_status visit(ir_dereference_variable *ir)
   {
      if (strcmp(this->name, ir->var->name) == 0) {
         this->found = true;
         return visit_stop;
      }

      return visit_continue;
   }

   bool variable_found() const
   {
      return this->found;
   }

private:
   const char *name;       /**< Find writes to a variable with this name. */
   bool found;             /**< Was a write to the variable found? */
};


/**
 * A visitor helper that provides methods for updating the types of
 * ir_dereferences.  Classes that update variable types (say, updating
 * array sizes) will want to use this so that dereference types stay in sync.
 */
class deref_type_updater : public ir_hierarchical_visitor {
public:
   virtual ir_visitor_status visit(ir_dereference_variable *ir)
   {
      ir->type = ir->var->type;
      return visit_continue;
   }

   virtual ir_visitor_status visit_leave(ir_dereference_array *ir)
   {
      const glsl_type *const vt = ir->array->type;
      if (vt->is_array())
         ir->type = vt->fields.array;
      return visit_continue;
   }

   virtual ir_visitor_status visit_leave(ir_dereference_record *ir)
   {
      ir->type = ir->record->type->fields.structure[ir->field_idx].type;
      return visit_continue;
   }
};


class array_resize_visitor : public deref_type_updater {
public:
   using deref_type_updater::visit;

   unsigned num_vertices;
   gl_shader_program *prog;
   gl_shader_stage stage;

   array_resize_visitor(unsigned num_vertices,
                        gl_shader_program *prog,
                        gl_shader_stage stage)
   {
      this->num_vertices = num_vertices;
      this->prog = prog;
      this->stage = stage;
   }

   virtual ~array_resize_visitor()
   {
      /* empty */
   }

   virtual ir_visitor_status visit(ir_variable *var)
   {
      if (!var->type->is_array() || var->data.mode != ir_var_shader_in ||
          var->data.patch)
         return visit_continue;

      unsigned size = var->type->length;

      if (stage == MESA_SHADER_GEOMETRY) {
         /* Generate a link error if the shader has declared this array with
          * an incorrect size.
          */
         if (!var->data.implicit_sized_array &&
             size && size != this->num_vertices) {
            linker_error(this->prog, "size of array %s declared as %u, "
                         "but number of input vertices is %u\n",
                         var->name, size, this->num_vertices);
            return visit_continue;
         }

         /* Generate a link error if the shader attempts to access an input
          * array using an index too large for its actual size assigned at
          * link time.
          */
         if (var->data.max_array_access >= (int)this->num_vertices) {
            linker_error(this->prog, "%s shader accesses element %i of "
                         "%s, but only %i input vertices\n",
                         _mesa_shader_stage_to_string(this->stage),
                         var->data.max_array_access, var->name, this->num_vertices);
            return visit_continue;
         }
      }

      var->type = glsl_type::get_array_instance(var->type->fields.array,
                                                this->num_vertices);
      var->data.max_array_access = this->num_vertices - 1;

      return visit_continue;
   }
};

class array_length_to_const_visitor : public ir_rvalue_visitor {
public:
   array_length_to_const_visitor()
   {
      this->progress = false;
   }

   virtual ~array_length_to_const_visitor()
   {
      /* empty */
   }

   bool progress;

   virtual void handle_rvalue(ir_rvalue **rvalue)
   {
      if (*rvalue == NULL || (*rvalue)->ir_type != ir_type_expression)
         return;

      ir_expression *expr = (*rvalue)->as_expression();
      if (expr) {
         if (expr->operation == ir_unop_implicitly_sized_array_length) {
            assert(!expr->operands[0]->type->is_unsized_array());
            ir_constant *constant = new(expr)
               ir_constant(expr->operands[0]->type->array_size());
            if (constant) {
               *rvalue = constant;
            }
         }
      }
   }
};

/**
 * Visitor that determines the highest stream id to which a (geometry) shader
 * emits vertices. It also checks whether End{Stream}Primitive is ever called.
 */
class find_emit_vertex_visitor : public ir_hierarchical_visitor {
public:
   find_emit_vertex_visitor(int max_allowed)
      : max_stream_allowed(max_allowed),
        invalid_stream_id(0),
        invalid_stream_id_from_emit_vertex(false),
        end_primitive_found(false),
        used_streams(0)
   {
      /* empty */
   }

   virtual ir_visitor_status visit_leave(ir_emit_vertex *ir)
   {
      int stream_id = ir->stream_id();

      if (stream_id < 0) {
         invalid_stream_id = stream_id;
         invalid_stream_id_from_emit_vertex = true;
         return visit_stop;
      }

      if (stream_id > max_stream_allowed) {
         invalid_stream_id = stream_id;
         invalid_stream_id_from_emit_vertex = true;
         return visit_stop;
      }

      used_streams |= 1 << stream_id;

      return visit_continue;
   }

   virtual ir_visitor_status visit_leave(ir_end_primitive *ir)
   {
      end_primitive_found = true;

      int stream_id = ir->stream_id();

      if (stream_id < 0) {
         invalid_stream_id = stream_id;
         invalid_stream_id_from_emit_vertex = false;
         return visit_stop;
      }

      if (stream_id > max_stream_allowed) {
         invalid_stream_id = stream_id;
         invalid_stream_id_from_emit_vertex = false;
         return visit_stop;
      }

      used_streams |= 1 << stream_id;

      return visit_continue;
   }

   bool error()
   {
      return invalid_stream_id != 0;
   }

   const char *error_func()
   {
      return invalid_stream_id_from_emit_vertex ?
         "EmitStreamVertex" : "EndStreamPrimitive";
   }

   int error_stream()
   {
      return invalid_stream_id;
   }

   unsigned active_stream_mask()
   {
      return used_streams;
   }

   bool uses_end_primitive()
   {
      return end_primitive_found;
   }

private:
   int max_stream_allowed;
   int invalid_stream_id;
   bool invalid_stream_id_from_emit_vertex;
   bool end_primitive_found;
   unsigned used_streams;
};

} /* anonymous namespace */

void
linker_error(gl_shader_program *prog, const char *fmt, ...)
{
   va_list ap;

   ralloc_strcat(&prog->data->InfoLog, "error: ");
   va_start(ap, fmt);
   ralloc_vasprintf_append(&prog->data->InfoLog, fmt, ap);
   va_end(ap);

   prog->data->LinkStatus = LINKING_FAILURE;
}


void
linker_warning(gl_shader_program *prog, const char *fmt, ...)
{
   va_list ap;

   ralloc_strcat(&prog->data->InfoLog, "warning: ");
   va_start(ap, fmt);
   ralloc_vasprintf_append(&prog->data->InfoLog, fmt, ap);
   va_end(ap);

}


void
link_invalidate_variable_locations(exec_list *ir)
{
   foreach_in_list(ir_instruction, node, ir) {
      ir_variable *const var = node->as_variable();

      if (var == NULL)
         continue;

      /* Only assign locations for variables that lack an explicit location.
       * Explicit locations are set for all built-in variables, generic vertex
       * shader inputs (via layout(location=...)), and generic fragment shader
       * outputs (also via layout(location=...)).
       */
      if (!var->data.explicit_location) {
         var->data.location = -1;
         var->data.location_frac = 0;
      }
   }
}


/**
 * Set clip_distance_array_size based and cull_distance_array_size on the given
 * shader.
 *
 * Also check for errors based on incorrect usage of gl_ClipVertex and
 * gl_ClipDistance and gl_CullDistance.
 * Additionally test whether the arrays gl_ClipDistance and gl_CullDistance
 * exceed the maximum size defined by gl_MaxCombinedClipAndCullDistances.
 *
 * Return false if an error was reported.
 */
static void
analyze_clip_cull_usage(struct gl_shader_program *prog,
                        struct gl_linked_shader *shader,
                        const struct gl_constants *consts,
                        struct shader_info *info)
{
   if (consts->DoDCEBeforeClipCullAnalysis) {
      /* Remove dead functions to avoid raising an error (eg: dead function
       * writes to gl_ClipVertex, and main() writes to gl_ClipDistance).
       */
      do_dead_functions(shader->ir);
   }

   info->clip_distance_array_size = 0;
   info->cull_distance_array_size = 0;

   if (prog->GLSL_Version >= (prog->IsES ? 300 : 130)) {
      /* From section 7.1 (Vertex Shader Special Variables) of the
       * GLSL 1.30 spec:
       *
       *   "It is an error for a shader to statically write both
       *   gl_ClipVertex and gl_ClipDistance."
       *
       * This does not apply to GLSL ES shaders, since GLSL ES defines neither
       * gl_ClipVertex nor gl_ClipDistance. However with
       * GL_EXT_clip_cull_distance, this functionality is exposed in ES 3.0.
       */
      find_variable gl_ClipDistance("gl_ClipDistance");
      find_variable gl_CullDistance("gl_CullDistance");
      find_variable gl_ClipVertex("gl_ClipVertex");
      find_variable * const variables[] = {
         &gl_ClipDistance,
         &gl_CullDistance,
         !prog->IsES ? &gl_ClipVertex : NULL,
         NULL
      };
      find_assignments(shader->ir, variables);

      /* From the ARB_cull_distance spec:
       *
       * It is a compile-time or link-time error for the set of shaders forming
       * a program to statically read or write both gl_ClipVertex and either
       * gl_ClipDistance or gl_CullDistance.
       *
       * This does not apply to GLSL ES shaders, since GLSL ES doesn't define
       * gl_ClipVertex.
       */
      if (!prog->IsES) {
         if (gl_ClipVertex.found && gl_ClipDistance.found) {
            linker_error(prog, "%s shader writes to both `gl_ClipVertex' "
                         "and `gl_ClipDistance'\n",
                         _mesa_shader_stage_to_string(shader->Stage));
            return;
         }
         if (gl_ClipVertex.found && gl_CullDistance.found) {
            linker_error(prog, "%s shader writes to both `gl_ClipVertex' "
                         "and `gl_CullDistance'\n",
                         _mesa_shader_stage_to_string(shader->Stage));
            return;
         }
      }

      if (gl_ClipDistance.found) {
         ir_variable *clip_distance_var =
                shader->symbols->get_variable("gl_ClipDistance");
         assert(clip_distance_var);
         info->clip_distance_array_size = clip_distance_var->type->length;
      }
      if (gl_CullDistance.found) {
         ir_variable *cull_distance_var =
                shader->symbols->get_variable("gl_CullDistance");
         assert(cull_distance_var);
         info->cull_distance_array_size = cull_distance_var->type->length;
      }
      /* From the ARB_cull_distance spec:
       *
       * It is a compile-time or link-time error for the set of shaders forming
       * a program to have the sum of the sizes of the gl_ClipDistance and
       * gl_CullDistance arrays to be larger than
       * gl_MaxCombinedClipAndCullDistances.
       */
      if ((uint32_t)(info->clip_distance_array_size + info->cull_distance_array_size) >
          consts->MaxClipPlanes) {
          linker_error(prog, "%s shader: the combined size of "
                       "'gl_ClipDistance' and 'gl_CullDistance' size cannot "
                       "be larger than "
                       "gl_MaxCombinedClipAndCullDistances (%u)",
                       _mesa_shader_stage_to_string(shader->Stage),
                       consts->MaxClipPlanes);
      }
   }
}


/**
 * Verify that a vertex shader executable meets all semantic requirements.
 *
 * Also sets info.clip_distance_array_size and
 * info.cull_distance_array_size as a side effect.
 *
 * \param shader  Vertex shader executable to be verified
 */
static void
validate_vertex_shader_executable(struct gl_shader_program *prog,
                                  struct gl_linked_shader *shader,
                                  const struct gl_constants *consts)
{
   if (shader == NULL)
      return;

   /* From the GLSL 1.10 spec, page 48:
    *
    *     "The variable gl_Position is available only in the vertex
    *      language and is intended for writing the homogeneous vertex
    *      position. All executions of a well-formed vertex shader
    *      executable must write a value into this variable. [...] The
    *      variable gl_Position is available only in the vertex
    *      language and is intended for writing the homogeneous vertex
    *      position. All executions of a well-formed vertex shader
    *      executable must write a value into this variable."
    *
    * while in GLSL 1.40 this text is changed to:
    *
    *     "The variable gl_Position is available only in the vertex
    *      language and is intended for writing the homogeneous vertex
    *      position. It can be written at any time during shader
    *      execution. It may also be read back by a vertex shader
    *      after being written. This value will be used by primitive
    *      assembly, clipping, culling, and other fixed functionality
    *      operations, if present, that operate on primitives after
    *      vertex processing has occurred. Its value is undefined if
    *      the vertex shader executable does not write gl_Position."
    *
    * All GLSL ES Versions are similar to GLSL 1.40--failing to write to
    * gl_Position is not an error.
    */
   if (prog->GLSL_Version < (prog->IsES ? 300 : 140)) {
      find_variable gl_Position("gl_Position");
      find_assignments(shader->ir, &gl_Position);
      if (!gl_Position.found) {
        if (prog->IsES) {
          linker_warning(prog,
                         "vertex shader does not write to `gl_Position'. "
                         "Its value is undefined. \n");
        } else {
          linker_error(prog,
                       "vertex shader does not write to `gl_Position'. \n");
        }
         return;
      }
   }

   analyze_clip_cull_usage(prog, shader, consts, &shader->Program->info);
}

static void
validate_tess_eval_shader_executable(struct gl_shader_program *prog,
                                     struct gl_linked_shader *shader,
                                     const struct gl_constants *consts)
{
   if (shader == NULL)
      return;

   analyze_clip_cull_usage(prog, shader, consts, &shader->Program->info);
}


/**
 * Verify that a fragment shader executable meets all semantic requirements
 *
 * \param shader  Fragment shader executable to be verified
 */
static void
validate_fragment_shader_executable(struct gl_shader_program *prog,
                                    struct gl_linked_shader *shader)
{
   if (shader == NULL)
      return;

   find_variable gl_FragColor("gl_FragColor");
   find_variable gl_FragData("gl_FragData");
   find_variable * const variables[] = { &gl_FragColor, &gl_FragData, NULL };
   find_assignments(shader->ir, variables);

   if (gl_FragColor.found && gl_FragData.found) {
      linker_error(prog,  "fragment shader writes to both "
                   "`gl_FragColor' and `gl_FragData'\n");
   }
}

/**
 * Verify that a geometry shader executable meets all semantic requirements
 *
 * Also sets prog->Geom.VerticesIn, and info.clip_distance_array_sizeand
 * info.cull_distance_array_size as a side effect.
 *
 * \param shader Geometry shader executable to be verified
 */
static void
validate_geometry_shader_executable(struct gl_shader_program *prog,
                                    struct gl_linked_shader *shader,
                                    const struct gl_constants *consts)
{
   if (shader == NULL)
      return;

   unsigned num_vertices =
      vertices_per_prim(shader->Program->info.gs.input_primitive);
   prog->Geom.VerticesIn = num_vertices;

   analyze_clip_cull_usage(prog, shader, consts, &shader->Program->info);
}

/**
 * Check if geometry shaders emit to non-zero streams and do corresponding
 * validations.
 */
static void
validate_geometry_shader_emissions(const struct gl_constants *consts,
                                   struct gl_shader_program *prog)
{
   struct gl_linked_shader *sh = prog->_LinkedShaders[MESA_SHADER_GEOMETRY];

   if (sh != NULL) {
      find_emit_vertex_visitor emit_vertex(consts->MaxVertexStreams - 1);
      emit_vertex.run(sh->ir);
      if (emit_vertex.error()) {
         linker_error(prog, "Invalid call %s(%d). Accepted values for the "
                      "stream parameter are in the range [0, %d].\n",
                      emit_vertex.error_func(),
                      emit_vertex.error_stream(),
                      consts->MaxVertexStreams - 1);
      }
      prog->Geom.ActiveStreamMask = emit_vertex.active_stream_mask();
      prog->Geom.UsesEndPrimitive = emit_vertex.uses_end_primitive();

      /* From the ARB_gpu_shader5 spec:
       *
       *   "Multiple vertex streams are supported only if the output primitive
       *    type is declared to be "points".  A program will fail to link if it
       *    contains a geometry shader calling EmitStreamVertex() or
       *    EndStreamPrimitive() if its output primitive type is not "points".
       *
       * However, in the same spec:
       *
       *   "The function EmitVertex() is equivalent to calling EmitStreamVertex()
       *    with <stream> set to zero."
       *
       * And:
       *
       *   "The function EndPrimitive() is equivalent to calling
       *    EndStreamPrimitive() with <stream> set to zero."
       *
       * Since we can call EmitVertex() and EndPrimitive() when we output
       * primitives other than points, calling EmitStreamVertex(0) or
       * EmitEndPrimitive(0) should not produce errors. This it also what Nvidia
       * does. We can use prog->Geom.ActiveStreamMask to check whether only the
       * first (zero) stream is active.
       * stream.
       */
      if (prog->Geom.ActiveStreamMask & ~(1 << 0) &&
          sh->Program->info.gs.output_primitive != GL_POINTS) {
         linker_error(prog, "EmitStreamVertex(n) and EndStreamPrimitive(n) "
                      "with n>0 requires point output\n");
      }
   }
}

bool
validate_intrastage_arrays(struct gl_shader_program *prog,
                           ir_variable *const var,
                           ir_variable *const existing,
                           bool match_precision)
{
   /* Consider the types to be "the same" if both types are arrays
    * of the same type and one of the arrays is implicitly sized.
    * In addition, set the type of the linked variable to the
    * explicitly sized array.
    */
   if (var->type->is_array() && existing->type->is_array()) {
      const glsl_type *no_array_var = var->type->fields.array;
      const glsl_type *no_array_existing = existing->type->fields.array;
      bool type_matches;

      type_matches = (match_precision ?
                      no_array_var == no_array_existing :
                      no_array_var->compare_no_precision(no_array_existing));

      if (type_matches &&
          ((var->type->length == 0)|| (existing->type->length == 0))) {
         if (var->type->length != 0) {
            if ((int)var->type->length <= existing->data.max_array_access) {
               linker_error(prog, "%s `%s' declared as type "
                           "`%s' but outermost dimension has an index"
                           " of `%i'\n",
                           mode_string(var),
                           var->name, var->type->name,
                           existing->data.max_array_access);
            }
            existing->type = var->type;
            return true;
         } else if (existing->type->length != 0) {
            if((int)existing->type->length <= var->data.max_array_access &&
               !existing->data.from_ssbo_unsized_array) {
               linker_error(prog, "%s `%s' declared as type "
                           "`%s' but outermost dimension has an index"
                           " of `%i'\n",
                           mode_string(var),
                           var->name, existing->type->name,
                           var->data.max_array_access);
            }
            return true;
         }
      }
   }
   return false;
}


/**
 * Perform validation of global variables used across multiple shaders
 */
static void
cross_validate_globals(const struct gl_constants *consts,
                       struct gl_shader_program *prog,
                       struct exec_list *ir, glsl_symbol_table *variables,
                       bool uniforms_only)
{
   foreach_in_list(ir_instruction, node, ir) {
      ir_variable *const var = node->as_variable();

      if (var == NULL)
         continue;

      if (uniforms_only && (var->data.mode != ir_var_uniform && var->data.mode != ir_var_shader_storage))
         continue;

      /* don't cross validate subroutine uniforms */
      if (var->type->contains_subroutine())
         continue;

      /* Don't cross validate interface instances. These are only relevant
       * inside a shader. The cross validation is done at the Interface Block
       * name level.
       */
      if (var->is_interface_instance())
         continue;

      /* Don't cross validate temporaries that are at global scope.  These
       * will eventually get pulled into the shaders 'main'.
       */
      if (var->data.mode == ir_var_temporary)
         continue;

      /* If a global with this name has already been seen, verify that the
       * new instance has the same type.  In addition, if the globals have
       * initializers, the values of the initializers must be the same.
       */
      ir_variable *const existing = variables->get_variable(var->name);
      if (existing != NULL) {
         /* Check if types match. */
         if (var->type != existing->type) {
            if (!validate_intrastage_arrays(prog, var, existing)) {
               /* If it is an unsized array in a Shader Storage Block,
                * two different shaders can access to different elements.
                * Because of that, they might be converted to different
                * sized arrays, then check that they are compatible but
                * ignore the array size.
                */
               if (!(var->data.mode == ir_var_shader_storage &&
                     var->data.from_ssbo_unsized_array &&
                     existing->data.mode == ir_var_shader_storage &&
                     existing->data.from_ssbo_unsized_array &&
                     var->type->gl_type == existing->type->gl_type)) {
                  linker_error(prog, "%s `%s' declared as type "
                                 "`%s' and type `%s'\n",
                                 mode_string(var),
                                 var->name, var->type->name,
                                 existing->type->name);
                  return;
               }
            }
         }

         if (var->data.explicit_location) {
            if (existing->data.explicit_location
                && (var->data.location != existing->data.location)) {
               linker_error(prog, "explicit locations for %s "
                            "`%s' have differing values\n",
                            mode_string(var), var->name);
               return;
            }

            if (var->data.location_frac != existing->data.location_frac) {
               linker_error(prog, "explicit components for %s `%s' have "
                            "differing values\n", mode_string(var), var->name);
               return;
            }

            existing->data.location = var->data.location;
            existing->data.explicit_location = true;
         } else {
            /* Check if uniform with implicit location was marked explicit
             * by earlier shader stage. If so, mark it explicit in this stage
             * too to make sure later processing does not treat it as
             * implicit one.
             */
            if (existing->data.explicit_location) {
               var->data.location = existing->data.location;
               var->data.explicit_location = true;
            }
         }

         /* From the GLSL 4.20 specification:
          * "A link error will result if two compilation units in a program
          *  specify different integer-constant bindings for the same
          *  opaque-uniform name.  However, it is not an error to specify a
          *  binding on some but not all declarations for the same name"
          */
         if (var->data.explicit_binding) {
            if (existing->data.explicit_binding &&
                var->data.binding != existing->data.binding) {
               linker_error(prog, "explicit bindings for %s "
                            "`%s' have differing values\n",
                            mode_string(var), var->name);
               return;
            }

            existing->data.binding = var->data.binding;
            existing->data.explicit_binding = true;
         }

         if (var->type->contains_atomic() &&
             var->data.offset != existing->data.offset) {
            linker_error(prog, "offset specifications for %s "
                         "`%s' have differing values\n",
                         mode_string(var), var->name);
            return;
         }

         /* Validate layout qualifiers for gl_FragDepth.
          *
          * From the AMD/ARB_conservative_depth specs:
          *
          *    "If gl_FragDepth is redeclared in any fragment shader in a
          *    program, it must be redeclared in all fragment shaders in
          *    that program that have static assignments to
          *    gl_FragDepth. All redeclarations of gl_FragDepth in all
          *    fragment shaders in a single program must have the same set
          *    of qualifiers."
          */
         if (strcmp(var->name, "gl_FragDepth") == 0) {
            bool layout_declared = var->data.depth_layout != ir_depth_layout_none;
            bool layout_differs =
               var->data.depth_layout != existing->data.depth_layout;

            if (layout_declared && layout_differs) {
               linker_error(prog,
                            "All redeclarations of gl_FragDepth in all "
                            "fragment shaders in a single program must have "
                            "the same set of qualifiers.\n");
            }

            if (var->data.used && layout_differs) {
               linker_error(prog,
                            "If gl_FragDepth is redeclared with a layout "
                            "qualifier in any fragment shader, it must be "
                            "redeclared with the same layout qualifier in "
                            "all fragment shaders that have assignments to "
                            "gl_FragDepth\n");
            }
         }

         /* Page 35 (page 41 of the PDF) of the GLSL 4.20 spec says:
          *
          *     "If a shared global has multiple initializers, the
          *     initializers must all be constant expressions, and they
          *     must all have the same value. Otherwise, a link error will
          *     result. (A shared global having only one initializer does
          *     not require that initializer to be a constant expression.)"
          *
          * Previous to 4.20 the GLSL spec simply said that initializers
          * must have the same value.  In this case of non-constant
          * initializers, this was impossible to determine.  As a result,
          * no vendor actually implemented that behavior.  The 4.20
          * behavior matches the implemented behavior of at least one other
          * vendor, so we'll implement that for all GLSL versions.
          * If (at least) one of these constant expressions is implicit,
          * because it was added by glsl_zero_init, we skip the verification.
          */
         if (var->constant_initializer != NULL) {
            if (existing->constant_initializer != NULL &&
                !existing->data.is_implicit_initializer &&
                !var->data.is_implicit_initializer) {
               if (!var->constant_initializer->has_value(existing->constant_initializer)) {
                  linker_error(prog, "initializers for %s "
                               "`%s' have differing values\n",
                               mode_string(var), var->name);
                  return;
               }
            } else {
               /* If the first-seen instance of a particular uniform did
                * not have an initializer but a later instance does,
                * replace the former with the later.
                */
               if (!var->data.is_implicit_initializer)
                  variables->replace_variable(existing->name, var);
            }
         }

         if (var->data.has_initializer) {
            if (existing->data.has_initializer
                && (var->constant_initializer == NULL
                    || existing->constant_initializer == NULL)) {
               linker_error(prog,
                            "shared global variable `%s' has multiple "
                            "non-constant initializers.\n",
                            var->name);
               return;
            }
         }

         if (existing->data.explicit_invariant != var->data.explicit_invariant) {
            linker_error(prog, "declarations for %s `%s' have "
                         "mismatching invariant qualifiers\n",
                         mode_string(var), var->name);
            return;
         }
         if (existing->data.centroid != var->data.centroid) {
            linker_error(prog, "declarations for %s `%s' have "
                         "mismatching centroid qualifiers\n",
                         mode_string(var), var->name);
            return;
         }
         if (existing->data.sample != var->data.sample) {
            linker_error(prog, "declarations for %s `%s` have "
                         "mismatching sample qualifiers\n",
                         mode_string(var), var->name);
            return;
         }
         if (existing->data.image_format != var->data.image_format) {
            linker_error(prog, "declarations for %s `%s` have "
                         "mismatching image format qualifiers\n",
                         mode_string(var), var->name);
            return;
         }

         /* Check the precision qualifier matches for uniform variables on
          * GLSL ES.
          */
         if (!consts->AllowGLSLRelaxedES &&
             prog->IsES && !var->get_interface_type() &&
             existing->data.precision != var->data.precision) {
            if ((existing->data.used && var->data.used) ||
                prog->GLSL_Version >= 300) {
               linker_error(prog, "declarations for %s `%s` have "
                            "mismatching precision qualifiers\n",
                            mode_string(var), var->name);
               return;
            } else {
               linker_warning(prog, "declarations for %s `%s` have "
                              "mismatching precision qualifiers\n",
                              mode_string(var), var->name);
            }
         }

         /* In OpenGL GLSL 3.20 spec, section 4.3.9:
          *
          *   "It is a link-time error if any particular shader interface
          *    contains:
          *
          *    - two different blocks, each having no instance name, and each
          *      having a member of the same name, or
          *
          *    - a variable outside a block, and a block with no instance name,
          *      where the variable has the same name as a member in the block."
          */
         const glsl_type *var_itype = var->get_interface_type();
         const glsl_type *existing_itype = existing->get_interface_type();
         if (var_itype != existing_itype) {
            if (!var_itype || !existing_itype) {
               linker_error(prog, "declarations for %s `%s` are inside block "
                            "`%s` and outside a block",
                            mode_string(var), var->name,
                            var_itype ? var_itype->name : existing_itype->name);
               return;
            } else if (strcmp(var_itype->name, existing_itype->name) != 0) {
               linker_error(prog, "declarations for %s `%s` are inside blocks "
                            "`%s` and `%s`",
                            mode_string(var), var->name,
                            existing_itype->name,
                            var_itype->name);
               return;
            }
         }
      } else
         variables->add_variable(var);
   }
}


/**
 * Perform validation of uniforms used across multiple shader stages
 */
static void
cross_validate_uniforms(const struct gl_constants *consts,
                        struct gl_shader_program *prog)
{
   glsl_symbol_table variables;
   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      if (prog->_LinkedShaders[i] == NULL)
         continue;

      cross_validate_globals(consts, prog, prog->_LinkedShaders[i]->ir,
                             &variables, true);
   }
}

/**
 * Accumulates the array of buffer blocks and checks that all definitions of
 * blocks agree on their contents.
 */
static bool
interstage_cross_validate_uniform_blocks(struct gl_shader_program *prog,
                                         bool validate_ssbo)
{
   int *ifc_blk_stage_idx[MESA_SHADER_STAGES];
   struct gl_uniform_block *blks = NULL;
   unsigned *num_blks = validate_ssbo ? &prog->data->NumShaderStorageBlocks :
      &prog->data->NumUniformBlocks;

   unsigned max_num_buffer_blocks = 0;
   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      if (prog->_LinkedShaders[i]) {
         if (validate_ssbo) {
            max_num_buffer_blocks +=
               prog->_LinkedShaders[i]->Program->info.num_ssbos;
         } else {
            max_num_buffer_blocks +=
               prog->_LinkedShaders[i]->Program->info.num_ubos;
         }
      }
   }

   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      struct gl_linked_shader *sh = prog->_LinkedShaders[i];

      ifc_blk_stage_idx[i] =
         (int *) malloc(sizeof(int) * max_num_buffer_blocks);
      for (unsigned int j = 0; j < max_num_buffer_blocks; j++)
         ifc_blk_stage_idx[i][j] = -1;

      if (sh == NULL)
         continue;

      unsigned sh_num_blocks;
      struct gl_uniform_block **sh_blks;
      if (validate_ssbo) {
         sh_num_blocks = prog->_LinkedShaders[i]->Program->info.num_ssbos;
         sh_blks = sh->Program->sh.ShaderStorageBlocks;
      } else {
         sh_num_blocks = prog->_LinkedShaders[i]->Program->info.num_ubos;
         sh_blks = sh->Program->sh.UniformBlocks;
      }

      for (unsigned int j = 0; j < sh_num_blocks; j++) {
         int index = link_cross_validate_uniform_block(prog->data, &blks,
                                                       num_blks, sh_blks[j]);

         if (index == -1) {
            linker_error(prog, "buffer block `%s' has mismatching "
                         "definitions\n", sh_blks[j]->name.string);

            for (unsigned k = 0; k <= i; k++) {
               free(ifc_blk_stage_idx[k]);
            }

            /* Reset the block count. This will help avoid various segfaults
             * from api calls that assume the array exists due to the count
             * being non-zero.
             */
            *num_blks = 0;
            return false;
         }

         ifc_blk_stage_idx[i][index] = j;
      }
   }

   /* Update per stage block pointers to point to the program list.
    * FIXME: We should be able to free the per stage blocks here.
    */
   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      for (unsigned j = 0; j < *num_blks; j++) {
         int stage_index = ifc_blk_stage_idx[i][j];

         if (stage_index != -1) {
            struct gl_linked_shader *sh = prog->_LinkedShaders[i];

            struct gl_uniform_block **sh_blks = validate_ssbo ?
               sh->Program->sh.ShaderStorageBlocks :
               sh->Program->sh.UniformBlocks;

            blks[j].stageref |= sh_blks[stage_index]->stageref;
            sh_blks[stage_index] = &blks[j];
         }
      }
   }

   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      free(ifc_blk_stage_idx[i]);
   }

   if (validate_ssbo)
      prog->data->ShaderStorageBlocks = blks;
   else
      prog->data->UniformBlocks = blks;

   return true;
}

/**
 * Verifies the invariance of built-in special variables.
 */
static bool
validate_invariant_builtins(struct gl_shader_program *prog,
                            const gl_linked_shader *vert,
                            const gl_linked_shader *frag)
{
   const ir_variable *var_vert;
   const ir_variable *var_frag;

   if (!vert || !frag)
      return true;

   /*
    * From OpenGL ES Shading Language 1.0 specification
    * (4.6.4 Invariance and Linkage):
    *     "The invariance of varyings that are declared in both the vertex and
    *     fragment shaders must match. For the built-in special variables,
    *     gl_FragCoord can only be declared invariant if and only if
    *     gl_Position is declared invariant. Similarly gl_PointCoord can only
    *     be declared invariant if and only if gl_PointSize is declared
    *     invariant. It is an error to declare gl_FrontFacing as invariant.
    *     The invariance of gl_FrontFacing is the same as the invariance of
    *     gl_Position."
    */
   var_frag = frag->symbols->get_variable("gl_FragCoord");
   if (var_frag && var_frag->data.invariant) {
      var_vert = vert->symbols->get_variable("gl_Position");
      if (var_vert && !var_vert->data.invariant) {
         linker_error(prog,
               "fragment shader built-in `%s' has invariant qualifier, "
               "but vertex shader built-in `%s' lacks invariant qualifier\n",
               var_frag->name, var_vert->name);
         return false;
      }
   }

   var_frag = frag->symbols->get_variable("gl_PointCoord");
   if (var_frag && var_frag->data.invariant) {
      var_vert = vert->symbols->get_variable("gl_PointSize");
      if (var_vert && !var_vert->data.invariant) {
         linker_error(prog,
               "fragment shader built-in `%s' has invariant qualifier, "
               "but vertex shader built-in `%s' lacks invariant qualifier\n",
               var_frag->name, var_vert->name);
         return false;
      }
   }

   var_frag = frag->symbols->get_variable("gl_FrontFacing");
   if (var_frag && var_frag->data.invariant) {
      linker_error(prog,
            "fragment shader built-in `%s' can not be declared as invariant\n",
            var_frag->name);
      return false;
   }

   return true;
}

/**
 * Populates a shaders symbol table with all global declarations
 */
static void
populate_symbol_table(gl_linked_shader *sh, glsl_symbol_table *symbols)
{
   sh->symbols = new(sh) glsl_symbol_table;

   _mesa_glsl_copy_symbols_from_table(sh->ir, symbols, sh->symbols);
}


/**
 * Remap variables referenced in an instruction tree
 *
 * This is used when instruction trees are cloned from one shader and placed in
 * another.  These trees will contain references to \c ir_variable nodes that
 * do not exist in the target shader.  This function finds these \c ir_variable
 * references and replaces the references with matching variables in the target
 * shader.
 *
 * If there is no matching variable in the target shader, a clone of the
 * \c ir_variable is made and added to the target shader.  The new variable is
 * added to \b both the instruction stream and the symbol table.
 *
 * \param inst         IR tree that is to be processed.
 * \param symbols      Symbol table containing global scope symbols in the
 *                     linked shader.
 * \param instructions Instruction stream where new variable declarations
 *                     should be added.
 */
static void
remap_variables(ir_instruction *inst, struct gl_linked_shader *target,
                hash_table *temps)
{
   class remap_visitor : public ir_hierarchical_visitor {
   public:
         remap_visitor(struct gl_linked_shader *target, hash_table *temps)
      {
         this->target = target;
         this->symbols = target->symbols;
         this->instructions = target->ir;
         this->temps = temps;
      }

      virtual ir_visitor_status visit(ir_dereference_variable *ir)
      {
         if (ir->var->data.mode == ir_var_temporary) {
            hash_entry *entry = _mesa_hash_table_search(temps, ir->var);
            ir_variable *var = entry ? (ir_variable *) entry->data : NULL;

            assert(var != NULL);
            ir->var = var;
            return visit_continue;
         }

         ir_variable *const existing =
            this->symbols->get_variable(ir->var->name);
         if (existing != NULL)
            ir->var = existing;
         else {
            ir_variable *copy = ir->var->clone(this->target, NULL);

            this->symbols->add_variable(copy);
            this->instructions->push_head(copy);
            ir->var = copy;
         }

         return visit_continue;
      }

   private:
      struct gl_linked_shader *target;
      glsl_symbol_table *symbols;
      exec_list *instructions;
      hash_table *temps;
   };

   remap_visitor v(target, temps);

   inst->accept(&v);
}


/**
 * Move non-declarations from one instruction stream to another
 *
 * The intended usage pattern of this function is to pass the pointer to the
 * head sentinel of a list (i.e., a pointer to the list cast to an \c exec_node
 * pointer) for \c last and \c false for \c make_copies on the first
 * call.  Successive calls pass the return value of the previous call for
 * \c last and \c true for \c make_copies.
 *
 * \param instructions Source instruction stream
 * \param last         Instruction after which new instructions should be
 *                     inserted in the target instruction stream
 * \param make_copies  Flag selecting whether instructions in \c instructions
 *                     should be copied (via \c ir_instruction::clone) into the
 *                     target list or moved.
 *
 * \return
 * The new "last" instruction in the target instruction stream.  This pointer
 * is suitable for use as the \c last parameter of a later call to this
 * function.
 */
static exec_node *
move_non_declarations(exec_list *instructions, exec_node *last,
                      bool make_copies, gl_linked_shader *target)
{
   hash_table *temps = NULL;

   if (make_copies)
      temps = _mesa_pointer_hash_table_create(NULL);

   foreach_in_list_safe(ir_instruction, inst, instructions) {
      if (inst->as_function())
         continue;

      ir_variable *var = inst->as_variable();
      if ((var != NULL) && (var->data.mode != ir_var_temporary))
         continue;

      assert(inst->as_assignment()
             || inst->as_call()
             || inst->as_if() /* for initializers with the ?: operator */
             || ((var != NULL) && (var->data.mode == ir_var_temporary)));

      if (make_copies) {
         inst = inst->clone(target, NULL);

         if (var != NULL)
            _mesa_hash_table_insert(temps, var, inst);
         else
            remap_variables(inst, target, temps);
      } else {
         inst->remove();
      }

      last->insert_after(inst);
      last = inst;
   }

   if (make_copies)
      _mesa_hash_table_destroy(temps, NULL);

   return last;
}


/**
 * This class is only used in link_intrastage_shaders() below but declaring
 * it inside that function leads to compiler warnings with some versions of
 * gcc.
 */
class array_sizing_visitor : public deref_type_updater {
public:
   using deref_type_updater::visit;

   array_sizing_visitor()
      : mem_ctx(ralloc_context(NULL)),
        unnamed_interfaces(_mesa_pointer_hash_table_create(NULL))
   {
   }

   ~array_sizing_visitor()
   {
      _mesa_hash_table_destroy(this->unnamed_interfaces, NULL);
      ralloc_free(this->mem_ctx);
   }

   virtual ir_visitor_status visit(ir_variable *var)
   {
      const glsl_type *type_without_array;
      bool implicit_sized_array = var->data.implicit_sized_array;
      fixup_type(&var->type, var->data.max_array_access,
                 var->data.from_ssbo_unsized_array,
                 &implicit_sized_array);
      var->data.implicit_sized_array = implicit_sized_array;
      type_without_array = var->type->without_array();
      if (var->type->is_interface()) {
         if (interface_contains_unsized_arrays(var->type)) {
            const glsl_type *new_type =
               resize_interface_members(var->type,
                                        var->get_max_ifc_array_access(),
                                        var->is_in_shader_storage_block());
            var->type = new_type;
            var->change_interface_type(new_type);
         }
      } else if (type_without_array->is_interface()) {
         if (interface_contains_unsized_arrays(type_without_array)) {
            const glsl_type *new_type =
               resize_interface_members(type_without_array,
                                        var->get_max_ifc_array_access(),
                                        var->is_in_shader_storage_block());
            var->change_interface_type(new_type);
            var->type = update_interface_members_array(var->type, new_type);
         }
      } else if (const glsl_type *ifc_type = var->get_interface_type()) {
         /* Store a pointer to the variable in the unnamed_interfaces
          * hashtable.
          */
         hash_entry *entry =
               _mesa_hash_table_search(this->unnamed_interfaces,
                                       ifc_type);

         ir_variable **interface_vars = entry ? (ir_variable **) entry->data : NULL;

         if (interface_vars == NULL) {
            interface_vars = rzalloc_array(mem_ctx, ir_variable *,
                                           ifc_type->length);
            _mesa_hash_table_insert(this->unnamed_interfaces, ifc_type,
                                    interface_vars);
         }
         unsigned index = ifc_type->field_index(var->name);
         assert(index < ifc_type->length);
         assert(interface_vars[index] == NULL);
         interface_vars[index] = var;
      }
      return visit_continue;
   }

   /**
    * For each unnamed interface block that was discovered while running the
    * visitor, adjust the interface type to reflect the newly assigned array
    * sizes, and fix up the ir_variable nodes to point to the new interface
    * type.
    */
   void fixup_unnamed_interface_types()
   {
      hash_table_call_foreach(this->unnamed_interfaces,
                              fixup_unnamed_interface_type, NULL);
   }

private:
   /**
    * If the type pointed to by \c type represents an unsized array, replace
    * it with a sized array whose size is determined by max_array_access.
    */
   static void fixup_type(const glsl_type **type, unsigned max_array_access,
                          bool from_ssbo_unsized_array, bool *implicit_sized)
   {
      if (!from_ssbo_unsized_array && (*type)->is_unsized_array()) {
         *type = glsl_type::get_array_instance((*type)->fields.array,
                                               max_array_access + 1);
         *implicit_sized = true;
         assert(*type != NULL);
      }
   }

   static const glsl_type *
   update_interface_members_array(const glsl_type *type,
                                  const glsl_type *new_interface_type)
   {
      const glsl_type *element_type = type->fields.array;
      if (element_type->is_array()) {
         const glsl_type *new_array_type =
            update_interface_members_array(element_type, new_interface_type);
         return glsl_type::get_array_instance(new_array_type, type->length);
      } else {
         return glsl_type::get_array_instance(new_interface_type,
                                              type->length);
      }
   }

   /**
    * Determine whether the given interface type contains unsized arrays (if
    * it doesn't, array_sizing_visitor doesn't need to process it).
    */
   static bool interface_contains_unsized_arrays(const glsl_type *type)
   {
      for (unsigned i = 0; i < type->length; i++) {
         const glsl_type *elem_type = type->fields.structure[i].type;
         if (elem_type->is_unsized_array())
            return true;
      }
      return false;
   }

   /**
    * Create a new interface type based on the given type, with unsized arrays
    * replaced by sized arrays whose size is determined by
    * max_ifc_array_access.
    */
   static const glsl_type *
   resize_interface_members(const glsl_type *type,
                            const int *max_ifc_array_access,
                            bool is_ssbo)
   {
      unsigned num_fields = type->length;
      glsl_struct_field *fields = new glsl_struct_field[num_fields];
      memcpy(fields, type->fields.structure,
             num_fields * sizeof(*fields));
      for (unsigned i = 0; i < num_fields; i++) {
         bool implicit_sized_array = fields[i].implicit_sized_array;
         /* If SSBO last member is unsized array, we don't replace it by a sized
          * array.
          */
         if (is_ssbo && i == (num_fields - 1))
            fixup_type(&fields[i].type, max_ifc_array_access[i],
                       true, &implicit_sized_array);
         else
            fixup_type(&fields[i].type, max_ifc_array_access[i],
                       false, &implicit_sized_array);
         fields[i].implicit_sized_array = implicit_sized_array;
      }
      glsl_interface_packing packing =
         (glsl_interface_packing) type->interface_packing;
      bool row_major = (bool) type->interface_row_major;
      const glsl_type *new_ifc_type =
         glsl_type::get_interface_instance(fields, num_fields,
                                           packing, row_major, type->name);
      delete [] fields;
      return new_ifc_type;
   }

   static void fixup_unnamed_interface_type(const void *key, void *data,
                                            void *)
   {
      const glsl_type *ifc_type = (const glsl_type *) key;
      ir_variable **interface_vars = (ir_variable **) data;
      unsigned num_fields = ifc_type->length;
      glsl_struct_field *fields = new glsl_struct_field[num_fields];
      memcpy(fields, ifc_type->fields.structure,
             num_fields * sizeof(*fields));
      bool interface_type_changed = false;
      for (unsigned i = 0; i < num_fields; i++) {
         if (interface_vars[i] != NULL &&
             fields[i].type != interface_vars[i]->type) {
            fields[i].type = interface_vars[i]->type;
            interface_type_changed = true;
         }
      }
      if (!interface_type_changed) {
         delete [] fields;
         return;
      }
      glsl_interface_packing packing =
         (glsl_interface_packing) ifc_type->interface_packing;
      bool row_major = (bool) ifc_type->interface_row_major;
      const glsl_type *new_ifc_type =
         glsl_type::get_interface_instance(fields, num_fields, packing,
                                           row_major, ifc_type->name);
      delete [] fields;
      for (unsigned i = 0; i < num_fields; i++) {
         if (interface_vars[i] != NULL)
            interface_vars[i]->change_interface_type(new_ifc_type);
      }
   }

   /**
    * Memory context used to allocate the data in \c unnamed_interfaces.
    */
   void *mem_ctx;

   /**
    * Hash table from const glsl_type * to an array of ir_variable *'s
    * pointing to the ir_variables constituting each unnamed interface block.
    */
   hash_table *unnamed_interfaces;
};

static bool
validate_xfb_buffer_stride(const struct gl_constants *consts, unsigned idx,
                           struct gl_shader_program *prog)
{
   /* We will validate doubles at a later stage */
   if (prog->TransformFeedback.BufferStride[idx] % 4) {
      linker_error(prog, "invalid qualifier xfb_stride=%d must be a "
                   "multiple of 4 or if its applied to a type that is "
                   "or contains a double a multiple of 8.",
                   prog->TransformFeedback.BufferStride[idx]);
      return false;
   }

   if (prog->TransformFeedback.BufferStride[idx] / 4 >
       consts->MaxTransformFeedbackInterleavedComponents) {
      linker_error(prog, "The MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS "
                   "limit has been exceeded.");
      return false;
   }

   return true;
}

/**
 * Check for conflicting xfb_stride default qualifiers and store buffer stride
 * for later use.
 */
static void
link_xfb_stride_layout_qualifiers(const struct gl_constants *consts,
                                  struct gl_shader_program *prog,
                                  struct gl_shader **shader_list,
                                  unsigned num_shaders)
{
   for (unsigned i = 0; i < MAX_FEEDBACK_BUFFERS; i++) {
      prog->TransformFeedback.BufferStride[i] = 0;
   }

   for (unsigned i = 0; i < num_shaders; i++) {
      struct gl_shader *shader = shader_list[i];

      for (unsigned j = 0; j < MAX_FEEDBACK_BUFFERS; j++) {
         if (shader->TransformFeedbackBufferStride[j]) {
            if (prog->TransformFeedback.BufferStride[j] == 0) {
               prog->TransformFeedback.BufferStride[j] =
                  shader->TransformFeedbackBufferStride[j];
               if (!validate_xfb_buffer_stride(consts, j, prog))
                  return;
            } else if (prog->TransformFeedback.BufferStride[j] !=
                       shader->TransformFeedbackBufferStride[j]){
               linker_error(prog,
                            "intrastage shaders defined with conflicting "
                            "xfb_stride for buffer %d (%d and %d)\n", j,
                            prog->TransformFeedback.BufferStride[j],
                            shader->TransformFeedbackBufferStride[j]);
               return;
            }
         }
      }
   }
}

/**
 * Check for conflicting bindless/bound sampler/image layout qualifiers at
 * global scope.
 */
static void
link_bindless_layout_qualifiers(struct gl_shader_program *prog,
                                struct gl_shader **shader_list,
                                unsigned num_shaders)
{
   bool bindless_sampler, bindless_image;
   bool bound_sampler, bound_image;

   bindless_sampler = bindless_image = false;
   bound_sampler = bound_image = false;

   for (unsigned i = 0; i < num_shaders; i++) {
      struct gl_shader *shader = shader_list[i];

      if (shader->bindless_sampler)
         bindless_sampler = true;
      if (shader->bindless_image)
         bindless_image = true;
      if (shader->bound_sampler)
         bound_sampler = true;
      if (shader->bound_image)
         bound_image = true;

      if ((bindless_sampler && bound_sampler) ||
          (bindless_image && bound_image)) {
         /* From section 4.4.6 of the ARB_bindless_texture spec:
          *
          *     "If both bindless_sampler and bound_sampler, or bindless_image
          *      and bound_image, are declared at global scope in any
          *      compilation unit, a link- time error will be generated."
          */
         linker_error(prog, "both bindless_sampler and bound_sampler, or "
                      "bindless_image and bound_image, can't be declared at "
                      "global scope");
      }
   }
}

/**
 * Check for conflicting viewport_relative settings across shaders, and sets
 * the value for the linked shader.
 */
static void
link_layer_viewport_relative_qualifier(struct gl_shader_program *prog,
                                       struct gl_program *gl_prog,
                                       struct gl_shader **shader_list,
                                       unsigned num_shaders)
{
   unsigned i;

   /* Find first shader with explicit layer declaration */
   for (i = 0; i < num_shaders; i++) {
      if (shader_list[i]->redeclares_gl_layer) {
         gl_prog->info.layer_viewport_relative =
            shader_list[i]->layer_viewport_relative;
         break;
      }
   }

   /* Now make sure that each subsequent shader's explicit layer declaration
    * matches the first one's.
    */
   for (; i < num_shaders; i++) {
      if (shader_list[i]->redeclares_gl_layer &&
          shader_list[i]->layer_viewport_relative !=
          gl_prog->info.layer_viewport_relative) {
         linker_error(prog, "all gl_Layer redeclarations must have identical "
                      "viewport_relative settings");
      }
   }
}

/**
 * Performs the cross-validation of tessellation control shader vertices and
 * layout qualifiers for the attached tessellation control shaders,
 * and propagates them to the linked TCS and linked shader program.
 */
static void
link_tcs_out_layout_qualifiers(struct gl_shader_program *prog,
                               struct gl_program *gl_prog,
                               struct gl_shader **shader_list,
                               unsigned num_shaders)
{
   if (gl_prog->info.stage != MESA_SHADER_TESS_CTRL)
      return;

   gl_prog->info.tess.tcs_vertices_out = 0;

   /* From the GLSL 4.0 spec (chapter 4.3.8.2):
    *
    *     "All tessellation control shader layout declarations in a program
    *      must specify the same output patch vertex count.  There must be at
    *      least one layout qualifier specifying an output patch vertex count
    *      in any program containing tessellation control shaders; however,
    *      such a declaration is not required in all tessellation control
    *      shaders."
    */

   for (unsigned i = 0; i < num_shaders; i++) {
      struct gl_shader *shader = shader_list[i];

      if (shader->info.TessCtrl.VerticesOut != 0) {
         if (gl_prog->info.tess.tcs_vertices_out != 0 &&
             gl_prog->info.tess.tcs_vertices_out !=
             (unsigned) shader->info.TessCtrl.VerticesOut) {
            linker_error(prog, "tessellation control shader defined with "
                         "conflicting output vertex count (%d and %d)\n",
                         gl_prog->info.tess.tcs_vertices_out,
                         shader->info.TessCtrl.VerticesOut);
            return;
         }
         gl_prog->info.tess.tcs_vertices_out =
            shader->info.TessCtrl.VerticesOut;
      }
   }

   /* Just do the intrastage -> interstage propagation right now,
    * since we already know we're in the right type of shader program
    * for doing it.
    */
   if (gl_prog->info.tess.tcs_vertices_out == 0) {
      linker_error(prog, "tessellation control shader didn't declare "
                   "vertices out layout qualifier\n");
      return;
   }
}


/**
 * Performs the cross-validation of tessellation evaluation shader
 * primitive type, vertex spacing, ordering and point_mode layout qualifiers
 * for the attached tessellation evaluation shaders, and propagates them
 * to the linked TES and linked shader program.
 */
static void
link_tes_in_layout_qualifiers(struct gl_shader_program *prog,
                              struct gl_program *gl_prog,
                              struct gl_shader **shader_list,
                              unsigned num_shaders)
{
   if (gl_prog->info.stage != MESA_SHADER_TESS_EVAL)
      return;

   int point_mode = -1;
   unsigned vertex_order = 0;

   gl_prog->info.tess._primitive_mode = TESS_PRIMITIVE_UNSPECIFIED;
   gl_prog->info.tess.spacing = TESS_SPACING_UNSPECIFIED;

   /* From the GLSL 4.0 spec (chapter 4.3.8.1):
    *
    *     "At least one tessellation evaluation shader (compilation unit) in
    *      a program must declare a primitive mode in its input layout.
    *      Declaration vertex spacing, ordering, and point mode identifiers is
    *      optional.  It is not required that all tessellation evaluation
    *      shaders in a program declare a primitive mode.  If spacing or
    *      vertex ordering declarations are omitted, the tessellation
    *      primitive generator will use equal spacing or counter-clockwise
    *      vertex ordering, respectively.  If a point mode declaration is
    *      omitted, the tessellation primitive generator will produce lines or
    *      triangles according to the primitive mode."
    */

   for (unsigned i = 0; i < num_shaders; i++) {
      struct gl_shader *shader = shader_list[i];

      if (shader->info.TessEval._PrimitiveMode != TESS_PRIMITIVE_UNSPECIFIED) {
         if (gl_prog->info.tess._primitive_mode != TESS_PRIMITIVE_UNSPECIFIED &&
             gl_prog->info.tess._primitive_mode !=
             shader->info.TessEval._PrimitiveMode) {
            linker_error(prog, "tessellation evaluation shader defined with "
                         "conflicting input primitive modes.\n");
            return;
         }
         gl_prog->info.tess._primitive_mode =
            shader->info.TessEval._PrimitiveMode;
      }

      if (shader->info.TessEval.Spacing != 0) {
         if (gl_prog->info.tess.spacing != 0 && gl_prog->info.tess.spacing !=
             shader->info.TessEval.Spacing) {
            linker_error(prog, "tessellation evaluation shader defined with "
                         "conflicting vertex spacing.\n");
            return;
         }
         gl_prog->info.tess.spacing = shader->info.TessEval.Spacing;
      }

      if (shader->info.TessEval.VertexOrder != 0) {
         if (vertex_order != 0 &&
             vertex_order != shader->info.TessEval.VertexOrder) {
            linker_error(prog, "tessellation evaluation shader defined with "
                         "conflicting ordering.\n");
            return;
         }
         vertex_order = shader->info.TessEval.VertexOrder;
      }

      if (shader->info.TessEval.PointMode != -1) {
         if (point_mode != -1 &&
             point_mode != shader->info.TessEval.PointMode) {
            linker_error(prog, "tessellation evaluation shader defined with "
                         "conflicting point modes.\n");
            return;
         }
         point_mode = shader->info.TessEval.PointMode;
      }

   }

   /* Just do the intrastage -> interstage propagation right now,
    * since we already know we're in the right type of shader program
    * for doing it.
    */
   if (gl_prog->info.tess._primitive_mode == TESS_PRIMITIVE_UNSPECIFIED) {
      linker_error(prog,
                   "tessellation evaluation shader didn't declare input "
                   "primitive modes.\n");
      return;
   }

   if (gl_prog->info.tess.spacing == TESS_SPACING_UNSPECIFIED)
      gl_prog->info.tess.spacing = TESS_SPACING_EQUAL;

   if (vertex_order == 0 || vertex_order == GL_CCW)
      gl_prog->info.tess.ccw = true;
   else
      gl_prog->info.tess.ccw = false;


   if (point_mode == -1 || point_mode == GL_FALSE)
      gl_prog->info.tess.point_mode = false;
   else
      gl_prog->info.tess.point_mode = true;
}


/**
 * Performs the cross-validation of layout qualifiers specified in
 * redeclaration of gl_FragCoord for the attached fragment shaders,
 * and propagates them to the linked FS and linked shader program.
 */
static void
link_fs_inout_layout_qualifiers(struct gl_shader_program *prog,
                                struct gl_linked_shader *linked_shader,
                                struct gl_shader **shader_list,
                                unsigned num_shaders)
{
   bool redeclares_gl_fragcoord = false;
   bool uses_gl_fragcoord = false;
   bool origin_upper_left = false;
   bool pixel_center_integer = false;

   if (linked_shader->Stage != MESA_SHADER_FRAGMENT ||
       (prog->GLSL_Version < 150 &&
        !prog->ARB_fragment_coord_conventions_enable))
      return;

   for (unsigned i = 0; i < num_shaders; i++) {
      struct gl_shader *shader = shader_list[i];
      /* From the GLSL 1.50 spec, page 39:
       *
       *   "If gl_FragCoord is redeclared in any fragment shader in a program,
       *    it must be redeclared in all the fragment shaders in that program
       *    that have a static use gl_FragCoord."
       */
      if ((redeclares_gl_fragcoord && !shader->redeclares_gl_fragcoord &&
           shader->uses_gl_fragcoord)
          || (shader->redeclares_gl_fragcoord && !redeclares_gl_fragcoord &&
              uses_gl_fragcoord)) {
             linker_error(prog, "fragment shader defined with conflicting "
                         "layout qualifiers for gl_FragCoord\n");
      }

      /* From the GLSL 1.50 spec, page 39:
       *
       *   "All redeclarations of gl_FragCoord in all fragment shaders in a
       *    single program must have the same set of qualifiers."
       */
      if (redeclares_gl_fragcoord && shader->redeclares_gl_fragcoord &&
          (shader->origin_upper_left != origin_upper_left ||
           shader->pixel_center_integer != pixel_center_integer)) {
         linker_error(prog, "fragment shader defined with conflicting "
                      "layout qualifiers for gl_FragCoord\n");
      }

      /* Update the linked shader state.  Note that uses_gl_fragcoord should
       * accumulate the results.  The other values should replace.  If there
       * are multiple redeclarations, all the fields except uses_gl_fragcoord
       * are already known to be the same.
       */
      if (shader->redeclares_gl_fragcoord || shader->uses_gl_fragcoord) {
         redeclares_gl_fragcoord = shader->redeclares_gl_fragcoord;
         uses_gl_fragcoord |= shader->uses_gl_fragcoord;
         origin_upper_left = shader->origin_upper_left;
         pixel_center_integer = shader->pixel_center_integer;
      }

      linked_shader->Program->info.fs.early_fragment_tests |=
         shader->EarlyFragmentTests || shader->PostDepthCoverage;
      linked_shader->Program->info.fs.inner_coverage |= shader->InnerCoverage;
      linked_shader->Program->info.fs.post_depth_coverage |=
         shader->PostDepthCoverage;
      linked_shader->Program->info.fs.pixel_interlock_ordered |=
         shader->PixelInterlockOrdered;
      linked_shader->Program->info.fs.pixel_interlock_unordered |=
         shader->PixelInterlockUnordered;
      linked_shader->Program->info.fs.sample_interlock_ordered |=
         shader->SampleInterlockOrdered;
      linked_shader->Program->info.fs.sample_interlock_unordered |=
         shader->SampleInterlockUnordered;
      linked_shader->Program->info.fs.advanced_blend_modes |= shader->BlendSupport;
   }

   linked_shader->Program->info.fs.pixel_center_integer = pixel_center_integer;
   linked_shader->Program->info.fs.origin_upper_left = origin_upper_left;
}

/**
 * Performs the cross-validation of geometry shader max_vertices and
 * primitive type layout qualifiers for the attached geometry shaders,
 * and propagates them to the linked GS and linked shader program.
 */
static void
link_gs_inout_layout_qualifiers(struct gl_shader_program *prog,
                                struct gl_program *gl_prog,
                                struct gl_shader **shader_list,
                                unsigned num_shaders)
{
   /* No in/out qualifiers defined for anything but GLSL 1.50+
    * geometry shaders so far.
    */
   if (gl_prog->info.stage != MESA_SHADER_GEOMETRY || prog->GLSL_Version < 150)
      return;

   int vertices_out = -1;

   gl_prog->info.gs.invocations = 0;
   gl_prog->info.gs.input_primitive = SHADER_PRIM_UNKNOWN;
   gl_prog->info.gs.output_primitive = SHADER_PRIM_UNKNOWN;

   /* From the GLSL 1.50 spec, page 46:
    *
    *     "All geometry shader output layout declarations in a program
    *      must declare the same layout and same value for
    *      max_vertices. There must be at least one geometry output
    *      layout declaration somewhere in a program, but not all
    *      geometry shaders (compilation units) are required to
    *      declare it."
    */

   for (unsigned i = 0; i < num_shaders; i++) {
      struct gl_shader *shader = shader_list[i];

      if (shader->info.Geom.InputType != SHADER_PRIM_UNKNOWN) {
         if (gl_prog->info.gs.input_primitive != SHADER_PRIM_UNKNOWN &&
             gl_prog->info.gs.input_primitive !=
             shader->info.Geom.InputType) {
            linker_error(prog, "geometry shader defined with conflicting "
                         "input types\n");
            return;
         }
         gl_prog->info.gs.input_primitive = (enum shader_prim)shader->info.Geom.InputType;
      }

      if (shader->info.Geom.OutputType != SHADER_PRIM_UNKNOWN) {
         if (gl_prog->info.gs.output_primitive != SHADER_PRIM_UNKNOWN &&
             gl_prog->info.gs.output_primitive !=
             shader->info.Geom.OutputType) {
            linker_error(prog, "geometry shader defined with conflicting "
                         "output types\n");
            return;
         }
         gl_prog->info.gs.output_primitive = (enum shader_prim)shader->info.Geom.OutputType;
      }

      if (shader->info.Geom.VerticesOut != -1) {
         if (vertices_out != -1 &&
             vertices_out != shader->info.Geom.VerticesOut) {
            linker_error(prog, "geometry shader defined with conflicting "
                         "output vertex count (%d and %d)\n",
                         vertices_out, shader->info.Geom.VerticesOut);
            return;
         }
         vertices_out = shader->info.Geom.VerticesOut;
      }

      if (shader->info.Geom.Invocations != 0) {
         if (gl_prog->info.gs.invocations != 0 &&
             gl_prog->info.gs.invocations !=
             (unsigned) shader->info.Geom.Invocations) {
            linker_error(prog, "geometry shader defined with conflicting "
                         "invocation count (%d and %d)\n",
                         gl_prog->info.gs.invocations,
                         shader->info.Geom.Invocations);
            return;
         }
         gl_prog->info.gs.invocations = shader->info.Geom.Invocations;
      }
   }

   /* Just do the intrastage -> interstage propagation right now,
    * since we already know we're in the right type of shader program
    * for doing it.
    */
   if (gl_prog->info.gs.input_primitive == SHADER_PRIM_UNKNOWN) {
      linker_error(prog,
                   "geometry shader didn't declare primitive input type\n");
      return;
   }

   if (gl_prog->info.gs.output_primitive == SHADER_PRIM_UNKNOWN) {
      linker_error(prog,
                   "geometry shader didn't declare primitive output type\n");
      return;
   }

   if (vertices_out == -1) {
      linker_error(prog,
                   "geometry shader didn't declare max_vertices\n");
      return;
   } else {
      gl_prog->info.gs.vertices_out = vertices_out;
   }

   if (gl_prog->info.gs.invocations == 0)
      gl_prog->info.gs.invocations = 1;
}


/**
 * Perform cross-validation of compute shader local_size_{x,y,z} layout and
 * derivative arrangement qualifiers for the attached compute shaders, and
 * propagate them to the linked CS and linked shader program.
 */
static void
link_cs_input_layout_qualifiers(struct gl_shader_program *prog,
                                struct gl_program *gl_prog,
                                struct gl_shader **shader_list,
                                unsigned num_shaders)
{
   /* This function is called for all shader stages, but it only has an effect
    * for compute shaders.
    */
   if (gl_prog->info.stage != MESA_SHADER_COMPUTE)
      return;

   for (int i = 0; i < 3; i++)
      gl_prog->info.workgroup_size[i] = 0;

   gl_prog->info.workgroup_size_variable = false;

   gl_prog->info.cs.derivative_group = DERIVATIVE_GROUP_NONE;

   /* From the ARB_compute_shader spec, in the section describing local size
    * declarations:
    *
    *     If multiple compute shaders attached to a single program object
    *     declare local work-group size, the declarations must be identical;
    *     otherwise a link-time error results. Furthermore, if a program
    *     object contains any compute shaders, at least one must contain an
    *     input layout qualifier specifying the local work sizes of the
    *     program, or a link-time error will occur.
    */
   for (unsigned sh = 0; sh < num_shaders; sh++) {
      struct gl_shader *shader = shader_list[sh];

      if (shader->info.Comp.LocalSize[0] != 0) {
         if (gl_prog->info.workgroup_size[0] != 0) {
            for (int i = 0; i < 3; i++) {
               if (gl_prog->info.workgroup_size[i] !=
                   shader->info.Comp.LocalSize[i]) {
                  linker_error(prog, "compute shader defined with conflicting "
                               "local sizes\n");
                  return;
               }
            }
         }
         for (int i = 0; i < 3; i++) {
            gl_prog->info.workgroup_size[i] =
               shader->info.Comp.LocalSize[i];
         }
      } else if (shader->info.Comp.LocalSizeVariable) {
         if (gl_prog->info.workgroup_size[0] != 0) {
            /* The ARB_compute_variable_group_size spec says:
             *
             *     If one compute shader attached to a program declares a
             *     variable local group size and a second compute shader
             *     attached to the same program declares a fixed local group
             *     size, a link-time error results.
             */
            linker_error(prog, "compute shader defined with both fixed and "
                         "variable local group size\n");
            return;
         }
         gl_prog->info.workgroup_size_variable = true;
      }

      enum gl_derivative_group group = shader->info.Comp.DerivativeGroup;
      if (group != DERIVATIVE_GROUP_NONE) {
         if (gl_prog->info.cs.derivative_group != DERIVATIVE_GROUP_NONE &&
             gl_prog->info.cs.derivative_group != group) {
            linker_error(prog, "compute shader defined with conflicting "
                         "derivative groups\n");
            return;
         }
         gl_prog->info.cs.derivative_group = group;
      }
   }

   /* Just do the intrastage -> interstage propagation right now,
    * since we already know we're in the right type of shader program
    * for doing it.
    */
   if (gl_prog->info.workgroup_size[0] == 0 &&
       !gl_prog->info.workgroup_size_variable) {
      linker_error(prog, "compute shader must contain a fixed or a variable "
                         "local group size\n");
      return;
   }

   if (gl_prog->info.cs.derivative_group == DERIVATIVE_GROUP_QUADS) {
      if (gl_prog->info.workgroup_size[0] % 2 != 0) {
         linker_error(prog, "derivative_group_quadsNV must be used with a "
                      "local group size whose first dimension "
                      "is a multiple of 2\n");
         return;
      }
      if (gl_prog->info.workgroup_size[1] % 2 != 0) {
         linker_error(prog, "derivative_group_quadsNV must be used with a local"
                      "group size whose second dimension "
                      "is a multiple of 2\n");
         return;
      }
   } else if (gl_prog->info.cs.derivative_group == DERIVATIVE_GROUP_LINEAR) {
      if ((gl_prog->info.workgroup_size[0] *
           gl_prog->info.workgroup_size[1] *
           gl_prog->info.workgroup_size[2]) % 4 != 0) {
         linker_error(prog, "derivative_group_linearNV must be used with a "
                      "local group size whose total number of invocations "
                      "is a multiple of 4\n");
         return;
      }
   }
}

/**
 * Link all out variables on a single stage which are not
 * directly used in a shader with the main function.
 */
static void
link_output_variables(struct gl_linked_shader *linked_shader,
                      struct gl_shader **shader_list,
                      unsigned num_shaders)
{
   struct glsl_symbol_table *symbols = linked_shader->symbols;

   for (unsigned i = 0; i < num_shaders; i++) {

      /* Skip shader object with main function */
      if (shader_list[i]->symbols->get_function("main"))
         continue;

      foreach_in_list(ir_instruction, ir, shader_list[i]->ir) {
         if (ir->ir_type != ir_type_variable)
            continue;

         ir_variable *var = (ir_variable *) ir;

         if (var->data.mode == ir_var_shader_out &&
               !symbols->get_variable(var->name)) {
            var = var->clone(linked_shader, NULL);
            symbols->add_variable(var);
            linked_shader->ir->push_head(var);
         }
      }
   }

   return;
}


/**
 * Combine a group of shaders for a single stage to generate a linked shader
 *
 * \note
 * If this function is supplied a single shader, it is cloned, and the new
 * shader is returned.
 */
struct gl_linked_shader *
link_intrastage_shaders(void *mem_ctx,
                        struct gl_context *ctx,
                        struct gl_shader_program *prog,
                        struct gl_shader **shader_list,
                        unsigned num_shaders,
                        bool allow_missing_main)
{
   struct gl_uniform_block *ubo_blocks = NULL;
   struct gl_uniform_block *ssbo_blocks = NULL;
   unsigned num_ubo_blocks = 0;
   unsigned num_ssbo_blocks = 0;

   /* Check that global variables defined in multiple shaders are consistent.
    */
   glsl_symbol_table variables;
   for (unsigned i = 0; i < num_shaders; i++) {
      if (shader_list[i] == NULL)
         continue;
      cross_validate_globals(&ctx->Const, prog, shader_list[i]->ir, &variables,
                             false);
   }

   if (!prog->data->LinkStatus)
      return NULL;

   /* Check that interface blocks defined in multiple shaders are consistent.
    */
   validate_intrastage_interface_blocks(prog, (const gl_shader **)shader_list,
                                        num_shaders);
   if (!prog->data->LinkStatus)
      return NULL;

   /* Check that there is only a single definition of each function signature
    * across all shaders.
    */
   for (unsigned i = 0; i < (num_shaders - 1); i++) {
      foreach_in_list(ir_instruction, node, shader_list[i]->ir) {
         ir_function *const f = node->as_function();

         if (f == NULL)
            continue;

         for (unsigned j = i + 1; j < num_shaders; j++) {
            ir_function *const other =
               shader_list[j]->symbols->get_function(f->name);

            /* If the other shader has no function (and therefore no function
             * signatures) with the same name, skip to the next shader.
             */
            if (other == NULL)
               continue;

            foreach_in_list(ir_function_signature, sig, &f->signatures) {
               if (!sig->is_defined)
                  continue;

               ir_function_signature *other_sig =
                  other->exact_matching_signature(NULL, &sig->parameters);

               if (other_sig != NULL && other_sig->is_defined) {
                  linker_error(prog, "function `%s' is multiply defined\n",
                               f->name);
                  return NULL;
               }
            }
         }
      }
   }

   /* Find the shader that defines main, and make a clone of it.
    *
    * Starting with the clone, search for undefined references.  If one is
    * found, find the shader that defines it.  Clone the reference and add
    * it to the shader.  Repeat until there are no undefined references or
    * until a reference cannot be resolved.
    */
   gl_shader *main = NULL;
   for (unsigned i = 0; i < num_shaders; i++) {
      if (_mesa_get_main_function_signature(shader_list[i]->symbols)) {
         main = shader_list[i];
         break;
      }
   }

   if (main == NULL && allow_missing_main)
      main = shader_list[0];

   if (main == NULL) {
      linker_error(prog, "%s shader lacks `main'\n",
                   _mesa_shader_stage_to_string(shader_list[0]->Stage));
      return NULL;
   }

   gl_linked_shader *linked = rzalloc(NULL, struct gl_linked_shader);
   linked->Stage = shader_list[0]->Stage;

   /* Create program and attach it to the linked shader */
   struct gl_program *gl_prog =
      ctx->Driver.NewProgram(ctx, shader_list[0]->Stage, prog->Name, false);
   if (!gl_prog) {
      prog->data->LinkStatus = LINKING_FAILURE;
      _mesa_delete_linked_shader(ctx, linked);
      return NULL;
   }

   _mesa_reference_shader_program_data(&gl_prog->sh.data, prog->data);

   /* Don't use _mesa_reference_program() just take ownership */
   linked->Program = gl_prog;

   linked->ir = new(linked) exec_list;
   clone_ir_list(mem_ctx, linked->ir, main->ir);

   link_fs_inout_layout_qualifiers(prog, linked, shader_list, num_shaders);
   link_tcs_out_layout_qualifiers(prog, gl_prog, shader_list, num_shaders);
   link_tes_in_layout_qualifiers(prog, gl_prog, shader_list, num_shaders);
   link_gs_inout_layout_qualifiers(prog, gl_prog, shader_list, num_shaders);
   link_cs_input_layout_qualifiers(prog, gl_prog, shader_list, num_shaders);

   if (linked->Stage != MESA_SHADER_FRAGMENT)
      link_xfb_stride_layout_qualifiers(&ctx->Const, prog, shader_list, num_shaders);

   link_bindless_layout_qualifiers(prog, shader_list, num_shaders);

   link_layer_viewport_relative_qualifier(prog, gl_prog, shader_list, num_shaders);

   populate_symbol_table(linked, shader_list[0]->symbols);

   /* The pointer to the main function in the final linked shader (i.e., the
    * copy of the original shader that contained the main function).
    */
   ir_function_signature *const main_sig =
      _mesa_get_main_function_signature(linked->symbols);

   /* Move any instructions other than variable declarations or function
    * declarations into main.
    */
   if (main_sig != NULL) {
      exec_node *insertion_point =
         move_non_declarations(linked->ir, &main_sig->body.head_sentinel, false,
                               linked);

      for (unsigned i = 0; i < num_shaders; i++) {
         if (shader_list[i] == main)
            continue;

         insertion_point = move_non_declarations(shader_list[i]->ir,
                                                 insertion_point, true, linked);
      }
   }

   if (!link_function_calls(prog, linked, shader_list, num_shaders)) {
      _mesa_delete_linked_shader(ctx, linked);
      return NULL;
   }

   if (linked->Stage != MESA_SHADER_FRAGMENT)
      link_output_variables(linked, shader_list, num_shaders);

   /* Make a pass over all variable declarations to ensure that arrays with
    * unspecified sizes have a size specified.  The size is inferred from the
    * max_array_access field.
    */
   array_sizing_visitor v;
   v.run(linked->ir);
   v.fixup_unnamed_interface_types();

   /* Now that we know the sizes of all the arrays, we can replace .length()
    * calls with a constant expression.
    */
   array_length_to_const_visitor len_v;
   len_v.run(linked->ir);

   /* Link up uniform blocks defined within this stage. */
   link_uniform_blocks(mem_ctx, &ctx->Const, prog, linked, &ubo_blocks,
                       &num_ubo_blocks, &ssbo_blocks, &num_ssbo_blocks);

   const unsigned max_uniform_blocks =
      ctx->Const.Program[linked->Stage].MaxUniformBlocks;
   if (num_ubo_blocks > max_uniform_blocks) {
      linker_error(prog, "Too many %s uniform blocks (%d/%d)\n",
                   _mesa_shader_stage_to_string(linked->Stage),
                   num_ubo_blocks, max_uniform_blocks);
   }

   const unsigned max_shader_storage_blocks =
      ctx->Const.Program[linked->Stage].MaxShaderStorageBlocks;
   if (num_ssbo_blocks > max_shader_storage_blocks) {
      linker_error(prog, "Too many %s shader storage blocks (%d/%d)\n",
                   _mesa_shader_stage_to_string(linked->Stage),
                   num_ssbo_blocks, max_shader_storage_blocks);
   }

   if (!prog->data->LinkStatus) {
      _mesa_delete_linked_shader(ctx, linked);
      return NULL;
   }

   /* Copy ubo blocks to linked shader list */
   linked->Program->sh.UniformBlocks =
      ralloc_array(linked, gl_uniform_block *, num_ubo_blocks);
   ralloc_steal(linked, ubo_blocks);
   for (unsigned i = 0; i < num_ubo_blocks; i++) {
      linked->Program->sh.UniformBlocks[i] = &ubo_blocks[i];
   }
   linked->Program->sh.NumUniformBlocks = num_ubo_blocks;
   linked->Program->info.num_ubos = num_ubo_blocks;

   /* Copy ssbo blocks to linked shader list */
   linked->Program->sh.ShaderStorageBlocks =
      ralloc_array(linked, gl_uniform_block *, num_ssbo_blocks);
   ralloc_steal(linked, ssbo_blocks);
   for (unsigned i = 0; i < num_ssbo_blocks; i++) {
      linked->Program->sh.ShaderStorageBlocks[i] = &ssbo_blocks[i];
   }
   linked->Program->info.num_ssbos = num_ssbo_blocks;

   /* At this point linked should contain all of the linked IR, so
    * validate it to make sure nothing went wrong.
    */
   validate_ir_tree(linked->ir);

   /* Set the size of geometry shader input arrays */
   if (linked->Stage == MESA_SHADER_GEOMETRY) {
      unsigned num_vertices =
         vertices_per_prim(gl_prog->info.gs.input_primitive);
      array_resize_visitor input_resize_visitor(num_vertices, prog,
                                                MESA_SHADER_GEOMETRY);
      foreach_in_list(ir_instruction, ir, linked->ir) {
         ir->accept(&input_resize_visitor);
      }
   }

   /* Set the linked source SHA1. */
   if (num_shaders == 1) {
      memcpy(linked->linked_source_sha1, shader_list[0]->compiled_source_sha1,
             SHA1_DIGEST_LENGTH);
   } else {
      struct mesa_sha1 sha1_ctx;
      _mesa_sha1_init(&sha1_ctx);

      for (unsigned i = 0; i < num_shaders; i++) {
         if (shader_list[i] == NULL)
            continue;

         _mesa_sha1_update(&sha1_ctx, shader_list[i]->compiled_source_sha1,
                           SHA1_DIGEST_LENGTH);
      }
      _mesa_sha1_final(&sha1_ctx, linked->linked_source_sha1);
   }

   return linked;
}

/**
 * Resize tessellation evaluation per-vertex inputs to the size of
 * tessellation control per-vertex outputs.
 */
static void
resize_tes_inputs(const struct gl_constants *consts,
                  struct gl_shader_program *prog)
{
   if (prog->_LinkedShaders[MESA_SHADER_TESS_EVAL] == NULL)
      return;

   gl_linked_shader *const tcs = prog->_LinkedShaders[MESA_SHADER_TESS_CTRL];
   gl_linked_shader *const tes = prog->_LinkedShaders[MESA_SHADER_TESS_EVAL];

   /* If no control shader is present, then the TES inputs are statically
    * sized to MaxPatchVertices; the actual size of the arrays won't be
    * known until draw time.
    */
   const int num_vertices = tcs
      ? tcs->Program->info.tess.tcs_vertices_out
      : consts->MaxPatchVertices;

   array_resize_visitor input_resize_visitor(num_vertices, prog,
                                             MESA_SHADER_TESS_EVAL);
   foreach_in_list(ir_instruction, ir, tes->ir) {
      ir->accept(&input_resize_visitor);
   }

   if (tcs) {
      /* Convert the gl_PatchVerticesIn system value into a constant, since
       * the value is known at this point.
       */
      foreach_in_list(ir_instruction, ir, tes->ir) {
         ir_variable *var = ir->as_variable();
         if (var && var->data.mode == ir_var_system_value &&
             var->data.location == SYSTEM_VALUE_VERTICES_IN) {
            void *mem_ctx = ralloc_parent(var);
            var->data.location = 0;
            var->data.explicit_location = false;
            var->data.mode = ir_var_auto;
            var->constant_value = new(mem_ctx) ir_constant(num_vertices);
         }
      }
   }
}

/**
 * Find a contiguous set of available bits in a bitmask.
 *
 * \param used_mask     Bits representing used (1) and unused (0) locations
 * \param needed_count  Number of contiguous bits needed.
 *
 * \return
 * Base location of the available bits on success or -1 on failure.
 */
static int
find_available_slots(unsigned used_mask, unsigned needed_count)
{
   unsigned needed_mask = (1 << needed_count) - 1;
   const int max_bit_to_test = (8 * sizeof(used_mask)) - needed_count;

   /* The comparison to 32 is redundant, but without it GCC emits "warning:
    * cannot optimize possibly infinite loops" for the loop below.
    */
   if ((needed_count == 0) || (max_bit_to_test < 0) || (max_bit_to_test > 32))
      return -1;

   for (int i = 0; i <= max_bit_to_test; i++) {
      if ((needed_mask & ~used_mask) == needed_mask)
         return i;

      needed_mask <<= 1;
   }

   return -1;
}


#define SAFE_MASK_FROM_INDEX(i) (((i) >= 32) ? ~0 : ((1 << (i)) - 1))

/**
 * Assign locations for either VS inputs or FS outputs.
 *
 * \param mem_ctx        Temporary ralloc context used for linking.
 * \param prog           Shader program whose variables need locations
 *                       assigned.
 * \param constants      Driver specific constant values for the program.
 * \param target_index   Selector for the program target to receive location
 *                       assignmnets.  Must be either \c MESA_SHADER_VERTEX or
 *                       \c MESA_SHADER_FRAGMENT.
 * \param do_assignment  Whether we are actually marking the assignment or we
 *                       are just doing a dry-run checking.
 *
 * \return
 * If locations are (or can be, in case of dry-running) successfully assigned,
 * true is returned.  Otherwise an error is emitted to the shader link log and
 * false is returned.
 */
static bool
assign_attribute_or_color_locations(void *mem_ctx,
                                    gl_shader_program *prog,
                                    const struct gl_constants *constants,
                                    unsigned target_index,
                                    bool do_assignment)
{
   /* Maximum number of generic locations.  This corresponds to either the
    * maximum number of draw buffers or the maximum number of generic
    * attributes.
    */
   unsigned max_index = (target_index == MESA_SHADER_VERTEX) ?
      constants->Program[target_index].MaxAttribs :
      MAX2(constants->MaxDrawBuffers, constants->MaxDualSourceDrawBuffers);

   /* Mark invalid locations as being used.
    */
   unsigned used_locations = ~SAFE_MASK_FROM_INDEX(max_index);
   unsigned double_storage_locations = 0;

   assert((target_index == MESA_SHADER_VERTEX)
          || (target_index == MESA_SHADER_FRAGMENT));

   gl_linked_shader *const sh = prog->_LinkedShaders[target_index];
   if (sh == NULL)
      return true;

   /* Operate in a total of four passes.
    *
    * 1. Invalidate the location assignments for all vertex shader inputs.
    *
    * 2. Assign locations for inputs that have user-defined (via
    *    glBindVertexAttribLocation) locations and outputs that have
    *    user-defined locations (via glBindFragDataLocation).
    *
    * 3. Sort the attributes without assigned locations by number of slots
    *    required in decreasing order.  Fragmentation caused by attribute
    *    locations assigned by the application may prevent large attributes
    *    from having enough contiguous space.
    *
    * 4. Assign locations to any inputs without assigned locations.
    */

   const int generic_base = (target_index == MESA_SHADER_VERTEX)
      ? (int) VERT_ATTRIB_GENERIC0 : (int) FRAG_RESULT_DATA0;

   const enum ir_variable_mode direction =
      (target_index == MESA_SHADER_VERTEX)
      ? ir_var_shader_in : ir_var_shader_out;


   /* Temporary storage for the set of attributes that need locations assigned.
    */
   struct temp_attr {
      unsigned slots;
      ir_variable *var;

      /* Used below in the call to qsort. */
      static int compare(const void *a, const void *b)
      {
         const temp_attr *const l = (const temp_attr *) a;
         const temp_attr *const r = (const temp_attr *) b;

         /* Reversed because we want a descending order sort below. */
         return r->slots - l->slots;
      }
   } to_assign[32];
   assert(max_index <= 32);

   /* Temporary array for the set of attributes that have locations assigned,
    * for the purpose of checking overlapping slots/components of (non-ES)
    * fragment shader outputs.
    */
   ir_variable *assigned[12 * 4]; /* (max # of FS outputs) * # components */
   unsigned assigned_attr = 0;

   unsigned num_attr = 0;

   foreach_in_list(ir_instruction, node, sh->ir) {
      ir_variable *const var = node->as_variable();

      if ((var == NULL) || (var->data.mode != (unsigned) direction))
         continue;

      if (var->data.explicit_location) {
         if ((var->data.location >= (int)(max_index + generic_base))
             || (var->data.location < 0)) {
            linker_error(prog,
                         "invalid explicit location %d specified for `%s'\n",
                         (var->data.location < 0)
                         ? var->data.location
                         : var->data.location - generic_base,
                         var->name);
            return false;
         }
      } else if (target_index == MESA_SHADER_VERTEX) {
         unsigned binding;

         if (prog->AttributeBindings->get(binding, var->name)) {
            assert(binding >= VERT_ATTRIB_GENERIC0);
            var->data.location = binding;
         }
      } else if (target_index == MESA_SHADER_FRAGMENT) {
         unsigned binding;
         unsigned index;
         const char *name = var->name;
         const glsl_type *type = var->type;

         while (type) {
            /* Check if there's a binding for the variable name */
            if (prog->FragDataBindings->get(binding, name)) {
               assert(binding >= FRAG_RESULT_DATA0);
               var->data.location = binding;

               if (prog->FragDataIndexBindings->get(index, name)) {
                  var->data.index = index;
               }
               break;
            }

            /* If not, but it's an array type, look for name[0] */
            if (type->is_array()) {
               name = ralloc_asprintf(mem_ctx, "%s[0]", name);
               type = type->fields.array;
               continue;
            }

            break;
         }
      }

      if (strcmp(var->name, "gl_LastFragData") == 0)
         continue;

      /* From GL4.5 core spec, section 15.2 (Shader Execution):
       *
       *     "Output binding assignments will cause LinkProgram to fail:
       *     ...
       *     If the program has an active output assigned to a location greater
       *     than or equal to the value of MAX_DUAL_SOURCE_DRAW_BUFFERS and has
       *     an active output assigned an index greater than or equal to one;"
       */
      if (target_index == MESA_SHADER_FRAGMENT && var->data.index >= 1 &&
          var->data.location - generic_base >=
          (int) constants->MaxDualSourceDrawBuffers) {
         linker_error(prog,
                      "output location %d >= GL_MAX_DUAL_SOURCE_DRAW_BUFFERS "
                      "with index %u for %s\n",
                      var->data.location - generic_base, var->data.index,
                      var->name);
         return false;
      }

      const unsigned slots = var->type->count_attribute_slots(target_index == MESA_SHADER_VERTEX);

      /* If the variable is not a built-in and has a location statically
       * assigned in the shader (presumably via a layout qualifier), make sure
       * that it doesn't collide with other assigned locations.  Otherwise,
       * add it to the list of variables that need linker-assigned locations.
       */
      if (var->data.location != -1) {
         if (var->data.location >= generic_base && var->data.index < 1) {
            /* From page 61 of the OpenGL 4.0 spec:
             *
             *     "LinkProgram will fail if the attribute bindings assigned
             *     by BindAttribLocation do not leave not enough space to
             *     assign a location for an active matrix attribute or an
             *     active attribute array, both of which require multiple
             *     contiguous generic attributes."
             *
             * I think above text prohibits the aliasing of explicit and
             * automatic assignments. But, aliasing is allowed in manual
             * assignments of attribute locations. See below comments for
             * the details.
             *
             * From OpenGL 4.0 spec, page 61:
             *
             *     "It is possible for an application to bind more than one
             *     attribute name to the same location. This is referred to as
             *     aliasing. This will only work if only one of the aliased
             *     attributes is active in the executable program, or if no
             *     path through the shader consumes more than one attribute of
             *     a set of attributes aliased to the same location. A link
             *     error can occur if the linker determines that every path
             *     through the shader consumes multiple aliased attributes,
             *     but implementations are not required to generate an error
             *     in this case."
             *
             * From GLSL 4.30 spec, page 54:
             *
             *    "A program will fail to link if any two non-vertex shader
             *     input variables are assigned to the same location. For
             *     vertex shaders, multiple input variables may be assigned
             *     to the same location using either layout qualifiers or via
             *     the OpenGL API. However, such aliasing is intended only to
             *     support vertex shaders where each execution path accesses
             *     at most one input per each location. Implementations are
             *     permitted, but not required, to generate link-time errors
             *     if they detect that every path through the vertex shader
             *     executable accesses multiple inputs assigned to any single
             *     location. For all shader types, a program will fail to link
             *     if explicit location assignments leave the linker unable
             *     to find space for other variables without explicit
             *     assignments."
             *
             * From OpenGL ES 3.0 spec, page 56:
             *
             *    "Binding more than one attribute name to the same location
             *     is referred to as aliasing, and is not permitted in OpenGL
             *     ES Shading Language 3.00 vertex shaders. LinkProgram will
             *     fail when this condition exists. However, aliasing is
             *     possible in OpenGL ES Shading Language 1.00 vertex shaders.
             *     This will only work if only one of the aliased attributes
             *     is active in the executable program, or if no path through
             *     the shader consumes more than one attribute of a set of
             *     attributes aliased to the same location. A link error can
             *     occur if the linker determines that every path through the
             *     shader consumes multiple aliased attributes, but implemen-
             *     tations are not required to generate an error in this case."
             *
             * After looking at above references from OpenGL, OpenGL ES and
             * GLSL specifications, we allow aliasing of vertex input variables
             * in: OpenGL 2.0 (and above) and OpenGL ES 2.0.
             *
             * NOTE: This is not required by the spec but its worth mentioning
             * here that we're not doing anything to make sure that no path
             * through the vertex shader executable accesses multiple inputs
             * assigned to any single location.
             */

            /* Mask representing the contiguous slots that will be used by
             * this attribute.
             */
            const unsigned attr = var->data.location - generic_base;
            const unsigned use_mask = (1 << slots) - 1;
            const char *const string = (target_index == MESA_SHADER_VERTEX)
               ? "vertex shader input" : "fragment shader output";

            /* Generate a link error if the requested locations for this
             * attribute exceed the maximum allowed attribute location.
             */
            if (attr + slots > max_index) {
               linker_error(prog,
                           "insufficient contiguous locations "
                           "available for %s `%s' %d %d %d\n", string,
                           var->name, used_locations, use_mask, attr);
               return false;
            }

            /* Generate a link error if the set of bits requested for this
             * attribute overlaps any previously allocated bits.
             */
            if ((~(use_mask << attr) & used_locations) != used_locations) {
               if (target_index == MESA_SHADER_FRAGMENT && !prog->IsES) {
                  /* From section 4.4.2 (Output Layout Qualifiers) of the GLSL
                   * 4.40 spec:
                   *
                   *    "Additionally, for fragment shader outputs, if two
                   *    variables are placed within the same location, they
                   *    must have the same underlying type (floating-point or
                   *    integer). No component aliasing of output variables or
                   *    members is allowed.
                   */
                  for (unsigned i = 0; i < assigned_attr; i++) {
                     unsigned assigned_slots =
                        assigned[i]->type->count_attribute_slots(false);
                     unsigned assig_attr =
                        assigned[i]->data.location - generic_base;
                     unsigned assigned_use_mask = (1 << assigned_slots) - 1;

                     if ((assigned_use_mask << assig_attr) &
                         (use_mask << attr)) {

                        const glsl_type *assigned_type =
                           assigned[i]->type->without_array();
                        const glsl_type *type = var->type->without_array();
                        if (assigned_type->base_type != type->base_type) {
                           linker_error(prog, "types do not match for aliased"
                                        " %ss %s and %s\n", string,
                                        assigned[i]->name, var->name);
                           return false;
                        }

                        unsigned assigned_component_mask =
                           ((1 << assigned_type->vector_elements) - 1) <<
                           assigned[i]->data.location_frac;
                        unsigned component_mask =
                           ((1 << type->vector_elements) - 1) <<
                           var->data.location_frac;
                        if (assigned_component_mask & component_mask) {
                           linker_error(prog, "overlapping component is "
                                        "assigned to %ss %s and %s "
                                        "(component=%d)\n",
                                        string, assigned[i]->name, var->name,
                                        var->data.location_frac);
                           return false;
                        }
                     }
                  }
               } else if (target_index == MESA_SHADER_FRAGMENT ||
                          (prog->IsES && prog->GLSL_Version >= 300)) {
                  linker_error(prog, "overlapping location is assigned "
                               "to %s `%s' %d %d %d\n", string, var->name,
                               used_locations, use_mask, attr);
                  return false;
               } else {
                  linker_warning(prog, "overlapping location is assigned "
                                 "to %s `%s' %d %d %d\n", string, var->name,
                                 used_locations, use_mask, attr);
               }
            }

            if (target_index == MESA_SHADER_FRAGMENT && !prog->IsES) {
               /* Only track assigned variables for non-ES fragment shaders
                * to avoid overflowing the array.
                *
                * At most one variable per fragment output component should
                * reach this.
                */
               assert(assigned_attr < ARRAY_SIZE(assigned));
               assigned[assigned_attr] = var;
               assigned_attr++;
            }

            used_locations |= (use_mask << attr);

            /* From the GL 4.5 core spec, section 11.1.1 (Vertex Attributes):
             *
             * "A program with more than the value of MAX_VERTEX_ATTRIBS
             *  active attribute variables may fail to link, unless
             *  device-dependent optimizations are able to make the program
             *  fit within available hardware resources. For the purposes
             *  of this test, attribute variables of the type dvec3, dvec4,
             *  dmat2x3, dmat2x4, dmat3, dmat3x4, dmat4x3, and dmat4 may
             *  count as consuming twice as many attributes as equivalent
             *  single-precision types. While these types use the same number
             *  of generic attributes as their single-precision equivalents,
             *  implementations are permitted to consume two single-precision
             *  vectors of internal storage for each three- or four-component
             *  double-precision vector."
             *
             * Mark this attribute slot as taking up twice as much space
             * so we can count it properly against limits.  According to
             * issue (3) of the GL_ARB_vertex_attrib_64bit behavior, this
             * is optional behavior, but it seems preferable.
             */
            if (var->type->without_array()->is_dual_slot())
               double_storage_locations |= (use_mask << attr);
         }

         continue;
      }

      if (num_attr >= max_index) {
         linker_error(prog, "too many %s (max %u)",
                      target_index == MESA_SHADER_VERTEX ?
                      "vertex shader inputs" : "fragment shader outputs",
                      max_index);
         return false;
      }
      to_assign[num_attr].slots = slots;
      to_assign[num_attr].var = var;
      num_attr++;
   }

   if (!do_assignment)
      return true;

   if (target_index == MESA_SHADER_VERTEX) {
      unsigned total_attribs_size =
         util_bitcount(used_locations & SAFE_MASK_FROM_INDEX(max_index)) +
         util_bitcount(double_storage_locations);
      if (total_attribs_size > max_index) {
         linker_error(prog,
                      "attempt to use %d vertex attribute slots only %d available ",
                      total_attribs_size, max_index);
         return false;
      }
   }

   /* If all of the attributes were assigned locations by the application (or
    * are built-in attributes with fixed locations), return early.  This should
    * be the common case.
    */
   if (num_attr == 0)
      return true;

   qsort(to_assign, num_attr, sizeof(to_assign[0]), temp_attr::compare);

   if (target_index == MESA_SHADER_VERTEX) {
      /* VERT_ATTRIB_GENERIC0 is a pseudo-alias for VERT_ATTRIB_POS.  It can
       * only be explicitly assigned by via glBindAttribLocation.  Mark it as
       * reserved to prevent it from being automatically allocated below.
       */
      find_deref_visitor find("gl_Vertex");
      find.run(sh->ir);
      if (find.variable_found())
         used_locations |= (1 << 0);
   }

   for (unsigned i = 0; i < num_attr; i++) {
      /* Mask representing the contiguous slots that will be used by this
       * attribute.
       */
      const unsigned use_mask = (1 << to_assign[i].slots) - 1;

      int location = find_available_slots(used_locations, to_assign[i].slots);

      if (location < 0) {
         const char *const string = (target_index == MESA_SHADER_VERTEX)
            ? "vertex shader input" : "fragment shader output";

         linker_error(prog,
                      "insufficient contiguous locations "
                      "available for %s `%s'\n",
                      string, to_assign[i].var->name);
         return false;
      }

      to_assign[i].var->data.location = generic_base + location;
      used_locations |= (use_mask << location);

      if (to_assign[i].var->type->without_array()->is_dual_slot())
         double_storage_locations |= (use_mask << location);
   }

   /* Now that we have all the locations, from the GL 4.5 core spec, section
    * 11.1.1 (Vertex Attributes), dvec3, dvec4, dmat2x3, dmat2x4, dmat3,
    * dmat3x4, dmat4x3, and dmat4 count as consuming twice as many attributes
    * as equivalent single-precision types.
    */
   if (target_index == MESA_SHADER_VERTEX) {
      unsigned total_attribs_size =
         util_bitcount(used_locations & SAFE_MASK_FROM_INDEX(max_index)) +
         util_bitcount(double_storage_locations);
      if (total_attribs_size > max_index) {
         linker_error(prog,
                      "attempt to use %d vertex attribute slots only %d available ",
                      total_attribs_size, max_index);
         return false;
      }
   }

   return true;
}

/**
 * Store the gl_FragDepth layout in the gl_shader_program struct.
 */
static void
store_fragdepth_layout(struct gl_shader_program *prog)
{
   if (prog->_LinkedShaders[MESA_SHADER_FRAGMENT] == NULL) {
      return;
   }

   struct exec_list *ir = prog->_LinkedShaders[MESA_SHADER_FRAGMENT]->ir;

   /* We don't look up the gl_FragDepth symbol directly because if
    * gl_FragDepth is not used in the shader, it's removed from the IR.
    * However, the symbol won't be removed from the symbol table.
    *
    * We're only interested in the cases where the variable is NOT removed
    * from the IR.
    */
   foreach_in_list(ir_instruction, node, ir) {
      ir_variable *const var = node->as_variable();

      if (var == NULL || var->data.mode != ir_var_shader_out) {
         continue;
      }

      if (strcmp(var->name, "gl_FragDepth") == 0) {
         switch (var->data.depth_layout) {
         case ir_depth_layout_none:
            prog->FragDepthLayout = FRAG_DEPTH_LAYOUT_NONE;
            return;
         case ir_depth_layout_any:
            prog->FragDepthLayout = FRAG_DEPTH_LAYOUT_ANY;
            return;
         case ir_depth_layout_greater:
            prog->FragDepthLayout = FRAG_DEPTH_LAYOUT_GREATER;
            return;
         case ir_depth_layout_less:
            prog->FragDepthLayout = FRAG_DEPTH_LAYOUT_LESS;
            return;
         case ir_depth_layout_unchanged:
            prog->FragDepthLayout = FRAG_DEPTH_LAYOUT_UNCHANGED;
            return;
         default:
            assert(0);
            return;
         }
      }
   }
}


/**
 * Initializes explicit location slots to INACTIVE_UNIFORM_EXPLICIT_LOCATION
 * for a variable, checks for overlaps between other uniforms using explicit
 * locations.
 */
static int
reserve_explicit_locations(struct gl_shader_program *prog,
                           string_to_uint_map *map, ir_variable *var)
{
   unsigned slots = var->type->uniform_locations();
   unsigned max_loc = var->data.location + slots - 1;
   unsigned return_value = slots;

   /* Resize remap table if locations do not fit in the current one. */
   if (max_loc + 1 > prog->NumUniformRemapTable) {
      prog->UniformRemapTable =
         reralloc(prog, prog->UniformRemapTable,
                  gl_uniform_storage *,
                  max_loc + 1);

      if (!prog->UniformRemapTable) {
         linker_error(prog, "Out of memory during linking.\n");
         return -1;
      }

      /* Initialize allocated space. */
      for (unsigned i = prog->NumUniformRemapTable; i < max_loc + 1; i++)
         prog->UniformRemapTable[i] = NULL;

      prog->NumUniformRemapTable = max_loc + 1;
   }

   for (unsigned i = 0; i < slots; i++) {
      unsigned loc = var->data.location + i;

      /* Check if location is already used. */
      if (prog->UniformRemapTable[loc] == INACTIVE_UNIFORM_EXPLICIT_LOCATION) {

         /* Possibly same uniform from a different stage, this is ok. */
         unsigned hash_loc;
         if (map->get(hash_loc, var->name) && hash_loc == loc - i) {
            return_value = 0;
            continue;
         }

         /* ARB_explicit_uniform_location specification states:
          *
          *     "No two default-block uniform variables in the program can have
          *     the same location, even if they are unused, otherwise a compiler
          *     or linker error will be generated."
          */
         linker_error(prog,
                      "location qualifier for uniform %s overlaps "
                      "previously used location\n",
                      var->name);
         return -1;
      }

      /* Initialize location as inactive before optimization
       * rounds and location assignment.
       */
      prog->UniformRemapTable[loc] = INACTIVE_UNIFORM_EXPLICIT_LOCATION;
   }

   /* Note, base location used for arrays. */
   map->put(var->data.location, var->name);

   return return_value;
}

static bool
reserve_subroutine_explicit_locations(struct gl_shader_program *prog,
                                      struct gl_program *p,
                                      ir_variable *var)
{
   unsigned slots = var->type->uniform_locations();
   unsigned max_loc = var->data.location + slots - 1;

   /* Resize remap table if locations do not fit in the current one. */
   if (max_loc + 1 > p->sh.NumSubroutineUniformRemapTable) {
      p->sh.SubroutineUniformRemapTable =
         reralloc(p, p->sh.SubroutineUniformRemapTable,
                  gl_uniform_storage *,
                  max_loc + 1);

      if (!p->sh.SubroutineUniformRemapTable) {
         linker_error(prog, "Out of memory during linking.\n");
         return false;
      }

      /* Initialize allocated space. */
      for (unsigned i = p->sh.NumSubroutineUniformRemapTable; i < max_loc + 1; i++)
         p->sh.SubroutineUniformRemapTable[i] = NULL;

      p->sh.NumSubroutineUniformRemapTable = max_loc + 1;
   }

   for (unsigned i = 0; i < slots; i++) {
      unsigned loc = var->data.location + i;

      /* Check if location is already used. */
      if (p->sh.SubroutineUniformRemapTable[loc] == INACTIVE_UNIFORM_EXPLICIT_LOCATION) {

         /* ARB_explicit_uniform_location specification states:
          *     "No two subroutine uniform variables can have the same location
          *     in the same shader stage, otherwise a compiler or linker error
          *     will be generated."
          */
         linker_error(prog,
                      "location qualifier for uniform %s overlaps "
                      "previously used location\n",
                      var->name);
         return false;
      }

      /* Initialize location as inactive before optimization
       * rounds and location assignment.
       */
      p->sh.SubroutineUniformRemapTable[loc] = INACTIVE_UNIFORM_EXPLICIT_LOCATION;
   }

   return true;
}
/**
 * Check and reserve all explicit uniform locations, called before
 * any optimizations happen to handle also inactive uniforms and
 * inactive array elements that may get trimmed away.
 */
static void
check_explicit_uniform_locations(const struct gl_extensions *exts,
                                 struct gl_shader_program *prog)
{
   prog->NumExplicitUniformLocations = 0;

   if (!exts->ARB_explicit_uniform_location)
      return;

   /* This map is used to detect if overlapping explicit locations
    * occur with the same uniform (from different stage) or a different one.
    */
   string_to_uint_map *uniform_map = new string_to_uint_map;

   if (!uniform_map) {
      linker_error(prog, "Out of memory during linking.\n");
      return;
   }

   unsigned entries_total = 0;
   unsigned mask = prog->data->linked_stages;
   while (mask) {
      const int i = u_bit_scan(&mask);
      struct gl_program *p = prog->_LinkedShaders[i]->Program;

      foreach_in_list(ir_instruction, node, prog->_LinkedShaders[i]->ir) {
         ir_variable *var = node->as_variable();
         if (!var || var->data.mode != ir_var_uniform)
            continue;

         if (var->data.explicit_location) {
            bool ret = false;
            if (var->type->without_array()->is_subroutine())
               ret = reserve_subroutine_explicit_locations(prog, p, var);
            else {
               int slots = reserve_explicit_locations(prog, uniform_map,
                                                      var);
               if (slots != -1) {
                  ret = true;
                  entries_total += slots;
               }
            }
            if (!ret) {
               delete uniform_map;
               return;
            }
         }
      }
   }

   link_util_update_empty_uniform_locations(prog);

   delete uniform_map;
   prog->NumExplicitUniformLocations = entries_total;
}

static void
link_assign_subroutine_types(struct gl_shader_program *prog)
{
   unsigned mask = prog->data->linked_stages;
   while (mask) {
      const int i = u_bit_scan(&mask);
      gl_program *p = prog->_LinkedShaders[i]->Program;

      p->sh.MaxSubroutineFunctionIndex = 0;
      foreach_in_list(ir_instruction, node, prog->_LinkedShaders[i]->ir) {
         ir_function *fn = node->as_function();
         if (!fn)
            continue;

         if (fn->is_subroutine)
            p->sh.NumSubroutineUniformTypes++;

         if (!fn->num_subroutine_types)
            continue;

         /* these should have been calculated earlier. */
         assert(fn->subroutine_index != -1);
         if (p->sh.NumSubroutineFunctions + 1 > MAX_SUBROUTINES) {
            linker_error(prog, "Too many subroutine functions declared.\n");
            return;
         }
         p->sh.SubroutineFunctions = reralloc(p, p->sh.SubroutineFunctions,
                                            struct gl_subroutine_function,
                                            p->sh.NumSubroutineFunctions + 1);
         p->sh.SubroutineFunctions[p->sh.NumSubroutineFunctions].name.string = ralloc_strdup(p, fn->name);
         resource_name_updated(&p->sh.SubroutineFunctions[p->sh.NumSubroutineFunctions].name);
         p->sh.SubroutineFunctions[p->sh.NumSubroutineFunctions].num_compat_types = fn->num_subroutine_types;
         p->sh.SubroutineFunctions[p->sh.NumSubroutineFunctions].types =
            ralloc_array(p, const struct glsl_type *,
                         fn->num_subroutine_types);

         /* From Section 4.4.4(Subroutine Function Layout Qualifiers) of the
          * GLSL 4.5 spec:
          *
          *    "Each subroutine with an index qualifier in the shader must be
          *    given a unique index, otherwise a compile or link error will be
          *    generated."
          */
         for (unsigned j = 0; j < p->sh.NumSubroutineFunctions; j++) {
            if (p->sh.SubroutineFunctions[j].index != -1 &&
                p->sh.SubroutineFunctions[j].index == fn->subroutine_index) {
               linker_error(prog, "each subroutine index qualifier in the "
                            "shader must be unique\n");
               return;
            }
         }
         p->sh.SubroutineFunctions[p->sh.NumSubroutineFunctions].index =
            fn->subroutine_index;

         if (fn->subroutine_index > (int)p->sh.MaxSubroutineFunctionIndex)
            p->sh.MaxSubroutineFunctionIndex = fn->subroutine_index;

         for (int j = 0; j < fn->num_subroutine_types; j++)
            p->sh.SubroutineFunctions[p->sh.NumSubroutineFunctions].types[j] = fn->subroutine_types[j];
         p->sh.NumSubroutineFunctions++;
      }
   }
}

static void
verify_subroutine_associated_funcs(struct gl_shader_program *prog)
{
   unsigned mask = prog->data->linked_stages;
   while (mask) {
      const int i = u_bit_scan(&mask);
      gl_program *p = prog->_LinkedShaders[i]->Program;
      glsl_symbol_table *symbols = prog->_LinkedShaders[i]->symbols;

      /* Section 6.1.2 (Subroutines) of the GLSL 4.00 spec says:
       *
       *   "A program will fail to compile or link if any shader
       *    or stage contains two or more functions with the same
       *    name if the name is associated with a subroutine type."
       */
      for (unsigned j = 0; j < p->sh.NumSubroutineFunctions; j++) {
         unsigned definitions = 0;
         char *name = p->sh.SubroutineFunctions[j].name.string;
         ir_function *fn = symbols->get_function(name);

         /* Calculate number of function definitions with the same name */
         foreach_in_list(ir_function_signature, sig, &fn->signatures) {
            if (sig->is_defined) {
               if (++definitions > 1) {
                  linker_error(prog, "%s shader contains two or more function "
                               "definitions with name `%s', which is "
                               "associated with a subroutine type.\n",
                               _mesa_shader_stage_to_string(i),
                               fn->name);
                  return;
               }
            }
         }
      }
   }
}


static void
set_always_active_io(exec_list *ir, ir_variable_mode io_mode)
{
   assert(io_mode == ir_var_shader_in || io_mode == ir_var_shader_out);

   foreach_in_list(ir_instruction, node, ir) {
      ir_variable *const var = node->as_variable();

      if (var == NULL || var->data.mode != io_mode)
         continue;

      /* Don't set always active on builtins that haven't been redeclared */
      if (var->data.how_declared == ir_var_declared_implicitly)
         continue;

      var->data.always_active_io = true;
   }
}

/**
 * When separate shader programs are enabled, only input/outputs between
 * the stages of a multi-stage separate program can be safely removed
 * from the shader interface. Other inputs/outputs must remain active.
 */
static void
disable_varying_optimizations_for_sso(struct gl_shader_program *prog)
{
   unsigned first, last;
   assert(prog->SeparateShader);

   first = MESA_SHADER_STAGES;
   last = 0;

   /* Determine first and last stage. Excluding the compute stage */
   for (unsigned i = 0; i < MESA_SHADER_COMPUTE; i++) {
      if (!prog->_LinkedShaders[i])
         continue;
      if (first == MESA_SHADER_STAGES)
         first = i;
      last = i;
   }

   if (first == MESA_SHADER_STAGES)
      return;

   for (unsigned stage = 0; stage < MESA_SHADER_STAGES; stage++) {
      gl_linked_shader *sh = prog->_LinkedShaders[stage];
      if (!sh)
         continue;

      /* Prevent the removal of inputs to the first and outputs from the last
       * stage, unless they are the initial pipeline inputs or final pipeline
       * outputs, respectively.
       *
       * The removal of IO between shaders in the same program is always
       * allowed.
       */
      if (stage == first && stage != MESA_SHADER_VERTEX)
         set_always_active_io(sh->ir, ir_var_shader_in);
      if (stage == last && stage != MESA_SHADER_FRAGMENT)
         set_always_active_io(sh->ir, ir_var_shader_out);
   }
}

static bool
link_varyings(const struct gl_constants *consts, struct gl_shader_program *prog,
              void *mem_ctx)
{
   /* Mark all generic shader inputs and outputs as unpaired. */
   for (unsigned i = MESA_SHADER_VERTEX; i <= MESA_SHADER_FRAGMENT; i++) {
      if (prog->_LinkedShaders[i] != NULL) {
         link_invalidate_variable_locations(prog->_LinkedShaders[i]->ir);
      }
   }

   if (!assign_attribute_or_color_locations(mem_ctx, prog, consts,
                                            MESA_SHADER_VERTEX, true)) {
      return false;
   }

   if (!assign_attribute_or_color_locations(mem_ctx, prog, consts,
                                            MESA_SHADER_FRAGMENT, true)) {
      return false;
   }

   prog->last_vert_prog = NULL;
   for (int i = MESA_SHADER_GEOMETRY; i >= MESA_SHADER_VERTEX; i--) {
      if (prog->_LinkedShaders[i] == NULL)
         continue;

      prog->last_vert_prog = prog->_LinkedShaders[i]->Program;
      break;
   }

   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      if (prog->_LinkedShaders[i] == NULL)
         continue;

      lower_vector_derefs(prog->_LinkedShaders[i]);
   }

   return true;
}



void
resource_name_updated(struct gl_resource_name *name)
{
   if (name->string) {
      name->length = strlen(name->string);

      const char *last_square_bracket = strrchr(name->string, '[');
      if (last_square_bracket) {
         name->last_square_bracket = last_square_bracket - name->string;
         name->suffix_is_zero_square_bracketed =
            strcmp(last_square_bracket, "[0]") == 0;
      } else {
         name->last_square_bracket = -1;
         name->suffix_is_zero_square_bracketed = false;
      }
   } else {
      name->length = 0;
      name->last_square_bracket = -1;
      name->suffix_is_zero_square_bracketed = false;
   }
}
