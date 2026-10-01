/*
 * Copyright 2020 Stephane Cuillerdier (aka Aiekick)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <optional>

#include "GlslConvert.h"

#include <cstdio>
#include <cstring>

#include "../compiler/glsl/ast.h"
#include "../compiler/glsl/ir_optimization.h"
#include "../mesa/program/program.h"
#include "../compiler/glsl/program.h"
#include "../compiler/glsl/ir_reader.h"
#include "../compiler/glsl/standalone_scaffolding.h"
#include "../mesa/main/mtypes.h"
#include "../mesa/main/menums.h"
#include "../compiler/glsl/builtin_functions.h"
//#include "../compiler/glsl/loop_analysis.h"

 //#include "ir_print_ir_visitor.h"
#include "ir_print_glsl_visitor.h"
//#include "../compiler/glsl/ir_builder_print_visitor.h"

#include "../compiler/glsl/string_to_uint_map.h"
#include "../compiler/glsl/linker.h"

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

GlslConvert::GlslConvert()
{

}

GlslConvert::~GlslConvert()
{

}



///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

char * GlslConvert::Optimize(
	const char * vShaderSource,
	ShaderStage vShaderType,
	ApiTarget vTarget,
	LanguageTarget vLanguageTarget,
	int vGLSLVersion,
    int vTargetGLSLVersion,
    bool isESShader,
	OptimizationStruct vOptimizationStruct)
{
	this->failed = false;
    char * optimized_shader = NULL;

	struct gl_shader* shader = rzalloc(NULL, struct gl_shader);
	shader->Stage = (gl_shader_stage)vShaderType;
	switch (shader->Stage) {
        case gl_shader_stage::MESA_SHADER_VERTEX:
            shader->Type = GL_VERTEX_SHADER;
            break;
        case gl_shader_stage::MESA_SHADER_FRAGMENT:
            shader->Type = GL_FRAGMENT_SHADER;
            break;
        case gl_shader_stage::MESA_SHADER_TESS_CTRL:
            shader->Type = GL_TESS_CONTROL_SHADER;
            break;
        case gl_shader_stage::MESA_SHADER_TESS_EVAL:
            shader->Type = GL_TESS_EVALUATION_SHADER;
            break;
        case gl_shader_stage::MESA_SHADER_GEOMETRY:
            shader->Type = GL_GEOMETRY_SHADER;
            break;
        case gl_shader_stage::MESA_SHADER_KERNEL:
            // todo : opencl kernel target to generate after the others
            //shader->Type = GL_KERNEL_SHADER;
            break;
        default:
            break;
	}

	vOptimizationStruct.stage = vShaderType;

	struct gl_context local_ctx;
	struct gl_context* ctx = &local_ctx;
	InitContext(ctx, vTarget, vGLSLVersion);

	gl_shader_compiler_options compileOptions =
		ctx->Const.ShaderCompilerOptions[(int)shader->Stage];
	FillCompilerOptions(&compileOptions, &vOptimizationStruct);

    ctx->Const.AllowGLSLExtensionDirectiveMidShader = true;

	ir_variable::temporaries_allocate_names = true;

	struct _mesa_glsl_parse_state* state
		= new(shader) _mesa_glsl_parse_state(ctx, shader->Stage, shader);

	struct gl_shader_program* program = 0;

	// si le format d'entrée est un ir
	//shader->ir = new(shader) exec_list;
	//_mesa_glsl_initialize_types(state);
	//_mesa_glsl_read_ir(state, shader->ir, input.c_str(), true);

	shader->Source = vShaderSource;
	const char* source = shader->Source;

	if (!(vOptimizationStruct.controlFlags & ControlFlags::CONTROL_SKIP_PREPROCESSING))
	{
        state->error = glcpp_preprocess(state, &source, &state->info_log, add_builtin_defines, state, ctx) != 0;
	}

	if (!state->error)
	{
		_mesa_glsl_lexer_ctor(state, source);
		_mesa_glsl_parse(state);
		_mesa_glsl_lexer_dtor(state);

		if (vLanguageTarget == LanguageTarget::LANGUAGE_TARGET_AST)
		{
			//https://stackoverflow.com/questions/7664788/freopen-stdout-and-console
			/* Print out the initial AST */
			FILE* fp = freopen("tmp_ast", "w", stdout);
			if (fp)
			{
				foreach_list_typed(ast_node, ast, link, &state->translation_unit)
				{
					ast->print();
				}
				fclose(fp);
			}

			freopen("CON", "w", stdout);
			printf("Ast Export => SUCCESS\n");

			fp = fopen("tmp_ast", "r");
			if (fp)
			{
#define MAX_LENGTH 1024
				char* buffer = new char[MAX_LENGTH];
				while (!feof(fp))
				{
					fgets(buffer, MAX_LENGTH, fp);
					if (ferror(fp))
					{
						break;
					}
					else
					{
						//res += buffer;
                        //TODO STUB
					}
				}

				delete[] buffer;
				fclose(fp);
			}
		}
		else
		{
			exec_list* ir = new (shader) exec_list();
			shader->ir = ir;

			if (!state->translation_unit.is_empty())
				_mesa_ast_to_hir(ir, state);

            if(!state->error)
                _mesa_set_shader_inout_layout(shader, state);

			if (!state->error)
			{
				// Link built-in functions
				shader->symbols = state->symbols;

				program = GetProgramFromShader(ctx, shader);

				if (program)
				{
					bool linked = false;

					if (!ir->is_empty() && !(vOptimizationStruct.controlFlags & ControlFlags::CONTROL_DO_PARTIAL_SHADER))
					{
						const gl_shader_stage stage = program->Shaders[0]->Stage;

						bool _allowMissingMain = true;
						program->data->LinkStatus = LINKING_SUCCESS;
						program->_LinkedShaders[stage] =
							link_intrastage_shaders(
								program /* mem_ctx */,
								ctx,
								program,
								program->Shaders,
								program->NumShaders,
								_allowMissingMain);

						if (program->_LinkedShaders[stage])
						{
							linked = true;

							struct gl_shader_compiler_options* const compiler_options =
								&ctx->Const.ShaderCompilerOptions[stage];

							ir = program->_LinkedShaders[stage]->ir;
						}
						else
						{
							linked = false;

							log = program->data->InfoLog;
							failed = true;
						}
					}

					// Do optimization post-link
                    apply_optimizations(ir, linked, &compileOptions, (gl_shader_stage) vShaderType);


					validate_ir_tree(ir);

					/*if (vLanguageTarget == LanguageTarget::LANGUAGE_TARGET_IR_BUILDER)
					{
						const gl_shader_stage stage = program->Shaders[0]->Stage;
						FILE *fp = freopen("tmp_builder", "w", stdout);
						if (fp)
						{
							_mesa_print_builder_for_ir(stdout, ir);
							fclose(fp);
						}
						freopen("CON", "w", stdout);
						fp = fopen("tmp_builder", "r");
						if (fp)
						{
#define MAX_LENGTH 1024
							char *buffer = new char[MAX_LENGTH];
							while (!feof(fp))
							{
								fgets(buffer, MAX_LENGTH, fp);
								if (ferror(fp)) break;
								else res += buffer;
							}
							delete[] buffer;
							fclose(fp);
						}
					}*/
				}

				if (vLanguageTarget == LanguageTarget::LANGUAGE_TARGET_IR)
				{
					/* Print out the initial IR */
					//res = IR_TO_IR::Convert(ir, state);
				}
				else if (vLanguageTarget == LanguageTarget::LANGUAGE_TARGET_GLSL)
				{
					/* Print out the initial GLSL */
                    state->es_shader = isESShader;
                    state->language_version = vTargetGLSLVersion;
                    state->original_language_version = vGLSLVersion;
					optimized_shader = IR_TO_GLSL::Convert(ir, state);
				}
				/*else if (vLanguageTarget == LanguageTarget::LANGUAGE_TARGET_HLSL)
				{

				}
				else if (vLanguageTarget == LanguageTarget::LANGUAGE_TARGET_METAL)
				{

				}*/
			}
			else
			{
				log = state->info_log;
				failed = true;
			}
		}

	}
	else
	{
		log = state->info_log;
		failed = true;
	}

	// free
	if (program)
	{
		_mesa_clear_shader_program_data(ctx, program);
		ralloc_free(program);
	}
	ralloc_free(state);
	ralloc_free(shader);

	ClearContext(ctx);

	return optimized_shader;
}

