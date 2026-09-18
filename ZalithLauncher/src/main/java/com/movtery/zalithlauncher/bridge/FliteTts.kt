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

package com.movtery.zalithlauncher.bridge

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.content.Context
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.utils.logging.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * 游戏复述功能的安卓 TTS 后端，替代缺失的 flite 语音引擎
 * Reference from FoldCraftLauncher PR #1821（FCL/src/main/java/com/mio/flite/FliteTts.kt，GPL-3.0）
 * https://github.com/FCL-Team/FoldCraftLauncher/pull/1821
 */
object FliteTts {
    private const val TAG = "FliteTTS"
    /** init 的快速判定窗口：无引擎时 onInit(ERROR) 几乎立即到达，超时则视为引擎仍在冷启动 */
    private const val FAST_CHECK_SECONDS = 2L

    private const val STATE_UNINIT = 0
    private const val STATE_INITIALIZING = 1
    private const val STATE_READY = 2
    private const val STATE_FAILED = 3

    // TextToSpeech 需要在带 Looper 的线程上构建与回调，游戏侧调用线程没有 Looper
    private val ttsThread = HandlerThread(TAG)
        .apply { start() }

    // 只在对象锁内读写，与 state 的流转共享同一把锁
    private val pending = ArrayDeque<PendingSpeech>()

    @Volatile
    private var state = STATE_UNINIT

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var readySignal = CountDownLatch(1)

    @Volatile
    private var candidateEngines: List<String> = emptyList()
    private val triedEngines: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private val utteranceCounter = AtomicLong()

    /** 引擎就绪前到达的朗读文本，就绪后按序补播 */
    private data class PendingSpeech(val text: String, val gain: Float)

    /** 初始化 TTS 引擎；引擎冷启动时乐观返回 true，就绪前的朗读会在就绪后按序补播 */
    @JvmStatic
    fun init(): Boolean {
        synchronized(this) {
            when (state) {
                STATE_READY -> return true
                STATE_FAILED -> return false
                STATE_INITIALIZING -> return true
                else -> {
                    readySignal = CountDownLatch(1)
                    state = STATE_INITIALIZING
                    val signal = readySignal
                    Handler(ttsThread.looper).post { constructTts(signal, null) }
                }
            }
        }
        // 快速判定期间不得持有对象锁：onInit 回调与 tts 赋值都依赖锁外的执行进度
        val arrived = try {
            readySignal.await(FAST_CHECK_SECONDS, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            false
        }
        return if (arrived) state == STATE_READY else true
    }

    /** 排队朗读一段 UTF-8 文本并立即返回；不可用或文本无效返回 -1 */
    @JvmStatic
    fun speak(message: ByteArray, gain: Float): Float {
        if (state == STATE_FAILED) return -1f
        val text = runCatching { String(message, Charsets.UTF_8) }.getOrNull() ?: return -1f
        if (text.isBlank()) return -1f
        synchronized(this) {
            val instance = tts
            if (state == STATE_READY && instance != null) {
                enqueue(instance, text, gain)
            } else {
                pending.addLast(PendingSpeech(text, gain))
            }
        }
        return 0f
    }

    /** 立即停止当前朗读并丢弃全部排队文本，游戏打断复述时的截停入口 */
    @JvmStatic
    fun cancel() {
        synchronized(this) { pending.clear() }
        tts?.runCatching { stop() }
    }

    /** 释放 TTS 引擎并丢弃全部排队文本 */
    @JvmStatic
    @Synchronized
    fun shutdown() {
        state = STATE_UNINIT
        releaseInstance()
        pending.clear()
        readySignal.countDown()
        triedEngines.clear()
        candidateEngines = emptyList()
    }

    private fun enqueue(instance: TextToSpeech, text: String, gain: Float) {
        val utteranceId = "zl-flite-${utteranceCounter.incrementAndGet()}"
        val params = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, gain) }
        if (instance.speak(text, TextToSpeech.QUEUE_ADD, params, utteranceId) != TextToSpeech.SUCCESS) {
            Logger.error(TAG, "speak enqueue failed: $utteranceId")
        }
    }

    /** 须持对象锁调用：把引擎就绪前排队的文本按序补播进 TTS 队列 */
    private fun drainPending() {
        val instance = tts ?: return
        while (pending.isNotEmpty()) {
            val speech = pending.removeFirst()
            enqueue(instance, speech.text, speech.gain)
        }
    }

    private fun constructTts(signal: CountDownLatch, engine: String?) {
        val ref = AtomicReference<TextToSpeech>()
        try {
            val listener = TextToSpeech.OnInitListener { code ->
                if (state == STATE_INITIALIZING) {
                    if (code == TextToSpeech.SUCCESS) {
                        synchronized(this) {
                            if (state == STATE_INITIALIZING) {
                                ref.get()?.let { tts = it }
                                state = STATE_READY
                                drainPending()
                            }
                        }
                        Logger.info(TAG, "TextToSpeech ready (engine=${engine ?: "default"})")
                        signal.countDown()
                    } else {
                        Logger.error(TAG, "engine init failed: engine=${engine ?: "default"} status=$code")
                        // 非终态：还有候选引擎时不放行等待方，由回退结果决定
                        tryNextEngine(signal, ref.get(), engine)
                    }
                }
                // 过期回调（期间已 shutdown/重新初始化）直接忽略
            }
            val context = GlobalContext.applicationContext
            val instance = if (engine == null) {
                TextToSpeech(context, listener)
            } else {
                TextToSpeech(context, listener, engine)
            }
            ref.set(instance)
            instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {}

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Logger.error(TAG, "utterance failed: $utteranceId")
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {}
            })
            synchronized(this) {
                if (state != STATE_INITIALIZING) {
                    // 构建期间已被 shutdown，立即释放避免泄漏
                    instance.shutdown()
                    return
                }
                tts = instance
                candidateEngines = runCatching { instance.engines.map { it.name } }.getOrDefault(emptyList())
            }
        } catch (e: Throwable) {
            Logger.error(TAG, "create TextToSpeech failed", e)
            tryNextEngine(signal, null, engine)
        }
    }

    /** 当前引擎初始化失败后，依次尝试设备上其他已安装的语音引擎 */
    private fun tryNextEngine(signal: CountDownLatch, failed: TextToSpeech?, failedEngine: String?) {
        triedEngines.add(failedEngine ?: "default")
        val next = candidateEngines.firstOrNull { it !in triedEngines }
        if (next == null) {
            state = STATE_FAILED
            pending.clear()
            Logger.error(TAG, "no usable TTS engine (tried=$triedEngines), narrator disabled")
            signal.countDown()
            return
        }
        Logger.info(TAG, "falling back to TTS engine: $next")
        Handler(ttsThread.looper).post {
            failed?.runCatching { shutdown() }
            constructTts(signal, next)
        }
    }

    private fun releaseInstance() {
        val instance = tts ?: return
        tts = null
        Handler(ttsThread.looper).post {
            try {
                instance.shutdown()
            } catch (e: Throwable) {
                Logger.error(TAG, "shutdown TextToSpeech failed", e)
            }
        }
    }
}
