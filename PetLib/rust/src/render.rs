//! 软件光栅化：glTF 网格 → 节点树姿态合成 → 投影 → 三角形覆盖 →
//! z-buffer 逐像素混合，末端经盒式降采样输出预乘 RGBA（Android Bitmap 原生格式）。

use glam::{Mat4, Vec2, Vec3};
use std::sync::{Arc, OnceLock};

use crate::lighting::Lighting;
use crate::model::{DrawItem, ModelData};
use crate::pool;
use crate::pose::{self, Skeleton};
use crate::skin::Texture;

/// 视线方向（project 与法线剔除共用）：(30, 23, 70) 的归一化。
const VIEW_FORWARD: Vec3 = Vec3::new(
    30. / 79.55501241279521,
    23. / 79.55501241279521,
    70. / 79.55501241279521,
);

/// 投影：弱透视（z 仅用于深度排序），模型竖直居中于视图。
pub fn project(v: Vec3, width: f32, height: f32) -> Vec3 {
    let forward = VIEW_FORWARD;
    let right = Vec3::Y.cross(forward).normalize();
    let up = forward.cross(right);
    let v = v - Vec3::new(0., 15., 0.);
    let scale = width / 44.;
    Vec3::new(
        width / 2. + v.dot(right) * scale,
        height / 2. - v.dot(up) * scale,
        -v.dot(forward),
    )
}

/// 视图基：世界坐标 → 屏幕坐标的投影参数。旧固定机位与场景相机
/// 都投影为同一组基，光栅化端只有一条代码路径。
#[derive(Clone, Copy)]
pub(crate) struct ViewBasis {
    right: Vec3,
    up: Vec3,
    dir: Vec3,
    /// 被减去的视点（与顶点同单位）
    origin: Vec3,
    /// 顶点单位 → 屏幕像素（正交取景系数；透视模式为注视解算用的
    /// 目标深度处等效系数）
    pub(crate) scale: f32,
    width: f32,
    height: f32,
    /// 焦距（像素，垂直 FOV 折算）；正交模式未用
    focal: f32,
    /// 相机距离（与顶点同单位）；正交模式未用
    distance: f32,
    /// 透视模式：投影带 1/w 除法，深度为 -1/w
    perspective: bool,
}

impl ViewBasis {
    /// 由相机的正交基参数构造（正交模式）。
    #[allow(clippy::too_many_arguments)]
    pub(crate) fn new(
        right: Vec3,
        up: Vec3,
        dir: Vec3,
        origin: Vec3,
        scale: f32,
        width: f32,
        height: f32,
    ) -> Self {
        Self {
            right,
            up,
            dir,
            origin,
            scale,
            width,
            height,
            focal: 0.,
            distance: 0.,
            perspective: false,
        }
    }

    /// 透视模式构造：`focal` 为垂直 FOV 折算的焦距（像素）。
    #[allow(clippy::too_many_arguments)]
    pub(crate) fn new_perspective(
        right: Vec3,
        up: Vec3,
        dir: Vec3,
        origin: Vec3,
        focal: f32,
        distance: f32,
        width: f32,
        height: f32,
    ) -> Self {
        // 注视解算的屏幕差换算系数：目标深度处的世界单位 → 像素。
        let scale = focal / distance.max(1e-3);
        Self {
            right,
            up,
            dir,
            origin,
            scale,
            width,
            height,
            focal,
            distance,
            perspective: true,
        }
    }

    /// 是否透视模式。
    pub(crate) fn perspective(&self) -> bool {
        self.perspective
    }

    /// 视深 w（沿视线到相机的距离，顶点像素单位，未夹取）。
    pub(crate) fn raw_depth(&self, v: Vec3) -> f32 {
        self.distance - (v - self.origin).dot(self.dir)
    }

    /// 投影。返回 (屏幕坐标, 视深 w)；正交模式 w 恒为 0（仿射路径）。
    pub(crate) fn project_view(&self, v: Vec3) -> (Vec3, f32) {
        let rel = v - self.origin;
        if !self.perspective {
            let z = -rel.dot(self.dir);
            let xy = Vec3::new(
                self.width / 2. + rel.dot(self.right) * self.scale,
                self.height / 2. - rel.dot(self.up) * self.scale,
                z,
            );
            return (xy, 0.);
        }
        // 透视：w 为沿视线到相机的距离；z 存 -1/w（屏幕线性插值
        // 自动透视正确）。w 过小视为贴住近平面。
        let w = (self.distance - rel.dot(self.dir)).max(1e-3);
        let inv = 1. / w;
        let xy = Vec3::new(
            self.width / 2. + rel.dot(self.right) * self.focal * inv,
            self.height / 2. - rel.dot(self.up) * self.focal * inv,
            -inv,
        );
        (xy, w)
    }
}

/// 旧固定机位基（顶点为模型像素单位）：与 project() 逐位一致。
pub(crate) fn legacy_basis(view: Vec2) -> ViewBasis {
    let forward = VIEW_FORWARD;
    let right = Vec3::Y.cross(forward).normalize();
    let up = forward.cross(right);
    ViewBasis::new(
        right,
        up,
        forward,
        Vec3::new(0., 15., 0.),
        view.x / 44.,
        view.x,
        view.y,
    )
}

/// 旧固定机位的相对距离缩放：1 保持原取景，值越大模型越小。
/// 正交投影不改变视角，仅按距离比例调整取景系数。
pub(crate) fn legacy_basis_with_distance(view: Vec2, distance: f32) -> ViewBasis {
    let mut basis = legacy_basis(view);
    basis.scale /= distance.max(1e-3);
    basis
}

/// 无纹理槽位哨兵：该材质本帧不绘制。
const NO_TEXTURE: u32 = u32::MAX;

pub(crate) struct SceneParams<'a> {
    /// 臂型对应的模型数据（骨架绑定同一结构的节点树）
    pub(crate) data: &'a ModelData,
    /// 材质索引 → 纹理；None 的材质跳过绘制
    pub(crate) textures: &'a [Option<Arc<Texture>>],
    pub(crate) outer_layer: bool,
}

