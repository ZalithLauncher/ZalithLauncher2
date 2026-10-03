#include "arbhelper.h"

#include "../config.h"

#define SIZE_THRESHOLD 0x40
void *resize(void** obj, size_t* cap, size_t esize) {
	size_t oldSize = *cap * esize;
	size_t newSize;
	if (oldSize >= SIZE_THRESHOLD) {
		newSize = oldSize + SIZE_THRESHOLD * esize;
	} else {
		newSize = oldSize * 2;
	}
	void *reloc = realloc(*obj, newSize);
	if (reloc) {
		*obj = reloc;
		*cap = newSize;
	}
	return reloc;
}

void initArray(sArray* arr) {
	arr->objs = calloc(DEFAULT_MALLOC_COUNT, sizeof(void*));
	arr->size = 0;
	arr->cap = DEFAULT_MALLOC_COUNT;
}
int pushArray(sArray* arr, void* obj) {
	if (arr->size >= arr->cap) {
		if (!resize((void**)&arr->objs, &arr->cap, sizeof(void*))) {
			return 0;
		}
	}
	
	arr->objs[arr->size] = obj;
	++arr->size;
	return 1;
}
void* popArray(sArray* arr) {
	if (!arr->size) {
		return NULL;
	}
	
	--arr->size;
	void *ret = arr->objs[arr->size];
	arr->objs[arr->size] = NULL;
	return ret;
}
void* popFIFO(sArray* arr) {
	if (!arr->size) {
		return NULL;
	}
	
	--arr->size;
	void *ret = arr->objs[0];
	memmove(arr->objs, arr->objs + 1, arr->size * sizeof(void*));
	arr->objs[arr->size] = NULL;
	return ret;
}
void freeArray(sArray* arr) {
	while (arr->size) {
		--arr->size;
		free(arr->objs[arr->size]);
	}
	free(arr->objs);
	arr->objs = NULL;
}

