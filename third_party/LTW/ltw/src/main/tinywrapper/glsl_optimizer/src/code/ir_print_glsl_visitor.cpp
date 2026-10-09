#include <inttypes.h>
#include "ir_print_glsl_visitor.h"
#include "../compiler/glsl/ir_visitor.h"
#include "../compiler/glsl_types.h"
#include "../compiler/glsl/loop_analysis.h"
#include "../compiler/glsl_types.h"
#include "../compiler/glsl/glsl_parser_extras.h"
#include "../util/macros.h"
#include "../util/hash_table.h"
#include "../util/u_string.h"
#include "../compiler/glsl/ast.h"

const char* const precision[] = { "", "highp ", "mediump ", "lowp " };

struct ga_entry : public exec_node
{
	ga_entry(ir_instruction* ir)
	{
		assert(ir);
		this->ir = ir;
	}
	ir_instruction* ir;
};

IR_TO_GLSL::global_print_tracker::global_print_tracker()
{
	mem_ctx = ralloc_context(0);
	var_counter = 0;
	var_hash = _mesa_pointer_hash_table_create(NULL);
	main_function_done = false;
}

IR_TO_GLSL::global_print_tracker::~global_print_tracker()
{
	_mesa_hash_table_destroy(var_hash, NULL);
	ralloc_free(mem_ctx);
}

static const int tex_sampler_type_count = 7;
// [glsl_sampler_dim]
static const char* tex_sampler_dim_name[tex_sampler_type_count] = {
	"1D", "2D", "3D", "Cube", "Rect", "Buf", "External",
};
static int tex_sampler_dim_size[tex_sampler_type_count] = {
	1, 2, 3, 3, 2, 2, 2,
};

static void print_texlod_workarounds(
	int usage_bitfield,
	int usage_proj_bitfield,
	sbuffer& str)
{
	static const char* precStrings[3] = { "lowp", "mediump", "highp" };
	static const char* precNameStrings[3] = { "low_", "medium_", "high_" };
	// Print out the texlod workarounds
	for (int prec = 0; prec < 3; prec++)
	{
		const char* precString = precStrings[prec];
		const char* precName = precNameStrings[prec];

		for (int dim = 0; dim < tex_sampler_type_count; dim++)
		{
			int mask = 1 << (dim + (prec * 8));
			if (usage_bitfield & mask)
			{
				str.append("%s vec4 impl_%stexture%sLodEXT(%s sampler%s sampler, highp vec%d coord, mediump float lod)\n", precString, precName, tex_sampler_dim_name[dim], precString, tex_sampler_dim_name[dim], tex_sampler_dim_size[dim]);
				str.append("{\n");
				str.append("#if defined(GL_EXT_shader_texture_lod)\n");
				str.append("\treturn texture%sLodEXT(sampler, coord, lod);\n", tex_sampler_dim_name[dim]);
				str.append("#else\n");
				str.append("\treturn texture%s(sampler, coord, lod);\n", tex_sampler_dim_name[dim]);
				str.append("#endif\n");
				str.append("}\n\n");
			}
			if (usage_proj_bitfield & mask)
			{
				// 2D projected read also has a vec4 UV variant
				if (dim == GLSL_SAMPLER_DIM_2D)
				{
					str.append("%s vec4 impl_%stexture2DProjLodEXT(%s sampler2D sampler, highp vec4 coord, mediump float lod)\n", precString, precName, precString);
					str.append("{\n");
					str.append("#if defined(GL_EXT_shader_texture_lod)\n");
					str.append("\treturn texture%sProjLodEXT(sampler, coord, lod);\n", tex_sampler_dim_name[dim]);
					str.append("#else\n");
					str.append("\treturn texture%sProj(sampler, coord, lod);\n", tex_sampler_dim_name[dim]);
					str.append("#endif\n");
					str.append("}\n\n");
				}
				str.append("%s vec4 impl_%stexture%sProjLodEXT(%s sampler%s sampler, highp vec%d coord, mediump float lod)\n", precString, precName, tex_sampler_dim_name[dim], precString, tex_sampler_dim_name[dim], tex_sampler_dim_size[dim] + 1);
				str.append("{\n");
				str.append("#if defined(GL_EXT_shader_texture_lod)\n");
				str.append("\treturn texture%sProjLodEXT(sampler, coord, lod);\n", tex_sampler_dim_name[dim]);
				str.append("#else\n");
				str.append("\treturn texture%sProj(sampler, coord, lod);\n", tex_sampler_dim_name[dim]);
				str.append("#endif\n");
				str.append("}\n\n");
			}
		}
	}
}

void IR_TO_GLSL::print_type(sbuffer& str, const glsl_type* t, bool arraySize)
{
	if (t->is_array())
	{
		print_type(str, t->fields.array, true);
		if (arraySize)
			str.append("[%u]", t->length);
	}
	else if ((t->is_struct()) && !is_gl_identifier(t->name))
	{
		str.append("%s", t->name, (void*)t);
	}
	else
	{
		str.append("%s", t->name);
	}
}

void IR_TO_GLSL::print_type_post(sbuffer& str, const glsl_type* t, bool arraySize)
{
	if (t->base_type == GLSL_TYPE_ARRAY)
	{
		if (!arraySize)
			str.append("[%u]", t->length);
	}
}

const char* str_primtype(GLenum type, bool out) {
    if(!out) switch (type) {
        case GL_POINTS: return "points";
        case GL_LINE_STRIP:
        case GL_LINES: return "lines";
        case GL_LINE_STRIP_ADJACENCY:
        case GL_LINES_ADJACENCY: return "lines_adjacency";
        case GL_TRIANGLE_STRIP:
        case GL_TRIANGLE_FAN:
        case GL_TRIANGLES: return "triangles";
        case GL_TRIANGLE_STRIP_ADJACENCY:
        case GL_TRIANGLES_ADJACENCY: return "triangles_adjacency";
        default:
            printf("ir_print_glsl_visitor: Unhandled geom shader input primtype: %x\n", type);
            return "unknown";
    }
    else switch(type) {
        case GL_POINTS: return "points";
        case GL_LINE_STRIP: return "line_strip";
        case GL_TRIANGLE_STRIP: return "triangle_strip";
        default:
            printf("ir_print_glsl_visitor: Unhandled geom shader output primtype: %x\n", type);
            return "unknown";
    }
}

bool should_print_layout(const ast_type_qualifier* qual) {
    return qual->flags.q.prim_type ||
            qual->flags.q.max_vertices;
}

void print_layout(sbuffer& str, struct _mesa_glsl_parse_state* state, ast_type_qualifier* qual, bool out) {
    bool first = true;
#define I_LAYOUT_QUALIFIER(cmp_op, block) \
    if(cmp_op) {        \
        if(!first) str.append(","); \
        else first = false;         \
        block                       \
    }
#define LAYOUT_QUALIFIER(name, block) I_LAYOUT_QUALIFIER(qual->flags.q.name, block)
    str.append("layout(");
    LAYOUT_QUALIFIER(prim_type,{
        str.append("%s", str_primtype(qual->prim_type, out));
    })
    LAYOUT_QUALIFIER(max_vertices,{
        unsigned qual_max_vertices = !0;
        qual->max_vertices->process_qualifier_constant(state, "max_vertices",
                                   &qual_max_vertices, true);
        str.append("max_vertices=%u", qual_max_vertices);
    })
    if(!out && state->cs_input_local_size_specified) {
        auto& local_size = state->cs_input_local_size;
        I_LAYOUT_QUALIFIER(local_size[0] != 0, str.append("local_size_x=%u",local_size[0]);)
        I_LAYOUT_QUALIFIER(local_size[1] != 0, str.append("local_size_y=%u",local_size[1]);)
        I_LAYOUT_QUALIFIER(local_size[2] != 0, str.append("local_size_z=%u",local_size[2]);)
    }
    str.append(") ");
}

void nan_check_warn() {
    static bool warned = false;
    if(warned) return;
    printf("LTW shader optimizer will emit NaN checks for fragment outputs. This may lead to loss of performance.\n");
    warned = true;
}

void print_nan_check_funcs(sbuffer& str) {
    nan_check_warn();
    const char* nan_check_func =
            "%1$s _ltw_removenan(%1$s colorVal) {\n"
            "   return mix(colorVal, %1$s(%2$s), isnan(colorVal));\n"
            "}\n";
    str.append(nan_check_func, "float", "0.0");
    str.append(nan_check_func, "vec2", "0.0,0.0");
    str.append(nan_check_func, "vec3", "0.0,0.0,0.0");
    str.append(nan_check_func, "vec4", "0.0,0.0,0.0,1.0");
}

// DANGER, the function allocates a new string
// DO NOT FORGET TO FREE IT
char * IR_TO_GLSL::Convert(
	exec_list* instructions,
	struct _mesa_glsl_parse_state* state)
{
	sbuffer res;
    bool shader_nan_check = false;

