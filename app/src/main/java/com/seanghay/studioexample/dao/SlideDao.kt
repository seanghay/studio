package com.seanghay.studioexample.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*
import com.seanghay.studioexample.SlideEntity

@Dao
interface SlideDao {

    @Query("Delete  From SlideEntity")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg slide: SlideEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(vararg slide: SlideEntity)

    @Query("Select * From SlideEntity")
    fun getAll(): List<SlideEntity>

    @Delete
    fun delete(slide: SlideEntity)

    @Transaction
    fun upsert(vararg slide: SlideEntity) {
        for (slideEntity in slide) {
            insert(slideEntity)
            update(slideEntity)
        }
    }
}