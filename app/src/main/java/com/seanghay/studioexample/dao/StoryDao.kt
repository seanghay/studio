package com.seanghay.studioexample.dao

import androidx.room.*
import com.seanghay.studioexample.StoryEntity


@Dao
interface StoryDao {

    @Query("Delete  From StoryEntity")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg slide: StoryEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(vararg slide: StoryEntity)

    @Query("Select * From StoryEntity")
    fun getAll(): List<StoryEntity>

    @Delete
    fun delete(slide: StoryEntity)

    @Transaction
    fun upsert(vararg slide: StoryEntity) {
        for (StoryEntity in slide) {
            insert(StoryEntity)
            update(StoryEntity)
        }
    }

}