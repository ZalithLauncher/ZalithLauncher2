/**
 * Created by: SerpentSpirale
 * Copyright (c) 2025 SerpentSpirale, artDev, CADIndie.
 * For use under LGPL-3.0
 */
#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include "shaderconv.h"
#include "../string_utils.h"

int NO_OPERATOR_VALUE = 9999;
int ADDITIVE_OPERATOR_VALUE = 5;



/** Just prints the shader at a stage, for heavy dump */
void VerbosePrint(char * source, char * stage) {
#ifdef SHADERCONV_VERBOSE
    printf("Stage - %s :\n%s\n", stage, source);
#endif
}

/** Convert the shader through multiple steps
 * @param source The start of the shader as a string
 * @param second_pass Whether gl4es tries to solve a linking error
 */
char * ConvertShaderVgpu(char* source, int is_vertex, int second_pass){

#ifdef SHADERCONV_DEBUG
        printf("New VGPU Shader source:\n%s\n", source);
#endif

    // Get the shader source
    int sourceLength = strlen(source) + 1;
    // For now, skip stuff


    // Remove 'const' storage qualifier
    //printf("REMOVING CONST qualifiers");
    //source = RemoveConstInsideBlocks(source, &sourceLength);
    //source = ReplaceVariableName(source, &sourceLength, "const", " ");




    // Avoid keyword clash with gl4es #define blocks
    //printf("REPLACING KEYWORDS");
    source = gl4es_inplace_replace_simple(source, &sourceLength, "#define texture2D texture\n", "");
    source = ReplaceVariableName(source, &sourceLength, "sample", "vgpu_Sample");
    source = ReplaceVariableName(source, &sourceLength, "texture", "vgpu_texture");

    source = ReplaceFunctionName(source, &sourceLength, "texture2D", "texture");
    source = ReplaceFunctionName(source, &sourceLength, "texture3D", "texture");
    source = ReplaceFunctionName(source, &sourceLength, "texture2DLod", "textureLod");



    //printf("REMOVING \" CHARS ");
    // " not really supported here
    source = gl4es_inplace_replace_simple(source, &sourceLength, "\"", "");
    VerbosePrint(source, "Function renames");

    // For now let's hope no extensions are used
    // TODO deal with extensions but properly
    //printf("REMOVING EXTENSIONS");
    //source = RemoveUnsupportedExtensions(source);

    // OpenGL natively supports non const global initializers, not OPENGL ES except if we add an extension
    //printf("ADDING EXTENSIONS\n");
    source = InsertExtensions(source, &sourceLength);
    VerbosePrint(source, "Extensions inserted");

    //printf("REPLACING mod OPERATORS");
    // No support for % operator, so we replace it
    source = ReplaceModOperator(source, &sourceLength);
    VerbosePrint(source, "Modulo operator replaced");

    //printf("COERCING INT TO FLOATS");
    // Hey we don't want to deal with implicit type stuff
    source = CoerceIntToFloat(source, &sourceLength);
    VerbosePrint(source, "Int coerced to float");

    //printf("FIXING ARRAY ACCESS");
    // Avoid any weird type trying to be an index for an array
    source = ForceIntegerArrayAccess(source, &sourceLength);
    VerbosePrint(source, "Array access cast to integers");

    //printf("WRAPPING FUNCTION");
    // Since everything is a float, we need to overload WAY TOO MANY functions
    source = WrapIvecFunctions(source, &sourceLength);
    VerbosePrint(source, "Wrapped ivec function renames");

    //printf("REMOVING DUBIOUS DEFINES");
    source = gl4es_inplace_replace_simple(source, &sourceLength, "#define texture texture2D\n", "");
    source = gl4es_inplace_replace_simple(source, &sourceLength, "#define attribute in\n", "");
    source = gl4es_inplace_replace_simple(source, &sourceLength, "#define varying out\n", "");

    if (is_vertex){
        source = ReplaceVariableName(source, &sourceLength, "attribute", "in");
        source = ReplaceVariableName(source, &sourceLength, "varying", "out");
    }else{
        source = ReplaceVariableName(source, &sourceLength, "varying", "in");
    }

    VerbosePrint(source, "Basic renames - PART 2");

    // Draw buffers aren't dealt the same on OPEN GL|ES
    if(!is_vertex && doesShaderVersionContainsES(source) ){
        //printf("REPLACING FRAG DATA");
        source = ReplaceGLFragData(source, &sourceLength);
        //printf("REPLACING FRAG COLOR");
        source = ReplaceGLFragColor(source, &sourceLength);

        VerbosePrint(source, "GL_FRAG renames");
    }

    //printf("FUCKING UP PRECISION");
    source = ReplacePrecisionQualifiers(source, &sourceLength, is_vertex);

    source = ProcessSwitchCases(source, &sourceLength);
    VerbosePrint(source, "Complex case statement replaced");

    source = FixSimpleSwitchCases(source, &sourceLength);
    VerbosePrint(source, "Simple case statement replaced");

    source = WrapSwitchStatements(source, &sourceLength);
    VerbosePrint(source, "Switch statement wrapped");

    source = RemoveUniformProperty(source);
    VerbosePrint(source, "Uniform property removed");

    source = ForceIntegerLayoutOutput(source, &sourceLength);
    VerbosePrint(source, "Layout output corrected back to integers");

    source = WrapBitShiftOperators(source, &sourceLength);
    VerbosePrint(source, "Bit shifts wrapped !");

    source = WrapBitwiseOrAnd(source, &sourceLength);
    VerbosePrint(source, "Inclusive Or wrapped");

    source = SimplifyRedundantParentheses(source, &sourceLength);
    VerbosePrint(source, "Simplified parentheses");

    source = SimplifyRedundantIntTypecasts(source, &sourceLength);
    VerbosePrint(source, "Non const typecast simplified");

    source = SimplifyConstIntTypecasts(source, &sourceLength);
    VerbosePrint(source, "Simplified const typecast typecast !");

    source = FixReturnTypes(source, &sourceLength);
    VerbosePrint(source, "Wrapped some function return statement");

#ifdef SHADERCONV_DEBUG
        printf("New VGPU Shader conversion:\n%s\n", source);
#endif

    return source;
}

static const char* switch_template = " switch (%n%[^)] { ";
static const char* case_template = " case %n%[^:] ";
static const char* declaration_template = " const float %s = %s ;";

#define VARIABLE_SIZE 1024
#define MODE_SWITCH 0
#define MODE_CASE 1

/**
 * Check if the input string is a conformant variable name or not
 * @returns 1 if yes, 0 if no
 */
unsigned char CheckVariableName(const char* name) {
    if(isalpha(name[0]) || name[0] == '_') {
        size_t cnt = 0;
        while(1) { // You only crash once
            cnt++;
            if(name[cnt] == 0) return 1;
            if(!isDigit(name[cnt]) && !isalpha(name[cnt]) && name[cnt] != '_') return 0;
        }
    }
    return 0;
}

/**
 * Convert switches or cases to be usable with the current int to float conversion
 * @param source The shader as a string
 * @param sourceLength The shader allocated length
 * @param mode Whether to scan and fix switches or cases
 * @return The shader as a string, converted appropriately, maybe in a different memory location
*/
char* FindAndCorrect(char* source, int* length, int mode) {
    const char*     template = mode == MODE_SWITCH ? switch_template : mode == MODE_CASE ? case_template : NULL;
    char*           scan_source = source;
    char            template_string[VARIABLE_SIZE];
    size_t          string_offset;
    size_t          offset = 0;
    unsigned char   rewind = 0;
    while(1) {
        int scan_result = sscanf(scan_source, template, &string_offset, &template_string);
        if(scan_result == 0) {
            scan_source++;
            continue;
        }else if(scan_result == EOF) {
            break;
        }
        offset = string_offset + (strstr(scan_source, mode == MODE_SWITCH ? "{" : mode == MODE_CASE ? ":" : 0) - scan_source); // find it by hand cause sscanf has trouble with two %n operators
        string_offset += (scan_source - source); // convert it from relative to scan to relative to base
        if(mode == MODE_SWITCH && !strstr(template_string, "int(") ) { // already cast to int, skip
            size_t insert_end_offset = string_offset + strlen(template_string);
            source = InplaceInsertByIndex(source, length, insert_end_offset, ")");
            source = InplaceInsertByIndex(source, length, string_offset, "int(");
            rewind = 1;
        }
        if(mode == MODE_CASE) {
            if(CheckVariableName(template_string)) {
                char   decltemplate_formatted[VARIABLE_SIZE];
                float  declared_value = 99;
                snprintf(decltemplate_formatted, VARIABLE_SIZE, declaration_template, template_string, "%f");
                printf("Scanning with template %s\n", decltemplate_formatted);
                char* scanbase = source;
                while(1) {
                    int result = sscanf(scanbase, decltemplate_formatted, &declared_value);
                    if(result == 0) {
                        scanbase++;
                        continue;
                    }else if(result == EOF) {
                        printf("Scanned the whole shader and didn't find declaration for %s with template \"%s\"\n", template_string, decltemplate_formatted);
                        abort();
                    }
                    break;
                }
                char   integer[VARIABLE_SIZE];
                snprintf(integer, VARIABLE_SIZE, "%i", (int)declared_value);
                size_t replace_end_offset = string_offset + strlen(template_string)-1;
                source = InplaceReplaceByIndex(source, length, string_offset, replace_end_offset, integer);
                rewind = 1;
            }
        }
        if(rewind) {
            scan_source = source; // since inplace replacement operations are destructive, the scan will be rewound after doing them
            rewind = 0;
        }else scan_source += offset;
    }
    return source;
}

