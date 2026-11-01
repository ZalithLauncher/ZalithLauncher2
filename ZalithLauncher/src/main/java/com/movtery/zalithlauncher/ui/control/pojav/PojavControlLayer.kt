package com.movtery.zalithlauncher.ui.control.pojav

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

/**
 * Layer kontrol kustom bergaya PojavLauncher untuk layar game.
 * Dipasang di atas layer mouse; sentuhan yang tidak mengenai kontrol diteruskan ke layer mouse.
 */
@Composable
fun PojavControlLayer(
    layoutFile: File,
    reloadKey: Int,
    visible: Boolean,
    opacity: Float,
    isGrabbing: Boolean,
    editMode: Boolean,
    backSignal: Int,
    cameraSensitivity: Float,
    onSpecial: (Int) -> Unit,
    onPointerOccupied: (Int) -> Unit,
    onPointerReleased: (Int) -> Unit,
    onExitEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    key(layoutFile.absolutePath, reloadKey, editMode) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PojavControlLayout(ctx).apply {
                    this.editMode = editMode
                    this.lastBackSignal = backSignal
                    load(layoutFile)
                }
            },
            update = { v ->
                v.onSpecial = onSpecial
                v.onPointerOccupied = onPointerOccupied
                v.onPointerReleased = onPointerReleased
                v.onExitEditor = onExitEditor
                v.cameraSensitivity = cameraSensitivity
                v.visibility = if (visible || editMode) View.VISIBLE else View.GONE
                v.alpha = if (editMode) 1f else opacity
                v.setGrabbing(isGrabbing)
                if (editMode && v.lastBackSignal != backSignal) {
                    v.lastBackSignal = backSignal
                    v.requestExit()
                }
            }
        )
    }
}