void GlslConvert::apply_optimizations(
        struct  exec_list *vIr,
        bool linked,
        gl_shader_compiler_options* vCompilerFlags,
        gl_shader_stage vShaderType
) {
   unsigned int passes = 0;
	while (passes < 1){
      passes ++;
      do_common_optimization(vIr, linked, vCompilerFlags, true);
      do_mat_op_to_vec(vIr);
      do_vec_index_to_cond_assign(vIr);
      lower_discard(vIr);
      lower_discard_flow(vIr);
      lower_instructions(vIr, false, false);
      lower_variable_index_to_cond_assign(vShaderType, vIr, true, true, true, true);
   }
}


static void init_gl_program(struct gl_program* prog, bool is_arb_asm, gl_shader_stage target)
{
	prog->RefCount = 1;
	prog->Format = GL_PROGRAM_FORMAT_ASCII_ARB;
	//prog->is_arb_asm = is_arb_asm;
	prog->info.stage = target;
}

static struct gl_program* new_program(UNUSED struct gl_context* ctx, gl_shader_stage target,
	UNUSED GLuint id, bool is_arb_asm)
{
    struct gl_program* prog = rzalloc(NULL, struct gl_program);
    init_gl_program(prog, is_arb_asm, target);
    return prog;
}

void GlslConvert::InitContext(struct gl_context* ctx, ApiTarget api, int vGlslVersion)
{
	gl_api glApi;
	if (vGlslVersion == 100 || vGlslVersion == 300)
		glApi = gl_api::API_OPENGLES2;
	else if (api == ApiTarget::API_OPENGL_COMPAT)
		glApi = gl_api::API_OPENGL_COMPAT;
	else if (api == ApiTarget::API_OPENGL_CORE)
		glApi = gl_api::API_OPENGL_CORE;

	initialize_context_to_defaults(ctx, glApi);

	_mesa_glsl_builtin_functions_init_or_ref();

	/* The standalone compiler needs to claim support for almost
	 * everything in order to compile the built-in functions.
	 */
	ctx->Const.GLSLVersion = vGlslVersion;

	ctx->Extensions.ARB_ES3_compatibility = true;
	ctx->Extensions.ARB_ES3_1_compatibility = true;
	ctx->Extensions.ARB_ES3_2_compatibility = true;

	ctx->Const.MaxComputeWorkGroupCount[0] = 65535;
	ctx->Const.MaxComputeWorkGroupCount[1] = 65535;
	ctx->Const.MaxComputeWorkGroupCount[2] = 65535;
	ctx->Const.MaxComputeWorkGroupSize[0] = 1024;
	ctx->Const.MaxComputeWorkGroupSize[1] = 1024;
	ctx->Const.MaxComputeWorkGroupSize[2] = 64;
	ctx->Const.MaxComputeWorkGroupInvocations = 1024;
	ctx->Const.MaxComputeSharedMemorySize = 32768;
	ctx->Const.MaxComputeVariableGroupSize[0] = 512;
	ctx->Const.MaxComputeVariableGroupSize[1] = 512;
	ctx->Const.MaxComputeVariableGroupSize[2] = 64;
	ctx->Const.MaxComputeVariableGroupInvocations = 512;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxTextureImageUnits = 16;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxUniformComponents = 1024;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxCombinedUniformComponents = 1024;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxInputComponents = 0; /* not used */
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxOutputComponents = 0; /* not used */
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxAtomicBuffers = 8;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxAtomicCounters = 8;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxImageUniforms = 8;
	ctx->Const.Program[MESA_SHADER_COMPUTE].MaxUniformBlocks = 64;
    ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformBlocks = 64;
    ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformBlocks = 64;

	switch (ctx->Const.GLSLVersion) {
	case 100:
		ctx->Const.MaxClipPlanes = 0;
		ctx->Const.MaxCombinedTextureImageUnits = 8;
		ctx->Const.MaxDrawBuffers = 2;
		ctx->Const.MinProgramTexelOffset = 0;
		ctx->Const.MaxProgramTexelOffset = 0;
		ctx->Const.MaxLights = 0;
		ctx->Const.MaxTextureCoordUnits = 0;
		ctx->Const.MaxTextureUnits = 8;

		ctx->Const.Program[MESA_SHADER_VERTEX].MaxAttribs = 8;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits = 0;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformComponents = 128 * 4;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxCombinedUniformComponents = 128 * 4;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxInputComponents = 0; /* not used */
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents = 32;

		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits =
			ctx->Const.MaxCombinedTextureImageUnits;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformComponents = 16 * 4;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxCombinedUniformComponents = 16 * 4;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents =
			ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxOutputComponents = 0; /* not used */

		ctx->Const.MaxVarying = ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents / 4;
		break;
	case 110:
	case 120:
		ctx->Const.MaxClipPlanes = 6;
		ctx->Const.MaxCombinedTextureImageUnits = 2;
		ctx->Const.MaxDrawBuffers = 8;
		ctx->Const.MinProgramTexelOffset = 0;
		ctx->Const.MaxProgramTexelOffset = 0;
		ctx->Const.MaxLights = 8;
		ctx->Const.MaxTextureCoordUnits = 2;
		ctx->Const.MaxTextureUnits = 2;

		ctx->Const.Program[MESA_SHADER_VERTEX].MaxAttribs = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits = 0;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformComponents = 512;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxCombinedUniformComponents = 512;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxInputComponents = 0; /* not used */
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents = 32;

		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits =
			ctx->Const.MaxCombinedTextureImageUnits;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformComponents = 64;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxCombinedUniformComponents = 64;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents =
			ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxOutputComponents = 0; /* not used */

		ctx->Const.MaxVarying = ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents / 4;
		break;
	case 130:
	case 140:
		ctx->Const.MaxClipPlanes = 8;
		ctx->Const.MaxCombinedTextureImageUnits = 16;
		ctx->Const.MaxDrawBuffers = 8;
		ctx->Const.MinProgramTexelOffset = -8;
		ctx->Const.MaxProgramTexelOffset = 7;
		ctx->Const.MaxLights = 8;
		ctx->Const.MaxTextureCoordUnits = 8;
		ctx->Const.MaxTextureUnits = 2;
		ctx->Const.MaxUniformBufferBindings = 84;
		ctx->Const.MaxVertexStreams = 4;
		ctx->Const.MaxTransformFeedbackBuffers = 4;

		ctx->Const.Program[MESA_SHADER_VERTEX].MaxAttribs = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxCombinedUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxInputComponents = 0; /* not used */
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents = 64;

		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxCombinedUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents =
			ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxOutputComponents = 0; /* not used */

		ctx->Const.MaxVarying = ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents / 4;
		break;
	case 150:
	case 330:
	case 400:
	case 410:
	case 420:
	case 430:
	case 440:
	case 450:
	case 460:
		ctx->Const.MaxClipPlanes = 8;
		ctx->Const.MaxDrawBuffers = 8;
		ctx->Const.MinProgramTexelOffset = -8;
		ctx->Const.MaxProgramTexelOffset = 7;
		ctx->Const.MaxLights = 8;
		ctx->Const.MaxTextureCoordUnits = 8;
		ctx->Const.MaxTextureUnits = 2;
		ctx->Const.MaxUniformBufferBindings = 84;
		ctx->Const.MaxVertexStreams = 4;
		ctx->Const.MaxTransformFeedbackBuffers = 4;
		ctx->Const.MaxShaderStorageBufferBindings = 4;
		ctx->Const.MaxShaderStorageBlockSize = 4096;
		ctx->Const.MaxAtomicBufferBindings = 4;

		ctx->Const.Program[MESA_SHADER_VERTEX].MaxAttribs = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxCombinedUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxInputComponents = 0; /* not used */
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents = 64;

		ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxCombinedUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxInputComponents =
			ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents;
		ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxOutputComponents = 128;

		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxCombinedUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents =
			ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxOutputComponents;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxOutputComponents = 0; /* not used */

		ctx->Const.MaxCombinedTextureImageUnits =
			ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits
			+ ctx->Const.Program[MESA_SHADER_GEOMETRY].MaxTextureImageUnits
			+ ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits;

		ctx->Const.MaxGeometryOutputVertices = 256;
		ctx->Const.MaxGeometryTotalOutputComponents = 1024;

		ctx->Const.MaxVarying = 60 / 4;
		break;
	case 300:
	case 310:
	case 320:
		ctx->Const.MaxClipPlanes = 8;
		ctx->Const.MaxCombinedTextureImageUnits = 32;
		ctx->Const.MaxDrawBuffers = 4;
		ctx->Const.MinProgramTexelOffset = -8;
		ctx->Const.MaxProgramTexelOffset = 7;
		ctx->Const.MaxLights = 0;
		ctx->Const.MaxTextureCoordUnits = 0;
		ctx->Const.MaxTextureUnits = 0;
		ctx->Const.MaxUniformBufferBindings = 84;
		ctx->Const.MaxVertexStreams = 4;
		ctx->Const.MaxTransformFeedbackBuffers = 4;

		ctx->Const.Program[MESA_SHADER_VERTEX].MaxAttribs = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxCombinedUniformComponents = 1024;
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxInputComponents = 0; /* not used */
		ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents = 16 * 4;

		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits = 16;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformComponents = 224;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxCombinedUniformComponents = 224;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents = 15 * 4;
		ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxOutputComponents = 0; /* not used */

		ctx->Const.MaxVarying = ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents / 4;
		break;
	}

	ctx->Const.GenerateTemporaryNames = true;
	ctx->Const.MaxPatchVertices = 32;

	/* GL_ARB_explicit_uniform_location, GL_MAX_UNIFORM_LOCATIONS */
	ctx->Const.MaxUserAssignableUniformLocations =
		4 * MESA_SHADER_STAGES * MAX_UNIFORMS;

	ctx->Driver.NewProgram = new_program;
	//ctx->Driver.DeleteProgram = 0;

	// Gl4es overrides
	ctx->Const.MaxTextureCoordUnits = 16;

    // Abused override
    ctx->Const.MaxClipPlanes = 8;
    ctx->Const.MaxCombinedTextureImageUnits = 32;
    ctx->Const.MaxDrawBuffers = 4;
    ctx->Const.MinProgramTexelOffset = -8;
    ctx->Const.MaxProgramTexelOffset = 7;
    ctx->Const.MaxLights = 0;
    ctx->Const.MaxTextureUnits = 0;
    ctx->Const.MaxUniformBufferBindings = 84;
    ctx->Const.MaxVertexStreams = 4;
    ctx->Const.MaxTransformFeedbackBuffers = 4;

    ctx->Const.Program[MESA_SHADER_VERTEX].MaxAttribs = 16;
    ctx->Const.Program[MESA_SHADER_VERTEX].MaxTextureImageUnits = 16;
    ctx->Const.Program[MESA_SHADER_VERTEX].MaxUniformComponents = 1024;
    ctx->Const.Program[MESA_SHADER_VERTEX].MaxCombinedUniformComponents = 1024;
    ctx->Const.Program[MESA_SHADER_VERTEX].MaxInputComponents = 0; /* not used */
    ctx->Const.Program[MESA_SHADER_VERTEX].MaxOutputComponents = 16 * 4;

    ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxTextureImageUnits = 16;
    ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxUniformComponents = 224;
    ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxCombinedUniformComponents = 224;
    ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents = 15 * 4;
    ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxOutputComponents = 0; /* not used */

    ctx->Const.MaxVarying = ctx->Const.Program[MESA_SHADER_FRAGMENT].MaxInputComponents / 4;
}

