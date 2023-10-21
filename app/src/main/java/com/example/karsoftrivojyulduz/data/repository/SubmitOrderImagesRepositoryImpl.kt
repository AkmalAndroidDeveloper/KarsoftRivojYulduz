package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.db.SubmitOrderImagesDao
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.repository.SubmitOrderImagesRepository
import kotlinx.coroutines.flow.flow

class SubmitOrderImagesRepositoryImpl(private val dao: SubmitOrderImagesDao) :
    SubmitOrderImagesRepository {
    override fun insertImage(submitImagesCacheData: SubmitImagesCacheData) {
        dao.insertImage(submitImagesCacheData)
    }

    override fun getAllImages(orderId: Int) = flow {
        val response = dao.getAllImages(orderId)
        emit(response)
    }
}