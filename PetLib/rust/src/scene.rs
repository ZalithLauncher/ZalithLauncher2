//! 场景：模型实例容器 + 相机 + 场景级渲染设置。
//!
//! 世界单位 = glTF 单位（1 单位 = 1 方块 = 16 模型像素），Y 向上，
//! 模型根原点落到实例位置。光照与超采样为场景级；外层显隐为模型级。

use glam::{Mat4, Quat, Vec3};

use crate::api::{LightingParams, Model};
use crate::lighting::Lighting;
use crate::model::PIXELS_PER_UNIT;
use crate::render::{self, Frame};
use crate::skin::Texture;

/// 实例句柄：scene.add 返回，从 0 起单调递增，remove 后不复用。
/// JNI 侧会长期缓存句柄，稳定性由"只增不减的空洞 Vec"保证。
pub type InstanceId = usize;

/// 相机投影模式。
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum CameraMode {
    /// 正交：`zoom` 为屏宽覆盖的世界单位数。
    Ortho { zoom: f32 },
    /// 透视：`fov` 为垂直视场角（度）。
    Perspective { fov: f32 },
}

/// 场景相机：绕注视目标的轨道参数。
#[derive(Clone, Copy, Debug)]
pub struct Camera {
    /// 注视目标（世界坐标）
    pub target: Vec3,
    /// 方位角（度）：0 = 从 +Z 看向原点，向 +X 增大
    pub azimuth: f32,
    /// 仰角（度，向上为正）
    pub elevation: f32,
    /// 相机距离（世界单位；正交下不影响取景，仅影响透视深度语义）
    pub distance: f32,
    pub mode: CameraMode,
}

impl Default for Camera {
    /// 默认机位等效移植旧固定机位（视线方向 (30,23,70)、
    /// 视野宽 44 旧像素单位 ≈ 2.75 世界单位）。
    fn default() -> Self {
        Self {
            target: Vec3::new(0., 15. / 16., 0.),
            azimuth: 23.198_59,
            elevation: 16.838_36,
            distance: 5.,
            mode: CameraMode::Ortho { zoom: 44. / 16. },
        }
    }
}

impl Camera {
    /// 投影视图基（顶点为模型像素单位）。
    pub(crate) fn basis(&self, width: f32, height: f32) -> render::ViewBasis {
        let az = self.azimuth.to_radians();
        let el = self.elevation.to_radians();
        let dir = Vec3::new(az.sin() * el.cos(), el.sin(), az.cos() * el.cos());
        let right = Vec3::Y.cross(dir).normalize();
        let up = dir.cross(right);
        let origin = self.target * PIXELS_PER_UNIT;
        let distance = self.distance * PIXELS_PER_UNIT;
        match self.mode {
            CameraMode::Ortho { zoom } => {
                let scale = (width / zoom.max(1e-3)) / PIXELS_PER_UNIT;
                render::ViewBasis::new(right, up, dir, origin, scale, width, height)
            }
            CameraMode::Perspective { fov } => {
                // 焦距：垂直 FOV 折算为屏幕像素（rel/w 为无量纲角比例）
                let focal = (height * 0.5) / (fov.to_radians() * 0.5).tan();
                render::ViewBasis::new_perspective(
                    right, up, dir, origin, focal, distance, width, height,
                )
            }
        }
    }
}

/// 场景：多模型容器与共享渲染设置。
pub struct Scene {
    /// push-only 空洞表：remove 置 None 留洞，id 永不复用。
    models: Vec<Option<Model>>,
    camera: Camera,
    lighting_params: LightingParams,
    lighting: Lighting,
    super_sample: f32,
    frame: Frame,
    /// 跨帧复用的三角形组装缓冲与纹理表。
    scratch: render::CollectScratch,
    tris: Vec<render::Tri>,
    table: Vec<std::sync::Arc<Texture>>,
}

impl Scene {
    pub fn new() -> Self {
        Self {
            models: Vec::new(),
            camera: Camera::default(),
            lighting_params: LightingParams::default(),
            lighting: Lighting::new(&LightingParams::default()),
            super_sample: 2.,
            frame: Frame::new(1, 1),
            scratch: render::CollectScratch::default(),
            tris: Vec::new(),
            table: Vec::new(),
        }
    }

