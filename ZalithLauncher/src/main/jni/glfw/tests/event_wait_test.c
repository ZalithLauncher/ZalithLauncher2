//
// Created by movte on 2026/11/22.
//
#include "event_wait.h"

#include <errno.h>
#include <inttypes.h>
#include <pthread.h>
#include <signal.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
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

static void drain(void) {
    // 排空全部待处理通知且必然立即返回
    event_wait_block(0.0, true);
}

/* --- 1. 已有待处理通知时立即返回 --- */
static void test_pending_returns_immediately(void) {
    event_wait_notify();
    int64_t start = wall_ns();
    event_wait_block(0.5, true);
    double ms = elapsed_ms(start);
    CHECKF(ms < 50.0, "pending_timed_returns_immediately", "ms=%.1f", ms);

    event_wait_notify();
    start = wall_ns();
    event_wait_block(0.0, false);
    ms = elapsed_ms(start);
    CHECKF(ms < 50.0, "pending_untimed_returns_immediately", "ms=%.1f", ms);
}

/* --- 2. 无通知时在截止时间超时 --- */
static void test_timeout_deadline(void) {
    int64_t start = wall_ns();
    event_wait_block(0.15, true);
    double ms = elapsed_ms(start);
    CHECKF(ms >= 140.0 && ms < 1500.0, "timeout_returns_after_deadline", "ms=%.1f", ms);
}

/* --- 3. 入口排空取走全部通知，之后恢复等待 --- */
static void test_drain_clears_all_pending(void) {
    event_wait_notify();
    event_wait_notify();
    int64_t start = wall_ns();
    event_wait_block(0.5, true);
    double ms = elapsed_ms(start);
    CHECKF(ms < 50.0, "drain_returns_immediately", "ms=%.1f", ms);
    start = wall_ns();
    event_wait_block(0.15, true);
    ms = elapsed_ms(start);
    CHECKF(ms >= 140.0 && ms < 1500.0, "drained_state_sleeps_full_deadline", "ms=%.1f", ms);
}

struct delay_notify_args {
    long delay_ms;
};

static void *delay_notify_thread(void *raw) {
    struct delay_notify_args *args = raw;
    struct timespec ts = {args->delay_ms / 1000, (args->delay_ms % 1000) * 1000000};
    nanosleep(&ts, NULL);
    event_wait_notify();
    return NULL;
}

/* --- 4. 其他线程的通知唤醒无限期阻塞中的等待者 --- */
static void test_notify_wakes_blocked_waiter(void) {
    struct delay_notify_args args = {100};
    pthread_t thread;
    pthread_create(&thread, NULL, delay_notify_thread, &args);
    int64_t start = wall_ns();
    event_wait_block(0.0, false);
    double ms = elapsed_ms(start);
    pthread_join(thread, NULL);
    CHECKF(ms >= 50.0 && ms < 1500.0, "notify_wakes_blocked_waiter", "ms=%.1f", ms);
}

/* --- 5. 信号中断不推迟截止时间 --- */
static void on_sigusr1(int sig) { (void)sig; }

struct signal_args {
    pthread_t target;
    long delay_ms;
};

static void *signal_thread(void *raw) {
    struct signal_args *args = raw;
    struct timespec ts = {args->delay_ms / 1000, (args->delay_ms % 1000) * 1000000};
    nanosleep(&ts, NULL);
    pthread_kill(args->target, SIGUSR1);
    return NULL;
}

static void test_interrupt_keeps_deadline(void) {
    struct sigaction sa = {0};
    sa.sa_handler = on_sigusr1;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0; // 不设 SA_RESTART，futex 必须以 EINTR 中断并重查截止时间
    sigaction(SIGUSR1, &sa, NULL);

    struct signal_args args = {pthread_self(), 80};
    pthread_t thread;
    pthread_create(&thread, NULL, signal_thread, &args);
    int64_t start = wall_ns();
    event_wait_block(0.4, true);
    double ms = elapsed_ms(start);
    pthread_join(thread, NULL);
    CHECKF(ms >= 390.0 && ms < 1500.0, "interrupt_keeps_deadline", "ms=%.1f", ms);
    drain();
}

