package com.movtery.zalithlauncher.ui.control.pojav

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

interface PojavControl {
    val data: ControlData
    val view: View
    fun refresh()
    fun place()
    fun releaseAll()
}

/**
 * Layer kontrol bergaya PojavLauncher (View asli, bukan Compose).
 * Sentuhan di area kosong tidak dikonsumsi, sehingga diteruskan ke layer mouse di bawahnya.
 */
@SuppressLint("ViewConstructor")
class PojavControlLayout(context: Context) : FrameLayout(context) {
    var controls: CustomControls = CustomControls()
        private set
    var file: File? = null
    var editMode = false
    var onSpecial: (Int) -> Unit = {}
    var onExitEditor: () -> Unit = {}
    var onPointerOccupied: (Int) -> Unit = {}
    var onPointerReleased: (Int) -> Unit = {}
    var cameraSensitivity = 1f
    var lastBackSignal = 0

    private val views = ArrayList<PojavControl>()
    private val openDrawers = HashSet<ControlDrawerData>()
    private var guiVisible = true
    private var grabbing = false
    private var dirty = false
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    val density: Float get() = resources.displayMetrics.density
    val layoutScaleFactor: Float get() = 100f / (if (controls.scaledAt > 0f) controls.scaledAt else 100f)
    private val screenW: Float get() = (if (width > 0) width else resources.displayMetrics.widthPixels).toFloat()
    private val screenH: Float get() = (if (height > 0) height else resources.displayMetrics.heightPixels).toFloat()

    fun sizePx(d: ControlData): Pair<Float, Float> =
        Pair(d.width * density * layoutScaleFactor, d.height * density * layoutScaleFactor)

    private fun evalExpr(expr: String, w: Float, h: Float): Float {
        val vars = mapOf(
            "top" to 0f, "left" to 0f,
            "right" to screenW - w, "bottom" to screenH - h,
            "width" to w, "height" to h,
            "screen_width" to screenW, "screen_height" to screenH,
            "margin" to 2f * density, "preferred_scale" to 100f
        )
        return PojavExpr.eval(expr, vars, density)
    }

    fun placeView(v: View, d: ControlData, pos: Pair<Float, Float>?) {
        val (w, h) = sizePx(d)
        val lp = (v.layoutParams as? LayoutParams) ?: LayoutParams(0, 0)
        lp.width = w.roundToInt()
        lp.height = h.roundToInt()
        lp.gravity = Gravity.TOP or Gravity.START
        val x = pos?.first ?: evalExpr(d.dynamicX, w, h)
        val y = pos?.second ?: evalExpr(d.dynamicY, w, h)
        lp.leftMargin = x.roundToInt()
        lp.topMargin = y.roundToInt()
        v.layoutParams = lp
    }

    fun isDrawerOpen(d: ControlDrawerData) = openDrawers.contains(d)
    fun setDrawerOpen(d: ControlDrawerData, open: Boolean) {
        if (open) openDrawers.add(d) else openDrawers.remove(d)
    }

    fun markDirty() { dirty = true }

    fun load(f: File?) {
        file = f
        controls = PojavLayoutStore.load(f)
        build()
    }

    fun build() {
        views.forEach { it.releaseAll() }
        removeAllViews()
        views.clear()
        controls.mControlDataList.forEach { add(PojavButton(this, it)) }
        controls.mJoystickDataList.forEach { add(PojavJoystick(this, it)) }
        controls.mDrawerDataList.forEach { dd ->
            val dv = PojavDrawer(this, dd)
            add(dv)
            dv.createSubs()
            dv.subs.forEach { add(it) }
        }
        if (editMode) addToolbar()
        post {
            placeAll()
            updateVisibility()
        }
    }

    private fun add(c: PojavControl) {
        views.add(c)
        addView(c.view)
    }

