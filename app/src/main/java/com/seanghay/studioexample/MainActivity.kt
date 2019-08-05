package com.seanghay.studioexample

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.seanghay.studio.core.StudioView
import com.seanghay.studio.gles.annotation.GlContext

class MainActivity : AppCompatActivity() {

    private lateinit var studioView: StudioView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar(Color.parseColor("#80FFFFFF"))
        setLightStatusBar(true)

        studioView = StudioView(this)
        setContentView(studioView)
    }

    fun setupStatusBar(color: Int) {
        window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.statusBarColor = color
    }

    open fun setLightStatusBar(light: Boolean = true) {
        if (light) {
            window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        }
    }
}


