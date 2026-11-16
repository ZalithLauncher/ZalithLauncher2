//! glTF 2.0 子集解析：GLB 二进制容器与内嵌 data URI 的 .gltf。
//!
//! 仅支持项目模型契约要求的子集：
//! - 单 buffer（GLB BIN chunk 或 data URI）
//! - TRS 节点树（不接受 matrix）
//! - 三角形网格 primitive（POSITION/NORMAL/TEXCOORD_0，f32；索引 u16/u32）
//! - 材质名（供皮肤/披风映射），不解析 PBR 参数
//! - 动画 rotation/translation 通道，LINEAR 插值，标量时间轴

use serde_json::Value;

/// 动画时间轴：均匀网格（帧率恒定，O(1) 采样）或一般时间点（对分查找）。
#[derive(Clone, Debug)]
pub(crate) enum Times {
    /// 均匀网格：每秒帧数
    Uniform(f32),
    /// 任意非均匀时间点（秒，单调不减）
    General(Vec<f32>),
}

impl Times {
    /// 时间轴时长：均匀网格由键数与采样率推算，一般时间轴取末键。
    pub(crate) fn duration(&self, keys: usize) -> f32 {
        match self {
            Self::Uniform(fps) => (keys.max(1) - 1) as f32 / fps,
            Self::General(times) => times.last().copied().unwrap_or(0.),
        }
    }
}

#[derive(Clone, Debug, Default)]
pub(crate) struct Node {
    pub(crate) name: Option<String>,
    pub(crate) children: Vec<usize>,
    /// 局部平移（glTF 单位）
    pub(crate) translation: [f32; 3],
    /// 局部旋转 (x, y, z, w)
    pub(crate) rotation: [f32; 4],
    /// 局部缩放
    pub(crate) scale: [f32; 3],
    /// 网格索引（document.meshes 下标）
    pub(crate) mesh: Option<usize>,
}

#[derive(Clone, Debug, Default)]
pub(crate) struct Primitive {
    pub(crate) positions: Vec<[f32; 3]>,
    pub(crate) normals: Vec<[f32; 3]>,
    pub(crate) uvs: Vec<[f32; 2]>,
    pub(crate) indices: Vec<u32>,
    /// 材质索引（document.materials 下标）
    pub(crate) material: usize,
}

#[derive(Clone, Debug, Default)]
pub(crate) struct Mesh {
    pub(crate) primitives: Vec<Primitive>,
}

#[derive(Clone, Debug)]
pub(crate) struct Material {
    pub(crate) name: Option<String>,
    /// 基色纹理索引（document.textures 下标）；未绑定为 None
    pub(crate) base_color_texture: Option<usize>,
}

/// 图片来源：内嵌字节可解码为纹理；外部引用由运行时纹理覆盖（皮肤/披风）。
#[derive(Clone, Debug)]
pub(crate) enum ImageSource {
    Data(Vec<u8>),
    External,
}

#[derive(Clone, Debug)]
pub(crate) struct AnimChannel {
    /// 目标节点索引
    pub(crate) node: usize,
    /// 旋转四元数键 (x, y, z, w)
    pub(crate) rotation: Vec<[f32; 4]>,
    /// 平移键
    pub(crate) translation: Vec<[f32; 3]>,
    /// 旋转通道时间轴；平移通道时间轴
    pub(crate) rotation_times: Option<Times>,
    pub(crate) translation_times: Option<Times>,
}

#[derive(Clone, Debug)]
pub(crate) struct AnimClip {
    pub(crate) name: String,
    pub(crate) channels: Vec<AnimChannel>,
}

pub(crate) struct GltfDocument {
    pub(crate) nodes: Vec<Node>,
    pub(crate) meshes: Vec<Mesh>,
    pub(crate) materials: Vec<Material>,
    /// 纹理表：每项为图片索引（textures[i].source）
    pub(crate) textures: Vec<usize>,
    /// 图片表：内嵌或外部引用
    pub(crate) images: Vec<ImageSource>,
    pub(crate) animations: Vec<AnimClip>,
    /// 场景根节点（scenes[0].nodes）
    pub(crate) roots: Vec<usize>,
}

const COMPONENT_F32: u64 = 5126;
const COMPONENT_U16: u64 = 5123;
const COMPONENT_U32: u64 = 5125;

/// 解析模型字节（GLB 容器或内嵌 .gltf）。
pub(crate) fn parse(bytes: &[u8]) -> Result<GltfDocument, String> {
    if bytes.len() >= 4 && &bytes[..4] == b"glTF" {
        let (json, bin) = parse_glb(bytes)?;
        build_document(&json, Some(&bin))
    } else {
        let json: Value = serde_json::from_slice(bytes)
            .map_err(|e| format!("glTF JSON parse failed: {e}"))?;
        build_document(&json, None)
    }
}

