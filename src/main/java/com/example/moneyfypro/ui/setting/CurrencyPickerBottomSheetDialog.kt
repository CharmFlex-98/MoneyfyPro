package com.example.moneyfypro.ui.setting

import android.content.Context
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.moneyfypro.R
import com.example.moneyfypro.data.ExpensesCurrency
import com.example.moneyfypro.databinding.CurrencyListItemBinding
import com.example.moneyfypro.databinding.DialogCurrencyPickerBinding
import com.example.moneyfypro.databinding.DropdownItemBinding
import com.example.moneyfypro.model.SettingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.*

class CurrencyPickerBottomSheetDialog: BottomSheetDialogFragment(), CurrencyItemsAdapter.OnCurrencyItemSelectedListener {
    private lateinit var binding: DialogCurrencyPickerBinding
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val currencyList = Currency.getAvailableCurrencies()

    companion object {
        const val TAG = "currency picker bottom sheet dialog"

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Configure keyboard
        setStyle(DialogFragment.STYLE_NORMAL, R.style.bottom_sheet_dialog_style)
        binding = DialogCurrencyPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView = binding.currencySelectionView
        val adapter = CurrencyItemsAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter.submitList(currencyList.map {
            ExpensesCurrency(it.displayName, it.currencyCode)
        }.sortedBy { it.name })
    }
    override fun currencyItemSelected(binding: CurrencyListItemBinding) {
        settingViewModel.chooseCurrency(binding.currencyCode.text.toString())
        dismiss()
    }
}