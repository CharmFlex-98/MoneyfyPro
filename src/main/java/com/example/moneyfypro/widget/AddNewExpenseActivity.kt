package com.example.moneyfypro.widget

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesDatabase
import com.example.moneyfypro.data.adjustedDateWithoutTimeZone
import com.example.moneyfypro.databinding.AddExpenseMiniBinding
import com.example.moneyfypro.ui.CategoryItemsNoFilterAdapter
import com.example.moneyfypro.ui.setting.categoryId
import com.example.moneyfypro.ui.setting.defaultCategories
import com.example.moneyfypro.ui.setting.setToString
import com.example.moneyfypro.ui.setting.stringToSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddNewExpenseActivity : AppCompatActivity() {
    private lateinit var binding: AddExpenseMiniBinding

    @Inject
    lateinit var expenseDatabase: ExpensesDatabase

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
            val sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE)
            val catsString = sharedPreferences.getString(
                sharedPreferences.categoryId(),
                sharedPreferences.setToString(sharedPreferences.defaultCategories(this@AddNewExpenseActivity))
            )
            catsString?.let {
                val adapter = CategoryItemsNoFilterAdapter(
                    this@AddNewExpenseActivity,
                    R.layout.dropdown_item,
                    sharedPreferences.stringToSet(it).toTypedArray()
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

        CoroutineScope(Dispatchers.IO).launch {
            expenseDatabase.getDao().insertExpense(
                Expense(
                    id = Date().time.toString(),
                    description = binding.descriptionContent.text.toString(),
                    amount = binding.amountContent.text.toString().toDouble() * counter,
                    date = Expense.adjustedDateWithoutTimeZone(Date()) ?: Date(),
                    category = binding.categoryField.text.toString()
                )
            )

            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }


    private fun isValidInputs(): Boolean {
        return (binding.categoryField.text.isNotBlank() && binding.descriptionContent.text.isNotBlank() && binding.amountContent.text.isNotBlank())
    }


}