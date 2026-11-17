//! Modrinth 关键帧剪辑的采样与待机调度

use glam::{Mat3, Quat, Vec2, Vec3};

use crate::clip_data::{Clip, Joint, CLIP_FPS, CLIP_IDLE, CLIP_SUBS};
use crate::pose::{flip_quat, ARM_REST_PIVOTS, LEG_REST_PIVOTS};

/// 随机子动画触发间隔（秒）
pub(crate) const SUB_INTERVAL: f32 = 8.0;
/// 点击反应剪辑的播放时长（截取自 0.83 秒的完整数据）
pub(crate) const INTERACT_DURATION: f32 = 0.5;

/// 采样剪辑得到的骨架关节目标
#[derive(Clone, Copy)]
pub(crate) struct ClipPose {
    pub body_rot: Quat,
    pub head_rot: Quat,
    /// [左, 右]
    pub arms: [Quat; 2],
    /// [左, 右]
    pub legs: [Quat; 2],
    pub body_pos: Vec3,
    /// 臂枢轴 [左, 右](肩点,像素)
    pub arm_pivots: [Vec3; 2],
    /// 腿枢轴 [左, 右](髋点,像素)
    pub leg_pivots: [Vec3; 2],
    /// 披风欧拉 (x, z)
    pub cape: Vec2,
}

/// 采样剪辑在时间 `t`（秒）的姿态
pub(crate) fn sample(clip: &Clip, t: f32) -> ClipPose {
    let mut pose = ClipPose {
        body_rot: Quat::IDENTITY,
        head_rot: Quat::IDENTITY,
        arms: [Quat::IDENTITY; 2],
        legs: [Quat::IDENTITY; 2],
        body_pos: Vec3::ZERO,
        arm_pivots: ARM_REST_PIVOTS,
        leg_pivots: LEG_REST_PIVOTS,
        cape: Vec2::ZERO,
    };
    for channel in clip.channels {
        match channel.joint {
            Joint::Body => {
                pose.body_rot = sample_rotation(channel.rotation, t);
                if !channel.translation.is_empty() {
                    pose.body_pos = sample_translation(channel.translation, t);
                }
            }
            Joint::Head => pose.head_rot = sample_rotation(channel.rotation, t),
            Joint::ArmLeft => {
                pose.arms[0] = sample_rotation(channel.rotation, t);
                if !channel.translation.is_empty() {
                    pose.arm_pivots[0] = sample_translation(channel.translation, t);
                }
            }
            Joint::ArmRight => {
                pose.arms[1] = sample_rotation(channel.rotation, t);
                if !channel.translation.is_empty() {
                    pose.arm_pivots[1] = sample_translation(channel.translation, t);
                }
            }
            Joint::LegLeft => {
                pose.legs[0] = sample_rotation(channel.rotation, t);
                if !channel.translation.is_empty() {
                    pose.leg_pivots[0] = sample_translation(channel.translation, t);
                }
            }
            Joint::LegRight => {
                pose.legs[1] = sample_rotation(channel.rotation, t);
                if !channel.translation.is_empty() {
                    pose.leg_pivots[1] = sample_translation(channel.translation, t);
                }
            }
            // 披风欧拉编码渲染基下的世界朝向:局部旋转先经根共轭再分解
            Joint::Cape => pose.cape = cape_euler(flip_quat(sample_rotation(channel.rotation, t))),
        }
    }
    pose
}

fn sample_rotation(keys: &[[f32; 4]], t: f32) -> Quat {
    if keys.is_empty() {
        return Quat::IDENTITY;
    }
    let frame = (t * CLIP_FPS).max(0.);
    let last = keys.len() - 1;
    // 生成数据的 6 位舍入会让模长轻微偏离 1,采样时归一化
    let quat = |k: &[f32; 4]| Quat::from_xyzw(k[0], k[1], k[2], k[3]).normalize();
    if last == 0 {
        return quat(&keys[0]);
    }
    let index = (frame as usize).min(last - 1);
    let frac = (frame - index as f32).clamp(0., 1.);
    quat(&keys[index]).slerp(quat(&keys[index + 1]), frac)
}

