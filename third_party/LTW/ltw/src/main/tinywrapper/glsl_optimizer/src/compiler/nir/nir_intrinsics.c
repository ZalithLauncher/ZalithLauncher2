/* Copyright (C) 2018 Red Hat
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

#include "nir.h"

const nir_intrinsic_info nir_intrinsic_infos[nir_num_intrinsics] = {
{
   .name = "accept_ray_intersection",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "addr_mode_is",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_MEMORY_MODES,
   },
   .index_map = {
      [NIR_INTRINSIC_MEMORY_MODES] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "alloc_vertices_and_primitives_amd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_add_gen_prim_count_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "atomic_add_gs_emit_prim_count_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_add_gs_invocation_count_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_add_xfb_prim_count_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_add",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_add_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_and",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_and_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_comp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_comp_swap_deref",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_exchange",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_exchange_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_inc",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_inc_deref",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_max",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_max_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_min",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_min_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_or",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_or_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_post_dec",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_post_dec_deref",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_pre_dec",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_pre_dec_deref",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "atomic_counter_read",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "atomic_counter_read_deref",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "atomic_counter_xor",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "atomic_counter_xor_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "ballot",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ballot_bit_count_exclusive",
   .num_srcs = 1,
   .src_components = {
      4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ballot_bit_count_inclusive",
   .num_srcs = 1,
   .src_components = {
      4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ballot_bit_count_reduce",
   .num_srcs = 1,
   .src_components = {
      4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ballot_bitfield_extract",
   .num_srcs = 2,
   .src_components = {
      4, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ballot_find_lsb",
   .num_srcs = 1,
   .src_components = {
      4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ballot_find_msb",
   .num_srcs = 1,
   .src_components = {
      4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "begin_invocation_interlock",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "bindless_image_atomic_add",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_and",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_comp_swap",
   .num_srcs = 5,
   .src_components = {
      -1, 4, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_dec_wrap",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_exchange",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_fadd",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_fmax",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_fmin",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_imax",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_imin",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_inc_wrap",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_or",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_umax",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_umin",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_atomic_xor",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_image_descriptor_amd",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "bindless_image_format",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "bindless_image_fragment_mask_load_amd",
   .num_srcs = 2,
   .src_components = {
      -1, 4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "bindless_image_load",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_DEST_TYPE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "bindless_image_load_raw_intel",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "bindless_image_order",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "bindless_image_samples",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "bindless_image_samples_identical",
   .num_srcs = 2,
   .src_components = {
      -1, 4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "bindless_image_size",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "bindless_image_sparse_load",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_DEST_TYPE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "bindless_image_store",
   .num_srcs = 5,
   .src_components = {
      -1, 4, 1, 0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_SRC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_SRC_TYPE] = 5,
    },
   .flags = 0,
},
{
   .name = "bindless_image_store_raw_intel",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "bindless_resource_ir3",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_DESC_SET,
   },
   .index_map = {
      [NIR_INTRINSIC_DESC_SET] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "block_image_store_agx",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x30,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_IMAGE_DIM,
   },
   .index_map = {
      [NIR_INTRINSIC_FORMAT] = 1,
      [NIR_INTRINSIC_IMAGE_DIM] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "btd_retire_intel",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "btd_spawn_intel",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "btd_stack_push_intel",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STACK_SIZE,
   },
   .index_map = {
      [NIR_INTRINSIC_STACK_SIZE] = 1,
    },
   .flags = 0,
},
{
   .name = "buffer_atomic_add_amd",
   .num_srcs = 2,
   .src_components = {
      4, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "bvh64_intersect_ray_amd",
   .num_srcs = 6,
   .src_components = {
      4, 2, 1, 3, 3, 3
   },
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "cond_end_ir3",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "control_barrier",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "convert_alu_types",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_SRC_TYPE,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_ROUNDING_MODE,
      NIR_INTRINSIC_SATURATE,
   },
   .index_map = {
      [NIR_INTRINSIC_SRC_TYPE] = 1,
      [NIR_INTRINSIC_DEST_TYPE] = 2,
      [NIR_INTRINSIC_ROUNDING_MODE] = 3,
      [NIR_INTRINSIC_SATURATE] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "copy_deref",
   .num_srcs = 2,
   .src_components = {
      -1, -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_DST_ACCESS,
      NIR_INTRINSIC_SRC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_DST_ACCESS] = 1,
      [NIR_INTRINSIC_SRC_ACCESS] = 2,
    },
   .flags = 0,
},
{
   .name = "copy_ubo_to_uniform_ir3",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
    },
   .flags = 0,
},
{
   .name = "demote",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "demote_if",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "deref_atomic_add",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_and",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_comp_swap",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_exchange",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_fadd",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_fcomp_swap",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_fmax",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_fmin",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_imax",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_imin",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_or",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_umax",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_umin",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_atomic_xor",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "deref_buffer_array_length",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "deref_mode_is",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_MEMORY_MODES,
   },
   .index_map = {
      [NIR_INTRINSIC_MEMORY_MODES] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "discard",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "discard_if",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "elect",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "emit_vertex",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "emit_vertex_with_counter",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "end_invocation_interlock",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "end_patch_ir3",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "end_primitive",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "end_primitive_with_counter",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "exclusive_scan",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_REDUCTION_OP,
   },
   .index_map = {
      [NIR_INTRINSIC_REDUCTION_OP] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "execute_callable",
   .num_srcs = 2,
   .src_components = {
      1, -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "execute_closest_hit_amd",
   .num_srcs = 6,
   .src_components = {
      1, 1, 1, 1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "execute_miss_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "export_amd",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_FLAGS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_FLAGS] = 3,
    },
   .flags = 0,
},
{
   .name = "first_invocation",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "gds_atomic_add_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "get_ssbo_size",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "get_ubo_size",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "global_atomic_add",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_add_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_add_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_add_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_and",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_and_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_and_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_and_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_comp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_comp_swap_2x32",
   .num_srcs = 3,
   .src_components = {
      2, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_comp_swap_amd",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_comp_swap_ir3",
   .num_srcs = 3,
   .src_components = {
      2, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_exchange",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_exchange_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_exchange_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_exchange_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_fadd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fadd_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fadd_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_fcomp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fcomp_swap_2x32",
   .num_srcs = 3,
   .src_components = {
      2, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fcomp_swap_amd",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_fmax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fmax_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fmax_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_fmin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fmin_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_fmin_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_imax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_imax_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_imax_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_imax_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_imin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_imin_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_imin_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_imin_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_or",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_or_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_or_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_or_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_umax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_umax_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_umax_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_umax_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_umin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_umin_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_umin_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_umin_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_xor",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_xor_2x32",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "global_atomic_xor_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "global_atomic_xor_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "group_memory_barrier",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "ignore_ray_intersection",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "image_atomic_add",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_and",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_comp_swap",
   .num_srcs = 5,
   .src_components = {
      1, 4, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_dec_wrap",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_exchange",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_fadd",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_fmax",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_fmin",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_imax",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_imin",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_inc_wrap",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_or",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_umax",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_umin",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_atomic_xor",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_add",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_and",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_comp_swap",
   .num_srcs = 5,
   .src_components = {
      -1, 4, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_dec_wrap",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_exchange",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_fadd",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_fmax",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_fmin",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_imax",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_imin",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_inc_wrap",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_or",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_umax",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_umin",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_atomic_xor",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_deref_descriptor_amd",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_format",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_fragment_mask_load_amd",
   .num_srcs = 2,
   .src_components = {
      -1, 4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_load",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_DEST_TYPE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_deref_load_param_intel",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_load_raw_intel",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_deref_order",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_samples",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_samples_identical",
   .num_srcs = 2,
   .src_components = {
      -1, 4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_deref_size",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_deref_sparse_load",
   .num_srcs = 4,
   .src_components = {
      -1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_DEST_TYPE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_deref_store",
   .num_srcs = 5,
   .src_components = {
      -1, 4, 1, 0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_SRC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_SRC_TYPE] = 5,
    },
   .flags = 0,
},
{
   .name = "image_deref_store_raw_intel",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "image_descriptor_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_format",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_fragment_mask_load_amd",
   .num_srcs = 2,
   .src_components = {
      1, 4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_load",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 6,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
      [NIR_INTRINSIC_DEST_TYPE] = 6,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_load_raw_intel",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_order",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_samples",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_samples_identical",
   .num_srcs = 2,
   .src_components = {
      1, 4
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_size",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "image_sparse_load",
   .num_srcs = 4,
   .src_components = {
      1, 4, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 6,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
      [NIR_INTRINSIC_DEST_TYPE] = 6,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "image_store",
   .num_srcs = 5,
   .src_components = {
      1, 4, 1, 0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 6,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
      NIR_INTRINSIC_SRC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
      [NIR_INTRINSIC_SRC_TYPE] = 6,
    },
   .flags = 0,
},
{
   .name = "image_store_raw_intel",
   .num_srcs = 3,
   .src_components = {
      1, 1, 0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_IMAGE_DIM,
      NIR_INTRINSIC_IMAGE_ARRAY,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_RANGE_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IMAGE_DIM] = 1,
      [NIR_INTRINSIC_IMAGE_ARRAY] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
      [NIR_INTRINSIC_RANGE_BASE] = 5,
    },
   .flags = 0,
},
{
   .name = "inclusive_scan",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_REDUCTION_OP,
   },
   .index_map = {
      [NIR_INTRINSIC_REDUCTION_OP] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "interp_deref_at_centroid",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "interp_deref_at_offset",
   .num_srcs = 2,
   .src_components = {
      1, 2
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "interp_deref_at_sample",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "interp_deref_at_vertex",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "is_helper_invocation",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "is_sparse_texels_resident",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x21,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "is_subgroup_invocation_lt_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "lane_permute_16_amd",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "last_invocation",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "launch_mesh_workgroups",
   .num_srcs = 1,
   .src_components = {
      3
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
    },
   .flags = 0,
},
{
   .name = "launch_mesh_workgroups_with_payload_deref",
   .num_srcs = 2,
   .src_components = {
      3, -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "load_aa_line_width",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_accel_struct_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_agx",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_SIGN_EXTEND,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_BASE] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_SIGN_EXTEND] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_alpha_reference_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_back_face_agx",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x21,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_barycentric_at_offset",
   .num_srcs = 1,
   .src_components = {
      2
   },
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_INTERP_MODE,
   },
   .index_map = {
      [NIR_INTRINSIC_INTERP_MODE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_barycentric_at_sample",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_INTERP_MODE,
   },
   .index_map = {
      [NIR_INTRINSIC_INTERP_MODE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_barycentric_centroid",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_INTERP_MODE,
   },
   .index_map = {
      [NIR_INTRINSIC_INTERP_MODE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_barycentric_model",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_INTERP_MODE,
   },
   .index_map = {
      [NIR_INTRINSIC_INTERP_MODE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_barycentric_pixel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_INTERP_MODE,
   },
   .index_map = {
      [NIR_INTRINSIC_INTERP_MODE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_barycentric_sample",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_INTERP_MODE,
   },
   .index_map = {
      [NIR_INTRINSIC_INTERP_MODE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_base_global_invocation_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_base_instance",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_base_vertex",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_base_workgroup_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_a_float",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_aaaa8888_unorm",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_b_float",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_g_float",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_r_float",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_rgba",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_blend_const_color_rgba8888_unorm",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_btd_global_arg_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_btd_local_arg_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_btd_resume_sbt_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_btd_shader_type_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_btd_stack_id_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_buffer_amd",
   .num_srcs = 4,
   .src_components = {
      4, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_MEMORY_MODES,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_MEMORY_MODES] = 2,
      [NIR_INTRINSIC_ACCESS] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_callable_sbt_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_callable_sbt_stride_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x10,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_clamp_vertex_color_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_clip_half_line_width_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_color0",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_color1",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_constant",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_constant_agx",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_SIGN_EXTEND,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_BASE] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_SIGN_EXTEND] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_constant_base_ptr",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_converted_output_pan",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_DEST_TYPE] = 1,
      [NIR_INTRINSIC_IO_SEMANTICS] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_coverage_mask_pan",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_cull_any_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_cull_back_face_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_cull_ccw_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_cull_front_face_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_cull_mask",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_cull_mask_and_flags_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_cull_small_prim_precision_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_cull_small_primitives_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_deref",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_deref_block_intel",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_desc_set_address_intel",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_draw_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_esgs_vertex_stride_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_fb_layers_v3d",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_first_vertex",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_force_vrs_rates_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_frag_coord",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_frag_invocation_count",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_frag_shading_rate",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_frag_size",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_front_face",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x21,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_fs_input_interp_deltas",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_IO_SEMANTICS] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_fully_covered",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_global_2x32",
   .num_srcs = 1,
   .src_components = {
      2
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_global_amd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_global_base_ptr",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_block_intel",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_global_const_block_intel",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_constant",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_constant_bounded",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_constant_offset",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_invocation_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_invocation_id_zero_base",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_invocation_index",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_global_ir3",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_gs_header_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_gs_vertex_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_helper_invocation",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x21,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_hit_attrib_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "load_hs_out_patch_data_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_hs_patch_stride_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_initial_edgeflags_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "load_input",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_input_vertex",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_instance_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_interpolated_input",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_intersection_opaque_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_invocation_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_is_indexed_draw",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_kernel_input",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_layer_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_lds_ngg_gs_out_vertex_base_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_lds_ngg_scratch_base_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_leaf_opaque_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_leaf_procedural_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_line_coord",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_line_width",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_local_invocation_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_local_invocation_index",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_local_pixel_agx",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_FORMAT,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_FORMAT] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_REORDER | NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_local_shared_r600",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_lshs_vertex_stride_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_merged_wave_info_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_mesh_inline_data_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ALIGN_OFFSET] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_mesh_view_count",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_mesh_view_indices",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_multisampled_pan",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_num_subgroups",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_num_vertices",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_num_vertices_per_primitive_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_num_workgroups",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ordered_id_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_output",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_packed_passthrough_primitive_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_param",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_PARAM_IDX,
   },
   .index_map = {
      [NIR_INTRINSIC_PARAM_IDX] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_patch_vertices_in",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_per_primitive_output",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_per_vertex_input",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_per_vertex_output",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_DEST_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
      [NIR_INTRINSIC_IO_SEMANTICS] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_persp_center_rhw_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_pipeline_stat_query_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_point_coord",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_point_coord_maybe_flipped",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "load_preamble",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_prim_gen_query_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_prim_xfb_query_enabled_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x1,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_primitive_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_primitive_location_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_DRIVER_LOCATION,
   },
   .index_map = {
      [NIR_INTRINSIC_DRIVER_LOCATION] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_printf_buffer_address",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_provoking_vtx_in_prim_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ptr_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "load_push_constant",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_rasterization_samples_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_raw_output_pan",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_IO_SEMANTICS] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_base_mem_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_flags",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_geometry_index",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_hit_kind",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_hit_sbt_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_hit_sbt_stride_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x10,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_hw_stack_size_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_instance_custom_index",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_launch_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_launch_size",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_launch_size_addr_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_miss_sbt_addr_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_miss_sbt_stride_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x10,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_num_dss_rt_stacks_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_object_direction",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_object_origin",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_object_to_world",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_COLUMN,
   },
   .index_map = {
      [NIR_INTRINSIC_COLUMN] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_query_global_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_sw_stack_size_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_t_max",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_t_min",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_world_direction",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_world_origin",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ray_world_to_object",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_COLUMN,
   },
   .index_map = {
      [NIR_INTRINSIC_COLUMN] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_rel_patch_id_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_reloc_const_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_PARAM_IDX,
   },
   .index_map = {
      [NIR_INTRINSIC_PARAM_IDX] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_attr_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_attr_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_es2gs_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_esgs_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_gs2vs_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_gsvs_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_mesh_scratch_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_mesh_scratch_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_task_draw_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_task_payload_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_tess_factors_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_tess_factors_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_tess_offchip_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ring_tess_offchip_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_rt_arg_scratch_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_rt_conversion_pan",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_SRC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_SRC_TYPE] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_rt_dynamic_callable_stack_base_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_id_no_per_sample",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_mask_in",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_pos",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_pos_from_id",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_pos_or_center",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_positions_amd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sample_positions_pan",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sampler_lod_parameters_pan",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sbt_base_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sbt_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_sbt_stride_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_scalar_arg_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_scratch",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ALIGN_MUL] = 1,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_scratch_base_ptr",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_scratch_dxil",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_shader_record_ptr",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_shared",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_shared2_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_OFFSET0,
      NIR_INTRINSIC_OFFSET1,
      NIR_INTRINSIC_ST64,
   },
   .index_map = {
      [NIR_INTRINSIC_OFFSET0] = 1,
      [NIR_INTRINSIC_OFFSET1] = 2,
      [NIR_INTRINSIC_ST64] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_shared_base_ptr",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_shared_block_intel",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_shared_dxil",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_shared_ir3",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_shared_uniform_block_intel",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_simd_width_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_smem_amd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ALIGN_MUL] = 1,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_smem_buffer_amd",
   .num_srcs = 2,
   .src_components = {
      4, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ALIGN_MUL] = 1,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ssbo",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_ssbo_address",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ssbo_block_intel",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_ssbo_ir3",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_ssbo_uniform_block_intel",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_stack",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
      NIR_INTRINSIC_CALL_IDX,
      NIR_INTRINSIC_VALUE_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
      [NIR_INTRINSIC_CALL_IDX] = 4,
      [NIR_INTRINSIC_VALUE_ID] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_streamout_buffer_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_streamout_config_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_streamout_offset_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_streamout_write_index_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_eq_mask",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_ge_mask",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_gt_mask",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_id_shift_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_invocation",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_le_mask",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_lt_mask",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_subgroup_size",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_task_payload",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_task_ring_entry_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tcs_header_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tcs_in_param_base_r600",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tcs_num_patches_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tcs_out_param_base_r600",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tcs_rel_patch_id_r600",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tcs_tess_factor_base_r600",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_coord",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_coord_r600",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_factor_base_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_level_inner",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_level_inner_default",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_level_outer",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_level_outer_default",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_param_base_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tess_rel_patch_id_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_texture_base_agx",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_texture_rect_scaling",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_tlb_color_v3d",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
    },
   .flags = 0,
},
{
   .name = "load_topology_id_intel",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_typed_buffer_amd",
   .num_srcs = 4,
   .src_components = {
      4, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 6,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_MEMORY_MODES,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_MEMORY_MODES] = 2,
      [NIR_INTRINSIC_ACCESS] = 3,
      [NIR_INTRINSIC_FORMAT] = 4,
      [NIR_INTRINSIC_ALIGN_MUL] = 5,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 6,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "load_ubo",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
      NIR_INTRINSIC_RANGE_BASE,
      NIR_INTRINSIC_RANGE,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
      [NIR_INTRINSIC_RANGE_BASE] = 4,
      [NIR_INTRINSIC_RANGE] = 5,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ubo_base_agx",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ubo_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_ubo_vec4",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_BASE] = 2,
      [NIR_INTRINSIC_COMPONENT] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_uniform",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_RANGE,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_RANGE] = 2,
      [NIR_INTRINSIC_DEST_TYPE] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_user_clip_plane",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_UCP_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_UCP_ID] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_user_data_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vbo_base_agx",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x40,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vector_arg_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ARG_UPPER_BOUND_U32_AMD] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vertex_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vertex_id_zero_base",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_view_index",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_offset",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_scale",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_x_offset",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_x_scale",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_xy_scale_and_offset",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 4,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_y_offset",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_y_scale",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_z_offset",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_viewport_z_scale",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vs_primitive_stride_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vs_vertex_stride_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_vulkan_descriptor",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_DESC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_DESC_TYPE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_work_dim",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_workgroup_id",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_workgroup_id_zero_base",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_workgroup_index",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_workgroup_num_input_primitives_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_workgroup_num_input_vertices_amd",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_workgroup_size",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 3,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "load_xfb_address",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x60,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "masked_swizzle_amd",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_SWIZZLE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_SWIZZLE_MASK] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "mbcnt_amd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "memcpy_deref",
   .num_srcs = 3,
   .src_components = {
      -1, -1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_DST_ACCESS,
      NIR_INTRINSIC_SRC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_DST_ACCESS] = 1,
      [NIR_INTRINSIC_SRC_ACCESS] = 2,
    },
   .flags = 0,
},
{
   .name = "memory_barrier",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "memory_barrier_atomic_counter",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "memory_barrier_buffer",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "memory_barrier_image",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "memory_barrier_shared",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "memory_barrier_tcs_patch",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "nop",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "optimization_barrier_vgpr_amd",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "ordered_xfb_counter_add_amd",
   .num_srcs = 2,
   .src_components = {
      1, 0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
    },
   .flags = 0,
},
{
   .name = "overwrite_tes_arguments_amd",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "overwrite_vs_arguments_amd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "preamble_end_ir3",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "preamble_start_ir3",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "printf",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "quad_broadcast",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "quad_swap_diagonal",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "quad_swap_horizontal",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "quad_swap_vertical",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "quad_swizzle_amd",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_SWIZZLE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_SWIZZLE_MASK] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "read_first_invocation",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "read_invocation",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "read_invocation_cond_ir3",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "reduce",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_REDUCTION_OP,
      NIR_INTRINSIC_CLUSTER_SIZE,
   },
   .index_map = {
      [NIR_INTRINSIC_REDUCTION_OP] = 1,
      [NIR_INTRINSIC_CLUSTER_SIZE] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "report_ray_intersection",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rotate",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_EXECUTION_SCOPE,
      NIR_INTRINSIC_CLUSTER_SIZE,
   },
   .index_map = {
      [NIR_INTRINSIC_EXECUTION_SCOPE] = 1,
      [NIR_INTRINSIC_CLUSTER_SIZE] = 2,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "rq_confirm_intersection",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rq_generate_intersection",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rq_initialize",
   .num_srcs = 8,
   .src_components = {
      -1, -1, 1, 1, 3, 1, 3, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rq_load",
   .num_srcs = 2,
   .src_components = {
      -1, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_RAY_QUERY_VALUE,
      NIR_INTRINSIC_COLUMN,
   },
   .index_map = {
      [NIR_INTRINSIC_RAY_QUERY_VALUE] = 1,
      [NIR_INTRINSIC_COLUMN] = 2,
    },
   .flags = 0,
},
{
   .name = "rq_proceed",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rq_terminate",
   .num_srcs = 1,
   .src_components = {
      -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rt_execute_callable",
   .num_srcs = 2,
   .src_components = {
      1, -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_CALL_IDX,
      NIR_INTRINSIC_STACK_SIZE,
   },
   .index_map = {
      [NIR_INTRINSIC_CALL_IDX] = 1,
      [NIR_INTRINSIC_STACK_SIZE] = 2,
    },
   .flags = 0,
},
{
   .name = "rt_resume",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_CALL_IDX,
      NIR_INTRINSIC_STACK_SIZE,
   },
   .index_map = {
      [NIR_INTRINSIC_CALL_IDX] = 1,
      [NIR_INTRINSIC_STACK_SIZE] = 2,
    },
   .flags = 0,
},
{
   .name = "rt_return_amd",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "rt_trace_ray",
   .num_srcs = 11,
   .src_components = {
      -1, 1, 1, 1, 1, 1, 3, 1, 3, 1, -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_CALL_IDX,
      NIR_INTRINSIC_STACK_SIZE,
   },
   .index_map = {
      [NIR_INTRINSIC_CALL_IDX] = 1,
      [NIR_INTRINSIC_STACK_SIZE] = 2,
    },
   .flags = 0,
},
{
   .name = "scoped_barrier",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_EXECUTION_SCOPE,
      NIR_INTRINSIC_MEMORY_SCOPE,
      NIR_INTRINSIC_MEMORY_SEMANTICS,
      NIR_INTRINSIC_MEMORY_MODES,
   },
   .index_map = {
      [NIR_INTRINSIC_EXECUTION_SCOPE] = 1,
      [NIR_INTRINSIC_MEMORY_SCOPE] = 2,
      [NIR_INTRINSIC_MEMORY_SEMANTICS] = 3,
      [NIR_INTRINSIC_MEMORY_MODES] = 4,
    },
   .flags = 0,
},
{
   .name = "set_vertex_and_primitive_count",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_STREAM_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_STREAM_ID] = 1,
    },
   .flags = 0,
},
{
   .name = "shader_clock",
   .num_srcs = 0,
   .has_dest = true,
   .dest_components = 2,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_MEMORY_SCOPE,
   },
   .index_map = {
      [NIR_INTRINSIC_MEMORY_SCOPE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "shared_atomic_add",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_add_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_and",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_and_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_comp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_comp_swap_dxil",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_exchange",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_exchange_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_fadd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_fcomp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_fmax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_fmin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_imax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_imax_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_imin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_imin_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_or",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_or_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_umax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_umax_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_umin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_umin_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shared_atomic_xor",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "shared_atomic_xor_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "shuffle",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "shuffle_down",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "shuffle_up",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "shuffle_xor",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "sparse_residency_code_and",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "ssbo_atomic_add",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_add_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_and",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_and_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_comp_swap",
   .num_srcs = 4,
   .src_components = {
      -1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_comp_swap_ir3",
   .num_srcs = 5,
   .src_components = {
      1, 1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_exchange",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_exchange_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_fadd",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_fcomp_swap",
   .num_srcs = 4,
   .src_components = {
      -1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_fmax",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_fmin",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_imax",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_imax_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_imin",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_imin_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_or",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_or_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_umax",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_umax_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_umin",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_umin_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_xor",
   .num_srcs = 3,
   .src_components = {
      -1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "ssbo_atomic_xor_ir3",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
    },
   .flags = 0,
},
{
   .name = "store_agx",
   .num_srcs = 3,
   .src_components = {
      0, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_FORMAT,
      NIR_INTRINSIC_SIGN_EXTEND,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_BASE] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
      [NIR_INTRINSIC_SIGN_EXTEND] = 4,
    },
   .flags = 0,
},
{
   .name = "store_buffer_amd",
   .num_srcs = 5,
   .src_components = {
      0, 4, 1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_MEMORY_MODES,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_MEMORY_MODES] = 3,
      [NIR_INTRINSIC_ACCESS] = 4,
    },
   .flags = 0,
},
{
   .name = "store_combined_output_pan",
   .num_srcs = 5,
   .src_components = {
      0, 1, 1, 1, 4
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_IO_SEMANTICS,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_SRC_TYPE,
      NIR_INTRINSIC_DEST_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_IO_SEMANTICS] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_SRC_TYPE] = 3,
      [NIR_INTRINSIC_DEST_TYPE] = 4,
    },
   .flags = 0,
},
{
   .name = "store_deref",
   .num_srcs = 2,
   .src_components = {
      -1, 0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
    },
   .flags = 0,
},
{
   .name = "store_deref_block_intel",
   .num_srcs = 2,
   .src_components = {
      -1, 0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
    },
   .flags = 0,
},
{
   .name = "store_global",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_global_2x32",
   .num_srcs = 2,
   .src_components = {
      0, 2
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_global_amd",
   .num_srcs = 3,
   .src_components = {
      0, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
      NIR_INTRINSIC_WRITE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
      [NIR_INTRINSIC_WRITE_MASK] = 5,
    },
   .flags = 0,
},
{
   .name = "store_global_block_intel",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_global_ir3",
   .num_srcs = 3,
   .src_components = {
      0, 2, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_ACCESS] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = 0,
},
{
   .name = "store_hit_attrib_amd",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "store_local_pixel_agx",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_FORMAT,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_FORMAT] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "store_local_shared_r600",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
    },
   .flags = 0,
},
{
   .name = "store_output",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 7,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_SRC_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
      NIR_INTRINSIC_IO_XFB,
      NIR_INTRINSIC_IO_XFB2,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_COMPONENT] = 3,
      [NIR_INTRINSIC_SRC_TYPE] = 4,
      [NIR_INTRINSIC_IO_SEMANTICS] = 5,
      [NIR_INTRINSIC_IO_XFB] = 6,
      [NIR_INTRINSIC_IO_XFB2] = 7,
    },
   .flags = 0,
},
{
   .name = "store_per_primitive_output",
   .num_srcs = 3,
   .src_components = {
      0, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_SRC_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_COMPONENT] = 3,
      [NIR_INTRINSIC_SRC_TYPE] = 4,
      [NIR_INTRINSIC_IO_SEMANTICS] = 5,
    },
   .flags = 0,
},
{
   .name = "store_per_vertex_output",
   .num_srcs = 3,
   .src_components = {
      0, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 5,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_SRC_TYPE,
      NIR_INTRINSIC_IO_SEMANTICS,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_COMPONENT] = 3,
      [NIR_INTRINSIC_SRC_TYPE] = 4,
      [NIR_INTRINSIC_IO_SEMANTICS] = 5,
    },
   .flags = 0,
},
{
   .name = "store_preamble",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "store_raw_output_pan",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 2,
   .indices = {
      NIR_INTRINSIC_IO_SEMANTICS,
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_IO_SEMANTICS] = 1,
      [NIR_INTRINSIC_BASE] = 2,
    },
   .flags = 0,
},
{
   .name = "store_scratch",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
      NIR_INTRINSIC_WRITE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_ALIGN_MUL] = 1,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 2,
      [NIR_INTRINSIC_WRITE_MASK] = 3,
    },
   .flags = 0,
},
{
   .name = "store_scratch_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "store_shared",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_shared2_amd",
   .num_srcs = 2,
   .src_components = {
      2, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_OFFSET0,
      NIR_INTRINSIC_OFFSET1,
      NIR_INTRINSIC_ST64,
   },
   .index_map = {
      [NIR_INTRINSIC_OFFSET0] = 1,
      [NIR_INTRINSIC_OFFSET1] = 2,
      [NIR_INTRINSIC_ST64] = 3,
    },
   .flags = 0,
},
{
   .name = "store_shared_block_intel",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_shared_dxil",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "store_shared_ir3",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
    },
   .flags = 0,
},
{
   .name = "store_shared_masked_dxil",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "store_ssbo",
   .num_srcs = 3,
   .src_components = {
      0, -1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_ssbo_block_intel",
   .num_srcs = 3,
   .src_components = {
      0, -1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_ssbo_ir3",
   .num_srcs = 4,
   .src_components = {
      0, 1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ACCESS,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
      [NIR_INTRINSIC_ACCESS] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_ssbo_masked_dxil",
   .num_srcs = 4,
   .src_components = {
      1, 1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "store_stack",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 6,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_CALL_IDX,
      NIR_INTRINSIC_VALUE_ID,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_ALIGN_MUL] = 2,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 3,
      [NIR_INTRINSIC_WRITE_MASK] = 4,
      [NIR_INTRINSIC_CALL_IDX] = 5,
      [NIR_INTRINSIC_VALUE_ID] = 6,
    },
   .flags = 0,
},
{
   .name = "store_task_payload",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 4,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_WRITE_MASK,
      NIR_INTRINSIC_ALIGN_MUL,
      NIR_INTRINSIC_ALIGN_OFFSET,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_WRITE_MASK] = 2,
      [NIR_INTRINSIC_ALIGN_MUL] = 3,
      [NIR_INTRINSIC_ALIGN_OFFSET] = 4,
    },
   .flags = 0,
},
{
   .name = "store_tf_r600",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "store_tlb_sample_color_v3d",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_BASE,
      NIR_INTRINSIC_COMPONENT,
      NIR_INTRINSIC_SRC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
      [NIR_INTRINSIC_COMPONENT] = 2,
      [NIR_INTRINSIC_SRC_TYPE] = 3,
    },
   .flags = 0,
},
{
   .name = "store_uniform_ir3",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "store_zs_agx",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_add",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_and",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_comp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_exchange",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_fadd",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_fcomp_swap",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_fmax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_fmin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_imax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_imin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_or",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_umax",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_umin",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "task_payload_atomic_xor",
   .num_srcs = 2,
   .src_components = {
      1, 1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_BASE,
   },
   .index_map = {
      [NIR_INTRINSIC_BASE] = 1,
    },
   .flags = 0,
},
{
   .name = "terminate",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "terminate_if",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "terminate_ray",
   .num_srcs = 0,
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "trace_ray",
   .num_srcs = 11,
   .src_components = {
      -1, 1, 1, 1, 1, 1, 3, 1, 3, 1, -1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = 0,
},
{
   .name = "trace_ray_intel",
   .num_srcs = 3,
   .src_components = {
      1, 1, 1
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_SYNCHRONOUS,
   },
   .index_map = {
      [NIR_INTRINSIC_SYNCHRONOUS] = 1,
    },
   .flags = 0,
},
{
   .name = "vote_all",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "vote_any",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "vote_feq",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "vote_ieq",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = true,
   .dest_components = 1,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "vulkan_resource_index",
   .num_srcs = 1,
   .src_components = {
      1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 3,
   .indices = {
      NIR_INTRINSIC_DESC_SET,
      NIR_INTRINSIC_BINDING,
      NIR_INTRINSIC_DESC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_DESC_SET] = 1,
      [NIR_INTRINSIC_BINDING] = 2,
      [NIR_INTRINSIC_DESC_TYPE] = 3,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "vulkan_resource_reindex",
   .num_srcs = 2,
   .src_components = {
      0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_DESC_TYPE,
   },
   .index_map = {
      [NIR_INTRINSIC_DESC_TYPE] = 1,
    },
   .flags = NIR_INTRINSIC_CAN_ELIMINATE | NIR_INTRINSIC_CAN_REORDER,
},
{
   .name = "write_invocation_amd",
   .num_srcs = 3,
   .src_components = {
      0, 0, 1
   },
   .has_dest = true,
   .dest_components = 0,
   .dest_bit_sizes = 0x0,
   .bit_size_src = 0,
   .num_indices = 0,
   .flags = NIR_INTRINSIC_CAN_ELIMINATE,
},
{
   .name = "xfb_counter_sub_amd",
   .num_srcs = 1,
   .src_components = {
      0
   },
   .has_dest = false,
   .dest_components = 0,
   .dest_bit_sizes = 0x20,
   .bit_size_src = -1,
   .num_indices = 1,
   .indices = {
      NIR_INTRINSIC_WRITE_MASK,
   },
   .index_map = {
      [NIR_INTRINSIC_WRITE_MASK] = 1,
    },
   .flags = 0,
},
};

const char *nir_intrinsic_index_names[NIR_INTRINSIC_NUM_INDEX_FLAGS] = {
   "base",
   "write_mask",
   "stream_id",
   "ucp_id",
   "range_base",
   "range",
   "desc_set",
   "binding",
   "component",
   "column",
   "interp_mode",
   "reduction_op",
   "cluster_size",
   "param_idx",
   "image_dim",
   "image_array",
   "format",
   "access",
   "call_idx",
   "stack_size",
   "align_mul",
   "align_offset",
   "desc_type",
   "src_type",
   "dest_type",
   "swizzle_mask",
   "offset0",
   "offset1",
   "st64",
   "arg_upper_bound_u32_amd",
   "dst_access",
   "src_access",
   "driver_location",
   "memory_semantics",
   "memory_modes",
   "memory_scope",
   "execution_scope",
   "io_semantics",
   "io_xfb",
   "io_xfb2",
   "ray_query_value",
   "rounding_mode",
   "saturate",
   "synchronous",
   "value_id",
   "sign_extend",
   "flags",
};
