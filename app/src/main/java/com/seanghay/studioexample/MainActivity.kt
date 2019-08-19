package com.seanghay.studioexample

import android.Manifest
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.seanghay.studio.core.StudioView
import com.seanghay.studio.gles.annotation.GlContext
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri


class MainActivity : AppCompatActivity() {

    private lateinit var studioView: StudioView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar(Color.parseColor("#80FFFFFF"))
        setLightStatusBar(true)

        val progressDialog = ProgressDialog(this)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.isIndeterminate = false
        progressDialog.setTitle("Video Rendering")
        progressDialog.setMessage("Exporting....")
        progressDialog.setCancelable(false)


        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

        val files = File(Environment.getExternalStorageDirectory(), "studio")
        setContentView(R.layout.activity_main)

//        studioView = StudioView(this)
//        setContentView(studioView)
//        studioView.setFiles(files.walk().toList())



        val outputDir = File(files, "output")
        outputDir.mkdirs()

        val musicFile = File(files, "music.m4a")

        if (!musicFile.exists()) {
            throw RuntimeException("Music file doesn't exists")
        }

        val executor = Executors.newFixedThreadPool(10) as ThreadPoolExecutor
        val image = BitmapFactory.decodeResource(resources, R.drawable.image)

        executor.execute {

            val creator = VideoCreate(files.walk().toList(), outputDir, musicFile.path, {
                runOnUiThread { progressDialog.progress = it }
            }) {
                runOnUiThread {
                    progressDialog.dismiss()
                    play(it)
                }
            }

            creator.configure(1080, 1080 , 20_000_000)
            creator.startEncoding()
        }

        progressDialog.show()
        executor.shutdown()
    }

    private fun play(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)
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




