/*
 * Copyright © 2012 Intel Corporation
 * Copyright © 2021 Valve Corporation
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
 * Linker functions related specifically to linking varyings between shader
 * stages.
 */

#include "../../mesa/main/errors.h"
#include "../../util/macros.h"
#include "../../mesa/main/menums.h"
#include "../../mesa/main/mtypes.h"
#include "../../util/hash_table.h"
#include "../../util/u_math.h"

#include "../nir/nir.h"
#include "../nir/nir_builder.h"
#include "gl_nir.h"
#include "gl_nir_link_varyings.h"
#include "gl_nir_linker.h"
#include "linker_util.h"
#include "../nir_gl_types.h"


/**
 * Get the varying type stripped of the outermost array if we're processing
 * a stage whose varyings are arrays indexed by a vertex number (such as
 * geometry shader inputs).
 */
static const struct glsl_type *
get_varying_type(const nir_variable *var, gl_shader_stage stage)
{
   const struct glsl_type *type = var->type;
   if (nir_is_arrayed_io(var, stage) || var->data.per_view) {
      assert(glsl_type_is_array(type));
      type = glsl_get_array_element(type);
   }

   return type;
}

static bool
varying_has_user_specified_location(const nir_variable *var)
{
   return var->data.explicit_location &&
      var->data.location >= VARYING_SLOT_VAR0;
}

static void
create_xfb_varying_names(void *mem_ctx, const struct glsl_type *t, char **name,
                         size_t name_length, unsigned *count,
                         const char *ifc_member_name,
                         const struct glsl_type *ifc_member_t,
                         char ***varying_names)
{
   if (glsl_type_is_interface(t)) {
      size_t new_length = name_length;

      assert(ifc_member_name && ifc_member_t);
      ralloc_asprintf_rewrite_tail(name, &new_length, ".%s", ifc_member_name);

      create_xfb_varying_names(mem_ctx, ifc_member_t, name, new_length, count,
                               NULL, NULL, varying_names);
   } else if (glsl_type_is_struct(t)) {
      for (unsigned i = 0; i < glsl_get_length(t); i++) {
         const char *field = glsl_get_struct_elem_name(t, i);
         size_t new_length = name_length;

         ralloc_asprintf_rewrite_tail(name, &new_length, ".%s", field);

         create_xfb_varying_names(mem_ctx, glsl_get_struct_field(t, i), name,
                                  new_length, count, NULL, NULL,
                                  varying_names);
      }
   } else if (glsl_type_is_struct(glsl_without_array(t)) ||
              glsl_type_is_interface(glsl_without_array(t)) ||
              (glsl_type_is_array(t) && glsl_type_is_array(glsl_get_array_element(t)))) {
      for (unsigned i = 0; i < glsl_get_length(t); i++) {
         size_t new_length = name_length;

         /* Append the subscript to the current variable name */
         ralloc_asprintf_rewrite_tail(name, &new_length, "[%u]", i);

         create_xfb_varying_names(mem_ctx, glsl_get_array_element(t), name,
                                  new_length, count, ifc_member_name,
                                  ifc_member_t, varying_names);
      }
   } else {
      (*varying_names)[(*count)++] = ralloc_strdup(mem_ctx, *name);
   }
}

static bool
process_xfb_layout_qualifiers(void *mem_ctx, const struct gl_linked_shader *sh,
                              struct gl_shader_program *prog,
                              unsigned *num_xfb_decls,
                              char ***varying_names)
{
   bool has_xfb_qualifiers = false;

   /* We still need to enable transform feedback mode even if xfb_stride is
    * only applied to a global out. Also we don't bother to propagate
    * xfb_stride to interface block members so this will catch that case also.
    */
   for (unsigned j = 0; j < MAX_FEEDBACK_BUFFERS; j++) {
      if (prog->TransformFeedback.BufferStride[j]) {
         has_xfb_qualifiers = true;
         break;
      }
   }

   nir_foreach_shader_out_variable(var, sh->Program->nir) {
      /* From the ARB_enhanced_layouts spec:
       *
       *    "Any shader making any static use (after preprocessing) of any of
       *     these *xfb_* qualifiers will cause the shader to be in a
       *     transform feedback capturing mode and hence responsible for
       *     describing the transform feedback setup.  This mode will capture
       *     any output selected by *xfb_offset*, directly or indirectly, to
       *     a transform feedback buffer."
       */
      if (var->data.explicit_xfb_buffer || var->data.explicit_xfb_stride) {
         has_xfb_qualifiers = true;
      }

      if (var->data.explicit_offset) {
         *num_xfb_decls += glsl_varying_count(var->type);
         has_xfb_qualifiers = true;
      }
   }

   if (*num_xfb_decls == 0)
      return has_xfb_qualifiers;

   unsigned i = 0;
   *varying_names = ralloc_array(mem_ctx, char *, *num_xfb_decls);
   nir_foreach_shader_out_variable(var, sh->Program->nir) {
      if (var->data.explicit_offset) {
         char *name;
         const struct glsl_type *type, *member_type;

         if (var->data.from_named_ifc_block) {
            type = var->interface_type;

            /* Find the member type before it was altered by lowering */
            const struct glsl_type *type_wa = glsl_without_array(type);
            member_type =
               glsl_get_struct_field(type_wa, glsl_get_field_index(type_wa, var->name));
            name = ralloc_strdup(NULL, glsl_get_type_name(type_wa));
         } else {
            type = var->type;
            member_type = NULL;
            name = ralloc_strdup(NULL, var->name);
         }
         create_xfb_varying_names(mem_ctx, type, &name, strlen(name), &i,
                                  var->name, member_type, varying_names);
         ralloc_free(name);
      }
   }

   assert(i == *num_xfb_decls);
   return has_xfb_qualifiers;
}

/**
 * Initialize this struct based on a string that was passed to
 * glTransformFeedbackVaryings.
 *
 * If the input is mal-formed, this call still succeeds, but it sets
 * this->var_name to a mal-formed input, so xfb_decl_find_output_var()
 * will fail to find any matching variable.
 */
static void
xfb_decl_init(struct xfb_decl *xfb_decl, const struct gl_constants *consts,
              const struct gl_extensions *exts, const void *mem_ctx,
              const char *input)
{
   /* We don't have to be pedantic about what is a valid GLSL variable name,
    * because any variable with an invalid name can't exist in the IR anyway.
    */
   xfb_decl->location = -1;
   xfb_decl->orig_name = input;
   xfb_decl->lowered_builtin_array_variable = none;
   xfb_decl->skip_components = 0;
   xfb_decl->next_buffer_separator = false;
   xfb_decl->matched_candidate = NULL;
   xfb_decl->stream_id = 0;
   xfb_decl->buffer = 0;
   xfb_decl->offset = 0;

   if (exts->ARB_transform_feedback3) {
      /* Parse gl_NextBuffer. */
      if (strcmp(input, "gl_NextBuffer") == 0) {
         xfb_decl->next_buffer_separator = true;
         return;
      }

      /* Parse gl_SkipComponents. */
      if (strcmp(input, "gl_SkipComponents1") == 0)
         xfb_decl->skip_components = 1;
      else if (strcmp(input, "gl_SkipComponents2") == 0)
         xfb_decl->skip_components = 2;
      else if (strcmp(input, "gl_SkipComponents3") == 0)
         xfb_decl->skip_components = 3;
      else if (strcmp(input, "gl_SkipComponents4") == 0)
         xfb_decl->skip_components = 4;

      if (xfb_decl->skip_components)
         return;
   }

   /* Parse a declaration. */
   const char *base_name_end;
   long subscript = link_util_parse_program_resource_name(input, strlen(input),
                                                          &base_name_end);
   xfb_decl->var_name = ralloc_strndup(mem_ctx, input, base_name_end - input);
   if (xfb_decl->var_name == NULL) {
      _mesa_error_no_memory(__func__);
      return;
   }

   if (subscript >= 0) {
      xfb_decl->array_subscript = subscript;
      xfb_decl->is_subscripted = true;
   } else {
      xfb_decl->is_subscripted = false;
   }

   /* For drivers that lower gl_ClipDistance to gl_ClipDistanceMESA, this
    * class must behave specially to account for the fact that gl_ClipDistance
    * is converted from a float[8] to a vec4[2].
    */
   if (consts->ShaderCompilerOptions[MESA_SHADER_VERTEX].LowerCombinedClipCullDistance &&
       strcmp(xfb_decl->var_name, "gl_ClipDistance") == 0) {
      xfb_decl->lowered_builtin_array_variable = clip_distance;
   }
   if (consts->ShaderCompilerOptions[MESA_SHADER_VERTEX].LowerCombinedClipCullDistance &&
       strcmp(xfb_decl->var_name, "gl_CullDistance") == 0) {
      xfb_decl->lowered_builtin_array_variable = cull_distance;
   }
}

/**
 * Determine whether two xfb_decl structs refer to the same variable and
 * array index (if applicable).
 */
static bool
xfb_decl_is_same(const struct xfb_decl *x, const struct xfb_decl *y)
{
   assert(xfb_decl_is_varying(x) && xfb_decl_is_varying(y));

   if (strcmp(x->var_name, y->var_name) != 0)
      return false;
   if (x->is_subscripted != y->is_subscripted)
      return false;
   if (x->is_subscripted && x->array_subscript != y->array_subscript)
      return false;
   return true;
}

/**
 * The total number of varying components taken up by this variable.  Only
 * valid if assign_location() has been called.
 */
static unsigned
xfb_decl_num_components(struct xfb_decl *xfb_decl)
{
   if (xfb_decl->lowered_builtin_array_variable)
      return xfb_decl->size;
   else
      return xfb_decl->vector_elements * xfb_decl->matrix_columns *
         xfb_decl->size * (_mesa_gl_datatype_is_64bit(xfb_decl->type) ? 2 : 1);
}

/**
 * Assign a location and stream ID for this xfb_decl object based on the
 * transform feedback candidate found by find_candidate.
 *
 * If an error occurs, the error is reported through linker_error() and false
 * is returned.
 */
static bool
xfb_decl_assign_location(struct xfb_decl *xfb_decl,
                         const struct gl_constants *consts,
                         struct gl_shader_program *prog,
                         bool disable_varying_packing, bool xfb_enabled)
{
   assert(xfb_decl_is_varying(xfb_decl));

   unsigned fine_location
      = xfb_decl->matched_candidate->toplevel_var->data.location * 4
      + xfb_decl->matched_candidate->toplevel_var->data.location_frac
      + xfb_decl->matched_candidate->struct_offset_floats;
   const unsigned dmul =
      glsl_type_is_64bit(glsl_without_array(xfb_decl->matched_candidate->type)) ? 2 : 1;

   if (glsl_type_is_array(xfb_decl->matched_candidate->type)) {
      /* Array variable */
      const struct glsl_type *element_type =
         glsl_get_array_element(xfb_decl->matched_candidate->type);
      const unsigned matrix_cols = glsl_get_matrix_columns(element_type);
      const unsigned vector_elements = glsl_get_vector_elements(element_type);
      unsigned actual_array_size;
      switch (xfb_decl->lowered_builtin_array_variable) {
      case clip_distance:
         actual_array_size = prog->last_vert_prog ?
            prog->last_vert_prog->info.clip_distance_array_size : 0;
         break;
      case cull_distance:
         actual_array_size = prog->last_vert_prog ?
            prog->last_vert_prog->info.cull_distance_array_size : 0;
         break;
      case none:
      default:
         actual_array_size = glsl_array_size(xfb_decl->matched_candidate->type);
         break;
      }

      if (xfb_decl->is_subscripted) {
         /* Check array bounds. */
         if (xfb_decl->array_subscript >= actual_array_size) {
            linker_error(prog, "Transform feedback varying %s has index "
                         "%i, but the array size is %u.",
                         xfb_decl->orig_name, xfb_decl->array_subscript,
                         actual_array_size);
            return false;
         }

         bool array_will_be_lowered =
            lower_packed_varying_needs_lowering(prog->last_vert_prog->nir,
                                                xfb_decl->matched_candidate->toplevel_var,
                                                nir_var_shader_out,
                                                disable_varying_packing,
                                                xfb_enabled) ||
            strcmp(xfb_decl->matched_candidate->toplevel_var->name, "gl_ClipDistance") == 0 ||
            strcmp(xfb_decl->matched_candidate->toplevel_var->name, "gl_CullDistance") == 0 ||
            strcmp(xfb_decl->matched_candidate->toplevel_var->name, "gl_TessLevelInner") == 0 ||
            strcmp(xfb_decl->matched_candidate->toplevel_var->name, "gl_TessLevelOuter") == 0;

         unsigned array_elem_size = xfb_decl->lowered_builtin_array_variable ?
            1 : (array_will_be_lowered ? vector_elements : 4) * matrix_cols * dmul;
         fine_location += array_elem_size * xfb_decl->array_subscript;
         xfb_decl->size = 1;
      } else {
         xfb_decl->size = actual_array_size;
      }
      xfb_decl->vector_elements = vector_elements;
      xfb_decl->matrix_columns = matrix_cols;
      if (xfb_decl->lowered_builtin_array_variable)
         xfb_decl->type = GL_FLOAT;
      else
         xfb_decl->type = glsl_get_gl_type(element_type);
   } else {
      /* Regular variable (scalar, vector, or matrix) */
      if (xfb_decl->is_subscripted) {
         linker_error(prog, "Transform feedback varying %s requested, "
                      "but %s is not an array.",
                      xfb_decl->orig_name, xfb_decl->var_name);
         return false;
      }
      xfb_decl->size = 1;
      xfb_decl->vector_elements = glsl_get_vector_elements(xfb_decl->matched_candidate->type);
      xfb_decl->matrix_columns = glsl_get_matrix_columns(xfb_decl->matched_candidate->type);
      xfb_decl->type = glsl_get_gl_type(xfb_decl->matched_candidate->type);
   }
   xfb_decl->location = fine_location / 4;
   xfb_decl->location_frac = fine_location % 4;

   /* From GL_EXT_transform_feedback:
    *   A program will fail to link if:
    *
    *   * the total number of components to capture in any varying
    *     variable in <varyings> is greater than the constant
    *     MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS_EXT and the
    *     buffer mode is SEPARATE_ATTRIBS_EXT;
    */
   if (prog->TransformFeedback.BufferMode == GL_SEPARATE_ATTRIBS &&
       xfb_decl_num_components(xfb_decl) >
       consts->MaxTransformFeedbackSeparateComponents) {
      linker_error(prog, "Transform feedback varying %s exceeds "
                   "MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS.",
                   xfb_decl->orig_name);
      return false;
   }

   /* Only transform feedback varyings can be assigned to non-zero streams,
    * so assign the stream id here.
    */
   xfb_decl->stream_id = xfb_decl->matched_candidate->toplevel_var->data.stream;

   unsigned array_offset = xfb_decl->array_subscript * 4 * dmul;
   unsigned struct_offset = xfb_decl->matched_candidate->xfb_offset_floats * 4;
   xfb_decl->buffer = xfb_decl->matched_candidate->toplevel_var->data.xfb.buffer;
   xfb_decl->offset = xfb_decl->matched_candidate->toplevel_var->data.offset +
      array_offset + struct_offset;

   return true;
}

