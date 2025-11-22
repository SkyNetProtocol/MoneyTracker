package com.example.loginapp.presentation.graph

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.databinding.FragmentGraphBinding
import com.example.loginapp.domain.model.IncomeExpenseSummary
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt("USER_ID", -1) ?: -1
        val category = arguments?.getString("CATEGORY", "PERSONAL") ?: "PERSONAL"
        if (userId != -1) {
            viewModel.loadSummary(userId, category)
        }

        setupChart()
        observeTransactions()
        observeSummary()
    }

    private fun setupChart() {
        val chart = binding.barChart as BarChart
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
    }

    private fun observeTransactions() {
        // No longer observing individual transactions for the chart
    }

    private fun observeSummary() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.summary.collect { summary ->
                    summary?.let {
                        binding.remainingTextView.text = "Remaining: $${it.remaining}"
                        updateChart(it)
                    }
                }
            }
        }
    }

    private fun updateChart(summary: IncomeExpenseSummary) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, summary.totalIncome.toFloat()))
        entries.add(BarEntry(1f, summary.totalExpense.toFloat()))

        val dataSet = BarDataSet(entries, "Income vs Expense")
        dataSet.colors = listOf(Color.GREEN, Color.RED)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.description.isEnabled = false
        binding.barChart.xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value.toInt()) {
                    0 -> "Income"
                    1 -> "Expense"
                    else -> ""
                }
            }
        }
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

