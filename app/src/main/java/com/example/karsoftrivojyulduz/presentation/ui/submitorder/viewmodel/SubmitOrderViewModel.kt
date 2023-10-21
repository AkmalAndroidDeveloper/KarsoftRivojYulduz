package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.OrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SimpleOrderRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.usecase.SubmitOrderImagesUseCase
import com.example.karsoftrivojyulduz.domain.usecase.impl.OrderImagesUseCaseImpl
import com.example.karsoftrivojyulduz.domain.usecase.impl.SimpleOrderUseCaseImpl
import com.example.karsoftrivojyulduz.util.convertor.JSONObjectConvertor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SubmitOrderViewModel(
    private val orderImagesRepositoryImpl: OrderImagesRepositoryImpl,
    private val simpleOrderRepositoryImpl: SimpleOrderRepositoryImpl
) :
    ViewModel() {

    // insert order images
    private val _successOrderImagesFlow = MutableSharedFlow<OrderImagesResponseData>()
    val successOrderImagesFlow: Flow<OrderImagesResponseData> get() = _successOrderImagesFlow

    private val _messageOrderImagesFlow = MutableSharedFlow<String>()
    val messageOrderImagesFlow: Flow<String> get() = _messageOrderImagesFlow

    private val _errorOrderImagesFlow = MutableSharedFlow<String>()
    val errorOrderImagesFlow: Flow<String> get() = _errorOrderImagesFlow


    // get simple order
    private val _successSimpleOrderFlow = MutableSharedFlow<SimpleOrderResponseData>()
    val successSimpleOrderFlow: Flow<SimpleOrderResponseData> get() = _successSimpleOrderFlow

    private val _messageSimpleOrderFlow = MutableSharedFlow<String>()
    val messageSimpleOrderFlow: Flow<String> get() = _messageSimpleOrderFlow

    private val _errorSimpleOrderFlow = MutableSharedFlow<String>()
    val errorSimpleOrderFlow: Flow<String> get() = _errorSimpleOrderFlow

    suspend fun insertOrderImages(
        orderImagesRequestData: OrderImagesRequestData
    ) {
        val orderImagesUseCaseImpl = OrderImagesUseCaseImpl(orderImagesRepositoryImpl)

        orderImagesUseCaseImpl.execute(orderImagesRequestData).catch {
            it.printStackTrace()
            _errorOrderImagesFlow.emit(it.localizedMessage ?: "")
        }.collect {
            if (it.isSuccessful) it.body()?.let { data -> _successOrderImagesFlow.emit(data) }
            else _messageOrderImagesFlow.emit(
                JSONObjectConvertor().convertErrorObjectToMessage(it) ?: ""
            )
        }
    }

    suspend fun getSimpleOrder(orderId: Int) {
        val simpleOrderUseCaseImpl = SimpleOrderUseCaseImpl(simpleOrderRepositoryImpl)

        simpleOrderUseCaseImpl.execute(orderId)
            .catch {
                it.printStackTrace()
                _errorSimpleOrderFlow.emit(it.localizedMessage ?: "")
            }
            .collect {
                if (it.isSuccessful)
                    it.body()?.let { data -> _successSimpleOrderFlow.emit(data) }
                else {
                    _messageSimpleOrderFlow.emit(
                        JSONObjectConvertor().convertErrorObjectToMessage(
                            it
                        ) ?: ""
                    )
                }
            }
    }
}