	if (state)
	{
        bool print_precision = false;
        if(state->es_shader && state->language_version >= 300){
            res.append("#version %i es\n", state->language_version);
            print_precision = true;
        }
        else
		    res.append("#version %i\n", state->language_version);


		if (state->ARB_shader_texture_lod_enable)
			res.append("#extension GL_ARB_shader_texture_lod : enable\n");
		if (state->ARB_draw_instanced_enable)
			res.append("#extension GL_ARB_draw_instanced : enable\n");
		//if (state->EXT_gpu_shader4_enable)
		//	res.append("#extension GL_EXT_gpu_shader4 : enable\n");
		//if (state->EXT_shader_texture_lod_enable)
		//	str.append("#extension GL_EXT_shader_texture_lod : enable\n");
		if (state->OES_standard_derivatives_enable)
			res.append("#extension GL_OES_standard_derivatives : enable\n");
		//if (state->EXT_shadow_samplers_enable)
		//	str.append("#extension GL_EXT_shadow_samplers : enable\n");
		if (state->EXT_frag_depth_enable)
			res.append("#extension GL_EXT_frag_depth : enable\n");
		if (state->es_shader && state->language_version < 300)
		{
			if (state->EXT_draw_buffers_enable)
				res.append("#extension GL_EXT_draw_buffers : enable\n");
			//	if (state->EXT_draw_instanced_enable)
			//		str.append("#extension GL_EXT_draw_instanced : enable\n");
		}
		if (state->EXT_shader_framebuffer_fetch_enable)
			res.append("#extension GL_EXT_shader_framebuffer_fetch : enable\n");
		if (state->ARB_shader_bit_encoding_enable)
			res.append("#extension GL_ARB_shader_bit_encoding : enable\n");
		if (state->EXT_texture_array_enable)
			res.append("#extension GL_EXT_texture_array : enable\n");

        // Search for internal GL variables and enable extensions based on them
        bool uses_buffer_sampler = false;
        bool sampler2d_highp = false;
        foreach_in_list(ir_instruction, ir, instructions)
        {
            // Skip non-variables
            if(ir->ir_type != ir_type_variable) continue;
            auto* var = (ir_variable*)ir;

            const char* type = var->type->name;
            if(type != nullptr) {
                // Check for buffer texture samplers. Need to enable (and/or set precision) for them
                if(!uses_buffer_sampler && (strstr(type, "samplerBuffer") != nullptr || strstr(type, "imageBuffer") != nullptr)) {
                    uses_buffer_sampler = true;
                    if(state->es_shader && state->language_version < 320) {
                        res.append("#extension GL_EXT_texture_buffer : enable\n");
                    }
                }
                // Check if shampler2DShadow is used in this shader. If yes, we need to explicitly enable high
                // precision for all 2D samplers, to make shadows on Mali look good.
                if(!sampler2d_highp && (strstr(type, "sampler2DShadow") != nullptr)) {
                    sampler2d_highp = true;
                }
            }
            // Check for interpolation type. Need to enable the noperspective interpolation extension
            // if used.
            if(var->data.interpolation == glsl_interp_mode::INTERP_MODE_NOPERSPECTIVE && !state->NV_shader_noperspective_interpolation_enable) {
                state->NV_shader_noperspective_interpolation_enable = true;
                res.append("#extension GL_NV_shader_noperspective_interpolation : enable\n");
            }
            // Skip non-internal variables
            if(strstr(var->name, "gl_") != var->name) continue;
            const char* name = var->name;
            // If gl_ClipDistance or gl_CullDistance is in use, enable the corresponding GLES extension
            if((strstr(name, "gl_ClipDistance") != nullptr || strstr(name, "gl_CullDistance") != nullptr) && !state->EXT_clip_cull_distance_enable) {
                state->EXT_clip_cull_distance_enable = true;
                res.append("#extension GL_EXT_clip_cull_distance : enable\n");
            }

        }

        if(print_precision) {
            const char* sampler2d_precision = "lowp";
            if(sampler2d_highp) sampler2d_precision = "highp";
            res.append("precision %1$s float;\n"
                       "precision %1$s int;\n"
                       "precision %2$s sampler2D;\n", "highp", sampler2d_precision);
            res.append("precision %1$s sampler3D;\n"
                       "precision %1$s samplerCubeShadow;\n"
                       "precision %2$s sampler2DShadow;\n"
                       "precision %2$s sampler2DArray;\n"
                       "precision %2$s sampler2DArrayShadow;\n"
                       "precision %1$s isampler2D;\n"
                       "precision %1$s isampler3D;\n"
                       "precision %1$s isamplerCube;\n"
                       "precision %1$s isampler2DArray;\n"
                       "precision %1$s usampler2D;\n"
                       "precision %1$s usampler3D;\n"
                       "precision %1$s usamplerCube;\n"
                       "precision %1$s usampler2DArray;\n", "lowp", sampler2d_precision);
            if(uses_buffer_sampler) {
                res.append("precision %1$s samplerBuffer;\n"
                           "precision %1$s isamplerBuffer;\n"
                           "precision %1$s usamplerBuffer;\n"
                           "precision %1$s imageBuffer;\n"
                           "precision %1$s iimageBuffer;\n"
                           "precision %1$s uimageBuffer;\n", "lowp");
            }
        }

		for (unsigned i = 0; i < state->num_user_structures; i++)
		{
			const glsl_type* const s = state->user_structures[i];
			res.append("struct %s {\n",
				s->name);
			for (unsigned j = 0; j < s->length; j++)
			{
                const glsl_type* type = (&s->fields.structure[j])->type;
				res.append("\t");
				print_type(res, type, false);
				res.append(" %s", (&s->fields.structure[j])->name);
                if(type->is_array()) {
                    res.append("[%i]",type->length);
                }
                res.append(";\n");
			}
			res.append("};\n");
		}
        if(should_print_layout(state->in_qualifier) || state->cs_input_local_size_specified) {
            print_layout(res, state, state->in_qualifier, false);
            res.append("in;\n");
        }
        if(should_print_layout(state->out_qualifier)) {
            print_layout(res, state, state->out_qualifier, true);
            res.append("out;\n");
        }
        const char* nan_check_env = getenv("LTW_SHADERCONV_CHECKNAN");
        bool emit_nan_check = nan_check_env != nullptr && *nan_check_env == '1';
        if(emit_nan_check && state->stage == MESA_SHADER_FRAGMENT) {
            print_nan_check_funcs(res);
            shader_nan_check = true;
        }
	}

	global_print_tracker global;
    global.enable_nan_check = shader_nan_check;
	int uses_texlod_impl = 0;
	int uses_texlodproj_impl = 0;
	loop_state* ls = analyze_loop_variables(instructions);
	if (ls)
	{
		foreach_in_list(ir_instruction, ir, instructions)
		{
			if (ir->ir_type == ir_type_variable)
			{
				ir_variable* var = static_cast<ir_variable*>(ir);
				if ((strstr(var->name, "gl_") == var->name)
					&& !var->data.invariant)
					continue;
			}

			ir_instruction* deconsted = const_cast<ir_instruction*>(ir);

			IR_TO_GLSL v(res, &global, state);
			v.loopstate = ls;

			ir->accept(&v);
			if (ir->ir_type != ir_type_function && !v.skipped_this_ir)
				res.append(";\n"); // uniforms

			uses_texlod_impl |= v.uses_texlod_impl;
			uses_texlodproj_impl |= v.uses_texlodproj_impl;
		}

		delete ls;
	}

	print_texlod_workarounds(uses_texlod_impl, uses_texlodproj_impl, res);
    res.append("\n\n");



    // DANGER, A NEW STRING IS ALLOCATED !
    // YOUR PROGRAM HAS TO FREE IT !
	return res.c_str_take_ownership();
}

IR_TO_GLSL::IR_TO_GLSL(
	sbuffer& str,
	global_print_tracker* vGlobals,
	const _mesa_glsl_parse_state* vState)
	: generated_source(str), global(vGlobals), state(vState)
{
	printable_names = _mesa_pointer_hash_table_create(NULL);
	symbols = _mesa_symbol_table_ctor();
	mem_ctx = ralloc_context(NULL);
}

IR_TO_GLSL::~IR_TO_GLSL()
{
	_mesa_hash_table_destroy(printable_names, NULL);
	_mesa_symbol_table_dtor(symbols);
	ralloc_free(mem_ctx);
}

void
IR_TO_GLSL::indent(void)
{
	if (previous_skipped)
		return;
	previous_skipped = false;
	for (int i = 0; i < indentation; i++)
		generated_source.append("  ");
}

void
IR_TO_GLSL::end_statement_line()
{
	if (!skipped_this_ir)
		generated_source.append(";\n");
	previous_skipped = skipped_this_ir;
	skipped_this_ir = false;
}

void
IR_TO_GLSL::newline_indent()
{
	if (expression_depth % 8 == 0)
	{
		++indentation;
		generated_source.append("\n");
		indent();
	}
}

void
IR_TO_GLSL::newline_deindent()
{
	if (expression_depth % 8 == 0)
	{
		--indentation;
		generated_source.append("\n");
		indent();
	}
}

void
IR_TO_GLSL::print_var_name(ir_variable* v)
{
	hash_entry* entry = _mesa_hash_table_search(global->var_hash, v);
    long id;


	if (entry)
	{
        id = (long)entry->data;
        if (!id && v->data.mode == ir_var_temporary)
        {
            id = ++global->var_counter;
            _mesa_hash_table_insert(global->var_hash, v, (void*)id);
        }
		if (id)
		{
			if (v->data.mode == ir_var_temporary)
				generated_source.append("tmpvar_%d", (int)id);
			else
				generated_source.append("%sB_%d", v->name, (int)id);
		}
		else
		{
			generated_source.append("%s", v->name);
		}
	}
	else
	{
        if (v->data.mode == ir_var_temporary) {
            id = ++global->var_counter;
            _mesa_hash_table_insert(global->var_hash, v, (void*)id);
            generated_source.append("tmpvar_%d", (int)id);
        }
        else
		    generated_source.append("%s", v->name);
	}
}

