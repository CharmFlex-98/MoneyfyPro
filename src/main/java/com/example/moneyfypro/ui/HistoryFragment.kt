package com.example.moneyfypro.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneyfypro.databinding.FragmentHistoryBinding
import com.example.moneyfypro.model.ExpensesViewModel
import com.example.moneyfypro.model.FilterViewModel
import com.example.moneyfypro.model.SettingViewModel
import com.example.moneyfypro.ui.setting.CurrencySelection
import com.example.moneyfypro.ui.setting.currencyId


class HistoryFragment : Fragment() {
    lateinit var _binding: FragmentHistoryBinding
    private val _expensesViewModel: ExpensesViewModel by activityViewModels()
    private val _settingViewModel: SettingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create adapter for expense item list
        val adapter = ExpenseItemAdapter(requireActivity());
        _binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }

        _expensesViewModel.expensesViewState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.expensesList)
        }
        _settingViewModel.saveCurrency.observe(viewLifecycleOwner) {
            if (it is CurrencySelection.CurrencySelectionConfirmed) {
               adapter.notifyDataSetChanged()
            }
        }
    }
}