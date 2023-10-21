package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.SubmitOrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.usecase.impl.SubmitOrderImagesUseCaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SubmitOrderImagesCachingViewModel(private val submitOrderImagesRepositoryImpl: SubmitOrderImagesRepositoryImpl) :
    ViewModel() {

    private val _successSubmitOrderImagesFlow = MutableSharedFlow<List<SubmitImagesCacheData>>()
    val successSubmitOrderImagesFlow: Flow<List<SubmitImagesCacheData>> get() = _successSubmitOrderImagesFlow

    fun insertImage(submitImagesCacheData: SubmitImagesCacheData) {
        val submitOrderImagesUseCaseImpl =
            SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
        viewModelScope.launch(Dispatchers.IO) {
            submitOrderImagesUseCaseImpl.insertImage(submitImagesCacheData)
        }
    }

    fun getAllImages(orderId: Int) {
        val submitOrderImagesUseCaseImpl =
            SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
        viewModelScope.launch(Dispatchers.IO) {
            submitOrderImagesUseCaseImpl.getAllImages(orderId).collect {
                _successSubmitOrderImagesFlow.emit(it)
            }
        }
    }
}