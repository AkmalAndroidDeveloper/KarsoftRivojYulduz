package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.remote.ApiService
import com.example.karsoftrivojyulduz.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.flow

class OrdersRepositoryImpl(private val apiService: ApiService) : OrdersRepository {
    override suspend fun getAllOrders(status: String) = flow {
        val response = apiService.getAllOrders(status = status)
        emit(response)
    }
}