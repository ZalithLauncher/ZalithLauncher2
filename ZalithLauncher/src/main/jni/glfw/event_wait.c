#define _GNU_SOURCE
#include "event_wait.h"
#include <errno.h>
#include <limits.h>
#include <linux/futex.h>
#include <math.h>
#include <sys/syscall.h>
#include <time.h>
#include <unistd.h>

_Static_assert(ATOMIC_INT_LOCK_FREE == 2, "Event sequence must be lock-free");
_Static_assert(sizeof(_Atomic uint32_t) == 4, "Futex words are 32-bit");

static int notify(struct gw_event_wait *events) {
    atomic_fetch_add_explicit(&events->sequence, 1, memory_order_release);
    return syscall(SYS_futex, &events->sequence, FUTEX_WAKE_PRIVATE, INT_MAX, NULL, NULL, 0) < 0
               ? GW_EVENT_PLATFORM_ERROR
               : GW_EVENT_READY;
}

void gw_events_reset(struct gw_event_wait *events) {
    uint32_t current = atomic_load_explicit(&events->sequence, memory_order_acquire);
    atomic_store_explicit(&events->consumed, current, memory_order_release);
    events->pumping = current;
    atomic_store_explicit(&events->active, true, memory_order_release);
}

void gw_events_stop(struct gw_event_wait *events) {
    atomic_store_explicit(&events->active, false, memory_order_release);
    (void)notify(events);
}

int gw_events_post(struct gw_event_wait *events) {
    if (!atomic_load_explicit(&events->active, memory_order_acquire))
        return GW_EVENT_NOT_INITIALIZED;
    return notify(events);
}

void gw_events_begin_pump(struct gw_event_wait *events) {
    /* Capture before the existing input queue snapshot. Producers arriving
       during dispatch must remain pending for the next wait/poll operation. */
    events->pumping = atomic_load_explicit(&events->sequence, memory_order_acquire);
}

void gw_events_end_pump(struct gw_event_wait *events) {
    atomic_store_explicit(&events->consumed, events->pumping, memory_order_release);
}

static int64_t monotonic_ns(void) {
    struct timespec now;
    if (clock_gettime(CLOCK_MONOTONIC, &now))
        return -1;
    return (int64_t)now.tv_sec * INT64_C(1000000000) + now.tv_nsec;
}

int gw_events_wait(struct gw_event_wait *events, double seconds, bool timed) {
    if (!atomic_load_explicit(&events->active, memory_order_acquire))
        return GW_EVENT_NOT_INITIALIZED;
    if (timed && (!(seconds > 0) || !isfinite(seconds)))
        return GW_EVENT_INVALID_VALUE;
    int64_t deadline = 0;
    if (timed) {
        int64_t now = monotonic_ns();
        if (now < 0)
            return GW_EVENT_PLATFORM_ERROR;
        /* Very large finite GLFW timeouts remain cancellable without integer overflow. */
        long double requested_ns = (long double)seconds * 1e9L;
        if (requested_ns >= (long double)(INT64_MAX - now))
            deadline = INT64_MAX;
        else {
            int64_t duration = (int64_t)requested_ns;
            deadline = now + (duration > 0 ? duration : 1);
        }
    }
    for (;;) {
        uint32_t observed = atomic_load_explicit(&events->sequence, memory_order_acquire);
        if (!atomic_load_explicit(&events->active, memory_order_acquire))
            return GW_EVENT_NOT_INITIALIZED;
        if (observed != atomic_load_explicit(&events->consumed, memory_order_acquire))
            return GW_EVENT_READY;
        struct timespec remaining, *timeout = NULL;
        if (timed) {
            int64_t now = monotonic_ns();
            if (now < 0)
                return GW_EVENT_PLATFORM_ERROR;
            if (now >= deadline)
                return GW_EVENT_TIMEOUT;
            int64_t left = deadline - now;
            remaining.tv_sec = left / INT64_C(1000000000);
            remaining.tv_nsec = left % INT64_C(1000000000);
            timeout = &remaining;
        }
        /* A post between the predicate and this syscall changes the word:
           FUTEX_WAIT returns EAGAIN instead of losing the wakeup. No callback
           runs while a wait lock is held. EINTR/spurious wakeups recheck state. */
        long rc =
            syscall(SYS_futex, &events->sequence, FUTEX_WAIT_PRIVATE, observed, timeout, NULL, 0);
        if (rc < 0 && errno != EINTR && errno != EAGAIN && errno != ETIMEDOUT)
            return GW_EVENT_PLATFORM_ERROR;
    }
}
