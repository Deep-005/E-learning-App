package com.example.courses.Objects

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
    private var sharedPreferences: SharedPreferences? = null

    fun getInstance(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("User  Preferences", Context.MODE_PRIVATE)
        }
        return sharedPreferences!!
    }

    fun saveUserName(name: String, context: Context) {
        val editor = getInstance(context).edit()
        editor.putString("USER_NAME", name)
        editor.apply()
    }
}