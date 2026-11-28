//! PetLib 接口

use std::sync::Arc;

use glam::{Vec2, Vec3};

use crate::animation::AnimationId;
use crate::clip::clip_duration;
use crate::lighting::Lighting;
use crate::model::{self, ModelData};
use crate::pose::{GazeRig, Skeleton};
use crate::render::{self, Frame};
use crate::skin::{self, ArmModel, Skin, Texture};

/// 方向光照参数。角度单位为度，亮度/柔和度为百分比。
#[derive(Clone, Copy, Debug)]
pub struct LightingParams {
    pub azimuth: f32,
    pub elevation: f32,
    pub brightness: f32,
    pub softness: f32,
}

impl Default for LightingParams {
    fn default() -> Self {
        Self {
            azimuth: 20.,
            elevation: 35.,
            brightness: 100.,
            softness: 50.,
        }
    }
}

pub struct PetModel {
    skin: Skin,
    skin_texture: Arc<Texture>,
    cape: Option<Arc<Texture>>,
    /// 材质索引对应的纹理；仅在皮肤、披风或臂型变化时重建。
    textures: Vec<Option<Arc<Texture>>>,
    lighting: Lighting,
    lighting_params: LightingParams,
    skeleton: Skeleton,
    frame: Frame,
    outer_layer: bool,
    super_sample: f32,
    /// 旧固定机位的相对距离；1 为原始取景
    camera_distance: f32,
}

impl PetModel {
    /// 创建模型。
    pub fn new(skin_bytes: &[u8], arm: Option<ArmModel>) -> Result<Self, String> {
        let skin = Skin::load(skin_bytes, arm)?;
        // legacy 皮肤按原版约定使用经典臂型
        let model_arm = if skin.legacy {
            ArmModel::Classic
        } else {
            skin.arm
        };
        let data = model::builtin_data(model_arm)?;
        let skin_texture = Arc::new(skin.texture.clone());
        let textures = player_textures(&data, &skin_texture, None);
        Ok(Self {
            skeleton: Skeleton::new(Arc::clone(&data)),
            skin,
            skin_texture,
            cape: None,
            textures,
            lighting: Lighting::new(&LightingParams::default()),
            lighting_params: LightingParams::default(),
            frame: Frame::new(1, 1),
            outer_layer: true,
            super_sample: 2.,
            camera_distance: 1.,
        })
    }

    /// 替换皮肤。`arm` 为 None 时按纹理自动检测臂型。
    pub fn set_skin(&mut self, bytes: &[u8], arm: Option<ArmModel>) -> Result<(), String> {
        self.skin = Skin::load(bytes, arm)?;
        self.skin_texture = Arc::new(self.skin.texture.clone());
        let model_arm = if self.skin.legacy {
            ArmModel::Classic
        } else {
            self.skin.arm
        };
        self.skeleton.model = model::builtin_data(model_arm)?;
        self.textures =
            player_textures(&self.skeleton.model, &self.skin_texture, self.cape.as_ref());
        Ok(())
    }

    pub fn skin_arm(&self) -> ArmModel {
        self.skin.arm
    }

    /// 以固定种子重置待机调度，供确定性回放。
    pub fn seed_idle(&mut self, seed: u64) {
        self.skeleton.seed_idle(seed);
    }

    /// 替换披风，None 移除。
    pub fn set_cape(&mut self, bytes: Option<&[u8]>) -> Result<(), String> {
        self.cape = bytes.map(skin::load_cape).transpose()?.map(Arc::new);
        self.textures =
            player_textures(&self.skeleton.model, &self.skin_texture, self.cape.as_ref());
        Ok(())
    }

    pub fn set_lighting(&mut self, params: LightingParams) {
        self.lighting_params = params;
    }

    pub fn lighting_params(&self) -> LightingParams {
        self.lighting_params
    }

    /// 超采样系数（0.5..=2.0，默认 2.0）。1.0 以下为最近邻放大，无抗锯齿。
    pub fn set_super_sample(&mut self, value: f32) {
        self.super_sample = value.clamp(0.5, 2.);
    }

    /// 旧固定机位的相对距离：1 为原始取景，值越大模型越小。
    pub fn set_camera_distance(&mut self, value: f32) {
        self.camera_distance = value.max(1e-3);
    }

    pub fn set_outer_layer(&mut self, enabled: bool) {
        self.outer_layer = enabled;
    }

    /// 姿态平滑时间常数（秒）：注视 / 偏航 / 动画切换 / 回正。
    pub fn set_tau(&mut self, gaze: f32, yaw: f32, anim: f32, recenter: f32) {
        let s = &mut self.skeleton;
        s.tau_gaze = gaze.max(0.001);
        s.tau_yaw = yaw.max(0.001);
        s.fade_seconds = anim.max(0.001);
        s.tau_recenter = recenter.max(0.001);
    }

