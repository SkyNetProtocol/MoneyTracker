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
                        binding.highestIncomeTextView.text = "Highest Income: PHP $amount"
                    }
                }
                launch {
                    viewModel.highestExpense.collect { amount ->
                        binding.highestExpenseTextView.text = "Highest Expense: PHP $amount"
                    }
                }
                launch {
                    viewModel.mostFrequentItem.collect { item ->
                        binding.mostFrequentItemTextView.text = "Most Frequent Item: ${item.ifEmpty { "None" }}"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
