package com.example.karsoftrivojyulduz.domain.model.submitOrder

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.karsoftrivojyulduz.util.constant.Constants
import org.jetbrains.annotations.NotNull

@Entity(tableName = Constants.TABLE_NAME)
data class SubmitImagesCacheData(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val orderId: Int,
    val path: String,
    val image: String
)
