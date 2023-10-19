package com.example.karsoftrivojyulduz.di

import com.example.karsoftrivojyulduz.presentation.ui.viewmodel.OrdersAndHistoriesViewModel
import com.example.karsoftrivojyulduz.presentation.ui.signin.viewmodel.SignInViewModel
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SimpleOrderViewModel
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        SignInViewModel(loginRepositoryImpl = get())
    }
    viewModel {
        OrdersAndHistoriesViewModel(ordersRepositoryImpl = get())
    }
    viewModel {
        SubmitOrderViewModel(orderImagesRepositoryImpl = get())
    }
    viewModel {
        SimpleOrderViewModel(simpleOrderRepositoryImpl = get())
    }
}