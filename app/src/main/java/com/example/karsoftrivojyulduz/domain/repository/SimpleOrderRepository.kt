package com.example.karsoftrivojyulduz.domain.repository

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface SimpleOrderRepository {

    suspend fun getSimpleOrder(orderId: Int): Flow<Response<SimpleOrderResponseData>>
}