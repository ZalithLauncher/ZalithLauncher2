/*
 * Copyright © 2012 Intel Corporation
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
 * \file link_varyings.cpp
 *
 * Linker functions related specifically to linking varyings between shader
 * stages.
 */


#include "../../mesa/main/errors.h"
#include "../../mesa/main/consts_exts.h"
#include "../../mesa/main/shader_types.h"
#include "glsl_symbol_table.h"
#include "ir.h"
#include "linker.h"
#include "link_varyings.h"


/**
 * Get the varying type stripped of the outermost array if we're processing
 * a stage whose varyings are arrays indexed by a vertex number (such as
 * geometry shader inputs).
 */
static const glsl_type *
get_varying_type(const ir_variable *var, gl_shader_stage stage)
{
   const glsl_type *type = var->type;

   if (!var->data.patch &&
       ((var->data.mode == ir_var_shader_out &&
         stage == MESA_SHADER_TESS_CTRL) ||
        (var->data.mode == ir_var_shader_in &&
         (stage == MESA_SHADER_TESS_CTRL || stage == MESA_SHADER_TESS_EVAL ||
          stage == MESA_SHADER_GEOMETRY)))) {
      assert(type->is_array());
      type = type->fields.array;
   }

   return type;
}

/**
 * Validate the types and qualifiers of an output from one stage against the
 * matching input to another stage.
 */
