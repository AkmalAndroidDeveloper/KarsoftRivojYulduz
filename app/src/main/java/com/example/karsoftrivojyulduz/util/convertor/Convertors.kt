package com.example.karsoftrivojyulduz.util.convertor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.TypeConverter
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.util.Collections

class Convertors {

    @TypeConverter
    fun bitmapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    @TypeConverter
    fun stringToBitmap(encodedString: String): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            bitmap

        } catch (e: Exception) {
            e.message
            null
        }
    }

    @TypeConverter
    fun listOfImagesFromCameraAndGalleryToString(listOfImages: List<SubmitImagesData>): String {
        return Gson().toJson(listOfImages).toString()
    }

    @TypeConverter
    fun stringToListOfImagesFromCameraAndGallery(value: String?): List<SubmitImagesData> {
        if (value == null) return Collections.emptyList()
        val listOfBookFromCategoryData = object : TypeToken<MutableList<SubmitImagesData>>() {}.type
        return Gson().fromJson(value, listOfBookFromCategoryData)
    }
}