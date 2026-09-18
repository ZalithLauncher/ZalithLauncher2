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

package com.movtery.zalithlauncher.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_THROWABLE_STACK
import com.movtery.zalithlauncher.ui.base.AbstractAppCompatActivity
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.getSerializableSafely
import dagger.hilt.android.AndroidEntryPoint

private const val BUNDLE_THROWABLE = "BUNDLE_THROWABLE"

/**
 * 用于显示致命崩溃信息的 Activity
 *
 * 此 Activity 会向用户展示一个 AlertDialog，详细说明崩溃情况
 *
 * 它被设计为与主启动器相互独立，以确保即使启动器本身出现严重问题，也能正常显示该界面
 */
@AndroidEntryPoint
class FatalErrorActivity : AbstractAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras ?: return run { finish() }
        val throwable = extras.getSerializableSafely(BUNDLE_THROWABLE, Throwable::class.java) ?: return run { finish() }

        val message = getString(R.string.crash_launch_crash_message)
        val throwableStack = Log.getStackTraceString(throwable)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.crash_launch_crash_title))
            .setMessage(message + "\n\n" + throwableStack)
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
            .setNeutralButton(android.R.string.copy) { _, _ ->
                copyText(COPY_LABEL_THROWABLE_STACK, throwableStack, this)
                finish()
            }
            .setCancelable(false)
            .showThemed()
    }
}

/**
 * 使用指定的 throwable 显示致命错误界面
 */
fun showFatalError(context: Context, throwable: Throwable) {
    val intent = Intent(context, FatalErrorActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(BUNDLE_THROWABLE, throwable)
    }
    context.startActivity(intent)
}