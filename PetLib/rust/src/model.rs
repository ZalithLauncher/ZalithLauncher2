//! 模型运行时数据：glTF 文档到渲染/姿态层的投影，按内容缓存共享。
//!
//! 内置模型以 GLB 形式嵌入库内；自定义模型加载复用同一份数据结构。
//! 命名约定（模型契约）：
//! - 关节名 Body/Head/Left_Arm/Right_Arm/Left_Leg/Right_Leg/Cape
//!   供程序化姿态（注视/摆臂/压扁/行走）按名查找，缺失则跳过对应效果
//! - 网格节点名以 "_Layer" 结尾的归入外层绘制序（半透明后绘）
//! - 材质名含 "cape"（不分大小写）绑定披风纹理，其余绑定皮肤；
//!   道具模型按材质绑定的内嵌纹理绘制
//! - 动画名 idle（基础待机）、idle_sub_*（随机子动画）、interact（点击反应）

use std::collections::HashMap;
#[cfg(not(target_arch = "wasm32"))]
use std::sync::{Mutex, OnceLock};
use std::sync::Arc;

use glam::{Quat, Vec3};

use crate::gltf::{self, Times};
use crate::skin::{ArmModel, Texture};

static BUILTIN_CLASSIC: &[u8] = include_bytes!("assets/classic-player.glb");
static BUILTIN_SLIM: &[u8] = include_bytes!("assets/slim-player.glb");

/// glTF 单位 → 模型像素。一个世界单位对应原版模型的 16 像素。
pub(crate) const PIXELS_PER_UNIT: f32 = 16.;

/// 解析后的节点：局部静止 TRS + 树结构。
#[derive(Debug)]
pub(crate) struct RuntimeNode {
    pub(crate) name: Option<String>,
    pub(crate) parent: Option<usize>,
    pub(crate) children: Vec<usize>,
    pub(crate) rest_translation: Vec3,
    pub(crate) rest_rotation: Quat,
    pub(crate) rest_scale: Vec3,
}

/// 绘制项：某节点下的一个网格 primitive，按材质索引解析纹理。
#[derive(Debug)]
pub(crate) struct DrawItem {
    pub(crate) node: usize,
    pub(crate) mesh: usize,
    pub(crate) primitive: usize,
    /// 材质索引（ModelData.materials 下标）
    pub(crate) material: usize,
    /// 外层（半透明后绘组），按网格节点名 "_Layer" 后缀判定
    pub(crate) layer: bool,
    /// 包围盒中心（像素，primitive 局部空间）
    pub(crate) center: Vec3,
    /// 包围盒尺寸（像素）
    pub(crate) extents: Vec3,
}

/// 材质运行时数据：名字供皮肤/披风映射，内嵌纹理供道具模型。
#[derive(Debug)]
pub(crate) struct MaterialData {
    pub(crate) name: Option<String>,
    /// 内嵌基色纹理（解码后）；外部引用或未绑定的材质为 None
    pub(crate) embedded: Option<Arc<Texture>>,
}

/// 运行时动画剪辑：节点索引 → 旋转/平移通道。
#[derive(Clone, Debug)]
pub(crate) struct RuntimeClip {
    pub(crate) name: String,
    pub(crate) channels: Vec<(usize, RuntimeChannel)>,
    /// 加载时计算，避免动画调度和手动剪辑每帧重复扫描全部通道。
    pub(crate) duration: f32,
}

impl RuntimeClip {
    fn new(name: String, channels: Vec<(usize, RuntimeChannel)>) -> Self {
        let mut duration = 0.0f32;
        for (_, channel) in &channels {
            if let Some(times) = &channel.rotation_times {
                duration = duration.max(times.duration(channel.rotation.len()));
            }
            if let Some(times) = &channel.translation_times {
                duration = duration.max(times.duration(channel.translation.len()));
            }
        }
        Self { name, channels, duration }
    }
}

#[derive(Clone, Debug)]
pub(crate) struct RuntimeChannel {
    pub(crate) rotation: Vec<[f32; 4]>,
    /// 与 rotation 键一一对应；键为空时为 None
    pub(crate) rotation_times: Option<Times>,
    pub(crate) translation: Vec<[f32; 3]>,
    /// 与 translation 键一一对应；键为空时为 None
    pub(crate) translation_times: Option<Times>,
}

pub(crate) struct ModelData {
    pub(crate) nodes: Vec<RuntimeNode>,
    /// 树先序遍历的绘制序列
    pub(crate) draw_list: Vec<DrawItem>,
    /// 材质表（与 gltf::Material 对齐，供 draw_list 材质索引引用）
    pub(crate) materials: Vec<MaterialData>,
    pub(crate) clips: Vec<RuntimeClip>,
    clip_index: HashMap<String, usize>,
    joint_index: HashMap<String, usize>,
    /// 网格几何（与 gltf::Mesh 对齐，供 draw_list 引用）
    pub(crate) meshes: Vec<gltf::Mesh>,
    /// 姿态层驱动的节点（具名关节 ∪ 有动画通道的节点）：
    /// 渲染端对驱动节点取姿态值，未驱动节点回退 glTF 静止 TRS。
    pub(crate) driven: Vec<bool>,
}

