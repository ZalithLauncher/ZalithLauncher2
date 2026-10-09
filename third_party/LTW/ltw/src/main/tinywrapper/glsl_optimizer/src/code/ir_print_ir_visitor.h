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

#ifndef IR_PRINT_IR_VISITOR_H
#define IR_PRINT_IR_VISITOR_H

#include <string>
#include "../compiler/glsl/ir.h"
#include "../compiler/glsl/ir_visitor.h"
#include "../mesa/program/symbol_table.h"
#include "st_printf.h"

class IR_TO_IR : public ir_visitor 
{
public:
	static char* Convert(
		struct exec_list *instructions,
		struct _mesa_glsl_parse_state *state);
	static void print_type(sbuffer& str, const glsl_type *t);

public:
	IR_TO_IR(sbuffer& str);
   virtual ~IR_TO_IR();

   void indent(void);

   /**
    * \name Visit methods
    *
    * As typical for the visitor pattern, there must be one \c visit method for
    * each concrete subclass of \c ir_instruction.  Virtual base classes within
    * the hierarchy should not have \c visit methods.
    */
   /*@{*/
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
   /*@}*/

private:
   /**
    * Fetch/generate a unique name for ir_variable.
    *
    * GLSL IR permits multiple ir_variables to share the same name.  This works
    * fine until we try to print it, when we really need a unique one.
    */
   const char *unique_name(ir_variable *var);

   /** A mapping from ir_variable * -> unique printable names. */
   hash_table *printable_names;
   _mesa_symbol_table *symbols;

   void *mem_ctx;
   sbuffer& generated_source;

   int indentation;
};

#endif /* IR_PRINT_IR_VISITOR_H */
