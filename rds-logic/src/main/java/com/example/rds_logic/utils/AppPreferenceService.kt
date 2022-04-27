package com.example.rds_logic.utils

import android.content.Context
import android.content.SharedPreferences

object AppPreferenceService {

    private const val NAME_RESEARCH = "SEHRResearch"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferencesResearch: SharedPreferences
    //SharedPreferences variables
    private val IS_OPTIN = Pair("is_optin", false)
    fun init(context: Context) {
        preferencesResearch = context.getSharedPreferences(NAME_RESEARCH, MODE)
    }
    //an inline function to put variable and save it
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }
    var isOptinResearch: Boolean
        get() = preferencesResearch.getBoolean(IS_OPTIN.first, IS_OPTIN.second)
        set(value) = preferencesResearch.edit {
            it.putBoolean(IS_OPTIN.first, value)
        }

}