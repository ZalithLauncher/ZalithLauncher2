# Automatic renderer selection

This is not a new renderer. Mirai still loads the existing wrappers.

- Minecraft 1.8 through 1.16.4 prefers GL4ES, then VirGL.
- Minecraft 1.17 and newer prefers LTW, then Zink.
- An instance renderer override is kept when that wrapper is available.
- If the override or preferred library is missing, the next wrapper in that pair is used.

Names in the UI mean the upstream libraries:

- LTW (OpenGL wrapper): `libltw.so`, an incomplete OpenGL 3.2 core wrapper on OpenGL ES.
- Zink: Mesa Zink through the existing Kopper path.
- GL4ES: `libgl4es_114.so`.
- VirGL: the existing VirGL renderer.

LTW does not provide Sodium, Iris, or OptiFine parity. Shader packs, mods, and resource packs still depend on the game version, the loader, and the device. There is no LTW Legacy renderer.

The launch log records the Minecraft version, the selected wrapper, the fallback reason, the loaded library, and the wrapper environment variables.
