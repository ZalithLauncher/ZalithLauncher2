//! 玩家骨架：具名关节的轨道混合与程序化姿态。
//!
//! 姿态以「节点 → 局部平移/旋转」映射输出，渲染端按模型节点树合成
//! 世界变换。程序化效果（注视/摆臂/压扁/行走）按模型契约名查找关节，
//! 缺失则跳过对应效果，非人形模型因此可复用同一套轨道系统。

use std::sync::Arc;

use glam::{EulerRot, Mat4, Quat, Vec2, Vec3};

use crate::animation::{evaluate_walking, AnimFrame, AnimationId, WALKING_PERIOD};
use crate::clip::{self, IdleClips};
use crate::model::ModelData;

/// 攻击动画时长 (20 tick/s 下的 6 tick)
const SWING_SECONDS: f32 = 0.30;
/// 压扁脉冲时长(0.08 上升 + 0.20 回落)
const SQUASH_SECONDS: f32 = 0.28;

pub const TAU_GAZE: f32 = 0.10;
pub const TAU_YAW: f32 = 0.15;
/// 动画轨道交叉淡入淡出时长(秒)。
pub const FADE_SECONDS: f32 = 0.2;
pub const TAU_RECENTER: f32 = 0.35;

/// 原始 GLTF 基 → 渲染基的 180°Y 根共轭(即 GLTF 骨架里
/// blockbench_export 节点的旋转)：四元数 (x, y, z, w) → (−x, y, −z, w)。
pub(crate) fn flip_quat(q: Quat) -> Quat {
    Quat::from_xyzw(-q.x, q.y, -q.z, q.w)
}

/// 背对切断阈值：偏航相对摄像机方向超过此角放弃跟随。
const GAZE_OFF_YAW: f32 = 90f32.to_radians();
/// 背对恢复阈值：拖回此角以内才恢复（迟滞）。
const GAZE_ON_YAW: f32 = 75f32.to_radians();
/// 过扭放弃阈值：头-身相对水平扭角超过此角放弃跟随。
const GAZE_TWIST_OFF: f32 = 60f32.to_radians();
/// 过扭恢复阈值（迟滞）。
const GAZE_TWIST_ON: f32 = 50f32.to_radians();

fn wrap_pi(angle: f32) -> f32 {
    let two_pi = std::f32::consts::TAU;
    let a = angle % two_pi;
    if a > std::f32::consts::PI {
        a - two_pi
    } else if a < -std::f32::consts::PI {
        a + two_pi
    } else {
        a
    }
}

/// 相机正面基准偏航。
/// 角色静止时头部以此角度朝向相机。
pub fn front_yaw() -> f32 {
    30f32.atan2(70.)
}

/// 欧拉角（弧度，XYZ 序）转四元数。
pub fn rotation(x: f32, y: f32, z: f32) -> Quat {
    Quat::from_euler(EulerRot::XYZ, x, y, z)
}

fn smooth(t: f32) -> f32 {
    let t = t.clamp(0., 1.);
    t * t * (3. - 2. * t)
}

/// 压扁脉冲包络:0.08 秒平滑下压,0.20 秒平滑回落
fn squash_pulse(phase: f32) -> f32 {
    if phase < 0.08 {
        smooth(phase / 0.08)
    } else {
        1. - smooth((phase - 0.08) / 0.20)
    }
}

/// 相机基准四元数：把角色正面基准对齐到投影相机的屏幕平面。
pub fn view_quat() -> Quat {
    Quat::from_rotation_y(front_yaw())
        * Quat::from_rotation_x(-(23f32 / (30f32 * 30f32 + 70f32 * 70f32).sqrt()).atan())
}

/// 具名关节索引（模型契约名，None 表示该模型缺少此关节）。
#[derive(Clone, Copy)]
struct Joints {
    body: Option<usize>,
    head: Option<usize>,
    arms: [Option<usize>; 2],
    legs: [Option<usize>; 2],
    cape: Option<usize>,
}

impl Joints {
    fn resolve(model: &ModelData) -> Self {
        Self {
            body: model.joint("Body"),
            head: model.joint("Head"),
            arms: [model.joint("Left_Arm"), model.joint("Right_Arm")],
            legs: [model.joint("Left_Leg"), model.joint("Right_Leg")],
            cape: model.joint("Cape"),
        }
    }
}

/// 节点局部姿态快照：全部节点的平移（像素）与旋转（GLTF 基）。
#[derive(Clone)]
pub struct NodeSnapshot {
    pub translations: Vec<Vec3>,
    pub rotations: Vec<Quat>,
}

impl NodeSnapshot {
    /// 静止姿态：平移取节点静止值，旋转取恒等。渲染端对未驱动节点
    /// 回退 glTF 静止旋转；驱动节点由骨架完全给定。
    pub(crate) fn rest(model: &ModelData) -> Self {
        Self {
            translations: model.nodes.iter().map(|n| n.rest_translation).collect(),
            rotations: vec![Quat::IDENTITY; model.nodes.len()],
        }
    }

    /// 将另一姿态按权重混入当前快照，复用已有节点数组。
    pub(crate) fn blend_in_place(&mut self, other: &Self, t: f32) {
        for (current, incoming) in self.translations.iter_mut().zip(&other.translations) {
            *current = current.lerp(*incoming, t);
        }
        for (current, incoming) in self.rotations.iter_mut().zip(&other.rotations) {
            *current = current.slerp(*incoming, t);
        }
    }
}

/// 动作来源
#[derive(Clone, Copy, PartialEq, Debug)]
enum Source {
    /// 基础待机循环
    Idle,
    /// 行走
    Walking,
    /// 随机待机子动画
    Sub(usize),
    /// 点击反应
    Interact,
}

/// 一条动画轨道：来源、本地时间与淡入淡出权重。
#[derive(Debug)]
struct Track {
    source: Source,
    time: f32,
    weight: f32,
    target: f32,
    /// 一次性轨道的定格时长；循环轨道为 INFINITY
    duration: f32,
}

/// 注视目标：屏幕模式每帧经活相机反投影；场景模式为世界固定点。
#[derive(Clone, Copy, Debug, PartialEq)]
pub(crate) enum GazeTarget {
    None,
    Screen(Vec2),
    Scene(Vec3),
}

/// 注视解算上下文：活相机视图基、观察距离与方位偏移。
/// 旧固定机位与场景相机都折算为同一组量，解算端只有一条代码路径。
pub(crate) struct GazeRig {
    pub(crate) basis: crate::render::ViewBasis,
    /// 观察距离（顶点像素单位）
    pub(crate) distance_px: f32,
    /// 相机方位角相对旧固定机位的偏移（弧度；背对切断用）
    pub(crate) yaw_offset: f32,
}

impl GazeRig {
    /// 旧固定机位（与历史解算逐位一致）。
    pub(crate) fn legacy(view: Vec2) -> Self {
        Self {
            basis: crate::render::legacy_basis(view),
            distance_px: LEGACY_VIEW_DISTANCE,
            yaw_offset: 0.,
        }
    }

