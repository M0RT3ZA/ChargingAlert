package ir.morteza_aghighi.chargingalert.tools

import android.content.Context
import android.content.SharedPreferences

private const val DEFAULT_PREFERENCES_NAME = "CA_SERVICE_KEY"

object SharedPrefs {

    fun setString(context: Context?, key: String?, value: String?) {
        val sharedPrefs = getPreferences(context!!)
        sharedPrefs.edit().let {
            it.putString(key, value)
            it.apply()
        }
    }

    fun getString(context: Context?, key: String?, defValue: String = "null"): String? {
        val sharedPrefs = getPreferences(context!!)
        return sharedPrefs.getString(key, defValue)
    }

    fun setBoolean(context: Context?, key: String?, value: Boolean) {
        val sharedPrefs = getPreferences(context!!)
        sharedPrefs.edit().let {
            it.putBoolean(key, value)
            it.apply()
        }
    }

    fun getBoolean(context: Context?, key: String?, defValue: Boolean = false): Boolean {
        val sharedPrefs = getPreferences(context!!)
        return sharedPrefs.getBoolean(key, defValue)
    }

    fun setInt(context: Context?, key: String?, value: Int) {
        val sharedPrefs = getPreferences(context!!)
        sharedPrefs.edit().let {
            it.putInt(key, value)
            it.apply()
        }
    }

    fun getInt(context: Context?, key: String?, defValue: Int = 0): Int {
        val sharedPrefs = getPreferences(context!!)
        return sharedPrefs.getInt(key, defValue)
    }

    fun setFloat(context: Context?, key: String?, value: Float) {
        val sharedPrefs = getPreferences(context!!)
        sharedPrefs.edit().let {
            it.putFloat(key, value)
            it.apply()
        }
    }

    fun getFloat(context: Context?, key: String?, defValue: Float = 0f): Float {
        val sharedPrefs = getPreferences(context!!)
        return sharedPrefs.getFloat(key, defValue)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(DEFAULT_PREFERENCES_NAME, 0)
    }
}