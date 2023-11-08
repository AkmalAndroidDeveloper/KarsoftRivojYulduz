package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import com.google.gson.annotations.SerializedName

data class Service(
    val description: String,
    val dimension: Dimension,
    val each: Int,
    @SerializedName("is_discount")
    val isDiscount: Boolean,
    @SerializedName("is_public")
    val isPublic: Boolean,
    val price: Int,
    @SerializedName("price_each")
    val priceEach: Int,
    val slug: String,
    val title: String
)