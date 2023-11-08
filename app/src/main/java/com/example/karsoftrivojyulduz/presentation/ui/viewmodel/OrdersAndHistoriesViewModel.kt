package com.example.karsoftrivojyulduz.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.OrdersRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.domain.usecase.impl.OrdersUseCaseImpl
import com.example.karsoftrivojyulduz.util.convertor.JSONObjectConvertor
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class OrdersAndHistoriesViewModel(private val ordersRepositoryImpl: OrdersRepositoryImpl) :
    ViewModel() {

    override fun onCleared() {
        Log.d("AAAAAA", "Viewmodel cleared")
        super.onCleared()
    }

    private val _successFlow = MutableSharedFlow<OrderAndHistoryResponseData>()
    val successFlow: Flow<OrderAndHistoryResponseData> = _successFlow

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> = _messageFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: Flow<String> = _errorFlow

    suspend fun getAllOrders(status: String) {
        viewModelScope.launch {
            val ordersUseCaseImpl = OrdersUseCaseImpl(ordersRepositoryImpl)

            ordersUseCaseImpl.execute(status).catch {
                it.printStackTrace()
                _errorFlow.emit(it.message ?: "")
            }.collect {
                if (it.isSuccessful) it.body()?.let { data -> _successFlow.emit(data) }
                else _messageFlow.emit(JSONObjectConvertor().convertErrorObjectToMessage(it) ?: "")
            }
        }
    }
}