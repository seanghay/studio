package com.seanghay.studioexample.experiment.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StudioViewModel: ViewModel() {

    val photos = mutableListOf<Uri>()
    val compressedPhotos = mutableListOf<String>()
    val isLoading = MutableLiveData<Boolean>()

    init {
        isLoading.value = false
    }

}