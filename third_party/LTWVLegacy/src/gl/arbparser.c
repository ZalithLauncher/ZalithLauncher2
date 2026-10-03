#include "arbparser.h"

#include <stdio.h>

#include "arbhelper.h"
// MAX_TEX
#include "../config.h"

// ARBCONV_DBG_RE - resolve* error ArbConverter debug logs
#ifdef DEBUG
#define ARBCONV_DBG_RE(...) printf(__VA_ARGS__);
#else
#define ARBCONV_DBG_RE(...)
#endif

#define IS_SWIZ_VALUE(v) ((((v) >= 'w') && ((v) <= 'z')) || \
  ((v) == 'r') || ((v) == 'g') || ((v) == 'b') || ((v) == 'a'))
#define IS_SWIZZLE(str) (IS_SWIZ_VALUE((str)[0]) && \
 (((str)[1] == '\0') || (IS_SWIZ_VALUE((str)[1]) && \
 (((str)[2] == '\0') || (IS_SWIZ_VALUE((str)[2]) && \
 (((str)[3] == '\0') || (IS_SWIZ_VALUE((str)[3]) && \
  ((str)[4] == '\0'))))))))
#define IS_NEW_STR_OR_SWIZZLE(str, t) (((str)[0] == ',') || ((t == 1) && IS_SWIZZLE(str)))
#define IS_NONE_OR_SWIZZLE (!newVar->strLen || IS_SWIZZLE(newVar->strParts[0]))

ptrdiff_t getTokenLength(const sCurStatus* curStatus) {
	return curStatus->endOfToken - curStatus->codePtr;
}
int compareTokenWith(const sCurStatus* curStatus, const char* compWith, const int len) {
	return (getTokenLength(curStatus) == len) ? strncmp(curStatus->codePtr, compWith, len) : 1;
}

eToken readNextToken(sCurStatus* curStatus) {
	curStatus->codePtr = curStatus->endOfToken;
	
	switch (*curStatus->codePtr) {
	case '\0':
		curStatus->curToken = TOK_NULL;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case ' ':
	case '\t':
		curStatus->curToken = TOK_WHITESPACE;
		curStatus->endOfToken = curStatus->codePtr + 1;
		while ((*curStatus->endOfToken == ' ') || (*curStatus->endOfToken == '\t')) {
			++curStatus->endOfToken;
		}
		break;
	case '\n':
	case '\r':
		curStatus->curToken = TOK_NEWLINE;
		curStatus->endOfToken = curStatus->codePtr + 1;
		if ((*curStatus->endOfToken == '\n') || (*curStatus->endOfToken == '\r')) {
			++curStatus->endOfToken;
		}
		break;
		
	case '-':
	case '+':
		curStatus->curToken = TOK_SIGN;
		curStatus->endOfToken = curStatus->codePtr + 1;
		curStatus->tokInt = (*curStatus->codePtr == '+') ? 1 : 0;
		break;
		
	case '0': case '1': case '2': case '3': case '4':
	case '5': case '6': case '7': case '8': case '9':
		curStatus->curToken = TOK_INTEGER;
		curStatus->endOfToken = curStatus->codePtr + 1;
		
		curStatus->tokInt = *curStatus->codePtr - '0';
		while ((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9')) {
			curStatus->tokInt = curStatus->tokInt * 10 + *curStatus->endOfToken - '0';
			++curStatus->endOfToken;
		}
		
		if ((curStatus->endOfToken[0] == '.') && (curStatus->endOfToken[1] == '.')) {
			break;
		}
		
		curStatus->tokFloat = curStatus->tokInt;
		if (*curStatus->endOfToken == '.') {
			curStatus->curToken = TOK_FLOATCONST;
			++curStatus->endOfToken;
			
			if ((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9')) {
				float exp = 0.1f;
				
				while ((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9')) {
					curStatus->tokFloat += (*curStatus->endOfToken - '0') * exp;
					exp /= 10.f;
					++curStatus->endOfToken;
				}
			}
		}
		
		if ((*curStatus->endOfToken == 'e') || (*curStatus->endOfToken == 'E')) {
			curStatus->curToken = TOK_FLOATCONST;
			++curStatus->endOfToken;
			
			int s = 1;
			if (*curStatus->endOfToken == '-') {
				s = 0;
				++curStatus->endOfToken;
			} else if (*curStatus->endOfToken == '+') {
				++curStatus->endOfToken;
			}
			
			if ((*curStatus->endOfToken < '0') || (*curStatus->endOfToken > '9')) {
				curStatus->curToken = TOK_UNKNOWN;
				break;
			}
			
			int e = 0;
			while ((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9')) {
				e = e * 10 + *curStatus->endOfToken - '0';
				++curStatus->endOfToken;
			}
			
			while (e-- != 0) curStatus->tokFloat *= s ? 10.f : 0.1f;
		}
		
		break;
		
	case '.': {
		curStatus->endOfToken = curStatus->codePtr + 1;
		
		// Plain '.' is TOK_POINT, '..' is TOK_UPTO
		if (*curStatus->endOfToken == '.') {
			curStatus->curToken = TOK_UPTO;
			curStatus->endOfToken = curStatus->codePtr + 2;
			break;
		} else if ((*curStatus->endOfToken < '0') || (*curStatus->endOfToken > '9')) {
			curStatus->curToken = TOK_POINT;
			break;
		}
		
		float exp = 0.1f;
		
		curStatus->curToken = TOK_FLOATCONST;
		curStatus->tokFloat = 0;
		while ((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9')) {
			curStatus->tokFloat += (*curStatus->endOfToken - '0') * exp;
			exp /= 10.f;
			++curStatus->endOfToken;
		}
		
		if ((*curStatus->endOfToken == 'e') || (*curStatus->endOfToken == 'E')) {
			++curStatus->endOfToken;
			
			int s = 1;
			if (*curStatus->endOfToken == '-') {
				s = 0;
				++curStatus->endOfToken;
			} else if (*curStatus->endOfToken == '+') {
				++curStatus->endOfToken;
			} else if ((*curStatus->endOfToken < '0') || (*curStatus->endOfToken > '9')) {
				curStatus->curToken = TOK_UNKNOWN;
				break;
			}
			
			int e = 0;
			while ((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9')) {
				e = e * 10 + *curStatus->endOfToken - '0';
				++curStatus->endOfToken;
			}
			
			while (e-- != 0) curStatus->tokFloat *= s ? 10.f : 0.1f;
		}
		}
		break;

	case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G':
	case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
	case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U':
	case 'V': case 'W': case 'X': case 'Y': case 'Z':
	case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
	case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
	case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
	case 'v': case 'w': case 'x': case 'y': case 'z':
	case '_': case '$':
		curStatus->curToken = TOK_IDENTIFIER;
		curStatus->endOfToken = curStatus->codePtr + 1;
		while (((*curStatus->endOfToken >= '0') && (*curStatus->endOfToken <= '9'))
		    || ((*curStatus->endOfToken >= 'A') && (*curStatus->endOfToken <= 'Z'))
		    || ((*curStatus->endOfToken >= 'a') && (*curStatus->endOfToken <= 'z'))
		    || (*curStatus->endOfToken == '_') || (*curStatus->endOfToken == '$')) {
			++curStatus->endOfToken;
		}
		
		if ((curStatus->endOfToken == curStatus->codePtr + 3) && (curStatus->codePtr[0] == 'E')
		 && (curStatus->codePtr[1] == 'N') && (curStatus->codePtr[2] == 'D')) {
			curStatus->curToken = TOK_END;
		}
		break;
		
	case ',':
		curStatus->curToken = TOK_COMMA;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case '=':
		curStatus->curToken = TOK_EQUALS;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case '[':
		curStatus->curToken = TOK_LSQBRACKET;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case ']':
		curStatus->curToken = TOK_RSQBRACKET;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case '{':
		curStatus->curToken = TOK_LBRACE;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case '}':
		curStatus->curToken = TOK_RBRACE;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case ';':
		curStatus->curToken = TOK_END_OF_INST;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
	case '#':
		curStatus->curToken = TOK_LINE_COMMENT;
		curStatus->endOfToken = curStatus->codePtr + 1;
		break;
		
	default:
		if (curStatus->status == ST_LINE_COMMENT) {
			// Ignore errors in comments
			curStatus->endOfToken = curStatus->codePtr + 1;
			curStatus->curToken = TOK_UNKNOWN;
		} else {
			curStatus->endOfToken = curStatus->codePtr;
			curStatus->curToken = TOK_UNKNOWN;
			curStatus->status = ST_ERROR;
		}
	}
	
	return curStatus->curToken;
}
void copyToken(const sCurStatus* curStatus, char* dest) {
	// Token length
	ptrdiff_t tokLen = getTokenLength(curStatus);
	
	// Copy token into destination
	memcpy(dest, curStatus->codePtr, tokLen);
	
	// Null-terminate the token
	dest[tokLen] = '\0';
}
char *getToken(const sCurStatus* curStatus) {
	// Allocate (exactly enough) space
	char *tok = (char*)malloc((getTokenLength(curStatus) + 1) * sizeof(char));
	
	// Copy token
	copyToken(curStatus, tok);
	
	// Return token
	return tok;
}

int resolveAttrib(sCurStatus_NewVar *newVar, int vertex) {
	char *tok = popFIFO((sArray*)newVar);
	
	if (!tok) {
		ARBCONV_DBG_RE("Failed to get attrib: (tok NULL)\n")
		return 1;
	} else if (vertex && !strcmp(tok, "vertex")) {
		free(tok);
		tok = popFIFO((sArray*)newVar);
		if (!tok) {
			ARBCONV_DBG_RE("Failed to get attrib: vertex(tok NULL)\n")
			return 1;
		} else if (!strcmp(tok, "position")) {
			// vertex.position => gl_Vertex
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("gl_Vertex"));
			newVar->var->init.strings_total_len = 9;
		} else if (!strcmp(tok, "normal")) {
			// vertex.normal => vec4(gl_Normal, 1.)
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("vec4(gl_Normal, 1.)"));
			newVar->var->init.strings_total_len = 19;
		} else if (!strcmp(tok, "color")) {
			free(tok);
			
			if (IS_NONE_OR_SWIZZLE) {
				// vertex.color => gl_Color
				pushArray((sArray*)&newVar->var->init, strdup("gl_Color"));
				newVar->var->init.strings_total_len = 8;
				return 0;
			}
			
			tok = popFIFO((sArray*)newVar);
			if (!strcmp(tok, "primary")) {
				// vertex.color.primary => gl_Color
				free(tok);
				pushArray((sArray*)&newVar->var->init, strdup("gl_Color"));
				newVar->var->init.strings_total_len = 8;
			} else if (!strcmp(tok, "secondary")) {
				// vertex.color.secondary => gl_SecondaryColor
				free(tok);
				pushArray((sArray*)&newVar->var->init, strdup("gl_SecondaryColor"));
				newVar->var->init.strings_total_len = 17;
			} else {
				ARBCONV_DBG_RE("Failed to get attrib: vertex.color.%s\n", tok)
				free(tok);
				return 1;
			}
		} else if (!strcmp(tok, "fogcoord")) {
			// vertex.fogcoord => gl_FogCoord
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("gl_FogCoord"));
			newVar->var->init.strings_total_len = 11;
		} else if (!strcmp(tok, "texcoord")) {
			free(tok);
			if (!IS_NONE_OR_SWIZZLE) {
				tok = popFIFO((sArray*)newVar);
				if ((tok[0] == '[') && (newVar->strLen >= 2) && (newVar->strParts[1][0] == ']')
				 && (newVar->strParts[0][0] >= '0') && (newVar->strParts[0][0] <= '9')) {
					free(tok);
					char *tex = popFIFO((sArray*)newVar);
					free(popFIFO((sArray*)newVar));
					size_t bufLen = 16 + strlen(tex);
					char *buf = (char*)malloc((bufLen + 1) * sizeof(char));
					sprintf(buf, "gl_MultiTexCoord%s", tex);
					free(tex);
					pushArray((sArray*)&newVar->var->init, buf);
					newVar->var->init.strings_total_len = bufLen;
				} else {
					ARBCONV_DBG_RE("Failed to get attrib: vertex.texcoord.%s\n", tok)
					free(tok);
					return 1;
				}
			} else {
				pushArray((sArray*)&newVar->var->init, strdup("gl_MultiTexCoord0"));
				newVar->var->init.strings_total_len = 17;
			}
		} else if (!strcmp(tok, "attrib")) {
			free(tok);
			tok = popFIFO((sArray*)newVar);
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get attrib: vertex.attrib(tok NULL)\n")
				return 1;
			} else if ((tok[0] == '[') && (newVar->strLen >= 2) && (newVar->strParts[1][0] == ']')
			 && (newVar->strParts[0][0] >= '0') && (newVar->strParts[0][0] <= '9')) {
				free(tok);
				char *attr = popFIFO((sArray*)newVar);
				free(popFIFO((sArray*)newVar));
				size_t bufLen = 16 + strlen(attr);
				char *buf = (char*)malloc((bufLen + 1) * sizeof(char));
				sprintf(buf, "gl_VertexAttrib_%s", attr);
				free(attr);
				pushArray((sArray*)&newVar->var->init, buf);
				newVar->var->init.strings_total_len = bufLen;
			} else {
				ARBCONV_DBG_RE("Failed to get attrib: vertex.attrib.%s\n", tok)
				free(tok);
				return 1;
			}
		} else {
			ARBCONV_DBG_RE("Failed to get attrib: vertex.%s\n", tok)
			free(tok);
			return 1;
		}
	} else if (!vertex && !strcmp(tok, "fragment")) {
		free(tok);
		tok = popFIFO((sArray*)newVar);
		
		if (!tok) {
			ARBCONV_DBG_RE("Failed to get attrib: fragment(tok NULL)\n")
			return 1;
		} else if (!strcmp(tok, "color")) {
			free(tok);
			
			if (IS_NONE_OR_SWIZZLE) {
				// fragment.color => gl_Color
				pushArray((sArray*)&newVar->var->init, strdup("gl_Color"));
				newVar->var->init.strings_total_len = 8;
				return 0;
			}
			
			tok = popFIFO((sArray*)newVar);
			if (!strcmp(tok, "primary")) {
				// fragment.color.primary => gl_Color
				free(tok);
				pushArray((sArray*)&newVar->var->init, strdup("gl_Color"));
				newVar->var->init.strings_total_len = 8;
			} else if (!strcmp(tok, "secondary")) {
				// fragment.color.secondary => gl_SecondaryColor
				free(tok);
				pushArray((sArray*)&newVar->var->init, strdup("gl_SecondaryColor"));
				newVar->var->init.strings_total_len = 17;
			} else {
				ARBCONV_DBG_RE("Failed to get attrib: fragment.color.%s\n", tok)
				free(tok);
				return 1;
			}
		} else if (!strcmp(tok, "texcoord")) {
			free(tok);
			if (!IS_NONE_OR_SWIZZLE) {
				tok = popFIFO((sArray*)newVar);
				if ((tok[0] == '[') && (newVar->strLen >= 2) && (newVar->strParts[1][0] == ']')
				 && (newVar->strParts[0][0] >= '0') && (newVar->strParts[0][0] <= '9')) {
					free(tok);
					char *tex = popFIFO((sArray*)newVar);
					free(popFIFO((sArray*)newVar));
					size_t bufLen = 13 + strlen(tex);
					char *buf = (char*)malloc((bufLen + 1) * sizeof(char));
					sprintf(buf, "gl_TexCoord[%s]", tex);
					pushArray((sArray*)&newVar->var->init, buf);
					free(tex);
					newVar->var->init.strings_total_len = bufLen;
				} else {
					ARBCONV_DBG_RE("Failed to get attrib: fragment.texcoord.%s\n", tok)
					free(tok);
					return 1;
				}
			} else {
				pushArray((sArray*)&newVar->var->init, strdup("gl_TexCoord[0]"));
				newVar->var->init.strings_total_len = 14;
			}
		} else if (!strcmp(tok, "fogcoord")) {
			// fragment.fogcoord => vec4(gl_FogFragCoord, 0., 0., 1.)
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("vec4(gl_FogFragCoord, 0., 0., 1.)"));
			newVar->var->init.strings_total_len = 33;
		} else if (!strcmp(tok, "position")) {
			// fragment.position => gl_FragCoord
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("gl_FragCoord"));
			newVar->var->init.strings_total_len = 12;
		} else {
			ARBCONV_DBG_RE("Failed to get attrib: fragment.%s\n", tok)
			free(tok);
			return 1;
		}
	} else {
		ARBCONV_DBG_RE("Failed to get attrib: %s\n", tok)
		free(tok);
		return 1;
	}
	/* TODO: (* todo, V done, X unsupported)
	 V "vertex" "." "position"
	 X "vertex" "." "weight" <vtxOptWeightNum>
	 V "vertex" "." "normal"
	 V "vertex" "." "color"
	 V "vertex" "." "color" "." "primary"
	 V "vertex" "." "color" "." "secondary"
	 V "vertex" "." "fogcoord"
	 V "vertex" "." "texcoord"
	 V "vertex" "." "texcoord" "[" <texCoordNum> "]"
	 * "vertex" "." "matrixindex" "[" <vtxWeightNum> "]"
	 V "vertex" "." "attrib" "[" <vtxAttribNum> "]"
	 * 
	 V "fragment" "." "color"
	 V "fragment" "." "color" "." "primary"
	 V "fragment" "." "color" "." "secondary"
	 V "fragment" "." "texcoord"
	 V "fragment" "." "texcoord" "[" <texCoordNum> "]"
	 V "fragment" "." "fogcoord"
	 V "fragment" "." "position"
	 */
	
	return 0;
}
int resolveOutput(sCurStatus_NewVar *newVar, int vertex, struct sSpecialCases *specialCases) {
	char *tok = popFIFO((sArray*)newVar);
	
	if (!tok) {
		ARBCONV_DBG_RE("Failed to get output: (tok NULL)\n")
		return 1;
	} else if (vertex && !strcmp(tok, "result")) {
		free(tok);
		tok = popFIFO((sArray*)newVar);
		
		if (!tok) {
			ARBCONV_DBG_RE("Failed to get output: result(tok NULL)\n")
			return 1;
		} else if (!strcmp(tok, "position")) {
			// result.position => gl_Position
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("gl_Position"));
			newVar->var->init.strings_total_len = 11;
		} else if (!strcmp(tok, "color")) {
			free(tok);
			
			if (IS_NONE_OR_SWIZZLE) {
				// result.color => gl_FrontColor
				pushArray((sArray*)&newVar->var->init, strdup("gl_FrontColor"));
				newVar->var->init.strings_total_len = 13;
				return 0;
			}
			
			tok = popFIFO((sArray*)newVar);
			if (!strcmp(tok, "front")) {
				free(tok);
				
				if (IS_NONE_OR_SWIZZLE) {
					// result.color.front => gl_FrontColor
					pushArray((sArray*)&newVar->var->init, strdup("gl_FrontColor"));
					newVar->var->init.strings_total_len = 13;
				}
				
				tok = popFIFO((sArray*)newVar);
				if (!strcmp(tok, "primary")) {
					// result.color.front.primary => gl_FrontColor
					free(tok);
					pushArray((sArray*)&newVar->var->init, strdup("gl_FrontColor"));
					newVar->var->init.strings_total_len = 13;
				} else if (!strcmp(tok, "secondary")) {
					// result.color.front.secondary => gl_FrontSecondaryColor
					free(tok);
					pushArray((sArray*)&newVar->var->init, strdup("gl_FrontSecondaryColor"));
					newVar->var->init.strings_total_len = 22;
				} else {
					ARBCONV_DBG_RE("Failed to get output: result.color.front.%s\n", tok)
					free(tok);
					return 1;
				}
			} else if (!strcmp(tok, "back")) {
				free(tok);
				
				if (IS_NONE_OR_SWIZZLE) {
					// result.color.back => gl_BackColor
					pushArray((sArray*)&newVar->var->init, strdup("gl_BackColor"));
					newVar->var->init.strings_total_len = 12;
				}
				
				tok = popFIFO((sArray*)newVar);
				if (!strcmp(tok, "primary")) {
					// result.color.back.primary => gl_BackColor
					free(tok);
					pushArray((sArray*)&newVar->var->init, strdup("gl_BackColor"));
					newVar->var->init.strings_total_len = 12;
				} else if (!strcmp(tok, "secondary")) {
					// result.color.back.secondary => gl_BackSecondaryColor
					free(tok);
					pushArray((sArray*)&newVar->var->init, strdup("gl_BackSecondaryColor"));
					newVar->var->init.strings_total_len = 21;
				} else {
					ARBCONV_DBG_RE("Failed to get output: result.color.back.%s\n", tok)
					free(tok);
					return 1;
				}
			} else if (!strcmp(tok, "primary")) {
				// result.color.primary => gl_FrontColor
				free(tok);
				pushArray((sArray*)&newVar->var->init, strdup("gl_FrontColor"));
				newVar->var->init.strings_total_len = 13;
			} else if (!strcmp(tok, "secondary")) {
				// result.color.secondary => gl_FrontSecondaryColor
				free(tok);
				pushArray((sArray*)&newVar->var->init, strdup("gl_FrontSecondaryColor"));
				newVar->var->init.strings_total_len = 22;
			} else {
				ARBCONV_DBG_RE("Failed to get output: result.color.%s\n", tok)
				free(tok);
				return 1;
			}
		} else if (!strcmp(tok, "fogcoord")) {
			// result.fogcoord => gl_FogFragCoord
			free(tok);
			specialCases->hasFogFragCoord = 1;
			pushArray((sArray*)&newVar->var->init, strdup("gl4es_FogFragCoordTemp"));
			newVar->var->init.strings_total_len = 22;
		} else if (!strcmp(tok, "pointsize")) {
			// result.pointsize => gl_Point.size
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("vec4(gl_Point.size, 0., 0., 0.)"));
			newVar->var->init.strings_total_len = 31;
		} else if (!strcmp(tok, "texcoord")) {
			free(tok);
			if (!IS_NONE_OR_SWIZZLE) {
				tok = popFIFO((sArray*)newVar);
				if ((tok[0] == '[')
				 && (newVar->strLen >= 2) && (newVar->strParts[1][0] == ']')
				 && (newVar->strParts[0][0] >= '0') && (newVar->strParts[0][0] <= '9')) {
					free(tok);
					char *tex = popFIFO((sArray*)newVar);
					free(popFIFO((sArray*)newVar));
					size_t bufLen = 13 + strlen(tex);
					char *buf = (char*)malloc((bufLen + 1) * sizeof(char));
					sprintf(buf, "gl_TexCoord[%s]", tex);
					pushArray((sArray*)&newVar->var->init, buf);
					free(tex);
					newVar->var->init.strings_total_len = bufLen;
				} else {
					ARBCONV_DBG_RE("Failed to get output: result.texcoord.%s\n", tok)
					free(tok);
					return 1;
				}
			} else {
				pushArray((sArray*)&newVar->var->init, strdup("gl_TexCoord[0]"));
				newVar->var->init.strings_total_len = 14;
			}
		} else {
			ARBCONV_DBG_RE("Failed to get output: result.%s\n", tok)
			free(tok);
			return 1;
		}
	} else if (!vertex && !strcmp(tok, "result")) {
		free(tok);
		tok = popFIFO((sArray*)newVar);
		
		if (!tok) {
			ARBCONV_DBG_RE("Failed to get output: result(tok NULL)\n")
			return 1;
		} else if (!strcmp(tok, "color")) {
			// result.color => gl_FragColor
			free(tok);
			pushArray((sArray*)&newVar->var->init, strdup("gl_FragColor"));
			newVar->var->init.strings_total_len = 12;
		} else if (!strcmp(tok, "depth")) {
			// result.depth => gl_FragDepth
			free(tok);
			specialCases->isDepthReplacing = 1;
			pushArray((sArray*)&newVar->var->init, strdup("gl4es_FragDepthTemp"));
			newVar->var->init.strings_total_len = 12;
		} else {
			ARBCONV_DBG_RE("Failed to get output: result.%s\n", tok)
			free(tok);
			return 1;
		}
	} else {
		ARBCONV_DBG_RE("Failed to get output: %s\n", tok)
		free(tok);
		return 1;
	}
	/* TODO: (V done)
	 V "result" "." "position"
	 V "result" "." "color"
	 V "result" "." "color" "." "primary"
	 V "result" "." "color" "." "secondary"
	 V "result" "." "color" "." "front"
	 V "result" "." "color" "." "front" "." "primary"
	 V "result" "." "color" "." "front" "." "secondary"
	 V "result" "." "color" "." "back"
	 V "result" "." "color" "." "back" "." "primary"
	 V "result" "." "color" "." "back" "." "secondary"
	 V "result" "." "fogcoord"
	 V "result" "." "pointsize"
	 V "result" "." "texcoord"
	 V "result" "." "texcoord" "[" <texCoordNum> "]"
	 * 
	 V "result" "." "color"
	 V "result" "." "depth"
	 */
	
	return 0;
}

