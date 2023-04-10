package com.example.moneyfypro.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


/**
 * Hilt dagger would know where to find the application context
 */
@HiltAndroidApp
class ExpensesApp: Application() {
}