# Copyright © 2019 Google LLC

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

files_mesa_format = [
  'u_format.c',
  'u_format_bptc.c',
  'u_format_etc.c',
  'u_format_fxt1.c',
  'u_format_latc.c',
  'u_format_other.c',
  'u_format_rgtc.c',
  'u_format_s3tc.c',
  'u_format_tests.c',
  'u_format_unpack_neon.c',
  'u_format_yuv.c',
  'u_format_zs.c',
]

# TODO: remove the quirk when GCC get test and fix inside release
# See: https://gcc.gnu.org/bugzilla/show_bug.cgi?id=108163
arm_neon_workaround = []
if cc.get_id() == 'gcc' and host_machine.cpu_family().contains('arm')
  arm_neon_workaround = gcc_lto_quirk
endif

u_format_pack_h = custom_target(
  'u_format_pack.h',
  input : ['u_format_table.py', 'u_format.csv'],
  output : 'u_format_pack.h',
  command : [prog_python, '@INPUT@', '--header'],
  depend_files : files('u_format_pack.py', 'u_format_parse.py'),
  capture : true,
)

u_format_table_c = custom_target(
  'u_format_table.c',
  input : ['u_format_table.py', 'u_format.csv'],
  output : 'u_format_table.c',
  command : [prog_python, '@INPUT@'],
  depend_files : files('u_format_pack.py', 'u_format_parse.py'),
  capture : true,
)

libmesa_format = static_library(
  'mesa_format',
  [files_mesa_format, u_format_table_c, u_format_pack_h],
  include_directories : [inc_include, inc_src, inc_mapi, inc_mesa, inc_gallium, inc_gallium_aux],
  # NOTE dep_valgrind used here instead of idep_mesautil due to chicken/egg
  # dependencies between util and util/format
  dependencies : [dep_m, dep_valgrind],
  c_args : [c_msvc_compat_args, arm_neon_workaround],
  gnu_symbol_visibility : 'hidden',
  build_by_default : false
)
