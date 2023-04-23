package com.example.moneyfypro.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.MessageFormat.format
import android.opengl.Visibility
import android.text.format.DateFormat.format
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil

import com.example.moneyfypro.data.Expense

import com.example.moneyfypro.data.toAmountFormat
import com.example.moneyfypro.databinding.ExpenseItemBinding
import com.example.moneyfypro.utils.CurrencyPreferenceManager
import com.example.moneyfypro.utils.expensesSharedPreferencesInstance

import java.text.SimpleDateFormat
import java.util.*

class ExpenseItemAdapter(private val activity: Activity, private val onViewPressedListener: OnViewPressedListener) :
    CustomListAdapterBase<Expense, ExpenseItemBinding, ExpenseItemAdapter.ExpenseItemViewHolder>(
        DiffCallback
    ) {

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return newItem.id == oldItem.id

            }

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return (newItem.description == oldItem.description &&
                        newItem.amount == oldItem.amount &&
                        newItem.category == oldItem.category)
            }
        }

    }


    interface OnViewPressedListener {
        fun detailViewPressed(position: Int)
    }

    class ExpenseItemViewHolder(
        binding: ExpenseItemBinding,
        activity: Activity,
        private val onViewPressedListener: OnViewPressedListener
    ) : CustomListItemViewHolder<Expense, ExpenseItemBinding>(binding) {
        private val sharedPreferences = expensesSharedPreferencesInstance(activity)
        override fun bind(data: Expense) {
            val manager = CurrencyPreferenceManager(sharedPreferences)
            binding.apply {
                val currencyCode = manager.getValue()
                expenseItemAmount.text = Expense.toAmountFormat(data.amount, currencyCode)
                expenseItemCategory.text = data.category
                expenseItemDescription.text = data.description
                expenseItemCategory.text = data.category
                dateSeparator.text = SimpleDateFormat("dd/MM/yyyy (E)", Locale.US).format(data.date)
                viewButton.setOnClickListener {
                    onViewPressedListener.detailViewPressed(adapterPosition)
                }
            }
        }

        fun showSeparator(showSpace: Boolean = true) {
            binding.apply {
                separator.visibility = View.VISIBLE
                if (showSpace) space.visibility = View.VISIBLE
            }
        }

        fun hideSeparator() {
            binding.apply {
                separator.visibility = View.GONE
                space.visibility = View.GONE
            }
        }

    }

    override fun createViewHolder(binding: ExpenseItemBinding): ExpenseItemViewHolder {
        return ExpenseItemViewHolder(binding, activity, onViewPressedListener)
    }

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        attachToRoot: Boolean
    ): ExpenseItemBinding {
        return ExpenseItemBinding.inflate(layoutInflater, parent, attachToRoot)
    }

    override fun onBindViewHolder(holder: ExpenseItemViewHolder, position: Int) {
        val currentExpense = currentList[position]
        holder.bind(currentExpense)

        if (position == 0) {
            holder.showSeparator(showSpace = false)
            return
        }

        val previousDate = currentList[position - 1].date
        val currentDate = currentExpense.date
        val format = SimpleDateFormat("yyyyMMdd", Locale.US)

        if (format.format(previousDate) != format.format(currentDate)) holder.showSeparator()
        else holder.hideSeparator()
    }




}
