package com.example.moneyfypro.data

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.moneyfypro.utils.ExpensesFilter
import kotlinx.coroutines.flow.Flow

/**
 * Provide interface for concrete subclasses to use
 * The source can be either from local database or from API call
 */
interface ExpensesRepository {

    suspend fun getAll(): List<Expense>

    suspend fun getFilteredExpenses(query: SupportSQLiteQuery): List<Expense>

    suspend fun getExpense(id: Int): Expense

    suspend fun insertExpense(newExpense: Expense)

    suspend fun updateExpense(expense: Expense)

    suspend fun deleteExpense(expense: Expense)
}