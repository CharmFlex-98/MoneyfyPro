package com.example.moneyfypro.ui

import android.graphics.Color
import android.graphics.Color.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.moneyfypro.R
import com.example.moneyfypro.databinding.FragmentSummaryBinding
import com.example.moneyfypro.model.ExpensesViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [SummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SummaryFragment : Fragment() {
    private lateinit var binding: FragmentSummaryBinding
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart = binding.pieChart
        setupPieChart(pieChart)

        expensesViewModel.apply {
            expensesViewState.observe(viewLifecycleOwner) {
                if (!this.hasExpenses()) {
                    binding.noDataText.visibility = View.VISIBLE
                    binding.pieChart.visibility = View.GONE
                    return@observe
                }
                binding.noDataText.visibility = View.GONE
                binding.pieChart.visibility = View.VISIBLE
                loadPieData(pieChart, getExpensesRatioByCategory())
            }
        }
    }


    /**
     * Setup Pie chart view
     */
    private fun setupPieChart(chart: PieChart) {
        chart.apply {
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(BLACK)
            centerText = "Summary"
            description.isEnabled = false
        }

        val legend = chart.legend
        legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            isWordWrapEnabled = true
            setDrawInside(false)
            isEnabled = true
        }
    }


    /**
     * Load pie data
     */
    private fun loadPieData(chart: PieChart, expensesRatioByCategory: Map<String, Double>) {
        val data = mutableListOf<PieEntry>()
        expensesRatioByCategory.forEach {
            data.add(PieEntry(it.value.toFloat(), it.key))
        }
        val colors = resources.getIntArray(R.array.palette_colors)
        val dataset = PieDataSet(data, "")
        dataset.setColors(colors, 255)
        dataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.selectionShift = 5f
//        dataset.iconsOffset = MPPointF(0f, 40f)
        dataset.valueLinePart1OffsetPercentage = 100f
        dataset.valueLinePart1Length = 0.5f
        dataset.valueLinePart2Length = 0.2f


        val pieData = PieData(dataset)
        pieData.setDrawValues(true)
        pieData.setValueFormatter(PercentFormatter(binding.pieChart))
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.BLACK)
        pieData.setValueTextSize(10f)

        chart.data = pieData
        chart.setDrawEntryLabels(false)
        chart.setExtraOffsets(25f, 0f, 25f, 0f)
        chart.invalidate()
        chart.animateY(1400, Easing.EaseInOutQuad)
    }
}