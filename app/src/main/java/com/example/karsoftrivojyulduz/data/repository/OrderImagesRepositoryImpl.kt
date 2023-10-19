package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.remote.ApiService
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import com.example.karsoftrivojyulduz.domain.repository.OrderImagesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import retrofit2.Response

class OrderImagesRepositoryImpl(private val apiService: ApiService) : OrderImagesRepository {
    override suspend fun insertOrdersImages(orderImagesRequestData: OrderImagesRequestData) = flow {
        val response = apiService.insertOrdersImages(
            orderImagesRequestData.order_id,
            orderImagesRequestData.description,
            orderImagesRequestData.images
        )
        emit(response)
    }
}