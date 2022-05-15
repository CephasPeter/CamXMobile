package com.ai.camxmobile.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel(){
    var lensFacing = MutableLiveData<CameraSelector?>()
    var flashEnabled = MutableLiveData<Boolean?>()

    var capturedBitmap = MutableLiveData<Bitmap>()
    var capturedUri = MutableLiveData<Uri>()
}