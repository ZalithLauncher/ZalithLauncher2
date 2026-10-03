#ifndef _GL4ES_ARBHELPER_H_
#define _GL4ES_ARBHELPER_H_

#include <stddef.h>

#include "khash.h"

//#define DEBUG
#ifdef DEBUG
#include <stdio.h>

// ARBCONV_DBG - general ArbConverter debug logs
#define ARBCONV_DBG(a) a
// ARBCONV_DBG_LP - code loop ArbConverter debug logs
#define ARBCONV_DBG_LP(a) a
// ARBCONV_DBG_AS - reassembly loop (2nd loop) ArbConverter debug logs
#define ARBCONV_DBG_AS(a) a
// ARBCONV_DBG_HEAVY - heavy ArbConverter debug logs and operations (e.g. check for pointer correctness...)
#define ARBCONV_DBG_HEAVY(a) a
#else
// ARBCONV_DBG - general ArbConverter debug logs
#define ARBCONV_DBG(a) 
// ARBCONV_DBG_LP - code loop ArbConverter debug logs
#define ARBCONV_DBG_LP(a) 
// ARBCONV_DBG_AS - reassembly loop (2nd loop) ArbConverter debug logs
#define ARBCONV_DBG_AS(a) 
// ARBCONV_DBG_HEAVY - heavy ArbConverter debug logs and operations (e.g. check for pointer correctness...)
#define ARBCONV_DBG_HEAVY(a) 
#endif

#ifdef MAX_TEX
#if MAX_TEX != 16
#error Please update the string
#endif
#define MAX_TEX_STR "16"
#define MAX_TEX_STRLEN 2
#endif

#define DEFAULT_MALLOC_COUNT 32
#define DEFAULT_STRING_CAP 1024
#define DEFAULT_STRING_MALLOC_SIZE (DEFAULT_STRING_CAP * sizeof(char))

void *resize(void** obj, size_t* cap, size_t esize);

typedef struct _sArray {
	void   **objs; // Array of pointers
	size_t size;
	size_t cap;
} sArray;

void initArray(sArray* arr);
int pushArray(sArray* arr, void* obj);
void* popArray(sArray* arr);
void* popFIFO(sArray* arr);
void freeArray(sArray* arr);

typedef enum _eVariableType {
	VARTYPE_ADDRESS = 0,
	VARTYPE_ATTRIB,
	VARTYPE_PARAM,
	VARTYPE_PARAM_MULT,
	VARTYPE_TEMP,
	VARTYPE_ALIAS,
	VARTYPE_OUTPUT,
	
	VARTYPE_CONST, // Used when having an anonymous variable
	VARTYPE_TEXTURE, // Not available in vertex programs, fixed name in fragment programs
	VARTYPE_TEXTARGET, // Not available in vertex programs, fixed name in fragment programs
	VARTYPE_UNK
} eVariableType;
typedef enum _eInstruction {
	INST_ABS = 0, INST_ADD, INST_ARL, INST_CMP, INST_COS, INST_DP3, INST_DP4, INST_DPH,
	INST_DST,     INST_EX2, INST_EXP, INST_FLR, INST_FRC, INST_KIL, INST_LG2, INST_LIT,
	INST_LOG,     INST_LRP, INST_MAD, INST_MAX, INST_MIN, INST_MOV, INST_MUL, INST_POW,
	INST_RCP,     INST_RSQ, INST_SCS, INST_SGE, INST_SIN, INST_SLT, INST_SUB, INST_SWZ,
	INST_TEX,     INST_TXB, INST_TXP, INST_XPD,
	
	INST_UNK
} eInstruction;
typedef enum _eStatus {
	ST_LINE_START = 0,
	
	ST_LINE_COMMENT,
	
	ST_VARIABLE,
	ST_VARIABLE_INIT,
	ST_ALIAS,
	ST_ALIASING,
	ST_INSTRUCTION,
	
	ST_OPTION,
	
	ST_DONE,
	ST_ERROR
} eStatus;
typedef enum _eToken {
	TOK_NULL = 0,
	
	TOK_WHITESPACE,
	
	TOK_SIGN,
	TOK_INTEGER,
	TOK_FLOATCONST,
	
	TOK_IDENTIFIER,
	TOK_POINT,
	TOK_UPTO,
	TOK_COMMA,
	TOK_EQUALS,
	TOK_LSQBRACKET,
	TOK_RSQBRACKET,
	TOK_LBRACE,
	TOK_RBRACE,
	TOK_END_OF_INST,
	
	TOK_LINE_COMMENT,
	
	TOK_NEWLINE,
	
	TOK_END,
	
	TOK_UNKNOWN
} eToken;

