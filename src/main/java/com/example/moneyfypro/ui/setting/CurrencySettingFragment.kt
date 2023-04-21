package com.example.moneyfypro.ui.setting

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.R
import com.example.moneyfypro.databinding.FragmentCurrencySettingBinding
import com.example.moneyfypro.model.SettingViewModel


class CurrencySettingFragment: DialogFragment() {
    private val settingViewModel: SettingViewModel by activityViewModels()
    private lateinit var binding: FragmentCurrencySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MoneyfyPro)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrencySettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureReturnCallback()
        settingViewModel.saveCurrency.observe(viewLifecycleOwner) {
            binding.currencySelectionText.text = it.currencyCode
        }
        binding.currencySelectionButton.setOnClickListener { openChangeCurrencyDialog() }
    }

    private fun configureReturnCallback() {
        val toolbar = binding.toolbar
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        saveCurrencySetting()
        super.onDismiss(dialog)
    }

    private fun saveCurrencySetting() {
        val confirmedCurrencyCode = settingViewModel.saveCurrency.value?.currencyCode ?: ""
        settingViewModel.setCurrency(confirmedCurrencyCode)
        val sharedPreferences = requireActivity().getSharedPreferences("share", Context.MODE_PRIVATE) ?: return
        sharedPreferences.let { pref ->
            val editor = pref.edit()
            editor.putString(sharedPreferences.currencyId(), confirmedCurrencyCode)
            editor.apply()
        }
    }


    private fun openChangeCurrencyDialog() {
        CurrencyPickerBottomSheetDialog().show(childFragmentManager, CurrencyPickerBottomSheetDialog.TAG)
    }

    companion object {
        const val TAG = "Currency setting"
    }
}