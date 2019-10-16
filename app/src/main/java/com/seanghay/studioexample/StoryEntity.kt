package com.seanghay.studioexample

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var title: String? = null,
    var path: String,
    var createdAt: Long
)