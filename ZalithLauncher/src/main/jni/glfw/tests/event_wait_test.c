/*
 * Host-side regression tests for glfw/event_wait.c.
 *
 * Compiled and executed on the development machine by
 * EventWaitNativeTest (ZalithLauncher unit tests). It links the real
 * production source, so the behavior verified here is the behavior the
 * game JVM gets through libpojavexec.
 *
 * Exit code is the number of failures; every case prints PASS/FAIL.
 */
#define _GNU_SOURCE
#include "event_wait.h"

#include <errno.h>
#include <inttypes.h>
#include <math.h>
#include <pthread.h>
#include <signal.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>

static int failures;

#define CHECK(cond, name) \
    do { \
        if (cond) { printf("PASS %s\n", name); } \
        else { failures++; printf("FAIL %s\n", name); } \
    } while (0)

#define CHECKF(cond, name, ...) \
    do { \
        if (cond) { printf("PASS %s\n", name); } \
        else { failures++; printf("FAIL %s: ", name); printf(__VA_ARGS__); printf("\n"); } \
    } while (0)

static int64_t wall_ns(void) {
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (int64_t)now.tv_sec * INT64_C(1000000000) + now.tv_nsec;
}

static double elapsed_ms(int64_t start) {
    return (double)(wall_ns() - start) / 1e6;
}

static void usleep_ms(long ms) { usleep(ms * 1000); }

/* --- 1. operations before the first reset are rejected --- */
static void test_wait_before_init(void) {
    struct gw_event_wait events = {0};
    atomic_store_explicit(&events.active, false, memory_order_release);
    CHECK(gw_events_wait(&events, 0.0, false) == GW_EVENT_NOT_INITIALIZED,
          "wait_before_init");
    CHECK(gw_events_post(&events) == GW_EVENT_NOT_INITIALIZED,
          "post_before_init");
}

/* --- 2. invalid timeouts are rejected with GLFW-style codes --- */
static void test_invalid_timeouts(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    CHECK(gw_events_wait(&events, 0.0, true) == GW_EVENT_INVALID_VALUE,
          "timeout_zero_invalid");
    CHECK(gw_events_wait(&events, -1.0, true) == GW_EVENT_INVALID_VALUE,
          "timeout_negative_invalid");
    CHECK(gw_events_wait(&events, 0.0 / 0.0, true) == GW_EVENT_INVALID_VALUE,
          "timeout_nan_invalid");
    CHECK(gw_events_wait(&events, INFINITY, true) == GW_EVENT_INVALID_VALUE,
          "timeout_infinite_invalid");
    /* Untimed waits ignore the duration argument and must not be rejected. */
    events.active = false; /* keep the untimed probe from blocking */
    CHECK(gw_events_wait(&events, -5.0, false) == GW_EVENT_NOT_INITIALIZED,
          "untimed_ignores_duration");
}

/* --- 3. timed wait without events returns TIMEOUT at the deadline --- */
static void test_timeout_deadline(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    int64_t start = wall_ns();
    int rc = gw_events_wait(&events, 0.15, true);
    double ms = elapsed_ms(start);
    CHECKF(rc == GW_EVENT_TIMEOUT && ms >= 140.0 && ms < 1500.0,
          "timeout_returns_after_deadline", "rc=%d ms=%.1f", rc, ms);
}

/* --- 4. already-pending events return without blocking --- */
static void test_pending_returns_immediately(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    gw_events_post(&events);
    int64_t start = wall_ns();
    int rc = gw_events_wait(&events, 0.5, true);
    double ms = elapsed_ms(start);
    CHECKF(rc == GW_EVENT_READY && ms < 50.0,
          "pending_timed_returns_immediately", "rc=%d ms=%.1f", rc, ms);
    start = wall_ns();
    rc = gw_events_wait(&events, 0.0, false);
    ms = elapsed_ms(start);
    CHECKF(rc == GW_EVENT_READY && ms < 50.0,
          "pending_untimed_returns_immediately", "rc=%d ms=%.1f", rc, ms);
}

struct delay_post_args {
    struct gw_event_wait *events;
    long delay_ms;
    bool stop_instead;
};

