package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import kotlinx.coroutines.flow.Flow

interface SubmitOrderImagesUseCase {

    suspend fun insertImage(submitImagesCacheData: SubmitImagesData)

    suspend fun getImages(orderId: Int, statusId: Int): Flow<List<SubmitImagesData>>

    suspend fun deleteImage(submitImagesData: SubmitImagesData)

    suspend fun deleteImagesByOrderId(orderId: Int, statusId: Int)

    suspend fun updateImagesByStatusId(statusId: Int)

    suspend fun getImagesSize(): Flow<Int>
}