package com.movtery.zalithlauncher.ui.control.pojav

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.util.Locale

/*
 * Port kontrol kustom PojavLauncher (format JSON kompatibel dengan Pojav v3).
 * Nilai keycode > 0 = kode GLFW, 0 = kosong, < 0 = tombol spesial.
 */

const val SPECIALBTN_KEYBOARD = -1
const val SPECIALBTN_TOGGLECTRL = -2
const val SPECIALBTN_MOUSEPRI = -3
const val SPECIALBTN_MOUSESEC = -4
const val SPECIALBTN_VIRTUALMOUSE = -5
const val SPECIALBTN_MOUSEMID = -6
const val SPECIALBTN_SCROLLUP = -7
const val SPECIALBTN_SCROLLDOWN = -8
const val SPECIALBTN_MENU = -9

object PojavJson {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
}

@Keep
open class ControlData {
    var dynamicX: String = "0"
    var dynamicY: String = "0"
    var isToggle: Boolean = false
    var passThruEnabled: Boolean = false
    var name: String = "button"
    var keycodes: IntArray = IntArray(4)
    var opacity: Float = 1f
    var bgColor: Int = 0x4D000000
    var strokeColor: Int = -1
    var strokeWidth: Float = 0f
    var cornerRadius: Float = 0f
    var isSwipeable: Boolean = false
    var displayInGame: Boolean = true
    var displayInMenu: Boolean = true
    var width: Float = 50f   // dp
    var height: Float = 50f  // dp

    fun containsKeycode(code: Int): Boolean = keycodes.contains(code)
}

@Keep
class ControlJoystickData : ControlData() {
    var absolute: Boolean = false
    var forwardLock: Boolean = true

    init {
        name = "joystick"
        keycodes = intArrayOf(87, 65, 83, 68) // W A S D
        width = 130f
        height = 130f
    }
}

@Keep
class ControlDrawerData {
    enum class Orientation { DOWN, LEFT, UP, RIGHT, FREE }

    var buttonProperties: MutableList<ControlData> = ArrayList()
    var properties: ControlData = ControlData().apply { name = "drawer" }
    var orientation: Orientation = Orientation.FREE
}

@Keep
class CustomControls {
    var scaledAt: Float = 100f
    var version: Int = -1
    var mControlDataList: MutableList<ControlData> = ArrayList()
    var mDrawerDataList: MutableList<ControlDrawerData> = ArrayList()
    var mJoystickDataList: MutableList<ControlJoystickData> = ArrayList()
}

fun ControlData.deepCopy(): ControlData =
    PojavJson.gson.fromJson(PojavJson.gson.toJson(this), this.javaClass)

fun ControlDrawerData.deepCopy(): ControlDrawerData =
    PojavJson.gson.fromJson(PojavJson.gson.toJson(this), ControlDrawerData::class.java)

/** Evaluator ekspresi posisi dinamis ala Pojav: ${var}, + - * /, kurung, dp(), px() */
object PojavExpr {
    private val varRegex = Regex("""\$\{(\w+)}""")

    fun eval(expr: String, vars: Map<String, Float>, density: Float): Float {
        val replaced = varRegex.replace(expr) { m ->
            String.format(Locale.US, "%.4f", vars[m.groupValues[1]] ?: 0f)
        }
        return try {
            Parser(replaced, density).parse()
        } catch (e: Exception) {
            0f
        }
    }

    private class Parser(private val s: String, private val density: Float) {
        private var i = 0

        fun parse(): Float = expr()

        private fun ws() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        private fun expr(): Float {
            var v = term()
            while (true) {
                ws()
                if (i < s.length && (s[i] == '+' || s[i] == '-')) {
                    val op = s[i++]
                    val r = term()
                    v = if (op == '+') v + r else v - r
                } else return v
            }
        }

        private fun term(): Float {
            var v = factor()
            while (true) {
                ws()
                if (i < s.length && (s[i] == '*' || s[i] == '/')) {
                    val op = s[i++]
                    val r = factor()
                    v = if (op == '*') v * r else v / r
                } else return v
            }
        }

