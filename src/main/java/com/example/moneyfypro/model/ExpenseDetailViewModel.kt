package com.example.moneyfypro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moneyfypro.data.Expense
import java.util.Date

class ExpenseDetailViewModel: ViewModel() {

    private val _id = MutableLiveData<String>()
    val id: LiveData<String>
        get() = _id

    private val _category = MutableLiveData<String>()
    val category: LiveData<String>
        get() = _category

    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    private val _amount = MutableLiveData<Double>()
    val amount: LiveData<Double>
        get() = _amount

    private val _date = MutableLiveData<Date>()
    val date: LiveData<Date>
        get() = _date


    fun steal(expenseDetail: Expense) {
        _id.value = expenseDetail.id
        _category.value = expenseDetail.category
        _amount.value = expenseDetail.amount
        _date.value = expenseDetail.date
        _description.value = expenseDetail.description
    }


    fun isValidDetails(): Boolean {
        return !(_category.value == null || _amount.value == null || _description.value == null || _date.value == null)
    }
}