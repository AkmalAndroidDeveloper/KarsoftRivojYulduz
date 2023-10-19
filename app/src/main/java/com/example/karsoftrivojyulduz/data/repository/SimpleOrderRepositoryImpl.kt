package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.remote.ApiService
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import com.example.karsoftrivojyulduz.domain.repository.SimpleOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class SimpleOrderRepositoryImpl(private val apiService: ApiService): SimpleOrderRepository {
    override suspend fun getSimpleOrder(orderId: Int) = flow {
        val response = apiService.getSimpleOrder(orderId = orderId)
        emit(response)
    }
}