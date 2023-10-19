package com.example.karsoftrivojyulduz.domain.repository

import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface SignInRepository {

    suspend fun signIn(body: SignInRequestData): Flow<Response<SignInResponseData>>
}