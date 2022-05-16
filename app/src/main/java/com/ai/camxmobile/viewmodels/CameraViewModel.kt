package com.ai.camxmobile.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ai.camxmobile.database.dao.ItemDetailRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(private val itemDetailRepo: ItemDetailRepo): ViewModel(){
    var lensFacing = MutableLiveData<CameraSelector?>()
    var flashEnabled = MutableLiveData<Boolean?>()

    var capturedBitmap = MutableLiveData<Bitmap>()
    var capturedUri = MutableLiveData<Uri>()
}