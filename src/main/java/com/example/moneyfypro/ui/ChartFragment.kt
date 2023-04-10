package com.example.moneyfypro.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.toAmountFormat
import com.example.moneyfypro.databinding.FragmentChartBinding
import com.example.moneyfypro.model.ExpensesViewModel
import com.example.moneyfypro.model.ExpensesViewState
import com.example.moneyfypro.model.SettingViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [ChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChartFragment : Fragment() {
    private lateinit var binding: FragmentChartBinding
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChartBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensesViewModel.expensesViewState.observe(viewLifecycleOwner) { displayValues() }
        settingViewModel.saveCurrency.observe(viewLifecycleOwner) { displayValues() }
    }

    private fun displayValues() {
        val currencyCode = settingViewModel.saveCurrency.value?.currencyCode ?: ""
        binding.apply {
            earningValue.text = Expense.toAmountFormat(expensesViewModel.totalEarning(), currencyCode)
            spendingValue.text = Expense.toAmountFormat(expensesViewModel.totalSpending(), currencyCode)
        }
        createLineChart(currencyCode)
    }


    /**
     * Create line chart
     */
    private fun createLineChart(currencyCode: String) {
        val lineChart = binding.lineChart
        lineChart.apply {
            animateX(300, Easing.EaseInOutBounce)
            description.isEnabled = false

            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = ExpensesChartAxisFormatter()
            xAxis.labelCount = 5
            minDate()?.let { xAxis.axisMaximum = it.time.toFloat() }
            maxDate()?.let { xAxis.axisMaximum = it.time.toFloat() }

            axisRight.isEnabled = false
            extraRightOffset = 30f

            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.textSize = 15F
            legend.form = Legend.LegendForm.LINE

            val dataGroup = ArrayList<ILineDataSet>()
            val dataset = getLineDataSet(currencyCode)
            dataset.lineWidth = 2f
            dataset.setDrawValues(false)
            dataset.setDrawCircles(false)
            dataset.mode = LineDataSet.Mode.LINEAR
            dataset.color = ContextCompat.getColor(this.context, R.color.blush)
            dataGroup.add(dataset)
            data = LineData(dataGroup)
            invalidate()
        }
    }

    /**
     * Add data to line chart
     */
    private fun getLineDataSet(currencyCode: String): LineDataSet {
        var res = dateExpensesMap().entries.map {
            val date = it.key.time.toFloat()
            val amount = it.value.toFloat()
            Entry(date, amount)
        }

        res = res.sortedBy { it.x }

        var cum = 0f;
        for (r in res) {
            r.y += cum
            cum = r.y
        }
        return LineDataSet(res.sortedBy { it.x }, "Expenses ($currencyCode)")
    }


    /**
     * Expenses per day
     */
    private fun dateExpensesMap(): Map<Date, Double> {
        val res = mutableMapOf<Date, Double>()
        val expensesList = expensesViewModel.expensesViewState.value?.expensesList ?: return res

        for (expense in expensesList) res[expense.date] = expense.amount + (res[expense.date] ?: 0.0)

        return res
    }


    /**
     * Get minimum date
     */
    private fun minDate(): Date? {
        return expensesViewModel.minDate()
    }


    /**
     * Get maximum date
     */
    private fun maxDate(): Date? {
        return expensesViewModel.maxDate()
    }




    private inner class ExpensesChartAxisFormatter(): IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return  SimpleDateFormat("MMM dd", Locale.US).format(Date(value.toLong()))
        }


    }
}