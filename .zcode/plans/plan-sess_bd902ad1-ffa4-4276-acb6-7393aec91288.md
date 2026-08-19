## 同步目标

把 ZL2 的 LWJGL 链路完整对齐 FCL 当前 HEAD（`70f256b4` 之后的 3.3.3 + 3.4.1 混合架构），包括：

- LWJGL 模块 3.3.3/3.4.1 源码、libs jar、构建脚本、合并产物，原样同步
- 两个 LWJGL natives AAR（仅这两个；NG-GL4ES/kopper-zink/openal/spirv 不动）
- 启动器侧最终解压效果：每个版本目录内同时包含 jars + `natives/<abi>` + `version`
- 启动时版本选择、类路径、`LD_LIBRARY_PATH`/`org.lwjgl.librarypath`、freetype、JNI 行为对齐 FCL
- 保留 ZL2 自己的套壳：`components` 组件解压、`LaunchArgs`/`Launcher`/`GameLauncher`、`ZLBridge`/`CallbackBridge` 命名与 JNI 架构

## 1. LWJGL 模块与构建脚本

- 删除 `LWJGL/lwjgl-3.3.6`、`LWJGL/lwjgl-3.4.1`（旧目录）及旧 per-version `compileOnly`
- 从 FCL 原样拷贝 `LWJGL/3.3.3`、`LWJGL/3.4.1`、`LWJGL/compileOnly`，包含 `src/main/java`、`libs/<ver>/*.jar`、`build.gradle.kts`
- `LWJGL/build.gradle.kts` 换成 FCL 的 `buildLwjgl` 聚合任务（依赖两个子模块 `jar`）
- 两个子模块 `build.gradle.kts` 仅改输出目录：`$rootDir/ZalithLauncher/src/main/assets/app_runtime/lwjgl/<ver>`（保持 FCL 的 jar 合并、excluded modules 拷贝、`version` 时间戳、可复现 jar、Java 8/17 toolchain 配置）
- `settings.gradle.kts`：注册 `:LWJGL:lwjgl-3.3.3`/`:LWJGL:lwjgl-3.4.1` 并 `projectDir` 重定向到 `LWJGL/3.3.3`/`LWJGL/3.4.1`（与 FCL 相同）
- `gradle/libs.versions.toml`：补 `jspecify 1.0.0`（3.4.1 构建引用）
- `ZalithLauncher/build.gradle.kts`：`merge*Assets` 的 `dependsOn` 改为两个新模块 jar；在现有 mergeAssets `doLast` 中按 `-Darch` 清理 `app_runtime/lwjgl/<ver>/natives` 下非目标 ABI（移植 FCL `d51e05c4` 逻辑，并声明 `lwjglArch` 输入避免切换架构不重跑）；更新注释

## 2. Natives AAR

- 删除 `ZalithLauncher/libs/lwjgl-3.3.6-natives-release.aar`
- 从 FCL 拷贝 `lwjgl-3.3.3-natives-release.aar`、`lwjgl-3.4.1-natives-release.aar`
- 两者资产路径均为 `assets/app_runtime/lwjgl/<ver>/natives/<abi>`，与 LWJGL 模块产物合并成同一棵 APK asset 树

## 3. 解压链路（最终解压效果对齐 FCL）

- `UnpackSingleTask`/`UnpackComponentsTask`：支持每个组件指定完整 asset 子路径（现有组件默认 `components/<component>` 不变）
- `Components.kt`：
  - `LWJGL333("lwjgl/3.3.3", assetsDir = "app_runtime/lwjgl/3.3.3")`
  - `LWJGL341("lwjgl/3.4.1", assetsDir = "app_runtime/lwjgl/3.4.1")`
  - 去掉旧 `lwjgl3/3.3.6`、`lwjgl-*-natives` companion
