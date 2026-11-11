# ZalithLauncher2 — แผนที่โปรเจกต์ (PROJECT_MAP)

> อ้างอิงจาก `toymr5182/ZalithLauncher2-Remake-By-Serloss` @ commit `4607f15` (shallow clone)
> ทุก path ในเอกสารนี้เป็น relative จาก `ZalithLauncher/src/main/java/com/movtery/zalithlauncher/`
> เว้นแต่จะระบุไว้เป็นอย่างอื่น

---

## 1. โปรเจกต์นี้คืออะไร

**Zalith Launcher 2 (ZL2)** — ตัวเปิดเกม Minecraft: Java Edition บน Android

| ส่วน | เทคโนโลยี |
|---|---|
| UI | Jetpack Compose + Material3, navigation3 (back stack เอง) |
| เอนจินรันเกม | PojavLauncher-style JNI (LWJGL → GLFW/SDL/EGL bridge) |
| โค้ดฝั่ง Java | HMCL code (`org.jackhuang.hmcl`) บางส่วน, SDL4Android (`org.libsdl.app`) |
| เก็บค่าตั้ง | MMKV (`setting/_MMKV.kt`) |
| ฐานข้อมูล | Room + SQLCipher (`database/AppDatabase.kt`) |
| NDK | C/C++ ใต้ `src/main/jni/` (prebuilt `.so` ใน `src/main/jniLibs/<abi>/`) |
| License | GPLv3 — **แก้แล้วต้องเปิดซอร์ส** |

เวอร์ชันปัจจุบัน: `launcher_version_code=200049`, `launcher_version_name=2.8.2`
(`ZalithLauncher/gradle.properties`)

---

## 2. โครงสร้างโมดูล (`settings.gradle.kts`)

```
:ZalithLauncher      ← ตัวแอป 99% ของงานอยู่ที่นี่
:LWJGL               ← ชิม LWJGL ที่ถูกแพตช์ให้รันบน Android
  :LWJGL:patches         CursorRegistry / SdlCursorRegistry (แทน cursor ของ GLFW)
  :LWJGL:lwjgl-3.3.3     jar ที่ประกอบจาก source + patch
  :LWJGL:lwjgl-3.4.1     (ตัวใหม่ เพิ่ม lwjgl-sdl + spng)
:LayerController     ← โมดูลควบคุม layout/ปุ่มลอย (ObservableControlLayout)
:ColorPicker         ← สี picker
:CardGrid            ← กริดแบบการ์ด (มี unit test ในตัว)
:Terracotta          ← Ktor + KCP networking (multiplayer relay)
:InputMap            ← keycode map (LwjglGlfwKeycode)
:Guide               ← หน้า guide/help
:PetLib              ← Rust (มี tools) — pet พากย์/กราฟิกประกอบ
```

Gradle: AGP `9.3.0`, Kotlin `2.4.21`, `compileSdk 37`, `minSdk 26`, `targetSdk 34`
(`gradle/libs.versions.toml`)

**ขนาด**: repo ~900MB, app module 528MB ส่วนใหญ่คือ `assets/runtimes` (158MB) และ
`assets/app_runtime` (22MB) และ jniLibs — เวลา clone/บิลด์ต้องเผื่อพื้นที่

---

## 3. เส้นทางเริ่มทำงาน (Startup)

```
ZLApplication (ZLApplication.kt)
  └─ init: PathManager.init / SettingsInitializer / GamePathManager / VersionsManager ...
SplashActivity (ui/activities/SplashActivity.kt, 424 บรรทัด)
  ├─ ตรวจ permission, แตก component/JRE ที่ยังไม่มี, ตรวจอัปเดต
  └─ ไป MainActivity
MainActivity (ui/activities/MainActivity.kt, 812 บรรทัด)
  ├─ Compose setContent + NavStateHost (navigation3)
  └─ เลือก version → VMActivity
VMActivity (ui/activities/VMActivity.kt, 1182 บรรทัด)  ← ตัวรันเกมจริง
  ├─ สร้าง SurfaceView, handler = GameHandler | JVMHandler
  ├─ handler.execute(surface, screenSize, scope) → launcher.launch()
  ├─ handler.ComposableLayout() = ปุ่มลอย/mouse/hotbar ทับจอเกม
  └─ onExit(code) → กลับ/โชว์ crash log
```