static void *delay_post_thread(void *raw) {
    struct delay_post_args *args = raw;
    usleep_ms(args->delay_ms);
    if (args->stop_instead)
        gw_events_stop(args->events);
    else
        gw_events_post(args->events);
    return NULL;
}

/* --- 5. a post from another thread wakes a blocked waiter --- */
static void test_post_wakes_blocked_waiter(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    struct delay_post_args args = {&events, 60, false};
    pthread_t thread;
    pthread_create(&thread, NULL, delay_post_thread, &args);
    int64_t start = wall_ns();
    int rc = gw_events_wait(&events, 0.0, false);
    double ms = elapsed_ms(start);
    pthread_join(thread, NULL);
    CHECKF(rc == GW_EVENT_READY && ms >= 50.0 && ms < 1500.0,
          "post_wakes_blocked_waiter", "rc=%d ms=%.1f", rc, ms);
}

/* --- 6. events arriving while pumping stay pending for the next wait --- */
static void test_post_during_pump_stays_pending(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    gw_events_post(&events); /* first batch: exists before the pump starts */
    gw_events_begin_pump(&events);
    gw_events_post(&events); /* second batch: arrives during dispatch */
    gw_events_end_pump(&events);
    int rc = gw_events_wait(&events, 0.05, true);
    CHECKF(rc == GW_EVENT_READY, "post_during_pump_stays_pending", "rc=%d", rc);
    /* Drain it and confirm the queue is empty afterwards. */
    gw_events_begin_pump(&events);
    gw_events_end_pump(&events);
    rc = gw_events_wait(&events, 0.05, true);
    CHECKF(rc == GW_EVENT_TIMEOUT, "pump_ack_clears_pending", "rc=%d", rc);
}

/* --- 7. stop wakes blocked waiters and disables further waits --- */
static void test_stop_wakes_waiters(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    struct delay_post_args args = {&events, 60, true};
    pthread_t thread;
    pthread_create(&thread, NULL, delay_post_thread, &args);
    int64_t start = wall_ns();
    int rc = gw_events_wait(&events, 0.0, false);
    double ms = elapsed_ms(start);
    pthread_join(thread, NULL);
    CHECKF(rc == GW_EVENT_NOT_INITIALIZED && ms >= 50.0 && ms < 1500.0,
          "stop_wakes_blocked_waiter", "rc=%d ms=%.1f", rc, ms);

    start = wall_ns();
    rc = gw_events_wait(&events, 5.0, true);
    ms = elapsed_ms(start);
    CHECKF(rc == GW_EVENT_NOT_INITIALIZED && ms < 50.0,
          "stopped_timed_wait_rejected", "rc=%d ms=%.1f", rc, ms);
    CHECK(gw_events_post(&events) == GW_EVENT_NOT_INITIALIZED,
          "stopped_post_rejected");
}

/* --- 8. reset rebuilds the wait state, dropping stale pendings --- */
static void test_reset_rebuilds_state(void) {
    struct gw_event_wait events = {0};
    for (int round = 0; round < 3; round++) {
        gw_events_reset(&events);
        gw_events_post(&events);
        gw_events_post(&events); /* never consumed */
        gw_events_stop(&events);
        gw_events_reset(&events); /* re-init must discard the stale events */
        int rc = gw_events_wait(&events, 0.05, true);
        CHECKF(rc == GW_EVENT_TIMEOUT, "reset_clears_stale_pending",
              "round=%d rc=%d", round, rc);
    }
    /* And the rebuilt state still delivers fresh posts. */
    gw_events_post(&events);
    CHECK(gw_events_wait(&events, 0.05, true) == GW_EVENT_READY,
          "reset_preserves_wakeups");
}

/* --- 9. a signal interrupt keeps the original deadline --- */
static void on_sigusr1(int sig) { (void)sig; }

struct signal_args {
    pthread_t target;
    long delay_ms;
};

static void *signal_thread(void *raw) {
    struct signal_args *args = raw;
    usleep_ms(args->delay_ms);
    pthread_kill(args->target, SIGUSR1);
    return NULL;
}

