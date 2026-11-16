//! Android JNI 桥，`com.movtery.petlib.PetLibNative` 的原生实现。

use std::panic::{catch_unwind, AssertUnwindSafe};

use jni::objects::{JByteArray, JClass, JObject, JString};
use jni::sys::{jboolean, jfloat, jint, jlong};
use jni::JNIEnv;

use crate::animation::AnimationId;
use crate::api::{LightingParams, Model, PetModel};
use crate::scene::{Camera, CameraMode, Scene};
use crate::skin::ArmModel;

use glam::Vec3;

mod bitmap {
    use jni::objects::JObject;
    use jni::sys::jobject;
    use jni::sys::JNIEnv as RawJNIEnv;
    use jni::JNIEnv;
    use std::ffi::c_void;
    use std::slice;

    /// ANDROID_BITMAP_FORMAT_RGBA_8888
    const FORMAT_RGBA_8888: i32 = 1;

    #[repr(C)]
    struct AndroidBitmapInfo {
        width: u32,
        height: u32,
        stride: u32,
        format: i32,
        flags: u32,
    }

    #[link(name = "jnigraphics")]
    extern "C" {
        fn AndroidBitmap_getInfo(
            env: *mut RawJNIEnv,
            bitmap: jobject,
            info: *mut AndroidBitmapInfo,
        ) -> i32;
        fn AndroidBitmap_lockPixels(
            env: *mut RawJNIEnv,
            bitmap: jobject,
            addr: *mut *mut c_void,
        ) -> i32;
        fn AndroidBitmap_unlockPixels(env: *mut RawJNIEnv, bitmap: jobject) -> i32;
    }

    /// 锁定位图并返回其像素切片，要求 RGBA_8888、尺寸匹配且行连续。
    /// 切片存活至 `unlock`，期间位图不得交回渲染管线。
    pub(super) fn lock(
        env: &mut JNIEnv,
        bitmap: &JObject,
        width: usize,
        height: usize,
    ) -> Result<&'static mut [u8], String> {
        let raw = bitmap.as_raw();
        let env_ptr = env.get_raw();
        let mut info = AndroidBitmapInfo {
            width: 0,
            height: 0,
            stride: 0,
            format: 0,
            flags: 0,
        };
        // SAFETY: info 指向本栈上合法结构，env/raw 为当前线程有效句柄。
        if unsafe { AndroidBitmap_getInfo(env_ptr, raw, &mut info) } != 0 {
            return Err("AndroidBitmap_getInfo failed.".into());
        }
        if info.format != FORMAT_RGBA_8888
            || info.width as usize != width
            || info.height as usize != height
            || info.stride != info.width * 4
        {
            return Err(format!(
                "Bitmap mismatch: format={} {}x{} stride={}, \
                 expected RGBA_8888 {width}x{height} with packed rows.",
                info.format, info.width, info.height, info.stride
            ));
        }
        let mut addr: *mut c_void = std::ptr::null_mut();
        // SAFETY: addr 指向本栈上合法指针槽。
        if unsafe { AndroidBitmap_lockPixels(env_ptr, raw, &mut addr) } != 0 || addr.is_null() {
            return Err("AndroidBitmap_lockPixels failed.".into());
        }
        // SAFETY: stride == width*4 保证像素按行连续；切片被宿主约定
        // 约束在 lock/unlock 区间内使用。
        Ok(unsafe { slice::from_raw_parts_mut(addr.cast::<u8>(), width * height * 4) })
    }

    pub(super) fn unlock(env: &mut JNIEnv, bitmap: &JObject) {
        // SAFETY: env/raw 为当前线程有效句柄。
        unsafe { AndroidBitmap_unlockPixels(env.get_raw(), bitmap.as_raw()) };
    }
}

/// 臂型编码
/// -1 自动检测，0 经典，1 纤细
fn arm_from(code: jint) -> Option<ArmModel> {
    match code {
        0 => Some(ArmModel::Classic),
        1 => Some(ArmModel::Slim),
        _ => None,
    }
}

/// 动画编码
/// 0 待机，1 行走
fn animation_from(code: jint) -> Option<AnimationId> {
    match code {
        0 => Some(AnimationId::Idle),
        1 => Some(AnimationId::Walking),
        _ => None,
    }
}