/// 解析 GLB 容器头与两个 chunk，返回 (JSON, BIN)。
fn parse_glb(bytes: &[u8]) -> Result<(Value, Vec<u8>), String> {
    if bytes.len() < 20 {
        return Err("incomplete GLB file".into());
    }
    let (magic, version, total) = (
        u32::from_le_bytes(bytes[0..4].try_into().unwrap()),
        u32::from_le_bytes(bytes[4..8].try_into().unwrap()),
        u32::from_le_bytes(bytes[8..12].try_into().unwrap()),
    );
    if magic != 0x4654_6C67 {
        return Err("invalid GLB magic".into());
    }
    if version != 2 {
        return Err(format!("unsupported GLB version: {version}"));
    }
    let total = total as usize;
    if total > bytes.len() {
        return Err("GLB declared length exceeds file size".into());
    }
    let bytes = &bytes[..total];
    let (json_len, json_type) = (
        u32::from_le_bytes(bytes[12..16].try_into().unwrap()) as usize,
        &bytes[16..20],
    );
    if json_type != b"JSON" {
        return Err("first GLB chunk is not JSON".into());
    }
    if 20 + json_len > bytes.len() {
        return Err("GLB JSON chunk out of bounds".into());
    }
    let json: Value = serde_json::from_slice(&bytes[20..20 + json_len])
        .map_err(|e| format!("GLB JSON parse failed: {e}"))?;

    let mut bin = Vec::new();
    let mut pos = 20 + json_len;
    if pos + 8 <= bytes.len() {
        let (bin_len, bin_type) = (
            u32::from_le_bytes(bytes[pos..pos + 4].try_into().unwrap()) as usize,
            &bytes[pos + 4..pos + 8],
        );
        pos += 8;
        if bin_type != b"BIN\x00" {
            return Err("second GLB chunk is not BIN".into());
        }
        if pos + bin_len > bytes.len() {
            return Err("GLB BIN chunk out of bounds".into());
        }
        bin = bytes[pos..pos + bin_len].to_vec();
    }
    Ok((json, bin))
}

/// 手写 base64 解码（标准字母表，容忍空白）。
fn base64_decode(input: &str) -> Result<Vec<u8>, String> {
    fn value(b: u8) -> Result<u32, String> {
        match b {
            b'A'..=b'Z' => Ok((b - b'A') as u32),
            b'a'..=b'z' => Ok((b - b'a' + 26) as u32),
            b'0'..=b'9' => Ok((b - b'0' + 52) as u32),
            b'+' => Ok(62),
            b'/' => Ok(63),
            _ => Err(format!("invalid base64 character {b:#04x}")),
        }
    }
    let clean: Vec<u8> = input.bytes().filter(|b| !b.is_ascii_whitespace()).collect();
    if clean.len() % 4 != 0 {
        return Err("base64 length not a multiple of 4".into());
    }
    let mut out = Vec::with_capacity(clean.len() * 3 / 4);
    for chunk in clean.chunks_exact(4) {
        let pad = chunk.iter().rev().take_while(|&&b| b == b'=').count();
        if pad > 2 {
            return Err("invalid base64 padding".into());
        }
        let mut acc = 0u32;
        for (slot, &b) in chunk.iter().enumerate() {
            if b == b'=' {
                // 填充位之后不允许再有数据位
                if chunk[slot..].iter().any(|&b| b != b'=') {
                    return Err("invalid base64 padding position".into());
                }
                continue;
            }
            acc |= value(b)? << (18 - slot * 6);
        }
        for shift in [16u32, 8, 0].into_iter().take(3 - pad) {
            out.push((acc >> shift) as u8);
        }
    }
    Ok(out)
}

/// 从 JSON 提取内嵌 buffer：仅支持 data URI，返回其二进制内容。
fn embedded_buffer(json: &Value) -> Result<Vec<u8>, String> {
    let buffers = json
        .get("buffers")
        .and_then(Value::as_array)
        .ok_or("missing buffers")?;
    if buffers.len() != 1 {
        return Err(format!("only a single buffer is supported, got {}", buffers.len()));
    }
    let uri = buffers[0]
        .get("uri")
        .and_then(Value::as_str)
        .ok_or("buffer lacks an embedded data URI (external .bin unsupported)")?;
    if !uri.starts_with("data:") {
        return Err("buffer URI is not an embedded data URI".into());
    }
    let payload = uri.split(',').nth(1).ok_or("data URI missing payload")?;
    base64_decode(payload)
}

