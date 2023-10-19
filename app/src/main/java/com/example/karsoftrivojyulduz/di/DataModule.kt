package com.example.karsoftrivojyulduz.di

import com.example.karsoftrivojyulduz.data.repository.OrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.OrdersRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SignInRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SimpleOrderRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    single {
        SignInRepositoryImpl(apiService = get())
    }
    single {
        OrdersRepositoryImpl(apiService = get())
    }
    single {
        OrderImagesRepositoryImpl(apiService = get())
    }
    single {
        SimpleOrderRepositoryImpl(apiService = get())
    }
}