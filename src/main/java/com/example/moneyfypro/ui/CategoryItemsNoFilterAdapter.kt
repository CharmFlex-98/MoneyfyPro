package com.example.moneyfypro.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes

class CategoryItemsNoFilterAdapter<T>(context: Context, @LayoutRes resource: Int, objects: Array<T>) : ArrayAdapter<T>(context, resource, objects) {

    override fun getFilter(): Filter {
        return NoFilter()
    }

    class NoFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults()

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit

    }
}