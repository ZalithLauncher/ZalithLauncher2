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

package com.movtery.zalithlauncher.ui.code_editor

import android.content.Context
import com.movtery.zalithlauncher.filemanager.os.FmLog
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.DefaultGrammarDefinition
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource
import java.nio.charset.StandardCharsets
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * TextMate 语法与主题注册表
 */
object TextMateRegistry {
    private const val GRAMMAR_DIR = "textmate/grammars"
    private const val THEME_DIR = "textmate/themes"

    private const val THEME_DARK = "dark_vs"
    private const val THEME_LIGHT = "light_vs"

    /**
     * 已打包的语法
     * 语法文件名与其对应的 TextMate scope
     */
    private val grammarScopes = listOf(
        "c.tmLanguage.json" to "source.c",
        "cpp.tmLanguage.json" to "source.cpp",
        "css.tmLanguage.json" to "source.css",
        "html.tmLanguage.json" to "text.html.basic",
        "java.tmLanguage.json" to "source.java",
        "javascript.tmLanguage.json" to "source.js",
        "json.tmLanguage.json" to "source.json",
        "kotlin.tmLanguage.json" to "source.kotlin",
        "log.tmLanguage.json" to "text.log",
        "markdown.tmLanguage.json" to "text.html.markdown",
        "python.tmLanguage.json" to "source.python",
        "shellscript.tmLanguage.json" to "source.shell",
        "sql.tmLanguage.json" to "source.sql",
        "typescript.tmLanguage.json" to "source.ts",
        "xml.tmLanguage.json" to "text.xml",
        "yaml.tmLanguage.json" to "source.yaml",
        "yaml-1.2.tmLanguage.json" to "source.yaml.1.2",
        "yaml-embedded.tmLanguage.json" to "source.yaml.embedded"
    )

    /**
     * 文件扩展名对应的 TextMate scope
     */
    private val textMateScopes = mapOf(
        "md" to "text.html.markdown",
        "markdown" to "text.html.markdown",
        "log" to "text.log",
        "json" to "source.json",
        "kt" to "source.kotlin",
        "kts" to "source.kotlin",
        "java" to "source.java",
        "py" to "source.python",
        "js" to "source.js",
        "mjs" to "source.js",
        "cjs" to "source.js",
        "ts" to "source.ts",
        "c" to "source.c",
        "h" to "source.c",
        "cpp" to "source.cpp",
        "hpp" to "source.cpp",
        "cc" to "source.cpp",
        "cxx" to "source.cpp",
        "css" to "source.css",
        "html" to "text.html.basic",
        "htm" to "text.html.basic",
        "xml" to "text.xml",
        "sh" to "source.shell",
        "bash" to "source.shell",
        "zsh" to "source.shell",
        "sql" to "source.sql",
        "yaml" to "source.yaml",
        "yml" to "source.yaml"
    )

    private const val TAG = "TextMateRegistry"

    private val grammarRegistry = GrammarRegistry.getInstance()
    private val themeRegistry = ThemeRegistry.getInstance()
    private val languageCache = mutableMapOf<String, TextMateLanguage>()
    private var loaded = false
    private var textMateEnabled = true
    private val lock = ReentrantLock()

    /**
     * jcodings 的 unicode 表格通过类加载器资源加载
     * 若 APK 中资源缺失，[org.jcodings.unicode.UnicodeEncoding.CaseFold] 等类的
     * 静态初始化会抛 [org.jcodings.exception.InternalException]
     * 高亮线程因此产生未捕获异常并杀掉整个进程
     *
     * 这里在加载语法前提前触发这些类的初始化，失败则禁用高亮
     */
    private fun jcodingsTablesAvailable(): Boolean {
        val tableClasses = listOf(
            "org.jcodings.unicode.UnicodeEncoding",
            "org.jcodings.unicode.UnicodeEncoding\$CaseFold",
            "org.jcodings.unicode.UnicodeEncoding\$CaseUnfold11",
            "org.jcodings.unicode.UnicodeEncoding\$CaseUnfold12",
            "org.jcodings.unicode.UnicodeEncoding\$CaseUnfold13",
            "org.jcodings.unicode.UnicodeEncoding\$CaseMappingSpecials"
        )
        val missing = tableClasses.filter { cls ->
            runCatching { Class.forName(cls) }.isFailure
        }
        return if (missing.isEmpty()) {
            true
        } else {
            FmLog.warn(TAG, "jcodings tables unavailable: $missing, TextMate highlighting disabled")
            false
        }
    }

