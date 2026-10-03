from pathlib import Path

buttons = Path("ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/components/Buttons.kt")
b = buttons.read_text()
old = """fun ScalingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,"""
new = """fun ScalingActionButton(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,"""
if old not in b:
    raise SystemExit("button signature missing")
b = b.replace(old, new, 1)
old_button = """    Button(
        onClick = onClick,"""
new_button = """    var longHandled by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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
        },"""
if old_button not in b:
    raise SystemExit("button body missing")
buttons.write_text(b.replace(old_button, new_button, 1))

launch = Path("ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/LauncherScreen.kt")
l = launch.read_text()
l = l.replace("version?.versionName", "version?.getVersionName()", 1)
launch.write_text(l)
print("fixed")