/// 单个模型组装三角形时使用的临时缓冲，由帧或场景持有并跨帧复用。
#[derive(Default)]
pub(crate) struct CollectScratch {
    world: Vec<Option<Mat4>>,
    chain: Vec<usize>,
    slots: Vec<u32>,
    /// 外层绘制项下标与排序深度；存下标可避免缓冲引用模型数据。
    layers: Vec<(usize, f32)>,
}

/// 光栅化三角形：投影后屏幕坐标、纹理像素坐标与光照明暗系数。
pub(crate) struct Tri {
    pub(crate) p: [Vec3; 3],
    /// 每顶点视深 w；全零 = 正交（仿射插值路径），非零 = 透视校正路径
    pub(crate) w: [f32; 3],
    pub(crate) uv: [Vec2; 3],
    pub(crate) shade: f32,
    /// 纹理槽位（本帧纹理表下标）
    pub(crate) tex: u32,
    /// 所属实例 id（拾取缓冲写入；0 = 无拾取）
    pub(crate) instance: u32,
}

/// 调试用部件变换快照：类型、包围盒尺寸与列主序世界矩阵
/// （平移列为包围盒中心的变换结果）。
pub(crate) struct PartTransform {
    pub(crate) kind: &'static str,
    pub(crate) size: [f32; 3],
    pub(crate) matrix: [f32; 16],
}

/// 压扁以脚底为锚，双脚下沉为零，头顶随脉冲下压；叠加整体偏航。
fn global_transform(pose: &Skeleton) -> Mat4 {
    Mat4::from_rotation_y(pose.model_yaw() + pose::front_yaw())
        * Mat4::from_translation(Vec3::Y * -12.)
        * Mat4::from_scale(Vec3::new(1., pose.squash, 1.))
        * Mat4::from_translation(Vec3::Y * 12.)
}

/// 节点局部矩阵：驱动节点取姿态值，未驱动节点回退静止 TRS。
fn local_matrix(data: &ModelData, pose: &Skeleton, node: usize) -> Mat4 {
    let n = &data.nodes[node];
    let (t, r) = if data.driven[node] {
        (pose.translations[node], pose.rotations[node])
    } else {
        (n.rest_translation, n.rest_rotation)
    };
    Mat4::from_translation(t) * Mat4::from_quat(r) * Mat4::from_scale(n.rest_scale)
}

/// 沿父链上溯并自顶向下 memoize 节点世界矩阵。
fn node_world(
    data: &ModelData,
    pose: &Skeleton,
    global: Mat4,
    node: usize,
    world: &mut [Option<Mat4>],
    chain: &mut Vec<usize>,
) -> Mat4 {
    chain.clear();
    let mut cur = node;
    while world[cur].is_none() {
        chain.push(cur);
        match data.nodes[cur].parent {
            Some(p) => cur = p,
            None => break,
        }
    }
    while let Some(&n) = chain.last() {
        let parent = data.nodes[n]
            .parent
            .map(|p| world[p].unwrap_or(Mat4::IDENTITY))
            .unwrap_or(global);
        world[n] = Some(parent * local_matrix(data, pose, n));
        chain.pop();
    }
    world[node].unwrap_or(Mat4::IDENTITY)
}

/// 组装一个模型实例的全部三角形（追加式：table/out 可跨实例累积，
/// 供场景把多个实例汇进同一张纹理表与同一趟光栅化）。
pub(crate) fn collect_tris(
    skeleton: &Skeleton,
    scene: &SceneParams,
    light: &Lighting,
    width: usize,
    height: usize,
    camera_distance: f32,
    scratch: &mut CollectScratch,
    table: &mut Vec<Arc<Texture>>,
    out: &mut Vec<Tri>,
) {
    let view = legacy_basis_with_distance(
        Vec2::new(width as f32, height as f32),
        camera_distance,
    );
    collect_tris_view(
        skeleton,
        scene,
        light,
        &view,
        Mat4::IDENTITY,
        0,
        scratch,
        table,
        out,
    );
}

/// 视图基 + 实例矩阵版本：场景渲染入口（instance_id 写入拾取缓冲）。
/// 纹理表按 Arc 克隆累积（场景跨实例共享所有权）。
pub(crate) fn collect_tris_view(
    skeleton: &Skeleton,
    scene: &SceneParams,
    light: &Lighting,
    view: &ViewBasis,
    instance_matrix: Mat4,
    instance_id: u32,
    scratch: &mut CollectScratch,
    table: &mut Vec<Arc<Texture>>,
    out: &mut Vec<Tri>,
) {
    let data = scene.data;
    let global = instance_matrix * global_transform(skeleton);
    let CollectScratch {
        world,
        chain,
        slots,
        layers,
    } = scratch;
    world.clear();
    world.resize(data.nodes.len(), None);
    chain.clear();
    let mut world_of = |node: usize| node_world(data, skeleton, global, node, world, chain);

    // 将本实例的材质映射到帧级纹理槽，跳过无纹理材质。
    slots.clear();
    for texture in scene.textures {
        match texture {
            Some(t) => {
                slots.push(table.len() as u32);
                table.push(Arc::clone(t));
            }
            None => slots.push(NO_TEXTURE),
        }
    }
    let slot_of = |item: &DrawItem| slots.get(item.material).copied().unwrap_or(NO_TEXTURE);

    // 基础网格按树先序绘制；半透明外层最后按相机深度排序（对齐 three.js 行为）。
    for item in &data.draw_list {
        if item.layer && !scene.outer_layer {
            continue;
        }
        if slot_of(item) == NO_TEXTURE {
            continue;
        }
        emit_item(
            data,
            scene,
            light,
            view,
            item,
            slot_of(item),
            instance_id,
            world_of(item.node),
            out,
        );
    }
    layers.clear();
    if scene.outer_layer {
        for (index, item) in data.draw_list.iter().enumerate() {
            if !item.layer || slot_of(item) == NO_TEXTURE {
                continue;
            }
            let w = world_of(item.node);
            let z = view.project_view(w.transform_point3(item.center)).0.z;
            layers.push((index, z));
        }
        // 深度相同时保持绘制序；不稳定排序配合下标决胜无需额外分配。
        layers.sort_unstable_by(|a, b| b.1.total_cmp(&a.1).then_with(|| a.0.cmp(&b.0)));
        for &(index, _) in layers.iter() {
            let item = &data.draw_list[index];
            emit_item(
                data,
                scene,
                light,
                view,
                item,
                slot_of(item),
                instance_id,
                world_of(item.node),
                out,
            );
        }
    }
}

