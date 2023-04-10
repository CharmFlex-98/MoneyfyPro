package com.example.moneyfypro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Expense::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class ExpensesDatabase: RoomDatabase() {

    abstract fun getDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "Expenses Database"
    }

}