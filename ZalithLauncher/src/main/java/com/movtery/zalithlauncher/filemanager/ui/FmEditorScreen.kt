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

package com.movtery.zalithlauncher.filemanager.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.config.FmConfig
import com.movtery.zalithlauncher.filemanager.ui.components.AppBarSubTexts
import com.movtery.zalithlauncher.filemanager.ui.components.FmDialogSurface
import com.movtery.zalithlauncher.filemanager.ui.components.FmEditDialog
import com.movtery.zalithlauncher.filemanager.ui.theme.fmBackgroundColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmCardColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmOnBackgroundColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmOnCardColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSecondaryTextColor
import com.movtery.zalithlauncher.filemanager.ui.theme.fmTopbarColors
import com.movtery.zalithlauncher.filemanager.viewmodel.FileManagerViewModel
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.code_editor.EditorState
import com.movtery.zalithlauncher.ui.code_editor.SoraEditor
import com.movtery.zalithlauncher.ui.code_editor.TextMateRegistry
import com.movtery.zalithlauncher.ui.code_editor.scheme.SchemeIDEADark
import com.movtery.zalithlauncher.ui.code_editor.scheme.SchemeIDEALight
import com.movtery.zalithlauncher.ui.components.ButtonPosition
import com.movtery.zalithlauncher.ui.components.PositionButton
import com.movtery.zalithlauncher.ui.components.PositionFilledTonalButton
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorSearcher
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.math.roundToInt

/** 不可见字符显示组合：行首 / 行内 / 行尾空白 + 行尾符 */
private const val NON_PRINTABLE_FLAGS =
    CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or
        CodeEditor.FLAG_DRAW_WHITESPACE_INNER or
        CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING or
        CodeEditor.FLAG_DRAW_LINE_SEPARATOR

private const val MIN_TEXT_SIZE_PX = 8f
private const val MAX_TEXT_SIZE_PX = 96f
private const val TEXT_SIZE_STEP = 1.15f

