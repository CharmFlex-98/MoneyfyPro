package com.example.moneyfypro.ui.setting

import androidx.recyclerview.widget.RecyclerView

interface ItemOnTouchListener {
    fun onMove(recyclerView: RecyclerView,
               viewHolder: RecyclerView.ViewHolder,
               target: RecyclerView.ViewHolder): Boolean

    fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
}