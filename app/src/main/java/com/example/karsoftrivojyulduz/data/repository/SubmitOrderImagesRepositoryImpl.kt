package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.db.SubmitOrderImagesDao
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.repository.SubmitOrderImagesRepository
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

class SubmitOrderImagesRepositoryImpl(private val dao: SubmitOrderImagesDao) :
    SubmitOrderImagesRepository {
    override suspend fun insertImage(submitImagesData: SubmitImagesData) {
        dao.insertImage(submitImagesData)
    }

    override suspend fun getImages(orderId: Int, statusId: Int) = flow {
        val response = dao.getImages(orderId, statusId)
        emit(response)
    }

    override suspend fun deleteImage(submitImagesData: SubmitImagesData) {
        dao.deleteImage(submitImagesData)
    }

    override suspend fun deleteImagesByOrderId(orderId: Int, statusId: Int) {
        dao.deleteImagesByOrderId(orderId, statusId)
    }

    override suspend fun updateImagesByStatusId(statusId: Int) {
        dao.updateImagesByStatusId(statusId)
    }

    override suspend fun getImagesSize() = channelFlow {
        send(dao.getImagesSize())
    }
}