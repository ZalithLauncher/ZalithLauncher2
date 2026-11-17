//! 常驻并行池：跨帧复用 OS 线程，免去逐帧 spawn/join 的内核调度开销。
//!
//! 任务以"函数指针 + 裸指针实参"投递，池本身不解引用任何指针；
//! 指针的有效期由 `run_jobs` 的完成等待保证：提交方阻塞至全部任务
//! 结束才返回，任务实参所指的数据因此始终存活。

use std::collections::VecDeque;
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::sync::{Arc, Condvar, Mutex, OnceLock};

/// 任务执行体。`arg` 由提交方构造，仅在本任务运行期间有效。
type Run = unsafe fn(*mut ());

#[derive(Clone, Copy)]
pub(crate) struct Job {
    pub(crate) run: Run,
    pub(crate) arg: *mut (),
}

// SAFETY: arg 所指数据只在 run_jobs 返回前被访问，
// 跨线程传递以 run_jobs 的完成等待为边界。
unsafe impl Send for Job {}

struct PoolState {
    queue: VecDeque<Job>,
    /// 已提交未完成的任务数（含主线程领取的）
    pending: usize,
    /// 任一任务 panic 时置位，由 run_jobs 收尾时重现
    panicked: bool,
}

struct Shared {
    state: Mutex<PoolState>,
    /// 有新任务入队
    job_ready: Condvar,
    /// pending 归零
    all_done: Condvar,
}

static POOL: OnceLock<Arc<Shared>> = OnceLock::new();

/// 提交一批互不依赖的任务并等待全部完成；主线程同样领取任务执行。
/// `workers` 为常驻工作线程数（主线程之外），仅首次调用生效。
/// 任务 panic 不穿越本函数边界：收尾时以 panic 重现，对齐旧的 join 语义。
pub(crate) fn run_jobs(jobs: &[Job], workers: usize) {
    if jobs.is_empty() {
        return;
    }
    let shared = POOL.get_or_init(|| spawn(workers));
    {
        let mut state = shared.state.lock().unwrap();
        state.panicked = false;
        state.queue.extend(jobs.iter().copied());
        state.pending += jobs.len();
    }
    shared.job_ready.notify_all();

    loop {
        let job = {
            let mut state = shared.state.lock().unwrap();
            loop {
                if let Some(job) = state.queue.pop_front() {
                    break Some(job);
                }
                if state.pending == 0 {
                    break None;
                }
                state = shared.all_done.wait(state).unwrap();
            }
        };
        match job {
            Some(job) => execute(&shared.state, &shared.all_done, job),
            None => break,
        }
    }

    if shared.state.lock().unwrap().panicked {
        panic!("parallel job panicked");
    }
}

fn execute(state: &Mutex<PoolState>, all_done: &Condvar, job: Job) {
    // SAFETY: run/arg 由提交方按本模块文档约定构造。
    let ok = catch_unwind(AssertUnwindSafe(|| unsafe { (job.run)(job.arg) })).is_ok();
    let mut state = state.lock().unwrap();
    state.pending -= 1;
    if !ok {
        state.panicked = true;
    }
    if state.pending == 0 {
        all_done.notify_all();
    }
}

fn worker_loop(shared: &Shared) {
    loop {
        let job = {
            let mut state = shared.state.lock().unwrap();
            loop {
                if let Some(job) = state.queue.pop_front() {
                    break job;
                }
                state = shared.job_ready.wait(state).unwrap();
            }
        };
        execute(&shared.state, &shared.all_done, job);
    }
}

fn spawn(workers: usize) -> Arc<Shared> {
    let shared = Arc::new(Shared {
        state: Mutex::new(PoolState {
            queue: VecDeque::new(),
            pending: 0,
            panicked: false,
        }),
        job_ready: Condvar::new(),
        all_done: Condvar::new(),
    });
    for index in 0..workers {
        let shared = Arc::clone(&shared);
        std::thread::Builder::new()
            .name(format!("PetLibRaster{index}"))
            .spawn(move || worker_loop(&shared))
            .expect("spawn raster worker");
    }
    shared
}
