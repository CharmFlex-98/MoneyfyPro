package com.example.moneyfypro.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.moneyfypro.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


@Entity
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val description: String,
    val amount: Double,
    val date: Date
) {
    companion object {
        const val CATEGORY_KEY = "category"
        const val DESCRIPTION_KEY = "description"
        const val AMOUNT_KEY = "amount"
        const val DATE_KEY = "date"
        const val ID_KEY = "id"
    }
}

fun Expense.Companion.dateFormat(): SimpleDateFormat {
    val format = "dd/MM/yyyy"
    return SimpleDateFormat(format, Locale.US)
}

fun Expense.Companion.toAmountFormat(amount: Double, currencyCode: String, currencyFormat: Boolean = true): String {
    if (!currencyFormat) return String.format("%.2f", amount)

    var currCode = currencyCode
    if (currencyCode.isEmpty()) currCode = "USD"
    val currency = Currency.getInstance(currCode)
    val format = NumberFormat.getCurrencyInstance()
    format.currency = currency

    return format.format(amount)
}