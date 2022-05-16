package com.ai.camxmobile.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ai.camxmobile.database.typeconverters.ImageLabelConverter
import com.google.mlkit.vision.label.ImageLabel

@Entity
data class ItemModel(
    @PrimaryKey
    var id: String
){
    var uri:String? =null
    var name:String? = null
    var createdDate: Long? = null

    @TypeConverters(ImageLabelConverter::class)
    var imageLabel: ImageLabel? = null
}