eVariableType STR2VARTYPE(char *str);
eInstruction STR2INST(char *str, int *sat);
#define INSTTEX(i) (((i) == INST_TEX) || ((i) == INST_TXB) || ((i) == INST_TXP))

#define ENUMVALUE2STR(v, e, v2) (v == e##v2) ? #v2 :
#ifdef DEBUG
#define VARTYPE2STR(vartype) ( \
	ENUMVALUE2STR(vartype, VARTYPE_,ADDRESS) \
	ENUMVALUE2STR(vartype, VARTYPE_,ATTRIB) \
	ENUMVALUE2STR(vartype, VARTYPE_,PARAM) \
	ENUMVALUE2STR(vartype, VARTYPE_,PARAM_MULT) \
	ENUMVALUE2STR(vartype, VARTYPE_,TEMP) \
	ENUMVALUE2STR(vartype, VARTYPE_,OUTPUT) \
	ENUMVALUE2STR(vartype, VARTYPE_,CONST) \
	"???")
#define INST2STR(inst) ( \
	ENUMVALUE2STR(inst, INST_,ABS) ENUMVALUE2STR(inst, INST_,ADD) \
	ENUMVALUE2STR(inst, INST_,ARL) ENUMVALUE2STR(inst, INST_,CMP) \
	ENUMVALUE2STR(inst, INST_,COS) ENUMVALUE2STR(inst, INST_,DP3) \
	ENUMVALUE2STR(inst, INST_,DP4) ENUMVALUE2STR(inst, INST_,DPH) \
	ENUMVALUE2STR(inst, INST_,DST) ENUMVALUE2STR(inst, INST_,EX2) \
	ENUMVALUE2STR(inst, INST_,EXP) ENUMVALUE2STR(inst, INST_,FLR) \
	ENUMVALUE2STR(inst, INST_,FRC) ENUMVALUE2STR(inst, INST_,KIL) \
	ENUMVALUE2STR(inst, INST_,LG2) ENUMVALUE2STR(inst, INST_,LIT) \
	ENUMVALUE2STR(inst, INST_,LOG) ENUMVALUE2STR(inst, INST_,LRP) \
	ENUMVALUE2STR(inst, INST_,MAD) ENUMVALUE2STR(inst, INST_,MAX) \
	ENUMVALUE2STR(inst, INST_,MIN) ENUMVALUE2STR(inst, INST_,MOV) \
	ENUMVALUE2STR(inst, INST_,MUL) ENUMVALUE2STR(inst, INST_,POW) \
	ENUMVALUE2STR(inst, INST_,RCP) ENUMVALUE2STR(inst, INST_,RSQ) \
	ENUMVALUE2STR(inst, INST_,SCS) ENUMVALUE2STR(inst, INST_,SGE) \
	ENUMVALUE2STR(inst, INST_,SIN) ENUMVALUE2STR(inst, INST_,SLT) \
	ENUMVALUE2STR(inst, INST_,SUB) ENUMVALUE2STR(inst, INST_,SWZ) \
	ENUMVALUE2STR(inst, INST_,TEX) ENUMVALUE2STR(inst, INST_,TXB) \
	ENUMVALUE2STR(inst, INST_,TXP) ENUMVALUE2STR(inst, INST_,XPD) \
	"???")
#define STATUS2STR(s) (\
	ENUMVALUE2STR(s, ST_,LINE_START) \
	ENUMVALUE2STR(s, ST_,LINE_COMMENT) \
	ENUMVALUE2STR(s, ST_,VARIABLE) \
	ENUMVALUE2STR(s, ST_,VARIABLE_INIT) \
	ENUMVALUE2STR(s, ST_,ALIAS) \
	ENUMVALUE2STR(s, ST_,ALIASING) \
	ENUMVALUE2STR(s, ST_,INSTRUCTION) \
	ENUMVALUE2STR(s, ST_,OPTION) \
	ENUMVALUE2STR(s, ST_,DONE) \
	ENUMVALUE2STR(s, ST_,ERROR) \
	"???")
