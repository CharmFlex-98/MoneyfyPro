package com.example.moneyfypro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.dateFormat
import com.example.moneyfypro.data.toAmountFormat
import com.example.moneyfypro.databinding.FragmentAddExpensesBinding
import com.example.moneyfypro.model.ExpenseDetailViewModel
import com.example.moneyfypro.model.ExpensesViewModel
import com.example.moneyfypro.model.SettingViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.absoluteValue

@AndroidEntryPoint
class AddExpensesFragment : DialogFragment() {
    private var _binding: FragmentAddExpensesBinding? = null
    private val binding: FragmentAddExpensesBinding
        get() = _binding!!


    @ActivityRetainedScoped
    val viewModel: ExpensesViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val expenseDetailViewModel: ExpenseDetailViewModel by activityViewModels()


    companion object {
        const val TAG = "AddExpensesDialog"
        const val IS_EDIT_MODE = "isEditMode"
        fun instance(isEditMode: Boolean = false): AddExpensesFragment {
            val res = AddExpensesFragment();
            val args = Bundle()
            args.apply {
               putBoolean(IS_EDIT_MODE, isEditMode)
            }
            res.arguments = args

            return res
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            binding.categoryField.id.toString(),
            binding.categoryField.text.toString()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddExpensesBinding.inflate(inflater, container, false)

        binding.apply {
            toolbar.title = if (!isEditMode()) "Add Expense" else "Edit Expense"
            val adapter = CategoryItemsNoFilterAdapter(
                requireContext(),
                R.layout.dropdown_item,
                settingViewModel.saveCategories.value?.toTypedArray() ?: arrayOf()
            )
            categoryField.setAdapter(adapter)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            amountField.hint =
                "Amount  (${settingViewModel.saveCurrency.value?.currencyCode ?: ""})"
            // load save instance
            categoryField.setText(savedInstanceState?.getString(categoryField.id.toString()))

            dateField.setOnClickListener {
                openDatePickerDialog()
            }
            expensesType.setOnCheckedChangeListener { group, checkedId -> group.check(checkedId) }
            spendingRadioButton.isChecked = true
            toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            toolbar.setNavigationOnClickListener {
                dismiss()
            }
            submitButton.setOnClickListener { onSubmit() }
        }

        if (isEditMode() && expenseDetailViewModel.isValidDetails()) loadExpenseData()
    }

    private fun isEditMode() = arguments != null && requireArguments().getBoolean(IS_EDIT_MODE)

    private fun loadExpenseData() {
        val amount = expenseDetailViewModel.amount.value!!
        binding.apply {
            categoryField.setText(expenseDetailViewModel.category.value)
            dateField.setText(Expense.dateFormat().format(expenseDetailViewModel.date.value!!))
            descriptionField.setText(expenseDetailViewModel.description.value)
            amountField.setText(
                Expense.toAmountFormat(
                    amount.absoluteValue,
                    "",
                    currencyFormat = false
                )
            )
            expensesType.check(if (amount < 0) R.id.spending_radio_button else R.id.earning_radio_button)
        }
    }

    private fun openDatePickerDialog() {
        val calender = Calendar.getInstance()
        val validator = DateValidatorPointBackward.now()
        val constraintBuilder = CalendarConstraints.Builder().setOpenAt(
            calender.timeInMillis
        ).setValidator(validator).build()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(constraintBuilder)
            .build()
        picker.addOnPositiveButtonClickListener {
            setDateText(Date(it))
        }
        picker.show(requireActivity().supportFragmentManager, "materialDatePicker")
    }

    private fun setDateText(date: Date) {
        binding.dateField.setText(Expense.dateFormat().format(date))
    }


    private fun onSubmit() {
        if (validInput()) {
            var amount = binding.amountField.text.toString().toDouble()
            amount =
                if (binding.expensesType.checkedRadioButtonId == binding.earningRadioButton.id) amount else -amount
            runBlocking {
                if (isEditMode() && expenseDetailViewModel.id.value != null) {
                    val id = expenseDetailViewModel.id.value!!
                    val cat = binding.categoryField.text.toString()
                    val desc = binding.descriptionField.text.toString()
                    val date = Expense.dateFormat().parse(binding.dateField.text.toString())!!
                    viewModel.updateExpense(
                        id = id,
                        category = cat,
                        description = desc,
                        amount = amount,
                        date = date
                    )
                    // So that when back to detail dialog page, the value there will be updated.
                    val expense = Expense(
                        id = id,
                        category = cat,
                        description = desc,
                        date = date,
                        amount = amount
                    )
                    expenseDetailViewModel.steal(expense)
                } else {
                    viewModel.insertExpense(
                        category = binding.categoryField.text.toString(),
                        description = binding.descriptionField.text.toString(),
                        amount = amount,
                        date = Expense.dateFormat().parse(binding.dateField.text.toString())!!
                    )

                }
                dismiss()
            }

        }
    }

    /**
     * Input is valid
     */
    private fun validInput(): Boolean {
        return (!binding.categoryField.text.isNullOrEmpty() &&
                !binding.amountField.text.isNullOrEmpty() &&
                !binding.descriptionField.text.isNullOrEmpty() &&
                !binding.dateField.text.isNullOrEmpty())
    }

}