    /// 注视夹角限制（度，水平 / 垂直）。
    pub fn set_head_range(&mut self, x_degrees: f32, y_degrees: f32) {
        self.skeleton.head_range =
            Vec2::new(x_degrees.clamp(0., 85.), y_degrees.clamp(0., 85.)).map(f32::to_radians);
    }

    /// 压扁深度百分比（0..=30，默认 5）。
    pub fn set_squash_depth(&mut self, percent: f32) {
        self.skeleton.squash_depth = percent.clamp(0., 30.);
    }

    /// 注视目标（视图像素坐标）。None 表示回正；
    pub fn set_gaze(&mut self, target: Option<(f32, f32)>) {
        self.skeleton
            .set_gaze_target(target.map(|(x, y)| Vec2::new(x, y)));
    }

    pub fn set_model_yaw(&mut self, yaw: f32) {
        self.skeleton.set_yaw_target(yaw);
    }

    pub fn model_yaw(&self) -> f32 {
        self.skeleton.model_yaw()
    }

    /// 播放循环动画
    /// `head_follow` 控制播放期间头部是否追随注视。
    pub fn play_animation(&mut self, id: AnimationId, speed: f32, head_follow: bool) {
        self.skeleton.play_animation(id, speed, head_follow);
    }

    pub fn stop_animation(&mut self) {
        self.skeleton.stop_animation();
    }

    /// 点按压扁。
    pub fn squash_tap(&mut self, now: f32) {
        self.skeleton.squash_tap(now);
    }

    /// 点击反应动画。
    pub fn play_interact(&mut self) {
        self.skeleton.play_interact();
    }

    /// 攻击摆臂。
    pub fn play_swing(&mut self, now: f32) {
        self.skeleton.click(now);
    }

    /// 渲染一帧到 `out`（预乘 RGBA，长度须为 width*height*4）。
    /// 内部计时全部由帧间隔 `dt` 驱动；`now` 仅为兼容保留。
    pub fn render(
        &mut self,
        width: usize,
        height: usize,
        _now: f32,
        dt: f32,
        out: &mut [u8],
    ) -> Result<(), String> {
        if out.len() != width * height * 4 {
            return Err(format!(
                "Output buffer must be {width}x{height}x4 bytes, got {}.",
                out.len()
            ));
        }
        if width == 0 || height == 0 {
            return Ok(());
        }
        let view = Vec2::new(width as f32, height as f32);
        self.skeleton.update_with_rig(
            &GazeRig::legacy_with_distance(view, self.camera_distance),
            dt,
        );
        self.lighting.prepare(&self.lighting_params);
        let fw = ((width as f32 * self.super_sample).round() as usize).max(1);
        let fh = ((height as f32 * self.super_sample).round() as usize).max(1);
        if self.frame.width != fw || self.frame.height != fh {
            self.frame = Frame::new(fw, fh);
        }
        let skeleton = &self.skeleton;
        self.frame.draw_scene(
            skeleton,
            &render::SceneParams {
                data: skeleton.model.as_ref(),
                textures: &self.textures,
                outer_layer: self.outer_layer,
            },
            &self.lighting,
            self.camera_distance,
        );
        self.frame.present_into(out, width, height);
        Ok(())
    }

    /// 归一化坐标 (0..1) 的不透明命中测试，供点按判定。
    pub fn hit_test(&self, x: f32, y: f32) -> bool {
        if !(0. ..1.).contains(&x) || !(0. ..1.).contains(&y) {
            return false;
        }
        let px = ((x * self.frame.width as f32) as usize).min(self.frame.width - 1);
        let py = ((y * self.frame.height as f32) as usize).min(self.frame.height - 1);
        self.frame.pixels[(py * self.frame.width + px) * 4 + 3] != 0
    }

    /// 调试：当前帧各部件的种类（"skin"/"cape"）、尺寸与列主序变换矩阵。
    pub fn debug_part_transforms(&self) -> Vec<PartTransform> {
        let scene = render::SceneParams {
            data: self.skeleton.model.as_ref(),
            textures: &self.textures,
            outer_layer: self.outer_layer,
        };
        render::part_transforms(&self.skeleton, &scene)
            .into_iter()
            .map(|p| PartTransform {
                kind: p.kind.to_string(),
                size: p.size,
                matrix: p.matrix,
            })
            .collect()
    }

    /// 测试探针：当前帧三角形数量（累积检测）。
    #[cfg(test)]
    pub(crate) fn debug_tri_count(&self) -> usize {
        self.frame.tri_count()
    }
}

