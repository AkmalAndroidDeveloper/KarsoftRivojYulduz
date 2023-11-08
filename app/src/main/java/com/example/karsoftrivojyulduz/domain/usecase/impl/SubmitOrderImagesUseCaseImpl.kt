package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.repository.SubmitOrderImagesRepository
import com.example.karsoftrivojyulduz.domain.usecase.SubmitOrderImagesUseCase
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

class SubmitOrderImagesUseCaseImpl(private val submitOrderImagesRepository: SubmitOrderImagesRepository) :
    SubmitOrderImagesUseCase {
    override suspend fun insertImage(submitImagesCacheData: SubmitImagesData) {
        submitOrderImagesRepository.insertImage(submitImagesCacheData)
    }

    override suspend fun getImages(orderId: Int, statusId: Int) = flow {
        submitOrderImagesRepository.getImages(orderId, statusId).collect {
            emit(it)
        }
    }

    override suspend fun deleteImage(submitImagesData: SubmitImagesData) {
        submitOrderImagesRepository.deleteImage(submitImagesData)
    }

    override suspend fun deleteImagesByOrderId(orderId: Int, statusId: Int) {
        submitOrderImagesRepository.deleteImagesByOrderId(orderId, statusId)
    }

    override suspend fun updateImagesByStatusId(statusId: Int) {
        submitOrderImagesRepository.updateImagesByStatusId(statusId)
    }

    override suspend fun getImagesSize() = channelFlow {
        submitOrderImagesRepository.getImagesSize().collect {
            send(it)
        }
    }
}