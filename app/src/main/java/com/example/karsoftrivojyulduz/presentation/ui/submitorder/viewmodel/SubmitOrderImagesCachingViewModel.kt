package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.SubmitOrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.usecase.impl.SubmitOrderImagesUseCaseImpl
import com.example.karsoftrivojyulduz.util.constant.Constants
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

    suspend fun insertImage(submitImagesData: SubmitImagesData) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.insertImage(submitImagesData)
        }
    }

    suspend fun getAllImages(orderId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.getAllImages(orderId).collect {
                _successSubmitOrderImagesFlow.emit(it)
            }
        }
    }

    suspend fun deleteImage(submitImagesData: SubmitImagesData) {
        viewModelScope.launch(Dispatchers.IO) {
            val submitOrderImagesUseCaseImpl =
                SubmitOrderImagesUseCaseImpl(submitOrderImagesRepositoryImpl)
            submitOrderImagesUseCaseImpl.deleteImage(submitImagesData)
        }
    }
}