static void
cross_validate_types_and_qualifiers(const struct gl_constants *consts,
                                    struct gl_shader_program *prog,
                                    const ir_variable *input,
                                    const ir_variable *output,
                                    gl_shader_stage consumer_stage,
                                    gl_shader_stage producer_stage)
{
   /* Check that the types match between stages.
    */
   const glsl_type *type_to_match = input->type;

   /* VS -> GS, VS -> TCS, VS -> TES, TES -> GS */
   const bool extra_array_level = (producer_stage == MESA_SHADER_VERTEX &&
                                   consumer_stage != MESA_SHADER_FRAGMENT) ||
                                  consumer_stage == MESA_SHADER_GEOMETRY;
   if (extra_array_level) {
      assert(type_to_match->is_array());
      type_to_match = type_to_match->fields.array;
   }

   if (type_to_match != output->type) {
      if (output->type->is_struct()) {
         /* Structures across shader stages can have different name
          * and considered to match in type if and only if structure
          * members match in name, type, qualification, and declaration
          * order. The precision doesn’t need to match.
          */
         if (!output->type->record_compare(type_to_match,
                                           false, /* match_name */
                                           true, /* match_locations */
                                           false /* match_precision */)) {
            linker_error(prog,
                  "%s shader output `%s' declared as struct `%s', "
                  "doesn't match in type with %s shader input "
                  "declared as struct `%s'\n",
                  _mesa_shader_stage_to_string(producer_stage),
                  output->name,
                  output->type->name,
                  _mesa_shader_stage_to_string(consumer_stage),
                  input->type->name);
         }
      } else if (!output->type->is_array() || !is_gl_identifier(output->name)) {
         /* There is a bit of a special case for gl_TexCoord.  This
          * built-in is unsized by default.  Applications that variable
          * access it must redeclare it with a size.  There is some
          * language in the GLSL spec that implies the fragment shader
          * and vertex shader do not have to agree on this size.  Other
          * driver behave this way, and one or two applications seem to
          * rely on it.
          *
          * Neither declaration needs to be modified here because the array
          * sizes are fixed later when update_array_sizes is called.
          *
          * From page 48 (page 54 of the PDF) of the GLSL 1.10 spec:
          *
          *     "Unlike user-defined varying variables, the built-in
          *     varying variables don't have a strict one-to-one
          *     correspondence between the vertex language and the
          *     fragment language."
          */
         linker_error(prog,
                      "%s shader output `%s' declared as type `%s', "
                      "but %s shader input declared as type `%s'\n",
                      _mesa_shader_stage_to_string(producer_stage),
                      output->name,
                      output->type->name,
                      _mesa_shader_stage_to_string(consumer_stage),
                      input->type->name);
         return;
      }
   }

   /* Check that all of the qualifiers match between stages.
    */

   /* According to the OpenGL and OpenGLES GLSL specs, the centroid qualifier
    * should match until OpenGL 4.3 and OpenGLES 3.1. The OpenGLES 3.0
    * conformance test suite does not verify that the qualifiers must match.
    * The deqp test suite expects the opposite (OpenGLES 3.1) behavior for
    * OpenGLES 3.0 drivers, so we relax the checking in all cases.
    */
   if (false /* always skip the centroid check */ &&
       prog->GLSL_Version < (prog->IsES ? 310 : 430) &&
       input->data.centroid != output->data.centroid) {
      linker_error(prog,
                   "%s shader output `%s' %s centroid qualifier, "
                   "but %s shader input %s centroid qualifier\n",
                   _mesa_shader_stage_to_string(producer_stage),
                   output->name,
                   (output->data.centroid) ? "has" : "lacks",
                   _mesa_shader_stage_to_string(consumer_stage),
                   (input->data.centroid) ? "has" : "lacks");
      return;
   }

   if (input->data.sample != output->data.sample) {
      linker_error(prog,
                   "%s shader output `%s' %s sample qualifier, "
                   "but %s shader input %s sample qualifier\n",
                   _mesa_shader_stage_to_string(producer_stage),
                   output->name,
                   (output->data.sample) ? "has" : "lacks",
                   _mesa_shader_stage_to_string(consumer_stage),
                   (input->data.sample) ? "has" : "lacks");
      return;
   }

   if (input->data.patch != output->data.patch) {
      linker_error(prog,
                   "%s shader output `%s' %s patch qualifier, "
                   "but %s shader input %s patch qualifier\n",
                   _mesa_shader_stage_to_string(producer_stage),
                   output->name,
                   (output->data.patch) ? "has" : "lacks",
                   _mesa_shader_stage_to_string(consumer_stage),
                   (input->data.patch) ? "has" : "lacks");
      return;
   }

   /* The GLSL 4.20 and GLSL ES 3.00 specifications say:
    *
    *    "As only outputs need be declared with invariant, an output from
    *     one shader stage will still match an input of a subsequent stage
    *     without the input being declared as invariant."
    *
    * while GLSL 4.10 says:
    *
    *    "For variables leaving one shader and coming into another shader,
    *     the invariant keyword has to be used in both shaders, or a link
    *     error will result."
    *
    * and GLSL ES 1.00 section 4.6.4 "Invariance and Linking" says:
    *
    *    "The invariance of varyings that are declared in both the vertex
    *     and fragment shaders must match."
    */
   if (input->data.explicit_invariant != output->data.explicit_invariant &&
       prog->GLSL_Version < (prog->IsES ? 300 : 420)) {
      linker_error(prog,
                   "%s shader output `%s' %s invariant qualifier, "
                   "but %s shader input %s invariant qualifier\n",
                   _mesa_shader_stage_to_string(producer_stage),
                   output->name,
                   (output->data.explicit_invariant) ? "has" : "lacks",
                   _mesa_shader_stage_to_string(consumer_stage),
                   (input->data.explicit_invariant) ? "has" : "lacks");
      return;
   }

   /* GLSL >= 4.40 removes text requiring interpolation qualifiers
    * to match cross stage, they must only match within the same stage.
    *
    * From page 84 (page 90 of the PDF) of the GLSL 4.40 spec:
    *
    *     "It is a link-time error if, within the same stage, the interpolation
    *     qualifiers of variables of the same name do not match.
    *
    * Section 4.3.9 (Interpolation) of the GLSL ES 3.00 spec says:
    *
    *    "When no interpolation qualifier is present, smooth interpolation
    *    is used."
    *
    * So we match variables where one is smooth and the other has no explicit
    * qualifier.
    */
   unsigned input_interpolation = input->data.interpolation;
   unsigned output_interpolation = output->data.interpolation;
   if (prog->IsES) {
      if (input_interpolation == INTERP_MODE_NONE)
         input_interpolation = INTERP_MODE_SMOOTH;
      if (output_interpolation == INTERP_MODE_NONE)
         output_interpolation = INTERP_MODE_SMOOTH;
   }
   if (input_interpolation != output_interpolation &&
       prog->GLSL_Version < 440) {
      if (!consts->AllowGLSLCrossStageInterpolationMismatch) {
         linker_error(prog,
                      "%s shader output `%s' specifies %s "
                      "interpolation qualifier, "
                      "but %s shader input specifies %s "
                      "interpolation qualifier\n",
                      _mesa_shader_stage_to_string(producer_stage),
                      output->name,
                      interpolation_string(output->data.interpolation),
                      _mesa_shader_stage_to_string(consumer_stage),
                      interpolation_string(input->data.interpolation));
         return;
      } else {
         linker_warning(prog,
                        "%s shader output `%s' specifies %s "
                        "interpolation qualifier, "
                        "but %s shader input specifies %s "
                        "interpolation qualifier\n",
                        _mesa_shader_stage_to_string(producer_stage),
                        output->name,
                        interpolation_string(output->data.interpolation),
                        _mesa_shader_stage_to_string(consumer_stage),
                        interpolation_string(input->data.interpolation));
      }
   }
}

