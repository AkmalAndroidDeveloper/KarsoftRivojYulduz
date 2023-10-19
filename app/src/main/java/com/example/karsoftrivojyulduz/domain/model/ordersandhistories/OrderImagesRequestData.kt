package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part

data class OrderImagesRequestData(
    @Part("order_id") val order_id: RequestBody,
    @Part("description") val description: RequestBody,
    @Part val images: List<MultipartBody.Part>,
)
