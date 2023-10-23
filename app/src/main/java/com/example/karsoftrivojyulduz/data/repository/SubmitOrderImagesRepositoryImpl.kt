package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.db.SubmitOrderImagesDao
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesCacheData
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

    override fun deleteImagesById(orderId: Int) {
        dao.deleteImagesById(orderId)
    }
}