static unsigned
xfb_decl_get_num_outputs(struct xfb_decl *xfb_decl)
{
   if (!xfb_decl_is_varying(xfb_decl)) {
      return 0;
   }

   if (varying_has_user_specified_location(xfb_decl->matched_candidate->toplevel_var)) {
      unsigned dmul = _mesa_gl_datatype_is_64bit(xfb_decl->type) ? 2 : 1;
      unsigned rows_per_element = DIV_ROUND_UP(xfb_decl->vector_elements * dmul, 4);
      return xfb_decl->size * xfb_decl->matrix_columns * rows_per_element;
   } else {
      return (xfb_decl_num_components(xfb_decl) + xfb_decl->location_frac + 3) / 4;
   }
}

static bool
xfb_decl_is_varying_written(struct xfb_decl *xfb_decl)
{
   if (xfb_decl->next_buffer_separator || xfb_decl->skip_components)
      return false;

   return xfb_decl->matched_candidate->toplevel_var->data.assigned;
}

/**
 * Update gl_transform_feedback_info to reflect this xfb_decl.
 *
 * If an error occurs, the error is reported through linker_error() and false
 * is returned.
 */
static bool
xfb_decl_store(struct xfb_decl *xfb_decl, const struct gl_constants *consts,
               struct gl_shader_program *prog,
               struct gl_transform_feedback_info *info,
               unsigned buffer, unsigned buffer_index,
               const unsigned max_outputs,
               BITSET_WORD *used_components[MAX_FEEDBACK_BUFFERS],
               bool *explicit_stride, unsigned *max_member_alignment,
               bool has_xfb_qualifiers, const void* mem_ctx)
{
   unsigned xfb_offset = 0;
   unsigned size = xfb_decl->size;
   /* Handle gl_SkipComponents. */
   if (xfb_decl->skip_components) {
      info->Buffers[buffer].Stride += xfb_decl->skip_components;
      size = xfb_decl->skip_components;
      goto store_varying;
   }

   if (xfb_decl->next_buffer_separator) {
      size = 0;
      goto store_varying;
   }

   if (has_xfb_qualifiers) {
      xfb_offset = xfb_decl->offset / 4;
   } else {
      xfb_offset = info->Buffers[buffer].Stride;
   }
   info->Varyings[info->NumVarying].Offset = xfb_offset * 4;

   {
      unsigned location = xfb_decl->location;
      unsigned location_frac = xfb_decl->location_frac;
      unsigned num_components = xfb_decl_num_components(xfb_decl);

      /* From GL_EXT_transform_feedback:
       *
       *   " A program will fail to link if:
       *
       *       * the total number of components to capture is greater than the
       *         constant MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS_EXT
       *         and the buffer mode is INTERLEAVED_ATTRIBS_EXT."
       *
       * From GL_ARB_enhanced_layouts:
       *
       *   " The resulting stride (implicit or explicit) must be less than or
       *     equal to the implementation-dependent constant
       *     gl_MaxTransformFeedbackInterleavedComponents."
       */
      if ((prog->TransformFeedback.BufferMode == GL_INTERLEAVED_ATTRIBS ||
           has_xfb_qualifiers) &&
          xfb_offset + num_components >
          consts->MaxTransformFeedbackInterleavedComponents) {
         linker_error(prog,
                      "The MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS "
                      "limit has been exceeded.");
         return false;
      }

      /* From the OpenGL 4.60.5 spec, section 4.4.2. Output Layout Qualifiers,
       * Page 76, (Transform Feedback Layout Qualifiers):
       *
       *   " No aliasing in output buffers is allowed: It is a compile-time or
       *     link-time error to specify variables with overlapping transform
       *     feedback offsets."
       */
      const unsigned max_components =
         consts->MaxTransformFeedbackInterleavedComponents;
      const unsigned first_component = xfb_offset;
      const unsigned last_component = xfb_offset + num_components - 1;
      const unsigned start_word = BITSET_BITWORD(first_component);
      const unsigned end_word = BITSET_BITWORD(last_component);
      BITSET_WORD *used;
      assert(last_component < max_components);

      if (!used_components[buffer]) {
         used_components[buffer] =
            rzalloc_array(mem_ctx, BITSET_WORD, BITSET_WORDS(max_components));
      }
      used = used_components[buffer];

      for (unsigned word = start_word; word <= end_word; word++) {
         unsigned start_range = 0;
         unsigned end_range = BITSET_WORDBITS - 1;

         if (word == start_word)
            start_range = first_component % BITSET_WORDBITS;

         if (word == end_word)
            end_range = last_component % BITSET_WORDBITS;

         if (used[word] & BITSET_RANGE(start_range, end_range)) {
            linker_error(prog,
                         "variable '%s', xfb_offset (%d) is causing aliasing.",
                         xfb_decl->orig_name, xfb_offset * 4);
            return false;
         }
         used[word] |= BITSET_RANGE(start_range, end_range);
      }

      const unsigned type_num_components =
         xfb_decl->vector_elements *
         (_mesa_gl_datatype_is_64bit(xfb_decl->type) ? 2 : 1);
      unsigned current_type_components_left = type_num_components;

      while (num_components > 0) {
         unsigned output_size = 0;

         /*  From GL_ARB_enhanced_layouts:
          *
          * "When an attribute variable declared using an array type is bound to
          * generic attribute index <i>, the active array elements are assigned to
          * consecutive generic attributes beginning with generic attribute <i>.  The
          * number of attributes and components assigned to each element are
          * determined according to the data type of array elements and "component"
          * layout qualifier (if any) specified in the declaration of the array."
          *
          * "When an attribute variable declared using a matrix type is bound to a
          * generic attribute index <i>, its values are taken from consecutive generic
          * attributes beginning with generic attribute <i>.  Such matrices are
          * treated as an array of column vectors with values taken from the generic
          * attributes.
          * This means there may be gaps in the varyings we are taking values from."
          *
          * Examples:
          *
          * | layout(location=0) dvec3[2] a; | layout(location=4) vec2[4] b; |
          * |                                |                               |
          * |        32b 32b 32b 32b         |        32b 32b 32b 32b        |
          * |      0  X   X   Y   Y          |      4  X   Y   0   0         |
          * |      1  Z   Z   0   0          |      5  X   Y   0   0         |
          * |      2  X   X   Y   Y          |      6  X   Y   0   0         |
          * |      3  Z   Z   0   0          |      7  X   Y   0   0         |
          *
          */
         if (varying_has_user_specified_location(xfb_decl->matched_candidate->toplevel_var)) {
            output_size = MIN3(num_components, current_type_components_left, 4);
            current_type_components_left -= output_size;
            if (current_type_components_left == 0) {
               current_type_components_left = type_num_components;
            }
         } else {
            output_size = MIN2(num_components, 4 - location_frac);
         }

         assert((info->NumOutputs == 0 && max_outputs == 0) ||
                info->NumOutputs < max_outputs);

         /* From the ARB_enhanced_layouts spec:
          *
          *    "If such a block member or variable is not written during a shader
          *    invocation, the buffer contents at the assigned offset will be
          *    undefined.  Even if there are no static writes to a variable or
          *    member that is assigned a transform feedback offset, the space is
          *    still allocated in the buffer and still affects the stride."
          */
         if (xfb_decl_is_varying_written(xfb_decl)) {
            info->Outputs[info->NumOutputs].ComponentOffset = location_frac;
            info->Outputs[info->NumOutputs].OutputRegister = location;
            info->Outputs[info->NumOutputs].NumComponents = output_size;
            info->Outputs[info->NumOutputs].StreamId = xfb_decl->stream_id;
            info->Outputs[info->NumOutputs].OutputBuffer = buffer;
            info->Outputs[info->NumOutputs].DstOffset = xfb_offset;
            ++info->NumOutputs;
         }
         info->Buffers[buffer].Stream = xfb_decl->stream_id;
         xfb_offset += output_size;

         num_components -= output_size;
         location++;
         location_frac = 0;
      }
   }

   if (explicit_stride && explicit_stride[buffer]) {
      if (_mesa_gl_datatype_is_64bit(xfb_decl->type) &&
          info->Buffers[buffer].Stride % 2) {
         linker_error(prog, "invalid qualifier xfb_stride=%d must be a "
                      "multiple of 8 as its applied to a type that is or "
                      "contains a double.",
                      info->Buffers[buffer].Stride * 4);
         return false;
      }

      if (xfb_offset > info->Buffers[buffer].Stride) {
         linker_error(prog, "xfb_offset (%d) overflows xfb_stride (%d) for "
                      "buffer (%d)", xfb_offset * 4,
                      info->Buffers[buffer].Stride * 4, buffer);
         return false;
      }
   } else {
      if (max_member_alignment && has_xfb_qualifiers) {
         max_member_alignment[buffer] = MAX2(max_member_alignment[buffer],
                                             _mesa_gl_datatype_is_64bit(xfb_decl->type) ? 2 : 1);
         info->Buffers[buffer].Stride = ALIGN(xfb_offset,
                                              max_member_alignment[buffer]);
      } else {
         info->Buffers[buffer].Stride = xfb_offset;
      }
   }

 store_varying:
   info->Varyings[info->NumVarying].name.string =
      ralloc_strdup(prog, xfb_decl->orig_name);
   resource_name_updated(&info->Varyings[info->NumVarying].name);
   info->Varyings[info->NumVarying].Type = xfb_decl->type;
   info->Varyings[info->NumVarying].Size = size;
   info->Varyings[info->NumVarying].BufferIndex = buffer_index;
   info->NumVarying++;
   info->Buffers[buffer].NumVaryings++;

   return true;
}

static const struct tfeedback_candidate *
xfb_decl_find_candidate(struct xfb_decl *xfb_decl,
                        struct gl_shader_program *prog,
                        struct hash_table *tfeedback_candidates)
{
   const char *name = xfb_decl->var_name;
   switch (xfb_decl->lowered_builtin_array_variable) {
   case none:
      name = xfb_decl->var_name;
      break;
   case clip_distance:
   case cull_distance:
      name = "gl_ClipDistanceMESA";
      break;
   }
   struct hash_entry *entry =
      _mesa_hash_table_search(tfeedback_candidates, name);

   xfb_decl->matched_candidate = entry ?
         (struct tfeedback_candidate *) entry->data : NULL;

   if (!xfb_decl->matched_candidate) {
      /* From GL_EXT_transform_feedback:
       *   A program will fail to link if:
       *
       *   * any variable name specified in the <varyings> array is not
       *     declared as an output in the geometry shader (if present) or
       *     the vertex shader (if no geometry shader is present);
       */
      linker_error(prog, "Transform feedback varying %s undeclared.",
                   xfb_decl->orig_name);
   }

   return xfb_decl->matched_candidate;
}

/**
 * Force a candidate over the previously matched one. It happens when a new
 * varying needs to be created to match the xfb declaration, for example,
 * to fullfil an alignment criteria.
 */
static void
xfb_decl_set_lowered_candidate(struct xfb_decl *xfb_decl,
                               struct tfeedback_candidate *candidate)
{
   xfb_decl->matched_candidate = candidate;

   /* The subscript part is no longer relevant */
   xfb_decl->is_subscripted = false;
   xfb_decl->array_subscript = 0;
}

/**
 * Parse all the transform feedback declarations that were passed to
 * glTransformFeedbackVaryings() and store them in xfb_decl objects.
 *
 * If an error occurs, the error is reported through linker_error() and false
 * is returned.
 */
static bool
parse_xfb_decls(const struct gl_constants *consts,
                const struct gl_extensions *exts,
                struct gl_shader_program *prog,
                const void *mem_ctx, unsigned num_names,
                char **varying_names, struct xfb_decl *decls)
{
   for (unsigned i = 0; i < num_names; ++i) {
      xfb_decl_init(&decls[i], consts, exts, mem_ctx, varying_names[i]);

      if (!xfb_decl_is_varying(&decls[i]))
         continue;

      /* From GL_EXT_transform_feedback:
       *   A program will fail to link if:
       *
       *   * any two entries in the <varyings> array specify the same varying
       *     variable;
       *
       * We interpret this to mean "any two entries in the <varyings> array
       * specify the same varying variable and array index", since transform
       * feedback of arrays would be useless otherwise.
       */
      for (unsigned j = 0; j < i; ++j) {
         if (xfb_decl_is_varying(&decls[j])) {
            if (xfb_decl_is_same(&decls[i], &decls[j])) {
               linker_error(prog, "Transform feedback varying %s specified "
                            "more than once.", varying_names[i]);
               return false;
            }
         }
      }
   }
   return true;
}

static int
cmp_xfb_offset(const void * x_generic, const void * y_generic)
{
   struct xfb_decl *x = (struct xfb_decl *) x_generic;
   struct xfb_decl *y = (struct xfb_decl *) y_generic;

   if (x->buffer != y->buffer)
      return x->buffer - y->buffer;
   return x->offset - y->offset;
}

/**
 * Store transform feedback location assignments into
 * prog->sh.LinkedTransformFeedback based on the data stored in
 * xfb_decls.
 *
 * If an error occurs, the error is reported through linker_error() and false
 * is returned.
 */
