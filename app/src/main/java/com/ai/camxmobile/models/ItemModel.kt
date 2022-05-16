package com.ai.camxmobile.models

import androidx.room.Entity
import com.google.mlkit.vision.label.ImageLabel

@Entity
data class ItemModel(var id: String){
    var uri:String? =null
    var name:String? = null
    var createdDate: Long? = null
    var imageLabel: ImageLabel? = null
}
