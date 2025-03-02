package com.airprint.smart.printer.ads

import android.content.Context

object SharedPreferenceHelper {
       const  val BD = "Printer"
    private val LANGUAGES = "languages"
    private val LANGUAGESELECTED = "languageselected"
    fun setLanguages(context: Context, name: String) {

        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LANGUAGES, name)
        editor.apply()
    }

    fun getLanguages(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGES, "en") ?: "en"

    }


    fun setString(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(context: Context, key: String, defaultValue: String? = null): String? {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue)
    }


    fun getId(context: Context, key:String): String {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""

    }

    fun setId(context: Context, key:String, value: String) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }



    fun setInt(context: Context, key: String, value: Int) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }



    fun remove(context: Context, key: String) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun clear(context: Context) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun setIsVisitIntroScreen(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getIsVisitIntroScreen(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun setLanguage(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getLanguage(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val sharedPreferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun  setLong(context: Context, key: String, defValue: Long){
        val  preferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putLong(key,defValue)
        editor.apply()
    }

    fun getLong(context: Context, key: String): Long {
        val preferences = context.getSharedPreferences(BD, Context.MODE_PRIVATE)
        return preferences.getLong(key, 0)
    }
}