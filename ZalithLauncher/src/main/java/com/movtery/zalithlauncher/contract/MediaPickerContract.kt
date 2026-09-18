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

package com.movtery.zalithlauncher.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

private const val DEFAULT_ALL_MIME_TYPES = "*/*"

private fun buildMimeTypeArray(
    allowImages: Boolean,
    allowVideos: Boolean
): Array<String> {
    val mimeTypes = mutableListOf<String>()

    if (allowImages) {
        mimeTypes.add("image/*")
    }
    if (allowVideos) {
        mimeTypes.add("video/*")
    }
    if (mimeTypes.isEmpty()) {
        mimeTypes.add(DEFAULT_ALL_MIME_TYPES)
    }

    return mimeTypes.toTypedArray()
}

/**
 * @param allowTypes 允许的 MimeType
 * @param allowMultiple 是否允许多选
 */
class MediaPickerContract(
    private val allowTypes: Array<String>,
    private val allowMultiple: Boolean = false
) : ActivityResultContract<Unit, List<Uri>?>() {
    /**
     * @param allowImages 是否允许选择图片
     * @param allowVideos 是否允许选择视频
     */
    constructor(
        allowImages: Boolean = true,
        allowVideos: Boolean = true,
        allowMultiple: Boolean = false
    ): this(
        allowTypes = buildMimeTypeArray(
            allowImages = allowImages,
            allowVideos = allowVideos
        ),
        allowMultiple = allowMultiple
    )

    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = determinePrimaryType()
            
            if (allowTypes.isNotEmpty()) {
                putExtra(Intent.EXTRA_MIME_TYPES, allowTypes)
            }
            
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri>? {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return null
        }
        return parseSelectedUris(intent)
    }

    private fun determinePrimaryType(): String {
        return when {
            allowTypes.isEmpty() || hasBothImageAndVideo() -> DEFAULT_ALL_MIME_TYPES
            allowTypes.size == 1 -> allowTypes[0]
            else -> {
                val mainType = allowTypes[0].substringBefore("/")
                //检查所有类型是否都属于同一大类
                if (allowTypes.all { it.startsWith("$mainType/") }) {
                    "$mainType/*"
                } else {
                    DEFAULT_ALL_MIME_TYPES
                }
            }
        }
    }

    private fun hasBothImageAndVideo(): Boolean {
        val hasImage = allowTypes.any { it.startsWith("image/") }
        val hasVideo = allowTypes.any { it.startsWith("video/") }
        return hasImage && hasVideo
    }

    private fun parseSelectedUris(intent: Intent): List<Uri> {
        val uris = mutableListOf<Uri>()

        intent.clipData?.let { clipData ->
            val maxItems = if (allowMultiple) clipData.itemCount else 1
            for (i in 0 until minOf(maxItems, clipData.itemCount)) {
                clipData.getItemAt(i).uri?.let { uri ->
                    uris.add(uri)
                }
            }
        } ?: run {
            intent.data?.let { uri ->
                uris.add(uri)
            }
        }

        return uris
    }
}