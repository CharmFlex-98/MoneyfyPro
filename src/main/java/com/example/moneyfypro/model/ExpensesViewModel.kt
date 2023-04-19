package com.example.moneyfypro.model

import androidx.lifecycle.*
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesRepository
import com.example.moneyfypro.utils.ExpensesFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Hilt dagger will auto inject the repository object needed.
 */
@HiltViewModel
class ExpensesViewModel @Inject constructor(private val repository: ExpensesRepository) :
    ViewModel() {
    val expensesViewState = MutableLiveData<ExpensesViewState>()

   init {
       initExpensesView()
   }


    fun insertExpense(category: String, description: String, amount: Double, date: Date) {
        viewModelScope.launch {
            val newExpense = getNewExpenseEntry(category, description, amount, date)
            repository.insertExpense(newExpense)
            expensesViewState.value = expensesViewState.value?.copy(
                expensesList = repository.getFilteredExpenses(expensesViewState.value?.expensesFilters!!.queryGenerator().query()).sortedByDescending { it.date }
            )
        }
    }

    fun updateExpense(id: String, category: String, description: String, amount: Double, date: Date) {
        viewModelScope.launch {
            val updatedExpense = getUpdatedExpenseEntry(id, category, description, amount, date)
            repository.updateExpense(updatedExpense)
            expensesViewState.value = expensesViewState.value?.copy(
                expensesList = repository.getFilteredExpenses(expensesViewState.value?.expensesFilters!!.queryGenerator().query()).sortedByDescending { it.date }
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            expensesViewState.value = expensesViewState.value?.copy(
                expensesList = repository.getFilteredExpenses(expensesViewState.value?.expensesFilters!!.queryGenerator().query()).sortedByDescending { it.date }
            )
        }
    }

    private fun getNewExpenseEntry(
        category: String,
        description: String,
        amount: Double,
        date: Date
    ): Expense {
        return Expense(
            Date().time.toString(),
            category = category,
            description = description,
            amount = amount,
            date = date
        )
    }

    private fun getUpdatedExpenseEntry(
        id: String,
        category: String,
        description: String,
        amount: Double,
        date: Date
    ): Expense {
        return Expense(
            id = id,
            category = category,
            description = description,
            amount = amount,
            date = date
        )
    }

    fun hasExpenses(): Boolean = expensesCount() > 0

    fun hasEarnings(): Boolean = earningCount() > 0

    fun hasRecord(): Boolean = hasExpenses() || hasEarnings()

    private fun expensesCount(): Int {
        return expensesViewState.value?.expensesList?.filter {
            it.amount < 0
        }?.size ?: 0
    }

    private fun earningCount(): Int {
        return expensesViewState.value?.expensesList?.filter {
            it.amount >= 0
        }?.size ?: 0
    }

    /**
     * Update expenses list
     */
    private fun initExpensesView() {
        viewModelScope.launch {
            expensesViewState.value = ExpensesViewState(
                expensesList = repository.getAll(),
                expensesFilters = null
            )
        }
    }

    /**
     * Set filter
     */
    fun setExpensesFilter(filter: ExpensesFilter) {
        viewModelScope.launch {
            expensesViewState.value = ExpensesViewState(
                expensesFilters = filter,
                expensesList = repository.getFilteredExpenses(filter.queryGenerator().query()).sortedByDescending { it.date }
            )
        }
    }

    /**
     * Get every expenses percentage portion by category
     */
    fun getExpensesRatioByCategory(): Map<String, Double> {
        val res = mutableMapOf<String, Double>()
        val filteredExpenses = expensesViewState.value?.expensesList ?: return res

        for (expense in filteredExpenses) {
            if (expense.amount > 0) continue

            val category = expense.category
            val amount = -expense.amount
            res[category] = amount + (res[category] ?: 0.0)
        }

        return res
    }


    /**
     * Get total amount
     */
    fun totalSpending(): Double {
        val expensesList = expensesViewState.value?.expensesList
        val filteredList = expensesList?.filter { expense -> expense.amount < 0 }
        val res = filteredList?.fold(0.0) { acc, i -> acc + i.amount} ?: return 0.0

        return if (res == 0.0) res else -res
    }


    /**
     * GEt total earning
     */
    fun totalEarning(): Double {
        val expensesList = expensesViewState.value?.expensesList
        val filteredList = expensesList?.filter { expense -> expense.amount > 0 }
        return filteredList?.fold(0.0) { acc, i -> acc + i.amount} ?: 0.0
    }


    /**
     * Get min date
     */
    fun minDate(): Date? = expensesViewState.value?.expensesList?.minByOrNull { it.date }?.date

    /**
     * Get maxDate
     */
    fun maxDate(): Date? = expensesViewState.value?.expensesList?.maxByOrNull { it.date }?.date
}