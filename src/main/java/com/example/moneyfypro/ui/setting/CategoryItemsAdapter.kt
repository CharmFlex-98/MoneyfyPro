package com.example.moneyfypro.ui.setting

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.databinding.CategoryItemBinding
import com.example.moneyfypro.databinding.DropdownItemBinding
import com.example.moneyfypro.ui.CustomListAdapterBase
import java.util.zip.Inflater

class CategoryItemsAdapter: CustomListAdapterBase<String, CategoryItemBinding, CategoryItemsAdapter.CategoryItemViewHolder>(
    DiffCallback) {

    override fun createViewHolder(binding: CategoryItemBinding): CategoryItemViewHolder {
        return CategoryItemViewHolder(binding)
    }

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        attachToRoot: Boolean
    ): CategoryItemBinding {
        return CategoryItemBinding.inflate(layoutInflater, parent, attachToRoot)
    }


    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

        }

    }



    class CategoryItemViewHolder(binding: CategoryItemBinding): CustomListItemViewHolder<String, CategoryItemBinding>(binding) {
        override fun bind(data: String) {
           binding.categoryTextView.text = data
        }
    }



}