/**
 * Convert switches and cases in the shader to be usable with the current int to float coercion
 * @param source The shader as a string
 * @param sourceLength The shader allocated length
 * @return The shader as a string, converted appropriately, maybe in a different memory location
*/

char* ProcessSwitchCases(char* source, int* length) {
    //source = FindAndCorrect(source, length, MODE_SWITCH);
    source = FindAndCorrect(source, length, MODE_CASE);
    return source;
}

/**
 * Wraps the content of a switch statement into an integer
 * @param source The shader as a string
 * @param sourceLength The allocated length of the shader
 * @return The shader as a string, maybe in a different memory location
 */
char * WrapSwitchStatements(char *source, int *sourceLength){
    unsigned long offset = 0;
    while (1){
        // Find the switch statement
        unsigned long startIndex = strstrPos(source + offset, "switch");
        if(startIndex == 0) break;

        // Go to the end of the switch statement
        startIndex += 5;

        // Get to the start parentheses, then to the end one
        unsigned long startParentheses = GetNextTokenPosition(source + offset, startIndex, '(', "\n\t\r ");
        if(startParentheses == startIndex) break;

        // Get to the end token
        unsigned long endParentheses = GetClosingTokenPosition(source + offset, startParentheses);
        if(endParentheses == startParentheses) break;

        // Insert the token replacements
        source = InplaceInsertByIndex(source, sourceLength, offset + endParentheses, ")");
        source = InplaceInsertByIndex(source, sourceLength, offset + startParentheses + 1, "int(");

        offset += endParentheses;
    }

    return source;
}

/**
 * Fix case <const float>: to case <const int>:
 * @param source The shader as a string
 * @param sourceLength The length of the allocated shader
 * @return The shader as a string, maybe in a different memory location
 */
char * FixSimpleSwitchCases(char *source, int *sourceLength){
    unsigned long offset = 0;
    while (1){
        // First find the case statement
        unsigned long startIndex = strstrPos(source + offset, "case ");
        if(startIndex == 0) break;

        // Reach the floating dot, fail otherwise
        unsigned long floatingIndex = GetNextTokenPosition(source + offset, startIndex, '.', "\\:()/+-");
        if(floatingIndex == startIndex) {
            offset += startIndex + 5; // 5 just to get ahead of the case statement
            continue;
        }

        source = InplaceReplaceByIndex(source, sourceLength, offset + floatingIndex, offset + floatingIndex + 1, "");

        offset += startIndex + 5; // 5 just to get ahead of the case statement
    }

    return source;
}

/**
 * Turn const arrays and its accesses into a function and function calls
 * @param source The shader as a string
 * @param sourceLength The shader allocated length
 * @return The shader as a string, maybe in a different memory location
 */
char * BackportConstArrays(char *source, int * sourceLength){
    unsigned long startPoint = strstrPos(source, "const");
    if(startPoint == 0){
        return source;
    }
    int constStart, constStop;
    GetNextWord(source, startPoint, &constStart, &constStop); // Catch the "const"

    int typeStart, typeStop;
    GetNextWord(source, constStop, &typeStart, &typeStop); // Catch the type, without []

    int variableNameStart, variableNameStop;
    GetNextWord(source, typeStop, &variableNameStart, &variableNameStop); // Catch the var name
    char * variableName = ExtractString(source, variableNameStart, variableNameStop);

    //Now, verify the data type is actually an array
    char * tokenStart = strstr(source + typeStop, "[");
    if( tokenStart != NULL && (tokenStart - source) < variableNameStart){
        // We've found an array. So we need to get to the starting parenthesis and isolate each member
        int startArray = GetNextTokenPosition(source, variableNameStop, '(', "");
        int endArray = GetClosingTokenPosition(source, startArray);

        // First pass, to count the amount of entries in the array
        int arrayEntryCount = -1;
        int currentPoint = startArray;
        while (currentPoint < endArray){
            ++arrayEntryCount;
            currentPoint = GetClosingTokenPositionTokenOverride(source, currentPoint, ',');
        }
        if(currentPoint == endArray){
            ++arrayEntryCount;
        }

        // Now we know how many entries we have, we can copy data
        int entryStart = startArray + 1;
        int entryEnd;
        for(int j=0; j<arrayEntryCount; ++j){
            // First, isolate the array entry
            entryEnd = GetClosingTokenPositionTokenOverride(source, entryStart, ',');

            // Replace the entry and jump to the end of the replacement
            source = InplaceReplaceByIndex(source, sourceLength, entryEnd , entryEnd +1, ";}"); // +2 - 1
            // Build the string to insert
            int indexStringLength = (j == 0 ? 1 : (int)(log10(j)+1));
            char * replacementString = malloc(19 + indexStringLength + 1);
            replacementString[19 + indexStringLength + 1] = '\0';
            memcpy(replacementString, "if(index==", 10);
            sprintf(replacementString + 10, "%d", j);
            strcpy(replacementString + 10 + indexStringLength, "){return ");

            // Insert the correct index in the replacement string
            source = InplaceInsertByIndex(source, sourceLength, entryStart, replacementString);

            entryStart = entryEnd + 19 + indexStringLength + 2;
            free(replacementString);
        }

        // The replacement string is not needed anymore


        // Add The last "}" to close the function
        source = InplaceInsertByIndex(source, sourceLength, entryStart, "}");
        // Add the argument section of the function
        source = InplaceReplaceByIndex(source, sourceLength, variableNameStop, startArray , "(int index){");
        // Remove the []
        source = InplaceReplaceByIndex(source, sourceLength, typeStop, variableNameStart - 1, " ");
        // Finally, remove the "const" keyword
        source = InplaceReplaceByIndex(source, sourceLength, startPoint, startPoint + 5, " ");

        // Now, we have to turn every array access to a function call
        // TODO change the start position to be more accurate to the end of the function !
        int searchOffset = endArray;
        while (1){
            int k = strstrPos(source + searchOffset, variableName) + searchOffset;
            if(k == searchOffset) break; // No more instances of the variable are found

            // Find [] to replace it with ()
            int startAccess = GetNextTokenPosition(source, k, '[', "");
            int endAccess = GetClosingTokenPosition(source, startAccess);
            source = InplaceReplaceByIndex(source, sourceLength, endAccess, endAccess, ")");
            source = InplaceReplaceByIndex(source, sourceLength, startAccess, startAccess, "(");

            // Jump ahead of the variable to search further
            searchOffset = k + 10;
        }

        free(variableName);

    }else{
        // Nothing, go to the next loop iteration
    }

    return source;
}

/**
 * Extract a substring from the provided string
 * @param source The shader as a string
 * @param startString The start of the substring
 * @param endString The end of the substring
 * @return A newly allocated substring. Don't forget to free() it !
 */
char * ExtractString(const char * source, int startString, int endString){
    char * subString = malloc((endString - startString) + 1);
    memcpy(subString, source + startString, (endString - startString));
    subString[(endString - startString)] = '\0';
    return subString;
}

/**
 * Replace the out vec4 from a fragment shader by the gl_FragColor constant
 * @param source The shader as a string
 * @return The shader, maybe in a different memory location
 */
