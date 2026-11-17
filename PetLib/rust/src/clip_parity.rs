//! 剪辑数据与 vendored GLTF 的奇偶校验(仅测试构建)。

use std::collections::HashMap;

use glam::{Quat, Vec3};
use serde_json::Value;

use crate::clip::{self, cape_euler, ClipPose};
use crate::clip_data::{Clip, CLIP_IDLE, CLIP_INTERACT, CLIP_SUBS};

const CLASSIC: &str = concat!(
    env!("CARGO_MANIFEST_DIR"),
    "/../tools/models/classic-player.gltf"
);
const SLIM: &str = concat!(
    env!("CARGO_MANIFEST_DIR"),
    "/../tools/models/slim-player.gltf"
);

const FPS: f32 = 30.0;
/// GLTF 单位 → 模型像素
const PIXELS: f32 = 16.0;

/// GLTF 动画名 → 剪辑静态数据
fn static_clips() -> Vec<(&'static str, &'static Clip)> {
    vec![
        ("idle", &CLIP_IDLE),
        ("idle_sub_1", &CLIP_SUBS[0]),
        ("idle_sub_2", &CLIP_SUBS[1]),
        ("idle_sub_3", &CLIP_SUBS[2]),
        ("interact", &CLIP_INTERACT),
    ]
}

fn load(path: &str) -> Value {
    let text = std::fs::read_to_string(path).expect("gltf readable");
    serde_json::from_str(&text).expect("gltf json")
}

/// GLTF 键值访问器,统一读为 4 宽(float,右侧补零)
fn accessor(gltf: &Value, buffer: &[u8], index: usize) -> Vec<[f32; 4]> {
    let acc = &gltf["accessors"][index];
    assert_eq!(acc["componentType"], 5126, "通道须为 float32");
    let n = match acc["type"].as_str() {
        Some("SCALAR") => 1,
        Some("VEC3") => 3,
        Some("VEC4") => 4,
        other => panic!("unsupported accessor type {other:?}"),
    };
    let view = &gltf["bufferViews"][acc["bufferView"].as_u64().expect("bufferView") as usize];
    let mut offset = view["byteOffset"].as_u64().unwrap_or(0) as usize
        + acc["byteOffset"].as_u64().unwrap_or(0) as usize;
    let stride = view["byteStride"].as_u64().map_or(4 * n, |s| s as usize);
    let count = acc["count"].as_u64().expect("count") as usize;
    (0..count)
        .map(|_| {
            let mut out = [0f32; 4];
            for (i, slot) in out.iter_mut().enumerate().take(n) {
                let bytes: [u8; 4] = buffer[offset + i * 4..][..4].try_into().expect("f32");
                *slot = f32::from_le_bytes(bytes);
            }
            offset += stride;
            out
        })
        .collect()
}

fn decode_base64(input: &str) -> Vec<u8> {
    fn digit(b: u8) -> u32 {
        match b {
            b'A'..=b'Z' => (b - b'A') as u32,
            b'a'..=b'z' => (b - b'a' + 26) as u32,
            b'0'..=b'9' => (b - b'0' + 52) as u32,
            b'+' => 62,
            b'/' => 63,
            _ => panic!("invalid base64 byte {b}"),
        }
    }
    let clean: Vec<u8> = input.bytes().filter(|b| !b.is_ascii_whitespace()).collect();
    let mut out = Vec::with_capacity(clean.len() * 3 / 4 + 3);
    for chunk in clean.chunks(4) {
        let mut acc = 0u32;
        let mut n = 0;
        for &b in chunk {
            if b == b'=' {
                break;
            }
            acc = (acc << 6) | digit(b);
            n += 1;
        }
        match n {
            4 => out.extend_from_slice(&[(acc >> 16) as u8, (acc >> 8) as u8, acc as u8]),
            3 => {
                out.push((acc >> 10) as u8);
                out.push((acc >> 2) as u8);
            }
            2 => out.push((acc >> 4) as u8),
            _ => panic!("truncated base64 chunk"),
        }
    }
    out
}

#[derive(Default)]
struct GltfClip {
    /// 节点名 → (rotation 键, translation 键),统一 4 宽
    channels: HashMap<String, (Vec<[f32; 4]>, Vec<[f32; 4]>)>,
    key_count: usize,
}

