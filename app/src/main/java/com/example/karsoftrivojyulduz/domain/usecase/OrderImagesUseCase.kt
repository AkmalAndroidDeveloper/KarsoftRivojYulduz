package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.Response

interface OrderImagesUseCase {

    suspend fun execute(
        orderImagesRequestData: OrderImagesRequestData
    ): Flow<Response<OrderImagesResponseData>>
}