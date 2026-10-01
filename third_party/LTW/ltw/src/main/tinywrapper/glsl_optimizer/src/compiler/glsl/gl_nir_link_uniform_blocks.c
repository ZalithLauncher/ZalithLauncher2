/*
 * Copyright © 2019 Intel Corporation
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
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

#include "../nir/nir.h"
#include "gl_nir_linker.h"
#include "ir_uniform.h" /* for gl_uniform_storage */
#include "linker_util.h"
#include "../../mesa/main/shader_types.h"

/**
 * This file contains code to do a nir-based linking for uniform blocks. This
 * includes ubos and ssbos.
 *
 * For the case of ARB_gl_spirv there are some differences compared with GLSL:
 *
 * 1. Linking doesn't use names: GLSL linking use names as core concept. But
 *    on SPIR-V, uniform block name, fields names, and other names are
 *    considered optional debug infor so could not be present. So the linking
 *    should work without it, and it is optional to not handle them at
 *    all. From ARB_gl_spirv spec.
 *
 *    "19. How should the program interface query operations behave for program
 *         objects created from SPIR-V shaders?
 *
 *     DISCUSSION: we previously said we didn't need reflection to work for
 *     SPIR-V shaders (at least for the first version), however we are left
 *     with specifying how it should "not work". The primary issue is that
 *     SPIR-V binaries are not required to have names associated with
 *     variables. They can be associated in debug information, but there is no
 *     requirement for that to be present, and it should not be relied upon.
 *
 *     Options:
 *
 *     <skip>
 *
 *    C) Allow as much as possible to work "naturally". You can query for the
 *    number of active resources, and for details about them. Anything that
 *    doesn't query by name will work as expected. Queries for maximum length
 *    of names return one. Queries for anything "by name" return INVALID_INDEX
 *    (or -1). Querying the name property of a resource returns an empty
 *    string. This may allow many queries to work, but it's not clear how
 *    useful it would be if you can't actually know which specific variable
 *    you are retrieving information on. If everything is specified a-priori
 *    by location/binding/offset/index/component in the shader, this may be
 *    sufficient.
 *
 *    RESOLVED.  Pick (c), but also allow debug names to be returned if an
 *    implementation wants to."
 *
 * When linking SPIR-V shaders this implemention doesn't care for the names,
 * as the main objective is functional, and not support optional debug
 * features.
 *
 * 2. Terminology: this file handles both UBO and SSBO, including both as
 *    "uniform blocks" analogously to what is done in the GLSL (IR) path.
 *
 *    From ARB_gl_spirv spec:
 *      "Mapping of Storage Classes:
 *       <skip>
 *       uniform blockN { ... } ...;  -> Uniform, with Block decoration
 *       <skip>
 *       buffer  blockN { ... } ...;  -> Uniform, with BufferBlock decoration"
 *
 * 3. Explicit data: for the SPIR-V path the code assumes that all structure
 *    members have an Offset decoration, all arrays have an ArrayStride and
 *    all matrices have a MatrixStride, even for nested structures. That way
 *    we don’t have to worry about the different layout modes. This is
 *    explicitly required in the SPIR-V spec:
 *
 *    "Composite objects in the UniformConstant, Uniform, and PushConstant
 *     Storage Classes must be explicitly laid out. The following apply to all
 *     the aggregate and matrix types describing such an object, recursively
 *     through their nested types:
 *
 *    – Each structure-type member must have an Offset Decoration.
 *    – Each array type must have an ArrayStride Decoration.
 *    – Each structure-type member that is a matrix or array-of-matrices must
 *      have be decorated with a MatrixStride Decoration, and one of the
 *      RowMajor or ColMajor Decorations."
 *
 *    Additionally, the structure members are expected to be presented in
 *    increasing offset order:
 *
 *   "a structure has lower-numbered members appearing at smaller offsets than
 *    higher-numbered members"
 */

enum block_type {
   BLOCK_UBO,
   BLOCK_SSBO
};

