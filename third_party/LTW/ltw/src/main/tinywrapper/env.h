//
// Created by maks on 12.07.2025.
//

#ifndef GL4ES_WRAPPER_ENV_H
#define GL4ES_WRAPPER_ENV_H

#include <stdbool.h>

bool env_istrue(const char* name);
bool env_istrue_d(const char* name, bool _default);

#endif //GL4ES_WRAPPER_ENV_H
