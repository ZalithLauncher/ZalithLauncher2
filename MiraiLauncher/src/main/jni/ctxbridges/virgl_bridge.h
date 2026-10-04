//
// Created by Vera-Firefly on 20.08.2024.
//

#ifndef VIRGL_BRIDGE_H
#define VIRGL_BRIDGE_H

bool loadSymbolsVirGL();
int virglInit();
void* virglCreateContext(void* contextSrc);
void* virglGetCurrentContext();
void virglMakeCurrent(void* window);
void virglSwapBuffers();
void virglSwapInterval(int interval);

#endif //VIRGL_BRIDGE_H
