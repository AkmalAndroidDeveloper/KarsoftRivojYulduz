package com.example.karsoftrivojyulduz.domain.usecase.impl

import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import com.example.karsoftrivojyulduz.domain.repository.SignInRepository
import com.example.karsoftrivojyulduz.domain.usecase.SignInUseCase
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class SignInUseCaseImpl(private val loginRepository: SignInRepository) : SignInUseCase {
    override suspend fun execute(body: SignInRequestData): Flow<Response<SignInResponseData>> {
        return loginRepository.signIn(body)
    }
}