    /// 旧固定机位的相对距离版本，注视换算与渲染共用同一缩放。
    pub(crate) fn legacy_with_distance(view: Vec2, distance: f32) -> Self {
        let distance = distance.max(1e-3);
        Self {
            basis: crate::render::legacy_basis_with_distance(view, distance),
            distance_px: LEGACY_VIEW_DISTANCE * distance,
            yaw_offset: 0.,
        }
    }

    /// 使用已经计算好的场景视图基构造注视上下文。
    pub(crate) fn from_view(
        camera: &crate::scene::Camera,
        basis: crate::render::ViewBasis,
    ) -> Self {
        Self {
            basis,
            distance_px: camera.distance * crate::model::PIXELS_PER_UNIT,
            yaw_offset: camera.azimuth.to_radians() - front_yaw(),
        }
    }
}

/// 旧固定机位的相机-模型距离（(30,23,70) 的模长）。
const LEGACY_VIEW_DISTANCE: f32 = 79.55501241279521;

/// 手动动画对象的播放状态（api::Animation 门面控制，本层推进时间与权重）。
#[derive(Debug)]
pub(crate) struct ManualState {
    pub(crate) clip: usize,
    pub(crate) time: f32,
    pub(crate) speed: f32,
    pub(crate) loop_: bool,
    pub(crate) playing: bool,
    pub(crate) weight: f32,
    pub(crate) target_weight: f32,
}

impl Track {
    fn looping(source: Source) -> Self {
        Self {
            source,
            time: 0.,
            weight: 1.,
            target: 1.,
            duration: f32::INFINITY,
        }
    }

    fn oneshot(source: Source, duration: f32) -> Self {
        Self {
            source,
            time: 0.,
            weight: 0.,
            target: 1.,
            duration,
        }
    }

    /// 权重从 0 淡入的循环轨道(一次性动作结束后重建基础动画用)
    fn base_fade_in(source: Source) -> Self {
        Self {
            source,
            time: 0.,
            weight: 0.,
            target: 1.,
            duration: f32::INFINITY,
        }
    }

    fn is_base(&self) -> bool {
        matches!(self.source, Source::Idle | Source::Walking)
    }

    fn is_oneshot(&self) -> bool {
        matches!(self.source, Source::Sub(_) | Source::Interact)
    }
}

/// 姿态状态：动画轨道混合、手动动画层，以及注视/偏航/压扁/摆臂等程序化效果。
/// 每帧经 update / update_with_rig 推进，输出节点局部平移（像素）与旋转。
pub struct Skeleton {
    pub(crate) model: Arc<ModelData>,
    joints: Joints,
    /// 随机子动画剪辑索引（文档序）
    sub_indices: Vec<usize>,
    /// 基础待机剪辑索引
    idle_index: Option<usize>,

    pub tau_gaze: f32,
    pub tau_yaw: f32,
    /// 动画轨道交叉淡入淡出时长(秒)
    pub fade_seconds: f32,
    pub tau_recenter: f32,
    /// 注视夹角限制（弧度，水平 / 垂直）。
    pub head_range: Vec2,
    /// 压扁深度百分比（0..=30）。
    pub squash_depth: f32,

    gaze_target: GazeTarget,
    yaw_target: f32,

    /// 当前基础动画；None 为停止（静止姿态）。
    animation: Option<(AnimationId, f32, bool)>,
    /// 动画轨道：活跃轨道与淡出中的旧轨道并存，按插入序混合。
    tracks: Vec<Track>,
    /// 待机调度（基础循环时间 + 随机子动画计时）
    idle_clips: IdleClips,

    /// 平滑后的注视增量（弧度，水平 / 垂直）。
    gaze: Vec2,
    /// 背对切断锁存：越过切断角置位，拖回恢复角以下复位。
    gaze_yaw_cut: bool,
    /// 过扭切断锁存：需求扭角越过放弃角置位，回到恢复角以内复位。
    gaze_twist_cut: bool,
    model_yaw: f32,
    /// 压扁脉冲已进行时间(秒)，None 为未播放
    squash_phase: Option<f32>,

    // 逐帧输出，render.rs 消费：节点局部平移（像素）与旋转（GLTF 基）。
    // 已驱动的关节由本层完全给定；未驱动节点渲染端回退静止值。
    pub translations: Vec<Vec3>,
    pub rotations: Vec<Quat>,
    /// 混合后的目标姿态（注视合成与测试读取）
    targets: NodeSnapshot,
    /// 头部最终局部旋转（注视平滑状态）
    head_slerp: Quat,
    /// 披风欧拉 z 的当前值（行走通道的 "保持当前" 语义）
    cape_z: f32,
    pub squash: f32,
    pub body_yaw: f32,

    /// 攻击摆臂已进行时间(秒)，None 为未播放
    swing_elapsed: Option<f32>,
    pub swing_progress: f32,
    swing_side: usize,

    /// 手动动画对象（并行权重混合层；常规调度不受影响）
    manual: Vec<ManualState>,
}

impl Skeleton {
    pub(crate) fn new(model: Arc<ModelData>) -> Self {
        let joints = Joints::resolve(&model);
        let sub_indices = model.clips_with_prefix("idle_sub_");
        let idle_index = model.clip("idle").map(|_| {
            model
                .clips
                .iter()
                .position(|c| c.name == "idle")
                .unwrap_or(0)
        });
        let rest = NodeSnapshot::rest(&model);
        Self {
            joints,
            sub_indices,
            idle_index,
            tau_gaze: TAU_GAZE,
            tau_yaw: TAU_YAW,
            fade_seconds: FADE_SECONDS,
            tau_recenter: TAU_RECENTER,
            head_range: Vec2::new(60f32.to_radians(), 53f32.to_radians()),
            squash_depth: 5.,
            gaze_target: GazeTarget::None,
            yaw_target: 0.,
            animation: Some((AnimationId::Idle, 1., true)),
            tracks: vec![Track::looping(Source::Idle)],
            idle_clips: IdleClips::new(),
            gaze: Vec2::ZERO,
            gaze_yaw_cut: false,
            gaze_twist_cut: false,
            model_yaw: 0.,
            squash_phase: None,
            translations: rest.translations.clone(),
            rotations: rest.rotations.clone(),
            targets: rest,
            head_slerp: Quat::IDENTITY,
            cape_z: 0.,
            squash: 1.,
            body_yaw: 0.,
            swing_elapsed: None,
            swing_progress: 0.,
            swing_side: 1,
            manual: Vec::new(),
            model,
        }
    }

    /// 注视目标：屏幕像素（随活相机逐帧反投影）。
    pub fn set_gaze_target(&mut self, target: Option<Vec2>) {
        self.gaze_target = target.map_or(GazeTarget::None, GazeTarget::Screen);
    }

    /// 注视目标：场景空间固定点（世界坐标，模型互看用）。
    pub fn set_gaze_scene(&mut self, target: Option<Vec3>) {
        self.gaze_target = target.map_or(GazeTarget::None, GazeTarget::Scene);
    }

    pub fn gaze_target(&self) -> Option<Vec2> {
        match self.gaze_target {
            GazeTarget::Screen(p) => Some(p),
            _ => None,
        }
    }

    /// 设置偏航目标角。
    pub fn set_yaw_target(&mut self, yaw: f32) {
        self.yaw_target += wrap_pi(yaw - self.yaw_target);
    }