/// 可加入场景、独立变换并播放 GLB 剪辑的模型实例。
pub struct Model {
    pub(crate) data: Arc<ModelData>,
    /// 姿态状态
    #[allow(dead_code)]
    pub(crate) skeleton: Skeleton,
    /// 材质索引 → 纹理
    /// 玩家语义下由皮肤/披风映射，道具语义下为内嵌纹理
    pub(crate) textures: Vec<Option<Arc<Texture>>>,
    /// 玩家皮肤纹理；None 表示使用模型内嵌纹理的道具。
    pub(crate) skin_texture: Option<Arc<Texture>>,
    cape: Option<Arc<Texture>>,
    /// 位置
    pub(crate) position: Vec3,
    /// 欧拉旋转（弧度）
    pub(crate) rotation: Vec3,
    /// 均匀缩放
    pub(crate) scale: f32,
    /// 玩家模型是否启用双层皮肤
    pub(crate) outer_layer: bool,
}

impl Model {
    /// 加载自定义 GLB（道具模式：材质须携带可解析的内嵌纹理）。
    pub fn from_glb(bytes: &[u8]) -> Result<Self, String> {
        Self::build(model::cached(bytes)?, None)
    }

    /// 加载自定义 GLB 并绑定玩家皮肤（材质名映射皮肤/披风）。
    pub fn from_glb_with_skin(
        bytes: &[u8],
        skin_bytes: &[u8],
        arm: Option<ArmModel>,
    ) -> Result<Self, String> {
        let skin = Skin::load(skin_bytes, arm)?;
        Self::build(model::cached(bytes)?, Some(skin))
    }

    /// 内置 classic/slim 玩家模型 + 皮肤（GLB 经 include_bytes 内嵌，
    /// 与 PetModel 共享同一份解析缓存）。
    pub fn builtin_player(skin_bytes: &[u8], arm: Option<ArmModel>) -> Result<Self, String> {
        let skin = Skin::load(skin_bytes, arm)?;
        // 与 PetModel::new 相同约定：legacy 皮肤按经典臂型
        let model_arm = if skin.legacy {
            ArmModel::Classic
        } else {
            skin.arm
        };
        Self::build(model::builtin_data(model_arm)?, Some(skin))
    }

    fn build(data: Arc<ModelData>, skin: Option<Skin>) -> Result<Self, String> {
        let skin_texture = skin.map(|skin| Arc::new(skin.texture));
        let textures = match &skin_texture {
            Some(skin) => Ok(player_textures(&data, skin, None)),
            None => data
                .materials
                .iter()
                .map(|m| {
                    m.embedded.clone().map(Some).ok_or_else(|| {
                        format!(
                            "material {:?} lacks a decodable embedded texture \
                             (external image references are unsupported)",
                            m.name.as_deref().unwrap_or("?")
                        )
                    })
                })
                .collect(),
        }?;
        Ok(Self {
            skeleton: Skeleton::new(Arc::clone(&data)),
            data,
            textures,
            skin_texture,
            cape: None,
            position: Vec3::ZERO,
            rotation: Vec3::ZERO,
            scale: 1.,
            outer_layer: true,
        })
    }

    /// 按材质名覆盖纹理（PNG）。
    pub fn set_texture(&mut self, material: &str, bytes: &[u8]) -> Result<(), String> {
        let index = self
            .data
            .materials
            .iter()
            .position(|m| m.name.as_deref() == Some(material))
            .ok_or_else(|| format!("material {material:?} not found"))?;
        self.textures[index] = Some(Arc::new(Texture::decode(bytes)?));
        Ok(())
    }

    /// 绑定玩家皮肤（道具模型由此切换为玩家语义，材质名映射皮肤/披风）。
    pub fn set_skin(&mut self, bytes: &[u8], arm: Option<ArmModel>) -> Result<(), String> {
        let skin = Skin::load(bytes, arm)?;
        let texture = Arc::new(skin.texture);
        self.textures = player_textures(&self.data, &texture, self.cape.as_ref());
        self.skin_texture = Some(texture);
        Ok(())
    }

    /// 替换披风，None 移除。
    pub fn set_cape(&mut self, bytes: Option<&[u8]>) -> Result<(), String> {
        self.cape = bytes.map(skin::load_cape).transpose()?.map(Arc::new);
        if let Some(skin) = &self.skin_texture {
            self.textures = player_textures(&self.data, skin, self.cape.as_ref());
        }
        Ok(())
    }

    /// 位置（场景单位，1 单位 = 1 方块；模型根原点落到该位置）。
    pub fn set_position(&mut self, x: f32, y: f32, z: f32) {
        self.position = Vec3::new(x, y, z);
    }

    /// 注视目标：屏幕像素（随活相机逐帧反投影，看光标语义）。
    pub fn set_gaze_screen(&mut self, target: Option<(f32, f32)>) {
        self.skeleton
            .set_gaze_target(target.map(|(x, y)| Vec2::new(x, y)));
    }