char * ReplaceFragmentOut(char * source, int *sourceLength){
    int startPosition = strstrPos(source, "out");
    if(startPosition == 0) return source; // No "out" keyword
    int t1, t2;
    GetNextWord(source, startPosition, &t1, &t2); // Catches "out"
    GetNextWord(source, t2, &t1, &t2); // Catches "vec4"
    GetNextWord(source, t2, &t1, &t2); // Catches the variableName

    // Load the variable inside another string
    char * variableName = malloc(t2 - t1 + 1);
    variableName[t2 - t1 + 1] = '\0';
    memcpy(variableName, source + t1, t2 - t1);

    // Removing the declaration
    source = InplaceReplaceByIndex(source, sourceLength, startPosition, t2 + 1, "");

    // Replacing occurrences of the variable
    source = ReplaceVariableName(source, sourceLength, variableName, "gl_FragColor");

    free(variableName);

    return source;
}

/**
 * Get to the start, then end of the next of current word.
 * @param source The shader as a string
 * @param startPoint The start point to look at
 * @param startWord Will point to the start of the word
 * @param endWord Will point to the end of the word
 */
void GetNextWord(char *source, int startPoint, int * startWord, int * endWord){
    // Step 1: Find the real start point
    int start = 0;
    while(1){
        if(isValidFunctionName(source[startPoint] ) || isDigit(source[startPoint])){
            start = startPoint;
            break;
        }
        ++startPoint;
    }

    // Step 2: Find the end of a word
    int end = 0;
    while (1){
        if(!isValidFunctionName(source[startPoint] ) && !isDigit(source[startPoint])){
            end = startPoint;
            break;
        }
        ++startPoint;
    }

    // Then return values
    *startWord = start;
    *endWord = end;
}

char * InsertExtensions(char *source, int *sourceLength){
    int insertPoint = FindPositionAfterDirectives(source);
    //printf("INSERT POINT: %i\n", insertPoint);

    source = InsertExtension(source, sourceLength, insertPoint+1, "GL_EXT_shader_non_constant_global_initializers");
    source = InsertExtension(source, sourceLength, insertPoint+1, "GL_EXT_texture_cube_map_array");
    source = InsertExtension(source, sourceLength, insertPoint+1, "GL_EXT_texture_buffer");
    source = InsertExtension(source, sourceLength, insertPoint+1, "GL_OES_texture_storage_multisample_2d_array");
    return source;
}

char * InsertExtension(char * source, int * sourceLength, const int insertPoint, const char * extension){
    // First, insert the model, then the extension
    source = InplaceInsertByIndex(source, sourceLength, insertPoint, "#ifdef __EXT__ \n#extension __EXT__ : enable\n#endif\n");
    source = gl4es_inplace_replace_simple(source, sourceLength, "__EXT__", extension);
    return source;
}

int doesShaderVersionContainsES(const char * source){
    int version = GetShaderVersion(source);
    return version >= 300 &&  version <= 320;
}

char * WrapIvecFunctions(char * source, int * sourceLength){
    source = WrapFunction(source, sourceLength, "texelFetch", "vgpu_texelFetch", "\nvec4 vgpu_texelFetch(sampler2D sampler, vec2 P, float lod){return texelFetch(sampler, ivec2(P), int(lod));}\n"
                                                                                 "vec4 vgpu_texelFetch(sampler3D sampler, vec3 P, float lod){return texelFetch(sampler, ivec3(P), int(lod));}\n"
                                                                                 "vec4 vgpu_texelFetch(sampler2DArray sampler, vec3 P, float lod){return texelFetch(sampler, ivec3(P), int(lod));}\n"
                                                                                 "#ifdef GL_EXT_texture_buffer\n"
                                                                                 "vec4 vgpu_texelFetch(samplerBuffer sampler, float P){return texelFetch(sampler, int(P));}\n"
                                                                                 "#endif\n"
                                                                                 "#ifdef GL_OES_texture_storage_multisample_2d_array\n"
                                                                                 "vec4 vgpu_texelFetch(sampler2DMS sampler, vec2 P, float _sample){return texelFetch(sampler, ivec2(P), int(_sample));}\n"
                                                                                 "vec4 vgpu_texelFetch(sampler2DMSArray sampler, vec3 P, float _sample){return texelFetch(sampler, ivec3(P), int(_sample));}\n"
                                                                                 "#endif\n");

    source = WrapFunction(source, sourceLength, "textureSize", "vgpu_textureSize", "\nvec2 vgpu_textureSize(sampler2D sampler, float lod){return vec2(textureSize(sampler, int(lod)));}\n"
                                                                                   "vec3 vgpu_textureSize(sampler3D sampler, float lod){return vec3(textureSize(sampler, int(lod)));}\n"
                                                                                   "vec2 vgpu_textureSize(samplerCube sampler, float lod){return vec2(textureSize(sampler, int(lod)));}\n"
                                                                                   "vec2 vgpu_textureSize(sampler2DShadow sampler, float lod){return vec2(textureSize(sampler, int(lod)));}\n"
                                                                                   "vec2 vgpu_textureSize(samplerCubeShadow sampler, float lod){return vec2(textureSize(sampler, int(lod)));}\n"
                                                                                   "#ifdef GL_EXT_texture_cube_map_array\n"
                                                                                   "vec3 vgpu_textureSize(samplerCubeArray sampler, float lod){return vec3(textureSize(sampler, int(lod)));}\n"
                                                                                   "vec3 vgpu_textureSize(samplerCubeArrayShadow sampler, float lod){return vec3(textureSize(sampler, int(lod)));}\n"
                                                                                   "#endif\n"
                                                                                   "vec3 vgpu_textureSize(sampler2DArray sampler, float lod){return vec3(textureSize(sampler, int(lod)));}\n"
                                                                                   "vec3 vgpu_textureSize(sampler2DArrayShadow sampler, float lod){return vec3(textureSize(sampler, int(lod)));}\n"
                                                                                   "#ifdef GL_EXT_texture_buffer\n"
                                                                                   "float vgpu_textureSize(samplerBuffer sampler){return float(textureSize(sampler));}\n"
                                                                                   "#endif\n"
                                                                                   "#ifdef GL_OES_texture_storage_multisample_2d_array\n"
                                                                                   "vec2 vgpu_textureSize(sampler2DMS sampler){return vec2(textureSize(sampler));}\n"
                                                                                   "vec3 vgpu_textureSize(sampler2DMSArray sampler){return vec3(textureSize(sampler));}\n"
                                                                                   "#endif\n");

    source = WrapFunction(source, sourceLength, "textureOffset", "vgpu_textureOffset", "\nvec4 vgpu_textureOffset(sampler2D tex, vec2 P, vec2 offset, float bias){ivec2 Size = textureSize(tex, 0);return texture(tex, P+offset/vec2(float(Size.x), float(Size.y)), bias);}\n"
                                                                                       "vec4 vgpu_textureOffset(sampler2D tex, vec2 P, vec2 offset){return vgpu_textureOffset(tex, P, offset, 0.0);}\n"
                                                                                       "vec4 vgpu_textureOffset(sampler3D tex, vec3 P, vec3 offset, float bias){ivec3 Size = textureSize(tex, 0);return texture(tex, P+offset/vec3(float(Size.x), float(Size.y), float(Size.z)), bias);}\n"
                                                                                       "vec4 vgpu_textureOffset(sampler3D tex, vec3 P, vec3 offset){return vgpu_textureOffset(tex, P, offset, 0.0);}\n"
                                                                                       "float vgpu_textureOffset(sampler2DShadow tex, vec3 P, vec2 offset, float bias){ivec2 Size = textureSize(tex, 0);return texture(tex, P+vec3(offset.x, offset.y, 0)/vec3(float(Size.x), float(Size.y), 1.0), bias);}\n"
                                                                                       "float vgpu_textureOffset(sampler2DShadow tex, vec3 P, vec2 offset){return vgpu_textureOffset(tex, P, offset, 0.0);}\n"
                                                                                       "vec4 vgpu_textureOffset(sampler2DArray tex, vec3 P, vec2 offset, float bias){ivec3 Size = textureSize(tex, 0);return texture(tex, P+vec3(offset.x, offset.y, 0)/vec3(float(Size.x), float(Size.y), float(Size.z)), bias);}\n"
                                                                                       "vec4 vgpu_textureOffset(sampler2DArray tex, vec3 P, vec2 offset){return vgpu_textureOffset(tex, P, offset, 0.0);}\n");

    source = WrapFunction(source, sourceLength, "shadow2D", "vgpu_shadow2D", "\nvec4 vgpu_shadow2D(sampler2DShadow shadow, vec3 coord){return vec4(texture(shadow, coord), 0.0, 0.0, 0.0);}\n"
                                                                             "vec4 vgpu_shadow2D(sampler2DShadow shadow, vec3 coord, float bias){return vec4(texture(shadow, coord, bias), 0.0, 0.0, 0.0);}\n");
    return source;
}