    /**
     * 注册 assets 中的语法与主题
     */
    private suspend fun ensureLoaded(context: Context) = withContext(Dispatchers.IO) {
        lock.withLock {
            if (!textMateEnabled) return@withLock
            if (loaded) return@withLock
            if (!jcodingsTablesAvailable()) {
                textMateEnabled = false
                return@withLock
            }
            val assets = context.assets

            grammarScopes.forEach { (file, scopeName) ->
                runCatching {
                    val path = "$GRAMMAR_DIR/$file"
                    val source = IGrammarSource.fromInputStream(
                        assets.open(path), path, StandardCharsets.UTF_8
                    )
                    grammarRegistry.loadGrammar(
                        DefaultGrammarDefinition.withGrammarSource(source, scopeName, scopeName)
                    )
                }.onFailure {
                    FmLog.warn(TAG, "Load TextMate grammar failed: $file", it)
                }
            }

            listOf(THEME_DARK, THEME_LIGHT).forEachIndexed { index, name ->
                runCatching {
                    val path = "$THEME_DIR/$name.json"
                    themeRegistry.loadTheme(
                        IThemeSource.fromInputStream(assets.open(path), path, StandardCharsets.UTF_8),
                        index == 0
                    )
                }.onFailure {
                    FmLog.warn(TAG, "Load TextMate theme failed: $name", it)
                }
            }
            // 库内不会自动从主题 JSON 读取明暗属性，手动标记以让配色方案正确判定
            themeRegistry.findThemeByFileName(THEME_DARK)?.isDark = true
            themeRegistry.findThemeByFileName(THEME_LIGHT)?.isDark = false

            loaded = true
        }
    }

    suspend fun applyTheme(isDark: Boolean, context: Context) {
        ensureLoaded(context)
        themeRegistry.setTheme(if (isDark) THEME_DARK else THEME_LIGHT)
    }

    /**
     * 获取指定 scope 的 TextMate 语言实例
     * @return 失败返回 null 由调用方降级为纯文本
     */
    suspend fun languageFor(scopeName: String, context: Context): TextMateLanguage? {
        ensureLoaded(context)
        if (!textMateEnabled) return null
        return withContext(Dispatchers.IO) {
            lock.withLock {
                runCatching {
                    languageCache.getOrPut(scopeName) {
                        // collectIdentifiers = true：收集文档标识符，提供代码补全条目
                        TextMateLanguage.create(scopeName, grammarRegistry, themeRegistry, true)
                    }
                }.getOrElse { e ->
                    FmLog.warn(TAG, "Create TextMate language failed: $scopeName", e)
                    null
                }
            }
        }
    }

    /**
     * 获取与文件名对应的编辑器语言
     * TextMate 语言首次创建需解析语法，耗时较高，应在 IO 线程调用
     * @return 加载失败时返回 null
     */
    suspend fun editorLanguageFor(name: String, context: Context): Language? {
        val ext = name.substringAfterLast('.', "").lowercase()
        return textMateScopes[ext]?.let { scope ->
            languageFor(scope, context)
        }
    }

    /**
     * 获取与当前主题一致的 TextMate 配色方案
     * @return 失败返回 null
     */
    suspend fun colorScheme(isDark: Boolean, context: Context): TextMateColorScheme? {
        applyTheme(isDark, context)
        val themeModel = themeRegistry.currentThemeModel ?: return null

        return runCatching {
            FmTextMateColorScheme(themeRegistry, themeModel)
        }.getOrElse { e ->
            FmLog.warn(TAG, "Create TextMate color scheme failed", e)
            null
        }
    }
}

private class FmTextMateColorScheme(
    themeRegistry: ThemeRegistry,
    themeModel: ThemeModel
) : TextMateColorScheme(themeRegistry, themeModel) {

    override fun setTheme(themeModel: ThemeModel) {
        super.setTheme(themeModel)
        setColor(
            CURRENT_LINE,
            if (themeModel.isDark) {
                0x1AFFFFFF
            } else {
                0x10000000
            }
        )
    }
}