/// 透视近平面（顶点像素单位）：越过相机平面的三角形按此裁剪。
const NEAR_EPS: f32 = 0.05;

/// 把一个绘制项的三角形写入输出（顶点变换、投影、近平面裁剪、剔除、明暗）。
fn emit_item(
    data: &ModelData,
    scene: &SceneParams,
    light: &Lighting,
    view: &ViewBasis,
    item: &DrawItem,
    slot: u32,
    instance_id: u32,
    world: Mat4,
    out: &mut Vec<Tri>,
) {
    let primitive = data.primitive(item);
    let texture = scene.textures[item.material]
        .as_deref()
        .expect("emitting item without texture");
    let (tw, th) = (texture.width as f32, texture.height as f32);

    for idx in primitive.indices.chunks_exact(3) {
        let i = [idx[0] as usize, idx[1] as usize, idx[2] as usize];
        let positions: [Vec3; 3] =
            i.map(|k| world.transform_point3(Vec3::from(primitive.positions[k])));
        let uvs: [Vec2; 3] =
            i.map(|k| Vec2::new(primitive.uvs[k][0] * tw, primitive.uvs[k][1] * th));
        // 法线随世界旋转（压扁为轴向对齐缩放，归一化后方向不变）
        let normal = world
            .transform_vector3(Vec3::from(primitive.normals[i[0]]))
            .normalize();
        let shade = light.shade(normal);

        if !view.perspective() {
            let p: [Vec3; 3] = positions.map(|v| view.project_view(v).0);
            // 背面剔除：屏幕坐标系 y 向下，正面三角形面积为负（缠绕已在
            // 加载时统一为外向）；外层保持双面绘制，对齐盒体路径行为。
            let area = edge(p[0], p[1], p[2]);
            if !item.layer && area >= 0. {
                continue;
            }
            out.push(Tri {
                p,
                w: [0.; 3],
                uv: uvs,
                shade,
                tex: slot,
                instance: instance_id,
            });
            continue;
        }

        // 透视：w 全部在近平面前 → 直投影；否则按近平面裁剪成
        // 凸多边形再扇形拆分（最多四顶点、两个三角形）。
        let ws: [f32; 3] = positions.map(|v| view.raw_depth(v));
        if ws.iter().all(|&w| w >= NEAR_EPS) {
            push_perspective_tri(
                view,
                positions,
                uvs,
                shade,
                item.layer,
                slot,
                instance_id,
                out,
            );
            continue;
        }
        let mut poly: [(Vec3, Vec2, f32); 4] = [
            (positions[0], uvs[0], ws[0]),
            (positions[1], uvs[1], ws[1]),
            (positions[2], uvs[2], ws[2]),
            (positions[2], uvs[2], ws[2]),
        ];
        let mut count = 0usize;
        for k in 0..3 {
            let (cv, cu, cw) = poly[k];
            let n = (k + 1) % 3;
            let (nv, nu, nw) = (positions[n], uvs[n], ws[n]);
            let (cur_in, nxt_in) = (cw >= NEAR_EPS, nw >= NEAR_EPS);
            if cur_in {
                poly[count] = (cv, cu, cw);
                count += 1;
            }
            if cur_in != nxt_in {
                let t = (NEAR_EPS - cw) / (nw - cw);
                poly[count] = (cv.lerp(nv, t), cu.lerp(nu, t), NEAR_EPS);
                count += 1;
            }
        }
        if count < 3 {
            continue;
        }
        for k in 1..(count - 1) {
            let (a, b, c) = (poly[0], poly[k], poly[k + 1]);
            push_perspective_tri(
                view,
                [a.0, b.0, c.0],
                [a.1, b.1, c.1],
                shade,
                item.layer,
                slot,
                instance_id,
                out,
            );
        }
    }
}

/// 投影并剔除一个透视三角形，写入输出。
#[allow(clippy::too_many_arguments)]
fn push_perspective_tri(
    view: &ViewBasis,
    positions: [Vec3; 3],
    uvs: [Vec2; 3],
    shade: f32,
    layer: bool,
    slot: u32,
    instance_id: u32,
    out: &mut Vec<Tri>,
) {
    let pw: [(Vec3, f32); 3] = positions.map(|v| view.project_view(v));
    let p = pw.map(|e| e.0);
    let area = edge(p[0], p[1], p[2]);
    if !layer && area >= 0. {
        return;
    }
    let w = pw.map(|e| e.1);
    out.push(Tri {
        p,
        w,
        uv: uvs,
        shade,
        tex: slot,
        instance: instance_id,
    });
}

/// 调试：当前帧各部件的类型、包围盒尺寸与列主序世界矩阵。
pub(crate) fn part_transforms(skeleton: &Skeleton, scene: &SceneParams) -> Vec<PartTransform> {
    let data = scene.data;
    let global = global_transform(skeleton);
    let mut world: Vec<Option<Mat4>> = vec![None; data.nodes.len()];
    let mut chain: Vec<usize> = Vec::new();
    let mut parts = Vec::new();
    for item in &data.draw_list {
        let w = node_world(data, skeleton, global, item.node, &mut world, &mut chain);
        let mut matrix = w;
        matrix.w_axis = w.transform_point3(item.center).extend(1.);
        parts.push(PartTransform {
            kind: if crate::model::material_is_cape(data.materials[item.material].name.as_deref()) {
                "cape"
            } else {
                "skin"
            },
            size: item.extents.to_array(),
            matrix: matrix.to_cols_array(),
        });
    }
    parts
}

