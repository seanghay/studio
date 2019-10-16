package com.seanghay.studioexample

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seanghay.studioexample.dao.AudioDao
import com.seanghay.studioexample.dao.SlideDao
import com.seanghay.studioexample.dao.StoryDao


@Database(version = 3, entities = [SlideEntity::class, AudioEntity::class, StoryEntity::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun slideDao(): SlideDao
    abstract fun audioDao(): AudioDao
    abstract fun storyDao(): StoryDao
}