/// 构建文档：`bin` 为 GLB BIN chunk；None 时从 JSON data URI 提取。
fn build_document(json: &Value, bin: Option<&[u8]>) -> Result<GltfDocument, String> {
    let owned;
    let bin = match bin {
        Some(b) => b,
        None => {
            owned = embedded_buffer(json)?;
            &owned
        }
    };

    let materials = json["materials"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(|m| Material {
                    name: m["name"].as_str().map(str::to_owned),
                    base_color_texture: m["pbrMetallicRoughness"]["baseColorTexture"]["index"]
                        .as_u64()
                        .map(|i| i as usize),
                })
                .collect()
        })
        .unwrap_or_default();

    let images = json["images"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(|img| {
                    if let Some(view) = img["bufferView"].as_u64() {
                        Ok(ImageSource::Data(
                            read_buffer_view(json, bin, view as usize)?.to_vec(),
                        ))
                    } else if let Some(uri) = img["uri"].as_str() {
                        if !uri.starts_with("data:") {
                            // 外部图片不可自包含解析；玩家模型由皮肤/披风替换，
                            // 道具模型在加载入口按需拒绝。
                            return Ok(ImageSource::External);
                        }
                        let payload = uri.split(',').nth(1).ok_or("image data URI missing payload")?;
                        Ok(ImageSource::Data(base64_decode(payload)?))
                    } else {
                        Err("image lacks bufferView or embedded URI".into())
                    }
                })
                .collect::<Result<Vec<_>, String>>()
        })
        .unwrap_or_else(|| Ok(Vec::new()))?;

    let textures = json["textures"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(|t| {
                    t["source"]
                        .as_u64()
                        .map(|i| i as usize)
                        .ok_or_else(|| "texture lacks source (image index)".to_owned())
                })
                .collect::<Result<Vec<_>, String>>()
        })
        .unwrap_or_else(|| Ok(Vec::new()))?;

    let meshes = json["meshes"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(|m| {
                    let primitives = m["primitives"]
                        .as_array()
                        .ok_or("mesh lacks primitives")?
                        .iter()
                        .map(|p| build_primitive(json, bin, p))
                        .collect::<Result<Vec<_>, String>>()?;
                    Ok(Mesh { primitives })
                })
                .collect::<Result<Vec<_>, String>>()
        })
        .unwrap_or_else(|| Ok(Vec::new()))?;

    let nodes = json["nodes"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(build_node)
                .collect::<Result<Vec<_>, String>>()
        })
        .unwrap_or_else(|| Ok(Vec::new()))?;

    let animations = json["animations"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(|a| build_clip(json, bin, a))
                .collect::<Result<Vec<_>, String>>()
        })
        .unwrap_or_else(|| Ok(Vec::new()))?;

    let roots = json["scenes"][0]["nodes"]
        .as_array()
        .map(|arr| {
            arr.iter()
                .map(Value::as_u64)
                .map(|i| i.map(|v| v as usize))
                .collect::<Option<Vec<_>>>()
                .ok_or("invalid scene root node")
        })
        .transpose()?
        .unwrap_or_default();

    Ok(GltfDocument {
        nodes,
        meshes,
        materials,
        textures,
        images,
        animations,
        roots,
    })
}

/// 读取 bufferView 的原始字节（供内嵌图片使用）。
fn read_buffer_view<'a>(json: &Value, bin: &'a [u8], index: usize) -> Result<&'a [u8], String> {
    let view = &json["bufferViews"][index];
    if view.is_null() {
        return Err(format!("bufferView {index} does not exist"));
    }
    let offset = view["byteOffset"].as_u64().unwrap_or(0) as usize;
    let length = view["byteLength"].as_u64().unwrap_or(0) as usize;
    if offset + length > bin.len() {
        return Err(format!("bufferView {index} data out of bounds"));
    }
    Ok(&bin[offset..offset + length])
}

fn build_node(node: &Value) -> Result<Node, String> {
    if node.get("matrix").is_some() {
        return Err(format!(
            "node {:?} uses matrix; the contract requires TRS",
            node.get("name").and_then(Value::as_str).unwrap_or("?")
        ));
    }
    let translation = node
        .get("translation")
        .and_then(f32_vec3)
        .unwrap_or([0., 0., 0.]);
    let rotation = node
        .get("rotation")
        .and_then(f32_vec4)
        .unwrap_or([0., 0., 0., 1.]);
    let scale = node.get("scale").and_then(f32_vec3).unwrap_or([1., 1., 1.]);
    Ok(Node {
        name: node["name"].as_str().map(str::to_owned),
        children: node["children"]
            .as_array()
            .map(|a| a.iter().filter_map(Value::as_u64).map(|i| i as usize).collect())
            .unwrap_or_default(),
        translation,
        rotation,
        scale,
        mesh: node["mesh"].as_u64().map(|i| i as usize),
    })
}

