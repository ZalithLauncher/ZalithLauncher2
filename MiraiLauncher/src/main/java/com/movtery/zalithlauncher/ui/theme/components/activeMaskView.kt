package com.movtery.zalithlauncher.ui.theme.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.graphics.createBitmap
import kotlin.math.hypot

fun Context.activeMaskView(
    animTime: Long = 800,
    maskComplete: () -> Unit,
    maskAnimFinish: () -> Unit
) {
    val windows=(this as Activity).window
    val rootView = windows.decorView.rootView as ViewGroup
    captureView(rootView,windows) {
        val bitmap = it
        val maskView = MaskView(this, bitmap)
        rootView.addView(maskView)
        maskComplete()
        maskView.animActive(animTime) {
            rootView.removeView(maskView)
            maskAnimFinish()
        }
    }
}
private fun captureView(view: View, window: Window, bitmapCallback: (Bitmap)->Unit) {
    // Above Android O, use PixelCopy
    val bitmap = createBitmap(view.width, view.height)
    val location = IntArray(2)
    view.getLocationInWindow(location)
    PixelCopy.request(window,
        Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
        bitmap,
        {
            if (it == PixelCopy.SUCCESS) {
                bitmapCallback.invoke(bitmap)
            }
        },
        Handler(Looper.getMainLooper())
    )
}

@SuppressLint("ViewConstructor")
private class MaskView(
    context: Context,
    private var bitmap: Bitmap
) : View(context) {
    private var maskRadius = 0f
    private val paint = Paint(ANTI_ALIAS_FLAG)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) = with(canvas) {
        val layer = saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        drawBitmap(bitmap, 0f, 0f, null)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        paint.maskFilter = BlurMaskFilter(60f, BlurMaskFilter.Blur.NORMAL)
        drawCircle(0f, height.toFloat() / 2f, maskRadius, paint)
        paint.xfermode = null
        restoreToCount(layer)
    }

    fun animActive(animTime: Long, animFinish: () -> Unit) {
        ValueAnimator.ofFloat(
            0f,
            hypot(rootView.width.toFloat(), rootView.height.toFloat())
        ).apply {
            duration = animTime
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                maskRadius = valueAnimator.animatedValue as Float
                invalidate()
            }
            addListener(onEnd = { animFinish() })
        }.start()
    }
}