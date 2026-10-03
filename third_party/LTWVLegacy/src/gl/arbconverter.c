#include "arbconverter.h"

#include <stddef.h>

#include "arbgenerator.h"
#include "arbhelper.h"
#include "arbparser.h"
#include "khash.h"

#define FAIL(str) curStatus.status = ST_ERROR; if (*error_msg) free(*error_msg); \
		*error_msg = strdup(str); continue
#define curStatusPtr &curStatus
char* gl4es_convertARB(const char* const code, int vertex, char **error_msg, int *error_ptr) {
	*error_ptr = -1; // Reinit error pointer
	
	struct sSpecialCases specialCases = {0, 0};
	const char *codeStart = code;
	// Not sure this is really OK...
	if ((codeStart[0] != '!') || (codeStart[1] != '!')) {
		while (1) {
			while ((*codeStart != '!') && (*codeStart != '\0')) {
				++codeStart;
			}
			if (*codeStart == '\0') {
				// Invalid start
				if (*error_msg)
					free(*error_msg);
				*error_msg = strdup("Invalid program start");
				*error_ptr = 0;
				return NULL;
			}
			if ((codeStart[0] == '!') && (codeStart[1] == '!')) {
				break;
			}
			++codeStart;
		}
	}
	if (vertex) {
		if (strncmp(codeStart, "!!ARBvp1.0", 10)) {
			if (*error_msg)
				free(*error_msg);
			*error_msg = strdup("Invalid program start");
			*error_ptr = codeStart - code;
			return NULL;
		}
	} else {
		if (strncmp(codeStart, "!!ARBfp1.0", 10)) {
			if (*error_msg)
				free(*error_msg);
			*error_msg = strdup("Invalid program start");
			*error_ptr = codeStart - code;
			return NULL;
		}
	}
	
	codeStart += 10;
	
	ARBCONV_DBG_HEAVY(printf("Generating code for:\n%s\n", codeStart);)
	
	sCurStatus curStatus = {0};
	initStatus(&curStatus, codeStart);
	readNextToken(&curStatus);
	if ((curStatus.curToken != TOK_NEWLINE) && (curStatus.curToken != TOK_WHITESPACE)) {
		curStatus.status = ST_ERROR;
	} else {
		readNextToken(&curStatus);
	}
	
	while ((curStatus.status != ST_ERROR) && (curStatus.status != ST_DONE)) {
		ARBCONV_DBG_LP(
			printf(
				"%-13s",
				STATUS2STR(curStatus.status)
			);
			if (curStatus.valueType == TYPE_INST_DECL) {
				printf(
					"-%3s%4s    (%2d)",
					INST2STR(curStatus.curValue.newInst.inst.type),
					curStatus.curValue.newInst.inst.saturated ? "_SAT" : "    ",
					curStatus.curValue.newInst.state
				);
			} else if (curStatus.valueType == TYPE_VARIABLE_DECL) {
				printf(
					"-%-10s (%2d)",
					VARTYPE2STR(curStatus.curValue.newVar.var->type),
					curStatus.curValue.newVar.state
				);
			} else if (curStatus.valueType == TYPE_ALIAS_DECL) {
				printf("-(string)       ");
			} else if (curStatus.valueType == TYPE_OPTION_DECL) {
				printf("-%15s", curStatus.curValue.newOpt.optName ? curStatus.curValue.newOpt.optName : "");
			} else {
				printf("                ");
			}
			printf(" / %-11s: %p - %p (%ld)\n",
				TOKEN2STR(curStatus.curToken),
				curStatus.codePtr,
				curStatus.endOfToken,
				curStatus.endOfToken - curStatus.codePtr
			);
			fflush(stdout);
		)
		
		parseToken(&curStatus, vertex, error_msg, &specialCases);
		
		readNextToken(&curStatus);
	}
	
	if (curStatus.status == ST_ERROR) {
		ARBCONV_DBG(
		char *codeDup = strdup(code);
		codeDup[curStatus.codePtr - code] = '\0';
		printf(
			"Failure, copy until EOF\n\n%s\033[91m%s\033[m\n",
			codeDup,
			curStatus.codePtr
		);
		free(codeDup);
		
		printf("\nVariables:\n==========\n");
		{
			const char *kvar;
			sVariable *vvar;
			kh_foreach(curStatus.varsMap, kvar, vvar, 
				if (vvar) {
					printf("Variable %10s pointing to %p (%10s)\n", kvar, vvar, vvar->names[0]);
				} else {
					printf("\033[91mVariable %10s pointing to NULLptr!\033[m\n", kvar);
				}
			)
		}
		for (size_t i = 0; i < curStatus.variables.size; ++i) {
			sVariable *varPtr = curStatus.variables.vars[i];
			printf("Variable %p %10s (%lu): %-10s (init = %3lu %s)\n", varPtr, varPtr->names[0], varPtr->names_count, VARTYPE2STR(varPtr->type), varPtr->init.strings_total_len, varPtr->init.strings_count ? varPtr->init.strings[0] : "(none)");
		}
		printf("\nInstructions:\n=============\n");
		for (size_t i = 0; i < curStatus.instructions.size; ++i) {
			sInstruction *instPtr = curStatus.instructions.insts[i];
			instPtr = curStatus.instructions.insts[i];
			if (INSTTEX(instPtr->type)) {
				printf("Instruction %3s%4s %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %ctexture[%2s]      %c%19s\n", INST2STR(instPtr->type), instPtr->saturated ? "_SAT" : "    ",
					instPtr->vars[0].sign ? (instPtr->vars[0].sign == -1 ? '-' : '+') : ' ', instPtr->vars[0].var ? ((instPtr->vars[0].var->type == VARTYPE_CONST) ? instPtr->vars[0].var->init.strings[0] : instPtr->vars[0].var->names[0]) : "(none)", instPtr->vars[0].floatArrAddr ? instPtr->vars[0].floatArrAddr : "", (instPtr->vars[0].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[1].sign ? (instPtr->vars[1].sign == -1 ? '-' : '+') : ' ', instPtr->vars[1].var ? ((instPtr->vars[1].var->type == VARTYPE_CONST) ? instPtr->vars[1].var->init.strings[0] : instPtr->vars[1].var->names[0]) : "(none)", instPtr->vars[1].floatArrAddr ? instPtr->vars[1].floatArrAddr : "", (instPtr->vars[1].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[2].sign ? (instPtr->vars[2].sign == -1 ? '-' : '+') : ' ', instPtr->vars[2].var->names[0],
					instPtr->vars[3].sign ? (instPtr->vars[3].sign == -1 ? '-' : '+') : ' ', (instPtr->vars[3].var == curStatus.tex1D) ? "1D" : (instPtr->vars[3].var == curStatus.tex2D) ? "2D" : (instPtr->vars[3].var == curStatus.tex3D) ? "3D" : (instPtr->vars[3].var == curStatus.texCUBE) ? "CUBE" : (instPtr->vars[3].var == curStatus.texRECT) ? "RECT" : "!!!"
				);
			} else {
				printf("Instruction %3s%4s %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c\n", INST2STR(instPtr->type), instPtr->saturated ? "_SAT" : "    ",
					instPtr->vars[0].sign ? (instPtr->vars[0].sign == -1 ? '-' : '+') : ' ', instPtr->vars[0].var ? ((instPtr->vars[0].var->type == VARTYPE_CONST) ? instPtr->vars[0].var->init.strings[0] : instPtr->vars[0].var->names[0]) : "(none)", instPtr->vars[0].floatArrAddr ? instPtr->vars[0].floatArrAddr : "", (instPtr->vars[0].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[1].sign ? (instPtr->vars[1].sign == -1 ? '-' : '+') : ' ', instPtr->vars[1].var ? ((instPtr->vars[1].var->type == VARTYPE_CONST) ? instPtr->vars[1].var->init.strings[0] : instPtr->vars[1].var->names[0]) : "(none)", instPtr->vars[1].floatArrAddr ? instPtr->vars[1].floatArrAddr : "", (instPtr->vars[1].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[2].sign ? (instPtr->vars[2].sign == -1 ? '-' : '+') : ' ', instPtr->vars[2].var ? ((instPtr->vars[2].var->type == VARTYPE_CONST) ? instPtr->vars[2].var->init.strings[0] : instPtr->vars[2].var->names[0]) : "(none)", instPtr->vars[2].floatArrAddr ? instPtr->vars[2].floatArrAddr : "", (instPtr->vars[2].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[3].sign ? (instPtr->vars[3].sign == -1 ? '-' : '+') : ' ', instPtr->vars[3].var ? ((instPtr->vars[3].var->type == VARTYPE_CONST) ? instPtr->vars[3].var->init.strings[0] : instPtr->vars[3].var->names[0]) : "(none)", instPtr->vars[3].floatArrAddr ? instPtr->vars[3].floatArrAddr : "", (instPtr->vars[3].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[3] == SWIZ_W) ? 'w' : ' '
				);
			}
		}
		printf("\n");)
		
		*error_ptr = curStatus.codePtr - code;
		
		// We have errored, output NULL
		freeStatus(&curStatus);
		free(curStatus.outputString);
		return NULL;
	}
	
	ARBCONV_DBG(printf("Success!\n");)
	
	// Variables are automatically created, only need to write main()
	size_t varIdx = (size_t)0;
	sVariable *varPtr;
	size_t instIdx = (size_t)0;
	sInstruction *instPtr;
	
	do {
		if (vertex) {
			// Add a structure (for addresses) with only an 'x' component
			APPEND_OUTPUT("#version 120\n\nstruct _structOnlyX { int x; };\n\nvoid main() {\n", 61)
			if (specialCases.hasFogFragCoord) {
				APPEND_OUTPUT("\tvec4 gl4es_FogFragCoordTemp = vec4(gl_FogFragCoord);\n", 54)
			}
		} else {
			// No address
			APPEND_OUTPUT("#version 120\n\nvoid main() {\n", 28)
			if (specialCases.isDepthReplacing) {
				APPEND_OUTPUT("\tvec4 gl4es_FragDepthTemp = vec4(gl_FragDepth);\n", 48)
			}
		}
		
		for (; (varIdx < curStatus.variables.size) && (curStatus.status != ST_ERROR); ++varIdx) {
			varPtr = curStatus.variables.vars[varIdx];
			
			ARBCONV_DBG_AS(
				printf("Variable #%2ld: %10s (%10s)\n", varIdx, varPtr->names[0], VARTYPE2STR(varPtr->type));
				fflush(stdout);
			)
			
			generateVariablePre(&curStatus, vertex, error_msg, varPtr);
		}
		if (curStatus.status == ST_ERROR) {
			--varIdx;
			*error_ptr = 1;
			break;
		}
		
		APPEND_OUTPUT("\t\n", 2)
		for (; (instIdx < curStatus.instructions.size) && (curStatus.status != ST_ERROR); ++instIdx) {
			instPtr = curStatus.instructions.insts[instIdx];
			
			ARBCONV_DBG_AS(
				if (INSTTEX(instPtr->type)) {
					printf("Instruction #%3ld: %3s%4s %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %ctexture[%2s]      %c%19s\n", instIdx, INST2STR(instPtr->type), instPtr->saturated ? "_SAT" : "    ",
						instPtr->vars[0].sign ? (instPtr->vars[0].sign == -1 ? '-' : '+') : ' ', instPtr->vars[0].var ? ((instPtr->vars[0].var->type == VARTYPE_CONST) ? instPtr->vars[0].var->init.strings[0] : instPtr->vars[0].var->names[0]) : "(none)", instPtr->vars[0].floatArrAddr ? instPtr->vars[0].floatArrAddr : "", (instPtr->vars[0].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[3] == SWIZ_W) ? 'w' : ' ',
						instPtr->vars[1].sign ? (instPtr->vars[1].sign == -1 ? '-' : '+') : ' ', instPtr->vars[1].var ? ((instPtr->vars[1].var->type == VARTYPE_CONST) ? instPtr->vars[1].var->init.strings[0] : instPtr->vars[1].var->names[0]) : "(none)", instPtr->vars[1].floatArrAddr ? instPtr->vars[1].floatArrAddr : "", (instPtr->vars[1].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[3] == SWIZ_W) ? 'w' : ' ',
						instPtr->vars[2].sign ? (instPtr->vars[2].sign == -1 ? '-' : '+') : ' ', instPtr->vars[2].var->names[0],
						instPtr->vars[3].sign ? (instPtr->vars[3].sign == -1 ? '-' : '+') : ' ', (instPtr->vars[3].var == curStatus.tex1D) ? "1D" : (instPtr->vars[3].var == curStatus.tex2D) ? "2D" : (instPtr->vars[3].var == curStatus.tex3D) ? "3D" : (instPtr->vars[3].var == curStatus.texCUBE) ? "CUBE" : (instPtr->vars[3].var == curStatus.texRECT) ? "RECT" : "!!!"
					);
				} else {
					printf("Instruction #%3ld: %3s%4s %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c\n", instIdx, INST2STR(instPtr->type), instPtr->saturated ? "_SAT" : "    ",
						instPtr->vars[0].sign ? (instPtr->vars[0].sign == -1 ? '-' : '+') : ' ', instPtr->vars[0].var ? ((instPtr->vars[0].var->type == VARTYPE_CONST) ? instPtr->vars[0].var->init.strings[0] : instPtr->vars[0].var->names[0]) : "(none)", instPtr->vars[0].floatArrAddr ? instPtr->vars[0].floatArrAddr : "", (instPtr->vars[0].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[3] == SWIZ_W) ? 'w' : ' ',
						instPtr->vars[1].sign ? (instPtr->vars[1].sign == -1 ? '-' : '+') : ' ', instPtr->vars[1].var ? ((instPtr->vars[1].var->type == VARTYPE_CONST) ? instPtr->vars[1].var->init.strings[0] : instPtr->vars[1].var->names[0]) : "(none)", instPtr->vars[1].floatArrAddr ? instPtr->vars[1].floatArrAddr : "", (instPtr->vars[1].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[3] == SWIZ_W) ? 'w' : ' ',
						instPtr->vars[2].sign ? (instPtr->vars[2].sign == -1 ? '-' : '+') : ' ', instPtr->vars[2].var ? ((instPtr->vars[2].var->type == VARTYPE_CONST) ? instPtr->vars[2].var->init.strings[0] : instPtr->vars[2].var->names[0]) : "(none)", instPtr->vars[2].floatArrAddr ? instPtr->vars[2].floatArrAddr : "", (instPtr->vars[2].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[3] == SWIZ_W) ? 'w' : ' ',
						instPtr->vars[3].sign ? (instPtr->vars[3].sign == -1 ? '-' : '+') : ' ', instPtr->vars[3].var ? ((instPtr->vars[3].var->type == VARTYPE_CONST) ? instPtr->vars[3].var->init.strings[0] : instPtr->vars[3].var->names[0]) : "(none)", instPtr->vars[3].floatArrAddr ? instPtr->vars[3].floatArrAddr : "", (instPtr->vars[3].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[3] == SWIZ_W) ? 'w' : ' '
					);
				}
				fflush(stdout);
			)
			
			generateInstruction(&curStatus, vertex, error_msg, instPtr);
		}
		if (curStatus.status == ST_ERROR) {
			--instIdx;
			*error_ptr = curStatus.instructions.insts[instIdx]->codeLocation - code;
			break;
		}
		
		APPEND_OUTPUT("\t\n", 2)
		for (varIdx = 0; (varIdx < curStatus.variables.size) && (curStatus.status != ST_ERROR); ++varIdx) {
			varPtr = curStatus.variables.vars[varIdx];
			
			ARBCONV_DBG_AS(
				printf("Variable #%2ld (output): %10s (%10s)\n", varIdx, varPtr->names[0], VARTYPE2STR(varPtr->type));
				fflush(stdout);
			)
			
			generateVariablePst(&curStatus, vertex, error_msg, varPtr);
		}
		if (curStatus.status == ST_ERROR) {
			--varIdx;
			*error_ptr = 2;
			break;
		}
		
		if (specialCases.hasFogFragCoord) {
			APPEND_OUTPUT("\tgl_FogFragCoord = gl4es_FogFragCoordTemp.x;\n", 45)
		}
		if (specialCases.isDepthReplacing) {
			APPEND_OUTPUT("\tgl_FragDepth = gl4es_FragDepthTemp.z;\n", 39)
		}
		switch (curStatus.fogType) {
		case FOG_NONE:
			break;
		case FOG_EXP:
			APPEND_OUTPUT(
				"\tgl_FragColor.rgb = mix(gl_Fog.color.rgb, gl_FragColor.rgb, "
				"clamp(exp(-gl_Fog.density * gl_FogFragCoord), 0., 1.));\n",
				116
			)
			break;
		case FOG_EXP2:
			APPEND_OUTPUT(
				"\tgl_FragColor.rgb = mix(gl_Fog.color.rgb, gl_FragColor.rgb, "
				"clamp(exp(-(gl_Fog.density * gl_FogFragCoord)*(gl_Fog.density * gl_FogFragCoord)), 0., 1.));\n",
				153
			)
			break;
		case FOG_LINEAR:
			APPEND_OUTPUT(
				"\tgl_FragColor.rgb = mix(gl_Fog.color.rgb, gl_FragColor.rgb, "
				"clamp((gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale, 0., 1.));\n",
				123
			)
			break;
		}
		if(curStatus.position_invariant) {
			APPEND_OUTPUT("\tgl_Position = ftransform();\n", 29)
		}
		APPEND_OUTPUT("}\n", 2)
	} while (0);
	
#undef FAIL
#undef APPEND_OUTPUT
#undef APPEND_OUTPUT2
	
	if (curStatus.status == ST_ERROR) {
		ARBCONV_DBG(printf("Failure!\n");
		
		printf("\nVariables:\n==========\n");
		{
			const char *kvar;
			sVariable *vvar;
			kh_foreach(curStatus.varsMap, kvar, vvar, 
				if (vvar) {
					printf("Variable %10s pointing to %p (%10s)\n", kvar, vvar, vvar->names[0]);
				} else {
					printf("\033[91mVariable %10s pointing to NULLptr!\033[m\n", kvar);
				}
			)
		}
		for (size_t i = 0; i < curStatus.variables.size; ++i) {
			varPtr = curStatus.variables.vars[i];
			printf("%sVariable %p %10s (%lu): %-10s (init = %3lu %s)\033[m\n", (i < varIdx) ? "" : "\033[91m", varPtr, varPtr->names[0], varPtr->names_count, VARTYPE2STR(varPtr->type), varPtr->init.strings_total_len, varPtr->init.strings_count ? varPtr->init.strings[0] : "(none)");
		}
		printf("\nInstructions:\n=============\n");
		for (size_t i = 0; i < curStatus.instructions.size; ++i) {
			instPtr = curStatus.instructions.insts[i];
			if (INSTTEX(instPtr->type)) {
				printf("%sInstruction %3s%4s %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %ctexture[%2s]      %c%19s\033[m\n", (i < instIdx) ? "" : "\033[91m", INST2STR(instPtr->type), instPtr->saturated ? "_SAT" : "    ",
					instPtr->vars[0].sign ? (instPtr->vars[0].sign == -1 ? '-' : '+') : ' ', instPtr->vars[0].var ? ((instPtr->vars[0].var->type == VARTYPE_CONST) ? instPtr->vars[0].var->init.strings[0] : instPtr->vars[0].var->names[0]) : "(none)", instPtr->vars[0].floatArrAddr ? instPtr->vars[0].floatArrAddr : "", (instPtr->vars[0].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[1].sign ? (instPtr->vars[1].sign == -1 ? '-' : '+') : ' ', instPtr->vars[1].var ? ((instPtr->vars[1].var->type == VARTYPE_CONST) ? instPtr->vars[1].var->init.strings[0] : instPtr->vars[1].var->names[0]) : "(none)", instPtr->vars[1].floatArrAddr ? instPtr->vars[1].floatArrAddr : "", (instPtr->vars[1].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[2].sign ? (instPtr->vars[2].sign == -1 ? '-' : '+') : ' ', instPtr->vars[2].var->names[0],
					instPtr->vars[3].sign ? (instPtr->vars[3].sign == -1 ? '-' : '+') : ' ', (instPtr->vars[3].var == curStatus.tex1D) ? "1D" : (instPtr->vars[3].var == curStatus.tex2D) ? "2D" : (instPtr->vars[3].var == curStatus.tex3D) ? "3D" : (instPtr->vars[3].var == curStatus.texCUBE) ? "CUBE" : (instPtr->vars[3].var == curStatus.texRECT) ? "RECT" : "!!!"
				);
			} else {
				printf("%sInstruction %3s%4s %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c %c%10s[%2s].%c%c%c%c\033[m\n", (i < instIdx) ? "" : "\033[91m", INST2STR(instPtr->type), instPtr->saturated ? "_SAT" : "    ",
					instPtr->vars[0].sign ? (instPtr->vars[0].sign == -1 ? '-' : '+') : ' ', instPtr->vars[0].var ? ((instPtr->vars[0].var->type == VARTYPE_CONST) ? instPtr->vars[0].var->init.strings[0] : instPtr->vars[0].var->names[0]) : "(none)", instPtr->vars[0].floatArrAddr ? instPtr->vars[0].floatArrAddr : "", (instPtr->vars[0].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[0].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[0].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[0].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[0].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[1].sign ? (instPtr->vars[1].sign == -1 ? '-' : '+') : ' ', instPtr->vars[1].var ? ((instPtr->vars[1].var->type == VARTYPE_CONST) ? instPtr->vars[1].var->init.strings[0] : instPtr->vars[1].var->names[0]) : "(none)", instPtr->vars[1].floatArrAddr ? instPtr->vars[1].floatArrAddr : "", (instPtr->vars[1].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[1].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[1].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[1].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[1].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[2].sign ? (instPtr->vars[2].sign == -1 ? '-' : '+') : ' ', instPtr->vars[2].var ? ((instPtr->vars[2].var->type == VARTYPE_CONST) ? instPtr->vars[2].var->init.strings[0] : instPtr->vars[2].var->names[0]) : "(none)", instPtr->vars[2].floatArrAddr ? instPtr->vars[2].floatArrAddr : "", (instPtr->vars[2].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[2].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[2].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[2].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[2].swizzle[3] == SWIZ_W) ? 'w' : ' ',
					instPtr->vars[3].sign ? (instPtr->vars[3].sign == -1 ? '-' : '+') : ' ', instPtr->vars[3].var ? ((instPtr->vars[3].var->type == VARTYPE_CONST) ? instPtr->vars[3].var->init.strings[0] : instPtr->vars[3].var->names[0]) : "(none)", instPtr->vars[3].floatArrAddr ? instPtr->vars[3].floatArrAddr : "", (instPtr->vars[3].swizzle[0] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[0] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[0] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[0] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[1] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[1] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[1] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[1] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[2] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[2] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[2] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[2] == SWIZ_W) ? 'w' : ' ', (instPtr->vars[3].swizzle[3] == SWIZ_X) ? 'x' : (instPtr->vars[3].swizzle[3] == SWIZ_Y) ? 'y' : (instPtr->vars[3].swizzle[3] == SWIZ_Z) ? 'z' : (instPtr->vars[3].swizzle[3] == SWIZ_W) ? 'w' : ' '
				);
			}
		}
		
		printf("\nBuffered output:\n%s\n", curStatus.outputString);)
		
		if (*error_ptr == -1) {
			if (*error_msg)
				free(*error_msg);
			*error_msg = strdup("Not enough memory(?)");
			*error_ptr = 0;
		}
		
		// We have errored, output NULL
		freeStatus(&curStatus);
		free(curStatus.outputString);
		return NULL;
	}
	
	ARBCONV_DBG(printf("Success!\n\nOutput:\n%s", curStatus.outputString);)
	
	freeStatus(&curStatus);
	return curStatus.outputString;
}
