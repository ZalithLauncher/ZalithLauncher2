from pathlib import Path

buttons = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/components/Buttons.kt")
b = buttons.read_text()
old = """    var longHandled by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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
new = """    val longHandled = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(isPressed, onLongClick) {
        if (isPressed && onLongClick != null) {
            kotlinx.coroutines.delay(480)
            if (isPressed) {
                longHandled.value = true
                onLongClick()
            }
        }
    }
    Button(
        onClick = {
            if (longHandled.value) longHandled.value = false else onClick()
        },"""
if old not in b:
    raise SystemExit("button block missing")
buttons.write_text(b.replace(old, new, 1))

screen = Path("MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MainScreen.kt")
m = screen.read_text()
if "import androidx.compose.foundation.background" not in m:
    m = m.replace(
        "import androidx.compose.foundation.clickable\n",
        "import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\n",
        1,
    )
    screen.write_text(m)
print("fixed")
