# Renderer validation

This document is the on-device procedure for validating the LTW renderers, and the template the
results get recorded in.

**No performance numbers or compatibility results are published for LTW or LTW Legacy yet.**
Everything below is a procedure, not a result. Anything measurable requires a physical Android
device running the game; it cannot be produced from a build machine, and Aerix does not publish
numbers it has not measured.

Fill in the tables on the devices you have, and the results can be written up from them.

## What can and cannot be established without a device

| Claim | Establishable without a device? | How it is checked |
| --- | --- | --- |
| The wrapper compiles and links for every ABI | Yes | CI build + `verify_ltw_apk.py` |
| The library is packaged in the APK as a valid ELF for each ABI | Yes | `verify_ltw_apk.py` |
| Selection picks the right wrapper per version | Yes | `RendererPickerTest` (JVM) |
| Environment variables and library names are correct | Yes | `LTWRendererTest`, `LTWLegacyRendererTest` |
| The game actually launches and renders | **No** | On-device |
| FPS, 1% lows, memory, startup time, chunk load time | **No** | On-device |
| Mod / resource pack / shader pack compatibility | **No** | On-device |

## Procedure

### 1. Prepare

- Use a **clean instance per test version**. Mixing mod sets across rows makes results
  unattributable.
- Record the device, SoC, GPU driver string, Android version and RAM for every table.
- Get the GPU string from the launch log (`GLES version detected`, and in-game `F3`).
- Launch once to let the shader cache warm, then restart before measuring.

### 2. Record the selection

Every launch logs the wrapper and the reason it was chosen. Confirm it matches the expectation
before measuring anything:

```
Renderer: LTW Legacy
Renderer Summary: OpenGL 1.x/2.1 translation layer for Minecraft 1.8 - 1.16.5, built from source.
```

If the wrapper is not the expected one, record that instead of a frame rate — a wrong wrapper
makes every other number meaningless.

### 3. Measure

Repeat each measurement **three times** and record the median. Note the exact camera position
and world; a comparison is only valid at the same place with the same render distance.

- **Average FPS and 1% low** — in-game `F3`, or the device's own GPU profiler. Record render
  distance, simulation distance and whether the chunk was fully loaded.
- **Memory** — peak RSS of the game process after 10 minutes of play.
- **Startup time** — from tapping Play to the main menu.
- **Chunk load time** — time to render distance at a fixed position after a fresh teleport.

### 4. Compare against the baseline

For each row, run the same test twice: once with the wrapper under test, once with the wrapper
it is being compared to (for legacy versions, GL4ES is the natural baseline). Same device, same
world, same settings.

## Results template

### Device

| Field | Value |
| --- | --- |
| Device / SoC | |
| GPU and driver string | |
| Android version | |
| RAM | |
| Aerix build (version code) | |

### Selection

| Minecraft version | Expected wrapper | Actual wrapper | Reason from log |
| --- | --- | --- | --- |
| 1.8.9 | LTW Legacy | | |
| 1.12.2 | LTW Legacy | | |
| 1.16.5 | LTW Legacy | | |
| 1.17.1 | LTW | | |
| 1.20.1 | LTW | | |
| 1.21.x | LTW | | |

### Performance

| Version | Renderer | Avg FPS | 1% low | Peak RSS | Startup | Chunk load |
| --- | --- | --- | --- | --- | --- | --- |
| 1.12.2 | LTW Legacy | | | | | |
| 1.12.2 | GL4ES | | | | | |
| 1.16.5 | LTW Legacy | | | | | |
| 1.16.5 | GL4ES | | | | | |
| 1.20.1 | LTW | | | | | |

### Mods

| Version | Mod | Loader | Result | Notes |
| --- | --- | --- | --- | --- |
| | | | | |

### Resource packs

| Version | Pack | Result | Notes |
| --- | --- | --- | --- |
| | | | |

### Shader packs

| Version | Pack | Loader (Iris/OptiFine/Canvas) | Result | Notes |
| --- | --- | --- | --- | --- |
| | | | | |

## Reporting a failure

A useful report contains:

1. Device and GPU driver string.
2. Minecraft version, loader and loader version.
3. The selected wrapper and the reason line from the launch log.
4. `glGetString(GL_RENDERER)` and `GL_VERSION` if the game or a mod can print them.
5. The full launcher log, and `latest.log` from the instance.

The launch log is reachable from Aerix's built-in log viewer.
