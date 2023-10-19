package com.example.karsoftrivojyulduz.domain.usecase

import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface SignInUseCase {

    suspend fun execute(body: SignInRequestData): Flow<Response<SignInResponseData>>
}