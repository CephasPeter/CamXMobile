package com.ai.camxmobile.database.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.mlkit.vision.label.ImageLabel
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ImageLabelConverter {
    var gson = Gson()

    @TypeConverter
    fun storedStringToDataList(value: String?): ArrayList<ImageLabel>? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<ArrayList<ImageLabel>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun dataStringToStoredString(data: ArrayList<ImageLabel>): String {
        return gson.toJson(data)
    }
}