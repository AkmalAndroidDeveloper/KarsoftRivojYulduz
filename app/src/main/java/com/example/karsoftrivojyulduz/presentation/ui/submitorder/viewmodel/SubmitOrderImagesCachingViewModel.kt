package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.SubmitOrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.usecase.impl.SubmitOrderImagesUseCaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubmitOrderImagesCachingViewModel(private val submitOrderImagesRepositoryImpl: SubmitOrderImagesRepositoryImpl) :
    ViewModel() {

    private val _successSubmitOrderImagesFlow = MutableStateFlow<List<SubmitImagesData>>(listOf())
    val successSubmitOrderImagesFlow: StateFlow<List<SubmitImagesData>> get() = _successSubmitOrderImagesFlow

    private val _successImageSize = MutableSharedFlow<Int>()
    val successImageSize: Flow<Int> get() = _successImageSize

    suspend fun getImages(orderId: Int, statusId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.getImages(orderId, statusId).collect {
                _successSubmitOrderImagesFlow.emit(it)
            }
        }
    }

    suspend fun insertImage(submitImagesData: SubmitImagesData) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.insertImage(submitImagesData)
        }
    }

    suspend fun deleteImage(submitImagesData: SubmitImagesData) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.deleteImage(submitImagesData)
        }
    }

    suspend fun deleteImagesByOrderId(orderId: Int, statusId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.deleteImagesByOrderId(orderId, statusId)
        }
    }

    suspend fun updateImagesByStatusId(statusId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.updateImagesByStatusId(statusId)
        }
    }

    suspend fun getImagesSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.getImagesSize().collect {
                _successImageSize.emit(it)
            }
        }
    }
}