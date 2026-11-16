//! 皮肤/披风纹理的解析、校验与处理。

use image::RgbaImage;

/// 手臂类型
#[derive(Clone, Copy, PartialEq, Eq, Hash, Debug)]
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

impl std::fmt::Debug for Texture {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("Texture")
            .field("width", &self.width)
            .field("height", &self.height)
            .finish()
    }
}

impl Texture {
    /// 解码 PNG 纹理。
    pub fn decode(bytes: &[u8]) -> Result<Self, String> {
        let image = image::load_from_memory_with_format(bytes, image::ImageFormat::Png)
            .map_err(|e| e.to_string())?
            .into_rgba8();
        Ok(Self::from_image(image))
    }

    /// 按内容嗅探解码（PNG/JPEG），供 glTF 内嵌贴图使用。
    pub(crate) fn decode_any(bytes: &[u8]) -> Result<Self, String> {
        let image = image::load_from_memory(bytes)
            .map_err(|e| e.to_string())?
            .into_rgba8();
        Ok(Self::from_image(image))
    }

    /// 展平为像素四元组：单份存储，解析后即可释放解码图像。
    pub(crate) fn from_image(image: RgbaImage) -> Self {
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

/// 解析后的皮肤：纹理、是否旧版 64×32、臂型。
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
            image = modernize_legacy(&image);
        }
        // 基础层不允许透明：强行补齐头/身/臂/腿所在行（重排后的左肢区域
        // 已由右肢镜像填充，此步为幂操作）。
        let regions = [(0, 0, 32, 16), (0, 16, 56, 16), (16, 48, 32, 16)];
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

/// 旧版 64×32 布局重排为现代 64×64 方形：上半原样复制；右肢基础层
/// 按面级拷贝填充左肢基础区域；左肢外层区域保持透明（旧版无外层数据）。
///
/// 翻转规则对齐旧渲染器的镜像语义（几何 x 翻转 + UV 区域不变）：
/// 前/后/上/下面的 u 轴沿 x，镜像后水平翻转；内/外侧面的 u 轴沿 z，
/// 镜像不改变方向，故不翻转。
fn modernize_legacy(image: &RgbaImage) -> RgbaImage {
    let (w, h) = (image.width(), image.height());
    let k = (w / 64) as i32;
    let mut out = RgbaImage::new(w, w);
    // 上半原样复制
    for y in 0..h {
        for x in 0..w {
            out.put_pixel(x, y, *image.get_pixel(x, y));
        }
    }
    // (src_x, src_y, dst_x, dst_y, 面宽, 面高)：全部水平翻转
    // （角点级核对：镜像几何使所有面的采样 u 方向反向）。
    const COPIES: [(i32, i32, i32, i32, i32, i32); 12] = [
        (4, 16, 20, 48, 4, 4),   // 腿顶
        (8, 16, 24, 48, 4, 4),   // 腿底
        (0, 20, 24, 52, 4, 12),  // 腿外侧
        (4, 20, 20, 52, 4, 12),  // 腿前
        (8, 20, 16, 52, 4, 12),  // 腿内侧
        (12, 20, 28, 52, 4, 12), // 腿后
        (44, 16, 36, 48, 4, 4),  // 臂顶
        (48, 16, 40, 48, 4, 4),  // 臂底
        (40, 20, 40, 52, 4, 12), // 臂外侧
        (44, 20, 36, 52, 4, 12), // 臂前
        (48, 20, 32, 52, 4, 12), // 臂内侧
        (52, 20, 44, 52, 4, 12), // 臂后
    ];
    for (sx, sy, dx, dy, fw, fh) in COPIES {
        let (sx, sy, dx, dy, fw, fh) = (sx * k, sy * k, dx * k, dy * k, fw * k, fh * k);
        for yy in 0..fh {
            for xx in 0..fw {
                let pixel = *image.get_pixel((sx + xx) as u32, (sy + yy) as u32);
                out.put_pixel((dx + fw - 1 - xx) as u32, (dy + yy) as u32, pixel);
            }
        }
    }
    out
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
    fn legacy_skin_modernizes_with_mirrored_left_limbs() {
        // 64×32 合成皮肤：每个 4×4 面填不同颜色，验证面级镜像映射
        let mut img = image::RgbaImage::new(64, 32);
        for p in img.pixels_mut() {
            *p = image::Rgba([0, 0, 0, 255]);
        }
        let face = |img: &mut image::RgbaImage, x: u32, y: u32, w: u32, h: u32, c: [u8; 4]| {
            for yy in y..y + h {
                for xx in x..x + w {
                    img.get_pixel_mut(xx, yy).0 = c;
                }
            }
        };
        // 右臂各面：顶/底/外/前/内/后
        face(&mut img, 44, 16, 4, 4, [10, 0, 0, 255]);
        face(&mut img, 48, 16, 4, 4, [20, 0, 0, 255]);
        face(&mut img, 40, 20, 4, 12, [30, 0, 0, 255]);
        face(&mut img, 44, 20, 4, 12, [40, 0, 0, 255]);
        face(&mut img, 48, 20, 4, 12, [50, 0, 0, 255]);
        face(&mut img, 52, 20, 4, 12, [60, 0, 0, 255]);
        let out = modernize_legacy(&img);
        assert_eq!(out.width(), 64);
        assert_eq!(out.height(), 64);
        // 上半原样保留
        assert_eq!(out.get_pixel(0, 0).0, [0, 0, 0, 255]);
        assert_eq!(out.get_pixel(44, 20).0, [40, 0, 0, 255]);
        // 左臂前面 = 右臂前面水平翻转（同色面，用逐列颜色区分验证翻转）
        let mut grad = image::RgbaImage::new(64, 32);
        for p in grad.pixels_mut() {
            *p = image::Rgba([0, 0, 0, 255]);
        }
        for xx in 0..4u32 {
            for yy in 0..12u32 {
                // 内/外/前三条带都写入按列渐变，验证翻转方向
                grad.get_pixel_mut(44 + xx, 20 + yy).0 = [xx as u8, 0, 0, 255];
                grad.get_pixel_mut(48 + xx, 20 + yy).0 = [xx as u8, 0, 0, 255];
                grad.get_pixel_mut(40 + xx, 20 + yy).0 = [xx as u8, 0, 0, 255];
            }
        }
        let out2 = modernize_legacy(&grad);
        // 左臂前面 (36,52)-(40,64)：第 xx 列应取源第 (3-xx) 列
        for xx in 0..4u32 {
            assert_eq!(out2.get_pixel(36 + xx, 52).0[0], 3 - xx as u8, "arm front should be mirrored");
        }
        // 左臂内侧 (32,52) = 右臂内侧 (48,20) 翻转
        assert_eq!(out2.get_pixel(32, 52).0, [3, 0, 0, 255]);
        // 左臂外侧 (40,52) = 右臂外侧 (40,20) 翻转
        assert_eq!(out2.get_pixel(40, 52).0, [3, 0, 0, 255]);
        // 外层区域保持透明
        assert_eq!(out2.get_pixel(48, 48).0[3], 0);
        assert_eq!(out2.get_pixel(0, 48).0[3], 0);
        assert_eq!(out2.get_pixel(16, 32).0[3], 0);
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