/// 帧缓冲：预乘 RGBA 像素、深度、逐像素实例 id 与跨帧复用的三角形缓冲。
pub struct Frame {
    pub width: usize,
    pub height: usize,
    /// 预乘 RGBA。
    pub pixels: Vec<u8>,
    depth: Vec<f32>,
    /// 每像素实例 id（拾取用；0 = 空）
    pub(crate) instances: Vec<u32>,
    pub bounds: [i32; 4],
    resample: Option<Resample>,
    /// 跨帧复用的组装缓冲与纹理表。
    tris: Vec<Tri>,
    scratch: CollectScratch,
    table: Vec<Arc<Texture>>,
}

impl Frame {
    pub fn new(w: usize, h: usize) -> Self {
        Self {
            width: w,
            height: h,
            pixels: vec![0; w * h * 4],
            depth: vec![f32::INFINITY; w * h],
            instances: vec![0; w * h],
            bounds: [0; 4],
            resample: None,
            tris: Vec::new(),
            scratch: CollectScratch::default(),
            table: Vec::new(),
        }
    }

    /// 测试探针：当前缓冲内的三角形数量（累积检测）。
    #[cfg(test)]
    pub(crate) fn tri_count(&self) -> usize {
        self.tris.len()
    }

    /// 组装场景并绘制一帧。
    pub(crate) fn draw_scene(
        &mut self,
        skeleton: &Skeleton,
        scene: &SceneParams,
        light: &Lighting,
        camera_distance: f32,
    ) {
        let mut tris = std::mem::take(&mut self.tris);
        let mut scratch = std::mem::take(&mut self.scratch);
        let mut table = std::mem::take(&mut self.table);
        tris.clear();
        table.clear();
        collect_tris(
            skeleton,
            scene,
            light,
            self.width,
            self.height,
            camera_distance,
            &mut scratch,
            &mut table,
            &mut tris,
        );
        self.draw(&tris, &table, light, false);
        self.tris = tris;
        self.scratch = scratch;
        self.table = table;
    }

    /// 多模型共用入口（场景路径）：tris 已含全部实例、table 为共享纹理表，
    /// `pick` 时把胜利像素的实例 id 写入拾取缓冲。
    pub(crate) fn draw_collected(
        &mut self,
        tris: &[Tri],
        table: &[Arc<Texture>],
        light: &Lighting,
        pick: bool,
    ) {
        self.draw(tris, table, light, pick);
    }

    fn draw(&mut self, tris: &[Tri], textures: &[Arc<Texture>], light: &Lighting, pick: bool) {
        // 只清上一帧写入过的矩形：每帧写入都落在当帧 bounds 内，
        // 逐帧接力清除即覆盖全部历史写入，区域外像素从未被触碰、保持为零。
        let left = self.bounds[0].clamp(0, self.width as i32) as usize;
        let right = self.bounds[2].clamp(0, self.width as i32) as usize;
        let top = self.bounds[1].clamp(0, self.height as i32) as usize;
        let bottom = self.bounds[3].clamp(0, self.height as i32) as usize;
        if left < right && top < bottom {
            for y in top..bottom {
                let row = y * self.width;
                self.pixels[(row + left) * 4..(row + right) * 4].fill(0);
                self.depth[row + left..row + right].fill(f32::INFINITY);
                self.instances[row + left..row + right].fill(0);
            }
        }
        let empty = [self.width as i32, self.height as i32, 0, 0];
        self.bounds = empty;

        let threads = if tris.is_empty() {
            1
        } else {
            raster_threads(self.width, self.height)
        };
        if threads <= 1 {
            let mut instances = None;
            if pick {
                instances = Some(self.instances.as_mut_slice());
            }
            let mut band = Band {
                pixels: &mut self.pixels[..],
                depth: &mut self.depth[..],
                instances,
                width: self.width,
                height: self.height,
                start_row: 0,
                end_row: self.height,
                bounds: empty,
            };
            for tri in tris {
                let tex = textures[tri.tex as usize].as_ref();
                triangle(
                    &mut band,
                    tri.p,
                    tri.w,
                    tri.uv,
                    tri.instance,
                    tex,
                    light.lut(tri.shade),
                );
            }
            self.bounds = band.bounds;
            return;
        }

        let width = self.width;
        let height = self.height;
        let band_count = threads * 4;
        let rows_per_band = height.div_ceil(band_count);
        let instances_ptr = if pick {
            self.instances.as_mut_ptr()
        } else {
            std::ptr::null_mut()
        };
        let mut raster_jobs: Vec<RasterJob> = Vec::with_capacity(band_count);
        let mut start_row = 0;
        while start_row < height {
            let end_row = (start_row + rows_per_band).min(height);
            raster_jobs.push(RasterJob {
                tris: tris.as_ptr(),
                tris_len: tris.len(),
                textures,
                light,
                pixels: self.pixels.as_mut_ptr(),
                depth: self.depth.as_mut_ptr(),
                instances: instances_ptr,
                width,
                height,
                start_row,
                end_row,
                bounds: empty,
            });
            start_row = end_row;
        }
        let jobs: Vec<pool::Job> = raster_jobs
            .iter_mut()
            .map(|job| pool::Job {
                run: run_raster_job,
                arg: (job as *mut RasterJob).cast(),
            })
            .collect();
        pool::run_jobs(&jobs, threads - 1);
        self.bounds = raster_jobs
            .iter()
            .map(|job| job.bounds)
            .reduce(merge_bounds)
            .unwrap_or(empty);
    }
}

/// 并行光栅任务的共享上下文。
struct RasterJob<'a> {
    tris: *const Tri,
    tris_len: usize,
    /// 帧级纹理表（槽位 = Tri.tex）
    textures: &'a [Arc<Texture>],
    light: &'a Lighting,
    pixels: *mut u8,
    depth: *mut f32,
    /// 每像素实例 id；空指针表示不拾取
    instances: *mut u32,
    width: usize,
    height: usize,
    start_row: usize,
    end_row: usize,
    bounds: [i32; 4],
}

