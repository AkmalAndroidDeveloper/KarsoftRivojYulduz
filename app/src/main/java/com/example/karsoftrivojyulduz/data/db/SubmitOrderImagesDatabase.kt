package com.example.karsoftrivojyulduz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData

@Database(entities = [SubmitImagesData::class], version = 2)
abstract class SubmitOrderImagesDatabase : RoomDatabase() {
    abstract fun getDao(): SubmitOrderImagesDao
}