package com.example.moneyfypro.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.moneyfypro.R
import com.example.moneyfypro.data.ExpensesRepository
import com.example.moneyfypro.utils.CurrencyPreferenceManager
import com.example.moneyfypro.utils.WidgetPeriodPreferenceManager
import com.example.moneyfypro.utils.expensesSharedPreferencesInstance
import com.example.moneyfypro.utils.getPeriodicTotalExpensesData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class MoneyfyProWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var repository: ExpensesRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (context != null && intent != null) {
            // Protect against rogue update broadcasts (not really a security issue,
            // just filter bad broadcasts out so subclasses are less likely to crash).
            val action = intent.action
            if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == action) {
                val extras = intent.extras
                if (extras != null) {
                    val appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                    if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                        onUpdate(
                            context,
                            AppWidgetManager.getInstance(context),
                            appWidgetIds,
                            intent
                        )
                    }
                }
            }
        }
    }


    /**
     * On update with Intent
     */
    private fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        intent: Intent
    ) {
        // There may be multiple widgets active, so update all of them
        val sharedPreferences = expensesSharedPreferencesInstance(context)
        val manager = WidgetPeriodPreferenceManager(sharedPreferences)
        val widgetPeriod = intent.getIntExtra(WidgetPeriodPreferenceManager.INTENT_EXTRA_KEY, 0)
        manager.editValue(widgetPeriod)

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
    val widgetScope = CoroutineScope(Dispatchers.Main + CoroutineExceptionHandler { _, _ -> })
    val sharedPreferences = expensesSharedPreferencesInstance(context)
    val widgetPeriodPreferenceManager = WidgetPeriodPreferenceManager(sharedPreferences)
    val currencyPreferenceManager = CurrencyPreferenceManager(sharedPreferences)
    val fromCalendar = Calendar.getInstance()
    when (widgetPeriodPreferenceManager.getValue()) {
        WidgetPeriodPreferenceManager.WidgetPeriod.DAY.ordinal -> fromCalendar.add(
            Calendar.DAY_OF_YEAR,
            -1
        )
        WidgetPeriodPreferenceManager.WidgetPeriod.WEEK.ordinal -> fromCalendar.add(
            Calendar.WEEK_OF_YEAR,
            -1
        )
        WidgetPeriodPreferenceManager.WidgetPeriod.MONTH.ordinal -> fromCalendar.add(
            Calendar.MONTH,
            -1
        )
        else -> fromCalendar.add(Calendar.MONTH, -1)
    }


    widgetScope.launch {
        val expensesData =
            async { getPeriodicTotalExpensesData(repository, fromCalendar.time, Date()) }
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                WidgetExpensesData(
                    data = expensesData.await(),
                    currency = currencyPreferenceManager.getValue()
                ),
                widgetPeriodPreferenceManager.getValue()
            )
        }
    }
}

private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    widgetExpensesData: WidgetExpensesData,
    widgetPeriodSelection: Int
) {
    val views = RemoteViews(context.packageName, R.layout.moneyfy_pro_widget)

    // Construct PendingIntents on views
    val pendingIntent = createMiniDialogPendingIntent(context)
    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)
    views.setOnClickPendingIntent(
        R.id.day_selection,
        createDataByPeriodPendingIntent(context, WidgetPeriodPreferenceManager.WidgetPeriod.DAY)
    )
    views.setOnClickPendingIntent(
        R.id.week_selection,
        createDataByPeriodPendingIntent(context, WidgetPeriodPreferenceManager.WidgetPeriod.WEEK)
    )
    views.setOnClickPendingIntent(
        R.id.month_selection,
        createDataByPeriodPendingIntent(context, WidgetPeriodPreferenceManager.WidgetPeriod.MONTH)
    )


    // Construct the progress bar
    val expenseStatusData = widgetExpensesData.data
    val currencyCode = widgetExpensesData.currency
    if (expenseStatusData.size == 2) {
        val totalVal = expenseStatusData.fold(0.0) { acc, i -> acc + i }.roundToInt()
        val profitVal = expenseStatusData[0].roundToInt()
        val spendVal = expenseStatusData[1].roundToInt()
        views.setTextViewText(R.id.profit_amount_text, context.getString(R.string.widget_income_text, "$profitVal ($currencyCode)"))
        views.setTextViewText(R.id.expense_amount_text, context.getString(R.string.widget_spend_text, "$spendVal ($currencyCode)"))
        views.setProgressBar(R.id.profit_bar, totalVal, profitVal, false)
        views.setProgressBar(R.id.expense_bar, totalVal, spendVal, false)
    }

    // Construct the period selection, highlight the selection
    val green = ContextCompat.getColor(context, R.color.teal_700)
    val white = ContextCompat.getColor(context, R.color.white)
    views.setTextColor(
        R.id.day_selection,
        if (widgetPeriodSelection == WidgetPeriodPreferenceManager.WidgetPeriod.DAY.ordinal) green else white
    )
    views.setTextColor(
        R.id.week_selection,
        if (widgetPeriodSelection == WidgetPeriodPreferenceManager.WidgetPeriod.WEEK.ordinal) green else white
    )
    views.setTextColor(
        R.id.month_selection,
        if (widgetPeriodSelection == WidgetPeriodPreferenceManager.WidgetPeriod.MONTH.ordinal) green else white
    )


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}


private fun createMiniDialogPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, AddNewExpenseActivity::class.java)

    return PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun createDataByPeriodPendingIntent(
    context: Context,
    selection: WidgetPeriodPreferenceManager.WidgetPeriod
): PendingIntent {
    val intent = Intent(context, MoneyfyProWidgetProvider::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val widgetManager = AppWidgetManager.getInstance(context)
    val componentName =
        ComponentName(context, MoneyfyProWidgetProvider::class.java)
    val widgetIds = widgetManager.getAppWidgetIds(componentName)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
    intent.putExtra(WidgetPeriodPreferenceManager.INTENT_EXTRA_KEY, selection.ordinal)

    // if request code is the same, and the flag is update current,
    // it will use the existing intent (if any) and update the extras from the new intent
    // so the previous intents will be replaced with the latest intent extras.
    return PendingIntent.getBroadcast(
        context,
        selection.ordinal,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )
}

private data class WidgetExpensesData(
    val data: List<Double>,
    val currency: String
)