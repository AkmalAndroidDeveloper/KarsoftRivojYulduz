package com.example.karsoftrivojyulduz.domain.model.submitorder

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.karsoftrivojyulduz.util.constant.Constants

@Entity(tableName = Constants.TABLE_NAME)
data class SubmitImagesCacheData(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val orderId: Int,
    val uri: String
)
