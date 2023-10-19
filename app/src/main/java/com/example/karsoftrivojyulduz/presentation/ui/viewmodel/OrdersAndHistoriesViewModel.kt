package com.example.karsoftrivojyulduz.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.karsoftrivojyulduz.data.repository.OrdersRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.domain.usecase.impl.OrdersUseCaseImpl
import com.example.karsoftrivojyulduz.util.JSONObjectConvertor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch

class OrdersAndHistoriesViewModel(private val ordersRepositoryImpl: OrdersRepositoryImpl) : ViewModel() {

    companion object {
        private const val TAG = "OrdersViewModel"
    }

    private val _successFlow = MutableSharedFlow<OrderAndHistoryResponseData>()
    val successFlow: Flow<OrderAndHistoryResponseData> get() = _successFlow

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> get() = _messageFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: Flow<String> get() = _errorFlow

    suspend fun getAllOrders(status: String) {
        val ordersUseCaseImpl = OrdersUseCaseImpl(ordersRepositoryImpl)

        ordersUseCaseImpl.execute(status)
            .catch {
                it.printStackTrace()
                _errorFlow.emit(it.message ?: "")
            }
            .collect {
                if (it.isSuccessful)
                    it.body()?.let { data -> _successFlow.emit(data) }
                else
                    _messageFlow.emit(JSONObjectConvertor().convertErrorObjectToMessage(it) ?: "")

            }
    }
}