unsafe fn run_raster_job(arg: *mut ()) {
    let job = &mut *(arg as *mut RasterJob);
    let rows = job.end_row - job.start_row;
    let instances = if job.instances.is_null() {
        None
    } else {
        // SAFETY: instances 与 pixels/depth 同长同行划分，区间互不重叠。
        Some(std::slice::from_raw_parts_mut(
            job.instances.add(job.start_row * job.width),
            rows * job.width,
        ))
    };
    let mut band = Band {
        // SAFETY: 行区间由提交方划分互不重叠，帧缓冲覆盖全部行。
        pixels: std::slice::from_raw_parts_mut(
            job.pixels.add(job.start_row * job.width * 4),
            rows * job.width * 4,
        ),
        depth: std::slice::from_raw_parts_mut(
            job.depth.add(job.start_row * job.width),
            rows * job.width,
        ),
        instances,
        width: job.width,
        height: job.height,
        start_row: job.start_row,
        end_row: job.end_row,
        bounds: [job.width as i32, job.height as i32, 0, 0],
    };
    // SAFETY: tris/textures/light 来自提交方调用栈，任务期间有效；
    // 三角形的槽位必指向表内存在的纹理（无纹理材质的绘制项不入列）。
    let tris = std::slice::from_raw_parts(job.tris, job.tris_len);
    let textures = job.textures;
    let light = job.light;
    for tri in tris {
        let tex = textures[tri.tex as usize].as_ref();
        triangle(
            &mut band,
            tri.p,
            tri.w,
            tri.uv,
            tri.instance,
            tex,
            light.lut(tri.shade),
        );
    }
    job.bounds = band.bounds;
}

/// 一段行区间及其独占的颜色/深度/拾取切片
struct Band<'a> {
    pixels: &'a mut [u8],
    depth: &'a mut [f32],
    instances: Option<&'a mut [u32]>,
    width: usize,
    height: usize,
    start_row: usize,
    end_row: usize,
    /// 区间内的写入包围盒
    bounds: [i32; 4],
}

fn merge_bounds(a: [i32; 4], b: [i32; 4]) -> [i32; 4] {
    [
        a[0].min(b[0]),
        a[1].min(b[1]),
        a[2].max(b[2]),
        a[3].max(b[3]),
    ]
}

#[cfg(test)]
static TEST_THREADS: std::sync::atomic::AtomicUsize = std::sync::atomic::AtomicUsize::new(0);

/// 光栅线程数：小帧不值得分片开销；条带轮转下最慢的核心决定整帧
/// 尾延迟，大小核异构设备只用大核，避免小核拖尾。
fn raster_threads(width: usize, height: usize) -> usize {
    #[cfg(test)]
    {
        let forced = TEST_THREADS.load(std::sync::atomic::Ordering::Relaxed);
        if forced != 0 {
            return forced;
        }
    }
    if width * height < 64 * 1024 {
        return 1;
    }
    static BIG_CORES: OnceLock<Option<usize>> = OnceLock::new();
    let available = std::thread::available_parallelism()
        .map_or(1, |n| n.get())
        .min(8);
    match *BIG_CORES.get_or_init(detect_big_cores) {
        Some(big) if big >= 2 => big.min(available),
        _ => available,
    }
}

/// 大核探测：优先读内核归一化算力 cpu_capacity，缺失时按核心最高
/// 频率之比估计（≥ 最快核心 75% 视为大核）；都读不到返回 None，
/// 按全部核心处理。
fn detect_big_cores() -> Option<usize> {
    let mut capacities = Vec::new();
    let mut freqs = Vec::new();
    for cpu in 0..16u32 {
        if let Some(v) = read_sysfs_f32(format!("/sys/devices/system/cpu/cpu{cpu}/cpu_capacity")) {
            capacities.push(v);
        }
        if let Some(v) = read_sysfs_u32(format!(
            "/sys/devices/system/cpu/cpu{cpu}/cpufreq/cpuinfo_max_freq"
        )) {
            freqs.push(v);
        }
    }
    if capacities.len() >= 4 {
        let max = capacities.iter().cloned().fold(0., f32::max);
        return Some(capacities.iter().filter(|&&c| c >= max * 0.75).count());
    }
    if freqs.len() >= 4 {
        let max = *freqs.iter().max()?;
        return Some(
            freqs
                .iter()
                .filter(|&&f| u64::from(f) * 4 >= u64::from(max) * 3)
                .count(),
        );
    }
    None
}

fn read_sysfs_u32(path: String) -> Option<u32> {
    std::fs::read_to_string(path).ok()?.trim().parse().ok()
}

fn read_sysfs_f32(path: String) -> Option<f32> {
    std::fs::read_to_string(path).ok()?.trim().parse().ok()
}