/**
 * Replace a function and its calls by a wrapper version, only if needed
 * @param source The shader code as a string
 * @param functionName The function to be replaced
 * @param wrapperFunctionName The replacing function name
 * @param function The wrapper function itself
 * @return The shader as a string, maybe in a different memory location
 */
char * WrapFunction(char * source, int * sourceLength, char * functionName, char * wrapperFunctionName, char * wrapperFunction){
    int originalSize = strlen(source);
    source = ReplaceFunctionName(source, sourceLength, functionName, wrapperFunctionName);
    // If some calls got replaced, add the wrapper
    if(originalSize != strlen(source)){
        int insertPoint = FindPositionAfterDirectives(source);
        source = InplaceInsertByIndex(source, sourceLength, insertPoint + 1, wrapperFunction);
    }

    return source;
}

/**
 * Replace the % operator with a mathematical equivalent (x - y * floor(x/y))
 * @param source The shader as a string
 * @return The shader as a string, maybe in a different memory location
 */
char * ReplaceModOperator(char * source, int * sourceLength){
    char * modelString = " mod(x, y) ";
    int startIndex, endIndex = 0;
    int * startPtr = &startIndex, *endPtr = &endIndex;

    for(int i=0;i<*sourceLength; ++i){
        if(source[i] != '%') continue;
        // A mod operator is found !
        char * leftOperand = GetOperandFromOperator(source, i, 0, startPtr);
        char * rightOperand = GetOperandFromOperator(source,  i, 1, endPtr);

        // Generate a model string to be inserted
        char * replacementString = malloc(strlen(modelString) + 1);
        strcpy(replacementString, modelString);
        int replacementSize = strlen(replacementString);
        replacementString = gl4es_inplace_replace(replacementString, &replacementSize, "x", leftOperand);
        replacementString = gl4es_inplace_replace(replacementString, &replacementSize, "y", rightOperand);

        // Insert the new string
        source = InplaceReplaceByIndex(source, sourceLength, startIndex, endIndex, replacementString);

        // Free all the temporary strings
        free(leftOperand);
        free(rightOperand);
        free(replacementString);
    }

    return source;
}

/**
 * Wrap << and >> operands with int() casts (caused by the float coercion)
 * @param source The shader as a string
 * @param sourceLength The allocated shader length
 * @return The shader as a string, maybe in a different memory location
 */
char * WrapBitShiftOperators(char * source, int *sourceLength) {
    int startIndex, endIndex = 0;
    int * startPtr = &startIndex, *endPtr = &endIndex;

    for(int i=0;i<*sourceLength-2; ++i){
        if((source[i] == '<' && source[i+1] == '<') || (source[i] == '>' && source[i+1] == '>')){
            // A bit shift operator is found
            char * leftOperand = GetOperandFromOperatorValueOverride(source, i, 0, startPtr, GetOperatorValue('<', '<'));
            char * rightOperand = GetOperandFromOperatorValueOverride(source,  i+1, 1, endPtr, GetOperatorValue('<', '<'));

            // Remember to insert from end to start in order to not throw away the result of the operation
            source = InplaceInsertByIndex(source, sourceLength, endIndex + 1, ")");
            source = InplaceInsertByIndex(source, sourceLength, i+2, "int(");

            source = InplaceInsertByIndex(source, sourceLength, i-1, ")");
            source = InplaceInsertByIndex(source, sourceLength, startIndex, "int(");

            i = endIndex;
        }
    }

    return source;
}

char * WrapBitwiseOrAnd(char * source, int *sourceLength) {
    int startIndex, endIndex = 0;
    int * startPtr = &startIndex, *endPtr = &endIndex;

    for(int i=0;i<*sourceLength-2; ++i){
        if((source[i] == '|' && !(source[i+1] == '|' || source[i-1] == '|'))
        || (source[i] == '&' && !(source[i+1] == '&' || source[i-1] == '&')
        || (source[i] == '^' && !(source[i+1] == '^' || source[i-1] == '^'))) ){
            // An inclusive operator is found
            char * leftOperand = GetOperandFromOperatorValueOverride(source, i, 0, startPtr, GetOperatorValue(source[i], ' '));
            char * rightOperand = GetOperandFromOperatorValueOverride(source,  i+1, 1, endPtr, GetOperatorValue(source[i], ' '));

            // Insert from end to start in order to not throw away the result of the index marking operation
            source = InplaceInsertByIndex(source, sourceLength, endIndex + 1, ")");
            source = InplaceInsertByIndex(source, sourceLength, i+2, "int(");

            source = InplaceInsertByIndex(source, sourceLength, i-1, ")");
            source = InplaceInsertByIndex(source, sourceLength, startIndex, "int(");

            i = endIndex;
        }
    }

    return source;
}

/**
 * Change all (u)ints to floats.
 * This is a hack to avoid dealing with implicit conversions on common operators
 * @param source The shader as a string
 * @return The shader as a string, maybe in a new memory location
 * @see ForceIntegerArrayAccess
 */
char * CoerceIntToFloat(char * source, int * sourceLength){
    // Let's go the "freestyle way"

    // Step 1 is to translate keywords
    // Attempt and loop unrolling -> worked well, time to fix my shit I guess
    source = ReplaceVariableName(source, sourceLength, "int", "float");
    source = WrapFunction(source, sourceLength, "int", "float", "\n ");
    source = ReplaceVariableName(source, sourceLength, "uint", "float");
    source = WrapFunction(source, sourceLength, "uint", "float", "\n ");

    // TODO Yes I could just do the same as above but I'm lazy at times
    source = gl4es_inplace_replace_simple(source, sourceLength, "ivec", "vec");
    source = gl4es_inplace_replace_simple(source, sourceLength, "uvec", "vec");

    source = gl4es_inplace_replace_simple(source, sourceLength, "isampleBuffer", "sampleBuffer");
    source = gl4es_inplace_replace_simple(source, sourceLength, "usampleBuffer", "sampleBuffer");

    source = gl4es_inplace_replace_simple(source, sourceLength, "isampler", "sampler");
    source = gl4es_inplace_replace_simple(source, sourceLength, "usampler", "sampler");


    // Step 3 is slower.
    // We need to parse hardcoded values like 1 and turn it into 1.(0)
    for(int i=0; i<*sourceLength; ++i){

        // Avoid version/line directives
        if(source[i] == '#' && (source[i + 1] == 'v' || source[i + 1] == 'l') ){
            // Look for the next line
            while (source[i] != '\n' && source[i] != '\0'){
                i++;
            }
        }

        if(!isDigit(source[i])){ continue; }
        // So there is a few situations that we have to distinguish:
        // functionName1 (      ----- meaning there is SOMETHING on its left side that is related to the number
        // function(1,          ----- there is something, and it ISN'T related to the number
        // float test=3;        ----- something on both sides, not related to the number.
        // float test=X.2       ----- There is a dot, so it is part of a float already
        // float test = 0.00000 ----- I have to backtrack to find the dot
        // float test = 4u      ----- I delete the u, then branch back to normal int handling

        if(source[i-1] == '.' || source[i+1] == '.') continue;// Number part of a float
        if(isValidFunctionName(source[i - 1])) continue; // Char attached to something related
        if(isDigit(source[i+1])) continue; // End of number not reached
        if(isDigit(source[i-1])){
            // Backtrack to check if the number is floating point
            int shouldBeCoerced = 0;
            for(int j=1; 1; ++j){
                if(isDigit(source[i-j])) continue;
                if(isValidFunctionName(source[i-j])) break; // Function or variable name, don't coerce
                if(source[i-j] == '.' || ((source[i-j] == '+' || source[i-j] == '-') && (source[i-j-1] == 'e'|| source[i-j-1] == 'E') )) break; // No coercion, float or scientific notation already
                // Nothing found, should be coerced then
                shouldBeCoerced = 1;
                break;
            }

            if(!shouldBeCoerced) continue;
        }

        // Check if we have the scientific notation
        if(((source[i-1] == '+' || source[i-1] == '-') && (source[i-2] == 'e'|| source[i-2] == 'E'))) continue;


        // Remove the potential uint literal marking
        if(source[i+1] == 'u') source[i+1] = ' ';

        // Now we know there is nothing related to the digit, turn it into a float
        source = InplaceInsertByIndex(source, sourceLength, i+1, ".0");
    }

    // TODO Hacks for special built in values and typecasts ?
    source = gl4es_inplace_replace_simple(source, sourceLength, "gl_VertexID", "float(gl_VertexID)");
    source = gl4es_inplace_replace_simple(source, sourceLength, "gl_InstanceID", "float(gl_InstanceID)");

    return source;
}