fn sample_translation(keys: &[[f32; 3]], t: f32) -> Vec3 {
    if keys.is_empty() {
        return Vec3::ZERO;
    }
    let frame = (t * CLIP_FPS).max(0.);
    let last = keys.len() - 1;
    if last == 0 {
        return Vec3::new(keys[0][0], keys[0][1], keys[0][2]);
    }
    let index = (frame as usize).min(last - 1);
    let frac = (frame - index as f32).clamp(0., 1.);
    let a = Vec3::new(keys[index][0], keys[index][1], keys[index][2]);
    let b = Vec3::new(keys[index + 1][0], keys[index + 1][1], keys[index + 1][2]);
    a.lerp(b, frac)
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

pub(crate) fn clip_duration(clip: &Clip) -> f32 {
    let keys = clip
        .channels
        .iter()
        .map(|c| c.rotation.len().max(c.translation.len()))
        .max()
        .unwrap_or(1);
    (keys.max(1) - 1) as f32 / CLIP_FPS
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
        #[cfg(not(test))]
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
    pub(crate) fn advance_base(&mut self, dt: f32, speed: f32) {
        self.base_time = (self.base_time + (dt * speed).min(0.05)) % clip_duration(&CLIP_IDLE);
    }

    pub(crate) fn tick(&mut self, dt: f32, speed: f32) -> Option<usize> {
        self.advance_base(dt, speed);
        self.countdown -= dt.min(0.05);
        if self.countdown <= 0. && !CLIP_SUBS.is_empty() {
            let index = self.pick_sub();
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

    /// 一次性动作播完后的回归
    pub(crate) fn rebase(&mut self) {
        self.base_time = 0.;
        self.countdown = SUB_INTERVAL;
    }

    /// 随机挑一个子动画，排除上一次播放的
    fn pick_sub(&mut self) -> usize {
        let count = CLIP_SUBS.len();
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
    use crate::clip_data::{CLIP_IDLE, CLIP_INTERACT, CLIP_SUBS};
    use crate::pose::rotation;

    fn all_clips() -> Vec<&'static Clip> {
        let mut clips = vec![&CLIP_IDLE, &CLIP_INTERACT];
        clips.extend(CLIP_SUBS.iter());
        clips
    }

    #[test]
    fn idle_sample_starts_near_rest() {
        let pose = sample(&CLIP_IDLE, 0.);
        let angle = |q: Quat| 2. * q.w.acos().abs();
        assert!(angle(pose.body_rot) < 0.02);
        assert!(angle(pose.head_rot) < 0.02);
        for arm in pose.arms {
            assert!(angle(arm) < 0.05, "arm at rest must hang straight");
        }
        for leg in pose.legs {
            assert!(angle(leg) < 0.001);
        }
        // 躯干下沉换算到像素后落在 [−0.65, −0.3]
        assert!((-0.66..=-0.3).contains(&pose.body_pos.y));
        assert!(pose.body_pos.x.abs() < 0.05);
        // idle 无四肢平移通道:枢轴为骨架静止值
        assert!(pose.arm_pivots[0].abs_diff_eq(Vec3::new(-5., 10., 0.), 0.001));
        assert!(pose.arm_pivots[1].abs_diff_eq(Vec3::new(5., 10., 0.), 0.001));
        assert!(pose.leg_pivots[0].abs_diff_eq(Vec3::new(-1.9, 0., 0.), 0.001));
        assert!(pose.leg_pivots[1].abs_diff_eq(Vec3::new(1.9, 0., 0.), 0.001));
    }

    #[test]
    fn sub_keeps_feet_vertically_planted() {
        let clip = &CLIP_SUBS[0];
        let duration = clip_duration(clip);
        let steps = 90;
        for i in 0..=steps {
            let pose = sample(clip, duration * i as f32 / steps as f32);
            for side in 0..2 {
                // 脚 = 髋部(0,12,0) + 腿枢轴 + 腿摆动(绕髋点旋转),与躯干位移无关
                let swing = pose.legs[side] * Vec3::new(0., -12., 0.);
                let foot = Vec3::new(0., 12., 0.) + pose.leg_pivots[side] + swing;
                assert!(
                    foot.y.abs() < 0.3,
                    "side {side} at {i}: foot y = {}",
                    foot.y
                );
                let rest_x = if side == 0 { -1.9 } else { 1.9 };
                assert!(
                    (foot.x - rest_x).abs() < 0.05,
                    "side {side} at {i}: foot x = {}",
                    foot.x
                );
            }
        }
    }

    #[test]
    fn cape_euler_roundtrips_every_key() {
        for clip in all_clips() {
            for channel in clip.channels {
                if channel.joint != Joint::Cape {
                    continue;
                }
                for key in channel.rotation {
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
        let pose = sample(&CLIP_IDLE, 0.);
        let base = std::f32::consts::PI * 0.06;
        assert!(
            (pose.cape.x - base).abs() < 0.15,
            "cape x = {}",
            pose.cape.x
        );
        assert!(pose.cape.y.abs() < 0.1, "cape z = {}", pose.cape.y);
    }

    #[test]
    fn clip_samples_stay_finite() {
        for clip in all_clips() {
            let duration = clip_duration(clip);
            let steps = (duration / 0.033).ceil() as usize;
            for i in 0..=steps {
                let pose = sample(clip, duration * i as f32 / steps.max(1) as f32);
                for q in [pose.body_rot, pose.head_rot] {
                    assert!(q.is_finite());
                    assert!((q.length() - 1.).abs() < 0.001);
                }
                assert!(pose.body_pos.is_finite());
                for pivot in pose.arm_pivots.iter().chain(pose.leg_pivots.iter()) {
                    assert!(pivot.is_finite());
                }
            }
        }
    }

    #[test]
    fn idle_loops_seamlessly() {
        let duration = clip_duration(&CLIP_IDLE);
        let a = sample(&CLIP_IDLE, 0.);
        let b = sample(&CLIP_IDLE, duration);
        assert!(a.body_rot.abs_diff_eq(b.body_rot, 0.001));
        assert!(a.head_rot.abs_diff_eq(b.head_rot, 0.001));
        assert!(a.arms[0].abs_diff_eq(b.arms[0], 0.001));
        assert!(a.cape.abs_diff_eq(b.cape, 0.001));
        assert!(a.body_pos.abs_diff_eq(b.body_pos, 0.001));
    }

    #[test]
    fn sub_limb_pivots_reach_animation_data() {
        // idle_sub_1 的腿枢轴平移通道必须进入采样结果(此前被整体丢弃)
        let clip = &CLIP_SUBS[0];
        let mut max_pivot_z: f32 = 0.;
        for i in 0..=90 {
            let pose = sample(clip, clip_duration(clip) * i as f32 / 90.);
            for pivot in pose.leg_pivots {
                max_pivot_z = max_pivot_z.max(pivot.z.abs());
            }
        }
        assert!(
            max_pivot_z > 0.5,
            "leg pivot z swing too small: {max_pivot_z}"
        );
    }
}
