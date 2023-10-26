package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import kotlinx.coroutines.flow.Flow

interface SubmitOrderImagesUseCase {

    suspend fun insertImage(submitImagesCacheData: SubmitImagesData)

    suspend fun getAllImages(orderId: Int): Flow<List<SubmitImagesData>>

    suspend fun deleteImage(submitImagesData: SubmitImagesData)

}