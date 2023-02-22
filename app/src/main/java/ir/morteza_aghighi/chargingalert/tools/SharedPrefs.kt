package ir.morteza_aghighi.chargingalert.tools

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager

object SharedPrefs {
    @JvmStatic
    @SuppressLint("ApplySharedPref")
    fun setString(key: String?, value: String?, context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.commit()
    }

    @JvmStatic
    fun getString(key: String?, context: Context?): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, "null")
    }

    @JvmStatic
    @SuppressLint("ApplySharedPref")
    fun setBoolean(key: String?, value: Boolean, context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    @JvmStatic
    fun getBoolean(key: String?, context: Context?): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(key, false)
    }

    @JvmStatic
    @SuppressLint("ApplySharedPref")
    fun setInt(key: String?, value: Int, context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    @JvmStatic
    fun getInt(key: String?, context: Context?): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, 0)
    }

    @SuppressLint("ApplySharedPref")
    fun setFloat(key: String?, value: Float, context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.commit()
    }

    fun getFloat(key: String?, context: Context?): Float {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getFloat(key, 0f)
    }
}