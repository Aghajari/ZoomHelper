package com.aghajari.zoomhelper.recycler

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.zoomhelper.R
import com.aghajari.zoomhelper.ZoomHelper
import com.aghajari.zoomhelper.ZoomableActivity

class RecyclerViewActivity : ZoomableActivity() {

    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        val rv = findViewById<RecyclerView>(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(FakeData.fakeData(this))
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        ZoomHelper.getInstance().addOnZoomStateChangedListener(adapter)
    }

    override fun onPause() {
        super.onPause()
        ZoomHelper.getInstance().removeOnZoomStateChangedListener(adapter)
    }
}