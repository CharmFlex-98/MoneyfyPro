package com.example.moneyfypro.ui.custom_view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.example.moneyfypro.databinding.ActivityMainBinding
import com.example.moneyfypro.ui.MainActivity
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DraggableFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FloatingActionButton(context, attrs, defStyleAttr), OnTouchListener {

    private var initialX = 0f
    private var touchOffsetX = 0f;
    private var initialY = 0f
    private var touchOffsetY = 0f

    init {
        this.setOnTouchListener(this)
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null || event == null) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                touchOffsetX = initialX - v.x
                initialY = event.rawY
                touchOffsetY = initialY - v.y
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                var currentViewX = event.rawX - touchOffsetX
                var currentViewY = event.rawY - touchOffsetY
                val displayMetrics = resources.displayMetrics
                val dpHeight = displayMetrics.heightPixels
                val dpWidth = displayMetrics.widthPixels

                currentViewX = currentViewX.coerceAtLeast(0f)
                currentViewX = currentViewX.coerceAtMost((dpWidth - v.width).toFloat())
                currentViewY = currentViewY.coerceAtLeast(0f)
                val bottomOffset: Int = ((context as? MainActivity)?.binding?.bottomNavBar?.height ?: 0)
                val topOffset = (context as? MainActivity)?.supportActionBar?.height ?: 0
                currentViewY = currentViewY.coerceAtMost((dpHeight - v.height - bottomOffset - topOffset).toFloat())

                v.animate()
                    .x(currentViewX)
                    .y(currentViewY)
                    .setDuration(0)
                    .start()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (event.rawX == initialX && event.rawY == initialY) {
                    (context as? OnClickListener)?.nonDragFABClick()
                }
                return true
            }
        }

        return false
    }

    interface OnClickListener {
        fun nonDragFABClick()
    }

    override fun performClick(): Boolean {
        // Just want to silent the warning
        return super.performClick()
    }
}