package com.example.bookie.data.remote.interceptors

import com.example.karsoftrivojyulduz.util.LocalStorage
import okhttp3.Interceptor

class AccessTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request().newBuilder()
            .addHeader(
                "Authorization",
                "Bearer ${LocalStorage().token}"
            ).build()
    )
}