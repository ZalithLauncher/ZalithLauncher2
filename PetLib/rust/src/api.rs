//! PetLib 接口

use glam::Vec2;

use crate::animation::AnimationId;
use crate::lighting::Lighting;
use crate::pose::Skeleton;
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
    cape: Option<Texture>,
    lighting: Lighting,
    lighting_params: LightingParams,
    skeleton: Skeleton,
    frame: Frame,
    outer_layer: bool,
    super_sample: f32,
}

impl PetModel {
    /// 创建模型。
    pub fn new(skin_bytes: &[u8], arm: Option<ArmModel>) -> Result<Self, String> {
        Ok(Self {
            skin: Skin::load(skin_bytes, arm)?,
            cape: None,
            lighting: Lighting::new(&LightingParams::default()),
            lighting_params: LightingParams::default(),
            skeleton: Skeleton::new(),
            frame: Frame::new(1, 1),
            outer_layer: true,
            super_sample: 2.,
        })
    }

    /// 替换皮肤。`arm` 为 None 时按纹理自动检测臂型。
    pub fn set_skin(&mut self, bytes: &[u8], arm: Option<ArmModel>) -> Result<(), String> {
        self.skin = Skin::load(bytes, arm)?;
        Ok(())
    }

    pub fn skin_arm(&self) -> ArmModel {
        self.skin.arm
    }

    /// 替换披风，None 移除。
    pub fn set_cape(&mut self, bytes: Option<&[u8]>) -> Result<(), String> {
        self.cape = bytes.map(skin::load_cape).transpose()?;
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

    pub fn set_outer_layer(&mut self, enabled: bool) {
        self.outer_layer = enabled;
    }

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
        now: f32,
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
        self.skeleton
            .update(Vec2::new(width as f32, height as f32), now, dt);
        self.lighting.prepare(&self.lighting_params);
        let fw = ((width as f32 * self.super_sample).round() as usize).max(1);
        let fh = ((height as f32 * self.super_sample).round() as usize).max(1);
        if self.frame.width != fw || self.frame.height != fh {
            self.frame = Frame::new(fw, fh);
        }
        self.frame.draw_scene(
            &self.skeleton,
            &render::SceneParams {
                skin: &self.skin,
                cape: self.cape.as_ref(),
                outer_layer: self.outer_layer,
            },
            &self.lighting,
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
        let parts = render::parts(
            &self.skeleton,
            &render::SceneParams {
                skin: &self.skin,
                cape: self.cape.as_ref(),
                outer_layer: self.outer_layer,
            },
        );
        parts
            .iter()
            .map(|p| PartTransform {
                kind: match p.tex {
                    render::Tex::Skin => "skin",
                    render::Tex::Cape => "cape",
                }
                .to_string(),
                size: p.size.to_array(),
                matrix: p.transform.to_cols_array(),
            })
            .collect()
    }
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
        assert!(
            !with_gaze
                .skeleton
                .head
                .abs_diff_eq(without.skeleton.head, 0.001),
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
        // 躯干局部旋转在静止姿态下为恒等，其世界 Z 轴即身体朝向。
        let body = &model.debug_part_transforms()[0];
        let (sin_f, cos_f) = (crate::pose::front_yaw().sin(), crate::pose::front_yaw().cos());
        let (dx, dz) = (body.matrix[8], body.matrix[10]);
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
        let (s, c) = (crate::pose::front_yaw().sin(), crate::pose::front_yaw().cos());
        let world = |x: f32, y: f32, z: f32| {
            (
                ((x * c + z * s) * 10.).round() as i32,
                (y * 10.).round() as i32,
                ((-x * s + z * c) * 10.).round() as i32,
            )
        };
        for (x, y, z) in [
            (0., 18., 0.),   // 躯干
            (0., 28., 0.),   // 头
            (6., 18., 0.),   // 左臂
            (-6., 18., 0.),  // 右臂
            (1.9, 6., 0.),   // 左腿
            (-1.9, 6., 0.),  // 右腿
        ] {
            let want = world(x, y, z);
            assert!(
                centers.contains(&want),
                "missing part near {want:?} (vanilla {x},{y},{z}); centers = {centers:?}"
            );
        }
    }
}
