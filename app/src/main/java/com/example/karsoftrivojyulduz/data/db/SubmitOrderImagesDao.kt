package com.example.karsoftrivojyulduz.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.util.constant.Constants

@Dao
interface SubmitOrderImagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(submitImagesData: SubmitImagesData): Long

    @Delete
    suspend fun deleteImage(submitImagesData: SubmitImagesData)

    @Query("SELECT * FROM ${Constants.TABLE_NAME} WHERE orderId = :orderId")
    suspend fun getAllImages(orderId: Int): List<SubmitImagesData>
}