package com.ai.camxmobile.database.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.mlkit.vision.label.ImageLabel
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ImageLabelConverter {
    @OptIn(ExperimentalSerializationApi::class)
    @TypeConverter
    fun fromList(value : ArrayList<ImageLabel>) = Json.encodeToString(value)

    @OptIn(ExperimentalSerializationApi::class)
    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<ArrayList<ImageLabel>>(value)
}