char **resolveParam(sCurStatus_NewVar *newVar, int vertex, int type) {
	(void)vertex;
	
	const char *matrixName = NULL;
	char *matrixNameMallocd = NULL;
	size_t mtxNameLen;
	int start = 0;
	int end = 3;
	int isMatrix = 0;
	int refuseEndGE4 = 1;
	
	char *tok = popFIFO((sArray*)newVar);
	if (!tok) {
		ARBCONV_DBG_RE("Failed to get param: (tok NULL)\n")
		return NULL;
	} else if (!strcmp(tok, "state")) {
		free(tok);
		tok = popFIFO((sArray*)newVar);
		
		if (!tok) {
			ARBCONV_DBG_RE("Failed to get param: state.(tok NULL)\n")
			return NULL;
		} else if (!strcmp(tok, "material")) {
			size_t propLen;
			const char *prop;
			
			free(tok);
			tok = popFIFO((sArray*)newVar);
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get param: state.material(tok NULL)\n")
				return NULL;
			} else if (!strcmp(tok, "front")) {
				free(tok);
				tok = popFIFO((sArray*)newVar);
				mtxNameLen = 16;
				matrixName = "gl_FrontMaterial";
			} else if (!strcmp(tok, "back")) {
				free(tok);
				tok = popFIFO((sArray*)newVar);
				mtxNameLen = 15;
				matrixName = "gl_BackMaterial";
			} else {
				mtxNameLen = 16;
				matrixName = "gl_FrontMaterial";
			}
			
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get param: [%s].(tok NULL)\n", matrixName)
				return NULL;
			} else if (!strcmp(tok, "ambient")) {
				free(tok);
				propLen = 7;
				prop = "ambient";
			} else if (!strcmp(tok, "diffuse")) {
				free(tok);
				propLen = 7;
				prop = "diffuse";
			} else if (!strcmp(tok, "specular")) {
				free(tok);
				propLen = 8;
				prop = "specular";
			} else if (!strcmp(tok, "emission")) {
				free(tok);
				propLen = 8;
				prop = "emission";
			} else if (!strcmp(tok, "shininess")) {
				free(tok);
				matrixNameMallocd = (char*)malloc((mtxNameLen + 17) * sizeof(char));
				sprintf(matrixNameMallocd, "vec4(%s.shininess)", matrixName);
				char **r = (char**)calloc(2, sizeof(char*));
				r[0] = matrixNameMallocd;
				r[1] = NULL;
				return r;
			} else {
				ARBCONV_DBG_RE("Failed to get param: [%s].%s\n", matrixName, tok)
				free(tok);
				return NULL;
			}
			
			matrixNameMallocd = (char*)malloc((mtxNameLen + propLen + 2) * sizeof(char));
			sprintf(matrixNameMallocd, "%s.%s", matrixName, prop);
			char **r = (char**)calloc(2, sizeof(char*));
			r[0] = matrixNameMallocd;
			r[1] = NULL;
			return r;
		} else if (!strcmp(tok, "light")) {
			free(tok);
			if (newVar->strParts[0][0] == '[') {
				free(popFIFO((sArray*)newVar));
				char *sln = popFIFO((sArray*)newVar);
				
				if ((sln[0] >= '0') && (sln[0] <= '9')) {
					if (newVar->strParts[0][0] != ']') {
						ARBCONV_DBG_RE("Failed to get param: state.light[%s(not ])\n", sln)
						return NULL;
					}
					free(popFIFO((sArray*)newVar));
					
					tok = popFIFO((sArray*)newVar);
					if (!tok) {
						ARBCONV_DBG_RE("Failed to get param: state.light[%s](tok NULL)\n", sln)
						return NULL;
					} else if (!strcmp(tok, "ambient")) {
						free(tok);
						mtxNameLen = 7;
						matrixName = "ambient";
					} else if (!strcmp(tok, "diffuse")) {
						free(tok);
						mtxNameLen = 7;
						matrixName = "diffuse";
					} else if (!strcmp(tok, "specular")) {
						free(tok);
						mtxNameLen = 8;
						matrixName = "specular";
					} else if (!strcmp(tok, "position")) {
						free(tok);
						mtxNameLen = 8;
						matrixName = "position";
					} else if (!strcmp(tok, "attenuation")) {
						free(tok);
						matrixNameMallocd = (char*)malloc((4 * strlen(sln) + 149) * sizeof(char));
						sprintf(
							matrixNameMallocd,
							"vec4(gl_LightSource[%s].constantAttenuation, gl_LightSource[%s].linearAttenuation, "
							"gl_LightSource[%s].quadraticAttenuation, gl_LightSource[%s].spotExponent)",
							sln, sln, sln, sln
						);
						char **r = (char**)calloc(2, sizeof(char*));
						r[0] = matrixNameMallocd;
						r[1] = NULL;
						return r;
					} else if (!strcmp(tok, "spot")) {
						free(tok);
						tok = popFIFO((sArray*)newVar);
						if (!tok) {
							ARBCONV_DBG_RE("Failed to get param: state.light[%s].spot(tok NULL)\n", sln)
							return NULL;
						} else if (!strcmp(tok, "direction")) {
							mtxNameLen = 9;
							matrixName = "spotDirection";
						} else {
							ARBCONV_DBG_RE("Failed to get param: state.light[%s].spot.%s\n", sln, tok)
							free(tok);
							return NULL;
						}
					} else if (!strcmp(tok, "half")) {
						free(tok);
						mtxNameLen = 10;
						matrixName = "halfVector";
					} else {
						ARBCONV_DBG_RE("Failed to get param: state.light[%s].%s\n", sln, tok)
						free(tok);
						return NULL;
					}
					
					matrixNameMallocd = (char*)malloc((mtxNameLen + strlen(sln) + 8) * sizeof(char));
					sprintf(matrixNameMallocd, "gl_LightSource[%s].%s", sln, matrixName);
					free(sln);
					char **r = (char**)calloc(2, sizeof(char*));
					r[0] = matrixNameMallocd;
					r[1] = NULL;
					return r;
				} else {
					ARBCONV_DBG_RE("Failed to get param: state.light.%s\n", sln)
					free(sln);
					return NULL;
				}
			} else {
				ARBCONV_DBG_RE("Failed to get param: state.light(not [)\n")
				return NULL;
			}
		} else if (!strcmp(tok, "lightmodel")) {
			free(tok);
			tok = popFIFO((sArray*)newVar);
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get param: state.lightmodel(tok NULL)\n")
				return NULL;
			} else if (!strcmp(tok, "ambient")) {
				free(tok);
				char **r = (char**)calloc(2, sizeof(char*));
				r[0] = strdup("gl_LightModel.ambient");
				r[1] = NULL;
				return r;
			} else if (!strcmp(tok, "scenecolor")) {
				free(tok);
				char **r = (char**)calloc(2, sizeof(char*));
				r[0] = strdup("gl_FrontLightModelProduct.sceneColor");
				r[1] = NULL;
				return r;
			} else if (!strcmp(tok, "front")) {
				free(tok);
				tok = popFIFO((sArray*)newVar);
				if (!tok) {
					ARBCONV_DBG_RE("Failed to get param: state.lightmodel.front(tok NULL)\n")
					return NULL;
				} else if (!strcmp(tok, "scenecolor")) {
					free(tok);
					char **r = (char**)calloc(2, sizeof(char*));
					r[0] = strdup("gl_FrontLightModelProduct.sceneColor");
					r[1] = NULL;
					return r;
				} else {
					ARBCONV_DBG_RE("Failed to get param: state.lightmodel.front.%s\n", tok)
					free(tok);
					return NULL;
				}
			} else if (!strcmp(tok, "back")) {
				free(tok);
				tok = popFIFO((sArray*)newVar);
				if (!tok) {
					ARBCONV_DBG_RE("Failed to get param: state.lightmodel.back(tok NULL)\n")
					return NULL;
				} else if (!strcmp(tok, "scenecolor")) {
					free(tok);
					char **r = (char**)calloc(2, sizeof(char*));
					r[0] = strdup("gl_BackLightModelProduct.sceneColor");
					r[1] = NULL;
					return r;
				} else {
					ARBCONV_DBG_RE("Failed to get param: state.lightmodel.back.%s\n", tok)
					free(tok);
					return NULL;
				}
			} else {
				ARBCONV_DBG_RE("Failed to get param: state.lightmodel.%s\n", tok)
				free(tok);
				return NULL;
			}
		} else if (!strcmp(tok, "lightprod")) {
			free(tok);
			if (newVar->strParts[0][0] == '[') {
				free(popFIFO((sArray*)newVar));
				char *sln = popFIFO((sArray*)newVar);
				size_t slnLen = strlen(sln);
				
				if ((sln[0] >= '0') && (sln[0] <= '9')) {
					size_t propLen;
					const char *prop;
					
					if (newVar->strParts[0][0] != ']') {
						ARBCONV_DBG_RE("Failed to get param: state.lightprod[%s(not ])\n", sln)
						return NULL;
					}
					
					free(popFIFO((sArray*)newVar));
					tok = popFIFO((sArray*)newVar);
					if (!tok) {
						ARBCONV_DBG_RE("Failed to get param: state.material(tok NULL)\n")
						return NULL;
					} else if (!strcmp(tok, "front")) {
						free(tok);
						tok = popFIFO((sArray*)newVar);
						mtxNameLen = 20;
						matrixName = "gl_FrontLightProduct";
					} else if (!strcmp(tok, "back")) {
						free(tok);
						tok = popFIFO((sArray*)newVar);
						mtxNameLen = 19;
						matrixName = "gl_BackLightProduct";
					} else {
						mtxNameLen = 20;
						matrixName = "gl_FrontLightProduct";
					}
					
					if (!tok) {
						ARBCONV_DBG_RE("Failed to get param: [%s][%s](tok NULL)\n", matrixName, sln)
						return NULL;
					} else if (!strcmp(tok, "ambient")) {
						free(tok);
						propLen = 7;
						prop = "ambient";
					} else if (!strcmp(tok, "diffuse")) {
						free(tok);
						propLen = 7;
						prop = "diffuse";
					} else if (!strcmp(tok, "specular")) {
						free(tok);
						propLen = 8;
						prop = "specular";
					} else {
						ARBCONV_DBG_RE("Failed to get param: [%s][%s].%s\n", matrixName, sln, tok)
						free(tok);
						return NULL;
					}
					
					matrixNameMallocd = (char*)malloc((mtxNameLen + slnLen + propLen + 4) * sizeof(char));
					sprintf(matrixNameMallocd, "%s[%s].%s", matrixName, sln, prop);
					free(sln);
					char **r = (char**)calloc(2, sizeof(char*));;
					r[0] = matrixNameMallocd;
					r[1] = NULL;
					return r;
				} else {
					ARBCONV_DBG_RE("Failed to get param: state.lightprod.%s\n", sln)
					free(sln);
					return NULL;
				}
			} else {
				ARBCONV_DBG_RE("Failed to get param: state.lightprod(not [)\n")
				return NULL;
			}
		} else if (!strcmp(tok, "matrix")) {
			isMatrix = 1;
			
			free(tok);
			tok = popFIFO((sArray*)newVar);
			
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get param: state.matrix(tok NULL)\n")
				return NULL;
			} else if (!strcmp(tok, "modelview")) {
				free(tok);
				if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
					int mvmtx = 0;
					int mvmtxsz = 0;
					if (newVar->strParts[0][0] == '[') {
						free(popFIFO((sArray*)newVar));
						tok = popFIFO((sArray*)newVar);
						for (char *numPtr = tok; *numPtr; ++numPtr) {
							if ((*numPtr < '0') || (*numPtr > '9')) {
								ARBCONV_DBG_RE("Failed to get param: state.modelview[(NaN)\n")
								free(tok);
								return NULL;
							}
							++mvmtxsz;
							mvmtx = mvmtx * 10 + *numPtr - '0';
						}
						free(tok);
						
						tok = popFIFO((sArray*)newVar);
						if (tok[0] != ']') {
							ARBCONV_DBG_RE("Failed to get param: state.modelview[%d(not ])\n", mvmtx)
							free(tok);
							return NULL;
						}
						free(tok);
						
						if (mvmtx != 0) {
							ARBCONV_DBG_RE("Failed to get param: state.modelview[%d (!=0)]\n", mvmtx)
							return NULL;
						}
					}
					
					if (!newVar->strLen || IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
						matrixName = "gl_ModelViewMatrixTranspose";
						mtxNameLen = 27;
					} else if (!strcmp(newVar->strParts[0], "invtrans")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ModelViewMatrixInverse";
						mtxNameLen = 25;
					} else if (!strcmp(newVar->strParts[0], "inverse")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ModelViewMatrixInverseTranspose";
						mtxNameLen = 34;
					} else if (!strcmp(newVar->strParts[0], "transpose")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ModelViewMatrix";
						mtxNameLen = 18;
					} else {
						matrixName = "gl_ModelViewMatrixTranspose";
						mtxNameLen = 27;
					}
					
					if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
						tok = popFIFO((sArray*)newVar);
						if (!strcmp(tok, "row")
						 && ((newVar->strLen && (newVar->strParts[0][0] >= '0') && (newVar->strParts[0][0] <= '9'))
						     || ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '[')
						         && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')))) {
							free(tok);
							int freeLast = 0;
							if (newVar->strParts[0][0] == '[') {
								free(popFIFO((sArray*)newVar));
								freeLast = 1;
							}
							tok = popFIFO((sArray*)newVar);
							for (char *numPtr = tok; *numPtr; ++numPtr) {
								start = start * 10 + *numPtr - '0';
							}
							free(tok);
							
							if ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '.')
							 && (newVar->strParts[0][1] == '.')
							 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
								end = 0;
								free(popFIFO((sArray*)newVar));
								tok = popFIFO((sArray*)newVar);
								for (char *numPtr = tok; *numPtr; ++numPtr) {
									end = end * 10 + *numPtr - '0';
								}
								free(tok);
							} else {
								end = start;
							}
							
							if (freeLast) {
								if (newVar->strParts[0][0] != ']') {
									ARBCONV_DBG_RE("Failed to get param: [%s].row[%d..%d(not ])\n", matrixName, start, end)
									return NULL;
								}
								free(popFIFO((sArray*)newVar));
							}
						} else {
							ARBCONV_DBG_RE("Failed to get param: [%s].%s\n", matrixName, tok)
							free(tok);
							return NULL;
						}
					}
					
					/* if (mvmtxsz && mvmtx) {
						mtxNameLen += 2 + mvmtxsz;
						matrixNameMallocd = malloc((mtxNameLen + 1) * sizeof(char));
						sprintf(matrixNameMallocd, "%s", matrixName);
					} */
				} else {
					matrixName = "gl_ModelViewMatrixTranspose";
					mtxNameLen = 27;
				}
			} else if (!strcmp(tok, "projection")) {
				free(tok);
				if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
					if (!strcmp(newVar->strParts[0], "invtrans")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ProjectionMatrixInverse";
						mtxNameLen = 35;
					} else if (!strcmp(newVar->strParts[0], "inverse")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ProjectionMatrixInverseTranspose";
						mtxNameLen = 44;
					} else if (!strcmp(newVar->strParts[0], "transpose")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ProjectionMatrix";
						mtxNameLen = 28;
					} else {
						matrixName = "gl_ProjectionMatrixTranspose";
						mtxNameLen = 37;
					}
					
					if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
						tok = popFIFO((sArray*)newVar);
						if (!strcmp(tok, "row")
						 && (newVar->strLen >= 3) && (newVar->strParts[0][0] == '[')
						 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
							free(tok);
							free(popFIFO((sArray*)newVar));
							tok = popFIFO((sArray*)newVar);
							for (char *numPtr = tok; *numPtr; ++numPtr) {
								start = start * 10 + *numPtr - '0';
							}
							free(tok);
							
							if ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '.')
							 && (newVar->strParts[0][1] == '.')
							 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
								end = 0;
								free(popFIFO((sArray*)newVar));
								tok = popFIFO((sArray*)newVar);
								for (char *numPtr = tok; *numPtr; ++numPtr) {
									end = end * 10 + *numPtr - '0';
								}
								free(tok);
							} else {
								end = start;
							}
							
							tok = popFIFO((sArray*)newVar);
							if (tok[0] != ']') {
								ARBCONV_DBG_RE("Failed to get param: [%s].row[%d..%d(not ])\n", matrixName, start, end)
								free(tok);
								return NULL;
							}
							free(tok);
						} else {
							ARBCONV_DBG_RE("Failed to get param: [%s].%s\n", matrixName, tok)
							free(tok);
							return NULL;
						}
					}
				} else {
					matrixName = "gl_ProjectionMatrixTranspose";
					mtxNameLen = 37;
				}
			} else if (!strcmp(tok, "mvp")) {
				free(tok);
				if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
					if (!strcmp(newVar->strParts[0], "invtrans")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ModelViewProjectionMatrixInverse";
						mtxNameLen = 35;
					} else if (!strcmp(newVar->strParts[0], "inverse")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ModelViewProjectionMatrixInverseTranspose";
						mtxNameLen = 44;
					} else if (!strcmp(newVar->strParts[0], "transpose")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_ModelViewProjectionMatrix";
						mtxNameLen = 28;
					} else {
						matrixName = "gl_ModelViewProjectionMatrixTranspose";
						mtxNameLen = 37;
					}
					
					if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
						tok = popFIFO((sArray*)newVar);
						if (!strcmp(tok, "row")
						 && (newVar->strLen >= 3) && (newVar->strParts[0][0] == '[')
						 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
							free(tok);
							free(popFIFO((sArray*)newVar));
							tok = popFIFO((sArray*)newVar);
							for (char *numPtr = tok; *numPtr; ++numPtr) {
								start = start * 10 + *numPtr - '0';
							}
							free(tok);
							
							if ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '.')
							 && (newVar->strParts[0][1] == '.')
							 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
								end = 0;
								free(popFIFO((sArray*)newVar));
								tok = popFIFO((sArray*)newVar);
								for (char *numPtr = tok; *numPtr; ++numPtr) {
									end = end * 10 + *numPtr - '0';
								}
								free(tok);
							} else {
								end = start;
							}
							
							tok = popFIFO((sArray*)newVar);
							if (tok[0] != ']') {
								ARBCONV_DBG_RE("Failed to get param: [%s].row[%d..%d(not ])\n", matrixName, start, end)
								free(tok);
								return NULL;
							}
							free(tok);
						} else {
							ARBCONV_DBG_RE("Failed to get param: [%s].%s\n", matrixName, tok)
							free(tok);
							return NULL;
						}
					}
				} else {
					matrixName = "gl_ModelViewProjectionMatrixTranspose";
					mtxNameLen = 37;
				}
			} else if (!strcmp(tok, "texture")) {
				free(tok);
				if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
					int mvmtx = 0;
					int mvmtxsz = 0;
					if (newVar->strParts[0][0] == '[') {
						free(popFIFO((sArray*)newVar));
						tok = popFIFO((sArray*)newVar);
						for (char *numPtr = tok; *numPtr; ++numPtr) {
							if ((*numPtr < '0') || (*numPtr > '9')) {
								ARBCONV_DBG_RE("Failed to get param: state.texture[(NaN)\n")
								free(tok);
								return NULL;
							}
							++mvmtxsz;
							mvmtx = mvmtx * 10 + *numPtr - '0';
						}
						free(tok);
						
						tok = popFIFO((sArray*)newVar);
						if (tok[0] != ']') {
							ARBCONV_DBG_RE("Failed to get param: state.texture[%d(not ])\n", mvmtx)
							free(tok);
							return NULL;
						}
						free(tok);
					}
					
					if (!newVar->strLen || IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
						matrixName = "gl_TextureMatrixTranspose";
						mtxNameLen = 25;
					} else if (!strcmp(newVar->strParts[0], "invtrans")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_TextureMatrixInverse";
						mtxNameLen = 23;
					} else if (!strcmp(newVar->strParts[0], "inverse")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_TextureMatrixInverseTranspose";
						mtxNameLen = 32;
					} else if (!strcmp(newVar->strParts[0], "transpose")) {
						free(popFIFO((sArray*)newVar));
						matrixName = "gl_TextureMatrix";
						mtxNameLen = 16;
					} else {
						matrixName = "gl_TextureMatrixTranspose";
						mtxNameLen = 25;
					}
					
					if (newVar->strLen && !IS_NEW_STR_OR_SWIZZLE(newVar->strParts[0], type)) {
						tok = popFIFO((sArray*)newVar);
						if (!strcmp(tok, "row")
						 && newVar->strLen && (newVar->strParts[0][0] >= '0') && (newVar->strParts[0][0] <= '9')) {
							free(tok);
							tok = popFIFO((sArray*)newVar);
							for (char *numPtr = tok; *numPtr; ++numPtr) {
								start = start * 10 + *numPtr - '0';
							}
							free(tok);
							
							if ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '.')
							 && (newVar->strParts[0][1] == '.')
							 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
								end = 0;
								free(popFIFO((sArray*)newVar));
								tok = popFIFO((sArray*)newVar);
								for (char *numPtr = tok; *numPtr; ++numPtr) {
									end = end * 10 + *numPtr - '0';
								}
								free(tok);
							} else {
								end = start;
							}
						} else {
							ARBCONV_DBG_RE("Failed to get param: [%s].%s\n", matrixName, tok)
							free(tok);
							return NULL;
						}
					}
					
					mtxNameLen += 2 + (mvmtxsz ? mvmtxsz : 1);
					matrixNameMallocd = malloc((mtxNameLen + 1) * sizeof(char));
					sprintf(matrixNameMallocd, "%s[%d]", matrixName, mvmtx);
				} else {
					matrixName = "gl_TextureMatrixTranspose";
					mtxNameLen = 25;
				}
			} else {
				ARBCONV_DBG_RE("Failed to get param: state.matrix.%s\n", tok)
				free(tok);
				return NULL;
			}
		} else {
			ARBCONV_DBG_RE("Failed to get param: state.%s\n", tok)
			free(tok);
			return NULL;
		}
	} else if (!strcmp(tok, "program")) {
		refuseEndGE4 = 0;
		free(tok);
		tok = popFIFO((sArray*)newVar);
		
		if (!tok) {
			ARBCONV_DBG_RE("Failed to get param: program(tok NULL)\n")
			return NULL;
		} else if (!strcmp(tok, "env")) {
			matrixName = "gl_ProgramEnv";
			mtxNameLen = 13;
			isMatrix = 1;
			
			free(tok);
			tok = popFIFO((sArray*)newVar);
			
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get param: program.env(tok NULL)\n")
				return NULL;
			} else if (tok[0] == '[') {
				free(tok);
				tok = popFIFO((sArray*)newVar);
				if ((tok[0] >= '0') && (tok[0] <= '9')) {
					for (char *numPtr = tok; *numPtr; ++numPtr) {
						start = start * 10 + *numPtr - '0';
					}
					free(tok);
					
					if ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '.') && (newVar->strParts[0][1] == '.')
					 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
						end = 0;
						free(popFIFO((sArray*)newVar));
						tok = popFIFO((sArray*)newVar);
						for (char *numPtr = tok; *numPtr; ++numPtr) {
							end = end * 10 + *numPtr - '0';
						}
						free(tok);
					} else {
						end = start;
					}
					
					tok = popFIFO((sArray*)newVar);
					if (tok[0] != ']') {
						ARBCONV_DBG_RE("Failed to get param: program.env[%d..%d(not ])\n", start, end)
						free(tok);
						return NULL;
					}
					free(tok);
				} else {
					ARBCONV_DBG_RE("Failed to get param: program.env[%s\n", tok)
					free(tok);
					return NULL;
				}
			} else {
				ARBCONV_DBG_RE("Failed to get param: program.env.%s\n", tok)
				free(tok);
				return NULL;
			}
		} else if (!strcmp(tok, "local")) {
			matrixName = "gl_ProgramLocal";
			mtxNameLen = 15;
			isMatrix = 1;
			
			free(tok);
			tok = popFIFO((sArray*)newVar);
			
			if (!tok) {
				ARBCONV_DBG_RE("Failed to get param: program.local(tok NULL)\n")
				return NULL;
			} else if (tok[0] == '[') {
				free(tok);
				tok = popFIFO((sArray*)newVar);
				if ((tok[0] >= '0') && (tok[0] <= '9')) {
					for (char *numPtr = tok; *numPtr; ++numPtr) {
						start = start * 10 + *numPtr - '0';
					}
					free(tok);
					
					if ((newVar->strLen >= 3) && (newVar->strParts[0][0] == '.') && (newVar->strParts[0][1] == '.')
					 && (newVar->strParts[1][0] >= '0') && (newVar->strParts[1][0] <= '9')) {
						end = 0;
						free(popFIFO((sArray*)newVar));
						tok = popFIFO((sArray*)newVar);
						for (char *numPtr = tok; *numPtr; ++numPtr) {
							end = end * 10 + *numPtr - '0';
						}
						free(tok);
					} else {
						end = start;
					}
					
					tok = popFIFO((sArray*)newVar);
					if (tok[0] != ']') {
						ARBCONV_DBG_RE("Failed to get param: program.local[%d..%d(not ])\n", start, end)
						free(tok);
						return NULL;
					}
					free(tok);
				} else {
					ARBCONV_DBG_RE("Failed to get param: program.local[%s\n", tok)
					free(tok);
					return NULL;
				}
			} else {
				ARBCONV_DBG_RE("Failed to get param: program.local.%s\n", tok)
				free(tok);
				return NULL;
			}
		} else {
			ARBCONV_DBG_RE("Failed to get param: program.%s\n", tok)
			free(tok);
			return NULL;
		}
	} else if (tok[0] == '{') {
		int valuesCnt = 0;
		
		sCurStatus pseudoSt;
		pseudoSt.curValue.newVar.state = 0;
		pseudoSt.status = ST_VARIABLE_INIT;
		pseudoSt.outputString = malloc(DEFAULT_STRING_MALLOC_SIZE);
		pseudoSt.outputString[0] = 'v';
		pseudoSt.outputString[1] = 'e';
		pseudoSt.outputString[2] = 'c';
		pseudoSt.outputString[3] = '4';
		pseudoSt.outputString[4] = '(';
		pseudoSt.outputString[5] = '\0';
		pseudoSt.outputEnd = pseudoSt.outputString + 5;
		pseudoSt.outLen = 5;
		pseudoSt.outCap = DEFAULT_STRING_CAP;
		pseudoSt.outLeft = DEFAULT_STRING_CAP - 6;
		
		do {
			free(tok);
			tok = popFIFO((sArray*)newVar);
			pseudoSt.endOfToken = tok; // Yes, this is weird...
			readNextToken(&pseudoSt);
			switch (pseudoSt.curToken) {
			case TOK_SIGN:
				if ((pseudoSt.curValue.newVar.state % 3) || appendString(&pseudoSt, pseudoSt.tokInt ? "+" : "-", 1)) {
					pseudoSt.status = ST_ERROR;
					continue;
				}
				
				++pseudoSt.curValue.newVar.state;
				break;
				
			case TOK_FLOATCONST:
				if (pseudoSt.curValue.newVar.state % 3 == 2) {
					pseudoSt.status = ST_ERROR;
					continue;
				}
				
				if (appendString(&pseudoSt, tok, (size_t)-1)) {
					pseudoSt.status = ST_ERROR;
					continue;
				}
				++valuesCnt;
				pseudoSt.curValue.newVar.state = 3*(pseudoSt.curValue.newVar.state / 3) + 2;
				break;
				
			case TOK_COMMA:
				if ((pseudoSt.curValue.newVar.state % 3 != 2) || (pseudoSt.curValue.newVar.state > 8)) {
					pseudoSt.status = ST_ERROR;
					continue;
				}
				
				++pseudoSt.curValue.newVar.state;
				if (appendString(&pseudoSt, ", ", 2)) {
					pseudoSt.status = ST_ERROR;
					continue;
				}
				break;
				
			case TOK_RBRACE:
				if (pseudoSt.curValue.newVar.state % 3 != 2) {
					pseudoSt.status = ST_ERROR;
					continue;
				}
				break;
				
			case TOK_NULL:
			case TOK_WHITESPACE:
			case TOK_INTEGER:
			case TOK_IDENTIFIER:
			case TOK_POINT:
			case TOK_UPTO:
			case TOK_EQUALS:
			case TOK_LSQBRACKET:
			case TOK_RSQBRACKET:
			case TOK_LBRACE:
			case TOK_END_OF_INST:
			case TOK_LINE_COMMENT:
			case TOK_NEWLINE:
			case TOK_END:
			case TOK_UNKNOWN:
				pseudoSt.status = ST_ERROR;
				continue;
			}
		} while (tok && (pseudoSt.status != ST_ERROR) && (pseudoSt.curToken != TOK_RBRACE));
		free(tok);
		if (pseudoSt.status == ST_ERROR) {
			ARBCONV_DBG_RE("Failed to get param: %s tok (%s))\n", TOKEN2STR(pseudoSt.curToken), pseudoSt.outputString)
			free(pseudoSt.outputString);
			return NULL;
		}
		
		if (((((valuesCnt != 1) && (valuesCnt != 4))) || appendString(&pseudoSt, ")", 1))
		  && ( (valuesCnt != 2)                       || appendString(&pseudoSt, ", 0., 0.)", 9))
		  && ( (valuesCnt != 3)                       || appendString(&pseudoSt, ", 0.)", 5))) {
			free(pseudoSt.outputString);
			ARBCONV_DBG_RE("Failed to get param: not enough memory?\n")
			return NULL;
		}
		
		char **r = (char**)calloc(2, sizeof(char*));
		r[0] = pseudoSt.outputString;
		r[1] = NULL;
		return r;
	} else if ((tok[0] >= '0') && (tok[0] <= '9')) {
		// Scalar
		char **r = (char**)calloc(2, sizeof(char*));
		r[0] = (char*)calloc(13 + 4*strlen(tok), sizeof(char));
		r[1] = NULL;
		
		sprintf(r[0], "vec4(%s, %s, %s, %s)", tok, tok, tok, tok);
		
		return r;
	} else {
		ARBCONV_DBG_RE("Failed to get param: %s\n", tok)
		free(tok);
		return NULL;
	}
	/* TODO: (* todo, V done, ! todo but only in vertex/fragment shaders, ? todo?)
	 * <paramSingleItemDecl>
	 \ V "state" "." "material" "." "ambient"
	 \ V "state" "." "material" "." "diffuse"
	 \ V "state" "." "material" "." "specular"
	 \ V "state" "." "material" "." "emission"
	 \ V "state" "." "material" "." "shininess"
	 \ V "state" "." "material" "." "front" "." "ambient"
	 \ V "state" "." "material" "." "front" "." "diffuse"
	 \ V "state" "." "material" "." "front" "." "specular"
	 \ V "state" "." "material" "." "front" "." "emission"
	 \ V "state" "." "material" "." "front" "." "shininess"
	 \ V "state" "." "material" "." "back" "." "ambient"
	 \ V "state" "." "material" "." "back" "." "diffuse"
	 \ V "state" "." "material" "." "back" "." "specular"
	 \ V "state" "." "material" "." "back" "." "emission"
	 \ V "state" "." "material" "." "back" "." "shininess"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "diffuse" 
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "position"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "attenuation"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "spot" "." "direction"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "half"
	 \ V "state" "." "lightmodel" "." "ambient"
	 \ V "state" "." "lightmodel" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "front" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "back" "." "scenecolor"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "specular"
	 \ ! "state" "." "texgen" "." "eye" "." "s"
	 \ ! "state" "." "texgen" "." "eye" "." "t"
	 \ ! "state" "." "texgen" "." "eye" "." "r"
	 \ ! "state" "." "texgen" "." "eye" "." "q"
	 \ ! "state" "." "texgen" "." "object" "." "s"
	 \ ! "state" "." "texgen" "." "object" "." "t"
	 \ ! "state" "." "texgen" "." "object" "." "r"
	 \ ! "state" "." "texgen" "." "object" "." "q"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "s"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "t"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "r"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "q"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "s"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "t"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "r"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "q"
	 \ * "state" "." "fog" "." "color"
	 \ * "state" "." "fog" "." "params"
	 \ ! "state" "." "clip" "[" <stateClipPlaneNum> "]" "." "plane"
	 \ ! "state" "." "point" "." "size"
	 \ ! "state" "." "point" "." "attenuation"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> "]"
	 \ V <optionalSign> <floatConstant>
	 \ V "{" <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 * ---
	 * <paramSingleItemUse>
	 \ V "state" "." "material" "." "ambient"
	 \ V "state" "." "material" "." "diffuse"
	 \ V "state" "." "material" "." "specular"
	 \ V "state" "." "material" "." "emission"
	 \ V "state" "." "material" "." "shininess"
	 \ V "state" "." "material" "." "front" "." "ambient"
	 \ V "state" "." "material" "." "front" "." "diffuse"
	 \ V "state" "." "material" "." "front" "." "specular"
	 \ V "state" "." "material" "." "front" "." "emission"
	 \ V "state" "." "material" "." "front" "." "shininess"
	 \ V "state" "." "material" "." "back" "." "ambient"
	 \ V "state" "." "material" "." "back" "." "diffuse"
	 \ V "state" "." "material" "." "back" "." "specular"
	 \ V "state" "." "material" "." "back" "." "emission"
	 \ V "state" "." "material" "." "back" "." "shininess"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "diffuse" 
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "position"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "attenuation"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "spot" "." "direction"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "half"
	 \ V "state" "." "lightmodel" "." "ambient"
	 \ V "state" "." "lightmodel" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "front" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "back" "." "scenecolor"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "specular"
	 \ ! "state" "." "texgen" "." "eye" "." "s"
	 \ ! "state" "." "texgen" "." "eye" "." "t"
	 \ ! "state" "." "texgen" "." "eye" "." "r"
	 \ ! "state" "." "texgen" "." "eye" "." "q"
	 \ ! "state" "." "texgen" "." "object" "." "s"
	 \ ! "state" "." "texgen" "." "object" "." "t"
	 \ ! "state" "." "texgen" "." "object" "." "r"
	 \ ! "state" "." "texgen" "." "object" "." "q"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "s"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "t"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "r"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "q"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "s"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "t"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "r"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "q"
	 \ * "state" "." "fog" "." "color"
	 \ * "state" "." "fog" "." "params"
	 \ ! "state" "." "clip" "[" <stateClipPlaneNum> "]" "." "plane"
	 \ ! "state" "." "point" "." "size"
	 \ ! "state" "." "point" "." "attenuation"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> "]"
	 \ * <floatConstant>
	 \ V "{" <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 * ---
	 * <paramMultipleItem>
	 \ V "state" "." "material" "." "ambient"
	 \ V "state" "." "material" "." "diffuse"
	 \ V "state" "." "material" "." "specular"
	 \ V "state" "." "material" "." "emission"
	 \ V "state" "." "material" "." "shininess"
	 \ V "state" "." "material" "." "front" "." "ambient"
	 \ V "state" "." "material" "." "front" "." "diffuse"
	 \ V "state" "." "material" "." "front" "." "specular"
	 \ V "state" "." "material" "." "front" "." "emission"
	 \ V "state" "." "material" "." "front" "." "shininess"
	 \ V "state" "." "material" "." "back" "." "ambient"
	 \ V "state" "." "material" "." "back" "." "diffuse"
	 \ V "state" "." "material" "." "back" "." "specular"
	 \ V "state" "." "material" "." "back" "." "emission"
	 \ V "state" "." "material" "." "back" "." "shininess"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "diffuse" 
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "position"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "attenuation"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "spot" "." "direction"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "half"
	 \ V "state" "." "lightmodel" "." "ambient"
	 \ V "state" "." "lightmodel" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "front" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "back" "." "scenecolor"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "specular"
	 \ ! "state" "." "texgen" "." "eye" "." "s"
	 \ ! "state" "." "texgen" "." "eye" "." "t"
	 \ ! "state" "." "texgen" "." "eye" "." "r"
	 \ ! "state" "." "texgen" "." "eye" "." "q"
	 \ ! "state" "." "texgen" "." "object" "." "s"
	 \ ! "state" "." "texgen" "." "object" "." "t"
	 \ ! "state" "." "texgen" "." "object" "." "r"
	 \ ! "state" "." "texgen" "." "object" "." "q"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "s"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "t"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "r"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "eye" "." "q"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "s"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "t"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "r"
	 \ ! "state" "." "texgen" "[" <texCoordNum> "]" "." "object" "." "q"
	 \ * "state" "." "fog" "." "color"
	 \ * "state" "." "fog" "." "params"
	 \ ! "state" "." "clip" "[" <stateClipPlaneNum> "]" "." "plane"
	 \ ! "state" "." "point" "." "size"
	 \ ! "state" "." "point" "." "attenuation"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection"
	 \ V "state" "." "matrix" "." "projection" "." "inverse"
	 \ V "state" "." "matrix" "." "projection" "." "transpose"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture"
	 \ V "state" "." "matrix" "." "texture" "." "inverse"
	 \ V "state" "." "matrix" "." "texture" "." "transpose"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> ".." <progEnvParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> ".." <progLocalParamNum> "]"
	 \ V <optionalSign> <floatConstant>
	 \ V "{" <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 * ---
	 * <optionalSign>
	 \ * ""
	 \ * "-"
	 \ * "+"
	 * ---
	 * <stateLightNumber>
	 \ * <integer> from 0 to MAX_LIGHTS-1
	 * ---
	 * <stateClipPlaneNum>
	 \ * <integer> from 0 to MAX_CLIP_PLANES-1
	 * ---
	 * <stateMatrixRowNum>
	 \ * <integer> from 0 to 3
	 * ---
	 * <stateModMatNum>
	 \ * <integer> from 0 to MAX_VERTEX_UNITS_ARB-1
	 * ---
	 * <statePaletteMatNum>
	 \ * <integer> from 0 to MAX_PALETTE_MATRICES_ARB-1
	 * ---
	 * <stateProgramMatNum>
	 \ * <integer> from 0 to MAX_PROGRAM_MATRICES_ARB-1
	 * ---
	 * <progEnvParamNum>
	 \ * <integer> from 0 to MAX_PROGRAM_ENV_PARAMETERS_ARB - 1
	 * ---
	 * <progLocalParamNum>
	 \ * <integer> from 0 to MAX_PROGRAM_LOCAL_PARAMETERS_ARB - 1
	 * 
	 * ==========================================================
	 * 
	 * <paramSingleItemDecl>
	 \ V "state" "." "material" "." "ambient"
	 \ V "state" "." "material" "." "diffuse"
	 \ V "state" "." "material" "." "specular"
	 \ V "state" "." "material" "." "emission"
	 \ V "state" "." "material" "." "shininess"
	 \ V "state" "." "material" "." "front" "." "ambient"
	 \ V "state" "." "material" "." "front" "." "diffuse"
	 \ V "state" "." "material" "." "front" "." "specular"
	 \ V "state" "." "material" "." "front" "." "emission"
	 \ V "state" "." "material" "." "front" "." "shininess"
	 \ V "state" "." "material" "." "back" "." "ambient"
	 \ V "state" "." "material" "." "back" "." "diffuse"
	 \ V "state" "." "material" "." "back" "." "specular"
	 \ V "state" "." "material" "." "back" "." "emission"
	 \ V "state" "." "material" "." "back" "." "shininess"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "diffuse" 
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "position"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "attenuation"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "spot" "." "direction"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "half"
	 \ V "state" "." "lightmodel" "." "ambient"
	 \ V "state" "." "lightmodel" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "front" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "back" "." "scenecolor"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "specular"
	 \ ! "state" "." "texenv" "." "color"
	 \ ! "state" "." "texenv" "[" <legacyTexUnitNum> "]" "." "color"
	 \ * "state" "." "fog" "." "color"
	 \ * "state" "." "fog" "." "params"
	 \ ! "state" "." "depth" "." "range"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> "]"
	 \ V <optionalSign> <floatConstant>
	 \ V "{" <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 * ---
	 * <paramSingleItemUse>
	 \ V "state" "." "material" "." "ambient"
	 \ V "state" "." "material" "." "diffuse"
	 \ V "state" "." "material" "." "specular"
	 \ V "state" "." "material" "." "emission"
	 \ V "state" "." "material" "." "shininess"
	 \ V "state" "." "material" "." "front" "." "ambient"
	 \ V "state" "." "material" "." "front" "." "diffuse"
	 \ V "state" "." "material" "." "front" "." "specular"
	 \ V "state" "." "material" "." "front" "." "emission"
	 \ V "state" "." "material" "." "front" "." "shininess"
	 \ V "state" "." "material" "." "back" "." "ambient"
	 \ V "state" "." "material" "." "back" "." "diffuse"
	 \ V "state" "." "material" "." "back" "." "specular"
	 \ V "state" "." "material" "." "back" "." "emission"
	 \ V "state" "." "material" "." "back" "." "shininess"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "diffuse" 
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "position"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "attenuation"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "spot" "." "direction"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "half"
	 \ V "state" "." "lightmodel" "." "ambient"
	 \ V "state" "." "lightmodel" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "front" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "back" "." "scenecolor"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "specular"
	 \ ! "state" "." "texenv" "." "color"
	 \ ! "state" "." "texenv" "[" <legacyTexUnitNum> "]" "." "color"
	 \ * "state" "." "fog" "." "color"
	 \ * "state" "." "fog" "." "params"
	 \ ! "state" "." "depth" "." "range"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> "]"
	 \ * <floatConstant>
	 \ V "{" <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 * ---
	 * <paramMultipleItem>
	 \ V "state" "." "material" "." "ambient"
	 \ V "state" "." "material" "." "diffuse"
	 \ V "state" "." "material" "." "specular"
	 \ V "state" "." "material" "." "emission"
	 \ V "state" "." "material" "." "shininess"
	 \ V "state" "." "material" "." "front" "." "ambient"
	 \ V "state" "." "material" "." "front" "." "diffuse"
	 \ V "state" "." "material" "." "front" "." "specular"
	 \ V "state" "." "material" "." "front" "." "emission"
	 \ V "state" "." "material" "." "front" "." "shininess"
	 \ V "state" "." "material" "." "back" "." "ambient"
	 \ V "state" "." "material" "." "back" "." "diffuse"
	 \ V "state" "." "material" "." "back" "." "specular"
	 \ V "state" "." "material" "." "back" "." "emission"
	 \ V "state" "." "material" "." "back" "." "shininess"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "diffuse" 
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "position"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "attenuation"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "spot" "." "direction"
	 \ V "state" "." "light" "[" <stateLightNumber> "]" "." "half"
	 \ V "state" "." "lightmodel" "." "ambient"
	 \ V "state" "." "lightmodel" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "front" "." "scenecolor"
	 \ V "state" "." "lightmodel" "." "back" "." "scenecolor"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "front" "." "specular"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "ambient"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "diffuse"
	 \ V "state" "." "lightprod" "[" <stateLightNumber> "]" "." "back" "." "specular"
	 \ ! "state" "." "texenv" "." "color"
	 \ ! "state" "." "texenv" "[" <legacyTexUnitNum> "]" "." "color"
	 \ * "state" "." "fog" "." "color"
	 \ * "state" "." "fog" "." "params"
	 \ ! "state" "." "depth" "." "range"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans"
	 \ V "state" "." "matrix" "." "modelview" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "modelview" "[" <stateModMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection"
	 \ V "state" "." "matrix" "." "projection" "." "inverse"
	 \ V "state" "." "matrix" "." "projection" "." "transpose"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans"
	 \ V "state" "." "matrix" "." "projection" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "projection" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans"
	 \ V "state" "." "matrix" "." "mvp" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "mvp" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture"
	 \ V "state" "." "matrix" "." "texture" "." "inverse"
	 \ V "state" "." "matrix" "." "texture" "." "transpose"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans"
	 \ V "state" "." "matrix" "." "texture" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "state" "." "matrix" "." "texture" "[" <texCoordNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "palette" "[" <statePaletteMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "inverse" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "transpose" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ ? "state" "." "matrix" "." "program" "[" <stateProgramMatNum> "]" "." "invtrans" "." "row" "[" <stateMatrixRowNum> ".." <stateMatrixRowNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> "]"
	 \ V "program" "." "env" "[" <progEnvParamNum> ".." <progEnvParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> "]"
	 \ V "program" "." "local" "[" <progLocalParamNum> ".." <progLocalParamNum> "]"
	 \ V <optionalSign> <floatConstant>
	 \ V "{" <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 \ V "{" <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "," <optionalSign> <floatConstant> "}"
	 * ---
	 * <optionalSign>
	 \ * ""
	 \ * "-"
	 \ * "+"
	 * ---
	 * <stateLightNumber>
	 \ * <integer> from 0 to MAX_LIGHTS-1
	 * ---
	 * <legacyTexUnitNum>
	 \ * <integer> from 0 to MAX_TEXTURE_UNITS-1
	 * ---
	 * <stateMatrixRowNum>
	 \ * <integer> from 0 to 3
	 * ---
	 * <stateModMatNum>
	 \ * <integer> from 0 to MAX_VERTEX_UNITS_ARB-1
	 * ---
	 * <texCoordNum>
	 \ * <integer> from 0 to MAX_TEXTURE_COORDS_ARB-1
	 * ---
	 * <statePaletteMatNum>
	 \ * <integer> from 0 to MAX_PALETTE_MATRICES_ARB-1
	 * ---
	 * <stateProgramMatNum>
	 \ * <integer> from 0 to MAX_PROGRAM_MATRICES_ARB-1
	 * ---
	 * <progEnvParamNum>
	 \ * <integer> from 0 to MAX_PROGRAM_ENV_PARAMETERS_ARB - 1 
	 * ---
	 * <progLocalParamNum>
	 \ * <integer> from 0 to MAX_PROGRAM_LOCAL_PARAMETERS_ARB - 1 
	 */
	
	if (isMatrix) {
		if ((start > end) || ((end > 3) && refuseEndGE4)) {
			ARBCONV_DBG_RE("Failed to get param: [%s] (se)\n", matrixNameMallocd?matrixNameMallocd:matrixName)
			if (matrixNameMallocd) free(matrixNameMallocd);
			return NULL;
		}
		
		if (((type == 0) || (type == 1)) && (start != end)) {
			// type = 0 and 1 are paramSingleItem*
			ARBCONV_DBG_RE("Failed to get param: [%s] (t)\n", matrixNameMallocd?matrixNameMallocd:matrixName)
			if (matrixNameMallocd) free(matrixNameMallocd);
			return NULL;
		}
		
		if (matrixNameMallocd) {
			matrixName = matrixNameMallocd;
		}
		
		char **ret = (char**)calloc(end - start + 2, sizeof(char*));
		for (int i = start; i <= end; ++i) {
			char *buf = (char*)calloc(mtxNameLen + 13, sizeof(char)); // Assume 32-bit array = 11 digits max
			sprintf(buf, "%s[%d]", matrixName, i);
			ret[i - start] = buf;
		}
		ret[end - start + 1] = NULL;
		
		if (matrixNameMallocd) {
			free(matrixNameMallocd);
		}
		
		return ret;
	}
	
	return (char**)0xFFFFFFFFU; // Unreachable
}

