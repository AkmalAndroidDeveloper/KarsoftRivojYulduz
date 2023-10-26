package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.repository.SubmitOrderImagesRepository
import com.example.karsoftrivojyulduz.domain.usecase.SubmitOrderImagesUseCase
import kotlinx.coroutines.flow.flow

class SubmitOrderImagesUseCaseImpl(private val submitOrderImagesRepository: SubmitOrderImagesRepository) :
    SubmitOrderImagesUseCase {
    override suspend fun insertImage(submitImagesCacheData: SubmitImagesData) {
        submitOrderImagesRepository.insertImage(submitImagesCacheData)
    }

    override suspend fun getAllImages(orderId: Int) = flow {
        submitOrderImagesRepository.getAllImages(orderId).collect {
            emit(it)
        }
    }

    override suspend fun deleteImage(submitImagesData: SubmitImagesData) {
        submitOrderImagesRepository.deleteImage(submitImagesData)
    }
}