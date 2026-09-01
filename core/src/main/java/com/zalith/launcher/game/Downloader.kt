package com.zalith.launcher.game

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Motor de downloads: paralelo, verificação SHA-1 e retomada por Range.
 * Incremental: arquivo existente e íntegro nunca é rebaixado — o Ganho
 * de tempo nas reinstalações/updates é a base da fase de otimização.
 */
class Downloader(parallelism: Int = 4) {

    class Task(
        val url: String,
        val dest: File,
        val sha1: String? = null,
        val size: Long = -1L
    ) {
        val name: String get() = dest.name
    }

    interface Listener {
        fun onTaskStart(task: Task) {}
        fun onTaskSkipped(task: Task) {}   // já íntegro — incremental
        fun onTaskDone(task: Task) {}
        fun onTaskFailed(task: Task, error: String) {}
    }

    private val pool = Executors.newFixedThreadPool(parallelism.coerceIn(1, 16)) { runnable ->
        Thread(runnable, "zl3-download").apply { isDaemon = true }
    }

    // ── verificação ───────────────────────────────────

    fun sha1Of(file: File): String? {
        if (!file.exists()) return null
        return try {
            val digest = MessageDigest.getInstance("SHA-1")
            file.inputStream().use { input ->
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    digest.update(buf, 0, n)
                }
            }
            val out = StringBuilder(40)
            for (b in digest.digest()) {
                out.append(Integer.toHexString((b.toInt() and 0xFF) + 0x100).substring(1))
            }
            out.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun isIntact(file: File, sha1: String?, size: Long): Boolean {
        if (!file.exists()) return false
        if (sha1 != null) return sha1 == sha1Of(file)
        return size <= 0 || file.length() == size
    }

    // ── download único ────────────────────────────────

    /** true = destino íntegro (baixado agora ou já estava). Nunca lança. */
    fun download(task: Task, listener: Listener? = null, attempts: Int = 3): Boolean {
        if (isIntact(task.dest, task.sha1, task.size)) {
            listener?.onTaskSkipped(task)
            return true
        }
        task.dest.parentFile?.mkdirs()
        val part = File(task.dest.parentFile, task.dest.name + ".part")

        listener?.onTaskStart(task)
        var lastError = "sem tentativa"
        for (attempt in 1..attempts) {
            try {
                downloadOnce(task, part)
                if (task.sha1 == null || sha1Of(part) == task.sha1) {
                    if (task.dest.exists()) task.dest.delete()
                    if (!part.renameTo(task.dest)) {
                        part.copyTo(task.dest, overwrite = true)
                        part.delete()
                    }
                    listener?.onTaskDone(task)
                    return true
                }
                lastError = "sha1 divergente"
                part.delete()
            } catch (e: Exception) {
                lastError = e.message ?: e.javaClass.simpleName
                // mantém o .part para retomar na próxima tentativa
            }
        }
        listener?.onTaskFailed(task, lastError)
        return false
    }

    private fun downloadOnce(task: Task, part: File) {
        val conn = URL(task.url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.setRequestProperty("User-Agent", Http.USER_AGENT)
        var append = false
        if (part.exists() && part.length() > 0 && task.size > 0 && part.length() < task.size) {
            conn.setRequestProperty("Range", "bytes=" + part.length() + "-")
            append = true
        }
        val code = conn.responseCode
        if (code !in 200..299 && code != 206) {
            conn.disconnect()
            throw IOException("HTTP " + code)
        }
        if (code == 200) append = false // servidor ignorou o Range: recomeça
        try {
            conn.inputStream.use { input ->
                FileOutputStream(part, append).use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                    }
                    out.flush()
                }
            }
        } finally {
            conn.disconnect()
        }
    }

    // ── lote paralelo ─────────────────────────────────

    /** Bloqueante: baixa tudo em paralelo. Devolve as tasks que falharam. */
    fun downloadAll(tasks: List<Task>, listener: Listener? = null): List<Task> {
        val failed = Collections.synchronizedList(ArrayList<Task>())
        val futures = ArrayList<Future<*>>()
        for (t in tasks) {
            futures.add(pool.submit(Runnable {
                if (!download(t, listener)) failed.add(t)
            }))
        }
        for (f in futures) f.get()
        return failed
    }
}
