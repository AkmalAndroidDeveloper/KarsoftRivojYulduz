package com.example.karsoftrivojyulduz.presentation.ui.signin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.karsoftrivojyulduz.data.repository.SignInRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import com.example.karsoftrivojyulduz.domain.usecase.impl.SignInUseCaseImpl
import com.example.karsoftrivojyulduz.util.convertor.JSONObjectConvertor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch

class SignInViewModel(private val loginRepositoryImpl: SignInRepositoryImpl) : ViewModel() {

    private val _successFlow = MutableSharedFlow<SignInResponseData>()
    val successFlow: Flow<SignInResponseData> get() = _successFlow

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> get() = _messageFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: Flow<String> get() = _errorFlow

    suspend fun signIn(body: SignInRequestData) {
        val loginUseCaseImpl = SignInUseCaseImpl(loginRepositoryImpl)

        loginUseCaseImpl.execute(body)
            .catch {
                _errorFlow.emit(it.localizedMessage ?: "")
            }
            .collect {
                if (it.isSuccessful) it.body()?.let { data -> _successFlow.emit(data) }
                else _messageFlow.emit(JSONObjectConvertor().convertErrorObjectToMessage(it) ?: "")
            }
    }
}