    pub fn model_yaw(&self) -> f32 {
        self.model_yaw
    }

    /// 以固定种子重建待机调度，供确定性回放。
    pub fn seed_idle(&mut self, seed: u64) {
        self.idle_clips = IdleClips::seeded(seed);
    }

    pub fn head_follow(&self) -> bool {
        // 随机子动画期间头部交给剪辑演绎，不追光标
        // 结束后视线状态已清零，按 τ_gaze 重新收敛回当前光标。
        self.animation.is_some_and(|(_, _, follow)| follow)
            && !self
                .tracks
                .iter()
                .any(|t| matches!(t.source, Source::Sub(_)) && t.target == 1.)
    }

    /// 播放动画。
    pub fn play_animation(&mut self, id: AnimationId, speed: f32, head_follow: bool) {
        for track in &mut self.tracks {
            if track.is_base() || matches!(track.source, Source::Sub(_)) {
                track.target = 0.;
            }
        }
        let source = match id {
            AnimationId::Idle => Source::Idle,
            AnimationId::Walking => Source::Walking,
        };
        self.tracks.push(Track::looping(source));
        self.animation = Some((id, speed.max(0.01), head_follow));
        self.idle_clips.reset();
    }

    /// 停止动画。
    pub fn stop_animation(&mut self) {
        self.animation = None;
        for track in &mut self.tracks {
            track.target = 0.;
        }
        self.idle_clips.reset();
    }

    /// 播放点击反应动画；播放中（未定格）重复调用无效果。
    pub fn play_interact(&mut self) {
        if self
            .tracks
            .iter()
            .any(|t| t.source == Source::Interact && t.target == 1. && t.time < t.duration)
        {
            return;
        }
        for track in &mut self.tracks {
            if track.is_oneshot() {
                track.target = 0.;
            }
        }
        self.tracks
            .push(Track::oneshot(Source::Interact, clip::INTERACT_DURATION));
    }

    /// 播放点按压扁。`now` 仅为兼容保留，内部使用有界相位计时。
    pub fn squash_tap(&mut self, _now: f32) {
        self.squash_phase = Some(0.);
        self.squash = 1.;
    }

    /// 播放攻击摆臂。`now` 仅为兼容保留，内部使用有界相位计时。
    pub fn click(&mut self, _now: f32) {
        self.swing_elapsed = Some(0.);
        self.swing_progress = 0.;
        self.body_yaw = 0.;
    }

    /// 推进所有轨道。
    fn advance(&mut self, dt: f32) {
        let mut finished = false;
        for track in &mut self.tracks {
            if !track.is_oneshot() {
                continue;
            }
            track.time = (track.time + dt.min(0.05)).min(track.duration);
            if track.target == 1. && track.time >= track.duration {
                track.target = 0.;
                finished = true;
            }
        }
        if finished {
            // 基础轨道若已在淡出完成后被回收
            let base = self
                .tracks
                .iter_mut()
                .filter(|t| t.is_base())
                .max_by(|a, b| a.weight.total_cmp(&b.weight));
            match base {
                Some(base) => {
                    base.target = 1.;
                    if let Some((AnimationId::Idle, ..)) = self.animation {
                        self.idle_clips.rearm();
                    }
                }
                None => {
                    // 基础轨道已回收：从循环起点淡入重建，相位归零被淡入掩盖。
                    if let Some((AnimationId::Idle, ..)) = self.animation {
                        self.idle_clips.rebase();
                    }
                    if let Some((id, _, _)) = self.animation {
                        let source = match id {
                            AnimationId::Idle => Source::Idle,
                            AnimationId::Walking => Source::Walking,
                        };
                        self.tracks.push(Track::base_fade_in(source));
                    }
                }
            }
        }

        // 基础循环推进
        match self.animation {
            Some((AnimationId::Idle, speed, _)) => {
                let oneshot_active = self.tracks.iter().any(|t| t.is_oneshot() && t.target == 1.);
                let fired = if oneshot_active {
                    None
                } else {
                    let idle_clip = self.idle_index.map(|i| &self.model.clips[i]);
                    match idle_clip {
                        Some(idle) => self.idle_clips.tick(dt, speed, &self.sub_indices, idle),
                        None => None,
                    }
                };
                if let Some(sub) = fired {
                    for track in &mut self.tracks {
                        if track.is_base() {
                            track.target = 0.;
                        }
                    }
                    let duration = self
                        .sub_indices
                        .get(sub)
                        .map(|&i| clip::clip_duration(&self.model.clips[i]))
                        .unwrap_or(0.);
                    self.tracks.push(Track::oneshot(Source::Sub(sub), duration));
                }
            }
            Some((AnimationId::Walking, speed, _)) => {
                if let Some(base) = self.tracks.iter_mut().find(|t| t.source == Source::Walking) {
                    // 按整周期回绕:长时间行走不会损失 f32 相位精度
                    base.time = (base.time + (dt * speed).min(0.05)) % WALKING_PERIOD;
                }
            }
            None => {}
        }

        // 权重线性斜坡与轨道回收。
        let rate = (dt / self.fade_seconds).max(0.);
        self.tracks.retain_mut(|track| {
            if track.weight < track.target {
                track.weight = (track.weight + rate).min(track.target);
            } else if track.weight > track.target {
                track.weight = (track.weight - rate).max(track.target);
            }
            track.weight > 0. || track.target > 0.
        });
    }

    /// 静止姿态：全部节点取静止 TR，披风取骨架基准欧拉 (0.06π, 0)。
    fn rest_snapshot(&self) -> NodeSnapshot {
        let mut rest = NodeSnapshot::rest(&self.model);
        if let Some(cape) = self.joints.cape {
            rest.rotations[cape] = clip::cape_quat(std::f32::consts::PI * 0.06, 0.);
        }
        rest
    }

    fn sample_track(&self, track: &Track, cape_z: f32) -> NodeSnapshot {
        match track.source {
            Source::Idle => {
                let Some(idle) = self.model.clip("idle") else {
                    return self.rest_snapshot();
                };
                clip::sample(&self.model, idle, self.idle_clips.base_time)
            }
            Source::Walking => self.snapshot_from_frame(evaluate_walking(track.time), cape_z),
            Source::Sub(index) => {
                let Some(&clip_index) = self.sub_indices.get(index) else {
                    return self.rest_snapshot();
                };
                clip::sample(&self.model, &self.model.clips[clip_index], track.time)
            }
            Source::Interact => {
                let Some(interact) = self.model.clip("interact") else {
                    return self.rest_snapshot();
                };
                clip::sample(&self.model, interact, track.time)
            }
        }
    }

