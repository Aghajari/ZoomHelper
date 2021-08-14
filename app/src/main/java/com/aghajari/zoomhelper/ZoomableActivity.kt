package com.aghajari.zoomhelper

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

open class ZoomableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZoomHelper.getInstance().minScale = 1f

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ZoomHelper.getInstance().dispatchTouchEvent(ev!!, this) || super.dispatchTouchEvent(ev)
    }

}