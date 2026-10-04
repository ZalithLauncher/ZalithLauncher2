from pathlib import Path

settings = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/setting/AllSettings.kt")
text = settings.read_text()
needle = '    val searchShadersPlatform = enumSetting("searchShadersPlatform", Platform.CURSEFORGE)\n}'
insert = needle[:-1] + '    val miraiQuietMode = boolSetting("miraiQuietMode", false)\n    val miraiVulkanFailCount = intSetting("miraiVulkanFailCount", 0)\n}'
if "miraiQuietMode" not in text:
    if needle not in text:
        raise SystemExit("settings anchor missing")
    settings.write_text(text.replace(needle, insert, 1))

buttons = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/components/Buttons.kt")
b = buttons.read_text()
if "onLongClick:" not in b:
    b = b.replace(
        "fun ScalingActionButton(\n    onClick: () -> Unit,",
        "fun ScalingActionButton(\n    onClick: () -> Unit,\n    onLongClick: (() -> Unit)? = null,",
        1,
    )
    b = b.replace(
        "    Button(\n        onClick = onClick,",
        """    var longHandled by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(isPressed, onLongClick) {
        if (isPressed && onLongClick != null) {
            kotlinx.coroutines.delay(480)
            if (isPressed) {
                longHandled = true
                onLongClick()
            }
        }
    }
    Button(
        onClick = {
            if (longHandled) longHandled = false else onClick()
        },""",
        1,
    )
    buttons.write_text(b)

rail = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MiraiNavigationRail.kt")
r = rail.read_text()
if "miraiQuietMode" not in r:
    r = r.replace("import com.movtery.zalithlauncher.R\n", "import com.movtery.zalithlauncher.R\nimport com.movtery.zalithlauncher.setting.AllSettings\n", 1)
    r = r.replace("import androidx.compose.foundation.clickable\n", "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.combinedClickable\n", 1)
    r = r.replace(
        "    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()\n",
        "    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()\n    val quiet = AllSettings.miraiQuietMode.state\n",
        1,
    )
    r = r.replace(
        """            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),""",
        """            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { AllSettings.miraiQuietMode.save(!quiet) }
                    ),""",
        1,
    )
    for icon in ("ic_search", "ic_dashboard_filled", "ic_group_filled", "ic_settings_filled"):
        r = r.replace(
            f"            LauncherSectionItem(\n                icon = R.drawable.{icon},",
            f"            if (!quiet) LauncherSectionItem(\n                icon = R.drawable.{icon},",
            1,
        )
    r = r.replace(
        "            AccountShortcut(account = account, onClick = onAccountClick)\n",
        "            if (!quiet) AccountShortcut(account = account, onClick = onAccountClick)\n",
        1,
    )
    rail.write_text(r)

screen = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MainScreen.kt")
m = screen.read_text()
if "LocalTime.now" not in m:
    m = m.replace(
        """    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor,
        contentColor = onBackgroundColor()
    ) {
        Column(""",
        """    val night = java.time.LocalTime.now().hour.let { it >= 19 || it < 6 }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor,
        contentColor = onBackgroundColor()
    ) {
        Box(Modifier.fillMaxSize()) {
        Column(""",
        1,
    )
    old = """            }
        }
    }
}"""
    new = """            }
        }
        if (night) {
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.34f)))
        }
        }
    }
}"""
    if old not in m:
        raise SystemExit("main screen close not found")
    screen.write_text(m.replace(old, new, 1))

launch = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/LauncherScreen.kt")
l = launch.read_text()
if "onLongClick = { showPick = true }" not in l:
    if "import androidx.compose.material3.AlertDialog" not in l:
        l = l.replace("import androidx.compose.material3.TextButton\n", "import androidx.compose.material3.TextButton\nimport androidx.compose.material3.AlertDialog\n", 1)
    l = l.replace(
        "    var showList by remember { mutableStateOf(false) }\n",
        "    var showList by remember { mutableStateOf(false) }\n    var showPick by remember { mutableStateOf(false) }\n",
        1,
    )
    l = l.replace(
        """            onClick = {
                onLaunchGame(null)
            },""",
        """            onClick = {
                onLaunchGame(null)
            },
            onLongClick = { showPick = true },""",
        1,
    )
    dialog = """
        if (showPick) {
            AlertDialog(
                onDismissRequest = { showPick = false },
                title = { Text(\"Choose before launch\") },
                text = {
                    Column {
                        Text(\"Version: ${version?.versionName ?: \"None selected\"}\")
                        TextButton(onClick = {
                            AllSettings.graphicsApi.save(com.movtery.zalithlauncher.game.version.installed.GraphicsApi.DEFAULT_OPENGL)
                            AllSettings.miraiVulkanFailCount.save(0)
                            showPick = false
                        }) { Text(\"Use OpenGL\") }
                        TextButton(onClick = {
                            val vulkan = com.movtery.zalithlauncher.game.version.installed.GraphicsApi.entries.firstOrNull { it.name.contains(\"VULKAN\") }
                            if (vulkan != null) AllSettings.graphicsApi.save(vulkan)
                            showPick = false
                        }) { Text(\"Use Vulkan\") }
                        TextButton(onClick = { toVersionManageScreen(); showPick = false }) { Text(\"Pick a version\") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPick = false; onLaunchGame(null) }) { Text(\"Launch\") }
                }
            )
        }
        if (AllSettings.miraiVulkanFailCount.state >= 2 && AllSettings.graphicsApi.state.name.contains(\"VULKAN\")) {
            AlertDialog(
                onDismissRequest = { AllSettings.miraiVulkanFailCount.save(0) },
                title = { Text(\"Vulkan crashed twice\") },
                text = { Text(\"Switch the next launch to OpenGL?\") },
                confirmButton = {
                    TextButton(onClick = {
                        AllSettings.graphicsApi.save(com.movtery.zalithlauncher.game.version.installed.GraphicsApi.DEFAULT_OPENGL)
                        AllSettings.miraiVulkanFailCount.save(0)
                    }) { Text(\"Use OpenGL\") }
                },
                dismissButton = {
                    TextButton(onClick = { AllSettings.miraiVulkanFailCount.save(0) }) { Text(\"Keep Vulkan\") }
                }
            )
        }
"""
    anchor = "        )\n    }\n}\n\n@Composable\nprivate fun ActionMenuCardContent("
    if anchor not in l:
        raise SystemExit("launch anchor missing")
    l = l.replace(anchor, "        )" + dialog + "    }\n}\n\n@Composable\nprivate fun ActionMenuCardContent(", 1)
    launch.write_text(l)
print("patched")