/*
 * It is worth to note that ARB_gl_spirv spec doesn't require us to do this
 * validation, but at the same time, it allow us to do it. The following
 * validation is easy and a nice-to-have.
*/
static bool
link_blocks_are_compatible(const struct gl_uniform_block *a,
                           const struct gl_uniform_block *b)
{
   /*
    * Names on ARB_gl_spirv are optional, so we are ignoring them. So
    * meanwhile on the equivalent GLSL method the matching is done using the
    * name, here we use the binding, that for SPIR-V binaries is explicit, and
    * mandatory, from OpenGL 4.6 spec, section "7.4.2. SPIR-V Shader Interface
    * Matching":
    *    "Uniform and shader storage block variables must also be decorated
    *     with a Binding"
    */
   if (a->Binding != b->Binding)
      return false;

   /* We are explicitly ignoring the names, so it would be good to check that
    * this is happening.
    */
   assert(a->name.string == NULL);
   assert(b->name.string == NULL);

   if (a->NumUniforms != b->NumUniforms)
      return false;

   if (a->_Packing != b->_Packing)
      return false;

   if (a->_RowMajor != b->_RowMajor)
      return false;

   for (unsigned i = 0; i < a->NumUniforms; i++) {
      if (a->Uniforms[i].Type != b->Uniforms[i].Type)
         return false;

      if (a->Uniforms[i].RowMajor != b->Uniforms[i].RowMajor)
         return false;

      if (a->Uniforms[i].Offset != b->Uniforms[i].Offset)
         return false;

      /* See comment on previous assert */
      assert(a->Uniforms[i].Name == NULL);
      assert(b->Uniforms[i].Name == NULL);
   }

   return true;
}

/**
 * Merges a buffer block into an array of buffer blocks that may or may not
 * already contain a copy of it.
 *
 * Returns the index of the block in the array (new if it was needed, or the
 * index of the copy of it). -1 if there are two incompatible block
 * definitions with the same binding.
 *
 */
static int
link_cross_validate_uniform_block(void *mem_ctx,
                                  struct gl_uniform_block **linked_blocks,
                                  unsigned int *num_linked_blocks,
                                  struct gl_uniform_block *new_block)
{
   /* We first check if new_block was already linked */
   for (unsigned int i = 0; i < *num_linked_blocks; i++) {
      struct gl_uniform_block *old_block = &(*linked_blocks)[i];

      if (old_block->Binding == new_block->Binding)
         return link_blocks_are_compatible(old_block, new_block) ? i : -1;
   }

   *linked_blocks = reralloc(mem_ctx, *linked_blocks,
                             struct gl_uniform_block,
                             *num_linked_blocks + 1);
   int linked_block_index = (*num_linked_blocks)++;
   struct gl_uniform_block *linked_block = &(*linked_blocks)[linked_block_index];

   memcpy(linked_block, new_block, sizeof(*new_block));
   linked_block->Uniforms = ralloc_array(*linked_blocks,
                                         struct gl_uniform_buffer_variable,
                                         linked_block->NumUniforms);

   memcpy(linked_block->Uniforms,
          new_block->Uniforms,
          sizeof(*linked_block->Uniforms) * linked_block->NumUniforms);

   return linked_block_index;
}


/**
 * Accumulates the array of buffer blocks and checks that all definitions of
 * blocks agree on their contents.
 */
static bool
nir_interstage_cross_validate_uniform_blocks(struct gl_shader_program *prog,
                                             enum block_type block_type)
{
   int *interfaceBlockStageIndex[MESA_SHADER_STAGES];
   struct gl_uniform_block *blks = NULL;
   unsigned *num_blks = block_type == BLOCK_SSBO ? &prog->data->NumShaderStorageBlocks :
      &prog->data->NumUniformBlocks;

   unsigned max_num_buffer_blocks = 0;
   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      if (prog->_LinkedShaders[i]) {
         if (block_type == BLOCK_SSBO) {
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

      interfaceBlockStageIndex[i] = malloc(max_num_buffer_blocks * sizeof(int));
      for (unsigned int j = 0; j < max_num_buffer_blocks; j++)
         interfaceBlockStageIndex[i][j] = -1;

      if (sh == NULL)
         continue;

      unsigned sh_num_blocks;
      struct gl_uniform_block **sh_blks;
      if (block_type == BLOCK_SSBO) {
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
            /* We use the binding as we are ignoring the names */
            linker_error(prog, "buffer block with binding `%i' has mismatching "
                         "definitions\n", sh_blks[j]->Binding);

            for (unsigned k = 0; k <= i; k++) {
               free(interfaceBlockStageIndex[k]);
            }

            /* Reset the block count. This will help avoid various segfaults
             * from api calls that assume the array exists due to the count
             * being non-zero.
             */
            *num_blks = 0;
            return false;
         }

         interfaceBlockStageIndex[i][index] = j;
      }
   }

