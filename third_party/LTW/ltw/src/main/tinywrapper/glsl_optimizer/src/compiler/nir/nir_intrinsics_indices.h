/* Copyright (C) 2018 Red Hat
 * Copyright (C) 2020 Valve Corporation
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

#ifndef _NIR_INTRINSICS_INDICES_
#define _NIR_INTRINSICS_INDICES_



static inline int
nir_intrinsic_base(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_BASE] > 0);
   return (int)instr->const_index[info->index_map[NIR_INTRINSIC_BASE] - 1];
}

static inline void
nir_intrinsic_set_base(nir_intrinsic_instr *instr, int val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_BASE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_BASE] - 1] = val;
}

static inline bool
nir_intrinsic_has_base(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_BASE] > 0;
}


static inline unsigned
nir_intrinsic_write_mask(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_WRITE_MASK] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_WRITE_MASK] - 1];
}

static inline void
nir_intrinsic_set_write_mask(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_WRITE_MASK] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_WRITE_MASK] - 1] = val;
}

static inline bool
nir_intrinsic_has_write_mask(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_WRITE_MASK] > 0;
}


static inline unsigned
nir_intrinsic_stream_id(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_STREAM_ID] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_STREAM_ID] - 1];
}

static inline void
nir_intrinsic_set_stream_id(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_STREAM_ID] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_STREAM_ID] - 1] = val;
}

static inline bool
nir_intrinsic_has_stream_id(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_STREAM_ID] > 0;
}


static inline unsigned
nir_intrinsic_ucp_id(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_UCP_ID] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_UCP_ID] - 1];
}

static inline void
nir_intrinsic_set_ucp_id(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_UCP_ID] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_UCP_ID] - 1] = val;
}

static inline bool
nir_intrinsic_has_ucp_id(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_UCP_ID] > 0;
}


static inline unsigned
nir_intrinsic_range_base(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_RANGE_BASE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_RANGE_BASE] - 1];
}

static inline void
nir_intrinsic_set_range_base(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_RANGE_BASE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_RANGE_BASE] - 1] = val;
}

static inline bool
nir_intrinsic_has_range_base(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_RANGE_BASE] > 0;
}


static inline unsigned
nir_intrinsic_range(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_RANGE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_RANGE] - 1];
}

static inline void
nir_intrinsic_set_range(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_RANGE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_RANGE] - 1] = val;
}

static inline bool
nir_intrinsic_has_range(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_RANGE] > 0;
}


static inline unsigned
nir_intrinsic_desc_set(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DESC_SET] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_DESC_SET] - 1];
}

static inline void
nir_intrinsic_set_desc_set(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DESC_SET] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_DESC_SET] - 1] = val;
}

static inline bool
nir_intrinsic_has_desc_set(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_DESC_SET] > 0;
}


static inline unsigned
nir_intrinsic_binding(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_BINDING] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_BINDING] - 1];
}

static inline void
nir_intrinsic_set_binding(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_BINDING] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_BINDING] - 1] = val;
}

static inline bool
nir_intrinsic_has_binding(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_BINDING] > 0;
}


static inline unsigned
nir_intrinsic_component(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_COMPONENT] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_COMPONENT] - 1];
}

static inline void
nir_intrinsic_set_component(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_COMPONENT] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_COMPONENT] - 1] = val;
}

static inline bool
nir_intrinsic_has_component(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_COMPONENT] > 0;
}


static inline unsigned
nir_intrinsic_column(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_COLUMN] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_COLUMN] - 1];
}

static inline void
nir_intrinsic_set_column(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_COLUMN] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_COLUMN] - 1] = val;
}

static inline bool
nir_intrinsic_has_column(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_COLUMN] > 0;
}


static inline unsigned
nir_intrinsic_interp_mode(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_INTERP_MODE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_INTERP_MODE] - 1];
}

static inline void
nir_intrinsic_set_interp_mode(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_INTERP_MODE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_INTERP_MODE] - 1] = val;
}

static inline bool
nir_intrinsic_has_interp_mode(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_INTERP_MODE] > 0;
}


static inline unsigned
nir_intrinsic_reduction_op(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_REDUCTION_OP] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_REDUCTION_OP] - 1];
}

static inline void
nir_intrinsic_set_reduction_op(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_REDUCTION_OP] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_REDUCTION_OP] - 1] = val;
}

static inline bool
nir_intrinsic_has_reduction_op(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_REDUCTION_OP] > 0;
}


static inline unsigned
nir_intrinsic_cluster_size(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_CLUSTER_SIZE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_CLUSTER_SIZE] - 1];
}

static inline void
nir_intrinsic_set_cluster_size(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_CLUSTER_SIZE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_CLUSTER_SIZE] - 1] = val;
}

static inline bool
nir_intrinsic_has_cluster_size(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_CLUSTER_SIZE] > 0;
}


static inline unsigned
nir_intrinsic_param_idx(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_PARAM_IDX] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_PARAM_IDX] - 1];
}

static inline void
nir_intrinsic_set_param_idx(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_PARAM_IDX] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_PARAM_IDX] - 1] = val;
}

static inline bool
nir_intrinsic_has_param_idx(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_PARAM_IDX] > 0;
}


static inline enum glsl_sampler_dim
nir_intrinsic_image_dim(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IMAGE_DIM] > 0);
   return (enum glsl_sampler_dim)instr->const_index[info->index_map[NIR_INTRINSIC_IMAGE_DIM] - 1];
}

static inline void
nir_intrinsic_set_image_dim(nir_intrinsic_instr *instr, enum glsl_sampler_dim val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IMAGE_DIM] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_IMAGE_DIM] - 1] = val;
}

static inline bool
nir_intrinsic_has_image_dim(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_IMAGE_DIM] > 0;
}


static inline bool
nir_intrinsic_image_array(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IMAGE_ARRAY] > 0);
   return (bool)instr->const_index[info->index_map[NIR_INTRINSIC_IMAGE_ARRAY] - 1];
}

static inline void
nir_intrinsic_set_image_array(nir_intrinsic_instr *instr, bool val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IMAGE_ARRAY] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_IMAGE_ARRAY] - 1] = val;
}

static inline bool
nir_intrinsic_has_image_array(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_IMAGE_ARRAY] > 0;
}


static inline enum pipe_format
nir_intrinsic_format(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_FORMAT] > 0);
   return (enum pipe_format)instr->const_index[info->index_map[NIR_INTRINSIC_FORMAT] - 1];
}

static inline void
nir_intrinsic_set_format(nir_intrinsic_instr *instr, enum pipe_format val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_FORMAT] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_FORMAT] - 1] = val;
}

static inline bool
nir_intrinsic_has_format(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_FORMAT] > 0;
}


static inline enum gl_access_qualifier
nir_intrinsic_access(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ACCESS] > 0);
   return (enum gl_access_qualifier)instr->const_index[info->index_map[NIR_INTRINSIC_ACCESS] - 1];
}

static inline void
nir_intrinsic_set_access(nir_intrinsic_instr *instr, enum gl_access_qualifier val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ACCESS] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_ACCESS] - 1] = val;
}

static inline bool
nir_intrinsic_has_access(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_ACCESS] > 0;
}


static inline unsigned
nir_intrinsic_call_idx(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_CALL_IDX] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_CALL_IDX] - 1];
}

static inline void
nir_intrinsic_set_call_idx(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_CALL_IDX] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_CALL_IDX] - 1] = val;
}

static inline bool
nir_intrinsic_has_call_idx(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_CALL_IDX] > 0;
}


static inline unsigned
nir_intrinsic_stack_size(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_STACK_SIZE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_STACK_SIZE] - 1];
}

static inline void
nir_intrinsic_set_stack_size(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_STACK_SIZE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_STACK_SIZE] - 1] = val;
}

static inline bool
nir_intrinsic_has_stack_size(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_STACK_SIZE] > 0;
}


static inline unsigned
nir_intrinsic_align_mul(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ALIGN_MUL] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_ALIGN_MUL] - 1];
}

static inline void
nir_intrinsic_set_align_mul(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ALIGN_MUL] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_ALIGN_MUL] - 1] = val;
}

static inline bool
nir_intrinsic_has_align_mul(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_ALIGN_MUL] > 0;
}


static inline unsigned
nir_intrinsic_align_offset(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ALIGN_OFFSET] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_ALIGN_OFFSET] - 1];
}

static inline void
nir_intrinsic_set_align_offset(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ALIGN_OFFSET] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_ALIGN_OFFSET] - 1] = val;
}

static inline bool
nir_intrinsic_has_align_offset(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_ALIGN_OFFSET] > 0;
}


static inline unsigned
nir_intrinsic_desc_type(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DESC_TYPE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_DESC_TYPE] - 1];
}

static inline void
nir_intrinsic_set_desc_type(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DESC_TYPE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_DESC_TYPE] - 1] = val;
}

static inline bool
nir_intrinsic_has_desc_type(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_DESC_TYPE] > 0;
}


static inline nir_alu_type
nir_intrinsic_src_type(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SRC_TYPE] > 0);
   return (nir_alu_type)instr->const_index[info->index_map[NIR_INTRINSIC_SRC_TYPE] - 1];
}

static inline void
nir_intrinsic_set_src_type(nir_intrinsic_instr *instr, nir_alu_type val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SRC_TYPE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_SRC_TYPE] - 1] = val;
}

static inline bool
nir_intrinsic_has_src_type(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_SRC_TYPE] > 0;
}


static inline nir_alu_type
nir_intrinsic_dest_type(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DEST_TYPE] > 0);
   return (nir_alu_type)instr->const_index[info->index_map[NIR_INTRINSIC_DEST_TYPE] - 1];
}

static inline void
nir_intrinsic_set_dest_type(nir_intrinsic_instr *instr, nir_alu_type val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DEST_TYPE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_DEST_TYPE] - 1] = val;
}

static inline bool
nir_intrinsic_has_dest_type(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_DEST_TYPE] > 0;
}


static inline unsigned
nir_intrinsic_swizzle_mask(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SWIZZLE_MASK] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_SWIZZLE_MASK] - 1];
}

static inline void
nir_intrinsic_set_swizzle_mask(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SWIZZLE_MASK] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_SWIZZLE_MASK] - 1] = val;
}

static inline bool
nir_intrinsic_has_swizzle_mask(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_SWIZZLE_MASK] > 0;
}


static inline uint8_t
nir_intrinsic_offset0(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_OFFSET0] > 0);
   return (uint8_t)instr->const_index[info->index_map[NIR_INTRINSIC_OFFSET0] - 1];
}

static inline void
nir_intrinsic_set_offset0(nir_intrinsic_instr *instr, uint8_t val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_OFFSET0] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_OFFSET0] - 1] = val;
}

static inline bool
nir_intrinsic_has_offset0(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_OFFSET0] > 0;
}


static inline uint8_t
nir_intrinsic_offset1(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_OFFSET1] > 0);
   return (uint8_t)instr->const_index[info->index_map[NIR_INTRINSIC_OFFSET1] - 1];
}

static inline void
nir_intrinsic_set_offset1(nir_intrinsic_instr *instr, uint8_t val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_OFFSET1] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_OFFSET1] - 1] = val;
}

static inline bool
nir_intrinsic_has_offset1(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_OFFSET1] > 0;
}


static inline bool
nir_intrinsic_st64(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ST64] > 0);
   return (bool)instr->const_index[info->index_map[NIR_INTRINSIC_ST64] - 1];
}

static inline void
nir_intrinsic_set_st64(nir_intrinsic_instr *instr, bool val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ST64] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_ST64] - 1] = val;
}

static inline bool
nir_intrinsic_has_st64(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_ST64] > 0;
}


static inline unsigned
nir_intrinsic_arg_upper_bound_u32_amd(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] - 1];
}

static inline void
nir_intrinsic_set_arg_upper_bound_u32_amd(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] - 1] = val;
}

static inline bool
nir_intrinsic_has_arg_upper_bound_u32_amd(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] > 0;
}


static inline enum gl_access_qualifier
nir_intrinsic_dst_access(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DST_ACCESS] > 0);
   return (enum gl_access_qualifier)instr->const_index[info->index_map[NIR_INTRINSIC_DST_ACCESS] - 1];
}

static inline void
nir_intrinsic_set_dst_access(nir_intrinsic_instr *instr, enum gl_access_qualifier val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DST_ACCESS] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_DST_ACCESS] - 1] = val;
}

static inline bool
nir_intrinsic_has_dst_access(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_DST_ACCESS] > 0;
}


static inline enum gl_access_qualifier
nir_intrinsic_src_access(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SRC_ACCESS] > 0);
   return (enum gl_access_qualifier)instr->const_index[info->index_map[NIR_INTRINSIC_SRC_ACCESS] - 1];
}

static inline void
nir_intrinsic_set_src_access(nir_intrinsic_instr *instr, enum gl_access_qualifier val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SRC_ACCESS] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_SRC_ACCESS] - 1] = val;
}

static inline bool
nir_intrinsic_has_src_access(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_SRC_ACCESS] > 0;
}


static inline unsigned
nir_intrinsic_driver_location(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DRIVER_LOCATION] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_DRIVER_LOCATION] - 1];
}

static inline void
nir_intrinsic_set_driver_location(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_DRIVER_LOCATION] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_DRIVER_LOCATION] - 1] = val;
}

static inline bool
nir_intrinsic_has_driver_location(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_DRIVER_LOCATION] > 0;
}


static inline nir_memory_semantics
nir_intrinsic_memory_semantics(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_MEMORY_SEMANTICS] > 0);
   return (nir_memory_semantics)instr->const_index[info->index_map[NIR_INTRINSIC_MEMORY_SEMANTICS] - 1];
}

static inline void
nir_intrinsic_set_memory_semantics(nir_intrinsic_instr *instr, nir_memory_semantics val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_MEMORY_SEMANTICS] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_MEMORY_SEMANTICS] - 1] = val;
}

static inline bool
nir_intrinsic_has_memory_semantics(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_MEMORY_SEMANTICS] > 0;
}


static inline nir_variable_mode
nir_intrinsic_memory_modes(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_MEMORY_MODES] > 0);
   return (nir_variable_mode)instr->const_index[info->index_map[NIR_INTRINSIC_MEMORY_MODES] - 1];
}

static inline void
nir_intrinsic_set_memory_modes(nir_intrinsic_instr *instr, nir_variable_mode val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_MEMORY_MODES] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_MEMORY_MODES] - 1] = val;
}

static inline bool
nir_intrinsic_has_memory_modes(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_MEMORY_MODES] > 0;
}


static inline nir_scope
nir_intrinsic_memory_scope(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_MEMORY_SCOPE] > 0);
   return (nir_scope)instr->const_index[info->index_map[NIR_INTRINSIC_MEMORY_SCOPE] - 1];
}

static inline void
nir_intrinsic_set_memory_scope(nir_intrinsic_instr *instr, nir_scope val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_MEMORY_SCOPE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_MEMORY_SCOPE] - 1] = val;
}

static inline bool
nir_intrinsic_has_memory_scope(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_MEMORY_SCOPE] > 0;
}


static inline nir_scope
nir_intrinsic_execution_scope(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_EXECUTION_SCOPE] > 0);
   return (nir_scope)instr->const_index[info->index_map[NIR_INTRINSIC_EXECUTION_SCOPE] - 1];
}

static inline void
nir_intrinsic_set_execution_scope(nir_intrinsic_instr *instr, nir_scope val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_EXECUTION_SCOPE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_EXECUTION_SCOPE] - 1] = val;
}

static inline bool
nir_intrinsic_has_execution_scope(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_EXECUTION_SCOPE] > 0;
}


static inline struct nir_io_semantics
nir_intrinsic_io_semantics(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IO_SEMANTICS] > 0);
   struct nir_io_semantics res;
   STATIC_ASSERT(sizeof(instr->const_index[0]) == sizeof(res));
   memcpy(&res, &instr->const_index[info->index_map[NIR_INTRINSIC_IO_SEMANTICS] - 1], sizeof(res));
   return res;
}

static inline void
nir_intrinsic_set_io_semantics(nir_intrinsic_instr *instr, struct nir_io_semantics val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IO_SEMANTICS] > 0);
   val._pad = 0; /* clear padding bits */
   STATIC_ASSERT(sizeof(instr->const_index[0]) == sizeof(val));
   memcpy(&instr->const_index[info->index_map[NIR_INTRINSIC_IO_SEMANTICS] - 1], &val, sizeof(val));
}