void GlslConvert::ClearContext(struct gl_context* ctx)
{
	_mesa_glsl_builtin_functions_decref();
}

struct gl_shader_program* GlslConvert::GetProgramFromShader(struct gl_context* ctx, struct gl_shader* shader)
{
	struct gl_shader_program* whole_program = 0;

	if (!ctx) return whole_program;
	if (!shader) return whole_program;

	whole_program = rzalloc(NULL, struct gl_shader_program);
	assert(whole_program != NULL);
	whole_program->data = rzalloc(whole_program, struct gl_shader_program_data);
	assert(whole_program->data != NULL);
	whole_program->data->InfoLog = ralloc_strdup(whole_program->data, "");

	/* Created just to avoid segmentation faults */
	whole_program->AttributeBindings = new string_to_uint_map;
	whole_program->FragDataBindings = new string_to_uint_map;
	whole_program->FragDataIndexBindings = new string_to_uint_map;

	// attach
	whole_program->Shaders =
		reralloc(whole_program, whole_program->Shaders,
			struct gl_shader*, whole_program->NumShaders + 1);
	assert(whole_program->Shaders != NULL);

	whole_program->Shaders[whole_program->NumShaders] = shader;
	whole_program->NumShaders++;

	return whole_program;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

void GlslConvert::FillCompilerOptions(gl_shader_compiler_options* vCompileOptions, OptimizationStruct* vOptimizationStruct)
{
	if (vCompileOptions && vOptimizationStruct)
	{
#define COND(FLAG) (&vOptimizationStruct->compilerFlags && GlslConvert::CompilerFlags::FLAG)
		//vCompileOptions->EmitNoLoops = COND(COMPILER_EmitNoLoops);
		vCompileOptions->EmitNoCont = COND(COMPILER_EmitNoCont);
		vCompileOptions->EmitNoMainReturn = COND(COMPILER_EmitNoMainReturn);
		//vCompileOptions->EmitNoPow = COND(COMPILER_EmitNoPow);
		//vCompileOptions->EmitNoSat = COND(COMPILER_EmitNoSat);
		vCompileOptions->LowerCombinedClipCullDistance = COND(COMPILER_LowerCombinedClipCullDistance);
		vCompileOptions->EmitNoIndirectInput = COND(COMPILER_EmitNoIndirectInput);
		vCompileOptions->EmitNoIndirectOutput = COND(COMPILER_EmitNoIndirectOutput);
		vCompileOptions->EmitNoIndirectTemp = COND(COMPILER_EmitNoIndirectTemp);
		vCompileOptions->EmitNoIndirectUniform = COND(COMPILER_EmitNoIndirectUniform);
		//vCompileOptions->EmitNoIndirectSampler = COND(COMPILER_EmitNoIndirectSampler);
		vCompileOptions->MaxIfDepth = vOptimizationStruct->instructionToLower.MaxIfDepth;
		//vCompileOptions->MaxUnrollIterations = vOptimizationStruct->instructionToLower.MaxUnrollIterations;
		vCompileOptions->OptimizeForAOS = COND(COMPILER_OptimizeForAOS);
		//vCompileOptions->LowerBufferInterfaceBlocks = COND(COMPILER_LowerBufferInterfaceBlocks);
		vCompileOptions->ClampBlockIndicesToArrayBounds = COND(COMPILER_ClampBlockIndicesToArrayBounds);
		vCompileOptions->PositionAlwaysInvariant = COND(COMPILER_PositionAlwaysInvariant);
#undef COND
	}
}
