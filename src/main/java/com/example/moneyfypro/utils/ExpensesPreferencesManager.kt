package com.example.moneyfypro.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.example.moneyfypro.R

/**
 * The base class of shared preferences manager for this application
 * Each preference has a class with a unique key
 */
abstract class ExpensesPreferencesManager<T>(protected val sharedPreferences: SharedPreferences) {

    companion object {
        const val KEY = "share"
    }

    abstract fun preferenceKey(): String

    fun editValue(value: T) {
        sharedPreferences.edit().apply {
            setValue(this, value)
            apply()
        }
    }

    protected abstract fun setValue(editor: SharedPreferences.Editor, value: T)

    abstract fun getValue(): T

    abstract fun defaultValue(): T

}

abstract class StringPreferencesManager(sharedPreferences: SharedPreferences): ExpensesPreferencesManager<String>(sharedPreferences) {
    override fun setValue(editor: SharedPreferences.Editor, value: String) {
        editor.putString(preferenceKey(), value)
    }

    override fun getValue(): String {
        return sharedPreferences.getString(preferenceKey(), defaultValue()) ?: defaultValue()
    }
}

abstract class IntPreferencesManager(sharedPreferences: SharedPreferences): ExpensesPreferencesManager<Int>(sharedPreferences) {
    override fun setValue(editor: SharedPreferences.Editor, value: Int) {
        editor.putInt(preferenceKey(), value)
    }

    override fun getValue(): Int {
        return sharedPreferences.getInt(preferenceKey(), defaultValue())
    }
}

class WidgetPeriodPreferenceManager(sharedPreferences: SharedPreferences): IntPreferencesManager(sharedPreferences) {

    enum class WidgetPeriod {
        DAY, WEEK, MONTH
    }

    override fun preferenceKey(): String {
        return "widget_period"
    }

    override fun defaultValue(): Int {
        return WidgetPeriod.MONTH.ordinal
    }

}



class CategoryPreferenceManager(private val context: Context, sharedPreferences: SharedPreferences): StringPreferencesManager(sharedPreferences) {
    override fun preferenceKey(): String {
        return "category_setting"
    }

    override fun defaultValue(): String {
        val separator = ","
        val defaultCatSet = context.resources.getStringArray(R.array.category_list).toSet()
        return defaultCatSet.joinToString(separator)
    }
}

class CurrencyPreferenceManager(sharedPreferences: SharedPreferences): StringPreferencesManager(sharedPreferences) {
    override fun preferenceKey(): String {
        return "currency_setting"
    }

    override fun defaultValue(): String {
        return "USD"
    }

}