/**
 * Wrap functions return statement if they have an int typecast
 * @param source The shader as a string
 * @param sourceLength The length of the shader
 */
char * FixReturnTypes(char * source, int * sourceLength) {
    unsigned long offset = 0;
    while (1){
        // Find a return statement
        unsigned long startingIndex = strstrPos(source + offset, "return ");
        if(startingIndex == 0) break;

        // Verify we have an int typecast
        unsigned long typecastIndex = GetNextTokenPosition(source, offset + startingIndex + strlen("return "), '(', " int");
        if(typecastIndex == offset + startingIndex + strlen("return ")) {
            offset += startingIndex + strlen("return ");
            continue;
        }

        // We have a typecast, get the end on instruction and wrap it
        unsigned long endInstructionIndex = GetNextTokenPosition(source, typecastIndex, ';', "");
        source = InplaceInsertByIndex(source, sourceLength, endInstructionIndex, ")");
        source = InplaceInsertByIndex(source, sourceLength, startingIndex + offset + strlen("return "), "float(");

        offset = endInstructionIndex;
    }
    return source;
}

/** Force all array accesses to use integers by adding an explicit typecast
 * @param source The shader as a string
 * @return The shader as a string, maybe at a new memory location */
char * ForceIntegerArrayAccess(char* source, int * sourceLength){
    char * markerStart = "$";
    char * markerEnd = "`";

    // Step 1, we need to mark all [] that are empty and must not be changed
    int leftCharIndex = 0;
    for(int i=0; i< *sourceLength; ++i){
        if(source[i] == '['){
            leftCharIndex = i;
            continue;
        }
        // If a start has been found
        if(leftCharIndex){
            if(source[i] == ' ' || source[i] == '\n'){
                continue;
            }
            // We find the other side and mark both ends
            if(source[i] == ']'){
                source[leftCharIndex] = *markerStart;
                source[i] = *markerEnd;
            }
        }
        //Something else is there, abort the marking phase for this one
        leftCharIndex = 0;
    }

    // Step 2, replace the array accesses with a forced typecast version
    source = gl4es_inplace_replace_simple(source, sourceLength, "]", ")]");
    source = gl4es_inplace_replace_simple(source, sourceLength, "[", "[int(");

    // Step 3, restore all marked empty []
    source = gl4es_inplace_replace_simple(source, sourceLength, markerStart, "[");
    source = gl4es_inplace_replace_simple(source, sourceLength, markerEnd, "]");

    return source;
}


/**
 * Turn layout (location=<float>) into layout (location=<int>)
 * @param source The shader as a string
 * @param sourceLength The shader allocated length
 * @return The shader as a string, maybe in a different memory location
 */
char * ForceIntegerLayoutOutput(char *source, int *sourceLength) {
    unsigned long offset = 0;
    while (1){
        unsigned long startIndex = strstrPos(source + offset, "location");
        if(startIndex == 0) break;

        // Find the assignment
        unsigned long assignmentIndex = GetNextTokenPosition(source + offset, startIndex, '=', "");

        // Find the dot for floating point
        unsigned long dotIndex = GetNextTokenPosition(source + offset, assignmentIndex, '.', "\\)");
        if(dotIndex == assignmentIndex) {
            offset += assignmentIndex;
            continue;
        }

        unsigned long endIndex = GetNextTokenPosition(source + offset, dotIndex, ')',"");
        source = InplaceReplaceByIndex(source, sourceLength, dotIndex + offset, endIndex + offset -1, "");

        // Add offset for next iteration
        offset += endIndex;
    }

    return source;
}

/** Small helper to help evaluate whether to continue or not I guess
 * Values over 9900 are not for real operators, more like stop indicators
 * @param operator The char corresponding to the current operator position
 * @param operator2 The char corresponding to the current operator position + 1 tp either the left or right. Used to evaluate 2 char operators
 */
int GetOperatorValue(char operator, char operator2){

    if(operator == ',' || operator == ';') return 9998;


    // Operators will appear reversed depending on the direction of the shader parser
    if(operator == '|' && operator2 == '|') return 14;
    if(operator == '^' && operator2 == '^') return 13;
    if(operator == '&' && operator2 == '&') return 12;
    if(operator == '|') return 11;
    if(operator == '^') return 10;
    if(operator == '&') return 9;
    if((operator == '=' && operator2 == '=')
        || (operator == '!' && operator2 == '=')
        || (operator == '=' && operator2 == '!')) return 8;

    if(operator == '=') return 9997; // Simple assignment is last

    // Note that 6 is evaluated before 7 here
    if((operator == '<' && operator2 == '<') || (operator == '>' && operator2 == '>')) return 6;
    if(operator == '<' || operator == '>') return 7; // TODO handle <=/>= ?
    if(operator == '+' || operator == '-') return ADDITIVE_OPERATOR_VALUE;
    if(operator == '*' || operator == '/' || operator == '%') return 4;

    return NO_OPERATOR_VALUE; // Meaning no value;
}



/** Get the left or right operand, given the last index of the operator
 * It bases its ability to get operands by evaluating the priority of operators.
 * @param source The shader as a string
 * @param operatorIndex The index the operator is found
 * @param rightOperand Whether we get the right or left operator
 * @param limit The left or right index of the operand
 * @return newly allocated string with the operand
 */
char* GetOperandFromOperator(char* source, int operatorIndex, int rightOperand, int * limit){
    return GetOperandFromOperatorValueOverride(
            source,
            operatorIndex,
            rightOperand,
            limit,
            GetOperatorValue(source[operatorIndex], source[rightOperand ? operatorIndex+1 : operatorIndex-1]));
}

/** test whether a keyword in present at the left side if the index */
int TestKeyword(const char *source, unsigned long index, const char* keyword){
    unsigned keywordLength = strlen(keyword);
    for(int i=0; i < keywordLength; ++i){
        if(source[index - i] != keyword[keywordLength - 1 - i]){
            return 0;
        }
    }
    return 1;
}