    /// 载入模型实例，返回实例句柄（单调递增，remove 后不复用）。
    pub fn add(&mut self, model: Model) -> InstanceId {
        self.models.push(Some(model));
        self.models.len() - 1
    }

    /// 移除实例并取回模型对象（槽位置 None 留洞，句柄永不复用）。
    pub fn remove(&mut self, id: InstanceId) -> Option<Model> {
        self.models.get_mut(id)?.take()
    }

    pub fn model(&self, id: InstanceId) -> Option<&Model> {
        self.models.get(id).and_then(Option::as_ref)
    }

    pub fn model_mut(&mut self, id: InstanceId) -> Option<&mut Model> {
        self.models.get_mut(id).and_then(Option::as_mut)
    }

    /// 存活实例数（不含已移除的空洞）。
    pub fn instance_count(&self) -> usize {
        self.models.iter().flatten().count()
    }

    pub fn camera(&self) -> Camera {
        self.camera
    }

    pub fn set_camera(&mut self, camera: Camera) {
        self.camera = camera;
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

    /// 渲染一帧到 `out`（预乘 RGBA，长度须为 width*height*4）。
    /// 场景内每个模型的姿态由 `dt` 逐帧推进。
    pub fn render(
        &mut self,
        width: usize,
        height: usize,
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
        self.lighting.prepare(&self.lighting_params);
        let fw = ((width as f32 * self.super_sample).round() as usize).max(1);
        let fh = ((height as f32 * self.super_sample).round() as usize).max(1);
        if self.frame.width != fw || self.frame.height != fh {
            self.frame = Frame::new(fw, fh);
        }
        let view = self.camera.basis(width as f32, height as f32);

        self.tris.clear();
        self.table.clear();
        let mut scratch = std::mem::take(&mut self.scratch);
        let rig = crate::pose::GazeRig::from_view(&self.camera, view);
        for (index, model) in self.models.iter_mut().enumerate() {
            let Some(model) = model else { continue };
            model.skeleton.update_with_rig(&rig, dt);
            let params = render::SceneParams {
                data: &model.data,
                textures: &model.textures,
                outer_layer: model.outer_layer,
            };
            render::collect_tris_view(
                &model.skeleton,
                &params,
                &self.lighting,
                &view,
                instance_matrix(model.position, model.rotation, model.scale),
                (index + 1) as u32,
                &mut scratch,
                &mut self.table,
                &mut self.tris,
            );
        }
        self.frame.draw_collected(&self.tris, &self.table, &self.lighting, true);
        self.scratch = scratch;
        self.frame.present_into(out, width, height);
        Ok(())
    }

    /// 归一化坐标 (0..1) 的像素级拾取，返回命中实例。
    pub fn hit_test(&self, x: f32, y: f32) -> Option<InstanceId> {
        if !(0. ..1.).contains(&x) || !(0. ..1.).contains(&y) {
            return None;
        }
        let px = ((x * self.frame.width as f32) as usize).min(self.frame.width - 1);
        let py = ((y * self.frame.height as f32) as usize).min(self.frame.height - 1);
        match self.frame.instances[py * self.frame.width + px] {
            0 => None,
            id => Some((id - 1) as InstanceId),
        }
    }
}

impl Default for Scene {
    fn default() -> Self {
        Self::new()
    }
}

/// 实例变换矩阵：网格顶点已是模型像素单位，位移换算 ×16 后
/// 位移 × 旋转（pitch/yaw/roll，弧度）× 均匀缩放。
fn instance_matrix(position: Vec3, rotation_rad: Vec3, scale: f32) -> Mat4 {
    let rotation = Quat::from_rotation_y(rotation_rad.y)
        * Quat::from_rotation_x(rotation_rad.x)
        * Quat::from_rotation_z(rotation_rad.z);
    Mat4::from_translation(position * PIXELS_PER_UNIT)
        * Mat4::from_quat(rotation)
        * Mat4::from_scale(Vec3::splat(scale))
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::gltf::test_fixture;
    use crate::api::Model;

    fn skin_png() -> Vec<u8> {
        let mut img = image::RgbaImage::new(64, 64);
        for p in img.pixels_mut() {
            *p = image::Rgba([255, 0, 0, 255]);
        }
        let mut png = Vec::new();
        img.write_to(&mut std::io::Cursor::new(&mut png), image::ImageFormat::Png)
            .unwrap();
        png
    }

    #[test]
    fn instance_rotation_uses_radians() {
        let matrix = instance_matrix(
            Vec3::ZERO,
            Vec3::new(std::f32::consts::FRAC_PI_2, 0., 0.),
            1.,
        );
        assert!(matrix.transform_point3(Vec3::Y).abs_diff_eq(Vec3::Z, 1e-6));
    }

    /// 两件道具模型必须都出现在帧里，且拾取能区分左右实例。
    #[test]
    fn scene_renders_two_props_and_picks_them() {
        let mut scene = Scene::new();
        let mut left = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        left.set_position(-0.8, 0., -0.3);
        let mut right = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        right.set_position(0.8, 0., 0.3);
        let a = scene.add(left);
        let b = scene.add(right);
        assert_eq!((a, b), (0, 1));

        let (w, h) = (128usize, 128usize);
        let mut out = vec![0u8; w * h * 4];
        scene.set_super_sample(1.);
        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let opaque = out.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();
        assert!(opaque > 0, "scene should render visible pixels");

        // 网格扫描：两个实例都应被拾取到（三角形朝向由 180°Y 根换算）
        let mut hit_ids = Vec::new();
        for ky in 0..20 {
            for kx in 0..20 {
                let x = 0.05 + 0.9 * kx as f32 / 20.;
                let y = 0.1 + 0.8 * ky as f32 / 20.;
                if let Some(id) = scene.hit_test(x, y) {
                    if !hit_ids.contains(&id) {
                        hit_ids.push(id);
                    }
                }
            }
        }
        assert!(hit_ids.contains(&a), "left instance should be picked: {hit_ids:?}");
        assert!(hit_ids.contains(&b), "right instance should be picked: {hit_ids:?}");
        assert_eq!(scene.hit_test(0.001, 0.001), None);
    }

    /// 实例缩放改变渲染大小；移除实例后其像素消失。
    #[test]
    fn scene_instance_transform_and_removal() {
        let mut scene = Scene::new();
        let mut prop = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        prop.set_scale(3.);
        let id = scene.add(prop);
        let (w, h) = (128usize, 128usize);
        let mut out = vec![0u8; w * h * 4];
        scene.set_super_sample(1.);
        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let opaque = out.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();

        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let _ = id;
        let removed = scene.remove(0).unwrap();
        drop(removed);
        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let after = out.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();
        assert_eq!(after, 0, "removed instance should render no pixels");
        assert!(opaque > 0);
    }

    /// 句柄稳定：remove 后 id 永不复用（JNI 侧长期缓存句柄的前提）。
    #[test]
    fn scene_ids_are_stable_and_never_reused() {
        let mut scene = Scene::new();
        let a = scene.add(Model::from_glb(&test_fixture::prop_glb()).unwrap());
        let b = scene.add(Model::from_glb(&test_fixture::prop_glb()).unwrap());
        assert_eq!((a, b), (0, 1));
        assert!(scene.remove(a).is_some());
        assert!(scene.remove(a).is_none(), "removing the same id twice should be None");
        let c = scene.add(Model::from_glb(&test_fixture::prop_glb()).unwrap());
        assert_eq!(c, 2, "holes are never reused; a new instance must get a new id");
        assert_eq!(scene.instance_count(), 2);
        assert!(scene.model(a).is_none());
        assert!(scene.model(b).is_some());
        assert!(scene.model(c).is_some());
    }

    /// 场景级光照影响所有实例。
    #[test]
    fn scene_lighting_is_shared() {
        let mut scene = Scene::new();
        scene.add(Model::from_glb(&test_fixture::prop_glb()).unwrap());
        let (w, h) = (128usize, 128usize);
        let mut out = vec![0u8; w * h * 4];
        scene.set_super_sample(1.);
        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let bright: u64 = out
            .chunks_exact(4)
            .filter(|p| p[3] > 0)
            .map(|p| p[0] as u64)
            .sum();

        scene.set_lighting(LightingParams {
            azimuth: 20.,
            elevation: 35.,
            brightness: 20.,
            softness: 50.,
        });
        scene.render(w, h, 1. / 60., &mut out).unwrap();
        let dim: u64 = out
            .chunks_exact(4)
            .filter(|p| p[3] > 0)
            .map(|p| p[0] as u64)
            .sum();
        assert!(dim < bright, "dimming scene lighting should reduce brightness: {dim} < {bright}");
    }

    /// 玩家模型与道具模型可以同场渲染。
    #[test]
    fn scene_mixes_player_and_prop() {
        let bytes = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/src/assets/classic-player.glb"
        ))
        .unwrap();
        let mut scene = Scene::new();
        let mut player = Model::from_glb_with_skin(&bytes, &skin_png(), None).unwrap();
        player.set_position(0.5, 0., 0.);
        scene.add(player);
        scene.add(Model::from_glb(&test_fixture::prop_glb()).unwrap());
        let (w, h) = (128usize, 128usize);
        let mut out = vec![0u8; w * h * 4];
        scene.set_super_sample(1.);
        for _ in 0..10 {
            scene.render(w, h, 1. / 60., &mut out).unwrap();
        }
        let opaque = out.iter().skip(3).step_by(4).filter(|&&v| v > 0).count();
        assert!(opaque > 1000, "mixed scene should render many pixels: {opaque}");
    }

    /// 透视模式近大远小；正交模式等大。两件相同道具分别放在
    /// 近处与远处，统计不透明像素数对比。
    #[test]
    fn perspective_mode_shrinks_far_objects() {
        let (w, h) = (128usize, 128usize);
        let mut out = vec![0u8; w * h * 4];
        let mut opaque = |scene: &mut Scene| -> usize {
            out.iter_mut().for_each(|b| *b = 0);
            scene.render(w, h, 1. / 60., &mut out).unwrap();
            out.iter().skip(3).step_by(4).filter(|&&v| v > 0).count()
        };

        // 透视：近处明显大于远处
        let mut persp = Scene::new();
        persp.set_camera(Camera {
            mode: CameraMode::Perspective { fov: 50. },
            ..Camera::default()
        });
        persp.set_super_sample(1.);
        let mut near = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        near.set_position(-0.3, 0., 1.4);
        let mut far = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        far.set_position(0.3, 0., -0.6);
        persp.add(near);
        persp.add(far);
        let persp_opaque = opaque(&mut persp);
        assert!(persp_opaque > 0, "perspective scene should render pixels");

        // 逐实例验证：只放近的 vs 只放远的
        let mut only_near = Scene::new();
        only_near.set_camera(Camera {
            mode: CameraMode::Perspective { fov: 50. },
            ..Camera::default()
        });
        only_near.set_super_sample(1.);
        let mut near_only = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        near_only.set_position(-0.3, 0., 1.4);
        only_near.add(near_only);
        let near_px = opaque(&mut only_near);
        assert!(near_px > 0);

        let mut only_far = Scene::new();
        only_far.set_camera(Camera {
            mode: CameraMode::Perspective { fov: 50. },
            ..Camera::default()
        });
        only_far.set_super_sample(1.);
        let mut far_only = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        far_only.set_position(0.3, 0., -0.6);
        only_far.add(far_only);
        let far_px = opaque(&mut only_far);
        assert!(far_px > 0);
        assert!(
            near_px > far_px * 3 / 2,
            "in perspective mode the near object should be significantly larger: near={near_px} far={far_px}"
        );

        // 正交：同一远近布局像素量接近（等大）
        let mut ortho_near = Scene::new();
        ortho_near.set_super_sample(1.);
        let mut n = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        n.set_position(-0.3, 0., 1.4);
        ortho_near.add(n);
        let ortho_near_px = opaque(&mut ortho_near);
        let mut ortho_far = Scene::new();
        ortho_far.set_super_sample(1.);
        let mut f = Model::from_glb(&test_fixture::prop_glb()).unwrap();
        f.set_position(0.0, 0., -0.6);
        ortho_far.add(f);
        let ortho_far_px = opaque(&mut ortho_far);
        assert!(
            (ortho_near_px as f32 - ortho_far_px as f32).abs() / (ortho_near_px.max(1) as f32) < 0.1,
            "in ortho mode near and far should render the same size: near={ortho_near_px} far={ortho_far_px}"
        );
    }
}
