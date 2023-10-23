package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.repository.SubmitOrderImagesRepository
import com.example.karsoftrivojyulduz.domain.usecase.SubmitOrderImagesUseCase
import kotlinx.coroutines.flow.flow

class SubmitOrderImagesUseCaseImpl(private val submitOrderImagesRepository: SubmitOrderImagesRepository) :
    SubmitOrderImagesUseCase {
    override fun insertImage(submitImagesCacheData: SubmitImagesCacheData) {
        submitOrderImagesRepository.insertImage(submitImagesCacheData)
    }

    override fun getAllImages(orderId: Int) = flow {
        submitOrderImagesRepository.getAllImages(orderId).collect {
            emit(it)
        }
    }

    override fun deleteImagesById(orderId: Int) {
        submitOrderImagesRepository.deleteImagesById(orderId)
    }
}