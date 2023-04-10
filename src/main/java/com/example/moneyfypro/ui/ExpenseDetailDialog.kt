package com.example.moneyfypro.ui

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.dateFormat
import com.example.moneyfypro.data.toAmountFormat
import com.example.moneyfypro.databinding.DialogExpenseDetailBinding
import com.example.moneyfypro.model.ExpensesViewModel
import com.example.moneyfypro.model.SettingViewModel
import kotlin.math.exp


class ExpenseDetailDialog : DialogFragment() {
    private lateinit var binding: DialogExpenseDetailBinding
    private val expenseViewModel: ExpensesViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogExpenseDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val mat = Resources.getSystem().displayMetrics
//        val width = mat.widthPixels * 0.7
//        val height = mat.heightPixels * 0.7
//        dialog?.window?.setLayout(width.toInt(), height.toInt())
        binding.apply {
            val args = requireArguments()
            val id = args.getInt(Expense.ID_KEY)

            if (id == 0) return@apply

            expenseViewModel.expensesViewState.observe(viewLifecycleOwner) { state ->

                val targetExpenses = state.expensesList.filter { expense -> expense.id == id }
                if (targetExpenses.count() != 1) return@observe

                val targetExpense = targetExpenses[0]

                categoryContent.text = targetExpense.category
                amountContent.text = Expense.toAmountFormat(targetExpense.amount, settingViewModel.saveCurrency.value?.currencyCode ?: "")
                descriptionContent.text = targetExpense.description
                dateContent.text = Expense.dateFormat().format(targetExpense.date)

            }

            closeDialogButton.setOnClickListener { dismiss() }
            editExpenseButton.setOnClickListener { editExpense(id) }
            deleteExpenseButton.setOnClickListener {
                deleteExpense(id)
                dismiss()
            }
        }
    }

    private fun editExpense(expenseId: Int) {
        val expense = createExpenseInstance(expenseId)
        if (expense != null) AddExpensesFragment.instance(expense)
            .show(requireActivity().supportFragmentManager, AddExpensesFragment.TAG)
    }

    private fun createExpenseInstance(expenseId: Int): Expense? {
        val targetExpense =
            expenseViewModel.expensesViewState.value?.expensesList?.firstOrNull { expense -> expense.id == expenseId }
                ?: return null

        val category = targetExpense.category
        val description = targetExpense.description
        val amount = targetExpense.amount
        val date = targetExpense.date

        return Expense(
            id = expenseId,
            category = category,
            description = description,
            amount = amount,
            date = date
        )
    }

    private fun deleteExpense(expenseId: Int) {
        println("trying to delete")
        expenseViewModel.expensesViewState.value?.expensesList?.firstOrNull {
            it.id == expenseId
        }?.let {
            println("yes the item will be deleted")
            expenseViewModel.deleteExpense(it)
        }
    }


    companion object {
        const val TAG = "Expense detail"
        fun instance(expense: Expense): ExpenseDetailDialog {
            val dialog = ExpenseDetailDialog()
            val bundle = Bundle()
            bundle.putInt(Expense.ID_KEY, expense.id)
            bundle.putString(Expense.CATEGORY_KEY, expense.category)
            bundle.putDouble(Expense.AMOUNT_KEY, expense.amount)
            bundle.putString(Expense.DESCRIPTION_KEY, expense.description)
            bundle.putString(Expense.DATE_KEY, Expense.dateFormat().format(expense.date))

            dialog.arguments = bundle
            return dialog
        }
    }
}