fn build_primitive(
    json: &Value,
    bin: &[u8],
    primitive: &Value,
) -> Result<Primitive, String> {
    let mode = primitive.get("mode").and_then(Value::as_u64).unwrap_or(4);
    if mode != 4 {
        return Err(format!("only triangle meshes (mode 4) are supported, got {mode}"));
    }
    let attributes = primitive
        .get("attributes")
        .ok_or("primitive lacks attributes")?;
    let positions = read_vec3_accessor(json, bin, &attributes["POSITION"])
        .map_err(|e| format!("POSITION: {e}"))?;
    let normals = read_vec3_accessor(json, bin, &attributes["NORMAL"])
        .map_err(|e| format!("NORMAL: {e}"))?;
    let uvs = read_vec2_accessor(json, bin, &attributes["TEXCOORD_0"])
        .map_err(|e| format!("TEXCOORD_0: {e}"))?;
    if uvs.len() != positions.len() || normals.len() != positions.len() {
        return Err("vertex attribute count mismatch".into());
    }
    let indices = read_index_accessor(json, bin, &primitive["indices"])
        .map_err(|e| format!("indices: {e}"))?;
    let material = primitive["material"].as_u64().unwrap_or(0) as usize;
    Ok(Primitive {
        positions,
        normals,
        uvs,
        indices,
        material,
    })
}

fn build_clip(json: &Value, bin: &[u8], anim: &Value) -> Result<AnimClip, String> {
    let name = anim["name"].as_str().unwrap_or_default().to_owned();
    let samplers = anim["samplers"]
        .as_array()
        .ok_or("animation lacks samplers")?;
    let mut channels = Vec::new();
    for channel in anim["channels"].as_array().ok_or("animation lacks channels")? {
        let target = &channel["target"];
        // path 不在支持集合内（scale/weights 等）时忽略该通道
        let path = target["path"].as_str().unwrap_or_default();
        if path != "rotation" && path != "translation" {
            continue;
        }
        let Some(node) = target["node"].as_u64().map(|i| i as usize) else {
            continue;
        };
        let sampler = samplers
            .get(
                channel["sampler"]
                    .as_u64()
                    .unwrap_or(usize::MAX as u64) as usize,
            )
            .ok_or("animation channel sampler index out of bounds")?;
        if let Some(interp) = sampler["interpolation"].as_str() {
            if interp != "LINEAR" {
                return Err(format!("animation {name:?}: interpolation {interp} unsupported; the contract requires LINEAR"));
            }
        }
        let raw_times = read_scalar_times(json, bin, &sampler["input"])?;
        let values = read_f32_accessor(json, bin, &sampler["output"])
            .map_err(|e| format!("animation {name:?} sampler output: {e}"))?;
        let (times, key_count) = classify_times(&raw_times)
            .ok_or_else(|| format!("animation {name:?} timeline is not monotonic"))?;
        // 同一节点可能同时有旋转与平移通道：合并进同一条 channel
        let channel_entry = match channels
            .iter_mut()
            .find(|c: &&mut AnimChannel| c.node == node)
        {
            Some(c) => c,
            None => {
                channels.push(AnimChannel {
                    node,
                    rotation: Vec::new(),
                    translation: Vec::new(),
                    rotation_times: None,
                    translation_times: None,
                });
                channels.last_mut().unwrap()
            }
        };
        if path == "rotation" {
            if values.len() % 4 != 0 || values.is_empty() {
                return Err(format!("animation {name:?} invalid rotation key count"));
            }
            let keys: Vec<[f32; 4]> = values
                .chunks_exact(4)
                .map(|c| [c[0], c[1], c[2], c[3]])
                .collect();
            if keys.len() != key_count {
                return Err(format!("animation {name:?} rotation key count differs from the timeline"));
            }
            channel_entry.rotation = keys;
            channel_entry.rotation_times = Some(times);
        } else {
            if values.len() % 3 != 0 || values.is_empty() {
                return Err(format!("animation {name:?} invalid translation key count"));
            }
            let keys: Vec<[f32; 3]> = values
                .chunks_exact(3)
                .map(|c| [c[0], c[1], c[2]])
                .collect();
            if keys.len() != key_count {
                return Err(format!("animation {name:?} translation key count differs from the timeline"));
            }
            channel_entry.translation = keys;
            channel_entry.translation_times = Some(times);
        }
    }
    Ok(AnimClip { name, channels })
}

/// 均匀网格判定：相邻时间差全部一致（1e-4 容差）→ Uniform(帧率)。
/// 返回 (时间轴, 键数量)。
fn classify_times(times: &[f32]) -> Option<(Times, usize)> {
    if times.iter().zip(times.iter().skip(1)).any(|(a, b)| b < a) {
        return None;
    }
    let count = times.len();
    if times.len() < 2 {
        return Some((Times::General(times.to_vec()), count));
    }
    let step = times[1] - times[0];
    let uniform = times
        .windows(2)
        .all(|w| (w[1] - w[0] - step).abs() < 1e-4);
    let axis = if uniform && step > 1e-6 {
        Times::Uniform(1. / step)
    } else {
        Times::General(times.to_vec())
    };
    Some((axis, count))
}

