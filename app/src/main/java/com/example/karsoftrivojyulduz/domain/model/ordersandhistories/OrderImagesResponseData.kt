package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import com.google.gson.annotations.SerializedName

data class OrderImagesResponseData(
    val data: Data,
    val message: String,
    val success: Boolean
) {
    data class Data(
        @SerializedName("created_at")
        val createdAt: String,
        val description: String,
        val id: Int
    )
}