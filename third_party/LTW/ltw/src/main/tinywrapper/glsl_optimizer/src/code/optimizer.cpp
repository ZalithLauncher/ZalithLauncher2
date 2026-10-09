#include "../../include/optimizer/optimizer.h"
#include "GlslConvert.h"



Optimizer::Optimizer()
	:instance(GlslConvert::Instance())
{
}

std::string Optimizer::Optimize(std::string vShaderSource, xShaderStage vShaderType, int vGLSLVersion, int vTargetGLSLVersion, bool isESShader)
{
	//return instance.Optimize(vShaderSource.c_str(), GlslConvert::ShaderStage(vShaderType), GlslConvert::API_OPENGL_COMPAT, GlslConvert::LanguageTarget::LANGUAGE_TARGET_GLSL, vGLSLVersion, vTargetGLSLVersion, isESShader, {});
    //TODO stub
    return  "TODO STUB";
}

bool Optimizer::Failed() const noexcept
{
	return instance.Failed();
}

std::string Optimizer::GetLog()
{
	return instance.GetLog();
}
