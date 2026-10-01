/* Copyright (C) 2015 Broadcom
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

#ifndef _NIR_BUILDER_OPCODES_
#define _NIR_BUILDER_OPCODES_



static inline nir_ssa_def *
nir_amul(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_amul, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_fequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_fequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_fequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_fequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_fequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_fequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_fequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_fequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_fequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_fequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_fequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_fequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_iequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_iequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_iequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_iequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_iequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_iequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_iequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_iequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_iequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_iequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b16all_iequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16all_iequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_fnequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_fnequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_fnequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_fnequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_fnequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_fnequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_fnequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_fnequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_fnequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_fnequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_fnequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_fnequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_inequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_inequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_inequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_inequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_inequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_inequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_inequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_inequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_inequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_inequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b16any_inequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b16any_inequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b16csel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_b16csel, src0, src1, src2);
}
static inline nir_ssa_def *
nir_b2b1(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 1)
      return src0;
   return nir_build_alu1(build, nir_op_b2b1, src0);
}
static inline nir_ssa_def *
nir_b2b16(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 16)
      return src0;
   return nir_build_alu1(build, nir_op_b2b16, src0);
}
static inline nir_ssa_def *
nir_b2b32(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 32)
      return src0;
   return nir_build_alu1(build, nir_op_b2b32, src0);
}
static inline nir_ssa_def *
nir_b2b8(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 8)
      return src0;
   return nir_build_alu1(build, nir_op_b2b8, src0);
}
static inline nir_ssa_def *
nir_b2f16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2f16, src0);
}
static inline nir_ssa_def *
nir_b2f32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2f32, src0);
}
static inline nir_ssa_def *
nir_b2f64(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2f64, src0);
}
static inline nir_ssa_def *
nir_b2i1(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2i1, src0);
}
static inline nir_ssa_def *
nir_b2i16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2i16, src0);
}
static inline nir_ssa_def *
nir_b2i32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2i32, src0);
}
static inline nir_ssa_def *
nir_b2i64(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2i64, src0);
}
static inline nir_ssa_def *
nir_b2i8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_b2i8, src0);
}
static inline nir_ssa_def *
nir_b32all_fequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_fequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_fequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_fequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_fequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_fequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_fequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_fequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_fequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_fequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_fequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_fequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_iequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_iequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_iequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_iequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_iequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_iequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_iequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_iequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_iequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_iequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b32all_iequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32all_iequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_fnequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_fnequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_fnequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_fnequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_fnequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_fnequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_fnequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_fnequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_fnequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_fnequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_fnequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_fnequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_inequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_inequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_inequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_inequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_inequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_inequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_inequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_inequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_inequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_inequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b32any_inequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b32any_inequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b32csel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_b32csel, src0, src1, src2);
}
static inline nir_ssa_def *
nir_b8all_fequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_fequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_fequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_fequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_fequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_fequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_fequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_fequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_fequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_fequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_fequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_fequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_iequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_iequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_iequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_iequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_iequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_iequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_iequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_iequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_iequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_iequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b8all_iequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8all_iequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_fnequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_fnequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_fnequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_fnequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_fnequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_fnequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_fnequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_fnequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_fnequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_fnequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_fnequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_fnequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_inequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_inequal16, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_inequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_inequal2, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_inequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_inequal3, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_inequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_inequal4, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_inequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_inequal5, src0, src1);
}
static inline nir_ssa_def *
nir_b8any_inequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_b8any_inequal8, src0, src1);
}
static inline nir_ssa_def *
nir_b8csel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_b8csel, src0, src1, src2);
}
static inline nir_ssa_def *
nir_ball_fequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_fequal16, src0, src1);
}
static inline nir_ssa_def *
nir_ball_fequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_fequal2, src0, src1);
}
static inline nir_ssa_def *
nir_ball_fequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_fequal3, src0, src1);
}
static inline nir_ssa_def *
nir_ball_fequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_fequal4, src0, src1);
}
static inline nir_ssa_def *
nir_ball_fequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_fequal5, src0, src1);
}
static inline nir_ssa_def *
nir_ball_fequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_fequal8, src0, src1);
}
static inline nir_ssa_def *
nir_ball_iequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_iequal16, src0, src1);
}
static inline nir_ssa_def *
nir_ball_iequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_iequal2, src0, src1);
}
static inline nir_ssa_def *
nir_ball_iequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_iequal3, src0, src1);
}
static inline nir_ssa_def *
nir_ball_iequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_iequal4, src0, src1);
}
static inline nir_ssa_def *
nir_ball_iequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_iequal5, src0, src1);
}
static inline nir_ssa_def *
nir_ball_iequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ball_iequal8, src0, src1);
}
static inline nir_ssa_def *
nir_bany_fnequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_fnequal16, src0, src1);
}
static inline nir_ssa_def *
nir_bany_fnequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_fnequal2, src0, src1);
}
static inline nir_ssa_def *
nir_bany_fnequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_fnequal3, src0, src1);
}
static inline nir_ssa_def *
nir_bany_fnequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_fnequal4, src0, src1);
}
static inline nir_ssa_def *
nir_bany_fnequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_fnequal5, src0, src1);
}
static inline nir_ssa_def *
nir_bany_fnequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_fnequal8, src0, src1);
}
static inline nir_ssa_def *
nir_bany_inequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_inequal16, src0, src1);
}
static inline nir_ssa_def *
nir_bany_inequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_inequal2, src0, src1);
}
static inline nir_ssa_def *
nir_bany_inequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_inequal3, src0, src1);
}
static inline nir_ssa_def *
nir_bany_inequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_inequal4, src0, src1);
}
static inline nir_ssa_def *
nir_bany_inequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_inequal5, src0, src1);
}
static inline nir_ssa_def *
nir_bany_inequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bany_inequal8, src0, src1);
}
static inline nir_ssa_def *
nir_bcsel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_bcsel, src0, src1, src2);
}
static inline nir_ssa_def *
nir_bfi(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_bfi, src0, src1, src2);
}
static inline nir_ssa_def *
nir_bfm(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_bfm, src0, src1);
}
static inline nir_ssa_def *
nir_bit_count(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_bit_count, src0);
}
static inline nir_ssa_def *
nir_bitfield_insert(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3)
{
   return nir_build_alu4(build, nir_op_bitfield_insert, src0, src1, src2, src3);
}
static inline nir_ssa_def *
nir_bitfield_reverse(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_bitfield_reverse, src0);
}
static inline nir_ssa_def *
nir_bitfield_select(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_bitfield_select, src0, src1, src2);
}
static inline nir_ssa_def *
nir_cube_face_coord_amd(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_cube_face_coord_amd, src0);
}
static inline nir_ssa_def *
nir_cube_face_index_amd(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_cube_face_index_amd, src0);
}
static inline nir_ssa_def *
nir_cube_r600(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_cube_r600, src0);
}
static inline nir_ssa_def *
nir_extr_agx(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3)
{
   return nir_build_alu4(build, nir_op_extr_agx, src0, src1, src2, src3);
}
static inline nir_ssa_def *
nir_extract_i16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_extract_i16, src0, src1);
}
static inline nir_ssa_def *
nir_extract_i8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_extract_i8, src0, src1);
}
static inline nir_ssa_def *
nir_extract_u16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_extract_u16, src0, src1);
}
static inline nir_ssa_def *
nir_extract_u8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_extract_u8, src0, src1);
}
static inline nir_ssa_def *
nir_f2f16(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 16)
      return src0;
   return nir_build_alu1(build, nir_op_f2f16, src0);
}
static inline nir_ssa_def *
nir_f2f16_rtne(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 16)
      return src0;
   return nir_build_alu1(build, nir_op_f2f16_rtne, src0);
}
static inline nir_ssa_def *
nir_f2f16_rtz(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 16)
      return src0;
   return nir_build_alu1(build, nir_op_f2f16_rtz, src0);
}
static inline nir_ssa_def *
nir_f2f32(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 32)
      return src0;
   return nir_build_alu1(build, nir_op_f2f32, src0);
}
static inline nir_ssa_def *
nir_f2f64(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 64)
      return src0;
   return nir_build_alu1(build, nir_op_f2f64, src0);
}
static inline nir_ssa_def *
nir_f2fmp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2fmp, src0);
}
static inline nir_ssa_def *
nir_f2i1(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2i1, src0);
}
static inline nir_ssa_def *
nir_f2i16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2i16, src0);
}
static inline nir_ssa_def *
nir_f2i32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2i32, src0);
}
static inline nir_ssa_def *
nir_f2i64(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2i64, src0);
}
static inline nir_ssa_def *
nir_f2i8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2i8, src0);
}
static inline nir_ssa_def *
nir_f2imp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2imp, src0);
}
static inline nir_ssa_def *
nir_f2u1(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2u1, src0);
}
static inline nir_ssa_def *
nir_f2u16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2u16, src0);
}
static inline nir_ssa_def *
nir_f2u32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2u32, src0);
}
static inline nir_ssa_def *
nir_f2u64(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2u64, src0);
}
static inline nir_ssa_def *
nir_f2u8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2u8, src0);
}
static inline nir_ssa_def *
nir_f2ump(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_f2ump, src0);
}
static inline nir_ssa_def *
nir_fabs(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fabs, src0);
}
static inline nir_ssa_def *
nir_fadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fadd, src0, src1);
}
static inline nir_ssa_def *
nir_fall_equal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fall_equal16, src0, src1);
}
static inline nir_ssa_def *
nir_fall_equal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fall_equal2, src0, src1);
}
static inline nir_ssa_def *
nir_fall_equal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fall_equal3, src0, src1);
}
static inline nir_ssa_def *
nir_fall_equal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fall_equal4, src0, src1);
}
static inline nir_ssa_def *
nir_fall_equal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fall_equal5, src0, src1);
}
static inline nir_ssa_def *
nir_fall_equal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fall_equal8, src0, src1);
}
static inline nir_ssa_def *
nir_fany_nequal16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fany_nequal16, src0, src1);
}
static inline nir_ssa_def *
nir_fany_nequal2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fany_nequal2, src0, src1);
}
static inline nir_ssa_def *
nir_fany_nequal3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fany_nequal3, src0, src1);
}
static inline nir_ssa_def *
nir_fany_nequal4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fany_nequal4, src0, src1);
}
static inline nir_ssa_def *
nir_fany_nequal5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fany_nequal5, src0, src1);
}
static inline nir_ssa_def *
nir_fany_nequal8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fany_nequal8, src0, src1);
}
static inline nir_ssa_def *
nir_fceil(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fceil, src0);
}
static inline nir_ssa_def *
nir_fclamp_pos_mali(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fclamp_pos_mali, src0);
}
static inline nir_ssa_def *
nir_fcos(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fcos, src0);
}
static inline nir_ssa_def *
nir_fcos_amd(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fcos_amd, src0);
}
static inline nir_ssa_def *
nir_fcos_mdg(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fcos_mdg, src0);
}
static inline nir_ssa_def *
nir_fcsel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_fcsel, src0, src1, src2);
}
static inline nir_ssa_def *
nir_fcsel_ge(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_fcsel_ge, src0, src1, src2);
}
static inline nir_ssa_def *
nir_fcsel_gt(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_fcsel_gt, src0, src1, src2);
}
static inline nir_ssa_def *
nir_fddx(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddx, src0);
}
static inline nir_ssa_def *
nir_fddx_coarse(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddx_coarse, src0);
}
static inline nir_ssa_def *
nir_fddx_fine(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddx_fine, src0);
}
static inline nir_ssa_def *
nir_fddx_must_abs_mali(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddx_must_abs_mali, src0);
}
static inline nir_ssa_def *
nir_fddy(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddy, src0);
}
static inline nir_ssa_def *
nir_fddy_coarse(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddy_coarse, src0);
}
static inline nir_ssa_def *
nir_fddy_fine(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddy_fine, src0);
}
static inline nir_ssa_def *
nir_fddy_must_abs_mali(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fddy_must_abs_mali, src0);
}
static inline nir_ssa_def *
nir_fdiv(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdiv, src0, src1);
}
static inline nir_ssa_def *
nir_fdot16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdot16, src0, src1);
}
static inline nir_ssa_def *
nir_fdot2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdot2, src0, src1);
}
static inline nir_ssa_def *
nir_fdot3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdot3, src0, src1);
}
static inline nir_ssa_def *
nir_fdot4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdot4, src0, src1);
}
static inline nir_ssa_def *
nir_fdot5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdot5, src0, src1);
}
static inline nir_ssa_def *
nir_fdot8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdot8, src0, src1);
}
static inline nir_ssa_def *
nir_fdph(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fdph, src0, src1);
}
static inline nir_ssa_def *
nir_feq(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_feq, src0, src1);
}
static inline nir_ssa_def *
nir_feq16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_feq16, src0, src1);
}
static inline nir_ssa_def *
nir_feq32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_feq32, src0, src1);
}
static inline nir_ssa_def *
nir_feq8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_feq8, src0, src1);
}
static inline nir_ssa_def *
nir_fexp2(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fexp2, src0);
}
static inline nir_ssa_def *
nir_ffloor(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ffloor, src0);
}
static inline nir_ssa_def *
nir_ffma(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_ffma, src0, src1, src2);
}
static inline nir_ssa_def *
nir_ffmaz(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_ffmaz, src0, src1, src2);
}
static inline nir_ssa_def *
nir_ffract(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ffract, src0);
}
static inline nir_ssa_def *
nir_fge(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fge, src0, src1);
}
static inline nir_ssa_def *
nir_fge16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fge16, src0, src1);
}
static inline nir_ssa_def *
nir_fge32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fge32, src0, src1);
}
static inline nir_ssa_def *
nir_fge8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fge8, src0, src1);
}
static inline nir_ssa_def *
nir_find_lsb(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_find_lsb, src0);
}
static inline nir_ssa_def *
nir_fisfinite(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fisfinite, src0);
}
static inline nir_ssa_def *
nir_fisfinite32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fisfinite32, src0);
}
static inline nir_ssa_def *
nir_fisnormal(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fisnormal, src0);
}
static inline nir_ssa_def *
nir_flog2(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_flog2, src0);
}
static inline nir_ssa_def *
nir_flrp(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_flrp, src0, src1, src2);
}
static inline nir_ssa_def *
nir_flt(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_flt, src0, src1);
}
static inline nir_ssa_def *
nir_flt16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_flt16, src0, src1);
}
static inline nir_ssa_def *
nir_flt32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_flt32, src0, src1);
}
static inline nir_ssa_def *
nir_flt8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_flt8, src0, src1);
}
static inline nir_ssa_def *
nir_fmax(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fmax, src0, src1);
}
static inline nir_ssa_def *
nir_fmin(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fmin, src0, src1);
}
static inline nir_ssa_def *
nir_fmod(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fmod, src0, src1);
}
static inline nir_ssa_def *
nir_fmul(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fmul, src0, src1);
}
static inline nir_ssa_def *
nir_fmulz(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fmulz, src0, src1);
}
static inline nir_ssa_def *
nir_fneg(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fneg, src0);
}
static inline nir_ssa_def *
nir_fneu(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fneu, src0, src1);
}
static inline nir_ssa_def *
nir_fneu16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fneu16, src0, src1);
}
static inline nir_ssa_def *
nir_fneu32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fneu32, src0, src1);
}
static inline nir_ssa_def *
nir_fneu8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fneu8, src0, src1);
}
static inline nir_ssa_def *
nir_fpow(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fpow, src0, src1);
}
static inline nir_ssa_def *
nir_fquantize2f16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fquantize2f16, src0);
}
static inline nir_ssa_def *
nir_frcp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_frcp, src0);
}
static inline nir_ssa_def *
nir_frem(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_frem, src0, src1);
}
static inline nir_ssa_def *
nir_frexp_exp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_frexp_exp, src0);
}
static inline nir_ssa_def *
nir_frexp_sig(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_frexp_sig, src0);
}
static inline nir_ssa_def *
nir_fround_even(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fround_even, src0);
}
static inline nir_ssa_def *
nir_frsq(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_frsq, src0);
}
static inline nir_ssa_def *
nir_fsat(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsat, src0);
}
static inline nir_ssa_def *
nir_fsat_signed_mali(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsat_signed_mali, src0);
}
static inline nir_ssa_def *
nir_fsign(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsign, src0);
}
static inline nir_ssa_def *
nir_fsin(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsin, src0);
}
static inline nir_ssa_def *
nir_fsin_agx(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsin_agx, src0);
}
static inline nir_ssa_def *
nir_fsin_amd(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsin_amd, src0);
}
static inline nir_ssa_def *
nir_fsin_mdg(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsin_mdg, src0);
}
static inline nir_ssa_def *
nir_fsqrt(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsqrt, src0);
}
static inline nir_ssa_def *
nir_fsub(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_fsub, src0, src1);
}
static inline nir_ssa_def *
nir_fsum2(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsum2, src0);
}
static inline nir_ssa_def *
nir_fsum3(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsum3, src0);
}
static inline nir_ssa_def *
nir_fsum4(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_fsum4, src0);
}
static inline nir_ssa_def *
nir_ftrunc(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ftrunc, src0);
}
static inline nir_ssa_def *
nir_i2f16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_i2f16, src0);
}
static inline nir_ssa_def *
nir_i2f32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_i2f32, src0);
}
static inline nir_ssa_def *
nir_i2f64(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_i2f64, src0);
}
static inline nir_ssa_def *
nir_i2fmp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_i2fmp, src0);
}
static inline nir_ssa_def *
nir_i2i1(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 1)
      return src0;
   return nir_build_alu1(build, nir_op_i2i1, src0);
}
static inline nir_ssa_def *
nir_i2i16(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 16)
      return src0;
   return nir_build_alu1(build, nir_op_i2i16, src0);
}
static inline nir_ssa_def *
nir_i2i32(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 32)
      return src0;
   return nir_build_alu1(build, nir_op_i2i32, src0);
}
static inline nir_ssa_def *
nir_i2i64(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 64)
      return src0;
   return nir_build_alu1(build, nir_op_i2i64, src0);
}
static inline nir_ssa_def *
nir_i2i8(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 8)
      return src0;
   return nir_build_alu1(build, nir_op_i2i8, src0);
}
static inline nir_ssa_def *
nir_i2imp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_i2imp, src0);
}
static inline nir_ssa_def *
nir_i32csel_ge(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_i32csel_ge, src0, src1, src2);
}
static inline nir_ssa_def *
nir_i32csel_gt(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_i32csel_gt, src0, src1, src2);
}
static inline nir_ssa_def *
nir_iabs(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_iabs, src0);
}
static inline nir_ssa_def *
nir_iadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_iadd, src0, src1);
}
static inline nir_ssa_def *
nir_iadd3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_iadd3, src0, src1, src2);
}
static inline nir_ssa_def *
nir_iadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_iadd_sat, src0, src1);
}
static inline nir_ssa_def *
nir_iand(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_iand, src0, src1);
}
static inline nir_ssa_def *
nir_ibfe(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_ibfe, src0, src1, src2);
}
static inline nir_ssa_def *
nir_ibitfield_extract(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_ibitfield_extract, src0, src1, src2);
}
static inline nir_ssa_def *
nir_idiv(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_idiv, src0, src1);
}
static inline nir_ssa_def *
nir_ieq(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ieq, src0, src1);
}
static inline nir_ssa_def *
nir_ieq16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ieq16, src0, src1);
}
static inline nir_ssa_def *
nir_ieq32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ieq32, src0, src1);
}
static inline nir_ssa_def *
nir_ieq8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ieq8, src0, src1);
}
static inline nir_ssa_def *
nir_ifind_msb(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ifind_msb, src0);
}
static inline nir_ssa_def *
nir_ifind_msb_rev(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ifind_msb_rev, src0);
}
static inline nir_ssa_def *
nir_ige(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ige, src0, src1);
}
static inline nir_ssa_def *
nir_ige16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ige16, src0, src1);
}
static inline nir_ssa_def *
nir_ige32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ige32, src0, src1);
}
static inline nir_ssa_def *
nir_ige8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ige8, src0, src1);
}
static inline nir_ssa_def *
nir_ihadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ihadd, src0, src1);
}
static inline nir_ssa_def *
nir_ilt(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ilt, src0, src1);
}
static inline nir_ssa_def *
nir_ilt16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ilt16, src0, src1);
}
static inline nir_ssa_def *
nir_ilt32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ilt32, src0, src1);
}
static inline nir_ssa_def *
nir_ilt8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ilt8, src0, src1);
}
static inline nir_ssa_def *
nir_imad24_ir3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_imad24_ir3, src0, src1, src2);
}
static inline nir_ssa_def *
nir_imadsh_mix16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_imadsh_mix16, src0, src1, src2);
}
static inline nir_ssa_def *
nir_imax(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imax, src0, src1);
}
static inline nir_ssa_def *
nir_imin(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imin, src0, src1);
}
static inline nir_ssa_def *
nir_imod(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imod, src0, src1);
}
static inline nir_ssa_def *
nir_imul(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imul, src0, src1);
}
static inline nir_ssa_def *
nir_imul24(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imul24, src0, src1);
}
static inline nir_ssa_def *
nir_imul24_relaxed(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imul24_relaxed, src0, src1);
}
static inline nir_ssa_def *
nir_imul_2x32_64(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imul_2x32_64, src0, src1);
}
static inline nir_ssa_def *
nir_imul_32x16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imul_32x16, src0, src1);
}
static inline nir_ssa_def *
nir_imul_high(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_imul_high, src0, src1);
}
static inline nir_ssa_def *
nir_ine(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ine, src0, src1);
}
static inline nir_ssa_def *
nir_ine16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ine16, src0, src1);
}
static inline nir_ssa_def *
nir_ine32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ine32, src0, src1);
}
static inline nir_ssa_def *
nir_ine8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ine8, src0, src1);
}
static inline nir_ssa_def *
nir_ineg(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ineg, src0);
}
static inline nir_ssa_def *
nir_inot(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_inot, src0);
}
static inline nir_ssa_def *
nir_insert_u16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_insert_u16, src0, src1);
}
static inline nir_ssa_def *
nir_insert_u8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_insert_u8, src0, src1);
}
static inline nir_ssa_def *
nir_ior(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ior, src0, src1);
}
static inline nir_ssa_def *
nir_irem(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_irem, src0, src1);
}
static inline nir_ssa_def *
nir_irhadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_irhadd, src0, src1);
}
static inline nir_ssa_def *
nir_ishl(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ishl, src0, src1);
}
static inline nir_ssa_def *
nir_ishr(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ishr, src0, src1);
}
static inline nir_ssa_def *
nir_isign(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_isign, src0);
}
static inline nir_ssa_def *
nir_isub(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_isub, src0, src1);
}
static inline nir_ssa_def *
nir_isub_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_isub_sat, src0, src1);
}
static inline nir_ssa_def *
nir_ixor(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ixor, src0, src1);
}
static inline nir_ssa_def *
nir_ldexp(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ldexp, src0, src1);
}
static inline nir_ssa_def *
nir_mov(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_mov, src0);
}
static inline nir_ssa_def *
nir_pack_32_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_32_2x16, src0);
}
static inline nir_ssa_def *
nir_pack_32_2x16_split(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_pack_32_2x16_split, src0, src1);
}
static inline nir_ssa_def *
nir_pack_32_4x8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_32_4x8, src0);
}
static inline nir_ssa_def *
nir_pack_32_4x8_split(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3)
{
   return nir_build_alu4(build, nir_op_pack_32_4x8_split, src0, src1, src2, src3);
}
static inline nir_ssa_def *
nir_pack_64_2x32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_64_2x32, src0);
}
static inline nir_ssa_def *
nir_pack_64_2x32_split(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_pack_64_2x32_split, src0, src1);
}
static inline nir_ssa_def *
nir_pack_64_4x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_64_4x16, src0);
}
static inline nir_ssa_def *
nir_pack_double_2x32_dxil(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_double_2x32_dxil, src0);
}
static inline nir_ssa_def *
nir_pack_half_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_half_2x16, src0);
}
static inline nir_ssa_def *
nir_pack_half_2x16_rtz_split(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_pack_half_2x16_rtz_split, src0, src1);
}
static inline nir_ssa_def *
nir_pack_half_2x16_split(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_pack_half_2x16_split, src0, src1);
}
static inline nir_ssa_def *
nir_pack_sint_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_sint_2x16, src0);
}
static inline nir_ssa_def *
nir_pack_snorm_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_snorm_2x16, src0);
}
static inline nir_ssa_def *
nir_pack_snorm_4x8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_snorm_4x8, src0);
}
static inline nir_ssa_def *
nir_pack_uint_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_uint_2x16, src0);
}
static inline nir_ssa_def *
nir_pack_unorm_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_unorm_2x16, src0);
}
static inline nir_ssa_def *
nir_pack_unorm_4x8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_unorm_4x8, src0);
}
static inline nir_ssa_def *
nir_pack_uvec2_to_uint(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_uvec2_to_uint, src0);
}
static inline nir_ssa_def *
nir_pack_uvec4_to_uint(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_pack_uvec4_to_uint, src0);
}
static inline nir_ssa_def *
nir_sad_u8x4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sad_u8x4, src0, src1, src2);
}
static inline nir_ssa_def *
nir_sdot_2x16_iadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sdot_2x16_iadd, src0, src1, src2);
}
static inline nir_ssa_def *
nir_sdot_2x16_iadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sdot_2x16_iadd_sat, src0, src1, src2);
}
static inline nir_ssa_def *
nir_sdot_4x8_iadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sdot_4x8_iadd, src0, src1, src2);
}
static inline nir_ssa_def *
nir_sdot_4x8_iadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sdot_4x8_iadd_sat, src0, src1, src2);
}
static inline nir_ssa_def *
nir_seq(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_seq, src0, src1);
}
static inline nir_ssa_def *
nir_sge(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_sge, src0, src1);
}
static inline nir_ssa_def *
nir_slt(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_slt, src0, src1);
}
static inline nir_ssa_def *
nir_sne(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_sne, src0, src1);
}
static inline nir_ssa_def *
nir_sudot_4x8_iadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sudot_4x8_iadd, src0, src1, src2);
}
static inline nir_ssa_def *
nir_sudot_4x8_iadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_sudot_4x8_iadd_sat, src0, src1, src2);
}
static inline nir_ssa_def *
nir_u2f16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_u2f16, src0);
}
static inline nir_ssa_def *
nir_u2f32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_u2f32, src0);
}
static inline nir_ssa_def *
nir_u2f64(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_u2f64, src0);
}
static inline nir_ssa_def *
nir_u2fmp(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_u2fmp, src0);
}
static inline nir_ssa_def *
nir_u2u1(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 1)
      return src0;
   return nir_build_alu1(build, nir_op_u2u1, src0);
}
static inline nir_ssa_def *
nir_u2u16(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 16)
      return src0;
   return nir_build_alu1(build, nir_op_u2u16, src0);
}
static inline nir_ssa_def *
nir_u2u32(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 32)
      return src0;
   return nir_build_alu1(build, nir_op_u2u32, src0);
}
static inline nir_ssa_def *
nir_u2u64(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 64)
      return src0;
   return nir_build_alu1(build, nir_op_u2u64, src0);
}
static inline nir_ssa_def *
nir_u2u8(nir_builder *build, nir_ssa_def *src0)
{
   if (src0->bit_size == 8)
      return src0;
   return nir_build_alu1(build, nir_op_u2u8, src0);
}
static inline nir_ssa_def *
nir_uabs_isub(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uabs_isub, src0, src1);
}
static inline nir_ssa_def *
nir_uabs_usub(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uabs_usub, src0, src1);
}
static inline nir_ssa_def *
nir_uadd_carry(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uadd_carry, src0, src1);
}
static inline nir_ssa_def *
nir_uadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uadd_sat, src0, src1);
}
static inline nir_ssa_def *
nir_ubfe(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_ubfe, src0, src1, src2);
}
static inline nir_ssa_def *
nir_ubitfield_extract(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_ubitfield_extract, src0, src1, src2);
}
static inline nir_ssa_def *
nir_uclz(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_uclz, src0);
}
static inline nir_ssa_def *
nir_udiv(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_udiv, src0, src1);
}
static inline nir_ssa_def *
nir_udot_2x16_uadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_udot_2x16_uadd, src0, src1, src2);
}
static inline nir_ssa_def *
nir_udot_2x16_uadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_udot_2x16_uadd_sat, src0, src1, src2);
}
static inline nir_ssa_def *
nir_udot_4x8_uadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_udot_4x8_uadd, src0, src1, src2);
}
static inline nir_ssa_def *
nir_udot_4x8_uadd_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_udot_4x8_uadd_sat, src0, src1, src2);
}
static inline nir_ssa_def *
nir_ufind_msb(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ufind_msb, src0);
}
static inline nir_ssa_def *
nir_ufind_msb_rev(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_ufind_msb_rev, src0);
}
static inline nir_ssa_def *
nir_uge(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uge, src0, src1);
}
static inline nir_ssa_def *
nir_uge16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uge16, src0, src1);
}
static inline nir_ssa_def *
nir_uge32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uge32, src0, src1);
}
static inline nir_ssa_def *
nir_uge8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uge8, src0, src1);
}
static inline nir_ssa_def *
nir_uhadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uhadd, src0, src1);
}
static inline nir_ssa_def *
nir_ult(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ult, src0, src1);
}
static inline nir_ssa_def *
nir_ult16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ult16, src0, src1);
}
static inline nir_ssa_def *
nir_ult32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ult32, src0, src1);
}
static inline nir_ssa_def *
nir_ult8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ult8, src0, src1);
}
static inline nir_ssa_def *
nir_umad24(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_umad24, src0, src1, src2);
}
static inline nir_ssa_def *
nir_umad24_relaxed(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_umad24_relaxed, src0, src1, src2);
}
static inline nir_ssa_def *
nir_umax(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umax, src0, src1);
}
static inline nir_ssa_def *
nir_umax_4x8_vc4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umax_4x8_vc4, src0, src1);
}
static inline nir_ssa_def *
nir_umin(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umin, src0, src1);
}
static inline nir_ssa_def *
nir_umin_4x8_vc4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umin_4x8_vc4, src0, src1);
}
static inline nir_ssa_def *
nir_umod(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umod, src0, src1);
}
static inline nir_ssa_def *
nir_umul24(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul24, src0, src1);
}
static inline nir_ssa_def *
nir_umul24_relaxed(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul24_relaxed, src0, src1);
}
static inline nir_ssa_def *
nir_umul_2x32_64(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul_2x32_64, src0, src1);
}
static inline nir_ssa_def *
nir_umul_32x16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul_32x16, src0, src1);
}
static inline nir_ssa_def *
nir_umul_high(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul_high, src0, src1);
}
static inline nir_ssa_def *
nir_umul_low(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul_low, src0, src1);
}
static inline nir_ssa_def *
nir_umul_unorm_4x8_vc4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_umul_unorm_4x8_vc4, src0, src1);
}
static inline nir_ssa_def *
nir_unpack_32_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_32_2x16, src0);
}
static inline nir_ssa_def *
nir_unpack_32_2x16_split_x(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_32_2x16_split_x, src0);
}
static inline nir_ssa_def *
nir_unpack_32_2x16_split_y(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_32_2x16_split_y, src0);
}
static inline nir_ssa_def *
nir_unpack_32_4x8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_32_4x8, src0);
}
static inline nir_ssa_def *
nir_unpack_64_2x32(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_64_2x32, src0);
}
static inline nir_ssa_def *
nir_unpack_64_2x32_split_x(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_64_2x32_split_x, src0);
}
static inline nir_ssa_def *
nir_unpack_64_2x32_split_y(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_64_2x32_split_y, src0);
}
static inline nir_ssa_def *
nir_unpack_64_4x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_64_4x16, src0);
}
static inline nir_ssa_def *
nir_unpack_double_2x32_dxil(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_double_2x32_dxil, src0);
}
static inline nir_ssa_def *
nir_unpack_half_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_half_2x16, src0);
}
static inline nir_ssa_def *
nir_unpack_half_2x16_flush_to_zero(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_half_2x16_flush_to_zero, src0);
}
static inline nir_ssa_def *
nir_unpack_half_2x16_split_x(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_half_2x16_split_x, src0);
}
static inline nir_ssa_def *
nir_unpack_half_2x16_split_x_flush_to_zero(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_half_2x16_split_x_flush_to_zero, src0);
}
static inline nir_ssa_def *
nir_unpack_half_2x16_split_y(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_half_2x16_split_y, src0);
}
static inline nir_ssa_def *
nir_unpack_half_2x16_split_y_flush_to_zero(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_half_2x16_split_y_flush_to_zero, src0);
}
static inline nir_ssa_def *
nir_unpack_snorm_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_snorm_2x16, src0);
}
static inline nir_ssa_def *
nir_unpack_snorm_4x8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_snorm_4x8, src0);
}
static inline nir_ssa_def *
nir_unpack_unorm_2x16(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_unorm_2x16, src0);
}
static inline nir_ssa_def *
nir_unpack_unorm_4x8(nir_builder *build, nir_ssa_def *src0)
{
   return nir_build_alu1(build, nir_op_unpack_unorm_4x8, src0);
}
static inline nir_ssa_def *
nir_urhadd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_urhadd, src0, src1);
}
static inline nir_ssa_def *
nir_urol(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_urol, src0, src1);
}
static inline nir_ssa_def *
nir_uror(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_uror, src0, src1);
}
static inline nir_ssa_def *
nir_usadd_4x8_vc4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_usadd_4x8_vc4, src0, src1);
}
static inline nir_ssa_def *
nir_ushr(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ushr, src0, src1);
}
static inline nir_ssa_def *
nir_ussub_4x8_vc4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_ussub_4x8_vc4, src0, src1);
}
static inline nir_ssa_def *
nir_usub_borrow(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_usub_borrow, src0, src1);
}
static inline nir_ssa_def *
nir_usub_sat(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_usub_sat, src0, src1);
}
static inline nir_ssa_def *
nir_vec16(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5, nir_ssa_def *src6, nir_ssa_def *src7, nir_ssa_def *src8, nir_ssa_def *src9, nir_ssa_def *src10, nir_ssa_def *src11, nir_ssa_def *src12, nir_ssa_def *src13, nir_ssa_def *src14, nir_ssa_def *src15)
{
   nir_ssa_def *srcs[16] = {src0, src1, src2, src3, src4, src5, src6, src7, src8, src9, src10, src11, src12, src13, src14, src15};
   return nir_build_alu_src_arr(build, nir_op_vec16, srcs);
}
static inline nir_ssa_def *
nir_vec2(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   return nir_build_alu2(build, nir_op_vec2, src0, src1);
}
static inline nir_ssa_def *
nir_vec3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   return nir_build_alu3(build, nir_op_vec3, src0, src1, src2);
}
static inline nir_ssa_def *
nir_vec4(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3)
{
   return nir_build_alu4(build, nir_op_vec4, src0, src1, src2, src3);
}
static inline nir_ssa_def *
nir_vec5(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4)
{
   nir_ssa_def *srcs[5] = {src0, src1, src2, src3, src4};
   return nir_build_alu_src_arr(build, nir_op_vec5, srcs);
}
static inline nir_ssa_def *
nir_vec8(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5, nir_ssa_def *src6, nir_ssa_def *src7)
{
   nir_ssa_def *srcs[8] = {src0, src1, src2, src3, src4, src5, src6, src7};
   return nir_build_alu_src_arr(build, nir_op_vec8, srcs);
}

struct _nir_accept_ray_intersection_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_addr_mode_is_indices {
   int _; /* exists to avoid empty initializers */
   nir_variable_mode memory_modes;
};
struct _nir_alloc_vertices_and_primitives_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_add_gen_prim_count_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_atomic_add_gs_emit_prim_count_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_add_gs_invocation_count_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_add_xfb_prim_count_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_atomic_counter_add_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_add_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_and_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_and_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_comp_swap_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_exchange_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_exchange_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_inc_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_inc_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_max_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_max_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_min_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_min_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_or_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_or_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_post_dec_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_post_dec_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_pre_dec_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_pre_dec_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_read_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_read_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_atomic_counter_xor_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range_base;
};
struct _nir_atomic_counter_xor_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_bit_count_exclusive_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_bit_count_inclusive_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_bit_count_reduce_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_bitfield_extract_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_find_lsb_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ballot_find_msb_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_begin_invocation_interlock_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_bindless_image_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_dec_wrap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_inc_wrap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_descriptor_amd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_format_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_fragment_mask_load_amd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_load_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   nir_alu_type dest_type;
};
struct _nir_bindless_image_load_raw_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_order_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_samples_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_samples_identical_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_size_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_image_sparse_load_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   nir_alu_type dest_type;
};
struct _nir_bindless_image_store_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   nir_alu_type src_type;
};
struct _nir_bindless_image_store_raw_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_bindless_resource_ir3_indices {
   int _; /* exists to avoid empty initializers */
   unsigned desc_set;
};
struct _nir_block_image_store_agx_indices {
   int _; /* exists to avoid empty initializers */
   enum pipe_format format;
   enum glsl_sampler_dim image_dim;
};
struct _nir_btd_retire_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_btd_spawn_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_btd_stack_push_intel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stack_size;
};
struct _nir_buffer_atomic_add_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_bvh64_intersect_ray_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_cond_end_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_control_barrier_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_convert_alu_types_indices {
   int _; /* exists to avoid empty initializers */
   nir_alu_type src_type;
   nir_alu_type dest_type;
   nir_rounding_mode rounding_mode;
   unsigned saturate;
};
struct _nir_copy_deref_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier dst_access;
   enum gl_access_qualifier src_access;
};
struct _nir_copy_ubo_to_uniform_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
};
struct _nir_demote_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_demote_if_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_deref_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_fcomp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_buffer_array_length_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_deref_mode_is_indices {
   int _; /* exists to avoid empty initializers */
   nir_variable_mode memory_modes;
};
struct _nir_discard_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_discard_if_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_elect_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_emit_vertex_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_emit_vertex_with_counter_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_end_invocation_interlock_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_end_patch_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_end_primitive_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_end_primitive_with_counter_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_exclusive_scan_indices {
   int _; /* exists to avoid empty initializers */
   unsigned reduction_op;
};
struct _nir_execute_callable_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_execute_closest_hit_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_execute_miss_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_export_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned flags;
};
struct _nir_first_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_gds_atomic_add_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_get_ssbo_size_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_get_ubo_size_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_add_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_add_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_add_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_and_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_and_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_and_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_comp_swap_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_comp_swap_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_comp_swap_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_exchange_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_exchange_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_exchange_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fadd_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fadd_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_fcomp_swap_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fcomp_swap_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fcomp_swap_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fmax_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fmax_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fmin_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_fmin_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_imax_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_imax_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_imax_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_imin_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_imin_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_imin_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_or_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_or_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_or_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_umax_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_umax_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_umax_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_umin_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_umin_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_umin_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_xor_2x32_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_global_atomic_xor_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_global_atomic_xor_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_group_memory_barrier_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ignore_ray_intersection_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_image_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_dec_wrap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_inc_wrap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_deref_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_dec_wrap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_inc_wrap_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_descriptor_amd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_format_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_fragment_mask_load_amd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_load_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   nir_alu_type dest_type;
};
struct _nir_image_deref_load_param_intel_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_image_deref_load_raw_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_order_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_samples_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_samples_identical_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_size_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_deref_sparse_load_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   nir_alu_type dest_type;
};
struct _nir_image_deref_store_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   nir_alu_type src_type;
};
struct _nir_image_deref_store_raw_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
};
struct _nir_image_descriptor_amd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_format_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_fragment_mask_load_amd_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_load_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
   nir_alu_type dest_type;
};
struct _nir_image_load_raw_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_order_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_samples_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_samples_identical_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_size_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_image_sparse_load_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
   nir_alu_type dest_type;
};
struct _nir_image_store_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
   nir_alu_type src_type;
};
struct _nir_image_store_raw_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum glsl_sampler_dim image_dim;
   bool image_array;
   enum pipe_format format;
   enum gl_access_qualifier access;
   unsigned range_base;
};
struct _nir_inclusive_scan_indices {
   int _; /* exists to avoid empty initializers */
   unsigned reduction_op;
};
struct _nir_interp_deref_at_centroid_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_interp_deref_at_offset_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_interp_deref_at_sample_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_interp_deref_at_vertex_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_is_helper_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_is_sparse_texels_resident_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_is_subgroup_invocation_lt_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_lane_permute_16_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_last_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_launch_mesh_workgroups_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
};
struct _nir_launch_mesh_workgroups_with_payload_deref_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_aa_line_width_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_accel_struct_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_agx_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   int base;
   enum pipe_format format;
   bool sign_extend;
};
struct _nir_load_alpha_reference_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_back_face_agx_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_barycentric_at_offset_indices {
   int _; /* exists to avoid empty initializers */
   unsigned interp_mode;
};
struct _nir_load_barycentric_at_sample_indices {
   int _; /* exists to avoid empty initializers */
   unsigned interp_mode;
};
struct _nir_load_barycentric_centroid_indices {
   int _; /* exists to avoid empty initializers */
   unsigned interp_mode;
};
struct _nir_load_barycentric_model_indices {
   int _; /* exists to avoid empty initializers */
   unsigned interp_mode;
};
struct _nir_load_barycentric_pixel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned interp_mode;
};
struct _nir_load_barycentric_sample_indices {
   int _; /* exists to avoid empty initializers */
   unsigned interp_mode;
};
struct _nir_load_base_global_invocation_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_base_instance_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_base_vertex_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_base_workgroup_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_a_float_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_aaaa8888_unorm_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_b_float_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_g_float_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_r_float_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_rgba_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_blend_const_color_rgba8888_unorm_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_btd_global_arg_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_btd_local_arg_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_btd_resume_sbt_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_btd_shader_type_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_btd_stack_id_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_buffer_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   nir_variable_mode memory_modes;
   enum gl_access_qualifier access;
};
struct _nir_load_callable_sbt_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_callable_sbt_stride_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_clamp_vertex_color_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_clip_half_line_width_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_color0_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_color1_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_constant_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_constant_agx_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   int base;
   enum pipe_format format;
   bool sign_extend;
};
struct _nir_load_constant_base_ptr_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_converted_output_pan_indices {
   int _; /* exists to avoid empty initializers */
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_coverage_mask_pan_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_any_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_back_face_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_ccw_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_front_face_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_mask_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_mask_and_flags_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_small_prim_precision_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_cull_small_primitives_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_deref_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_load_deref_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_load_desc_set_address_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_draw_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_esgs_vertex_stride_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_fb_layers_v3d_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_first_vertex_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_force_vrs_rates_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_frag_coord_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_frag_invocation_count_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_frag_shading_rate_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_frag_size_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_front_face_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_fs_input_interp_deltas_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_fully_covered_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_global_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_2x32_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_base_ptr_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_global_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_const_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_global_constant_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_constant_bounded_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_constant_offset_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_global_invocation_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_global_invocation_id_zero_base_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_global_invocation_index_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_global_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_gs_header_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_gs_vertex_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_helper_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_hit_attrib_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_hs_out_patch_data_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_hs_patch_stride_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_initial_edgeflags_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_input_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_input_vertex_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_instance_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_interpolated_input_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_intersection_opaque_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_invocation_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_is_indexed_draw_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_kernel_input_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_layer_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_lds_ngg_gs_out_vertex_base_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_lds_ngg_scratch_base_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_leaf_opaque_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_leaf_procedural_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_line_coord_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_line_width_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_local_invocation_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_local_invocation_index_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_local_pixel_agx_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   enum pipe_format format;
};
struct _nir_load_local_shared_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_lshs_vertex_stride_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_merged_wave_info_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_mesh_inline_data_intel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned align_offset;
};
struct _nir_load_mesh_view_count_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_mesh_view_indices_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
};
struct _nir_load_multisampled_pan_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_num_subgroups_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_num_vertices_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_num_vertices_per_primitive_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_num_workgroups_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ordered_id_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_output_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_packed_passthrough_primitive_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_param_indices {
   int _; /* exists to avoid empty initializers */
   unsigned param_idx;
};
struct _nir_load_patch_vertices_in_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_per_primitive_output_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_per_vertex_input_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_per_vertex_output_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type dest_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_load_persp_center_rhw_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_pipeline_stat_query_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_point_coord_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_point_coord_maybe_flipped_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_preamble_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_prim_gen_query_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_prim_xfb_query_enabled_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_primitive_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_primitive_location_ir3_indices {
   int _; /* exists to avoid empty initializers */
   unsigned driver_location;
};
struct _nir_load_printf_buffer_address_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_provoking_vtx_in_prim_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ptr_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_push_constant_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_rasterization_samples_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_raw_output_pan_indices {
   int _; /* exists to avoid empty initializers */
   struct nir_io_semantics io_semantics;
};
struct _nir_load_ray_base_mem_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_flags_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_geometry_index_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_hit_kind_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_hit_sbt_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_hit_sbt_stride_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_hw_stack_size_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_instance_custom_index_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_launch_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_launch_size_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_launch_size_addr_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_miss_sbt_addr_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_miss_sbt_stride_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_num_dss_rt_stacks_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_object_direction_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_object_origin_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_object_to_world_indices {
   int _; /* exists to avoid empty initializers */
   unsigned column;
};
struct _nir_load_ray_query_global_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_sw_stack_size_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_t_max_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_t_min_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_world_direction_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_world_origin_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ray_world_to_object_indices {
   int _; /* exists to avoid empty initializers */
   unsigned column;
};
struct _nir_load_rel_patch_id_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_reloc_const_intel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned param_idx;
};
struct _nir_load_ring_attr_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_attr_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_es2gs_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_esgs_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_gs2vs_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_gsvs_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_load_ring_mesh_scratch_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_mesh_scratch_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_task_draw_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_task_payload_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_tess_factors_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_tess_factors_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_tess_offchip_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ring_tess_offchip_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_rt_arg_scratch_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_rt_conversion_pan_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   nir_alu_type src_type;
};
struct _nir_load_rt_dynamic_callable_stack_base_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_id_no_per_sample_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_mask_in_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_pos_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_pos_from_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_pos_or_center_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_positions_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sample_positions_pan_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sampler_lod_parameters_pan_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sbt_base_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sbt_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_sbt_stride_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_scalar_arg_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned arg_upper_bound_u32_amd;
};
struct _nir_load_scratch_indices {
   int _; /* exists to avoid empty initializers */
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_scratch_base_ptr_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_scratch_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_shader_record_ptr_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_shared_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_shared2_amd_indices {
   int _; /* exists to avoid empty initializers */
   uint8_t offset0;
   uint8_t offset1;
   bool st64;
};
struct _nir_load_shared_base_ptr_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_shared_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_shared_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_shared_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_shared_uniform_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_simd_width_intel_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_smem_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_smem_buffer_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_ssbo_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_ssbo_address_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ssbo_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_ssbo_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_ssbo_uniform_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_stack_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
   unsigned call_idx;
   unsigned value_id;
};
struct _nir_load_streamout_buffer_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_streamout_config_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_streamout_offset_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_streamout_write_index_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_eq_mask_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_ge_mask_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_gt_mask_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_id_shift_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_le_mask_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_lt_mask_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_subgroup_size_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_task_payload_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_task_ring_entry_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tcs_header_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tcs_in_param_base_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tcs_num_patches_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tcs_out_param_base_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tcs_rel_patch_id_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tcs_tess_factor_base_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_coord_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_coord_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_factor_base_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_level_inner_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_level_inner_default_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_level_outer_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_level_outer_default_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_param_base_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tess_rel_patch_id_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_texture_base_agx_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_texture_rect_scaling_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_tlb_color_v3d_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
};
struct _nir_load_topology_id_intel_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_load_typed_buffer_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   nir_variable_mode memory_modes;
   enum gl_access_qualifier access;
   enum pipe_format format;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_load_ubo_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
   unsigned range_base;
   unsigned range;
};
struct _nir_load_ubo_base_agx_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ubo_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_ubo_vec4_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   int base;
   unsigned component;
};
struct _nir_load_uniform_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned range;
   nir_alu_type dest_type;
};
struct _nir_load_user_clip_plane_indices {
   int _; /* exists to avoid empty initializers */
   unsigned ucp_id;
};
struct _nir_load_user_data_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_vbo_base_agx_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_vector_arg_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned arg_upper_bound_u32_amd;
};
struct _nir_load_vertex_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_vertex_id_zero_base_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_view_index_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_offset_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_scale_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_x_offset_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_x_scale_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_xy_scale_and_offset_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_y_offset_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_y_scale_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_z_offset_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_viewport_z_scale_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_vs_primitive_stride_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_vs_vertex_stride_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_vulkan_descriptor_indices {
   int _; /* exists to avoid empty initializers */
   unsigned desc_type;
};
struct _nir_load_work_dim_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_workgroup_id_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_workgroup_id_zero_base_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_workgroup_index_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_workgroup_num_input_primitives_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_workgroup_num_input_vertices_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_workgroup_size_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_load_xfb_address_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_masked_swizzle_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned swizzle_mask;
};
struct _nir_mbcnt_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_memcpy_deref_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier dst_access;
   enum gl_access_qualifier src_access;
};
struct _nir_memory_barrier_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_memory_barrier_atomic_counter_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_memory_barrier_buffer_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_memory_barrier_image_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_memory_barrier_shared_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_memory_barrier_tcs_patch_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_nop_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_optimization_barrier_vgpr_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ordered_xfb_counter_add_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
};
struct _nir_overwrite_tes_arguments_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_overwrite_vs_arguments_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_preamble_end_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_preamble_start_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_printf_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_quad_broadcast_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_quad_swap_diagonal_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_quad_swap_horizontal_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_quad_swap_vertical_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_quad_swizzle_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned swizzle_mask;
};
struct _nir_read_first_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_read_invocation_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_read_invocation_cond_ir3_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_reduce_indices {
   int _; /* exists to avoid empty initializers */
   unsigned reduction_op;
   unsigned cluster_size;
};
struct _nir_report_ray_intersection_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rotate_indices {
   int _; /* exists to avoid empty initializers */
   nir_scope execution_scope;
   unsigned cluster_size;
};
struct _nir_rq_confirm_intersection_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rq_generate_intersection_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rq_initialize_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rq_load_indices {
   int _; /* exists to avoid empty initializers */
   nir_ray_query_value ray_query_value;
   unsigned column;
};
struct _nir_rq_proceed_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rq_terminate_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rt_execute_callable_indices {
   int _; /* exists to avoid empty initializers */
   unsigned call_idx;
   unsigned stack_size;
};
struct _nir_rt_resume_indices {
   int _; /* exists to avoid empty initializers */
   unsigned call_idx;
   unsigned stack_size;
};
struct _nir_rt_return_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_rt_trace_ray_indices {
   int _; /* exists to avoid empty initializers */
   unsigned call_idx;
   unsigned stack_size;
};
struct _nir_scoped_barrier_indices {
   int _; /* exists to avoid empty initializers */
   nir_scope execution_scope;
   nir_scope memory_scope;
   nir_memory_semantics memory_semantics;
   nir_variable_mode memory_modes;
};
struct _nir_set_vertex_and_primitive_count_indices {
   int _; /* exists to avoid empty initializers */
   unsigned stream_id;
};
struct _nir_shader_clock_indices {
   int _; /* exists to avoid empty initializers */
   nir_scope memory_scope;
};
struct _nir_shared_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_add_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_and_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_comp_swap_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_exchange_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_fcomp_swap_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_imax_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_imin_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_or_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_umax_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_umin_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shared_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_shared_atomic_xor_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shuffle_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shuffle_down_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shuffle_up_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_shuffle_xor_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_sparse_residency_code_and_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_ssbo_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_add_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_and_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_comp_swap_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_exchange_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_fcomp_swap_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_imax_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_imin_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_or_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_umax_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_umin_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_ssbo_atomic_xor_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
};
struct _nir_store_agx_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   int base;
   enum pipe_format format;
   bool sign_extend;
};
struct _nir_store_buffer_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   nir_variable_mode memory_modes;
   enum gl_access_qualifier access;
};
struct _nir_store_combined_output_pan_indices {
   int _; /* exists to avoid empty initializers */
   struct nir_io_semantics io_semantics;
   unsigned component;
   nir_alu_type src_type;
   nir_alu_type dest_type;
};
struct _nir_store_deref_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
};
struct _nir_store_deref_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
};
struct _nir_store_global_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_global_2x32_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_global_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
   unsigned write_mask;
};
struct _nir_store_global_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_global_ir3_indices {
   int _; /* exists to avoid empty initializers */
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_hit_attrib_amd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_store_local_pixel_agx_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   enum pipe_format format;
};
struct _nir_store_local_shared_r600_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
};
struct _nir_store_output_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned component;
   nir_alu_type src_type;
   struct nir_io_semantics io_semantics;
   struct nir_io_xfb io_xfb;
   struct nir_io_xfb io_xfb2;
};
struct _nir_store_per_primitive_output_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned component;
   nir_alu_type src_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_store_per_vertex_output_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned component;
   nir_alu_type src_type;
   struct nir_io_semantics io_semantics;
};
struct _nir_store_preamble_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_store_raw_output_pan_indices {
   int _; /* exists to avoid empty initializers */
   struct nir_io_semantics io_semantics;
   int base;
};
struct _nir_store_scratch_indices {
   int _; /* exists to avoid empty initializers */
   unsigned align_mul;
   unsigned align_offset;
   unsigned write_mask;
};
struct _nir_store_scratch_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_store_shared_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_shared2_amd_indices {
   int _; /* exists to avoid empty initializers */
   uint8_t offset0;
   uint8_t offset1;
   bool st64;
};
struct _nir_store_shared_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_shared_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_store_shared_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_shared_masked_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_store_ssbo_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_ssbo_block_intel_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_ssbo_ir3_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
   enum gl_access_qualifier access;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_ssbo_masked_dxil_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_store_stack_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned align_mul;
   unsigned align_offset;
   unsigned write_mask;
   unsigned call_idx;
   unsigned value_id;
};
struct _nir_store_task_payload_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned write_mask;
   unsigned align_mul;
   unsigned align_offset;
};
struct _nir_store_tf_r600_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_store_tlb_sample_color_v3d_indices {
   int _; /* exists to avoid empty initializers */
   int base;
   unsigned component;
   nir_alu_type src_type;
};
struct _nir_store_uniform_ir3_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_store_zs_agx_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_add_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_and_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_comp_swap_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_exchange_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_fadd_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_fcomp_swap_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_fmax_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_fmin_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_imax_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_imin_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_or_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_umax_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_umin_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_task_payload_atomic_xor_indices {
   int _; /* exists to avoid empty initializers */
   int base;
};
struct _nir_terminate_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_terminate_if_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_terminate_ray_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_trace_ray_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_trace_ray_intel_indices {
   int _; /* exists to avoid empty initializers */
   bool synchronous;
};
struct _nir_vote_all_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_vote_any_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_vote_feq_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_vote_ieq_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_vulkan_resource_index_indices {
   int _; /* exists to avoid empty initializers */
   unsigned desc_set;
   unsigned binding;
   unsigned desc_type;
};
struct _nir_vulkan_resource_reindex_indices {
   int _; /* exists to avoid empty initializers */
   unsigned desc_type;
};
struct _nir_write_invocation_amd_indices {
   int _; /* exists to avoid empty initializers */
};
struct _nir_xfb_counter_sub_amd_indices {
   int _; /* exists to avoid empty initializers */
   unsigned write_mask;
};