static void test_interrupt_keeps_deadline(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    struct sigaction sa = {0};
    sa.sa_handler = on_sigusr1;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0; /* no SA_RESTART: the futex wait must surface EINTR */
    sigaction(SIGUSR1, &sa, NULL);

    struct signal_args args = {pthread_self(), 80};
    pthread_t thread;
    pthread_create(&thread, NULL, signal_thread, &args);
    int64_t start = wall_ns();
    int rc = gw_events_wait(&events, 0.4, true);
    double ms = elapsed_ms(start);
    pthread_join(thread, NULL);
    CHECKF(rc == GW_EVENT_TIMEOUT && ms >= 390.0 && ms < 1500.0,
          "interrupt_keeps_deadline", "rc=%d ms=%.1f", rc, ms);
}

/* --- 10. many producer/consumer rounds never lose a wakeup --- */
#define STRESS_ROUNDS 2000

struct stress_args {
    struct gw_event_wait *events;
    _Atomic int consumed_round;
    _Atomic int waiter_failures;
    int observed;
};

static void *stress_waiter_thread(void *raw) {
    struct stress_args *args = raw;
    for (int i = 0; i < STRESS_ROUNDS; i++) {
        /* A lost wakeup surfaces as a timeout instead of an endless hang. */
        int rc = gw_events_wait(args->events, 2.0, true);
        if (rc != GW_EVENT_READY) {
            args->waiter_failures++;
            break;
        }
        gw_events_begin_pump(args->events);
        gw_events_end_pump(args->events);
        args->observed++;
        atomic_store_explicit(&args->consumed_round, i + 1, memory_order_release);
    }
    return NULL;
}

static void test_stress_no_lost_wakeups(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    struct stress_args args = {&events, 0, 0, 0};
    pthread_t thread;
    pthread_create(&thread, NULL, stress_waiter_thread, &args);

    for (int i = 0; i < STRESS_ROUNDS; i++) {
        while (atomic_load_explicit(&args.consumed_round, memory_order_acquire) < i)
            usleep_ms(1);
        gw_events_post(&events); /* input arrives on the "Android" thread */
    }
    pthread_join(thread, NULL);
    CHECKF(args.waiter_failures == 0 && args.observed == STRESS_ROUNDS,
          "stress_no_lost_wakeups", "failures=%d observed=%d",
          args.waiter_failures, args.observed);
}

/* --- 11. a blocked waiter spends wall time, not CPU time --- */
struct cpu_args {
    struct gw_event_wait *events;
    double wall_ms;
    double cpu_ms;
};

static void *cpu_waiter_thread(void *raw) {
    struct cpu_args *args = raw;
    struct timespec cpu_start, cpu_end;
    int64_t wall_start = wall_ns();
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &cpu_start);
    gw_events_wait(args->events, 0.0, false); /* untimed block */
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &cpu_end);
    args->wall_ms = elapsed_ms(wall_start);
    args->cpu_ms = (double)((int64_t)cpu_end.tv_sec * INT64_C(1000000000) + cpu_end.tv_nsec -
                            (int64_t)cpu_start.tv_sec * INT64_C(1000000000) - cpu_start.tv_nsec) /
                   1e6;
    return NULL;
}

static void test_blocked_wait_burns_no_cpu(void) {
    struct gw_event_wait events = {0};
    gw_events_reset(&events);
    struct cpu_args args = {&events, 0.0, 0.0};
    pthread_t thread;
    pthread_create(&thread, NULL, cpu_waiter_thread, &args);
    usleep_ms(300); /* let the waiter settle into the futex */
    gw_events_post(&events);
    pthread_join(thread, NULL);
    CHECKF(args.wall_ms >= 250.0 && args.cpu_ms < 100.0,
          "blocked_wait_burns_no_cpu", "wall=%.1fms cpu=%.1fms",
          args.wall_ms, args.cpu_ms);
}

int main(void) {
    test_wait_before_init();
    test_invalid_timeouts();
    test_timeout_deadline();
    test_pending_returns_immediately();
    test_post_wakes_blocked_waiter();
    test_post_during_pump_stays_pending();
    test_stop_wakes_waiters();
    test_reset_rebuilds_state();
    test_interrupt_keeps_deadline();
    test_stress_no_lost_wakeups();
    test_blocked_wait_burns_no_cpu();
    printf(failures ? "event_wait_test: %d FAILURE(S)\n" : "event_wait_test: all passed\n",
           failures);
    return failures;
}
