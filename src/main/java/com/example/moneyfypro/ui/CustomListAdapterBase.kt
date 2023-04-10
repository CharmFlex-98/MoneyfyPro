package com.example.moneyfypro.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.util.zip.Inflater

abstract class CustomListAdapterBase<D, VB : ViewBinding,
        VH : CustomListAdapterBase.CustomListItemViewHolder<D, VB>>(
    diffCallback: DiffUtil.ItemCallback<D>
) : ListAdapter<D, VH>(diffCallback) {
    lateinit var binding: VB
    lateinit var recyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        binding = createBinding(inflater, parent, false)
        return createViewHolder(binding)
    }

    abstract fun createViewHolder(binding: VB): VH

    abstract fun createBinding(layoutInflater: LayoutInflater, parent: ViewGroup, attachToRoot: Boolean): VB


    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position])
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    abstract class CustomListItemViewHolder<D, VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        abstract fun bind(data: D)


    }

}