const char*
IR_TO_GLSL::unique_name(ir_variable* v)
{
	/* var->name can be NULL in function prototypes when a type is given for a
	 * parameter but no name is given.  In that case, just return an empty
	 * string.  Don't worry about tracking the generated name in the printable
	 * names hash because this is the only scope where it can ever appear.
	 */
	if (v->name == NULL)
	{
		static unsigned arg = 1;
		return ralloc_asprintf(this->mem_ctx, "parameter@%u", arg++);
	}

	/* Do we already have a name for this variable? */
	struct hash_entry* entry =
		_mesa_hash_table_search(this->printable_names, v);

	if (entry != NULL)
	{
		return (const char*)entry->data;
	}

	const char* name = NULL;

	/* If there's no conflict, just use the original name */
	if (_mesa_symbol_table_find_symbol(this->symbols, v->name) == NULL)
	{
		name = v->name;
	}
	else
	{
		static unsigned i = 1;
		name = ralloc_asprintf(this->mem_ctx, "%s@%u", v->name, ++i);
	}

	_mesa_hash_table_insert(this->printable_names, v, (void*)name);
	_mesa_symbol_table_add_symbol(this->symbols, name, v);

	return name;
}

void
IR_TO_GLSL::visit(ir_rvalue*)
{
	generated_source.append("error");
}

static const char* image_load_store_format(pipe_format format) {
    switch (format) {
        case PIPE_FORMAT_R32G32B32A32_FLOAT: return "rgba32f";
        case PIPE_FORMAT_R16G16B16A16_FLOAT: return "rgba16f";
        case PIPE_FORMAT_R32G32_FLOAT: return "rg32f";
        case PIPE_FORMAT_R16G16_FLOAT: return "rg16f";
        case PIPE_FORMAT_R11G11B10_FLOAT: return "r11f_g11f_b10f";
        case PIPE_FORMAT_R32_FLOAT: return "r32f";
        case PIPE_FORMAT_R16_FLOAT: return "r16f";
        case PIPE_FORMAT_R16G16B16A16_SSCALED: return "rgba16";
        case PIPE_FORMAT_R10G10B10A2_SSCALED: return "rgb10_a2";
        case PIPE_FORMAT_R8G8B8A8_SSCALED: return "rgba8";
        case PIPE_FORMAT_R16G16_SSCALED: return "rg16";
        case PIPE_FORMAT_R8G8_SSCALED: return "rg8";
        case PIPE_FORMAT_R16_SSCALED: return "r16";
        case PIPE_FORMAT_R8_SSCALED: return "r8";
        case PIPE_FORMAT_R16G16B16A16_SNORM: return "rgba16_snorm";
        case PIPE_FORMAT_R8G8B8A8_SNORM: return "rgba8_snorm";
        case PIPE_FORMAT_R16G16_SNORM: return "rg16_snorm";
        case PIPE_FORMAT_R8G8_SNORM: return "rg8_snorm";
        case PIPE_FORMAT_R16_SNORM: return "r16_snorm";
        case PIPE_FORMAT_R8_SNORM: return "r8_snorm";
        case PIPE_FORMAT_R32G32B32A32_SINT: return "rgba32i";
        case PIPE_FORMAT_R16G16B16A16_SINT: return "rgba16i";
        case PIPE_FORMAT_R8G8B8A8_SINT: return "rgba8i";
        case PIPE_FORMAT_R32G32_SINT: return "rg32i";
        case PIPE_FORMAT_R16G16_SINT: return "rg16i";
        case PIPE_FORMAT_R8G8_SINT: return "rg8i";
        case PIPE_FORMAT_R32_SINT: return "r32i";
        case PIPE_FORMAT_R16_SINT: return "r16i";
        case PIPE_FORMAT_R8_SINT: return "r8i";
        case PIPE_FORMAT_R32G32B32A32_UINT: return "rgba32ui";
        case PIPE_FORMAT_R16G16B16A16_UINT: return "rgba16ui";
        case PIPE_FORMAT_R10G10B10A2_UINT: return "rgb10_a2ui";
        case PIPE_FORMAT_R8G8B8A8_UINT: return "rgba8ui";
        case PIPE_FORMAT_R32G32_UINT: return "rg32ui";
        case PIPE_FORMAT_R16G16_UINT: return "rg16ui";
        case PIPE_FORMAT_R8G8_UINT: return "rg8ui";
        case PIPE_FORMAT_R32_UINT: return "r32ui";
        case PIPE_FORMAT_R16_UINT: return "r16ui";
        case PIPE_FORMAT_R8_UINT: return "r8ui";
        default:
            printf("ir_print_glsl_visitor: Unknown image load store format: %u\n", format);
            return "unknown";
    }
}
void IR_TO_GLSL::print_generic_layout_block(ir_variable* ir) {
    auto &data = ir->data;
    // Maybe pick better formats? Not sure yet
    if (data.image_format == 0 && ir->type->is_image()) {
        switch(ir->type->sampled_type){
            case GLSL_TYPE_FLOAT: data.image_format = PIPE_FORMAT_R32_FLOAT; break;
            case GLSL_TYPE_INT: data.image_format = PIPE_FORMAT_R32_SINT; break;
            case GLSL_TYPE_UINT: data.image_format = PIPE_FORMAT_R32_UINT; break;
            default: return;
        }
    }
    bool should_print = data.binding || data.explicit_location || data.explicit_component || data.image_format || ir->type->is_image();
    if(!should_print) return;
    bool packed_stream = data.stream & (1u << 31);
    if(packed_stream) {
        generated_source.append("// GS packed stream: %u,%u,%u,%u",
                                ir->data.stream & 3, (ir->data.stream >> 2) & 3,
                                (ir->data.stream >> 4) & 3, (ir->data.stream >> 6) & 3);
    }

    bool first = true;
#define SEPARATE { if(!first) { generated_source.append(","); } else { first = false; } }
    generated_source.append("layout(");
    if(data.binding) {
        SEPARATE generated_source.append("binding=%i", data.binding);
    }
    if(data.explicit_location) {
        const int binding_base = (this->state->stage == MESA_SHADER_VERTEX ? (int)VERT_ATTRIB_GENERIC0 : (int)FRAG_RESULT_DATA0);
        const int location = data.location - binding_base;
        SEPARATE generated_source.append("location=%i", location);
    }
    if(data.explicit_component) {
        SEPARATE generated_source.append("component=%u", data.location_frac);
    }
    if(data.stream) {
        SEPARATE if(packed_stream) {
            generated_source.append("LTW_PACKED_STREAM_REPORT_TO_DEVS");
        }else {
            generated_source.append("stream=%u", data.stream);
        }

    }
    if(data.image_format) {
        SEPARATE generated_source.append(image_load_store_format(data.image_format));
    }
    generated_source.append(") ");
#undef SEPARATE
}

void
IR_TO_GLSL::visit(ir_variable* ir)
{
   // HACK to handle unoptimized shaders
   if(ir->type->is_void())
      return;

	if ( ir->is_in_uniform_block()) // only supporting uniform blocks for now, might add SSBOs later
	{
		visit_uniform_block( ir );
		return;
	}

    if(!ir->data.explicit_location && ir->data.mode == ir_var_shader_out && this->state->stage == MESA_SHADER_FRAGMENT) {
        generated_source.append("/* LTW INSERT LOCATION ");
        print_var_name(ir);
        generated_source.append(" LTW */");
    } else {
        print_generic_layout_block(ir);
    }

	int decormode = this->mode;
	// GLSL 1.30 and up use "in" and "out" for everything
	if (this->state->language_version >= 130)
		decormode = 0;
	else {
		switch (this->state->stage) {
			case MESA_SHADER_FRAGMENT:
				decormode = 2;
				break;
			case MESA_SHADER_VERTEX:
				decormode = 1;
				break;
		}
	}

	// give an id to any variable defined in a
	// function that is not an uniform
	if (this->mode == 0 && ir->data.mode != ir_var_uniform)
	{
		hash_entry* entry = _mesa_hash_table_search(global->var_hash, ir);
		if (entry == 0)
		{
			long id = ++global->var_counter;
			_mesa_hash_table_insert(global->var_hash, ir, (void*)id);
		}
	}

	// if this is a loop induction variable, do not print it
	// (will be printed inside loop body)
	if (!inside_loop_body)
	{
		ir_loop* lo = ir->as_loop();
		if (lo)
		{
			loop_variable_state* inductor_state = loopstate->get(lo);
			if (inductor_state && inductor_state->induction_variables.length() == 1 &&
				can_emit_canonical_for(inductor_state))
			{
				skipped_this_ir = true;
				return;
			}
		}
	}

	const char* const cent = (ir->data.centroid) ? "centroid " : "";
	const char* const samp = (ir->data.sample) ? "sample " : "";
	const char* const patc = (ir->data.patch) ? "patch " : "";
	const char* const inv = (ir->data.invariant) ? "invariant " : "";
	const char* const explicit_inv = (ir->data.explicit_invariant) ? "explicit_invariant " : "";
	const char* const prec = (ir->data.precise) ? "precise " : "";
	const char* const bindless = (ir->data.bindless) ? "bindless " : "";
	const char* const bound = (ir->data.bound) ? "bound " : "";
	const char* const memory_read_only = (ir->data.memory_read_only) ? "readonly " : "";
	const char* const memory_write_only = (ir->data.memory_write_only) ? "writeonly " : "";
	const char* const memory_coherent = (ir->data.memory_coherent) ? "coherent " : "";
	const char* const memory_volatile = (ir->data.memory_volatile) ? "volatile " : "";
	const char* const memory_restrict = (ir->data.memory_restrict) ? "restrict " : "";
	const char* const mode[3][ir_var_mode_count] =
	{
		{ "", "uniform ", "shader_storage", "shared ", "in ",        "out ",     "in ", "out ", "inout ", "const_in ", "sys ", "" },
		{ "", "uniform ", "shader_storage", "shared ", "attribute ", "varying ", "in ", "out ", "inout ", "const_in ", "sys ", "" },
		{ "", "uniform ", "shader_storage", "shared ", "varying ",   "out ",     "in ", "out ", "inout ", "const_in ", "sys ", "" }
	};
	const char* const interp[] = { "", "smooth ", "flat ", "noperspective ", "EXPLICIT ", "COLOR " };
	STATIC_ASSERT(ARRAY_SIZE(interp) == INTERP_MODE_COUNT);

    // Qualcomm blob ignores image precision, but who cares about it?
    if(ir->type->is_image() && ir->data.precision == GLSL_PRECISION_NONE)
        ir->data.precision = GLSL_PRECISION_MEDIUM; // maybe try highp/lowp...

	// keep invariant declaration for builtin variables
	if (strstr(ir->name, "gl_") == ir->name)
	{
		generated_source.append("%s", inv);
		print_var_name(ir);
		return;
	}

	generated_source.append("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",cent, bindless, bound,memory_read_only, memory_write_only,
		memory_coherent, memory_volatile, memory_restrict,
		samp, patc, inv, explicit_inv, prec, interp[ir->data.interpolation], mode[decormode][ir->data.mode],
		precision[ir->data.precision]);

	print_type(generated_source, ir->type, false);
	generated_source.append(" ");
	print_var_name(ir);
	print_type_post(generated_source, ir->type, false);

	if (ir->constant_value &&
		ir->data.mode != ir_var_shader_in &&
		ir->data.mode != ir_var_shader_out &&
		//ir->data.mode != ir_var_shader_inout &&
		ir->data.mode != ir_var_function_in &&
		ir->data.mode != ir_var_function_out &&
		ir->data.mode != ir_var_function_inout)
	{
		generated_source.append(" = ");
		visit(ir->constant_value);
	}
}

