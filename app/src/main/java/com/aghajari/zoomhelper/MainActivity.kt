package com.aghajari.zoomhelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.aghajari.zoomhelper.recycler.RecyclerViewActivity
import com.aghajari.zoomhelper.simpleact.SimpleActivity
import com.aghajari.zoomhelper.simpleact.CustomActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onSimpleActivityClick(v:View){
        startActivity(Intent(this, SimpleActivity::class.java))
    }

    fun onCustomActivityClick(v:View){
        startActivity(Intent(this, CustomActivity::class.java))
    }

    fun onRecyclerViewActivityClick(v:View){
        startActivity(Intent(this, RecyclerViewActivity::class.java))
    }
}
