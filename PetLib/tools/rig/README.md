# PetLib 动画骨架工具

在 Blender 5.x 里为 PetLib 制作人物模型剪辑的骨架工程。**rig.blend 是
纯骨架工作区**（骨架 + 网格 + 60fps，零剪辑零动作）：做一份动画、导出
一份动画；要接入 PetLib 预设时由集成者配置参数（见"项目集成"）。
运行时契约全文见 `../MODEL_CONTRACT.md`。

## 骨架结构

```
blockbench_export（Armature 对象，180°Y 根旋转，结构性保留）
└── Orbit
    ├── Body ──┬── Head ──────── (Head_2, Hat_Layer)
    │          ├── Left_Arm ──── (Left_Arm_2, Left_Arm_Layer)
    │          ├── Right_Arm ─── (Right_Arm_2, Right_Arm_Layer)
    │          ├── Cape ──────── (Cape_2)
    │          └── (Body_2, Body_Layer)
    ├── Left_Leg ── (Left_Leg_2, Left_Leg_Layer)
    └── Right_Leg ─ (Right_Leg_2, Right_Leg_Layer)
```

- **场景 60fps**、帧范围 1-250；时间轴单位即导出单位（帧 f → t=(f−1)/60 秒）。
- **骨骼即关节**：选中一根骨即选中该部件（双层网格骨父级到关节骨），
  骨的显示名 = 关节名，按部件配色。
- **网格原点 = 关节枢轴**：旋转骨/网格都绕枢轴转。

## 命令行环境（Windows cmd）

下文命令均使用 **Windows `cmd`** 语法。先打开 `cmd`，进入本目录并设置
仓库根目录与 Blender 命令（`set` 只在当前窗口有效）：

```bat
cd /d "C:\path\to\ZalithLauncher2\PetLib\tools\rig"
set "REPO_ROOT=..\..\.."
set "BLENDER=blender"
```

把第一行路径换成本机仓库路径。`REPO_ROOT=..\..\..` 是从
`PetLib/tools/rig` 指向仓库根目录，脚本会自行转成绝对路径。默认要求
`blender.exe` 与 `python` 已加入 `PATH`；Blender 未加入时，把第三行改为
实际程序路径，例如：

```bat
set "BLENDER=C:\Program Files\Blender Foundation\Blender 5.0\blender.exe"
```

## 制作剪辑

打开 `rig.blend`，进入 Animation 工作区，自动打键已开。骨架工程不带任何剪辑，所有轨道都是你自己的。

### 一个剪辑的完整流程

1. **摆姿势**：视口选中骨架 `blockbench_export`，`Ctrl+Tab` 进 Pose Mode，开始做动画。
2. **命名 action**：Dope Sheet → Action Editor 左侧，把 action 改成想要的名字，这就是导出 GLB 里的动画名，自由命名。
3. **推入 NLA**：Action Editor 右上角菜单 `Action → Push Down`（或在 NLA 编辑器对该 action 使用 Push Down），生成一条与 action 同名的轨道。轨道是导出的采样单位。
4. **回放检查**：只取消目标轨道的静音按空格播放；看完重新静音。做完一个就推入一条，工程里可同时存多条。
5. **导出**：见下节——按名采样，一条命令出一份单动画 GLB。

### 要点与建议

- **覆盖全部 8 根关节骨**：剪辑键是绝对值，未打键的骨在播放时回落到立正 rest 只想要某个部件动，其余骨也要在首帧补一个静止键。
- **逐帧打键非必需**：导出按每帧采样，曲线插值类型（BEZIER/LINEAR）不影响产物形状；稀疏打键 + 默认插值即可，所见即所得。
- **想基于内置动画魔改**：`script/rebuild_actions.py` 把 classic GLB 的参考剪辑逐帧重建进 rig.blend（一条 NLA 轨道一个），在其上改了再导出即可；用完删掉对应轨道，工程恢复纯净。

  ```bat
  rem 先导出实际 Rig 的静止基准，供重建与采样共用关节帧校正
  "%BLENDER%" -b --factory-startup rig.blend -P script/export_base.py -- "%REPO_ROOT%"
  "%BLENDER%" -b --factory-startup rig.blend -P script/rebuild_actions.py -- "%REPO_ROOT%"
  ```

## 导出与校验

两条路径共用骨架版基准 GLB；`out/` 整个目录被 git 忽略，`export_base.py` / `sample_clips.py` / `reference_clips.py` 会自建该目录。

### 日常：导出一份动画

```bat
rem 1. 基准 GLB：节点树 / 网格 / 静止 TRS（无动画）
"%BLENDER%" -b --factory-startup rig.blend -P script/export_base.py -- "%REPO_ROOT%"

rem 2. 按名采样：只采指定的 NLA 轨道（通常一条）→ JSON
rem 把 idle 换成实际的 NLA 轨道名；多个轨道名可依次列在命令末尾
set "CLIP_NAME=idle"
"%BLENDER%" -b --factory-startup rig.blend -P script/sample_clips.py -- "%REPO_ROOT%" "%CLIP_NAME%"

rem 3. 合并 → out/rig-export.glb（恰好一份动画）
python script/merge_channels.py "%REPO_ROOT%"

rem 4. 契约校验
python script/validate_glb.py out\rig-export.glb
```