static inline nir_intrinsic_instr *
_nir_build_accept_ray_intersection(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_accept_ray_intersection);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_addr_mode_is(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_addr_mode_is_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_addr_mode_is);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_memory_modes(intrin, indices.memory_modes);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_alloc_vertices_and_primitives_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_alloc_vertices_and_primitives_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_atomic_add_gen_prim_count_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_atomic_add_gen_prim_count_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_add_gen_prim_count_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_atomic_add_gs_emit_prim_count_amd(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_add_gs_emit_prim_count_amd);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_atomic_add_gs_invocation_count_amd(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_add_gs_invocation_count_amd);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_atomic_add_xfb_prim_count_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_atomic_add_xfb_prim_count_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_add_xfb_prim_count_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_add_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_add_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_and_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_and_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_atomic_counter_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_comp_swap_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_comp_swap_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_exchange_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_exchange_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_inc(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_atomic_counter_inc_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_inc);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_inc_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_inc_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_max(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_max_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_max);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_max_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_max_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_min(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_min_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_min);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_min_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_min_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_or_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_or_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_post_dec(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_atomic_counter_post_dec_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_post_dec);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_post_dec_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_post_dec_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_pre_dec(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_atomic_counter_pre_dec_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_pre_dec);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_pre_dec_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_pre_dec_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_read(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_atomic_counter_read_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_read);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_read_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_read_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_atomic_counter_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_atomic_counter_xor_deref(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_atomic_counter_xor_deref);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot_bit_count_exclusive(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot_bit_count_exclusive);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot_bit_count_inclusive(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot_bit_count_inclusive);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot_bit_count_reduce(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot_bit_count_reduce);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot_bitfield_extract(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot_bitfield_extract);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot_find_lsb(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot_find_lsb);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ballot_find_msb(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ballot_find_msb);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_begin_invocation_interlock(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_begin_invocation_interlock);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_bindless_image_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_dec_wrap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_dec_wrap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_dec_wrap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_inc_wrap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_inc_wrap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_inc_wrap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_descriptor_amd(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_bindless_image_descriptor_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_descriptor_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_format(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_bindless_image_format_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_format);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_fragment_mask_load_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_bindless_image_fragment_mask_load_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_fragment_mask_load_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_load_raw_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_bindless_image_load_raw_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_load_raw_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_order(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_bindless_image_order_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_order);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_samples(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_bindless_image_samples_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_samples);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_samples_identical(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_bindless_image_samples_identical_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_samples_identical);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_size(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_bindless_image_size_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_size);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bindless_image_sparse_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_bindless_image_sparse_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_sparse_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_bindless_image_store(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_bindless_image_store_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_store);

   intrin->num_components = src3->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_src_type(intrin, indices.src_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_bindless_image_store_raw_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_bindless_image_store_raw_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_image_store_raw_intel);

   intrin->num_components = src2->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_bindless_resource_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_bindless_resource_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bindless_resource_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_desc_set(intrin, indices.desc_set);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_block_image_store_agx(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_block_image_store_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_block_image_store_agx);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_btd_retire_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_btd_retire_intel);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_btd_spawn_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_btd_spawn_intel);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_btd_stack_push_intel(nir_builder *build, struct _nir_btd_stack_push_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_btd_stack_push_intel);

   nir_intrinsic_set_stack_size(intrin, indices.stack_size);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_buffer_atomic_add_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_buffer_atomic_add_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_buffer_atomic_add_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_bvh64_intersect_ray_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_bvh64_intersect_ray_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   intrin->src[5] = nir_src_for_ssa(src5);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_cond_end_ir3(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_cond_end_ir3);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_control_barrier(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_control_barrier);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_convert_alu_types(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_convert_alu_types_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_convert_alu_types);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_src_type(intrin, indices.src_type);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_rounding_mode(intrin, indices.rounding_mode);
   nir_intrinsic_set_saturate(intrin, indices.saturate);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_copy_deref(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_copy_deref_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_copy_deref);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_dst_access(intrin, indices.dst_access);
   nir_intrinsic_set_src_access(intrin, indices.src_access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_copy_ubo_to_uniform_ir3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_copy_ubo_to_uniform_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_copy_ubo_to_uniform_ir3);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_demote(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_demote);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_demote_if(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_demote_if);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_deref_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_fcomp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_deref_atomic_fcomp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_fcomp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_deref_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_buffer_array_length(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_deref_buffer_array_length_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_buffer_array_length);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_deref_mode_is(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_deref_mode_is_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_deref_mode_is);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_memory_modes(intrin, indices.memory_modes);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_discard(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_discard);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_discard_if(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_discard_if);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_elect(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_elect);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_emit_vertex(nir_builder *build, struct _nir_emit_vertex_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_emit_vertex);

   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_emit_vertex_with_counter(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_emit_vertex_with_counter_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_emit_vertex_with_counter);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_end_invocation_interlock(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_end_invocation_interlock);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_end_patch_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_end_patch_ir3);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_end_primitive(nir_builder *build, struct _nir_end_primitive_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_end_primitive);

   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_end_primitive_with_counter(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_end_primitive_with_counter_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_end_primitive_with_counter);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_exclusive_scan(nir_builder *build, nir_ssa_def *src0, struct _nir_exclusive_scan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_exclusive_scan);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_reduction_op(intrin, indices.reduction_op);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_execute_callable(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_execute_callable);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_execute_closest_hit_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_execute_closest_hit_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   intrin->src[5] = nir_src_for_ssa(src5);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_execute_miss_amd(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_execute_miss_amd);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_export_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_export_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_export_amd);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_flags(intrin, indices.flags);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_first_invocation(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_first_invocation);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_gds_atomic_add_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_gds_atomic_add_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_gds_atomic_add_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_get_ssbo_size(nir_builder *build, nir_ssa_def *src0, struct _nir_get_ssbo_size_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_get_ssbo_size);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_get_ubo_size(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_get_ubo_size);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_add_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_add_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_add_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_add_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_add_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_add_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_add_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_add_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_and_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_and_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_and_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_and_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_and_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_and_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_and_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_and_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_comp_swap_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_comp_swap_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_comp_swap_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_global_atomic_comp_swap_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_comp_swap_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_comp_swap_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_comp_swap_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_comp_swap_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_exchange_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_exchange_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_exchange_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_exchange_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_exchange_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_exchange_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_exchange_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_exchange_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fadd_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fadd_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fadd_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_fadd_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fadd_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fcomp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fcomp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fcomp_swap_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fcomp_swap_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fcomp_swap_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_global_atomic_fcomp_swap_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fcomp_swap_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fmax_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fmax_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fmax_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_fmax_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fmax_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fmin_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fmin_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_fmin_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_fmin_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_fmin_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imax_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imax_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imax_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_imax_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imax_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imax_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_imax_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imax_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imin_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imin_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imin_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_imin_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imin_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_imin_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_imin_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_imin_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_or_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_or_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_or_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_or_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_or_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_or_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_or_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_or_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umax_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umax_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umax_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_umax_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umax_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umax_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_umax_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umax_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umin_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umin_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umin_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_umin_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umin_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_umin_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_umin_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_umin_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_xor_2x32(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_xor_2x32);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_xor_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_global_atomic_xor_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_xor_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_global_atomic_xor_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_global_atomic_xor_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_global_atomic_xor_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_group_memory_barrier(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_group_memory_barrier);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_ignore_ray_intersection(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ignore_ray_intersection);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_image_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_image_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_dec_wrap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_dec_wrap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_dec_wrap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_inc_wrap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_inc_wrap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_inc_wrap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_image_deref_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_dec_wrap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_dec_wrap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_dec_wrap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_inc_wrap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_inc_wrap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_inc_wrap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_descriptor_amd(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_deref_descriptor_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_descriptor_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_format(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_deref_format_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_format);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_fragment_mask_load_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_deref_fragment_mask_load_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_fragment_mask_load_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_load_param_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_deref_load_param_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_load_param_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_load_raw_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_deref_load_raw_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_load_raw_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_order(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_deref_order_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_order);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_samples(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_deref_samples_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_samples);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_samples_identical(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_deref_samples_identical_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_samples_identical);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_size(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_deref_size_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_size);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_deref_sparse_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_deref_sparse_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_sparse_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_image_deref_store(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_image_deref_store_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_store);

   intrin->num_components = src3->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_src_type(intrin, indices.src_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_image_deref_store_raw_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_image_deref_store_raw_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_deref_store_raw_intel);

   intrin->num_components = src2->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_image_descriptor_amd(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_descriptor_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_descriptor_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_format(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_format_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_format);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_fragment_mask_load_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_fragment_mask_load_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_fragment_mask_load_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_load_raw_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_load_raw_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_load_raw_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_order(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_order_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_order);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_samples(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_image_samples_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_samples);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_samples_identical(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_samples_identical_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_samples_identical);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_size(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_image_size_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_size);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_image_sparse_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_image_sparse_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_sparse_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_image_store(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_image_store_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_store);

   intrin->num_components = src3->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);
   nir_intrinsic_set_src_type(intrin, indices.src_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_image_store_raw_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_image_store_raw_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_image_store_raw_intel);

   intrin->num_components = src2->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_image_dim(intrin, indices.image_dim);
   nir_intrinsic_set_image_array(intrin, indices.image_array);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_range_base(intrin, indices.range_base);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_inclusive_scan(nir_builder *build, nir_ssa_def *src0, struct _nir_inclusive_scan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_inclusive_scan);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_reduction_op(intrin, indices.reduction_op);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_interp_deref_at_centroid(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_interp_deref_at_centroid);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_interp_deref_at_offset(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_interp_deref_at_offset);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_interp_deref_at_sample(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_interp_deref_at_sample);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_interp_deref_at_vertex(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_interp_deref_at_vertex);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_is_helper_invocation(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_is_helper_invocation);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_is_sparse_texels_resident(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_is_sparse_texels_resident);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_is_subgroup_invocation_lt_amd(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_is_subgroup_invocation_lt_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_lane_permute_16_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_lane_permute_16_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_last_invocation(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_last_invocation);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_launch_mesh_workgroups(nir_builder *build, nir_ssa_def *src0, struct _nir_launch_mesh_workgroups_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_launch_mesh_workgroups);

   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_launch_mesh_workgroups_with_payload_deref(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_launch_mesh_workgroups_with_payload_deref);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_load_aa_line_width(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_aa_line_width);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_accel_struct_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_accel_struct_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_agx(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_agx);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_sign_extend(intrin, indices.sign_extend);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_alpha_reference_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_alpha_reference_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_back_face_agx(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_back_face_agx);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_barycentric_at_offset(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_barycentric_at_offset_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_barycentric_at_offset);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_interp_mode(intrin, indices.interp_mode);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_barycentric_at_sample(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_barycentric_at_sample_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_barycentric_at_sample);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_interp_mode(intrin, indices.interp_mode);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_barycentric_centroid(nir_builder *build, unsigned bit_size, struct _nir_load_barycentric_centroid_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_barycentric_centroid);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   nir_intrinsic_set_interp_mode(intrin, indices.interp_mode);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_barycentric_model(nir_builder *build, unsigned bit_size, struct _nir_load_barycentric_model_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_barycentric_model);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);
   nir_intrinsic_set_interp_mode(intrin, indices.interp_mode);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_barycentric_pixel(nir_builder *build, unsigned bit_size, struct _nir_load_barycentric_pixel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_barycentric_pixel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   nir_intrinsic_set_interp_mode(intrin, indices.interp_mode);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_barycentric_sample(nir_builder *build, unsigned bit_size, struct _nir_load_barycentric_sample_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_barycentric_sample);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   nir_intrinsic_set_interp_mode(intrin, indices.interp_mode);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_base_global_invocation_id(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_base_global_invocation_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_base_instance(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_base_instance);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_base_vertex(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_base_vertex);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_base_workgroup_id(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_base_workgroup_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_a_float(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_a_float);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_aaaa8888_unorm(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_aaaa8888_unorm);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_b_float(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_b_float);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_g_float(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_g_float);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_r_float(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_r_float);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_rgba(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_rgba);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_blend_const_color_rgba8888_unorm(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_blend_const_color_rgba8888_unorm);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_btd_global_arg_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_btd_global_arg_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_btd_local_arg_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_btd_local_arg_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_btd_resume_sbt_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_btd_resume_sbt_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_btd_shader_type_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_btd_shader_type_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_btd_stack_id_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_btd_stack_id_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_buffer_amd(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_load_buffer_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_buffer_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_memory_modes(intrin, indices.memory_modes);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_callable_sbt_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_callable_sbt_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_callable_sbt_stride_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_callable_sbt_stride_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 16, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_clamp_vertex_color_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_clamp_vertex_color_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_clip_half_line_width_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_clip_half_line_width_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_color0(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_color0);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_color1(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_color1);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_constant(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_constant_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_constant);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_constant_agx(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_constant_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_constant_agx);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_sign_extend(intrin, indices.sign_extend);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_constant_base_ptr(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_constant_base_ptr);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_converted_output_pan(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_converted_output_pan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_converted_output_pan);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_coverage_mask_pan(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_coverage_mask_pan);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_any_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_any_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_back_face_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_back_face_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_ccw_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_ccw_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_front_face_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_front_face_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_mask(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_mask);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_mask_and_flags_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_mask_and_flags_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_small_prim_precision_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_small_prim_precision_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_cull_small_primitives_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_cull_small_primitives_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_deref(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_deref_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_deref);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_deref_block_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_deref_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_deref_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_desc_set_address_intel(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_desc_set_address_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_draw_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_draw_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_esgs_vertex_stride_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_esgs_vertex_stride_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_fb_layers_v3d(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_fb_layers_v3d);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_first_vertex(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_first_vertex);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_force_vrs_rates_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_force_vrs_rates_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_frag_coord(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_frag_coord);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_frag_invocation_count(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_frag_invocation_count);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_frag_shading_rate(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_frag_shading_rate);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_frag_size(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_frag_size);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_front_face(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_front_face);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_fs_input_interp_deltas(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_fs_input_interp_deltas_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_fs_input_interp_deltas);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_fully_covered(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_fully_covered);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_global_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_2x32(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_global_2x32_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_2x32);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_amd(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_global_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_base_ptr(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_base_ptr);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_block_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_global_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_const_block_intel(nir_builder *build, unsigned num_components, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_global_const_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_const_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_constant(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_global_constant_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_constant);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_constant_bounded(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_load_global_constant_bounded_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_constant_bounded);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_constant_offset(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_global_constant_offset_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_constant_offset);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_invocation_id(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_invocation_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_invocation_id_zero_base(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_invocation_id_zero_base);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_invocation_index(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_invocation_index);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_global_ir3(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_global_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_global_ir3);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_gs_header_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_gs_header_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_gs_vertex_offset_amd(nir_builder *build, struct _nir_load_gs_vertex_offset_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_gs_vertex_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_helper_invocation(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_helper_invocation);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_hit_attrib_amd(nir_builder *build, struct _nir_load_hit_attrib_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_hit_attrib_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_hs_out_patch_data_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_hs_out_patch_data_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_hs_patch_stride_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_hs_patch_stride_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_initial_edgeflags_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_initial_edgeflags_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_input(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_input_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_input);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_input_vertex(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_input_vertex_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_input_vertex);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_instance_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_instance_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_interpolated_input(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_interpolated_input_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_interpolated_input);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_intersection_opaque_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_intersection_opaque_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_invocation_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_invocation_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_is_indexed_draw(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_is_indexed_draw);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_kernel_input(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_kernel_input_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_kernel_input);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_layer_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_layer_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_lds_ngg_gs_out_vertex_base_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_lds_ngg_gs_out_vertex_base_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_lds_ngg_scratch_base_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_lds_ngg_scratch_base_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_leaf_opaque_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_leaf_opaque_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_leaf_procedural_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_leaf_procedural_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_line_coord(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_line_coord);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_line_width(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_line_width);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_local_invocation_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_local_invocation_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_local_invocation_index(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_local_invocation_index);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_local_pixel_agx(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_local_pixel_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_local_pixel_agx);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_format(intrin, indices.format);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_local_shared_r600(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_local_shared_r600);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_lshs_vertex_stride_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_lshs_vertex_stride_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_merged_wave_info_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_merged_wave_info_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_mesh_inline_data_intel(nir_builder *build, unsigned bit_size, struct _nir_load_mesh_inline_data_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_mesh_inline_data_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_mesh_view_count(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_mesh_view_count);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_mesh_view_indices(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_mesh_view_indices_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_mesh_view_indices);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_multisampled_pan(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_multisampled_pan);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_num_subgroups(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_num_subgroups);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_num_vertices(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_num_vertices);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_num_vertices_per_primitive_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_num_vertices_per_primitive_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_num_workgroups(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_num_workgroups);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ordered_id_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ordered_id_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_output(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_output_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_output);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_packed_passthrough_primitive_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_packed_passthrough_primitive_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_param(nir_builder *build, unsigned num_components, unsigned bit_size, struct _nir_load_param_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_param);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   nir_intrinsic_set_param_idx(intrin, indices.param_idx);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_patch_vertices_in(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_patch_vertices_in);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_per_primitive_output(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_per_primitive_output_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_per_primitive_output);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_per_vertex_input(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_per_vertex_input_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_per_vertex_input);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_per_vertex_output(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_per_vertex_output_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_per_vertex_output);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_persp_center_rhw_ir3(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_persp_center_rhw_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_pipeline_stat_query_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_pipeline_stat_query_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_point_coord(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_point_coord);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_point_coord_maybe_flipped(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_point_coord_maybe_flipped);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_preamble(nir_builder *build, unsigned num_components, unsigned bit_size, struct _nir_load_preamble_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_preamble);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_prim_gen_query_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_prim_gen_query_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_prim_xfb_query_enabled_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_prim_xfb_query_enabled_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 1, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_primitive_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_primitive_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_primitive_location_ir3(nir_builder *build, struct _nir_load_primitive_location_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_primitive_location_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_driver_location(intrin, indices.driver_location);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_printf_buffer_address(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_printf_buffer_address);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_provoking_vtx_in_prim_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_provoking_vtx_in_prim_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ptr_dxil(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ptr_dxil);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_push_constant(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_push_constant_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_push_constant);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_rasterization_samples_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_rasterization_samples_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_raw_output_pan(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_raw_output_pan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_raw_output_pan);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_base_mem_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_base_mem_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_flags(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_flags);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_geometry_index(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_geometry_index);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_hit_kind(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_hit_kind);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_hit_sbt_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_hit_sbt_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_hit_sbt_stride_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_hit_sbt_stride_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 16, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_hw_stack_size_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_hw_stack_size_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_instance_custom_index(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_instance_custom_index);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_launch_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_launch_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_launch_size(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_launch_size);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_launch_size_addr_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_launch_size_addr_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_miss_sbt_addr_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_miss_sbt_addr_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_miss_sbt_stride_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_miss_sbt_stride_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 16, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_num_dss_rt_stacks_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_num_dss_rt_stacks_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_object_direction(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_object_direction);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_object_origin(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_object_origin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_object_to_world(nir_builder *build, struct _nir_load_ray_object_to_world_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_object_to_world);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);
   nir_intrinsic_set_column(intrin, indices.column);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_query_global_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_query_global_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_sw_stack_size_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_sw_stack_size_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_t_max(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_t_max);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_t_min(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_t_min);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_world_direction(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_world_direction);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_world_origin(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_world_origin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ray_world_to_object(nir_builder *build, struct _nir_load_ray_world_to_object_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ray_world_to_object);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);
   nir_intrinsic_set_column(intrin, indices.column);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_rel_patch_id_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_rel_patch_id_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_reloc_const_intel(nir_builder *build, struct _nir_load_reloc_const_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_reloc_const_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_param_idx(intrin, indices.param_idx);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_attr_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_attr_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_attr_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_attr_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_es2gs_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_es2gs_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_esgs_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_esgs_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_gs2vs_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_gs2vs_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_gsvs_amd(nir_builder *build, struct _nir_load_ring_gsvs_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_gsvs_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);
   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_mesh_scratch_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_mesh_scratch_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_mesh_scratch_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_mesh_scratch_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_task_draw_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_task_draw_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_task_payload_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_task_payload_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_tess_factors_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_tess_factors_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_tess_factors_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_tess_factors_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_tess_offchip_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_tess_offchip_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ring_tess_offchip_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ring_tess_offchip_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_rt_arg_scratch_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_rt_arg_scratch_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_rt_conversion_pan(nir_builder *build, struct _nir_load_rt_conversion_pan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_rt_conversion_pan);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_src_type(intrin, indices.src_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_rt_dynamic_callable_stack_base_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_rt_dynamic_callable_stack_base_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_id_no_per_sample(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_id_no_per_sample);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_mask_in(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_mask_in);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_pos(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_pos);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_pos_from_id(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_pos_from_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_pos_or_center(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_pos_or_center);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_positions_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_positions_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sample_positions_pan(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sample_positions_pan);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sampler_lod_parameters_pan(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sampler_lod_parameters_pan);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sbt_base_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sbt_base_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sbt_offset_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sbt_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_sbt_stride_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_sbt_stride_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_scalar_arg_amd(nir_builder *build, unsigned num_components, struct _nir_load_scalar_arg_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_scalar_arg_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_arg_upper_bound_u32_amd(intrin, indices.arg_upper_bound_u32_amd);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_scratch(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_scratch_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_scratch);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_scratch_base_ptr(nir_builder *build, unsigned num_components, unsigned bit_size, struct _nir_load_scratch_base_ptr_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_scratch_base_ptr);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_scratch_dxil(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_scratch_dxil);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shader_record_ptr(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shader_record_ptr);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_shared_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared2_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_shared2_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared2_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_offset0(intrin, indices.offset0);
   nir_intrinsic_set_offset1(intrin, indices.offset1);
   nir_intrinsic_set_st64(intrin, indices.st64);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared_base_ptr(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared_base_ptr);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared_block_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_shared_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared_dxil(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared_dxil);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared_ir3(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_shared_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared_ir3);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_shared_uniform_block_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_shared_uniform_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_shared_uniform_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_simd_width_intel(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_simd_width_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_smem_amd(nir_builder *build, unsigned num_components, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_smem_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_smem_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_smem_buffer_amd(nir_builder *build, unsigned num_components, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_smem_buffer_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_smem_buffer_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ssbo(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_ssbo_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ssbo);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ssbo_address(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ssbo_address);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ssbo_block_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_ssbo_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ssbo_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ssbo_ir3(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_load_ssbo_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ssbo_ir3);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ssbo_uniform_block_intel(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_ssbo_uniform_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ssbo_uniform_block_intel);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_stack(nir_builder *build, unsigned num_components, unsigned bit_size, struct _nir_load_stack_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_stack);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);
   nir_intrinsic_set_call_idx(intrin, indices.call_idx);
   nir_intrinsic_set_value_id(intrin, indices.value_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_streamout_buffer_amd(nir_builder *build, struct _nir_load_streamout_buffer_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_streamout_buffer_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_streamout_config_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_streamout_config_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_streamout_offset_amd(nir_builder *build, struct _nir_load_streamout_offset_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_streamout_offset_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_streamout_write_index_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_streamout_write_index_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_eq_mask(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_eq_mask);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_ge_mask(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_ge_mask);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_gt_mask(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_gt_mask);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_id_shift_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_id_shift_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_invocation(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_invocation);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_le_mask(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_le_mask);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_lt_mask(nir_builder *build, unsigned num_components, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_lt_mask);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_subgroup_size(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_subgroup_size);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_task_payload(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_task_payload_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_task_payload);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_task_ring_entry_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_task_ring_entry_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tcs_header_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tcs_header_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tcs_in_param_base_r600(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tcs_in_param_base_r600);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tcs_num_patches_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tcs_num_patches_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tcs_out_param_base_r600(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tcs_out_param_base_r600);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tcs_rel_patch_id_r600(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tcs_rel_patch_id_r600);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tcs_tess_factor_base_r600(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tcs_tess_factor_base_r600);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_coord(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_coord);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_coord_r600(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_coord_r600);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_factor_base_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_factor_base_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_level_inner(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_level_inner);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_level_inner_default(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_level_inner_default);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_level_outer(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_level_outer);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_level_outer_default(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_level_outer_default);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_param_base_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_param_base_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tess_rel_patch_id_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tess_rel_patch_id_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_texture_base_agx(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_texture_base_agx);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_texture_rect_scaling(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_texture_rect_scaling);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_tlb_color_v3d(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_tlb_color_v3d_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_tlb_color_v3d);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_topology_id_intel(nir_builder *build, struct _nir_load_topology_id_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_topology_id_intel);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_typed_buffer_amd(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_load_typed_buffer_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_typed_buffer_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_memory_modes(intrin, indices.memory_modes);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ubo(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_ubo_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ubo);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = intrin->dest.ssa.bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);
   nir_intrinsic_set_range_base(intrin, indices.range_base);
   nir_intrinsic_set_range(intrin, indices.range);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ubo_base_agx(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ubo_base_agx);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ubo_dxil(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ubo_dxil);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_ubo_vec4(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_load_ubo_vec4_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_ubo_vec4);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_uniform(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_uniform_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_uniform);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_range(intrin, indices.range);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_user_clip_plane(nir_builder *build, struct _nir_load_user_clip_plane_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_user_clip_plane);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);
   nir_intrinsic_set_ucp_id(intrin, indices.ucp_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_user_data_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_user_data_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vbo_base_agx(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vbo_base_agx);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 64, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vector_arg_amd(nir_builder *build, unsigned num_components, struct _nir_load_vector_arg_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vector_arg_amd);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, 32, NULL);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_arg_upper_bound_u32_amd(intrin, indices.arg_upper_bound_u32_amd);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vertex_id(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vertex_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vertex_id_zero_base(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vertex_id_zero_base);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_view_index(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_view_index);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_offset(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_offset);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_scale(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_scale);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_x_offset(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_x_offset);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_x_scale(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_x_scale);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_xy_scale_and_offset(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_xy_scale_and_offset);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 4, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_y_offset(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_y_offset);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_y_scale(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_y_scale);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_z_offset(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_z_offset);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_viewport_z_scale(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_viewport_z_scale);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vs_primitive_stride_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vs_primitive_stride_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vs_vertex_stride_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vs_vertex_stride_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_vulkan_descriptor(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_load_vulkan_descriptor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_vulkan_descriptor);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_desc_type(intrin, indices.desc_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_work_dim(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_work_dim);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_workgroup_id(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_workgroup_id);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_workgroup_id_zero_base(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_workgroup_id_zero_base);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_workgroup_index(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_workgroup_index);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_workgroup_num_input_primitives_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_workgroup_num_input_primitives_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_workgroup_num_input_vertices_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_workgroup_num_input_vertices_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_workgroup_size(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_workgroup_size);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 3, 32, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_load_xfb_address(nir_builder *build, unsigned bit_size, struct _nir_load_xfb_address_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_load_xfb_address);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_masked_swizzle_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_masked_swizzle_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_masked_swizzle_amd);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_swizzle_mask(intrin, indices.swizzle_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_mbcnt_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_mbcnt_amd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_memcpy_deref(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_memcpy_deref_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memcpy_deref);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_dst_access(intrin, indices.dst_access);
   nir_intrinsic_set_src_access(intrin, indices.src_access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_memory_barrier(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memory_barrier);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_memory_barrier_atomic_counter(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memory_barrier_atomic_counter);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_memory_barrier_buffer(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memory_barrier_buffer);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_memory_barrier_image(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memory_barrier_image);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_memory_barrier_shared(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memory_barrier_shared);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_memory_barrier_tcs_patch(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_memory_barrier_tcs_patch);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_nop(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_nop);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_optimization_barrier_vgpr_amd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_optimization_barrier_vgpr_amd);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ordered_xfb_counter_add_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_ordered_xfb_counter_add_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ordered_xfb_counter_add_amd);

   intrin->num_components = src1->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_overwrite_tes_arguments_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_overwrite_tes_arguments_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_overwrite_vs_arguments_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_overwrite_vs_arguments_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_preamble_end_ir3(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_preamble_end_ir3);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_preamble_start_ir3(nir_builder *build, unsigned bit_size)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_preamble_start_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_printf(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_printf);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_quad_broadcast(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_quad_broadcast);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_quad_swap_diagonal(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_quad_swap_diagonal);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_quad_swap_horizontal(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_quad_swap_horizontal);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_quad_swap_vertical(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_quad_swap_vertical);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_quad_swizzle_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_quad_swizzle_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_quad_swizzle_amd);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_swizzle_mask(intrin, indices.swizzle_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_read_first_invocation(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_read_first_invocation);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_read_invocation(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_read_invocation);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_read_invocation_cond_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_read_invocation_cond_ir3);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_reduce(nir_builder *build, nir_ssa_def *src0, struct _nir_reduce_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_reduce);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_reduction_op(intrin, indices.reduction_op);
   nir_intrinsic_set_cluster_size(intrin, indices.cluster_size);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_report_ray_intersection(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_report_ray_intersection);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_rotate(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_rotate_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rotate);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_execution_scope(intrin, indices.execution_scope);
   nir_intrinsic_set_cluster_size(intrin, indices.cluster_size);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_rq_confirm_intersection(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rq_confirm_intersection);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_rq_generate_intersection(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rq_generate_intersection);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_rq_initialize(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5, nir_ssa_def *src6, nir_ssa_def *src7)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rq_initialize);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   intrin->src[5] = nir_src_for_ssa(src5);
   intrin->src[6] = nir_src_for_ssa(src6);
   intrin->src[7] = nir_src_for_ssa(src7);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_rq_load(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_rq_load_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rq_load);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_ray_query_value(intrin, indices.ray_query_value);
   nir_intrinsic_set_column(intrin, indices.column);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_rq_proceed(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rq_proceed);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_rq_terminate(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rq_terminate);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_rt_execute_callable(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_rt_execute_callable_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rt_execute_callable);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_call_idx(intrin, indices.call_idx);
   nir_intrinsic_set_stack_size(intrin, indices.stack_size);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_rt_resume(nir_builder *build, struct _nir_rt_resume_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rt_resume);

   nir_intrinsic_set_call_idx(intrin, indices.call_idx);
   nir_intrinsic_set_stack_size(intrin, indices.stack_size);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_rt_return_amd(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rt_return_amd);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_rt_trace_ray(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5, nir_ssa_def *src6, nir_ssa_def *src7, nir_ssa_def *src8, nir_ssa_def *src9, nir_ssa_def *src10, struct _nir_rt_trace_ray_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_rt_trace_ray);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   intrin->src[5] = nir_src_for_ssa(src5);
   intrin->src[6] = nir_src_for_ssa(src6);
   intrin->src[7] = nir_src_for_ssa(src7);
   intrin->src[8] = nir_src_for_ssa(src8);
   intrin->src[9] = nir_src_for_ssa(src9);
   intrin->src[10] = nir_src_for_ssa(src10);
   nir_intrinsic_set_call_idx(intrin, indices.call_idx);
   nir_intrinsic_set_stack_size(intrin, indices.stack_size);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_scoped_barrier(nir_builder *build, struct _nir_scoped_barrier_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_scoped_barrier);

   nir_intrinsic_set_execution_scope(intrin, indices.execution_scope);
   nir_intrinsic_set_memory_scope(intrin, indices.memory_scope);
   nir_intrinsic_set_memory_semantics(intrin, indices.memory_semantics);
   nir_intrinsic_set_memory_modes(intrin, indices.memory_modes);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_set_vertex_and_primitive_count(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_set_vertex_and_primitive_count_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_set_vertex_and_primitive_count);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_stream_id(intrin, indices.stream_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_shader_clock(nir_builder *build, struct _nir_shader_clock_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shader_clock);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 2, 32, NULL);
   nir_intrinsic_set_memory_scope(intrin, indices.memory_scope);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_add_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_add_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_and_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_and_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_shared_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_comp_swap_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_comp_swap_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_exchange_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_exchange_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_fcomp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_shared_atomic_fcomp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_fcomp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_imax_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_imax_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_imin_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_imin_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_or_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_or_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_umax_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_umax_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_umin_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_umin_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_shared_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shared_atomic_xor_dxil(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shared_atomic_xor_dxil);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shuffle(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shuffle);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shuffle_down(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shuffle_down);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shuffle_up(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shuffle_up);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_shuffle_xor(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_shuffle_xor);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_sparse_residency_code_and(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_sparse_residency_code_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, 32, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_add_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_add_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_add_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_and_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_and_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_and_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_comp_swap_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_ssbo_atomic_comp_swap_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_comp_swap_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_exchange_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_exchange_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_exchange_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_fcomp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_fcomp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_fcomp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_imax_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_imax_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_imax_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_imin_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_imin_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_imin_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_or_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_or_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_or_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_umax_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_umax_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_umax_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_umin_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_umin_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_umin_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_ssbo_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_ssbo_atomic_xor_ir3(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_ssbo_atomic_xor_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_ssbo_atomic_xor_ir3);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_store_agx(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_agx);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_format(intrin, indices.format);
   nir_intrinsic_set_sign_extend(intrin, indices.sign_extend);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_buffer_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_store_buffer_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_buffer_amd);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_memory_modes(intrin, indices.memory_modes);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_combined_output_pan(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, struct _nir_store_combined_output_pan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_combined_output_pan);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_src_type(intrin, indices.src_type);
   nir_intrinsic_set_dest_type(intrin, indices.dest_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_deref(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_deref_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_deref);

   intrin->num_components = src1->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_deref_block_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_deref_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_deref_block_intel);

   intrin->num_components = src1->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_global(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_global_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_global);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_global_2x32(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_global_2x32_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_global_2x32);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_global_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_global_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_global_amd);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_global_block_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_global_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_global_block_intel);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_global_ir3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_global_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_global_ir3);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_hit_attrib_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_store_hit_attrib_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_hit_attrib_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_local_pixel_agx(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_local_pixel_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_local_pixel_agx);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_format(intrin, indices.format);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_local_shared_r600(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_local_shared_r600_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_local_shared_r600);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_output(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_output_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_output);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_src_type(intrin, indices.src_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);
   nir_intrinsic_set_io_xfb(intrin, indices.io_xfb);
   nir_intrinsic_set_io_xfb2(intrin, indices.io_xfb2);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_per_primitive_output(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_per_primitive_output_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_per_primitive_output);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_src_type(intrin, indices.src_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_per_vertex_output(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_per_vertex_output_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_per_vertex_output);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_src_type(intrin, indices.src_type);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_preamble(nir_builder *build, nir_ssa_def *src0, struct _nir_store_preamble_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_preamble);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_raw_output_pan(nir_builder *build, nir_ssa_def *src0, struct _nir_store_raw_output_pan_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_raw_output_pan);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_io_semantics(intrin, indices.io_semantics);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_scratch(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_scratch_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_scratch);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_scratch_dxil(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_scratch_dxil);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_shared(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_shared_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_shared);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_shared2_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_shared2_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_shared2_amd);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_offset0(intrin, indices.offset0);
   nir_intrinsic_set_offset1(intrin, indices.offset1);
   nir_intrinsic_set_st64(intrin, indices.st64);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_shared_block_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_shared_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_shared_block_intel);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_shared_dxil(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_shared_dxil);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_shared_ir3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_shared_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_shared_ir3);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_shared_masked_dxil(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_shared_masked_dxil);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_ssbo(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_ssbo_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_ssbo);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_ssbo_block_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_ssbo_block_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_ssbo_block_intel);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_ssbo_ir3(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, struct _nir_store_ssbo_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_ssbo_ir3);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_access(intrin, indices.access);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_ssbo_masked_dxil(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_ssbo_masked_dxil);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_stack(nir_builder *build, nir_ssa_def *src0, struct _nir_store_stack_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_stack);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_call_idx(intrin, indices.call_idx);
   nir_intrinsic_set_value_id(intrin, indices.value_id);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_task_payload(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_task_payload_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_task_payload);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   if (!indices.align_mul)
      indices.align_mul = src0->bit_size / 8u;
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);
   nir_intrinsic_set_align_mul(intrin, indices.align_mul);
   nir_intrinsic_set_align_offset(intrin, indices.align_offset);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_tf_r600(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_tf_r600);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_tlb_sample_color_v3d(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_store_tlb_sample_color_v3d_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_tlb_sample_color_v3d);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);
   nir_intrinsic_set_component(intrin, indices.component);
   nir_intrinsic_set_src_type(intrin, indices.src_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_uniform_ir3(nir_builder *build, nir_ssa_def *src0, struct _nir_store_uniform_ir3_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_uniform_ir3);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_store_zs_agx(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_store_zs_agx_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_store_zs_agx);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_add(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_add_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_add);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_and(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_and_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_and);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_comp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_task_payload_atomic_comp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_comp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_exchange(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_exchange_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_exchange);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_fadd(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_fadd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_fadd);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_fcomp_swap(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_task_payload_atomic_fcomp_swap_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_fcomp_swap);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_fmax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_fmax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_fmax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_fmin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_fmin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_fmin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_imax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_imax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_imax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_imin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_imin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_imin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_or(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_or_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_or);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_umax(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_umax_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_umax);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_umin(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_umin_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_umin);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_task_payload_atomic_xor(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_task_payload_atomic_xor_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_task_payload_atomic_xor);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_base(intrin, indices.base);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_terminate(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_terminate);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_terminate_if(nir_builder *build, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_terminate_if);

   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_terminate_ray(nir_builder *build)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_terminate_ray);


   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_trace_ray(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, nir_ssa_def *src3, nir_ssa_def *src4, nir_ssa_def *src5, nir_ssa_def *src6, nir_ssa_def *src7, nir_ssa_def *src8, nir_ssa_def *src9, nir_ssa_def *src10)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_trace_ray);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   intrin->src[3] = nir_src_for_ssa(src3);
   intrin->src[4] = nir_src_for_ssa(src4);
   intrin->src[5] = nir_src_for_ssa(src5);
   intrin->src[6] = nir_src_for_ssa(src6);
   intrin->src[7] = nir_src_for_ssa(src7);
   intrin->src[8] = nir_src_for_ssa(src8);
   intrin->src[9] = nir_src_for_ssa(src9);
   intrin->src[10] = nir_src_for_ssa(src10);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_intrinsic_instr *
_nir_build_trace_ray_intel(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2, struct _nir_trace_ray_intel_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_trace_ray_intel);

   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);
   nir_intrinsic_set_synchronous(intrin, indices.synchronous);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}
static inline nir_ssa_def *
_nir_build_vote_all(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_vote_all);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_vote_any(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_vote_any);

      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_vote_feq(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_vote_feq);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_vote_ieq(nir_builder *build, unsigned bit_size, nir_ssa_def *src0)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_vote_ieq);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, 1, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_vulkan_resource_index(nir_builder *build, unsigned num_components, unsigned bit_size, nir_ssa_def *src0, struct _nir_vulkan_resource_index_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_vulkan_resource_index);

   intrin->num_components = num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   nir_intrinsic_set_desc_set(intrin, indices.desc_set);
   nir_intrinsic_set_binding(intrin, indices.binding);
   nir_intrinsic_set_desc_type(intrin, indices.desc_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_vulkan_resource_reindex(nir_builder *build, unsigned bit_size, nir_ssa_def *src0, nir_ssa_def *src1, struct _nir_vulkan_resource_reindex_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_vulkan_resource_reindex);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   nir_intrinsic_set_desc_type(intrin, indices.desc_type);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_ssa_def *
_nir_build_write_invocation_amd(nir_builder *build, nir_ssa_def *src0, nir_ssa_def *src1, nir_ssa_def *src2)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_write_invocation_amd);

   intrin->num_components = src0->num_components;
      nir_ssa_dest_init(&intrin->instr, &intrin->dest, intrin->num_components, src0->bit_size, NULL);
   intrin->src[0] = nir_src_for_ssa(src0);
   intrin->src[1] = nir_src_for_ssa(src1);
   intrin->src[2] = nir_src_for_ssa(src2);

   nir_builder_instr_insert(build, &intrin->instr);
   return &intrin->dest.ssa;
}
static inline nir_intrinsic_instr *
_nir_build_xfb_counter_sub_amd(nir_builder *build, nir_ssa_def *src0, struct _nir_xfb_counter_sub_amd_indices indices)
{
   nir_intrinsic_instr *intrin = nir_intrinsic_instr_create(
      build->shader, nir_intrinsic_xfb_counter_sub_amd);

   intrin->num_components = src0->num_components;
   intrin->src[0] = nir_src_for_ssa(src0);
   if (!indices.write_mask)
      indices.write_mask = BITFIELD_MASK(intrin->num_components);
   nir_intrinsic_set_write_mask(intrin, indices.write_mask);

   nir_builder_instr_insert(build, &intrin->instr);
   return intrin;
}

#define nir_build_accept_ray_intersection _nir_build_accept_ray_intersection
#define nir_accept_ray_intersection nir_build_accept_ray_intersection
#ifdef __cplusplus
#define nir_build_addr_mode_is(build, bit_size, src0, ...) \
_nir_build_addr_mode_is(build, bit_size, src0, _nir_addr_mode_is_indices{0, __VA_ARGS__})
#else
#define nir_build_addr_mode_is(build, bit_size, src0, ...) \
_nir_build_addr_mode_is(build, bit_size, src0, (struct _nir_addr_mode_is_indices){0, __VA_ARGS__})
#endif
#define nir_addr_mode_is nir_build_addr_mode_is
#define nir_build_alloc_vertices_and_primitives_amd _nir_build_alloc_vertices_and_primitives_amd
#define nir_alloc_vertices_and_primitives_amd nir_build_alloc_vertices_and_primitives_amd
#ifdef __cplusplus
#define nir_build_atomic_add_gen_prim_count_amd(build, src0, ...) \
_nir_build_atomic_add_gen_prim_count_amd(build, src0, _nir_atomic_add_gen_prim_count_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_add_gen_prim_count_amd(build, src0, ...) \
_nir_build_atomic_add_gen_prim_count_amd(build, src0, (struct _nir_atomic_add_gen_prim_count_amd_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_add_gen_prim_count_amd nir_build_atomic_add_gen_prim_count_amd
#define nir_build_atomic_add_gs_emit_prim_count_amd _nir_build_atomic_add_gs_emit_prim_count_amd
#define nir_atomic_add_gs_emit_prim_count_amd nir_build_atomic_add_gs_emit_prim_count_amd
#define nir_build_atomic_add_gs_invocation_count_amd _nir_build_atomic_add_gs_invocation_count_amd
#define nir_atomic_add_gs_invocation_count_amd nir_build_atomic_add_gs_invocation_count_amd
#ifdef __cplusplus
#define nir_build_atomic_add_xfb_prim_count_amd(build, src0, ...) \
_nir_build_atomic_add_xfb_prim_count_amd(build, src0, _nir_atomic_add_xfb_prim_count_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_add_xfb_prim_count_amd(build, src0, ...) \
_nir_build_atomic_add_xfb_prim_count_amd(build, src0, (struct _nir_atomic_add_xfb_prim_count_amd_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_add_xfb_prim_count_amd nir_build_atomic_add_xfb_prim_count_amd
#ifdef __cplusplus
#define nir_build_atomic_counter_add(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_add(build, bit_size, src0, src1, _nir_atomic_counter_add_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_add(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_add(build, bit_size, src0, src1, (struct _nir_atomic_counter_add_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_add nir_build_atomic_counter_add
#define nir_build_atomic_counter_add_deref _nir_build_atomic_counter_add_deref
#define nir_atomic_counter_add_deref nir_build_atomic_counter_add_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_and(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_and(build, bit_size, src0, src1, _nir_atomic_counter_and_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_and(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_and(build, bit_size, src0, src1, (struct _nir_atomic_counter_and_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_and nir_build_atomic_counter_and
#define nir_build_atomic_counter_and_deref _nir_build_atomic_counter_and_deref
#define nir_atomic_counter_and_deref nir_build_atomic_counter_and_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_atomic_counter_comp_swap(build, bit_size, src0, src1, src2, _nir_atomic_counter_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_atomic_counter_comp_swap(build, bit_size, src0, src1, src2, (struct _nir_atomic_counter_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_comp_swap nir_build_atomic_counter_comp_swap
#define nir_build_atomic_counter_comp_swap_deref _nir_build_atomic_counter_comp_swap_deref
#define nir_atomic_counter_comp_swap_deref nir_build_atomic_counter_comp_swap_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_exchange(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_exchange(build, bit_size, src0, src1, _nir_atomic_counter_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_exchange(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_exchange(build, bit_size, src0, src1, (struct _nir_atomic_counter_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_exchange nir_build_atomic_counter_exchange
#define nir_build_atomic_counter_exchange_deref _nir_build_atomic_counter_exchange_deref
#define nir_atomic_counter_exchange_deref nir_build_atomic_counter_exchange_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_inc(build, bit_size, src0, ...) \
_nir_build_atomic_counter_inc(build, bit_size, src0, _nir_atomic_counter_inc_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_inc(build, bit_size, src0, ...) \
_nir_build_atomic_counter_inc(build, bit_size, src0, (struct _nir_atomic_counter_inc_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_inc nir_build_atomic_counter_inc
#define nir_build_atomic_counter_inc_deref _nir_build_atomic_counter_inc_deref
#define nir_atomic_counter_inc_deref nir_build_atomic_counter_inc_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_max(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_max(build, bit_size, src0, src1, _nir_atomic_counter_max_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_max(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_max(build, bit_size, src0, src1, (struct _nir_atomic_counter_max_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_max nir_build_atomic_counter_max
#define nir_build_atomic_counter_max_deref _nir_build_atomic_counter_max_deref
#define nir_atomic_counter_max_deref nir_build_atomic_counter_max_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_min(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_min(build, bit_size, src0, src1, _nir_atomic_counter_min_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_min(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_min(build, bit_size, src0, src1, (struct _nir_atomic_counter_min_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_min nir_build_atomic_counter_min
#define nir_build_atomic_counter_min_deref _nir_build_atomic_counter_min_deref
#define nir_atomic_counter_min_deref nir_build_atomic_counter_min_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_or(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_or(build, bit_size, src0, src1, _nir_atomic_counter_or_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_or(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_or(build, bit_size, src0, src1, (struct _nir_atomic_counter_or_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_or nir_build_atomic_counter_or
#define nir_build_atomic_counter_or_deref _nir_build_atomic_counter_or_deref
#define nir_atomic_counter_or_deref nir_build_atomic_counter_or_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_post_dec(build, bit_size, src0, ...) \
_nir_build_atomic_counter_post_dec(build, bit_size, src0, _nir_atomic_counter_post_dec_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_post_dec(build, bit_size, src0, ...) \
_nir_build_atomic_counter_post_dec(build, bit_size, src0, (struct _nir_atomic_counter_post_dec_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_post_dec nir_build_atomic_counter_post_dec
#define nir_build_atomic_counter_post_dec_deref _nir_build_atomic_counter_post_dec_deref
#define nir_atomic_counter_post_dec_deref nir_build_atomic_counter_post_dec_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_pre_dec(build, bit_size, src0, ...) \
_nir_build_atomic_counter_pre_dec(build, bit_size, src0, _nir_atomic_counter_pre_dec_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_pre_dec(build, bit_size, src0, ...) \
_nir_build_atomic_counter_pre_dec(build, bit_size, src0, (struct _nir_atomic_counter_pre_dec_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_pre_dec nir_build_atomic_counter_pre_dec
#define nir_build_atomic_counter_pre_dec_deref _nir_build_atomic_counter_pre_dec_deref
#define nir_atomic_counter_pre_dec_deref nir_build_atomic_counter_pre_dec_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_read(build, bit_size, src0, ...) \
_nir_build_atomic_counter_read(build, bit_size, src0, _nir_atomic_counter_read_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_read(build, bit_size, src0, ...) \
_nir_build_atomic_counter_read(build, bit_size, src0, (struct _nir_atomic_counter_read_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_read nir_build_atomic_counter_read
#define nir_build_atomic_counter_read_deref _nir_build_atomic_counter_read_deref
#define nir_atomic_counter_read_deref nir_build_atomic_counter_read_deref
#ifdef __cplusplus
#define nir_build_atomic_counter_xor(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_xor(build, bit_size, src0, src1, _nir_atomic_counter_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_atomic_counter_xor(build, bit_size, src0, src1, ...) \
_nir_build_atomic_counter_xor(build, bit_size, src0, src1, (struct _nir_atomic_counter_xor_indices){0, __VA_ARGS__})
#endif
#define nir_atomic_counter_xor nir_build_atomic_counter_xor
#define nir_build_atomic_counter_xor_deref _nir_build_atomic_counter_xor_deref
#define nir_atomic_counter_xor_deref nir_build_atomic_counter_xor_deref
#define nir_build_ballot _nir_build_ballot
#define nir_ballot nir_build_ballot
#define nir_build_ballot_bit_count_exclusive _nir_build_ballot_bit_count_exclusive
#define nir_ballot_bit_count_exclusive nir_build_ballot_bit_count_exclusive
#define nir_build_ballot_bit_count_inclusive _nir_build_ballot_bit_count_inclusive
#define nir_ballot_bit_count_inclusive nir_build_ballot_bit_count_inclusive
#define nir_build_ballot_bit_count_reduce _nir_build_ballot_bit_count_reduce
#define nir_ballot_bit_count_reduce nir_build_ballot_bit_count_reduce
#define nir_build_ballot_bitfield_extract _nir_build_ballot_bitfield_extract
#define nir_ballot_bitfield_extract nir_build_ballot_bitfield_extract
#define nir_build_ballot_find_lsb _nir_build_ballot_find_lsb
#define nir_ballot_find_lsb nir_build_ballot_find_lsb
#define nir_build_ballot_find_msb _nir_build_ballot_find_msb
#define nir_ballot_find_msb nir_build_ballot_find_msb
#define nir_build_begin_invocation_interlock _nir_build_begin_invocation_interlock
#define nir_begin_invocation_interlock nir_build_begin_invocation_interlock
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_add(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_add(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_add(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_add(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_add nir_build_bindless_image_atomic_add
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_and(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_and(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_and(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_and(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_and nir_build_bindless_image_atomic_and
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_bindless_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, _nir_bindless_image_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_bindless_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, (struct _nir_bindless_image_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_comp_swap nir_build_bindless_image_atomic_comp_swap
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_dec_wrap_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_dec_wrap_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_dec_wrap nir_build_bindless_image_atomic_dec_wrap
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_exchange nir_build_bindless_image_atomic_exchange
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_fadd nir_build_bindless_image_atomic_fadd
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_fmax nir_build_bindless_image_atomic_fmax
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_fmin nir_build_bindless_image_atomic_fmin
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_imax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_imax(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_imax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_imax(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_imax nir_build_bindless_image_atomic_imax
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_imin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_imin(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_imin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_imin(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_imin nir_build_bindless_image_atomic_imin
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_inc_wrap_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_inc_wrap_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_inc_wrap nir_build_bindless_image_atomic_inc_wrap
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_or(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_or(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_or(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_or(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_or nir_build_bindless_image_atomic_or
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_umax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_umax(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_umax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_umax(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_umax nir_build_bindless_image_atomic_umax
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_umin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_umin(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_umin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_umin(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_umin nir_build_bindless_image_atomic_umin
#ifdef __cplusplus
#define nir_build_bindless_image_atomic_xor(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_xor(build, bit_size, src0, src1, src2, src3, _nir_bindless_image_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_atomic_xor(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_atomic_xor(build, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_atomic_xor nir_build_bindless_image_atomic_xor
#ifdef __cplusplus
#define nir_build_bindless_image_descriptor_amd(build, num_components, bit_size, src0, ...) \
_nir_build_bindless_image_descriptor_amd(build, num_components, bit_size, src0, _nir_bindless_image_descriptor_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_descriptor_amd(build, num_components, bit_size, src0, ...) \
_nir_build_bindless_image_descriptor_amd(build, num_components, bit_size, src0, (struct _nir_bindless_image_descriptor_amd_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_descriptor_amd nir_build_bindless_image_descriptor_amd
#ifdef __cplusplus
#define nir_build_bindless_image_format(build, bit_size, src0, ...) \
_nir_build_bindless_image_format(build, bit_size, src0, _nir_bindless_image_format_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_format(build, bit_size, src0, ...) \
_nir_build_bindless_image_format(build, bit_size, src0, (struct _nir_bindless_image_format_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_format nir_build_bindless_image_format
#ifdef __cplusplus
#define nir_build_bindless_image_fragment_mask_load_amd(build, src0, src1, ...) \
_nir_build_bindless_image_fragment_mask_load_amd(build, src0, src1, _nir_bindless_image_fragment_mask_load_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_fragment_mask_load_amd(build, src0, src1, ...) \
_nir_build_bindless_image_fragment_mask_load_amd(build, src0, src1, (struct _nir_bindless_image_fragment_mask_load_amd_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_fragment_mask_load_amd nir_build_bindless_image_fragment_mask_load_amd
#ifdef __cplusplus
#define nir_build_bindless_image_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_load(build, num_components, bit_size, src0, src1, src2, src3, _nir_bindless_image_load_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_load(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_load_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_load nir_build_bindless_image_load
#ifdef __cplusplus
#define nir_build_bindless_image_load_raw_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_bindless_image_load_raw_intel(build, num_components, bit_size, src0, src1, _nir_bindless_image_load_raw_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_load_raw_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_bindless_image_load_raw_intel(build, num_components, bit_size, src0, src1, (struct _nir_bindless_image_load_raw_intel_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_load_raw_intel nir_build_bindless_image_load_raw_intel
#ifdef __cplusplus
#define nir_build_bindless_image_order(build, bit_size, src0, ...) \
_nir_build_bindless_image_order(build, bit_size, src0, _nir_bindless_image_order_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_order(build, bit_size, src0, ...) \
_nir_build_bindless_image_order(build, bit_size, src0, (struct _nir_bindless_image_order_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_order nir_build_bindless_image_order
#ifdef __cplusplus
#define nir_build_bindless_image_samples(build, bit_size, src0, ...) \
_nir_build_bindless_image_samples(build, bit_size, src0, _nir_bindless_image_samples_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_samples(build, bit_size, src0, ...) \
_nir_build_bindless_image_samples(build, bit_size, src0, (struct _nir_bindless_image_samples_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_samples nir_build_bindless_image_samples
#ifdef __cplusplus
#define nir_build_bindless_image_samples_identical(build, bit_size, src0, src1, ...) \
_nir_build_bindless_image_samples_identical(build, bit_size, src0, src1, _nir_bindless_image_samples_identical_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_samples_identical(build, bit_size, src0, src1, ...) \
_nir_build_bindless_image_samples_identical(build, bit_size, src0, src1, (struct _nir_bindless_image_samples_identical_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_samples_identical nir_build_bindless_image_samples_identical
#ifdef __cplusplus
#define nir_build_bindless_image_size(build, num_components, bit_size, src0, src1, ...) \
_nir_build_bindless_image_size(build, num_components, bit_size, src0, src1, _nir_bindless_image_size_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_size(build, num_components, bit_size, src0, src1, ...) \
_nir_build_bindless_image_size(build, num_components, bit_size, src0, src1, (struct _nir_bindless_image_size_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_size nir_build_bindless_image_size
#ifdef __cplusplus
#define nir_build_bindless_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, _nir_bindless_image_sparse_load_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_bindless_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_bindless_image_sparse_load_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_sparse_load nir_build_bindless_image_sparse_load
#ifdef __cplusplus
#define nir_build_bindless_image_store(build, src0, src1, src2, src3, src4, ...) \
_nir_build_bindless_image_store(build, src0, src1, src2, src3, src4, _nir_bindless_image_store_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_store(build, src0, src1, src2, src3, src4, ...) \
_nir_build_bindless_image_store(build, src0, src1, src2, src3, src4, (struct _nir_bindless_image_store_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_store nir_build_bindless_image_store
#ifdef __cplusplus
#define nir_build_bindless_image_store_raw_intel(build, src0, src1, src2, ...) \
_nir_build_bindless_image_store_raw_intel(build, src0, src1, src2, _nir_bindless_image_store_raw_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_image_store_raw_intel(build, src0, src1, src2, ...) \
_nir_build_bindless_image_store_raw_intel(build, src0, src1, src2, (struct _nir_bindless_image_store_raw_intel_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_image_store_raw_intel nir_build_bindless_image_store_raw_intel
#ifdef __cplusplus
#define nir_build_bindless_resource_ir3(build, bit_size, src0, ...) \
_nir_build_bindless_resource_ir3(build, bit_size, src0, _nir_bindless_resource_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_bindless_resource_ir3(build, bit_size, src0, ...) \
_nir_build_bindless_resource_ir3(build, bit_size, src0, (struct _nir_bindless_resource_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_bindless_resource_ir3 nir_build_bindless_resource_ir3
#ifdef __cplusplus
#define nir_build_block_image_store_agx(build, src0, src1, ...) \
_nir_build_block_image_store_agx(build, src0, src1, _nir_block_image_store_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_block_image_store_agx(build, src0, src1, ...) \
_nir_build_block_image_store_agx(build, src0, src1, (struct _nir_block_image_store_agx_indices){0, __VA_ARGS__})
#endif
#define nir_block_image_store_agx nir_build_block_image_store_agx
#define nir_build_btd_retire_intel _nir_build_btd_retire_intel
#define nir_btd_retire_intel nir_build_btd_retire_intel
#define nir_build_btd_spawn_intel _nir_build_btd_spawn_intel
#define nir_btd_spawn_intel nir_build_btd_spawn_intel
#ifdef __cplusplus
#define nir_build_btd_stack_push_intel(build, ...) \
_nir_build_btd_stack_push_intel(build, _nir_btd_stack_push_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_btd_stack_push_intel(build, ...) \
_nir_build_btd_stack_push_intel(build, (struct _nir_btd_stack_push_intel_indices){0, __VA_ARGS__})
#endif
#define nir_btd_stack_push_intel nir_build_btd_stack_push_intel
#ifdef __cplusplus
#define nir_build_buffer_atomic_add_amd(build, bit_size, src0, src1, ...) \
_nir_build_buffer_atomic_add_amd(build, bit_size, src0, src1, _nir_buffer_atomic_add_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_buffer_atomic_add_amd(build, bit_size, src0, src1, ...) \
_nir_build_buffer_atomic_add_amd(build, bit_size, src0, src1, (struct _nir_buffer_atomic_add_amd_indices){0, __VA_ARGS__})
#endif
#define nir_buffer_atomic_add_amd nir_build_buffer_atomic_add_amd
#define nir_build_bvh64_intersect_ray_amd _nir_build_bvh64_intersect_ray_amd
#define nir_bvh64_intersect_ray_amd nir_build_bvh64_intersect_ray_amd
#define nir_build_cond_end_ir3 _nir_build_cond_end_ir3
#define nir_cond_end_ir3 nir_build_cond_end_ir3
#define nir_build_control_barrier _nir_build_control_barrier
#define nir_control_barrier nir_build_control_barrier
#ifdef __cplusplus
#define nir_build_convert_alu_types(build, bit_size, src0, ...) \
_nir_build_convert_alu_types(build, bit_size, src0, _nir_convert_alu_types_indices{0, __VA_ARGS__})
#else
#define nir_build_convert_alu_types(build, bit_size, src0, ...) \
_nir_build_convert_alu_types(build, bit_size, src0, (struct _nir_convert_alu_types_indices){0, __VA_ARGS__})
#endif
#define nir_convert_alu_types nir_build_convert_alu_types
#ifdef __cplusplus
#define nir_build_copy_deref(build, src0, src1, ...) \
_nir_build_copy_deref(build, src0, src1, _nir_copy_deref_indices{0, __VA_ARGS__})
#else
#define nir_build_copy_deref(build, src0, src1, ...) \
_nir_build_copy_deref(build, src0, src1, (struct _nir_copy_deref_indices){0, __VA_ARGS__})
#endif
#define nir_copy_deref nir_build_copy_deref
#ifdef __cplusplus
#define nir_build_copy_ubo_to_uniform_ir3(build, src0, src1, ...) \
_nir_build_copy_ubo_to_uniform_ir3(build, src0, src1, _nir_copy_ubo_to_uniform_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_copy_ubo_to_uniform_ir3(build, src0, src1, ...) \
_nir_build_copy_ubo_to_uniform_ir3(build, src0, src1, (struct _nir_copy_ubo_to_uniform_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_copy_ubo_to_uniform_ir3 nir_build_copy_ubo_to_uniform_ir3
#define nir_build_demote _nir_build_demote
#define nir_demote nir_build_demote
#define nir_build_demote_if _nir_build_demote_if
#define nir_demote_if nir_build_demote_if
#ifdef __cplusplus
#define nir_build_deref_atomic_add(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_add(build, bit_size, src0, src1, _nir_deref_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_add(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_add(build, bit_size, src0, src1, (struct _nir_deref_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_add nir_build_deref_atomic_add
#ifdef __cplusplus
#define nir_build_deref_atomic_and(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_and(build, bit_size, src0, src1, _nir_deref_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_and(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_and(build, bit_size, src0, src1, (struct _nir_deref_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_and nir_build_deref_atomic_and
#ifdef __cplusplus
#define nir_build_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, _nir_deref_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, (struct _nir_deref_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_comp_swap nir_build_deref_atomic_comp_swap
#ifdef __cplusplus
#define nir_build_deref_atomic_exchange(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_exchange(build, bit_size, src0, src1, _nir_deref_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_exchange(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_exchange(build, bit_size, src0, src1, (struct _nir_deref_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_exchange nir_build_deref_atomic_exchange
#ifdef __cplusplus
#define nir_build_deref_atomic_fadd(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_fadd(build, bit_size, src0, src1, _nir_deref_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_fadd(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_fadd(build, bit_size, src0, src1, (struct _nir_deref_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_fadd nir_build_deref_atomic_fadd
#ifdef __cplusplus
#define nir_build_deref_atomic_fcomp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_deref_atomic_fcomp_swap(build, bit_size, src0, src1, src2, _nir_deref_atomic_fcomp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_fcomp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_deref_atomic_fcomp_swap(build, bit_size, src0, src1, src2, (struct _nir_deref_atomic_fcomp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_fcomp_swap nir_build_deref_atomic_fcomp_swap
#ifdef __cplusplus
#define nir_build_deref_atomic_fmax(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_fmax(build, bit_size, src0, src1, _nir_deref_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_fmax(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_fmax(build, bit_size, src0, src1, (struct _nir_deref_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_fmax nir_build_deref_atomic_fmax
#ifdef __cplusplus
#define nir_build_deref_atomic_fmin(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_fmin(build, bit_size, src0, src1, _nir_deref_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_fmin(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_fmin(build, bit_size, src0, src1, (struct _nir_deref_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_fmin nir_build_deref_atomic_fmin
#ifdef __cplusplus
#define nir_build_deref_atomic_imax(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_imax(build, bit_size, src0, src1, _nir_deref_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_imax(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_imax(build, bit_size, src0, src1, (struct _nir_deref_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_imax nir_build_deref_atomic_imax
#ifdef __cplusplus
#define nir_build_deref_atomic_imin(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_imin(build, bit_size, src0, src1, _nir_deref_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_imin(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_imin(build, bit_size, src0, src1, (struct _nir_deref_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_imin nir_build_deref_atomic_imin
#ifdef __cplusplus
#define nir_build_deref_atomic_or(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_or(build, bit_size, src0, src1, _nir_deref_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_or(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_or(build, bit_size, src0, src1, (struct _nir_deref_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_or nir_build_deref_atomic_or
#ifdef __cplusplus
#define nir_build_deref_atomic_umax(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_umax(build, bit_size, src0, src1, _nir_deref_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_umax(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_umax(build, bit_size, src0, src1, (struct _nir_deref_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_umax nir_build_deref_atomic_umax
#ifdef __cplusplus
#define nir_build_deref_atomic_umin(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_umin(build, bit_size, src0, src1, _nir_deref_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_umin(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_umin(build, bit_size, src0, src1, (struct _nir_deref_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_umin nir_build_deref_atomic_umin
#ifdef __cplusplus
#define nir_build_deref_atomic_xor(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_xor(build, bit_size, src0, src1, _nir_deref_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_atomic_xor(build, bit_size, src0, src1, ...) \
_nir_build_deref_atomic_xor(build, bit_size, src0, src1, (struct _nir_deref_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_deref_atomic_xor nir_build_deref_atomic_xor
#ifdef __cplusplus
#define nir_build_deref_buffer_array_length(build, bit_size, src0, ...) \
_nir_build_deref_buffer_array_length(build, bit_size, src0, _nir_deref_buffer_array_length_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_buffer_array_length(build, bit_size, src0, ...) \
_nir_build_deref_buffer_array_length(build, bit_size, src0, (struct _nir_deref_buffer_array_length_indices){0, __VA_ARGS__})
#endif
#define nir_deref_buffer_array_length nir_build_deref_buffer_array_length
#ifdef __cplusplus
#define nir_build_deref_mode_is(build, bit_size, src0, ...) \
_nir_build_deref_mode_is(build, bit_size, src0, _nir_deref_mode_is_indices{0, __VA_ARGS__})
#else
#define nir_build_deref_mode_is(build, bit_size, src0, ...) \
_nir_build_deref_mode_is(build, bit_size, src0, (struct _nir_deref_mode_is_indices){0, __VA_ARGS__})
#endif
#define nir_deref_mode_is nir_build_deref_mode_is
#define nir_build_discard _nir_build_discard
#define nir_discard nir_build_discard
#define nir_build_discard_if _nir_build_discard_if
#define nir_discard_if nir_build_discard_if
#define nir_build_elect _nir_build_elect
#define nir_elect nir_build_elect
#ifdef __cplusplus
#define nir_build_emit_vertex(build, ...) \
_nir_build_emit_vertex(build, _nir_emit_vertex_indices{0, __VA_ARGS__})
#else
#define nir_build_emit_vertex(build, ...) \
_nir_build_emit_vertex(build, (struct _nir_emit_vertex_indices){0, __VA_ARGS__})
#endif
#define nir_emit_vertex nir_build_emit_vertex
#ifdef __cplusplus
#define nir_build_emit_vertex_with_counter(build, src0, src1, ...) \
_nir_build_emit_vertex_with_counter(build, src0, src1, _nir_emit_vertex_with_counter_indices{0, __VA_ARGS__})
#else
#define nir_build_emit_vertex_with_counter(build, src0, src1, ...) \
_nir_build_emit_vertex_with_counter(build, src0, src1, (struct _nir_emit_vertex_with_counter_indices){0, __VA_ARGS__})
#endif
#define nir_emit_vertex_with_counter nir_build_emit_vertex_with_counter
#define nir_build_end_invocation_interlock _nir_build_end_invocation_interlock
#define nir_end_invocation_interlock nir_build_end_invocation_interlock
#define nir_build_end_patch_ir3 _nir_build_end_patch_ir3
#define nir_end_patch_ir3 nir_build_end_patch_ir3
#ifdef __cplusplus
#define nir_build_end_primitive(build, ...) \
_nir_build_end_primitive(build, _nir_end_primitive_indices{0, __VA_ARGS__})
#else
#define nir_build_end_primitive(build, ...) \
_nir_build_end_primitive(build, (struct _nir_end_primitive_indices){0, __VA_ARGS__})
#endif
#define nir_end_primitive nir_build_end_primitive
#ifdef __cplusplus
#define nir_build_end_primitive_with_counter(build, src0, src1, ...) \
_nir_build_end_primitive_with_counter(build, src0, src1, _nir_end_primitive_with_counter_indices{0, __VA_ARGS__})
#else
#define nir_build_end_primitive_with_counter(build, src0, src1, ...) \
_nir_build_end_primitive_with_counter(build, src0, src1, (struct _nir_end_primitive_with_counter_indices){0, __VA_ARGS__})
#endif
#define nir_end_primitive_with_counter nir_build_end_primitive_with_counter
#ifdef __cplusplus
#define nir_build_exclusive_scan(build, src0, ...) \
_nir_build_exclusive_scan(build, src0, _nir_exclusive_scan_indices{0, __VA_ARGS__})
#else
#define nir_build_exclusive_scan(build, src0, ...) \
_nir_build_exclusive_scan(build, src0, (struct _nir_exclusive_scan_indices){0, __VA_ARGS__})
#endif
#define nir_exclusive_scan nir_build_exclusive_scan
#define nir_build_execute_callable _nir_build_execute_callable
#define nir_execute_callable nir_build_execute_callable
#define nir_build_execute_closest_hit_amd _nir_build_execute_closest_hit_amd
#define nir_execute_closest_hit_amd nir_build_execute_closest_hit_amd
#define nir_build_execute_miss_amd _nir_build_execute_miss_amd
#define nir_execute_miss_amd nir_build_execute_miss_amd
#ifdef __cplusplus
#define nir_build_export_amd(build, src0, ...) \
_nir_build_export_amd(build, src0, _nir_export_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_export_amd(build, src0, ...) \
_nir_build_export_amd(build, src0, (struct _nir_export_amd_indices){0, __VA_ARGS__})
#endif
#define nir_export_amd nir_build_export_amd
#define nir_build_first_invocation _nir_build_first_invocation
#define nir_first_invocation nir_build_first_invocation
#ifdef __cplusplus
#define nir_build_gds_atomic_add_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_gds_atomic_add_amd(build, bit_size, src0, src1, src2, _nir_gds_atomic_add_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_gds_atomic_add_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_gds_atomic_add_amd(build, bit_size, src0, src1, src2, (struct _nir_gds_atomic_add_amd_indices){0, __VA_ARGS__})
#endif
#define nir_gds_atomic_add_amd nir_build_gds_atomic_add_amd
#ifdef __cplusplus
#define nir_build_get_ssbo_size(build, src0, ...) \
_nir_build_get_ssbo_size(build, src0, _nir_get_ssbo_size_indices{0, __VA_ARGS__})
#else
#define nir_build_get_ssbo_size(build, src0, ...) \
_nir_build_get_ssbo_size(build, src0, (struct _nir_get_ssbo_size_indices){0, __VA_ARGS__})
#endif
#define nir_get_ssbo_size nir_build_get_ssbo_size
#define nir_build_get_ubo_size _nir_build_get_ubo_size
#define nir_get_ubo_size nir_build_get_ubo_size
#define nir_build_global_atomic_add _nir_build_global_atomic_add
#define nir_global_atomic_add nir_build_global_atomic_add
#define nir_build_global_atomic_add_2x32 _nir_build_global_atomic_add_2x32
#define nir_global_atomic_add_2x32 nir_build_global_atomic_add_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_add_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_add_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_add_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_add_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_add_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_add_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_add_amd nir_build_global_atomic_add_amd
#ifdef __cplusplus
#define nir_build_global_atomic_add_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_add_ir3(build, bit_size, src0, src1, _nir_global_atomic_add_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_add_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_add_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_add_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_add_ir3 nir_build_global_atomic_add_ir3
#define nir_build_global_atomic_and _nir_build_global_atomic_and
#define nir_global_atomic_and nir_build_global_atomic_and
#define nir_build_global_atomic_and_2x32 _nir_build_global_atomic_and_2x32
#define nir_global_atomic_and_2x32 nir_build_global_atomic_and_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_and_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_and_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_and_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_and_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_and_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_and_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_and_amd nir_build_global_atomic_and_amd
#ifdef __cplusplus
#define nir_build_global_atomic_and_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_and_ir3(build, bit_size, src0, src1, _nir_global_atomic_and_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_and_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_and_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_and_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_and_ir3 nir_build_global_atomic_and_ir3
#define nir_build_global_atomic_comp_swap _nir_build_global_atomic_comp_swap
#define nir_global_atomic_comp_swap nir_build_global_atomic_comp_swap
#define nir_build_global_atomic_comp_swap_2x32 _nir_build_global_atomic_comp_swap_2x32
#define nir_global_atomic_comp_swap_2x32 nir_build_global_atomic_comp_swap_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_comp_swap_amd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_global_atomic_comp_swap_amd(build, bit_size, src0, src1, src2, src3, _nir_global_atomic_comp_swap_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_comp_swap_amd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_global_atomic_comp_swap_amd(build, bit_size, src0, src1, src2, src3, (struct _nir_global_atomic_comp_swap_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_comp_swap_amd nir_build_global_atomic_comp_swap_amd
#ifdef __cplusplus
#define nir_build_global_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, _nir_global_atomic_comp_swap_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_comp_swap_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_comp_swap_ir3 nir_build_global_atomic_comp_swap_ir3
#define nir_build_global_atomic_exchange _nir_build_global_atomic_exchange
#define nir_global_atomic_exchange nir_build_global_atomic_exchange
#define nir_build_global_atomic_exchange_2x32 _nir_build_global_atomic_exchange_2x32
#define nir_global_atomic_exchange_2x32 nir_build_global_atomic_exchange_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_exchange_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_exchange_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_exchange_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_exchange_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_exchange_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_exchange_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_exchange_amd nir_build_global_atomic_exchange_amd
#ifdef __cplusplus
#define nir_build_global_atomic_exchange_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_exchange_ir3(build, bit_size, src0, src1, _nir_global_atomic_exchange_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_exchange_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_exchange_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_exchange_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_exchange_ir3 nir_build_global_atomic_exchange_ir3
#define nir_build_global_atomic_fadd _nir_build_global_atomic_fadd
#define nir_global_atomic_fadd nir_build_global_atomic_fadd
#define nir_build_global_atomic_fadd_2x32 _nir_build_global_atomic_fadd_2x32
#define nir_global_atomic_fadd_2x32 nir_build_global_atomic_fadd_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_fadd_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_fadd_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_fadd_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_fadd_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_fadd_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_fadd_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_fadd_amd nir_build_global_atomic_fadd_amd
#define nir_build_global_atomic_fcomp_swap _nir_build_global_atomic_fcomp_swap
#define nir_global_atomic_fcomp_swap nir_build_global_atomic_fcomp_swap
#define nir_build_global_atomic_fcomp_swap_2x32 _nir_build_global_atomic_fcomp_swap_2x32
#define nir_global_atomic_fcomp_swap_2x32 nir_build_global_atomic_fcomp_swap_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_fcomp_swap_amd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_global_atomic_fcomp_swap_amd(build, bit_size, src0, src1, src2, src3, _nir_global_atomic_fcomp_swap_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_fcomp_swap_amd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_global_atomic_fcomp_swap_amd(build, bit_size, src0, src1, src2, src3, (struct _nir_global_atomic_fcomp_swap_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_fcomp_swap_amd nir_build_global_atomic_fcomp_swap_amd
#define nir_build_global_atomic_fmax _nir_build_global_atomic_fmax
#define nir_global_atomic_fmax nir_build_global_atomic_fmax
#define nir_build_global_atomic_fmax_2x32 _nir_build_global_atomic_fmax_2x32
#define nir_global_atomic_fmax_2x32 nir_build_global_atomic_fmax_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_fmax_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_fmax_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_fmax_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_fmax_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_fmax_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_fmax_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_fmax_amd nir_build_global_atomic_fmax_amd
#define nir_build_global_atomic_fmin _nir_build_global_atomic_fmin
#define nir_global_atomic_fmin nir_build_global_atomic_fmin
#define nir_build_global_atomic_fmin_2x32 _nir_build_global_atomic_fmin_2x32
#define nir_global_atomic_fmin_2x32 nir_build_global_atomic_fmin_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_fmin_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_fmin_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_fmin_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_fmin_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_fmin_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_fmin_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_fmin_amd nir_build_global_atomic_fmin_amd
#define nir_build_global_atomic_imax _nir_build_global_atomic_imax
#define nir_global_atomic_imax nir_build_global_atomic_imax
#define nir_build_global_atomic_imax_2x32 _nir_build_global_atomic_imax_2x32
#define nir_global_atomic_imax_2x32 nir_build_global_atomic_imax_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_imax_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_imax_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_imax_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_imax_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_imax_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_imax_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_imax_amd nir_build_global_atomic_imax_amd
#ifdef __cplusplus
#define nir_build_global_atomic_imax_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_imax_ir3(build, bit_size, src0, src1, _nir_global_atomic_imax_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_imax_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_imax_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_imax_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_imax_ir3 nir_build_global_atomic_imax_ir3
#define nir_build_global_atomic_imin _nir_build_global_atomic_imin
#define nir_global_atomic_imin nir_build_global_atomic_imin
#define nir_build_global_atomic_imin_2x32 _nir_build_global_atomic_imin_2x32
#define nir_global_atomic_imin_2x32 nir_build_global_atomic_imin_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_imin_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_imin_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_imin_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_imin_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_imin_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_imin_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_imin_amd nir_build_global_atomic_imin_amd
#ifdef __cplusplus
#define nir_build_global_atomic_imin_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_imin_ir3(build, bit_size, src0, src1, _nir_global_atomic_imin_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_imin_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_imin_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_imin_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_imin_ir3 nir_build_global_atomic_imin_ir3
#define nir_build_global_atomic_or _nir_build_global_atomic_or
#define nir_global_atomic_or nir_build_global_atomic_or
#define nir_build_global_atomic_or_2x32 _nir_build_global_atomic_or_2x32
#define nir_global_atomic_or_2x32 nir_build_global_atomic_or_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_or_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_or_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_or_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_or_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_or_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_or_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_or_amd nir_build_global_atomic_or_amd
#ifdef __cplusplus
#define nir_build_global_atomic_or_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_or_ir3(build, bit_size, src0, src1, _nir_global_atomic_or_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_or_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_or_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_or_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_or_ir3 nir_build_global_atomic_or_ir3
#define nir_build_global_atomic_umax _nir_build_global_atomic_umax
#define nir_global_atomic_umax nir_build_global_atomic_umax
#define nir_build_global_atomic_umax_2x32 _nir_build_global_atomic_umax_2x32
#define nir_global_atomic_umax_2x32 nir_build_global_atomic_umax_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_umax_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_umax_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_umax_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_umax_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_umax_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_umax_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_umax_amd nir_build_global_atomic_umax_amd
#ifdef __cplusplus
#define nir_build_global_atomic_umax_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_umax_ir3(build, bit_size, src0, src1, _nir_global_atomic_umax_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_umax_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_umax_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_umax_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_umax_ir3 nir_build_global_atomic_umax_ir3
#define nir_build_global_atomic_umin _nir_build_global_atomic_umin
#define nir_global_atomic_umin nir_build_global_atomic_umin
#define nir_build_global_atomic_umin_2x32 _nir_build_global_atomic_umin_2x32
#define nir_global_atomic_umin_2x32 nir_build_global_atomic_umin_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_umin_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_umin_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_umin_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_umin_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_umin_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_umin_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_umin_amd nir_build_global_atomic_umin_amd
#ifdef __cplusplus
#define nir_build_global_atomic_umin_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_umin_ir3(build, bit_size, src0, src1, _nir_global_atomic_umin_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_umin_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_umin_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_umin_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_umin_ir3 nir_build_global_atomic_umin_ir3
#define nir_build_global_atomic_xor _nir_build_global_atomic_xor
#define nir_global_atomic_xor nir_build_global_atomic_xor
#define nir_build_global_atomic_xor_2x32 _nir_build_global_atomic_xor_2x32
#define nir_global_atomic_xor_2x32 nir_build_global_atomic_xor_2x32
#ifdef __cplusplus
#define nir_build_global_atomic_xor_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_xor_amd(build, bit_size, src0, src1, src2, _nir_global_atomic_xor_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_xor_amd(build, bit_size, src0, src1, src2, ...) \
_nir_build_global_atomic_xor_amd(build, bit_size, src0, src1, src2, (struct _nir_global_atomic_xor_amd_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_xor_amd nir_build_global_atomic_xor_amd
#ifdef __cplusplus
#define nir_build_global_atomic_xor_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_xor_ir3(build, bit_size, src0, src1, _nir_global_atomic_xor_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_global_atomic_xor_ir3(build, bit_size, src0, src1, ...) \
_nir_build_global_atomic_xor_ir3(build, bit_size, src0, src1, (struct _nir_global_atomic_xor_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_global_atomic_xor_ir3 nir_build_global_atomic_xor_ir3
#define nir_build_group_memory_barrier _nir_build_group_memory_barrier
#define nir_group_memory_barrier nir_build_group_memory_barrier
#define nir_build_ignore_ray_intersection _nir_build_ignore_ray_intersection
#define nir_ignore_ray_intersection nir_build_ignore_ray_intersection
#ifdef __cplusplus
#define nir_build_image_atomic_add(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_add(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_add(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_add(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_add nir_build_image_atomic_add
#ifdef __cplusplus
#define nir_build_image_atomic_and(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_and(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_and(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_and(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_and nir_build_image_atomic_and
#ifdef __cplusplus
#define nir_build_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, _nir_image_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_image_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, (struct _nir_image_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_comp_swap nir_build_image_atomic_comp_swap
#ifdef __cplusplus
#define nir_build_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_dec_wrap_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_dec_wrap_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_dec_wrap nir_build_image_atomic_dec_wrap
#ifdef __cplusplus
#define nir_build_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_exchange(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_exchange nir_build_image_atomic_exchange
#ifdef __cplusplus
#define nir_build_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_fadd(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_fadd nir_build_image_atomic_fadd
#ifdef __cplusplus
#define nir_build_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_fmax(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_fmax nir_build_image_atomic_fmax
#ifdef __cplusplus
#define nir_build_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_fmin(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_fmin nir_build_image_atomic_fmin
#ifdef __cplusplus
#define nir_build_image_atomic_imax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_imax(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_imax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_imax(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_imax nir_build_image_atomic_imax
#ifdef __cplusplus
#define nir_build_image_atomic_imin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_imin(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_imin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_imin(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_imin nir_build_image_atomic_imin
#ifdef __cplusplus
#define nir_build_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_inc_wrap_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_inc_wrap_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_inc_wrap nir_build_image_atomic_inc_wrap
#ifdef __cplusplus
#define nir_build_image_atomic_or(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_or(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_or(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_or(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_or nir_build_image_atomic_or
#ifdef __cplusplus
#define nir_build_image_atomic_umax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_umax(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_umax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_umax(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_umax nir_build_image_atomic_umax
#ifdef __cplusplus
#define nir_build_image_atomic_umin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_umin(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_umin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_umin(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_umin nir_build_image_atomic_umin
#ifdef __cplusplus
#define nir_build_image_atomic_xor(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_xor(build, bit_size, src0, src1, src2, src3, _nir_image_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_image_atomic_xor(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_atomic_xor(build, bit_size, src0, src1, src2, src3, (struct _nir_image_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_image_atomic_xor nir_build_image_atomic_xor
#ifdef __cplusplus
#define nir_build_image_deref_atomic_add(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_add(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_add(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_add(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_add nir_build_image_deref_atomic_add
#ifdef __cplusplus
#define nir_build_image_deref_atomic_and(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_and(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_and(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_and(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_and nir_build_image_deref_atomic_and
#ifdef __cplusplus
#define nir_build_image_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_image_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, _nir_image_deref_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_image_deref_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, src4, (struct _nir_image_deref_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_comp_swap nir_build_image_deref_atomic_comp_swap
#ifdef __cplusplus
#define nir_build_image_deref_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_dec_wrap_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_dec_wrap(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_dec_wrap_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_dec_wrap nir_build_image_deref_atomic_dec_wrap
#ifdef __cplusplus
#define nir_build_image_deref_atomic_exchange(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_exchange(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_exchange(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_exchange(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_exchange nir_build_image_deref_atomic_exchange
#ifdef __cplusplus
#define nir_build_image_deref_atomic_fadd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_fadd(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_fadd(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_fadd(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_fadd nir_build_image_deref_atomic_fadd
#ifdef __cplusplus
#define nir_build_image_deref_atomic_fmax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_fmax(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_fmax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_fmax(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_fmax nir_build_image_deref_atomic_fmax
#ifdef __cplusplus
#define nir_build_image_deref_atomic_fmin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_fmin(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_fmin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_fmin(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_fmin nir_build_image_deref_atomic_fmin
#ifdef __cplusplus
#define nir_build_image_deref_atomic_imax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_imax(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_imax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_imax(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_imax nir_build_image_deref_atomic_imax
#ifdef __cplusplus
#define nir_build_image_deref_atomic_imin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_imin(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_imin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_imin(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_imin nir_build_image_deref_atomic_imin
#ifdef __cplusplus
#define nir_build_image_deref_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_inc_wrap_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_inc_wrap(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_inc_wrap_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_inc_wrap nir_build_image_deref_atomic_inc_wrap
#ifdef __cplusplus
#define nir_build_image_deref_atomic_or(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_or(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_or(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_or(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_or nir_build_image_deref_atomic_or
#ifdef __cplusplus
#define nir_build_image_deref_atomic_umax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_umax(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_umax(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_umax(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_umax nir_build_image_deref_atomic_umax
#ifdef __cplusplus
#define nir_build_image_deref_atomic_umin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_umin(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_umin(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_umin(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_umin nir_build_image_deref_atomic_umin
#ifdef __cplusplus
#define nir_build_image_deref_atomic_xor(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_xor(build, bit_size, src0, src1, src2, src3, _nir_image_deref_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_atomic_xor(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_atomic_xor(build, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_atomic_xor nir_build_image_deref_atomic_xor
#ifdef __cplusplus
#define nir_build_image_deref_descriptor_amd(build, num_components, bit_size, src0, ...) \
_nir_build_image_deref_descriptor_amd(build, num_components, bit_size, src0, _nir_image_deref_descriptor_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_descriptor_amd(build, num_components, bit_size, src0, ...) \
_nir_build_image_deref_descriptor_amd(build, num_components, bit_size, src0, (struct _nir_image_deref_descriptor_amd_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_descriptor_amd nir_build_image_deref_descriptor_amd
#ifdef __cplusplus
#define nir_build_image_deref_format(build, bit_size, src0, ...) \
_nir_build_image_deref_format(build, bit_size, src0, _nir_image_deref_format_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_format(build, bit_size, src0, ...) \
_nir_build_image_deref_format(build, bit_size, src0, (struct _nir_image_deref_format_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_format nir_build_image_deref_format
#ifdef __cplusplus
#define nir_build_image_deref_fragment_mask_load_amd(build, src0, src1, ...) \
_nir_build_image_deref_fragment_mask_load_amd(build, src0, src1, _nir_image_deref_fragment_mask_load_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_fragment_mask_load_amd(build, src0, src1, ...) \
_nir_build_image_deref_fragment_mask_load_amd(build, src0, src1, (struct _nir_image_deref_fragment_mask_load_amd_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_fragment_mask_load_amd nir_build_image_deref_fragment_mask_load_amd
#ifdef __cplusplus
#define nir_build_image_deref_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_load(build, num_components, bit_size, src0, src1, src2, src3, _nir_image_deref_load_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_load(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_load_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_load nir_build_image_deref_load
#ifdef __cplusplus
#define nir_build_image_deref_load_param_intel(build, num_components, bit_size, src0, ...) \
_nir_build_image_deref_load_param_intel(build, num_components, bit_size, src0, _nir_image_deref_load_param_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_load_param_intel(build, num_components, bit_size, src0, ...) \
_nir_build_image_deref_load_param_intel(build, num_components, bit_size, src0, (struct _nir_image_deref_load_param_intel_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_load_param_intel nir_build_image_deref_load_param_intel
#ifdef __cplusplus
#define nir_build_image_deref_load_raw_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_deref_load_raw_intel(build, num_components, bit_size, src0, src1, _nir_image_deref_load_raw_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_load_raw_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_deref_load_raw_intel(build, num_components, bit_size, src0, src1, (struct _nir_image_deref_load_raw_intel_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_load_raw_intel nir_build_image_deref_load_raw_intel
#ifdef __cplusplus
#define nir_build_image_deref_order(build, bit_size, src0, ...) \
_nir_build_image_deref_order(build, bit_size, src0, _nir_image_deref_order_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_order(build, bit_size, src0, ...) \
_nir_build_image_deref_order(build, bit_size, src0, (struct _nir_image_deref_order_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_order nir_build_image_deref_order
#ifdef __cplusplus
#define nir_build_image_deref_samples(build, bit_size, src0, ...) \
_nir_build_image_deref_samples(build, bit_size, src0, _nir_image_deref_samples_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_samples(build, bit_size, src0, ...) \
_nir_build_image_deref_samples(build, bit_size, src0, (struct _nir_image_deref_samples_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_samples nir_build_image_deref_samples
#ifdef __cplusplus
#define nir_build_image_deref_samples_identical(build, bit_size, src0, src1, ...) \
_nir_build_image_deref_samples_identical(build, bit_size, src0, src1, _nir_image_deref_samples_identical_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_samples_identical(build, bit_size, src0, src1, ...) \
_nir_build_image_deref_samples_identical(build, bit_size, src0, src1, (struct _nir_image_deref_samples_identical_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_samples_identical nir_build_image_deref_samples_identical
#ifdef __cplusplus
#define nir_build_image_deref_size(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_deref_size(build, num_components, bit_size, src0, src1, _nir_image_deref_size_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_size(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_deref_size(build, num_components, bit_size, src0, src1, (struct _nir_image_deref_size_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_size nir_build_image_deref_size
#ifdef __cplusplus
#define nir_build_image_deref_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, _nir_image_deref_sparse_load_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_deref_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_image_deref_sparse_load_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_sparse_load nir_build_image_deref_sparse_load
#ifdef __cplusplus
#define nir_build_image_deref_store(build, src0, src1, src2, src3, src4, ...) \
_nir_build_image_deref_store(build, src0, src1, src2, src3, src4, _nir_image_deref_store_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_store(build, src0, src1, src2, src3, src4, ...) \
_nir_build_image_deref_store(build, src0, src1, src2, src3, src4, (struct _nir_image_deref_store_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_store nir_build_image_deref_store
#ifdef __cplusplus
#define nir_build_image_deref_store_raw_intel(build, src0, src1, src2, ...) \
_nir_build_image_deref_store_raw_intel(build, src0, src1, src2, _nir_image_deref_store_raw_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_image_deref_store_raw_intel(build, src0, src1, src2, ...) \
_nir_build_image_deref_store_raw_intel(build, src0, src1, src2, (struct _nir_image_deref_store_raw_intel_indices){0, __VA_ARGS__})
#endif
#define nir_image_deref_store_raw_intel nir_build_image_deref_store_raw_intel
#ifdef __cplusplus
#define nir_build_image_descriptor_amd(build, num_components, bit_size, src0, ...) \
_nir_build_image_descriptor_amd(build, num_components, bit_size, src0, _nir_image_descriptor_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_image_descriptor_amd(build, num_components, bit_size, src0, ...) \
_nir_build_image_descriptor_amd(build, num_components, bit_size, src0, (struct _nir_image_descriptor_amd_indices){0, __VA_ARGS__})
#endif
#define nir_image_descriptor_amd nir_build_image_descriptor_amd
#ifdef __cplusplus
#define nir_build_image_format(build, bit_size, src0, ...) \
_nir_build_image_format(build, bit_size, src0, _nir_image_format_indices{0, __VA_ARGS__})
#else
#define nir_build_image_format(build, bit_size, src0, ...) \
_nir_build_image_format(build, bit_size, src0, (struct _nir_image_format_indices){0, __VA_ARGS__})
#endif
#define nir_image_format nir_build_image_format
#ifdef __cplusplus
#define nir_build_image_fragment_mask_load_amd(build, src0, src1, ...) \
_nir_build_image_fragment_mask_load_amd(build, src0, src1, _nir_image_fragment_mask_load_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_image_fragment_mask_load_amd(build, src0, src1, ...) \
_nir_build_image_fragment_mask_load_amd(build, src0, src1, (struct _nir_image_fragment_mask_load_amd_indices){0, __VA_ARGS__})
#endif
#define nir_image_fragment_mask_load_amd nir_build_image_fragment_mask_load_amd
#ifdef __cplusplus
#define nir_build_image_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_load(build, num_components, bit_size, src0, src1, src2, src3, _nir_image_load_indices{0, __VA_ARGS__})
#else
#define nir_build_image_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_load(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_image_load_indices){0, __VA_ARGS__})
#endif
#define nir_image_load nir_build_image_load
#ifdef __cplusplus
#define nir_build_image_load_raw_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_load_raw_intel(build, num_components, bit_size, src0, src1, _nir_image_load_raw_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_image_load_raw_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_load_raw_intel(build, num_components, bit_size, src0, src1, (struct _nir_image_load_raw_intel_indices){0, __VA_ARGS__})
#endif
#define nir_image_load_raw_intel nir_build_image_load_raw_intel
#ifdef __cplusplus
#define nir_build_image_order(build, bit_size, src0, ...) \
_nir_build_image_order(build, bit_size, src0, _nir_image_order_indices{0, __VA_ARGS__})
#else
#define nir_build_image_order(build, bit_size, src0, ...) \
_nir_build_image_order(build, bit_size, src0, (struct _nir_image_order_indices){0, __VA_ARGS__})
#endif
#define nir_image_order nir_build_image_order
#ifdef __cplusplus
#define nir_build_image_samples(build, bit_size, src0, ...) \
_nir_build_image_samples(build, bit_size, src0, _nir_image_samples_indices{0, __VA_ARGS__})
#else
#define nir_build_image_samples(build, bit_size, src0, ...) \
_nir_build_image_samples(build, bit_size, src0, (struct _nir_image_samples_indices){0, __VA_ARGS__})
#endif
#define nir_image_samples nir_build_image_samples
#ifdef __cplusplus
#define nir_build_image_samples_identical(build, bit_size, src0, src1, ...) \
_nir_build_image_samples_identical(build, bit_size, src0, src1, _nir_image_samples_identical_indices{0, __VA_ARGS__})
#else
#define nir_build_image_samples_identical(build, bit_size, src0, src1, ...) \
_nir_build_image_samples_identical(build, bit_size, src0, src1, (struct _nir_image_samples_identical_indices){0, __VA_ARGS__})
#endif
#define nir_image_samples_identical nir_build_image_samples_identical
#ifdef __cplusplus
#define nir_build_image_size(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_size(build, num_components, bit_size, src0, src1, _nir_image_size_indices{0, __VA_ARGS__})
#else
#define nir_build_image_size(build, num_components, bit_size, src0, src1, ...) \
_nir_build_image_size(build, num_components, bit_size, src0, src1, (struct _nir_image_size_indices){0, __VA_ARGS__})
#endif
#define nir_image_size nir_build_image_size
#ifdef __cplusplus
#define nir_build_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, _nir_image_sparse_load_indices{0, __VA_ARGS__})
#else
#define nir_build_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_image_sparse_load(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_image_sparse_load_indices){0, __VA_ARGS__})
#endif
#define nir_image_sparse_load nir_build_image_sparse_load
#ifdef __cplusplus
#define nir_build_image_store(build, src0, src1, src2, src3, src4, ...) \
_nir_build_image_store(build, src0, src1, src2, src3, src4, _nir_image_store_indices{0, __VA_ARGS__})
#else
#define nir_build_image_store(build, src0, src1, src2, src3, src4, ...) \
_nir_build_image_store(build, src0, src1, src2, src3, src4, (struct _nir_image_store_indices){0, __VA_ARGS__})
#endif
#define nir_image_store nir_build_image_store
#ifdef __cplusplus
#define nir_build_image_store_raw_intel(build, src0, src1, src2, ...) \
_nir_build_image_store_raw_intel(build, src0, src1, src2, _nir_image_store_raw_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_image_store_raw_intel(build, src0, src1, src2, ...) \
_nir_build_image_store_raw_intel(build, src0, src1, src2, (struct _nir_image_store_raw_intel_indices){0, __VA_ARGS__})
#endif
#define nir_image_store_raw_intel nir_build_image_store_raw_intel
#ifdef __cplusplus
#define nir_build_inclusive_scan(build, src0, ...) \
_nir_build_inclusive_scan(build, src0, _nir_inclusive_scan_indices{0, __VA_ARGS__})
#else
#define nir_build_inclusive_scan(build, src0, ...) \
_nir_build_inclusive_scan(build, src0, (struct _nir_inclusive_scan_indices){0, __VA_ARGS__})
#endif
#define nir_inclusive_scan nir_build_inclusive_scan
#define nir_build_interp_deref_at_centroid _nir_build_interp_deref_at_centroid
#define nir_interp_deref_at_centroid nir_build_interp_deref_at_centroid
#define nir_build_interp_deref_at_offset _nir_build_interp_deref_at_offset
#define nir_interp_deref_at_offset nir_build_interp_deref_at_offset
#define nir_build_interp_deref_at_sample _nir_build_interp_deref_at_sample
#define nir_interp_deref_at_sample nir_build_interp_deref_at_sample
#define nir_build_interp_deref_at_vertex _nir_build_interp_deref_at_vertex
#define nir_interp_deref_at_vertex nir_build_interp_deref_at_vertex
#define nir_build_is_helper_invocation _nir_build_is_helper_invocation
#define nir_is_helper_invocation nir_build_is_helper_invocation
#define nir_build_is_sparse_texels_resident _nir_build_is_sparse_texels_resident
#define nir_is_sparse_texels_resident nir_build_is_sparse_texels_resident
#define nir_build_is_subgroup_invocation_lt_amd _nir_build_is_subgroup_invocation_lt_amd
#define nir_is_subgroup_invocation_lt_amd nir_build_is_subgroup_invocation_lt_amd
#define nir_build_lane_permute_16_amd _nir_build_lane_permute_16_amd
#define nir_lane_permute_16_amd nir_build_lane_permute_16_amd
#define nir_build_last_invocation _nir_build_last_invocation
#define nir_last_invocation nir_build_last_invocation
#ifdef __cplusplus
#define nir_build_launch_mesh_workgroups(build, src0, ...) \
_nir_build_launch_mesh_workgroups(build, src0, _nir_launch_mesh_workgroups_indices{0, __VA_ARGS__})
#else
#define nir_build_launch_mesh_workgroups(build, src0, ...) \
_nir_build_launch_mesh_workgroups(build, src0, (struct _nir_launch_mesh_workgroups_indices){0, __VA_ARGS__})
#endif
#define nir_launch_mesh_workgroups nir_build_launch_mesh_workgroups
#define nir_build_launch_mesh_workgroups_with_payload_deref _nir_build_launch_mesh_workgroups_with_payload_deref
#define nir_launch_mesh_workgroups_with_payload_deref nir_build_launch_mesh_workgroups_with_payload_deref
#define nir_build_load_aa_line_width _nir_build_load_aa_line_width
#define nir_load_aa_line_width nir_build_load_aa_line_width
#define nir_build_load_accel_struct_amd _nir_build_load_accel_struct_amd
#define nir_load_accel_struct_amd nir_build_load_accel_struct_amd
#ifdef __cplusplus
#define nir_build_load_agx(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_agx(build, num_components, bit_size, src0, src1, _nir_load_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_load_agx(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_agx(build, num_components, bit_size, src0, src1, (struct _nir_load_agx_indices){0, __VA_ARGS__})
#endif
#define nir_load_agx nir_build_load_agx
#define nir_build_load_alpha_reference_amd _nir_build_load_alpha_reference_amd
#define nir_load_alpha_reference_amd nir_build_load_alpha_reference_amd
#define nir_build_load_back_face_agx _nir_build_load_back_face_agx
#define nir_load_back_face_agx nir_build_load_back_face_agx
#ifdef __cplusplus
#define nir_build_load_barycentric_at_offset(build, bit_size, src0, ...) \
_nir_build_load_barycentric_at_offset(build, bit_size, src0, _nir_load_barycentric_at_offset_indices{0, __VA_ARGS__})
#else
#define nir_build_load_barycentric_at_offset(build, bit_size, src0, ...) \
_nir_build_load_barycentric_at_offset(build, bit_size, src0, (struct _nir_load_barycentric_at_offset_indices){0, __VA_ARGS__})
#endif
#define nir_load_barycentric_at_offset nir_build_load_barycentric_at_offset
#ifdef __cplusplus
#define nir_build_load_barycentric_at_sample(build, bit_size, src0, ...) \
_nir_build_load_barycentric_at_sample(build, bit_size, src0, _nir_load_barycentric_at_sample_indices{0, __VA_ARGS__})
#else
#define nir_build_load_barycentric_at_sample(build, bit_size, src0, ...) \
_nir_build_load_barycentric_at_sample(build, bit_size, src0, (struct _nir_load_barycentric_at_sample_indices){0, __VA_ARGS__})
#endif
#define nir_load_barycentric_at_sample nir_build_load_barycentric_at_sample
#ifdef __cplusplus
#define nir_build_load_barycentric_centroid(build, bit_size, ...) \
_nir_build_load_barycentric_centroid(build, bit_size, _nir_load_barycentric_centroid_indices{0, __VA_ARGS__})
#else
#define nir_build_load_barycentric_centroid(build, bit_size, ...) \
_nir_build_load_barycentric_centroid(build, bit_size, (struct _nir_load_barycentric_centroid_indices){0, __VA_ARGS__})
#endif
#define nir_load_barycentric_centroid nir_build_load_barycentric_centroid
#ifdef __cplusplus
#define nir_build_load_barycentric_model(build, bit_size, ...) \
_nir_build_load_barycentric_model(build, bit_size, _nir_load_barycentric_model_indices{0, __VA_ARGS__})
#else
#define nir_build_load_barycentric_model(build, bit_size, ...) \
_nir_build_load_barycentric_model(build, bit_size, (struct _nir_load_barycentric_model_indices){0, __VA_ARGS__})
#endif
#define nir_load_barycentric_model nir_build_load_barycentric_model
#ifdef __cplusplus
#define nir_build_load_barycentric_pixel(build, bit_size, ...) \
_nir_build_load_barycentric_pixel(build, bit_size, _nir_load_barycentric_pixel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_barycentric_pixel(build, bit_size, ...) \
_nir_build_load_barycentric_pixel(build, bit_size, (struct _nir_load_barycentric_pixel_indices){0, __VA_ARGS__})
#endif
#define nir_load_barycentric_pixel nir_build_load_barycentric_pixel
#ifdef __cplusplus
#define nir_build_load_barycentric_sample(build, bit_size, ...) \
_nir_build_load_barycentric_sample(build, bit_size, _nir_load_barycentric_sample_indices{0, __VA_ARGS__})
#else
#define nir_build_load_barycentric_sample(build, bit_size, ...) \
_nir_build_load_barycentric_sample(build, bit_size, (struct _nir_load_barycentric_sample_indices){0, __VA_ARGS__})
#endif
#define nir_load_barycentric_sample nir_build_load_barycentric_sample
#define nir_build_load_base_global_invocation_id _nir_build_load_base_global_invocation_id
#define nir_load_base_global_invocation_id nir_build_load_base_global_invocation_id
#define nir_build_load_base_instance _nir_build_load_base_instance
#define nir_load_base_instance nir_build_load_base_instance
#define nir_build_load_base_vertex _nir_build_load_base_vertex
#define nir_load_base_vertex nir_build_load_base_vertex
#define nir_build_load_base_workgroup_id _nir_build_load_base_workgroup_id
#define nir_load_base_workgroup_id nir_build_load_base_workgroup_id
#define nir_build_load_blend_const_color_a_float _nir_build_load_blend_const_color_a_float
#define nir_load_blend_const_color_a_float nir_build_load_blend_const_color_a_float
#define nir_build_load_blend_const_color_aaaa8888_unorm _nir_build_load_blend_const_color_aaaa8888_unorm
#define nir_load_blend_const_color_aaaa8888_unorm nir_build_load_blend_const_color_aaaa8888_unorm
#define nir_build_load_blend_const_color_b_float _nir_build_load_blend_const_color_b_float
#define nir_load_blend_const_color_b_float nir_build_load_blend_const_color_b_float
#define nir_build_load_blend_const_color_g_float _nir_build_load_blend_const_color_g_float
#define nir_load_blend_const_color_g_float nir_build_load_blend_const_color_g_float
#define nir_build_load_blend_const_color_r_float _nir_build_load_blend_const_color_r_float
#define nir_load_blend_const_color_r_float nir_build_load_blend_const_color_r_float
#define nir_build_load_blend_const_color_rgba _nir_build_load_blend_const_color_rgba
#define nir_load_blend_const_color_rgba nir_build_load_blend_const_color_rgba
#define nir_build_load_blend_const_color_rgba8888_unorm _nir_build_load_blend_const_color_rgba8888_unorm
#define nir_load_blend_const_color_rgba8888_unorm nir_build_load_blend_const_color_rgba8888_unorm
#define nir_build_load_btd_global_arg_addr_intel _nir_build_load_btd_global_arg_addr_intel
#define nir_load_btd_global_arg_addr_intel nir_build_load_btd_global_arg_addr_intel
#define nir_build_load_btd_local_arg_addr_intel _nir_build_load_btd_local_arg_addr_intel
#define nir_load_btd_local_arg_addr_intel nir_build_load_btd_local_arg_addr_intel
#define nir_build_load_btd_resume_sbt_addr_intel _nir_build_load_btd_resume_sbt_addr_intel
#define nir_load_btd_resume_sbt_addr_intel nir_build_load_btd_resume_sbt_addr_intel
#define nir_build_load_btd_shader_type_intel _nir_build_load_btd_shader_type_intel
#define nir_load_btd_shader_type_intel nir_build_load_btd_shader_type_intel
#define nir_build_load_btd_stack_id_intel _nir_build_load_btd_stack_id_intel
#define nir_load_btd_stack_id_intel nir_build_load_btd_stack_id_intel
#ifdef __cplusplus
#define nir_build_load_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_load_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, _nir_load_buffer_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_load_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_load_buffer_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_buffer_amd nir_build_load_buffer_amd
#define nir_build_load_callable_sbt_addr_intel _nir_build_load_callable_sbt_addr_intel
#define nir_load_callable_sbt_addr_intel nir_build_load_callable_sbt_addr_intel
#define nir_build_load_callable_sbt_stride_intel _nir_build_load_callable_sbt_stride_intel
#define nir_load_callable_sbt_stride_intel nir_build_load_callable_sbt_stride_intel
#define nir_build_load_clamp_vertex_color_amd _nir_build_load_clamp_vertex_color_amd
#define nir_load_clamp_vertex_color_amd nir_build_load_clamp_vertex_color_amd
#define nir_build_load_clip_half_line_width_amd _nir_build_load_clip_half_line_width_amd
#define nir_load_clip_half_line_width_amd nir_build_load_clip_half_line_width_amd
#define nir_build_load_color0 _nir_build_load_color0
#define nir_load_color0 nir_build_load_color0
#define nir_build_load_color1 _nir_build_load_color1
#define nir_load_color1 nir_build_load_color1
#ifdef __cplusplus
#define nir_build_load_constant(build, num_components, bit_size, src0, ...) \
_nir_build_load_constant(build, num_components, bit_size, src0, _nir_load_constant_indices{0, __VA_ARGS__})
#else
#define nir_build_load_constant(build, num_components, bit_size, src0, ...) \
_nir_build_load_constant(build, num_components, bit_size, src0, (struct _nir_load_constant_indices){0, __VA_ARGS__})
#endif
#define nir_load_constant nir_build_load_constant
#ifdef __cplusplus
#define nir_build_load_constant_agx(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_constant_agx(build, num_components, bit_size, src0, src1, _nir_load_constant_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_load_constant_agx(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_constant_agx(build, num_components, bit_size, src0, src1, (struct _nir_load_constant_agx_indices){0, __VA_ARGS__})
#endif
#define nir_load_constant_agx nir_build_load_constant_agx
#define nir_build_load_constant_base_ptr _nir_build_load_constant_base_ptr
#define nir_load_constant_base_ptr nir_build_load_constant_base_ptr
#ifdef __cplusplus
#define nir_build_load_converted_output_pan(build, num_components, bit_size, src0, ...) \
_nir_build_load_converted_output_pan(build, num_components, bit_size, src0, _nir_load_converted_output_pan_indices{0, __VA_ARGS__})
#else
#define nir_build_load_converted_output_pan(build, num_components, bit_size, src0, ...) \
_nir_build_load_converted_output_pan(build, num_components, bit_size, src0, (struct _nir_load_converted_output_pan_indices){0, __VA_ARGS__})
#endif
#define nir_load_converted_output_pan nir_build_load_converted_output_pan
#define nir_build_load_coverage_mask_pan _nir_build_load_coverage_mask_pan
#define nir_load_coverage_mask_pan nir_build_load_coverage_mask_pan
#define nir_build_load_cull_any_enabled_amd _nir_build_load_cull_any_enabled_amd
#define nir_load_cull_any_enabled_amd nir_build_load_cull_any_enabled_amd
#define nir_build_load_cull_back_face_enabled_amd _nir_build_load_cull_back_face_enabled_amd
#define nir_load_cull_back_face_enabled_amd nir_build_load_cull_back_face_enabled_amd
#define nir_build_load_cull_ccw_amd _nir_build_load_cull_ccw_amd
#define nir_load_cull_ccw_amd nir_build_load_cull_ccw_amd
#define nir_build_load_cull_front_face_enabled_amd _nir_build_load_cull_front_face_enabled_amd
#define nir_load_cull_front_face_enabled_amd nir_build_load_cull_front_face_enabled_amd
#define nir_build_load_cull_mask _nir_build_load_cull_mask
#define nir_load_cull_mask nir_build_load_cull_mask
#define nir_build_load_cull_mask_and_flags_amd _nir_build_load_cull_mask_and_flags_amd
#define nir_load_cull_mask_and_flags_amd nir_build_load_cull_mask_and_flags_amd
#define nir_build_load_cull_small_prim_precision_amd _nir_build_load_cull_small_prim_precision_amd
#define nir_load_cull_small_prim_precision_amd nir_build_load_cull_small_prim_precision_amd
#define nir_build_load_cull_small_primitives_enabled_amd _nir_build_load_cull_small_primitives_enabled_amd
#define nir_load_cull_small_primitives_enabled_amd nir_build_load_cull_small_primitives_enabled_amd
#ifdef __cplusplus
#define nir_build_load_deref(build, num_components, bit_size, src0, ...) \
_nir_build_load_deref(build, num_components, bit_size, src0, _nir_load_deref_indices{0, __VA_ARGS__})
#else
#define nir_build_load_deref(build, num_components, bit_size, src0, ...) \
_nir_build_load_deref(build, num_components, bit_size, src0, (struct _nir_load_deref_indices){0, __VA_ARGS__})
#endif
#define nir_load_deref nir_build_load_deref
#ifdef __cplusplus
#define nir_build_load_deref_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_deref_block_intel(build, num_components, bit_size, src0, _nir_load_deref_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_deref_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_deref_block_intel(build, num_components, bit_size, src0, (struct _nir_load_deref_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_deref_block_intel nir_build_load_deref_block_intel
#define nir_build_load_desc_set_address_intel _nir_build_load_desc_set_address_intel
#define nir_load_desc_set_address_intel nir_build_load_desc_set_address_intel
#define nir_build_load_draw_id _nir_build_load_draw_id
#define nir_load_draw_id nir_build_load_draw_id
#define nir_build_load_esgs_vertex_stride_amd _nir_build_load_esgs_vertex_stride_amd
#define nir_load_esgs_vertex_stride_amd nir_build_load_esgs_vertex_stride_amd
#define nir_build_load_fb_layers_v3d _nir_build_load_fb_layers_v3d
#define nir_load_fb_layers_v3d nir_build_load_fb_layers_v3d
#define nir_build_load_first_vertex _nir_build_load_first_vertex
#define nir_load_first_vertex nir_build_load_first_vertex
#define nir_build_load_force_vrs_rates_amd _nir_build_load_force_vrs_rates_amd
#define nir_load_force_vrs_rates_amd nir_build_load_force_vrs_rates_amd
#define nir_build_load_frag_coord _nir_build_load_frag_coord
#define nir_load_frag_coord nir_build_load_frag_coord
#define nir_build_load_frag_invocation_count _nir_build_load_frag_invocation_count
#define nir_load_frag_invocation_count nir_build_load_frag_invocation_count
#define nir_build_load_frag_shading_rate _nir_build_load_frag_shading_rate
#define nir_load_frag_shading_rate nir_build_load_frag_shading_rate
#define nir_build_load_frag_size _nir_build_load_frag_size
#define nir_load_frag_size nir_build_load_frag_size
#define nir_build_load_front_face _nir_build_load_front_face
#define nir_load_front_face nir_build_load_front_face
#ifdef __cplusplus
#define nir_build_load_fs_input_interp_deltas(build, bit_size, src0, ...) \
_nir_build_load_fs_input_interp_deltas(build, bit_size, src0, _nir_load_fs_input_interp_deltas_indices{0, __VA_ARGS__})
#else
#define nir_build_load_fs_input_interp_deltas(build, bit_size, src0, ...) \
_nir_build_load_fs_input_interp_deltas(build, bit_size, src0, (struct _nir_load_fs_input_interp_deltas_indices){0, __VA_ARGS__})
#endif
#define nir_load_fs_input_interp_deltas nir_build_load_fs_input_interp_deltas
#define nir_build_load_fully_covered _nir_build_load_fully_covered
#define nir_load_fully_covered nir_build_load_fully_covered
#ifdef __cplusplus
#define nir_build_load_global(build, num_components, bit_size, src0, ...) \
_nir_build_load_global(build, num_components, bit_size, src0, _nir_load_global_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global(build, num_components, bit_size, src0, ...) \
_nir_build_load_global(build, num_components, bit_size, src0, (struct _nir_load_global_indices){0, __VA_ARGS__})
#endif
#define nir_load_global nir_build_load_global
#ifdef __cplusplus
#define nir_build_load_global_2x32(build, num_components, bit_size, src0, ...) \
_nir_build_load_global_2x32(build, num_components, bit_size, src0, _nir_load_global_2x32_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_2x32(build, num_components, bit_size, src0, ...) \
_nir_build_load_global_2x32(build, num_components, bit_size, src0, (struct _nir_load_global_2x32_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_2x32 nir_build_load_global_2x32
#ifdef __cplusplus
#define nir_build_load_global_amd(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_global_amd(build, num_components, bit_size, src0, src1, _nir_load_global_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_amd(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_global_amd(build, num_components, bit_size, src0, src1, (struct _nir_load_global_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_amd nir_build_load_global_amd
#define nir_build_load_global_base_ptr _nir_build_load_global_base_ptr
#define nir_load_global_base_ptr nir_build_load_global_base_ptr
#ifdef __cplusplus
#define nir_build_load_global_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_global_block_intel(build, num_components, bit_size, src0, _nir_load_global_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_global_block_intel(build, num_components, bit_size, src0, (struct _nir_load_global_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_block_intel nir_build_load_global_block_intel
#ifdef __cplusplus
#define nir_build_load_global_const_block_intel(build, num_components, src0, src1, ...) \
_nir_build_load_global_const_block_intel(build, num_components, src0, src1, _nir_load_global_const_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_const_block_intel(build, num_components, src0, src1, ...) \
_nir_build_load_global_const_block_intel(build, num_components, src0, src1, (struct _nir_load_global_const_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_const_block_intel nir_build_load_global_const_block_intel
#ifdef __cplusplus
#define nir_build_load_global_constant(build, num_components, bit_size, src0, ...) \
_nir_build_load_global_constant(build, num_components, bit_size, src0, _nir_load_global_constant_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_constant(build, num_components, bit_size, src0, ...) \
_nir_build_load_global_constant(build, num_components, bit_size, src0, (struct _nir_load_global_constant_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_constant nir_build_load_global_constant
#ifdef __cplusplus
#define nir_build_load_global_constant_bounded(build, num_components, bit_size, src0, src1, src2, ...) \
_nir_build_load_global_constant_bounded(build, num_components, bit_size, src0, src1, src2, _nir_load_global_constant_bounded_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_constant_bounded(build, num_components, bit_size, src0, src1, src2, ...) \
_nir_build_load_global_constant_bounded(build, num_components, bit_size, src0, src1, src2, (struct _nir_load_global_constant_bounded_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_constant_bounded nir_build_load_global_constant_bounded
#ifdef __cplusplus
#define nir_build_load_global_constant_offset(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_global_constant_offset(build, num_components, bit_size, src0, src1, _nir_load_global_constant_offset_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_constant_offset(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_global_constant_offset(build, num_components, bit_size, src0, src1, (struct _nir_load_global_constant_offset_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_constant_offset nir_build_load_global_constant_offset
#define nir_build_load_global_invocation_id _nir_build_load_global_invocation_id
#define nir_load_global_invocation_id nir_build_load_global_invocation_id
#define nir_build_load_global_invocation_id_zero_base _nir_build_load_global_invocation_id_zero_base
#define nir_load_global_invocation_id_zero_base nir_build_load_global_invocation_id_zero_base
#define nir_build_load_global_invocation_index _nir_build_load_global_invocation_index
#define nir_load_global_invocation_index nir_build_load_global_invocation_index
#ifdef __cplusplus
#define nir_build_load_global_ir3(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_global_ir3(build, num_components, bit_size, src0, src1, _nir_load_global_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_load_global_ir3(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_global_ir3(build, num_components, bit_size, src0, src1, (struct _nir_load_global_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_load_global_ir3 nir_build_load_global_ir3
#define nir_build_load_gs_header_ir3 _nir_build_load_gs_header_ir3
#define nir_load_gs_header_ir3 nir_build_load_gs_header_ir3
#ifdef __cplusplus
#define nir_build_load_gs_vertex_offset_amd(build, ...) \
_nir_build_load_gs_vertex_offset_amd(build, _nir_load_gs_vertex_offset_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_gs_vertex_offset_amd(build, ...) \
_nir_build_load_gs_vertex_offset_amd(build, (struct _nir_load_gs_vertex_offset_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_gs_vertex_offset_amd nir_build_load_gs_vertex_offset_amd
#define nir_build_load_helper_invocation _nir_build_load_helper_invocation
#define nir_load_helper_invocation nir_build_load_helper_invocation
#ifdef __cplusplus
#define nir_build_load_hit_attrib_amd(build, ...) \
_nir_build_load_hit_attrib_amd(build, _nir_load_hit_attrib_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_hit_attrib_amd(build, ...) \
_nir_build_load_hit_attrib_amd(build, (struct _nir_load_hit_attrib_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_hit_attrib_amd nir_build_load_hit_attrib_amd
#define nir_build_load_hs_out_patch_data_offset_amd _nir_build_load_hs_out_patch_data_offset_amd
#define nir_load_hs_out_patch_data_offset_amd nir_build_load_hs_out_patch_data_offset_amd
#define nir_build_load_hs_patch_stride_ir3 _nir_build_load_hs_patch_stride_ir3
#define nir_load_hs_patch_stride_ir3 nir_build_load_hs_patch_stride_ir3
#define nir_build_load_initial_edgeflags_amd _nir_build_load_initial_edgeflags_amd
#define nir_load_initial_edgeflags_amd nir_build_load_initial_edgeflags_amd
#ifdef __cplusplus
#define nir_build_load_input(build, num_components, bit_size, src0, ...) \
_nir_build_load_input(build, num_components, bit_size, src0, _nir_load_input_indices{0, __VA_ARGS__})
#else
#define nir_build_load_input(build, num_components, bit_size, src0, ...) \
_nir_build_load_input(build, num_components, bit_size, src0, (struct _nir_load_input_indices){0, __VA_ARGS__})
#endif
#define nir_load_input nir_build_load_input
#ifdef __cplusplus
#define nir_build_load_input_vertex(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_input_vertex(build, num_components, bit_size, src0, src1, _nir_load_input_vertex_indices{0, __VA_ARGS__})
#else
#define nir_build_load_input_vertex(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_input_vertex(build, num_components, bit_size, src0, src1, (struct _nir_load_input_vertex_indices){0, __VA_ARGS__})
#endif
#define nir_load_input_vertex nir_build_load_input_vertex
#define nir_build_load_instance_id _nir_build_load_instance_id
#define nir_load_instance_id nir_build_load_instance_id
#ifdef __cplusplus
#define nir_build_load_interpolated_input(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_interpolated_input(build, num_components, bit_size, src0, src1, _nir_load_interpolated_input_indices{0, __VA_ARGS__})
#else
#define nir_build_load_interpolated_input(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_interpolated_input(build, num_components, bit_size, src0, src1, (struct _nir_load_interpolated_input_indices){0, __VA_ARGS__})
#endif
#define nir_load_interpolated_input nir_build_load_interpolated_input
#define nir_build_load_intersection_opaque_amd _nir_build_load_intersection_opaque_amd
#define nir_load_intersection_opaque_amd nir_build_load_intersection_opaque_amd
#define nir_build_load_invocation_id _nir_build_load_invocation_id
#define nir_load_invocation_id nir_build_load_invocation_id
#define nir_build_load_is_indexed_draw _nir_build_load_is_indexed_draw
#define nir_load_is_indexed_draw nir_build_load_is_indexed_draw
#ifdef __cplusplus
#define nir_build_load_kernel_input(build, num_components, bit_size, src0, ...) \
_nir_build_load_kernel_input(build, num_components, bit_size, src0, _nir_load_kernel_input_indices{0, __VA_ARGS__})
#else
#define nir_build_load_kernel_input(build, num_components, bit_size, src0, ...) \
_nir_build_load_kernel_input(build, num_components, bit_size, src0, (struct _nir_load_kernel_input_indices){0, __VA_ARGS__})
#endif
#define nir_load_kernel_input nir_build_load_kernel_input
#define nir_build_load_layer_id _nir_build_load_layer_id
#define nir_load_layer_id nir_build_load_layer_id
#define nir_build_load_lds_ngg_gs_out_vertex_base_amd _nir_build_load_lds_ngg_gs_out_vertex_base_amd
#define nir_load_lds_ngg_gs_out_vertex_base_amd nir_build_load_lds_ngg_gs_out_vertex_base_amd
#define nir_build_load_lds_ngg_scratch_base_amd _nir_build_load_lds_ngg_scratch_base_amd
#define nir_load_lds_ngg_scratch_base_amd nir_build_load_lds_ngg_scratch_base_amd
#define nir_build_load_leaf_opaque_intel _nir_build_load_leaf_opaque_intel
#define nir_load_leaf_opaque_intel nir_build_load_leaf_opaque_intel
#define nir_build_load_leaf_procedural_intel _nir_build_load_leaf_procedural_intel
#define nir_load_leaf_procedural_intel nir_build_load_leaf_procedural_intel
#define nir_build_load_line_coord _nir_build_load_line_coord
#define nir_load_line_coord nir_build_load_line_coord
#define nir_build_load_line_width _nir_build_load_line_width
#define nir_load_line_width nir_build_load_line_width
#define nir_build_load_local_invocation_id _nir_build_load_local_invocation_id
#define nir_load_local_invocation_id nir_build_load_local_invocation_id
#define nir_build_load_local_invocation_index _nir_build_load_local_invocation_index
#define nir_load_local_invocation_index nir_build_load_local_invocation_index
#ifdef __cplusplus
#define nir_build_load_local_pixel_agx(build, num_components, bit_size, src0, ...) \
_nir_build_load_local_pixel_agx(build, num_components, bit_size, src0, _nir_load_local_pixel_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_load_local_pixel_agx(build, num_components, bit_size, src0, ...) \
_nir_build_load_local_pixel_agx(build, num_components, bit_size, src0, (struct _nir_load_local_pixel_agx_indices){0, __VA_ARGS__})
#endif
#define nir_load_local_pixel_agx nir_build_load_local_pixel_agx
#define nir_build_load_local_shared_r600 _nir_build_load_local_shared_r600
#define nir_load_local_shared_r600 nir_build_load_local_shared_r600
#define nir_build_load_lshs_vertex_stride_amd _nir_build_load_lshs_vertex_stride_amd
#define nir_load_lshs_vertex_stride_amd nir_build_load_lshs_vertex_stride_amd
#define nir_build_load_merged_wave_info_amd _nir_build_load_merged_wave_info_amd
#define nir_load_merged_wave_info_amd nir_build_load_merged_wave_info_amd
#ifdef __cplusplus
#define nir_build_load_mesh_inline_data_intel(build, bit_size, ...) \
_nir_build_load_mesh_inline_data_intel(build, bit_size, _nir_load_mesh_inline_data_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_mesh_inline_data_intel(build, bit_size, ...) \
_nir_build_load_mesh_inline_data_intel(build, bit_size, (struct _nir_load_mesh_inline_data_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_mesh_inline_data_intel nir_build_load_mesh_inline_data_intel
#define nir_build_load_mesh_view_count _nir_build_load_mesh_view_count
#define nir_load_mesh_view_count nir_build_load_mesh_view_count
#ifdef __cplusplus
#define nir_build_load_mesh_view_indices(build, num_components, bit_size, src0, ...) \
_nir_build_load_mesh_view_indices(build, num_components, bit_size, src0, _nir_load_mesh_view_indices_indices{0, __VA_ARGS__})
#else
#define nir_build_load_mesh_view_indices(build, num_components, bit_size, src0, ...) \
_nir_build_load_mesh_view_indices(build, num_components, bit_size, src0, (struct _nir_load_mesh_view_indices_indices){0, __VA_ARGS__})
#endif
#define nir_load_mesh_view_indices nir_build_load_mesh_view_indices
#define nir_build_load_multisampled_pan _nir_build_load_multisampled_pan
#define nir_load_multisampled_pan nir_build_load_multisampled_pan
#define nir_build_load_num_subgroups _nir_build_load_num_subgroups
#define nir_load_num_subgroups nir_build_load_num_subgroups
#define nir_build_load_num_vertices _nir_build_load_num_vertices
#define nir_load_num_vertices nir_build_load_num_vertices
#define nir_build_load_num_vertices_per_primitive_amd _nir_build_load_num_vertices_per_primitive_amd
#define nir_load_num_vertices_per_primitive_amd nir_build_load_num_vertices_per_primitive_amd
#define nir_build_load_num_workgroups _nir_build_load_num_workgroups
#define nir_load_num_workgroups nir_build_load_num_workgroups
#define nir_build_load_ordered_id_amd _nir_build_load_ordered_id_amd
#define nir_load_ordered_id_amd nir_build_load_ordered_id_amd
#ifdef __cplusplus
#define nir_build_load_output(build, num_components, bit_size, src0, ...) \
_nir_build_load_output(build, num_components, bit_size, src0, _nir_load_output_indices{0, __VA_ARGS__})
#else
#define nir_build_load_output(build, num_components, bit_size, src0, ...) \
_nir_build_load_output(build, num_components, bit_size, src0, (struct _nir_load_output_indices){0, __VA_ARGS__})
#endif
#define nir_load_output nir_build_load_output
#define nir_build_load_packed_passthrough_primitive_amd _nir_build_load_packed_passthrough_primitive_amd
#define nir_load_packed_passthrough_primitive_amd nir_build_load_packed_passthrough_primitive_amd
#ifdef __cplusplus
#define nir_build_load_param(build, num_components, bit_size, ...) \
_nir_build_load_param(build, num_components, bit_size, _nir_load_param_indices{0, __VA_ARGS__})
#else
#define nir_build_load_param(build, num_components, bit_size, ...) \
_nir_build_load_param(build, num_components, bit_size, (struct _nir_load_param_indices){0, __VA_ARGS__})
#endif
#define nir_load_param nir_build_load_param
#define nir_build_load_patch_vertices_in _nir_build_load_patch_vertices_in
#define nir_load_patch_vertices_in nir_build_load_patch_vertices_in
#ifdef __cplusplus
#define nir_build_load_per_primitive_output(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_per_primitive_output(build, num_components, bit_size, src0, src1, _nir_load_per_primitive_output_indices{0, __VA_ARGS__})
#else
#define nir_build_load_per_primitive_output(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_per_primitive_output(build, num_components, bit_size, src0, src1, (struct _nir_load_per_primitive_output_indices){0, __VA_ARGS__})
#endif
#define nir_load_per_primitive_output nir_build_load_per_primitive_output
#ifdef __cplusplus
#define nir_build_load_per_vertex_input(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_per_vertex_input(build, num_components, bit_size, src0, src1, _nir_load_per_vertex_input_indices{0, __VA_ARGS__})
#else
#define nir_build_load_per_vertex_input(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_per_vertex_input(build, num_components, bit_size, src0, src1, (struct _nir_load_per_vertex_input_indices){0, __VA_ARGS__})
#endif
#define nir_load_per_vertex_input nir_build_load_per_vertex_input
#ifdef __cplusplus
#define nir_build_load_per_vertex_output(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_per_vertex_output(build, num_components, bit_size, src0, src1, _nir_load_per_vertex_output_indices{0, __VA_ARGS__})
#else
#define nir_build_load_per_vertex_output(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_per_vertex_output(build, num_components, bit_size, src0, src1, (struct _nir_load_per_vertex_output_indices){0, __VA_ARGS__})
#endif
#define nir_load_per_vertex_output nir_build_load_per_vertex_output
#define nir_build_load_persp_center_rhw_ir3 _nir_build_load_persp_center_rhw_ir3
#define nir_load_persp_center_rhw_ir3 nir_build_load_persp_center_rhw_ir3
#define nir_build_load_pipeline_stat_query_enabled_amd _nir_build_load_pipeline_stat_query_enabled_amd
#define nir_load_pipeline_stat_query_enabled_amd nir_build_load_pipeline_stat_query_enabled_amd
#define nir_build_load_point_coord _nir_build_load_point_coord
#define nir_load_point_coord nir_build_load_point_coord
#define nir_build_load_point_coord_maybe_flipped _nir_build_load_point_coord_maybe_flipped
#define nir_load_point_coord_maybe_flipped nir_build_load_point_coord_maybe_flipped
#ifdef __cplusplus
#define nir_build_load_preamble(build, num_components, bit_size, ...) \
_nir_build_load_preamble(build, num_components, bit_size, _nir_load_preamble_indices{0, __VA_ARGS__})
#else
#define nir_build_load_preamble(build, num_components, bit_size, ...) \
_nir_build_load_preamble(build, num_components, bit_size, (struct _nir_load_preamble_indices){0, __VA_ARGS__})
#endif
#define nir_load_preamble nir_build_load_preamble
#define nir_build_load_prim_gen_query_enabled_amd _nir_build_load_prim_gen_query_enabled_amd
#define nir_load_prim_gen_query_enabled_amd nir_build_load_prim_gen_query_enabled_amd
#define nir_build_load_prim_xfb_query_enabled_amd _nir_build_load_prim_xfb_query_enabled_amd
#define nir_load_prim_xfb_query_enabled_amd nir_build_load_prim_xfb_query_enabled_amd
#define nir_build_load_primitive_id _nir_build_load_primitive_id
#define nir_load_primitive_id nir_build_load_primitive_id
#ifdef __cplusplus
#define nir_build_load_primitive_location_ir3(build, ...) \
_nir_build_load_primitive_location_ir3(build, _nir_load_primitive_location_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_load_primitive_location_ir3(build, ...) \
_nir_build_load_primitive_location_ir3(build, (struct _nir_load_primitive_location_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_load_primitive_location_ir3 nir_build_load_primitive_location_ir3
#define nir_build_load_printf_buffer_address _nir_build_load_printf_buffer_address
#define nir_load_printf_buffer_address nir_build_load_printf_buffer_address
#define nir_build_load_provoking_vtx_in_prim_amd _nir_build_load_provoking_vtx_in_prim_amd
#define nir_load_provoking_vtx_in_prim_amd nir_build_load_provoking_vtx_in_prim_amd
#define nir_build_load_ptr_dxil _nir_build_load_ptr_dxil
#define nir_load_ptr_dxil nir_build_load_ptr_dxil
#ifdef __cplusplus
#define nir_build_load_push_constant(build, num_components, bit_size, src0, ...) \
_nir_build_load_push_constant(build, num_components, bit_size, src0, _nir_load_push_constant_indices{0, __VA_ARGS__})
#else
#define nir_build_load_push_constant(build, num_components, bit_size, src0, ...) \
_nir_build_load_push_constant(build, num_components, bit_size, src0, (struct _nir_load_push_constant_indices){0, __VA_ARGS__})
#endif
#define nir_load_push_constant nir_build_load_push_constant
#define nir_build_load_rasterization_samples_amd _nir_build_load_rasterization_samples_amd
#define nir_load_rasterization_samples_amd nir_build_load_rasterization_samples_amd
#ifdef __cplusplus
#define nir_build_load_raw_output_pan(build, num_components, bit_size, src0, ...) \
_nir_build_load_raw_output_pan(build, num_components, bit_size, src0, _nir_load_raw_output_pan_indices{0, __VA_ARGS__})
#else
#define nir_build_load_raw_output_pan(build, num_components, bit_size, src0, ...) \
_nir_build_load_raw_output_pan(build, num_components, bit_size, src0, (struct _nir_load_raw_output_pan_indices){0, __VA_ARGS__})
#endif
#define nir_load_raw_output_pan nir_build_load_raw_output_pan
#define nir_build_load_ray_base_mem_addr_intel _nir_build_load_ray_base_mem_addr_intel
#define nir_load_ray_base_mem_addr_intel nir_build_load_ray_base_mem_addr_intel
#define nir_build_load_ray_flags _nir_build_load_ray_flags
#define nir_load_ray_flags nir_build_load_ray_flags
#define nir_build_load_ray_geometry_index _nir_build_load_ray_geometry_index
#define nir_load_ray_geometry_index nir_build_load_ray_geometry_index
#define nir_build_load_ray_hit_kind _nir_build_load_ray_hit_kind
#define nir_load_ray_hit_kind nir_build_load_ray_hit_kind
#define nir_build_load_ray_hit_sbt_addr_intel _nir_build_load_ray_hit_sbt_addr_intel
#define nir_load_ray_hit_sbt_addr_intel nir_build_load_ray_hit_sbt_addr_intel
#define nir_build_load_ray_hit_sbt_stride_intel _nir_build_load_ray_hit_sbt_stride_intel
#define nir_load_ray_hit_sbt_stride_intel nir_build_load_ray_hit_sbt_stride_intel
#define nir_build_load_ray_hw_stack_size_intel _nir_build_load_ray_hw_stack_size_intel
#define nir_load_ray_hw_stack_size_intel nir_build_load_ray_hw_stack_size_intel
#define nir_build_load_ray_instance_custom_index _nir_build_load_ray_instance_custom_index
#define nir_load_ray_instance_custom_index nir_build_load_ray_instance_custom_index
#define nir_build_load_ray_launch_id _nir_build_load_ray_launch_id
#define nir_load_ray_launch_id nir_build_load_ray_launch_id
#define nir_build_load_ray_launch_size _nir_build_load_ray_launch_size
#define nir_load_ray_launch_size nir_build_load_ray_launch_size
#define nir_build_load_ray_launch_size_addr_amd _nir_build_load_ray_launch_size_addr_amd
#define nir_load_ray_launch_size_addr_amd nir_build_load_ray_launch_size_addr_amd
#define nir_build_load_ray_miss_sbt_addr_intel _nir_build_load_ray_miss_sbt_addr_intel
#define nir_load_ray_miss_sbt_addr_intel nir_build_load_ray_miss_sbt_addr_intel
#define nir_build_load_ray_miss_sbt_stride_intel _nir_build_load_ray_miss_sbt_stride_intel
#define nir_load_ray_miss_sbt_stride_intel nir_build_load_ray_miss_sbt_stride_intel
#define nir_build_load_ray_num_dss_rt_stacks_intel _nir_build_load_ray_num_dss_rt_stacks_intel
#define nir_load_ray_num_dss_rt_stacks_intel nir_build_load_ray_num_dss_rt_stacks_intel
#define nir_build_load_ray_object_direction _nir_build_load_ray_object_direction
#define nir_load_ray_object_direction nir_build_load_ray_object_direction
#define nir_build_load_ray_object_origin _nir_build_load_ray_object_origin
#define nir_load_ray_object_origin nir_build_load_ray_object_origin
#ifdef __cplusplus
#define nir_build_load_ray_object_to_world(build, ...) \
_nir_build_load_ray_object_to_world(build, _nir_load_ray_object_to_world_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ray_object_to_world(build, ...) \
_nir_build_load_ray_object_to_world(build, (struct _nir_load_ray_object_to_world_indices){0, __VA_ARGS__})
#endif
#define nir_load_ray_object_to_world nir_build_load_ray_object_to_world
#define nir_build_load_ray_query_global_intel _nir_build_load_ray_query_global_intel
#define nir_load_ray_query_global_intel nir_build_load_ray_query_global_intel
#define nir_build_load_ray_sw_stack_size_intel _nir_build_load_ray_sw_stack_size_intel
#define nir_load_ray_sw_stack_size_intel nir_build_load_ray_sw_stack_size_intel
#define nir_build_load_ray_t_max _nir_build_load_ray_t_max
#define nir_load_ray_t_max nir_build_load_ray_t_max
#define nir_build_load_ray_t_min _nir_build_load_ray_t_min
#define nir_load_ray_t_min nir_build_load_ray_t_min
#define nir_build_load_ray_world_direction _nir_build_load_ray_world_direction
#define nir_load_ray_world_direction nir_build_load_ray_world_direction
#define nir_build_load_ray_world_origin _nir_build_load_ray_world_origin
#define nir_load_ray_world_origin nir_build_load_ray_world_origin
#ifdef __cplusplus
#define nir_build_load_ray_world_to_object(build, ...) \
_nir_build_load_ray_world_to_object(build, _nir_load_ray_world_to_object_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ray_world_to_object(build, ...) \
_nir_build_load_ray_world_to_object(build, (struct _nir_load_ray_world_to_object_indices){0, __VA_ARGS__})
#endif
#define nir_load_ray_world_to_object nir_build_load_ray_world_to_object
#define nir_build_load_rel_patch_id_ir3 _nir_build_load_rel_patch_id_ir3
#define nir_load_rel_patch_id_ir3 nir_build_load_rel_patch_id_ir3
#ifdef __cplusplus
#define nir_build_load_reloc_const_intel(build, ...) \
_nir_build_load_reloc_const_intel(build, _nir_load_reloc_const_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_reloc_const_intel(build, ...) \
_nir_build_load_reloc_const_intel(build, (struct _nir_load_reloc_const_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_reloc_const_intel nir_build_load_reloc_const_intel
#define nir_build_load_ring_attr_amd _nir_build_load_ring_attr_amd
#define nir_load_ring_attr_amd nir_build_load_ring_attr_amd
#define nir_build_load_ring_attr_offset_amd _nir_build_load_ring_attr_offset_amd
#define nir_load_ring_attr_offset_amd nir_build_load_ring_attr_offset_amd
#define nir_build_load_ring_es2gs_offset_amd _nir_build_load_ring_es2gs_offset_amd
#define nir_load_ring_es2gs_offset_amd nir_build_load_ring_es2gs_offset_amd
#define nir_build_load_ring_esgs_amd _nir_build_load_ring_esgs_amd
#define nir_load_ring_esgs_amd nir_build_load_ring_esgs_amd
#define nir_build_load_ring_gs2vs_offset_amd _nir_build_load_ring_gs2vs_offset_amd
#define nir_load_ring_gs2vs_offset_amd nir_build_load_ring_gs2vs_offset_amd
#ifdef __cplusplus
#define nir_build_load_ring_gsvs_amd(build, ...) \
_nir_build_load_ring_gsvs_amd(build, _nir_load_ring_gsvs_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ring_gsvs_amd(build, ...) \
_nir_build_load_ring_gsvs_amd(build, (struct _nir_load_ring_gsvs_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_ring_gsvs_amd nir_build_load_ring_gsvs_amd
#define nir_build_load_ring_mesh_scratch_amd _nir_build_load_ring_mesh_scratch_amd
#define nir_load_ring_mesh_scratch_amd nir_build_load_ring_mesh_scratch_amd
#define nir_build_load_ring_mesh_scratch_offset_amd _nir_build_load_ring_mesh_scratch_offset_amd
#define nir_load_ring_mesh_scratch_offset_amd nir_build_load_ring_mesh_scratch_offset_amd
#define nir_build_load_ring_task_draw_amd _nir_build_load_ring_task_draw_amd
#define nir_load_ring_task_draw_amd nir_build_load_ring_task_draw_amd
#define nir_build_load_ring_task_payload_amd _nir_build_load_ring_task_payload_amd
#define nir_load_ring_task_payload_amd nir_build_load_ring_task_payload_amd
#define nir_build_load_ring_tess_factors_amd _nir_build_load_ring_tess_factors_amd
#define nir_load_ring_tess_factors_amd nir_build_load_ring_tess_factors_amd
#define nir_build_load_ring_tess_factors_offset_amd _nir_build_load_ring_tess_factors_offset_amd
#define nir_load_ring_tess_factors_offset_amd nir_build_load_ring_tess_factors_offset_amd
#define nir_build_load_ring_tess_offchip_amd _nir_build_load_ring_tess_offchip_amd
#define nir_load_ring_tess_offchip_amd nir_build_load_ring_tess_offchip_amd
#define nir_build_load_ring_tess_offchip_offset_amd _nir_build_load_ring_tess_offchip_offset_amd
#define nir_load_ring_tess_offchip_offset_amd nir_build_load_ring_tess_offchip_offset_amd
#define nir_build_load_rt_arg_scratch_offset_amd _nir_build_load_rt_arg_scratch_offset_amd
#define nir_load_rt_arg_scratch_offset_amd nir_build_load_rt_arg_scratch_offset_amd
#ifdef __cplusplus
#define nir_build_load_rt_conversion_pan(build, ...) \
_nir_build_load_rt_conversion_pan(build, _nir_load_rt_conversion_pan_indices{0, __VA_ARGS__})
#else
#define nir_build_load_rt_conversion_pan(build, ...) \
_nir_build_load_rt_conversion_pan(build, (struct _nir_load_rt_conversion_pan_indices){0, __VA_ARGS__})
#endif
#define nir_load_rt_conversion_pan nir_build_load_rt_conversion_pan
#define nir_build_load_rt_dynamic_callable_stack_base_amd _nir_build_load_rt_dynamic_callable_stack_base_amd
#define nir_load_rt_dynamic_callable_stack_base_amd nir_build_load_rt_dynamic_callable_stack_base_amd
#define nir_build_load_sample_id _nir_build_load_sample_id
#define nir_load_sample_id nir_build_load_sample_id
#define nir_build_load_sample_id_no_per_sample _nir_build_load_sample_id_no_per_sample
#define nir_load_sample_id_no_per_sample nir_build_load_sample_id_no_per_sample
#define nir_build_load_sample_mask_in _nir_build_load_sample_mask_in
#define nir_load_sample_mask_in nir_build_load_sample_mask_in
#define nir_build_load_sample_pos _nir_build_load_sample_pos
#define nir_load_sample_pos nir_build_load_sample_pos
#define nir_build_load_sample_pos_from_id _nir_build_load_sample_pos_from_id
#define nir_load_sample_pos_from_id nir_build_load_sample_pos_from_id
#define nir_build_load_sample_pos_or_center _nir_build_load_sample_pos_or_center
#define nir_load_sample_pos_or_center nir_build_load_sample_pos_or_center
#define nir_build_load_sample_positions_amd _nir_build_load_sample_positions_amd
#define nir_load_sample_positions_amd nir_build_load_sample_positions_amd
#define nir_build_load_sample_positions_pan _nir_build_load_sample_positions_pan
#define nir_load_sample_positions_pan nir_build_load_sample_positions_pan
#define nir_build_load_sampler_lod_parameters_pan _nir_build_load_sampler_lod_parameters_pan
#define nir_load_sampler_lod_parameters_pan nir_build_load_sampler_lod_parameters_pan
#define nir_build_load_sbt_base_amd _nir_build_load_sbt_base_amd
#define nir_load_sbt_base_amd nir_build_load_sbt_base_amd
#define nir_build_load_sbt_offset_amd _nir_build_load_sbt_offset_amd
#define nir_load_sbt_offset_amd nir_build_load_sbt_offset_amd
#define nir_build_load_sbt_stride_amd _nir_build_load_sbt_stride_amd
#define nir_load_sbt_stride_amd nir_build_load_sbt_stride_amd
#ifdef __cplusplus
#define nir_build_load_scalar_arg_amd(build, num_components, ...) \
_nir_build_load_scalar_arg_amd(build, num_components, _nir_load_scalar_arg_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_scalar_arg_amd(build, num_components, ...) \
_nir_build_load_scalar_arg_amd(build, num_components, (struct _nir_load_scalar_arg_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_scalar_arg_amd nir_build_load_scalar_arg_amd
#ifdef __cplusplus
#define nir_build_load_scratch(build, num_components, bit_size, src0, ...) \
_nir_build_load_scratch(build, num_components, bit_size, src0, _nir_load_scratch_indices{0, __VA_ARGS__})
#else
#define nir_build_load_scratch(build, num_components, bit_size, src0, ...) \
_nir_build_load_scratch(build, num_components, bit_size, src0, (struct _nir_load_scratch_indices){0, __VA_ARGS__})
#endif
#define nir_load_scratch nir_build_load_scratch
#ifdef __cplusplus
#define nir_build_load_scratch_base_ptr(build, num_components, bit_size, ...) \
_nir_build_load_scratch_base_ptr(build, num_components, bit_size, _nir_load_scratch_base_ptr_indices{0, __VA_ARGS__})
#else
#define nir_build_load_scratch_base_ptr(build, num_components, bit_size, ...) \
_nir_build_load_scratch_base_ptr(build, num_components, bit_size, (struct _nir_load_scratch_base_ptr_indices){0, __VA_ARGS__})
#endif
#define nir_load_scratch_base_ptr nir_build_load_scratch_base_ptr
#define nir_build_load_scratch_dxil _nir_build_load_scratch_dxil
#define nir_load_scratch_dxil nir_build_load_scratch_dxil
#define nir_build_load_shader_record_ptr _nir_build_load_shader_record_ptr
#define nir_load_shader_record_ptr nir_build_load_shader_record_ptr
#ifdef __cplusplus
#define nir_build_load_shared(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared(build, num_components, bit_size, src0, _nir_load_shared_indices{0, __VA_ARGS__})
#else
#define nir_build_load_shared(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared(build, num_components, bit_size, src0, (struct _nir_load_shared_indices){0, __VA_ARGS__})
#endif
#define nir_load_shared nir_build_load_shared
#ifdef __cplusplus
#define nir_build_load_shared2_amd(build, bit_size, src0, ...) \
_nir_build_load_shared2_amd(build, bit_size, src0, _nir_load_shared2_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_shared2_amd(build, bit_size, src0, ...) \
_nir_build_load_shared2_amd(build, bit_size, src0, (struct _nir_load_shared2_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_shared2_amd nir_build_load_shared2_amd
#define nir_build_load_shared_base_ptr _nir_build_load_shared_base_ptr
#define nir_load_shared_base_ptr nir_build_load_shared_base_ptr
#ifdef __cplusplus
#define nir_build_load_shared_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared_block_intel(build, num_components, bit_size, src0, _nir_load_shared_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_shared_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared_block_intel(build, num_components, bit_size, src0, (struct _nir_load_shared_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_shared_block_intel nir_build_load_shared_block_intel
#define nir_build_load_shared_dxil _nir_build_load_shared_dxil
#define nir_load_shared_dxil nir_build_load_shared_dxil
#ifdef __cplusplus
#define nir_build_load_shared_ir3(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared_ir3(build, num_components, bit_size, src0, _nir_load_shared_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_load_shared_ir3(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared_ir3(build, num_components, bit_size, src0, (struct _nir_load_shared_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_load_shared_ir3 nir_build_load_shared_ir3
#ifdef __cplusplus
#define nir_build_load_shared_uniform_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared_uniform_block_intel(build, num_components, bit_size, src0, _nir_load_shared_uniform_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_shared_uniform_block_intel(build, num_components, bit_size, src0, ...) \
_nir_build_load_shared_uniform_block_intel(build, num_components, bit_size, src0, (struct _nir_load_shared_uniform_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_shared_uniform_block_intel nir_build_load_shared_uniform_block_intel
#define nir_build_load_simd_width_intel _nir_build_load_simd_width_intel
#define nir_load_simd_width_intel nir_build_load_simd_width_intel
#ifdef __cplusplus
#define nir_build_load_smem_amd(build, num_components, src0, src1, ...) \
_nir_build_load_smem_amd(build, num_components, src0, src1, _nir_load_smem_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_smem_amd(build, num_components, src0, src1, ...) \
_nir_build_load_smem_amd(build, num_components, src0, src1, (struct _nir_load_smem_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_smem_amd nir_build_load_smem_amd
#ifdef __cplusplus
#define nir_build_load_smem_buffer_amd(build, num_components, src0, src1, ...) \
_nir_build_load_smem_buffer_amd(build, num_components, src0, src1, _nir_load_smem_buffer_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_smem_buffer_amd(build, num_components, src0, src1, ...) \
_nir_build_load_smem_buffer_amd(build, num_components, src0, src1, (struct _nir_load_smem_buffer_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_smem_buffer_amd nir_build_load_smem_buffer_amd
#ifdef __cplusplus
#define nir_build_load_ssbo(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ssbo(build, num_components, bit_size, src0, src1, _nir_load_ssbo_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ssbo(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ssbo(build, num_components, bit_size, src0, src1, (struct _nir_load_ssbo_indices){0, __VA_ARGS__})
#endif
#define nir_load_ssbo nir_build_load_ssbo
#define nir_build_load_ssbo_address _nir_build_load_ssbo_address
#define nir_load_ssbo_address nir_build_load_ssbo_address
#ifdef __cplusplus
#define nir_build_load_ssbo_block_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ssbo_block_intel(build, num_components, bit_size, src0, src1, _nir_load_ssbo_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ssbo_block_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ssbo_block_intel(build, num_components, bit_size, src0, src1, (struct _nir_load_ssbo_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_ssbo_block_intel nir_build_load_ssbo_block_intel
#ifdef __cplusplus
#define nir_build_load_ssbo_ir3(build, num_components, bit_size, src0, src1, src2, ...) \
_nir_build_load_ssbo_ir3(build, num_components, bit_size, src0, src1, src2, _nir_load_ssbo_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ssbo_ir3(build, num_components, bit_size, src0, src1, src2, ...) \
_nir_build_load_ssbo_ir3(build, num_components, bit_size, src0, src1, src2, (struct _nir_load_ssbo_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_load_ssbo_ir3 nir_build_load_ssbo_ir3
#ifdef __cplusplus
#define nir_build_load_ssbo_uniform_block_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ssbo_uniform_block_intel(build, num_components, bit_size, src0, src1, _nir_load_ssbo_uniform_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ssbo_uniform_block_intel(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ssbo_uniform_block_intel(build, num_components, bit_size, src0, src1, (struct _nir_load_ssbo_uniform_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_ssbo_uniform_block_intel nir_build_load_ssbo_uniform_block_intel
#ifdef __cplusplus
#define nir_build_load_stack(build, num_components, bit_size, ...) \
_nir_build_load_stack(build, num_components, bit_size, _nir_load_stack_indices{0, __VA_ARGS__})
#else
#define nir_build_load_stack(build, num_components, bit_size, ...) \
_nir_build_load_stack(build, num_components, bit_size, (struct _nir_load_stack_indices){0, __VA_ARGS__})
#endif
#define nir_load_stack nir_build_load_stack
#ifdef __cplusplus
#define nir_build_load_streamout_buffer_amd(build, ...) \
_nir_build_load_streamout_buffer_amd(build, _nir_load_streamout_buffer_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_streamout_buffer_amd(build, ...) \
_nir_build_load_streamout_buffer_amd(build, (struct _nir_load_streamout_buffer_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_streamout_buffer_amd nir_build_load_streamout_buffer_amd
#define nir_build_load_streamout_config_amd _nir_build_load_streamout_config_amd
#define nir_load_streamout_config_amd nir_build_load_streamout_config_amd
#ifdef __cplusplus
#define nir_build_load_streamout_offset_amd(build, ...) \
_nir_build_load_streamout_offset_amd(build, _nir_load_streamout_offset_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_streamout_offset_amd(build, ...) \
_nir_build_load_streamout_offset_amd(build, (struct _nir_load_streamout_offset_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_streamout_offset_amd nir_build_load_streamout_offset_amd
#define nir_build_load_streamout_write_index_amd _nir_build_load_streamout_write_index_amd
#define nir_load_streamout_write_index_amd nir_build_load_streamout_write_index_amd
#define nir_build_load_subgroup_eq_mask _nir_build_load_subgroup_eq_mask
#define nir_load_subgroup_eq_mask nir_build_load_subgroup_eq_mask
#define nir_build_load_subgroup_ge_mask _nir_build_load_subgroup_ge_mask
#define nir_load_subgroup_ge_mask nir_build_load_subgroup_ge_mask
#define nir_build_load_subgroup_gt_mask _nir_build_load_subgroup_gt_mask
#define nir_load_subgroup_gt_mask nir_build_load_subgroup_gt_mask
#define nir_build_load_subgroup_id _nir_build_load_subgroup_id
#define nir_load_subgroup_id nir_build_load_subgroup_id
#define nir_build_load_subgroup_id_shift_ir3 _nir_build_load_subgroup_id_shift_ir3
#define nir_load_subgroup_id_shift_ir3 nir_build_load_subgroup_id_shift_ir3
#define nir_build_load_subgroup_invocation _nir_build_load_subgroup_invocation
#define nir_load_subgroup_invocation nir_build_load_subgroup_invocation
#define nir_build_load_subgroup_le_mask _nir_build_load_subgroup_le_mask
#define nir_load_subgroup_le_mask nir_build_load_subgroup_le_mask
#define nir_build_load_subgroup_lt_mask _nir_build_load_subgroup_lt_mask
#define nir_load_subgroup_lt_mask nir_build_load_subgroup_lt_mask
#define nir_build_load_subgroup_size _nir_build_load_subgroup_size
#define nir_load_subgroup_size nir_build_load_subgroup_size
#ifdef __cplusplus
#define nir_build_load_task_payload(build, num_components, bit_size, src0, ...) \
_nir_build_load_task_payload(build, num_components, bit_size, src0, _nir_load_task_payload_indices{0, __VA_ARGS__})
#else
#define nir_build_load_task_payload(build, num_components, bit_size, src0, ...) \
_nir_build_load_task_payload(build, num_components, bit_size, src0, (struct _nir_load_task_payload_indices){0, __VA_ARGS__})
#endif
#define nir_load_task_payload nir_build_load_task_payload
#define nir_build_load_task_ring_entry_amd _nir_build_load_task_ring_entry_amd
#define nir_load_task_ring_entry_amd nir_build_load_task_ring_entry_amd
#define nir_build_load_tcs_header_ir3 _nir_build_load_tcs_header_ir3
#define nir_load_tcs_header_ir3 nir_build_load_tcs_header_ir3
#define nir_build_load_tcs_in_param_base_r600 _nir_build_load_tcs_in_param_base_r600
#define nir_load_tcs_in_param_base_r600 nir_build_load_tcs_in_param_base_r600
#define nir_build_load_tcs_num_patches_amd _nir_build_load_tcs_num_patches_amd
#define nir_load_tcs_num_patches_amd nir_build_load_tcs_num_patches_amd
#define nir_build_load_tcs_out_param_base_r600 _nir_build_load_tcs_out_param_base_r600
#define nir_load_tcs_out_param_base_r600 nir_build_load_tcs_out_param_base_r600
#define nir_build_load_tcs_rel_patch_id_r600 _nir_build_load_tcs_rel_patch_id_r600
#define nir_load_tcs_rel_patch_id_r600 nir_build_load_tcs_rel_patch_id_r600
#define nir_build_load_tcs_tess_factor_base_r600 _nir_build_load_tcs_tess_factor_base_r600
#define nir_load_tcs_tess_factor_base_r600 nir_build_load_tcs_tess_factor_base_r600
#define nir_build_load_tess_coord _nir_build_load_tess_coord
#define nir_load_tess_coord nir_build_load_tess_coord
#define nir_build_load_tess_coord_r600 _nir_build_load_tess_coord_r600
#define nir_load_tess_coord_r600 nir_build_load_tess_coord_r600
#define nir_build_load_tess_factor_base_ir3 _nir_build_load_tess_factor_base_ir3
#define nir_load_tess_factor_base_ir3 nir_build_load_tess_factor_base_ir3
#define nir_build_load_tess_level_inner _nir_build_load_tess_level_inner
#define nir_load_tess_level_inner nir_build_load_tess_level_inner
#define nir_build_load_tess_level_inner_default _nir_build_load_tess_level_inner_default
#define nir_load_tess_level_inner_default nir_build_load_tess_level_inner_default
#define nir_build_load_tess_level_outer _nir_build_load_tess_level_outer
#define nir_load_tess_level_outer nir_build_load_tess_level_outer
#define nir_build_load_tess_level_outer_default _nir_build_load_tess_level_outer_default
#define nir_load_tess_level_outer_default nir_build_load_tess_level_outer_default
#define nir_build_load_tess_param_base_ir3 _nir_build_load_tess_param_base_ir3
#define nir_load_tess_param_base_ir3 nir_build_load_tess_param_base_ir3
#define nir_build_load_tess_rel_patch_id_amd _nir_build_load_tess_rel_patch_id_amd
#define nir_load_tess_rel_patch_id_amd nir_build_load_tess_rel_patch_id_amd
#define nir_build_load_texture_base_agx _nir_build_load_texture_base_agx
#define nir_load_texture_base_agx nir_build_load_texture_base_agx
#define nir_build_load_texture_rect_scaling _nir_build_load_texture_rect_scaling
#define nir_load_texture_rect_scaling nir_build_load_texture_rect_scaling
#ifdef __cplusplus
#define nir_build_load_tlb_color_v3d(build, num_components, bit_size, src0, ...) \
_nir_build_load_tlb_color_v3d(build, num_components, bit_size, src0, _nir_load_tlb_color_v3d_indices{0, __VA_ARGS__})
#else
#define nir_build_load_tlb_color_v3d(build, num_components, bit_size, src0, ...) \
_nir_build_load_tlb_color_v3d(build, num_components, bit_size, src0, (struct _nir_load_tlb_color_v3d_indices){0, __VA_ARGS__})
#endif
#define nir_load_tlb_color_v3d nir_build_load_tlb_color_v3d
#ifdef __cplusplus
#define nir_build_load_topology_id_intel(build, ...) \
_nir_build_load_topology_id_intel(build, _nir_load_topology_id_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_load_topology_id_intel(build, ...) \
_nir_build_load_topology_id_intel(build, (struct _nir_load_topology_id_intel_indices){0, __VA_ARGS__})
#endif
#define nir_load_topology_id_intel nir_build_load_topology_id_intel
#ifdef __cplusplus
#define nir_build_load_typed_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_load_typed_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, _nir_load_typed_buffer_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_typed_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, ...) \
_nir_build_load_typed_buffer_amd(build, num_components, bit_size, src0, src1, src2, src3, (struct _nir_load_typed_buffer_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_typed_buffer_amd nir_build_load_typed_buffer_amd
#ifdef __cplusplus
#define nir_build_load_ubo(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ubo(build, num_components, bit_size, src0, src1, _nir_load_ubo_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ubo(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ubo(build, num_components, bit_size, src0, src1, (struct _nir_load_ubo_indices){0, __VA_ARGS__})
#endif
#define nir_load_ubo nir_build_load_ubo
#define nir_build_load_ubo_base_agx _nir_build_load_ubo_base_agx
#define nir_load_ubo_base_agx nir_build_load_ubo_base_agx
#define nir_build_load_ubo_dxil _nir_build_load_ubo_dxil
#define nir_load_ubo_dxil nir_build_load_ubo_dxil
#ifdef __cplusplus
#define nir_build_load_ubo_vec4(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ubo_vec4(build, num_components, bit_size, src0, src1, _nir_load_ubo_vec4_indices{0, __VA_ARGS__})
#else
#define nir_build_load_ubo_vec4(build, num_components, bit_size, src0, src1, ...) \
_nir_build_load_ubo_vec4(build, num_components, bit_size, src0, src1, (struct _nir_load_ubo_vec4_indices){0, __VA_ARGS__})
#endif
#define nir_load_ubo_vec4 nir_build_load_ubo_vec4
#ifdef __cplusplus
#define nir_build_load_uniform(build, num_components, bit_size, src0, ...) \
_nir_build_load_uniform(build, num_components, bit_size, src0, _nir_load_uniform_indices{0, __VA_ARGS__})
#else
#define nir_build_load_uniform(build, num_components, bit_size, src0, ...) \
_nir_build_load_uniform(build, num_components, bit_size, src0, (struct _nir_load_uniform_indices){0, __VA_ARGS__})
#endif
#define nir_load_uniform nir_build_load_uniform
#ifdef __cplusplus
#define nir_build_load_user_clip_plane(build, ...) \
_nir_build_load_user_clip_plane(build, _nir_load_user_clip_plane_indices{0, __VA_ARGS__})
#else
#define nir_build_load_user_clip_plane(build, ...) \
_nir_build_load_user_clip_plane(build, (struct _nir_load_user_clip_plane_indices){0, __VA_ARGS__})
#endif
#define nir_load_user_clip_plane nir_build_load_user_clip_plane
#define nir_build_load_user_data_amd _nir_build_load_user_data_amd
#define nir_load_user_data_amd nir_build_load_user_data_amd
#define nir_build_load_vbo_base_agx _nir_build_load_vbo_base_agx
#define nir_load_vbo_base_agx nir_build_load_vbo_base_agx
#ifdef __cplusplus
#define nir_build_load_vector_arg_amd(build, num_components, ...) \
_nir_build_load_vector_arg_amd(build, num_components, _nir_load_vector_arg_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_load_vector_arg_amd(build, num_components, ...) \
_nir_build_load_vector_arg_amd(build, num_components, (struct _nir_load_vector_arg_amd_indices){0, __VA_ARGS__})
#endif
#define nir_load_vector_arg_amd nir_build_load_vector_arg_amd
#define nir_build_load_vertex_id _nir_build_load_vertex_id
#define nir_load_vertex_id nir_build_load_vertex_id
#define nir_build_load_vertex_id_zero_base _nir_build_load_vertex_id_zero_base
#define nir_load_vertex_id_zero_base nir_build_load_vertex_id_zero_base
#define nir_build_load_view_index _nir_build_load_view_index
#define nir_load_view_index nir_build_load_view_index
#define nir_build_load_viewport_offset _nir_build_load_viewport_offset
#define nir_load_viewport_offset nir_build_load_viewport_offset
#define nir_build_load_viewport_scale _nir_build_load_viewport_scale
#define nir_load_viewport_scale nir_build_load_viewport_scale
#define nir_build_load_viewport_x_offset _nir_build_load_viewport_x_offset
#define nir_load_viewport_x_offset nir_build_load_viewport_x_offset
#define nir_build_load_viewport_x_scale _nir_build_load_viewport_x_scale
#define nir_load_viewport_x_scale nir_build_load_viewport_x_scale
#define nir_build_load_viewport_xy_scale_and_offset _nir_build_load_viewport_xy_scale_and_offset
#define nir_load_viewport_xy_scale_and_offset nir_build_load_viewport_xy_scale_and_offset
#define nir_build_load_viewport_y_offset _nir_build_load_viewport_y_offset
#define nir_load_viewport_y_offset nir_build_load_viewport_y_offset
#define nir_build_load_viewport_y_scale _nir_build_load_viewport_y_scale
#define nir_load_viewport_y_scale nir_build_load_viewport_y_scale
#define nir_build_load_viewport_z_offset _nir_build_load_viewport_z_offset
#define nir_load_viewport_z_offset nir_build_load_viewport_z_offset
#define nir_build_load_viewport_z_scale _nir_build_load_viewport_z_scale
#define nir_load_viewport_z_scale nir_build_load_viewport_z_scale
#define nir_build_load_vs_primitive_stride_ir3 _nir_build_load_vs_primitive_stride_ir3
#define nir_load_vs_primitive_stride_ir3 nir_build_load_vs_primitive_stride_ir3
#define nir_build_load_vs_vertex_stride_ir3 _nir_build_load_vs_vertex_stride_ir3
#define nir_load_vs_vertex_stride_ir3 nir_build_load_vs_vertex_stride_ir3
#ifdef __cplusplus
#define nir_build_load_vulkan_descriptor(build, num_components, bit_size, src0, ...) \
_nir_build_load_vulkan_descriptor(build, num_components, bit_size, src0, _nir_load_vulkan_descriptor_indices{0, __VA_ARGS__})
#else
#define nir_build_load_vulkan_descriptor(build, num_components, bit_size, src0, ...) \
_nir_build_load_vulkan_descriptor(build, num_components, bit_size, src0, (struct _nir_load_vulkan_descriptor_indices){0, __VA_ARGS__})
#endif
#define nir_load_vulkan_descriptor nir_build_load_vulkan_descriptor
#define nir_build_load_work_dim _nir_build_load_work_dim
#define nir_load_work_dim nir_build_load_work_dim
#define nir_build_load_workgroup_id _nir_build_load_workgroup_id
#define nir_load_workgroup_id nir_build_load_workgroup_id
#define nir_build_load_workgroup_id_zero_base _nir_build_load_workgroup_id_zero_base
#define nir_load_workgroup_id_zero_base nir_build_load_workgroup_id_zero_base
#define nir_build_load_workgroup_index _nir_build_load_workgroup_index
#define nir_load_workgroup_index nir_build_load_workgroup_index
#define nir_build_load_workgroup_num_input_primitives_amd _nir_build_load_workgroup_num_input_primitives_amd
#define nir_load_workgroup_num_input_primitives_amd nir_build_load_workgroup_num_input_primitives_amd
#define nir_build_load_workgroup_num_input_vertices_amd _nir_build_load_workgroup_num_input_vertices_amd
#define nir_load_workgroup_num_input_vertices_amd nir_build_load_workgroup_num_input_vertices_amd
#define nir_build_load_workgroup_size _nir_build_load_workgroup_size
#define nir_load_workgroup_size nir_build_load_workgroup_size
#ifdef __cplusplus
#define nir_build_load_xfb_address(build, bit_size, ...) \
_nir_build_load_xfb_address(build, bit_size, _nir_load_xfb_address_indices{0, __VA_ARGS__})
#else
#define nir_build_load_xfb_address(build, bit_size, ...) \
_nir_build_load_xfb_address(build, bit_size, (struct _nir_load_xfb_address_indices){0, __VA_ARGS__})
#endif
#define nir_load_xfb_address nir_build_load_xfb_address
#ifdef __cplusplus
#define nir_build_masked_swizzle_amd(build, src0, ...) \
_nir_build_masked_swizzle_amd(build, src0, _nir_masked_swizzle_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_masked_swizzle_amd(build, src0, ...) \
_nir_build_masked_swizzle_amd(build, src0, (struct _nir_masked_swizzle_amd_indices){0, __VA_ARGS__})
#endif
#define nir_masked_swizzle_amd nir_build_masked_swizzle_amd
#define nir_build_mbcnt_amd _nir_build_mbcnt_amd
#define nir_mbcnt_amd nir_build_mbcnt_amd
#ifdef __cplusplus
#define nir_build_memcpy_deref(build, src0, src1, src2, ...) \
_nir_build_memcpy_deref(build, src0, src1, src2, _nir_memcpy_deref_indices{0, __VA_ARGS__})
#else
#define nir_build_memcpy_deref(build, src0, src1, src2, ...) \
_nir_build_memcpy_deref(build, src0, src1, src2, (struct _nir_memcpy_deref_indices){0, __VA_ARGS__})
#endif
#define nir_memcpy_deref nir_build_memcpy_deref
#define nir_build_memory_barrier _nir_build_memory_barrier
#define nir_memory_barrier nir_build_memory_barrier
#define nir_build_memory_barrier_atomic_counter _nir_build_memory_barrier_atomic_counter
#define nir_memory_barrier_atomic_counter nir_build_memory_barrier_atomic_counter
#define nir_build_memory_barrier_buffer _nir_build_memory_barrier_buffer
#define nir_memory_barrier_buffer nir_build_memory_barrier_buffer
#define nir_build_memory_barrier_image _nir_build_memory_barrier_image
#define nir_memory_barrier_image nir_build_memory_barrier_image
#define nir_build_memory_barrier_shared _nir_build_memory_barrier_shared
#define nir_memory_barrier_shared nir_build_memory_barrier_shared
#define nir_build_memory_barrier_tcs_patch _nir_build_memory_barrier_tcs_patch
#define nir_memory_barrier_tcs_patch nir_build_memory_barrier_tcs_patch
#define nir_build_nop _nir_build_nop
#define nir_nop nir_build_nop
#define nir_build_optimization_barrier_vgpr_amd _nir_build_optimization_barrier_vgpr_amd
#define nir_optimization_barrier_vgpr_amd nir_build_optimization_barrier_vgpr_amd
#ifdef __cplusplus
#define nir_build_ordered_xfb_counter_add_amd(build, src0, src1, ...) \
_nir_build_ordered_xfb_counter_add_amd(build, src0, src1, _nir_ordered_xfb_counter_add_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_ordered_xfb_counter_add_amd(build, src0, src1, ...) \
_nir_build_ordered_xfb_counter_add_amd(build, src0, src1, (struct _nir_ordered_xfb_counter_add_amd_indices){0, __VA_ARGS__})
#endif
#define nir_ordered_xfb_counter_add_amd nir_build_ordered_xfb_counter_add_amd
#define nir_build_overwrite_tes_arguments_amd _nir_build_overwrite_tes_arguments_amd
#define nir_overwrite_tes_arguments_amd nir_build_overwrite_tes_arguments_amd
#define nir_build_overwrite_vs_arguments_amd _nir_build_overwrite_vs_arguments_amd
#define nir_overwrite_vs_arguments_amd nir_build_overwrite_vs_arguments_amd
#define nir_build_preamble_end_ir3 _nir_build_preamble_end_ir3
#define nir_preamble_end_ir3 nir_build_preamble_end_ir3
#define nir_build_preamble_start_ir3 _nir_build_preamble_start_ir3
#define nir_preamble_start_ir3 nir_build_preamble_start_ir3
#define nir_build_printf _nir_build_printf
#define nir_printf nir_build_printf
#define nir_build_quad_broadcast _nir_build_quad_broadcast
#define nir_quad_broadcast nir_build_quad_broadcast
#define nir_build_quad_swap_diagonal _nir_build_quad_swap_diagonal
#define nir_quad_swap_diagonal nir_build_quad_swap_diagonal
#define nir_build_quad_swap_horizontal _nir_build_quad_swap_horizontal
#define nir_quad_swap_horizontal nir_build_quad_swap_horizontal
#define nir_build_quad_swap_vertical _nir_build_quad_swap_vertical
#define nir_quad_swap_vertical nir_build_quad_swap_vertical
#ifdef __cplusplus
#define nir_build_quad_swizzle_amd(build, src0, ...) \
_nir_build_quad_swizzle_amd(build, src0, _nir_quad_swizzle_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_quad_swizzle_amd(build, src0, ...) \
_nir_build_quad_swizzle_amd(build, src0, (struct _nir_quad_swizzle_amd_indices){0, __VA_ARGS__})
#endif
#define nir_quad_swizzle_amd nir_build_quad_swizzle_amd
#define nir_build_read_first_invocation _nir_build_read_first_invocation
#define nir_read_first_invocation nir_build_read_first_invocation
#define nir_build_read_invocation _nir_build_read_invocation
#define nir_read_invocation nir_build_read_invocation
#define nir_build_read_invocation_cond_ir3 _nir_build_read_invocation_cond_ir3
#define nir_read_invocation_cond_ir3 nir_build_read_invocation_cond_ir3
#ifdef __cplusplus
#define nir_build_reduce(build, src0, ...) \
_nir_build_reduce(build, src0, _nir_reduce_indices{0, __VA_ARGS__})
#else
#define nir_build_reduce(build, src0, ...) \
_nir_build_reduce(build, src0, (struct _nir_reduce_indices){0, __VA_ARGS__})
#endif
#define nir_reduce nir_build_reduce
#define nir_build_report_ray_intersection _nir_build_report_ray_intersection
#define nir_report_ray_intersection nir_build_report_ray_intersection
#ifdef __cplusplus
#define nir_build_rotate(build, src0, src1, ...) \
_nir_build_rotate(build, src0, src1, _nir_rotate_indices{0, __VA_ARGS__})
#else
#define nir_build_rotate(build, src0, src1, ...) \
_nir_build_rotate(build, src0, src1, (struct _nir_rotate_indices){0, __VA_ARGS__})
#endif
#define nir_rotate nir_build_rotate
#define nir_build_rq_confirm_intersection _nir_build_rq_confirm_intersection
#define nir_rq_confirm_intersection nir_build_rq_confirm_intersection
#define nir_build_rq_generate_intersection _nir_build_rq_generate_intersection
#define nir_rq_generate_intersection nir_build_rq_generate_intersection
#define nir_build_rq_initialize _nir_build_rq_initialize
#define nir_rq_initialize nir_build_rq_initialize
#ifdef __cplusplus
#define nir_build_rq_load(build, num_components, bit_size, src0, src1, ...) \
_nir_build_rq_load(build, num_components, bit_size, src0, src1, _nir_rq_load_indices{0, __VA_ARGS__})
#else
#define nir_build_rq_load(build, num_components, bit_size, src0, src1, ...) \
_nir_build_rq_load(build, num_components, bit_size, src0, src1, (struct _nir_rq_load_indices){0, __VA_ARGS__})
#endif
#define nir_rq_load nir_build_rq_load
#define nir_build_rq_proceed _nir_build_rq_proceed
#define nir_rq_proceed nir_build_rq_proceed
#define nir_build_rq_terminate _nir_build_rq_terminate
#define nir_rq_terminate nir_build_rq_terminate
#ifdef __cplusplus
#define nir_build_rt_execute_callable(build, src0, src1, ...) \
_nir_build_rt_execute_callable(build, src0, src1, _nir_rt_execute_callable_indices{0, __VA_ARGS__})
#else
#define nir_build_rt_execute_callable(build, src0, src1, ...) \
_nir_build_rt_execute_callable(build, src0, src1, (struct _nir_rt_execute_callable_indices){0, __VA_ARGS__})
#endif
#define nir_rt_execute_callable nir_build_rt_execute_callable
#ifdef __cplusplus
#define nir_build_rt_resume(build, ...) \
_nir_build_rt_resume(build, _nir_rt_resume_indices{0, __VA_ARGS__})
#else
#define nir_build_rt_resume(build, ...) \
_nir_build_rt_resume(build, (struct _nir_rt_resume_indices){0, __VA_ARGS__})
#endif
#define nir_rt_resume nir_build_rt_resume
#define nir_build_rt_return_amd _nir_build_rt_return_amd
#define nir_rt_return_amd nir_build_rt_return_amd
#ifdef __cplusplus
#define nir_build_rt_trace_ray(build, src0, src1, src2, src3, src4, src5, src6, src7, src8, src9, src10, ...) \
_nir_build_rt_trace_ray(build, src0, src1, src2, src3, src4, src5, src6, src7, src8, src9, src10, _nir_rt_trace_ray_indices{0, __VA_ARGS__})
#else
#define nir_build_rt_trace_ray(build, src0, src1, src2, src3, src4, src5, src6, src7, src8, src9, src10, ...) \
_nir_build_rt_trace_ray(build, src0, src1, src2, src3, src4, src5, src6, src7, src8, src9, src10, (struct _nir_rt_trace_ray_indices){0, __VA_ARGS__})
#endif
#define nir_rt_trace_ray nir_build_rt_trace_ray
#ifdef __cplusplus
#define nir_build_scoped_barrier(build, ...) \
_nir_build_scoped_barrier(build, _nir_scoped_barrier_indices{0, __VA_ARGS__})
#else
#define nir_build_scoped_barrier(build, ...) \
_nir_build_scoped_barrier(build, (struct _nir_scoped_barrier_indices){0, __VA_ARGS__})
#endif
#define nir_scoped_barrier nir_build_scoped_barrier
#ifdef __cplusplus
#define nir_build_set_vertex_and_primitive_count(build, src0, src1, ...) \
_nir_build_set_vertex_and_primitive_count(build, src0, src1, _nir_set_vertex_and_primitive_count_indices{0, __VA_ARGS__})
#else
#define nir_build_set_vertex_and_primitive_count(build, src0, src1, ...) \
_nir_build_set_vertex_and_primitive_count(build, src0, src1, (struct _nir_set_vertex_and_primitive_count_indices){0, __VA_ARGS__})
#endif
#define nir_set_vertex_and_primitive_count nir_build_set_vertex_and_primitive_count
#ifdef __cplusplus
#define nir_build_shader_clock(build, ...) \
_nir_build_shader_clock(build, _nir_shader_clock_indices{0, __VA_ARGS__})
#else
#define nir_build_shader_clock(build, ...) \
_nir_build_shader_clock(build, (struct _nir_shader_clock_indices){0, __VA_ARGS__})
#endif
#define nir_shader_clock nir_build_shader_clock
#ifdef __cplusplus
#define nir_build_shared_atomic_add(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_add(build, bit_size, src0, src1, _nir_shared_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_add(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_add(build, bit_size, src0, src1, (struct _nir_shared_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_add nir_build_shared_atomic_add
#define nir_build_shared_atomic_add_dxil _nir_build_shared_atomic_add_dxil
#define nir_shared_atomic_add_dxil nir_build_shared_atomic_add_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_and(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_and(build, bit_size, src0, src1, _nir_shared_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_and(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_and(build, bit_size, src0, src1, (struct _nir_shared_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_and nir_build_shared_atomic_and
#define nir_build_shared_atomic_and_dxil _nir_build_shared_atomic_and_dxil
#define nir_shared_atomic_and_dxil nir_build_shared_atomic_and_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_shared_atomic_comp_swap(build, bit_size, src0, src1, src2, _nir_shared_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_shared_atomic_comp_swap(build, bit_size, src0, src1, src2, (struct _nir_shared_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_comp_swap nir_build_shared_atomic_comp_swap
#define nir_build_shared_atomic_comp_swap_dxil _nir_build_shared_atomic_comp_swap_dxil
#define nir_shared_atomic_comp_swap_dxil nir_build_shared_atomic_comp_swap_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_exchange(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_exchange(build, bit_size, src0, src1, _nir_shared_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_exchange(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_exchange(build, bit_size, src0, src1, (struct _nir_shared_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_exchange nir_build_shared_atomic_exchange
#define nir_build_shared_atomic_exchange_dxil _nir_build_shared_atomic_exchange_dxil
#define nir_shared_atomic_exchange_dxil nir_build_shared_atomic_exchange_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_fadd(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_fadd(build, bit_size, src0, src1, _nir_shared_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_fadd(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_fadd(build, bit_size, src0, src1, (struct _nir_shared_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_fadd nir_build_shared_atomic_fadd
#ifdef __cplusplus
#define nir_build_shared_atomic_fcomp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_shared_atomic_fcomp_swap(build, bit_size, src0, src1, src2, _nir_shared_atomic_fcomp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_fcomp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_shared_atomic_fcomp_swap(build, bit_size, src0, src1, src2, (struct _nir_shared_atomic_fcomp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_fcomp_swap nir_build_shared_atomic_fcomp_swap
#ifdef __cplusplus
#define nir_build_shared_atomic_fmax(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_fmax(build, bit_size, src0, src1, _nir_shared_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_fmax(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_fmax(build, bit_size, src0, src1, (struct _nir_shared_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_fmax nir_build_shared_atomic_fmax
#ifdef __cplusplus
#define nir_build_shared_atomic_fmin(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_fmin(build, bit_size, src0, src1, _nir_shared_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_fmin(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_fmin(build, bit_size, src0, src1, (struct _nir_shared_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_fmin nir_build_shared_atomic_fmin
#ifdef __cplusplus
#define nir_build_shared_atomic_imax(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_imax(build, bit_size, src0, src1, _nir_shared_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_imax(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_imax(build, bit_size, src0, src1, (struct _nir_shared_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_imax nir_build_shared_atomic_imax
#define nir_build_shared_atomic_imax_dxil _nir_build_shared_atomic_imax_dxil
#define nir_shared_atomic_imax_dxil nir_build_shared_atomic_imax_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_imin(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_imin(build, bit_size, src0, src1, _nir_shared_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_imin(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_imin(build, bit_size, src0, src1, (struct _nir_shared_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_imin nir_build_shared_atomic_imin
#define nir_build_shared_atomic_imin_dxil _nir_build_shared_atomic_imin_dxil
#define nir_shared_atomic_imin_dxil nir_build_shared_atomic_imin_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_or(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_or(build, bit_size, src0, src1, _nir_shared_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_or(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_or(build, bit_size, src0, src1, (struct _nir_shared_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_or nir_build_shared_atomic_or
#define nir_build_shared_atomic_or_dxil _nir_build_shared_atomic_or_dxil
#define nir_shared_atomic_or_dxil nir_build_shared_atomic_or_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_umax(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_umax(build, bit_size, src0, src1, _nir_shared_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_umax(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_umax(build, bit_size, src0, src1, (struct _nir_shared_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_umax nir_build_shared_atomic_umax
#define nir_build_shared_atomic_umax_dxil _nir_build_shared_atomic_umax_dxil
#define nir_shared_atomic_umax_dxil nir_build_shared_atomic_umax_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_umin(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_umin(build, bit_size, src0, src1, _nir_shared_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_umin(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_umin(build, bit_size, src0, src1, (struct _nir_shared_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_umin nir_build_shared_atomic_umin
#define nir_build_shared_atomic_umin_dxil _nir_build_shared_atomic_umin_dxil
#define nir_shared_atomic_umin_dxil nir_build_shared_atomic_umin_dxil
#ifdef __cplusplus
#define nir_build_shared_atomic_xor(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_xor(build, bit_size, src0, src1, _nir_shared_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_shared_atomic_xor(build, bit_size, src0, src1, ...) \
_nir_build_shared_atomic_xor(build, bit_size, src0, src1, (struct _nir_shared_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_shared_atomic_xor nir_build_shared_atomic_xor
#define nir_build_shared_atomic_xor_dxil _nir_build_shared_atomic_xor_dxil
#define nir_shared_atomic_xor_dxil nir_build_shared_atomic_xor_dxil
#define nir_build_shuffle _nir_build_shuffle
#define nir_shuffle nir_build_shuffle
#define nir_build_shuffle_down _nir_build_shuffle_down
#define nir_shuffle_down nir_build_shuffle_down
#define nir_build_shuffle_up _nir_build_shuffle_up
#define nir_shuffle_up nir_build_shuffle_up
#define nir_build_shuffle_xor _nir_build_shuffle_xor
#define nir_shuffle_xor nir_build_shuffle_xor
#define nir_build_sparse_residency_code_and _nir_build_sparse_residency_code_and
#define nir_sparse_residency_code_and nir_build_sparse_residency_code_and
#ifdef __cplusplus
#define nir_build_ssbo_atomic_add(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_add(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_add(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_add(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_add nir_build_ssbo_atomic_add
#ifdef __cplusplus
#define nir_build_ssbo_atomic_add_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_add_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_add_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_add_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_add_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_add_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_add_ir3 nir_build_ssbo_atomic_add_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_and(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_and(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_and(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_and(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_and nir_build_ssbo_atomic_and
#ifdef __cplusplus
#define nir_build_ssbo_atomic_and_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_and_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_and_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_and_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_and_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_and_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_and_ir3 nir_build_ssbo_atomic_and_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_comp_swap(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_comp_swap nir_build_ssbo_atomic_comp_swap
#ifdef __cplusplus
#define nir_build_ssbo_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_ssbo_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, src3, src4, _nir_ssbo_atomic_comp_swap_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, src3, src4, ...) \
_nir_build_ssbo_atomic_comp_swap_ir3(build, bit_size, src0, src1, src2, src3, src4, (struct _nir_ssbo_atomic_comp_swap_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_comp_swap_ir3 nir_build_ssbo_atomic_comp_swap_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_exchange(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_exchange(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_exchange(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_exchange(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_exchange nir_build_ssbo_atomic_exchange
#ifdef __cplusplus
#define nir_build_ssbo_atomic_exchange_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_exchange_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_exchange_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_exchange_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_exchange_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_exchange_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_exchange_ir3 nir_build_ssbo_atomic_exchange_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_fadd(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_fadd(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_fadd(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_fadd(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_fadd nir_build_ssbo_atomic_fadd
#ifdef __cplusplus
#define nir_build_ssbo_atomic_fcomp_swap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_fcomp_swap(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_fcomp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_fcomp_swap(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_fcomp_swap(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_fcomp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_fcomp_swap nir_build_ssbo_atomic_fcomp_swap
#ifdef __cplusplus
#define nir_build_ssbo_atomic_fmax(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_fmax(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_fmax(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_fmax(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_fmax nir_build_ssbo_atomic_fmax
#ifdef __cplusplus
#define nir_build_ssbo_atomic_fmin(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_fmin(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_fmin(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_fmin(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_fmin nir_build_ssbo_atomic_fmin
#ifdef __cplusplus
#define nir_build_ssbo_atomic_imax(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_imax(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_imax(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_imax(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_imax nir_build_ssbo_atomic_imax
#ifdef __cplusplus
#define nir_build_ssbo_atomic_imax_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_imax_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_imax_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_imax_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_imax_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_imax_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_imax_ir3 nir_build_ssbo_atomic_imax_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_imin(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_imin(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_imin(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_imin(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_imin nir_build_ssbo_atomic_imin
#ifdef __cplusplus
#define nir_build_ssbo_atomic_imin_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_imin_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_imin_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_imin_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_imin_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_imin_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_imin_ir3 nir_build_ssbo_atomic_imin_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_or(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_or(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_or(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_or(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_or nir_build_ssbo_atomic_or
#ifdef __cplusplus
#define nir_build_ssbo_atomic_or_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_or_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_or_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_or_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_or_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_or_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_or_ir3 nir_build_ssbo_atomic_or_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_umax(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_umax(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_umax(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_umax(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_umax nir_build_ssbo_atomic_umax
#ifdef __cplusplus
#define nir_build_ssbo_atomic_umax_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_umax_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_umax_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_umax_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_umax_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_umax_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_umax_ir3 nir_build_ssbo_atomic_umax_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_umin(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_umin(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_umin(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_umin(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_umin nir_build_ssbo_atomic_umin
#ifdef __cplusplus
#define nir_build_ssbo_atomic_umin_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_umin_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_umin_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_umin_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_umin_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_umin_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_umin_ir3 nir_build_ssbo_atomic_umin_ir3
#ifdef __cplusplus
#define nir_build_ssbo_atomic_xor(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_xor(build, bit_size, src0, src1, src2, _nir_ssbo_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_xor(build, bit_size, src0, src1, src2, ...) \
_nir_build_ssbo_atomic_xor(build, bit_size, src0, src1, src2, (struct _nir_ssbo_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_xor nir_build_ssbo_atomic_xor
#ifdef __cplusplus
#define nir_build_ssbo_atomic_xor_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_xor_ir3(build, bit_size, src0, src1, src2, src3, _nir_ssbo_atomic_xor_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_ssbo_atomic_xor_ir3(build, bit_size, src0, src1, src2, src3, ...) \
_nir_build_ssbo_atomic_xor_ir3(build, bit_size, src0, src1, src2, src3, (struct _nir_ssbo_atomic_xor_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_ssbo_atomic_xor_ir3 nir_build_ssbo_atomic_xor_ir3
#ifdef __cplusplus
#define nir_build_store_agx(build, src0, src1, src2, ...) \
_nir_build_store_agx(build, src0, src1, src2, _nir_store_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_store_agx(build, src0, src1, src2, ...) \
_nir_build_store_agx(build, src0, src1, src2, (struct _nir_store_agx_indices){0, __VA_ARGS__})
#endif
#define nir_store_agx nir_build_store_agx
#ifdef __cplusplus
#define nir_build_store_buffer_amd(build, src0, src1, src2, src3, src4, ...) \
_nir_build_store_buffer_amd(build, src0, src1, src2, src3, src4, _nir_store_buffer_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_store_buffer_amd(build, src0, src1, src2, src3, src4, ...) \
_nir_build_store_buffer_amd(build, src0, src1, src2, src3, src4, (struct _nir_store_buffer_amd_indices){0, __VA_ARGS__})
#endif
#define nir_store_buffer_amd nir_build_store_buffer_amd
#ifdef __cplusplus
#define nir_build_store_combined_output_pan(build, src0, src1, src2, src3, src4, ...) \
_nir_build_store_combined_output_pan(build, src0, src1, src2, src3, src4, _nir_store_combined_output_pan_indices{0, __VA_ARGS__})
#else
#define nir_build_store_combined_output_pan(build, src0, src1, src2, src3, src4, ...) \
_nir_build_store_combined_output_pan(build, src0, src1, src2, src3, src4, (struct _nir_store_combined_output_pan_indices){0, __VA_ARGS__})
#endif
#define nir_store_combined_output_pan nir_build_store_combined_output_pan
#ifdef __cplusplus
#define nir_build_store_deref(build, src0, src1, ...) \
_nir_build_store_deref(build, src0, src1, _nir_store_deref_indices{0, __VA_ARGS__})
#else
#define nir_build_store_deref(build, src0, src1, ...) \
_nir_build_store_deref(build, src0, src1, (struct _nir_store_deref_indices){0, __VA_ARGS__})
#endif
#define nir_store_deref nir_build_store_deref
#ifdef __cplusplus
#define nir_build_store_deref_block_intel(build, src0, src1, ...) \
_nir_build_store_deref_block_intel(build, src0, src1, _nir_store_deref_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_store_deref_block_intel(build, src0, src1, ...) \
_nir_build_store_deref_block_intel(build, src0, src1, (struct _nir_store_deref_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_store_deref_block_intel nir_build_store_deref_block_intel
#ifdef __cplusplus
#define nir_build_store_global(build, src0, src1, ...) \
_nir_build_store_global(build, src0, src1, _nir_store_global_indices{0, __VA_ARGS__})
#else
#define nir_build_store_global(build, src0, src1, ...) \
_nir_build_store_global(build, src0, src1, (struct _nir_store_global_indices){0, __VA_ARGS__})
#endif
#define nir_store_global nir_build_store_global
#ifdef __cplusplus
#define nir_build_store_global_2x32(build, src0, src1, ...) \
_nir_build_store_global_2x32(build, src0, src1, _nir_store_global_2x32_indices{0, __VA_ARGS__})
#else
#define nir_build_store_global_2x32(build, src0, src1, ...) \
_nir_build_store_global_2x32(build, src0, src1, (struct _nir_store_global_2x32_indices){0, __VA_ARGS__})
#endif
#define nir_store_global_2x32 nir_build_store_global_2x32
#ifdef __cplusplus
#define nir_build_store_global_amd(build, src0, src1, src2, ...) \
_nir_build_store_global_amd(build, src0, src1, src2, _nir_store_global_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_store_global_amd(build, src0, src1, src2, ...) \
_nir_build_store_global_amd(build, src0, src1, src2, (struct _nir_store_global_amd_indices){0, __VA_ARGS__})
#endif
#define nir_store_global_amd nir_build_store_global_amd
#ifdef __cplusplus
#define nir_build_store_global_block_intel(build, src0, src1, ...) \
_nir_build_store_global_block_intel(build, src0, src1, _nir_store_global_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_store_global_block_intel(build, src0, src1, ...) \
_nir_build_store_global_block_intel(build, src0, src1, (struct _nir_store_global_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_store_global_block_intel nir_build_store_global_block_intel
#ifdef __cplusplus
#define nir_build_store_global_ir3(build, src0, src1, src2, ...) \
_nir_build_store_global_ir3(build, src0, src1, src2, _nir_store_global_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_store_global_ir3(build, src0, src1, src2, ...) \
_nir_build_store_global_ir3(build, src0, src1, src2, (struct _nir_store_global_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_store_global_ir3 nir_build_store_global_ir3
#ifdef __cplusplus
#define nir_build_store_hit_attrib_amd(build, src0, ...) \
_nir_build_store_hit_attrib_amd(build, src0, _nir_store_hit_attrib_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_store_hit_attrib_amd(build, src0, ...) \
_nir_build_store_hit_attrib_amd(build, src0, (struct _nir_store_hit_attrib_amd_indices){0, __VA_ARGS__})
#endif
#define nir_store_hit_attrib_amd nir_build_store_hit_attrib_amd
#ifdef __cplusplus
#define nir_build_store_local_pixel_agx(build, src0, src1, ...) \
_nir_build_store_local_pixel_agx(build, src0, src1, _nir_store_local_pixel_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_store_local_pixel_agx(build, src0, src1, ...) \
_nir_build_store_local_pixel_agx(build, src0, src1, (struct _nir_store_local_pixel_agx_indices){0, __VA_ARGS__})
#endif
#define nir_store_local_pixel_agx nir_build_store_local_pixel_agx
#ifdef __cplusplus
#define nir_build_store_local_shared_r600(build, src0, src1, ...) \
_nir_build_store_local_shared_r600(build, src0, src1, _nir_store_local_shared_r600_indices{0, __VA_ARGS__})
#else
#define nir_build_store_local_shared_r600(build, src0, src1, ...) \
_nir_build_store_local_shared_r600(build, src0, src1, (struct _nir_store_local_shared_r600_indices){0, __VA_ARGS__})
#endif
#define nir_store_local_shared_r600 nir_build_store_local_shared_r600
#ifdef __cplusplus
#define nir_build_store_output(build, src0, src1, ...) \
_nir_build_store_output(build, src0, src1, _nir_store_output_indices{0, __VA_ARGS__})
#else
#define nir_build_store_output(build, src0, src1, ...) \
_nir_build_store_output(build, src0, src1, (struct _nir_store_output_indices){0, __VA_ARGS__})
#endif
#define nir_store_output nir_build_store_output
#ifdef __cplusplus
#define nir_build_store_per_primitive_output(build, src0, src1, src2, ...) \
_nir_build_store_per_primitive_output(build, src0, src1, src2, _nir_store_per_primitive_output_indices{0, __VA_ARGS__})
#else
#define nir_build_store_per_primitive_output(build, src0, src1, src2, ...) \
_nir_build_store_per_primitive_output(build, src0, src1, src2, (struct _nir_store_per_primitive_output_indices){0, __VA_ARGS__})
#endif
#define nir_store_per_primitive_output nir_build_store_per_primitive_output
#ifdef __cplusplus
#define nir_build_store_per_vertex_output(build, src0, src1, src2, ...) \
_nir_build_store_per_vertex_output(build, src0, src1, src2, _nir_store_per_vertex_output_indices{0, __VA_ARGS__})
#else
#define nir_build_store_per_vertex_output(build, src0, src1, src2, ...) \
_nir_build_store_per_vertex_output(build, src0, src1, src2, (struct _nir_store_per_vertex_output_indices){0, __VA_ARGS__})
#endif
#define nir_store_per_vertex_output nir_build_store_per_vertex_output
#ifdef __cplusplus
#define nir_build_store_preamble(build, src0, ...) \
_nir_build_store_preamble(build, src0, _nir_store_preamble_indices{0, __VA_ARGS__})
#else
#define nir_build_store_preamble(build, src0, ...) \
_nir_build_store_preamble(build, src0, (struct _nir_store_preamble_indices){0, __VA_ARGS__})
#endif
#define nir_store_preamble nir_build_store_preamble
#ifdef __cplusplus
#define nir_build_store_raw_output_pan(build, src0, ...) \
_nir_build_store_raw_output_pan(build, src0, _nir_store_raw_output_pan_indices{0, __VA_ARGS__})
#else
#define nir_build_store_raw_output_pan(build, src0, ...) \
_nir_build_store_raw_output_pan(build, src0, (struct _nir_store_raw_output_pan_indices){0, __VA_ARGS__})
#endif
#define nir_store_raw_output_pan nir_build_store_raw_output_pan
#ifdef __cplusplus
#define nir_build_store_scratch(build, src0, src1, ...) \
_nir_build_store_scratch(build, src0, src1, _nir_store_scratch_indices{0, __VA_ARGS__})
#else
#define nir_build_store_scratch(build, src0, src1, ...) \
_nir_build_store_scratch(build, src0, src1, (struct _nir_store_scratch_indices){0, __VA_ARGS__})
#endif
#define nir_store_scratch nir_build_store_scratch
#define nir_build_store_scratch_dxil _nir_build_store_scratch_dxil
#define nir_store_scratch_dxil nir_build_store_scratch_dxil
#ifdef __cplusplus
#define nir_build_store_shared(build, src0, src1, ...) \
_nir_build_store_shared(build, src0, src1, _nir_store_shared_indices{0, __VA_ARGS__})
#else
#define nir_build_store_shared(build, src0, src1, ...) \
_nir_build_store_shared(build, src0, src1, (struct _nir_store_shared_indices){0, __VA_ARGS__})
#endif
#define nir_store_shared nir_build_store_shared
#ifdef __cplusplus
#define nir_build_store_shared2_amd(build, src0, src1, ...) \
_nir_build_store_shared2_amd(build, src0, src1, _nir_store_shared2_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_store_shared2_amd(build, src0, src1, ...) \
_nir_build_store_shared2_amd(build, src0, src1, (struct _nir_store_shared2_amd_indices){0, __VA_ARGS__})
#endif
#define nir_store_shared2_amd nir_build_store_shared2_amd
#ifdef __cplusplus
#define nir_build_store_shared_block_intel(build, src0, src1, ...) \
_nir_build_store_shared_block_intel(build, src0, src1, _nir_store_shared_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_store_shared_block_intel(build, src0, src1, ...) \
_nir_build_store_shared_block_intel(build, src0, src1, (struct _nir_store_shared_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_store_shared_block_intel nir_build_store_shared_block_intel
#define nir_build_store_shared_dxil _nir_build_store_shared_dxil
#define nir_store_shared_dxil nir_build_store_shared_dxil
#ifdef __cplusplus
#define nir_build_store_shared_ir3(build, src0, src1, ...) \
_nir_build_store_shared_ir3(build, src0, src1, _nir_store_shared_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_store_shared_ir3(build, src0, src1, ...) \
_nir_build_store_shared_ir3(build, src0, src1, (struct _nir_store_shared_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_store_shared_ir3 nir_build_store_shared_ir3
#define nir_build_store_shared_masked_dxil _nir_build_store_shared_masked_dxil
#define nir_store_shared_masked_dxil nir_build_store_shared_masked_dxil
#ifdef __cplusplus
#define nir_build_store_ssbo(build, src0, src1, src2, ...) \
_nir_build_store_ssbo(build, src0, src1, src2, _nir_store_ssbo_indices{0, __VA_ARGS__})
#else
#define nir_build_store_ssbo(build, src0, src1, src2, ...) \
_nir_build_store_ssbo(build, src0, src1, src2, (struct _nir_store_ssbo_indices){0, __VA_ARGS__})
#endif
#define nir_store_ssbo nir_build_store_ssbo
#ifdef __cplusplus
#define nir_build_store_ssbo_block_intel(build, src0, src1, src2, ...) \
_nir_build_store_ssbo_block_intel(build, src0, src1, src2, _nir_store_ssbo_block_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_store_ssbo_block_intel(build, src0, src1, src2, ...) \
_nir_build_store_ssbo_block_intel(build, src0, src1, src2, (struct _nir_store_ssbo_block_intel_indices){0, __VA_ARGS__})
#endif
#define nir_store_ssbo_block_intel nir_build_store_ssbo_block_intel
#ifdef __cplusplus
#define nir_build_store_ssbo_ir3(build, src0, src1, src2, src3, ...) \
_nir_build_store_ssbo_ir3(build, src0, src1, src2, src3, _nir_store_ssbo_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_store_ssbo_ir3(build, src0, src1, src2, src3, ...) \
_nir_build_store_ssbo_ir3(build, src0, src1, src2, src3, (struct _nir_store_ssbo_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_store_ssbo_ir3 nir_build_store_ssbo_ir3
#define nir_build_store_ssbo_masked_dxil _nir_build_store_ssbo_masked_dxil
#define nir_store_ssbo_masked_dxil nir_build_store_ssbo_masked_dxil
#ifdef __cplusplus
#define nir_build_store_stack(build, src0, ...) \
_nir_build_store_stack(build, src0, _nir_store_stack_indices{0, __VA_ARGS__})
#else
#define nir_build_store_stack(build, src0, ...) \
_nir_build_store_stack(build, src0, (struct _nir_store_stack_indices){0, __VA_ARGS__})
#endif
#define nir_store_stack nir_build_store_stack
#ifdef __cplusplus
#define nir_build_store_task_payload(build, src0, src1, ...) \
_nir_build_store_task_payload(build, src0, src1, _nir_store_task_payload_indices{0, __VA_ARGS__})
#else
#define nir_build_store_task_payload(build, src0, src1, ...) \
_nir_build_store_task_payload(build, src0, src1, (struct _nir_store_task_payload_indices){0, __VA_ARGS__})
#endif
#define nir_store_task_payload nir_build_store_task_payload
#define nir_build_store_tf_r600 _nir_build_store_tf_r600
#define nir_store_tf_r600 nir_build_store_tf_r600
#ifdef __cplusplus
#define nir_build_store_tlb_sample_color_v3d(build, src0, src1, ...) \
_nir_build_store_tlb_sample_color_v3d(build, src0, src1, _nir_store_tlb_sample_color_v3d_indices{0, __VA_ARGS__})
#else
#define nir_build_store_tlb_sample_color_v3d(build, src0, src1, ...) \
_nir_build_store_tlb_sample_color_v3d(build, src0, src1, (struct _nir_store_tlb_sample_color_v3d_indices){0, __VA_ARGS__})
#endif
#define nir_store_tlb_sample_color_v3d nir_build_store_tlb_sample_color_v3d
#ifdef __cplusplus
#define nir_build_store_uniform_ir3(build, src0, ...) \
_nir_build_store_uniform_ir3(build, src0, _nir_store_uniform_ir3_indices{0, __VA_ARGS__})
#else
#define nir_build_store_uniform_ir3(build, src0, ...) \
_nir_build_store_uniform_ir3(build, src0, (struct _nir_store_uniform_ir3_indices){0, __VA_ARGS__})
#endif
#define nir_store_uniform_ir3 nir_build_store_uniform_ir3
#ifdef __cplusplus
#define nir_build_store_zs_agx(build, src0, src1, src2, ...) \
_nir_build_store_zs_agx(build, src0, src1, src2, _nir_store_zs_agx_indices{0, __VA_ARGS__})
#else
#define nir_build_store_zs_agx(build, src0, src1, src2, ...) \
_nir_build_store_zs_agx(build, src0, src1, src2, (struct _nir_store_zs_agx_indices){0, __VA_ARGS__})
#endif
#define nir_store_zs_agx nir_build_store_zs_agx
#ifdef __cplusplus
#define nir_build_task_payload_atomic_add(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_add(build, bit_size, src0, src1, _nir_task_payload_atomic_add_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_add(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_add(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_add_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_add nir_build_task_payload_atomic_add
#ifdef __cplusplus
#define nir_build_task_payload_atomic_and(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_and(build, bit_size, src0, src1, _nir_task_payload_atomic_and_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_and(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_and(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_and_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_and nir_build_task_payload_atomic_and
#ifdef __cplusplus
#define nir_build_task_payload_atomic_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_task_payload_atomic_comp_swap(build, bit_size, src0, src1, src2, _nir_task_payload_atomic_comp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_comp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_task_payload_atomic_comp_swap(build, bit_size, src0, src1, src2, (struct _nir_task_payload_atomic_comp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_comp_swap nir_build_task_payload_atomic_comp_swap
#ifdef __cplusplus
#define nir_build_task_payload_atomic_exchange(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_exchange(build, bit_size, src0, src1, _nir_task_payload_atomic_exchange_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_exchange(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_exchange(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_exchange_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_exchange nir_build_task_payload_atomic_exchange
#ifdef __cplusplus
#define nir_build_task_payload_atomic_fadd(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_fadd(build, bit_size, src0, src1, _nir_task_payload_atomic_fadd_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_fadd(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_fadd(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_fadd_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_fadd nir_build_task_payload_atomic_fadd
#ifdef __cplusplus
#define nir_build_task_payload_atomic_fcomp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_task_payload_atomic_fcomp_swap(build, bit_size, src0, src1, src2, _nir_task_payload_atomic_fcomp_swap_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_fcomp_swap(build, bit_size, src0, src1, src2, ...) \
_nir_build_task_payload_atomic_fcomp_swap(build, bit_size, src0, src1, src2, (struct _nir_task_payload_atomic_fcomp_swap_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_fcomp_swap nir_build_task_payload_atomic_fcomp_swap
#ifdef __cplusplus
#define nir_build_task_payload_atomic_fmax(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_fmax(build, bit_size, src0, src1, _nir_task_payload_atomic_fmax_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_fmax(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_fmax(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_fmax_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_fmax nir_build_task_payload_atomic_fmax
#ifdef __cplusplus
#define nir_build_task_payload_atomic_fmin(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_fmin(build, bit_size, src0, src1, _nir_task_payload_atomic_fmin_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_fmin(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_fmin(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_fmin_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_fmin nir_build_task_payload_atomic_fmin
#ifdef __cplusplus
#define nir_build_task_payload_atomic_imax(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_imax(build, bit_size, src0, src1, _nir_task_payload_atomic_imax_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_imax(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_imax(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_imax_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_imax nir_build_task_payload_atomic_imax
#ifdef __cplusplus
#define nir_build_task_payload_atomic_imin(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_imin(build, bit_size, src0, src1, _nir_task_payload_atomic_imin_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_imin(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_imin(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_imin_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_imin nir_build_task_payload_atomic_imin
#ifdef __cplusplus
#define nir_build_task_payload_atomic_or(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_or(build, bit_size, src0, src1, _nir_task_payload_atomic_or_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_or(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_or(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_or_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_or nir_build_task_payload_atomic_or
#ifdef __cplusplus
#define nir_build_task_payload_atomic_umax(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_umax(build, bit_size, src0, src1, _nir_task_payload_atomic_umax_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_umax(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_umax(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_umax_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_umax nir_build_task_payload_atomic_umax
#ifdef __cplusplus
#define nir_build_task_payload_atomic_umin(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_umin(build, bit_size, src0, src1, _nir_task_payload_atomic_umin_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_umin(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_umin(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_umin_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_umin nir_build_task_payload_atomic_umin
#ifdef __cplusplus
#define nir_build_task_payload_atomic_xor(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_xor(build, bit_size, src0, src1, _nir_task_payload_atomic_xor_indices{0, __VA_ARGS__})
#else
#define nir_build_task_payload_atomic_xor(build, bit_size, src0, src1, ...) \
_nir_build_task_payload_atomic_xor(build, bit_size, src0, src1, (struct _nir_task_payload_atomic_xor_indices){0, __VA_ARGS__})
#endif
#define nir_task_payload_atomic_xor nir_build_task_payload_atomic_xor
#define nir_build_terminate _nir_build_terminate
#define nir_terminate nir_build_terminate
#define nir_build_terminate_if _nir_build_terminate_if
#define nir_terminate_if nir_build_terminate_if
#define nir_build_terminate_ray _nir_build_terminate_ray
#define nir_terminate_ray nir_build_terminate_ray
#define nir_build_trace_ray _nir_build_trace_ray
#define nir_trace_ray nir_build_trace_ray
#ifdef __cplusplus
#define nir_build_trace_ray_intel(build, src0, src1, src2, ...) \
_nir_build_trace_ray_intel(build, src0, src1, src2, _nir_trace_ray_intel_indices{0, __VA_ARGS__})
#else
#define nir_build_trace_ray_intel(build, src0, src1, src2, ...) \
_nir_build_trace_ray_intel(build, src0, src1, src2, (struct _nir_trace_ray_intel_indices){0, __VA_ARGS__})
#endif
#define nir_trace_ray_intel nir_build_trace_ray_intel
#define nir_build_vote_all _nir_build_vote_all
#define nir_vote_all nir_build_vote_all
#define nir_build_vote_any _nir_build_vote_any
#define nir_vote_any nir_build_vote_any
#define nir_build_vote_feq _nir_build_vote_feq
#define nir_vote_feq nir_build_vote_feq
#define nir_build_vote_ieq _nir_build_vote_ieq
#define nir_vote_ieq nir_build_vote_ieq
#ifdef __cplusplus
#define nir_build_vulkan_resource_index(build, num_components, bit_size, src0, ...) \
_nir_build_vulkan_resource_index(build, num_components, bit_size, src0, _nir_vulkan_resource_index_indices{0, __VA_ARGS__})
#else
#define nir_build_vulkan_resource_index(build, num_components, bit_size, src0, ...) \
_nir_build_vulkan_resource_index(build, num_components, bit_size, src0, (struct _nir_vulkan_resource_index_indices){0, __VA_ARGS__})
#endif
#define nir_vulkan_resource_index nir_build_vulkan_resource_index
#ifdef __cplusplus
#define nir_build_vulkan_resource_reindex(build, bit_size, src0, src1, ...) \
_nir_build_vulkan_resource_reindex(build, bit_size, src0, src1, _nir_vulkan_resource_reindex_indices{0, __VA_ARGS__})
#else
#define nir_build_vulkan_resource_reindex(build, bit_size, src0, src1, ...) \
_nir_build_vulkan_resource_reindex(build, bit_size, src0, src1, (struct _nir_vulkan_resource_reindex_indices){0, __VA_ARGS__})
#endif
#define nir_vulkan_resource_reindex nir_build_vulkan_resource_reindex
#define nir_build_write_invocation_amd _nir_build_write_invocation_amd
#define nir_write_invocation_amd nir_build_write_invocation_amd
#ifdef __cplusplus
#define nir_build_xfb_counter_sub_amd(build, src0, ...) \
_nir_build_xfb_counter_sub_amd(build, src0, _nir_xfb_counter_sub_amd_indices{0, __VA_ARGS__})
#else
#define nir_build_xfb_counter_sub_amd(build, src0, ...) \
_nir_build_xfb_counter_sub_amd(build, src0, (struct _nir_xfb_counter_sub_amd_indices){0, __VA_ARGS__})
#endif
#define nir_xfb_counter_sub_amd nir_build_xfb_counter_sub_amd

#endif /* _NIR_BUILDER_OPCODES_ */
