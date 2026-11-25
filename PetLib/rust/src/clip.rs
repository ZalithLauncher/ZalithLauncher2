//! 运行时模型剪辑的采样与待机调度

use glam::{Mat3, Quat, Vec2};

use crate::gltf::Times;
use crate::model::{ModelData, RuntimeClip};
use crate::pose::{flip_quat, NodeSnapshot};

/// 随机子动画触发间隔（秒）
pub(crate) const SUB_INTERVAL: f32 = 8.0;
/// 点击反应剪辑的播放时长（截取自完整数据）
pub(crate) const INTERACT_DURATION: f32 = 0.5;

/// 剪辑时长（秒）：均匀网格按键数推算，一般时间轴取末值。
pub(crate) fn clip_duration(clip: &RuntimeClip) -> f32 {
    clip.duration
}

/// 在均匀时间网格上定位相邻旋转键，归一化后进行球面插值。
fn sample_rotation_uniform(keys: &[[f32; 4]], fps: f32, t: f32) -> Quat {
    let frame = (t * fps).max(0.);
    let last = keys.len() - 1;
    // 6 位舍入让模长轻微偏离 1，采样时归一化
    let quat = |k: &[f32; 4]| Quat::from_xyzw(k[0], k[1], k[2], k[3]).normalize();
    if last == 0 {
        return quat(&keys[0]);
    }
    let index = (frame as usize).min(last - 1);
    let frac = (frame - index as f32).clamp(0., 1.);
    quat(&keys[index]).slerp(quat(&keys[index + 1]), frac)
}

/// 一般时间轴采样：对分查找所在区间后线性插值。
fn sample_rotation_general(keys: &[[f32; 4]], times: &[f32], t: f32) -> Quat {
    let quat = |k: &[f32; 4]| Quat::from_xyzw(k[0], k[1], k[2], k[3]).normalize();
    if keys.len() == 1 {
        return quat(&keys[0]);
    }
    let (a, b) = bracket(times, t);
    quat(&keys[a]).slerp(quat(&keys[b]), frac_between(times, a, b, t))
}

fn sample_translation_uniform(keys: &[[f32; 3]], fps: f32, t: f32) -> glam::Vec3 {
    let frame = (t * fps).max(0.);
    let last = keys.len() - 1;
    if last == 0 {
        return glam::Vec3::new(keys[0][0], keys[0][1], keys[0][2]);
    }
    let index = (frame as usize).min(last - 1);
    let frac = (frame - index as f32).clamp(0., 1.);
    let a = glam::Vec3::new(keys[index][0], keys[index][1], keys[index][2]);
    let b = glam::Vec3::new(keys[index + 1][0], keys[index + 1][1], keys[index + 1][2]);
    a.lerp(b, frac)
}

fn sample_translation_general(keys: &[[f32; 3]], times: &[f32], t: f32) -> glam::Vec3 {
    if keys.len() == 1 {
        return glam::Vec3::new(keys[0][0], keys[0][1], keys[0][2]);
    }
    let (a, b) = bracket(times, t);
    let va = glam::Vec3::new(keys[a][0], keys[a][1], keys[a][2]);
    let vb = glam::Vec3::new(keys[b][0], keys[b][1], keys[b][2]);
    va.lerp(vb, frac_between(times, a, b, t))
}

/// 找到 t 所在区间的两个键下标（夹取到端点）。
fn bracket(times: &[f32], t: f32) -> (usize, usize) {
    let upper = times.partition_point(|&x| x <= t).max(1).min(times.len() - 1);
    (upper - 1, upper)
}

fn frac_between(times: &[f32], a: usize, b: usize, t: f32) -> f32 {
    let span = times[b] - times[a];
    if span <= 1e-9 {
        return 0.;
    }
    ((t - times[a]) / span).clamp(0., 1.)
}

