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

/* Timed waits end with a short in-place spin so the caller observes the
   deadline directly instead of paying the kernel wake-up latency (~1-3 ms
   on Android, deep idle exit included). The spin window is the EWMA of the
   measured futex wake-up delay plus a fixed safety buffer. */
enum {
    /* EWMA update: new = avg + (sample - avg) / 10 */
    GW_OVERSLEEP_SMOOTHING_DEN = 10,
    /* One-off samples beyond this are outliers (e.g. a GC pause) and are
       ignored so they cannot poison the average. */
    GW_MAX_SINGLE_OVERSLEEP_NS = 25000000,
    /* Cap on the EWMA itself. Android wake-up latency (deep idle exit plus
       scheduler delay on little cores) runs up to ~4 ms, well above the
       ~2 ms seen on desktop JVMs, so the cap must not clip the learned
       delay on mid/low-end devices; the spin always covers whatever is left. */
    GW_MAX_AVERAGE_OVERSLEEP_NS = 4000000,
    /* Always keep this much slack for the spin, even with no samples yet. */
    GW_SPIN_SAFETY_BUFFER_NS = 500000
};

#if defined(__aarch64__) || defined(__arm__)
#define gw_cpu_relax() __asm__ __volatile__("yield" ::: "memory")
#elif defined(__x86_64__) || defined(__i386__)
#define gw_cpu_relax() __asm__ __volatile__("pause" ::: "memory")
#else
#define gw_cpu_relax() do { } while (0)
#endif

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
    events->oversleep_ewma = 0;
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

/* Sample the futex wake-up delay and fold it into the EWMA. Only positive
   in-range samples count: waking early (event, signal, spurious) or after a
   huge stall says nothing about the routine wake-up latency. */
static void note_oversleep(struct gw_event_wait *events, int64_t oversleep) {
    if (oversleep <= 0 || oversleep > GW_MAX_SINGLE_OVERSLEEP_NS)
        return;
    int64_t average = events->oversleep_ewma +
                      (oversleep - events->oversleep_ewma) / GW_OVERSLEEP_SMOOTHING_DEN;
    if (average > GW_MAX_AVERAGE_OVERSLEEP_NS)
        average = GW_MAX_AVERAGE_OVERSLEEP_NS;
    events->oversleep_ewma = average;
}

/* Spin the last stretch before the deadline instead of re-entering the
   kernel: wake-up latency there is microseconds, not milliseconds. Events
   and shutdown still end the spin immediately. */
static int spin_until_deadline(struct gw_event_wait *events, int64_t deadline) {
    for (;;) {
        if (!atomic_load_explicit(&events->active, memory_order_acquire))
            return GW_EVENT_NOT_INITIALIZED;
        if (atomic_load_explicit(&events->sequence, memory_order_acquire) !=
            atomic_load_explicit(&events->consumed, memory_order_acquire))
            return GW_EVENT_READY;
        int64_t now = monotonic_ns();
        if (now < 0)
            return GW_EVENT_PLATFORM_ERROR;
        if (now >= deadline)
            return GW_EVENT_TIMEOUT;
        gw_cpu_relax();
    }
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
        /* Sleep only up to the scheduled wake-up point short of the deadline;
           the remaining stretch is covered by the spin. */
        int64_t sleep_until = 0;
        if (timed) {
            int64_t now = monotonic_ns();
            if (now < 0)
                return GW_EVENT_PLATFORM_ERROR;
            if (now >= deadline)
                return GW_EVENT_TIMEOUT;
            int64_t left = deadline - now;
            int64_t spin_window = events->oversleep_ewma + GW_SPIN_SAFETY_BUFFER_NS;
            if (left <= spin_window)
                return spin_until_deadline(events, deadline);
            sleep_until = deadline - spin_window;
            remaining.tv_sec = (sleep_until - now) / INT64_C(1000000000);
            remaining.tv_nsec = (sleep_until - now) % INT64_C(1000000000);
            timeout = &remaining;
        }
        /* A post between the predicate and this syscall changes the word:
           FUTEX_WAIT returns EAGAIN instead of losing the wakeup. No callback
           runs while a wait lock is held. EINTR/spurious wakeups recheck state. */
        long rc =
            syscall(SYS_futex, &events->sequence, FUTEX_WAIT_PRIVATE, observed, timeout, NULL, 0);
        if (timed) {
            /* How far past the scheduled wake-up point the kernel actually
               returned us: the delay the next wait must pre-subtract. */
            int64_t arrived = monotonic_ns();
            if (arrived > sleep_until)
                note_oversleep(events, arrived - sleep_until);
        }
        if (rc < 0 && errno != EINTR && errno != EAGAIN && errno != ETIMEDOUT)
            return GW_EVENT_PLATFORM_ERROR;
    }
}
