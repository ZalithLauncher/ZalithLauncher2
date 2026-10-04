from pathlib import Path

replacements = {
    "MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer/renderers/LTWRenderer.kt": [
        ('override fun getRendererName(): String = "LTW (Large Thin Wrapper)"', 'override fun getRendererName(): String = "LTW (OpenGL wrapper)"'),
        ('"Incomplete OpenGL 3.2 core wrapper on OpenGL ES; game, mod, and device compatibility varies."', '"OpenGL wrapper on OpenGL ES. Not a Minecraft renderer. Shader and mod support depends on the game and device."'),
        ('override fun getMinMCVersion(): String = "1.18"', 'override fun getMinMCVersion(): String = "1.17"'),
    ],
    "MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer/renderers/GL4ESRenderer.kt": [
        ('override fun getRendererName(): String = "GL4ES"', 'override fun getRendererName(): String = "GL4ES"'),
    ],
    "MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer/renderers/VirGLRenderer.kt": [
        ('override fun getRendererName(): String = "VirGLRenderer"', 'override fun getRendererName(): String = "VirGL"'),
    ],
    "MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer/renderers/KopperZinkRenderer.kt": [
        ('override fun getRendererName(): String = "Kopper Zink"', 'override fun getRendererName(): String = "Zink"'),
    ],
    "MiraiLauncher/src/test/java/com/movtery/zalithlauncher/game/renderer/LTWRendererTest.kt": [
        ('assertEquals("1.18", LTWRenderer.getMinMCVersion())', 'assertEquals("1.17", LTWRenderer.getMinMCVersion())'),
    ],
}
for path, pairs in replacements.items():
    file = Path(path)
    text = file.read_text()
    for old, new in pairs:
        if old not in text:
            raise SystemExit(f"missing {old} in {path}")
        text = text.replace(old, new, 1)
    file.write_text(text)

launcher = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/launch/GameLauncher.kt")
text = launcher.read_text()
old = """        if (!Renderers.isCurrentRendererValid()) {
            Renderers.setCurrentRenderer(version.getRenderer())
        }"""
new = """        if (!Renderers.isCurrentRendererValid()) {
            val available = Renderers.getRenderers().map { it.getUniqueIdentifier() }.toSet()
            val manual = version.getVersionConfig().renderer.getValue()
            val choice = RendererPicker.pick(version.getVersionName(), manual, available)
            Renderers.lastPickReason = choice.reason
            Renderers.setCurrentRenderer(choice.identifier.ifEmpty { version.getRenderer() })
        }"""
if old not in text:
    raise SystemExit("launch hook missing")
text = text.replace(old, new, 1)
if "import com.movtery.zalithlauncher.game.renderer.RendererPicker" not in text:
    text = text.replace(
        "import com.movtery.zalithlauncher.game.renderer.Renderers\n",
        "import com.movtery.zalithlauncher.game.renderer.RendererPicker\nimport com.movtery.zalithlauncher.game.renderer.Renderers\n",
        1,
    )
old_info = 'appendInfo("Renderer: ${renderer.getRendererName()}")'
new_info = """appendInfo("Renderer: ${renderer.getRendererName()}")
        appendInfo("Renderer pick: ${Renderers.lastPickReason}")
        appendInfo("Renderer library: ${renderer.getRendererLibrary()}")
        appendInfo("Renderer env: ${renderer.getRendererEnv().value}")"""
if old_info not in text:
    raise SystemExit("renderer log missing")
launcher.write_text(text.replace(old_info, new_info, 1))

renderers = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer/Renderers.kt")
r = renderers.read_text()
if "lastPickReason" not in r:
    r = r.replace(
        "object Renderers {",
        "object Renderers {\n    var lastPickReason: String = \"not selected\"",
        1,
    )
    renderers.write_text(r)
print("wired")
