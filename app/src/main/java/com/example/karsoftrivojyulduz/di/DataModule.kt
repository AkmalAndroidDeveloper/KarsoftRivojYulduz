package com.example.karsoftrivojyulduz.di

import android.content.Context
import androidx.room.Room
import com.example.karsoftrivojyulduz.data.db.SubmitOrderImagesDao
import com.example.karsoftrivojyulduz.data.db.SubmitOrderImagesDatabase
import com.example.karsoftrivojyulduz.data.repository.OrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.OrdersRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SignInRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SimpleOrderRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SubmitOrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.util.constant.Constants
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
    single {
        SubmitOrderImagesRepositoryImpl(dao = get())
    }
    single {
        getDatabase(get())
    }
    single {
        provideSubmitOrderImagesDao(get())
    }
}

private fun getDatabase(context: Context): SubmitOrderImagesDatabase {
    return Room.databaseBuilder(
        context,
        SubmitOrderImagesDatabase::class.java,
        Constants.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()
}

private fun provideSubmitOrderImagesDao(db: SubmitOrderImagesDatabase): SubmitOrderImagesDao {
    return db.getDao()
}