package com.example.moneyfypro.ui.setting

import android.content.Context
import android.content.SharedPreferences
import com.example.moneyfypro.R

fun SharedPreferences.setToString(data: Set<String>): String {
    val separator = ","
    return data.joinToString(separator)
}

fun SharedPreferences.stringToSet(data: String): Set<String> {
    return data.split(",").toSet()
}

fun SharedPreferences.categoryId(): String {
    return "category_setting"
}

fun SharedPreferences.defaultCategories(context: Context): Set<String> {
    return context.resources.getStringArray(R.array.category_list).toSet()
}

fun SharedPreferences.currencyId(): String {
    return "currency_setting"
}

fun SharedPreferences.defaultCurrency(): String {
    return "USD"
}
