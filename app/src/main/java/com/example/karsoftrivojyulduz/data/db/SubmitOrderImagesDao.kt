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

    @Query("SELECT * FROM ${Constants.TABLE_NAME} WHERE orderId = :orderId AND statusId = :statusId")
    suspend fun getImages(orderId: Int, statusId: Int): List<SubmitImagesData>

    @Query("DELETE FROM ${Constants.TABLE_NAME} WHERE orderId = :orderId AND statusId = :statusId")
    suspend fun deleteImagesByOrderId(orderId: Int, statusId: Int)

    @Query("UPDATE ${Constants.TABLE_NAME} SET statusId = :statusId")
    suspend fun updateImagesByStatusId(statusId: Int)

    @Query("SELECT COUNT(*) FROM ${Constants.TABLE_NAME}")
    suspend fun getImagesSize(): Int
}