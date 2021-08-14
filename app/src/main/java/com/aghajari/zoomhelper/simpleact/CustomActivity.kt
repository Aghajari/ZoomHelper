package com.aghajari.zoomhelper.simpleact

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.aghajari.zoomhelper.R
import com.aghajari.zoomhelper.ZoomHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CustomActivity : AppCompatActivity() {

    val zoomHelper = ZoomHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        zoomHelper.shadowColor = Color.RED
        zoomHelper.maxScale = 2f
        zoomHelper.minScale = 1f
        zoomHelper.shadowAlphaFactory = 4f

        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.layoutParams.height = resources.displayMetrics.widthPixels * 350 / 600

        //ZoomHelper.addZoomableView(imageView)

        Glide.with(this)
            .load("https://picsum.photos/600/350")
            .apply(RequestOptions.centerCropTransform().placeholder(R.drawable.place_holder))
            .into(imageView)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return zoomHelper.dispatchTouchEvent(ev!!, this) || super.dispatchTouchEvent(ev)
    }


}