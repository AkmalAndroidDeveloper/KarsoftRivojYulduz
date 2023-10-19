package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.domain.repository.OrdersRepository
import com.example.karsoftrivojyulduz.domain.usecase.OrdersUseCase
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class OrdersUseCaseImpl(private val ordersRepository: OrdersRepository) : OrdersUseCase {
    override suspend fun execute(status: String): Flow<Response<OrderAndHistoryResponseData>> {
        return ordersRepository.getAllOrders(status)
    }
}