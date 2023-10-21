package com.example.karsoftrivojyulduz.di

import com.example.bookie.data.remote.interceptors.AccessTokenInterceptor
import com.example.karsoftrivojyulduz.data.remote.ApiService
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        getApiService(retrofit = get())
    }
    single<Retrofit> {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val interceptor = AccessTokenInterceptor()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(interceptor)
            .build()

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .build()
    }
}

fun getApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}