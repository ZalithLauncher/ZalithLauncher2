#include <inttypes.h>
#include "ir_print_ir_visitor.h"
#include "../compiler/glsl_types.h"
#include "../compiler/glsl/glsl_parser_extras.h"
#include "../util/macros.h"
#include "../util/hash_table.h"
#include "../util/u_string.h"

void IR_TO_IR::print_type(sbuffer& str, const glsl_type *t)
{
	if (t->is_array()) 
	{
		str.append("(array ");
		print_type(str, t->fields.array);
		str.append(" %u)", t->length);
	}
	else if (t->is_struct() && !is_gl_identifier(t->name)) 
	{
		str.append("%s@%p", t->name, (void *)t);
	}
	else 
	{
		str.append("%s", t->name);
	}
}

char* IR_TO_IR::Convert(exec_list *instructions, struct _mesa_glsl_parse_state *state)
{
	sbuffer res;

	if (state)
	{
		for (unsigned i = 0; i < state->num_user_structures; i++)
		{
			const glsl_type *const s = state->user_structures[i];
			res.append("(structure (%s) (%s@%p) (%u) (\n",
				s->name, s->name, (void *)s, s->length);
			for (unsigned j = 0; j < s->length; j++)
			{
				res.append("\t((");
				print_type(res, s->fields.structure[j].type);
				res.append(")(%s))\n", s->fields.structure[j].name);
			}
			res.append(")\n");
		}
	}

	res.append("(\n");
	foreach_in_list(ir_instruction, ir, instructions)
	{
		ir_instruction *deconsted = const_cast<ir_instruction *>(ir);
		IR_TO_IR v(res);
		deconsted->accept(&v);
		if (ir->ir_type != ir_type_function)
			res.append("\n");
	}
	res.append(")\n");

   // DANGER, A NEW STRING IS ALLOCATED !
   // YOUR PROGRAM HAS TO FREE IT !
   return res.c_str_take_ownership();
}

IR_TO_IR::IR_TO_IR(sbuffer& str) : generated_source(str)
{
   indentation = 0;
   printable_names = _mesa_pointer_hash_table_create(NULL);
   symbols = _mesa_symbol_table_ctor();
   mem_ctx = ralloc_context(NULL);
}

IR_TO_IR::~IR_TO_IR()
{
   _mesa_hash_table_destroy(printable_names, NULL);
   _mesa_symbol_table_dtor(symbols);
   ralloc_free(mem_ctx);
}

void IR_TO_IR::indent(void)
{
   for (int i = 0; i < indentation; i++)
      generated_source.append("  ");
}

const char *
IR_TO_IR::unique_name(ir_variable *var)
{
   /* var->name can be NULL in function prototypes when a type is given for a
    * parameter but no name is given.  In that case, just return an empty
    * string.  Don't worry about tracking the generated name in the printable
    * names hash because this is the only scope where it can ever appear.
    */
   if (var->name == NULL) {
      static unsigned arg = 1;
      return ralloc_asprintf(this->mem_ctx, "parameter@%u", arg++);
   }

   /* Do we already have a name for this variable? */
   struct hash_entry * entry =
      _mesa_hash_table_search(this->printable_names, var);

   if (entry != NULL) {
      return (const char *) entry->data;
   }

   /* If there's no conflict, just use the original name */
   const char* name = NULL;
   if (_mesa_symbol_table_find_symbol(this->symbols, var->name) == NULL) {
      name = var->name;
   } else {
      static unsigned i = 1;
      name = ralloc_asprintf(this->mem_ctx, "%s@%u", var->name, ++i);
   }
   _mesa_hash_table_insert(this->printable_names, var, (void *) name);
   _mesa_symbol_table_add_symbol(this->symbols, name, var);
   return name;
}

void IR_TO_IR::visit(ir_rvalue *)
{
   generated_source.append("error");
}

