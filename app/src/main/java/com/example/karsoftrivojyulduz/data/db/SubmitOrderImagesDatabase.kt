package com.example.karsoftrivojyulduz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.util.convertor.Convertors

@Database(entities = [SubmitImagesData::class], version = 1)
@TypeConverters(Convertors::class)
abstract class SubmitOrderImagesDatabase : RoomDatabase() {
    abstract fun getDao(): SubmitOrderImagesDao
}