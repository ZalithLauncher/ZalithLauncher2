package dev.horizon.android

import android.app.NativeActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.Keep

/** NativeActivity entry point for the Starling runtime embedded in Voxium. */
@Keep
class HorizonActivity : NativeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