    /// 把行走等程序化欧拉帧折算成节点局部姿态（GLTF 基）。
    fn snapshot_from_frame(&self, frame: AnimFrame, cape_z: f32) -> NodeSnapshot {
        let mut snap = self.rest_snapshot();
        let joints = self.joints;
        if let Some(body) = joints.body {
            snap.rotations[body] = rotation(frame.body_rot.x, frame.body_rot.y, frame.body_rot.z);
            snap.translations[body] = frame.body_pos;
        }
        if let Some(head) = joints.head {
            snap.rotations[head] = rotation(frame.head_rot.x, frame.head_rot.y, frame.head_rot.z);
            snap.translations[head].y += frame.head_pos;
        }
        for (slot, arm) in joints.arms.iter().enumerate() {
            if let Some(node) = *arm {
                let e = frame.arms[slot];
                snap.rotations[node] = rotation(e.x, e.y, e.z);
            }
        }
        for (slot, leg) in joints.legs.iter().enumerate() {
            if let Some(node) = *leg {
                let e = frame.legs[slot];
                snap.rotations[node] = rotation(e.x, e.y, e.z);
            }
        }
        if let Some(cape) = joints.cape {
            snap.rotations[cape] = clip::cape_quat(frame.cape_x, frame.cape_z.unwrap_or(cape_z));
        }
        snap
    }

    /// 按权重混合所有轨道，权重之和不足 1 的部分回落到静止姿态
    fn blended(&self) -> NodeSnapshot {
        let cape_z = self.cape_z;
        let mut acc_weight = 0.;
        let mut acc: Option<NodeSnapshot> = None;
        for track in &self.tracks {
            if track.weight <= 0. {
                continue;
            }
            let pose = self.sample_track(track, cape_z);
            match &mut acc {
                None => acc = Some(pose),
                Some(current) => {
                    current.blend_in_place(&pose, track.weight / (acc_weight + track.weight));
                }
            }
            acc_weight += track.weight;
        }
        // 手动动画与自动轨道使用同一套权重归一化规则。
        for st in &self.manual {
            if st.weight <= 0. {
                continue;
            }
            let pose = clip::sample(&self.model, &self.model.clips[st.clip], st.time);
            match &mut acc {
                None => acc = Some(pose),
                Some(current) => {
                    current.blend_in_place(&pose, st.weight / (acc_weight + st.weight));
                }
            }
            acc_weight += st.weight;
        }
        let mut result = acc.unwrap_or_else(|| self.rest_snapshot());
        if acc_weight < 1. {
            result.blend_in_place(&self.rest_snapshot(), 1. - acc_weight);
        }
        result
    }

    /// 逐帧推进。
    /// `now` 仅为兼容保留，内部计时全部由帧间隔 `dt` 驱动;
    /// `view` 为输出视图尺寸（像素），注视目标坐标与之同空间。
    /// 等效于旧固定机位的注视上下文。
    pub fn update(&mut self, view: Vec2, _now: f32, dt: f32) {
        self.update_with_rig(&GazeRig::legacy(view), dt);
    }

    /// 场景路径入口：注视解算经活相机视图基。
    pub(crate) fn update_with_rig(&mut self, rig: &GazeRig, dt: f32) {
        // 负间隔非法：平滑系数与动画进度都不允许倒退。
        let dt = dt.max(0.);
        self.advance(dt);
        let targets = self.blended();
        self.translations.clone_from(&targets.translations);
        self.rotations.clone_from(&targets.rotations);
        self.targets = targets;

        // 整体偏航平滑。
        self.model_yaw += (self.yaw_target - self.model_yaw) * (1. - (-dt / self.tau_yaw).exp());
        let shift = wrap_pi(self.model_yaw) - self.model_yaw;
        self.model_yaw += shift;
        self.yaw_target += shift;

        // 头部姿态：
        // 追踪中  世界空间解算指向输入点的朝向，受颈部活动范围截断，以 τ_gaze 平滑追随；
        //        背对摄像机或头-身水平扭角超限时放弃跟随，头部回正到身体朝向。
        // 未追踪  与身体朝向对齐（仅动画摆动），以 τ_recenter 平滑回正。
        let parent = Quat::from_rotation_y(self.model_yaw + front_yaw());
        let blended_head = self.targets.rotations[self.joints.head.unwrap_or(0)];
        let gaze_req = match self.gaze_target {
            GazeTarget::None => None,
            t => Some(t),
        };
        let (head_target, tau) = match gaze_req.filter(|_| self.head_follow()) {
            Some(_) if self.joints.head.is_some() => {
                let raw = self.solve_gaze(rig);
                // 背对切断（迟滞）：model_yaw 即相对相机正面的偏航
                //（活相机按方位偏移折算）。
                let facing = wrap_pi(self.model_yaw - rig.yaw_offset).abs();
                if facing > GAZE_OFF_YAW {
                    self.gaze_yaw_cut = true;
                } else if facing < GAZE_ON_YAW {
                    self.gaze_yaw_cut = false;
                }
                // 过扭放弃（迟滞）：头-身相对水平扭角。
                let twist = wrap_pi(raw.x - self.model_yaw).abs();
                if twist > GAZE_TWIST_OFF {
                    self.gaze_twist_cut = true;
                } else if twist < GAZE_TWIST_ON {
                    self.gaze_twist_cut = false;
                }
                if self.gaze_yaw_cut || self.gaze_twist_cut {
                    self.gaze = Vec2::ZERO;
                    (blended_head, self.tau_recenter)
                } else {
                    let target = Vec2::new(
                        raw.x.clamp(-self.head_range.x, self.head_range.x),
                        raw.y.clamp(-self.head_range.y, self.head_range.y),
                    );
                    self.gaze += (target - self.gaze) * (1. - (-dt / self.tau_gaze).exp());
                    // 头部局部旋转换入渲染基做注视合成，再换回原始基输出
                    let head_world = view_quat()
                        * Quat::from_rotation_y(self.gaze.x)
                        * Quat::from_rotation_x(self.gaze.y)
                        * flip_quat(blended_head);
                    (flip_quat(parent.inverse() * head_world), self.tau_gaze)
                }
            }
            _ => {
                self.gaze = Vec2::ZERO;
                self.gaze_yaw_cut = false;
                self.gaze_twist_cut = false;
                (blended_head, self.tau_recenter)
            }
        };
        if self.joints.head.is_some() {
            self.head_slerp = self
                .head_slerp
                .slerp(head_target, 1. - (-dt / tau).exp())
                .normalize();
            self.rotations[self.joints.head.unwrap()] = self.head_slerp;
        }

        // 压扁脉冲。
        if let Some(phase) = self.squash_phase.as_mut() {
            *phase += dt;
            if *phase >= SQUASH_SECONDS {
                self.squash_phase = None;
            }
        }
        self.squash = 1.
            - self.squash_phase.map_or(0., squash_pulse)
                * (self.squash_depth / 100.).clamp(0., 0.3);

        // 攻击摆臂。
        if let Some(elapsed) = self.swing_elapsed.as_mut() {
            *elapsed += dt;
            if *elapsed >= SWING_SECONDS {
                self.swing_elapsed = None;
            }
        }
        self.swing_progress = self
            .swing_elapsed
            .map_or(0., |e| (e / SWING_SECONDS).clamp(0., 1.));
        self.body_yaw = if self.swing_elapsed.is_some() {
            -(self.swing_progress.sqrt() * std::f32::consts::TAU).sin()
                * 0.2
                * if self.swing_side == 0 { -1. } else { 1. }
        } else {
            0.
        };

        // 程序化扭转并入躯干局部旋转；攻击摆臂覆写对应臂。
        if let Some(body) = self.joints.body {
            self.rotations[body] = self.rotations[body] * Quat::from_rotation_y(self.body_yaw);
        }
        if self.swing_elapsed.is_some() {
            let side = self.swing_side;
            if let Some(node) = self.joints.arms[side] {
                let sign = if side == 0 { -1. } else { 1. };
                let p = self.swing_progress;
                let pi = std::f32::consts::PI;
                // 原版 HumanoidModel 攻击曲线：四次缓动、躯干扭转与侧向翻滚。
                let eased = 1. - (1. - p).powi(4);
                let strike =
                    (eased * pi).sin() * 1.2 + (p * pi).sin() * -(self.gaze.y - 0.7) * 0.75;
                self.rotations[node] =
                    rotation(strike, self.body_yaw * 3., -sign * ((p * pi).sin() * 0.4));
            }
        }

        // 披风欧拉 z 的保持值（行走轨道的 cape_z 语义）
        if let Some(cape) = self.joints.cape {
            self.cape_z = clip::cape_euler(flip_quat(self.rotations[cape])).y;
        }

        // 手动动画对象：时间推进与权重斜坡（一次性播完自动淡出）。
        let rate = (dt / self.fade_seconds).max(0.);
        for st in &mut self.manual {
            if st.playing {
                st.time += (dt * st.speed).max(0.).min(0.05);
                let dur = clip::clip_duration(&self.model.clips[st.clip]).max(1e-6);
                if st.loop_ {
                    st.time %= dur;
                } else if st.time >= dur {
                    st.time = dur;
                    st.target_weight = 0.;
                }
            }
            if st.weight < st.target_weight {
                st.weight = (st.weight + rate).min(st.target_weight);
            } else if st.weight > st.target_weight {
                st.weight = (st.weight - rate).max(st.target_weight);
            }
        }
    }

