/**
 * Created by: SerpentSpirale
 * Copyright (c) 2025 SerpentSpirale, artDev, CADIndie.
 * For use under LGPL-3.0
 */

#ifndef UNTITLED_SHADERCONV_H
#define UNTITLED_SHADERCONV_H

char * ConvertShaderVgpu(char* source, int is_vertex, int second_pass);

char * GLSLHeader(char* source);
char * RemoveConstInsideBlocks(char* source, int * sourceLength);
char * ForceIntegerArrayAccess(char* source, int * sourceLength);
char * CoerceIntToFloat(char * source, int * sourceLength);
char * ReplaceModOperator(char * source, int * sourceLength);
char * WrapBitShiftOperators(char * source, int *sourceLength);
char * WrapBitwiseOrAnd(char * source, int *sourceLength);
char * WrapIvecFunctions(char * source, int * sourceLength);
char * WrapFunction(char * source, int * sourceLength, char * functionName, char * wrapperFunctionName, char * wrapperFunction);
int FindPositionAfterDirectives(char * source);
int FindPositionAfterVersion(const char * source);
char * ReplaceGLFragData(char * source, int * sourceLength);
char * ReplaceGLFragColor(char * source, int * sourceLength);
char * ReplaceVariableName(char * source, int * sourceLength, char * initialName, char* newName);
char * ReplaceFunctionName(char * source, int * sourceLength, char * initialName, char * finalName);
char * RemoveUnsupportedExtensions(char * source);
int doesShaderVersionContainsES(const char * source);
char *ReplacePrecisionQualifiers(char *source, int *sourceLength, int isVertex);
char * GetClosingTokens(char openingToken);
int isOpeningToken(char openingToken);
int GetClosingTokenPosition(const char * source, int initialTokenPosition);
int GetClosingTokenPositionTokenOverride(const char * source, int initialTokenPosition, char initialToken);
int GetNextTokenPosition(const char * source, int initialPosition, char token, const char * acceptedChars);
void GetNextWord(char *source, int startPoint, int * startWord, int * endWord);
unsigned long strstrPos(const char * haystack, const char * needle);
char * insertIntAtFunctionCall(char * source, int * sourceSize, const char * functionName, int argumentPosition);
char * InsertExtension(char * source, int * sourceLength, int insertPoint, const char * extension);
char * InsertExtensions(char *source, int *sourceLength);
int GetShaderVersion(const char * source);
char * ReplaceFragmentOut(char * source, int *sourceLength);
char * BackportConstArrays(char *source, int * sourceLength);
char * ExtractString(const char * source, int startString, int endString);
char * RemoveUniformProperty(char * source);
char* ProcessSwitchCases(char* source, int* length);
char * ForceIntegerLayoutOutput(char * source, int *sourceLength);
char * FixSimpleSwitchCases(char *source, int *sourceLength);
char * WrapSwitchStatements(char *source, int *sourceLength);
char * SimplifyConstIntTypecasts(char * source, int * sourceLength);
char * SimplifyRedundantIntTypecasts(char * source, int * sourceLength);
char * SimplifyRedundantParentheses(char * source, int * sourceLength);
char * FixReturnTypes(char * source, int * sourceLength);

char* GetOperandFromOperator(char* source, int operatorIndex, int rightOperand, int * limit);
char* GetOperandFromOperatorValueOverride(char* source, int operatorIndex, int rightOperand, int * limit, int overrideTokenValue);
int GetOperatorValue(char operator, char operator2);

int IsVariableNamePresent(const char * source, const int * sourceLength, const char * variableName);

#endif //UNTITLED_SHADERCONV_H