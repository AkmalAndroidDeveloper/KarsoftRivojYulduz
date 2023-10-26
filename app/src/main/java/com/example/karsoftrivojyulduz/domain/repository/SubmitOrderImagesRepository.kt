package com.example.karsoftrivojyulduz.domain.repository

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import kotlinx.coroutines.flow.Flow

interface SubmitOrderImagesRepository {

    suspend fun insertImage(submitImagesData: SubmitImagesData)

    suspend fun getAllImages(orderId: Int): Flow<List<SubmitImagesData>>

    suspend fun deleteImage(submitImagesData: SubmitImagesData)
}