impl ModelData {
    fn parse(bytes: &[u8]) -> Result<Self, String> {
        let doc = gltf::parse(bytes)?;
        let mut nodes: Vec<RuntimeNode> = doc
            .nodes
            .iter()
            .map(|n| RuntimeNode {
                name: n.name.clone(),
                parent: None,
                children: n.children.clone(),
                rest_translation: Vec3::from(n.translation) * PIXELS_PER_UNIT,
                rest_rotation: Quat::from_xyzw(
                    n.rotation[0], n.rotation[1], n.rotation[2], n.rotation[3],
                ),
                rest_scale: Vec3::from(n.scale),
            })
            .collect();
        // 父索引解析：先收集边，避免双可变借用
        let mut parents: Vec<Option<usize>> = vec![None; nodes.len()];
        for (index, node) in nodes.iter().enumerate() {
            for &child in &node.children {
                let slot = parents.get_mut(child).ok_or_else(|| {
                    format!("node {child} out of bounds (parent {index})")
                })?;
                if slot.is_some() {
                    return Err(format!("node {child} has multiple parents"));
                }
                *slot = Some(index);
            }
        }
        for (node, parent) in nodes.iter_mut().zip(parents) {
            node.parent = parent;
        }

        // 网格顶点换算到像素域（draw_list 的包围盒与渲染均依赖）
        let mut meshes = doc.meshes;
        for mesh in &mut meshes {
            for primitive in &mut mesh.primitives {
                for v in primitive.positions.iter_mut() {
                    *v = [
                        v[0] * PIXELS_PER_UNIT,
                        v[1] * PIXELS_PER_UNIT,
                        v[2] * PIXELS_PER_UNIT,
                    ];
                }
            }
        }

        // 材质表：解码内嵌基色纹理；外部引用留给皮肤/披风运行时替换
        let mut materials = Vec::with_capacity(doc.materials.len());
        for material in &doc.materials {
            let embedded = match material
                .base_color_texture
                .and_then(|t| doc.textures.get(t).copied())
                .and_then(|img| doc.images.get(img))
            {
                Some(gltf::ImageSource::Data(bytes)) => {
                    Some(Arc::new(Texture::decode_any(bytes)?))
                }
                _ => None,
            };
            materials.push(MaterialData {
                name: material.name.clone(),
                embedded,
            });
        }

        // 名字索引
        let mut joint_index = HashMap::new();
        for (index, node) in nodes.iter().enumerate() {
            if let Some(name) = &node.name {
                joint_index.insert(name.clone(), index);
            }
        }

        // 绘制序：从根先序遍历（与 glTF 场景顺序一致）
        let mut draw_list = Vec::new();
        let mut stack: Vec<usize> = doc.roots.iter().rev().copied().collect();
        while let Some(node) = stack.pop() {
            if let Some(mesh) = doc.nodes.get(node).and_then(|n| n.mesh) {
                for (primitive, prim) in meshes[mesh].primitives.iter().enumerate() {
                    let layer = nodes[node]
                        .name
                        .as_deref()
                        .is_some_and(|n| n.ends_with("_Layer"));
                    // 顶点已换算像素：包围盒中心/尺寸供排序与调试
                    let mut min = glam::Vec3::splat(f32::MAX);
                    let mut max = glam::Vec3::splat(f32::MIN);
                    for v in &prim.positions {
                        let v = glam::Vec3::from(*v);
                        min = min.min(v);
                        max = max.max(v);
                    }
                    let extents = max - min;
                    draw_list.push(DrawItem {
                        node,
                        mesh,
                        primitive,
                        material: prim.material,
                        layer,
                        center: (min + max) * 0.5,
                        extents,
                    });
                }
            }
            stack.extend(nodes[node].children.iter().rev().copied());
        }

        let mut clips = Vec::new();
        let mut clip_index = HashMap::new();
        for clip in &doc.animations {
            let channels = clip
                .channels
                .iter()
                .map(|c| {
                    (
                        c.node,
                        RuntimeChannel {
                            rotation: c.rotation.clone(),
                            rotation_times: c.rotation_times.clone(),
                            // 平移键换算到像素域（旋转为无量纲四元数，不动）
                            translation: c
                                .translation
                                .iter()
                                .map(|k| (glam::Vec3::from(*k) * PIXELS_PER_UNIT).to_array())
                                .collect(),
                            translation_times: c.translation_times.clone(),
                        },
                    )
                })
                .collect();
            clip_index.insert(clip.name.clone(), clips.len());
            clips.push(RuntimeClip::new(clip.name.clone(), channels));
        }

        // 驱动节点 = 契约名关节 ∪ 任意剪辑通道的目标节点
        let mut driven = vec![false; nodes.len()];
        for name in [
            "Body",
            "Head",
            "Left_Arm",
            "Right_Arm",
            "Left_Leg",
            "Right_Leg",
            "Cape",
        ] {
            if let Some(&i) = joint_index.get(name) {
                driven[i] = true;
            }
        }
        for clip in &doc.animations {
            for channel in &clip.channels {
                if let Some(slot) = driven.get_mut(channel.node) {
                    *slot = true;
                }
            }
        }

        Ok(Self {
            nodes,
            draw_list,
            materials,
            clips,
            clip_index,
            joint_index,
            meshes,
            driven,
        })
    }

