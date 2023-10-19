package com.example.karsoftrivojyulduz.util

import android.content.Context
import android.content.SharedPreferences
import com.example.karsoftrivojyulduz.app.App

class LocalStorage {
    companion object {
        val pref: SharedPreferences =
            App.instance.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
    }

    var token by StringPreference(pref)

    var isLogin by BooleanPreference(pref, false)
}