#endif
#define TOKEN2STR(t) (\
	ENUMVALUE2STR(t, TOK_,NULL) \
	ENUMVALUE2STR(t, TOK_,WHITESPACE) \
	ENUMVALUE2STR(t, TOK_,SIGN) \
	ENUMVALUE2STR(t, TOK_,INTEGER) \
	ENUMVALUE2STR(t, TOK_,FLOATCONST) \
	ENUMVALUE2STR(t, TOK_,IDENTIFIER) \
	ENUMVALUE2STR(t, TOK_,POINT) \
	ENUMVALUE2STR(t, TOK_,UPTO) \
	ENUMVALUE2STR(t, TOK_,COMMA) \
	ENUMVALUE2STR(t, TOK_,EQUALS) \
	ENUMVALUE2STR(t, TOK_,LSQBRACKET) \
	ENUMVALUE2STR(t, TOK_,RSQBRACKET) \
	ENUMVALUE2STR(t, TOK_,LBRACE) \
	ENUMVALUE2STR(t, TOK_,RBRACE) \
	ENUMVALUE2STR(t, TOK_,END_OF_INST) \
	ENUMVALUE2STR(t, TOK_,LINE_COMMENT) \
	ENUMVALUE2STR(t, TOK_,NEWLINE) \
	ENUMVALUE2STR(t, TOK_,END) \
	ENUMVALUE2STR(t, TOK_,UNKNOWN) \
	"???")

typedef struct _sVariableInit { // sArray manipulable
	char   **strings;
	size_t strings_count;
	size_t strings_cap;
	
	size_t strings_total_len;
} sVariableInit;
typedef struct _sVariable { // sArray manipulable
	char          **names;
	size_t        names_count;
	size_t        names_cap;
	
	eVariableType type;
	
	sVariableInit init;
	int           size; // Optional (VARTYPE_PARAM_MULT)
} sVariable;
typedef struct _sVariables {
	sVariable **vars;
	size_t    size;
	size_t    cap;
} sVariables;

sVariable *createVariable(eVariableType type);
void deleteVariable(sVariable **var);

KHASH_MAP_DECLARE_STR(variables, sVariable*)
#define kh_truly_exist(h, i) ((i < kh_end(h)) && kh_exist(h, i))
#define kh_str_exist(h, s) kh_truly_exist(h, kh_get(variables, h, s))

#define MAX_OPERANDS 4
typedef struct _sInstruction {
	eInstruction type;
	int saturated;
	
	struct _sInstruction_Vars {
		sVariable *var;
		
		char *floatArrAddr;
		int  sign;
		enum {
			SWIZ_NONE = 0, SWIZ_X, SWIZ_Y, SWIZ_Z, SWIZ_W
		} swizzle[4];
	} vars[MAX_OPERANDS];
	
	const char *codeLocation;
} sInstruction;
typedef struct _sInstruction_Vars sInstruction_Vars;
typedef struct _sInstructions {
	sInstruction **insts;
	size_t       size;
	size_t       cap;
} sInstructions;

sInstruction *copyInstruction(const sInstruction *orig);

typedef struct _sCurStatus_NewInst {
	sInstruction inst;   // duplicateInstruction(&newInst);
	unsigned int curArg;
	int          state;  // Dependant on variable type and current status
} sCurStatus_NewInst;
typedef struct _sCurStatus_NewVar { // sArray manipulable
	char      **strParts; // copyToken();
	size_t    strLen;
	size_t    strCap;
	
	sVariable *var;       // createVariable();
	int       state;      // Dependant on variable type and current status
} sCurStatus_NewVar;
typedef struct _sCurStatus_NewOpt {
	char *optName;
} sCurStatus_NewOpt;
typedef struct _sCurStatus {
	eStatus      status;
	
	int          arbVersion;
	
	const char   *codePtr;
	
	const char   *endOfToken;
	eToken       curToken;
	unsigned int tokInt;
	float        tokFloat;
	
	char         *outputString;
	char         *outputEnd;
	size_t       outLen;
	size_t       outCap;
	size_t       outLeft;
	
	enum {
		TYPE_NONE, TYPE_INST_DECL, TYPE_VARIABLE_DECL, TYPE_ALIAS_DECL, TYPE_OPTION_DECL
	} valueType;
	union {
		sCurStatus_NewInst newInst;
		sCurStatus_NewVar  newVar;
		char               *string; // getToken();
		sCurStatus_NewOpt  newOpt;
	} curValue;
	
	sCurStatus_NewVar _fixedNewVar;
	
	sVariable    **texVars;
	sVariable    *tex1D;
	sVariable    *tex2D;
	sVariable    *tex3D;
	sVariable    *texCUBE;
	sVariable    *texRECT;
	
	khash_t(variables) *varsMap;
	sVariables         variables;
	sInstructions      instructions;
	enum { FOG_NONE, FOG_EXP, FOG_EXP2, FOG_LINEAR } fogType;
	int			position_invariant;
} sCurStatus;

void initStatus(sCurStatus* curStatus, const char* code);
void freeStatus(sCurStatus* curStatus);

int appendString(sCurStatus *curStatusPtr, const char *str, size_t strLen);


#endif // _GL4ES_ARBHELPER_H_
