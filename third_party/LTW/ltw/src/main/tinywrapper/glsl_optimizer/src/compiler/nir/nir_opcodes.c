
#include "nir.h"

nir_op
nir_type_conversion_op(nir_alu_type src, nir_alu_type dst, nir_rounding_mode rnd)
{
   nir_alu_type src_base = (nir_alu_type) nir_alu_type_get_base_type(src);
   nir_alu_type dst_base = (nir_alu_type) nir_alu_type_get_base_type(dst);
   unsigned src_bit_size = nir_alu_type_get_type_size(src);
   unsigned dst_bit_size = nir_alu_type_get_type_size(dst);

   if (src == dst && src_base == nir_type_float) {
      return nir_op_mov;
   } else if (src == dst && src_base == nir_type_bool) {
      return nir_op_mov;
   } else if ((src_base == nir_type_int || src_base == nir_type_uint) &&
              (dst_base == nir_type_int || dst_base == nir_type_uint) &&
              src_bit_size == dst_bit_size) {
      /* Integer <-> integer conversions with the same bit-size on both
       * ends are just no-op moves.
       */
      return nir_op_mov;
   }

   /* f2b, i2b, and u2b do not exist.  Use ine or fne (via nir_type_conversion)
    * instead.
    */
   assert(src_base == dst_base || dst_base != nir_type_bool);

   switch (src_base) {
      case nir_type_int:
         switch (dst_base) {
            case nir_type_int:
            case nir_type_uint:

               switch (dst_bit_size) {
                  case 1:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2i1;
                  case 8:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2i8;
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2i16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2i32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2i64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_float:
               switch (dst_bit_size) {
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2f16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2f32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_i2f64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_bool:
            default:
               unreachable("Invalid nir alu base type");
         }
      case nir_type_uint:
         switch (dst_base) {
            case nir_type_int:
            case nir_type_uint:

               switch (dst_bit_size) {
                  case 1:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2u1;
                  case 8:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2u8;
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2u16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2u32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2u64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_float:
               switch (dst_bit_size) {
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2f16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2f32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_u2f64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_bool:
            default:
               unreachable("Invalid nir alu base type");
         }
      case nir_type_float:
         switch (dst_base) {
            case nir_type_int:
               switch (dst_bit_size) {
                  case 1:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2i1;
                  case 8:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2i8;
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2i16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2i32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2i64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_uint:
               switch (dst_bit_size) {
                  case 1:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2u1;
                  case 8:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2u8;
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2u16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2u32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2u64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_float:
               switch (dst_bit_size) {
                  case 16:
                     switch(rnd) {
                        case nir_rounding_mode_rtne:
                           return nir_op_f2f16_rtne;
                        case nir_rounding_mode_rtz:
                           return nir_op_f2f16_rtz;
                        case nir_rounding_mode_undef:
                           return nir_op_f2f16;
                        default:
                           unreachable("Invalid 16-bit nir rounding mode");
                     }
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2f32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_f2f64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_bool:
            default:
               unreachable("Invalid nir alu base type");
         }
      case nir_type_bool:
         switch (dst_base) {
            case nir_type_int:
            case nir_type_uint:

               switch (dst_bit_size) {
                  case 1:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i1;
                  case 8:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i8;
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_float:
               switch (dst_bit_size) {
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2f16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2f32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2f64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            case nir_type_bool:

               switch (dst_bit_size) {
                  case 1:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i1;
                  case 8:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i8;
                  case 16:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i16;
                  case 32:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i32;
                  case 64:
                     assert(rnd == nir_rounding_mode_undef);
                     return nir_op_b2i64;
                  default:
                     unreachable("Invalid nir alu bit size");
               }
            default:
               unreachable("Invalid nir alu base type");
         }
      default:
         unreachable("Invalid nir alu base type");
   }
}

const nir_op_info nir_op_infos[nir_num_opcodes] = {
{
   .name = "amul",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "b16all_fequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_fequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_fequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_fequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_fequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_fequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_iequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_iequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_iequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_iequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_iequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16all_iequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_fnequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_fnequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_fnequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_fnequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_fnequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_fnequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_inequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_inequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_inequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_inequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_inequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16any_inequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool16,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b16csel",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_bool16, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "b2b1",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2b16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2b32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2b8",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2f16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2f32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2f64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2i1",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2i16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2i32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2i64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b2i8",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int8,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_bool
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "b32all_fequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_fequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_fequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_fequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_fequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_fequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_iequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_iequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_iequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_iequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_iequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32all_iequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_fnequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_fnequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_fnequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_fnequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_fnequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_fnequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_inequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_inequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_inequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_inequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_inequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32any_inequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool32,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b32csel",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_bool32, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "b8all_fequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_fequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_fequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_fequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_fequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_fequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_iequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_iequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_iequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_iequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_iequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8all_iequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_fnequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_fnequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_fnequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_fnequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_fnequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_fnequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_inequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_inequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_inequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_inequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_inequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8any_inequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool8,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "b8csel",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_bool8, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "ball_fequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_fequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_fequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_fequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_fequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_fequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_iequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_iequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_iequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_iequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_iequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ball_iequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_fnequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_fnequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_fnequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_fnequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_fnequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_fnequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_inequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_inequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_inequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_inequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_inequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bany_inequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_bool1,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "bcsel",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_bool1, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "bfi",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "bfm",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "bit_count",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "bitfield_insert",
   .num_inputs = 4,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "bitfield_reverse",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "bitfield_select",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "cube_face_coord_amd",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_float32,
   .input_sizes = {
      3
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "cube_face_index_amd",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      3
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "cube_r600",
   .num_inputs = 1,
   .output_size = 4,
   .output_type = nir_type_float32,
   .input_sizes = {
      3
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "extr_agx",
   .num_inputs = 4,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "extract_i16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "extract_i8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "extract_u16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "extract_u8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "f2f16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2f16_rtne",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2f16_rtz",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2f32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2f64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2fmp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2i1",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2i16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2i32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2i64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2i8",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int8,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2imp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2u1",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2u16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2u32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2u64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2u8",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint8,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "f2ump",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "fabs",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fadd",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "fall_equal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fall_equal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fall_equal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fall_equal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fall_equal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fall_equal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fany_nequal16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fany_nequal2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fany_nequal3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fany_nequal4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fany_nequal5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fany_nequal8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float32,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fceil",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fclamp_pos_mali",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fcos",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fcos_amd",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fcos_mdg",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fcsel",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_float32, nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "fcsel_ge",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_float32, nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "fcsel_gt",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_float32, nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "fddx",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddx_coarse",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddx_fine",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddx_must_abs_mali",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddy",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddy_coarse",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddy_fine",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fddy_must_abs_mali",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fdiv",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fdot16",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot16_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      16, 16
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot2",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot2_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      2, 2
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot3",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot3_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      3, 3
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot4",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot4_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      4, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot5",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot5_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      5, 5
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot8",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdot8_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      8, 8
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fdph",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      3, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fdph_replicated",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      3, 4
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "feq",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "feq16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "feq32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "feq8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fexp2",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ffloor",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ffma",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ffmaz",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_float32, nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ffract",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fge",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fge16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fge32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fge8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "find_lsb",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fisfinite",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fisfinite32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fisnormal",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "flog2",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "flrp",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "flt",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "flt16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "flt32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "flt8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fmax",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "fmin",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "fmod",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fmul",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "fmulz",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "fneg",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fneu",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fneu16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fneu32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fneu8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "fpow",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fquantize2f16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "frcp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "frem",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "frexp_exp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "frexp_sig",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fround_even",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "frsq",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsat",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsat_signed_mali",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsign",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsin",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsin_agx",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsin_amd",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsin_mdg",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsqrt",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsub",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsum2",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsum3",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      3
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "fsum4",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_float,
   .input_sizes = {
      4
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ftrunc",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "i2f16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2f32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2f64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2fmp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int32
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2i1",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2i16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2i32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2i64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2i8",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int8,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i2imp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int32
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "i32csel_ge",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "i32csel_gt",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_SELECTION
},
{
   .name = "iabs",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "iadd",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "iadd3",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "iadd_sat",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "iand",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "ibfe",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ibitfield_extract",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "idiv",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ieq",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ieq16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ieq32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ieq8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ifind_msb",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ifind_msb_rev",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ige",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ige16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ige32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ige8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ihadd",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ilt",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ilt16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ilt32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ilt8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "imad24_ir3",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "imadsh_mix16",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "imax",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "imin",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "imod",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "imul",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "imul24",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "imul24_relaxed",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "imul_2x32_64",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int64,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "imul_32x16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "imul_high",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ine",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ine16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ine32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ine8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ineg",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "inot",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "insert_u16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "insert_u8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ior",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "irem",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "irhadd",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ishl",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ishr",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "isign",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "isub",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "isub_sat",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ixor",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "ldexp",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "mov",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_32_2x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_uint16
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_32_2x16_split",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint16, nir_type_uint16
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_32_4x8",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      4
   },
   .input_types = {
      nir_type_uint8
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_32_4x8_split",
   .num_inputs = 4,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0, 0
   },
   .input_types = {
      nir_type_uint8, nir_type_uint8, nir_type_uint8, nir_type_uint8
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_64_2x32",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint64,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_64_2x32_split",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint64,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_64_4x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint64,
   .input_sizes = {
      4
   },
   .input_types = {
      nir_type_uint16
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_double_2x32_dxil",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint64,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_half_2x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_half_2x16_rtz_split",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      1, 1
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_half_2x16_split",
   .num_inputs = 2,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      1, 1
   },
   .input_types = {
      nir_type_float32, nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_sint_2x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_int32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_snorm_2x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_snorm_4x8",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      4
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_uint_2x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_unorm_2x16",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_unorm_4x8",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      4
   },
   .input_types = {
      nir_type_float32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_uvec2_to_uint",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      2
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "pack_uvec4_to_uint",
   .num_inputs = 1,
   .output_size = 1,
   .output_type = nir_type_uint32,
   .input_sizes = {
      4
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "sad_u8x4",
   .num_inputs = 3,
   .output_size = 1,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "sdot_2x16_iadd",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "sdot_2x16_iadd_sat",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "sdot_4x8_iadd",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "sdot_4x8_iadd_sat",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "seq",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "sge",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "slt",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "sne",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_float,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_float, nir_type_float
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "sudot_4x8_iadd",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "sudot_4x8_iadd_sat",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "u2f16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2f32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2f64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2fmp",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2u1",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint1,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2u16",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2u32",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2u64",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint64,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "u2u8",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint8,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = true,
   .algebraic_properties =
      0
},
{
   .name = "uabs_isub",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int, nir_type_int
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uabs_usub",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uadd_carry",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "uadd_sat",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ubfe",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ubitfield_extract",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uclz",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "udiv",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "udot_2x16_uadd",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "udot_2x16_uadd_sat",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "udot_4x8_uadd",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "udot_4x8_uadd_sat",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ufind_msb",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ufind_msb_rev",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uge",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uge16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uge32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uge8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uhadd",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "ult",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool1,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ult16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool16,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ult32",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ult8",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_bool8,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "umad24",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "umad24_relaxed",
   .num_inputs = 3,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "umax",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "umax_4x8_vc4",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "umin",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "umin_4x8_vc4",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "umod",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "umul24",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "umul24_relaxed",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "umul_2x32_64",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint64,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "umul_32x16",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "umul_high",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "umul_low",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint32, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "umul_unorm_4x8_vc4",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "unpack_32_2x16",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_uint16,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_32_2x16_split_x",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_32_2x16_split_y",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint16,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_32_4x8",
   .num_inputs = 1,
   .output_size = 4,
   .output_type = nir_type_uint8,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_64_2x32",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_uint32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint64
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_64_2x32_split_x",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint64
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_64_2x32_split_y",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_uint32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint64
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_64_4x16",
   .num_inputs = 1,
   .output_size = 4,
   .output_type = nir_type_uint16,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint64
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_double_2x32_dxil",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_uint32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint64
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_half_2x16",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_float32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_half_2x16_flush_to_zero",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_float32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_half_2x16_split_x",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_half_2x16_split_x_flush_to_zero",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_half_2x16_split_y",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_half_2x16_split_y_flush_to_zero",
   .num_inputs = 1,
   .output_size = 0,
   .output_type = nir_type_float32,
   .input_sizes = {
      0
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_snorm_2x16",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_float32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_snorm_4x8",
   .num_inputs = 1,
   .output_size = 4,
   .output_type = nir_type_float32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_unorm_2x16",
   .num_inputs = 1,
   .output_size = 2,
   .output_type = nir_type_float32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "unpack_unorm_4x8",
   .num_inputs = 1,
   .output_size = 4,
   .output_type = nir_type_float32,
   .input_sizes = {
      1
   },
   .input_types = {
      nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "urhadd",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE
},
{
   .name = "urol",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "uror",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "usadd_4x8_vc4",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      NIR_OP_IS_2SRC_COMMUTATIVE | NIR_OP_IS_ASSOCIATIVE
},
{
   .name = "ushr",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "ussub_4x8_vc4",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_int32,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_int32, nir_type_int32
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "usub_borrow",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "usub_sat",
   .num_inputs = 2,
   .output_size = 0,
   .output_type = nir_type_uint,
   .input_sizes = {
      0, 0
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "vec16",
   .num_inputs = 16,
   .output_size = 16,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "vec2",
   .num_inputs = 2,
   .output_size = 2,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "vec3",
   .num_inputs = 3,
   .output_size = 3,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "vec4",
   .num_inputs = 4,
   .output_size = 4,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1, 1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "vec5",
   .num_inputs = 5,
   .output_size = 5,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1, 1, 1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
{
   .name = "vec8",
   .num_inputs = 8,
   .output_size = 8,
   .output_type = nir_type_uint,
   .input_sizes = {
      1, 1, 1, 1, 1, 1, 1, 1
   },
   .input_types = {
      nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint, nir_type_uint
   },
   .is_conversion = false,
   .algebraic_properties =
      0
},
};