void
IR_TO_GLSL::visit(ir_function_signature* ir)
{
	_mesa_symbol_table_push_scope(symbols);

	print_type(generated_source, ir->return_type, true);
	generated_source.append(" %s(", ir->function_name());

	if (!ir->parameters.is_empty())
	{
		previous_skipped = false;
		bool first = true;
		foreach_in_list(ir_variable, inst, &ir->parameters)
		{
			if (!first)
				generated_source.append(", ");
			indent();
			inst->accept(this);
			first = false;
		}
		indent();
	}

	if (ir->body.is_empty() && strcmp(ir->function()->name, "main") != 0)
	{
		generated_source.append(");\n");
		return;
	}

	generated_source.append(")\n");

	indent();
	generated_source.append("{\n");
	indentation++;
	previous_skipped = false;

	// insert postponed global assigments
	if (strcmp(ir->function()->name, "main") == 0)
	{
		assert(!global->main_function_done);
		global->main_function_done = true;
		foreach_in_list(ga_entry, node, &global->global_assignements)
		{
			ir_instruction* as = node->ir;
			as->accept(this);
			generated_source.append(";\n");
		}
	}

	foreach_in_list(ir_instruction, inst, &ir->body)
	{
		indent();
		inst->accept(this);
		end_statement_line();
	}

	indentation--;
	indent();
	generated_source.append("}\n");

	_mesa_symbol_table_pop_scope(symbols);
}

void
IR_TO_GLSL::visit(ir_function* ir)
{
	bool found_non_builtin_proto = false;
	foreach_in_list(ir_function_signature, sig, &ir->signatures)
	{
		if (!sig->is_builtin())
			found_non_builtin_proto = true;
	}
	if (!found_non_builtin_proto)
		return;

	int oldMode = this->mode;
	this->mode = 0;

	foreach_in_list(ir_function_signature, sig, &ir->signatures)
	{
		indent();
		sig->accept(this);
		generated_source.append("\n");
	}

	this->mode = oldMode;

	indent();
}

const char* const operator_glsl_strs[] = {
   "~", // bit not
   "!", // logic not
   "-", //neg
   "abs", // abs
   "sign", //sign
   "1.0/", // rcp
   "1.0/sqrt", //rcq
   "sqrt", //sqrt
   "exp", // exp
   "log", // log
   "exp2", //exp2
   "log2", //log2
   "int", //f2i
   "uint", //f2u
   "float", // i2f
   "bool", // f2b
   "float", //b2f
   "ir_unop_b2f16",
   "bool", //i2b
   "int",//"b2i",
   "float",// "u2f",
   "uint",// "i2u",
   "int",// "u2i",
   "float",// "d2f",
   "double",// "f2d",
   "f2f16",
   "f2fmp",
   "f162f",
   "i2i",
   "i2imp",
   "u2u",
   "u2ump",
   "int", //"d2i",
   "double", // "i2d",
   "uint",//  "d2u",
   "double", //"u2d",
   "bool",// "d2b",
   "f162b",
   "intBitsToFloat",
   "floatBitsToInt",
   "uintBitsToFloat",
   "floatBitsToUint",
   "bitcast_u642d",
   "bitcast_i642d",
   "bitcast_d2u64",
   "bitcast_d2i64",
   "i642i",
   "u642i",
   "i642u",
   "u642u",
   "i642b",
   "i642f",
   "u642f",
   "i642d",
   "u642d",
   "i2i64",
   "u2i64",
   "b2i64",
   "f2i64",
   "d2i64",
   "i2u64",
   "u2u64",
   "f2u64",
   "d2u64",
   "u642i64",
   "i642u64",
   "trunc",
   "ceil",
   "floor",
   "fract",
   "roundEven",
   "sin",
   "cos",
   "atan",
   "dFdx",
   "dFdxCoarse",
   "dFdxFine",
   "dFdy",
   "dFdyCoarse",
   "dFdyFine",
   "packSnorm2x16",
   "packSnorm4x8",
   "packUnorm2x16",
   "packUnorm4x8",
   "packHalf2x16",
   "unpackSnorm2x16",
   "unpackSnorm4x8",
   "unpackUnorm2x16",
   "unpackUnorm4x8",
   "unpackHalf2x16",
   "bitfield_reverse",
   "bit_count",
   "find_msb",
   "find_lsb",
   "clz", // clz ?
   "clamp", // "saturate" clamps between 0.0 and 1.0. So we hack the printer to add arguments
   "packDouble2x32",
   "unpackDouble2x32",
   "packSampler2x32",
   "packImage2x32",
   "unpackSampler2x32",
   "unpackImage2x32",
   "frexp_sig",
   "frexp_exp",
   "subroutine_to_int",
   "interpolate_at_centroid",
   "get_buffer_size",
   "ssbo_unsized_array_length",
   "implicitly_sized_array_length",
   "packInt2x32",
   "packUint2x32",
   "unpackInt2x32",
   "unpackUint2x32",
   "+",
   "-",
   "add_sat",
   "sub_sat",
   "abs_sub",
   "avg",
   "avg_round",
   "*",
   "mul_32x16",
   "imul_high_TODO",
   "/",
   "carry_TODO",
   "borrow_TODO",
   "mod",
   "<",
   ">=",
   "==",
   "!=",
   "==", // all_equal_TODO
   "!=", // any_nequal_TODO
   "<<",
   ">>",
   "&",
   "^",
   "|",
   "&&",
   "^^",
   "||",
   "dot",
   "min",
   "max",
   "pow",
   "ubo_load_TODO",
   "ldexp_TODO",
   "vector_extract_TODO",
   "interpolate_at_offse_TODOt",
   "interpolate_at_sample_TODO",
   // NOTE: atan2 operation, replaced with atan(x,y)
   "atan",
   "fma",
   "mix", // lrp ?
   "csel",
   "bitfield_extract_TODO",
   "vector_insert_TODO",
   "bitfield_insert_TODO",
   "vector_TODO",
};

