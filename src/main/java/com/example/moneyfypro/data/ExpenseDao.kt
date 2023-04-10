package com.example.moneyfypro.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense")
    suspend fun getAll(): List<Expense>

    @RawQuery
    suspend fun getFilteredExpenses(query: SupportSQLiteQuery): List<Expense>

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun getExpense(id: Int): Expense

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(newExpense: Expense)

}