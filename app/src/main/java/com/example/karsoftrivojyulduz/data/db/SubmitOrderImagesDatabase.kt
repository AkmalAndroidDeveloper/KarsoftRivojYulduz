package com.example.karsoftrivojyulduz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.util.convertor.ConvertorBitmapToString

@Database(entities = [SubmitImagesCacheData::class], version = 1)
abstract class SubmitOrderImagesDatabase : RoomDatabase() {
    abstract fun getDao(): SubmitOrderImagesDao
}