const char* const operator_glsl_enum_strs[] = {
   "bit_not",
   "logic_not",
   "neg",
   "abs",
   "sign",
   "rcp",
   "rsq",
   "sqrt",
   "exp",
   "log",
   "exp2",
   "log2",
   "f2i",
   "f2u",
   "i2f",
   "f2b",
   "b2f",
   "i2b",
   "b2i",
   "u2f",
   "i2u",
   "u2i",
   "d2f",
   "f2d",
   "d2i",
   "i2d",
   "d2u",
   "u2d",
   "d2b",
   "bitcast_i2f",
   "bitcast_f2i",
   "bitcast_u2f",
   "bitcast_f2u",
   "bitcast_u642d",
   "bitcast_i642d",
   "bitcast_d2u64",
   "bitcast_d2i64",
   "i642i",
   "u642i",
   "i642u",
   "u642u",
   "i642b",
   "i642f",
   "u642f",
   "i642d",
   "u642d",
   "i2i64",
   "u2i64",
   "b2i64",
   "f2i64",
   "d2i64",
   "i2u64",
   "u2u64",
   "f2u64",
   "d2u64",
   "u642i64",
   "i642u64",
   "trunc",
   "ceil",
   "floor",
   "fract",
   "round_even",
   "sin",
   "cos",
   "atan",
   "dFdx",
   "dFdx_coarse",
   "dFdx_fine",
   "dFdy",
   "dFdy_coarse",
   "dFdy_fine",
   "pack_snorm_2x16",
   "pack_snorm_4x8",
   "pack_unorm_2x16",
   "pack_unorm_4x8",
   "pack_half_2x16",
   "unpack_snorm_2x16",
   "unpack_snorm_4x8",
   "unpack_unorm_2x16",
   "unpack_unorm_4x8",
   "unpack_half_2x16",
   "bitfield_reverse",
   "bit_count",
   "find_msb",
   "find_lsb",
   "saturate",
   "pack_double_2x32",
   "unpack_double_2x32",
   "pack_sampler_2x32",
   "pack_image_2x32",
   "unpack_sampler_2x32",
   "unpack_image_2x32",
   "frexp_sig",
   "frexp_exp",
   "noise",
   "subroutine_to_int",
   "interpolate_at_centroid",
   "get_buffer_size",
   "ssbo_unsized_array_length",
   "pack_int_2x32",
   "pack_uint_2x32",
   "unpack_int_2x32",
   "unpack_uint_2x32",
   "add",
   "sub",
   "mul",
   "imul_high",
   "div",
   "carry",
   "borrow",
   "mod",
   "less",
   "gequal",
   "equal",
   "nequal",
   "all_equal",
   "any_nequal",
   "lshift",
   "rshift",
   "bit_and",
   "bit_xor",
   "bit_or",
   "logic_and",
   "logic_xor",
   "logic_or",
   "dot",
   "min",
   "max",
   "pow",
   "ubo_load",
   "ldexp",
   "vector_extract",
   "interpolate_at_offset",
   "interpolate_at_sample",
        // NOTE: atan2 operation, replaced with atan(x,y)
   "atan",
   "fma",
   "lrp",
   "csel",
   "bitfield_extract",
   "vector_insert",
   "bitfield_insert",
   "vector",
};


static const char* const operator_vec_glsl_strs[] = {
	"lessThan",
	"greaterThan",
	"lessThanEqual",
	"greaterThanEqual",
	"equal",
	"notEqual",
};

static bool is_binop_func_like(ir_expression_operation op, const glsl_type* type)
{
	if (op == ir_binop_equal ||
		op == ir_binop_nequal ||
		op == ir_binop_mod || op == ir_binop_atan2 ||
		(op >= ir_binop_dot && op <= ir_binop_pow))
		return true;
	if (type->is_vector() && (op >= ir_binop_less && op <= ir_binop_nequal))
	{
		return true;
	}
	return false;
}

void
IR_TO_GLSL::visit(ir_expression* ir)
{
	expression_depth++;
	newline_indent();

	if (ir->num_operands == 1)
	{
		if (ir->operation >= ir_unop_f2i && ir->operation <= ir_unop_u2i) {
			print_type(generated_source, ir->type, true);
			generated_source.append("(");
		}
		else if (ir->operation == ir_unop_rcp) {
			generated_source.append("(1.0/(");
		}
		else {
			generated_source.append("%s(", operator_glsl_strs[ir->operation]);
		}
		if (ir->operands[0])
			ir->operands[0]->accept(this);

      // Saturate is only accepted by some glsl compilers, so transform it into a clamp 0.0 - 1.0
      if(ir->operation == ir_unop_saturate)
         generated_source.append(", 0.0, 1.0");

		generated_source.append(")");
		if (ir->operation == ir_unop_rcp) {
			generated_source.append(")");
		}
	}
	else if (ir->operation == ir_triop_csel)
	{
		generated_source.append("mix(");
		ir->operands[2]->accept(this);
		generated_source.append(", ");
		ir->operands[1]->accept(this);
		auto x = ir->operands[1]->type->vector_elements;
		if (ir->operands[1]->type->vector_elements > 1)
			generated_source.append(", bvec%d(", ir->operands[1]->type->vector_elements);
		else
			generated_source.append(", bool(");
		ir->operands[0]->accept(this);
		generated_source.append("))");
	}
	else if (ir->operation == ir_binop_vector_extract)
	{
		// a[b]

		if (ir->operands[0])
			ir->operands[0]->accept(this);
		generated_source.append("[");
		if (ir->operands[1])
			ir->operands[1]->accept(this);
		generated_source.append("]");
	}
	else if (is_binop_func_like(ir->operation, ir->type))
	{
		if (ir->operation == ir_binop_mod)
		{
			generated_source.append("(");
			print_type(generated_source, ir->type, true);
			generated_source.append("(");
		}
		if (ir->type->is_vector() && (ir->operation >= ir_binop_less && ir->operation <= ir_binop_nequal))
			generated_source.append("%s (", operator_vec_glsl_strs[ir->operation - ir_binop_less]);
		else
		{
			if (ir->operation >= ir_binop_less && ir->operation <= ir_binop_nequal)
			{
				generated_source.append("(");
				if (ir->operands[0])
					ir->operands[0]->accept(this);

				generated_source.append(" %s ", operator_glsl_strs[ir->operation]);

				if (ir->operands[1])
					ir->operands[1]->accept(this);
				generated_source.append(")");
				return;
			}
			generated_source.append("%s (", operator_glsl_strs[ir->operation]);
		}


		if (ir->operands[0]){
            // mod needs floating types for some reason
            if(ir->operation == ir_binop_mod)
                generated_source.append("float(");
            ir->operands[0]->accept(this);
            if(ir->operation == ir_binop_mod)
                generated_source.append(")");
        }

		generated_source.append(", ");
		if (ir->operands[1]) {
            if(ir->operation == ir_binop_mod)
                generated_source.append("float(");
            ir->operands[1]->accept(this);
            if(ir->operation == ir_binop_mod)
                generated_source.append(")");
        }
		generated_source.append(")");
		if (ir->operation == ir_binop_mod)
			generated_source.append("))");
	}
	else if (ir->num_operands == 2)
	{
		generated_source.append("(");
		if (ir->operands[0])
			ir->operands[0]->accept(this);

		generated_source.append(" %s ", operator_glsl_strs[ir->operation]);

		if (ir->operands[1])
			ir->operands[1]->accept(this);
		generated_source.append(")");
	} else if(ir->operation == ir_triop_fma && state->es_shader && state->language_version < 320) {
        // unfuse the FMA. Technically some es310 devices support FMA with GL_OES_gpu_shader5 but im
        // too lazy to implement checks for that
        generated_source.append("((");
        ir->operands[0]->accept(this);
        generated_source.append(") * (");
        ir->operands[1]->accept(this);
        generated_source.append(") + (");
        ir->operands[2]->accept(this);
        generated_source.append("))");
    } else {
		// ternary op
		generated_source.append("%s (", operator_glsl_strs[ir->operation]);
		if (ir->operands[0])
			ir->operands[0]->accept(this);
		generated_source.append(", ");
		if (ir->operands[1])
			ir->operands[1]->accept(this);
		generated_source.append(", ");
		if (ir->operands[2])
			ir->operands[2]->accept(this);
		generated_source.append(")");
	}

	newline_deindent();
	expression_depth--;
}

