package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import com.example.karsoftrivojyulduz.domain.repository.SimpleOrderRepository
import com.example.karsoftrivojyulduz.domain.usecase.SimpleOrderUseCase
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class SimpleOrderUseCaseImpl(private val simpleOrderRepository: SimpleOrderRepository): SimpleOrderUseCase {
    override suspend fun execute(orderId: Int): Flow<Response<SimpleOrderResponseData>> {
        return simpleOrderRepository.getSimpleOrder(orderId)
    }
}