package com.example.moneyfypro.utils


import android.content.Context
import android.content.SharedPreferences
import com.example.moneyfypro.data.ExpensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.absoluteValue

suspend fun getPeriodicTotalExpensesData(repository: ExpensesRepository, from: Date, to: Date): List<Double> {
    return withContext(Dispatchers.IO) {
        val expenses = repository.getFilteredExpenses(DateFilter(from, to).queryGenerator().query())

        if (expenses.isEmpty()) return@withContext listOf(0.0, 0.0)

        val res = mutableListOf<Double>()
        val profitAmount = expenses.filter { it.amount > 0 }.fold(0.0) {acc, item -> acc + item.amount}
        res.add(profitAmount)

        val expenseAmount = expenses.filter { it.amount < 0 }.fold(0.0) {acc, item -> acc + item.amount}.absoluteValue
        res.add(expenseAmount)

        return@withContext res
    }
}

fun expensesSharedPreferencesInstance(context: Context): SharedPreferences {
    return context.getSharedPreferences(ExpensesPreferencesManager.KEY, Context.MODE_PRIVATE)
}
