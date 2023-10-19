package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

data class OrderImagesResponseData(
    val data: Data,
    val message: String,
    val success: Boolean
) {
    data class Data(
        val created_at: String,
        val description: String,
        val id: Int
    )
}