/// 读取 accessor 的扁平 f32 数据（bufferView 字节序 + 可选 stride）。
fn read_f32_data(
    json: &Value,
    bin: &[u8],
    accessor: &Value,
) -> Result<(Vec<f32>, usize), String> {
    let component = accessor["componentType"].as_u64().unwrap_or(0);
    if component != COMPONENT_F32 {
        return Err(format!("componentType {component} unsupported (float32 only)"));
    }
    let ty = accessor["type"].as_str().unwrap_or_default();
    let n = match ty {
        "SCALAR" => 1,
        "VEC2" => 2,
        "VEC3" => 3,
        "VEC4" => 4,
        other => return Err(format!("type {other:?} unsupported")),
    };
    let count = accessor["count"].as_u64().unwrap_or(0) as usize;
    let view_index = accessor["bufferView"]
        .as_u64()
        .ok_or("accessor lacks bufferView")? as usize;
    let view = &json["bufferViews"][view_index];
    if view.is_null() {
        return Err(format!("bufferView {view_index} does not exist"));
    }
    let view_offset = view["byteOffset"].as_u64().unwrap_or(0) as usize;
    let view_length = view["byteLength"].as_u64().unwrap_or(0) as usize;
    let offset = view_offset + accessor["byteOffset"].as_u64().unwrap_or(0) as usize;
    let stride = view["byteStride"]
        .as_u64()
        .map(|s| s as usize)
        .unwrap_or(4 * n);
    if stride < 4 * n {
        return Err(format!("byteStride {stride} smaller than element size {}", 4 * n));
    }
    let end = offset + (count.max(1).saturating_sub(1)) * stride + 4 * n;
    if end > bin.len() {
        return Err(format!("accessor out of bounds: need {end} bytes, buffer has {} bytes", bin.len()));
    }
    let mut out = Vec::with_capacity(count * n);
    for i in 0..count {
        let base = offset + i * stride;
        for k in 0..n {
            let b = &bin[base + k * 4..base + k * 4 + 4];
            out.push(f32::from_le_bytes(b.try_into().unwrap()));
        }
    }
    let _ = view_length;
    Ok((out, n))
}

/// 读取 accessor 的扁平 f32 数据（不分组）。
fn read_f32_accessor(json: &Value, bin: &[u8], index: &Value) -> Result<Vec<f32>, String> {
    let Some(i) = index.as_u64() else {
        return Err("missing accessor index".into());
    };
    read_f32_data(json, bin, &json["accessors"][i as usize])
        .map(|(data, _)| data)
        .map_err(|e| format!("accessor {i}: {e}"))
}

fn read_vec3_accessor(
    json: &Value,
    bin: &[u8],
    index: &Value,
) -> Result<Vec<[f32; 3]>, String> {
    let Some(i) = index.as_u64() else {
        return Err("missing accessor index".into());
    };
    let (data, n) = read_f32_data(json, bin, &json["accessors"][i as usize])
        .map_err(|e| format!("accessor {i}: {e}"))?;
    if n != 3 {
        return Err(format!("expected VEC3, got {n} components"));
    }
    Ok(data.chunks_exact(3).map(|c| [c[0], c[1], c[2]]).collect())
}

fn read_vec2_accessor(
    json: &Value,
    bin: &[u8],
    index: &Value,
) -> Result<Vec<[f32; 2]>, String> {
    let Some(i) = index.as_u64() else {
        return Err("missing accessor index".into());
    };
    let (data, n) = read_f32_data(json, bin, &json["accessors"][i as usize])
        .map_err(|e| format!("accessor {i}: {e}"))?;
    if n != 2 {
        return Err(format!("expected VEC2, got {n} components"));
    }
    Ok(data.chunks_exact(2).map(|c| [c[0], c[1]]).collect())
}

fn read_scalar_times(json: &Value, bin: &[u8], index: &Value) -> Result<Vec<f32>, String> {
    let Some(i) = index.as_u64() else {
        return Err("missing timeline accessor index".into());
    };
    let (data, n) = read_f32_data(json, bin, &json["accessors"][i as usize])
        .map_err(|e| format!("accessor {i}: {e}"))?;
    if n != 1 {
        return Err(format!("timeline expects SCALAR, got {n} components"));
    }
    Ok(data)
}

fn read_index_accessor(json: &Value, bin: &[u8], index: &Value) -> Result<Vec<u32>, String> {
    let Some(i) = index.as_u64() else {
        return Err("missing index accessor".into());
    };
    let accessor = &json["accessors"][i as usize];
    let component = accessor["componentType"].as_u64().unwrap_or(0);
    let view_index = accessor["bufferView"].as_u64().ok_or("index lacks bufferView")? as usize;
    let view = &json["bufferViews"][view_index];
    if view.is_null() {
        return Err(format!("bufferView {view_index} does not exist"));
    }
    let count = accessor["count"].as_u64().unwrap_or(0) as usize;
    let (width, read): (usize, fn(&[u8]) -> u32) = match component {
        COMPONENT_U16 => (2, |b: &[u8]| u16::from_le_bytes(b[..2].try_into().unwrap()) as u32),
        COMPONENT_U32 => (4, |b: &[u8]| u32::from_le_bytes(b[..4].try_into().unwrap())),
        other => return Err(format!("index componentType {other} unsupported (u16/u32 only)")),
    };
    let offset = view["byteOffset"].as_u64().unwrap_or(0) as usize
        + accessor["byteOffset"].as_u64().unwrap_or(0) as usize;
    if offset + count * width > bin.len() {
        return Err("index data out of bounds".into());
    }
    Ok((0..count)
        .map(|k| read(&bin[offset + k * width..]))
        .collect())
}

