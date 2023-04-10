package com.example.moneyfypro.data

import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing data from local database
 */
class ExpensesRepositoryImp(private val expenseDao: ExpenseDao): ExpensesRepository {
    override suspend fun getAll(): List<Expense> {
        return expenseDao.getAll()
    }

    override suspend fun getFilteredExpenses(query: SupportSQLiteQuery): List<Expense> {
        return expenseDao.getFilteredExpenses(query)
    }


    override suspend fun getExpense(id: Int): Expense {
        return expenseDao.getExpense(id)
    }

    override suspend fun insertExpense(newExpense: Expense) {
        return expenseDao.insertExpense(newExpense)
    }

    override suspend fun updateExpense(expense: Expense) {
        return expenseDao.updateExpense(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        return expenseDao.deleteExpense(expense)
    }
}