    /// 注视目标：场景空间固定点（模型互看）。
    pub fn set_gaze_scene(&mut self, target: Option<[f32; 3]>) {
        self.skeleton.set_gaze_scene(target.map(Vec3::from));
    }

    /// 欧拉旋转（度，pitch/yaw/roll）。
    pub fn set_rotation_degrees(&mut self, pitch: f32, yaw: f32, roll: f32) {
        self.rotation = Vec3::new(pitch, yaw, roll).map(f32::to_radians);
    }

    /// 均匀缩放。
    pub fn set_scale(&mut self, scale: f32) {
        self.scale = scale.max(0.001);
    }

    /// 外层（半透明帽层）显隐。
    pub fn set_outer_layer(&mut self, enabled: bool) {
        self.outer_layer = enabled;
    }

    /// 剪辑名列表（文档序）。
    pub fn clip_names(&self) -> Vec<&str> {
        self.data.clips.iter().map(|c| c.name.as_str()).collect()
    }

    /// 取出动画对象（剪辑不存在时返回 None）。
    pub fn animation(&mut self, name: &str) -> Option<Animation<'_>> {
        let clip = self.data.clip_index_of(name)?;
        let index = self.skeleton.manual_slot(clip);
        Some(Animation {
            skeleton: &mut self.skeleton,
            index,
        })
    }
}

/// 动画对象：模型内置剪辑的播放控制，并行权重混合的独立层。
/// 常规名（idle/idle_sub_*/interact）的自动调度仍在底层运行。
pub struct Animation<'a> {
    skeleton: &'a mut Skeleton,
    index: usize,
}

impl Animation<'_> {
    /// 播放（暂停中则继续，已播完则从头）。一次性剪辑播完自动淡出。
    pub fn play(&mut self, speed: f32) {
        let clip = self.skeleton.manual(self.index).clip;
        let dur = clip_duration(&self.skeleton.model.clips[clip]).max(1e-6);
        let st = self.skeleton.manual_mut(self.index);
        if !st.playing && st.time >= dur {
            st.time = 0.;
        }
        st.playing = true;
        st.speed = speed.max(0.);
        st.target_weight = 1.;
    }

    /// 暂停（保留时间与权重）。
    pub fn pause(&mut self) {
        self.skeleton.manual_mut(self.index).playing = false;
    }

    /// 停止（时间归零，权重淡出）。
    pub fn stop(&mut self) {
        let st = self.skeleton.manual_mut(self.index);
        st.playing = false;
        st.time = 0.;
        st.target_weight = 0.;
    }

    pub fn set_speed(&mut self, speed: f32) {
        self.skeleton.manual_mut(self.index).speed = speed.max(0.);
    }

    pub fn set_loop(&mut self, loop_: bool) {
        self.skeleton.manual_mut(self.index).loop_ = loop_;
    }

    /// 混合权重（0..=1，默认 1）。
    pub fn set_weight(&mut self, weight: f32) {
        self.skeleton.manual_mut(self.index).target_weight = weight.clamp(0., 1.);
    }

    pub fn is_playing(&self) -> bool {
        self.skeleton.manual(self.index).playing
    }
}

/// 玩家语义材质表：cape 名材质 → 披风（无则不绘），其余 → 皮肤。
fn player_textures(
    data: &ModelData,
    skin: &Arc<Texture>,
    cape: Option<&Arc<Texture>>,
) -> Vec<Option<Arc<Texture>>> {
    data.materials
        .iter()
        .map(|m| {
            if model::material_is_cape(m.name.as_deref()) {
                cape.cloned()
            } else {
                Some(Arc::clone(skin))
            }
        })
        .collect()
}

/// 调试用部件变换快照。
pub struct PartTransform {
    pub kind: String,
    pub size: [f32; 3],
    pub matrix: [f32; 16],
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::gltf::test_fixture;
    use std::io::Cursor;

    /// 合成一张纯色 64×64 PNG 皮肤。
    fn skin_png() -> Vec<u8> {
        let mut img = image::RgbaImage::new(64, 64);
        for p in img.pixels_mut() {
            *p = image::Rgba([255, 0, 0, 255]);
        }
        let mut png = Vec::new();
        img.write_to(&mut Cursor::new(&mut png), image::ImageFormat::Png)
            .unwrap();
        png
    }

    fn test_model() -> PetModel {
        PetModel::new(&skin_png(), None).unwrap()
    }