void
IR_TO_GLSL::visit(ir_texture* ir)
{
	glsl_sampler_dim sampler_dim = (glsl_sampler_dim)ir->sampler->type->sampler_dimensionality;
	const bool is_shadow = ir->sampler->type->sampler_shadow;
	const bool is_array = ir->sampler->type->sampler_array;

	if (ir->op == ir_txs)
	{
		generated_source.append("textureSize (");
		ir->sampler->accept(this);
		//TODO stub !
        generated_source.append(", 0");
        /*
        if (ir_texture::has_lod(ir->sampler->type))
		{
			generated_source.append(", ");
			ir->lod_info.lod->accept(this);
		}
         */
		generated_source.append(")");
		return;
	}

	const glsl_type* uv_type = ir->coordinate->type;
	const int uv_dim = uv_type->vector_elements;
	int sampler_uv_dim = tex_sampler_dim_size[sampler_dim];
	if (is_shadow)
		sampler_uv_dim += 1;
	if (is_array)
		sampler_uv_dim += 1;
	const bool is_proj = ((ir->op == ir_tex || ir->op == ir_txb || ir->op == ir_txl || ir->op == ir_txd) && uv_dim > sampler_uv_dim);
	const bool is_lod = (ir->op == ir_txl);

	/*if (is_lod && state->es_shader && state->language_version < 300 && state->stage == MESA_SHADER_FRAGMENT)
	{
		// Special workaround for GLES 2.0 LOD samplers to prevent a lot of debug spew.
		const glsl_precision prec = ir->sampler->type->get_precision();
		const char *precString = "";
		// Sampler bitfield is 7 bits, so use 0-7 for lowp, 8-15 for mediump and 16-23 for highp.
		int position = (int)sampler_dim;
		switch (prec)
		{
		case glsl_precision_high:
			position += 16;
			precString = "_high_";
			break;
		case glsl_precision_medium:
			position += 8;
			precString = "_medium_";
			break;
		case glsl_precision_low:
		default:
			precString = "_low_";
			break;
		}
		generated_source.append("impl%s", precString);
		if (is_proj)
			uses_texlodproj_impl |= (1 << position);
		else
			uses_texlod_impl |= (1 << position);
	}*/

	// texture function name
	//ACS: shadow lookups and lookups with dimensionality included in the name were deprecated in 130

	if (state->language_version < 130)
	{
		if(ir->op == ir_txf || ir->op == ir_txf_ms) {
			// texelfetch has to be emulated manually.
			// FIXME emulated the fetch properly
			generated_source.append("vec4(1.0)");
			return;
		}
        // Note: when backporting a texture op with a shadow sampler, only take the X component of it
		generated_source.append("%s", is_shadow ? "shadow" : "texture");
		generated_source.append("%s", tex_sampler_dim_name[sampler_dim]);
	}
	else
	{
        if (ir->op == ir_txf || ir->op == ir_txf_ms){
            generated_source.append("texelFetch");
        } else {
            // The signature changed from vec4 to float, so when forward porting we need to wrap it
            if (is_shadow && ir->type->vector_elements > 2){
                generated_source.append(ir->type->name);
                generated_source.append("(");
            }

            generated_source.append("texture");
        }

	}

	if (is_array && state->EXT_texture_array_enable)
		generated_source.append("Array");

	if (is_proj)
		generated_source.append("Proj");
	if (ir->op == ir_txl)
		generated_source.append("Lod");
	if (ir->op == ir_txd)
		generated_source.append("Grad");
	if (ir->offset != NULL)
		generated_source.append("Offset");

	if (state->es_shader)
	{
		/*if ((is_shadow && state->EXT_shadow_samplers_enable) ||
			(ir->op == ir_txl && state->EXT_shader_texture_lod_enable))
		{
			generated_source.append("EXT");
		}*/
	}

	if (ir->op == ir_txd)
	{
		/*if (state->es_shader && state->EXT_shader_texture_lod_enable)
			generated_source.append("EXT");
		else */if (!state->es_shader && state->ARB_shader_texture_lod_enable)
			generated_source.append("ARB");
	}

	generated_source.append(" (");

	// sampler
	ir->sampler->accept(this);
	generated_source.append(", ");

    GLenum type_enum = ir->sampler->type->gl_type;
    bool buffer_texture_sampler;
    switch(type_enum) {
        case GL_SAMPLER_BUFFER:
        case GL_INT_SAMPLER_BUFFER:
        case GL_UNSIGNED_INT_SAMPLER_BUFFER:
        case GL_IMAGE_BUFFER:
        case GL_INT_IMAGE_BUFFER:
        case GL_UNSIGNED_INT_IMAGE_BUFFER:
            buffer_texture_sampler = true;
            break;
        default:
            buffer_texture_sampler = false;
    }

	// texture coordinate
    if(sampler_uv_dim > 4) { // samplerCubeArrayShadow (jesus christ)
        ir->coordinate->accept(this);
        generated_source.append(", ");
        ir->shadow_comparator->accept(this); // the "float compare" bit, because khronos people were not crazy enough for vec5
    }else if(is_shadow || is_proj) { // these two need to generate a new vector because of the split
        generated_source.append("vec%i(", sampler_uv_dim);
        ir->coordinate->accept(this);
        if(is_shadow) {
            generated_source.append(", ");
            ir->shadow_comparator->accept(this);
        }
        if(is_proj) {
            generated_source.append(", ");
            ir->projector->accept(this);
        }
        generated_source.append(")");
    }else {
        ir->coordinate->accept(this);
    }

	// lod
    // Don't print LOD if fetching texel from buffer texture
    bool is_txf_from_buffer_texture = ir->op == ir_txf && buffer_texture_sampler;
	if ((ir->op == ir_txl || ir->op == ir_txf) && state->language_version >= 130 && !is_txf_from_buffer_texture)
	{
		generated_source.append(", ");
		ir->lod_info.lod->accept(this);
	}

	// sample index
	if (ir->op == ir_txf_ms)
	{
		generated_source.append(", ");
		ir->lod_info.sample_index->accept(this);
	}

	// grad
	if (ir->op == ir_txd)
	{
		generated_source.append(", ");
		ir->lod_info.grad.dPdx->accept(this);
		generated_source.append(", ");
		ir->lod_info.grad.dPdy->accept(this);
	}

	// texel offset
	if (ir->offset != NULL)
	{
		generated_source.append(", ");
		ir->offset->accept(this);
	}

	// lod bias
	if (ir->op == ir_txb)
	{
		generated_source.append(", ");
		ir->lod_info.bias->accept(this);
	}

    // Close the function call
    generated_source.append(")");

    // Handle forward/back porting
    if (is_shadow) {
        //printf("stateversion: %i, %i\n", state->language_version, state->original_language_version);
        if (state->language_version >= 130 && ir->type->vector_elements > 2){
            printf("closing vec4 wrapper");
            generated_source.append(")"); // Close the vec4 wrapper
        } else {
            if (state->language_version < 130 && state->original_language_version >= 130) {
                printf("reducing vec4 to float");
                generated_source.append(".x"); // Reduce from vec4 to float
            }
        }
    }
}

void
IR_TO_GLSL::visit(ir_swizzle* ir)
{
	const unsigned swiz[4] = {
	   ir->mask.x,
	   ir->mask.y,
	   ir->mask.z,
	   ir->mask.w,
	};

	if (ir->val->type == glsl_type::float_type ||
		ir->val->type == glsl_type::int_type ||
		ir->val->type == glsl_type::uint_type)
	{
		if (ir->mask.num_components != 1)
		{
			print_type(generated_source, ir->type, true);
			generated_source.append("(");
		}
	}

	ir->val->accept(this);

	if (ir->val->type == glsl_type::float_type ||
		ir->val->type == glsl_type::int_type ||
		ir->val->type == glsl_type::uint_type)
	{
		if (ir->mask.num_components != 1)
		{
			generated_source.append(")");
		}
		return;
	}

	// Swizzling scalar types is not allowed so just return now.
	if (ir->val->type->vector_elements == 1)
		return;

	generated_source.append(".");
	for (unsigned i = 0; i < ir->mask.num_components; i++)
	{
		generated_source.append("%c", "xyzw"[swiz[i]]);
	}
}

void
IR_TO_GLSL::visit(ir_dereference_variable* ir)
{
	ir_variable* var = ir->variable_referenced();
	print_var_name(var);
}

void
IR_TO_GLSL::visit(ir_dereference_array* ir)
{
	ir->array->accept(this);
	generated_source.append("[");
	ir->array_index->accept(this);
	generated_source.append("]");
}

void
IR_TO_GLSL::visit(ir_dereference_record* ir)
{
	ir->record->accept(this);

	const char* field_name =
		ir->record->type->fields.structure[ir->field_idx].name;
	generated_source.append(".%s ", field_name);
}

bool
IR_TO_GLSL::try_print_array_assignment(ir_dereference* lhs, ir_rvalue* rhs)
{
	if (this->state->language_version >= 120)
		return false;
	ir_dereference_variable* rhsarr = rhs->as_dereference_variable();
	if (rhsarr == NULL)
		return false;
	const glsl_type* lhstype = lhs->type;
	const glsl_type* rhstype = rhsarr->type;
	if (!lhstype->is_array() || !rhstype->is_array())
		return false;
	if (lhstype->array_size() != rhstype->array_size())
		return false;
	if (lhstype->base_type != rhstype->base_type)
		return false;

	const unsigned size = rhstype->array_size();
	for (unsigned i = 0; i < size; i++)
	{
		lhs->accept(this);
		generated_source.append("[%d]=", i);
		rhs->accept(this);
		generated_source.append("[%d]", i);
		if (i != size - 1)
			generated_source.append(";");
	}
	return true;
}

void
IR_TO_GLSL::emit_assignment_part(ir_dereference* lhs, ir_rvalue* rhs, unsigned write_mask, ir_rvalue* dstIndex)
{
	lhs->accept(this);

	if (dstIndex)
	{
		// if dst index is a constant, then emit a swizzle
		ir_constant* dstConst = dstIndex->as_constant();
		if (dstConst)
		{
			const char* comps = "xyzw";
			char comp = comps[dstConst->get_int_component(0)];
			generated_source.append(".%c", comp);
		}
		else
		{
			generated_source.append("[");
			dstIndex->accept(this);
			generated_source.append("]");
		}
	}

	char mask[5];
	unsigned j = 0;
	const glsl_type* lhsType = lhs->type;
	const glsl_type* rhsType = rhs->type;
	if (!dstIndex && lhsType->matrix_columns <= 1 && lhsType->vector_elements > 1 && write_mask != (1 << lhsType->vector_elements) - 1)
	{
		for (unsigned i = 0; i < 4; i++) {
			if ((write_mask & (1 << i)) != 0) {
				mask[j] = "xyzw"[i];
				j++;
			}
		}
		lhsType = glsl_type::get_instance(lhsType->base_type, j, 1);
	}
	mask[j] = '\0';
	bool hasWriteMask = false;
	if (mask[0])
	{
		generated_source.append(".%s", mask);
		hasWriteMask = true;
	}

	generated_source.append(" = ");

    bool rhs_nan_check = global->enable_nan_check &&
            lhs->variable_referenced()->data.mode == ir_var_shader_out &&
            lhsType->is_float();
    if(rhs_nan_check) generated_source.append("_ltw_removenan(");

	bool typeMismatch = !dstIndex && (lhsType != rhsType);
	const bool addSwizzle = hasWriteMask && typeMismatch;
	if (typeMismatch)
	{
		if (!addSwizzle)
			print_type(generated_source, lhsType, true);
		generated_source.append("(");
	}

	rhs->accept(this);

	if (typeMismatch)
	{
		generated_source.append(")");
		if (addSwizzle)
			generated_source.append(".%s", mask);
	}

    if(rhs_nan_check) generated_source.append(")");
}

