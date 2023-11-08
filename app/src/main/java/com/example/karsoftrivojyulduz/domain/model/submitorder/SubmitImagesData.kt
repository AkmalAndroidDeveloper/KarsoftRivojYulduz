package com.example.karsoftrivojyulduz.domain.model.submitorder

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.karsoftrivojyulduz.util.constant.Constants

@Entity(tableName = Constants.TABLE_NAME)
data class SubmitImagesData(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val statusId: Int,
    val orderId: Int,
    val uri: String,
    val path: String
)