#define FAIL(str) curStatusPtr->status = ST_ERROR; if (*error_msg) free(*error_msg); \
		*error_msg = strdup(str); return
void parseToken(sCurStatus* curStatusPtr, int vertex, char **error_msg, struct sSpecialCases *specialCases) {
	if (((curStatusPtr->curToken == TOK_UNKNOWN) && (curStatusPtr->status != ST_LINE_COMMENT))
		|| (curStatusPtr->curToken == TOK_NULL)) {
		FAIL("Unknown token");
	}
	
	// TODO: replace '$' in variable names with something else that is also valid in GLSL
	switch (curStatusPtr->status) {
	case ST_LINE_START:
		switch (curStatusPtr->curToken) {
		case TOK_IDENTIFIER: {
			char *tok = getToken(curStatusPtr);
			eInstruction inst;
			eVariableType vtype;
			if ((inst = STR2INST(tok, &curStatusPtr->curValue.newInst.inst.saturated)) != INST_UNK) {
				if (INSTTEX(inst) && vertex) {
					FAIL("Texture instructions are only valid in fragment shaders");
				}
				if (vertex && curStatusPtr->curValue.newInst.inst.saturated) {
					FAIL("Instruction cannot be saturated in ARB vertex shaders");
				}
				curStatusPtr->status = ST_INSTRUCTION;
				curStatusPtr->valueType = TYPE_INST_DECL;
				curStatusPtr->curValue.newInst.curArg = 0;
				curStatusPtr->curValue.newInst.state = 0;
				curStatusPtr->curValue.newInst.inst.type = inst;
				for (int i = 0; i < MAX_OPERANDS; ++i) {
					curStatusPtr->curValue.newInst.inst.vars[i].var = NULL;
					curStatusPtr->curValue.newInst.inst.vars[i].floatArrAddr = NULL;
					curStatusPtr->curValue.newInst.inst.vars[i].sign = 0;
					curStatusPtr->curValue.newInst.inst.vars[i].swizzle[0] = SWIZ_NONE;
					curStatusPtr->curValue.newInst.inst.vars[i].swizzle[1] = SWIZ_NONE;
					curStatusPtr->curValue.newInst.inst.vars[i].swizzle[2] = SWIZ_NONE;
					curStatusPtr->curValue.newInst.inst.vars[i].swizzle[3] = SWIZ_NONE;
				}
				curStatusPtr->curValue.newInst.inst.codeLocation = curStatusPtr->codePtr;
			} else if ((vtype = STR2VARTYPE(tok)) != VARTYPE_UNK) {
				if ((vtype == VARTYPE_ADDRESS) && !vertex) {
					FAIL("Addresses are only allowed in vertex shaders");
				} else if (vtype == VARTYPE_ALIAS) {
					curStatusPtr->status = ST_ALIAS;
					curStatusPtr->valueType = TYPE_ALIAS_DECL;
					curStatusPtr->curValue.string = NULL;
				} else {
					curStatusPtr->status = ST_VARIABLE;
					curStatusPtr->valueType = TYPE_VARIABLE_DECL;
					curStatusPtr->curValue.newVar.var = createVariable(vtype);
					curStatusPtr->curValue.newVar.state = 0;
					if ((vtype != VARTYPE_ADDRESS) && (vtype != VARTYPE_TEMP)) {
						initArray((sArray*)&curStatusPtr->curValue.newVar);
					}
				}
			} else if (!strcmp(tok, "OPTION")) {
				curStatusPtr->status = ST_OPTION;
				curStatusPtr->valueType = TYPE_OPTION_DECL;
				curStatusPtr->curValue.newOpt.optName = NULL;
			} else {
				free(tok);
				FAIL("Unknown operand");
			}
			free(tok);
			}
			break;
			
		case TOK_LINE_COMMENT:
			curStatusPtr->status = ST_LINE_COMMENT;
			break;
			
		case TOK_WHITESPACE:
		case TOK_NEWLINE:
			curStatusPtr->status = ST_LINE_START;
			break;
			
		case TOK_END:
			curStatusPtr->status = ST_DONE;
			return;
			
		default:
			FAIL("Invalid token");
		}
		break;
		
	case ST_LINE_COMMENT:
		if (curStatusPtr->curToken == TOK_NEWLINE) {
			curStatusPtr->status = ST_LINE_START;
		}
		break;
		
	case ST_VARIABLE:
		switch (curStatusPtr->curValue.newVar.var->type) {
		case VARTYPE_ADDRESS:
		case VARTYPE_TEMP:
			switch (curStatusPtr->curToken) {
			case TOK_IDENTIFIER: {
				if (curStatusPtr->curValue.newVar.state != 0) {
					// Already identified
					FAIL("Invalid state");
				}
				
				char *tok = getToken(curStatusPtr);
				if (kh_str_exist(curStatusPtr->varsMap, tok)) {
					// Identifier already exists
					free(tok);
					FAIL("Cannot redefine variable");
				}
				
				if (!strcmp(tok, "half")) {
					// Special case for the 'half' keyword
					pushArray((sArray*)curStatusPtr->curValue.newVar.var, strdup("gl4es_half"));
					
					// Hopefully this doesn't make a free-after-free in case of error (though it shouldn't)
					int ret;
					khint_t varIdx = kh_put(variables, curStatusPtr->varsMap, tok, &ret);
					if (ret < 0) {
						FAIL("Unknown error");
					}
					kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				}
				
				pushArray((sArray*)curStatusPtr->curValue.newVar.var, tok);
				curStatusPtr->curValue.newVar.state = 1;
				}
				break;
				
			case TOK_COMMA: {
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				int ret;
				khint_t varIdx = kh_put(
					variables,
					curStatusPtr->varsMap,
					curStatusPtr->curValue.newVar.var->names[0],
					&ret
				);
				if (ret < 0) {
					FAIL("Unknown error");
				}
				kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->curValue.newVar.var);
				curStatusPtr->curValue.newVar.var = createVariable(curStatusPtr->curValue.newVar.var->type);
				curStatusPtr->curValue.newVar.state = 0;
				}
				break;
				
			case TOK_END_OF_INST: {
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				int ret;
				khint_t varIdx = kh_put(
					variables,
					curStatusPtr->varsMap,
					curStatusPtr->curValue.newVar.var->names[0],
					&ret
				);
				if (ret < 0) {
					FAIL("Unknown error");
				}
				kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->curValue.newVar.var);
				curStatusPtr->valueType = TYPE_NONE;
				curStatusPtr->status = ST_LINE_START;
				}
				break;
				
			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			default:
				FAIL("Invalid token");
			}
			break;

		case VARTYPE_ATTRIB:
		case VARTYPE_OUTPUT:
		case VARTYPE_PARAM:
			switch (curStatusPtr->curToken) {
			case TOK_IDENTIFIER: {
				if (curStatusPtr->curValue.newVar.state != 0) {
					// Already identified
					FAIL("Invalid state");
				}
				
				char *tok = getToken(curStatusPtr);
				if (kh_str_exist(curStatusPtr->varsMap, tok)) {
					// Identifier already exists
					free(tok);
					FAIL("Cannot redefine variable");
				}
				
				if (!strcmp(tok, "half")) {
					// Special case for the 'half' keyword
					pushArray((sArray*)curStatusPtr->curValue.newVar.var, strdup("gl4es_half"));
					
					// Hopefully this doesn't make a free-after-free in case of error (though it shouldn't)
					int ret;
					khint_t varIdx = kh_put(variables, curStatusPtr->varsMap, tok, &ret);
					if (ret < 0) {
						FAIL("Unknown error");
					}
					kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				}
				
				pushArray((sArray*)curStatusPtr->curValue.newVar.var, tok);
				curStatusPtr->curValue.newVar.state = 1;
				}
				break;
				
			case TOK_EQUALS:
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				curStatusPtr->status = ST_VARIABLE_INIT;
				curStatusPtr->curValue.newVar.state = 0;
				break;
				
			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			case TOK_LSQBRACKET:
				if (curStatusPtr->curValue.newVar.var->type == VARTYPE_PARAM) {
					if (curStatusPtr->curValue.newVar.state != 1) {
						FAIL("Invalid state");
					}
					
					curStatusPtr->curValue.newVar.var->type = VARTYPE_PARAM_MULT;
					pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup(","));
					curStatusPtr->curValue.newVar.state = 0;
					break;
				}
				
				/* FALLTHROUGH */
			default:
				FAIL("Invalid token");
			}
			break;
			
		case VARTYPE_PARAM_MULT:
			switch (curStatusPtr->curToken) {
			case TOK_INTEGER:
				if (curStatusPtr->curValue.newVar.state != 0) {
					FAIL("Invalid state");
				}
				
				curStatusPtr->curValue.newVar.var->size = curStatusPtr->tokInt;
				curStatusPtr->curValue.newVar.state = 1;
				break;
				
			case TOK_EQUALS:
				if (curStatusPtr->curValue.newVar.state != 2) {
					FAIL("Invalid state");
				}
				
				curStatusPtr->status = ST_VARIABLE_INIT;
				curStatusPtr->curValue.newVar.state = 0;
				break;
				
			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			case TOK_RSQBRACKET:
				if (curStatusPtr->curValue.newVar.state == 2) {
					FAIL("Invalid state");
				}
				
				curStatusPtr->curValue.newVar.state = 2;
				
				break;
				
			default:
				FAIL("Invalid token");
			}
			break;
			
		case VARTYPE_ALIAS:
		case VARTYPE_CONST:
		case VARTYPE_TEXTURE:
		case VARTYPE_TEXTARGET:
		case VARTYPE_UNK:
			// Cannot happen (fallthrough?)
			FAIL("Unknown error (unintended fallthrough?)");
		}
		break;
	case ST_VARIABLE_INIT:
		switch (curStatusPtr->curValue.newVar.var->type) {
		case VARTYPE_ATTRIB:
			switch (curStatusPtr->curToken) {
			case TOK_INTEGER:
				if (curStatusPtr->curValue.newVar.state != 2) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 3;
				break;
				
			case TOK_IDENTIFIER: {
				if (curStatusPtr->curValue.newVar.state != 0) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				
				curStatusPtr->curValue.newVar.state = 1;
				}
				break;
				
			case TOK_POINT:
				if (curStatusPtr->curValue.newVar.state == 0) {
					FAIL("Invalid state");
				}
				curStatusPtr->curValue.newVar.state = 0;
				break;
				
			case TOK_LSQBRACKET:
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 2;
				break;
				
			case TOK_RSQBRACKET:
				if (curStatusPtr->curValue.newVar.state != 3) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 1;
				break;
				
			case TOK_END_OF_INST: {
				char *tok = NULL;
				
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				if (resolveAttrib(&curStatusPtr->curValue.newVar, vertex)) {
					FAIL("Not a valid attribute");
				}
				
				if ((tok = popFIFO((sArray*)&curStatusPtr->curValue.newVar))) {
					free(tok);
					FAIL("Not a valid attribute");
				}
				
				int ret;
				khint_t varIdx = kh_put(
					variables,
					curStatusPtr->varsMap,
					curStatusPtr->curValue.newVar.var->names[0],
					&ret
				);
				if (ret < 0) {
					FAIL("Unknown error");
				}
				kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->curValue.newVar.var);
				freeArray((sArray*)&curStatusPtr->curValue.newVar);
				curStatusPtr->valueType = TYPE_NONE;
				curStatusPtr->status = ST_LINE_START;
				}
				break;
				
			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			default:
				FAIL("Invalid token");
			}
			break;

		case VARTYPE_OUTPUT:
			switch (curStatusPtr->curToken) {
			case TOK_IDENTIFIER: {
				if (curStatusPtr->curValue.newVar.state != 0) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				
				curStatusPtr->curValue.newVar.state = 1;
				}
				break;
				
			case TOK_POINT:
				if (curStatusPtr->curValue.newVar.state == 0) {
					FAIL("Invalid state");
				}
				curStatusPtr->curValue.newVar.state = 0;
				break;
				
			case TOK_END_OF_INST: {
				char *tok = NULL;
				
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				if (resolveOutput(&curStatusPtr->curValue.newVar, vertex, specialCases)) {
					FAIL("Not a valid output");
				}
				
				if ((tok = popFIFO((sArray*)&curStatusPtr->curValue.newVar))) {
					free(tok);
					FAIL("Not a valid output");
				}
				
				int ret;
				khint_t varIdx = kh_put(
					variables,
					curStatusPtr->varsMap,
					curStatusPtr->curValue.newVar.var->names[0],
					&ret
				);
				if (ret < 0) {
					FAIL("Unknown error");
				}
				kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->curValue.newVar.var);
				freeArray((sArray*)&curStatusPtr->curValue.newVar);
				curStatusPtr->valueType = TYPE_NONE;
				curStatusPtr->status = ST_LINE_START;
				}
				break;
				
			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			default:
				FAIL("Invalid token");
			}
			break;
			
		case VARTYPE_PARAM:
			switch (curStatusPtr->curToken) {
			case TOK_SIGN:
				if ((curStatusPtr->curValue.newVar.state < 4) || (curStatusPtr->curValue.newVar.state > 10)
					|| (curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				break;
				
			case TOK_INTEGER:
				/* ... 0 works, too, in theory
				if (curStatusPtr->curValue.newVar.state != 2) {
					FAIL("Invalid state");
				} */
				if ((curStatusPtr->curValue.newVar.state > 10)
					|| (curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				if (curStatusPtr->curValue.newVar.state > 2) {
					size_t tokLen = getTokenLength(curStatusPtr);
					char *tok = (char*)malloc((tokLen + 2) * sizeof(char));
					copyToken(curStatusPtr, tok);
					tok[tokLen] = '.';
					tok[tokLen + 1] = '\0';
					pushArray((sArray*)&curStatusPtr->curValue.newVar, tok);
					++curStatusPtr->curValue.newVar.state;
				} else {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
					curStatusPtr->curValue.newVar.state = 3;
				}
				break;
				
			case TOK_FLOATCONST:
				if ((curStatusPtr->curValue.newVar.state == 2) || (curStatusPtr->curValue.newVar.state > 10)
					|| (curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				++curStatusPtr->curValue.newVar.state;
				break;
				
			case TOK_IDENTIFIER:
				if (curStatusPtr->curValue.newVar.state != 0) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 1;
				break;
				
			case TOK_POINT:
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				curStatusPtr->curValue.newVar.state = 0;
				break;
				
			case TOK_COMMA:
				if ((curStatusPtr->curValue.newVar.state < 5) || (curStatusPtr->curValue.newVar.state > 9)
					|| !(curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				++curStatusPtr->curValue.newVar.state;
				break;
				
			case TOK_LSQBRACKET:
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 2;
				break;
				
			case TOK_RSQBRACKET:
				if (curStatusPtr->curValue.newVar.state != 3) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 1;
				break;
				
			case TOK_LBRACE:
				if (curStatusPtr->curValue.newVar.state != 0) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 4;
				break;
			case TOK_RBRACE:
				if ((curStatusPtr->curValue.newVar.state != 5) && (curStatusPtr->curValue.newVar.state != 7)
					&& (curStatusPtr->curValue.newVar.state != 9) && (curStatusPtr->curValue.newVar.state != 11)) {
					FAIL("Invalid state");
				}
				
				if (curStatusPtr->curValue.newVar.state == 7) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup(","));
					pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup("0.0"));
					curStatusPtr->curValue.newVar.state += 2;
				}
				if (curStatusPtr->curValue.newVar.state == 9) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup(","));
					pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup("0.0"));
					curStatusPtr->curValue.newVar.state += 2;
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 12;
				break;
				
			case TOK_END_OF_INST: {
				if ((curStatusPtr->curValue.newVar.state != 1) && (curStatusPtr->curValue.newVar.state != 12)) {
					FAIL("Invalid state");
				}
				
				if (!curStatusPtr->curValue.newVar.strLen) {
					FAIL("No parameter given");
				}
				
				char **param = resolveParam(&curStatusPtr->curValue.newVar, vertex, 0);
				
				if (!param) {
					FAIL("Not a valid param");
				}
				
				if (curStatusPtr->curValue.newVar.strLen) {
					free(param);
					FAIL("Not a valid single param");
				}
				
				for (char **parm = param; *parm; ++parm) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar.var->init, *parm);
				}
				free(param);
				
				curStatusPtr->curValue.newVar.var->init.strings_total_len = -1;
				
				int ret;
				khint_t varIdx = kh_put(
					variables,
					curStatusPtr->varsMap,
					curStatusPtr->curValue.newVar.var->names[0],
					&ret
				);
				if (ret < 0) {
					FAIL("Unknown error");
				}
				kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->curValue.newVar.var);
				freeArray((sArray*)&curStatusPtr->curValue.newVar);
				curStatusPtr->valueType = TYPE_NONE;
				curStatusPtr->status = ST_LINE_START;
				}
				break;

			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			default:
				FAIL("Invalid token");
			}
			break;
			
		case VARTYPE_PARAM_MULT:
			switch (curStatusPtr->curToken) {
			case TOK_SIGN:
				if ((curStatusPtr->curValue.newVar.state < 6) || (curStatusPtr->curValue.newVar.state == 15)
					|| !(curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				break;
				
			case TOK_INTEGER:
				if ((curStatusPtr->curValue.newVar.state == 1) || (curStatusPtr->curValue.newVar.state == 15)
					|| !(curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				if (curStatusPtr->curValue.newVar.state < 6) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				} else {
					size_t tokLen = getTokenLength(curStatusPtr);
					char *tok = (char*)malloc((tokLen + 2) * sizeof(char));
					copyToken(curStatusPtr, tok);
					tok[tokLen] = '.';
					tok[tokLen + 1] = '\0';
					pushArray((sArray*)&curStatusPtr->curValue.newVar, tok);
				}
				++curStatusPtr->curValue.newVar.state;
				break;
				
			case TOK_FLOATCONST:
				if ((curStatusPtr->curValue.newVar.state < 6) || (curStatusPtr->curValue.newVar.state == 15)
					|| !(curStatusPtr->curValue.newVar.state % 2)) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				++curStatusPtr->curValue.newVar.state;
				break;
				
			case TOK_IDENTIFIER:
				if (curStatusPtr->curValue.newVar.state != 1) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 2;
				break;
				
			case TOK_POINT:
				if (curStatusPtr->curValue.newVar.state != 2) {
					FAIL("Invalid state");
				}
				
				curStatusPtr->curValue.newVar.state = 1;
				break;
				
			case TOK_UPTO:
				if (curStatusPtr->curValue.newVar.state != 4) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 5;
				
				break;
				
			case TOK_COMMA:
				if (curStatusPtr->curValue.newVar.state == 2) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
					curStatusPtr->curValue.newVar.state = 1;
				} else if ((curStatusPtr->curValue.newVar.state > 7) && !(curStatusPtr->curValue.newVar.state % 2)) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
					++curStatusPtr->curValue.newVar.state;
				} else {
					FAIL("Invalid state");
				}
				break;
				
			case TOK_LSQBRACKET:
				if (curStatusPtr->curValue.newVar.state != 2) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 3;
				break;
				
			case TOK_RSQBRACKET:
				if ((curStatusPtr->curValue.newVar.state != 4) && (curStatusPtr->curValue.newVar.state != 6)) {
					FAIL("Invalid state");
				}
				
				pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newVar.state = 2;
				break;
				
			case TOK_LBRACE:
				if (curStatusPtr->curValue.newVar.state == 0) {
					curStatusPtr->curValue.newVar.state = 1;
				} else if (curStatusPtr->curValue.newVar.state == 1) {
					pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
					curStatusPtr->curValue.newVar.state = 7;
				} else {
					FAIL("Invalid state");
				}
				break;
			case TOK_RBRACE:
				if (curStatusPtr->curValue.newVar.state == 2) {
					curStatusPtr->curValue.newVar.state = 15;
				} else if ((curStatusPtr->curValue.newVar.state > 7) && !(curStatusPtr->curValue.newVar.state % 2)) {
					while (curStatusPtr->curValue.newVar.state < 14) {
						pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup(","));
						pushArray((sArray*)&curStatusPtr->curValue.newVar, strdup("0."));
						curStatusPtr->curValue.newVar.state += 2;
					}
					pushArray((sArray*)&curStatusPtr->curValue.newVar, getToken(curStatusPtr));
					curStatusPtr->curValue.newVar.state = 2;
				} else {
					FAIL("Invalid state");
				}
				break;
				
			case TOK_END_OF_INST: {
				if (curStatusPtr->curValue.newVar.state != 15) {
					FAIL("Invalid state");
				}
				
				if (curStatusPtr->curValue.newVar.strLen == 1) {
					FAIL("No param given");
				}
				
				// Unknown total length... (and useless info anyway)
				curStatusPtr->curValue.newVar.var->init.strings_total_len = 0;
				do {
					// Remove the comma
					free(popFIFO((sArray*)&curStatusPtr->curValue.newVar));
					
					char **param = resolveParam(&curStatusPtr->curValue.newVar, vertex, 2);
					// If param is not resolved, fail
					if (!param) {
						curStatusPtr->status = ST_ERROR;
						break;
					}
					
					for (char **parm = param; *parm; ++parm) {
						ARBCONV_DBG_HEAVY(printf("Resolved param %p: ", *parm);fflush(stdout);printf("%s\n", *parm);)
						pushArray((sArray*)&curStatusPtr->curValue.newVar.var->init, *parm);
					}
					free(param);
					
					// Repeat until list empty OR not a comma
				} while (curStatusPtr->curValue.newVar.strLen
					&& (curStatusPtr->curValue.newVar.strParts[0][0] == ','));
				// Actually fail if failed
				if ((curStatusPtr->status == ST_ERROR) || curStatusPtr->curValue.newVar.strLen) {
					FAIL("Invalid param given");
				}
				
				curStatusPtr->curValue.newVar.var->init.strings_total_len -= 2;
				
				if (curStatusPtr->curValue.newVar.strLen) {
					// Is this even possible?
					FAIL("Too many arguments");
				}
				
				int ret;
				khint_t varIdx = kh_put(
					variables,
					curStatusPtr->varsMap,
					curStatusPtr->curValue.newVar.var->names[0],
					&ret
				);
				if (ret < 0) {
					FAIL("Unknown error");
				}
				kh_val(curStatusPtr->varsMap, varIdx) = curStatusPtr->curValue.newVar.var;
				pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->curValue.newVar.var);
				freeArray((sArray*)&curStatusPtr->curValue.newVar);
				curStatusPtr->valueType = TYPE_NONE;
				curStatusPtr->status = ST_LINE_START;
				}
				break;

			case TOK_WHITESPACE:
			case TOK_NEWLINE:
				break;
				
			default:
				FAIL("Invalid token");
			}
			break;

		case VARTYPE_ADDRESS:
		case VARTYPE_TEMP:
		case VARTYPE_ALIAS:
		case VARTYPE_CONST:
		case VARTYPE_TEXTURE:
		case VARTYPE_TEXTARGET:
		case VARTYPE_UNK:
			// Cannot happen (fallthrough?)
			FAIL("Unknown error (unintended fallthrough?)");
		}
		break;

	case ST_ALIAS:
		switch (curStatusPtr->curToken) {
		case TOK_IDENTIFIER: {
			if (curStatusPtr->curValue.string) {
				// Already have a string
				FAIL("Too many names");
			}
			
			char *tok = getToken(curStatusPtr);
			if (kh_str_exist(curStatusPtr->varsMap, tok)) {
				// Identifier already exists
				free(tok);
				FAIL("Cannot redeclare variable");
			}
			curStatusPtr->curValue.string = tok;
			}
			break;
			
		case TOK_WHITESPACE:
		case TOK_NEWLINE:
			break;
			
		case TOK_EQUALS:
			curStatusPtr->status = ST_ALIASING;
			break;
			
		default:
			FAIL("Unknown token");
		}
		break;
	case ST_ALIASING:
		switch (curStatusPtr->curToken) {
		case TOK_IDENTIFIER: {
			if (curStatusPtr->valueType != TYPE_ALIAS_DECL) {
				// Already aliased
				FAIL("Too many identifiers");
			}
			
			char *tok = getToken(curStatusPtr);
			khint_t varIdx = kh_get(variables, curStatusPtr->varsMap, tok);
			free(tok);
			if (!kh_truly_exist(curStatusPtr->varsMap, varIdx)) {
				// Aliasing to empty space
				FAIL("Cannot alias to inexistant variable");
			}
			
			sVariable *var = kh_val(curStatusPtr->varsMap, varIdx);
			pushArray((sArray*)var, curStatusPtr->curValue.string);
			
			int ret;
			varIdx = kh_put(variables, curStatusPtr->varsMap, curStatusPtr->curValue.string, &ret);
			if (ret < 0) {
				FAIL("Unknown error");
			}
			kh_val(curStatusPtr->varsMap, varIdx) = var;
			
			curStatusPtr->valueType = TYPE_NONE;
			}
			break;

		case TOK_WHITESPACE:
		case TOK_NEWLINE:
			break;
			
		case TOK_END_OF_INST:
			if (curStatusPtr->valueType == TYPE_NONE) {
				curStatusPtr->status = ST_LINE_START;
			} else {
				FAIL("No alias target");
			}
			break;
			
		default:
			FAIL("Invalid token");
		}
		break;
		
	case ST_INSTRUCTION: {
		// States
#define STATE_START 0
#define STATE_AFTER_SIGN 1
#define STATE_AFTER_VALID 2
#define STATE_AFTER_VALID_LSQBR_START 3
#define STATE_AFTER_VALID_LSQBR_ADDR 4
#define STATE_AFTER_VALID_LSQBR_ADOT 5
#define STATE_AFTER_VALID_LSQBR_ADOK 6
#define STATE_AFTER_VALID_LSQBR_SIGN 7
#define STATE_AFTER_VALID_LSQBR_END 8
#define STATE_AFTER_VALID_RSQBR 9
#define STATE_AFTER_VALID_DOT 10
#define STATE_AFTER_ELEMENT 11
#define STATE_AFTER_DOT 12
#define STATE_AFTER_TEXSPLINT 13
#define STATE_AFTER_TEXSAMPLER 14
#define STATE_AFTER_NUMBER 15
#define STATE_LSQBR_START 16
#define STATE_LSQBR_END 17
#define STATE_LBRACE 18
#define STATE_LBRACE_NUM1 19
#define STATE_LBRACE_COM1 20
#define STATE_LBRACE_NUM2 21
#define STATE_LBRACE_COM2 22
#define STATE_LBRACE_NUM3 23
#define STATE_LBRACE_COM3 24
#define STATE_LBRACE_NUM4 25
#define STATE_RBRACE 26
#define STATE_AFTER_SWIZZLE -1
		
		sInstruction_Vars *curVarPtr = &curStatusPtr->curValue.newInst.inst.vars[curStatusPtr->curValue.newInst.curArg];
		int texSampler = INSTTEX(curStatusPtr->curValue.newInst.inst.type)
				 && (curStatusPtr->curValue.newInst.curArg == 3);
		switch (curStatusPtr->curToken) {
		case TOK_WHITESPACE:
		case TOK_NEWLINE:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_AFTER_VALID_LSQBR_ADDR:
			case STATE_AFTER_VALID_LSQBR_ADOT:
			case STATE_AFTER_VALID_LSQBR_ADOK:
			case STATE_AFTER_VALID_LSQBR_SIGN: {
				char *faa = (char*)realloc(
					curVarPtr->floatArrAddr,
					(strlen(curVarPtr->floatArrAddr) + getTokenLength(curStatusPtr) + 1) * sizeof(char)
				);
				if (!faa) {
					FAIL("Failed to realloc (out of memory?)");
				}
				copyToken(curStatusPtr, faa + strlen(faa));
				curVarPtr->floatArrAddr = faa;
				}
				break;
				
			default:
				break;
			}
			break;
			
		case TOK_SIGN:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_START:
				curVarPtr->sign = curStatusPtr->tokInt ? 1 : -1;
				curStatusPtr->curValue.newInst.state = STATE_AFTER_SIGN;
				break;
				
			case STATE_AFTER_VALID_LSQBR_ADOK: {
				char *faa = (char*)realloc(
					curVarPtr->floatArrAddr,
					(strlen(curVarPtr->floatArrAddr) + getTokenLength(curStatusPtr) + 1) * sizeof(char)
				);
				if (!faa) {
					FAIL("Failed to realloc (out of memory?)");
				}
				copyToken(curStatusPtr, faa + strlen(faa));
				curVarPtr->floatArrAddr = faa;
				++curStatusPtr->curValue.newInst.state;
				}
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_INTEGER:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_START:
				if (texSampler) {
					if ((curStatusPtr->tokInt < 1) || (curStatusPtr->tokInt > 3)) {
						FAIL("Invalid texture sampler");
					}
					curStatusPtr->curValue.newInst.state = STATE_AFTER_TEXSPLINT;
					break;
				}
				
				/* FALLTHROUGH */
			case STATE_AFTER_SIGN:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_AFTER_NUMBER;
				break;
				
			case STATE_AFTER_VALID_LSQBR_START:
				curVarPtr->floatArrAddr = getToken(curStatusPtr);
				curStatusPtr->curValue.newInst.state = STATE_AFTER_VALID_LSQBR_END;
				break;
				
			case STATE_LSQBR_START:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_LSQBR_END;
				break;
				
			case STATE_AFTER_VALID_LSQBR_SIGN: {
				char *faa = (char*)realloc(
					curVarPtr->floatArrAddr,
					(strlen(curVarPtr->floatArrAddr) + getTokenLength(curStatusPtr) + 1) * sizeof(char)
				);
				if (!faa) {
					FAIL("Failed to realloc (out of memory?)");
				}
				copyToken(curStatusPtr, faa + strlen(faa));
				curVarPtr->floatArrAddr = faa;
				curStatusPtr->curValue.newInst.state = STATE_AFTER_VALID_LSQBR_END;
				}
				break;
				
			case STATE_LBRACE:
			case STATE_LBRACE_COM1:
			case STATE_LBRACE_COM2:
			case STATE_LBRACE_COM3:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				++curStatusPtr->curValue.newInst.state;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_FLOATCONST:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_START:
			case STATE_AFTER_SIGN: {
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_AFTER_NUMBER;
				}
				break;
				
			case STATE_LBRACE:
			case STATE_LBRACE_COM1:
			case STATE_LBRACE_COM2:
			case STATE_LBRACE_COM3:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				++curStatusPtr->curValue.newInst.state;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_IDENTIFIER: {
			char *tok = getToken(curStatusPtr);
			
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_START:
				if (texSampler) {
					if (getTokenLength(curStatusPtr) != 4) {
						FAIL("Invalid texture sampler");
					} else if (!strncmp(curStatusPtr->codePtr, "CUBE", 4)) {
						curVarPtr->var = curStatusPtr->texCUBE;
					} else if (!strncmp(curStatusPtr->codePtr, "RECT", 4)) {
						curVarPtr->var = curStatusPtr->texRECT;
					} else {
						FAIL("Invalid texture sampler");
					}
					curStatusPtr->curValue.newInst.state = STATE_AFTER_TEXSAMPLER;
					break;
				}
				
				/* FALLTHROUGH */
			case STATE_AFTER_SIGN: {
				khint_t idx = kh_get(variables, curStatusPtr->varsMap, tok);
				
				if (kh_truly_exist(curStatusPtr->varsMap, idx)) {
					curVarPtr->var = kh_val(curStatusPtr->varsMap, idx);
					curStatusPtr->curValue.newInst.state = STATE_AFTER_VALID;
				} else {
					pushArray((sArray*)&curStatusPtr->_fixedNewVar, strdup(tok));
					curStatusPtr->curValue.newInst.state = STATE_AFTER_ELEMENT;
				}
				}
				break;
				
			case STATE_AFTER_DOT:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, strdup(tok));
				curStatusPtr->curValue.newInst.state = STATE_AFTER_ELEMENT;
				break;
				
			case STATE_AFTER_VALID_DOT: {
				if (getTokenLength(curStatusPtr) > 4) {
					FAIL("Swizzle too long");
				}
				int e = 0;
				for (int i = 0; !e && (tok[i] != '\0'); ++i) {
					switch (tok[i]) {
					case 'r': case 'x':
						curVarPtr->swizzle[i] = SWIZ_X;
						break;
					case 'g': case 'y':
						curVarPtr->swizzle[i] = SWIZ_Y;
						break;
					case 'b': case 'z':
						curVarPtr->swizzle[i] = SWIZ_Z;
						break;
					case 'a': case 'w':
						curVarPtr->swizzle[i] = SWIZ_W;
						break;
					default:
						e = 1; break;
					}
				}
				if (e) {
					free(tok);
					FAIL("Invalid swizzle value");
				}
				
				curStatusPtr->curValue.newInst.state = STATE_AFTER_SWIZZLE;
				}
				break;
				
			case STATE_AFTER_VALID_LSQBR_START: {
				khint_t idx = kh_get(variables, curStatusPtr->varsMap, tok);
				if (!kh_truly_exist(curStatusPtr->varsMap, idx)) {
					free(tok);
					FAIL("Invalid relative addressing (not a declared address)");
				}
				sVariable *var = kh_val(curStatusPtr->varsMap, idx);
				if (var->type != VARTYPE_ADDRESS) {
					free(tok);
					FAIL("Invalid relative addressing (not an address)");
				}
				
				curVarPtr->floatArrAddr = getToken(curStatusPtr);
				++curStatusPtr->curValue.newInst.state;
				}
				break;
				
			case STATE_AFTER_VALID_LSQBR_ADOT: {
				if ((getTokenLength(curStatusPtr) != 1) || (tok[0] != 'x')) {
					free(tok);
					FAIL("Invalid address mask");
				}
				
				char *faa = (char*)realloc(
					curVarPtr->floatArrAddr,
					(strlen(curVarPtr->floatArrAddr) + getTokenLength(curStatusPtr) + 1) * sizeof(char)
				);
				if (!faa) {
					FAIL("Failed to realloc (out of memory?)");
				}
				copyToken(curStatusPtr, faa + strlen(faa));
				curVarPtr->floatArrAddr = faa;
				
				++curStatusPtr->curValue.newInst.state;
				break;
			}
				
			case STATE_AFTER_TEXSPLINT:
				if (texSampler) {
					if ((getTokenLength(curStatusPtr) != 1) || strncmp(curStatusPtr->codePtr, "D", 1)) {
						FAIL("Invalid texture sampler");
					} else {
						switch (curStatusPtr->tokInt) {
						case 1:
							curVarPtr->var = curStatusPtr->tex1D;
							break;
						case 2:
							curVarPtr->var = curStatusPtr->tex2D;
							break;
						case 3:
							curVarPtr->var = curStatusPtr->tex3D;
							break;
							
						default:
							FAIL("Invalid texture sampler (shouldn't happen here)");
							break;
						}
					}
					curStatusPtr->curValue.newInst.state = STATE_AFTER_TEXSAMPLER;
					break;
				}
				
				/* FALLTHROUGH */
			default:
				free(tok);
				FAIL("Invalid state");
			}
			
			free(tok);
			break;
		}
			
		case TOK_POINT:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_AFTER_VALID:
			case STATE_AFTER_VALID_RSQBR:
				curStatusPtr->curValue.newInst.state = STATE_AFTER_VALID_DOT;
				break;
				
			case STATE_AFTER_VALID_LSQBR_ADDR: {
				char *faa = (char*)realloc(
					curVarPtr->floatArrAddr,
					(strlen(curVarPtr->floatArrAddr) + getTokenLength(curStatusPtr) + 1) * sizeof(char)
				);
				if (!faa) {
					FAIL("Failed to realloc (out of memory?)");
				}
				copyToken(curStatusPtr, faa + strlen(faa));
				curVarPtr->floatArrAddr = faa;
				
				++curStatusPtr->curValue.newInst.state;
				}
				break;
				
			case STATE_AFTER_ELEMENT:
				curStatusPtr->curValue.newInst.state = STATE_AFTER_DOT;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_LSQBRACKET:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_AFTER_VALID:
				curStatusPtr->curValue.newInst.state = STATE_AFTER_VALID_LSQBR_START;
				break;
				
			case STATE_AFTER_ELEMENT:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_LSQBR_START;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_RSQBRACKET:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_AFTER_VALID_LSQBR_END:
				curStatusPtr->curValue.newInst.state = STATE_AFTER_VALID_RSQBR;
				break;
				
			case STATE_LSQBR_END:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_AFTER_ELEMENT;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_LBRACE:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_START:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_LBRACE;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_RBRACE:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_LBRACE_NUM1:
			case STATE_LBRACE_NUM2:
			case STATE_LBRACE_NUM3:
			case STATE_LBRACE_NUM4:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				curStatusPtr->curValue.newInst.state = STATE_RBRACE;
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		case TOK_END_OF_INST:
			if (INSTTEX(curStatusPtr->curValue.newInst.inst.type)) {
				if (curStatusPtr->curValue.newInst.curArg != 3) {
					FAIL("Not enough arguments for texture instruction");
				}
				if (curStatusPtr->curValue.newInst.state != STATE_AFTER_TEXSAMPLER) {
					FAIL("Invalid texture instruction");
				}
				
				pushArray(
					(sArray*)&curStatusPtr->instructions,
					copyInstruction(&curStatusPtr->curValue.newInst.inst)
				);
				curStatusPtr->valueType = TYPE_NONE;
				curStatusPtr->status = ST_LINE_START;
				break;
			}
			
			/* FALLTHROUGH */
		case TOK_COMMA:
			switch (curStatusPtr->curValue.newInst.state) {
			case STATE_LBRACE_NUM1:
			case STATE_LBRACE_NUM2:
			case STATE_LBRACE_NUM3:
			case STATE_LBRACE_NUM4:
				pushArray((sArray*)&curStatusPtr->_fixedNewVar, getToken(curStatusPtr));
				++curStatusPtr->curValue.newInst.state;
				return;
				
			case STATE_AFTER_ELEMENT:
			case STATE_AFTER_NUMBER: // Should be able to go
			case STATE_RBRACE:       // directly to the resolveParam part...
				if (INSTTEX(curStatusPtr->curValue.newInst.inst.type)
				 && (curStatusPtr->curValue.newInst.curArg == 2)) {
					if ((curStatusPtr->_fixedNewVar.strLen != 4)
					 || (curStatusPtr->_fixedNewVar.strParts[1][0] != '[')
					 || (curStatusPtr->_fixedNewVar.strParts[3][0] != ']')
					 || strcmp(curStatusPtr->_fixedNewVar.strParts[0], "texture")) {
						FAIL("Invalid texture instruction");
					}
					
					unsigned int id = 0;
					
					char *idPtr = curStatusPtr->_fixedNewVar.strParts[2];
					while ((*idPtr >= '0') && (*idPtr <= '9')) {
						id = id * 10 + *idPtr - '0';
						++idPtr;
					}
					if (*idPtr != '\0') {
						FAIL("Invalid texture ID");
					}
					
					if (id > MAX_TEX) {
						FAIL("Invalid texture ID (ID too big)");
					}
					
					if (!curStatusPtr->texVars[id]->size) {
						pushArray((sArray*)curStatusPtr->texVars[id], strdup(curStatusPtr->_fixedNewVar.strParts[2]));
					}
					
					curVarPtr->var = curStatusPtr->texVars[id];
					
					freeArray((sArray*)&curStatusPtr->_fixedNewVar);
					initArray((sArray*)&curStatusPtr->_fixedNewVar);
					
					if (curStatusPtr->curToken != TOK_COMMA) {
						FAIL("Invalid texture instruction");
					}
					
					curStatusPtr->curValue.newInst.state = STATE_START;
					++curStatusPtr->curValue.newInst.curArg;
					break;
				} else {
					int failure;
					curStatusPtr->_fixedNewVar.var = createVariable(VARTYPE_CONST);
					
					if ((vertex && !strcmp(curStatusPtr->_fixedNewVar.strParts[0], "vertex"))
					 || (!vertex && !strcmp(curStatusPtr->_fixedNewVar.strParts[0], "fragment"))) {
						failure = resolveAttrib(&curStatusPtr->_fixedNewVar, vertex);
					} else if (!strcmp(curStatusPtr->_fixedNewVar.strParts[0], "result")) {
						failure = resolveOutput(&curStatusPtr->_fixedNewVar, vertex, specialCases);
					} else {
						char **resolved = resolveParam(&curStatusPtr->_fixedNewVar, vertex, 1);
						
						if (!resolved) {
							deleteVariable(&curStatusPtr->_fixedNewVar.var);
							FAIL("Invalid value (implicit param?)");
						}
						failure = 0;
						
						for (char **resvd = resolved; *resvd; ++resvd) {
							pushArray((sArray*)&curStatusPtr->_fixedNewVar.var->init, *resvd);
						}
						free(resolved);
					}
					
					if (failure) {
						deleteVariable(&curStatusPtr->_fixedNewVar.var);
						FAIL("Invalid value (implicit attrib or output)");
					}
					
					if (curStatusPtr->_fixedNewVar.strLen) {
						if (!IS_SWIZZLE(curStatusPtr->_fixedNewVar.strParts[0])
						 || (curStatusPtr->_fixedNewVar.strLen > 1)) {
							// Too many elements
							deleteVariable(&curStatusPtr->_fixedNewVar.var);
							FAIL("Invalid value");
						}
						
						// Swizzle
						int e = 0;
						char *swiz = popArray((sArray*)&curStatusPtr->_fixedNewVar);
						for (int i = 0; !e && (swiz[i] != '\0'); ++i) {
							if (i > 3) {
								e = 1;
								break;
							}
							switch (swiz[i]) {
							case 'r': case 'x':
								curVarPtr->swizzle[i] = SWIZ_X;
								break;
							case 'g': case 'y':
								curVarPtr->swizzle[i] = SWIZ_Y;
								break;
							case 'b': case 'z':
								curVarPtr->swizzle[i] = SWIZ_Z;
								break;
							case 'a': case 'w':
								curVarPtr->swizzle[i] = SWIZ_W;
								break;
							default:
								e = 1; continue;
							}
						}
						free(swiz);
						if (e) {
							deleteVariable(&curStatusPtr->_fixedNewVar.var);
							FAIL("Invalid swizzle");
						}
					}
					
					pushArray((sArray*)&curStatusPtr->variables, curStatusPtr->_fixedNewVar.var);
					curVarPtr->var = curStatusPtr->_fixedNewVar.var;
					
					freeArray((sArray*)&curStatusPtr->_fixedNewVar);
					initArray((sArray*)&curStatusPtr->_fixedNewVar);
					curStatusPtr->_fixedNewVar.var = NULL;
				}
				
				/* FALLTHROUGH */
			case STATE_AFTER_VALID:
			case STATE_AFTER_VALID_RSQBR:
			case STATE_AFTER_SWIZZLE:
				if (curStatusPtr->curToken == TOK_COMMA) {
					curStatusPtr->curValue.newInst.state = STATE_START;
					++curStatusPtr->curValue.newInst.curArg;
					if (curStatusPtr->curValue.newInst.curArg >= MAX_OPERANDS) {
						FAIL("Too many operands");
					}
				} else {
					if (INSTTEX(curStatusPtr->curValue.newInst.inst.type)) {
						FAIL("Invalid texture instruction");
					}
					
					pushArray(
						(sArray*)&curStatusPtr->instructions,
						copyInstruction(&curStatusPtr->curValue.newInst.inst)
					);
					curStatusPtr->valueType = TYPE_NONE;
					curStatusPtr->status = ST_LINE_START;
				}
				
				break;
				
			default:
				FAIL("Invalid state");
			}
			break;
			
		default:
			FAIL("Invalid token");
		}
		break;
		
