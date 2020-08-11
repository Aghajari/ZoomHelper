package com.aghajari.zoomhelper.simpleact

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.aghajari.zoomhelper.R
import com.aghajari.zoomhelper.ZoomHelper
import com.aghajari.zoomhelper.ZoomableActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SimpleActivity : ZoomableActivity(), ZoomHelper.OnZoomStateChangedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.layoutParams.height = resources.displayMetrics.widthPixels * 350 / 600

        //ZoomHelper.addZoomableView(imageView) //layout will make this
        ZoomHelper.addZoomableView(findViewById(R.id.textView))

        Glide.with(this)
            .load("https://picsum.photos/600/350")
            .apply(RequestOptions.centerCropTransform().placeholder(R.drawable.place_holder))
            .into(imageView)
    }

    override fun onZoomStateChanged(zoomHelper: ZoomHelper, zoomableView: View, isZooming: Boolean) {
        Toast.makeText(this,if (isZooming) "Zooming started" else "Zooming ended",Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        ZoomHelper.getInstance().addOnZoomStateChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        ZoomHelper.getInstance().removeOnZoomStateChangedListener(this)
    }
}