- 解压结果变为 `filesDir/components/lwjgl/<ver>/`，内含全部 jar、`natives/<abi>/`、`version`，即 FCL `runtime/lwjgl/<ver>` 的等价树（根目录名不同是 ZL2 自己的套壳）
- 删除旧 `ZalithLauncher/src/main/assets/components/lwjgl3/` 资产；运行 LWJGL jar 任务生成新 `assets/app_runtime/lwjgl/<ver>/` 产物并保留在仓库中

## 4. 启动链路

- `LaunchArgs`：
  - `lwjglVersionDir()` 改为 `>=341 -> "3.4.1"`，否则 `"3.3.3"`
  - classpath 目录改为 `PathManager.DIR_COMPONENTS/lwjgl/<ver>`
  - 顺序保持 `lwjgl.jar` -> `merged-modules` -> 其余模块 -> LWJGL2 时最后 `lwjgl-lwjglx.jar`，与 FCL `addLWJGLClassPath` 语义一致
- `Launcher`：
  - `lwjglNativesDir` 改为 `components/lwjgl/<ver>/natives/<abi>`
  - 删除 `getLwjglNativesDirName()`
  - `${natives_directory}`/`-Djava.library.path` 替换中包含 LWJGL natives 目录（对齐 FCL `${natives_directory}` = 完整 library path）
  - 保留 `org.lwjgl.librarypath`、LD_LIBRARY_PATH 优先、`org.lwjgl.openal/freetype/spvc` 参数（freetype 绝对路径与 FCL `d62a366e` 行为一致）
- 保留现有 `detectLwjglVersion` 与 `_LibraryReplacement` 过滤（与 FCL `LauncherHelper`/`LibFilter`/`GameRepository` 等价），只更新注释与阈值语义

## 5. JNI / 原生行为对齐（保留 ZL2 套壳）

- 生成符号清单：FCL 3.3.3/3.4.1 的 `GLFW`、`CallbackBridge`、`PojavRendererInit` native 声明 vs ZL2 `libpojavexec` 导出符号，缺什么补什么（现有 critical/non-critical 方法表已基本一致，预计主要是验证）
- 逐项移植 FCL 提交中与 LWJGL 相关的行为，不搬 FCL 的 `FCLBridge`/`FCLauncher`/`DefaultLauncher` 套壳：
  - `a11d82a3`、`44934bee`：`input_bridge_v3.c` 输入接口、事件队列、尺寸上报对齐，保留 ZL2 logger、光标形状、graphic output 钩子
  - `165a6899`、`70f256b4`：`gl_bridge.c` 上下文创建/GL 版本探测相关修复（Java 侧随模块原样已同步，C 侧按需移植），保留 ZL2 renderer_config/插件体系
  - `723db052`、`0f042040`：只移植对 LWJGL/GLFW 有影响的 hook/EGL 行为，不照搬 FCL 的 linkerhook 与驱动加载架构
  - 说明：FCL 的 GLFW 不再调用 `nativeSetCursorShape`，ZL2 游戏侧光标形状 hook 随模块原样同步会停用；启动器侧接口不受影响

## 6. 验证

- `./gradlew :LWJGL:buildLwjgl` 生成 3.3.3/3.4.1 合并资产
- `./gradlew :ZalithLauncher:assembleDebug`（含 NDK/JNI 编译）
- `./gradlew :ZalithLauncher:testDebugUnitTest`
- 解包 APK，确认 `assets/app_runtime/lwjgl/3.3.3`、`3.4.1` 包含 jar + `natives/<abi>` + `version`，且旧 `components/lwjgl3` 不再存在
- 对照 FCL 提交的模块产物检查合并 jar 内容/符号，确认同步无遗漏

## 风险与说明

- 3.4.1 模块要求 Java 17 toolchain，ZL2 已配置 foojay resolver
- 合并 jar 会在本地重新生成，`version` 时间戳与 FCL 必然不同，jar 二进制可能因编译环境有细微差异，但源码与产物结构原样同步
- 若编译或 JNI 符号检查发现 FCL 模块还依赖当前 ZL2 JNI 未提供的入口，会按“保留 ZL2 命名、补齐符号”的方式解决