char* GetOperandFromOperatorValueOverride(char* source, int operatorIndex, int rightOperand, int * limit, int overrideTokenValue){
    int parserState = 0;
    int parserDirection = rightOperand ? 1 : -1;
    int operandStartIndex = 0, operandEndIndex = 0;
    int parenthesesLeft = 0, hasFoundParentheses = 0;
    int operatorValue = overrideTokenValue;
    int lastOperator = 0; // Used to determine priority for unary operators

    char parenthesesStart = rightOperand ? '(' : ')';
    char parenthesesEnd = rightOperand ? ')' : '(';
    char bracketStart = rightOperand ? '[' : ']';
    char bracketEnd = rightOperand ? ']' : '[';
    int stringIndex = operatorIndex;

    // Get to the operand
    while (parserState == 0){
        stringIndex += parserDirection;
        if(source[stringIndex] != ' '){
            parserState = 1;
            // Place the mark
            if(rightOperand){
                operandStartIndex = stringIndex;
            }else{
                operandEndIndex = stringIndex;
            }

            // Special case for unary operator when parsing to the right
            if(GetOperatorValue(source[stringIndex], source[stringIndex+parserDirection]) == ADDITIVE_OPERATOR_VALUE ){ // 5 is +- operators
                stringIndex += parserDirection;
            }
        }
    }

    // Get to the other side of the operand, the twist is here.
    while (parenthesesLeft > 0 || parserState == 1){

        // Look for parentheses or border of case statements
        if(source[stringIndex] == parenthesesStart || source[stringIndex] == bracketStart){

            hasFoundParentheses = 1;
            parenthesesLeft += 1;
            stringIndex += parserDirection;
            continue;
        }

        if(source[stringIndex] == parenthesesEnd || source[stringIndex] == bracketEnd || source[stringIndex] == ':' ||
                TestKeyword(source, stringIndex, "case ") || TestKeyword(source, stringIndex, "return ")){
            hasFoundParentheses = 1;
            parenthesesLeft -= 1;

            // Likely to happen in a function call
            if(parenthesesLeft < 0){
                parserState = 3;
                if(rightOperand){
                    operandEndIndex = stringIndex - 1;
                }else{
                    operandStartIndex = stringIndex + 1;
                }
                continue;
            }
            stringIndex += parserDirection;
            continue;
        }

        // Small optimisation
        if(parenthesesLeft > 0){
            stringIndex += parserDirection;
            continue;
        }

        // So by now the following assumptions are made
        // 1 - We aren't between parentheses
        // 2 - No implicit multiplications are present
        // 3 - No fuckery with operators like "test = +-+-+-+-+-+-+-+-3;" although I attempt to support them

        // Higher value operators have less priority
        int currentValue = GetOperatorValue(source[stringIndex], source[stringIndex + parserDirection]);


        // The condition is different due to the evaluation order which is left to right, aside from the unary operators
        if((rightOperand ? currentValue >= operatorValue: currentValue > operatorValue)){
            if(currentValue == NO_OPERATOR_VALUE){
                if(source[stringIndex] == ' '){
                    stringIndex += parserDirection;
                    continue;
                }

                // Found an operand, so reset the operator eval for unary
                if(rightOperand) lastOperator = NO_OPERATOR_VALUE;

                // maybe it is the start of a function ?
                if(hasFoundParentheses){
                    parserState = 2;
                    continue;
                }
                // If no full () set is found, assume we didn't fully travel the operand
                stringIndex += parserDirection;
                continue;
            }

            // Special case when parsing unary operator to the right
            if(rightOperand && operatorValue == ADDITIVE_OPERATOR_VALUE && lastOperator < currentValue){
                stringIndex += parserDirection;
                continue;
            }

            // Stop, we found an operator of same worth.
            parserState = 3;
            if(rightOperand){
                operandEndIndex = stringIndex - 1;
            }else{
                operandStartIndex = stringIndex + 1;
            }
        }

        // Special case for unary operators from the right
        if(rightOperand && operatorValue == ADDITIVE_OPERATOR_VALUE) { // 5 is + - operators
            lastOperator = currentValue;
        } // Special case for unary operators from the left
        if(!rightOperand && operatorValue < ADDITIVE_OPERATOR_VALUE && currentValue == ADDITIVE_OPERATOR_VALUE){
            lastOperator = NO_OPERATOR_VALUE;
            for(int j=1; 1; ++j){
                int subCurrentValue = GetOperatorValue(source[stringIndex - j], source[stringIndex - j + parserDirection]);
                if(subCurrentValue != NO_OPERATOR_VALUE){
                    lastOperator = subCurrentValue;
                    continue;
                }

                // No operator value, can be almost anything
                if(source[stringIndex - j] == ' ') continue;
                // Else we found something. Did we find a high priority operator ?
                if(lastOperator <= operatorValue){ // If so, we allow continuing and going out of the loop
                    stringIndex -= j;
                    parserState = 1;
                    break;
                }
                // No other operator found
                operandStartIndex = stringIndex;
                parserState = 3;
                break;
            }
        }
        stringIndex += parserDirection;
    }

    // Status when we get the name of a function and nothing else.
    while (parserState == 2){
        if(source[stringIndex] != ' '){
            stringIndex += parserDirection;
            continue;
        }
        if(rightOperand){
            operandEndIndex = stringIndex - 1;
        }else{
            operandStartIndex = stringIndex + 1;
        }
        parserState = 3;
    }

    // At this point, we know both the start and end point of our operand, let's copy it
    char * operand = malloc(operandEndIndex - operandStartIndex + 2);
    memcpy(operand, source+operandStartIndex, operandEndIndex - operandStartIndex + 1);
    // Make sure the string is null terminated
    operand[operandEndIndex - operandStartIndex + 1] = '\0';

    // Send back the limitIndex
    *limit = rightOperand ? operandEndIndex : operandStartIndex;

    return operand;
}

/**
 * Replace any gl_FragData[n] reference by creating an out variable with the manual layout binding
 * @param source  The shader source as a string
 * @return The shader as a string, maybe at a different memory location
 */
char * ReplaceGLFragData(char * source, int * sourceLength){

    // 10 is arbitrary, but I don't expect the shader to use so many
    // TODO I guess the array could be accessed with one or more spaces :think:
    // TODO wait they can access via a variable !
    for (int i = 0; i < 10; ++i) {
        // Check for 2 forms on the glFragData and take the first one found
        char needle[30];
        sprintf(needle, "gl_FragData[%i]", i);

        // Skip if the draw buffer isn't used at this index
        char * useFragData = strstr(source, &needle[0]);
        if(useFragData == NULL){
            sprintf(needle, "gl_FragData[int(%i.0)]", i);
            useFragData = strstr(source, &needle[0]);
            if(useFragData == NULL) continue;
        }

        // Construct replacement string
        char replacement[20];
        char replacementLine[70];
        sprintf(replacement, "vgpu_FragData%i", i);
        sprintf(replacementLine, "\nlayout(location = %i) out mediump vec4 %s;\n", i, replacement);
        int insertPoint = FindPositionAfterDirectives(source);

        // And place them into the shader
        source = gl4es_inplace_replace_simple(source, sourceLength, &needle[0], &replacement[0]);
        source = InplaceInsertByIndex(source, sourceLength, insertPoint + 1, &replacementLine[0]);
    }
    return source;
}

/**
 * Replace the gl_FragColor
 * @param source The shader as a string
 * @return The shader a a string, maybe in a different memory location
 */
char * ReplaceGLFragColor(char * source, int * sourceLength){
    if(strstr(source, "gl_FragColor")){
        source = gl4es_inplace_replace_simple(source, sourceLength, "gl_FragColor", "vgpu_FragColor");
        int insertPoint = FindPositionAfterDirectives(source);
        source = InplaceInsertByIndex(source, sourceLength, insertPoint + 1, "out mediump vec4 vgpu_FragColor;\n");
    }
    return source;
}

/**
 * Remove all extensions right now by replacing them with spaces
 * @param source The shader as a string
 * @return The shader as a string, maybe in a different memory location
 */
char * RemoveUnsupportedExtensions(char * source){
    //TODO remove only specific extensions ?
    for(char * extensionPtr = strstr(source, "#extension "); extensionPtr; extensionPtr = strstr(source, "#extension ")){
        int i = 0;
        while(extensionPtr[i] != '\n'){
            extensionPtr[i] = ' ';
            ++i;
        }
    }
    return source;
}

/**
 * Replace the variable name in a shader, mostly used to avoid keyword clashing
 * @param source The shader as a string
 * @param initialName The initial name for the variable
 * @param newName The new name for the variable
 * @return The shader as a string, maybe in a different memory location
 */
char * ReplaceVariableName(char * source, int * sourceLength, char * initialName, char* newName) {

    char * toReplace = malloc(strlen(initialName) + 3);
    char * replacement = malloc(strlen(newName) + 3);
    char * charBefore = "{}([];+-*/~!%<>,&| \n\t";
    char * charAfter = ")[];+-*/%<>;,|&. \n\t";

    // Prepare the fixed part of the strings
    strcpy(toReplace+1, initialName);
    toReplace[strlen(initialName)+2] = '\0';

    strcpy(replacement+1, newName);
    replacement[strlen(newName)+2] = '\0';

    for (int i = 0; i < strlen(charBefore); ++i) {
        for (int j = 0; j < strlen(charAfter); ++j) {
            // Prepare the string to replace
            toReplace[0] = charBefore[i];
            toReplace[strlen(initialName)+1] = charAfter[j];

            // Prepare the replacement string
            replacement[0] = charBefore[i];
            replacement[strlen(newName)+1] = charAfter[j];

            source = gl4es_inplace_replace_simple(source, sourceLength, toReplace, replacement);
        }
    }

    free(toReplace);
    free(replacement);

    return source;
}

/**
 * Look if the variable name is present
 * @return 1 if the variable is present, 0 otherwise
 */
int IsVariableNamePresent(const char * source, const int * sourceLength, const char * variableName) {
    char * toReplace = malloc(strlen(variableName) + 3);
    char * charBefore = "{}([];+-*/~!%<>,&| \n\t";
    char * charAfter = ")[];+-*/%<>;,|&. \n\t";

    unsigned variableLength = strlen(variableName);

    // Prepare the fixed part of the strings
    strcpy(toReplace + 1, variableName);
    toReplace[variableLength + 2] = '\0';

    for (int i = 0; i < strlen(charBefore); ++i) {
        for (int j = 0; j < strlen(charAfter); ++j) {
            toReplace[0] = charBefore[i];
            toReplace[variableLength+1] = charAfter[j];

            // Look if the combination exists
            if (strstr(source, toReplace) != NULL) {
                return 1;
            }
        }
    }

    free(toReplace);

    return 0;
}

