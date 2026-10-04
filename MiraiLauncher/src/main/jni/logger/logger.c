//
// Created by movte on 2025/4/25.
//

#include <stdarg.h>
#include <stdio.h>

void zl_log(const char *level, const char *fmt, ...) {
    va_list args;
    char buffer[1024];
    va_start(args, fmt);
    vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);

    fprintf(stderr, "[%s] %s\n", level, buffer);
}