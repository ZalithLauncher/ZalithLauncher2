//
// Created by movte on 2026/11/22.
//
#ifndef GLFW_EVENT_WAIT_H
#define GLFW_EVENT_WAIT_H

#include <stdbool.h>

void event_wait_notify(void);
void event_wait_block(double seconds, bool timed);

#endif
