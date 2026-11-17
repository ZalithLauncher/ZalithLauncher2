//! PetLib 渲染核心：皮肤玩家模型的软件光栅化预览。

pub mod animation;
pub mod api;
mod clip;
mod clip_data;
#[cfg(test)]
mod clip_parity;
pub mod lighting;
mod pool;
pub mod pose;
pub mod render;
pub mod skin;

#[cfg(target_os = "android")]
mod jni;