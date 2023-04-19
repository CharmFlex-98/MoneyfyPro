package com.example.moneyfypro.ui
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.dateFormat
import com.example.moneyfypro.data.toAmountFormat
import com.example.moneyfypro.databinding.DialogExpenseDetailBinding
import com.example.moneyfypro.model.ExpenseDetailViewModel
import com.example.moneyfypro.model.ExpensesViewModel



class ExpenseDetailDialog : DialogFragment() {
    private lateinit var binding: DialogExpenseDetailBinding
    private val expenseViewModel: ExpensesViewModel by activityViewModels()
    private val expenseDetailViewModel: ExpenseDetailViewModel by activityViewModels()

    companion object {
        const val TAG = "Expense detail"
    }

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
        binding.apply {
            expenseDetailViewModel.apply {
                category.observe(viewLifecycleOwner) { categoryContent.text = it }
                amount.observe(viewLifecycleOwner) { amountContent.text = Expense.toAmountFormat(it, "", false) }
                description.observe(viewLifecycleOwner) { descriptionContent.text = it }
                date.observe(viewLifecycleOwner) {
                    dateContent.text = Expense.dateFormat().format(it)
                }
            }

            closeDialogButton.setOnClickListener { dismiss() }
            editExpenseButton.setOnClickListener { editExpense() }
            deleteExpenseButton.setOnClickListener {
                deleteExpense(expenseDetailViewModel.id.value ?: "")
                dismiss()
            }
        }
    }

    private fun editExpense() {
        AddExpensesFragment.instance(isEditMode = true)
            .show(requireActivity().supportFragmentManager, AddExpensesFragment.TAG)
    }


    private fun deleteExpense(expenseId: String) {
        expenseViewModel.expensesViewState.value?.expensesList?.firstOrNull {
            it.id == expenseId
        }?.let {
            expenseViewModel.deleteExpense(it)
        }
    }
}