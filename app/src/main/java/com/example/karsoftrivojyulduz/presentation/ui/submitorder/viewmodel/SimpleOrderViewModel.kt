package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import androidx.lifecycle.ViewModel
import com.example.karsoftrivojyulduz.data.repository.SimpleOrderRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import com.example.karsoftrivojyulduz.domain.usecase.impl.SimpleOrderUseCaseImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch

class SimpleOrderViewModel(private val simpleOrderRepositoryImpl: SimpleOrderRepositoryImpl) :
    ViewModel() {

    private val _successFlow = MutableSharedFlow<SimpleOrderResponseData>()
    val successFlow: Flow<SimpleOrderResponseData> get() = _successFlow

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> get() = _messageFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: Flow<String> get() = _errorFlow

    suspend fun getSimpleOrder(orderId: Int) {
        val simpleOrderUseCaseImpl = SimpleOrderUseCaseImpl(simpleOrderRepositoryImpl)

        simpleOrderUseCaseImpl.execute(orderId)
            .catch {
                it.printStackTrace()
                _errorFlow.emit(it.localizedMessage ?: "")
            }
            .collect {
                if (it.isSuccessful)
                    it.body()?.let { data -> _successFlow.emit(data) }
                else {
//                    _messageFlow.emit(JSONObjectConvertor().convertErrorObjectToMessage(it) ?: "")
                    _messageFlow.emit(it.body()?.message ?: "")
                }
            }
    }
}