/**
 * Replace a function definition and calls to the function to another name
 * @param source The shader as a string
 * @param sourceLength The shader length
 * @param initialName The name be to changed
 * @param finalName The name to use instead
 * @return The shader as a string, maybe in a different memory location
 */
char * ReplaceFunctionName(char * source, int * sourceLength, char * initialName, char * finalName){
    for(unsigned long currentPosition = 0; 1; currentPosition += strlen(initialName)){
        unsigned long newPosition = strstrPos(source + currentPosition, initialName);
        if(newPosition == 0) // No more calls
            break;

        // Check if that is indeed a function call on the right side
        if (source[GetNextTokenPosition(source, currentPosition + newPosition + strlen(initialName), '(', " \n\t")] != '('){
            currentPosition += newPosition;
            continue; // Skip to the next potential call
        }

        // Check the naming on the left side
        if (isValidFunctionName(source[currentPosition + newPosition - 1])){
            currentPosition += newPosition;
            continue; // Skip to the next potential call
        }

        // This is a valid function call/definition, replace it
        source = InplaceReplaceByIndex(source, sourceLength, currentPosition + newPosition, currentPosition + newPosition + strlen(initialName) - 1, finalName);
        currentPosition += newPosition;
    }
    return source;
}

/**
 * Remove all "uniform" keywords from uniform variables with a default initializer.
 * The default "uniform" initializer is not part of the GLSL ES specification.
 * @param source The shader as a string
 * @param sourceLength The allocated length of the shader
 * @return The shader as a string, maybe in a different memory position. Probably not here though.
 */
char * RemoveUniformProperty(char * source){
    unsigned long currentPosition = 0;
    while(1){
        unsigned long newPosition = strstrPos(source + currentPosition, "uniform ");
        if(newPosition == 0)  // No more uniform vars
            break;

        // Now, get to the end of declaration/initialization
        int endPosition = GetNextTokenPosition(source + currentPosition + newPosition, 0, ';', "\\=");
        if (endPosition == 0) {
            // It tripped at the =, meaning there is an init phase, remove the uniform tag
            for(int i = 0; i<8; ++i){
                source[currentPosition + newPosition + i] = ' ';
            }
        }
        currentPosition += newPosition + strlen("uniform");
    }

    return source;
}

/** Remove 'const ' storage qualifier from variables inside {..} blocks
 * @param source The pointer to the start of the shader */
char * RemoveConstInsideBlocks(char* source, int * sourceLength){
    int insideBlock = 0;
    char * keyword = "const \0";
    int keywordLength = strlen(keyword);


    for(int i=0; i< *sourceLength+1; ++i){
        // Step 1, look for a block
        if(source[i] == '{'){
            insideBlock += 1;
            continue;
        }
        if(!insideBlock) continue;

        // A block has been found, look for the keyword
        if(source[i] == keyword[0]){
            int keywordMatch = 1;
            int j = 1;
            while (j < keywordLength){
                if (source[i+j] != keyword[j]){
                    keywordMatch = 0;
                    break;
                }
                j +=1;
            }
            // A match is found, remove it
            if(keywordMatch){
                source = InplaceReplaceByIndex(source, sourceLength, i, i+j - 1, " ");
                continue;
            }
        }

        // Look for an exit
        if(source[i] == '}'){
            insideBlock -= 1;
            continue;
        }
    }
    return source;
}

/** Find the first point which is right after most shader directives
 * @param source The shader as a string
 * @return The index position after the #version line, start of the shader if not found
 */
int FindPositionAfterDirectives(char * source){
    const char * position = strstr(source, "#version");
    if (position == NULL) return 0;
    for(int i=7; 1; ++i){
        if(position[i] == '\n'){
            if(position[i+1] == '#') continue; // a directive is present right after, skip
            return i;
        }
    }
}

int FindPositionAfterVersion(const char * source){
    const char * position = strstr(source, "#version");
    if (position == NULL) return 0;
    for(int i=7; 1; ++i){
        if(position[i] == '\n'){
            return i;
        }
    }
}

/**
 * Replace and inserts precision qualifiers as necessary, see LIBGL_VGPU_PRECISION
 * @param source The shader as a string
 * @param sourceLength The length of the string
 * @return The shader as a string, maybe in a different memory location
 */
char * ReplacePrecisionQualifiers(char * source, int * sourceLength, int isVertex){

    if(!doesShaderVersionContainsES(source)){
#ifdef SHADERCONV_DEBUG
            printf("\nSKIPPING the replacement qualifiers step\n");
#endif
        return source;
    }

    // Step 1 is to remove any "precision" qualifiers
    for(unsigned long currentPosition=strstrPos(source, "precision "); currentPosition>0;currentPosition=strstrPos(source, "precision ")){
        // Once a qualifier is found, get to the end of the instruction and replace
        int endPosition = GetNextTokenPosition(source, currentPosition, ';', "");
        source = InplaceReplaceByIndex(source, sourceLength, currentPosition, endPosition,"");
    }

    // Step 2 is to insert precision qualifiers, even the ones we think are defaults, since there are defaults only for some types

    int insertPoint = FindPositionAfterDirectives(source);
    source = InplaceInsertByIndex(source, sourceLength, insertPoint,
                                  "\nprecision lowp sampler2D;\n"
                                  "precision lowp sampler3D;\n"
                                  "precision lowp sampler2DShadow;\n"
                                  "precision lowp samplerCubeShadow;\n"
                                  "precision lowp sampler2DArray;\n"
                                  "precision lowp sampler2DArrayShadow;\n"
                                  "precision lowp samplerCube;\n"
                                  "#ifdef GL_EXT_texture_buffer\n"
                                  "precision lowp samplerBuffer;\n"
                                  "precision lowp imageBuffer;\n"
                                  "#endif\n"
                                  "#ifdef GL_EXT_texture_cube_map_array\n"
                                  "precision lowp imageCubeArray;\n"
                                  "precision lowp samplerCubeArray;\n"
                                  "precision lowp samplerCubeArrayShadow;\n"
                                  "#endif\n"
                                  "#ifdef GL_OES_texture_storage_multisample_2d_array\n"
                                  "precision lowp sampler2DMS;\n"
                                  "precision lowp sampler2DMSArray;\n"
                                  "#endif\n");

    if(GetShaderVersion(source) > 300){
        source = InplaceInsertByIndex(source, sourceLength,insertPoint,
                                      "\nprecision lowp image2D;\n"
                                      "precision lowp image2DArray;\n"
                                      "precision lowp image3D;\n"
                                      "precision lowp imageCube;\n");
    }
    int supportHighp = 1;
    source = InplaceInsertByIndex(source, sourceLength, insertPoint, supportHighp ? "\nprecision highp float;\n" : "\nprecision medium float;\n");

    if (2){
        char * target_precision;
        switch (2) {
            case 1: target_precision = "highp"; break;
            case 2: target_precision = "mediump"; break;
            case 3: target_precision = "lowp"; break;
            default: target_precision = "highp";
        }
        source = ReplaceVariableName(source, sourceLength, "highp", target_precision);
        source = ReplaceVariableName(source, sourceLength, "mediump", target_precision);
        source = ReplaceVariableName(source, sourceLength, "lowp", target_precision);
    }

    return source;
}


/**
 * Simplify <int(const float)> to <const int>
 * @param source The shader as a string
 * @param sourceLength The length of the shader
 * @param sour
 */
char * SimplifyConstIntTypecasts(char * source, int * sourceLength) {
    unsigned long currentPosition = 0;
    while (1){
        // Find the start of a typecast
        unsigned long startPos = strstrPos(source + currentPosition, "int(");
        if(startPos == 0) return source;

        // Go to the end of said typecast
        unsigned long endPos = GetNextTokenPosition(source + currentPosition, startPos + 4, ')', "  (\n\t\r01234567890.-");
        if(endPos == startPos + 4) {
            currentPosition += startPos + 3;
            continue;
        }

        // Find the floating dot
        unsigned long dotPos = GetNextTokenPosition(source + currentPosition, startPos, '.', "\\)");
        if(dotPos == startPos) {
            currentPosition += startPos + 3;
            continue;
        }

        // Then replace the cast
        source = InplaceReplaceByIndex(source, sourceLength, currentPosition + dotPos, currentPosition + endPos, "");
        source = InplaceReplaceByIndex(source, sourceLength, currentPosition + startPos, currentPosition + startPos + 3, "");

        currentPosition += startPos;
    }
    return source;
}

