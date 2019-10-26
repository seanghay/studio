package com.seanghay.studioexample

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.seanghay.studioexample.adapter.StoryListAdapter
import kotlinx.android.synthetic.main.activity_home.*

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

        buttonCreate.isEnabled = false

        adapter.onItemClicked = {
            play(it.path)
        }

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
            adapter.patch(appDatabase.storyDao().getAll().sortedByDescending { it.createdAt })
        }
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
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            buttonCreate.isEnabled = true
            adapter.patch(appDatabase.storyDao().getAll().sortedByDescending { it.createdAt })
        } else {
            buttonCreate.isEnabled = false
        }
    }


    private fun setRecyclerViewConfigurations() {
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }
}