void IR_TO_IR::visit(ir_variable *ir)
{
   generated_source.append("(declare ");

   char binding[32] = {0};
   if (ir->data.binding)
      snprintf(binding, sizeof(binding), "binding=%i ", ir->data.binding);

   char loc[32] = {0};
   if (ir->data.location != -1)
      snprintf(loc, sizeof(loc), "location=%i ", ir->data.location);

   char component[32] = {0};
   if (ir->data.explicit_component || ir->data.location_frac != 0)
      snprintf(component, sizeof(component), "component=%i ",
                    ir->data.location_frac);

   char stream[32] = {0};
   if (ir->data.stream & (1u << 31)) {
      if (ir->data.stream & ~(1u << 31)) {
         snprintf(stream, sizeof(stream), "stream(%u,%u,%u,%u) ",
                  ir->data.stream & 3, (ir->data.stream >> 2) & 3,
                  (ir->data.stream >> 4) & 3, (ir->data.stream >> 6) & 3);
      }
   } else if (ir->data.stream) {
      snprintf(stream, sizeof(stream), "stream%u ", ir->data.stream);
   }

   char image_format[32] = {0};
   if (ir->data.image_format) {
      snprintf(image_format, sizeof(image_format), "format=%x ",
                    ir->data.image_format);
   }

   const char *const cent = (ir->data.centroid) ? "centroid " : "";
   const char *const samp = (ir->data.sample) ? "sample " : "";
   const char *const patc = (ir->data.patch) ? "patch " : "";
   const char *const inv = (ir->data.invariant) ? "invariant " : "";
   const char *const explicit_inv = (ir->data.explicit_invariant) ? "explicit_invariant " : "";
   const char *const prec = (ir->data.precise) ? "precise " : "";
   const char *const bindless = (ir->data.bindless) ? "bindless " : "";
   const char *const bound = (ir->data.bound) ? "bound " : "";
   const char *const memory_read_only = (ir->data.memory_read_only) ? "readonly " : "";
   const char *const memory_write_only = (ir->data.memory_write_only) ? "writeonly " : "";
   const char *const memory_coherent = (ir->data.memory_coherent) ? "coherent " : "";
   const char *const memory_volatile = (ir->data.memory_volatile) ? "volatile " : "";
   const char *const memory_restrict = (ir->data.memory_restrict) ? "restrict " : "";
   const char *const mode[] = { "", "uniform ", "shader_storage ",
                                "shader_shared ", "shader_in ", "shader_out ",
                                "in ", "out ", "inout ",
								"const_in ", "sys ", "temporary " };
   STATIC_ASSERT(ARRAY_SIZE(mode) == ir_var_mode_count);
   /* aiekick 03/01/2020 */
   const char *const precision[] = { "", "highp ", "mediump ", "lowp " };
   const char *const interp[] = { "", "smooth", "flat", "noperspective", "EXPLICIT ", "COLOR " };
   STATIC_ASSERT(ARRAY_SIZE(interp) == INTERP_MODE_COUNT);

   generated_source.append("(%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s) ",
           binding, loc, component, cent, bindless, bound,
           image_format, memory_read_only, memory_write_only,
           memory_coherent, memory_volatile, memory_restrict,
           samp, patc, inv, explicit_inv, prec, precision[ir->data.precision], mode[ir->data.mode],
           stream,
           interp[ir->data.interpolation]);

   print_type(generated_source, ir->type);
   generated_source.append(" %s)", unique_name(ir));
}


void IR_TO_IR::visit(ir_function_signature *ir)
{
   _mesa_symbol_table_push_scope(symbols);
   generated_source.append("(signature ");
   indentation++;

   print_type(generated_source, ir->return_type);
   generated_source.append("\n");
   indent();

   generated_source.append("(parameters\n");
   indentation++;

   foreach_in_list(ir_variable, inst, &ir->parameters) {
      indent();
      inst->accept(this);
      generated_source.append("\n");
   }
   indentation--;

   indent();
   generated_source.append(")\n");

   indent();

   generated_source.append("(\n");
   indentation++;

   foreach_in_list(ir_instruction, inst, &ir->body) {
      indent();
      inst->accept(this);
      generated_source.append("\n");
   }
   indentation--;
   indent();
   generated_source.append("))\n");
   indentation--;
   _mesa_symbol_table_pop_scope(symbols);
}


void IR_TO_IR::visit(ir_function *ir)
{
   generated_source.append("(%s function %s\n", ir->is_subroutine ? "subroutine" : "", ir->name);
   indentation++;
   foreach_in_list(ir_function_signature, sig, &ir->signatures) {
      indent();
      sig->accept(this);
      generated_source.append("\n");
   }
   indentation--;
   indent();
   generated_source.append(")\n\n");
}


void IR_TO_IR::visit(ir_expression *ir)
{
   generated_source.append("(expression ");

   print_type(generated_source, ir->type);

   generated_source.append(" %s ", ir_expression_operation_strings[ir->operation]);

   for (unsigned i = 0; i < ir->num_operands; i++) {
      ir->operands[i]->accept(this);
   }

   generated_source.append(") ");
}