static inline bool
nir_intrinsic_has_io_semantics(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_IO_SEMANTICS] > 0;
}


static inline struct nir_io_xfb
nir_intrinsic_io_xfb(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IO_XFB] > 0);
   struct nir_io_xfb res;
   STATIC_ASSERT(sizeof(instr->const_index[0]) == sizeof(res));
   memcpy(&res, &instr->const_index[info->index_map[NIR_INTRINSIC_IO_XFB] - 1], sizeof(res));
   return res;
}

static inline void
nir_intrinsic_set_io_xfb(nir_intrinsic_instr *instr, struct nir_io_xfb val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IO_XFB] > 0);
   STATIC_ASSERT(sizeof(instr->const_index[0]) == sizeof(val));
   memcpy(&instr->const_index[info->index_map[NIR_INTRINSIC_IO_XFB] - 1], &val, sizeof(val));
}

static inline bool
nir_intrinsic_has_io_xfb(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_IO_XFB] > 0;
}


static inline struct nir_io_xfb
nir_intrinsic_io_xfb2(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IO_XFB2] > 0);
   struct nir_io_xfb res;
   STATIC_ASSERT(sizeof(instr->const_index[0]) == sizeof(res));
   memcpy(&res, &instr->const_index[info->index_map[NIR_INTRINSIC_IO_XFB2] - 1], sizeof(res));
   return res;
}