/// # Safety
/// `ptr` 必须是此前 `nativeCreate` 返回且尚未销毁的句柄，或为 0。
unsafe fn model_from(ptr: jlong) -> Option<&'static mut PetModel> {
    if ptr == 0 {
        None
    } else {
        Some(&mut *(ptr as *mut PetModel))
    }
}

/// # Safety
/// 同 `model_from`：句柄未销毁且由宿主渲染线程独占使用。
unsafe fn scene_from(ptr: jlong) -> Option<&'static mut Scene> {
    if ptr == 0 {
        None
    } else {
        Some(&mut *(ptr as *mut Scene))
    }
}

/// 读取 Java 字符串，null 或解码失败返回 None。
fn read_jstring(env: &mut JNIEnv, s: &JString) -> Option<String> {
    if s.is_null() {
        return None;
    }
    env.get_string(s).ok().map(|js| String::from(js))
}

/// 场景 + 实例双定位（实例级 JNI 操作的公共前置）。
///
/// # Safety
/// 句柄由宿主渲染线程独占使用。
unsafe fn scene_model(scene: jlong, id: jint) -> Result<&'static mut Model, String> {
    let scene = scene_from(scene).ok_or_else(|| "Invalid scene handle.".to_string())?;
    scene
        .model_mut(id as usize)
        .ok_or_else(|| format!("Invalid instance handle {id}."))
}

/// 剪辑操作的公共骨架：定位实例 → 按名取动画对象 → 执行。
/// Some(错误) = 实例/剪辑不存在或名称为空；None = 成功。
fn with_clip(
    env: &mut JNIEnv,
    scene: jlong,
    id: jint,
    name: &JString,
    op: impl FnOnce(&mut crate::api::Animation<'_>),
) -> Option<String> {
    let Some(name) = read_jstring(env, name) else {
        return Some("Clip name is required.".to_string());
    };
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let model = match unsafe { scene_model(scene, id) } {
            Ok(model) => model,
            Err(message) => return Some(message),
        };
        match model.animation(&name) {
            Some(mut anim) => {
                op(&mut anim);
                None
            }
            None => Some(format!("Clip {name:?} not found.")),
        }
    })
}

fn read_bytes(env: &mut JNIEnv, array: &JByteArray) -> Option<Vec<u8>> {
    if array.is_null() {
        return None;
    }
    let len = env.get_array_length(array).ok()? as usize;
    let mut buf = vec![0u8; len];
    // SAFETY: jbyte 与 u8 尺寸对齐，长度取自数组本身。
    let as_jbyte =
        unsafe { std::slice::from_raw_parts_mut(buf.as_mut_ptr() as *mut jni::sys::jbyte, len) };
    env.get_byte_array_region(array, 0, as_jbyte).ok()?;
    Some(buf)
}

/// panic 兜底：渲染线程的恐慌不允许穿越 JNI 边界，失败时返回类型默认值。
fn guard<R: Default>(f: impl FnOnce() -> R) -> R {
    catch_unwind(AssertUnwindSafe(f)).unwrap_or_default()
}

/// 以 `IllegalArgumentException` 抛出消息；抛出失败时静默。
fn throw(env: &mut JNIEnv, message: &str) {
    let _ = env.throw_new("java/lang/IllegalArgumentException", message);
}

