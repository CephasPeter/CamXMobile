package com.ai.camxmobile.database.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.mlkit.vision.label.ImageLabel
import com.google.gson.reflect.TypeToken

class ImageLabelConverter {
    var gson = Gson()

    @TypeConverter
    fun storedStringToDataList(value: String?): ImageLabel? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<ImageLabel?>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun dataStringToStoredString(data: ImageLabel?): String {
        return gson.toJson(data)
    }

}