จุดสำคัญ: `VMActivity` **ไม่มี UI หลัก** นอกจาก surface + overlay ที่ handler แต่ละตัววาด

---

## 4. Launch pipeline (หัวใจของโปรเจกต์)

โฟลเดอร์: `game/launch/`

| ไฟล์ | หน้าที่ |
|---|---|
| `GameLaunchFlow.kt` (312) | คุมลำดับเฟสทั้งหมด (task queue) |
| `Launcher.kt` (605) | ประกอบ JVM args, classpath, env, เรียก `GameLauncher` |
| `GameLauncher.kt` (630) | เตรียม renderer/LWJGL/component, เรียก native |
| `GameService.kt` | foreground service กันระบบฆ่า |
| `JvmLauncher.kt`, `JvmLaunchInfo.kt` | โหมด "รัน JAR ทั่วไป" (ไม่ใช่ MC) |
| `LaunchArgs.kt` | สร้าง argument list ทั้งหมด (JVM + MC + quick play) |
| `MCOptions.kt` | แปลง options.txt |
| `LibSortFix.kt` | จัดลำดับ jar ใน classpath (แก้ conflict) |
| `handler/AbstractHandler.kt` | base: `execute`, `ComposableLayout`, `onPause/Resume/Destroy`, `shouldIgnoreKeyEvent`, `sendMouseRight` |
| `handler/GameHandler.kt` (232+) | เกม MC: overlay ครบ (control layout, mouse, hotbar, keyboard) |
| `handler/JVMHandler.kt` | โหมด JVM ทั่วไป: overlay แบบง่าย |

**เฟสใน `GameLaunchFlow`** (ตามลำดับ): ตรวจ account → สร้าง/แพตช์ version manifest
(`game/version/manifest/`, HMCL `VersionPatch`, Lwjgl3ify, Forge/NeoForge installer) →
ดาวน์โหลด lib (`GameLibDownloader`) → ตรวจ integrity (`VersionVerifier`) →
ประกอบ args → `runGame`

**`LaunchArgs`** สร้าง 4 ก้อน: JVM args (manifest), `-cp` (LWJGL แยกจาก libs เกม),
MC client args, quick-play args — ตัวแปรแทนที่ `varArgMap`
(`auth_player_name`, `game_directory`, `assets_root`, `version_type` ฯลฯ)

---

## 5. Native / JNI layer

Java/Kotlin side:
- `bridge/ZLBridge.java` — จุดเรียก native หลัก (`setupBridgeWindow`, `sendInputData`,
  `renderAWTScreenFrame`, clipboard, `nativeInitialize`)
- `bridge/ZLNativeInvoker.kt` — ฟังก์ชันที่ **native เรียกกลับเข้า Kotlin**
- `bridge/ZLBridgeStates.kt` — สถานะ cursor/input ที่ native อ่าน
- `bridge/NativeLibraryLoader.java`, `bridge/LoggerBridge`
- `bridge/ZLPathRedirector.java` — path redirect ของ SdlIme
- `org/lwjgl/glfw/CallbackBridge.java` (563) — คิว input ทั้งหมด + `notifyLauncher`
  (ตัวเปิด SDL3/SDL2 integration) + clipboard
- `org/libsdl/app/*` — SDLActivity 2195 บรรทัด (แก้เฉพาะเมื่อจำเป็นจริงๆ)