/**
 * Validate front and back color outputs against single color input
 */
static void
cross_validate_front_and_back_color(const struct gl_constants *consts,
                                    struct gl_shader_program *prog,
                                    const ir_variable *input,
                                    const ir_variable *front_color,
                                    const ir_variable *back_color,
                                    gl_shader_stage consumer_stage,
                                    gl_shader_stage producer_stage)
{
   if (front_color != NULL && front_color->data.assigned)
      cross_validate_types_and_qualifiers(consts, prog, input, front_color,
                                          consumer_stage, producer_stage);

   if (back_color != NULL && back_color->data.assigned)
      cross_validate_types_and_qualifiers(consts, prog, input, back_color,
                                          consumer_stage, producer_stage);
}

static unsigned
compute_variable_location_slot(ir_variable *var, gl_shader_stage stage)
{
   unsigned location_start = VARYING_SLOT_VAR0;

   switch (stage) {
      case MESA_SHADER_VERTEX:
         if (var->data.mode == ir_var_shader_in)
            location_start = VERT_ATTRIB_GENERIC0;
         break;
      case MESA_SHADER_TESS_CTRL:
      case MESA_SHADER_TESS_EVAL:
         if (var->data.patch)
            location_start = VARYING_SLOT_PATCH0;
         break;
      case MESA_SHADER_FRAGMENT:
         if (var->data.mode == ir_var_shader_out)
            location_start = FRAG_RESULT_DATA0;
         break;
      default:
         break;
   }

   return var->data.location - location_start;
}

struct explicit_location_info {
   ir_variable *var;
   bool base_type_is_integer;
   unsigned base_type_bit_size;
   unsigned interpolation;
   bool centroid;
   bool sample;
   bool patch;
};