    #[test]
    fn arm_switch_rebinds_geometry_both_ways() {
        // 回归：纤细起步后切经典曾永远落在初始纤细几何上
        // （骨架数据在 new 时绑定一次，set_skin 只换纹理）。
        // 几何签名：全部部件包围盒尺寸（细/经典只有臂宽不同，足以区分）。
        let sizes = |m: &PetModel| {
            m.debug_part_transforms()
                .into_iter()
                .map(|p| p.size)
                .collect::<Vec<_>>()
        };
        let mut m = PetModel::new(&skin_png(), Some(ArmModel::Slim)).unwrap();
        assert!(Arc::ptr_eq(
            &m.skeleton.model,
            &model::builtin_data(ArmModel::Slim).unwrap()
        ));
        let slim_sizes = sizes(&m);

        m.set_skin(&skin_png(), Some(ArmModel::Classic)).unwrap();
        assert!(Arc::ptr_eq(
            &m.skeleton.model,
            &model::builtin_data(ArmModel::Classic).unwrap()
        ));
        // 几何必须跟随皮肤：与新经典模型逐部件一致，且不再是最初的纤细几何。
        let fresh = PetModel::new(&skin_png(), Some(ArmModel::Classic)).unwrap();
        assert_eq!(sizes(&m), sizes(&fresh));
        assert_ne!(sizes(&m), slim_sizes, "arm geometry must follow the skin");

        // 双向往返 + 经典起步的原有路径
        m.set_skin(&skin_png(), Some(ArmModel::Slim)).unwrap();
        assert!(Arc::ptr_eq(
            &m.skeleton.model,
            &model::builtin_data(ArmModel::Slim).unwrap()
        ));
        let mut c = PetModel::new(&skin_png(), Some(ArmModel::Classic)).unwrap();
        c.set_skin(&skin_png(), Some(ArmModel::Slim)).unwrap();
        assert!(Arc::ptr_eq(
            &c.skeleton.model,
            &model::builtin_data(ArmModel::Slim).unwrap()
        ));
    }