    fun placeAll() {
        views.forEach {
            it.refresh()
            it.place()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            placeAll()
            updateVisibility()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        views.forEach { it.releaseAll() }
    }

    private fun isHideable(d: ControlData) =
        !d.containsKeycode(SPECIALBTN_TOGGLECTRL) && !d.containsKeycode(SPECIALBTN_VIRTUALMOUSE)

    private fun shouldShow(d: ControlData): Boolean {
        if (editMode) return true
        if (!isHideable(d)) return true
        if (!guiVisible) return false
        return if (grabbing) d.displayInGame else d.displayInMenu
    }

    fun updateVisibility() {
        for (c in views) {
            var show = shouldShow(c.data)
            val parentDrawer = (c as? PojavButton)?.parentDrawer
            if (parentDrawer != null) {
                show = show && parentDrawer.isOpen && shouldShow(parentDrawer.drawerData.properties)
            }
            c.view.visibility = if (show) VISIBLE else GONE
        }
    }

    fun setGrabbing(g: Boolean) {
        if (grabbing != g) {
            grabbing = g
            updateVisibility()
        }
    }

    fun toggleGui() {
        guiVisible = !guiVisible
        updateVisibility()
    }

    fun hitButton(rawX: Float, rawY: Float, exclude: PojavButton?): PojavButton? {
        val loc = IntArray(2)
        for (i in views.indices.reversed()) {
            val c = views[i]
            if (c !is PojavButton || c === exclude || c is PojavDrawer || c.visibility != VISIBLE) continue
            c.getLocationOnScreen(loc)
            if (rawX >= loc[0] && rawX <= loc[0] + c.width && rawY >= loc[1] && rawY <= loc[1] + c.height) return c
        }
        return null
    }

    // ---------------- Mode edit ----------------

    private var dragRawX = 0f
    private var dragRawY = 0f
    private var dragLeft = 0
    private var dragTop = 0
    private var dragMoved = false

    private fun fmt(f: Float) = String.format(Locale.US, "%.5f", f)

    private fun genDynX(x: Float, w: Float): String =
        if (x + w / 2f > screenW / 2f) "${fmt((x + w) / screenW)} * \${screen_width} - \${width}"
        else "${fmt(x / screenW)} * \${screen_width}"

    private fun genDynY(y: Float, h: Float): String =
        if (y + h / 2f > screenH / 2f) "${fmt((y + h) / screenH)} * \${screen_height} - \${height}"
        else "${fmt(y / screenH)} * \${screen_height}"

    fun handleEditTouch(v: View, c: PojavControl, e: MotionEvent, draggable: Boolean): Boolean {
        val lp = v.layoutParams as? LayoutParams ?: return true
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                v.parent?.requestDisallowInterceptTouchEvent(true)
                dragRawX = e.rawX
                dragRawY = e.rawY
                dragLeft = lp.leftMargin
                dragTop = lp.topMargin
                dragMoved = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.rawX - dragRawX
                val dy = e.rawY - dragRawY
                if (!dragMoved && hypot(dx, dy) > slop) dragMoved = true
                if (dragMoved && draggable) {
                    lp.leftMargin = (dragLeft + dx).roundToInt().coerceIn(0, max(0, width - lp.width))
                    lp.topMargin = (dragTop + dy).roundToInt().coerceIn(0, max(0, height - lp.height))
                    v.layoutParams = lp
                    (c as? PojavDrawer)?.placeSubs()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (dragMoved && draggable) {
                    c.data.dynamicX = genDynX(lp.leftMargin.toFloat(), lp.width.toFloat())
                    c.data.dynamicY = genDynY(lp.topMargin.toFloat(), lp.height.toFloat())
                    dirty = true
                } else if (!dragMoved) {
                    PojavEditDialog(this, c).show()
                }
            }
        }
        return true
    }

    private fun toast(t: String) = Toast.makeText(context, t, Toast.LENGTH_SHORT).show()

