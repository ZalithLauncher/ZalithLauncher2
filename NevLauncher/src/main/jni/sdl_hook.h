#ifndef ZALITH_SDL_HOOK_H
#define ZALITH_SDL_HOOK_H

/** 把 SDL 符号的 dlsym 解析结果换成 dlsym 层代理，返回 NULL 表示该符号不拦截 */
void *sdlDlsymProxy(const char *symbol, void *real);

#endif