/// 光栅化三角形。`uv` 为纹理像素坐标（新路径由网格归一化 UV 直接换算）；
/// `w` 全零走仿射插值（正交），非零走透视校正插值。
fn triangle(
    band: &mut Band,
    p: [Vec3; 3],
    w: [f32; 3],
    uv: [Vec2; 3],
    instance: u32,
    tex: &Texture,
    lut: &[[u8; 256]; 3],
) {
    let area = edge(p[0], p[1], p[2]);
    if area.abs() < 0.0001 {
        return;
    }
    let minx = p
        .iter()
        .map(|p| p.x)
        .fold(f32::INFINITY, f32::min)
        .floor()
        .max(0.) as usize;
    let maxx = p
        .iter()
        .map(|p| p.x)
        .fold(f32::NEG_INFINITY, f32::max)
        .ceil()
        .min(band.width as f32) as usize;
    let miny = p
        .iter()
        .map(|p| p.y)
        .fold(f32::INFINITY, f32::min)
        .floor()
        .max(0.)
        .max(band.start_row as f32) as usize;
    let maxy = p
        .iter()
        .map(|p| p.y)
        .fold(f32::NEG_INFINITY, f32::max)
        .ceil()
        .min(band.height as f32)
        .min(band.end_row as f32) as usize;
    if minx >= maxx || miny >= maxy {
        return;
    }
    let inv_area = area.recip();
    let ax = -(p[2].y - p[1].y) * inv_area;
    let ay = (p[2].x - p[1].x) * inv_area;
    let cx = -(p[1].y - p[0].y) * inv_area;
    let cy = (p[1].x - p[0].x) * inv_area;
    let origin = Vec3::new(minx as f32 + 0.5, miny as f32 + 0.5, 0.);
    let a0 = edge(p[1], p[2], origin) * inv_area;
    let c0 = edge(p[0], p[1], origin) * inv_area;
    let za = p[0].z - p[1].z;
    let zc = p[2].z - p[1].z;
    // 透视校正：屏幕线性插值 (u/w, v/w, 1/w)，逐像素除回。
    // 仿射路径 w 全 1，插值退化为直通（x*1.0 逐位精确）。
    let persp = w[0] != 0.;
    let (iw0, iw1, iw2) = if persp {
        (1. / w[0], 1. / w[1], 1. / w[2])
    } else {
        (1., 1., 1.)
    };
    let iwa = iw0 - iw1;
    let iwc = iw2 - iw1;
    let uw1 = Vec2::new(uv[1].x * iw1, uv[1].y * iw1);
    let tuw_a = Vec2::new(uv[0].x * iw0 - uw1.x, uv[0].y * iw0 - uw1.y);
    let tuw_c = Vec2::new(uv[2].x * iw2 - uw1.x, uv[2].y * iw2 - uw1.y);
    // 定点、半开覆盖：共享边恰有一个归属者，外层斜边不重复混合。
    let points = p.map(|v| {
        (
            (v.x as f64 * 256.).round() as i64,
            (v.y as f64 * 256.).round() as i64,
        )
    });
    let edges = [(0, 1), (1, 2), (2, 0)].map(|(i, j)| {
        let (s, e) = if area > 0. {
            (points[i], points[j])
        } else {
            (points[j], points[i])
        };
        let dx = e.0 - s.0;
        let dy = e.1 - s.1;
        let bias = if dy < 0 || (dy == 0 && dx > 0) { 0 } else { -1 };
        (
            dx,
            dy,
            dx * (miny as i64 * 256 + 128 - s.1) - dy * (minx as i64 * 256 + 128 - s.0) + bias,
        )
    });
    // 逐像素 x 步进的覆盖增量（dy·256 为移位常量乘），纯整数增量免乘法
    let steps = edges.map(|(_, dy, _)| dy * 256);
    let tex_w = tex.width as usize;
    for y in miny..maxy {
        let row = (y - band.start_row) * band.width;
        let depth_row = &mut band.depth[row + minx..row + maxx];
        let pixel_row = &mut band.pixels[(row + minx) * 4..(row + maxx) * 4];
        let mut instances_row = band
            .instances
            .as_deref_mut()
            .map(|s| &mut s[row + minx..row + maxx]);
        let step_y = (y - miny) as i64 * 256;
        let mut coverage = edges.map(|(dx, _, start)| start + dx * step_y);
        let row_a = a0 + (y - miny) as f32 * ay;
        let row_c = c0 + (y - miny) as f32 * cy;
        // 行级写入区间，行末并回包围盒，免逐像素四次 min/max
        let mut wrote = false;
        let mut row_lo = 0usize;
        let mut row_hi = 0usize;
        for (i, (depth, px)) in depth_row
            .iter_mut()
            .zip(pixel_row.chunks_exact_mut(4))
            .enumerate()
        {
            let covered = coverage[0] >= 0 && coverage[1] >= 0 && coverage[2] >= 0;
            coverage = [
                coverage[0] - steps[0],
                coverage[1] - steps[1],
                coverage[2] - steps[2],
            ];
            if !covered {
                continue;
            }
            let a = row_a + i as f32 * ax;
            let c = row_c + i as f32 * cx;
            let z = p[1].z + za * a + zc * c;
            if z >= *depth {
                continue;
            }
            let inv_iw = 1. / (iw1 + iwa * a + iwc * c);
            let tx = (uw1.x + tuw_a.x * a + tuw_c.x * c) * inv_iw;
            let ty = (uw1.y + tuw_a.y * a + tuw_c.y * c) * inv_iw;

            let xx = (tx as u32).min(tex.width - 1) as usize;
            let yy = (ty as u32).min(tex.height - 1) as usize;
            let base = tex.pixels[yy * tex_w + xx];
            if base[3] < 16 {
                continue;
            }
            let alpha = base[3];
            if alpha == 255 {
                px.copy_from_slice(&[
                    lut[0][base[0] as usize],
                    lut[1][base[1] as usize],
                    lut[2][base[2] as usize],
                    255,
                ]);
            } else {
                let aa = alpha as u32;
                let inv = 255 - aa;
                for ch in 0..3 {
                    px[ch] = ((u32::from(lut[ch][base[ch] as usize]) * aa
                        + u32::from(px[ch]) * inv
                        + 127)
                        / 255) as u8;
                }
                px[3] = (aa + (u32::from(px[3]) * inv + 127) / 255) as u8;
            }
            *depth = z;
            if let Some(inst) = instances_row.as_deref_mut() {
                inst[i] = instance;
            }
            if !wrote {
                row_lo = i;
                wrote = true;
            }
            row_hi = i;
        }
        if wrote {
            band.bounds[0] = band.bounds[0].min((minx + row_lo) as i32);
            band.bounds[1] = band.bounds[1].min(y as i32);
            band.bounds[2] = band.bounds[2].max((minx + row_hi + 1) as i32);
            band.bounds[3] = band.bounds[3].max(y as i32 + 1);
        }
    }
}

