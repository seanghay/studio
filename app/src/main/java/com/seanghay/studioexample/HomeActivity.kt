package com.seanghay.studioexample

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.room.Room
import com.seanghay.studioexample.adapter.StoryListAdapter
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File


class HomeActivity : AppCompatActivity() {

    private val adapter: StoryListAdapter = StoryListAdapter()
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, "app-v1")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        setRecyclerViewConfigurations()

        buttonCreate.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonCreate.setOnLongClickListener {
            openFolder()
            false
        }

        buttonCreate.isEnabled = false

        adapter.onItemClicked = {
            play(it.path)
        }

        adapter.onDeleteClick = {
            appDatabase.storyDao().delete(it)
            File(it.path).deleteRecursively()
        }

        adapter.onSharedClick = { shareVideo(it.path) }

        if (ActivityCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                0
            )
        } else {
            buttonCreate.isEnabled = true
        }

    }

    private fun openFolder() {


    }

    override fun onResume() {
        super.onResume()

        appDatabase.storyDao().getAllFlowable().observe(this, Observer {
            adapter.patch(it.sortedByDescending { d -> d.createdAt })
        })
    }

    private fun shareVideo(filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

        ShareCompat.IntentBuilder.from(this)
            .setStream(uri)
            .setType("video/mp4")
            .setChooserTitle("Share video...")
            .startChooser()

    }

    private fun play(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        buttonCreate.isEnabled =
            requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }


    private fun setRecyclerViewConfigurations() {
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }
}