        private fun factor(): Float {
            ws()
            if (i >= s.length) throw IllegalStateException("eof")
            val c = s[i]
            if (c == '-') { i++; return -factor() }
            if (c == '+') { i++; return factor() }
            if (c == '(') {
                i++
                val v = expr()
                ws()
                if (i < s.length && s[i] == ')') i++
                return v
            }
            if (c.isLetter()) {
                val st = i
                while (i < s.length && s[i].isLetter()) i++
                val fn = s.substring(st, i)
                ws()
                if (i >= s.length || s[i] != '(') throw IllegalStateException("bad fn")
                i++
                val a = expr()
                ws()
                if (i < s.length && s[i] == ')') i++
                return when (fn) {
                    "dp" -> a / density
                    "px" -> a * density
                    else -> a
                }
            }
            val st = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (st == i) throw IllegalStateException("bad char")
            return s.substring(st, i).toFloat()
        }
    }
}

object PojavLayoutStore {
    /** Letak file layout Pojav yang dipakai game (dapat diakses lewat Android/data/<paket>/files/pojav_controls/) */
    fun defaultFile(context: Context): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(base, "pojav_controls")
        dir.mkdirs()
        return File(dir, "default.json")
    }

    @Suppress("SENSELESS_COMPARISON", "USELESS_ELVIS")
    private fun normalize(c: CustomControls): CustomControls {
        if (c.mControlDataList == null) c.mControlDataList = ArrayList()
        if (c.mDrawerDataList == null) c.mDrawerDataList = ArrayList()
        if (c.mJoystickDataList == null) c.mJoystickDataList = ArrayList()
        if (c.scaledAt <= 0f) c.scaledAt = 100f
        fun fix(d: ControlData?) {
            if (d == null) return
            val k: IntArray? = d.keycodes
            val out = IntArray(4)
            if (k != null) System.arraycopy(k, 0, out, 0, minOf(4, k.size))
            d.keycodes = out
            if (d.dynamicX == null) d.dynamicX = "0"
            if (d.dynamicY == null) d.dynamicY = "0"
            if (d.name == null) d.name = ""
        }
        c.mControlDataList.forEach { fix(it) }
        c.mJoystickDataList.forEach { fix(it) }
        c.mDrawerDataList.forEach { dr ->
            if (dr.buttonProperties == null) dr.buttonProperties = ArrayList()
            fix(dr.properties)
            dr.buttonProperties.forEach { fix(it) }
        }
        return c
    }

    /** Memuat layout Pojav; bila file tidak ada / bukan format Pojav, dipakai layout bawaan. */
    fun load(file: File?): CustomControls {
        if (file != null && file.isFile) {
            try {
                val text = file.readText()
                if (text.contains("mControlDataList")) {
                    val c = PojavJson.gson.fromJson(text, CustomControls::class.java)
                    if (c != null) return normalize(c)
                }
            } catch (_: Exception) {
            }
        }
        return PojavDefaultLayout.create()
    }

    fun save(file: File, controls: CustomControls) {
        file.parentFile?.mkdirs()
        file.writeText(PojavJson.gson.toJson(controls))
    }
}

object PojavDefaultLayout {
    private fun btn(
        name: String, key: Int, x: String, y: String,
        w: Float = 50f, h: Float = 50f,
        toggle: Boolean = false, pass: Boolean = false
    ) = ControlData().apply {
        this.name = name
        keycodes = intArrayOf(key, 0, 0, 0)
        dynamicX = x
        dynamicY = y
        width = w
        height = h
        isToggle = toggle
        passThruEnabled = pass
    }

