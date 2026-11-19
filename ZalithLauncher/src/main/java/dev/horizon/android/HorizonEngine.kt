package dev.horizon.android

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/** Launches Starling with a game directory isolated from Voxium's Java data. */
object HorizonEngine {
    private const val TAG = "VoxiumRust"

    fun launch(activity: Activity, onStatus: (String) -> Unit = {}) {
        if (Build.SUPPORTED_ABIS.none { it == "arm64-v8a" }) {
            Toast.makeText(activity, "Rust Edition currently requires a 64-bit ARM device.", Toast.LENGTH_LONG).show()
            return
        }
        if (HorizonInstaller.isReady(activity)) {
            startGame(activity, onStatus)
            return
        }

        onStatus("Preparing Rust Edition…")
        HorizonInstaller.install(activity, object : HorizonInstallCallback {
            override fun onProgress(stage: Int, currentBytes: Long, totalBytes: Long) {
                val label = when (stage) {
                    0 -> "Checking game version…"
                    1 -> "Downloading game resources…"
                    2 -> "Unpacking game files…"
                    3 -> "Preparing worlds…"
                    4 -> "Preparing touch controls…"
                    5 -> "Finishing installation…"
                    else -> "Installing Rust Edition…"
                }
                activity.runOnUiThread { onStatus(label) }
            }

            override fun onComplete(success: Boolean, errorMessage: String?) {
                activity.runOnUiThread {
                    if (success) {
                        onStatus("Starting Rust Edition…")
                        startGame(activity, onStatus)
                    } else {
                        onStatus("Rust installation failed")
                        Toast.makeText(activity, errorMessage ?: "Could not install Rust Edition.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun startGame(activity: Activity, onStatus: (String) -> Unit) {
        try {
            val gameDir = File(activity.filesDir, "voxium/rust/game")
            val resourcesDir = HorizonInstaller.getHorizonHomeDir(activity)
            gameDir.mkdirs()
            File(gameDir, "saves").mkdirs()
            File(gameDir, "resourcepacks").mkdirs()
            File(gameDir, "horizon-mods").mkdirs()

            val args = JSONArray().apply {
                put("--resources")
                put(resourcesDir.absolutePath)
                put("--game-dir")
                put(gameDir.absolutePath)
                put("--name")
                put("Voxium")
            }
            val config = JSONObject().apply {
                put("args", args)
                put("renderer", "vulkan")
                put("render_scale", 1.0)
                put("input_host", "touch")
                put("orientation", "sensor_landscape")
                put("avoid_cutout", true)
                put("use_horizon_controls", true)
            }
            val bytes = config.toString(2).toByteArray(StandardCharsets.UTF_8)
            listOf(
                File(activity.filesDir, "launch_config.json"),
                File(activity.filesDir, "launch.json"),
                File(gameDir, "launch_config.json"),
                File(gameDir, "launch.json")
            ).forEach { target ->
                target.parentFile?.mkdirs()
                val temp = File(target.parentFile, target.name + ".partial")
                FileOutputStream(temp).use { output ->
                    output.write(bytes)
                    output.fd.sync()
                }
                if (target.exists() && !target.delete()) {
                    temp.delete()
                    error("Could not replace ${target.name}")
                }
                if (!temp.renameTo(target)) {
                    temp.delete()
                    error("Could not write ${target.name}")
                }
            }
            onStatus("Launching Rust Edition…")
            activity.startActivity(Intent(activity, HorizonActivity::class.java))
        } catch (error: Throwable) {
            Log.e(TAG, "Could not launch Starling", error)
            onStatus("Rust launch failed")
            Toast.makeText(activity, error.message ?: "Could not launch Rust Edition.", Toast.LENGTH_LONG).show()
        }
    }
}