    /// 手动动画对象槽位（按剪辑索引查找，无则创建）。
    pub(crate) fn manual_slot(&mut self, clip: usize) -> usize {
        if let Some(i) = self.manual.iter().position(|s| s.clip == clip) {
            return i;
        }
        self.manual.push(ManualState {
            clip,
            time: 0.,
            speed: 1.,
            loop_: false,
            playing: false,
            weight: 0.,
            target_weight: 0.,
        });
        self.manual.len() - 1
    }

    pub(crate) fn manual(&self, index: usize) -> &ManualState {
        &self.manual[index]
    }

    pub(crate) fn manual_mut(&mut self, index: usize) -> &mut ManualState {
        &mut self.manual[index]
    }

    /// 经活相机视图基解算指向目标的 yaw/pitch（未按颈部活动范围夹取）。
    /// 屏幕模式为"眼睛投影与光标的屏幕差 × 观察距离"；场景模式为
    /// "目标投影与眼睛投影的屏幕差 × 目标真实距离"。
    fn solve_gaze(&self, rig: &GazeRig) -> Vec2 {
        let Some(head) = self.joints.head else {
            return Vec2::ZERO;
        };
        // 眼睛的世界位置：根偏航 ∘ 颈部（头部枢轴，含 bob）+ 相机基准上移。
        // 头部枢轴 = 髋部 12px + 头关节局部 y（静止 12px，含行走 bob）。
        let head_pivot_y =
            self.model.nodes[head].rest_translation.y + self.targets.translations[head].y;
        let neck = Mat4::from_rotation_y(self.model_yaw + front_yaw())
            * Mat4::from_translation(Vec3::Y * head_pivot_y);
        let view_basis = view_quat();
        let eye = neck.transform_point3(Vec3::ZERO) + view_basis * (Vec3::Y * 3.);
        let (p, _) = rig.basis.project_view(eye);
        let unit = rig.basis.scale;
        let (dx, dy, distance) = match self.gaze_target {
            GazeTarget::Screen(cursor) => (
                (cursor.x - p.x) / unit,
                (p.y - cursor.y) / unit,
                rig.distance_px,
            ),
            GazeTarget::Scene(target) => {
                let (pt, _) = rig.basis.project_view(target);
                (
                    (pt.x - p.x) / unit,
                    (p.y - pt.y) / unit,
                    (target - eye).length(),
                )
            }
            GazeTarget::None => return Vec2::ZERO,
        };
        let h = dx.atan2(distance);
        let v = (-dy / (dx * dx + dy * dy + distance * distance).sqrt()).asin();
        Vec2::new(h, v)
    }

    // ===== 测试辅助 =====
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::builtin_data;
    use crate::pose::rotation;
    use crate::skin::ArmModel;
    use glam::Quat;

    fn model() -> Arc<ModelData> {
        builtin_data(ArmModel::Classic).unwrap()
    }

    fn skeleton() -> Skeleton {
        Skeleton::new(model())
    }

    fn sub_active(s: &Skeleton) -> bool {
        s.tracks
            .iter()
            .any(|t| matches!(t.source, Source::Sub(_)) && t.target == 1.)
    }

    fn active_sub(s: &Skeleton) -> Option<usize> {
        s.tracks.iter().find_map(|t| match t.source {
            Source::Sub(index) if t.target == 1. => Some(index),
            _ => None,
        })
    }