// Try to print (X = X + const) as (X += const), mostly to satisfy
// OpenGL ES 2.0 loop syntax restrictions.
static bool try_print_increment(IR_TO_GLSL* vis, ir_assignment* ir)
{
	//if (ir->condition)
	//	return false;

	// Needs to be + on rhs
	ir_expression* rhsOp = ir->rhs->as_expression();
	if (!rhsOp || rhsOp->operation != ir_binop_add)
		return false;

	// Needs to write to whole variable
	ir_variable* lhsVar = ir->whole_variable_written();
	if (lhsVar == NULL)
		return false;

	// Types must match
	if (ir->lhs->type != ir->rhs->type)
		return false;

	// Type must be scalar
	if (!ir->lhs->type->is_scalar())
		return false;

	// rhs0 must be variable deref, same one as lhs
	ir_dereference_variable* rhsDeref = rhsOp->operands[0]->as_dereference_variable();
	if (rhsDeref == NULL)
		return false;
	if (lhsVar != rhsDeref->var)
		return false;

	// rhs1 must be a constant
	ir_constant* rhsConst = rhsOp->operands[1]->as_constant();
	if (!rhsConst)
		return false;

	// print variable name
	ir->lhs->accept(vis);

	// print ++ or +=const
	if (ir->lhs->type->base_type <= GLSL_TYPE_INT && rhsConst->is_one())
	{
		vis->generated_source.append("++");
	}
	else
	{
		vis->generated_source.append(" += ");
		rhsConst->accept(vis);
	}

	return true;
}

void
IR_TO_GLSL::visit(ir_assignment* ir)
{
	// if this is a loop induction variable initial assignment, and we aren't inside loop body:
	 // do not print it (will be printed when inside loop body)
	if (!inside_loop_body)
	{
		ir_variable* whole_var = ir->whole_variable_written();
		if (/*!ir->condition &&*/ whole_var)
		{
			ir_loop* lo = whole_var->as_loop();
			if (lo)
			{
				loop_variable_state* inductor_state = loopstate->get(lo);
				if (inductor_state && inductor_state->induction_variables.length() == 1 &&
					can_emit_canonical_for(inductor_state))
				{
					skipped_this_ir = true;
					return;
				}
			}
		}
	}

	if (this->mode != 0)
	{
		// assignments in global scope are postponed to main function
		assert(!global->main_function_done);
		global->global_assignements.push_tail(new(global->mem_ctx) ga_entry(ir));
		generated_source.append("//"); // for the ; that will follow (ugly, I know)
		return;
	}

	// if RHS is ir_triop_vector_insert, then we have to do some special dance. If source expression is:
	//   dst = vector_insert (a, b, idx)
	// then emit it like:
	//   dst = a;
	//   dst.idx = b;
	ir_expression* rhsOp = ir->rhs->as_expression();
	if (rhsOp && rhsOp->operation == ir_triop_vector_insert)
	{
		// skip assignment if lhs and rhs would be the same
		bool skip_assign = false;
		ir_dereference_variable* lhsDeref = ir->lhs->as_dereference_variable();
		ir_dereference_variable* rhsDeref = rhsOp->operands[0]->as_dereference_variable();
		if (lhsDeref && rhsDeref)
		{
			if (lhsDeref->var == rhsDeref->var)
				skip_assign = true;
		}

		if (!skip_assign)
		{
			emit_assignment_part(ir->lhs, rhsOp->operands[0], ir->write_mask, NULL);
			generated_source.append(";");
		}
		emit_assignment_part(ir->lhs, rhsOp->operands[1], ir->write_mask, rhsOp->operands[2]);
		return;
	}

	if (try_print_increment(this, ir))
		return;

	if (try_print_array_assignment(ir->lhs, ir->rhs))
		return;

    /*
	if (ir->condition)
	{
		generated_source.append("if (");
		ir->condition->accept(this);
		generated_source.append(") ");
	}
     */

	emit_assignment_part(ir->lhs, ir->rhs, ir->write_mask, NULL);
}

#ifdef _MSC_VER
#define isnan(x) _isnan(x)
#define isinf(x) (!_finite(x))
#endif

#define fpcheck(x) (isnan(x) || isinf(x))

void
print_float(sbuffer& str, float f)
{
	// Kind of roundabout way, but this is to satisfy two things:
	// * MSVC and gcc-based compilers differ a bit in how they treat float
	//   widht/precision specifiers. Want to match for tests.
	// * GLSL (early version at least) require floats to have ".0" or
	//   exponential notation.
	char tmp[64];
	snprintf(tmp, 64, "%.7g", f);

	char* posE = NULL;
	posE = strchr(tmp, 'e');
	if (!posE)
		posE = strchr(tmp, 'E');

	// snprintf formats infinity as inf.0 or -inf.0, which isn't useful here.
	// GLSL has no infinity constant so print an equivalent expression instead.
	if (f == HUGE_VALF)
		strcpy(tmp, "(1.0/0.0)");

	if (f == -HUGE_VALF)
		strcpy(tmp, "(-1.0/0.0)");

	// Do similar thing for NaN
	if (f != f)
		strcpy(tmp, "(0.0/0.0)");

#if _MSC_VER
	// While gcc would print something like 1.0e+07, MSVC will print 1.0e+007 -
	// only for exponential notation, it seems, will add one extra useless zero. Let's try to remove
	// that so compiler output matches.
	if (posE != NULL)
	{
		if ((posE[1] == '+' || posE[1] == '-') && posE[2] == '0')
		{
			char* p = posE + 2;
			while (p[0])
			{
				p[0] = p[1];
				++p;
			}
		}
	}
#endif

	str.append("%s", tmp);

	// need to append ".0"?
	if (!strchr(tmp, '.') && (posE == NULL))
		str.append(".0");
}

void IR_TO_GLSL::print_float_checked(sbuffer& str, float f) {
        int valid_float = 0;
        char float_dest[5];
        snprintf(float_dest, 5, "%f", f);
        valid_float = strstr(float_dest, "nan") == nullptr && strstr(float_dest, "inf") == nullptr;
	if (!valid_float)
		{
		// Non-printable float. If we have bit conversions, we're fine. otherwise do hand-wavey things in print_float().
		if ((state->es_shader && (state->language_version >= 300))
			|| (state->language_version >= 330)
			|| (state->ARB_shader_bit_encoding_enable))
		{
			str.append("uintBitsToFloat(%uu)", *((unsigned int*)(&f)));
			return;
		}
	}
	print_float(str, f);
	return;
}

void
IR_TO_GLSL::visit(ir_constant* ir)
{
	const glsl_type* type = ir->type;

	if (type == glsl_type::float_type)
	{
		print_float_checked(generated_source, ir->value.f[0]);
		return;
	}
	else if (type == glsl_type::int_type)
	{
		// Need special handling for INT_MIN
		if (ir->value.u[0] == 0x80000000)
			generated_source.append("int(0x%X)", ir->value.i[0]);
		else
			generated_source.append("%d", ir->value.i[0]);
		return;
	}
	else if (type == glsl_type::uint_type)
	{
		// ES 2.0 doesn't support uints, neither does GLSL < 130
		if ((state->es_shader && (state->language_version < 300))
			|| (state->language_version < 130))
			generated_source.append("%u", ir->value.u[0]);
		else
		{
			// Old Adreno drivers try to be smart with '0u' and treat that as 'const int'. Sigh.
			if (ir->value.u[0] == 0)
				generated_source.append("uint(0)");
			else
				generated_source.append("%uu", ir->value.u[0]);
		}
		return;
	}

	const glsl_type* const base_type = ir->type->get_base_type();

	print_type(generated_source, type, true);
	generated_source.append("(");

	if (ir->type->is_array())
	{
		for (unsigned i = 0; i < ir->type->length; i++)
		{
			if (i != 0)
				generated_source.append(", ");
			ir->get_array_element(i)->accept(this);
		}
	}
	else if (ir->type->is_struct())
	{
		bool first = true;
		for(unsigned int i = 0; i < ir->type->length; i++)
		{
			ir_instruction* inst = ir->const_elements[i];
			if (!first)
				generated_source.append(", ");
			first = false;
			inst->accept(this);
		}
	}
	else
	{
		bool first = true;
		for (unsigned i = 0; i < ir->type->components(); i++)
		{
			if (!first)
				generated_source.append(", ");
			first = false;
			switch (base_type->base_type) {
			case GLSL_TYPE_UINT:
			{
				// ES 2.0 doesn't support uints, neither does GLSL < 130
				if ((state->es_shader && (state->language_version < 300))
					|| (state->language_version < 130))
					generated_source.append("%u", ir->value.u[i]);
				else
					generated_source.append("%uu", ir->value.u[i]);
				break;
			}
			case GLSL_TYPE_INT:
			{
				// Need special handling for INT_MIN
				if (ir->value.u[i] == 0x80000000)
					generated_source.append("int(0x%X)", ir->value.i[i]);
				else
					generated_source.append("%d", ir->value.i[i]);
				break;
			}
			case GLSL_TYPE_FLOAT: print_float_checked(generated_source, ir->value.f[i]); break;
			case GLSL_TYPE_BOOL:  generated_source.append("%d", ir->value.b[i]); break;
			default: assert(0);
			}
		}
	}
	generated_source.append(")");
}

void
IR_TO_GLSL::visit(ir_call* ir)
{
	if (this->mode != 0)
	{
		// calls in global scope are postponed to main function
		assert(!global->main_function_done);
		global->global_assignements.push_tail(new(global->mem_ctx) ga_entry(ir));
		generated_source.append("//"); // for the ; that will follow (ugly, I know)
		return;
	}

	if (ir->return_deref)
	{
		visit(ir->return_deref);
		generated_source.append(" = ");
	}

	generated_source.append("%s (", ir->callee_name());
	bool first = true;
	foreach_in_list(ir_instruction, inst, &ir->actual_parameters)
	{
		if (!first)
			generated_source.append(", ");
		inst->accept(this);
		first = false;
	}
	generated_source.append(")");
}

