package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesCacheData
import kotlinx.coroutines.flow.Flow

interface SubmitOrderImagesUseCase {

    fun insertImage(submitImagesCacheData: SubmitImagesCacheData)

    fun getAllImages(orderId: Int): Flow<List<SubmitImagesCacheData>>

    fun deleteImagesById(orderId: Int)
}