Native (`src/main/jni/`):
```
ctxbridges/        egl_bridge, gl_bridge, vulkan_bridge, xawt_bridge  ← ตัววาดจอ
driver_helper/     Vulkan driver (turnip) loader
environ/           env/argv, Android env patch
linkerhook/        dlopen/linker internals
jsound/            OpenAL/audio hooks
flite/             TTS (อ่านข้อความเกม)
awt_xawt/          Java AWT (สำหรับ mod ที่ใช้ Swing/AWT)
jre_launcher.c, egl_bridge.c, input_bridge_v3.c
bigcoreaffinity.c, exit_hook.c
```
NDK build: `Android.mk` + `Application.mk`; `.so` prebuilt อยู่ใน `jniLibs/{arm64-v8a,armeabi-v7a,x86,x86_64}`

**แก้ native ต้องมี NDK + prebuilt libs ครบ** — ใน Termux เครื่องนี้ยังไม่ได้ตรวจว่ามี

---

## 6. Rendering

โฟลเดอร์: `game/renderer/`

```
RendererInterface.kt      ← สัญญา: id, name, summary, requiresLibraries, isAvailable(), ComposableLayout (config UI)
ConfigurableRenderer.kt   ← base ที่มีหน้า config อัตโนมัติ
SimpleFPEWrapper.kt       ← ห่อ renderer แบบ fixed-function
RenderersList.kt          ← ทะเบียน renderer ทั้งหมด (จุดที่ต้องเพิ่มเมื่อเพิ่ม renderer ใหม่)
Renderers.kt              ← map id → instance + helper
renderers/
  MobileGluesRenderer.kt   ← MobileGlues (Zink/GL translation หลัก)
  KopperZinkRenderer.kt
  VirGLRenderer.kt
  NGGL4ESRenderer.kt
  GL4ESRenderer.kt
  FreedrenoRenderer.kt
  PanfrostRenderer.kt
config/                    ← DSL สำหรับหน้าตั้งค่า renderer
  (spec/resolver/store + composables)
```

แต่ละ renderer ประกาศ **config schema** ของตัวเอง → หน้าตั้งค่าถูกสร้างอัตโนมัติ
(`RendererConfigDialog.kt` ใน `ui/screens/content/settings/`)
ค่าที่เลือกเก็บใน `versionConfig.renderer` / `driver` / `graphicsApi`
(fallback ไป `AllSettings`)

**LWJGL**: `LaunchArgs.getLWJGL3ClassPath()` ประกอบ classpath จาก
`DIR_COMPONENTS/lwjgl/<3.3.3|3.4.1>`; เก่าใช้ LWJGL2 (version 1..299) แยกเส้นทาง
มี patch `CursorRegistry`/`SdlCursorRegistry` แทน GLFW cursor

---

## 7. Control / Input (ซับซ้อนที่สุดของ UI)

`game/control/`
- `ControlManager.kt` — โหลด/บันทึก/นำเข้า layout จาก `PathManager.DIR_CONTROL_LAYOUTS`
  (JSON, ไฟล์เริ่มต้น `assets/default_layout.json`)
- `ControlData.kt` — `data class ControlData(file, ObservableControlLayout, isSupport)`
- `StyleCabinet.kt` — สี/สไตล์ปุ่ม

`ui/control/`
| ส่วน | ไฟล์ |
|---|---|
| mouse/touch | `mouse/TouchInput.kt`, `MouseLayout.kt`, `SwitchableMouseLayout.kt`, `CursorHotspot.kt` |
| keyboard | `Keyboard.kt`, `input/*` (`InputListener`, `TextInput`, `LegacyTextInput`, `TextInputMode`) |
| gamepad | `gamepad/*` (mapping, remap UI, Joystick, `_MMKV`) |
| joystick | `joystick/JoystickDirection.kt`, `_Movement.kt` |
| gyro | `gyroscope/GyroscopeReader.kt` |
| hotbar | `Hotbar.kt` |
| event | `event/Events.kt`, `KeyEventHandler.kt` |

`game/sdl/` — `SdlBridge.kt` (เปิด/ปิด SDL path, focus, text input native),
`DirectGamepad.kt`, `SdlTextSender.kt`
`game/support/touch_controller/` — `TouchController.kt`, `ControllerProxy.kt`,
`VibrationHandler.kt` (สั่นตาม `version.getTouchVibrateDuration()`)

