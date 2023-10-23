package com.example.karsoftrivojyulduz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.util.constant.Constants

@Dao
interface SubmitOrderImagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImage(submitImagesCacheData: SubmitImagesCacheData)

    @Query("DELETE FROM ${Constants.TABLE_NAME} WHERE orderId = :orderId")
    fun deleteImagesById(orderId: Int)

    @Query("SELECT * FROM ${Constants.TABLE_NAME} WHERE orderId = :orderId")
    fun getAllImages(orderId: Int): List<SubmitImagesCacheData>
}