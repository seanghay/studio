package com.seanghay.studioexample.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.seanghay.studioexample.StoryEntity
import io.reactivex.Flowable


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

    @Query("Select * From StoryEntity")
    fun getAllFlowable(): LiveData<List<StoryEntity>>

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