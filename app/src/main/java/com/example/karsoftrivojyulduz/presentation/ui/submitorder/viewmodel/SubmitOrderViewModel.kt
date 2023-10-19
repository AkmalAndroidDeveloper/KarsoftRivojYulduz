package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.OrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import com.example.karsoftrivojyulduz.domain.usecase.impl.OrderImagesUseCaseImpl
import com.example.karsoftrivojyulduz.util.JSONObjectConvertor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.catch
import okhttp3.MultipartBody

class SubmitOrderViewModel(private val orderImagesRepositoryImpl: OrderImagesRepositoryImpl) :
    ViewModel() {

    override fun onCleared() {
        super.onCleared()
    }

    private val _successFlow = MutableSharedFlow<OrderImagesResponseData>()
    val successFlow: Flow<OrderImagesResponseData> get() = _successFlow

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> get() = _messageFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: Flow<String> get() = _errorFlow

    suspend fun insertOrderImages(
        orderImagesRequestData: OrderImagesRequestData
    ) {
        val orderImagesUseCaseImpl = OrderImagesUseCaseImpl(orderImagesRepositoryImpl)

        orderImagesUseCaseImpl.execute(orderImagesRequestData).catch {
            it.printStackTrace()
            _errorFlow.emit(it.localizedMessage ?: "")
        }.collect {
            if (it.isSuccessful) it.body()?.let { data -> _successFlow.emit(data) }
            else _messageFlow.emit(JSONObjectConvertor().convertErrorObjectToMessage(it) ?: "")
        }
    }
}