package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface OrdersUseCase {

    suspend fun execute(status: String): Flow<Response<OrderAndHistoryResponseData>>
}