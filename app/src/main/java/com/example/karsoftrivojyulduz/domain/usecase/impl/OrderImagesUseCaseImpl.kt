package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import com.example.karsoftrivojyulduz.domain.repository.OrderImagesRepository
import com.example.karsoftrivojyulduz.domain.usecase.OrderImagesUseCase
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class OrderImagesUseCaseImpl(private val orderImagesRepository: OrderImagesRepository) :
    OrderImagesUseCase {
    override suspend fun execute(orderImagesRequestData: OrderImagesRequestData): Flow<Response<OrderImagesResponseData>> {
        return orderImagesRepository.insertOrdersImages(orderImagesRequestData)
    }

}