/// 采样剪辑到节点局部姿态；未覆盖的节点保持静止位。
pub(crate) fn sample(data: &ModelData, clip: &RuntimeClip, t: f32) -> NodeSnapshot {
    let mut snap = NodeSnapshot::rest(data);
    for (node, channel) in &clip.channels {
        if !channel.rotation.is_empty() {
            snap.rotations[*node] = match &channel.rotation_times {
                Some(Times::Uniform(fps)) => {
                    sample_rotation_uniform(&channel.rotation, *fps, t)
                }
                Some(Times::General(times)) => {
                    sample_rotation_general(&channel.rotation, times, t)
                }
                None => continue,
            };
        }
        if !channel.translation.is_empty() {
            snap.translations[*node] = match &channel.translation_times {
                Some(Times::Uniform(fps)) => {
                    sample_translation_uniform(&channel.translation, *fps, t)
                }
                Some(Times::General(times)) => {
                    sample_translation_general(&channel.translation, times, t)
                }
                None => continue,
            };
        }
    }
    snap
}

/// 把披风四元数分解为渲染端欧拉 (x, z)：目标旋转 = rotation(x, π, z)，
/// 即 Rx(x)·Ry(π)·Rz(z)，要求目标不含 y 分量（披风剪辑满足）。
pub(crate) fn cape_euler(q: Quat) -> Vec2 {
    // glam Mat3 的 x/y/z_axis 即矩阵列
    let m = Mat3::from_quat(q);
    let x = m.z_axis.y.atan2(-m.z_axis.z);
    let z = m.y_axis.x.atan2(-m.x_axis.x);
    Vec2::new(x, z)
}

/// 披风欧拉回组成节点局部四元数（行走/静止的程序化披风）。
pub(crate) fn cape_quat(x: f32, z: f32) -> Quat {
    flip_quat(crate::pose::rotation(x, std::f32::consts::PI, z))
}

fn next_rand(rng: &mut u64) -> u64 {
    let mut x = *rng;
    x ^= x << 13;
    x ^= x >> 7;
    x ^= x << 17;
    *rng = x;
    x
}

/// 待机调度：基础 idle 循环，每 8 秒随机播一个子动画（不连续重复），
/// 播完淡回基础循环并重新计时。
pub(crate) struct IdleClips {
    /// 基础循环的已播时间(采样点)
    pub(crate) base_time: f32,
    /// 距下次随机子动画的秒数
    pub(crate) countdown: f32,
    last_sub: Option<usize>,
    rng: u64,
}

impl IdleClips {
    pub(crate) fn new() -> Self {
        // 测试下用固定种子：姿态断言不随随机子动画的选择而波动
        #[cfg(test)]
        return Self::seeded(0x2545F4914F6CDD1D);
        // wasm32-unknown-unknown 无时钟系统调用，SystemTime::now 会 trap；
        // 固定种子兜底，调试台回放前会显式 seed_idle 覆盖
        #[cfg(all(target_family = "wasm", not(test)))]
        return Self::seeded(1);
        #[cfg(not(any(test, target_family = "wasm")))]
        {
            let seed = std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .map(|d| d.subsec_nanos() as u64)
                .unwrap_or(1);
            Self::seeded(seed)
        }
    }

    pub(crate) fn seeded(seed: u64) -> Self {
        Self {
            base_time: 0.,
            countdown: SUB_INTERVAL,
            last_sub: None,
            rng: seed | 1,
        }
    }

    /// 基础循环推进一帧
    pub(crate) fn advance_base(&mut self, dt: f32, speed: f32, idle: &RuntimeClip) {
        self.base_time = (self.base_time + (dt * speed).min(0.05)) % clip_duration(idle);
    }

    pub(crate) fn tick(
        &mut self,
        dt: f32,
        speed: f32,
        subs: &[usize],
        idle: &RuntimeClip,
    ) -> Option<usize> {
        self.advance_base(dt, speed, idle);
        self.countdown -= dt.min(0.05);
        if self.countdown <= 0. && !subs.is_empty() {
            let index = self.pick_sub(subs);
            self.last_sub = Some(index);
            self.countdown = SUB_INTERVAL;
            return Some(index);
        }
        None
    }

    /// 回到基础循环起点并重新计时
    pub(crate) fn reset(&mut self) {
        self.base_time = 0.;
        self.countdown = SUB_INTERVAL;
        self.last_sub = None;
    }

    /// 一次性动作播完后的回归。
    pub(crate) fn rebase(&mut self) {
        self.base_time = 0.;
        self.countdown = SUB_INTERVAL;
    }