fn error_or_null<'local>(env: &mut JNIEnv<'local>, error: Option<String>) -> JString<'local> {
    match error {
        Some(message) => env
            .new_string(message.as_str())
            .unwrap_or_else(|_| env.new_string("Failed to apply.").unwrap()),
        None => JString::default(),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeCreate<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    skin: JByteArray,
    arm: jint,
) -> jlong {
    guard(|| {
        let Some(bytes) = read_bytes(&mut env, &skin) else {
            throw(&mut env, "Skin data is required.");
            return 0;
        };
        match PetModel::new(&bytes, arm_from(arm)) {
            Ok(model) => Box::into_raw(Box::new(model)) as jlong,
            Err(e) => {
                throw(&mut env, &e);
                0
            }
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeDestroy(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    guard(|| {
        // SAFETY: 宿主约定句柄只销毁一次。
        if ptr != 0 {
            drop(unsafe { Box::from_raw(ptr as *mut PetModel) });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetSkin<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    ptr: jlong,
    bytes: JByteArray,
    arm: jint,
) -> JString<'local> {
    let error = guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Some(model) = (unsafe { model_from(ptr) }) else {
            return Some("Invalid model handle.".to_string());
        };
        let Some(data) = read_bytes(&mut env, &bytes) else {
            return Some("Skin data is required.".to_string());
        };
        model.set_skin(&data, arm_from(arm)).err()
    });
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetCape<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    ptr: jlong,
    bytes: JByteArray,
) -> JString<'local> {
    let error = guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Some(model) = (unsafe { model_from(ptr) }) else {
            return Some("Invalid model handle.".to_string());
        };
        let data = read_bytes(&mut env, &bytes);
        model.set_cape(data.as_deref()).err()
    });
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetLighting(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    azimuth: jfloat,
    elevation: jfloat,
    brightness: jfloat,
    softness: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_lighting(LightingParams {
                azimuth,
                elevation,
                brightness,
                softness,
            });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetSuperSample(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    value: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_super_sample(value);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetCameraDistance(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    value: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_camera_distance(value);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetOuterLayer(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    enabled: jboolean,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_outer_layer(enabled != 0);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetTau(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    gaze: jfloat,
    yaw: jfloat,
    anim: jfloat,
    recenter: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_tau(gaze, yaw, anim, recenter);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetHeadRange(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    x: jfloat,
    y: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_head_range(x, y);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetSquashDepth(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    depth: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_squash_depth(depth);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetGaze(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    x: jfloat,
    y: jfloat,
    has_target: jboolean,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_gaze(if has_target != 0 { Some((x, y)) } else { None });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSetYaw(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    yaw: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.set_model_yaw(yaw);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeGetYaw(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) -> jfloat {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        unsafe { model_from(ptr) }.map_or(0., |model| model.model_yaw())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativePlayAnimation(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    id: jint,
    speed: jfloat,
    head_follow: jboolean,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let (Some(model), Some(id)) = (unsafe { model_from(ptr) }, animation_from(id)) {
            model.play_animation(id, speed, head_follow != 0);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeStopAnimation(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.stop_animation();
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSquashTap(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    now: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.squash_tap(now);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativePlaySwing(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    now: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.play_swing(now);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativePlayInteract(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(model) = unsafe { model_from(ptr) } {
            model.play_interact();
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeRender<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    ptr: jlong,
    width: jint,
    height: jint,
    now: jfloat,
    dt: jfloat,
    bitmap: JObject<'local>,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Some(model) = (unsafe { model_from(ptr) }) else {
            return;
        };
        let pixels = match bitmap::lock(&mut env, &bitmap, width as usize, height as usize) {
            Ok(pixels) => pixels,
            Err(message) => {
                throw(&mut env, &message);
                return;
            }
        };
        // 即使渲染 panic 也要解锁位图，锁 panic 由外层 guard 兜底。
        let result = catch_unwind(AssertUnwindSafe(|| {
            model.render(width as usize, height as usize, now, dt, pixels)
        }));
        bitmap::unlock(&mut env, &bitmap);
        if let Ok(Err(e)) = result {
            throw(&mut env, &e);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeHitTest(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    x: jfloat,
    y: jfloat,
) -> jboolean {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        unsafe { model_from(ptr) }.is_some_and(|model| model.hit_test(x, y)) as jboolean
    })
}

// ==================== 场景桥（Scene / Model / Animation） ====================
//
// 句柄契约与 PetModel 系列一致：所有句柄仅宿主渲染线程使用。
// 模型句柄为两段式生命周期：nativeModelCreate* 在调用线程产出并持有，
// nativeSceneAddModel 消费该句柄（所有权一次性转入场景），
// 此后模型由场景独占，随 nativeSceneRemoveModel 或场景销毁而释放。

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneCreate(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    guard(|| Box::into_raw(Box::new(Scene::new())) as jlong)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneDestroy(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    guard(|| {
        // SAFETY: 宿主约定句柄只销毁一次。
        if ptr != 0 {
            drop(unsafe { Box::from_raw(ptr as *mut Scene) });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeModelCreatePlayer<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    skin: JByteArray,
    arm: jint,
) -> jlong {
    guard(|| {
        let Some(bytes) = read_bytes(&mut env, &skin) else {
            throw(&mut env, "Skin data is required.");
            return 0;
        };
        match Model::builtin_player(&bytes, arm_from(arm)) {
            Ok(model) => Box::into_raw(Box::new(model)) as jlong,
            Err(e) => {
                throw(&mut env, &e);
                0
            }
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeModelCreateProp<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    glb: JByteArray,
) -> jlong {
    guard(|| {
        let Some(bytes) = read_bytes(&mut env, &glb) else {
            throw(&mut env, "GLB data is required.");
            return 0;
        };
        match Model::from_glb(&bytes) {
            Ok(model) => Box::into_raw(Box::new(model)) as jlong,
            Err(e) => {
                throw(&mut env, &e);
                0
            }
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeModelCreatePropWithSkin<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    glb: JByteArray,
    skin: JByteArray,
    arm: jint,
) -> jlong {
    guard(|| {
        let (Some(glb), Some(skin)) = (read_bytes(&mut env, &glb), read_bytes(&mut env, &skin))
        else {
            throw(&mut env, "GLB and skin data are required.");
            return 0;
        };
        match Model::from_glb_with_skin(&glb, &skin, arm_from(arm)) {
            Ok(model) => Box::into_raw(Box::new(model)) as jlong,
            Err(e) => {
                throw(&mut env, &e);
                0
            }
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeModelDestroy(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    guard(|| {
        // SAFETY: 宿主约定未加入场景的模型句柄只销毁一次。
        if ptr != 0 {
            drop(unsafe { Box::from_raw(ptr as *mut Model) });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneAddModel(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    model: jlong,
) -> jint {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Some(scene) = (unsafe { scene_from(scene) }) else {
            return -1;
        };
        if model == 0 {
            return -1;
        }
        // SAFETY: 模型句柄由本调用消费，所有权仅此一次转入场景。
        let model = unsafe { Box::from_raw(model as *mut Model) };
        scene.add(*model) as jint
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneRemoveModel(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(scene) = unsafe { scene_from(scene) } {
            drop(scene.remove(id as usize));
        }
    })
}

/// 相机模式编码：0 正交（zoom_or_fov = 屏宽覆盖的世界单位数），
/// 1 透视（zoom_or_fov = 垂直视场角，度）。
#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetCamera(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    tx: jfloat,
    ty: jfloat,
    tz: jfloat,
    azimuth: jfloat,
    elevation: jfloat,
    distance: jfloat,
    mode: jint,
    zoom_or_fov: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Some(scene) = (unsafe { scene_from(scene) }) else {
            return;
        };
        let mode = match mode {
            0 => CameraMode::Ortho {
                zoom: zoom_or_fov.max(1e-3),
            },
            1 => CameraMode::Perspective {
                fov: zoom_or_fov.clamp(1., 179.),
            },
            _ => return,
        };
        scene.set_camera(Camera {
            target: Vec3::new(tx, ty, tz),
            azimuth,
            elevation,
            distance,
            mode,
        });
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetLighting(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    azimuth: jfloat,
    elevation: jfloat,
    brightness: jfloat,
    softness: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(scene) = unsafe { scene_from(scene) } {
            scene.set_lighting(LightingParams {
                azimuth,
                elevation,
                brightness,
                softness,
            });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetSuperSample(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    value: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Some(scene) = unsafe { scene_from(scene) } {
            scene.set_super_sample(value);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetSkin<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    bytes: JByteArray,
    arm: jint,
) -> JString<'local> {
    let error = guard(|| {
        let Some(data) = read_bytes(&mut env, &bytes) else {
            return Some("Skin data is required.".to_string());
        };
        // SAFETY: 句柄由宿主渲染线程独占使用。
        match unsafe { scene_model(scene, id) } {
            Ok(model) => model.set_skin(&data, arm_from(arm)).err(),
            Err(message) => Some(message),
        }
    });
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetCape<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    bytes: JByteArray,
) -> JString<'local> {
    let error = guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        match unsafe { scene_model(scene, id) } {
            Ok(model) => model.set_cape(read_bytes(&mut env, &bytes).as_deref()).err(),
            Err(message) => Some(message),
        }
    });
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetTexture<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    material: JString<'local>,
    bytes: JByteArray,
) -> JString<'local> {
    let error = guard(|| {
        let (Some(material), Some(data)) = (
            read_jstring(&mut env, &material),
            read_bytes(&mut env, &bytes),
        ) else {
            return Some("Material name and texture data are required.".to_string());
        };
        // SAFETY: 句柄由宿主渲染线程独占使用。
        match unsafe { scene_model(scene, id) } {
            Ok(model) => model.set_texture(&material, &data).err(),
            Err(message) => Some(message),
        }
    });
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetPosition(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    x: jfloat,
    y: jfloat,
    z: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.set_position(x, y, z);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetRotation(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    pitch: jfloat,
    yaw: jfloat,
    roll: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.set_rotation_degrees(pitch, yaw, roll);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetScale(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    scale: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.set_scale(scale);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetOuterLayer(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    enabled: jboolean,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.set_outer_layer(enabled != 0);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetGazeScreen(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    x: jfloat,
    y: jfloat,
    has_target: jboolean,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.set_gaze_screen(if has_target != 0 { Some((x, y)) } else { None });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSetGazeScene(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    x: jfloat,
    y: jfloat,
    z: jfloat,
    has_target: jboolean,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.set_gaze_scene(if has_target != 0 { Some([x, y, z]) } else { None });
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipPlay<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString<'local>,
    speed: jfloat,
) -> JString<'local> {
    let error = with_clip(&mut env, scene, id, &name, |anim| anim.play(speed));
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipPause<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString<'local>,
) -> JString<'local> {
    let error = with_clip(&mut env, scene, id, &name, |anim| anim.pause());
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipStop<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString<'local>,
) -> JString<'local> {
    let error = with_clip(&mut env, scene, id, &name, |anim| anim.stop());
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipSetSpeed<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString<'local>,
    speed: jfloat,
) -> JString<'local> {
    let error = with_clip(&mut env, scene, id, &name, |anim| anim.set_speed(speed));
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipSetLoop<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString<'local>,
    loop_: jboolean,
) -> JString<'local> {
    let error = with_clip(&mut env, scene, id, &name, |anim| anim.set_loop(loop_ != 0));
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipSetWeight<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString<'local>,
    weight: jfloat,
) -> JString<'local> {
    let error = with_clip(&mut env, scene, id, &name, |anim| anim.set_weight(weight));
    error_or_null(&mut env, error)
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneClipIsPlaying(
    mut env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    name: JString,
) -> jboolean {
    guard(|| {
        let Some(name) = read_jstring(&mut env, &name) else {
            return 0;
        };
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Ok(model) = (unsafe { scene_model(scene, id) }) else {
            return 0;
        };
        model
            .animation(&name)
            .is_some_and(|anim| anim.is_playing()) as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneSquashTap(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
    now: jfloat,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.skeleton.squash_tap(now);
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeScenePlayInteract(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    id: jint,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        if let Ok(model) = unsafe { scene_model(scene, id) } {
            model.skeleton.play_interact();
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneRender<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    scene: jlong,
    width: jint,
    height: jint,
    dt: jfloat,
    bitmap: JObject<'local>,
) {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        let Some(scene) = (unsafe { scene_from(scene) }) else {
            return;
        };
        let pixels = match bitmap::lock(&mut env, &bitmap, width as usize, height as usize) {
            Ok(pixels) => pixels,
            Err(message) => {
                throw(&mut env, &message);
                return;
            }
        };
        // 即使渲染 panic 也要解锁位图，锁 panic 由外层 guard 兜底。
        let result = catch_unwind(AssertUnwindSafe(|| {
            scene.render(width as usize, height as usize, dt, pixels)
        }));
        bitmap::unlock(&mut env, &bitmap);
        if let Ok(Err(e)) = result {
            throw(&mut env, &e);
        }
    })
}

/// 归一化坐标拾取，返回命中实例句柄；未命中返回 -1。
#[no_mangle]
pub extern "system" fn Java_com_movtery_petlib_PetLibNative_nativeSceneHitTest(
    _env: JNIEnv,
    _class: JClass,
    scene: jlong,
    x: jfloat,
    y: jfloat,
) -> jint {
    guard(|| {
        // SAFETY: 句柄由宿主渲染线程独占使用。
        unsafe { scene_from(scene) }
            .and_then(|scene| scene.hit_test(x, y))
            .map_or(-1, |id| id as jint)
    })
}