static bool
store_tfeedback_info(const struct gl_constants *consts,
                     struct gl_shader_program *prog, unsigned num_xfb_decls,
                     struct xfb_decl *xfb_decls, bool has_xfb_qualifiers,
                     const void *mem_ctx)
{
   if (!prog->last_vert_prog)
      return true;

   /* Make sure MaxTransformFeedbackBuffers is less than 32 so the bitmask for
    * tracking the number of buffers doesn't overflow.
    */
   assert(consts->MaxTransformFeedbackBuffers < 32);

   bool separate_attribs_mode =
      prog->TransformFeedback.BufferMode == GL_SEPARATE_ATTRIBS;

   struct gl_program *xfb_prog = prog->last_vert_prog;
   xfb_prog->sh.LinkedTransformFeedback =
      rzalloc(xfb_prog, struct gl_transform_feedback_info);

   /* The xfb_offset qualifier does not have to be used in increasing order
    * however some drivers expect to receive the list of transform feedback
    * declarations in order so sort it now for convenience.
    */
   if (has_xfb_qualifiers) {
      qsort(xfb_decls, num_xfb_decls, sizeof(*xfb_decls),
            cmp_xfb_offset);
   }

   xfb_prog->sh.LinkedTransformFeedback->Varyings =
      rzalloc_array(xfb_prog, struct gl_transform_feedback_varying_info,
                    num_xfb_decls);

   unsigned num_outputs = 0;
   for (unsigned i = 0; i < num_xfb_decls; ++i) {
      if (xfb_decl_is_varying_written(&xfb_decls[i]))
         num_outputs += xfb_decl_get_num_outputs(&xfb_decls[i]);
   }

   xfb_prog->sh.LinkedTransformFeedback->Outputs =
      rzalloc_array(xfb_prog, struct gl_transform_feedback_output,
                    num_outputs);

   unsigned num_buffers = 0;
   unsigned buffers = 0;
   BITSET_WORD *used_components[MAX_FEEDBACK_BUFFERS] = {0};

   if (!has_xfb_qualifiers && separate_attribs_mode) {
      /* GL_SEPARATE_ATTRIBS */
      for (unsigned i = 0; i < num_xfb_decls; ++i) {
         if (!xfb_decl_store(&xfb_decls[i], consts, prog,
                             xfb_prog->sh.LinkedTransformFeedback,
                             num_buffers, num_buffers, num_outputs,
                             used_components, NULL, NULL, has_xfb_qualifiers,
                             mem_ctx))
            return false;

         buffers |= 1 << num_buffers;
         num_buffers++;
      }
   }
   else {
      /* GL_INVERLEAVED_ATTRIBS */
      int buffer_stream_id = -1;
      unsigned buffer =
         num_xfb_decls ? xfb_decls[0].buffer : 0;
      bool explicit_stride[MAX_FEEDBACK_BUFFERS] = { false };
      unsigned max_member_alignment[MAX_FEEDBACK_BUFFERS] = { 1, 1, 1, 1 };
      /* Apply any xfb_stride global qualifiers */
      if (has_xfb_qualifiers) {
         for (unsigned j = 0; j < MAX_FEEDBACK_BUFFERS; j++) {
            if (prog->TransformFeedback.BufferStride[j]) {
               explicit_stride[j] = true;
               xfb_prog->sh.LinkedTransformFeedback->Buffers[j].Stride =
                  prog->TransformFeedback.BufferStride[j] / 4;
            }
         }
      }

      for (unsigned i = 0; i < num_xfb_decls; ++i) {
         if (has_xfb_qualifiers &&
             buffer != xfb_decls[i].buffer) {
            /* we have moved to the next buffer so reset stream id */
            buffer_stream_id = -1;
            num_buffers++;
         }

         if (xfb_decls[i].next_buffer_separator) {
            if (!xfb_decl_store(&xfb_decls[i], consts, prog,
                                xfb_prog->sh.LinkedTransformFeedback,
                                buffer, num_buffers, num_outputs,
                                used_components, explicit_stride,
                                max_member_alignment, has_xfb_qualifiers,
                                mem_ctx))
               return false;
            num_buffers++;
            buffer_stream_id = -1;
            continue;
         }

         if (has_xfb_qualifiers) {
            buffer = xfb_decls[i].buffer;
         } else {
            buffer = num_buffers;
         }

         if (xfb_decl_is_varying(&xfb_decls[i])) {
            if (buffer_stream_id == -1)  {
               /* First varying writing to this buffer: remember its stream */
               buffer_stream_id = (int) xfb_decls[i].stream_id;

               /* Only mark a buffer as active when there is a varying
                * attached to it. This behaviour is based on a revised version
                * of section 13.2.2 of the GL 4.6 spec.
                */
               buffers |= 1 << buffer;
            } else if (buffer_stream_id !=
                       (int) xfb_decls[i].stream_id) {
               /* Varying writes to the same buffer from a different stream */
               linker_error(prog,
                            "Transform feedback can't capture varyings belonging "
                            "to different vertex streams in a single buffer. "
                            "Varying %s writes to buffer from stream %u, other "
                            "varyings in the same buffer write from stream %u.",
                            xfb_decls[i].orig_name,
                            xfb_decls[i].stream_id,
                            buffer_stream_id);
               return false;
            }
         }

         if (!xfb_decl_store(&xfb_decls[i], consts, prog,
                             xfb_prog->sh.LinkedTransformFeedback,
                             buffer, num_buffers, num_outputs, used_components,
                             explicit_stride, max_member_alignment,
                             has_xfb_qualifiers, mem_ctx))
            return false;
      }
   }
   assert(xfb_prog->sh.LinkedTransformFeedback->NumOutputs == num_outputs);

   xfb_prog->sh.LinkedTransformFeedback->ActiveBuffers = buffers;
   return true;
}

/**
 * Enum representing the order in which varyings are packed within a
 * packing class.
 *
 * Currently we pack vec4's first, then vec2's, then scalar values, then
 * vec3's.  This order ensures that the only vectors that are at risk of
 * having to be "double parked" (split between two adjacent varying slots)
 * are the vec3's.
 */
enum packing_order_enum {
   PACKING_ORDER_VEC4,
   PACKING_ORDER_VEC2,
   PACKING_ORDER_SCALAR,
   PACKING_ORDER_VEC3,
};

/**
 * Structure recording the relationship between a single producer output
 * and a single consumer input.
 */
struct match {
   /**
    * Packing class for this varying, computed by compute_packing_class().
    */
   unsigned packing_class;

   /**
    * Packing order for this varying, computed by compute_packing_order().
    */
   enum packing_order_enum packing_order;

   /**
    * The output variable in the producer stage.
    */
   nir_variable *producer_var;

   /**
    * The input variable in the consumer stage.
    */
   nir_variable *consumer_var;

   /**
    * The location which has been assigned for this varying.  This is
    * expressed in multiples of a float, with the first generic varying
    * (i.e. the one referred to by VARYING_SLOT_VAR0) represented by the
    * value 0.
    */
   unsigned generic_location;
};

/**
 * Data structure recording the relationship between outputs of one shader
 * stage (the "producer") and inputs of another (the "consumer").
 */
struct varying_matches
{
   /**
    * If true, this driver disables varying packing, so all varyings need to
    * be aligned on slot boundaries, and take up a number of slots equal to
    * their number of matrix columns times their array size.
    *
    * Packing may also be disabled because our current packing method is not
    * safe in SSO or versions of OpenGL where interpolation qualifiers are not
    * guaranteed to match across stages.
    */
   bool disable_varying_packing;

   /**
    * If true, this driver disables packing for varyings used by transform
    * feedback.
    */
   bool disable_xfb_packing;

   /**
    * If true, this driver has transform feedback enabled. The transform
    * feedback code usually requires at least some packing be done even
    * when varying packing is disabled, fortunately where transform feedback
    * requires packing it's safe to override the disabled setting. See
    * is_varying_packing_safe().
    */
   bool xfb_enabled;

   bool enhanced_layouts_enabled;

   /**
    * If true, this driver prefers varyings to be aligned to power of two
    * in a slot.
    */
   bool prefer_pot_aligned_varyings;

   struct match *matches;

   /**
    * The number of elements in the \c matches array that are currently in
    * use.
    */
   unsigned num_matches;

   /**
    * The number of elements that were set aside for the \c matches array when
    * it was allocated.
    */
   unsigned matches_capacity;

   gl_shader_stage producer_stage;
   gl_shader_stage consumer_stage;
};

/**
 * Comparison function passed to qsort() to sort varyings by packing_class and
 * then by packing_order.
 */
static int
varying_matches_match_comparator(const void *x_generic, const void *y_generic)
{
   const struct match *x = (const struct match *) x_generic;
   const struct match *y = (const struct match *) y_generic;

   if (x->packing_class != y->packing_class)
      return x->packing_class - y->packing_class;
   return x->packing_order - y->packing_order;
}

/**
 * Comparison function passed to qsort() to sort varyings used only by
 * transform feedback when packing of other varyings is disabled.
 */
static int
varying_matches_xfb_comparator(const void *x_generic, const void *y_generic)
{
   const struct match *x = (const struct match *) x_generic;

   if (x->producer_var != NULL && x->producer_var->data.is_xfb_only)
      return varying_matches_match_comparator(x_generic, y_generic);

   /* FIXME: When the comparator returns 0 it means the elements being
    * compared are equivalent. However the qsort documentation says:
    *
    *    "The order of equivalent elements is undefined."
    *
    * In practice the sort ends up reversing the order of the varyings which
    * means locations are also assigned in this reversed order and happens to
    * be what we want. This is also whats happening in
    * varying_matches_match_comparator().
    */
   return 0;
}

/**
 * Comparison function passed to qsort() to sort varyings NOT used by
 * transform feedback when packing of xfb varyings is disabled.
 */
static int
varying_matches_not_xfb_comparator(const void *x_generic, const void *y_generic)
{
   const struct match *x = (const struct match *) x_generic;

   if (x->producer_var != NULL && !x->producer_var->data.is_xfb)
      return varying_matches_match_comparator(x_generic, y_generic);

   /* FIXME: When the comparator returns 0 it means the elements being
    * compared are equivalent. However the qsort documentation says:
    *
    *    "The order of equivalent elements is undefined."
    *
    * In practice the sort ends up reversing the order of the varyings which
    * means locations are also assigned in this reversed order and happens to
    * be what we want. This is also whats happening in
    * varying_matches_match_comparator().
    */
   return 0;
}

static bool
is_unpackable_tess(gl_shader_stage producer_stage,
                   gl_shader_stage consumer_stage)
{
   if (consumer_stage == MESA_SHADER_TESS_EVAL ||
       consumer_stage == MESA_SHADER_TESS_CTRL ||
       producer_stage == MESA_SHADER_TESS_CTRL)
      return true;

   return false;
}

static void
init_varying_matches(void *mem_ctx, struct varying_matches *vm,
                     const struct gl_constants *consts,
                     const struct gl_extensions *exts,
                     gl_shader_stage producer_stage,
                     gl_shader_stage consumer_stage,
                     bool sso)
{
   /* Tessellation shaders treat inputs and outputs as shared memory and can
    * access inputs and outputs of other invocations.
    * Therefore, they can't be lowered to temps easily (and definitely not
    * efficiently).
    */
   bool unpackable_tess =
      is_unpackable_tess(producer_stage, consumer_stage);

   /* Transform feedback code assumes varying arrays are packed, so if the
    * driver has disabled varying packing, make sure to at least enable
    * packing required by transform feedback. See below for exception.
    */
   bool xfb_enabled = exts->EXT_transform_feedback && !unpackable_tess;

   /* Some drivers actually requires packing to be explicitly disabled
    * for varyings used by transform feedback.
    */
   bool disable_xfb_packing = consts->DisableTransformFeedbackPacking;

   /* Disable packing on outward facing interfaces for SSO because in ES we
    * need to retain the unpacked varying information for draw time
    * validation.
    *
    * Packing is still enabled on individual arrays, structs, and matrices as
    * these are required by the transform feedback code and it is still safe
    * to do so. We also enable packing when a varying is only used for
    * transform feedback and its not a SSO.
    */
   bool disable_varying_packing =
      consts->DisableVaryingPacking || unpackable_tess;
   if (sso && (producer_stage == MESA_SHADER_NONE || consumer_stage == MESA_SHADER_NONE))
      disable_varying_packing = true;

   /* Note: this initial capacity is rather arbitrarily chosen to be large
    * enough for many cases without wasting an unreasonable amount of space.
    * varying_matches_record() will resize the array if there are more than
    * this number of varyings.
    */
   vm->matches_capacity = 8;
   vm->matches = (struct match *)
      ralloc_array(mem_ctx, struct match, vm->matches_capacity);
   vm->num_matches = 0;

   vm->disable_varying_packing = disable_varying_packing;
   vm->disable_xfb_packing = disable_xfb_packing;
   vm->xfb_enabled = xfb_enabled;
   vm->enhanced_layouts_enabled = exts->ARB_enhanced_layouts;
   vm->prefer_pot_aligned_varyings = consts->PreferPOTAlignedVaryings;
   vm->producer_stage = producer_stage;
   vm->consumer_stage = consumer_stage;
}

/**
 * Packing is always safe on individual arrays, structures, and matrices. It
 * is also safe if the varying is only used for transform feedback.
 */
static bool
is_varying_packing_safe(struct varying_matches *vm,
                        const struct glsl_type *type, const nir_variable *var)
{
   if (is_unpackable_tess(vm->producer_stage, vm->consumer_stage))
      return false;

   return vm->xfb_enabled && (glsl_type_is_array_or_matrix(type) ||
                              glsl_type_is_struct(type) ||
                              var->data.is_xfb_only);
}

static bool
is_packing_disabled(struct varying_matches *vm, const struct glsl_type *type,
                    const nir_variable *var)
{
   return (vm->disable_varying_packing && !is_varying_packing_safe(vm, type, var)) ||
      (vm->disable_xfb_packing && var->data.is_xfb &&
       !(glsl_type_is_array(type) || glsl_type_is_struct(type) ||
         glsl_type_is_matrix(type))) || var->data.must_be_shader_input;
}

/**
 * Compute the "packing class" of the given varying.  This is an unsigned
 * integer with the property that two variables in the same packing class can
 * be safely backed into the same vec4.
 */
