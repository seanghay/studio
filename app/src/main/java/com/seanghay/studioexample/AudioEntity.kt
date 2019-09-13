package com.seanghay.studioexample

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var path: String
)