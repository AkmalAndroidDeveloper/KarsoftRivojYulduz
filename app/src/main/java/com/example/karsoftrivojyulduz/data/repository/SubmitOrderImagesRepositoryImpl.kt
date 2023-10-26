package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.db.SubmitOrderImagesDao
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.repository.SubmitOrderImagesRepository
import kotlinx.coroutines.flow.flow

class SubmitOrderImagesRepositoryImpl(private val dao: SubmitOrderImagesDao) :
    SubmitOrderImagesRepository {
    override suspend fun insertImage(submitImagesCacheData: SubmitImagesData) {
        dao.insertImage(submitImagesCacheData)
    }

    override suspend fun getAllImages(orderId: Int) = flow {
        val response = dao.getAllImages(orderId)
        emit(response)
    }

    override suspend fun deleteImage(submitImagesData: SubmitImagesData) {
        dao.deleteImage(submitImagesData)
    }
}