   /* Update per stage block pointers to point to the program list.
    */
   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      for (unsigned j = 0; j < *num_blks; j++) {
         int stage_index = interfaceBlockStageIndex[i][j];

         if (stage_index != -1) {
            struct gl_linked_shader *sh = prog->_LinkedShaders[i];

            struct gl_uniform_block **sh_blks = block_type == BLOCK_SSBO ?
               sh->Program->sh.ShaderStorageBlocks :
               sh->Program->sh.UniformBlocks;

            blks[j].stageref |= sh_blks[stage_index]->stageref;
            sh_blks[stage_index] = &blks[j];
         }
      }
   }

   for (unsigned i = 0; i < MESA_SHADER_STAGES; i++) {
      free(interfaceBlockStageIndex[i]);
   }

   if (block_type == BLOCK_SSBO)
      prog->data->ShaderStorageBlocks = blks;
   else {
      prog->data->NumUniformBlocks = *num_blks;
      prog->data->UniformBlocks = blks;
   }

   return true;
}

/*
 * Iterates @type in order to compute how many individual leaf variables
 * contains.
 */
static void
iterate_type_count_variables(const struct glsl_type *type,
                             unsigned int *num_variables)
{
   for (unsigned i = 0; i < glsl_get_length(type); i++) {
      const struct glsl_type *field_type;

      if (glsl_type_is_struct_or_ifc(type))
         field_type = glsl_get_struct_field(type, i);
      else
         field_type = glsl_get_array_element(type);

      if (glsl_type_is_leaf(field_type) || glsl_type_is_unsized_array(field_type))
         (*num_variables)++;
      else
         iterate_type_count_variables(field_type, num_variables);
   }
}


static void
fill_individual_variable(const struct glsl_type *type,
                         struct gl_uniform_buffer_variable *variables,
                         unsigned int *variable_index,
                         unsigned int *offset,
                         struct gl_shader_program *prog,
                         struct gl_uniform_block *block)
{
   /* ARB_gl_spirv: allowed to ignore names. Thus, we don't need to initialize
    * the variable's Name or IndexName.
    */
   variables[*variable_index].Type = type;

   if (glsl_type_is_matrix(type)) {
      variables[*variable_index].RowMajor = glsl_matrix_type_is_row_major(type);
   } else {
      /* default value, better that potential meaningless garbage */
      variables[*variable_index].RowMajor = false;
   }

   /**
    * Although ARB_gl_spirv points that the offsets need to be included (see
    * "Mappings of layouts"), in the end those are only valid for
    * root-variables, and we would need to recompute offsets when we iterate
    * over non-trivial types, like aoa. So we compute the offset always.
    */
   variables[*variable_index].Offset = *offset;
   (*offset) += glsl_get_explicit_size(type, true);

   (*variable_index)++;
}

static void
iterate_type_fill_variables(const struct glsl_type *type,
                            struct gl_uniform_buffer_variable *variables,
                            unsigned int *variable_index,
                            unsigned int *offset,
                            struct gl_shader_program *prog,
                            struct gl_uniform_block *block)
{
   unsigned struct_base_offset;

   bool struct_or_ifc = glsl_type_is_struct_or_ifc(type);
   if (struct_or_ifc)
      struct_base_offset = *offset;

   for (unsigned i = 0; i < glsl_get_length(type); i++) {
      const struct glsl_type *field_type;

      if (struct_or_ifc) {
         field_type = glsl_get_struct_field(type, i);

         *offset = struct_base_offset + glsl_get_struct_field_offset(type, i);
      } else {
         field_type = glsl_get_array_element(type);
      }

      if (glsl_type_is_leaf(field_type) || glsl_type_is_unsized_array(field_type)) {
         fill_individual_variable(field_type, variables, variable_index,
                                  offset, prog, block);
      } else {
         iterate_type_fill_variables(field_type, variables, variable_index,
                                     offset, prog, block);
      }
   }
}

/*
 * In opposite to the equivalent glsl one, this one only allocates the needed
 * space. We do a initial count here, just to avoid re-allocating for each one
 * we find.
 */
static void
allocate_uniform_blocks(void *mem_ctx,
                        struct gl_linked_shader *shader,
                        struct gl_uniform_block **out_blks, unsigned *num_blocks,
                        struct gl_uniform_buffer_variable **out_variables,
                        unsigned *num_variables,
                        enum block_type block_type)
{
   *num_variables = 0;
   *num_blocks = 0;

