#pragma once
#include <string>

enum class xShaderStage
{
	MESA_SHADER_NONE = -1,
	MESA_SHADER_VERTEX = 0,
	MESA_SHADER_TESS_CTRL = 1,
	MESA_SHADER_TESS_EVAL = 2,
	MESA_SHADER_GEOMETRY = 3,
	MESA_SHADER_FRAGMENT = 4,
	MESA_SHADER_COMPUTE = 5,
	/* must be last so it doesn't affect the GL pipeline */
	MESA_SHADER_KERNEL = 6,
};
enum class xApiTarget
{
	API_OPENGL_COMPAT = 0,
	API_OPENGL_CORE
};

class GlslConvert;

struct Optimizer
{
public:
	Optimizer();
public:
	std::string Optimize(
		std::string vShaderSource,
		xShaderStage vShaderType,
		int vGLSLVersion,
        int vTargetGLSLVersion,
        bool isESShader);
	bool Failed()const noexcept;
	std::string GetLog();
private:
	GlslConvert& instance;
};



