#include <stdio.h>
#include "build_info.h"
#include "logs.h"
#include "../../version.h"

void print_build_infos()
{
	SHUT_LOGD("v%d.%d.%d built on %s %s\n", MAJOR, MINOR, REVISION, __DATE__, __TIME__);
}
