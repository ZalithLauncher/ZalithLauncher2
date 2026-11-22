//
// Created by movte on 2026/11/22.
//
#define _GNU_SOURCE
#include "event_wait.h"

#include <errno.h>
#include <limits.h>
#include <linux/futex.h>
#include <stdatomic.h>
#include <stdint.h>
#include <sys/syscall.h>
#include <time.h>
#include <unistd.h>

_Static_assert(sizeof(_Atomic uint32_t) == 4, "futex word must be 32-bit");

static _Atomic uint32_t pending;

static int futex(_Atomic uint32_t *addr, int op, uint32_t val, struct timespec *timeout) {
    return (int)syscall(SYS_futex, addr, op, val, timeout, NULL, 0);
}

static int64_t monotonic_ns(void) {
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (int64_t)now.tv_sec * INT64_C(1000000000) + now.tv_nsec;
}

void event_wait_notify(void) {
    atomic_fetch_add_explicit(&pending, 1, memory_order_release);
    (void)futex(&pending, FUTEX_WAKE_PRIVATE, INT_MAX, NULL);
}

void event_wait_block(double seconds, bool timed) {
    if (atomic_exchange_explicit(&pending, 0, memory_order_acq_rel) != 0)
        return;
    int64_t deadline = 0;
    if (timed) {
        double ns = seconds * 1e9;
        if (!(ns > 0.0)) return; // NaN 与非正数一律视为立即返回
        if (ns > 1e15) ns = 1e15; // 钳到约 11.6 天，远离 int64 溢出
        deadline = monotonic_ns() + (int64_t)ns;
    }
    for (;;) {
        struct timespec remaining, *timeout = NULL;
        if (timed) {
            int64_t left = deadline - monotonic_ns();
            if (left <= 0) return;
            remaining.tv_sec = left / INT64_C(1000000000);
            remaining.tv_nsec = left % INT64_C(1000000000);
            timeout = &remaining;
        }
        if (futex(&pending, FUTEX_WAIT_PRIVATE, 0, timeout) < 0 &&
            errno != EINTR && errno != EAGAIN && errno != ETIMEDOUT)
            return;
        if (atomic_exchange_explicit(&pending, 0, memory_order_acq_rel) != 0)
            return;
    }
}
