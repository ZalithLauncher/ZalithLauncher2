//! 皮肤/披风纹理的解析、校验与处理。

use image::RgbaImage;

/// 手臂类型
#[derive(Clone, Copy, PartialEq, Eq, Debug)]
pub enum ArmModel {
    /// 经典 4px
    Classic,
    /// 纤细 3px
    Slim,
}

/// 单张模型纹理（皮肤或披风）。像素以 RGBA 四元组平铺存储，
/// 供光栅化逐像素查表着色直接索引。
#[derive(Clone)]
pub struct Texture {
    pub width: u32,
    pub height: u32,
    pub pixels: Vec<[u8; 4]>,
}

impl Texture {
    pub fn decode(bytes: &[u8]) -> Result<Self, String> {
        let image = image::load_from_memory_with_format(bytes, image::ImageFormat::Png)
            .map_err(|e| e.to_string())?
            .into_rgba8();
        Ok(Self::from_image(image))
    }

    /// 展平为像素四元组：单份存储，解析后即可释放解码图像。
    fn from_image(image: RgbaImage) -> Self {
        let (width, height) = (image.width(), image.height());
        let pixels = image
            .into_raw()
            .chunks_exact(4)
            .map(|p| [p[0], p[1], p[2], p[3]])
            .collect();
        Self {
            width,
            height,
            pixels,
        }
    }
}

pub struct Skin {
    pub texture: Texture,
    pub legacy: bool,
    pub arm: ArmModel,
}

impl Skin {
    /// 解析并处理皮肤：尺寸校验、旧版 64×32 皮肤的下层不透明化。
    /// 臂型取显式指定值；未指定时按纹理自动检测（旧版皮肤恒为经典）。
    pub fn load(bytes: &[u8], arm: Option<ArmModel>) -> Result<Self, String> {
        if bytes.len() > 8 * 1024 * 1024 {
            return Err("Skin file must not exceed 8 MB.".into());
        }
        if !bytes.starts_with(&[137, 80, 78, 71, 13, 10, 26, 10]) {
            return Err("Please select a PNG skin.".into());
        }
        // 在解压任意输入前先校验尺寸声明。
        if bytes.len() < 24 {
            return Err("PNG file is incomplete.".into());
        }
        let w = u32::from_be_bytes(bytes[16..20].try_into().unwrap());
        let h = u32::from_be_bytes(bytes[20..24].try_into().unwrap());
        if w < 64 || w > 1024 || w % 64 != 0 || (h != w && h != w / 2) {
            return Err(
                "Skin must be a 64×64, 64×32, or proportional HD PNG, up to 1024×1024.".into(),
            );
        }
        let image = image::load_from_memory_with_format(bytes, image::ImageFormat::Png)
            .map_err(|e| e.to_string())?
            .into_rgba8();
        let legacy = h * 2 == w;
        let arm = arm.unwrap_or_else(|| {
            if legacy {
                ArmModel::Classic
            } else {
                detect_arm(&image)
            }
        });
        let mut image = image;
        let k = w / 64;
        if legacy {
            let transparent =
                (0..16 * k).any(|y| (32 * k..64 * k).any(|x| image.get_pixel(x, y)[3] < 128));
            if !transparent {
                for y in 0..16 * k {
                    for x in 32 * k..64 * k {
                        image.get_pixel_mut(x, y)[3] = 0;
                    }
                }
            }
        }
        // 基础层不允许透明：强行补齐头/身/臂/腿所在行。
        let mut regions = vec![(0, 0, 32, 16), (0, 16, 56, 16)];
        if !legacy {
            regions.push((16, 48, 32, 16));
        }
        for (x, y, w, h) in regions {
            for yy in y * k..(y + h) * k {
                for xx in x * k..(x + w) * k {
                    image.get_pixel_mut(xx, yy)[3] = 255;
                }
            }
        }
        Ok(Self {
            texture: Texture::from_image(image),
            legacy,
            arm,
        })
    }
}