    /// 按名查剪辑。
    pub(crate) fn clip(&self, name: &str) -> Option<&RuntimeClip> {
        self.clip_index.get(name).map(|&i| &self.clips[i])
    }

    /// 按名查剪辑索引。
    pub(crate) fn clip_index_of(&self, name: &str) -> Option<usize> {
        self.clip_index.get(name).copied()
    }

    /// 按前缀收集剪辑索引（按文档顺序，保证确定性），供 idle_sub_* 随机子动画。
    pub(crate) fn clips_with_prefix(&self, prefix: &str) -> Vec<usize> {
        self.clips
            .iter()
            .enumerate()
            .filter(|(_, clip)| clip.name.starts_with(prefix))
            .map(|(i, _)| i)
            .collect()
    }

    /// 按名查关节节点索引。
    pub(crate) fn joint(&self, name: &str) -> Option<usize> {
        self.joint_index.get(name).copied()
    }

    pub(crate) fn primitive(&self, item: &DrawItem) -> &gltf::Primitive {
        &self.meshes[item.mesh].primitives[item.primitive]
    }
}

/// 材质名含 "cape"（不分大小写）判定披风绑定。
pub(crate) fn material_is_cape(name: Option<&str>) -> bool {
    name.is_some_and(|n| n.to_ascii_lowercase().contains("cape"))
}

/// 解析缓存：键为模型字节内容（GLB），同一文件的所有实例共享一份解析数据。
/// 多线程目标（Android）用 Mutex；wasm 单线程用无锁静态，
/// panic=abort 不留卡死状态。
#[cfg(target_arch = "wasm32")]
static mut CACHE_MAP: Option<HashMap<Arc<[u8]>, Arc<ModelData>>> = None;
#[cfg(not(target_arch = "wasm32"))]
static CACHE: OnceLock<Mutex<HashMap<Arc<[u8]>, Arc<ModelData>>>> = OnceLock::new();

/// 解析模型字节（GLB），按内容缓存共享。
pub(crate) fn cached(bytes: &[u8]) -> Result<Arc<ModelData>, String> {
    #[cfg(target_arch = "wasm32")]
    {
        // SAFETY: wasm 单线程，静态可变访问无并发；panic=abort 不留卡死状态。
        let map = unsafe { &mut *std::ptr::addr_of_mut!(CACHE_MAP) };
        let map = map.get_or_insert_with(HashMap::new);
        if let Some(data) = map.get(bytes) {
            return Ok(Arc::clone(data));
        }
        let data = Arc::new(ModelData::parse(bytes)?);
        map.insert(Arc::from(bytes), Arc::clone(&data));
        Ok(data)
    }
    #[cfg(not(target_arch = "wasm32"))]
    {
        let cache = CACHE.get_or_init(|| Mutex::new(HashMap::new()));
        // 缓存可恢复：即使前次解析 panic 毒化锁也继续用现有数据
        let mut guard = cache
            .lock()
            .unwrap_or_else(|poisoned| poisoned.into_inner());
        if let Some(data) = guard.get(bytes) {
            return Ok(Arc::clone(data));
        }
        let data = Arc::new(ModelData::parse(bytes)?);
        guard.insert(Arc::from(bytes), Arc::clone(&data));
        Ok(data)
    }
}