#undef STATE_START
#undef STATE_AFTER_SIGN
#undef STATE_AFTER_VALID
#undef STATE_AFTER_VALID_LSQBR
#undef STATE_AFTER_VALID_LSQBRNUM
#undef STATE_AFTER_VALID_RSQBR
#undef STATE_AFTER_VALID_DOT
#undef STATE_AFTER_ELEMENT
#undef STATE_AFTER_DOT
#undef STATE_AFTER_TEXSPLINT
#undef STATE_AFTER_TEXSAMPLER
#undef STATE_AFTER_NUMBER
#undef STATE_LSQBR_START
#undef STATE_LSQBR_END
#undef STATE_LBRACE
#undef STATE_LBRACE_NUM1
#undef STATE_LBRACE_COM1
#undef STATE_LBRACE_NUM2
#undef STATE_LBRACE_COM2
#undef STATE_LBRACE_NUM3
#undef STATE_LBRACE_COM3
#undef STATE_LBRACE_NUM4
#undef STATE_RBRACE
#undef STATE_AFTER_SWIZZLE
	}
		
	case ST_OPTION:
		switch (curStatusPtr->curToken) {
		case TOK_IDENTIFIER:
			if (curStatusPtr->curValue.newOpt.optName) {
				FAIL("Too many options");
			}
			
			curStatusPtr->curValue.newOpt.optName = getToken(curStatusPtr);
			break;
			
		case TOK_WHITESPACE:
		case TOK_NEWLINE:
			break;
			
		case TOK_END_OF_INST:
			if (!strcmp(curStatusPtr->curValue.newOpt.optName, "ARB_precision_hint_fastest")) {
				// Nothing to do
			} else if (!strcmp(curStatusPtr->curValue.newOpt.optName, "ARB_precision_hint_nicest")) {
				// Nothing to do
			} else if (!vertex && !strcmp(curStatusPtr->curValue.newOpt.optName, "ARB_fog_exp")) {
				curStatusPtr->fogType = FOG_EXP;
			} else if (!vertex && !strcmp(curStatusPtr->curValue.newOpt.optName, "ARB_fog_exp2")) {
				curStatusPtr->fogType = FOG_EXP2;
			} else if (!vertex && !strcmp(curStatusPtr->curValue.newOpt.optName, "ARB_fog_linear")) {
				curStatusPtr->fogType = FOG_LINEAR;
			} else if (vertex && !strcmp(curStatusPtr->curValue.newOpt.optName, "ARB_position_invariant")) {
				curStatusPtr->position_invariant = 1;
			} else {
				FAIL("Unknown option");
			}
			
			free(curStatusPtr->curValue.newOpt.optName);
			
			curStatusPtr->valueType = TYPE_NONE;
			curStatusPtr->status = ST_LINE_START;
			break;
			
		default:
			FAIL("Invalid token");
		}
		break;
		
	case ST_DONE:
	case ST_ERROR:
		// Shouldn't happen (fallthrough?)
		return;
	}
}