fn parse_clip(gltf: &Value, buffer: &[u8], name: &str) -> GltfClip {
    let anim = gltf["animations"]
        .as_array()
        .expect("animations")
        .iter()
        .find(|a| a["name"] == name)
        .unwrap_or_else(|| panic!("animation {name} missing"));
    let mut clip = GltfClip::default();
    for channel in anim["channels"].as_array().expect("channels") {
        let target = &channel["target"];
        let path = target["path"].as_str().expect("path");
        let sampler = &anim["samplers"][channel["sampler"].as_u64().expect("sampler") as usize];
        let times = accessor(
            gltf,
            buffer,
            sampler["input"].as_u64().expect("input") as usize,
        );
        let values = accessor(
            gltf,
            buffer,
            sampler["output"].as_u64().expect("output") as usize,
        );
        // 采样约定:键时间必须落在 1/30 秒网格上
        for (i, t) in times.iter().enumerate() {
            let expected = i as f32 / FPS;
            assert!(
                (t[0] - expected).abs() < 1e-4,
                "{name}: key {i} at {} not on 30fps grid",
                t[0]
            );
        }
        clip.key_count = clip.key_count.max(values.len());
        let node = gltf["nodes"][target["node"].as_u64().expect("node") as usize]["name"]
            .as_str()
            .expect("node name")
            .to_string();
        let entry = clip.channels.entry(node).or_default();
        match path {
            "rotation" => entry.0 = values,
            "translation" => entry.1 = values,
            _ => {}
        }
    }
    clip
}

/// GLTF 键 → 四元数(原始键直采,归一化抵消生成舍入)
fn to_quat(k: &[f32; 4]) -> Quat {
    Quat::from_xyzw(k[0], k[1], k[2], k[3]).normalize()
}

fn to_pixels(k: &[f32; 4]) -> [f32; 3] {
    [k[0] * PIXELS, k[1] * PIXELS, k[2] * PIXELS]
}

fn lerp_pixels(a: &[f32; 4], b: &[f32; 4], frac: f32) -> [f32; 3] {
    let pa = to_pixels(a);
    let pb = to_pixels(b);
    [
        pa[0] + (pb[0] - pa[0]) * frac,
        pa[1] + (pb[1] - pa[1]) * frac,
        pa[2] + (pb[2] - pa[2]) * frac,
    ]
}

/// 按 clip.rs 的采样语义直接从 GLTF 键求值,返回 (旋转, 平移像素)
fn expected_pose(clip: &GltfClip, node: &str, t: f32) -> (Quat, [f32; 3]) {
    let Some((rotation, translation)) = clip.channels.get(node) else {
        return (Quat::IDENTITY, [0.; 3]);
    };
    let quat_at = |keys: &[[f32; 4]]| -> Quat {
        let frame = (t * FPS).max(0.);
        let last = keys.len() - 1;
        if last == 0 {
            return to_quat(&keys[0]);
        }
        let index = (frame as usize).min(last - 1);
        let frac = (frame - index as f32).clamp(0., 1.);
        to_quat(&keys[index]).slerp(to_quat(&keys[index + 1]), frac)
    };
    let lerp_at = |keys: &[[f32; 4]]| -> [f32; 3] {
        let frame = (t * FPS).max(0.);
        let last = keys.len() - 1;
        if last == 0 {
            return to_pixels(&keys[0]);
        }
        let index = (frame as usize).min(last - 1);
        let frac = (frame - index as f32).clamp(0., 1.);
        lerp_pixels(&keys[index], &keys[index + 1], frac)
    };
    let rotation_quat = if rotation.is_empty() {
        Quat::IDENTITY
    } else {
        quat_at(rotation)
    };
    let translation_vec = if translation.is_empty() {
        [0.; 3]
    } else {
        lerp_at(translation)
    };
    (rotation_quat, translation_vec)
}

fn assert_close_quat(actual: Quat, expected: Quat, what: &str, clip_name: &str, t: f32) {
    // 归一化后再比:slerp 的浮点模长误差会被 acos 放大成假角度
    let angle = actual.normalize().angle_between(expected.normalize());
    assert!(
        angle < 1e-3,
        "{clip_name}@{t:.3} {what}: {:?} vs {:?} (angle {angle})",
        actual,
        expected
    );
}

