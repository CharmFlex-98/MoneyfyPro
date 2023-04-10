package com.example.moneyfypro.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.dateFormat
import com.example.moneyfypro.data.toAmountFormat
import com.example.moneyfypro.databinding.FragmentAddExpensesBinding
import com.example.moneyfypro.model.ExpensesViewModel
import com.example.moneyfypro.model.SettingViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.runBlocking
import java.util.*

@AndroidEntryPoint
class AddExpensesFragment() : DialogFragment() {
    private var _binding: FragmentAddExpensesBinding? = null
    private val binding: FragmentAddExpensesBinding
        get() = _binding!!


    @ActivityRetainedScoped
    val viewModel: ExpensesViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()


    companion object {
        const val TAG = "AddExpensesDialog"
        fun instance(referencedExpense: Expense): AddExpensesFragment {
            val res = AddExpensesFragment();
            val args = Bundle()
            args.apply {
                putString(Expense.CATEGORY_KEY, referencedExpense.category)
                putString(Expense.DESCRIPTION_KEY, referencedExpense.description)
                putDouble(Expense.AMOUNT_KEY, referencedExpense.amount)
                putString(Expense.DATE_KEY, Expense.dateFormat().format(referencedExpense.date))
                putInt(Expense.ID_KEY, referencedExpense.id)
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
            amountField.hint = "Amount  (${settingViewModel.saveCurrency.value})"
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

        if (isEditMode()) loadExpenseData()
    }

    private fun isEditMode() = arguments != null

    private fun loadExpenseData() {
        val args = requireArguments()
        val amount = args.getDouble(Expense.AMOUNT_KEY)
        binding.apply {
            categoryField.setText(args.getString(Expense.CATEGORY_KEY))
            args.getString(Expense.DATE_KEY)?.let {
                dateField.setText(Expense.dateFormat().parse(it)?.toString() ?: "")
            }
            dateField.setText(args.getString(Expense.DATE_KEY))
            descriptionField.setText(args.getString(Expense.DESCRIPTION_KEY))
            amountField.setText(Expense.toAmountFormat(args.getDouble(Expense.AMOUNT_KEY), "", currencyFormat = false))
            expensesType.check(if (amount < 0) R.id.spending_radio_button else R.id.earning_radio_button)
        }
    }

    private fun openDatePickerDialog() {
        val calender = Calendar.getInstance()
        val constraintBuilder = CalendarConstraints.Builder().setOpenAt(
            calender.timeInMillis
        ).build()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(constraintBuilder)
            .build()
        picker.addOnPositiveButtonClickListener {
            setDateText(it)
        }
        picker.show(requireActivity().supportFragmentManager, "materialDatePicker")
    }

    private fun setDateText(date: Long) {
        val dateObj = Date(date)
        binding.dateField.setText(Expense.dateFormat().format(dateObj))
    }


    private fun onSubmit() {
        if (validInput()) {
            var amount = binding.amountField.text.toString().toDouble()
            amount =
                if (binding.expensesType.checkedRadioButtonId == binding.earningRadioButton.id) amount else -amount
            runBlocking {
                if (isEditMode()) {
                    viewModel.updateExpense(
                        id = requireArguments().getInt(Expense.ID_KEY),
                        category = binding.categoryField.text.toString(),
                        description = binding.descriptionField.text.toString(),
                        amount = amount,
                        date = Expense.dateFormat().parse(binding.dateField.text.toString())!!
                    )
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