   nir_foreach_variable_in_shader(var, shader->Program->nir) {
      if (block_type == BLOCK_UBO && !nir_variable_is_in_ubo(var))
         continue;

      if (block_type == BLOCK_SSBO && !nir_variable_is_in_ssbo(var))
         continue;

      const struct glsl_type *type = glsl_without_array(var->type);
      unsigned aoa_size = glsl_get_aoa_size(var->type);
      unsigned buffer_count = aoa_size == 0 ? 1 : aoa_size;

      *num_blocks += buffer_count;

      unsigned int block_variables = 0;
      iterate_type_count_variables(type, &block_variables);

      *num_variables += block_variables * buffer_count;
   }

   if (*num_blocks == 0) {
      assert(*num_variables == 0);
      return;
   }

   assert(*num_variables != 0);

   struct gl_uniform_block *blocks =
      rzalloc_array(mem_ctx, struct gl_uniform_block, *num_blocks);

   struct gl_uniform_buffer_variable *variables =
      rzalloc_array(blocks, struct gl_uniform_buffer_variable, *num_variables);

   *out_blks = blocks;
   *out_variables = variables;
}

static void
fill_block(struct gl_uniform_block *block,
           nir_variable *var,
           struct gl_uniform_buffer_variable *variables,
           unsigned *variable_index,
           unsigned array_index,
           struct gl_shader_program *prog,
           const gl_shader_stage stage)
{
   const struct glsl_type *type = glsl_without_array(var->type);

   block->name.string = NULL; /* ARB_gl_spirv: allowed to ignore names */
   resource_name_updated(&block->name);
   /* From ARB_gl_spirv spec:
    *    "Vulkan uses only one binding point for a resource array,
    *     while OpenGL still uses multiple binding points, so binding
    *     numbers are counted differently for SPIR-V used in Vulkan
    *     and OpenGL
    */
   block->Binding = var->data.binding + array_index;
   block->Uniforms = &variables[*variable_index];
   block->stageref = 1U << stage;

   /* From SPIR-V 1.0 spec, 3.20, Decoration:
    *    "RowMajor
    *     Applies only to a member of a structure type.
    *     Only valid on a matrix or array whose most basic
    *     element is a matrix. Indicates that components
    *     within a row are contiguous in memory."
    *
    * So the SPIR-V binary doesn't report if the block was defined as RowMajor
    * or not. In any case, for the components it is mandatory to set it, so it
    * is not needed a default RowMajor value to know it.
    *
    * Setting to the default, but it should be ignored.
    */
   block->_RowMajor = false;

   /* From ARB_gl_spirv spec:
    *     "Mapping of layouts
    *
    *       std140/std430 -> explicit *Offset*, *ArrayStride*, and
    *                        *MatrixStride* Decoration on struct members
    *       shared/packed  ->  not allowed"
    *
    * So we would not have a value for _Packing, and in fact it would be
    * useless so far. Using a default value. It should be ignored.
    */
   block->_Packing = 0;
   block->linearized_array_index = array_index;

   unsigned old_variable_index = *variable_index;
   unsigned offset = 0;
   iterate_type_fill_variables(type, variables, variable_index, &offset, prog, block);
   block->NumUniforms = *variable_index - old_variable_index;

   block->UniformBufferSize =  glsl_get_explicit_size(type, false);

   /* From OpenGL 4.6 spec, section 7.6.2.3, "SPIR-V Uniform Offsets and
    * strides"
    *
    *   "If the variable is decorated as a BufferBlock , its offsets and
    *    strides must not contradict std430 alignment and minimum offset
    *    requirements. Otherwise, its offsets and strides must not contradict
    *    std140 alignment and minimum offset requirements."
    *
    * So although we are computing the size based on the offsets and
    * array/matrix strides, at the end we need to ensure that the alignment is
    * the same that with std140. From ARB_uniform_buffer_object spec:
    *
    *   "For uniform blocks laid out according to [std140] rules, the minimum
    *    buffer object size returned by the UNIFORM_BLOCK_DATA_SIZE query is
    *    derived by taking the offset of the last basic machine unit consumed
    *    by the last uniform of the uniform block (including any end-of-array
    *    or end-of-structure padding), adding one, and rounding up to the next
    *    multiple of the base alignment required for a vec4."
    */
   block->UniformBufferSize = glsl_align(block->UniformBufferSize, 16);
}

