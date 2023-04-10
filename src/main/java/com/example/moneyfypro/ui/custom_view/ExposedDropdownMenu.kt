package com.example.moneyfypro.ui.custom_view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class ExposedDropdownMenu(context: Context, attributes: AttributeSet) :
    MaterialAutoCompleteTextView(context, attributes) {

    override fun getFreezesText(): Boolean {
        return super.getFreezesText()
    }
}