`out/` 产物会被下一次导出覆盖；要同时留多份动画请自行改名拷贝。
第 2 步可同时给多个剪辑名，产物就是多动画 GLB——但按本工具的定位，
一份导出 = 一份动画，多动画请在项目集成侧组合。第 2 步依赖第 1 步的
`out/rig-base.glb`（关节帧校正自检）；采样前骨架姿态会被归零（未打键
的骨按契约回落立正 rest），其余轨道采样期间自动静音。

### 守门样例：五份参考剪辑

Rust 门禁（下方 `cargo test` 命令）需要含 idle + interact 的守门样例。
参考剪辑不住在 blend 里，直接从 classic GLB 纯 Python 采样（与内置模型渲染逐位一致）：

```bat
"%BLENDER%" -b --factory-startup rig.blend -P script/export_base.py -- "%REPO_ROOT%"
rem classic GLB → JSON（5 条）
python script/reference_clips.py "%REPO_ROOT%"
rem 合并为 out/rig-export.glb
python script/merge_channels.py "%REPO_ROOT%"
cargo test --manifest-path "%REPO_ROOT%\PetLib\rust\Cargo.toml" --lib rig_tool
```

### 皮肤纹理预览

材质已固定为最近邻采样、背面剔除和 `DITHERED` 透明，并关闭透明背面。
动画师只需替换图像纹理节点的图片，不要把材质改为 `BLENDED` 或重新开启
`Show Transparent Back`，否则皮肤外层的透明区域可能露出另一侧表面。

修改材质或重建工作区后可单独校验：

```bat
"%BLENDER%" -b rig.blend --python-exit-code 1 -P script/validate_rig.py
```

## 管线为什么长这样

Blender 的 glTF 导出器对"骨父级网格 + NLA 动画"的序列化与 PetLib 运行时的合成约定不自洽，直接导出会引入系统性偏移（历史上造成过 4cm 网格漂移与 2 倍速动画）。
所以管线绕开导出器的动画/网格路径：

- **动画**：`sample_clips.py`（或 `reference_clips.py`）给出每帧关节
  世界矩阵（纯 glTF 空间）；`merge_channels.py` 用纯数学
  `local = world(parent)⁻¹ @ world(joint)` 重解每关节本地通道写进
  基准 GLB——数值上与参考实现逐位一致。
- **网格**：blend 内网格经世界包围盒自检与参考模型是同一几何，导出时
  按名称换回参考模型的顶点序列化。

### Blender 5.0 后台模式怪癖（脚本已规避，改写脚本时注意）

- 仅 `frame_set` 不会重估骨架姿态：采样每帧对轨道做 mute 抖动
  （mute→update→unmute→update）强制求值。全部轨道静音时同样不重估
  ——静止求值也要抖动（以静音态收尾）。
- 残留的活动动作（`animation_data.action`）会把逐轨求值粘成首采样轨
  的数据：采样/保存前必须置 `None`。
- 求值预热（unmute→求值→remute）会永久毒化姿态，不要用。
- 骨骼坐标系的 Y 永远沿"头→尾"：`pose_bones[].matrix` 读到的是骨骼
  帧而非关节枢轴帧（骨尾指向骨架 +Z，骨骼帧恒偏 90°——这是导出器
  骨→节点换算所需的约定，内置模型关节节点即按此作者化，勿改骨尾
  取向）。采样器按基准 GLB 的关节节点自动校正（骨骼帧→关节帧）并
  做静止自检，因此第 2 步依赖第 1 步产物。

## 项目集成

导出动画名是自由的；接入 PetLib 运行时（待机轮换、点击反应等自动
调度）时按下表命名/配置，运行时按名字识别：

| 动画名 | 用途 | 参考时长 |
|--------|------|----------|
| `idle` | 基础待机循环 | 3.0s（181 帧） |
| `idle_sub_*` | 随机待机子动画（每 8s 随机抽一个，不连续重复） | 参考剪辑 3.0s |
| `interact` | 点击反应（运行时截取前 0.5s） | 0.85s（51 帧） |

参考剪辑的时长/节奏可直接取自 `script/reference_clips.py` 的产物。

## 文件

| 文件 | 作用 |
|------|------|
| `rig.blend` | 纯骨架工作区（生成产物，入 git）：骨架 + 参考网格，零剪辑、60fps |
| `script/build_rig.py` | 从 `../../rust/src/assets/classic-player.glb` 重建 rig.blend（自检：世界矩阵不变、顶点世界不变、立正对称、方向） |
| `script/rebuild_actions.py` | 把 classic 参考剪辑逐帧重建进 rig.blend（魔改起点；用完删轨道） |
| `script/export_base.py` | 基准 GLB：节点树/网格/静止 TRS |
| `script/sample_clips.py` | 按名采样 rig.blend 的 NLA 轨道 → JSON（单动画出口） |
| `script/reference_clips.py` | 从 classic GLB 纯 Python 采样参考剪辑 → JSON（守门样例/取参考） |
| `script/merge_channels.py` | 基准 GLB + JSON → 单动画/参考 GLB |
| `script/validate_glb.py` | 契约校验（纯 Python，无需 Blender） |
| `script/validate_rig.py` | 骨架结构与皮肤预览材质校验（Blender 后台运行） |
| `out/` | 导出产物目录（git 忽略）：rig-export.glb / rig-base.glb / clip-samples.json |

`out/rig-export.glb` 是 Rust 测试守门的样例文件（加载 + 动画驱动 +
与内置模型渲染对比），由守门样例路径生成。

## 重建骨架

```bat
"%BLENDER%" -b --factory-startup -P script/build_rig.py -- "%REPO_ROOT%"
```
