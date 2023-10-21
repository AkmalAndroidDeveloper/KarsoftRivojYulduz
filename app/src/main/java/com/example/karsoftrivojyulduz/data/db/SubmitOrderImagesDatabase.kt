package com.example.karsoftrivojyulduz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.util.convertor.ImageConvertor

@Database(entities = [SubmitImagesCacheData::class], version = 2)
@TypeConverters(ImageConvertor::class)
abstract class SubmitOrderImagesDatabase : RoomDatabase() {
    abstract fun getDao(): SubmitOrderImagesDao
}