static inline void
nir_intrinsic_set_io_xfb2(nir_intrinsic_instr *instr, struct nir_io_xfb val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_IO_XFB2] > 0);
   STATIC_ASSERT(sizeof(instr->const_index[0]) == sizeof(val));
   memcpy(&instr->const_index[info->index_map[NIR_INTRINSIC_IO_XFB2] - 1], &val, sizeof(val));
}

static inline bool
nir_intrinsic_has_io_xfb2(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_IO_XFB2] > 0;
}


static inline nir_ray_query_value
nir_intrinsic_ray_query_value(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_RAY_QUERY_VALUE] > 0);
   return (nir_ray_query_value)instr->const_index[info->index_map[NIR_INTRINSIC_RAY_QUERY_VALUE] - 1];
}

static inline void
nir_intrinsic_set_ray_query_value(nir_intrinsic_instr *instr, nir_ray_query_value val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_RAY_QUERY_VALUE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_RAY_QUERY_VALUE] - 1] = val;
}

static inline bool
nir_intrinsic_has_ray_query_value(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_RAY_QUERY_VALUE] > 0;
}


static inline nir_rounding_mode
nir_intrinsic_rounding_mode(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ROUNDING_MODE] > 0);
   return (nir_rounding_mode)instr->const_index[info->index_map[NIR_INTRINSIC_ROUNDING_MODE] - 1];
}