    private fun addToolbar() {
        val bar = LinearLayout(context)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.setBackgroundColor(0xCC202020.toInt())
        fun b(text: String, onClick: () -> Unit) {
            val btn = Button(context)
            btn.text = text
            btn.isAllCaps = false
            btn.textSize = 11f
            btn.minWidth = 0
            btn.minimumWidth = 0
            btn.setOnClickListener { onClick() }
            bar.addView(btn)
        }
        b("+ Tombol") { addButton() }
        b("+ Joystick") { addJoystick() }
        b("+ Laci") { addDrawer() }
        b("Simpan") { if (save()) toast("Layout disimpan") }
        b("Reset") { confirmReset() }
        b("Keluar") { requestExit() }
        addView(bar, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
    }

    private fun centerButton(name: String) = ControlData().apply {
        this.name = name
        dynamicX = "0.5 * \${screen_width} - \${width} / 2"
        dynamicY = "0.5 * \${screen_height}"
    }

    fun addButton() {
        controls.mControlDataList.add(centerButton("Baru"))
        dirty = true
        build()
    }

    fun addJoystick() {
        controls.mJoystickDataList.add(ControlJoystickData().apply {
            dynamicX = "0.5 * \${screen_width} - \${width} / 2"
            dynamicY = "0.5 * \${screen_height}"
        })
        dirty = true
        build()
    }

    fun addDrawer() {
        controls.mDrawerDataList.add(ControlDrawerData().apply {
            properties = centerButton("Laci")
            orientation = ControlDrawerData.Orientation.RIGHT
        })
        dirty = true
        build()
    }

    fun addSubButton(drawer: PojavDrawer) {
        drawer.drawerData.buttonProperties.add(ControlData().apply {
            name = "Sub"
            width = drawer.drawerData.properties.width
            height = drawer.drawerData.properties.height
            dynamicX = "0.5 * \${screen_width}"
            dynamicY = "0.5 * \${screen_height}"
        })
        setDrawerOpen(drawer.drawerData, true)
        dirty = true
        build()
    }

    fun deleteControl(c: PojavControl) {
        val pd = (c as? PojavButton)?.parentDrawer
        when {
            c is PojavDrawer -> controls.mDrawerDataList.remove(c.drawerData)
            c is PojavJoystick -> controls.mJoystickDataList.remove(c.joystickData)
            pd != null -> pd.drawerData.buttonProperties.remove(c.data)
            else -> controls.mControlDataList.remove(c.data)
        }
        dirty = true
        build()
    }

    fun cloneControl(c: PojavControl) {
        val pd = (c as? PojavButton)?.parentDrawer
        val pos = "0.5 * \${screen_width}"
        val posY = "0.5 * \${screen_height}"
        when {
            c is PojavDrawer -> controls.mDrawerDataList.add(c.drawerData.deepCopy().also {
                it.properties.dynamicX = pos
                it.properties.dynamicY = posY
            })
            c is PojavJoystick -> controls.mJoystickDataList.add(
                PojavJson.gson.fromJson(PojavJson.gson.toJson(c.joystickData), ControlJoystickData::class.java).also {
                    it.dynamicX = pos
                    it.dynamicY = posY
                })
            pd != null -> pd.drawerData.buttonProperties.add(c.data.deepCopy())
            else -> controls.mControlDataList.add(c.data.deepCopy().also {
                it.dynamicX = pos
                it.dynamicY = posY
            })
        }
        dirty = true
        build()
    }

    fun save(): Boolean {
        val f = file ?: return false
        return try {
            controls.scaledAt = 100f
            PojavLayoutStore.save(f, controls)
            dirty = false
            true
        } catch (e: Exception) {
            toast("Gagal menyimpan: ${e.message}")
            false
        }
    }

    private fun confirmReset() {
        AlertDialog.Builder(context)
            .setTitle("Reset layout")
            .setMessage("Kembalikan ke layout bawaan? Perubahan yang belum disimpan akan hilang.")
            .setPositiveButton("Reset") { _, _ ->
                controls = PojavDefaultLayout.create()
                openDrawers.clear()
                dirty = true
                build()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    fun requestExit() {
        if (!dirty) {
            onExitEditor()
            return
        }
        AlertDialog.Builder(context)
            .setTitle("Keluar dari editor")
            .setMessage("Simpan perubahan kontrol?")
            .setPositiveButton("Simpan") { _, _ ->
                save()
                onExitEditor()
            }
            .setNegativeButton("Buang") { _, _ -> onExitEditor() }
            .setNeutralButton("Batal", null)
            .show()
    }
}

/** Tombol bergaya Pojav (juga dipakai sebagai sub-tombol laci) */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
open class PojavButton(
    val host: PojavControlLayout,
    override val data: ControlData,
    val parentDrawer: PojavDrawer? = null
) : TextView(host.context), PojavControl {
    override val view: View get() = this

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radiusPx = 0f
    protected var pressedVisual = false
    private var toggled = false
    private var keysDown = false
    private var outOfBounds = false
    private var lastRawX = 0f
    private var lastRawY = 0f
    private var swipeTarget: PojavButton? = null

    init {
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        setPadding(4, 4, 4, 4)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        outlineProvider = null
        translationZ = 10f
    }

    override fun refresh() {
        val (w, h) = host.sizePx(data)
        val gd = GradientDrawable()
        gd.setColor(data.bgColor)
        gd.setStroke((data.strokeWidth * host.density).roundToInt(), data.strokeColor)
        gd.cornerRadius = (min(w, h) / 2f) * (data.cornerRadius / 100f)
        background = gd
        radiusPx = gd.cornerRadius
        alpha = data.opacity.coerceIn(0f, 1f)
        text = data.name
        overlayPaint.color =
            if (data.isToggle) Color.argb(128, 33, 150, 243) else Color.argb(60, 255, 255, 255)
        invalidate()
    }

    override fun place() {
        host.placeView(this, data, parentDrawer?.subPosition(this))
    }

    override fun releaseAll() {
        toggled = false
        sendKeyPresses(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (toggled || (!data.isToggle && pressedVisual)) {
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radiusPx, radiusPx, overlayPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (host.editMode) {
            val draggable = parentDrawer == null ||
                parentDrawer.drawerData.orientation == ControlDrawerData.Orientation.FREE
            return host.handleEditTouch(this, this, event, draggable)
        }
        return handleGameTouch(event)
    }

    protected open fun handleGameTouch(e: MotionEvent): Boolean {
        val pid = e.getPointerId(e.actionIndex)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                host.onPointerOccupied(pid)
                lastRawX = e.rawX
                lastRawY = e.rawY
                if (!data.isToggle) sendKeyPresses(true)
                setVisualPressed(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (data.passThruEnabled && CallbackBridge.isGrabbing()) {
                    CallbackBridge.sendCursorDelta(
                        (e.rawX - lastRawX) * host.cameraSensitivity,
                        (e.rawY - lastRawY) * host.cameraSensitivity
                    )
                }
                lastRawX = e.rawX
                lastRawY = e.rawY
                val out = e.x < 0 || e.x > width || e.y < 0 || e.y > height
                if (data.isSwipeable) {
                    val target = if (out) host.hitButton(e.rawX, e.rawY, this) else null
                    if (target !== swipeTarget) {
                        swipeTarget?.swipeRelease()
                        swipeTarget = target
                        target?.swipePress()
                    }
                    if (out && !outOfBounds) {
                        if (!data.isToggle) sendKeyPresses(false)
                    } else if (!out && outOfBounds) {
                        if (!data.isToggle) sendKeyPresses(true)
                    }
                }
                outOfBounds = out
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                host.onPointerReleased(pid)
                swipeTarget?.swipeRelease()
                swipeTarget = null
                val wasOut = outOfBounds
                outOfBounds = false
                if (wasOut || !triggerToggle()) sendKeyPresses(false)
                setVisualPressed(false)
            }
            else -> return false
        }
        return true
    }

    private fun setVisualPressed(p: Boolean) {
        pressedVisual = p
        invalidate()
    }

    fun swipePress() {
        if (!data.isToggle) sendKeyPresses(true)
    }

    fun swipeRelease() {
        if (!data.isToggle) sendKeyPresses(false)
    }

    /** @return true bila tombol bertipe toggle dan statusnya diubah */
    private fun triggerToggle(): Boolean {
        if (!data.isToggle) return false
        toggled = !toggled
        invalidate()
        sendKeyPresses(toggled)
        return true
    }

    fun sendKeyPresses(isDown: Boolean) {
        if (keysDown == isDown) return
        keysDown = isDown
        pressedVisual = isDown
        invalidate()
        for (k in data.keycodes) {
            when {
                k > 0 -> {
                    CallbackBridge.sendKeyPress(k, CallbackBridge.getCurrentMods(), isDown)
                    CallbackBridge.setModifiers(k, isDown)
                }
                k < 0 -> sendSpecial(k, isDown)
            }
        }
    }

    private fun sendSpecial(k: Int, isDown: Boolean) {
        when (k) {
            SPECIALBTN_KEYBOARD -> if (isDown) host.onSpecial(SPECIALBTN_KEYBOARD)
            SPECIALBTN_TOGGLECTRL -> if (isDown) host.toggleGui()
            SPECIALBTN_VIRTUALMOUSE -> if (isDown) host.onSpecial(SPECIALBTN_VIRTUALMOUSE)
            SPECIALBTN_MOUSEPRI -> CallbackBridge.sendMouseButton(0, isDown)
            SPECIALBTN_MOUSESEC -> CallbackBridge.sendMouseButton(1, isDown)
            SPECIALBTN_MOUSEMID -> CallbackBridge.sendMouseButton(2, isDown)
            SPECIALBTN_SCROLLDOWN -> if (!isDown) CallbackBridge.sendScroll(0.0, 1.0)
            SPECIALBTN_SCROLLUP -> if (!isDown) CallbackBridge.sendScroll(0.0, -1.0)
            SPECIALBTN_MENU -> if (isDown) host.onSpecial(SPECIALBTN_MENU)
        }
    }
}

/** Laci: tombol yang membuka/menutup sekelompok sub-tombol */
@SuppressLint("ViewConstructor")
class PojavDrawer(host: PojavControlLayout, val drawerData: ControlDrawerData) :
    PojavButton(host, drawerData.properties) {

    val subs = ArrayList<PojavButton>()

    var isOpen: Boolean
        get() = host.isDrawerOpen(drawerData)
        set(v) = host.setDrawerOpen(drawerData, v)

    fun createSubs() {
        subs.clear()
        drawerData.buttonProperties.forEach { subs.add(PojavButton(host, it, this)) }
    }

    fun placeSubs() {
        subs.forEach { it.place() }
    }

    override fun place() {
        super.place()
        placeSubs()
    }

    fun subPosition(sub: PojavButton): Pair<Float, Float>? {
        val o = drawerData.orientation
        if (o == ControlDrawerData.Orientation.FREE) return null
        val lp = layoutParams as? FrameLayout.LayoutParams ?: return null
        val i = subs.indexOf(sub) + 1
        val l = lp.leftMargin.toFloat()
        val t = lp.topMargin.toFloat()
        val w = lp.width.toFloat()
        val h = lp.height.toFloat()
        return when (o) {
            ControlDrawerData.Orientation.RIGHT -> Pair(l + i * w, t)
            ControlDrawerData.Orientation.LEFT -> Pair(l - i * w, t)
            ControlDrawerData.Orientation.UP -> Pair(l, t - i * h)
            ControlDrawerData.Orientation.DOWN -> Pair(l, t + i * h)
            ControlDrawerData.Orientation.FREE -> null
        }
    }

    override fun handleGameTouch(e: MotionEvent): Boolean {
        val pid = e.getPointerId(e.actionIndex)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                host.onPointerOccupied(pid)
                pressedVisual = true
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                host.onPointerReleased(pid)
                pressedVisual = false
                invalidate()
                isOpen = !isOpen
                placeSubs()
                host.updateVisibility()
            }
            MotionEvent.ACTION_CANCEL -> {
                host.onPointerReleased(pid)
                pressedVisual = false
                invalidate()
            }
            else -> return false
        }
        return true
    }
}

/** Joystick bergaya Pojav: 8 arah, dengan forward lock (sprint) */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class PojavJoystick(private val host: PojavControlLayout, val joystickData: ControlJoystickData) :
    View(host.context), PojavControl {
    override val data: ControlData get() = joystickData
    override val view: View get() = this

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var hx = 0f
    private var hy = 0f
    private val dirs = BooleanArray(4) // maju, kiri, mundur, kanan
    private var sprint = false
    private var locked = false

    init {
        translationZ = 10f
    }

    override fun refresh() {
        basePaint.color = data.bgColor
        strokePaint.color = if (data.strokeWidth > 0f) data.strokeColor else Color.argb(90, 255, 255, 255)
        strokePaint.strokeWidth = max(2f, data.strokeWidth * host.density)
        handlePaint.color = Color.argb(150, 255, 255, 255)
        alpha = data.opacity.coerceIn(0f, 1f)
        invalidate()
    }

    override fun place() {
        host.placeView(this, data, null)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f - strokePaint.strokeWidth
        canvas.drawCircle(cx, cy, r, basePaint)
        canvas.drawCircle(cx, cy, r, strokePaint)
        handlePaint.color = if (locked) Color.argb(200, 33, 150, 243) else Color.argb(150, 255, 255, 255)
        canvas.drawCircle(cx + hx, cy + hy, r * 0.4f, handlePaint)
    }

    private fun key(i: Int, down: Boolean) {
        val code = data.keycodes.getOrElse(i) { 0 }
        if (code > 0) CallbackBridge.sendKeyPress(code, CallbackBridge.getCurrentMods(), down)
    }

    private fun setDirs(f: Boolean, l: Boolean, b: Boolean, r: Boolean, sprintOn: Boolean) {
        val want = booleanArrayOf(f, l, b, r)
        for (i in 0..3) {
            if (dirs[i] != want[i]) {
                dirs[i] = want[i]
                key(i, want[i])
            }
        }
        if (sprint != sprintOn) {
            sprint = sprintOn
            CallbackBridge.sendKeyPress(341, CallbackBridge.getCurrentMods(), sprintOn)
            CallbackBridge.setModifiers(341, sprintOn)
        }
    }

    override fun releaseAll() {
        locked = false
        hx = 0f
        hy = 0f
        setDirs(false, false, false, false, false)
    }

    private fun update(e: MotionEvent) {
        val cx = width / 2f
        val cy = height / 2f
        var dx = e.x - cx
        var dy = e.y - cy
        val maxR = min(width, height) / 2f
        val dist = hypot(dx, dy)
        val s = (dist / maxR).coerceAtMost(1f)
        if (dist > maxR) {
            dx = dx / dist * maxR
            dy = dy / dist * maxR
        }
        hx = dx
        hy = dy
        if (s < 0.25f) {
            setDirs(false, false, false, false, false)
        } else {
            val ang = (Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble())) + 360.0) % 360.0
            val sector = (((ang + 22.5) % 360.0) / 45.0).toInt()
            val f = sector == 1 || sector == 2 || sector == 3
            val l = sector == 3 || sector == 4 || sector == 5
            val b = sector == 5 || sector == 6 || sector == 7
            val r = sector == 7 || sector == 0 || sector == 1
            setDirs(f, l, b, r, joystickData.forwardLock && sector == 2 && s >= 0.9f)
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (host.editMode) return host.handleEditTouch(this, this, event, true)
        val pid = event.getPointerId(event.actionIndex)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                host.onPointerOccupied(pid)
                if (locked) releaseAll()
                update(event)
            }
            MotionEvent.ACTION_MOVE -> update(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                host.onPointerReleased(pid)
                if (sprint && event.actionMasked != MotionEvent.ACTION_CANCEL) {
                    // forward lock: tetap maju + sprint sampai disentuh lagi
                    locked = true
                    hx = 0f
                    hy = -min(width, height) / 2f * 0.8f
                    invalidate()
                } else {
                    releaseAll()
                    invalidate()
                }
            }
            else -> return false
        }
        return true
    }
}
