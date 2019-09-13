package com.seanghay.studioexample.dao

import androidx.room.*
import com.seanghay.studioexample.AudioEntity

@Dao
interface AudioDao {

    @Query("Select * From AudioEntity Limit 1")
    fun first(): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(audioEntity: AudioEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(audioEntity: AudioEntity)

    @Delete
    fun delete(audioEntity: AudioEntity)

    @Query("Delete From AudioEntity")
    fun deleteAll()

    @Transaction
    fun upsert(vararg audio: AudioEntity) {
        for (audioEntity in audio) {
            save(audioEntity)
            update(audioEntity)
        }
    }
}