input ไหล: Compose overlay / SDL → `CallbackBridge.send*` → native คิว →
GLFW callback → เกม

---

## 8. Versions, paths, isolation

- `path/PathManager.kt` — path กลางทั้งหมด (`DIR_FILES_EXTERNAL`, `DIR_FILES_PRIVATE`,
  `DIR_CACHE`, `DIR_NATIVE_LIB`, `DIR_RUNTIME_MOD`, `DIR_COMPONENTS`,
  `DIR_CONTROL_LAYOUTS`, `DIR_TERRACOTTA`, ...) + `handleLegacy()`
- `game/path/GamePathManager.kt` — รองรับ **หลาย game dir** (Room `GamePathDao`),
  ค่าเริ่มต้น `external/.minecraft`, id `default`
- `game/version/installed/`
  - `Version.kt` — getter สำคัญ: `getGameDir()` (ถ้า `isIsolation()` → โฟลเดอร์ version),
    `getRenderer()/getDriver()/getGraphicsApi()/getJavaRuntime()/getRamAllocation()/getControlPath()`
    ทุกตัว fallback ไป `AllSettings`
  - `VersionsManager.kt` — scan/refresh/rename/save version (`StateFlow<List<Version>>`)
  - `VersionConfig.kt` — ตั้งค่าราย version (`vv.json`-style)
  - `VersionFolders.kt` — mods / resourcepacks / saves / shaderpacks / screenshots
  - `VersionInfoParser.kt`, `cleanup/`
- `game/multirt/` — multi runtime (JRE หลาย instance)

---

## 9. ดาวน์โหลด / component / JRE

- เกม: `game/download/game/GameInstaller.kt` (38KB, ตัวใหญ่), `GameLibDownloader`
- Addon: `game/download/addon/` + `platform/{curseforge,modrinth,...}`
  (ต้องมี `CURSEFORGE_API_KEY`)
- Component: `components/Components.kt` enum `AUTH_LIBS, CACIOCAVALLO, CACIOCAVALLO17,
  LWJGL333, LWJGL341, LAUNCHER`; `components/jre/Jre.kt`
- asset ที่แถม: `assets/components/launcher/` = `HMCLTransformerDiscoveryService-1.0.jar`,
  `MioLaunchWrapper.jar`, `MioLibPatcher.jar`, `forge_installer.jar`
- JRE: `assets/runtimes/{jre-8,17,21,25,jna}` เป็น `.tar.xz` แยก abi
  (jre-25 ไม่มี x86 — เฉพาะ arm/arm64/x86_64)

---

## 10. Accounts

`game/account/`
```
AccountsManager.kt, Account.kt, AccountDao.kt, AccountType
microsoft/      ← OAuth (ต้องมี OAUTH_CLIENT_ID), device code
offline/        ← ชื่อเดี่ยว
auth_server/    ← custom authlib-injector
yggdrasil/      ← รวมถึง offline Yggdrasil server ที่ app รันเอง (LaunchArgs)
wardrobe/       ← skin/cape (DIR_ACCOUNT_SKIN, DIR_ACCOUNT_CAPE)
```
DB: Room + SQLCipher (`database/AppDatabase.kt`, `Converters.kt`)

---

## 11. Settings system (จุดที่แก้ง่ายและบ่อย)

```
setting/SettingsRegistry.kt   ← base class: stringSetting/boolSetting/intSetting/enumSetting/
                               stringListSetting/parcelableSetting
setting/AllSettings.kt        ← ค่าทั้งหมดของแอป (single object), จัดกลุ่มด้วยคอมเมนต์:
                               Renderer / Game / Java / Log / Mouse / Gamepad / Gyroscope /
                               Launcher theme / Background / ...
setting/SettingsInitializer.kt← ค่า default + migration
setting/_MMKV.kt              ← backend
setting/enums/, setting/unit/ ← enum และหน่วย (MB, %, ...)
```
หน้า UI: `ui/screens/content/settings/` → `RendererSettingsScreen`,
`GameSettingsScreen`, `ControlSettingsScreen`, `GamepadSettingsScreen`,
`JavaManageScreen`, `LauncherSettingsScreen`, `ControlManageScreen`,
`AboutInfoScreen`, `RendererConfigDialog` (+ `layouts/`)