/**
 * Simplify <int(int(float))> to <int(float)>
 * @param source The shader as a string
 * @param sourceLength The shader length
 * @return The shader as a string, probably not in a different memory location
 */
char * SimplifyRedundantIntTypecasts(char * source, int * sourceLength) {
    for(int i=0; i < strlen(source) - 4; ++i){
        if(source[i] != 'i' || source[i+1] != 'n' || source[i+2] != 't' || source[i+3] != '(') continue;
        // Get the next parentheses opening
        const int secondParenthesesIndex = GetNextTokenPosition(source, i+3, '(', " int");
        if(secondParenthesesIndex == i+3) continue;

        // Get to the second parentheses end
        const int thirdParenthesesIndex = GetClosingTokenPosition(source, secondParenthesesIndex);
        if(thirdParenthesesIndex == secondParenthesesIndex) continue;

        // Then the first parentheses end
        const int fourthParenthesesIndex = GetNextTokenPosition(source, thirdParenthesesIndex, ')', " ");
        if(fourthParenthesesIndex == thirdParenthesesIndex) continue;

        // Redundant parentheses found, remove them
        source = InplaceReplaceByIndex(source, sourceLength, fourthParenthesesIndex, fourthParenthesesIndex, "");
        source = InplaceReplaceByIndex(source, sourceLength, i, i+3, "");
        i--;
    }

    return source;
}


/**
 * Simplify ((<operationHere>)) into (<operationHere>)
 * @param source The shader as a string
 * @param sourceLength The length of the shader
 */
char * SimplifyRedundantParentheses(char * source, int * sourceLength){
    for(int i=0; i < strlen(source); ++i){
        if(source[i] != '(') continue;
        // Get the next parentheses opening
        const int secondParenthesesIndex = GetNextTokenPosition(source, i, '(', " ");
        if(secondParenthesesIndex == i) continue;

        // Get to the second parentheses end
        const int thirdParenthesesIndex = GetClosingTokenPosition(source, secondParenthesesIndex);
        if(thirdParenthesesIndex == secondParenthesesIndex) continue;

        // Then the first parentheses end
        const int fourthParenthesesIndex = GetNextTokenPosition(source, thirdParenthesesIndex, ')', " ");
        if(fourthParenthesesIndex == thirdParenthesesIndex) continue;

        // Redundant parentheses found, remove them
        source = InplaceReplaceByIndex(source, sourceLength, fourthParenthesesIndex, fourthParenthesesIndex, "");
        source = InplaceReplaceByIndex(source, sourceLength, i, i, "");
        i--;
    }

    return source;
}

/**
 * @param openingToken The opening token
 * @return All closing tokens, if available
 */
char * GetClosingTokens(char openingToken){
    switch (openingToken) {
        case '(': return ")";
        case '[': return "]";
        case ',': return ",)";
        case '{': return "}";
        case ';': return ";";

        default: return "";
    }
}

/**
 * @param openingToken The opening token
 * @return Whether the token is an opening token
 */
int isOpeningToken(char openingToken){
    return openingToken != ',' && strlen(GetClosingTokens(openingToken)) != 0;
}

int GetClosingTokenPosition(const char * source, int initialTokenPosition){
    return GetClosingTokenPositionTokenOverride(source, initialTokenPosition, source[initialTokenPosition]);
}

/**
 * Get the index of the closing token within a string, same as initialTokenPosition if not found
 * @param source The string to look into
 * @param initialTokenPosition The opening token position
 * @return The closing token position
 */
int GetClosingTokenPositionTokenOverride(const char * source, int initialTokenPosition, char initialToken){
    // Step 1: Determine the closing token
    char openingToken = initialToken;
    char * closingTokens = GetClosingTokens(openingToken);

    if (strlen(closingTokens) == 0){
        printf("No closing tokens, somehow \n");
        return initialTokenPosition;
    }

    // Step 2: Go through the string to find what we want
    for(int i=initialTokenPosition+1; i<strlen(source); ++i){
        // Loop though all the available closing tokens first, since opening/closing tokens can be identical
        for(int j=0; j<strlen(closingTokens); ++j){
            if (source[i] == closingTokens[j]){
                return i;
            }
        }

        if (isOpeningToken(source[i])){
            i = GetClosingTokenPosition(source, i);
            continue;
        }
    }
    printf("No closing tokens 2 , somehow \n");
    return initialTokenPosition; // Nothing found
}



/**
 * Return the position of the first token corresponding to what we want
 * @param source The source string
 * @param initialPosition The starting position to look from
 * @param token The token you want to find
 * @param acceptedChars If started with a backslash, all chars you will trip on.
 * Otherwise, all chars we can go over without tripping. Empty means all chars are allowed.
 * @return
 */
int GetNextTokenPosition(const char * source, int initialPosition, const char token, const char * acceptedChars){
    int inverseTripping = strlen(acceptedChars) > 0 && acceptedChars[0] == '\\';

    for(int i=initialPosition+1; i< strlen(source); ++i){
        if (source[i] == token){
            return i;
        }

        // Tripping check
        if(strlen(acceptedChars) > 0){
            int acceptedCharFound = 0;
            for(int j=0; j< strlen(acceptedChars); ++j){
                if (source[i] == acceptedChars[j]) {
                    if(inverseTripping)  // Tripped, break out.
                        return initialPosition;
                    // Else, we're good, no need to do more checks
                    acceptedCharFound = 1;
                    break;
                }
            }
            if(!inverseTripping && !acceptedCharFound)
                return initialPosition; // Tripped, meaning the accepted token is not found
        }


    }
    return initialPosition;
}

/**
 * @param haystack
 * @param needle
 * @return The position of the first occurrence of the needle in the haystack
 */
unsigned long strstrPos(const char * haystack, const char * needle){
    char * substr = strstr(haystack, needle);
    if (substr == NULL) return 0;
    return (substr - haystack);
}

/**
 * Inserts int(...) on a specific argument from each call to the function
 * @param source The shader as a string
 * @param functionName The name of the function to manipulate
 * @param argumentPosition The position of the argument to manipulate, from 0. If not found, no changes are made.
 * @return The shader as a string, maybe in a different memory location
 */
char * insertIntAtFunctionCall(char * source, int * sourceSize, const char * functionName, int argumentPosition){
    //TODO a less naive function for edge-cases ?
    unsigned long functionCallPosition = strstrPos(source, functionName);
    while(functionCallPosition != 0){
        int openingTokenPosition = GetNextTokenPosition(source, functionCallPosition + strlen(functionName), '(', " \n\r\t");
        if (source[openingTokenPosition] == '('){
            // Function call found, determine the start and end of the argument
            int endArgPos = openingTokenPosition;
            int startArgPos = openingTokenPosition;

            // Note the additional check to see we aren't at the end of a function
            for(int argCount=0; argCount<=argumentPosition && source[startArgPos] != ')'; ++argCount){
                endArgPos = GetClosingTokenPositionTokenOverride(source, endArgPos, ',');
                if (argCount == argumentPosition){
                    // Argument found, insert the int(...)
                    source = InplaceInsertByIndex(source, sourceSize, endArgPos+1, ")");
                    source = InplaceInsertByIndex(source, sourceSize, startArgPos+1, "int(");
                    break;
                }
                // Not the arg we want, got to the next one
                startArgPos = endArgPos;
            }
        }

        // Get the next function call
        unsigned long offset = strstrPos(source + functionCallPosition + strlen(functionName), functionName);
        if (offset == 0) break; // No more function calls
        functionCallPosition += offset + strlen(functionName);
    }
    return source;
}

/** Convenience macro to test shader versions */
#define test_version_context(version, context) \
    if(strstr(versionString, "#version "#version " " #context) != NULL ){ free(versionString); return version; }

#define test_version(version) \
    if(strstr(versionString, "#version "#version) != NULL ){ free(versionString); return version; }

/**
 * @param source The shader as a string
 * @return The shader version: eg. 310 for #version 310 es
 */
int GetShaderVersion(const char * source) {
    int endVersionPosition = FindPositionAfterVersion(source);
    char * versionString = ExtractString(source, 0, endVersionPosition);

    test_version(150)
    test_version(100)
    test_version_context(320, es)
    test_version_context(310, es)
    test_version_context(300, es)

    test_version(120)
    test_version(110)
    test_version(140)
    test_version(130)

    test_version(460)
    test_version(450)
    test_version(440)
    test_version(430)
    test_version(420)
    test_version(410)
    test_version(400)

    free(versionString);
    return 100;
}