static unsigned
varying_matches_compute_packing_class(const nir_variable *var)
{
   /* Without help from the back-end, there is no way to pack together
    * variables with different interpolation types, because
    * lower_packed_varyings must choose exactly one interpolation type for
    * each packed varying it creates.
    *
    * However, we can safely pack together floats, ints, and uints, because:
    *
    * - varyings of base type "int" and "uint" must use the "flat"
    *   interpolation type, which can only occur in GLSL 1.30 and above.
    *
    * - On platforms that support GLSL 1.30 and above, lower_packed_varyings
    *   can store flat floats as ints without losing any information (using
    *   the ir_unop_bitcast_* opcodes).
    *
    * Therefore, the packing class depends only on the interpolation type.
    */
   bool is_interpolation_flat = var->data.interpolation == INTERP_MODE_FLAT ||
      glsl_contains_integer(var->type) || glsl_contains_double(var->type);

   const unsigned interp = is_interpolation_flat
      ? (unsigned) INTERP_MODE_FLAT : var->data.interpolation;

   assert(interp < (1 << 3));

   const unsigned packing_class = (interp << 0) |
                                  (var->data.centroid << 3) |
                                  (var->data.sample << 4) |
                                  (var->data.patch << 5) |
                                  (var->data.must_be_shader_input << 6);

   return packing_class;
}

/**
 * Compute the "packing order" of the given varying.  This is a sort key we
 * use to determine when to attempt to pack the given varying relative to
 * other varyings in the same packing class.
 */
static enum packing_order_enum
varying_matches_compute_packing_order(const nir_variable *var)
{
   const struct glsl_type *element_type = glsl_without_array(var->type);

   switch (glsl_get_component_slots(element_type) % 4) {
   case 1: return PACKING_ORDER_SCALAR;
   case 2: return PACKING_ORDER_VEC2;
   case 3: return PACKING_ORDER_VEC3;
   case 0: return PACKING_ORDER_VEC4;
   default:
      assert(!"Unexpected value of vector_elements");
      return PACKING_ORDER_VEC4;
   }
}

/**
 * Built-in / reserved GL variables names start with "gl_"
 */
static bool
is_gl_identifier(const char *s)
{
   return s && s[0] == 'g' && s[1] == 'l' && s[2] == '_';
}

/**
 * Record the given producer/consumer variable pair in the list of variables
 * that should later be assigned locations.
 *
 * It is permissible for \c consumer_var to be NULL (this happens if a
 * variable is output by the producer and consumed by transform feedback, but
 * not consumed by the consumer).
 *
 * If \c producer_var has already been paired up with a consumer_var, or
 * producer_var is part of fixed pipeline functionality (and hence already has
 * a location assigned), this function has no effect.
 *
 * Note: as a side effect this function may change the interpolation type of
 * \c producer_var, but only when the change couldn't possibly affect
 * rendering.
 */
static void
varying_matches_record(void *mem_ctx, struct varying_matches *vm,
                       nir_variable *producer_var, nir_variable *consumer_var)
{
   assert(producer_var != NULL || consumer_var != NULL);

   if ((producer_var &&
       (producer_var->data.explicit_location || producer_var->data.location != -1)) ||
       (consumer_var &&
        (consumer_var->data.explicit_location || consumer_var->data.location != -1))) {
      /* Either a location already exists for this variable (since it is part
       * of fixed functionality), or it has already been assigned explicitly.
       */
      return;
   }

   /* The varyings should not have been matched and assgned previously */
   assert((producer_var == NULL || producer_var->data.location == -1) &&
          (consumer_var == NULL || consumer_var->data.location == -1));

   bool needs_flat_qualifier = consumer_var == NULL &&
      (glsl_contains_integer(producer_var->type) ||
       glsl_contains_double(producer_var->type));

   if (!vm->disable_varying_packing &&
       (!vm->disable_xfb_packing || producer_var  == NULL || !producer_var->data.is_xfb) &&
       (needs_flat_qualifier ||
        (vm->consumer_stage != MESA_SHADER_NONE && vm->consumer_stage != MESA_SHADER_FRAGMENT))) {
      /* Since this varying is not being consumed by the fragment shader, its
       * interpolation type varying cannot possibly affect rendering.
       * Also, this variable is non-flat and is (or contains) an integer
       * or a double.
       * If the consumer stage is unknown, don't modify the interpolation
       * type as it could affect rendering later with separate shaders.
       *
       * lower_packed_varyings requires all integer varyings to flat,
       * regardless of where they appear.  We can trivially satisfy that
       * requirement by changing the interpolation type to flat here.
       */
      if (producer_var) {
         producer_var->data.centroid = false;
         producer_var->data.sample = false;
         producer_var->data.interpolation = INTERP_MODE_FLAT;
      }

      if (consumer_var) {
         consumer_var->data.centroid = false;
         consumer_var->data.sample = false;
         consumer_var->data.interpolation = INTERP_MODE_FLAT;
      }
   }

   if (vm->num_matches == vm->matches_capacity) {
      vm->matches_capacity *= 2;
      vm->matches = (struct match *)
         reralloc(mem_ctx, vm->matches, struct match, vm->matches_capacity);
   }

   /* We must use the consumer to compute the packing class because in GL4.4+
    * there is no guarantee interpolation qualifiers will match across stages.
    *
    * From Section 4.5 (Interpolation Qualifiers) of the GLSL 4.30 spec:
    *
    *    "The type and presence of interpolation qualifiers of variables with
    *    the same name declared in all linked shaders for the same cross-stage
    *    interface must match, otherwise the link command will fail.
    *
    *    When comparing an output from one stage to an input of a subsequent
    *    stage, the input and output don't match if their interpolation
    *    qualifiers (or lack thereof) are not the same."
    *
    * This text was also in at least revison 7 of the 4.40 spec but is no
    * longer in revision 9 and not in the 4.50 spec.
    */
   const nir_variable *const var = (consumer_var != NULL)
      ? consumer_var : producer_var;

   if (producer_var && consumer_var &&
       consumer_var->data.must_be_shader_input) {
      producer_var->data.must_be_shader_input = 1;
   }

   vm->matches[vm->num_matches].packing_class
      = varying_matches_compute_packing_class(var);
   vm->matches[vm->num_matches].packing_order
      = varying_matches_compute_packing_order(var);

   vm->matches[vm->num_matches].producer_var = producer_var;
   vm->matches[vm->num_matches].consumer_var = consumer_var;
   vm->num_matches++;
}

/**
 * Choose locations for all of the variable matches that were previously
 * passed to varying_matches_record().
 * \param components  returns array[slot] of number of components used
 *                    per slot (1, 2, 3 or 4)
 * \param reserved_slots  bitmask indicating which varying slots are already
 *                        allocated
 * \return number of slots (4-element vectors) allocated
 */
static unsigned
varying_matches_assign_locations(struct varying_matches *vm,
                                 struct gl_shader_program *prog,
                                 uint8_t components[], uint64_t reserved_slots)
{
   /* If packing has been disabled then we cannot safely sort the varyings by
    * class as it may mean we are using a version of OpenGL where
    * interpolation qualifiers are not guaranteed to be matching across
    * shaders, sorting in this case could result in mismatching shader
    * interfaces.
    * When packing is disabled the sort orders varyings used by transform
    * feedback first, but also depends on *undefined behaviour* of qsort to
    * reverse the order of the varyings. See: xfb_comparator().
    *
    * If packing is only disabled for xfb varyings (mutually exclusive with
    * disable_varying_packing), we then group varyings depending on if they
    * are captured for transform feedback. The same *undefined behaviour* is
    * taken advantage of.
    */
   if (vm->disable_varying_packing) {
      /* Only sort varyings that are only used by transform feedback. */
      qsort(vm->matches, vm->num_matches, sizeof(*vm->matches),
            &varying_matches_xfb_comparator);
   } else if (vm->disable_xfb_packing) {
      /* Only sort varyings that are NOT used by transform feedback. */
      qsort(vm->matches, vm->num_matches, sizeof(*vm->matches),
            &varying_matches_not_xfb_comparator);
   } else {
      /* Sort varying matches into an order that makes them easy to pack. */
      qsort(vm->matches, vm->num_matches, sizeof(*vm->matches),
            &varying_matches_match_comparator);
   }

   unsigned generic_location = 0;
   unsigned generic_patch_location = MAX_VARYING*4;
   bool previous_var_xfb = false;
   bool previous_var_xfb_only = false;
   unsigned previous_packing_class = ~0u;

   /* For tranform feedback separate mode, we know the number of attributes
    * is <= the number of buffers.  So packing isn't critical.  In fact,
    * packing vec3 attributes can cause trouble because splitting a vec3
    * effectively creates an additional transform feedback output.  The
    * extra TFB output may exceed device driver limits.
    *
    * Also don't pack vec3 if the driver prefers power of two aligned
    * varyings. Packing order guarantees that vec4, vec2 and vec1 will be
    * pot-aligned, we only need to take care of vec3s
    */
   const bool dont_pack_vec3 =
      (prog->TransformFeedback.BufferMode == GL_SEPARATE_ATTRIBS &&
       prog->TransformFeedback.NumVarying > 0) ||
      vm->prefer_pot_aligned_varyings;

   for (unsigned i = 0; i < vm->num_matches; i++) {
      unsigned *location = &generic_location;
      const nir_variable *var;
      const struct glsl_type *type;
      bool is_vertex_input = false;

      if (vm->matches[i].consumer_var) {
         var = vm->matches[i].consumer_var;
         type = get_varying_type(var, vm->consumer_stage);
         if (vm->consumer_stage == MESA_SHADER_VERTEX)
            is_vertex_input = true;
      } else {
         if (!vm->matches[i].producer_var)
            continue; /* The varying was optimised away */

         var = vm->matches[i].producer_var;
         type = get_varying_type(var, vm->producer_stage);
      }

      if (var->data.patch)
         location = &generic_patch_location;

      /* Advance to the next slot if this varying has a different packing
       * class than the previous one, and we're not already on a slot
       * boundary.
       *
       * Also advance if varying packing is disabled for transform feedback,
       * and previous or current varying is used for transform feedback.
       *
       * Also advance to the next slot if packing is disabled. This makes sure
       * we don't assign varyings the same locations which is possible
       * because we still pack individual arrays, records and matrices even
       * when packing is disabled. Note we don't advance to the next slot if
       * we can pack varyings together that are only used for transform
       * feedback.
       */
      if (var->data.must_be_shader_input ||
          (vm->disable_xfb_packing &&
           (previous_var_xfb || var->data.is_xfb)) ||
          (vm->disable_varying_packing &&
           !(previous_var_xfb_only && var->data.is_xfb_only)) ||
          (previous_packing_class != vm->matches[i].packing_class) ||
          (vm->matches[i].packing_order == PACKING_ORDER_VEC3 &&
           dont_pack_vec3)) {
         *location = ALIGN(*location, 4);
      }

      previous_var_xfb = var->data.is_xfb;
      previous_var_xfb_only = var->data.is_xfb_only;
      previous_packing_class = vm->matches[i].packing_class;

      /* The number of components taken up by this variable. For vertex shader
       * inputs, we use the number of slots * 4, as they have different
       * counting rules.
       */
      unsigned num_components = 0;
      if (is_vertex_input) {
         num_components = glsl_count_attribute_slots(type, is_vertex_input) * 4;
      } else {
         if (is_packing_disabled(vm, type, var)) {
            num_components = glsl_count_attribute_slots(type, false) * 4;
         } else {
            num_components = glsl_get_component_slots_aligned(type, *location);
         }
      }

      /* The last slot for this variable, inclusive. */
      unsigned slot_end = *location + num_components - 1;

      /* FIXME: We could be smarter in the below code and loop back over
       * trying to fill any locations that we skipped because we couldn't pack
       * the varying between an explicit location. For now just let the user
       * hit the linking error if we run out of room and suggest they use
       * explicit locations.
       */
      while (slot_end < MAX_VARYING * 4u) {
         const unsigned slots = (slot_end / 4u) - (*location / 4u) + 1;
         const uint64_t slot_mask = ((1ull << slots) - 1) << (*location / 4u);

         assert(slots > 0);

         if ((reserved_slots & slot_mask) == 0) {
            break;
         }

         *location = ALIGN(*location + 1, 4);
         slot_end = *location + num_components - 1;
      }

      if (!var->data.patch && slot_end >= MAX_VARYING * 4u) {
         linker_error(prog, "insufficient contiguous locations available for "
                      "%s it is possible an array or struct could not be "
                      "packed between varyings with explicit locations. Try "
                      "using an explicit location for arrays and structs.",
                      var->name);
      }

      if (slot_end < MAX_VARYINGS_INCL_PATCH * 4u) {
         for (unsigned j = *location / 4u; j < slot_end / 4u; j++)
            components[j] = 4;
         components[slot_end / 4u] = (slot_end & 3) + 1;
      }

      vm->matches[i].generic_location = *location;

      *location = slot_end + 1;
   }

   return (generic_location + 3) / 4;
}

static void
varying_matches_assign_temp_locations(struct varying_matches *vm,
                                      struct gl_shader_program *prog,
                                      uint64_t reserved_slots)
{
   unsigned tmp_loc = 0;
   for (unsigned i = 0; i < vm->num_matches; i++) {
      nir_variable *producer_var = vm->matches[i].producer_var;
      nir_variable *consumer_var = vm->matches[i].consumer_var;

      while (tmp_loc < MAX_VARYINGS_INCL_PATCH) {
         if (reserved_slots & (UINT64_C(1) << tmp_loc))
            tmp_loc++;
         else
            break;
      }

      if (producer_var) {
         assert(producer_var->data.location == -1);
         producer_var->data.location = VARYING_SLOT_VAR0 + tmp_loc;
      }

      if (consumer_var) {
         assert(consumer_var->data.location == -1);
         consumer_var->data.location = VARYING_SLOT_VAR0 + tmp_loc;
      }

      tmp_loc++;
   }
}

/**
 * Update the producer and consumer shaders to reflect the locations
 * assignments that were made by varying_matches_assign_locations().
 */
