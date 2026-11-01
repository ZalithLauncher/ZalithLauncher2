package com.movtery.zalithlauncher.ui.control.pojav

import android.app.AlertDialog
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import java.util.Locale
import kotlin.math.roundToInt

/** Dialog edit kontrol (nama, tombol, ukuran, posisi dinamis, warna, dsb) seperti editor Pojav */
class PojavEditDialog(
    private val host: PojavControlLayout,
    private val control: PojavControl
) {
    private val ctx = host.context
    private val d = control.data
    private var dialog: AlertDialog? = null

    private fun dp(v: Int) = (v * ctx.resources.displayMetrics.density).roundToInt()

    private fun colorStr(c: Int) = String.format(Locale.US, "%08X", c)

    private fun parseColor(s: String, fallback: Int): Int = try {
        val h = s.trim().removePrefix("#")
        val v = h.toLong(16)
        if (h.length <= 6) (0xFF000000L or v).toInt() else v.toInt()
    } catch (e: Exception) {
        fallback
    }

    fun show() {
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(8), dp(16), dp(8))
        val scroll = ScrollView(ctx)
        scroll.addView(root)

        fun label(t: String) {
            val tv = TextView(ctx)
            tv.text = t
            tv.textSize = 12f
            tv.setPadding(0, dp(8), 0, 0)
            root.addView(tv)
        }

        fun field(t: String, v: String, number: Boolean = false): EditText {
            label(t)
            val et = EditText(ctx)
            et.setText(v)
            et.isSingleLine = true
            if (number) et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            root.addView(et)
            return et
        }

        fun check(t: String, v: Boolean): CheckBox {
            val cb = CheckBox(ctx)
            cb.text = t
            cb.isChecked = v
            root.addView(cb)
            return cb
        }

        fun spinner(t: String, opts: List<String>, sel: Int): Spinner {
            label(t)
            val sp = Spinner(ctx)
            sp.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, opts)
            sp.setSelection(sel.coerceAtLeast(0))
            root.addView(sp)
            return sp
        }

        val isDrawer = control is PojavDrawer
        val isJoy = control is PojavJoystick

        val nameEt = field("Nama", d.name)

        val keyNames = PojavKeys.entries.map { it.first }
        val keySpinners = ArrayList<Spinner>()
        val unknownKeys = BooleanArray(4)
        if (!isDrawer) {
            val labels = if (isJoy) listOf("Maju", "Kiri", "Mundur", "Kanan")
            else listOf("Tombol 1", "Tombol 2", "Tombol 3", "Tombol 4")
            for (i in 0..3) {
                val code = d.keycodes.getOrElse(i) { 0 }
                val idx = PojavKeys.indexOf(code)
                unknownKeys[i] = idx < 0
                keySpinners += spinner(labels[i], keyNames, idx)
            }
        }

        val wEt = field("Lebar (dp)", d.width.toString(), true)
        val hEt = field("Tinggi (dp)", d.height.toString(), true)
        val xEt = field("Posisi X (ekspresi dinamis)", d.dynamicX)
        val yEt = field("Posisi Y (ekspresi dinamis)", d.dynamicY)
        val opEt = field("Opasitas (0-100)", (d.opacity * 100f).roundToInt().toString(), true)
        val bgEt = field("Warna latar (AARRGGBB)", colorStr(d.bgColor))
        val stEt = field("Warna garis (AARRGGBB)", colorStr(d.strokeColor))
        val swEt = field("Tebal garis (dp)", d.strokeWidth.toString(), true)
        val crEt = field("Sudut membulat (0-100)", d.cornerRadius.toString(), true)

        val toggleCb = if (!isJoy && !isDrawer) check("Toggle (tahan hidup/mati)", d.isToggle) else null
        val swipeCb = if (!isJoy && !isDrawer) check("Bisa digeser (swipe)", d.isSwipeable) else null
        val passCb = if (!isJoy && !isDrawer) check("Teruskan sentuhan ke kamera", d.passThruEnabled) else null
        val gameCb = check("Tampil saat di dalam game", d.displayInGame)
        val menuCb = check("Tampil saat di menu", d.displayInMenu)

        val lockCb = if (control is PojavJoystick) check("Forward lock (sprint)", control.joystickData.forwardLock) else null
        val orientSp = if (control is PojavDrawer) {
            val o = ControlDrawerData.Orientation.values()
            spinner("Arah laci", o.map { it.name }, o.indexOf(control.drawerData.orientation))
        } else null

        fun apply() {
            d.name = nameEt.text.toString()
            keySpinners.forEachIndexed { i, sp ->
                val sel = sp.selectedItemPosition
                if (!(unknownKeys[i] && sel == 0)) {
                    d.keycodes[i] = PojavKeys.entries.getOrNull(sel)?.second ?: 0
                }
            }
            d.width = wEt.text.toString().toFloatOrNull()?.coerceAtLeast(8f) ?: d.width
            d.height = hEt.text.toString().toFloatOrNull()?.coerceAtLeast(8f) ?: d.height
            d.dynamicX = xEt.text.toString().ifBlank { "0" }
            d.dynamicY = yEt.text.toString().ifBlank { "0" }
            d.opacity = ((opEt.text.toString().toFloatOrNull() ?: 100f) / 100f).coerceIn(0f, 1f)
            d.bgColor = parseColor(bgEt.text.toString(), d.bgColor)
            d.strokeColor = parseColor(stEt.text.toString(), d.strokeColor)
            d.strokeWidth = swEt.text.toString().toFloatOrNull()?.coerceAtLeast(0f) ?: d.strokeWidth
            d.cornerRadius = crEt.text.toString().toFloatOrNull()?.coerceIn(0f, 100f) ?: d.cornerRadius
            toggleCb?.let { d.isToggle = it.isChecked }
            swipeCb?.let { d.isSwipeable = it.isChecked }
            passCb?.let { d.passThruEnabled = it.isChecked }
            d.displayInGame = gameCb.isChecked
            d.displayInMenu = menuCb.isChecked
            if (control is PojavJoystick && lockCb != null) control.joystickData.forwardLock = lockCb.isChecked
            if (control is PojavDrawer && orientSp != null) {
                control.drawerData.orientation = ControlDrawerData.Orientation.values()[orientSp.selectedItemPosition]
            }
            host.markDirty()
            host.build()
        }

        fun action(t: String, run: () -> Unit) {
            val b = Button(ctx)
            b.text = t
            b.isAllCaps = false
            b.setOnClickListener {
                apply()
                dialog?.dismiss()
                run()
            }
            root.addView(b)
        }

        action("Duplikat") { host.cloneControl(control) }
        if (control is PojavDrawer) {
            action("Tambah sub-tombol") { host.addSubButton(control) }
            action(if (control.isOpen) "Tutup sub-tombol" else "Tampilkan sub-tombol") {
                control.isOpen = !control.isOpen
                host.updateVisibility()
            }
        }

        val title = when {
            isJoy -> "Edit joystick"
            isDrawer -> "Edit laci"
            else -> "Edit tombol"
        }
        dialog = AlertDialog.Builder(ctx)
            .setTitle(title)
            .setView(scroll)
            .setPositiveButton("Simpan") { _, _ -> apply() }
            .setNegativeButton("Batal", null)
            .setNeutralButton("Hapus") { _, _ -> host.deleteControl(control) }
            .create()
        dialog?.show()
    }
}
