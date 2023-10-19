package com.example.karsoftrivojyulduz.di

import android.content.Context
import com.example.bookie.data.remote.interceptors.AccessTokenInterceptor
import com.example.bookie.data.remote.interceptors.CacheInterceptor
import com.example.karsoftrivojyulduz.data.remote.ApiService
import com.example.karsoftrivojyulduz.util.Constants
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

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
        val cacheInterceptor = CacheInterceptor()
        val cache = provideCache(context = get())

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(interceptor)
//            .addNetworkInterceptor(cacheInterceptor)
//            .cache(cache)
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

fun provideCache(context: Context): Cache {
    val httpCacheDirectory = File(context.cacheDir, "http-cache")
    val cacheSize: Long = 10 * 1024 * 1024 // 10 MB
    return Cache(httpCacheDirectory, cacheSize)
}