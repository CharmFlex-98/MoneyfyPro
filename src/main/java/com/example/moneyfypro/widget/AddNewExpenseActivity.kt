package com.example.moneyfypro.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesDatabase
import com.example.moneyfypro.data.ExpensesRepository
import com.example.moneyfypro.data.adjustedDateWithoutTimeZone
import com.example.moneyfypro.databinding.AddExpenseMiniBinding
import com.example.moneyfypro.ui.CategoryItemsNoFilterAdapter
import com.example.moneyfypro.utils.CategoryPreferenceManager
import com.example.moneyfypro.utils.expensesSharedPreferencesInstance
import com.example.moneyfypro.utils.stringToSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddNewExpenseActivity : AppCompatActivity() {
    private lateinit var binding: AddExpenseMiniBinding

    @Inject
    lateinit var repository: ExpensesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddExpenseMiniBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // The setup binding has a callback which potentially instantiate a snack bar
        // Snack bar need a parent view to be attached to so we need to make sure the content view is set up.
        setupBinding()
    }


    private fun setupBinding() {
        binding.apply {
            val sharedPreferences = expensesSharedPreferencesInstance(this@AddNewExpenseActivity)
            val manager = CategoryPreferenceManager(this@AddNewExpenseActivity, sharedPreferences)
            val catsString = manager.getValue()
            catsString.let {
                val adapter = CategoryItemsNoFilterAdapter(
                    this@AddNewExpenseActivity,
                    R.layout.dropdown_item,
                    it.stringToSet().toTypedArray()
                )
                categoryField.setAdapter(adapter)
            }

            submitExpenseButton.setOnClickListener {
                submitExpense(isSpending = true)

            }
            submitIncomeButton.setOnClickListener {
                submitExpense(isSpending = false)
            }


        }
    }

    private fun submitExpense(isSpending: Boolean) {
        if (!isValidInputs()) {
            Snackbar.make(binding.root, "Invalid input", 1000).show()
            return
        }
        val counter = if (isSpending) -1 else 1
        val fromCalendar = Calendar.getInstance()
        fromCalendar.add(Calendar.DATE, -7)

        lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.IO) {
                repository.insertExpense(
                    Expense(
                        id = Date().time.toString(),
                        description = binding.descriptionContent.text.toString(),
                        amount = binding.amountContent.text.toString().toDouble() * counter,
                        date = Expense.adjustedDateWithoutTimeZone(Date()) ?: Date(),
                        category = binding.categoryField.text.toString()
                    )
                )
            }

            val widgetManager = AppWidgetManager.getInstance(this@AddNewExpenseActivity)
            val componentName =
                ComponentName(this@AddNewExpenseActivity, MoneyfyProWidgetProvider::class.java)
            val widgetIds = widgetManager.getAppWidgetIds(componentName)

            updateAppWidgets(this@AddNewExpenseActivity, widgetManager, widgetIds, repository)
            finish()
        }
    }


    private fun isValidInputs(): Boolean {
        return (binding.categoryField.text.isNotBlank() && binding.descriptionContent.text.isNotBlank() && binding.amountContent.text.isNotBlank())
    }


}