package com.example.karsoftrivojyulduz.data.repository

import com.example.karsoftrivojyulduz.data.remote.ApiService
import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.repository.SignInRepository
import kotlinx.coroutines.flow.flow

class SignInRepositoryImpl(private val apiService: ApiService) : SignInRepository {
    override suspend fun signIn(body: SignInRequestData) = flow {
        val response = apiService.signIn(body)
        emit(response)
    }
}