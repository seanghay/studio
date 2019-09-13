package com.seanghay.studioexample

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seanghay.studioexample.dao.AudioDao
import com.seanghay.studioexample.dao.SlideDao


@Database(version = 2, entities = [SlideEntity::class, AudioEntity::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun slideDao(): SlideDao
    abstract fun audioDao(): AudioDao
}