fn f32_vec3(v: &Value) -> Option<[f32; 3]> {
    let a = v.as_array()?;
    if a.len() != 3 {
        return None;
    }
    Some([
        a[0].as_f64()? as f32,
        a[1].as_f64()? as f32,
        a[2].as_f64()? as f32,
    ])
}

fn f32_vec4(v: &Value) -> Option<[f32; 4]> {
    let a = v.as_array()?;
    if a.len() != 4 {
        return None;
    }
    Some([
        a[0].as_f64()? as f32,
        a[1].as_f64()? as f32,
        a[2].as_f64()? as f32,
        a[3].as_f64()? as f32,
    ])
}

#[cfg(test)]
pub(crate) mod test_fixture {
    /// 2×2 纯色 PNG（RGBA），供道具模型内嵌纹理测试。
    pub(crate) fn tiny_png(r: u8, g: u8, b: u8) -> Vec<u8> {
        let mut img = image::RgbaImage::new(2, 2);
        for p in img.pixels_mut() {
            *p = image::Rgba([r, g, b, 255]);
        }
        let mut png = Vec::new();
        img.write_to(&mut std::io::Cursor::new(&mut png), image::ImageFormat::Png)
            .unwrap();
        png
    }

    /// 单材质单三角形的道具 GLB：材质绑定一张内嵌 PNG 纹理。
    /// 节点名 prop_root / prop_mesh。
    pub(crate) fn prop_glb() -> Vec<u8> {
        let png = tiny_png(200, 40, 90);
        let json = format!(
            r#"{{
                "asset": {{"version": "2.0"}},
                "scene": 0,
                "scenes": [{{"nodes": [0]}}],
                "nodes": [
                    {{"name": "blockbench_export", "children": [1]}},
                    {{"name": "prop_root", "children": [2]}},
                    {{"name": "prop_mesh", "mesh": 0}}
                ],
                "meshes": [{{"primitives": [{{"attributes": {{
                    "POSITION": 0, "NORMAL": 1, "TEXCOORD_0": 2}},
                    "indices": 3, "material": 0}}]}}],
                "materials": [{{"name": "prop_mat", "pbrMetallicRoughness":
                    {{"baseColorTexture": {{"index": 0}}}}}}],
                "textures": [{{"source": 0}}],
                "images": [{{"bufferView": 0, "mimeType": "image/png"}}],
                "bufferViews": [
                    {{"buffer": 0, "byteOffset": 0, "byteLength": {png_len}}},
                    {{"buffer": 0, "byteOffset": {png_len}, "byteLength": {pos_len}, "target": 34962}},
                    {{"buffer": 0, "byteOffset": {off_nrm}, "byteLength": {nrm_len}, "target": 34962}},
                    {{"buffer": 0, "byteOffset": {off_uv}, "byteLength": {uv_len}, "target": 34962}},
                    {{"buffer": 0, "byteOffset": {off_idx}, "byteLength": {idx_len}, "target": 34963}}
                ],
                "accessors": [
                    {{"bufferView": 1, "componentType": 5126, "count": 3, "type": "VEC3", "min": [0,0,0], "max": [1,1,1]}},
                    {{"bufferView": 2, "componentType": 5126, "count": 3, "type": "VEC3"}},
                    {{"bufferView": 3, "componentType": 5126, "count": 3, "type": "VEC2"}},
                    {{"bufferView": 4, "componentType": 5125, "count": 3, "type": "SCALAR"}}
                ],
                "buffers": [{{"byteLength": {total}}}]
            }}"#,
            png_len = png.len(),
            pos_len = 36,
            nrm_len = 36,
            uv_len = 24,
            idx_len = 12,
            off_nrm = png.len() + 36,
            off_uv = png.len() + 72,
            off_idx = png.len() + 96,
            total = png.len() + 108,
        );
        let bin: Vec<u8> = png
            .iter()
            .copied()
            // 三顶点位置（单位方块正面三角形）
            .chain([0f32, 0f32, 0f32, 1f32, 0f32, 0f32, 1f32, 1f32, 0f32]
                .iter()
                .flat_map(|f| f.to_le_bytes()))
            // 法线
            .chain([0f32, 0f32, 1f32].iter().flat_map(|f| f.to_le_bytes()).cycle().take(36))
            // UV
            .chain([0f32, 1f32, 1f32, 1f32, 1f32, 0f32].iter().flat_map(|f| f.to_le_bytes()))
            // 索引
            .chain([0u32, 1, 2].iter().flat_map(|f| f.to_le_bytes()))
            .collect();
        assert_eq!(bin.len(), png.len() + 108);
        pack_glb(json.as_bytes(), &bin)
    }

    /// 打包 GLB 容器：JSON chunk（空格补齐）+ BIN chunk（零补齐）。
    pub(crate) fn pack_glb(json: &[u8], bin: &[u8]) -> Vec<u8> {
        let json_pad = (4 - json.len() % 4) % 4;
        let bin_pad = (4 - bin.len() % 4) % 4;
        let json_len = json.len() + json_pad;
        let bin_len = bin.len() + bin_pad;
        let mut out = Vec::with_capacity(12 + 8 + json_len + 8 + bin_len);
        out.extend_from_slice(&[0x67, 0x6C, 0x54, 0x46]); // glTF
        out.extend_from_slice(&(2u32).to_le_bytes());
        out.extend_from_slice(&((12 + 8 + json_len + 8 + bin_len) as u32).to_le_bytes());
        out.extend_from_slice(&(json_len as u32).to_le_bytes());
        out.extend_from_slice(b"JSON");
        out.extend_from_slice(json);
        out.extend(std::iter::repeat(b' ').take(json_pad));
        out.extend_from_slice(&(bin_len as u32).to_le_bytes());
        out.extend_from_slice(b"BIN\x00");
        out.extend_from_slice(bin);
        out.extend(std::iter::repeat(0u8).take(bin_pad));
        out
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn load(path: &str) -> GltfDocument {
        let bytes = std::fs::read(path).expect("model file should be readable");
        parse(&bytes).expect("model should parse")
    }

    fn classic_gltf() -> GltfDocument {
        load(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/../tools/models/classic-player.gltf"
        ))
    }

    fn classic_glb() -> GltfDocument {
        load(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/src/assets/classic-player.glb"
        ))
    }

    fn node_by_name<'a>(doc: &'a GltfDocument, name: &str) -> &'a Node {
        doc.nodes
            .iter()
            .find(|n| n.name.as_deref() == Some(name))
            .unwrap_or_else(|| panic!("missing node {name}"))
    }

    #[test]
    fn parses_vendored_gltf_and_glb_identically() {
        let gltf = classic_gltf();
        let glb = classic_glb();
        assert_eq!(gltf.nodes.len(), glb.nodes.len());
        assert_eq!(gltf.animations.len(), glb.animations.len());
        for (a, b) in gltf.animations.iter().zip(&glb.animations) {
            assert_eq!(a.name, b.name);
            assert_eq!(a.channels.len(), b.channels.len());
            for (ca, cb) in a.channels.iter().zip(&b.channels) {
                assert_eq!(ca.rotation, cb.rotation);
                assert_eq!(ca.translation, cb.translation);
            }
        }
        assert_eq!(gltf.meshes.len(), glb.meshes.len());
        // 网格属性（UV/法线/位置/索引）也必须一致
        for (ma, mb) in gltf.meshes.iter().zip(&glb.meshes) {
            for (pa, pb) in ma.primitives.iter().zip(&mb.primitives) {
                assert_eq!(pa.positions, pb.positions, "positions");
                assert_eq!(pa.uvs, pb.uvs, "uvs");
                assert_eq!(pa.indices, pb.indices, "indices");
                assert_eq!(pa.material, pb.material);
            }
        }
    }

    #[test]
    fn head_front_face_uvs_are_vanilla() {
        for doc in [classic_gltf(), classic_glb()] {
            let head_node = doc
                .nodes
                .iter()
                .find(|n| n.name.as_deref() == Some("Head"))
                .expect("Head node");
            let mesh = &doc.meshes[doc.nodes[head_node.children[0]].mesh.unwrap()];
            let prim = &mesh.primitives[0];
            // 全部 24 个 UV 与 vanilla 布局逐角点核对（×64 换算到像素域，
            // 容差 0.1 px 覆盖 FBX 的半纹素内缩）
            let vanilla = [
                [8., 16.], [16., 16.], [16., 8.], [8., 8.],   // 前
                [24., 16.], [32., 16.], [32., 8.], [24., 8.], // 后
                [24., 8.], [16., 8.], [16., 0.], [24., 0.],   // 底
                [16., 0.], [8., 0.], [8., 8.], [16., 8.],     // 顶
                [16., 16.], [24., 16.], [24., 8.], [16., 8.], // 左
                [0., 16.], [8., 16.], [8., 8.], [0., 8.],     // 右
            ];
            for (k, uv) in prim.uvs.iter().enumerate() {
                assert!(
                    (uv[0] * 64. - vanilla[k][0]).abs() < 0.1
                        && (uv[1] * 64. - vanilla[k][1]).abs() < 0.1,
                    "head uv[{k}] = {uv:?} (expected {:?})",
                    vanilla[k]
                );
            }
        }
    }

    #[test]
    fn vendored_node_tree_and_materials() {
        let doc = classic_gltf();
        assert_eq!(doc.roots, vec![0]);
        for name in [
            "Body",
            "Head",
            "Left_Arm",
            "Right_Arm",
            "Left_Leg",
            "Right_Leg",
            "Cape",
        ] {
            node_by_name(&doc, name);
        }
        // 左臂静止平移 (−5, 10, 0) GLTF 单位 ÷16 = (−0.3125, 0.625, 0)
        let arm = node_by_name(&doc, "Left_Arm");
        assert!((arm.translation[0] - -0.3125).abs() < 1e-6);
        assert!((arm.translation[1] - 0.625).abs() < 1e-6);
        // 材质：Mat（皮肤）与 cape（披风）
        assert_eq!(doc.materials.len(), 2);
        assert_eq!(doc.materials[1].name.as_deref(), Some("cape"));
        // 披风网格绑定 cape 材质（网格在 Cape_2 子节点上）
        let cape_node = node_by_name(&doc, "Cape");
        let cape_child = &doc.nodes[cape_node.children[0]];
        let cape_mesh = &doc.meshes[cape_child.mesh.expect("Cape_2 should have a mesh")];
        assert_eq!(cape_mesh.primitives.len(), 1);
        assert_eq!(cape_mesh.primitives[0].material, 1);
        // 网格尺寸：每部件 24 顶点 36 索引
        let head_node = node_by_name(&doc, "Head");
        let child = &doc.nodes[head_node.children[0]];
        let head_mesh = &doc.meshes[child.mesh.unwrap()];
        assert_eq!(head_mesh.primitives[0].positions.len(), 24);
        assert_eq!(head_mesh.primitives[0].indices.len(), 36);
    }

    #[test]
    fn vendored_animation_names_and_uniform_times() {
        let doc = classic_gltf();
        let names: Vec<&str> = doc
            .animations
            .iter()
            .map(|c| c.name.as_str())
            .collect();
        for expected in ["idle", "idle_sub_1", "idle_sub_2", "idle_sub_3", "interact"] {
            assert!(names.contains(&expected), "missing animation {expected}: {names:?}");
        }
        let idle = doc
            .animations
            .iter()
            .find(|c| c.name == "idle")
            .unwrap();
        // idle 含 Body/Head/双臂/双腿/Cape 七条通道（节点合并后）
        assert_eq!(idle.channels.len(), 7);
        for channel in &idle.channels {
            if let Some(times) = &channel.rotation_times {
                let Times::Uniform(fps) = times else {
                    panic!("idle rotation timeline should be a uniform grid");
                };
                assert!((fps - 30.).abs() < 0.01, "fps = {fps}");
            }
        }
    }

    #[test]
    fn rejects_bad_glb() {
        assert!(parse(b"not a glb").is_err());
        let mut truncated = std::fs::read(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/src/assets/classic-player.glb"
        ))
        .unwrap();
        truncated.truncate(10);
        assert!(parse(&truncated).is_err());
    }

    #[test]
    fn base64_decode_matches_expected() {
        assert_eq!(base64_decode("").unwrap(), Vec::<u8>::new());
        assert_eq!(base64_decode("aGVsbG8=").unwrap(), b"hello".to_vec());
        assert_eq!(base64_decode("aGVsbG8h").unwrap(), b"hello!".to_vec());
        assert_eq!(base64_decode("aGVs\nbG8=").unwrap(), b"hello".to_vec());
        assert!(base64_decode("a=").is_err());
        assert!(base64_decode("aGVsbG8").is_err());
    }

    #[test]
    fn parses_embedded_material_texture_from_glb() {
        let doc = parse(&test_fixture::prop_glb()).expect("prop GLB should parse");
        assert_eq!(doc.materials.len(), 1);
        assert_eq!(doc.materials[0].name.as_deref(), Some("prop_mat"));
        assert_eq!(doc.materials[0].base_color_texture, Some(0));
        assert_eq!(doc.textures, vec![0]);
        let png = test_fixture::tiny_png(200, 40, 90);
        assert_eq!(doc.images.len(), 1);
        assert!(matches!(&doc.images[0], ImageSource::Data(data) if data == &png));
    }

    #[test]
    fn vendored_player_images_are_external() {
        // Modrinth 玩家模型引用外部贴图（steve.png/cape），
        // 运行时由皮肤/披风替换，解析层保留为外部引用。
        let doc = classic_gltf();
        assert_eq!(doc.images.len(), 2);
        assert!(doc.images.iter().all(|i| matches!(i, ImageSource::External)));
        assert_eq!(doc.textures, vec![0, 1]);
    }
}
