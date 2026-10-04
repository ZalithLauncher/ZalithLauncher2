//
// Created by movte on 2025/4/25.
//

#ifndef ZALITHLAUNCHER_LOGGER_H
#define ZALITHLAUNCHER_LOGGER_H

#define LOG_E "ERROR"
#define LOG_W "WARN"
#define LOG_I "INFO"
#define LOG_D "DEBUG"

#define LOG_TO_E(...) zl_log(LOG_E, __VA_ARGS__)
#define LOG_TO_W(...) zl_log(LOG_W, __VA_ARGS__)
#define LOG_TO_I(...) zl_log(LOG_I, __VA_ARGS__)
#define LOG_TO_D(...) zl_log(LOG_D, __VA_ARGS__)

void zl_log(const char *level, const char *fmt, ...);

#endif // ZALITHLAUNCHER_LOGGER_H