static void
varying_matches_store_locations(struct varying_matches *vm)
{
   /* Check is location needs to be packed with lower_packed_varyings() or if
    * we can just use ARB_enhanced_layouts packing.
    */
   bool pack_loc[MAX_VARYINGS_INCL_PATCH] = {0};
   const struct glsl_type *loc_type[MAX_VARYINGS_INCL_PATCH][4] = { {NULL, NULL} };

   for (unsigned i = 0; i < vm->num_matches; i++) {
      nir_variable *producer_var = vm->matches[i].producer_var;
      nir_variable *consumer_var = vm->matches[i].consumer_var;
      unsigned generic_location = vm->matches[i].generic_location;
      unsigned slot = generic_location / 4;
      unsigned offset = generic_location % 4;

      if (producer_var) {
         producer_var->data.location = VARYING_SLOT_VAR0 + slot;
         producer_var->data.location_frac = offset;
      }

      if (consumer_var) {
         consumer_var->data.location = VARYING_SLOT_VAR0 + slot;
         consumer_var->data.location_frac = offset;
      }

      /* Find locations suitable for native packing via
       * ARB_enhanced_layouts.
       */
      if (vm->enhanced_layouts_enabled) {
         nir_variable *var = producer_var ? producer_var : consumer_var;
         unsigned stage = producer_var ? vm->producer_stage : vm->consumer_stage;
         const struct glsl_type *type =
            get_varying_type(var, stage);
         unsigned comp_slots = glsl_get_component_slots(type) + offset;
         unsigned slots = comp_slots / 4;
         if (comp_slots % 4)
            slots += 1;

         if (producer_var && consumer_var) {
            if (glsl_type_is_array_or_matrix(type) || glsl_type_is_struct(type) ||
                glsl_type_is_64bit(type)) {
               for (unsigned j = 0; j < slots; j++) {
                  pack_loc[slot + j] = true;
               }
            } else if (offset + glsl_get_vector_elements(type) > 4) {
               pack_loc[slot] = true;
               pack_loc[slot + 1] = true;
            } else {
               loc_type[slot][offset] = type;
            }
         } else {
            for (unsigned j = 0; j < slots; j++) {
               pack_loc[slot + j] = true;
            }
         }
      }
   }

   /* Attempt to use ARB_enhanced_layouts for more efficient packing if
    * suitable.
    */
   if (vm->enhanced_layouts_enabled) {
      for (unsigned i = 0; i < vm->num_matches; i++) {
         nir_variable *producer_var = vm->matches[i].producer_var;
         nir_variable *consumer_var = vm->matches[i].consumer_var;
         if (!producer_var || !consumer_var)
            continue;

         unsigned generic_location = vm->matches[i].generic_location;
         unsigned slot = generic_location / 4;
         if (pack_loc[slot])
            continue;

         const struct glsl_type *type =
            get_varying_type(producer_var, vm->producer_stage);
         bool type_match = true;
         for (unsigned j = 0; j < 4; j++) {
            if (loc_type[slot][j]) {
               if (glsl_get_base_type(type) !=
                   glsl_get_base_type(loc_type[slot][j]))
                  type_match = false;
            }
         }

         if (type_match) {
            producer_var->data.explicit_location = 1;
            consumer_var->data.explicit_location = 1;
         }
      }
   }
}

/**
 * Is the given variable a varying variable to be counted against the
 * limit in ctx->Const.MaxVarying?
 * This includes variables such as texcoords, colors and generic
 * varyings, but excludes variables such as gl_FrontFacing and gl_FragCoord.
 */
static bool
var_counts_against_varying_limit(gl_shader_stage stage, const nir_variable *var)
{
   /* Only fragment shaders will take a varying variable as an input */
   if (stage == MESA_SHADER_FRAGMENT &&
       var->data.mode == nir_var_shader_in) {
      switch (var->data.location) {
      case VARYING_SLOT_POS:
      case VARYING_SLOT_FACE:
      case VARYING_SLOT_PNTC:
         return false;
      default:
         return true;
      }
   }
   return false;
}

struct tfeedback_candidate_generator_state {
   /**
    * Memory context used to allocate hash table keys and values.
    */
   void *mem_ctx;

   /**
    * Hash table in which tfeedback_candidate objects should be stored.
    */
   struct hash_table *tfeedback_candidates;

   gl_shader_stage stage;

   /**
    * Pointer to the toplevel variable that is being traversed.
    */
   nir_variable *toplevel_var;

   /**
    * Total number of varying floats that have been visited so far.  This is
    * used to determine the offset to each varying within the toplevel
    * variable.
    */
   unsigned varying_floats;

   /**
    * Offset within the xfb. Counted in floats.
    */
   unsigned xfb_offset_floats;
};

/**
 * Generates tfeedback_candidate structs describing all possible targets of
 * transform feedback.
 *
 * tfeedback_candidate structs are stored in the hash table
 * tfeedback_candidates.  This hash table maps varying names to instances of the
 * tfeedback_candidate struct.
 */
static void
tfeedback_candidate_generator(struct tfeedback_candidate_generator_state *state,
                              char **name, size_t name_length,
                              const struct glsl_type *type,
                              const struct glsl_struct_field *named_ifc_member)
{
   switch (glsl_get_base_type(type)) {
   case GLSL_TYPE_INTERFACE:
      if (named_ifc_member) {
         ralloc_asprintf_rewrite_tail(name, &name_length, ".%s",
                                      named_ifc_member->name);
         tfeedback_candidate_generator(state, name, name_length,
                                       named_ifc_member->type, NULL);
         return;
      }

   case GLSL_TYPE_STRUCT:
      for (unsigned i = 0; i < glsl_get_length(type); i++) {
         size_t new_length = name_length;

         /* Append '.field' to the current variable name. */
         if (name) {
            ralloc_asprintf_rewrite_tail(name, &new_length, ".%s",
                                         glsl_get_struct_elem_name(type, i));
         }

         tfeedback_candidate_generator(state, name, new_length,
                                       glsl_get_struct_field(type, i), NULL);
      }

      return;
   case GLSL_TYPE_ARRAY:
      if (glsl_type_is_struct(glsl_without_array(type)) ||
          glsl_type_is_interface(glsl_without_array(type)) ||
          glsl_type_is_array(glsl_get_array_element(type))) {

         for (unsigned i = 0; i < glsl_get_length(type); i++) {
            size_t new_length = name_length;

            /* Append the subscript to the current variable name */
            ralloc_asprintf_rewrite_tail(name, &new_length, "[%u]", i);

            tfeedback_candidate_generator(state, name, new_length,
                                          glsl_get_array_element(type),
                                          named_ifc_member);
         }

         return;
      }

   default:
      assert(!glsl_type_is_struct(glsl_without_array(type)));
      assert(!glsl_type_is_interface(glsl_without_array(type)));

      struct tfeedback_candidate *candidate
         = rzalloc(state->mem_ctx, struct tfeedback_candidate);
      candidate->toplevel_var = state->toplevel_var;
      candidate->type = type;

      if (glsl_type_is_64bit(glsl_without_array(type))) {
         /*  From ARB_gpu_shader_fp64:
          *
          * If any variable captured in transform feedback has double-precision
          * components, the practical requirements for defined behavior are:
          *     ...
          * (c) each double-precision variable captured must be aligned to a
          *     multiple of eight bytes relative to the beginning of a vertex.
          */
         state->xfb_offset_floats = ALIGN(state->xfb_offset_floats, 2);
         /* 64-bit members of structs are also aligned. */
         state->varying_floats = ALIGN(state->varying_floats, 2);
      }

      candidate->xfb_offset_floats = state->xfb_offset_floats;
      candidate->struct_offset_floats = state->varying_floats;

      _mesa_hash_table_insert(state->tfeedback_candidates,
                              ralloc_strdup(state->mem_ctx, *name),
                              candidate);

      const unsigned component_slots = glsl_get_component_slots(type);

      if (varying_has_user_specified_location(state->toplevel_var)) {
         state->varying_floats += glsl_count_attribute_slots(type, false) * 4;
      } else {
         state->varying_floats += component_slots;
      }

      state->xfb_offset_floats += component_slots;
   }
}

static void
populate_consumer_input_sets(void *mem_ctx, nir_shader *nir,
                             struct hash_table *consumer_inputs,
                             struct hash_table *consumer_interface_inputs,
                             nir_variable *consumer_inputs_with_locations[VARYING_SLOT_TESS_MAX])
{
   memset(consumer_inputs_with_locations, 0,
          sizeof(consumer_inputs_with_locations[0]) * VARYING_SLOT_TESS_MAX);

   nir_foreach_shader_in_variable(input_var, nir) {
      /* All interface blocks should have been lowered by this point */
      assert(!glsl_type_is_interface(input_var->type));

      if (input_var->data.explicit_location) {
         /* assign_varying_locations only cares about finding the
          * nir_variable at the start of a contiguous location block.
          *
          *     - For !producer, consumer_inputs_with_locations isn't used.
          *
          *     - For !consumer, consumer_inputs_with_locations is empty.
          *
          * For consumer && producer, if you were trying to set some
          * nir_variable to the middle of a location block on the other side
          * of producer/consumer, cross_validate_outputs_to_inputs() should
          * be link-erroring due to either type mismatch or location
          * overlaps.  If the variables do match up, then they've got a
          * matching data.location and you only looked at
          * consumer_inputs_with_locations[var->data.location], not any
          * following entries for the array/structure.
          */
         consumer_inputs_with_locations[input_var->data.location] =
            input_var;
      } else if (input_var->interface_type != NULL) {
         char *const iface_field_name =
            ralloc_asprintf(mem_ctx, "%s.%s",
               glsl_get_type_name(glsl_without_array(input_var->interface_type)),
               input_var->name);
         _mesa_hash_table_insert(consumer_interface_inputs,
                                 iface_field_name, input_var);
      } else {
         _mesa_hash_table_insert(consumer_inputs,
                                 ralloc_strdup(mem_ctx, input_var->name),
                                 input_var);
      }
   }
}

/**
 * Find a variable from the consumer that "matches" the specified variable
 *
 * This function only finds inputs with names that match.  There is no
 * validation (here) that the types, etc. are compatible.
 */
static nir_variable *
get_matching_input(void *mem_ctx,
                   const nir_variable *output_var,
                   struct hash_table *consumer_inputs,
                   struct hash_table *consumer_interface_inputs,
                   nir_variable *consumer_inputs_with_locations[VARYING_SLOT_TESS_MAX])
{
   nir_variable *input_var;

   if (output_var->data.explicit_location) {
      input_var = consumer_inputs_with_locations[output_var->data.location];
   } else if (output_var->interface_type != NULL) {
      char *const iface_field_name =
         ralloc_asprintf(mem_ctx, "%s.%s",
            glsl_get_type_name(glsl_without_array(output_var->interface_type)),
            output_var->name);
      struct hash_entry *entry =
         _mesa_hash_table_search(consumer_interface_inputs, iface_field_name);
      input_var = entry ? (nir_variable *) entry->data : NULL;
   } else {
      struct hash_entry *entry =
         _mesa_hash_table_search(consumer_inputs, output_var->name);
      input_var = entry ? (nir_variable *) entry->data : NULL;
   }

   return (input_var == NULL || input_var->data.mode != nir_var_shader_in)
      ? NULL : input_var;
}

static int
io_variable_cmp(const void *_a, const void *_b)
{
   const nir_variable *const a = *(const nir_variable **) _a;
   const nir_variable *const b = *(const nir_variable **) _b;

   if (a->data.explicit_location && b->data.explicit_location)
      return b->data.location - a->data.location;

   if (a->data.explicit_location && !b->data.explicit_location)
      return 1;

   if (!a->data.explicit_location && b->data.explicit_location)
      return -1;

   return -strcmp(a->name, b->name);
}

/**
 * Sort the shader IO variables into canonical order
 */
static void
canonicalize_shader_io(nir_shader *nir, nir_variable_mode io_mode)
{
   nir_variable *var_table[MAX_PROGRAM_OUTPUTS * 4];
   unsigned num_variables = 0;

   nir_foreach_variable_with_modes(var, nir, io_mode) {
      /* If we have already encountered more I/O variables that could
       * successfully link, bail.
       */
      if (num_variables == ARRAY_SIZE(var_table))
         return;

      var_table[num_variables++] = var;
   }

   if (num_variables == 0)
      return;

   /* Sort the list in reverse order (io_variable_cmp handles this).  Later
    * we're going to push the variables on to the IR list as a stack, so we
    * want the last variable (in canonical order) to be first in the list.
    */
   qsort(var_table, num_variables, sizeof(var_table[0]), io_variable_cmp);

   /* Remove the variable from it's current location in the varible list, and
    * put it at the front.
    */
   for (unsigned i = 0; i < num_variables; i++) {
      exec_node_remove(&var_table[i]->node);
      exec_list_push_head(&nir->variables, &var_table[i]->node);
   }
}

/**
 * Generate a bitfield map of the explicit locations for shader varyings.
 *
 * Note: For Tessellation shaders we are sitting right on the limits of the
 * 64 bit map. Per-vertex and per-patch both have separate location domains
 * with a max of MAX_VARYING.
 */
static uint64_t
reserved_varying_slot(struct gl_linked_shader *sh,
                      nir_variable_mode io_mode)
{
   assert(io_mode == nir_var_shader_in || io_mode == nir_var_shader_out);
   /* Avoid an overflow of the returned value */
   assert(MAX_VARYINGS_INCL_PATCH <= 64);

   uint64_t slots = 0;
   int var_slot;

   if (!sh)
      return slots;

   nir_foreach_variable_with_modes(var, sh->Program->nir, io_mode) {
      if (!var->data.explicit_location ||
          var->data.location < VARYING_SLOT_VAR0)
         continue;

      var_slot = var->data.location - VARYING_SLOT_VAR0;

      bool is_gl_vertex_input = io_mode == nir_var_shader_in &&
                                sh->Stage == MESA_SHADER_VERTEX;
      unsigned num_elements =
         glsl_count_attribute_slots(get_varying_type(var, sh->Stage),
                                    is_gl_vertex_input);
      for (unsigned i = 0; i < num_elements; i++) {
         if (var_slot >= 0 && var_slot < MAX_VARYINGS_INCL_PATCH)
            slots |= UINT64_C(1) << var_slot;
         var_slot += 1;
      }
   }

   return slots;
}

/**
 * Sets the bits in the inputs_read, or outputs_written
 * bitfield corresponding to this variable.
 */
static void
set_variable_io_mask(BITSET_WORD *bits, nir_variable *var, gl_shader_stage stage)
{
   assert(var->data.mode == nir_var_shader_in ||
          var->data.mode == nir_var_shader_out);
   assert(var->data.location >= VARYING_SLOT_VAR0);

   const struct glsl_type *type = var->type;
   if (nir_is_arrayed_io(var, stage) || var->data.per_view) {
      assert(glsl_type_is_array(type));
      type = glsl_get_array_element(type);
   }

   unsigned location = var->data.location - VARYING_SLOT_VAR0;
   unsigned slots = glsl_count_attribute_slots(type, false);
   for (unsigned i = 0; i < slots; i++) {
      BITSET_SET(bits, location + i);
   }
}

