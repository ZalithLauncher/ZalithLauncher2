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
// 参考 Fold Craft Launcher （https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCL/src/main/java/com/mio/util/LayoutConverter.kt）

package com.movtery.zalithlauncher.layoutconverter

import java.io.File

/**
 * FCL 控制布局与 ZalithLauncher2 (ZL2) 控制布局之间的纯 Kotlin 转换门面。
 *
 * 语义基准为 control-converter 项目的 cc.py（Python 参考实现），
 * 替代旧的 libcc.so (Go/cgo) JNI 方案：不再依赖特定 ABI，全部平台可用。
 *
 * 调用约定与旧 JNI 版一致：同步阻塞，成功返回 null，失败返回错误信息；调用方需在后台线程执行。
 */
object LayoutConverter {

    /**
     * 将 ZL2 控制布局 JSON 转换为 FCL 格式。
     *
     * @return 转换成功返回 null；失败返回错误信息
     */
    fun convertZl2ToFcl(input: File, output: File): String? = runCatching {
        val source = CcJson.loadJson(input.readText())
        val result = CcConverter.convertZlToFcl(source)
        output.parentFile?.mkdirs()
        output.writeText(CcJson.encodePretty(result))
        null
    }.getOrElse { t -> "${t.javaClass.simpleName}: ${t.message}" }

    /** 判断布局 JSON 文本是否为 ZL2 格式（导入时自动识别用）。 */
    @JvmStatic
    fun isZl2Layout(jsonText: String): Boolean = CcConverter.isZl2Layout(jsonText)
}