impl Frame {
    /// 降采样/缩放并输出预乘 RGBA 到调用方缓冲。
    /// 帧大于目标时盒式平均（预乘空间直接平均即抗锯齿），
    /// 帧小于目标时最近邻放大。
    pub fn present_into(&mut self, out: &mut [u8], w: usize, h: usize) {
        debug_assert_eq!(out.len(), w * h * 4);
        if self.width == w && self.height == h {
            // 内部即 RGBA，同尺寸直出为整幅拷贝
            out.copy_from_slice(&self.pixels);
        } else if self.width > w && self.height > h {
            let threads = raster_threads(w, h);
            let src_width = self.width;
            if self.width == w * 2 && self.height == h * 2 {
                // 精确 2×2 降采样：每个源像素权重恰为 256/256，与通用权重路径
                // 等价，直接四像素求和免去逐权重乘加。
                let src = self.pixels.as_slice();
                parallel_downsample(out, w, h, threads, |out, y0| {
                    downsample_exact2(src, src_width, out, w, y0)
                });
            } else {
                if self.resample.as_ref().map(|r| r.source) != Some((self.width, self.height)) {
                    self.resample = Some(Resample {
                        source: (self.width, self.height),
                        columns: box_filter(self.width, w),
                        rows: box_filter(self.height, h),
                    });
                }
                let src = self.pixels.as_slice();
                let resample = self.resample.as_ref().unwrap();
                parallel_downsample(out, w, h, threads, |out, y0| {
                    downsample_weighted(src, src_width, resample, out, w, y0)
                });
            }
        } else {
            let mut last_sy = usize::MAX;
            for y in 0..h {
                let sy = y * self.height / h;
                let dst_off = y * w * 4;
                if sy == last_sy {
                    out.copy_within(dst_off - w * 4..dst_off, dst_off);
                } else {
                    last_sy = sy;
                    for x in 0..w {
                        let sx = x * self.width / w;
                        let src = (sy * self.width + sx) * 4;
                        out[dst_off + x * 4..dst_off + x * 4 + 4]
                            .copy_from_slice(&self.pixels[src..src + 4]);
                    }
                }
            }
        }
    }
}

/// 按行区间把降采样任务交常驻线程池并行执行；区间互不重叠，
/// 各行结果只依赖自身行号。
fn parallel_downsample(
    out: &mut [u8],
    w: usize,
    h: usize,
    threads: usize,
    draw: impl Fn(&mut [u8], usize) + Sync,
) {
    if threads <= 1 {
        draw(out, 0);
        return;
    }
    let rows_per_band = h.div_ceil(threads);
    let mut present_jobs: Vec<PresentJob> = Vec::with_capacity(threads);
    let mut start = 0;
    while start < h {
        let rows = (start + rows_per_band).min(h) - start;
        present_jobs.push(PresentJob {
            draw: &draw,
            out: out.as_mut_ptr(),
            w,
            y0: start,
            rows,
        });
        start += rows;
    }
    let jobs: Vec<pool::Job> = present_jobs
        .iter_mut()
        .map(|job| pool::Job {
            run: run_present_job,
            arg: (job as *mut PresentJob).cast(),
        })
        .collect();
    pool::run_jobs(&jobs, threads - 1);
}

/// 并行降采样任务。
/// SAFETY: `draw` 指向提交方栈上的闭包，行区间互不重叠，
/// 全部引用由 `pool::run_jobs` 的完成等待约束在运行期内。
struct PresentJob<'a> {
    draw: &'a (dyn Fn(&mut [u8], usize) + Sync),
    out: *mut u8,
    w: usize,
    y0: usize,
    rows: usize,
}

unsafe fn run_present_job(arg: *mut ()) {
    let job = &*(arg as *const PresentJob);
    // SAFETY: 行区间由提交方划分互不重叠，out 覆盖整个目标缓冲。
    let chunk =
        std::slice::from_raw_parts_mut(job.out.add(job.y0 * job.w * 4), job.rows * job.w * 4);
    (job.draw)(chunk, job.y0);
}

/// 目标第 [y0, y0 + 行数) 行的精确 2×2 盒式平均。
fn downsample_exact2(pixels: &[u8], src_width: usize, out: &mut [u8], w: usize, y0: usize) {
    let rows = out.len() / (w * 4);
    for row in 0..rows {
        let y = y0 + row;
        let src_row = (y * 2 * src_width) * 4;
        for x in 0..w {
            let l = x * 8;
            let out_off = (row * w + x) * 4;
            for ch in 0..4 {
                let sum = u32::from(pixels[src_row + l + ch])
                    + u32::from(pixels[src_row + l + 4 + ch])
                    + u32::from(pixels[src_row + src_width * 4 + l + ch])
                    + u32::from(pixels[src_row + src_width * 4 + l + 4 + ch]);
                out[out_off + ch] = ((sum + 2) >> 2) as u8;
            }
        }
    }
}

/// 目标第 [y0, y0 + 行数) 行的权重盒式平均。
fn downsample_weighted(
    pixels: &[u8],
    src_width: usize,
    resample: &Resample,
    out: &mut [u8],
    w: usize,
    y0: usize,
) {
    let rows = out.len() / (w * 4);
    for (row, weights) in resample.rows[y0..y0 + rows].iter().enumerate() {
        for (x, col) in resample.columns.iter().enumerate() {
            let mut acc = [0u32; 4];
            let mut total = 0u32;
            for (dy, &wy) in weights.factors.iter().enumerate() {
                let src = pixels[(weights.start + dy) * src_width * 4 + col.start * 4..].as_ptr();
                for (dx, &wx) in col.factors.iter().enumerate() {
                    let weight = wx * wy;
                    total += weight;
                    let px = unsafe { src.add(dx * 4) };
                    for ch in 0..4 {
                        acc[ch] += unsafe { *px.add(ch) } as u32 * weight;
                    }
                }
            }
            let out_off = (row * w + x) * 4;
            out[out_off..out_off + 4].copy_from_slice(&[
                ((acc[0] + total / 2) / total) as u8,
                ((acc[1] + total / 2) / total) as u8,
                ((acc[2] + total / 2) / total) as u8,
                ((acc[3] + total / 2) / total) as u8,
            ]);
        }
    }
}

fn edge(a: Vec3, b: Vec3, p: Vec3) -> f32 {
    (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)
}

struct Weights {
    start: usize,
    factors: Vec<u32>,
}