static inline void
nir_intrinsic_set_rounding_mode(nir_intrinsic_instr *instr, nir_rounding_mode val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_ROUNDING_MODE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_ROUNDING_MODE] - 1] = val;
}

static inline bool
nir_intrinsic_has_rounding_mode(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_ROUNDING_MODE] > 0;
}


static inline unsigned
nir_intrinsic_saturate(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SATURATE] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_SATURATE] - 1];
}

static inline void
nir_intrinsic_set_saturate(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SATURATE] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_SATURATE] - 1] = val;
}

static inline bool
nir_intrinsic_has_saturate(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_SATURATE] > 0;
}


static inline bool
nir_intrinsic_synchronous(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SYNCHRONOUS] > 0);
   return (bool)instr->const_index[info->index_map[NIR_INTRINSIC_SYNCHRONOUS] - 1];
}

static inline void
nir_intrinsic_set_synchronous(nir_intrinsic_instr *instr, bool val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SYNCHRONOUS] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_SYNCHRONOUS] - 1] = val;
}

static inline bool
nir_intrinsic_has_synchronous(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_SYNCHRONOUS] > 0;
}


static inline unsigned
nir_intrinsic_value_id(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_VALUE_ID] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_VALUE_ID] - 1];
}

static inline void
nir_intrinsic_set_value_id(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_VALUE_ID] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_VALUE_ID] - 1] = val;
}

