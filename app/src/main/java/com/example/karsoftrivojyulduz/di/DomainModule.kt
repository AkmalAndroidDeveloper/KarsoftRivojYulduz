package com.example.karsoftrivojyulduz.di

import com.example.karsoftrivojyulduz.domain.usecase.impl.OrderImagesUseCaseImpl
import com.example.karsoftrivojyulduz.domain.usecase.impl.OrdersUseCaseImpl
import com.example.karsoftrivojyulduz.domain.usecase.impl.SignInUseCaseImpl
import com.example.karsoftrivojyulduz.domain.usecase.impl.SimpleOrderUseCaseImpl
import com.example.karsoftrivojyulduz.domain.usecase.impl.SubmitOrderImagesUseCaseImpl
import org.koin.dsl.module

val domainModule = module {
    factory {
        SignInUseCaseImpl(loginRepository = get())
    }
    factory {
        OrdersUseCaseImpl(ordersRepository = get())
    }
    factory {
        OrderImagesUseCaseImpl(orderImagesRepository = get())
    }
    factory {
        SimpleOrderUseCaseImpl(simpleOrderRepository = get())
    }
    factory {
        SubmitOrderImagesUseCaseImpl(submitOrderImagesRepository = get())
    }
}