static bool
check_location_aliasing(struct explicit_location_info explicit_locations[][4],
                        ir_variable *var,
                        unsigned location,
                        unsigned component,
                        unsigned location_limit,
                        const glsl_type *type,
                        unsigned interpolation,
                        bool centroid,
                        bool sample,
                        bool patch,
                        gl_shader_program *prog,
                        gl_shader_stage stage)
{
   unsigned last_comp;
   unsigned base_type_bit_size;
   const glsl_type *type_without_array = type->without_array();
   const bool base_type_is_integer =
      glsl_base_type_is_integer(type_without_array->base_type);
   const bool is_struct = type_without_array->is_struct();
   if (is_struct) {
      /* structs don't have a defined underlying base type so just treat all
       * component slots as used and set the bit size to 0. If there is
       * location aliasing, we'll fail anyway later.
       */
      last_comp = 4;
      base_type_bit_size = 0;
   } else {
      unsigned dmul = type_without_array->is_64bit() ? 2 : 1;
      last_comp = component + type_without_array->vector_elements * dmul;
      base_type_bit_size =
         glsl_base_type_get_bit_size(type_without_array->base_type);
   }

   while (location < location_limit) {
      unsigned comp = 0;
      while (comp < 4) {
         struct explicit_location_info *info =
            &explicit_locations[location][comp];

         if (info->var) {
            if (info->var->type->without_array()->is_struct() || is_struct) {
               /* Structs cannot share location since they are incompatible
                * with any other underlying numerical type.
                */
               linker_error(prog,
                            "%s shader has multiple %sputs sharing the "
                            "same location that don't have the same "
                            "underlying numerical type. Struct variable '%s', "
                            "location %u\n",
                            _mesa_shader_stage_to_string(stage),
                            var->data.mode == ir_var_shader_in ? "in" : "out",
                            is_struct ? var->name : info->var->name,
                            location);
               return false;
            } else if (comp >= component && comp < last_comp) {
               /* Component aliasing is not allowed */
               linker_error(prog,
                            "%s shader has multiple %sputs explicitly "
                            "assigned to location %d and component %d\n",
                            _mesa_shader_stage_to_string(stage),
                            var->data.mode == ir_var_shader_in ? "in" : "out",
                            location, comp);
               return false;
            } else {
               /* From the OpenGL 4.60.5 spec, section 4.4.1 Input Layout
                * Qualifiers, Page 67, (Location aliasing):
                *
                *   " Further, when location aliasing, the aliases sharing the
                *     location must have the same underlying numerical type
                *     and bit width (floating-point or integer, 32-bit versus
                *     64-bit, etc.) and the same auxiliary storage and
                *     interpolation qualification."
                */

               /* If the underlying numerical type isn't integer, implicitly
                * it will be float or else we would have failed by now.
                */
               if (info->base_type_is_integer != base_type_is_integer) {
                  linker_error(prog,
                               "%s shader has multiple %sputs sharing the "
                               "same location that don't have the same "
                               "underlying numerical type. Location %u "
                               "component %u.\n",
                               _mesa_shader_stage_to_string(stage),
                               var->data.mode == ir_var_shader_in ?
                               "in" : "out", location, comp);
                  return false;
               }

               if (info->base_type_bit_size != base_type_bit_size) {
                  linker_error(prog,
                               "%s shader has multiple %sputs sharing the "
                               "same location that don't have the same "
                               "underlying numerical bit size. Location %u "
                               "component %u.\n",
                               _mesa_shader_stage_to_string(stage),
                               var->data.mode == ir_var_shader_in ?
                               "in" : "out", location, comp);
                  return false;
               }

               if (info->interpolation != interpolation) {
                  linker_error(prog,
                               "%s shader has multiple %sputs sharing the "
                               "same location that don't have the same "
                               "interpolation qualification. Location %u "
                               "component %u.\n",
                               _mesa_shader_stage_to_string(stage),
                               var->data.mode == ir_var_shader_in ?
                               "in" : "out", location, comp);
                  return false;
               }

               if (info->centroid != centroid ||
                   info->sample != sample ||
                   info->patch != patch) {
                  linker_error(prog,
                               "%s shader has multiple %sputs sharing the "
                               "same location that don't have the same "
                               "auxiliary storage qualification. Location %u "
                               "component %u.\n",
                               _mesa_shader_stage_to_string(stage),
                               var->data.mode == ir_var_shader_in ?
                               "in" : "out", location, comp);
                  return false;
               }
            }
         } else if (comp >= component && comp < last_comp) {
            info->var = var;
            info->base_type_is_integer = base_type_is_integer;
            info->base_type_bit_size = base_type_bit_size;
            info->interpolation = interpolation;
            info->centroid = centroid;
            info->sample = sample;
            info->patch = patch;
         }

         comp++;

         /* We need to do some special handling for doubles as dvec3 and
          * dvec4 consume two consecutive locations. We don't need to
          * worry about components beginning at anything other than 0 as
          * the spec does not allow this for dvec3 and dvec4.
          */
         if (comp == 4 && last_comp > 4) {
            last_comp = last_comp - 4;
            /* Bump location index and reset the component index */
            location++;
            comp = 0;
            component = 0;
         }
      }

      location++;
   }

   return true;
}

static bool
validate_explicit_variable_location(const struct gl_constants *consts,
                                    struct explicit_location_info explicit_locations[][4],
                                    ir_variable *var,
                                    gl_shader_program *prog,
                                    gl_linked_shader *sh)
{
   const glsl_type *type = get_varying_type(var, sh->Stage);
   unsigned num_elements = type->count_attribute_slots(false);
   unsigned idx = compute_variable_location_slot(var, sh->Stage);
   unsigned slot_limit = idx + num_elements;

   /* Vertex shader inputs and fragment shader outputs are validated in
    * assign_attribute_or_color_locations() so we should not attempt to
    * validate them again here.
    */
   unsigned slot_max;
   if (var->data.mode == ir_var_shader_out) {
      assert(sh->Stage != MESA_SHADER_FRAGMENT);
      slot_max =
         consts->Program[sh->Stage].MaxOutputComponents / 4;
   } else {
      assert(var->data.mode == ir_var_shader_in);
      assert(sh->Stage != MESA_SHADER_VERTEX);
      slot_max =
         consts->Program[sh->Stage].MaxInputComponents / 4;
   }

   if (slot_limit > slot_max) {
      linker_error(prog,
                   "Invalid location %u in %s shader\n",
                   idx, _mesa_shader_stage_to_string(sh->Stage));
      return false;
   }

   const glsl_type *type_without_array = type->without_array();
   if (type_without_array->is_interface()) {
      for (unsigned i = 0; i < type_without_array->length; i++) {
         glsl_struct_field *field = &type_without_array->fields.structure[i];
         unsigned field_location = field->location -
            (field->patch ? VARYING_SLOT_PATCH0 : VARYING_SLOT_VAR0);
         unsigned field_slots = field->type->count_attribute_slots(false);
         if (!check_location_aliasing(explicit_locations, var,
                                      field_location,
                                      0,
                                      field_location + field_slots,
                                      field->type,
                                      field->interpolation,
                                      field->centroid,
                                      field->sample,
                                      field->patch,
                                      prog, sh->Stage)) {
            return false;
         }
      }
   } else if (!check_location_aliasing(explicit_locations, var,
                                       idx, var->data.location_frac,
                                       slot_limit, type,
                                       var->data.interpolation,
                                       var->data.centroid,
                                       var->data.sample,
                                       var->data.patch,
                                       prog, sh->Stage)) {
      return false;
   }

   return true;
}

