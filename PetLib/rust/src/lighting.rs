//! 方向光照，朗伯明暗带 + sRGB 精确的量化查表着色。

use glam::Vec3;

use crate::api::LightingParams;

const LIGHT_LEVELS: usize = 128;
const LIGHT_RANGE: f32 = 1.5;
const LIGHT_FLOOR: f32 = 0.35;

pub struct Lighting {
    signature: [u32; 4],
    levels: Vec<[[u8; 256]; 3]>,
    direction: Vec3,
    contrast: f32,
    brightness: f32,
}

impl Lighting {
    pub fn new(p: &LightingParams) -> Self {
        let mut light = Self {
            signature: [0; 4],
            levels: Vec::new(),
            direction: Vec3::Z,
            contrast: 1.,
            brightness: 1.,
        };
        light.prepare(p);
        light
    }

    /// 参数未变时复用既有查表，避免逐帧重建。
    pub fn prepare(&mut self, p: &LightingParams) {
        let signature = [
            p.azimuth.to_bits(),
            p.elevation.to_bits(),
            p.brightness.to_bits(),
            p.softness.to_bits(),
        ];
        if self.signature == signature && !self.levels.is_empty() {
            return;
        }
        let forward = Vec3::new(30., 23., 70.).normalize();
        let right = Vec3::Y.cross(forward).normalize();
        let up = forward.cross(right);
        let az = p.azimuth.to_radians();
        let el = p.elevation.to_radians();
        self.direction = (forward * (el.cos() * az.cos()) + right * (el.cos() * az.sin()) + up * el.sin()).normalize();
        // 柔和度将朗伯明暗带的对比向平灰收敛。
        self.contrast = 2.6 + (0.6 - 2.6) * (p.softness / 100.).clamp(0., 1.);
        self.brightness = p.brightness / 100.;
        self.levels = (0..LIGHT_LEVELS)
            .map(|level| {
                let shade = level as f32 / (LIGHT_LEVELS - 1) as f32 * LIGHT_RANGE;
                let mut lut = [[0u8; 256]; 3];
                for v in 0..256 {
                    let c = v as f32 / 255.;
                    let linear = if c <= 0.04045 {
                        c / 12.92
                    } else {
                        ((c + 0.055) / 1.055).powf(2.4)
                    };
                    let l = (linear * shade).min(1.);
                    let out = if l <= 0.0031308 {
                        l * 12.92
                    } else {
                        1.055 * l.powf(1. / 2.4) - 0.055
                    };
                    for ch in 0..3 {
                        lut[ch][v] = (out * 255. + 0.5) as u8;
                    }
                }
                lut
            })
            .collect();
        self.signature = signature;
    }

    /// 世界空间法线 → 明暗系数。
    pub fn shade(&self, world_normal: Vec3) -> f32 {
        let t = (world_normal.dot(self.direction) + 1.) * 0.5;
        let t = (0.5 + (t - 0.5) * self.contrast).clamp(0., 1.);
        (LIGHT_FLOOR + (1. - LIGHT_FLOOR) * t) * self.brightness
    }

    pub fn lut(&self, shade: f32) -> &[[u8; 256]; 3] {
        let level = ((shade / LIGHT_RANGE) * (LIGHT_LEVELS - 1) as f32)
            .round()
            .clamp(0., (LIGHT_LEVELS - 1) as f32) as usize;
        &self.levels[level]
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn shade_stays_in_floor_range() {
        let p = LightingParams::default();
        let light = Lighting::new(&p);
        for n in [Vec3::X, -Vec3::X, Vec3::Y, Vec3::Z, -Vec3::Z, Vec3::ONE] {
            let s = light.shade(n);
            assert!((0.3..=1.6).contains(&s), "{n}: {s}");
        }
    }

    #[test]
    fn prepare_reuses_levels_on_same_params() {
        let p = LightingParams::default();
        let mut light = Lighting::new(&p);
        let ptr = light.levels.as_ptr();
        light.prepare(&p);
        assert_eq!(ptr, light.levels.as_ptr());
    }
}
