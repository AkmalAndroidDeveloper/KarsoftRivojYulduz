package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import com.google.gson.annotations.SerializedName

data class Detail(
    @SerializedName("created_at")
    val createdAt: String,
    val description: String,
    val id: Int
)