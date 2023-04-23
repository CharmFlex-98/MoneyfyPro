package com.example.moneyfypro.widget

import android.content.SharedPreferences

enum class WidgetDataPeriod {
    DAY, WEEK, MONTH
}
fun SharedPreferences.getWidgetDataPeriod() {
    getInt(widgetPeriodKey(), defaultWidgetDataPeriod().ordinal)
}

fun SharedPreferences.setWidgetDataPeriod(widgetDataPeriod: WidgetDataPeriod) {
    edit().apply {
        putInt(widgetPeriodKey(), widgetDataPeriod.ordinal)
        apply()
    }
}

private fun defaultWidgetDataPeriod(): WidgetDataPeriod {
    return WidgetDataPeriod.MONTH
}

private fun widgetPeriodKey(): String {
    return "widget_period"
}