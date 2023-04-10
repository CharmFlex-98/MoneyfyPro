package com.example.moneyfypro.utils

import com.example.moneyfypro.data.*
import java.util.*

interface ExpensesFilter {

    fun filter(expenses: List<Expense>): List<Expense>

    fun queryGenerator(): QueryGenerator

}


/**
 * Combined filter
 */
class CombinedFilter(private val filters: List<ExpensesFilter>): ExpensesFilter {
    override fun filter(expenses: List<Expense>): List<Expense> {
        return filters.fold(expenses) { acc, i -> i.filter(acc) }
    }

    override fun queryGenerator(): QueryGenerator {
        val queryGenerators = filters.map { it.queryGenerator() }
        return CombinedFilterQueryGenerator(queryGenerators)
    }
}



/**
 * Filter by date
 */
class DateFilter(private val startDate: Date?, private val endDate: Date?): ExpensesFilter {

    override fun filter(expenses: List<Expense>): List<Expense> {
        if ((startDate == null) and (endDate == null)) return expenses

        var filteredExpenses = expenses

        if (startDate != null) {
            filteredExpenses = filteredExpenses.filter {
                it.date >= startDate
            }
        }
        if (endDate != null) {
            filteredExpenses = filteredExpenses.filter {
                it.date <= endDate
            }
        }

        return filteredExpenses
    }

    override fun queryGenerator(): QueryGenerator {
        return DateFilterQueryGenerator(startDate, endDate)
    }
}


/**
 * Category filter
 */
class CategoryFilter(private val categories: List<String>): ExpensesFilter {
    override fun filter(expenses: List<Expense>): List<Expense> {
        TODO("Not yet implemented")
    }

    override fun queryGenerator(): QueryGenerator {
        return CategoryFilterQueryGenerator(categories)
    }

}


/**
 * Keyword filter
 */
class KeywordFilter(private val keyword: String): ExpensesFilter {
    override fun filter(expenses: List<Expense>): List<Expense> {
        TODO("Not yet implemented")
    }

    override fun queryGenerator(): QueryGenerator {
        return KeywordFilterQueryGenerator(keyword)
    }

}

