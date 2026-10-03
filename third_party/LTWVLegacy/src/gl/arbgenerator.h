#ifndef _GL4ES_ARBGENERATOR_H_
#define _GL4ES_ARBGENERATOR_H_

#include <stddef.h>

#include "arbhelper.h"

#define APPEND_OUTPUT(str, len) \
		if (appendString(curStatusPtr, str, len)) {      \
			FAIL("Unknown error (not enough memory?"); \
		}
#define APPEND_OUTPUT2(str) APPEND_OUTPUT(str, (size_t)-1)

void generateVariablePre(sCurStatus *curStatusPtr, int vertex, char **error_msg, sVariable *varPtr);
void generateInstruction(sCurStatus *curStatusPtr, int vertex, char **error_msg, sInstruction *instPtr);
void generateVariablePst(sCurStatus *curStatusPtr, int vertex, char **error_msg, sVariable *varPtr);

#endif // _GL4ES_ARBGENERATOR_H_