**เพิ่ม setting ใหม่ = 3 ที่**: `AllSettings` → composable ในหน้า settings →
(ถ้าเป็นราย version) `VersionConfig` + `Version.getX()`

---

## 12. Navigation / UI

```
ui/screens/{TitledNavKey,BackStackNavKey,NestedNavKey,NormalNavKey,NavStateHost,_Navigation}.kt
viewmodel/ScreenBackStackViewModel.kt  ← ถือ back stack ของแต่ละหน้า root:
   mainScreen, settingsScreen, downloadScreen, downloadGameScreen,
   downloadModPackScreen, downloadModScreen, downloadResourcePackScreen, downloadSavesScreen
```
จอหลัก: `ui/screens/main/MainScreen.kt` (+ `control_editor/`, `crashlogs/`)
เนื้อหา: `ui/screens/content/{home,download,versions,addoninfo,elements,settings}`
เกม: `ui/screens/game/{GameScreen,JVMScreen,multiplayer}`
ธีม: `ui/theme/` (+ `festivals/` เอฟเฟกต์เทศกาล), `ui/vulkan_checker/`

> navigation3 + back stack ที่เขียนเอง → **ห้ามใช้ `NavController` แบบเดิม**

---

## 13. TaskSystem (progress + log)

`coroutine/` — `TaskSystem.kt`, `Task.kt`, `TaskStage.kt`, `TitledTask.kt`,
`TaskFlowExecutor.kt`, `TaskLogOutput.kt`, `DataBridge.kt`
ใช้กับทุกงานยาว (ติดตั้ง/ดาวน์โหลด/แตกไฟล์) แล้วโชว์ผ่าน UI พร้อม log
→ ถ้าเพิ่มงานยาว ควรใช้ TaskSystem ไม่ใช่ coroutine เปล่า

## 14. ระบบอื่น

| ระบบ | ที่อยู่ |
|---|---|
| File manager (ในแอป) | `filemanager/` + `filemanager/ui/*` (`FileManagerActivity`, `FmMainPage`, `FmEditorScreen`, `FmTrashScreen`) |
| Terracotta (เล่น multiplayer ผ่าน relay) | `terracotta/` (`Terracotta.kt`, `TerracottaNodeList.kt`, `TerracottaState.kt`, `TerracottaVPNService.java`, `profile/`) |
| อัปเดตตัว launcher | `upgrade/` (`GithubContentApi.kt`, `RemoteData.kt`) + `ui/upgrade/` |
| Crash log | `crashlogs/` + `ui/screens/main/crashlogs/` |
| Keep alive / foreground | `keepalive/`, `game/launch/GameService.kt`, `notification/` |
| Sponsor/legal | `sponsor/`, `legal/`, `assets/legal/` |
| Guide | `ui/guide/` + module `:Guide` |
| Plugin (apk/renderer/natives/ffmpeg/driver) | `game/plugin/{ApkPlugin,PluginLoader,driver,ffmpeg,natives,renderer,renderer_v2}` |

---

## 15. Build & CI

- `.github/workflows/{build,push_ci,release_ci}.yml`
- matrix abi: `all, arm, arm64, x86, x86_64`; secrets: `KEY_PASSWORD`, `STORE_PASSWORD`,
  `OAUTH_CLIENT_ID`, `CURSEFORGE_API_KEY`
- gradle.properties มี keystore default (ใช้ dev build ได้)

**สถานะในเครื่องนี้ (Termux)**: ยังไม่ได้ลองบิลด์ — ต้องมี JDK 21+, Android SDK 37,
NDK, และพื้นที่ ~5–10GB. **ยังไม่ยืนยัน**

---

## 16. สูตร "จะแก้อะไร ไปที่ไหน"