void
IR_TO_GLSL::visit(ir_return* ir)
{
	generated_source.append("return");

	ir_rvalue* const value = ir->get_value();
	if (value)
	{
		generated_source.append(" ");
		value->accept(this);
	}
}

void
IR_TO_GLSL::visit(ir_discard* ir)
{
    if (ir->condition != NULL)
    {
        generated_source.append("if ( ");
        ir->condition->accept(this);
        generated_source.append(") {");
    }

	generated_source.append("discard");

    if (ir->condition != NULL) {
        generated_source.append(" ;}");
    }
}

void
IR_TO_GLSL::visit(ir_demote* ir)
{
	generated_source.append("(demote TODO)");
}

void
IR_TO_GLSL::visit(ir_if* ir)
{
	generated_source.append("if (");
	ir->condition->accept(this);

	generated_source.append(")\n");
	indent();
	indentation++; previous_skipped = false;

	generated_source.append("{\n");
	foreach_in_list(ir_instruction, inst, &ir->then_instructions)
	{
		indent();
		inst->accept(this);
		end_statement_line();
	}

	indentation--;
	indent();
	generated_source.append("}\n");

	if (!ir->else_instructions.is_empty())
	{
		indent();
		generated_source.append("else\n");
		indent();
		indentation++; previous_skipped = false;

		generated_source.append("{\n");
		foreach_in_list(ir_instruction, inst, &ir->else_instructions)
		{
			indent();
			inst->accept(this);
			end_statement_line();
		}
		indentation--;
		indent();
		generated_source.append("}\n");
		skipped_this_ir = true;
	}
}

bool
IR_TO_GLSL::can_emit_canonical_for(loop_variable_state* ls)
{
	if (ls == NULL)
		return false;

	if (ls->induction_variables.is_empty())
		return false;

	if (ls->terminators.is_empty())
		return false;

	// only support for loops with one terminator condition
	int terminatorCount = ls->terminators.length();
	if (terminatorCount != 1)
		return false;

	return true;
}

bool
IR_TO_GLSL::emit_canonical_for(ir_loop* ir)
{
	loop_variable_state* const ls = this->loopstate->get(ir);

	if (!can_emit_canonical_for(ls))
		return false;

	hash_table* terminator_hash = _mesa_pointer_hash_table_create(NULL);
	hash_table* induction_hash = _mesa_pointer_hash_table_create(NULL);

	generated_source.append("for (");
	inside_loop_body = true;

	// emit loop induction variable declarations.
	// only for loops with single induction variable, to avoid cases of different types of them
	if (ls->induction_variables.length() == 1)
	{
		foreach_in_list(loop_variable, indvar, &ls->induction_variables)
		{
			ir_loop* lo = indvar->var->as_loop();
			if (lo)
			{
				if (!this->loopstate->get(lo))
					continue;
			}
			ir_variable* var = indvar->var;
			print_type(generated_source, var->type, false);
			generated_source.append(" ");
			print_var_name(var);
			print_type_post(generated_source, var->type, false);
			if (indvar->first_assignment)
			{
				generated_source.append(" = ");
				// if the var is an array add the proper initializer
				if (var->type->is_vector())
				{
					print_type(generated_source, var->type, false);
					generated_source.append("(");
				}
				indvar->first_assignment->accept(this);
				if (var->type->is_vector())
				{
					generated_source.append(")");
				}
			}
		}
	}
	generated_source.append("; ");

	// emit loop terminating conditions
	foreach_in_list(loop_terminator, term, &ls->terminators)
	{
		_mesa_hash_table_insert(terminator_hash, term->ir, term);

		// IR has conditions in the form of "if (x) break",
		// whereas for loop needs them negated, in the form
		// if "while (x) continue the loop".
		// See if we can print them using syntax that reads nice.
		bool handled = false;
		ir_expression* term_expr = term->ir->condition->as_expression();
		if (term_expr)
		{
			// Binary comparison conditions
			const char* termOp = NULL;
			switch (term_expr->operation)
			{
			case ir_binop_less: termOp = "<"; break;
				//case ir_binop_greater: termOp = ">"; break;
				//case ir_binop_lequal: termOp = "<="; break;
			case ir_binop_gequal: termOp = ">="; break;
			case ir_binop_equal: termOp = "=="; break;
			case ir_binop_nequal: termOp = "!="; break;
			default: break;
			}
			if (termOp != NULL)
			{
				term_expr->operands[0]->accept(this);
				generated_source.append(" %s ", termOp);
				term_expr->operands[1]->accept(this);
				handled = true;
			}

			// Unary logic not
			if (!handled && term_expr->operation == ir_unop_logic_not)
			{
				term_expr->operands[0]->accept(this);
				handled = true;
			}
		}

		// More complex condition, print as "!(x)"
		if (!handled)
		{
			generated_source.append("!(");
			term->ir->condition->accept(this);
			generated_source.append(")");
		}
	}
	generated_source.append("; ");

	// emit loop induction variable updates
	bool first = true;
	foreach_in_list(loop_variable, indvar, &ls->induction_variables)
	{
		_mesa_hash_table_insert(induction_hash, indvar->first_assignment, indvar);
		if (!first)
			generated_source.append(", ");
		visit(indvar->first_assignment);
		first = false;
	}
	generated_source.append(")\n");
	indentation++; previous_skipped = false;

	indent();
	generated_source.append("{\n");

	inside_loop_body = false;

	// emit loop body
	foreach_in_list(ir_instruction, inst, &ir->body_instructions)
	{
		// skip termination & induction statements,
		// they are part of "for" clause
		if (_mesa_hash_table_search(terminator_hash, inst))
			continue;
		if (_mesa_hash_table_search(induction_hash, inst))
			continue;

		indent();
		inst->accept(this);
		end_statement_line();
	}
	indentation--;

	indent();
	generated_source.append("}\n");

	_mesa_hash_table_destroy(terminator_hash, NULL);
	_mesa_hash_table_destroy(induction_hash, NULL);

	return true;
}

void
IR_TO_GLSL::visit(ir_loop* ir)
{
	if (emit_canonical_for(ir))
		return;

	generated_source.append("while (true)\n{\n");
	indentation++; previous_skipped = false;
	foreach_in_list(ir_instruction, inst, &ir->body_instructions)
	{
		indent();
		inst->accept(this);
		end_statement_line();
	}
	indentation--;
	indent();
	generated_source.append("}\n");
}

void
IR_TO_GLSL::visit(ir_loop_jump* ir)
{
	generated_source.append("%s", ir->is_break() ? "break" : "continue");
}

void
IR_TO_GLSL::visit(ir_emit_vertex* ir)
{
	generated_source.append("EmitVertex();");
	ir->stream->accept(this);
	generated_source.append("\n");
}

void
IR_TO_GLSL::visit(ir_end_primitive* ir)
{
	generated_source.append("EndPrimitive();");
	ir->stream->accept(this);
	generated_source.append("\n");
}

void
IR_TO_GLSL::visit(ir_barrier*)
{
	generated_source.append("barrier();\n");
}

void IR_TO_GLSL::visit_uniform_block(ir_variable *ir) {
	const glsl_type* itype = ir->get_interface_type();

	for ( int i = 0; i < global->num_uniform_blocks; i++ )
	{
		if ( itype->name == global->processed_uniform_blocks[i] )
		{
			skipped_this_ir = true;
			return;
		}
	}

	assert( global->num_uniform_blocks < sizeof( global->processed_uniform_blocks ) / sizeof( global->processed_uniform_blocks[0] ) );

    global->processed_uniform_blocks[global->num_uniform_blocks++] = itype->name;

	const char* packing = nullptr;

	switch ( itype->interface_packing )
	{
		case GLSL_INTERFACE_PACKING_STD140: packing = "std140"; break;
		case GLSL_INTERFACE_PACKING_STD430: packing = "std430"; break;
		case GLSL_INTERFACE_PACKING_SHARED: packing = "shared"; break;
		default: packing = nullptr;  break;
	}

    bool explicit_location = ir->data.explicit_location;
    bool output_layout_qualifier = packing || explicit_location;
    if(output_layout_qualifier) {
        generated_source.append("layout(");
        char insert_comma[] = {0, 0};
        if(packing) {
            generated_source.append("%s%s", insert_comma, packing);
            insert_comma[0] = ',';
        }
        if(explicit_location) {
            generated_source.append("%slocation=%i", insert_comma, ir->data.location);
            insert_comma[0] = ',';
        }
        generated_source.append(")");
    }
	generated_source.append( "uniform %s {\n", itype->name );

	for ( unsigned int i = 0; i < itype->length; i++ )
	{
		const glsl_type* field_type = itype->fields.structure[i].type;
		const char* field_name = itype->fields.structure[i].name;
		const unsigned int precision_int = itype->fields.structure[i].precision;
		generated_source.append( "  %s ", precision[precision_int]);
		print_type( generated_source, field_type, false );
		generated_source.append( " %s", field_name );
		print_type_post( generated_source, field_type, false );
		generated_source.append( ";\n" );
	}

	generated_source.append( "}" );

	if ( ir->is_interface_instance() )
	{
		generated_source.append( " %s", ir->name );
	}
}