/**
 * 文本编辑器页面
 * @param path 待编辑文件的绝对路径
 * @param vm 文件管理器视图模型
 * @param snackHost 全局 Snackbar 宿主
 * @param onBack 返回主页面回调
 * @param onExit 退出文件管理器回调
 * @param onToggleOrientation 横竖屏切换回调
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FmEditorScreen(
    path: Path,
    vm: FileManagerViewModel,
    snackHost: SnackbarHostState,
    contentAlpha: Animatable<Float, AnimationVector1D>,
    onBack: () -> Unit,
    onExit: () -> Unit,
    onToggleOrientation: () -> Unit
) {
    val editorUi by vm.editorUi.collectAsStateWithLifecycle()
    val isDark = isLauncherInDarkTheme()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    LaunchedEffect(path) {
        vm.editorOpen(path)
    }

    var language by remember { mutableStateOf<Language?>(null) }
    var scheme by remember { mutableStateOf<EditorColorScheme?>(null) }
    val fallbackScheme = remember(isDark) {
        if (isDark) SchemeIDEADark() else SchemeIDEALight()
    }
    LaunchedEffect(path, isDark) {
        language = TextMateRegistry.editorLanguageFor(path.fileName?.toString() ?: "", context)
        scheme = TextMateRegistry.colorScheme(isDark, context)
    }

    var editor by remember { mutableStateOf<CodeEditor?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var gotoDialog by remember { mutableStateOf(false) }

    var fabMenuExpanded by remember { mutableStateOf(false) }

    var wordwrap by remember { mutableStateOf(FmConfig.editorWordwrap()) }

    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchCurrent by remember { mutableIntStateOf(0) }
    var searchTotal by remember { mutableIntStateOf(0) }

    var replaceExpanded by remember { mutableStateOf(false) }
    var replaceQuery by remember { mutableStateOf("") }

    /** 搜索选项 */
    var matchCase by remember { mutableStateOf(FmConfig.editorSearchMatchCase()) }
    var wholeWord by remember { mutableStateOf(FmConfig.editorSearchWholeWord()) }
    var useRegex by remember { mutableStateOf(FmConfig.editorSearchRegex()) }

    /** 根据当前选项构造搜索选项 */
    fun currentSearchOptions(): EditorSearcher.SearchOptions {
        val type = when {
            useRegex -> EditorSearcher.SearchOptions.TYPE_REGULAR_EXPRESSION
            wholeWord -> EditorSearcher.SearchOptions.TYPE_WHOLE_WORD
            else -> EditorSearcher.SearchOptions.TYPE_NORMAL
        }
        return EditorSearcher.SearchOptions(type, !matchCase)
    }

    /** 读取搜索器当前匹配计数并同步到界面（无搜索模式时清零） */
    fun updateSearchCount() {
        val searcher = editor?.searcher ?: return
        // stopSearch 后 pattern 为空，访问计数会抛 IllegalStateException，须先判断
        if (!searcher.hasQuery()) {
            searchTotal = 0
            searchCurrent = 0
            return
        }
        searchTotal = searcher.matchedPositionCount
        searchCurrent = (searcher.currentMatchedPositionIndex + 1).coerceAtLeast(0)
    }

    /** 用当前选项执行搜索；非法正则时停止搜索并清零计数 */
    fun applySearch(pattern: String) {
        val searcher = editor?.searcher ?: return
        if (pattern.isEmpty()) {
            searcher.stopSearch()
        } else {
            val searched = runCatching {
                searcher.search(pattern, currentSearchOptions())
            }.isSuccess
            if (!searched) {
                searcher.stopSearch()
            }
        }
        updateSearchCount()
    }

    /** 输入变化时实时搜索 */
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applySearch(query)
    }

    /** 搜索选项变化时重新搜索 */
    fun onSearchOptionChange() {
        applySearch(searchQuery)
    }

    /** 替换后重新执行搜索以刷新匹配结果 */
    fun refreshSearch() {
        applySearch(searchQuery)
    }

    /** 关闭搜索面板并清除高亮 */
    fun closeSearch() {
        searchVisible = false
        searchQuery = ""
        searchTotal = 0
        searchCurrent = 0
        replaceExpanded = false
        replaceQuery = ""
        editor?.searcher?.stopSearch()
    }

    var userReadOnly by remember { mutableStateOf(false) }
    var completionEnabled by remember { mutableStateOf(FmConfig.editorCompletionEnabled()) }
    var lineNumberEnabled by remember { mutableStateOf(FmConfig.editorLineNumber()) }
    var highlightLine by remember { mutableStateOf(FmConfig.editorHighlightLine()) }
    var nonPrintableVisible by remember { mutableStateOf(FmConfig.editorNonPrintable()) }

    var fontSizePx by remember { mutableFloatStateOf(FmConfig.editorFontSize()) }

    val writable = editorUi.writable
    val effectiveReadOnly = !writable || userReadOnly

    val requestExit: () -> Unit = {
        if (vm.editorHasDirty()) {
            vm.editorRequestExitConfirm()
        } else {
            onBack()
        }
    }

    val editorTopBar: @Composable () -> Unit = {
        TopAppBar(
            colors = fmTopbarColors(),
            title = {
                Column {
                    Text(
                        text = path.fileName?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    AppBarSubTexts(
                        texts = listOfNotNull(
                            if (effectiveReadOnly) stringResource(R.string.fm_editor_read_only) else null
                        )
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = requestExit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.fm_nav_back)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onToggleOrientation
                ) {
                    Icon(
                        painter = if (isLandscape) {
                            painterResource(R.drawable.ic_fullscreen_exit)
                        } else {
                            painterResource(R.drawable.ic_mobile_rotate_filled)
                        },
                        contentDescription = null
                    )
                }

                EditorMoreMenu(
                    editor = editor,
                    writable = writable,
                    wordwrap = wordwrap,
                    userReadOnly = userReadOnly,
                    completionEnabled = completionEnabled,
                    lineNumberEnabled = lineNumberEnabled,
                    highlightLine = highlightLine,
                    nonPrintableVisible = nonPrintableVisible,
                    fontSizePx = fontSizePx,
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    onToggleWordwrap = {
                        wordwrap = !wordwrap
                        FmConfig.setEditorWordwrap(wordwrap)
                        editor?.setWordwrap(wordwrap)
                    },
                    onToggleReadOnly = { userReadOnly = !userReadOnly },
                    onToggleCompletion = {
                        completionEnabled = !completionEnabled
                        FmConfig.setEditorCompletionEnabled(completionEnabled)
                        editor?.getComponent(EditorAutoCompletion::class.java)
                            ?.setEnabled(completionEnabled)
                    },
                    onToggleLineNumber = {
                        lineNumberEnabled = !lineNumberEnabled
                        FmConfig.setEditorLineNumber(lineNumberEnabled)
                        editor?.setLineNumberEnabled(lineNumberEnabled)
                    },
                    onToggleHighlightLine = {
                        highlightLine = !highlightLine
                        FmConfig.setEditorHighlightLine(highlightLine)
                        editor?.setHighlightCurrentLine(highlightLine)
                    },
                    onToggleNonPrintable = {
                        nonPrintableVisible = !nonPrintableVisible
                        FmConfig.setEditorNonPrintable(nonPrintableVisible)
                        editor?.setNonPrintablePaintingFlags(
                            if (nonPrintableVisible) NON_PRINTABLE_FLAGS else 0
                        )
                    },
                    onSearch = { searchVisible = true },
                    onGotoLine = { gotoDialog = true },
                    onFontDecrease = {
                        val next = (fontSizePx / TEXT_SIZE_STEP).coerceAtLeast(MIN_TEXT_SIZE_PX)
                        fontSizePx = next
                        FmConfig.setEditorFontSize(next)
                        editor?.setTextSizePx(next)
                    },
                    onFontIncrease = {
                        val next = (fontSizePx * TEXT_SIZE_STEP).coerceAtMost(MAX_TEXT_SIZE_PX)
                        fontSizePx = next
                        FmConfig.setEditorFontSize(next)
                        editor?.setTextSizePx(next)
                    }
                )
            }
        )
    }

    val error = editorUi.error
    if (error != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = contentAlpha.value }
        ) {
            editorTopBar()
            EditorErrorContent(error, onBack)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = contentAlpha.value }
        ) {
            SoraEditor(
                modifier = Modifier.fillMaxSize(),
                state = editorUi.state,
                isReadOnly = effectiveReadOnly,
                language = language,
                scheme = scheme ?: fallbackScheme,
                onSaveClick = { vm.editorSave() },
                onTextChange = { vm.editorTextChanged() },
                onEditorCreated = { e ->
                    editor = e
                    e.isWordwrap = wordwrap
                    e.setLineNumberEnabled(lineNumberEnabled)
                    e.setHighlightCurrentLine(highlightLine)
                    e.getComponent(EditorAutoCompletion::class.java).setEnabled(completionEnabled)
                    e.setNonPrintablePaintingFlags(if (nonPrintableVisible) NON_PRINTABLE_FLAGS else 0)
                    val savedFontSize = fontSizePx
                    if (savedFontSize > 0f) {
                        e.setTextSizePx(savedFontSize)
                    } else {
                        // 未设置过字号时，采用编辑器默认值并回写，后续调整以此为基础
                        fontSizePx = e.textSizePx
                    }
                    // 搜索匹配在后台线程异步执行，完成（或 stopSearch）时派发事件，
                    // 事件线程非主线程，post 回主线程刷新计数
                    e.subscribeEvent(PublishSearchResultEvent::class.java) { _, _ ->
                        e.post {
                            updateSearchCount()
                        }
                    }
                },
                containerColor = fmBackgroundColor(),
                contentColor = fmOnBackgroundColor(),
                topBar = editorTopBar,
                snackbarHost = {
                    SnackbarHost(snackHost)
                },
                floatingActionButton = {
                    FloatingActionButtonMenu(
                        expanded = fabMenuExpanded,
                        button = {
                            ToggleFloatingActionButton(
                                checked = fabMenuExpanded,
                                onCheckedChange = { fabMenuExpanded = it },
                                content = {
                                    Crossfade(
                                        fabMenuExpanded
                                    ) { isExpanded ->
                                        Icon(
                                            painter = painterResource(
                                                if (isExpanded) {
                                                    R.drawable.ic_close
                                                } else {
                                                    R.drawable.ic_more_horiz
                                                }
                                            ),
                                            contentDescription = null,
                                            tint = if (isExpanded) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    ) {
                        FloatingActionButtonMenuItem(
                            onClick = { editor?.undo() },
                            text = { Text(stringResource(R.string.fm_editor_undo)) },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_undo),
                                    contentDescription = null
                                )
                            }
                        )
                        FloatingActionButtonMenuItem(
                            onClick = { editor?.redo() },
                            text = { Text(stringResource(R.string.fm_editor_redo)) },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_redo),
                                    contentDescription = null
                                )
                            }
                        )
                        if (!effectiveReadOnly && editorUi.state is EditorState.Success) {
                            FloatingActionButtonMenuItem(
                                onClick = { vm.editorSave() },
                                text = { Text(stringResource(R.string.generic_save)) },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_save_filled),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                content = if (!searchVisible) null else {
                    {
                        EditorSearchBar(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            editor = editor,
                            query = searchQuery,
                            onQueryChange = ::onSearchQueryChange,
                            currentIndex = searchCurrent,
                            totalCount = searchTotal,
                            onPrevious = {
                                editor?.searcher?.gotoPrevious()
                                updateSearchCount()
                            },
                            onNext = {
                                editor?.searcher?.gotoNext()
                                updateSearchCount()
                            },
                            replaceExpanded = replaceExpanded,
                            onToggleReplace = { replaceExpanded = !replaceExpanded },
                            replaceQuery = replaceQuery,
                            onReplaceQueryChange = { replaceQuery = it },
                            replaceEnabled = !effectiveReadOnly,
                            matchCase = matchCase,
                            wholeWord = wholeWord,
                            useRegex = useRegex,
                            onToggleMatchCase = {
                                matchCase = !matchCase
                                FmConfig.setEditorSearchMatchCase(matchCase)
                                onSearchOptionChange()
                            },
                            onToggleWholeWord = {
                                wholeWord = !wholeWord
                                FmConfig.setEditorSearchWholeWord(wholeWord)
                                onSearchOptionChange()
                            },
                            onToggleRegex = {
                                useRegex = !useRegex
                                FmConfig.setEditorSearchRegex(useRegex)
                                onSearchOptionChange()
                            },
                            onReplaceOne = {
                                editor?.searcher?.replaceCurrentMatch(replaceQuery)
                                refreshSearch()
                            },
                            onReplaceAll = {
                                editor?.searcher?.replaceAll(replaceQuery) { refreshSearch() }
                            },
                            onClose = ::closeSearch,
                        )
                    }
                }
            )
        }
    }

    // 转到指定行对话框
    if (gotoDialog) {
        FmGotoLineDialog(
            maxLine = editor?.lineCount ?: 1,
            onDismiss = { gotoDialog = false },
            onConfirm = { line ->
                editor?.jumpToLine(line - 1)
                gotoDialog = false
            }
        )
    }

    // 保存中
    if (editorUi.saving) {
        FmDialogSurface(
            onDismissRequest = { vm.editorCancelSave() }
        ) {
            Text(
                text = stringResource(R.string.generic_saving),
                style = MaterialTheme.typography.titleMedium
            )
            LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.editorCancelSave() }
            ) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    }

    // 未保存修改的退出确认框
    if (editorUi.exitConfirm) {
        FmDialogSurface(
            onDismissRequest = { vm.editorCancelExitConfirm() }
        ) {
            Text(
                text = stringResource(R.string.generic_save),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.fm_editor_save_changes_message),
                style = MaterialTheme.typography.bodyMedium
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PositionButton(
                    modifier = Modifier.fillMaxWidth(),
                    position = ButtonPosition.Top,
                    onClick = {
                        vm.editorSave { success ->
                            // 保存成功后才退出
                            if (success) {
                                vm.editorCancelExitConfirm()
                                onBack()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.fm_editor_save_and_exit))
                }
                PositionFilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    position = ButtonPosition.Middle,
                    onClick = {
                        vm.editorCancelExitConfirm()
                        onBack()
                    }
                ) {
                    Text(stringResource(R.string.fm_editor_discard))
                }
                PositionFilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    position = ButtonPosition.Bottom,
                    onClick = {
                        vm.editorCancelExitConfirm()
                    }
                ) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        }
    }
}

