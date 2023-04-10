package com.example.moneyfypro.model

import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.utils.ExpensesFilter

data class ExpensesViewState(
    val expensesList: List<Expense>,
    val expensesFilters: ExpensesFilter?,
)