package com.example.karsoftrivojyulduz.util.convertor

import org.json.JSONObject
import retrofit2.Response

class JSONObjectConvertor {
    fun convertErrorObjectToMessage(response: Response<*>): String? {
        val errorObject = response.errorBody()?.charStream()?.readText()?.let { JSONObject(it) }
        return errorObject?.getString("message")
    }
}