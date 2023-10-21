package com.example.karsoftrivojyulduz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesData
import com.example.karsoftrivojyulduz.util.constant.Constants
import org.jetbrains.annotations.NotNull

@Dao
interface SubmitOrderImagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImage(submitImagesCacheData: SubmitImagesCacheData)

    @Query("SELECT * FROM ${Constants.TABLE_NAME} WHERE orderId = :orderId")
    fun getAllImages(orderId: Int): List<SubmitImagesCacheData>
}