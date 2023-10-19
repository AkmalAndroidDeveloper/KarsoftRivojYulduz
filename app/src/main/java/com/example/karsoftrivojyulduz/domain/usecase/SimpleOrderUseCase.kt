package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface SimpleOrderUseCase {
    suspend fun execute(orderId: Int): Flow<Response<SimpleOrderResponseData>>
}