#define TST_STR_VAL_PREF(str, pref, inst) (!strncmp(str, #inst, strlen(#inst))) ? pref##inst :
eVariableType STR2VARTYPE(char *str) {
	return 
		TST_STR_VAL_PREF(str, VARTYPE_, ADDRESS) TST_STR_VAL_PREF(str, VARTYPE_, ATTRIB)
		TST_STR_VAL_PREF(str, VARTYPE_, PARAM)   TST_STR_VAL_PREF(str, VARTYPE_, TEMP)
		TST_STR_VAL_PREF(str, VARTYPE_, ALIAS)   TST_STR_VAL_PREF(str, VARTYPE_, OUTPUT)
		VARTYPE_UNK;
}
eInstruction STR2INST(char *str, int *sat) {
	if ((str[0] == '\0') || (str[1] == '\0') || (str[2] == '\0')) return INST_UNK;
	*sat = !strcmp(str + 3, "_SAT");
	if ((((str[0] < 'A') || (str[0] > 'Z')) && ((str[0] < '0') || (str[0] > '9')))
	 || (((str[1] < 'A') || (str[1] > 'Z')) && ((str[1] < '0') || (str[1] > '9')))
	 || (((str[2] < 'A') || (str[2] > 'Z')) && ((str[2] < '0') || (str[2] > '9')))
	 || ((str[3] != '\0') && !*sat)) {
		return INST_UNK;
	}
	
	switch (str[0]) {
	case 'A':
		switch (str[1]) {
		case 'B':
			return (str[2] == 'S') ? INST_ABS : INST_UNK;
		case 'D':
			return (str[2] == 'D') ? INST_ADD : INST_UNK;
		case 'R':
			return (str[2] == 'L') ? INST_ARL : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'C':
		switch (str[1]) {
		case 'M':
			return (str[2] == 'P') ? INST_CMP : INST_UNK;
		case 'O':
			return (str[2] == 'S') ? INST_COS : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'D':
		switch (str[2]) {
		case '3':
			return (str[1] == 'P') ? INST_DP3 : INST_UNK;
		case '4':
			return (str[1] == 'P') ? INST_DP4 : INST_UNK;
		case 'H':
			return (str[1] == 'P') ? INST_DPH : INST_UNK;
		case 'T':
			return (str[1] == 'S') ? INST_DST : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'E':
		if (str[1] != 'X') return INST_UNK;
		switch (str[2]) {
		case '2':
			return INST_EX2;
		case 'P':
			return INST_EXP;
		default:
			return INST_UNK;
		}
	case 'F':
		switch (str[1]) {
		case 'L':
			return (str[2] == 'R') ? INST_FLR : INST_UNK;
		case 'R':
			return (str[2] == 'C') ? INST_FRC : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'K':
		return ((str[1] == 'I') && (str[2] == 'L')) ? INST_KIL : INST_UNK;
	case 'L':
		switch (str[1]) {
		case 'G':
			return (str[2] == '2') ? INST_LG2 : INST_UNK;
		case 'I':
			return (str[2] == 'T') ? INST_LIT : INST_UNK;
		case 'O':
			return (str[2] == 'G') ? INST_LOG : INST_UNK;
		case 'R':
			return (str[2] == 'P') ? INST_LRP : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'M':
		switch (str[2]) {
		case 'D':
			return (str[1] == 'A') ? INST_MAD : INST_UNK;
		case 'X':
			return (str[1] == 'A') ? INST_MAX : INST_UNK;
		case 'N':
			return (str[1] == 'I') ? INST_MIN : INST_UNK;
		case 'V':
			return (str[1] == 'O') ? INST_MOV : INST_UNK;
		case 'L':
			return (str[1] == 'U') ? INST_MUL : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'P':
		return ((str[1] == 'O') && (str[2] == 'W')) ? INST_POW : INST_UNK;
	case 'R':
		switch (str[1]) {
		case 'C':
			return (str[2] == 'P') ? INST_RCP : INST_UNK;
		case 'S':
			return (str[2] == 'Q') ? INST_RSQ : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'S':
		switch (str[1]) {
		case 'C':
			return (str[2] == 'S') ? INST_SCS : INST_UNK;
		case 'G':
			return (str[2] == 'E') ? INST_SGE : INST_UNK;
		case 'I':
			return (str[2] == 'N') ? INST_SIN : INST_UNK;
		case 'L':
			return (str[2] == 'T') ? INST_SLT : INST_UNK;
		case 'U':
			return (str[2] == 'B') ? INST_SUB : INST_UNK;
		case 'W':
			return (str[2] == 'Z') ? INST_SWZ : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'T':
		switch (str[2]) {
		case 'X':
			return (str[1] == 'E') ? INST_TEX : INST_UNK;
		case 'B':
			return (str[1] == 'X') ? INST_TXB : INST_UNK;
		case 'P':
			return (str[1] == 'X') ? INST_TXP : INST_UNK;
		default:
			return INST_UNK;
		}
	case 'X':
		return ((str[1] == 'P') && (str[2] == 'D')) ? INST_XPD : INST_UNK;
	default:
		return INST_UNK;
	}
}
#define INSTTEX(i) (((i) == INST_TEX) || ((i) == INST_TXB) || ((i) == INST_TXP))

KHASH_MAP_IMPL_STR(variables, sVariable*)

sVariable *createVariable(eVariableType type) {
	sVariable *var = (sVariable*)calloc(1, sizeof(sVariable)); // malloc *should* be fine here
	initArray((sArray*)var);
	initArray((sArray*)&var->init);
	var->type = type;
	var->init.strings_total_len = 0;
	var->size = 0;
	return var;
}
void deleteVariable(sVariable **var) {
	freeArray((sArray*)*var);
	
	char *stringPtr;
	while ((stringPtr = popArray((sArray*)&(*var)->init))) {
		free(stringPtr);
	}
	freeArray((sArray*)&(*var)->init);
	free(*var);
	*var = NULL;
}

sInstruction *copyInstruction(const sInstruction *orig) {
	sInstruction *dup = (sInstruction*)malloc(sizeof(sInstruction));
	memcpy(dup, orig, sizeof(sInstruction));
	return dup;
}

void initStatus(sCurStatus* curStatus, const char* code) {
	curStatus->status = ST_LINE_START;
	curStatus->arbVersion = 10; // Todo?
	curStatus->codePtr = code;
	curStatus->endOfToken = code;
	
	curStatus->outputString = malloc(DEFAULT_STRING_MALLOC_SIZE);
	curStatus->outputString[0] = '\0';
	curStatus->outputEnd = curStatus->outputString;
	curStatus->outLen = 0;
	curStatus->outCap = DEFAULT_STRING_CAP;
	curStatus->outLeft = DEFAULT_STRING_CAP - 1;
	
	curStatus->valueType = TYPE_NONE;
	
	curStatus->texVars = (sVariable**)malloc(MAX_TEX * sizeof(sVariable*));
	for (size_t i = 0; i < MAX_TEX; ++i) {
		curStatus->texVars[i] = createVariable(VARTYPE_TEXTURE);
	}
	curStatus->tex1D = (sVariable*)malloc(sizeof(sVariable));
	curStatus->tex1D->type = VARTYPE_TEXTARGET;
	curStatus->tex2D = (sVariable*)malloc(sizeof(sVariable));
	curStatus->tex2D->type = VARTYPE_TEXTARGET;
	curStatus->tex3D = (sVariable*)malloc(sizeof(sVariable));
	curStatus->tex3D->type = VARTYPE_TEXTARGET;
	curStatus->texCUBE = (sVariable*)malloc(sizeof(sVariable));
	curStatus->texCUBE->type = VARTYPE_TEXTARGET;
	curStatus->texRECT = (sVariable*)malloc(sizeof(sVariable));
	curStatus->texRECT->type = VARTYPE_TEXTARGET;
	
	curStatus->varsMap = kh_init(variables);
	initArray((sArray*)&curStatus->variables);
	initArray((sArray*)&curStatus->instructions);
	curStatus->fogType = FOG_NONE;
	
	initArray((sArray*)&curStatus->_fixedNewVar);
	curStatus->_fixedNewVar.var = NULL;
}

void freeStatus(sCurStatus* curStatus) {
	if (curStatus->valueType == TYPE_INST_DECL) {
		for (int i = 0; i < MAX_OPERANDS; ++i) {
			if (curStatus->curValue.newInst.inst.vars[i].floatArrAddr) {
				free(curStatus->curValue.newInst.inst.vars[i].floatArrAddr);
			}
		}
	} else if (curStatus->valueType == TYPE_VARIABLE_DECL) {
		char *strPtr;
		while ((strPtr = (char*)popArray((sArray*)&curStatus->curValue.newVar))) {
			free(strPtr);
		}
		freeArray((sArray*)&curStatus->curValue.newVar);
		
		deleteVariable(&curStatus->curValue.newVar.var);
	} else if (curStatus->valueType == TYPE_ALIAS_DECL) {
		if (curStatus->curValue.string) {
			free(curStatus->curValue.string);
		}
	} else if (curStatus->valueType == TYPE_OPTION_DECL) {
		if (curStatus->curValue.newOpt.optName) {
			free(curStatus->curValue.newOpt.optName);
		}
	}
	curStatus->valueType = TYPE_NONE;
	
	for (size_t i = 0; i < MAX_TEX; ++i) {
		deleteVariable(&curStatus->texVars[i]);
	}
	free(curStatus->texVars);
	free(curStatus->tex1D);
	free(curStatus->tex2D);
	free(curStatus->tex3D);
	free(curStatus->texCUBE);
	free(curStatus->texRECT);
	
	kh_destroy(variables, curStatus->varsMap);
	
	sVariable *varPtr;
	while ((varPtr = (sVariable*)popArray((sArray*)&curStatus->variables))) {
		deleteVariable(&varPtr);
	}
	sInstruction *instPtr;
	while ((instPtr = (sInstruction*)popArray((sArray*)&curStatus->instructions))) {
		for (int i = 0; i < MAX_OPERANDS; ++i) {
			if (instPtr->vars[i].floatArrAddr) {
				free(instPtr->vars[i].floatArrAddr);
			}
		}
		free(instPtr);
	}
	
	freeArray((sArray*)&curStatus->variables);
	freeArray((sArray*)&curStatus->instructions);
	
	freeArray((sArray*)&curStatus->_fixedNewVar);
}

int appendString(sCurStatus *curStatusPtr, const char *str, size_t strLen) {
	if (strLen == (size_t)-1) {
		strLen = strlen(str);
	}
	
	if (curStatusPtr->outLeft < strLen) {
		char *oldOut = curStatusPtr->outputString;
		while (curStatusPtr->outLeft < strLen) {
			if (curStatusPtr->outCap >= SIZE_THRESHOLD) {
				curStatusPtr->outLeft += SIZE_THRESHOLD * sizeof(char);
			} else {
				curStatusPtr->outLeft *= 2;
			}
			if (!resize((void**)&curStatusPtr->outputString, &curStatusPtr->outCap, sizeof(char))) {
				return 1;
			}
		}
		curStatusPtr->outputEnd += curStatusPtr->outputString - oldOut;
	}
	
	ARBCONV_DBG_HEAVY(ARBCONV_DBG(
	char *dup = malloc((strLen + 1) * sizeof(char)); memcpy(dup, str, strLen); dup[strLen] = '\0'; printf(
		"Appending '%s' to %p (%p + %ld = %p)\n",
		dup,
		curStatusPtr->outputEnd,
		curStatusPtr->outputString,
		curStatusPtr->outLen,
		curStatusPtr->outputString + curStatusPtr->outLen
	); free(dup); fflush(stdout);)
	if (curStatusPtr->outputEnd != curStatusPtr->outputString + curStatusPtr->outLen) {
		printf("\033[01;31mERROR!!!\033[m\n%s\n", str);
		curStatusPtr->status = ST_ERROR;
		return 1;
	})
	
	strcpy(curStatusPtr->outputEnd, str);
	curStatusPtr->outLen += strLen;
	curStatusPtr->outLeft -= strLen;
	curStatusPtr->outputEnd += strLen;
	
	return 0;
}
