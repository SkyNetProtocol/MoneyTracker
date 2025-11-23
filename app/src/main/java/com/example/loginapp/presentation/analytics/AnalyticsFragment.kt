package com.example.loginapp.presentation.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loginapp.databinding.FragmentAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt("USER_ID", -1) ?: -1
        val category = arguments?.getString("CATEGORY", "PERSONAL") ?: "PERSONAL"
        if (userId != -1) {
            viewModel.loadAnalytics(userId, category)
        }

        observeAnalytics()
    }

    private fun observeAnalytics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.highestIncome.collect { amount ->
                        binding.highestIncomeTextView.text = "PHP ${String.format("%.2f", amount)}"
                    }
                }
                launch {
                    viewModel.highestExpense.collect { amount ->
                        binding.highestExpenseTextView.text = "PHP ${String.format("%.2f", amount)}"
                    }
                }
                launch {
                    viewModel.mostFrequentItem.collect { item ->
                        binding.mostFrequentItemTextView.text = item.ifEmpty { "None" }
                    }
                }
                launch {
                    viewModel.expenseByCategory.collect { expenseMap ->
                        setupPieChart(expenseMap)
                    }
                }
                launch {
                    viewModel.incomeVsExpense.collect { (income, expense) ->
                        setupBarChart(income, expense)
                    }
                }
            }
        }
    }

    private fun setupPieChart(expenseMap: Map<String, Double>) {
        val pieChart = binding.expensePieChart
        
        val entries = expenseMap.map { (category, amount) ->
            com.github.mikephil.charting.data.PieEntry(amount.toFloat(), category)
        }

        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "Expenses")
        dataSet.colors = com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = android.graphics.Color.WHITE

        val data = com.github.mikephil.charting.data.PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Expenses"
        pieChart.setCenterTextSize(14f)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart(income: Double, expense: Double) {
        val barChart = binding.incomeExpenseBarChart

        val entries = listOf(
            com.github.mikephil.charting.data.BarEntry(0f, income.toFloat()),
            com.github.mikephil.charting.data.BarEntry(1f, expense.toFloat())
        )

        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Income vs Expense")
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#4CAF50"), // Green for Income
            android.graphics.Color.parseColor("#F44336")  // Red for Expense
        )
        dataSet.valueTextSize = 12f

        val data = com.github.mikephil.charting.data.BarData(dataSet)
        barChart.data = data
        barChart.description.isEnabled = false
        
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(listOf("Income", "Expense"))
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
