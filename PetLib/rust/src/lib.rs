//! PetLib 渲染核心：皮肤玩家模型的软件光栅化预览与场景渲染。

pub mod animation;
pub mod api;
mod clip;
mod gltf;
mod model;
pub mod lighting;
mod pool;
pub mod pose;
pub mod render;
pub mod scene;
pub mod skin;

#[cfg(target_os = "android")]
mod jni;
