/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.control.gamepad

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R

enum class GamepadRemap(
    val code: Int,
    val isMotion: Boolean = false
) {
    ButtonA(KeyEvent.KEYCODE_BUTTON_A) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "A")
        override fun getIconRes(): Int = R.drawable.img_controller_button_a
    },
    ButtonB(KeyEvent.KEYCODE_BUTTON_B) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "B")
        override fun getIconRes(): Int = R.drawable.img_controller_button_b
    },
    ButtonX(KeyEvent.KEYCODE_BUTTON_X) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "X")
        override fun getIconRes(): Int = R.drawable.img_controller_button_x
    },
    ButtonY(KeyEvent.KEYCODE_BUTTON_Y) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "Y")
        override fun getIconRes(): Int = R.drawable.img_controller_button_y
    },
    ButtonStart(KeyEvent.KEYCODE_BUTTON_START) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "Start")
        override fun getIconRes(): Int = R.drawable.img_controller_menu
    },
    ButtonSelect(KeyEvent.KEYCODE_BUTTON_SELECT) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "Select")
        override fun getIconRes(): Int = R.drawable.img_controller_view
    },
    MotionX(MotionEvent.AXIS_X, true) {
        @Composable
        override fun getText() = stringResource(
            id = R.string.settings_gamepad_remapping_left_joystick,
            stringResource(R.string.settings_gamepad_remapping_joystick_right)
        )
        override fun getIconRes(): Int = R.drawable.img_controller_stick_left
    },
    MotionY(MotionEvent.AXIS_Y, true) {
        @Composable
        override fun getText() = stringResource(
            id = R.string.settings_gamepad_remapping_left_joystick,
            stringResource(R.string.settings_gamepad_remapping_joystick_bottom)
        )
        override fun getIconRes(): Int = R.drawable.img_controller_stick_left
    },
    ButtonLeftStick(KeyEvent.KEYCODE_BUTTON_THUMBL) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "ThumbL")
        override fun getIconRes(): Int = R.drawable.img_controller_stick_left_press
    },
    MotionZ(MotionEvent.AXIS_Z, true) {
        @Composable
        override fun getText() = stringResource(
            id = R.string.settings_gamepad_remapping_right_joystick,
            stringResource(R.string.settings_gamepad_remapping_joystick_right)
        )
        override fun getIconRes(): Int = R.drawable.img_controller_stick_right
    },
    MotionRZ(MotionEvent.AXIS_RZ, true) {
        @Composable
        override fun getText() = stringResource(
            id = R.string.settings_gamepad_remapping_right_joystick,
            stringResource(R.string.settings_gamepad_remapping_joystick_bottom)
        )
        override fun getIconRes(): Int = R.drawable.img_controller_stick_right
    },
    ButtonRightStick(KeyEvent.KEYCODE_BUTTON_THUMBR) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "ThumbR")
        override fun getIconRes(): Int = R.drawable.img_controller_stick_right_press
    },
    ButtonL1(KeyEvent.KEYCODE_BUTTON_L1) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "LB")
        override fun getIconRes(): Int = R.drawable.img_controller_lb
    },
    ButtonR1(KeyEvent.KEYCODE_BUTTON_R1) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "RB")
        override fun getIconRes(): Int = R.drawable.img_controller_rb
    },
    MotionLeftTrigger(MotionEvent.AXIS_LTRIGGER, true) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "LT")
        override fun getIconRes(): Int = R.drawable.img_controller_lt
    },
    MotionRightTrigger(MotionEvent.AXIS_RTRIGGER, true) {
        @Composable
        override fun getText() = stringResource(R.string.settings_gamepad_remapping_press, "RT")
        override fun getIconRes(): Int = R.drawable.img_controller_rt
    },
    MotionHatX(MotionEvent.AXIS_HAT_X, true) {
        @Composable
        override fun getText() = stringResource(
            id = R.string.settings_gamepad_remapping_dpad,
            stringResource(R.string.settings_gamepad_remapping_dpad_right)
        )
        override fun getIconRes(): Int = R.drawable.img_controller_digi_right
    },
    MotionHatY(MotionEvent.AXIS_HAT_Y, true) {
        @Composable
        override fun getText() = stringResource(
            id = R.string.settings_gamepad_remapping_dpad,
            stringResource(R.string.settings_gamepad_remapping_dpad_down)
        )
        override fun getIconRes(): Int = R.drawable.img_controller_digi_down
    };

    @Composable
    abstract fun getText(): String

    /**
     * https://opengameart.org/content/controller-input-icons
     * This project contains assets by ElDuderino, licensed under CC0 1.0 Universal.
     */
    abstract fun getIconRes(): Int
}