static uint8_t
get_num_components(nir_variable *var)
{
   if (glsl_type_is_struct_or_ifc(glsl_without_array(var->type)))
      return 4;

   return glsl_get_vector_elements(glsl_without_array(var->type));
}

static void
tcs_add_output_reads(nir_shader *shader, BITSET_WORD **read)
{
   nir_foreach_function(function, shader) {
      if (!function->impl)
         continue;

      nir_foreach_block(block, function->impl) {
         nir_foreach_instr(instr, block) {
            if (instr->type != nir_instr_type_intrinsic)
               continue;

            nir_intrinsic_instr *intrin = nir_instr_as_intrinsic(instr);
            if (intrin->intrinsic != nir_intrinsic_load_deref)
               continue;

            nir_deref_instr *deref = nir_src_as_deref(intrin->src[0]);
            if (!nir_deref_mode_is(deref, nir_var_shader_out))
               continue;

            nir_variable *var = nir_deref_instr_get_variable(deref);
            for (unsigned i = 0; i < get_num_components(var); i++) {
               if (var->data.location < VARYING_SLOT_VAR0)
                  continue;

               unsigned comp = var->data.location_frac;
               set_variable_io_mask(read[comp + i], var, shader->info.stage);
            }
         }
      }
   }
}

/* We need to replace any interp intrinsics with undefined (shader_temp) inputs
 * as no further NIR pass expects to see this.
 */
static bool
replace_unused_interpolate_at_with_undef(nir_builder *b, nir_instr *instr,
                                         void *data)
{
   if (instr->type == nir_instr_type_intrinsic) {
      nir_intrinsic_instr *intrin = nir_instr_as_intrinsic(instr);

      if (intrin->intrinsic == nir_intrinsic_interp_deref_at_centroid ||
          intrin->intrinsic == nir_intrinsic_interp_deref_at_sample ||
          intrin->intrinsic == nir_intrinsic_interp_deref_at_offset) {
         nir_variable *var = nir_intrinsic_get_var(intrin, 0);
         if (var->data.mode == nir_var_shader_temp) {
            /* Create undef and rewrite the interp uses */
            nir_ssa_def *undef =
               nir_ssa_undef(b, intrin->dest.ssa.num_components,
                             intrin->dest.ssa.bit_size);
            nir_ssa_def_rewrite_uses(&intrin->dest.ssa, undef);

            nir_instr_remove(&intrin->instr);
            return true;
         }
      }
   }

   return false;
}

static void
fixup_vars_lowered_to_temp(nir_shader *shader, nir_variable_mode mode)
{
   /* Remove all interpolate uses of the unset varying and replace with undef. */
   if (mode == nir_var_shader_in && shader->info.stage == MESA_SHADER_FRAGMENT) {
      (void) nir_shader_instructions_pass(shader,
                                          replace_unused_interpolate_at_with_undef,
                                          nir_metadata_block_index |
                                          nir_metadata_dominance,
                                          NULL);
   }

   nir_lower_global_vars_to_local(shader);
   nir_fixup_deref_modes(shader);
}

/**
 * Helper for removing unused shader I/O variables, by demoting them to global
 * variables (which may then be dead code eliminated).
 *
 * Example usage is:
 *
 * progress = nir_remove_unused_io_vars(producer, consumer, nir_var_shader_out,
 *                                      read, patches_read) ||
 *                                      progress;
 *
 * The "used" should be an array of 4 BITSET_WORDs representing each
 * .location_frac used.  Note that for vector variables, only the first channel
 * (.location_frac) is examined for deciding if the variable is used!
 */
static bool
remove_unused_io_vars(nir_shader *producer, nir_shader *consumer,
                      struct gl_shader_program *prog,
                      nir_variable_mode mode,
                      BITSET_WORD **used_by_other_stage)
{
   assert(mode == nir_var_shader_in || mode == nir_var_shader_out);

   bool progress = false;
   nir_shader *shader = mode == nir_var_shader_out ? producer : consumer;

   BITSET_WORD **used;
   nir_foreach_variable_with_modes_safe(var, shader, mode) {
      used = used_by_other_stage;

      /* Skip builtins dead builtins are removed elsewhere */
      if (is_gl_identifier(var->name))
         continue;

      if (var->data.location < VARYING_SLOT_VAR0 && var->data.location >= 0)
         continue;

      /* Skip xfb varyings and any other type we cannot remove */
      if (var->data.always_active_io)
         continue;

      if (var->data.explicit_xfb_buffer)
         continue;

      BITSET_WORD *other_stage = used[var->data.location_frac];

      /* if location == -1 lower varying to global as it has no match and is not
       * a xfb varying, this must be done after skiping bultins as builtins
       * could be assigned a location of -1.
       * We also lower unused varyings with explicit locations.
       */
      bool use_found = false;
      if (var->data.location >= 0) {
         unsigned location = var->data.location - VARYING_SLOT_VAR0;

         const struct glsl_type *type = var->type;
         if (nir_is_arrayed_io(var, shader->info.stage) || var->data.per_view) {
            assert(glsl_type_is_array(type));
            type = glsl_get_array_element(type);
         }

         unsigned slots = glsl_count_attribute_slots(type, false);
         for (unsigned i = 0; i < slots; i++) {
            if (BITSET_TEST(other_stage, location + i)) {
               use_found = true;
               break;
            }
         }
      }

      if (!use_found) {
         /* This one is invalid, make it a global variable instead */
         var->data.location = 0;
         var->data.mode = nir_var_shader_temp;

         progress = true;

         if (mode == nir_var_shader_in) {
            if (!prog->IsES && prog->GLSL_Version <= 120) {
               /* On page 25 (page 31 of the PDF) of the GLSL 1.20 spec:
                *
                *     Only those varying variables used (i.e. read) in
                *     the fragment shader executable must be written to
                *     by the vertex shader executable; declaring
                *     superfluous varying variables in a vertex shader is
                *     permissible.
                *
                * We interpret this text as meaning that the VS must
                * write the variable for the FS to read it.  See
                * "glsl1-varying read but not written" in piglit.
                */
               linker_error(prog, "%s shader varying %s not written "
                            "by %s shader\n.",
                            _mesa_shader_stage_to_string(consumer->info.stage),
                            var->name,
                            _mesa_shader_stage_to_string(producer->info.stage));
            } else {
               linker_warning(prog, "%s shader varying %s not written "
                              "by %s shader\n.",
                              _mesa_shader_stage_to_string(consumer->info.stage),
                              var->name,
                              _mesa_shader_stage_to_string(producer->info.stage));
            }
         }
      }
   }

   if (progress)
      fixup_vars_lowered_to_temp(shader, mode);

   return progress;
}

static bool
remove_unused_varyings(nir_shader *producer, nir_shader *consumer,
                       struct gl_shader_program *prog, void *mem_ctx)
{
   assert(producer->info.stage != MESA_SHADER_FRAGMENT);
   assert(consumer->info.stage != MESA_SHADER_VERTEX);

   int max_loc_out = 0;
   nir_foreach_shader_out_variable(var, producer) {
      if (var->data.location < VARYING_SLOT_VAR0)
         continue;

      const struct glsl_type *type = var->type;
      if (nir_is_arrayed_io(var, producer->info.stage) || var->data.per_view) {
         assert(glsl_type_is_array(type));
         type = glsl_get_array_element(type);
      }
      unsigned slots = glsl_count_attribute_slots(type, false);

      max_loc_out = max_loc_out < (var->data.location - VARYING_SLOT_VAR0) + slots ?
         (var->data.location - VARYING_SLOT_VAR0) + slots : max_loc_out;
   }

   int max_loc_in = 0;
   nir_foreach_shader_in_variable(var, consumer) {
      if (var->data.location < VARYING_SLOT_VAR0)
         continue;

      const struct glsl_type *type = var->type;
      if (nir_is_arrayed_io(var, consumer->info.stage) || var->data.per_view) {
         assert(glsl_type_is_array(type));
         type = glsl_get_array_element(type);
      }
      unsigned slots = glsl_count_attribute_slots(type, false);

      max_loc_in = max_loc_in < (var->data.location - VARYING_SLOT_VAR0) + slots ?
         (var->data.location - VARYING_SLOT_VAR0) + slots : max_loc_in;
   }

   /* Old glsl shaders that don't use explicit locations can contain greater
    * than 64 varyings before unused varyings are removed so we must count them
    * and make use of the BITSET macros to keep track of used slots. Once we
    * have removed these excess varyings we can make use of further nir varying
    * linking optimimisation passes.
    */
   BITSET_WORD *read[4];
   BITSET_WORD *written[4];
   int max_loc = MAX2(max_loc_in, max_loc_out);
   for (unsigned i = 0; i < 4; i++) {
      read[i] = rzalloc_array(mem_ctx, BITSET_WORD, BITSET_WORDS(max_loc));
      written[i] = rzalloc_array(mem_ctx, BITSET_WORD, BITSET_WORDS(max_loc));
   }

   nir_foreach_shader_out_variable(var, producer) {
      if (var->data.location < VARYING_SLOT_VAR0)
         continue;

      for (unsigned i = 0; i < get_num_components(var); i++) {
         unsigned comp = var->data.location_frac;
         set_variable_io_mask(written[comp + i], var, producer->info.stage);
      }
   }

   nir_foreach_shader_in_variable(var, consumer) {
      if (var->data.location < VARYING_SLOT_VAR0)
         continue;

      for (unsigned i = 0; i < get_num_components(var); i++) {
         unsigned comp = var->data.location_frac;
         set_variable_io_mask(read[comp + i], var, consumer->info.stage);
      }
   }

   /* Each TCS invocation can read data written by other TCS invocations,
    * so even if the outputs are not used by the TES we must also make
    * sure they are not read by the TCS before demoting them to globals.
    */
   if (producer->info.stage == MESA_SHADER_TESS_CTRL)
      tcs_add_output_reads(producer, read);

   bool progress = false;
   progress =
      remove_unused_io_vars(producer, consumer, prog, nir_var_shader_out, read);
   progress =
      remove_unused_io_vars(producer, consumer, prog, nir_var_shader_in, written) || progress;

   return progress;
}

static bool
should_add_varying_match_record(nir_variable *const input_var,
                                struct gl_shader_program *prog,
                                struct gl_linked_shader *producer,
                                struct gl_linked_shader *consumer) {

   /* If a matching input variable was found, add this output (and the input) to
    * the set.  If this is a separable program and there is no consumer stage,
    * add the output.
    *
    * Always add TCS outputs. They are shared by all invocations
    * within a patch and can be used as shared memory.
    */
   return input_var || (prog->SeparateShader && consumer == NULL) ||
             producer->Stage == MESA_SHADER_TESS_CTRL;
}

/* This assigns some initial unoptimised varying locations so that our nir
 * optimisations can perform some initial optimisations and also does initial
 * processing of
 */
static bool
assign_initial_varying_locations(const struct gl_constants *consts,
                                 const struct gl_extensions *exts,
                                 void *mem_ctx,
                                 struct gl_shader_program *prog,
                                 struct gl_linked_shader *producer,
                                 struct gl_linked_shader *consumer,
                                 unsigned num_xfb_decls,
                                 struct xfb_decl *xfb_decls,
                                 struct varying_matches *vm)
{
   init_varying_matches(mem_ctx, vm, consts, exts,
                        producer ? producer->Stage : MESA_SHADER_NONE,
                        consumer ? consumer->Stage : MESA_SHADER_NONE,
                        prog->SeparateShader);

   struct hash_table *tfeedback_candidates =
         _mesa_hash_table_create(mem_ctx, _mesa_hash_string,
                                 _mesa_key_string_equal);
   struct hash_table *consumer_inputs =
         _mesa_hash_table_create(mem_ctx, _mesa_hash_string,
                                 _mesa_key_string_equal);
   struct hash_table *consumer_interface_inputs =
         _mesa_hash_table_create(mem_ctx, _mesa_hash_string,
                                 _mesa_key_string_equal);
   nir_variable *consumer_inputs_with_locations[VARYING_SLOT_TESS_MAX] = {
      NULL,
   };

   if (consumer)
      populate_consumer_input_sets(mem_ctx, consumer->Program->nir,
                                   consumer_inputs, consumer_interface_inputs,
                                   consumer_inputs_with_locations);

   if (producer) {
      nir_foreach_shader_out_variable(output_var, producer->Program->nir) {
         /* Only geometry shaders can use non-zero streams */
         assert(output_var->data.stream == 0 ||
                (output_var->data.stream < MAX_VERTEX_STREAMS &&
                 producer->Stage == MESA_SHADER_GEOMETRY));

         if (num_xfb_decls > 0) {
            /* From OpenGL 4.6 (Core Profile) spec, section 11.1.2.1
             * ("Vertex Shader Variables / Output Variables")
             *
             * "Each program object can specify a set of output variables from
             * one shader to be recorded in transform feedback mode (see
             * section 13.3). The variables that can be recorded are those
             * emitted by the first active shader, in order, from the
             * following list:
             *
             *  * geometry shader
             *  * tessellation evaluation shader
             *  * tessellation control shader
             *  * vertex shader"
             *
             * But on OpenGL ES 3.2, section 11.1.2.1 ("Vertex Shader
             * Variables / Output Variables") tessellation control shader is
             * not included in the stages list.
             */
            if (!prog->IsES || producer->Stage != MESA_SHADER_TESS_CTRL) {

               const struct glsl_type *type = output_var->data.from_named_ifc_block ?
                  output_var->interface_type : output_var->type;
               if (!output_var->data.patch && producer->Stage == MESA_SHADER_TESS_CTRL) {
                  assert(glsl_type_is_array(type));
                  type = glsl_get_array_element(type);
               }

               const struct glsl_struct_field *ifc_member = NULL;
               if (output_var->data.from_named_ifc_block) {
                  ifc_member =
                     glsl_get_struct_field_data(glsl_without_array(type),
                        glsl_get_field_index(glsl_without_array(type), output_var->name));
               }

               char *name;
               if (glsl_type_is_struct(glsl_without_array(type)) ||
                   (glsl_type_is_array(type) && glsl_type_is_array(glsl_get_array_element(type)))) {
                  type = output_var->type;
                  name = ralloc_strdup(NULL, output_var->name);
               } else if (glsl_type_is_interface(glsl_without_array(type))) {
                  name = ralloc_strdup(NULL, glsl_get_type_name(glsl_without_array(type)));
               } else  {
                  name = ralloc_strdup(NULL, output_var->name);
               }

               struct tfeedback_candidate_generator_state state;
               state.mem_ctx = mem_ctx;
               state.tfeedback_candidates = tfeedback_candidates;
               state.stage = producer->Stage;
               state.toplevel_var = output_var;
               state.varying_floats = 0;
               state.xfb_offset_floats = 0;

               tfeedback_candidate_generator(&state, &name, strlen(name), type,
                                             ifc_member);
               ralloc_free(name);
            }
         }

         nir_variable *const input_var =
            get_matching_input(mem_ctx, output_var, consumer_inputs,
                               consumer_interface_inputs,
                               consumer_inputs_with_locations);

         if (should_add_varying_match_record(input_var, prog, producer,
                                             consumer)) {
            varying_matches_record(mem_ctx, vm, output_var, input_var);
         }

         /* Only stream 0 outputs can be consumed in the next stage */
         if (input_var && output_var->data.stream != 0) {
            linker_error(prog, "output %s is assigned to stream=%d but "
                         "is linked to an input, which requires stream=0",
                         output_var->name, output_var->data.stream);
            return false;
         }
      }
   } else {
      /* If there's no producer stage, then this must be a separable program.
       * For example, we may have a program that has just a fragment shader.
       * Later this program will be used with some arbitrary vertex (or
       * geometry) shader program.  This means that locations must be assigned
       * for all the inputs.
       */
      nir_foreach_shader_in_variable(input_var, consumer->Program->nir) {
         varying_matches_record(mem_ctx, vm, NULL, input_var);
      }
   }