/**
 * 悬浮搜索面板
 */
@Composable
private fun EditorSearchBar(
    editor: CodeEditor?,
    query: String,
    onQueryChange: (String) -> Unit,
    currentIndex: Int,
    totalCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    replaceExpanded: Boolean,
    onToggleReplace: () -> Unit,
    replaceQuery: String,
    onReplaceQueryChange: (String) -> Unit,
    replaceEnabled: Boolean,
    matchCase: Boolean,
    wholeWord: Boolean,
    useRegex: Boolean,
    onToggleMatchCase: () -> Unit,
    onToggleWholeWord: () -> Unit,
    onToggleRegex: () -> Unit,
    onReplaceOne: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(300.dp),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
        color = fmCardColor(),
        contentColor = fmOnCardColor()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 搜索结果计数
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .defaultMinSize(minWidth = 32.dp),
                    text = if (totalCount > 0) "$currentIndex/$totalCount" else "0",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onPrevious,
                    enabled = editor != null && query.isNotEmpty(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_upward),
                        contentDescription = null
                    )
                }

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onNext,
                    enabled = editor != null && query.isNotEmpty(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_downward),
                        contentDescription = null
                    )
                }

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onClose,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null
                    )
                }
            }

            // 展开替换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onToggleReplace
                ) {
                    Icon(
                        painter = painterResource(
                            if (replaceExpanded) {
                                R.drawable.ic_keyboard_arrow_down
                            } else {
                                R.drawable.ic_keyboard_arrow_right
                            }
                        ),
                        contentDescription = null
                    )
                }

                SmallOutlinedEditField(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SearchOptionToggle(
                                iconRes = R.drawable.ic_match_case,
                                contentDescription = stringResource(R.string.fm_editor_search_match_case),
                                checked = matchCase,
                                onToggle = onToggleMatchCase
                            )
                            SearchOptionToggle(
                                iconRes = R.drawable.ic_match_word,
                                contentDescription = stringResource(R.string.fm_editor_search_whole_word),
                                checked = wholeWord,
                                onToggle = onToggleWholeWord
                            )
                            SearchOptionToggle(
                                iconRes = R.drawable.ic_regular_expression,
                                contentDescription = stringResource(R.string.fm_editor_search_regex),
                                checked = useRegex,
                                onToggle = onToggleRegex
                            )
                        }
                    }
                )
            }

            // 替换输入与替换按钮
            if (replaceExpanded) {
                SmallOutlinedEditField(
                    modifier = Modifier
                        .padding(start = 28.dp)
                        .fillMaxWidth()
                        .height(40.dp),
                    value = replaceQuery,
                    onValueChange = onReplaceQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.medium
                )

                // 替换按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    SmallActionButton(
                        text = stringResource(R.string.fm_editor_replace_all),
                        enabled = replaceEnabled && query.isNotEmpty(),
                        onClick = onReplaceAll
                    )
                    SmallActionButton(
                        text = stringResource(R.string.fm_editor_replace),
                        enabled = replaceEnabled && query.isNotEmpty() && totalCount > 0,
                        onClick = onReplaceOne
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchOptionToggle(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(contentDescription)
            }
        },
        state = tooltipState,
    ) {
        val shape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(shape)
                .background(
                    if (checked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
                .combinedClickable(
                    onClick = onToggle,
                    onLongClick = {
                        scope.launch { tooltipState.show() }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
                tint = if (checked) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(28.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 编辑器更多菜单
 */
@Composable
private fun EditorMoreMenu(
    editor: CodeEditor?,
    writable: Boolean,
    wordwrap: Boolean,
    userReadOnly: Boolean,
    completionEnabled: Boolean,
    lineNumberEnabled: Boolean,
    highlightLine: Boolean,
    nonPrintableVisible: Boolean,
    fontSizePx: Float,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onToggleWordwrap: () -> Unit,
    onToggleReadOnly: () -> Unit,
    onToggleCompletion: () -> Unit,
    onToggleLineNumber: () -> Unit,
    onToggleHighlightLine: () -> Unit,
    onToggleNonPrintable: () -> Unit,
    onSearch: () -> Unit,
    onGotoLine: () -> Unit,
    onFontDecrease: () -> Unit,
    onFontIncrease: () -> Unit
) {
    @Composable
    fun MenuItem(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int?,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        dismissOnClick: Boolean = true,
        onClick: () -> Unit
    ) {
        DropdownMenuItem(
            modifier = modifier,
            enabled = enabled,
            text = { Text(stringResource(labelRes)) },
            leadingIcon = iconRes?.let {
                {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null
                    )
                }
            },
            onClick = {
                if (dismissOnClick) onExpandedChange(false)
                onClick()
            }
        )
    }

    @Composable
    fun SwitchItem(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        checked: Boolean,
        enabled: Boolean = true,
        dismissOnClick: Boolean = false,
        onToggle: () -> Unit
    ) {
        DropdownMenuItem(
            enabled = enabled,
            text = { Text(stringResource(labelRes)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null
                )
            },
            trailingIcon = {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    enabled = enabled
                )
            },
            onClick = {
                if (dismissOnClick) onExpandedChange(false)
                onToggle()
            }
        )
    }

    Box {
        IconButton(
            onClick = { onExpandedChange(true) }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.generic_more)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            MenuItem(R.string.generic_search, R.drawable.ic_search, enabled = editor != null, onClick = onSearch)
            MenuItem(R.string.fm_editor_goto_line, R.drawable.ic_keyboard_double_arrow_up, enabled = editor != null, onClick = onGotoLine)

            HorizontalDivider()

            SwitchItem(R.string.fm_editor_wordwrap, R.drawable.ic_format_align_left, wordwrap, enabled = editor != null, onToggle = onToggleWordwrap)
            SwitchItem(
                R.string.fm_editor_read_only_mode, R.drawable.ic_block_outlined,
                userReadOnly, enabled = editor != null && writable, onToggle = onToggleReadOnly
            )
            SwitchItem(R.string.fm_editor_completion, R.drawable.ic_code, completionEnabled, enabled = editor != null, onToggle = onToggleCompletion)
            SwitchItem(R.string.fm_editor_line_number, R.drawable.ic_text_format, lineNumberEnabled, enabled = editor != null, onToggle = onToggleLineNumber)
            SwitchItem(R.string.fm_editor_highlight_line, R.drawable.ic_center_focus_strong_outlined, highlightLine, enabled = editor != null, onToggle = onToggleHighlightLine)
            SwitchItem(R.string.fm_editor_non_printable, R.drawable.ic_text_snippet_filled, nonPrintableVisible, enabled = editor != null, onToggle = onToggleNonPrintable)

            HorizontalDivider()

            // 字号
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_text_format),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = "${fontSizePx.roundToInt()} px",
                    style = MaterialTheme.typography.labelLarge,
                )
                IconButton(
                    onClick = onFontDecrease,
                    enabled = editor != null && fontSizePx > MIN_TEXT_SIZE_PX,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("−", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(
                    onClick = onFontIncrease,
                    enabled = editor != null && fontSizePx < MAX_TEXT_SIZE_PX,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * 转到指定行对话框
 * @param maxLine 最大行数（1 起始）
 * @param onConfirm 确认跳转，参数为 1 起始行号
 */
@Composable
private fun FmGotoLineDialog(
    maxLine: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val invalidError = stringResource(R.string.fm_editor_goto_invalid)

    FmEditDialog(
        title = stringResource(R.string.fm_editor_goto_line),
        value = input,
        onValueChange = {
            input = it.filter(Char::isDigit)
            error = null
        },
        label = {
            Text(stringResource(R.string.fm_editor_goto_hint, maxLine))
        },
        isError = error != null,
        supportingText = error?.let {
            { Text(it) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        confirmEnabled = input.isNotEmpty() && error == null,
        onDismissRequest = onDismiss,
        onCancel = onDismiss,
        onConfirm = {
            val line = input.toIntOrNull()
            if (line == null || line < 1 || line > maxLine) {
                error = invalidError
            } else {
                onConfirm(line)
            }
        }
    )
}

@Composable
private fun EditorErrorContent(
    error: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = fmSecondaryTextColor(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        FilledTonalButton(onClick = onBack) {
            Text(stringResource(R.string.generic_back))
        }
    }
}
