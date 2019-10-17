package com.seanghay.studioexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
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

        adapter.onItemClicked = {
            play(it.path)
        }


        Editable.Factory.getInstance().newEditable("")


    }


    private fun play(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)


    }

    override fun onResume() {
        super.onResume()
        adapter.patch(appDatabase.storyDao().getAll().sortedByDescending { it.createdAt })
    }

    private fun setRecyclerViewConfigurations() {
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }
}