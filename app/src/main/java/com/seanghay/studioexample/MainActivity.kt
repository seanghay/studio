package com.seanghay.studioexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.seanghay.studio.gles.StudioEngine
import com.seanghay.studio.gles.annotation.GlContext

class MainActivity : AppCompatActivity() {

    private val studio = StudioEngine()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        lifecycle.addObserver(studio)
    }


    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(studio)
    }
}