    #[test]
    fn gaze_recenters_after_target_removed() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.set_gaze_target(Some(Vec2::new(400., 100.)));
        for i in 0..120 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.x > 0.01);
        s.set_gaze_target(None);
        for i in 120..600 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() < 0.001, "gaze = {:?}", s.gaze);
    }

    #[test]
    fn random_sub_suspends_gaze_until_finished() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.set_gaze_target(Some(Vec2::new(900., 100.)));
        // 2 秒：注视收敛
        for i in 0..120 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() > 0.01);
        // 推进到子动画播放中（固定种子下 8 秒触发）
        for i in 120..540 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(sub_active(&s), "sub must be playing at 9s");
        // 子动画期间移动光标：头部只跟剪辑，不追光标
        s.set_gaze_target(Some(Vec2::new(100., 400.)));
        for i in 540..570 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() < 0.001, "gaze must be suspended during sub");
        let head = s.model.joint("Head").unwrap();
        assert!(
            s.rotations[head].abs_diff_eq(s.targets.rotations[head], 0.2),
            "head must follow the clip during sub"
        );
        // 子动画播完（11 秒）：头部平滑转回新光标位置
        for i in 570..800 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(!sub_active(&s), "sub must have finished by 13.3s");
        assert!(s.gaze.length() > 0.01, "gaze must resume after sub");
    }

    #[test]
    fn gaze_follows_directly_within_twist_limit() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        // 需求角在自然范围内：直接跟随，注视收敛到需求角本身。
        // 只跑 5 秒：8 秒起随机子动画会暂时接管头部。
        s.set_gaze_target(Some(Vec2::new(920., 262.5)));
        for i in 0..300 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let expected = s.solve_gaze(&GazeRig::legacy(view));
        assert!((s.gaze.x - expected.x).abs() < 0.001);
    }

    #[test]
    fn gaze_abandons_beyond_twist_limit() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        // 需求扭角远超自然范围：放弃跟随，头部回正到身体朝向。
        s.set_gaze_target(Some(Vec2::new(5000., 262.5)));
        for i in 0..600 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() < 0.001, "gaze = {:?}", s.gaze);
        let head = s.model.joint("Head").unwrap();
        assert!(
            s.rotations[head].abs_diff_eq(s.targets.rotations[head], 0.2),
            "head must realign with body"
        );
    }

    #[test]
    fn back_facing_cuts_gaze_with_hysteresis() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.set_gaze_target(Some(Vec2::new(920., 262.5)));
        // 全程压在 8 秒（首次随机子动画）之前：子动画会重置视线状态、
        // 结束后按新鲜状态重新评估切断锁存。
        for i in 0..90 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let expected = s.solve_gaze(&GazeRig::legacy(view));
        assert!((s.gaze.x - expected.x).abs() < 0.001);
        // 背对：切断跟随，头部回正。
        s.set_yaw_target(std::f32::consts::PI);
        for i in 90..180 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() < 0.001);
        let head = s.model.joint("Head").unwrap();
        assert!(
            s.rotations[head].abs_diff_eq(s.targets.rotations[head], 0.2),
            "head must realign while back-facing"
        );
        // 拖回 82°（处于 75°~90° 迟滞区间）：保持切断。
        s.set_yaw_target(82f32.to_radians());
        for i in 180..270 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(
            s.gaze.length() < 0.001,
            "hysteresis must hold within (75°, 90°) from camera"
        );
        // 回到正面：恢复跟随。
        s.set_yaw_target(0.);
        for i in 270..420 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let expected = s.solve_gaze(&GazeRig::legacy(view));
        assert!((s.gaze.x - expected.x).abs() < 0.001);
    }

    #[test]
    fn gaze_rules_mirror_for_rightward_turns() {
        // 上述规则的镜像：身体向右转、光标在屏幕左侧，判定量级必须一致。
        // 全程压在 8 秒（首次随机子动画）之前。
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        // 需求角超夹角上限、未过扭：贴在负向夹角上限。
        s.set_gaze_target(Some(Vec2::new(-443., 262.5)));
        for i in 0..90 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let expected = s.solve_gaze(&GazeRig::legacy(view));
        assert!((s.gaze.x - expected.x).abs() < 0.001);
        // 需求扭角远超自然范围：放弃。
        s.set_gaze_target(Some(Vec2::new(-4500., 262.5)));
        for i in 90..180 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() < 0.001, "gaze = {:?}", s.gaze);
        // 光标回到可跟随位置，继续背对切断的验证。
        s.set_gaze_target(Some(Vec2::new(-443., 262.5)));
        // 背对（负方向）切断。
        s.set_yaw_target(-std::f32::consts::PI);
        for i in 180..270 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.gaze.length() < 0.001);
        // 拖回 -82°（处于 -90°~-75° 迟滞区间）：保持切断。
        s.set_yaw_target(-82f32.to_radians());
        for i in 270..360 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(
            s.gaze.length() < 0.001,
            "hysteresis must hold within (-90°, -75°) from camera"
        );
        // 回到正面：恢复跟随，看向左侧光标。
        s.set_yaw_target(0.);
        for i in 360..450 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let expected = s.solve_gaze(&GazeRig::legacy(view));
        assert!((s.gaze.x - expected.x).abs() < 0.001);
    }

    #[test]
    fn fast_full_turn_drag_keeps_direction() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let tau = std::f32::consts::TAU;
        // 18 帧内把目标拖过一整圈(整宽快拖)
        let mut prev = s.model_yaw();
        let mut total = 0.;
        for i in 1..=18 {
            s.set_yaw_target(i as f32 / 18. * tau);
            s.update(view, i as f32 / 60., 1. / 60.);
            // 规范化意义下的帧间位移必须始终朝拖拽方向
            let step = wrap_pi(s.model_yaw() - prev);
            assert!(step > -1e-6, "yaw reversed at frame {i}: {step}");
            total += step;
            prev = s.model_yaw();
        }
        // 松手后继续沿同方向收敛到整圈目标
        for i in 18..180 {
            s.update(view, i as f32 / 60., 1. / 60.);
            let step = wrap_pi(s.model_yaw() - prev);
            total += step;
            prev = s.model_yaw();
        }
        assert!(total > tau - 0.1, "must have chased the full turn: {total}");
        // 稳定后视觉停在整圈位置(与起点同相)
        assert!(wrap_pi(s.model_yaw()).abs() < 0.05);
    }

    #[test]
    fn model_yaw_approaches_target() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.set_yaw_target(std::f32::consts::FRAC_PI_2);
        for i in 0..600 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!((s.model_yaw - std::f32::consts::FRAC_PI_2).abs() < 0.001);
    }

    #[test]
    fn recentered_head_aligns_with_body() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.set_yaw_target(std::f32::consts::FRAC_PI_2);
        for i in 0..600 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        // 无输入时头部与身体朝向对齐（仅存动画摆动），不额外朝相机偏转。
        // 子动画的头部摆动存在 τ_recenter 量级的追踪滞后，容差随之放宽。
        let head = s.model.joint("Head").unwrap();
        assert!(
            s.rotations[head].abs_diff_eq(s.targets.rotations[head], 0.2),
            "head={:?} sway={:?}",
            s.rotations[head],
            s.targets.rotations[head]
        );
    }

    #[test]
    fn interact_overrides_then_returns() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        for i in 0..60 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        s.play_interact();
        s.play_interact(); // 播放中重复调用无效
        assert_eq!(
            s.tracks
                .iter()
                .filter(|t| t.source == Source::Interact)
                .count(),
            1
        );
        s.update(view, 1.01, 1. / 60.);
        // 0.5 秒后结束：回归基础动画并重新计时随机子动画
        for i in 1..40 {
            s.update(view, 1.01 + i as f32 / 60., 1. / 60.);
        }
        assert!(
            !s.tracks
                .iter()
                .any(|t| t.source == Source::Interact && t.target == 1.),
            "interact must end after 0.5s"
        );
        // 结束后重新计时（当帧起倒计时已扣除若干帧的推进量）
        assert!(
            s.idle_clips.countdown > clip::SUB_INTERVAL - 0.5,
            "sub countdown must be re-armed after interact"
        );
    }

    /// 一次性动画播完时基础轨道未淡出（interact 期间一直在底下混），
    /// 基础相位必须保持冻结值继续，而非全权重下跳回循环起点。
    /// 回归：旧回归逻辑无条件 rebase/reset 基础相位。
    #[test]
    fn oneshot_finish_keeps_base_idle_phase() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 1. / 60.;
        let mut now = 0.;
        // 2 秒：idle 相位推进到循环中段（0.1, 3-0.1）之间
        for _ in 0..120 {
            s.update(view, now, dt);
            now += dt;
        }
        let idle_dur = clip::clip_duration(s.model.clip("idle").unwrap());
        let phase = s.idle_clips.base_time;
        assert!(
            phase > 0.1 && phase < idle_dur - 0.1,
            "phase {phase} must be mid-loop"
        );

        s.play_interact();
        // 0.5 秒 = 30 帧后 interact 定格：结束当帧起 tick 恢复（+1 帧）
        for _ in 0..30 {
            s.update(view, now, dt);
            now += dt;
        }
        assert!(
            (s.idle_clips.base_time - (phase + dt)).abs() < 0.02 && s.idle_clips.base_time > 1.,
            "base phase must continue from frozen value, got {}",
            s.idle_clips.base_time
        );
    }

    /// 同上：行走基础轨道的相位在 interact 结束后不得归零。
    /// 行走相位在一次性动作期间照常推进，播完应从该处继续。
    #[test]
    fn oneshot_finish_keeps_walking_phase() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 1. / 60.;
        let mut now = 0.;
        s.play_animation(AnimationId::Walking, 1., true);
        for _ in 0..120 {
            s.update(view, now, dt);
            now += dt;
        }
        let phase = s
            .tracks
            .iter()
            .find(|t| t.source == Source::Walking)
            .unwrap()
            .time;

        s.play_interact();
        for _ in 0..30 {
            s.update(view, now, dt);
            now += dt;
        }
        let after = s
            .tracks
            .iter()
            .find(|t| t.source == Source::Walking)
            .unwrap()
            .time;
        let expected = (phase + 0.5) % WALKING_PERIOD;
        assert!(
            (after - expected).abs() < 0.02,
            "walking phase must keep advancing, got {after}, expected {expected}"
        );
    }

    #[test]
    fn play_animation_keeps_interact_until_finished() {
        // interact 是 0.5 秒一次性动作：切换基础动画不打断它，
        // 播完后淡回到切换后的动画。停止动画则立即撤销它。
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        for i in 0..60 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        s.play_interact();
        s.play_animation(AnimationId::Walking, 1., true);
        s.update(view, 1.01, 1. / 60.);
        assert!(
            s.tracks
                .iter()
                .any(|t| t.source == Source::Interact && t.target == 1.),
            "switch must not cancel interact"
        );
        for i in 1..40 {
            s.update(view, 1.01 + i as f32 / 60., 1. / 60.);
        }
        assert!(!s
            .tracks
            .iter()
            .any(|t| t.source == Source::Interact && t.target == 1.));

        s.play_interact();
        s.stop_animation();
        assert!(
            !s.tracks
                .iter()
                .any(|t| t.source == Source::Interact && t.target == 1.),
            "stop must cancel interact"
        );
    }

    #[test]
    fn long_run_stays_finite() {
        // 10 虚拟分钟：注视持续输入、周期性 interact/压扁、待机与行走交替,
        // 宿主时钟取一年后的巨值(长 uptime 场景,精度极差),
        // 全程不得出现非有限值、姿态发散或轨道泄漏。
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.set_gaze_target(Some(Vec2::new(600., 200.)));
        let huge_now = 86_400. * 365.;
        for i in 0..(60 * 60 * 10) {
            if i % 900 == 300 {
                s.play_interact();
            }
            if i % 1800 == 0 {
                s.squash_tap(huge_now);
            }
            if i % 3600 == 0 {
                if (i / 3600) % 2 == 0 {
                    s.play_animation(AnimationId::Walking, 1., true);
                } else {
                    s.play_animation(AnimationId::Idle, 1., true);
                }
            }
            s.update(view, huge_now + i as f32 / 60., 1. / 60.);
            for q in &s.rotations {
                assert!(q.is_finite());
            }
            for t in &s.translations {
                assert!(t.is_finite());
            }
            assert!(s.squash.is_finite());
            assert!(
                s.tracks.len() <= 4,
                "tracks must stay bounded: {:?}",
                s.tracks
            );
            assert!(s.tracks.iter().all(|t| (0.0..=1.0).contains(&t.weight)));
        }
        let head = s.model.joint("Head").unwrap();
        assert!(
            (s.rotations[head].length() - 1.).abs() < 0.001,
            "head must stay normalized"
        );
    }

    /// 压扁/摆臂的相位计时不得依赖宿主绝对时钟:
    /// 宿主时钟是设备开机以来的秒数,长 uptime 下 f32 精度极差。
    #[test]
    fn squash_and_swing_survive_huge_host_clock() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let huge = 86_400. * 365.; // 一年的秒数
        s.squash_tap(huge);
        s.click(huge);
        for i in 0..30 {
            s.update(view, huge + i as f32 / 60., 1. / 60.);
        }
        assert!(s.squash_phase.is_none(), "squash must finish");
        assert!(s.swing_elapsed.is_none(), "swing must finish");
        assert!(
            (s.squash - 1.).abs() < 1e-6,
            "squash must return to full height"
        );
    }

    /// 行走轨道时间按整周期回绕:跨过回绕点后姿态与连续累计同相。
    #[test]
    fn walking_time_wraps_in_skeleton() {
        let mut s = skeleton();
        s.play_animation(AnimationId::Walking, 1., true);
        if let Some(base) = s.tracks.iter_mut().find(|t| t.source == Source::Walking) {
            base.time = WALKING_PERIOD - 0.01;
        }
        let view = Vec2::new(450., 525.);
        for i in 0..30 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let track = s
            .tracks
            .iter()
            .find(|t| t.source == Source::Walking)
            .expect("walking track");
        assert!(
            track.time < WALKING_PERIOD,
            "walking time must stay wrapped"
        );
        let f = evaluate_walking(track.time);
        let expected = rotation(f.arms[0].x, f.arms[0].y, f.arms[0].z);
        let arm_l = s.model.joint("Left_Arm").unwrap();
        assert!(s.rotations[arm_l].abs_diff_eq(expected, 1e-4));
    }

    #[test]
    fn animation_switch_blends_from_current_pose() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        for i in 0..60 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let arm_l = s.model.joint("Left_Arm").unwrap();
        let before = s.rotations[arm_l];
        s.play_animation(AnimationId::Walking, 0.8, true);
        s.update(view, 1.01, 1. / 60.);
        // 切换瞬间不应直接跳到行走目标。
        let f = evaluate_walking(0.08);
        let walking = rotation(f.arms[0].x, f.arms[0].y, f.arms[0].z);
        assert!(before.abs_diff_eq(s.rotations[arm_l], 0.4));
        assert!(!s.rotations[arm_l].abs_diff_eq(walking, 0.0001));
        for i in 2..180 {
            s.update(view, 1. + i as f32 / 60., 1. / 60.);
        }
        // 交叉淡入淡出完成后只剩行走轨道,姿态与轨道时间严格一致。
        assert_eq!(s.tracks.len(), 1);
        assert_eq!(s.tracks[0].source, Source::Walking);
        let f = evaluate_walking(s.tracks[0].time);
        let walking = rotation(f.arms[0].x, f.arms[0].y, f.arms[0].z);
        assert!(s.rotations[arm_l].abs_diff_eq(walking, 0.0001));
    }

    #[test]
    fn squash_taps_restart_from_full_height() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        s.squash_tap(0.);
        for i in 0..6 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        let mid = s.squash;
        assert!(mid < 1.);
        // 连续点按：重启后脉冲回到满高。
        s.squash_tap(0.1);
        s.update(view, 0.11, 1. / 60.);
        assert!(s.squash > mid);
    }

    #[test]
    fn scheduler_fires_sub_after_interval_and_returns() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 1. / 60.;
        let mut now = 0.;
        // 8 秒后触发子动画（允许 ±0.1s）
        let mut fired_at = None;
        for i in 0..600 {
            s.update(view, now, dt);
            now += dt;
            if sub_active(&s) {
                fired_at = Some(i);
                break;
            }
        }
        let fired_at = fired_at.expect("sub must fire within 10s");
        assert!((fired_at as f32 * dt - clip::SUB_INTERVAL).abs() < 0.1);
        // 子动画 3 秒后淡回基础循环
        let mut returned = false;
        for _ in 0..220 {
            s.update(view, now, dt);
            now += dt;
            if !sub_active(&s) {
                returned = true;
                break;
            }
        }
        assert!(returned, "sub must return to base after ~3s");
        // 回归后一个完整间隔内不再触发
        for _ in 0..470 {
            s.update(view, now, dt);
            now += dt;
            assert!(!sub_active(&s));
        }
    }

    /// 子动画播完后必须淡回基础待机循环,而非落到静止姿态。
    /// 回归测试:基础轨道淡出后会被回收,子动画结束时须重建淡入。
    #[test]
    fn sub_finish_fades_back_to_base_idle() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 1. / 60.;
        let mut now = 0.;
        // 等首个子动画触发(固定种子下 8 秒)
        for _ in 0..(8 * 60) {
            s.update(view, now, dt);
            now += dt;
        }
        assert!(sub_active(&s), "sub must have fired");
        // 播完子动画(3 秒)并等淡出结束
        for _ in 0..(3 * 60 + 30) {
            s.update(view, now, dt);
            now += dt;
        }
        assert!(!sub_active(&s));
        assert!(
            s.tracks.iter().any(|t| t.is_base() && t.target == 1.),
            "base track must be revived: {:?}",
            s.tracks
        );
        // 再跑 1 秒:姿态必须是 idle 剪辑(躯干下沉),静止姿态的躯干平移 y 为 0
        for _ in 0..60 {
            s.update(view, now, dt);
            now += dt;
        }
        let body = s.model.joint("Body").unwrap();
        assert!(
            s.translations[body].y < -0.3,
            "must fade back to idle clip, body y = {}",
            s.translations[body].y
        );
    }

    #[test]
    fn scheduler_never_repeats_last_sub() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 0.05;
        let mut last = None;
        let mut fired = 0;
        let mut prev_active = false;
        let mut now = 0.;
        while fired < 12 {
            s.update(view, now, dt);
            now += dt;
            assert!(now < 600., "scheduler stalled");
            let active = active_sub(&s);
            if active.is_some() && !prev_active {
                fired += 1;
                if let Some(prev) = last {
                    assert_ne!(Some(prev), active, "sub must not repeat consecutively");
                }
                last = active;
            }
            prev_active = active.is_some();
        }
    }

    #[test]
    fn play_animation_restarts_idle_schedule() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 1. / 60.;
        let mut now = 0.;
        for _ in 0..500 {
            s.update(view, now, dt);
            now += dt;
        }
        s.play_animation(AnimationId::Idle, 1., true);
        // 重启后重新计时:一个完整间隔内不触发
        for i in 0..470 {
            s.update(view, now, dt);
            now += dt;
            assert!(!sub_active(&s), "fired early at frame {i}");
        }
    }

    #[test]
    fn base_idle_time_wraps_for_looping() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let duration = clip::clip_duration(s.model.clip("idle").unwrap());
        // 覆盖多轮循环与子动画穿插:基础采样时间必须始终落在循环长度内
        let mut now = 0.;
        for _ in 0..400 {
            s.update(view, now, 0.05);
            now += 0.05;
            assert!(
                (0.0..=duration).contains(&s.idle_clips.base_time),
                "base time {} must wrap within {duration}",
                s.idle_clips.base_time
            );
        }
    }

    #[test]
    fn scheduler_runs_indefinitely() {
        // 20 虚拟分钟:子动画持续触发(不因时间累计而停摆),
        // 基础循环相位持续回绕(不冻结在末帧)。
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        let dt = 1. / 60.;
        let mut sub_fires = 0;
        let mut phase_wraps = 0;
        let mut last_idle_time = 0f32;
        let mut prev_active = false;
        let mut now = 0.;
        for _ in 0..(60 * 60 * 20) {
            s.update(view, now, dt);
            now += dt;
            let active = sub_active(&s);
            if !active {
                if s.idle_clips.base_time < last_idle_time {
                    phase_wraps += 1;
                }
                last_idle_time = s.idle_clips.base_time;
            } else if !prev_active {
                sub_fires += 1;
            }
            prev_active = active;
        }
        assert!(sub_fires > 100, "subs must keep firing: {sub_fires}");
        assert!(phase_wraps > 300, "idle must keep looping: {phase_wraps}");
    }

    /// 腿挂静态髋部:躯干位移(含 idle 的 -0.64px 下沉)不得出现在脚上。
    #[test]
    fn body_motion_does_not_carry_legs() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        for i in 0..180 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        // idle 阶段躯干持续下沉,腿枢轴必须保持在髋部静止位置。
        let body = s.model.joint("Body").unwrap();
        let leg_l = s.model.joint("Left_Leg").unwrap();
        let leg_r = s.model.joint("Right_Leg").unwrap();
        assert!(
            s.translations[body].y < -0.3,
            "idle body must sink: {}",
            s.translations[body].y
        );
        assert_eq!(s.translations[leg_l], s.model.nodes[leg_l].rest_translation);
        assert_eq!(s.translations[leg_r], s.model.nodes[leg_r].rest_translation);
    }

    /// 停止动画后所有轨道淡出,输出收敛到静止姿态。
    #[test]
    fn stop_fades_to_rest_pose() {
        let mut s = skeleton();
        let view = Vec2::new(450., 525.);
        for i in 0..60 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        s.stop_animation();
        for i in 60..120 {
            s.update(view, i as f32 / 60., 1. / 60.);
        }
        assert!(s.tracks.is_empty(), "tracks must drain: {:?}", s.tracks);
        let body = s.model.joint("Body").unwrap();
        assert!(s.translations[body].abs_diff_eq(s.model.nodes[body].rest_translation, 0.001));
        let cape_e = clip::cape_euler(flip_quat(s.rotations[s.model.joint("Cape").unwrap()]));
        assert!((cape_e.x - std::f32::consts::PI * 0.06).abs() < 0.01);
    }

    /// 行走时头部俯仰摆动的幅度保持原版观感（配方未变的回归锚点）。
    #[test]
    fn walking_head_sway_matches_reference_range() {
        let mut s = skeleton();
        s.play_animation(AnimationId::Walking, 1., true);
        let view = Vec2::new(450., 525.);
        let head = s.model.joint("Head").unwrap();
        let mut max_x = 0f32;
        for i in 0..1200 {
            s.update(view, i as f32 / 60., 1. / 60.);
            let (x, _) = euler_xy(s.rotations[head]);
            max_x = max_x.max(x.abs());
        }
        // skinview3d 行走头部摆幅 ±0.1 弧度
        assert!((max_x - 0.1).abs() < 0.02, "head sway = {max_x}");
    }

    fn euler_xy(q: Quat) -> (f32, f32) {
        // 仅测试用的小角度欧拉抽取
        let m = glam::Mat3::from_quat(q);
        (m.z_axis.y.asin(), 0.)
    }
}