fn assert_close_vec(actual: Vec3, expected: [f32; 3], what: &str, clip_name: &str, t: f32) {
    for i in 0..3 {
        assert!(
            (actual[i] - expected[i]).abs() < 5e-3,
            "{clip_name}@{t:.3} {what}[{i}]: {} vs {}",
            actual[i],
            expected[i]
        );
    }
}

fn check_clip(gltf: &Value, buffer: &[u8], name: &str, data: &'static Clip) {
    let parsed = parse_clip(gltf, buffer, name);
    let mut checked = 0;
    // 无平移通道时枢轴保持骨架静止值
    let expected_pivot = |node: &str, t: f32, rest: [f32; 3]| -> [f32; 3] {
        match parsed.channels.get(node) {
            Some((_, translation)) if !translation.is_empty() => expected_pose(&parsed, node, t).1,
            _ => rest,
        }
    };
    // 采样点:所有关键帧及其间隔中点
    let steps = parsed.key_count.max(2) * 2;
    for i in 0..=steps {
        let t = (parsed.key_count.max(1) - 1) as f32 * (i as f32 / steps as f32);
        let actual: ClipPose = clip::sample(data, t);
        let (body_rot, body_pos) = expected_pose(&parsed, "Body", t);
        let (head_rot, _) = expected_pose(&parsed, "Head", t);
        let (arm_right, _) = expected_pose(&parsed, "Right_Arm", t);
        let (arm_left, _) = expected_pose(&parsed, "Left_Arm", t);
        let (leg_right, _) = expected_pose(&parsed, "Right_Leg", t);
        let (leg_left, _) = expected_pose(&parsed, "Left_Leg", t);
        let arm_left_pos = expected_pivot("Left_Arm", t, [-5., 10., 0.]);
        let arm_right_pos = expected_pivot("Right_Arm", t, [5., 10., 0.]);
        let leg_left_pos = expected_pivot("Left_Leg", t, [-1.9, 0., 0.]);
        let leg_right_pos = expected_pivot("Right_Leg", t, [1.9, 0., 0.]);

        assert_close_quat(actual.body_rot, body_rot, "body_rot", name, t);
        assert_close_vec(actual.body_pos, body_pos, "body_pos", name, t);
        assert_close_quat(actual.head_rot, head_rot, "head_rot", name, t);
        assert_close_quat(actual.arms[0], arm_left, "arm_left", name, t);
        assert_close_quat(actual.arms[1], arm_right, "arm_right", name, t);
        assert_close_quat(actual.legs[0], leg_left, "leg_left", name, t);
        assert_close_quat(actual.legs[1], leg_right, "leg_right", name, t);
        assert_close_vec(
            actual.arm_pivots[0],
            arm_left_pos,
            "arm_left pivot",
            name,
            t,
        );
        assert_close_vec(
            actual.arm_pivots[1],
            arm_right_pos,
            "arm_right pivot",
            name,
            t,
        );
        assert_close_vec(
            actual.leg_pivots[0],
            leg_left_pos,
            "leg_left pivot",
            name,
            t,
        );
        assert_close_vec(
            actual.leg_pivots[1],
            leg_right_pos,
            "leg_right pivot",
            name,
            t,
        );

        // 披风:采样端对局部旋转做根共轭后分解,此处用同一管线回验
        let (cape_rot, _) = expected_pose(&parsed, "Cape", t);
        let euler = cape_euler(crate::pose::flip_quat(cape_rot));
        assert!(
            (actual.cape.x - euler.x).abs() < 1e-3 && (actual.cape.y - euler.y).abs() < 1e-3,
            "{name}@{t:.3} cape: {:?} vs {:?}",
            actual.cape,
            euler
        );
        checked += 1;
    }
    assert!(checked > 0);
}

#[test]
fn clip_data_matches_vendored_gltf() {
    let gltf = load(CLASSIC);
    let buffer = decode_base64(
        gltf["buffers"][0]["uri"]
            .as_str()
            .expect("data uri")
            .split_once(',')
            .expect("base64 payload")
            .1,
    );
    for (name, data) in static_clips() {
        check_clip(&gltf, &buffer, name, data);
    }
}

/// slim 与 classic 的动画数据必须逐字节一致(骨架差异仅在臂宽)
#[test]
fn slim_animations_match_classic() {
    assert_eq!(load(SLIM)["animations"], load(CLASSIC)["animations"]);
}