static inline bool
nir_intrinsic_has_value_id(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_VALUE_ID] > 0;
}


static inline bool
nir_intrinsic_sign_extend(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SIGN_EXTEND] > 0);
   return (bool)instr->const_index[info->index_map[NIR_INTRINSIC_SIGN_EXTEND] - 1];
}

static inline void
nir_intrinsic_set_sign_extend(nir_intrinsic_instr *instr, bool val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_SIGN_EXTEND] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_SIGN_EXTEND] - 1] = val;
}

static inline bool
nir_intrinsic_has_sign_extend(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_SIGN_EXTEND] > 0;
}


static inline unsigned
nir_intrinsic_flags(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_FLAGS] > 0);
   return (unsigned)instr->const_index[info->index_map[NIR_INTRINSIC_FLAGS] - 1];
}

static inline void
nir_intrinsic_set_flags(nir_intrinsic_instr *instr, unsigned val)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   assert(info->index_map[NIR_INTRINSIC_FLAGS] > 0);
   instr->const_index[info->index_map[NIR_INTRINSIC_FLAGS] - 1] = val;
}

static inline bool
nir_intrinsic_has_flags(const nir_intrinsic_instr *instr)
{
   const nir_intrinsic_info *info = &nir_intrinsic_infos[instr->intrinsic];
   return info->index_map[NIR_INTRINSIC_FLAGS] > 0;
}

#endif /* _NIR_INTRINSICS_INDICES_ */