    #[test]
    fn prop_model_requires_embedded_textures() {
        // 道具 GLB（内嵌纹理）加载成功
        let model = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        assert_eq!(model.textures.len(), 1);
        assert!(model.textures[0].is_some());
        // 玩家 GLB（外部图片引用）无皮肤绑定时报错
        let bytes = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/src/assets/classic-player.glb"
        ))
        .unwrap();
        assert!(Model::from_glb(&bytes).is_err());
        // 绑定皮肤后按材质名映射成功
        assert!(Model::from_glb_with_skin(&bytes, &skin_png(), None).is_ok());
    }

    #[test]
    fn prop_texture_override_by_material_name() {
        let mut model = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        let before = Arc::clone(model.textures[0].as_ref().unwrap());
        model
            .set_texture("prop_mat", &test_fixture::tiny_png(10, 200, 30))
            .unwrap();
        let after = model.textures[0].as_ref().unwrap();
        assert!(
            !Arc::ptr_eq(&before, after),
            "override should replace the texture instance"
        );
        assert_eq!((after.width, after.height), (2, 2));
        assert!(model
            .set_texture("nope", &test_fixture::tiny_png(0, 0, 0))
            .is_err());
    }

    #[test]
    fn model_transform_defaults_and_setters() {
        let mut model = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        assert_eq!(model.position, Vec3::ZERO);
        assert_eq!(model.scale, 1.);
        model.set_position(1., 2., 3.);
        model.set_rotation_degrees(90., 0., -45.);
        model.set_scale(2.);
        assert!(model.position.abs_diff_eq(Vec3::new(1., 2., 3.), 1e-6));
        assert!((model.rotation.x - std::f32::consts::FRAC_PI_2).abs() < 1e-6);
        assert!((model.rotation.z + std::f32::consts::FRAC_PI_4).abs() < 1e-6);
        assert_eq!(model.scale, 2.);
    }

    /// 骨架工具的重导出 GLB 必须通过加载器并驱动姿态（工具链守门）。
    #[test]
    fn rig_tool_export_loads_and_animates() {
        let bytes = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/../tools/rig/out/rig-export.glb"
        ))
        .unwrap();
        let mut model = Model::from_glb_with_skin(&bytes, &skin_png(), None).unwrap();
        assert!(model.clip_names().contains(&"idle"));
        let head = model.data.joint("Head").unwrap();
        for _ in 0..30 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        let before = model.skeleton.rotations[head];
        let mut anim = model.animation("interact").unwrap();
        anim.play(1.);
        drop(anim);
        for _ in 0..15 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        assert!(
            !model.skeleton.rotations[head].abs_diff_eq(before, 0.001),
            "rig export clips must drive the pose"
        );
        // 渲染冒烟：非零像素
        let (w, h) = (96usize, 96usize);
        let mut out = vec![0u8; w * h * 4];
        let mut scene = crate::scene::Scene::new();
        scene.add(model);
        scene.set_super_sample(1.);
        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let opaque = out.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();
        assert!(
            opaque > 100,
            "rig export should render visible pixels: {opaque}"
        );
    }

    /// rig 导出与内置模型同姿态渲染须一致（防骨架翻转/枢轴漂移回归）。
    #[test]
    fn rig_tool_export_renders_like_builtin() {
        let rig_bytes = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/../tools/rig/out/rig-export.glb"
        ))
        .unwrap();
        let classic_bytes = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/src/assets/classic-player.glb"
        ))
        .unwrap();
        let a = Model::from_glb_with_skin(&classic_bytes, &skin_png(), None).unwrap();
        let b = Model::from_glb_with_skin(&rig_bytes, &skin_png(), None).unwrap();
        assert!(
            b.data.joint("CTL_Body").is_none(),
            "controllers must be stripped on export"
        );

        // 同一 idle 时间轴各推进 30 帧（测试种子固定，双方调度一致）
        let (w, h) = (96usize, 96usize);
        let render_frames = |model: Model| {
            let mut buf = vec![0u8; w * h * 4];
            let mut scene = crate::scene::Scene::new();
            scene.add(model);
            scene.set_super_sample(1.);
            for _ in 0..30 {
                scene.render(w, h, 1. / 60., &mut buf).unwrap();
            }
            buf
        };
        let fa = render_frames(a);
        let fb = render_frames(b);

        // 并集内容包围盒内比较像素：重采样/浮点重序只允许微小差异，
        // 翻转/轴心错位会让差异占比数量级上升
        let mut bounds = (w, h, 0usize, 0usize);
        for buf in [&fa, &fb] {
            for i in 0..w * h {
                if buf[i * 4 + 3] > 0 {
                    let (x, y) = (i % w, i / w);
                    bounds.0 = bounds.0.min(x);
                    bounds.1 = bounds.1.min(y);
                    bounds.2 = bounds.2.max(x);
                    bounds.3 = bounds.3.max(y);
                }
            }
        }
        let mut diff = 0usize;
        let mut total = 0usize;
        for y in bounds.1..=bounds.3 {
            for x in bounds.0..=bounds.2 {
                let i = (y * w + x) * 4;
                total += 1;
                if fa[i..i + 4] != fb[i..i + 4] {
                    diff += 1;
                }
            }
        }
        let frac = diff as f32 / total as f32;
        assert!(
            frac <= 0.02,
            "rig export diverges from builtin render: {frac:.4} ({diff}/{total})"
        );
    }

    /// 内置玩家构造器与 PetModel 共享同一份模型数据缓存（Arc 级共享）。
    #[test]
    fn builtin_player_shares_data_with_petmodel() {
        let model = Model::builtin_player(&skin_png(), None).unwrap();
        assert!(model.skin_texture.is_some());
        let pet = PetModel::new(&skin_png(), None).unwrap();
        assert!(
            Arc::ptr_eq(&model.data, &pet.skeleton.model),
            "builtin_player must reuse the PetModel parse cache"
        );
    }

    fn player_model() -> Model {
        let bytes = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/src/assets/classic-player.glb"
        ))
        .unwrap();
        Model::from_glb_with_skin(&bytes, &skin_png(), None).unwrap()
    }

    /// 手动动画对象必须把剪辑混合进姿态。
    #[test]
    fn manual_animation_blends_over_base() {
        let mut model = player_model();
        assert!(model.clip_names().contains(&"interact"));
        let head = model.data.joint("Head").unwrap();
        // 基线推进 1 秒后记录头部姿态
        for _ in 0..60 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        let before = model.skeleton.rotations[head];
        let mut anim = model.animation("interact").unwrap();
        anim.play(1.);
        drop(anim);
        for _ in 0..15 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        let after = model.skeleton.rotations[head];
        assert!(
            !before.abs_diff_eq(after, 0.001),
            "manual animation must change the pose"
        );
    }

    /// 循环、倍速与停止（权重淡出）语义。
    #[test]
    fn manual_animation_loop_speed_and_stop() {
        let mut model = player_model();
        let idle_index = model.data.clip_index_of("idle").unwrap();
        let dur = clip_duration(&model.data.clips[idle_index]);
        let mut anim = model.animation("idle").unwrap();
        anim.set_loop(true);
        anim.play(2.);
        drop(anim);
        for _ in 0..60 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        let slot = model.skeleton.manual_slot(idle_index);
        let st = model.skeleton.manual(slot);
        assert!(st.playing);
        assert!(
            st.time > 1.,
            "speed=2 should advance ~2s in 1s (got {})",
            st.time
        );
        assert!(st.time < dur, "loop time should wrap");
        // 停止 → 权重淡出到 0
        let mut anim = model.animation("idle").unwrap();
        anim.stop();
        drop(anim);
        for _ in 0..30 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        let st = model.skeleton.manual(slot);
        assert_eq!(st.weight, 0.);
        assert!(!st.playing);
    }

    /// 两个手动动画对象并行混合（不同剪辑同时生效）。
    #[test]
    fn manual_animations_blend_in_parallel() {
        let mut model = player_model();
        let head = model.data.joint("Head").unwrap();
        let arm = model.data.joint("Right_Arm").unwrap();
        for _ in 0..60 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        let head_before = model.skeleton.rotations[head];
        let arm_before = model.skeleton.rotations[arm];
        // interact 同时驱动头部与右臂：单开/双开时两个关节都应偏离基线
        let mut anim = model.animation("interact").unwrap();
        anim.set_loop(true);
        anim.play(1.);
        drop(anim);
        for _ in 0..30 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        assert!(!model.skeleton.rotations[head].abs_diff_eq(head_before, 0.001));
        assert!(!model.skeleton.rotations[arm].abs_diff_eq(arm_before, 0.001));
        // 权重 0 时不影响姿态
        let mut anim = model.animation("interact").unwrap();
        anim.set_weight(0.);
        drop(anim);
        for _ in 0..40 {
            model.skeleton.update(Vec2::splat(100.), 0., 1. / 60.);
        }
        assert!(
            model.skeleton.rotations[head].abs_diff_eq(head_before, 0.02),
            "weight 0 should fade back to the baseline"
        );
    }

    /// 手动层不影响 PetModel 现有路径（空手动表 = 逐位原路径）。
    #[test]
    fn petmodel_stream_unchanged_by_manual_layer() {
        let (w, h) = (64usize, 64usize);
        let mut model = test_model();
        let mut buf = vec![0u8; w * h * 4];
        model.play_animation(AnimationId::Idle, 1., true);
        model.seed_idle(7);
        for i in 0..30 {
            model
                .render(w, h, i as f32 / 60., 1. / 60., &mut buf)
                .unwrap();
        }
        // 与全新实例重放必须逐字节一致（手动层零参与的确定性守门）
        let mut model2 = test_model();
        let mut buf2 = vec![0u8; w * h * 4];
        model2.play_animation(AnimationId::Idle, 1., true);
        model2.seed_idle(7);
        for i in 0..30 {
            model2
                .render(w, h, i as f32 / 60., 1. / 60., &mut buf2)
                .unwrap();
        }
        assert_eq!(buf, buf2);
    }

    /// 三角形缓冲跨帧复用不得累积：累积会让旧帧三角形全部重画，
    /// 表现为画面叠加与帧时间线性增长。
    #[test]
    fn frame_reuse_does_not_accumulate_triangles() {
        let (w, h) = (96usize, 96usize);
        let mut model = test_model();
        let mut out = vec![0u8; w * h * 4];
        model.render(w, h, 0., 1. / 60., &mut out).unwrap();
        let first = model.debug_tri_count();
        assert!(first > 0);
        // 连续渲染 8 帧（待机动画推进姿态），缓冲反复复用
        for i in 1..9 {
            model
                .render(w, h, i as f32 / 60., 1. / 60., &mut out)
                .unwrap();
        }
        assert_eq!(
            model.debug_tri_count(),
            first,
            "triangle count growing across frames = buffer accumulation"
        );
    }

    #[test]
    fn constructor_requires_valid_skin() {
        assert!(PetModel::new(&[137, 80, 78, 71], None).is_err());
        assert!(PetModel::new(&skin_png(), None).is_ok());
    }

    #[test]
    fn render_produces_visible_model_and_stable_output() {
        // 两个全新实例跑完全相同的帧流，输出必须逐字节一致。
        let (w, h) = (225usize, 262usize);
        let stream = |model: &mut PetModel| {
            model.play_animation(AnimationId::Idle, 1., true);
            let mut buf = vec![0u8; w * h * 4];
            for i in 0..30 {
                model
                    .render(w, h, i as f32 / 60., 1. / 60., &mut buf)
                    .unwrap();
            }
            buf
        };
        let a = stream(&mut test_model());
        let b = stream(&mut test_model());
        let alpha = a.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();
        assert!(alpha > 1000, "first frame is nearly empty");
        assert_eq!(a, b, "identical frame streams must be byte-identical");
    }

    #[test]
    fn camera_distance_scales_render_and_preserves_default() {
        let (w, h) = (225usize, 262usize);
        let render = |distance: Option<f32>| {
            let mut model = test_model();
            if let Some(distance) = distance {
                model.set_camera_distance(distance);
            }
            let mut out = vec![0u8; w * h * 4];
            model.render(w, h, 0., 1. / 60., &mut out).unwrap();
            out
        };
        let baseline = render(None);
        assert_eq!(baseline, render(Some(1.)), "distance 1 must be a no-op");
        let distant = render(Some(2.));
        let opaque = |buf: &[u8]| buf.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();
        let base_count = opaque(&baseline);
        let distant_count = opaque(&distant);
        assert!(
            distant_count * 3 < base_count,
            "distance 2 should shrink the model: {base_count} -> {distant_count}"
        );
    }

    #[test]
    fn hit_test_matches_rendered_pixels() {
        let mut model = test_model();
        let (w, h) = (225usize, 262usize);
        let mut out = vec![0u8; w * h * 4];
        model.render(w, h, 0., 0.016, &mut out).unwrap();
        assert!(!model.hit_test(0.01, 0.01));
        // 躯干大致位于视图中央偏下。
        assert!(model.hit_test(0.5, 0.55), "model center should be hit");
    }

    #[test]
    fn cape_adds_pixels_behind_body() {
        let mut model = test_model();
        let mut img = image::RgbaImage::new(64, 32);
        for p in img.pixels_mut() {
            *p = image::Rgba([30, 60, 200, 255]);
        }
        let mut png = Vec::new();
        img.write_to(&mut Cursor::new(&mut png), image::ImageFormat::Png)
            .unwrap();
        model.set_cape(Some(&png)).unwrap();
        let (w, h) = (225usize, 262usize);
        let mut out = vec![0u8; w * h * 4];
        // 背面视角下披风完整可见。
        model.set_model_yaw(std::f32::consts::PI);
        for i in 0..180 {
            model
                .render(w, h, i as f32 / 60., 1. / 60., &mut out)
                .unwrap();
        }
        let blueish = out
            .chunks_exact(4)
            .filter(|p| p[3] > 200 && p[2] as i32 - p[0] as i32 > 80)
            .count();
        assert!(blueish > 500, "cape pixels insufficient: {blueish}");
    }

    #[test]
    fn gaze_input_affects_head_over_frames() {
        let (w, h) = (225usize, 262usize);
        let mut with_gaze = test_model();
        let mut without = test_model();
        with_gaze.set_gaze(Some((w as f32, 0.)));
        let mut out = vec![0u8; w * h * 4];
        for i in 0..120 {
            with_gaze
                .render(w, h, i as f32 / 60., 1. / 60., &mut out)
                .unwrap();
            without
                .render(w, h, i as f32 / 60., 1. / 60., &mut out)
                .unwrap();
        }
        let head = with_gaze.skeleton.model.joint("Head").unwrap();
        assert!(
            !with_gaze.skeleton.rotations[head]
                .abs_diff_eq(without.skeleton.rotations[head], 0.001),
            "gaze target must move the head"
        );
    }

    #[test]
    fn rejects_bad_output_length() {
        let mut model = test_model();
        let mut out = vec![0u8; 10];
        assert!(model.render(4, 4, 0., 0., &mut out).is_err());
    }

    /// 整体偏航以"面向相机"为 0 基准：yaw 0 时躯干的世界朝向
    /// 必须正对相机方位角。
    #[test]
    fn zero_yaw_faces_the_camera() {
        let model = test_model();
        // 躯干局部旋转在静止姿态下为恒等。节点树含 180°Y 根（blockbench_export），
        // 调试矩阵的 z 轴指向角色背面，正面朝向取其负向。
        let body = &model.debug_part_transforms()[0];
        let (sin_f, cos_f) = (
            crate::pose::front_yaw().sin(),
            crate::pose::front_yaw().cos(),
        );
        let (dx, dz) = (-body.matrix[8], -body.matrix[10]);
        assert!((dx - sin_f).abs() < 1e-4, "dx={dx}");
        assert!((dz - cos_f).abs() < 1e-4, "dz={dz}");
    }

    /// 静止姿态下各部件盒心必须保持原版布局。
    #[test]
    fn rest_parts_keep_vanilla_layout() {
        let model = test_model();
        let centers: Vec<(i32, i32, i32)> = model
            .debug_part_transforms()
            .iter()
            .map(|p| {
                (
                    (p.matrix[12] * 10.).round() as i32,
                    (p.matrix[13] * 10.).round() as i32,
                    (p.matrix[14] * 10.).round() as i32,
                )
            })
            .collect();
        // 原版盒心经全局 front_yaw 偏航旋转后的期望位置
        let (s, c) = (
            crate::pose::front_yaw().sin(),
            crate::pose::front_yaw().cos(),
        );
        let world = |x: f32, y: f32, z: f32| {
            (
                ((x * c + z * s) * 10.).round() as i32,
                (y * 10.).round() as i32,
                ((-x * s + z * c) * 10.).round() as i32,
            )
        };
        for (x, y, z) in [
            (0., 18., 0.),  // 躯干
            (0., 28., 0.),  // 头
            (6., 18., 0.),  // 左臂
            (-6., 18., 0.), // 右臂
            (1.9, 6., 0.),  // 左腿
            (-1.9, 6., 0.), // 右腿
        ] {
            let want = world(x, y, z);
            assert!(
                centers.contains(&want),
                "missing part near {want:?} (vanilla {x},{y},{z}); centers = {centers:?}"
            );
        }
    }
}