void IR_TO_IR::visit(ir_texture *ir)
{
   generated_source.append("(%s ", ir->opcode_string());

   if (ir->op == ir_samples_identical) {
      ir->sampler->accept(this);
      generated_source.append(" ");
      ir->coordinate->accept(this);
      generated_source.append(")");
      return;
   }

   print_type(generated_source, ir->type);
   generated_source.append(" ");

   ir->sampler->accept(this);
   generated_source.append(" ");

   if (ir->op != ir_txs && ir->op != ir_query_levels &&
       ir->op != ir_texture_samples) {
      ir->coordinate->accept(this);

      generated_source.append(" ");

      if (ir->offset != NULL) {
	 ir->offset->accept(this);
      } else {
	 generated_source.append("0");
      }

      generated_source.append(" ");
   }

   if (ir->op != ir_txf && ir->op != ir_txf_ms &&
       ir->op != ir_txs && ir->op != ir_tg4 &&
       ir->op != ir_query_levels && ir->op != ir_texture_samples) {
      if (ir->projector)
	 ir->projector->accept(this);
      else
	 generated_source.append("1");

      if (ir->shadow_comparator) {
	 generated_source.append(" ");
	 ir->shadow_comparator->accept(this);
      } else {
	 generated_source.append(" ()");
      }
   }

   generated_source.append(" ");
   switch (ir->op)
   {
   case ir_tex:
   case ir_lod:
   case ir_query_levels:
   case ir_texture_samples:
      break;
   case ir_txb:
      ir->lod_info.bias->accept(this);
      break;
   case ir_txl:
   case ir_txf:
   case ir_txs:
      ir->lod_info.lod->accept(this);
      break;
   case ir_txf_ms:
      ir->lod_info.sample_index->accept(this);
      break;
   case ir_txd:
      generated_source.append("(");
      ir->lod_info.grad.dPdx->accept(this);
      generated_source.append(" ");
      ir->lod_info.grad.dPdy->accept(this);
      generated_source.append(")");
      break;
   case ir_tg4:
      ir->lod_info.component->accept(this);
      break;
   case ir_samples_identical:
      unreachable("ir_samples_identical was already handled");
   };
   generated_source.append(")");
}


void IR_TO_IR::visit(ir_swizzle *ir)
{
   const unsigned swiz[4] = {
      ir->mask.x,
      ir->mask.y,
      ir->mask.z,
      ir->mask.w,
   };

   generated_source.append("(swiz ");
   for (unsigned i = 0; i < ir->mask.num_components; i++) {
      generated_source.append("%c", "xyzw"[swiz[i]]);
   }
   generated_source.append(" ");
   ir->val->accept(this);
   generated_source.append(")");
}


void IR_TO_IR::visit(ir_dereference_variable *ir)
{
   ir_variable *var = ir->variable_referenced();
   generated_source.append("(var_ref %s) ", unique_name(var));
}


void IR_TO_IR::visit(ir_dereference_array *ir)
{
   generated_source.append("(array_ref ");
   ir->array->accept(this);
   ir->array_index->accept(this);
   generated_source.append(") ");
}


void IR_TO_IR::visit(ir_dereference_record *ir)
{
   generated_source.append("(record_ref ");
   ir->record->accept(this);

   const char *field_name =
      ir->record->type->fields.structure[ir->field_idx].name;
   generated_source.append(" %s) ", field_name);
}


void IR_TO_IR::visit(ir_assignment *ir)
{
   generated_source.append("(assign ");

   //if (ir->condition)
   //   ir->condition->accept(this);

   char mask[5];
   unsigned j = 0;

   for (unsigned i = 0; i < 4; i++) {
      if ((ir->write_mask & (1 << i)) != 0) {
	 mask[j] = "xyzw"[i];
	 j++;
      }
   }
   mask[j] = '\0';

   generated_source.append(" (%s) ", mask);

   ir->lhs->accept(this);

   generated_source.append(" ");

   ir->rhs->accept(this);
   generated_source.append(") ");
}


