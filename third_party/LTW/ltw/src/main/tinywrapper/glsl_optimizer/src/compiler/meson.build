# Copyright © 2017 Intel Corporation

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

inc_compiler = include_directories('.')
inc_glsl = include_directories('glsl')
#inc_spirv = include_directories('spirv')

float64_glsl_file = files('glsl/float64.glsl')

files_libcompiler = files(
  'builtin_type_macros.h',
  'glsl_types.cpp',
  'glsl_types.h',
  'nir_types.cpp',
  'nir_types.h',
  'shader_enums.c',
  'shader_enums.h',
  'shader_info.h',
)

ir_expression_operation_h = custom_target(
  'ir_expression_operation.h',
  input : 'glsl/ir_expression_operation.py',
  output : 'ir_expression_operation.h',
  command : [prog_python, '@INPUT@', 'enum'],
  capture : true,
)

libcompiler = static_library(
  'compiler',
  [files_libcompiler, ir_expression_operation_h],
  include_directories : [inc_mapi, inc_mesa, inc_compiler, inc_include, inc_src, inc_gallium, inc_gallium_aux],
  c_args : [c_msvc_compat_args, no_override_init_args],
  cpp_args : [cpp_msvc_compat_args],
  gnu_symbol_visibility : 'hidden',
  dependencies : [dep_valgrind],
  build_by_default : false,
)

#subdir('spirv')

subdir('nir')



if with_clc
  subdir('clc')
endif
subdir('glsl')
