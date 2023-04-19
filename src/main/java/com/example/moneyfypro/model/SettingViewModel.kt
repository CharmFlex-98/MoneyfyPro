package com.example.moneyfypro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moneyfypro.ui.setting.CurrencySelection
import java.util.Currency

class SettingViewModel : ViewModel() {
    private val _saveCategories = MutableLiveData<MutableSet<String>>()
    // This is like a getter method
    val saveCategories: LiveData<MutableSet<String>>
        get() = _saveCategories

    private val _saveCurrency = MutableLiveData<CurrencySelection>()
    val saveCurrency: LiveData<CurrencySelection>
    get() = _saveCurrency

    fun setCategories(categories: Set<String>) {
        _saveCategories.value = categories.toMutableSet()
    }

    fun addCategory(newCategory: String) {
        val cats = saveCategories.value ?: return
        cats.add(newCategory)
        setCategories(cats)
    }

    fun removeCategory(category: String) {
        val cats = saveCategories.value ?: return
        cats.remove(category)
        setCategories(cats)
    }


    fun chooseCurrency(currencyCode: String) {
        _saveCurrency.value = CurrencySelection.CurrencySelectionInProgress(currencyCode)
    }

    fun setCurrency(currencyCode: String) {
        _saveCurrency.value = CurrencySelection.CurrencySelectionConfirmed(currencyCode)
    }
}