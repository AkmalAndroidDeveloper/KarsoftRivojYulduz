package com.example.karsoftrivojyulduz.domain.repository

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface OrdersRepository {

    suspend fun getAllOrders(status: String): Flow<Response<OrderAndHistoryResponseData>>
}