/**
 * Validate explicit locations for the inputs to the first stage and the
 * outputs of the last stage in a program, if those are not the VS and FS
 * shaders.
 */
void
validate_first_and_last_interface_explicit_locations(const struct gl_constants *consts,
                                                     struct gl_shader_program *prog,
                                                     gl_shader_stage first_stage,
                                                     gl_shader_stage last_stage)
{
   /* VS inputs and FS outputs are validated in
    * assign_attribute_or_color_locations()
    */
   bool validate_first_stage = first_stage != MESA_SHADER_VERTEX;
   bool validate_last_stage = last_stage != MESA_SHADER_FRAGMENT;
   if (!validate_first_stage && !validate_last_stage)
      return;

   struct explicit_location_info explicit_locations[MAX_VARYING][4];

   gl_shader_stage stages[2] = { first_stage, last_stage };
   bool validate_stage[2] = { validate_first_stage, validate_last_stage };
   ir_variable_mode var_direction[2] = { ir_var_shader_in, ir_var_shader_out };

   for (unsigned i = 0; i < 2; i++) {
      if (!validate_stage[i])
         continue;

      gl_shader_stage stage = stages[i];

      gl_linked_shader *sh = prog->_LinkedShaders[stage];
      assert(sh);

      memset(explicit_locations, 0, sizeof(explicit_locations));

      foreach_in_list(ir_instruction, node, sh->ir) {
         ir_variable *const var = node->as_variable();

         if (var == NULL ||
             !var->data.explicit_location ||
             var->data.location < VARYING_SLOT_VAR0 ||
             var->data.mode != var_direction[i])
            continue;

         if (!validate_explicit_variable_location(
               consts, explicit_locations, var, prog, sh)) {
            return;
         }
      }
   }
}

/**
 * Check if we should force input / output matching between shader
 * interfaces.
 *
 * Section 4.3.4 (Inputs) of the GLSL 4.10 specifications say:
 *
 *   "Only the input variables that are actually read need to be
 *    written by the previous stage; it is allowed to have
 *    superfluous declarations of input variables."
 *
 * However it's not defined anywhere as to how we should handle
 * inputs that are not written in the previous stage and it's not
 * clear what "actually read" means.
 *
 * The GLSL 4.20 spec however is much clearer:
 *
 *    "Only the input variables that are statically read need to
 *     be written by the previous stage; it is allowed to have
 *     superfluous declarations of input variables."
 *
 * It also has a table that states it is an error to statically
 * read an input that is not defined in the previous stage. While
 * it is not an error to not statically write to the output (it
 * just needs to be defined to not be an error).
 *
 * The text in the GLSL 4.20 spec was an attempt to clarify the
 * previous spec iterations. However given the difference in spec
 * and that some applications seem to depend on not erroring when
 * the input is not actually read in control flow we only apply
 * this rule to GLSL 4.20 and higher. GLSL 4.10 shaders have been
 * seen in the wild that depend on the less strict interpretation.
 */
static bool
static_input_output_matching(struct gl_shader_program *prog)
{
   return prog->GLSL_Version >= (prog->IsES ? 0 : 420);
}

/**
 * Validate that outputs from one stage match inputs of another
 */
