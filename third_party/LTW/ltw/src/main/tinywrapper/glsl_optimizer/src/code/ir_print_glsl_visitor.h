/* -*- c++ -*- */
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

#ifndef IR_PRINT_GLSL_VISITOR_H
#define IR_PRINT_GLSL_VISITOR_H

#include <string>
#include "../compiler/glsl/ir.h"
#include "../compiler/glsl/ir_visitor.h"
#include "../mesa/program/symbol_table.h"
#include "st_printf.h"

class loop_state;
class loop_variable_state;
class IR_TO_GLSL : public ir_visitor 
{
public:
	struct global_print_tracker
	{
		global_print_tracker();
		~global_print_tracker();

        const char* processed_uniform_blocks[64] = { 0 };
        int   num_uniform_blocks = 0;
		unsigned	var_counter;
		hash_table*	var_hash;
		exec_list	global_assignements;
		void* mem_ctx;
		bool	main_function_done;
        bool enable_nan_check;
	};

public:
	static char * Convert(
		exec_list *instructions,
		struct _mesa_glsl_parse_state *state);
	static void print_type(sbuffer& str, const glsl_type *t, bool arraySize);
	static void print_type_post(sbuffer& str, const glsl_type *t, bool arraySize);

public:
	IR_TO_GLSL(
		sbuffer& str, 
		global_print_tracker* vGlobals,
		const _mesa_glsl_parse_state* vState);
   virtual ~IR_TO_GLSL();

   void print_float_checked(sbuffer& str, float f);
   void indent(void);
   void end_statement_line();
   void newline_indent();
   void newline_deindent();
   void print_var_name(ir_variable* v);
   void print_generic_layout_block(ir_variable* v);
   const char *unique_name(ir_variable *var);
   void emit_assignment_part(ir_dereference* lhs, ir_rvalue* rhs, unsigned write_mask, ir_rvalue* dstIndex);
   bool can_emit_canonical_for(loop_variable_state *ls);
   bool emit_canonical_for(ir_loop* ir);
   bool try_print_array_assignment(ir_dereference* lhs, ir_rvalue* rhs);

   virtual void visit(ir_rvalue *);
   virtual void visit(ir_variable *);
   virtual void visit(ir_function_signature *);
   virtual void visit(ir_function *);
   virtual void visit(ir_expression *);
   virtual void visit(ir_texture *);
   virtual void visit(ir_swizzle *);
   virtual void visit(ir_dereference_variable *);
   virtual void visit(ir_dereference_array *);
   virtual void visit(ir_dereference_record *);
   virtual void visit(ir_assignment *);
   virtual void visit(ir_constant *);
   virtual void visit(ir_call *);
   virtual void visit(ir_return *);
   virtual void visit(ir_discard *);
   virtual void visit(ir_demote *);
   virtual void visit(ir_if *);
   virtual void visit(ir_loop *);
   virtual void visit(ir_loop_jump *);
   virtual void visit(ir_emit_vertex *);
   virtual void visit(ir_end_primitive *);
   virtual void visit(ir_barrier *);
   void visit_uniform_block(ir_variable* var);

public:
	int mode = 1;
	global_print_tracker* global;
	const _mesa_glsl_parse_state* state;
	hash_table *printable_names = 0;
	_mesa_symbol_table *symbols = 0;
	void *mem_ctx = 0;
	sbuffer& generated_source;
	loop_state* loopstate = 0;
	int expression_depth = 0;
	int indentation = 0;
	bool inside_loop_body = false;
	bool skipped_this_ir = false;
	bool previous_skipped = false;
	int uses_texlod_impl = 0; // 3 bits per tex_dimension, bit set for each precision if any texture sampler needs the GLES2 lod workaround.
	int uses_texlodproj_impl = 0; // 3 bits per tex_dimension, bit set for each precision if any texture sampler needs the GLES2 lod workaround.
};

#endif /* IR_PRINT_GLSL_VISITOR_H */