/* --- 6. 多轮通知/消费竞争不丢失唤醒 --- */
#define STRESS_ROUNDS 500

struct stress_state {
    _Atomic int consumed_rounds;
    _Atomic int waiter_failures;
};

static void *stress_waiter_thread(void *raw) {
    struct stress_state *state = raw;
    for (int i = 0; i < STRESS_ROUNDS; i++) {
        // 丢唤醒会表现为 2 秒超时而不是死挂
        int64_t start = wall_ns();
        event_wait_block(2.0, true);
        if (elapsed_ms(start) >= 2000.0) {
            state->waiter_failures++;
            break;
        }
        atomic_store_explicit(&state->consumed_rounds, i + 1, memory_order_release);
    }
    return NULL;
}

static void test_stress_no_lost_wakeups(void) {
    struct stress_state state;
    atomic_store_explicit(&state.consumed_rounds, 0, memory_order_release);
    atomic_store_explicit(&state.waiter_failures, 0, memory_order_release);
    pthread_t thread;
    pthread_create(&thread, NULL, stress_waiter_thread, &state);

    for (int i = 0; i < STRESS_ROUNDS; i++) {
        int64_t wait_start = wall_ns();
        // 等上一轮被消费；等待者失败退出时不得死等，超时即判负
        while (atomic_load_explicit(&state.consumed_rounds, memory_order_acquire) < i) {
            if (wall_ns() - wait_start > INT64_C(3000000000)) {
                state.waiter_failures++;
                pthread_join(thread, NULL);
                CHECKF(false, "stress_no_lost_wakeups", "waiter stalled at round %d", i);
                return;
            }
            nanosleep(&(struct timespec){0, 1000000}, NULL);
        }
        event_wait_notify();
    }
    pthread_join(thread, NULL);
    CHECKF(state.waiter_failures == 0 && state.consumed_rounds == STRESS_ROUNDS,
          "stress_no_lost_wakeups", "failures=%d rounds=%d",
          state.waiter_failures, state.consumed_rounds);
}

/* --- 7. 阻塞等待消耗墙钟时间而非 CPU 时间 --- */
struct cpu_result {
    double wall_ms;
    double cpu_ms;
};

static void *cpu_waiter_thread(void *raw) {
    struct cpu_result *result = raw;
    struct timespec cpu_start, cpu_end;
    int64_t wall_start = wall_ns();
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &cpu_start);
    event_wait_block(0.0, false); // 无限期阻塞
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &cpu_end);
    result->wall_ms = elapsed_ms(wall_start);
    result->cpu_ms = (double)((int64_t)(cpu_end.tv_sec - cpu_start.tv_sec) * INT64_C(1000000000) +
                              (cpu_end.tv_nsec - cpu_start.tv_nsec)) / 1e6;
    return NULL;
}

static void test_blocked_wait_burns_no_cpu(void) {
    struct cpu_result result = {0.0, 0.0};
    pthread_t thread;
    pthread_create(&thread, NULL, cpu_waiter_thread, &result);
    nanosleep(&(struct timespec){0, 300000000}, NULL); // 让等待者沉入 futex
    event_wait_notify();
    pthread_join(thread, NULL);
    CHECKF(result.wall_ms >= 250.0 && result.cpu_ms < 100.0,
          "blocked_wait_burns_no_cpu", "wall=%.1fms cpu=%.1fms",
          result.wall_ms, result.cpu_ms);
}

int main(void) {
    drain(); // 清理前序用例可能的残留
    test_pending_returns_immediately();
    test_timeout_deadline();
    test_drain_clears_all_pending();
    test_notify_wakes_blocked_waiter();
    test_interrupt_keeps_deadline();
    test_stress_no_lost_wakeups();
    test_blocked_wait_burns_no_cpu();
    printf(failures ? "event_wait_test: %d FAILURE(S)\n" : "event_wait_test: all passed\n",
           failures);
    return failures;
}