   for (unsigned i = 0; i < num_xfb_decls; ++i) {
      if (!xfb_decl_is_varying(&xfb_decls[i]))
         continue;

      const struct tfeedback_candidate *matched_candidate
         = xfb_decl_find_candidate(&xfb_decls[i], prog, tfeedback_candidates);

      if (matched_candidate == NULL)
         return false;

      /* There are two situations where a new output varying is needed:
       *
       *  - If varying packing is disabled for xfb and the current declaration
       *    is subscripting an array, whether the subscript is aligned or not.
       *    to preserve the rest of the array for the consumer.
       *
       *  - If a builtin variable needs to be copied to a new variable
       *    before its content is modified by another lowering pass (e.g.
       *    \c gl_Position is transformed by \c nir_lower_viewport_transform).
       */
      const bool lowered =
         (vm->disable_xfb_packing && xfb_decls[i].is_subscripted) ||
         (matched_candidate->toplevel_var->data.explicit_location &&
          matched_candidate->toplevel_var->data.location < VARYING_SLOT_VAR0 &&
          (!consumer || consumer->Stage == MESA_SHADER_FRAGMENT) &&
          (consts->ShaderCompilerOptions[producer->Stage].LowerBuiltinVariablesXfb &
              BITFIELD_BIT(matched_candidate->toplevel_var->data.location)));

      if (lowered) {
         nir_variable *new_var;
         struct tfeedback_candidate *new_candidate = NULL;

         new_var = gl_nir_lower_xfb_varying(producer->Program->nir,
                                            xfb_decls[i].orig_name,
                                            matched_candidate->toplevel_var);
         if (new_var == NULL)
            return false;

         /* Create new candidate and replace matched_candidate */
         new_candidate = rzalloc(mem_ctx, struct tfeedback_candidate);
         new_candidate->toplevel_var = new_var;
         new_candidate->type = new_var->type;
         new_candidate->struct_offset_floats = 0;
         new_candidate->xfb_offset_floats = 0;
         _mesa_hash_table_insert(tfeedback_candidates,
                                 ralloc_strdup(mem_ctx, new_var->name),
                                 new_candidate);

         xfb_decl_set_lowered_candidate(&xfb_decls[i], new_candidate);
         matched_candidate = new_candidate;
      }

      /* Mark as xfb varying */
      matched_candidate->toplevel_var->data.is_xfb = 1;

      /* Mark xfb varyings as always active */
      matched_candidate->toplevel_var->data.always_active_io = 1;

      /* Mark any corresponding inputs as always active also. We must do this
       * because we have a NIR pass that lowers vectors to scalars and another
       * that removes unused varyings.
       * We don't split varyings marked as always active because there is no
       * point in doing so. This means we need to mark both sides of the
       * interface as always active otherwise we will have a mismatch and
       * start removing things we shouldn't.
       */
      nir_variable *const input_var =
         get_matching_input(mem_ctx, matched_candidate->toplevel_var,
                            consumer_inputs, consumer_interface_inputs,
                            consumer_inputs_with_locations);
      if (input_var) {
         input_var->data.is_xfb = 1;
         input_var->data.always_active_io = 1;
      }

      /* Add the xfb varying to varying matches if it wasn't already added */
      if ((!should_add_varying_match_record(input_var, prog, producer,
                                            consumer) &&
           !matched_candidate->toplevel_var->data.is_xfb_only) || lowered) {
         matched_candidate->toplevel_var->data.is_xfb_only = 1;
         varying_matches_record(mem_ctx, vm, matched_candidate->toplevel_var,
                                NULL);
      }
   }

   uint64_t reserved_out_slots = 0;
   if (producer)
      reserved_out_slots = reserved_varying_slot(producer, nir_var_shader_out);

   uint64_t reserved_in_slots = 0;
   if (consumer)
      reserved_in_slots = reserved_varying_slot(consumer, nir_var_shader_in);

   /* Assign temporary user varying locations. This is required for our NIR
    * varying optimisations to do their matching.
    */
   const uint64_t reserved_slots = reserved_out_slots | reserved_in_slots;
   varying_matches_assign_temp_locations(vm, prog, reserved_slots);

   for (unsigned i = 0; i < num_xfb_decls; ++i) {
      if (!xfb_decl_is_varying(&xfb_decls[i]))
         continue;

      xfb_decls[i].matched_candidate->initial_location =
         xfb_decls[i].matched_candidate->toplevel_var->data.location;
      xfb_decls[i].matched_candidate->initial_location_frac =
         xfb_decls[i].matched_candidate->toplevel_var->data.location_frac;
   }

   return true;
}

static void
link_shader_opts(struct varying_matches *vm,
                 nir_shader *producer, nir_shader *consumer,
                 struct gl_shader_program *prog, void *mem_ctx)
{
   /* If we can't pack the stage using this pass then we can't lower io to
    * scalar just yet. Instead we leave it to a later NIR linking pass that uses
    * ARB_enhanced_layout style packing to pack things further.
    *
    * Otherwise we might end up causing linking errors and perf regressions
    * because the new scalars will be assigned individual slots and can overflow
    * the available slots.
    */
   if (producer->options->lower_to_scalar && !vm->disable_varying_packing &&
      !vm->disable_xfb_packing) {
      NIR_PASS_V(producer, nir_lower_io_to_scalar_early, nir_var_shader_out);
      NIR_PASS_V(consumer, nir_lower_io_to_scalar_early, nir_var_shader_in);
   }

   gl_nir_opts(producer);
   gl_nir_opts(consumer);

   if (nir_link_opt_varyings(producer, consumer))
      gl_nir_opts(consumer);

   NIR_PASS_V(producer, nir_remove_dead_variables, nir_var_shader_out, NULL);
   NIR_PASS_V(consumer, nir_remove_dead_variables, nir_var_shader_in, NULL);

   if (remove_unused_varyings(producer, consumer, prog, mem_ctx)) {
      NIR_PASS_V(producer, nir_lower_global_vars_to_local);
      NIR_PASS_V(consumer, nir_lower_global_vars_to_local);

      gl_nir_opts(producer);
      gl_nir_opts(consumer);

      /* Optimizations can cause varyings to become unused.
       * nir_compact_varyings() depends on all dead varyings being removed so
       * we need to call nir_remove_dead_variables() again here.
       */
      NIR_PASS_V(producer, nir_remove_dead_variables, nir_var_shader_out,
                 NULL);
      NIR_PASS_V(consumer, nir_remove_dead_variables, nir_var_shader_in,
                 NULL);
   }

   nir_link_varying_precision(producer, consumer);
}

/**
 * Assign locations for all variables that are produced in one pipeline stage
 * (the "producer") and consumed in the next stage (the "consumer").
 *
 * Variables produced by the producer may also be consumed by transform
 * feedback.
 *
 * \param num_xfb_decls is the number of declarations indicating
 *        variables that may be consumed by transform feedback.
 *
 * \param xfb_decls is a pointer to an array of xfb_decl objects
 *        representing the result of parsing the strings passed to
 *        glTransformFeedbackVaryings().  assign_location() will be called for
 *        each of these objects that matches one of the outputs of the
 *        producer.
 *
 * When num_xfb_decls is nonzero, it is permissible for the consumer to
 * be NULL.  In this case, varying locations are assigned solely based on the
 * requirements of transform feedback.
 */
static bool
assign_final_varying_locations(const struct gl_constants *consts,
                               const struct gl_extensions *exts,
                               void *mem_ctx,
                               struct gl_shader_program *prog,
                               struct gl_linked_shader *producer,
                               struct gl_linked_shader *consumer,
                               unsigned num_xfb_decls,
                               struct xfb_decl *xfb_decls,
                               const uint64_t reserved_slots,
                               struct varying_matches *vm)
{
   init_varying_matches(mem_ctx, vm, consts, exts,
                        producer ? producer->Stage : MESA_SHADER_NONE,
                        consumer ? consumer->Stage : MESA_SHADER_NONE,
                        prog->SeparateShader);

   /* Regather varying matches as we ran optimisations and the previous pointers
    * are no longer valid.
    */
   if (producer) {
      nir_foreach_shader_out_variable(var_out, producer->Program->nir) {
         if (var_out->data.location < VARYING_SLOT_VAR0 ||
             var_out->data.explicit_location)
            continue;

         if (vm->num_matches == vm->matches_capacity) {
            vm->matches_capacity *= 2;
            vm->matches = (struct match *)
               reralloc(mem_ctx, vm->matches, struct match,
                        vm->matches_capacity);
         }

         vm->matches[vm->num_matches].packing_class
            = varying_matches_compute_packing_class(var_out);
         vm->matches[vm->num_matches].packing_order
            = varying_matches_compute_packing_order(var_out);

         vm->matches[vm->num_matches].producer_var = var_out;
         vm->matches[vm->num_matches].consumer_var = NULL;
         vm->num_matches++;
      }

      /* Regather xfb varyings too */
      for (unsigned i = 0; i < num_xfb_decls; i++) {
         if (!xfb_decl_is_varying(&xfb_decls[i]))
            continue;

         /* Varying pointer was already reset */
         if (xfb_decls[i].matched_candidate->initial_location == -1)
            continue;

         bool UNUSED is_reset = false;
         bool UNUSED no_outputs = true;
         nir_foreach_shader_out_variable(var_out, producer->Program->nir) {
            no_outputs = false;
            assert(var_out->data.location != -1);
            if (var_out->data.location ==
                xfb_decls[i].matched_candidate->initial_location &&
                var_out->data.location_frac ==
                xfb_decls[i].matched_candidate->initial_location_frac) {
               xfb_decls[i].matched_candidate->toplevel_var = var_out;
               xfb_decls[i].matched_candidate->initial_location = -1;
               is_reset = true;
               break;
            }
         }
         assert(is_reset || no_outputs);
      }
   }

   bool found_match = false;
   if (consumer) {
      nir_foreach_shader_in_variable(var_in, consumer->Program->nir) {
         if (var_in->data.location < VARYING_SLOT_VAR0 ||
             var_in->data.explicit_location)
            continue;

         found_match = false;
         for (unsigned i = 0; i < vm->num_matches; i++) {
            if (vm->matches[i].producer_var &&
                (vm->matches[i].producer_var->data.location == var_in->data.location &&
                 vm->matches[i].producer_var->data.location_frac == var_in->data.location_frac)) {

               vm->matches[i].consumer_var = var_in;
               found_match = true;
               break;
            }
         }
         if (!found_match) {
            if (vm->num_matches == vm->matches_capacity) {
               vm->matches_capacity *= 2;
               vm->matches = (struct match *)
                  reralloc(mem_ctx, vm->matches, struct match,
                           vm->matches_capacity);
            }

            vm->matches[vm->num_matches].packing_class
               = varying_matches_compute_packing_class(var_in);
            vm->matches[vm->num_matches].packing_order
               = varying_matches_compute_packing_order(var_in);

            vm->matches[vm->num_matches].producer_var = NULL;
            vm->matches[vm->num_matches].consumer_var = var_in;
            vm->num_matches++;
         }
      }
   }

   uint8_t components[MAX_VARYINGS_INCL_PATCH] = {0};
   const unsigned slots_used =
      varying_matches_assign_locations(vm, prog, components, reserved_slots);
   varying_matches_store_locations(vm);

   for (unsigned i = 0; i < num_xfb_decls; ++i) {
      if (xfb_decl_is_varying(&xfb_decls[i])) {
         if (!xfb_decl_assign_location(&xfb_decls[i], consts, prog,
             vm->disable_varying_packing, vm->xfb_enabled))
            return false;
      }
   }

   if (producer) {
      gl_nir_lower_packed_varyings(consts, prog, mem_ctx, slots_used, components,
                                   nir_var_shader_out, 0, producer,
                                   vm->disable_varying_packing,
                                   vm->disable_xfb_packing, vm->xfb_enabled);
      nir_lower_pack(producer->Program->nir);
   }

   if (consumer) {
      unsigned consumer_vertices = 0;
      if (consumer && consumer->Stage == MESA_SHADER_GEOMETRY)
         consumer_vertices = prog->Geom.VerticesIn;

      gl_nir_lower_packed_varyings(consts, prog, mem_ctx, slots_used, components,
                                   nir_var_shader_in, consumer_vertices,
                                   consumer, vm->disable_varying_packing,
                                   vm->disable_xfb_packing, vm->xfb_enabled);
      nir_lower_pack(consumer->Program->nir);
   }

   return true;
}

