package com.ai.camxmobile.viewmodels

import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel(){
    var lensFacing = MutableLiveData<CameraSelector?>()
    var flashEnabled = MutableLiveData<Boolean?>()
}