void IR_TO_IR::visit(ir_constant *ir)
{
   generated_source.append("(constant ");
   print_type(generated_source, ir->type);
   generated_source.append(" (");

   if (ir->type->is_array()) {
      for (unsigned i = 0; i < ir->type->length; i++)
	 ir->get_array_element(i)->accept(this);
   } else if (ir->type->is_struct()) {
      for (unsigned i = 0; i < ir->type->length; i++) {
	 generated_source.append("(%s ", ir->type->fields.structure[i].name);
         ir->get_record_field(i)->accept(this);
	 generated_source.append(")");
      }
   } else {
      for (unsigned i = 0; i < ir->type->components(); i++) {
	 if (i != 0)
	    generated_source.append(" ");
	 switch (ir->type->base_type) {
	 case GLSL_TYPE_UINT:  generated_source.append("%u", ir->value.u[i]); break;
	 case GLSL_TYPE_INT:   generated_source.append("%d", ir->value.i[i]); break;
	 case GLSL_TYPE_FLOAT:
            if (ir->value.f[i] == 0.0f)
               /* 0.0 == -0.0, so print with %f to get the proper sign. */
               generated_source.append("%f", ir->value.f[i]);
            else if (fabs(ir->value.f[i]) < 0.000001f)
               generated_source.append("%a", ir->value.f[i]);
            else if (fabs(ir->value.f[i]) > 1000000.0f)
               generated_source.append("%e", ir->value.f[i]);
            else
               generated_source.append("%f", ir->value.f[i]);
            break;
	 case GLSL_TYPE_SAMPLER:
	 case GLSL_TYPE_IMAGE:
	 case GLSL_TYPE_UINT64:
            generated_source.append("%" PRIu64, ir->value.u64[i]);
            break;
	 case GLSL_TYPE_INT64: generated_source.append("%" PRIi64, ir->value.i64[i]); break;
	 case GLSL_TYPE_BOOL:  generated_source.append("%d", ir->value.b[i]); break;
	 case GLSL_TYPE_DOUBLE:
            if (ir->value.d[i] == 0.0)
               /* 0.0 == -0.0, so print with %f to get the proper sign. */
               generated_source.append("%.1f", ir->value.d[i]);
            else if (fabs(ir->value.d[i]) < 0.000001)
               generated_source.append("%a", ir->value.d[i]);
            else if (fabs(ir->value.d[i]) > 1000000.0)
               generated_source.append("%e", ir->value.d[i]);
            else
               generated_source.append("%f", ir->value.d[i]);
            break;
	 default:
            unreachable("Invalid constant type");
	 }
      }
   }
   generated_source.append(")) ");
}


void
IR_TO_IR::visit(ir_call *ir)
{
   generated_source.append("(call %s ", ir->callee_name());
   if (ir->return_deref)
      ir->return_deref->accept(this);
   generated_source.append(" (");
   foreach_in_list(ir_rvalue, param, &ir->actual_parameters) {
      param->accept(this);
   }
   generated_source.append("))\n");
}


void
IR_TO_IR::visit(ir_return *ir)
{
   generated_source.append("(return");

   ir_rvalue *const value = ir->get_value();
   if (value) {
      generated_source.append(" ");
      value->accept(this);
   }

   generated_source.append(")");
}


void
IR_TO_IR::visit(ir_discard *ir)
{
   generated_source.append("(discard ");

   if (ir->condition != NULL) {
      generated_source.append(" ");
      ir->condition->accept(this);
   }

   generated_source.append(")");
}


void
IR_TO_IR::visit(ir_demote *ir)
{
   generated_source.append("(demote)");
}


void
IR_TO_IR::visit(ir_if *ir)
{
   generated_source.append("(if ");
   ir->condition->accept(this);

   generated_source.append("(\n");
   indentation++;

   foreach_in_list(ir_instruction, inst, &ir->then_instructions) {
      indent();
      inst->accept(this);
      generated_source.append("\n");
   }

   indentation--;
   indent();
   generated_source.append(")\n");

   indent();
   if (!ir->else_instructions.is_empty()) {
      generated_source.append("(\n");
      indentation++;

      foreach_in_list(ir_instruction, inst, &ir->else_instructions) {
	 indent();
	 inst->accept(this);
	 generated_source.append("\n");
      }
      indentation--;
      indent();
      generated_source.append("))\n");
   } else {
      generated_source.append("())\n");
   }
}


void
IR_TO_IR::visit(ir_loop *ir)
{
   generated_source.append("(loop (\n");
   indentation++;

   foreach_in_list(ir_instruction, inst, &ir->body_instructions) {
      indent();
      inst->accept(this);
      generated_source.append("\n");
   }
   indentation--;
   indent();
   generated_source.append("))\n");
}


void
IR_TO_IR::visit(ir_loop_jump *ir)
{
   generated_source.append("%s", ir->is_break() ? "break" : "continue");
}

void
IR_TO_IR::visit(ir_emit_vertex *ir)
{
   generated_source.append("(emit-vertex ");
   ir->stream->accept(this);
   generated_source.append(")\n");
}

void
IR_TO_IR::visit(ir_end_primitive *ir)
{
   generated_source.append("(end-primitive ");
   ir->stream->accept(this);
   generated_source.append(")\n");
}

void
IR_TO_IR::visit(ir_barrier *)
{
   generated_source.append("(barrier)\n");
}
