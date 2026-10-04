package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.JoystickStylePreview
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.resolveThemeConfig
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank

/**
 * 为摇杆控件选择外观
 */
@Composable
fun EditJoystickStyle(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    data: ObservableJoystickData,
    joystickStyles: List<ObservableJoystickStyle>,
    openJoystickStyleList: () -> Unit,
) {
    BaseScreen(
        screenKey = screenKey,
        currentKey = currentKey
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (joystickStyles.isNotEmpty()) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 120.dp)
                ) {
                    items(joystickStyles) { style ->
                        ChoseStyleItem(
                            modifier = Modifier.padding(all = 8.dp),
                            style = style,
                            selected = data.joystickStyleId == style.uuid,
                            onSelectedChange = { selected ->
                                data.joystickStyleId = if (selected) style.uuid else null
                            }
                        )
                    }
                }
            } else {
                InfoLayoutTextItem(
                    modifier = Modifier.padding(all = 24.dp),
                    title = stringResource(R.string.control_editor_edit_joystick_style_list_empty),
                    onClick = openJoystickStyleList
                )
            }
        }
    }
}

@Composable
private fun ChoseStyleItem(
    style: ObservableJoystickStyle,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isLauncherInDarkTheme()
    val config = resolveThemeConfig(style, isDark)

    InfoLayoutItem(
        modifier = modifier,
        onClick = {
            onSelectedChange(!selected)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JoystickStylePreview(
                modifier = Modifier.size(50.dp),
                config = config
            )

            Spacer(modifier = Modifier.height(4.dp))

            MarqueeText(
                modifier = Modifier.fillMaxWidth(),
                text = style.name.takeIf { it.isNotEmptyOrBlank() } ?: stringResource(R.string.generic_unspecified),
                textAlign = TextAlign.Center
            )

            RadioButton(
                selected = selected,
                onClick = {
                    onSelectedChange(!selected)
                }
            )
        }
    }
}
