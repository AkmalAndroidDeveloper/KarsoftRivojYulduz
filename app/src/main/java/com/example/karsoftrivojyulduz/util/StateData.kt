package com.example.karsoftrivojyulduz.util

sealed class StateData<out T>{

    data class Success<T>(val data:T): StateData<T>()

    data class Message<T>(val message:String): StateData<T>()

    data class Error<T>(val error:Throwable): StateData<T>()

}