/// 披风纹理：64×32 或等比例高清，上限与皮肤一致。
pub fn load_cape(bytes: &[u8]) -> Result<Texture, String> {
    if bytes.len() > 8 * 1024 * 1024 {
        return Err("Cape file must not exceed 8 MB.".into());
    }
    if !bytes.starts_with(&[137, 80, 78, 71, 13, 10, 26, 10]) {
        return Err("Please select a PNG cape.".into());
    }
    if bytes.len() < 24 {
        return Err("PNG file is incomplete.".into());
    }
    let w = u32::from_be_bytes(bytes[16..20].try_into().unwrap());
    let h = u32::from_be_bytes(bytes[20..24].try_into().unwrap());
    // 在解压任意输入前校验尺寸声明，拒绝解码炸弹。
    if w < 64 || w > 1024 || w % 64 != 0 || h * 2 != w {
        return Err("Cape must be a 64×32 or proportional HD PNG, up to 1024×512.".into());
    }
    Texture::decode(bytes)
}

/// slim 臂型自动检测
fn detect_arm(image: &RgbaImage) -> ArmModel {
    let k = image.width() / 64;
    // 经典 4px 臂型在 64×64 布局中存在四块冗余条带（通常）
    let regions = [
        (50, 16, 2, 4),
        (54, 20, 2, 12),
        (42, 48, 2, 4),
        (46, 52, 2, 12),
    ];
    let mut all_black = true;
    let mut all_white = true;
    for &(x, y, w, h) in &regions {
        for yy in y * k..(y + h) * k {
            for xx in x * k..(x + w) * k {
                let p = image.get_pixel(xx, yy);
                if p[3] != 255 {
                    return ArmModel::Slim;
                }
                let black = p[0] == 0 && p[1] == 0 && p[2] == 0;
                let white = p[0] == 255 && p[1] == 255 && p[2] == 255;
                all_black &= black;
                all_white &= white;
            }
        }
    }
    if all_black || all_white {
        ArmModel::Slim
    } else {
        ArmModel::Classic
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// 合成一张纯色 PNG，供测试充当皮肤。
    fn solid_png(w: u32, h: u32, rgba: [u8; 4]) -> Vec<u8> {
        let mut img = image::RgbaImage::new(w, h);
        for p in img.pixels_mut() {
            *p = image::Rgba(rgba);
        }
        let mut png = Vec::new();
        img.write_to(&mut std::io::Cursor::new(&mut png), image::ImageFormat::Png)
            .unwrap();
        png
    }

    #[test]
    fn loads_modern_skin() {
        let skin = Skin::load(&solid_png(64, 64, [255, 0, 0, 255]), None).unwrap();
        assert!(!skin.legacy);
        assert_eq!(skin.texture.width, 64);
        assert_eq!(skin.texture.pixels.len(), 64 * 64);
    }

    #[test]
    fn rejects_bad_dimensions() {
        let mut png = solid_png(64, 64, [255, 0, 0, 255]);
        assert!(Skin::load(&solid_png(4, 4, [0, 0, 0, 255]), None).is_err());
        png[16..20].copy_from_slice(&100u32.to_be_bytes());
        assert!(Skin::load(&png, None).is_err());
    }

    #[test]
    fn transparent_arm_strips_detect_slim() {
        let mut img = image::RgbaImage::new(64, 64);
        for p in img.pixels_mut() {
            *p = image::Rgba([255, 0, 0, 255]);
        }
        // 经典布局下 (50..52, 16..20) 属于右臂外侧，应为不透明；
        // 清空它即满足纤细判定的透明条件。
        for y in 16..20 {
            for x in 50..52 {
                img.get_pixel_mut(x, y)[3] = 0;
            }
        }
        assert_eq!(detect_arm(&img), ArmModel::Slim);
        for y in 16..20 {
            for x in 50..52 {
                img.get_pixel_mut(x, y)[3] = 255;
            }
        }
        assert_eq!(detect_arm(&img), ArmModel::Classic);
    }

    #[test]
    fn cape_rejects_skin_shapes() {
        // 64×64 不满足 2:1 披风比例，尺寸门直接拒绝。
        assert!(load_cape(&solid_png(64, 64, [255, 0, 0, 255])).is_err());
    }

    #[test]
    fn cape_rejects_oversized_dimensions() {
        // 只改尺寸声明即可触发上限检查，无需真实解码。
        let mut png = solid_png(64, 32, [0, 0, 0, 255]);
        png[16..20].copy_from_slice(&2048u32.to_be_bytes());
        png[20..24].copy_from_slice(&1024u32.to_be_bytes());
        assert!(load_cape(&png).is_err());
    }
}