void
cross_validate_outputs_to_inputs(const struct gl_constants *consts,
                                 struct gl_shader_program *prog,
                                 gl_linked_shader *producer,
                                 gl_linked_shader *consumer)
{
   glsl_symbol_table parameters;
   struct explicit_location_info output_explicit_locations[MAX_VARYING][4] = {};
   struct explicit_location_info input_explicit_locations[MAX_VARYING][4] = {};

   /* Find all shader outputs in the "producer" stage.
    */
   foreach_in_list(ir_instruction, node, producer->ir) {
      ir_variable *const var = node->as_variable();

      if (var == NULL || var->data.mode != ir_var_shader_out)
         continue;

      if (!var->data.explicit_location
          || var->data.location < VARYING_SLOT_VAR0)
         parameters.add_variable(var);
      else {
         /* User-defined varyings with explicit locations are handled
          * differently because they do not need to have matching names.
          */
         if (!validate_explicit_variable_location(consts,
                                                  output_explicit_locations,
                                                  var, prog, producer)) {
            return;
         }
      }
   }


   /* Find all shader inputs in the "consumer" stage.  Any variables that have
    * matching outputs already in the symbol table must have the same type and
    * qualifiers.
    *
    * Exception: if the consumer is the geometry shader, then the inputs
    * should be arrays and the type of the array element should match the type
    * of the corresponding producer output.
    */
   foreach_in_list(ir_instruction, node, consumer->ir) {
      ir_variable *const input = node->as_variable();

      if (input == NULL || input->data.mode != ir_var_shader_in)
         continue;

      if (strcmp(input->name, "gl_Color") == 0 && input->data.used) {
         const ir_variable *const front_color =
            parameters.get_variable("gl_FrontColor");

         const ir_variable *const back_color =
            parameters.get_variable("gl_BackColor");

         cross_validate_front_and_back_color(consts, prog, input,
                                             front_color, back_color,
                                             consumer->Stage, producer->Stage);
      } else if (strcmp(input->name, "gl_SecondaryColor") == 0 && input->data.used) {
         const ir_variable *const front_color =
            parameters.get_variable("gl_FrontSecondaryColor");

         const ir_variable *const back_color =
            parameters.get_variable("gl_BackSecondaryColor");

         cross_validate_front_and_back_color(consts, prog, input,
                                             front_color, back_color,
                                             consumer->Stage, producer->Stage);
      } else {
         /* The rules for connecting inputs and outputs change in the presence
          * of explicit locations.  In this case, we no longer care about the
          * names of the variables.  Instead, we care only about the
          * explicitly assigned location.
          */
         ir_variable *output = NULL;
         if (input->data.explicit_location
             && input->data.location >= VARYING_SLOT_VAR0) {

            const glsl_type *type = get_varying_type(input, consumer->Stage);
            unsigned num_elements = type->count_attribute_slots(false);
            unsigned idx =
               compute_variable_location_slot(input, consumer->Stage);
            unsigned slot_limit = idx + num_elements;

            if (!validate_explicit_variable_location(consts,
                                                     input_explicit_locations,
                                                     input, prog, consumer)) {
               return;
            }

            while (idx < slot_limit) {
               if (idx >= MAX_VARYING) {
                  linker_error(prog,
                               "Invalid location %u in %s shader\n", idx,
                               _mesa_shader_stage_to_string(consumer->Stage));
                  return;
               }

               output = output_explicit_locations[idx][input->data.location_frac].var;

               if (output == NULL) {
                  /* A linker failure should only happen when there is no
                   * output declaration and there is Static Use of the
                   * declared input.
                   */
                  if (input->data.used && static_input_output_matching(prog)) {
                     linker_error(prog,
                                  "%s shader input `%s' with explicit location "
                                  "has no matching output\n",
                                  _mesa_shader_stage_to_string(consumer->Stage),
                                  input->name);
                     break;
                  }
               } else if (input->data.location != output->data.location) {
                  linker_error(prog,
                               "%s shader input `%s' with explicit location "
                               "has no matching output\n",
                               _mesa_shader_stage_to_string(consumer->Stage),
                               input->name);
                  break;
               }
               idx++;
            }
         } else {
            output = parameters.get_variable(input->name);
         }

         if (output != NULL) {
            /* Interface blocks have their own validation elsewhere so don't
             * try validating them here.
             */
            if (!(input->get_interface_type() &&
                  output->get_interface_type()))
               cross_validate_types_and_qualifiers(consts, prog, input, output,
                                                   consumer->Stage,
                                                   producer->Stage);
         } else {
            /* Check for input vars with unmatched output vars in prev stage
             * taking into account that interface blocks could have a matching
             * output but with different name, so we ignore them.
             */
            assert(!input->data.assigned);
            if (input->data.used && !input->get_interface_type() &&
                !input->data.explicit_location &&
                static_input_output_matching(prog))
               linker_error(prog,
                            "%s shader input `%s' "
                            "has no matching output in the previous stage\n",
                            _mesa_shader_stage_to_string(consumer->Stage),
                            input->name);
         }
      }
   }
}