/// 定点（256/像素）盒式滤波权重表：每个目标像素的源覆盖恰好铺满一次。
fn box_filter(src: usize, dst: usize) -> Vec<Weights> {
    (0..dst)
        .map(|i| {
            let left = i as u64 * src as u64 * 256 / dst as u64;
            let right = (i as u64 + 1) * src as u64 * 256 / dst as u64;
            let start = (left / 256) as usize;
            let end = right.div_ceil(256) as usize;
            let factors = (start..end)
                .map(|j| (((j as u64 + 1) * 256).min(right) - (j as u64 * 256).max(left)) as u32)
                .collect();
            Weights { start, factors }
        })
        .collect()
}

struct Resample {
    source: (usize, usize),
    columns: Vec<Weights>,
    rows: Vec<Weights>,
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Cursor;

    #[test]
    fn box_filter_tiles_the_source_exactly_once() {
        for (src, dst) in [(900usize, 360usize), (630, 420), (540, 270), (3, 7)] {
            let filter = box_filter(src, dst);
            assert_eq!(filter.len(), dst);
            let mut covered = vec![0u64; src];
            for entry in &filter {
                assert!(entry.start < src);
                for (j, &factor) in entry.factors.iter().enumerate() {
                    assert!(factor > 0);
                    covered[entry.start + j] += factor as u64;
                }
            }
            assert!(
                covered.iter().all(|&c| c == 256),
                "{src}->{dst}: {covered:?}"
            );
        }
    }

    #[test]
    fn downsample_averages_premultiplied_colors() {
        let mut frame = Frame::new(4, 4);
        // 两行不透明红（预乘 RGBA），两行半透明红。
        for y in 0..4 {
            for x in 0..4 {
                let off = (y * 4 + x) * 4;
                let v = if y < 2 {
                    [255, 0, 0, 255]
                } else {
                    [128, 0, 0, 128]
                };
                frame.pixels[off..off + 4].copy_from_slice(&v);
            }
        }
        let mut out = vec![0u8; 2 * 2 * 4];
        frame.present_into(&mut out, 2, 2);
        // 上行 = 不透明红；下行 = 一半透明 → 预乘减半。
        assert_eq!(&out[0..4], &[255, 0, 0, 255]);
        assert_eq!(&out[4..8], &[255, 0, 0, 255]);
        assert_eq!(&out[8..12], &[128, 0, 0, 128]);
    }

    #[test]
    fn downsample_exact_double_matches_weighted_reference() {
        let mut frame = Frame::new(4, 4);
        for (i, px) in frame.pixels.chunks_exact_mut(4).enumerate() {
            px.copy_from_slice(&[
                (i * 53 % 256) as u8,
                (i * 97 % 256) as u8,
                (i * 29 % 256) as u8,
                (i * 11 % 255) as u8,
            ]);
        }
        let mut out = vec![0u8; 2 * 2 * 4];
        frame.present_into(&mut out, 2, 2);
        // 参考值：按通用盒式滤波权重（每源像素 256/256）逐通道独立计算。
        for y in 0..2 {
            for x in 0..2 {
                let mut expected = [0u8; 4];
                for ch in 0..4 {
                    let mut acc = 0u32;
                    for dy in 0..2 {
                        for dx in 0..2 {
                            let si = ((y * 2 + dy) * 4 + x * 2 + dx) * 4 + ch;
                            acc += u32::from(frame.pixels[si]) * 65536;
                        }
                    }
                    expected[ch] = ((acc + 131072) / 262144) as u8;
                }
                let off = (y * 2 + x) * 4;
                assert_eq!(&out[off..off + 4], &expected, "pixel ({x},{y})");
            }
        }
    }

    /// 矩形清屏的不变量：零写入的一帧必须不留任何上一帧痕迹。
    #[test]
    fn draw_clears_everything_left_by_the_previous_frame() {
        let mut img = image::RgbaImage::new(64, 64);
        for p in img.pixels_mut() {
            *p = image::Rgba([255, 0, 0, 255]);
        }
        let mut png = Vec::new();
        img.write_to(&mut Cursor::new(&mut png), image::ImageFormat::Png)
            .unwrap();
        let light = Lighting::new(&crate::api::LightingParams::default());

        let mut frame = Frame::new(8, 8);
        // 人为留下上一帧的写入痕迹与深度
        for y in 1..3 {
            for x in 2..5 {
                let off = (y * 8 + x) * 4;
                frame.pixels[off..off + 4].copy_from_slice(&[9, 9, 9, 255]);
                frame.depth[y * 8 + x] = 1.;
            }
        }
        frame.bounds = [2, 1, 5, 3];
        frame.draw(&[], &[], &light, false);
        assert!(frame.pixels.iter().all(|&v| v == 0));
        assert!(frame.depth.iter().all(|d| d.is_infinite()));
    }

    /// 并行条带与单线程必须产出逐字节一致的帧。
    #[test]
    fn parallel_raster_matches_serial_output() {
        use std::sync::atomic::Ordering;

        fn skin_png() -> Vec<u8> {
            let mut img = image::RgbaImage::new(64, 64);
            for p in img.pixels_mut() {
                *p = image::Rgba([120, 90, 60, 255]);
            }
            let mut png = Vec::new();
            img.write_to(&mut Cursor::new(&mut png), image::ImageFormat::Png)
                .unwrap();
            png
        }

        fn stream(threads: usize) -> Vec<u8> {
            TEST_THREADS.store(threads, Ordering::Relaxed);
            let mut model = crate::api::PetModel::new(&skin_png(), None).expect("model");
            model.play_animation(crate::animation::AnimationId::Walking, 1., true);
            let (w, h) = (150usize, 170usize);
            let mut out = vec![0u8; w * h * 4];
            for i in 0..40 {
                model
                    .render(w, h, i as f32 / 60., 1. / 60., &mut out)
                    .unwrap();
            }
            out
        }

        let serial = stream(1);
        let parallel = stream(4);
        TEST_THREADS.store(0, Ordering::Relaxed);
        assert_eq!(serial, parallel);
    }
}
