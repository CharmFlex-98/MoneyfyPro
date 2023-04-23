package com.example.moneyfypro.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.moneyfypro.R
import com.example.moneyfypro.data.ExpensesRepository
import com.example.moneyfypro.utils.DateFilter
import com.example.moneyfypro.utils.getPeriodicTotalExpensesData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class MoneyfyProWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var repository: ExpensesRepository


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        val fromCalendar = Calendar.getInstance()
        fromCalendar.add(Calendar.DATE, -7)
        updateAppWidgets(context, appWidgetManager, appWidgetIds, repository)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    repository: ExpensesRepository
) {
    val widgetScope = CoroutineScope(Dispatchers.Main +  CoroutineExceptionHandler { _, _ -> })

    val fromCalendar = Calendar.getInstance()
    fromCalendar.add(Calendar.DATE, -7)
    widgetScope.launch {
        val expensesData =
            async { getPeriodicTotalExpensesData(repository, fromCalendar.time, Date()) }
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, expensesData.await())
        }
    }
}

private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    expenseStatusData: List<Double>
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.moneyfy_pro_widget)
    val intent = Intent(context, AddNewExpenseActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

    // Construct the progress bar
    if (expenseStatusData.size == 2) {
        val totalVal = expenseStatusData.fold(0.0) { acc, i -> acc + i }.roundToInt()
        val profitVal = expenseStatusData[0].roundToInt()
        val spendVal = expenseStatusData[1].roundToInt()
        views.setTextViewText(R.id.profit_amount_text, "PROFIT: $profitVal")
        views.setTextViewText(R.id.expense_amount_text, "SPEND: $spendVal")
        views.setProgressBar(R.id.profit_bar, totalVal, expenseStatusData[0].roundToInt(), false)
        views.setProgressBar(R.id.expense_bar, totalVal, expenseStatusData[1].roundToInt(), false)
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}