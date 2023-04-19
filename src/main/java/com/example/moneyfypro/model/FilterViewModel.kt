package com.example.moneyfypro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesRepository
import com.example.moneyfypro.data.dateFormat
import com.example.moneyfypro.utils.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(private val repository: ExpensesRepository) :
    ViewModel() {
    // Filters
    private val _startDate = MutableLiveData<Date>()
    val startDate: LiveData<Date>
        get() = _startDate

    private val _endDate = MutableLiveData<Date>()
    val endDate: LiveData<Date>
        get() = _endDate

    private val _keyword = MutableLiveData<String>()
    val keyword: LiveData<String>
        get() = _keyword

    private val _selectedCategories = MutableLiveData<List<String>>()
    val selectedCategories: LiveData<List<String>>
        get() = _selectedCategories


    init {
        _startDate.value = Expense.dateFormat().parse("01/01/1970")
        _endDate.value = Date(Date().time)
        _keyword.value = ""
    }


    // Methods

    fun setStartData(date: Date) {
        _startDate.value = date
    }

    fun setEndDate(date: Date) {
        _endDate.value = date
    }

    fun setKeyword(keyword: String) {
        _keyword.value = keyword
    }

    fun setCategoriesFilter(categories: List<String>) {
        _selectedCategories.value = categories
    }

    /**
     * Get date validator
     */
    fun dateValidator(startDate: Boolean): DateValidator? {
        if (startDate && _endDate.value != null) return DateValidatorPointBackward.before(_endDate.value!!.time)
        if (!startDate && _startDate.value != null) return CompositeDateValidator.allOf(
            listOf(
                DateValidatorPointForward.from(_startDate.value!!.time),
                DateValidatorPointBackward.before(Date().time)
            )
        )
        return null
    }


    ///
    //// THIS WILL RETURN THE EXPENSES FILTERS
    ///


    /**
     * Collect all filters
     */
    private fun collectAllFilters(): List<ExpensesFilter> {
        val filters = mutableListOf<ExpensesFilter>()

        // Append date filter
        filters.add(dateFilter())

        // Other filters (depend on situation)
        if (!_keyword.value.isNullOrEmpty()) filters.add(keywordFilter())
        println("see this ${selectedCategories.value}")
        if (!selectedCategories.value.isNullOrEmpty()) filters.add(categoryFilter())

        return filters
    }

    /**
     * Get Combined Filter
     */
    fun allFilters(): ExpensesFilter {
        return CombinedFilter(collectAllFilters())
    }


    /**
     * Date filter
     */
    private fun dateFilter(): ExpensesFilter {
        return DateFilter(_startDate.value, _endDate.value)
    }

    /**
     * Keyword filter
     */
    private fun keywordFilter(): ExpensesFilter {
        return KeywordFilter(_keyword.value ?: "")
    }


    /**
     * Category filter
     */
    private fun categoryFilter(): ExpensesFilter {
        return CategoryFilter(selectedCategories.value ?: listOf())
    }

}