    pub(crate) fn rearm(&mut self) {
        self.countdown = SUB_INTERVAL;
    }

    /// 随机挑一个子动画，排除上一次播放的
    fn pick_sub(&mut self, subs: &[usize]) -> usize {
        let count = subs.len();
        if count == 0 {
            return 0;
        }
        let excluded = self.last_sub.filter(|_| count > 1);
        let pool = count - usize::from(excluded.is_some());
        let pick = next_rand(&mut self.rng) as usize % pool;
        if excluded.is_some_and(|last| pick >= last) {
            pick + 1
        } else {
            pick
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::builtin_data;
    use crate::pose::rotation;
    use glam::Vec3;
    use std::sync::Arc;

    fn data() -> Arc<ModelData> {
        builtin_data(crate::skin::ArmModel::Classic).unwrap()
    }

    fn all_clips(data: &ModelData) -> Vec<&RuntimeClip> {
        let mut clips = Vec::new();
        if let Some(idle) = data.clip("idle") {
            clips.push(idle);
        }
        if let Some(interact) = data.clip("interact") {
            clips.push(interact);
        }
        for sub in data.clips_with_prefix("idle_sub_") {
            clips.push(&data.clips[sub]);
        }
        clips
    }

    #[test]
    fn idle_sample_starts_near_rest() {
        let data = data();
        let pose = sample(&data, data.clip("idle").unwrap(), 0.);
        let angle = |q: Quat| 2. * q.w.acos().abs();
        let body = data.joint("Body").unwrap();
        let head = data.joint("Head").unwrap();
        let arm_l = data.joint("Left_Arm").unwrap();
        let arm_r = data.joint("Right_Arm").unwrap();
        assert!(angle(pose.rotations[body]) < 0.02);
        assert!(angle(pose.rotations[head]) < 0.02);
        assert!(angle(pose.rotations[arm_l]) < 0.05, "left arm should hang at rest");
        assert!(angle(pose.rotations[arm_r]) < 0.05, "right arm should hang at rest");
        // 躯干下沉（像素）
        assert!((-0.66..=-0.3).contains(&pose.translations[body].y));
        assert!(pose.translations[body].x.abs() < 0.05);
        // idle 无四肢平移通道：枢轴为骨架静止值
        let leg_l = data.joint("Left_Leg").unwrap();
        let leg_r = data.joint("Right_Leg").unwrap();
        assert!(pose.translations[arm_l].abs_diff_eq(Vec3::new(-5., 10., 0.), 0.001));
        assert!(pose.translations[arm_r].abs_diff_eq(Vec3::new(5., 10., 0.), 0.001));
        assert!(pose.translations[leg_l].abs_diff_eq(Vec3::new(-1.9, 0., 0.), 0.001));
        assert!(pose.translations[leg_r].abs_diff_eq(Vec3::new(1.9, 0., 0.), 0.001));
    }

    #[test]
    fn sub_keeps_feet_vertically_planted() {
        let data = data();
        let clip = data.clip("idle_sub_1").unwrap();
        let duration = clip_duration(clip);
        let steps = 90;
        let leg_l = data.joint("Left_Leg").unwrap();
        let leg_r = data.joint("Right_Leg").unwrap();
        for i in 0..=steps {
            let pose = sample(&data, clip, duration * i as f32 / steps as f32);
            for (node, rest_x) in [(leg_l, -1.9f32), (leg_r, 1.9f32)] {
                // 脚 = 髋部(0,12,0) + 腿枢轴 + 腿摆动(绕髋点旋转)，与躯干位移无关
                let swing = pose.rotations[node] * Vec3::new(0., -12., 0.);
                let foot = Vec3::new(0., 12., 0.) + pose.translations[node] + swing;
                assert!(foot.y.abs() < 0.3, "side {node} at {i}: foot y = {}", foot.y);
                assert!(
                    (foot.x - rest_x).abs() < 0.05,
                    "side {node} at {i}: foot x = {}",
                    foot.x
                );
            }
        }
    }

    #[test]
    fn cape_euler_roundtrips_every_key() {
        let data = data();
        let cape = data.joint("Cape").unwrap();
        for clip in all_clips(&data) {
            for (node, channel) in clip.channels.iter() {
                if *node != cape {
                    continue;
                }
                for key in channel.rotation.iter() {
                    let q = flip_quat(Quat::from_xyzw(key[0], key[1], key[2], key[3]));
                    let e = cape_euler(q);
                    let restored = rotation(e.x, std::f32::consts::PI, e.y);
                    // 四元数双覆盖：按旋转角比较而非分量
                    assert!(
                        restored.angle_between(q) < 0.01,
                        "cape euler roundtrip failed for {key:?}: {e:?}"
                    );
                }
            }
        }
    }

    #[test]
    fn cape_rest_matches_skeleton_convention() {
        // 披风静止欧拉必须落在骨架基准 (0.06π, 0) 附近，
        // 这是整套坐标系换算（180° 翻转 + 模型朝向）正确性的锚点。
        let data = data();
        let pose = sample(&data, data.clip("idle").unwrap(), 0.);
        let cape = data.joint("Cape").unwrap();
        let e = cape_euler(flip_quat(pose.rotations[cape]));
        let base = std::f32::consts::PI * 0.06;
        assert!((e.x - base).abs() < 0.15, "cape x = {}", e.x);
        assert!(e.y.abs() < 0.1, "cape z = {}", e.y);
    }

    #[test]
    fn clip_samples_stay_finite() {
        let data = data();
        for clip in all_clips(&data) {
            let duration = clip_duration(clip);
            let steps = (duration / 0.033).ceil() as usize;
            for i in 0..=steps {
                let pose = sample(&data, clip, duration * i as f32 / steps.max(1) as f32);
                for q in &pose.rotations {
                    assert!(q.is_finite());
                    assert!((q.length() - 1.).abs() < 0.001);
                }
                for t in &pose.translations {
                    assert!(t.is_finite());
                }
            }
        }
    }

    #[test]
    fn idle_loops_seamlessly() {
        let data = data();
        let clip = data.clip("idle").unwrap();
        let duration = clip_duration(clip);
        let a = sample(&data, clip, 0.);
        let b = sample(&data, clip, duration);
        let body = data.joint("Body").unwrap();
        let head = data.joint("Head").unwrap();
        let arm_l = data.joint("Left_Arm").unwrap();
        let cape = data.joint("Cape").unwrap();
        assert!(a.rotations[body].abs_diff_eq(b.rotations[body], 0.001));
        assert!(a.rotations[head].abs_diff_eq(b.rotations[head], 0.001));
        assert!(a.rotations[arm_l].abs_diff_eq(b.rotations[arm_l], 0.001));
        assert!(a.rotations[cape].abs_diff_eq(b.rotations[cape], 0.001));
        assert!(a.translations[body].abs_diff_eq(b.translations[body], 0.001));
    }

    #[test]
    fn sub_limb_pivots_reach_animation_data() {
        // idle_sub_1 的腿枢轴平移通道必须进入采样结果(此前被整体丢弃)
        let data = data();
        let clip = data.clip("idle_sub_1").unwrap();
        let duration = clip_duration(clip);
        let leg_l = data.joint("Left_Leg").unwrap();
        let leg_r = data.joint("Right_Leg").unwrap();
        let mut max_pivot_z: f32 = 0.;
        for i in 0..=90 {
            let pose = sample(&data, clip, duration * i as f32 / 90.);
            for node in [leg_l, leg_r] {
                max_pivot_z = max_pivot_z.max(pose.translations[node].z.abs());
            }
        }
        assert!(max_pivot_z > 0.5, "leg pivot z swing too small: {max_pivot_z}");
    }

    #[test]
    fn idle_clips_scheduler_basics() {
        let data = data();
        let idle = data.clip("idle").unwrap();
        let subs = data.clips_with_prefix("idle_sub_");
        let mut sched = IdleClips::seeded(0x2545F4914F6CDD1D);
        // 8 秒触发
        let mut fired = false;
        for _ in 0..(8 * 60 + 2) {
            if sched.tick(1. / 60., 1., &subs, &idle).is_some() {
                fired = true;
                break;
            }
        }
        assert!(fired, "sub-clip must fire within 8s");
        assert!((sched.countdown - SUB_INTERVAL).abs() < 1e-4, "countdown should restart after firing");
    }
}