| อยากทำ | แก้ที่ |
|---|---|
| เพิ่ม/แก้ renderer | `game/renderer/renderers/` + ลงทะเบียนใน `RenderersList.kt` |
| เพิ่มตัวเลือกในหน้า renderer | `game/renderer/config/` (schema จะสร้าง UI เอง) |
| เพิ่ม setting | `setting/AllSettings.kt` + `ui/screens/content/settings/*Screen.kt` |
| เพิ่ม setting ราย version | `VersionConfig.kt` + getter ใน `Version.kt` |
| แก้ JVM/MC argument | `game/launch/LaunchArgs.kt` |
| แก้ลำดับติดตั้ง/ตรวจไฟล์ | `game/launch/GameLaunchFlow.kt` |
| แก้ overlay ในเกม (ปุ่มลอย/mouse) | `game/launch/handler/GameHandler.kt` + `ui/control/` |
| แก้ layout ปุ่มเริ่มต้น | `assets/default_layout.json` + `game/control/ControlManager.kt` |
| เพิ่มปุ่ม/แก้ input mapping | `ui/control/event/`, `InputMap` module |
| แก้ path เก็บไฟล์ | `path/PathManager.kt` (และ `GamePathManager` สำหรับ game dir) |
| เพิ่มหน้าใหม่ | `ui/screens/...` + key ใหม่ใน `viewmodel/ScreenBackStackViewModel.kt` |
| แก้โลจิก SDL/text input | `game/sdl/SdlBridge.kt`, `org/libsdl/app/SdlImeController.java`, `CallbackBridge.notifyLauncher` |
| แก้ของ native | `src/main/jni/...` + `Android.mk` + ต้อง rebuild `.so` |
| แก้สี/ธีม | `ui/theme/` + `:ColorPicker` |
| แก้ข้อความ/แปลภาษา | `src/main/res/values*/strings.xml` |

---

## 17. ข้อควรระวัง (จากโค้ดจริง)

1. **GPLv3** — fork แล้วแจกต้องเปิดซอร์ส
2. `VMActivity` + `GameHandler` เป็น hotspot: พังแล้วเกมเปิดไม่ขึ้นเลย
   แก้แล้วต้องทดสอบเปิดเกมจริง ไม่ใช่แค่ compile
3. **หลายเส้นทาง fallback**: ค่าราย version → `AllSettings` → default
   ถ้าตั้งค่าไม่ติด ให้ไล่ตาม `Version.getX()` ก่อน
4. `assets/` ใหญ่ (158MB runtimes) — อย่า `git add -A` โดยไม่ตั้งใจ
5. **native ต้องมี NDK**; ถ้าไม่มี ให้จำกัดงานฝั่ง Kotlin/Compose เท่านั้น
6. `CallbackBridge` มี `@CriticalNative` — เปลี่ยนลายเซ็น native พังทั้ง input stack
7. ตัวเลข `launcher_version_code` ต้องมากกว่าเดิมเสมอถ้าจะอัปเดตทับ
8. upstream คือ `ZalithLauncher/ZalithLauncher2` — fork นี้ชื่อ `...-Remake-By-Serloss`
   เวลาเทียบ diff/merge ให้เทียบกับ upstream

---

## 18. สรุปสถาปัตยกรรมในหนึ่งย่อหน้า

Compose UI (navigation3 + back stack เอง) → เลือก version → `VMActivity` สร้าง surface →
`GameLaunchFlow` เตรียม manifest/libs/args → `Launcher`/`GameLauncher` ประกอบ JVM +
classpath (LWJGL ที่แพตช์แล้ว) + renderer (MobileGlues/Zink/VirGL/GL4ES...) →
เรียก native (`jni/ctxbridges`) ให้ JVM (JRE ที่แตกจาก asset) รัน Minecraft →
input จาก Compose overlay/SDL/gamepad ไหลผ่าน `CallbackBridge` → native → GLFW → เกม →
วาดกลับผ่าน EGL bridge ลง surface; overlay ปุ่มลอย/floating control มาจาก
`:LayerController` + JSON layout ที่ `ControlManager` โหลด