/// 取内置模型数据（首次调用解析，其后共享）。
pub(crate) fn builtin_data(arm: ArmModel) -> Result<Arc<ModelData>, String> {
    match arm {
        ArmModel::Classic => cached(BUILTIN_CLASSIC),
        ArmModel::Slim => cached(BUILTIN_SLIM),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn clip_duration_uses_each_channels_own_key_count() {
        let clip = RuntimeClip::new(
            "mixed".to_owned(),
            vec![(
                0,
                RuntimeChannel {
                    rotation: vec![[0., 0., 0., 1.]; 2],
                    rotation_times: Some(Times::Uniform(10.)),
                    translation: vec![[0.; 3]; 21],
                    translation_times: Some(Times::General(vec![0., 0.05])),
                },
            )],
        );
        assert!((clip.duration - 0.1).abs() < 1e-6);
    }

    #[test]
    fn builtin_models_parse_and_cache() {
        let classic = builtin_data(ArmModel::Classic).unwrap();
        let again = builtin_data(ArmModel::Classic).unwrap();
        assert!(Arc::ptr_eq(&classic, &again), "cache should return the same instance");
        let slim = builtin_data(ArmModel::Slim).unwrap();
        assert!(!Arc::ptr_eq(&classic, &slim), "two arm models should cache separately");

        // 内置模型契约：具名关节齐全
        for name in ["Body", "Head", "Left_Arm", "Right_Arm", "Left_Leg", "Right_Leg", "Cape"] {
            assert!(classic.joint(name).is_some(), "missing joint {name}");
        }
        // 具名剪辑齐全
        assert!(classic.clip("idle").is_some());
        assert!(classic.clip("interact").is_some());
        let subs = classic.clips_with_prefix("idle_sub_");
        assert_eq!(subs.len(), 3, "should have three random sub-clips");
        // 子动画顺序按文档序确定（调度器的种子回放依赖此顺序）
        let names: Vec<&str> = subs
            .iter()
            .map(|&i| classic.clips[i].name.as_str())
            .collect();
        assert_eq!(names, ["idle_sub_1", "idle_sub_2", "idle_sub_3"]);
    }

    #[test]
    fn draw_list_walks_tree_with_material_mapping() {
        let data = builtin_data(ArmModel::Classic).unwrap();
        // 13 个 primitive（12 皮肤 + 1 披风）
        assert_eq!(data.draw_list.len(), 13);
        let capes = data
            .draw_list
            .iter()
            .filter(|item| {
                material_is_cape(data.materials[item.material].name.as_deref())
            })
            .count();
        assert_eq!(capes, 1, "expected exactly one cape primitive");
        // 外层组：6 个 Layer 网格（帽/身/双臂/双腿外层）
        let layers = data.draw_list.iter().filter(|i| i.layer).count();
        assert_eq!(layers, 6);
        // 先序：首项是头部基础网格（树中第一个带网格节点为 Head_2）
        assert!(!data.draw_list[0].layer);
        assert_eq!(
            data.nodes[data.draw_list[0].node].name.as_deref(),
            Some("Head_2")
        );
    }

    #[test]
    fn prop_model_loads_embedded_textures() {
        let data = crate::gltf::test_fixture::prop_glb();
        let data = cached(&data).unwrap();
        assert_eq!(data.materials.len(), 1);
        let embedded = data.materials[0]
            .embedded
            .as_ref()
            .expect("prop material should have an embedded texture");
        assert_eq!((embedded.width, embedded.height), (2, 2));
    }

    #[test]
    fn identical_content_shares_cached_parse() {
        let bytes = crate::gltf::test_fixture::prop_glb();
        let a = cached(&bytes).unwrap();
        let b = cached(&bytes).unwrap();
        assert!(Arc::ptr_eq(&a, &b), "same content should hit the same cached instance");
    }

    #[test]
    fn node_tree_parents_resolved() {
        let data = builtin_data(ArmModel::Classic).unwrap();
        let arm = data.joint("Right_Arm").unwrap();
        let body = data.joint("Body").unwrap();
        assert_eq!(data.nodes[arm].parent, Some(body), "arm parent should be the body");
        // 静止平移按 1 单位 = 16 像素换算：肩点 (−5, 10, 0) 像素
        let left = data.joint("Left_Arm").unwrap();
        assert!(data.nodes[left].rest_translation.abs_diff_eq(Vec3::new(-5., 10., 0.), 1e-4));
    }

    #[test]
    fn classic_and_slim_share_node_structure() {
        // 骨架绑定一份模型数据；臂型切换仅替换几何侧。
        let classic = builtin_data(ArmModel::Classic).unwrap();
        let slim = builtin_data(ArmModel::Slim).unwrap();
        assert_eq!(classic.nodes.len(), slim.nodes.len());
        // 只比较驱动节点与网格节点：编辑器辅助节点（无网格、不驱动）
        // 在两份文件间本就不同，且与渲染无关。
        for (index, (a, b)) in classic.nodes.iter().zip(&slim.nodes).enumerate() {
            assert_eq!(a.name, b.name);
            assert_eq!(a.parent, b.parent);
            let is_mesh_node = classic.draw_list.iter().any(|i| i.node == index);
            if classic.driven[index] || is_mesh_node {
                assert!(a.rest_translation.abs_diff_eq(b.rest_translation, 1e-3));
            }
        }
        assert_eq!(classic.draw_list.len(), slim.draw_list.len());
    }
}