    fun create(): CustomControls {
        val c = CustomControls()
        c.mJoystickDataList.add(ControlJoystickData().apply {
            dynamicX = "px(24)"
            dynamicY = "\${bottom} - px(24)"
        })
        c.mControlDataList.add(btn("Jump", 32, "\${right} - px(24)", "\${bottom} - px(100)", 56f, 56f))
        c.mControlDataList.add(btn("Sneak", 340, "\${right} - px(24)", "\${bottom} - px(24)", 56f, 56f, toggle = true))
        c.mControlDataList.add(btn("Attack", SPECIALBTN_MOUSEPRI, "\${right} - px(100)", "\${bottom} - px(64)", 60f, 60f, pass = true))
        c.mControlDataList.add(btn("Use", SPECIALBTN_MOUSESEC, "\${right} - px(100)", "\${bottom} - px(144)", 60f, 60f, pass = true))
        val top = listOf(
            Triple("Keyboard", SPECIALBTN_KEYBOARD, 0),
            Triple("GUI", SPECIALBTN_TOGGLECTRL, 1),
            Triple("Chat", 84, 2),
            Triple("Inv", 69, 3),
            Triple("Drop", 81, 4),
            Triple("F5", 294, 5),
            Triple("Menu", SPECIALBTN_MENU, 6)
        )
        top.forEach { (n, k, i) ->
            c.mControlDataList.add(btn(n, k, "px(${8 + i * 52})", "px(8)", 48f, 36f))
        }
        return c
    }
}

object PojavKeys {
    val entries: List<Pair<String, Int>> by lazy {
        val l = ArrayList<Pair<String, Int>>()
        l += "(kosong)" to 0
        l += "SPECIAL_KEYBOARD" to SPECIALBTN_KEYBOARD
        l += "SPECIAL_GUI (sembunyi kontrol)" to SPECIALBTN_TOGGLECTRL
        l += "SPECIAL_PRI (klik kiri)" to SPECIALBTN_MOUSEPRI
        l += "SPECIAL_SEC (klik kanan)" to SPECIALBTN_MOUSESEC
        l += "SPECIAL_MID (klik tengah)" to SPECIALBTN_MOUSEMID
        l += "SPECIAL_MOUSE (virtual mouse)" to SPECIALBTN_VIRTUALMOUSE
        l += "SPECIAL_SCROLLUP" to SPECIALBTN_SCROLLUP
        l += "SPECIAL_SCROLLDOWN" to SPECIALBTN_SCROLLDOWN
        l += "SPECIAL_MENU" to SPECIALBTN_MENU
        for (c in 'A'..'Z') l += c.toString() to c.code
        for (c in '0'..'9') l += c.toString() to c.code
        l += "SPACE" to 32
        l += "ENTER" to 257
        l += "ESCAPE" to 256
        l += "TAB" to 258
        l += "BACKSPACE" to 259
        l += "LEFT_SHIFT" to 340
        l += "LEFT_CTRL" to 341
        l += "LEFT_ALT" to 342
        l += "RIGHT_SHIFT" to 344
        l += "RIGHT_CTRL" to 345
        l += "RIGHT_ALT" to 346
        l += "CAPS_LOCK" to 280
        l += "UP" to 265
        l += "DOWN" to 264
        l += "LEFT" to 263
        l += "RIGHT" to 262
        for (i in 1..12) l += "F$i" to (289 + i)
        l += "INSERT" to 260
        l += "DELETE" to 261
        l += "PAGE_UP" to 266
        l += "PAGE_DOWN" to 267
        l += "HOME" to 268
        l += "END" to 269
        l += "MINUS (-)" to 45
        l += "EQUAL (=)" to 61
        l += "COMMA (,)" to 44
        l += "PERIOD (.)" to 46
        l += "SLASH (/)" to 47
        l += "SEMICOLON (;)" to 59
        l += "APOSTROPHE (')" to 39
        l += "BACKSLASH (\\)" to 92
        l += "LEFT_BRACKET ([)" to 91
        l += "RIGHT_BRACKET (])" to 93
        l += "GRAVE (`)" to 96
        for (i in 0..9) l += "KP_$i" to (320 + i)
        l
    }

    fun indexOf(code: Int): Int = entries.indexOfFirst { it.second == code }
}