static bool
check_against_output_limit(const struct gl_constants *consts, gl_api api,
                           struct gl_shader_program *prog,
                           struct gl_linked_shader *producer,
                           unsigned num_explicit_locations)
{
   unsigned output_vectors = num_explicit_locations;
   nir_foreach_shader_out_variable(var, producer->Program->nir) {
      if (!var->data.explicit_location &&
          var_counts_against_varying_limit(producer->Stage, var)) {
         /* outputs for fragment shader can't be doubles */
         output_vectors += glsl_count_attribute_slots(var->type, false);
      }
   }

   assert(producer->Stage != MESA_SHADER_FRAGMENT);
   unsigned max_output_components =
      consts->Program[producer->Stage].MaxOutputComponents;

   const unsigned output_components = output_vectors * 4;
   if (output_components > max_output_components) {
      if (api == API_OPENGLES2 || prog->IsES)
         linker_error(prog, "%s shader uses too many output vectors "
                      "(%u > %u)\n",
                      _mesa_shader_stage_to_string(producer->Stage),
                      output_vectors,
                      max_output_components / 4);
      else
         linker_error(prog, "%s shader uses too many output components "
                      "(%u > %u)\n",
                      _mesa_shader_stage_to_string(producer->Stage),
                      output_components,
                      max_output_components);

      return false;
   }

   return true;
}

static bool
check_against_input_limit(const struct gl_constants *consts, gl_api api,
                          struct gl_shader_program *prog,
                          struct gl_linked_shader *consumer,
                          unsigned num_explicit_locations)
{
   unsigned input_vectors = num_explicit_locations;

   nir_foreach_shader_in_variable(var, consumer->Program->nir) {
      if (!var->data.explicit_location &&
          var_counts_against_varying_limit(consumer->Stage, var)) {
         /* vertex inputs aren't varying counted */
         input_vectors += glsl_count_attribute_slots(var->type, false);
      }
   }

   assert(consumer->Stage != MESA_SHADER_VERTEX);
   unsigned max_input_components =
      consts->Program[consumer->Stage].MaxInputComponents;

   const unsigned input_components = input_vectors * 4;
   if (input_components > max_input_components) {
      if (api == API_OPENGLES2 || prog->IsES)
         linker_error(prog, "%s shader uses too many input vectors "
                      "(%u > %u)\n",
                      _mesa_shader_stage_to_string(consumer->Stage),
                      input_vectors,
                      max_input_components / 4);
      else
         linker_error(prog, "%s shader uses too many input components "
                      "(%u > %u)\n",
                      _mesa_shader_stage_to_string(consumer->Stage),
                      input_components,
                      max_input_components);

      return false;
   }

   return true;
}

/* Lower unset/unused inputs/outputs */
static void
remove_unused_shader_inputs_and_outputs(struct gl_shader_program *prog,
                                        unsigned stage, nir_variable_mode mode)
{
   bool progress = false;
   nir_shader *shader = prog->_LinkedShaders[stage]->Program->nir;

   nir_foreach_variable_with_modes_safe(var, shader, mode) {
      if (!var->data.is_xfb_only && var->data.location == -1) {
         var->data.location = 0;
         var->data.mode = nir_var_shader_temp;
         progress = true;
      }
   }

   if (progress)
      fixup_vars_lowered_to_temp(shader, mode);
}

static bool
link_varyings(struct gl_shader_program *prog, unsigned first,
              unsigned last, const struct gl_constants *consts,
              const struct gl_extensions *exts, gl_api api, void *mem_ctx)
{
   bool has_xfb_qualifiers = false;
   unsigned num_xfb_decls = 0;
   char **varying_names = NULL;
   struct xfb_decl *xfb_decls = NULL;

   if (last > MESA_SHADER_FRAGMENT)
      return true;

   /* From the ARB_enhanced_layouts spec:
    *
    *    "If the shader used to record output variables for transform feedback
    *    varyings uses the "xfb_buffer", "xfb_offset", or "xfb_stride" layout
    *    qualifiers, the values specified by TransformFeedbackVaryings are
    *    ignored, and the set of variables captured for transform feedback is
    *    instead derived from the specified layout qualifiers."
    */
   for (int i = MESA_SHADER_FRAGMENT - 1; i >= 0; i--) {
      /* Find last stage before fragment shader */
      if (prog->_LinkedShaders[i]) {
         has_xfb_qualifiers =
            process_xfb_layout_qualifiers(mem_ctx, prog->_LinkedShaders[i],
                                          prog, &num_xfb_decls,
                                          &varying_names);
         break;
      }
   }

   if (!has_xfb_qualifiers) {
      num_xfb_decls = prog->TransformFeedback.NumVarying;
      varying_names = prog->TransformFeedback.VaryingNames;
   }

   if (num_xfb_decls != 0) {
      /* From GL_EXT_transform_feedback:
       *   A program will fail to link if:
       *
       *   * the <count> specified by TransformFeedbackVaryingsEXT is
       *     non-zero, but the program object has no vertex or geometry
       *     shader;
       */
      if (first >= MESA_SHADER_FRAGMENT) {
         linker_error(prog, "Transform feedback varyings specified, but "
                      "no vertex, tessellation, or geometry shader is "
                      "present.\n");
         return false;
      }

      xfb_decls = rzalloc_array(mem_ctx, struct xfb_decl,
                                      num_xfb_decls);
      if (!parse_xfb_decls(consts, exts, prog, mem_ctx, num_xfb_decls,
                           varying_names, xfb_decls))
         return false;
   }

   struct gl_linked_shader *linked_shader[MESA_SHADER_STAGES];
   unsigned num_shaders = 0;

   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      if (prog->_LinkedShaders[i])
         linked_shader[num_shaders++] = prog->_LinkedShaders[i];
   }

   struct varying_matches vm;
   if (last < MESA_SHADER_FRAGMENT &&
       (num_xfb_decls != 0 || prog->SeparateShader)) {
         struct gl_linked_shader *producer = prog->_LinkedShaders[last];
         if (!assign_initial_varying_locations(consts, exts, mem_ctx, prog,
                                               producer, NULL, num_xfb_decls,
                                               xfb_decls, &vm))
            return false;
   }

   if (last <= MESA_SHADER_FRAGMENT && !prog->SeparateShader) {
      remove_unused_shader_inputs_and_outputs(prog, first, nir_var_shader_in);
      remove_unused_shader_inputs_and_outputs(prog, last, nir_var_shader_out);
   }

   if (prog->SeparateShader) {
      struct gl_linked_shader *consumer = linked_shader[0];
      if (!assign_initial_varying_locations(consts, exts, mem_ctx, prog, NULL,
                                            consumer, 0, NULL, &vm))
         return false;
   }

   if (num_shaders == 1) {
      /* Linking shaders also optimizes them. Separate shaders, compute shaders
       * and shaders with a fixed-func VS or FS that don't need linking are
       * optimized here.
       */
      gl_nir_opts(linked_shader[0]->Program->nir);
   } else {
      /* Linking the stages in the opposite order (from fragment to vertex)
       * ensures that inter-shader outputs written to in an earlier stage
       * are eliminated if they are (transitively) not used in a later
       * stage.
       */
      for (int i = num_shaders - 2; i >= 0; i--) {
         unsigned stage_num_xfb_decls =
            linked_shader[i + 1]->Stage == MESA_SHADER_FRAGMENT ?
            num_xfb_decls : 0;

         if (!assign_initial_varying_locations(consts, exts, mem_ctx, prog,
                                               linked_shader[i],
                                               linked_shader[i + 1],
                                               stage_num_xfb_decls, xfb_decls,
                                               &vm))
            return false;

         /* Now that validation is done its safe to remove unused varyings. As
          * we have both a producer and consumer its safe to remove unused
          * varyings even if the program is a SSO because the stages are being
          * linked together i.e. we have a multi-stage SSO.
          */
         link_shader_opts(&vm, linked_shader[i]->Program->nir,
                          linked_shader[i + 1]->Program->nir,
                          prog, mem_ctx);

         remove_unused_shader_inputs_and_outputs(prog, linked_shader[i]->Stage,
                                                 nir_var_shader_out);
         remove_unused_shader_inputs_and_outputs(prog,
                                                 linked_shader[i + 1]->Stage,
                                                 nir_var_shader_in);
      }
   }

   if (!prog->SeparateShader) {
      /* If not SSO remove unused varyings from the first/last stage */
      NIR_PASS_V(prog->_LinkedShaders[first]->Program->nir,
                 nir_remove_dead_variables, nir_var_shader_in, NULL);
      NIR_PASS_V(prog->_LinkedShaders[last]->Program->nir,
                 nir_remove_dead_variables, nir_var_shader_out, NULL);
   } else {
      /* Sort inputs / outputs into a canonical order.  This is necessary so
       * that inputs / outputs of separable shaders will be assigned
       * predictable locations regardless of the order in which declarations
       * appeared in the shader source.
       */
      if (first != MESA_SHADER_VERTEX) {
         canonicalize_shader_io(prog->_LinkedShaders[first]->Program->nir,
                                nir_var_shader_in);
      }

      if (last != MESA_SHADER_FRAGMENT) {
         canonicalize_shader_io(prog->_LinkedShaders[last]->Program->nir,
                                nir_var_shader_out);
      }
   }

   /* If there is no fragment shader we need to set transform feedback.
    *
    * For SSO we also need to assign output locations.  We assign them here
    * because we need to do it for both single stage programs and multi stage
    * programs.
    */
   if (last < MESA_SHADER_FRAGMENT &&
       (num_xfb_decls != 0 || prog->SeparateShader)) {
      const uint64_t reserved_out_slots =
         reserved_varying_slot(prog->_LinkedShaders[last], nir_var_shader_out);
      if (!assign_final_varying_locations(consts, exts, mem_ctx, prog,
                                          prog->_LinkedShaders[last], NULL,
                                          num_xfb_decls, xfb_decls,
                                          reserved_out_slots, &vm))
         return false;
   }

   if (prog->SeparateShader) {
      struct gl_linked_shader *const sh = prog->_LinkedShaders[first];

      const uint64_t reserved_slots =
         reserved_varying_slot(sh, nir_var_shader_in);

      /* Assign input locations for SSO, output locations are already
       * assigned.
       */
      if (!assign_final_varying_locations(consts, exts, mem_ctx, prog,
                                          NULL /* producer */,
                                          sh /* consumer */,
                                          0 /* num_xfb_decls */,
                                          NULL /* xfb_decls */,
                                          reserved_slots, &vm))
         return false;
   }

   if (num_shaders == 1) {
      gl_nir_opt_dead_builtin_varyings(consts, api, prog, NULL, linked_shader[0],
                                       0, NULL);
      gl_nir_opt_dead_builtin_varyings(consts, api, prog, linked_shader[0], NULL,
                                       num_xfb_decls, xfb_decls);
   } else {
      /* Linking the stages in the opposite order (from fragment to vertex)
       * ensures that inter-shader outputs written to in an earlier stage
       * are eliminated if they are (transitively) not used in a later
       * stage.
       */
      int next = last;
      for (int i = next - 1; i >= 0; i--) {
         if (prog->_LinkedShaders[i] == NULL && i != 0)
            continue;

         struct gl_linked_shader *const sh_i = prog->_LinkedShaders[i];
         struct gl_linked_shader *const sh_next = prog->_LinkedShaders[next];

         gl_nir_opt_dead_builtin_varyings(consts, api, prog, sh_i, sh_next,
                                          next == MESA_SHADER_FRAGMENT ? num_xfb_decls : 0,
                                          xfb_decls);

         const uint64_t reserved_out_slots =
            reserved_varying_slot(sh_i, nir_var_shader_out);
         const uint64_t reserved_in_slots =
            reserved_varying_slot(sh_next, nir_var_shader_in);

         if (!assign_final_varying_locations(consts, exts, mem_ctx, prog, sh_i,
                   sh_next, next == MESA_SHADER_FRAGMENT ? num_xfb_decls : 0,
                   xfb_decls, reserved_out_slots | reserved_in_slots, &vm))
            return false;

         /* This must be done after all dead varyings are eliminated. */
         if (sh_i != NULL) {
            unsigned slots_used = util_bitcount64(reserved_out_slots);
            if (!check_against_output_limit(consts, api, prog, sh_i, slots_used))
               return false;
         }

         unsigned slots_used = util_bitcount64(reserved_in_slots);
         if (!check_against_input_limit(consts, api, prog, sh_next, slots_used))
            return false;

         next = i;
      }
   }

   if (!store_tfeedback_info(consts, prog, num_xfb_decls, xfb_decls,
                             has_xfb_qualifiers, mem_ctx))
      return false;

   return true;
}

bool
gl_nir_link_varyings(const struct gl_constants *consts,
                     const struct gl_extensions *exts,
                     gl_api api, struct gl_shader_program *prog)
{
   void *mem_ctx = ralloc_context(NULL);

   unsigned first, last;

   first = MESA_SHADER_STAGES;
   last = 0;

   /* We need to initialise the program resource list because the varying
    * packing pass my start inserting varyings onto the list.
    */
   init_program_resource_list(prog);

   /* Determine first and last stage. */
   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      if (!prog->_LinkedShaders[i])
         continue;
      if (first == MESA_SHADER_STAGES)
         first = i;
      last = i;
   }

   bool r = link_varyings(prog, first, last, consts, exts, api, mem_ctx);
   if (r) {
      for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
         if (!prog->_LinkedShaders[i])
            continue;

         /* Check for transform feedback varyings specified via the API */
         prog->_LinkedShaders[i]->Program->nir->info.has_transform_feedback_varyings =
            prog->TransformFeedback.NumVarying > 0;

         /* Check for transform feedback varyings specified in the Shader */
         if (prog->last_vert_prog) {
            prog->_LinkedShaders[i]->Program->nir->info.has_transform_feedback_varyings |=
               prog->last_vert_prog->sh.LinkedTransformFeedback->NumVarying > 0;
         }
      }

      /* Assign NIR XFB info to the last stage before the fragment shader */
      for (int stage = MESA_SHADER_FRAGMENT - 1; stage >= 0; stage--) {
         struct gl_linked_shader *sh = prog->_LinkedShaders[stage];
         if (sh && stage != MESA_SHADER_TESS_CTRL) {
            sh->Program->nir->xfb_info =
               gl_to_nir_xfb_info(sh->Program->sh.LinkedTransformFeedback,
                                  sh->Program->nir);
            break;
         }
      }
   }

   ralloc_free(mem_ctx);
   return r;
}
