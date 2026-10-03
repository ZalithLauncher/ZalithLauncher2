#include "arbgenerator.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "arbhelper.h"

#define FAIL(str) curStatusPtr->status = ST_ERROR; if (*error_msg) free(*error_msg); \
		*error_msg = strdup(str); return
void generateVariablePre(sCurStatus *curStatusPtr, int vertex, char **error_msg, sVariable *varPtr) {
	(void)vertex;
	
	if (varPtr->type == VARTYPE_CONST) {
		return;
	} else if (varPtr->type == VARTYPE_ADDRESS) {
		// To be on the safe side, use a struct with only an 'x' component
		APPEND_OUTPUT("\t_structOnlyX ", 14)
		APPEND_OUTPUT2(varPtr->names[0])
		APPEND_OUTPUT(";\n", 2)
		return;
	}
	
	APPEND_OUTPUT("\tvec4 ", 6)
	APPEND_OUTPUT2(varPtr->names[0])
	
	int skipNL = 0;
	switch (varPtr->type) {
	case VARTYPE_ATTRIB:
	case VARTYPE_OUTPUT:
	case VARTYPE_PARAM:
		APPEND_OUTPUT(" = ", 3)
		APPEND_OUTPUT(varPtr->init.strings[0], varPtr->init.strings_total_len)
		break;
		
	case VARTYPE_PARAM_MULT:
		skipNL = 1;
		APPEND_OUTPUT("[", 1)
		// if size is not defined, deduce size using varPtr->init
		if (varPtr->size <= 0) {
			varPtr->size = varPtr->init.strings_count;
		}
		char buf[11]; // Assume 32-bits array address, should never overflow...
		sprintf(buf, "%d", varPtr->size);
		APPEND_OUTPUT2(buf)
		APPEND_OUTPUT("];\n", 3)
		if (varPtr->init.strings_count <= 10) {
			// Single-digit array, optimize by removing the sprintf
			for (size_t i = 0; i < varPtr->init.strings_count; ++i) {
				APPEND_OUTPUT("\t", 1)
				APPEND_OUTPUT2(varPtr->names[0])
				APPEND_OUTPUT("[", 1)
				APPEND_OUTPUT(&"0123456789"[i], 1)
				APPEND_OUTPUT("] = ", 4)
				APPEND_OUTPUT2(varPtr->init.strings[i])
				APPEND_OUTPUT(";\n", 2)
			}
		} else {
			for (size_t i = 0; i < varPtr->init.strings_count; ++i) {
				sprintf(buf, "%zd", i);
				APPEND_OUTPUT("\t", 1)
				APPEND_OUTPUT2(varPtr->names[0])
				APPEND_OUTPUT("[", 1)
				APPEND_OUTPUT2(buf)
				APPEND_OUTPUT("] = ", 4)
				APPEND_OUTPUT2(varPtr->init.strings[i])
				APPEND_OUTPUT(";\n", 2)
			}
		}
		break;
		
	case VARTYPE_CONST:
	case VARTYPE_TEMP:
		break;
		
	case VARTYPE_ADDRESS:
	case VARTYPE_ALIAS:
	case VARTYPE_TEXTURE:
	case VARTYPE_TEXTARGET:
	case VARTYPE_UNK:
		FAIL("Invalid variable type (unintended fallthrough?)");
	}
	
	if (!skipNL) {
		APPEND_OUTPUT(";\n", 2)
	}
}
void generateInstruction(sCurStatus *curStatusPtr, int vertex, char **error_msg, sInstruction *instPtr) {
	// Data access and output
#define SWIZ(i, s) instPtr->vars[i].swizzle[s]
#define SWIZORX(i, s) SWIZ(i, s) ? SWIZ(i, s) : (s + 1)
#define PUSH_SWIZZLE(s) \
		switch (s) {               \
		case SWIZ_X:               \
			APPEND_OUTPUT("x", 1); \
			break;                 \
		case SWIZ_Y:               \
			APPEND_OUTPUT("y", 1); \
			break;                 \
		case SWIZ_Z:               \
			APPEND_OUTPUT("z", 1); \
			break;                 \
		case SWIZ_W:               \
			APPEND_OUTPUT("w", 1); \
			break;                 \
		case SWIZ_NONE:            \
			break;                 \
		}
	
	// Instruction assertions
#define ASSERT_COUNT(cnt) \
		if (((cnt < MAX_OPERANDS) && instPtr->vars[cnt].var) || (cnt && !instPtr->vars[cnt - 1].var)) { \
			FAIL("Invalid instruction (not enough/too many arguments)");                                \
		}
#define ASSERT_MASKDST(i) \
		if ((instPtr->vars[i].var->type != VARTYPE_TEMP) && (instPtr->vars[i].var->type != VARTYPE_OUTPUT) \
		 && (instPtr->vars[i].var->type != VARTYPE_CONST)) {                                               \
			FAIL("Variable is not a valid masked destination register");                                   \
		}                                                                                                  \
		if (instPtr->vars[i].sign != 0) {                                                                  \
			FAIL("Variable is not a valid masked destination register");                                   \
		}                                                                                                  \
		if (instPtr->vars[i].floatArrAddr != NULL) {                                                       \
			FAIL("Variable is not a valid masked destination register");                                   \
		}                                                                                                  \
		for (int sw = 0; (sw < 3) && (SWIZ(i, sw + 1) != SWIZ_NONE); ++sw) {                               \
			if ((SWIZ(i, sw) >= SWIZ(i, sw + 1))) {                                                        \
				FAIL("Variable is not a valid masked destination register");                               \
			}                                                                                              \
		}                                                                                                  \
		if (curStatusPtr->status == ST_ERROR) {                                                            \
			return;                                                                                        \
		}
#define ASSERT_VECTSRC(i) \
		if ((instPtr->vars[i].var->type != VARTYPE_TEMP) && (instPtr->vars[i].var->type != VARTYPE_ATTRIB) \
		 && (instPtr->vars[i].var->type != VARTYPE_PARAM) && (instPtr->vars[i].var->type != VARTYPE_CONST) \
		 && (instPtr->vars[i].var->type != VARTYPE_PARAM_MULT)) {                                          \
			FAIL("Variable is not a valid vector source register");                                        \
		}                                                                                                  \
		if ((SWIZ(i, 1) != SWIZ_NONE) && (SWIZ(i, 3) == SWIZ_NONE)) {                                      \
			FAIL("Variable is not a valid vector source register");                                        \
		}
#define ASSERT_SCALSRC(i) \
		if ((instPtr->vars[i].var->type != VARTYPE_TEMP) && (instPtr->vars[i].var->type != VARTYPE_ATTRIB) \
		 && (instPtr->vars[i].var->type != VARTYPE_PARAM) && (instPtr->vars[i].var->type != VARTYPE_CONST) \
		 && (instPtr->vars[i].var->type != VARTYPE_PARAM_MULT)) {                                          \
			FAIL("Variable is not a valid vector source scalar");                                          \
		}                                                                                                  \
		if ((SWIZ(i, 0) == SWIZ_NONE) || (SWIZ(i, 1) != SWIZ_NONE)) {                                      \
			FAIL("Variable is not a valid vector source scalar");                                          \
		}
#define INST_VECTOR \
		ASSERT_COUNT(2)   \
		ASSERT_MASKDST(0) \
		ASSERT_VECTSRC(1)
#define INST_SCALAR \
		ASSERT_COUNT(2)   \
		ASSERT_MASKDST(0) \
		ASSERT_SCALSRC(1)
#define INST_BINSCL \
		ASSERT_COUNT(3)   \
		ASSERT_MASKDST(0) \
		ASSERT_SCALSRC(1) \
		ASSERT_SCALSRC(2)
#define INST_BINVEC \
		ASSERT_COUNT(3)   \
		ASSERT_MASKDST(0) \
		ASSERT_VECTSRC(1) \
		ASSERT_VECTSRC(2)
#define INST_TRIVEC \
		ASSERT_COUNT(4)   \
		ASSERT_MASKDST(0) \
		ASSERT_VECTSRC(1) \
		ASSERT_VECTSRC(2) \
		ASSERT_VECTSRC(3)
#define INST_SAMPLE \
		ASSERT_COUNT(4)                                                                                      \
		ASSERT_MASKDST(0)                                                                                    \
		ASSERT_VECTSRC(1)                                                                                    \
		if (instPtr->vars[2].var->type != VARTYPE_TEXTURE) {                                                 \
			FAIL("Invalid texture variable");                                                                \
		}                                                                                                    \
		if ((instPtr->vars[3].var != curStatusPtr->tex1D) && (instPtr->vars[3].var != curStatusPtr->tex2D)   \
		 && (instPtr->vars[3].var != curStatusPtr->tex3D) && (instPtr->vars[3].var != curStatusPtr->texCUBE) \
		 /* && (instPtr->vars[3].var != curStatusPtr->texRECT) */) {                                         \
			FAIL("Invalid texture sampler target");                                                          \
		}
	
	// Misc pushing
/* Append a DeSTination MASK (i is destination index, b is base swizzle vector or destination) */
#define PUSH_DSTMASK(i, b) \
		if (((b == i) || (SWIZ(b, 0) == SWIZ_NONE)) && (SWIZ(i, 0) != SWIZ_NONE)) {      \
			APPEND_OUTPUT(".", 1)                                                        \
			for (int sw = 0; (sw < 4) && (SWIZ(i, sw) != SWIZ_NONE); ++sw) {             \
				PUSH_SWIZZLE(SWIZ(i, sw))                                                \
			}                                                                            \
		} else if ((b != i) && (SWIZ(b, 0) != SWIZ_NONE) && (SWIZ(i, 0) == SWIZ_NONE)) { \
			APPEND_OUTPUT(".", 1) /* b is a vector */                                    \
			if (SWIZ(b, 1) == SWIZ_NONE) {                                               \
				PUSH_SWIZZLE(SWIZ(b, 0))                                                 \
				PUSH_SWIZZLE(SWIZ(b, 0))                                                 \
				PUSH_SWIZZLE(SWIZ(b, 0))                                                 \
				PUSH_SWIZZLE(SWIZ(b, 0))                                                 \
			} else {                                                                     \
				PUSH_SWIZZLE(SWIZ(b, 0))                                                 \
				PUSH_SWIZZLE(SWIZ(b, 1))                                                 \
				PUSH_SWIZZLE(SWIZ(b, 2))                                                 \
				PUSH_SWIZZLE(SWIZ(b, 3))                                                 \
			}                                                                            \
		} else if ((b != i) && (SWIZ(b, 0) != SWIZ_NONE)) {                              \
			APPEND_OUTPUT(".", 1) /* b is a vector */                                    \
			if (SWIZ(b, 1) == SWIZ_NONE) {                                               \
				for (int sw = 0; (sw < 4) && (SWIZ(i, sw) != SWIZ_NONE); ++sw) {         \
					PUSH_SWIZZLE(SWIZ(b, 0))                                             \
				}                                                                        \
			} else {                                                                     \
				for (int sw = 0; (sw < 4) && (SWIZ(i, sw) != SWIZ_NONE); ++sw) {         \
					PUSH_SWIZZLE(SWIZ(b, SWIZ(i, sw) - 1))                               \
				}                                                                        \
			}                                                                            \
		}
#define PUSH_DESTLEN(i) \
		for (dstSwizLen = 0; (dstSwizLen < 4) && (SWIZ(i, dstSwizLen) != SWIZ_NONE); ++dstSwizLen) ; \
		switch (dstSwizLen) {                                                                        \
		case 1:                                                                                      \
			APPEND_OUTPUT("(", 1)                                                                    \
			break;                                                                                   \
		case 2:                                                                                      \
			APPEND_OUTPUT("vec2(", 5)                                                                \
			break;                                                                                   \
		case 3:                                                                                      \
			APPEND_OUTPUT("vec3(", 5)                                                                \
			break;                                                                                   \
		case 0:                                                                                      \
		case 4:                                                                                      \
			APPEND_OUTPUT("vec4(", 5)                                                                \
			break;                                                                                   \
		default:                                                                                     \
			FAIL("Invalid destination swizzle length");                                              \
		}
#define PUSH_VARNAME(i) \
		if (instPtr->vars[i].sign == -1) {                        \
			APPEND_OUTPUT("-", 1)                                 \
		}                                                         \
		if (instPtr->vars[i].var->type == VARTYPE_CONST) {        \
			APPEND_OUTPUT2(instPtr->vars[i].var->init.strings[0]) \
		} else {                                                  \
			APPEND_OUTPUT2(instPtr->vars[i].var->names[0])        \
		}                                                         \
		if (instPtr->vars[i].floatArrAddr != NULL) {              \
			APPEND_OUTPUT("[", 1)                                 \
			APPEND_OUTPUT2(instPtr->vars[i].floatArrAddr)         \
			APPEND_OUTPUT("]", 1)                                 \
		}
#define PUSH_PRE_SAT(p) \
		if (instPtr->saturated) {      \
			APPEND_OUTPUT("clamp(", 6) \
		} else if (p) {                \
			APPEND_OUTPUT("(", 1)      \
		}
#define PUSH_POSTSAT(p) \
		if (instPtr->saturated) {         \
			APPEND_OUTPUT(", 0., 1.)", 9) \
		} else if (p) {                   \
			APPEND_OUTPUT(")", 1)         \
		}
	
	// Instruction variable pushing
#define PUSH_MASKDST(i) \
		PUSH_VARNAME(i)    \
		PUSH_DSTMASK(i, i)
#define PUSH_VECTSRC(i) \
		PUSH_VARNAME(i)                    \
		if (SWIZ(i, 0) != SWIZ_NONE) {     \
			APPEND_OUTPUT(".", 1)          \
			PUSH_SWIZZLE(SWIZ(i, 0))       \
			if (SWIZ(i, 3) == SWIZ_NONE) { \
				PUSH_SWIZZLE(SWIZ(i, 0))   \
				PUSH_SWIZZLE(SWIZ(i, 0))   \
				PUSH_SWIZZLE(SWIZ(i, 0))   \
			} else {                       \
				PUSH_SWIZZLE(SWIZ(i, 1))   \
				PUSH_SWIZZLE(SWIZ(i, 2))   \
				PUSH_SWIZZLE(SWIZ(i, 3))   \
			}                              \
		}
/* Append a VEctor SouRCe ComponenT */
#define PUSH_VESRCCT(i, s) \
		PUSH_VARNAME(i)                    \
		APPEND_OUTPUT(".", 1)              \
		if (SWIZ(i, 0) == SWIZ_NONE) {     \
			PUSH_SWIZZLE(s + 1)            \
		} else {                           \
			if (SWIZ(i, 3) == SWIZ_NONE) { \
				PUSH_SWIZZLE(SWIZ(i, 0))   \
			} else {                       \
				PUSH_SWIZZLE(SWIZ(i, s))   \
			}                              \
		}
/* Append a SCALar SouRCe (i is index, d is extend/duplicate to vec4) */
#define PUSH_SCALSRC(i, d) \
		PUSH_VARNAME(i)              \
		APPEND_OUTPUT(".", 1)        \
		PUSH_SWIZZLE(SWIZ(i, 0))     \
		if (d) {                     \
			PUSH_SWIZZLE(SWIZ(i, 0)) \
			PUSH_SWIZZLE(SWIZ(i, 0)) \
			PUSH_SWIZZLE(SWIZ(i, 0)) \
		}
	
	// Textures
/* Append a VECTor SaMPler */
#define PUSH_VECTSMP(i, j) \
		PUSH_VECTSRC(i)                                             \
		if (instPtr->vars[j].var == curStatusPtr->tex1D) {          \
			APPEND_OUTPUT(".x", 2)                                  \
		} else if (instPtr->vars[j].var == curStatusPtr->tex2D) {   \
			APPEND_OUTPUT(".xy", 3)                                 \
		} else if (instPtr->vars[j].var == curStatusPtr->tex3D) {   \
			APPEND_OUTPUT(".xyz", 4)                                \
		} else if (instPtr->vars[j].var == curStatusPtr->texCUBE) { \
			APPEND_OUTPUT(".xyz", 4)                                \
		} else {                                                    \
			FAIL("Invalid variable texture target");                \
		}
/* Append a texture SAMPLER */
#define PUSH_SAMPLER(i, j) \
		if (instPtr->vars[i].var->type != VARTYPE_TEXTURE) {        \
			FAIL("Invalid variable type");                          \
		}                                                           \
		APPEND_OUTPUT("gl_Sampler", 10)                             \
		if (instPtr->vars[j].var == curStatusPtr->tex1D) {          \
			APPEND_OUTPUT("1D", 2)                                  \
		} else if (instPtr->vars[j].var == curStatusPtr->tex2D) {   \
			APPEND_OUTPUT("2D", 2)                                  \
		} else if (instPtr->vars[j].var == curStatusPtr->tex3D) {   \
			APPEND_OUTPUT("3D", 2)                                  \
		} else if (instPtr->vars[j].var == curStatusPtr->texCUBE) { \
			APPEND_OUTPUT("Cube", 4)                                \
		} else {                                                    \
			FAIL("Invalid variable texture target");                \
		}                                                           \
		APPEND_OUTPUT("_", 1)                                       \
		APPEND_OUTPUT2(instPtr->vars[i].var->names[0])
/* Append a SAMPler FunCtioN */
#define PUSH_SAMPFCN(i) \
		if (instPtr->vars[i].var == curStatusPtr->tex1D) {          \
			APPEND_OUTPUT("1D", 2)                                  \
		} else if (instPtr->vars[i].var == curStatusPtr->tex2D) {   \
			APPEND_OUTPUT("2D", 2)                                  \
		} else if (instPtr->vars[i].var == curStatusPtr->tex3D) {   \
			APPEND_OUTPUT("3D", 2)                                  \
		} else if (instPtr->vars[i].var == curStatusPtr->texCUBE) { \
			APPEND_OUTPUT("Cube", 4)                                \
		} else {                                                    \
			FAIL("Invalid variable texture target");                \
		}
	
	// Misc
#define FINISH_INST(dst) \
		if (dst) {              \
			PUSH_DSTMASK(0, 0)  \
		}                       \
		APPEND_OUTPUT(";\n", 2) \
		break;
	
	int dstSwizLen;
	switch (instPtr->type) {
	case INST_ABS:
		INST_VECTOR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("abs(", 4)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_ADD:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(" + ", 3)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_ARL:
		ASSERT_COUNT(2)
		if (!vertex) {
			FAIL("Invalid instruction in fragment shader");
		}
		if (instPtr->vars[0].var->type != VARTYPE_ADDRESS) {
			FAIL("Invalid ARL destination");
		}
		if ((SWIZ(0, 0) != SWIZ_X) || (SWIZ(0, 1) != SWIZ_NONE)) {
			FAIL("Invalid address mask");
		}
		ASSERT_SCALSRC(1)
		APPEND_OUTPUT("\t", 1)
		PUSH_VARNAME(0)
		APPEND_OUTPUT(".x = int(floor(", 15)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))", 2)
		FINISH_INST(0)
		
	case INST_CMP:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_TRIVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("(", 1)
		if (dstSwizLen == 0) {
			PUSH_VESRCCT(1, 0)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, 0)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, 0)
			APPEND_OUTPUT(", (", 3)
			PUSH_VESRCCT(1, 1)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, 1)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, 1)
			APPEND_OUTPUT(", (", 3)
			PUSH_VESRCCT(1, 2)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, 2)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, 2)
			APPEND_OUTPUT(", (", 3)
			PUSH_VESRCCT(1, 3)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, 3)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, 3)
		}
		if (dstSwizLen >= 1) {
			PUSH_VESRCCT(1, SWIZ(0, 0) - 1)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, SWIZ(0, 0) - 1)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, SWIZ(0, 0) - 1)
		}
		if (dstSwizLen >= 2) {
			APPEND_OUTPUT(", (", 3)
			PUSH_VESRCCT(1, SWIZ(0, 1) - 1)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, SWIZ(0, 1) - 1)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, SWIZ(0, 1) - 1)
		}
		if (dstSwizLen >= 3) {
			APPEND_OUTPUT(", (", 3)
			PUSH_VESRCCT(1, SWIZ(0, 2) - 1)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, SWIZ(0, 2) - 1)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, SWIZ(0, 2) - 1)
		}
		if (dstSwizLen >= 4) {
			APPEND_OUTPUT(", (", 3)
			PUSH_VESRCCT(1, SWIZ(0, 3) - 1)
			APPEND_OUTPUT(" < 0.) ? ", 9)
			PUSH_VESRCCT(2, SWIZ(0, 3) - 1)
			APPEND_OUTPUT(" : ", 3)
			PUSH_VESRCCT(3, SWIZ(0, 3) - 1)
		}
		APPEND_OUTPUT(")", 1)
		
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_COS:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("cos(", 4)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_DP3:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("dot(", 4)
		PUSH_VECTSRC(1)
		APPEND_OUTPUT(".xyz, ", 6)
		PUSH_VECTSRC(2)
		APPEND_OUTPUT(".xyz))", 6)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_DP4:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("dot(", 4)
		PUSH_VECTSRC(1)
		APPEND_OUTPUT(", ", 2)
		PUSH_VECTSRC(2)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_DPH:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("dot(vec4(", 9)
		PUSH_VECTSRC(1)
		APPEND_OUTPUT(".xyz, 1.), ", 11)
		PUSH_VECTSRC(2)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_DST:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("vec4(1., ", 9)
		PUSH_VESRCCT(1, 1)
		APPEND_OUTPUT(" * ", 3)
		PUSH_VESRCCT(2, 1)
		APPEND_OUTPUT(", ", 2)
		PUSH_VESRCCT(1, 2)
		APPEND_OUTPUT(", ", 2)
		PUSH_VESRCCT(2, 3)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		
	case INST_EX2: // "Exact"
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(1)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("exp2(", 5)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(1)
		FINISH_INST(0)
		
	case INST_EXP: // Approximate
		if (!vertex) {
			FAIL("Invalid instruction in fragment shader");
		}
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = vec4(exp2(floor(", 19)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT(")), fract(", 10)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("), exp2(", 8)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("), 1.)", 6)
		FINISH_INST(1)
		
	case INST_FLR:
		INST_VECTOR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("floor(", 6)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_FRC:
		INST_VECTOR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("fract(", 6)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_KIL:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		ASSERT_COUNT(1)
		ASSERT_VECTSRC(0)
		APPEND_OUTPUT("\tif ((", 6)
		PUSH_VESRCCT(0, 0)
		APPEND_OUTPUT(" < 0.) || (", 11)
		PUSH_VESRCCT(0, 1)
		APPEND_OUTPUT(" < 0.) || (", 11)
		PUSH_VESRCCT(0, 2)
		APPEND_OUTPUT(" < 0.) || (", 11)
		PUSH_VESRCCT(0, 3)
		APPEND_OUTPUT(" < 0.)) discard;\n", 17);
		break;
		
	case INST_LG2: // "Exact"
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("log2(", 5)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_LIT:
		INST_VECTOR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("vec4(1.0, max(", 14)
		PUSH_VESRCCT(1, 0)
		APPEND_OUTPUT(", 0.0), (", 9)
		PUSH_VESRCCT(1, 0)
		APPEND_OUTPUT(" > 0.0) ? pow(max(", 18)
		PUSH_VESRCCT(1, 1)
		APPEND_OUTPUT(", 0.0), clamp(", 14)
		PUSH_VESRCCT(1, 3)
		APPEND_OUTPUT(", -180., 180.)) : 0.0, 1.0)", 27)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		
	case INST_LOG: // Approximate
		if (!vertex) {
			FAIL("Invalid instruction in fragment shader");
		}
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = vec4(floor(log2(abs(", 23)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))), abs(", 9)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT(") / exp2(floor(log2(abs(", 24)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT(")))), log2(abs(", 15)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT(")), 1.)", 7)
		FINISH_INST(1)
		
	case INST_LRP:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_TRIVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("mix(", 4)
		PUSH_VARNAME(3)
		PUSH_DSTMASK(0, 3)
		APPEND_OUTPUT(", ", 2)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		APPEND_OUTPUT(", ", 2)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_MAD:
		INST_TRIVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(1)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(" * ", 3)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		APPEND_OUTPUT(" + ", 3)
		PUSH_VARNAME(3)
		PUSH_DSTMASK(0, 3)
		PUSH_POSTSAT(1)
		FINISH_INST(0)
		
	case INST_MAX:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("max(", 4)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(", ", 2)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_MIN:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("min(", 4)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(", ", 2)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_MOV:
		INST_VECTOR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_MUL:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(" * ", 3)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_POW:
		INST_BINSCL
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("pow(", 4)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT(", ", 2)
		PUSH_SCALSRC(2, 0)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_RCP:
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("1.0 / ", 6)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_RSQ:
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("inversesqrt(", 12)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_SCS:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("vec4(cos(", 9)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("), sin(", 7)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("), 0., 0.)", 10)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		break;
		
	case INST_SGE:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = vec4((", 9)
		PUSH_VESRCCT(1, 0)
		APPEND_OUTPUT(" >= ", 4)
		PUSH_VESRCCT(2, 0)
		APPEND_OUTPUT(") ? 1. : 0., (", 14)
		PUSH_VESRCCT(1, 1)
		APPEND_OUTPUT(" >= ", 4)
		PUSH_VESRCCT(2, 1)
		APPEND_OUTPUT(") ? 1. : 0., (", 14)
		PUSH_VESRCCT(1, 2)
		APPEND_OUTPUT(" >= ", 4)
		PUSH_VESRCCT(2, 2)
		APPEND_OUTPUT(") ? 1. : 0., (", 14)
		PUSH_VESRCCT(1, 3)
		APPEND_OUTPUT(" >= ", 4)
		PUSH_VESRCCT(2, 3)
		APPEND_OUTPUT(") ? 1. : 0.)", 12)
		FINISH_INST(1)
		
	case INST_SIN:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_SCALAR
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_DESTLEN(0)
		APPEND_OUTPUT("sin(", 9)
		PUSH_SCALSRC(1, 0)
		APPEND_OUTPUT("))", 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		break;
		
	case INST_SLT:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = vec4((", 9)
		PUSH_VESRCCT(1, 0)
		APPEND_OUTPUT(" < ", 3)
		PUSH_VESRCCT(2, 0)
		APPEND_OUTPUT(") ? 1. : 0., (", 14)
		PUSH_VESRCCT(1, 1)
		APPEND_OUTPUT(" < ", 3)
		PUSH_VESRCCT(2, 1)
		APPEND_OUTPUT(") ? 1. : 0., (", 14)
		PUSH_VESRCCT(1, 2)
		APPEND_OUTPUT(" < ", 3)
		PUSH_VESRCCT(2, 2)
		APPEND_OUTPUT(") ? 1. : 0., (", 14)
		PUSH_VESRCCT(1, 3)
		APPEND_OUTPUT(" < ", 3)
		PUSH_VESRCCT(2, 3)
		APPEND_OUTPUT(") ? 1. : 0.)", 12)
		FINISH_INST(1)
		
	case INST_SUB:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		PUSH_VARNAME(1)
		PUSH_DSTMASK(0, 1)
		APPEND_OUTPUT(" - ", 3)
		PUSH_VARNAME(2)
		PUSH_DSTMASK(0, 2)
		PUSH_POSTSAT(0)
		FINISH_INST(0)
		
	case INST_SWZ:
		// TODO
		FAIL("ARBconv TODO: SWZ");
		break;
		
	case INST_TEX:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_SAMPLE
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		// APPEND_OUTPUT("texture(", 8)
		APPEND_OUTPUT("texture", 7) // Deprecated! (but texture is not official until 1.30)
		PUSH_SAMPFCN(3)
		APPEND_OUTPUT("(", 1)
		PUSH_SAMPLER(2, 3)
		APPEND_OUTPUT(", ", 2)
		PUSH_VECTSMP(1, 3)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		
	case INST_TXB:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_SAMPLE
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		// APPEND_OUTPUT("texture(", 8)
		APPEND_OUTPUT("texture", 7) // Deprecated! (but texture is not official until 1.30)
		PUSH_SAMPFCN(3)
		APPEND_OUTPUT("(", 1)
		PUSH_SAMPLER(2, 3)
		APPEND_OUTPUT(", ", 2)
		PUSH_VECTSMP(1, 3)
		APPEND_OUTPUT(", ", 2)
		PUSH_VESRCCT(1, 3)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		
	case INST_TXP:
		if (vertex) {
			FAIL("Invalid instruction in vertex shader");
		}
		INST_SAMPLE
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		// APPEND_OUTPUT("textureProj(", 12)
		APPEND_OUTPUT("texture", 7) // Deprecated! (but texture is not official until 1.30)
		PUSH_SAMPFCN(3)
		APPEND_OUTPUT("Proj(", 5)
		PUSH_SAMPLER(2, 3)
		APPEND_OUTPUT(", ", 2)
		PUSH_VECTSRC(1)
		APPEND_OUTPUT(")", 1)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		
	case INST_XPD:
		INST_BINVEC
		APPEND_OUTPUT("\t", 1)
		PUSH_MASKDST(0)
		APPEND_OUTPUT(" = ", 3)
		PUSH_PRE_SAT(0)
		APPEND_OUTPUT("vec4(cross(", 11)
		PUSH_VECTSRC(1)
		APPEND_OUTPUT(".xyz, ", 6)
		PUSH_VECTSRC(2)
		APPEND_OUTPUT(".xyz), 0.)", 10)
		PUSH_POSTSAT(0)
		FINISH_INST(1)
		
	case INST_UNK:
		FAIL("Unknown instruction (unexpected fallthrough?)");
	}
	
#undef FINISH_INST
#undef PUSH_SAMPFCN
#undef PUSH_SAMPLER
#undef PUSH_VECTSMP
#undef PUSH_SCALSRC
#undef PUSH_VESRCCT
#undef PUSH_VECTSRC
#undef PUSH_MASKDST
#undef PUSH_POSTSAT
#undef PUSH_PRE_SAT
#undef PUSH_VARNAME
#undef PUSH_DSTMASK
#undef INST_SAMPLE
#undef INST_TRIVEC
#undef INST_BINVEC
#undef INST_BINSCL
#undef INST_SCALAR
#undef INST_VECTOR
#undef ASSERT_SCALSRC
#undef ASSERT_VECTSRC
#undef ASSERT_MASKDST
#undef ASSERT_COUNT
#undef PUSH_SWIZZLE
#undef SWIZ
}
void generateVariablePst(sCurStatus *curStatusPtr, int vertex, char **error_msg, sVariable *varPtr) {
	(void)vertex;
	
	if (varPtr->type != VARTYPE_OUTPUT) {
		return;
	}
	
	APPEND_OUTPUT("\t", 1)
	APPEND_OUTPUT(varPtr->init.strings[0], varPtr->init.strings_total_len)
	APPEND_OUTPUT(" = ", 3)
	APPEND_OUTPUT2(varPtr->names[0])
	APPEND_OUTPUT(";\n", 2)
}