/*
 * Link ubos/ssbos for a given linked_shader/stage.
 */
static void
link_linked_shader_uniform_blocks(void *mem_ctx,
                                  struct gl_shader_program *prog,
                                  struct gl_linked_shader *shader,
                                  struct gl_uniform_block **blocks,
                                  unsigned *num_blocks,
                                  enum block_type block_type)
{
   struct gl_uniform_buffer_variable *variables = NULL;
   unsigned num_variables = 0;

   allocate_uniform_blocks(mem_ctx, shader,
                           blocks, num_blocks,
                           &variables, &num_variables,
                           block_type);

   /* Fill the content of uniforms and variables */
   unsigned block_index = 0;
   unsigned variable_index = 0;
   struct gl_uniform_block *blks = *blocks;

   nir_foreach_variable_in_shader(var, shader->Program->nir) {
      if (block_type == BLOCK_UBO && !nir_variable_is_in_ubo(var))
         continue;

      if (block_type == BLOCK_SSBO && !nir_variable_is_in_ssbo(var))
         continue;

      unsigned aoa_size = glsl_get_aoa_size(var->type);
      unsigned buffer_count = aoa_size == 0 ? 1 : aoa_size;

      for (unsigned array_index = 0; array_index < buffer_count; array_index++) {
         fill_block(&blks[block_index], var, variables, &variable_index,
                    array_index, prog, shader->Stage);
         block_index++;
      }
   }

   assert(block_index == *num_blocks);
   assert(variable_index == num_variables);
}

bool
gl_nir_link_uniform_blocks(struct gl_shader_program *prog)
{
   void *mem_ctx = ralloc_context(NULL);
   bool ret = false;
   for (int stage = 0; stage < MESA_SHADER_STAGES; stage++) {
      struct gl_linked_shader *const linked = prog->_LinkedShaders[stage];
      struct gl_uniform_block *ubo_blocks = NULL;
      unsigned num_ubo_blocks = 0;
      struct gl_uniform_block *ssbo_blocks = NULL;
      unsigned num_ssbo_blocks = 0;

      if (!linked)
         continue;

      link_linked_shader_uniform_blocks(mem_ctx, prog, linked,
                                        &ubo_blocks, &num_ubo_blocks,
                                        BLOCK_UBO);

      link_linked_shader_uniform_blocks(mem_ctx, prog, linked,
                                        &ssbo_blocks, &num_ssbo_blocks,
                                        BLOCK_SSBO);

      if (!prog->data->LinkStatus) {
         goto out;
      }

      prog->data->linked_stages |= 1 << stage;

      /* Copy ubo blocks to linked shader list */
      linked->Program->sh.UniformBlocks =
         ralloc_array(linked, struct gl_uniform_block *, num_ubo_blocks);
      ralloc_steal(linked, ubo_blocks);
      linked->Program->sh.NumUniformBlocks = num_ubo_blocks;
      for (unsigned i = 0; i < num_ubo_blocks; i++) {
         linked->Program->sh.UniformBlocks[i] = &ubo_blocks[i];
      }

      /* We need to set it twice to avoid the value being overwritten by the
       * one from nir in brw_shader_gather_info. TODO: get a way to set the
       * info once, and being able to gather properly the info.
       */
      linked->Program->nir->info.num_ubos = num_ubo_blocks;
      linked->Program->info.num_ubos = num_ubo_blocks;

      /* Copy ssbo blocks to linked shader list */
      linked->Program->sh.ShaderStorageBlocks =
         ralloc_array(linked, struct gl_uniform_block *, num_ssbo_blocks);
      ralloc_steal(linked, ssbo_blocks);
      for (unsigned i = 0; i < num_ssbo_blocks; i++) {
         linked->Program->sh.ShaderStorageBlocks[i] = &ssbo_blocks[i];
      }

      /* See previous comment on num_ubo_blocks */
      linked->Program->nir->info.num_ssbos = num_ssbo_blocks;
      linked->Program->info.num_ssbos = num_ssbo_blocks;
   }

   if (!nir_interstage_cross_validate_uniform_blocks(prog, BLOCK_UBO))
      goto out;

   if (!nir_interstage_cross_validate_uniform_blocks(prog, BLOCK_SSBO))
      goto out;

   ret = true;
out:
   ralloc_free(mem_ctx);
   return ret;
}
