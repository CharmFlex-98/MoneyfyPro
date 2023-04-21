package com.example.moneyfypro.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesDatabase
import com.example.moneyfypro.data.adjustedDateWithoutTimeZone
import com.example.moneyfypro.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class MoneyfyProWidget : AppWidgetProvider() {
    @Inject lateinit var database: ExpensesDatabase


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val bundle = intent?.extras
        if (bundle != null && bundle.getString("expensesInfo")?.isNotEmpty() == true) {
            runBlocking {
                database.getDao().insertExpense(Expense(
                    Date().time.toString(),
                    category = "Housing",
                    description = "Testing1",
                    date = Expense.adjustedDateWithoutTimeZone(Date()) ?: Date(),
                    amount = -5000.0
                ))
            }

        }
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        //Update database


        //
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.moneyfy_pro_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)
    val intent = Intent(context, AddNewExpenseActivity::class.java)
//    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//    val bundle = Bundle()
//    bundle.putString("expensesInfo", "50")
//    bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
//    intent.putExtras(bundle)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}