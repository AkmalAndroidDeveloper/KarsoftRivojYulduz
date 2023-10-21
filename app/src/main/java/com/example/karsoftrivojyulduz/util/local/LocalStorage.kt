package com.example.karsoftrivojyulduz.util.local

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

    var fromOrdersFragment by BooleanPreference(pref, false)
}
