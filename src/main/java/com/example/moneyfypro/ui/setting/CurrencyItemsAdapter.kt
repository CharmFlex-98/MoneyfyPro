package com.example.moneyfypro.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesCurrency
import com.example.moneyfypro.databinding.CurrencyListItemBinding
import com.example.moneyfypro.databinding.DropdownItemBinding
import com.example.moneyfypro.ui.CustomListAdapterBase
import java.util.zip.Inflater

class CurrencyItemsAdapter(private val listener: OnCurrencyItemSelectedListener?):
    CustomListAdapterBase<ExpensesCurrency, CurrencyListItemBinding, CurrencyItemsAdapter.CurrencyItemViewHolder>(
        diffUtil
    ) {

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ExpensesCurrency>() {
            override fun areItemsTheSame(oldItem: ExpensesCurrency, newItem: ExpensesCurrency): Boolean {
                return oldItem.code == newItem.code
            }

            override fun areContentsTheSame(oldItem: ExpensesCurrency, newItem: ExpensesCurrency): Boolean {
                return oldItem.name == newItem.name && oldItem.code == newItem.code
            }

        }
    }

    override fun createViewHolder(binding: CurrencyListItemBinding): CurrencyItemViewHolder {
        return CurrencyItemViewHolder(binding, listener)
    }

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        attachToRoot: Boolean
    ): CurrencyListItemBinding {
        return CurrencyListItemBinding.inflate(layoutInflater, parent, attachToRoot)
    }

    interface OnCurrencyItemSelectedListener {
        fun currencyItemSelected(binding: CurrencyListItemBinding)
    }



    class CurrencyItemViewHolder(binding: CurrencyListItemBinding) : CustomListItemViewHolder<ExpensesCurrency, CurrencyListItemBinding>(binding) {
        private var listener: OnCurrencyItemSelectedListener? = null

        constructor(binding: CurrencyListItemBinding, listener: OnCurrencyItemSelectedListener?) : this(binding) {
            this.listener = listener
        }

        override fun bind(data: ExpensesCurrency) {
            setListeners()
            binding.currencyName.text = data.name
            binding.currencyCode.text = data.code
        }

        private fun setListeners() {
            itemView.setOnClickListener {
                listener?.currencyItemSelected(binding)
            }
        }


    }
}