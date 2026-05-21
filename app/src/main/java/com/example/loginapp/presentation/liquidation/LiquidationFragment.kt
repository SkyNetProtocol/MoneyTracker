package com.example.loginapp.presentation.liquidation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginapp.databinding.FragmentLiquidationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LiquidationFragment : Fragment() {

    private var _binding: FragmentLiquidationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LiquidationViewModel by viewModels()
    private lateinit var adapter: PendingLiquidationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiquidationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments
        val userId = arguments?.getInt("USER_ID", -1) ?: -1
        val category = arguments?.getString("CATEGORY", "PERSONAL") ?: "PERSONAL"

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadPendingTransactions(userId, category)
    }

    private fun setupRecyclerView() {
        adapter = PendingLiquidationAdapter { transaction ->
            viewModel.liquidateTransaction(transaction)
        }
        binding.liquidationRecyclerView.adapter = adapter
        binding.liquidationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.liquidateAllButton.setOnClickListener {
            viewModel.liquidateAll()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pendingTransactions.collect { list ->
                        adapter.submitList(list)
                        if (list.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.liquidationRecyclerView.visibility = View.GONE
                            binding.liquidateAllButton.isEnabled = false
                            binding.liquidateAllButton.alpha = 0.5f
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.liquidationRecyclerView.visibility = View.VISIBLE
                            binding.liquidateAllButton.isEnabled = true
                            binding.liquidateAllButton.alpha = 1.0f
                        }
                    }
                }

                launch {
                    viewModel.pendingTotal.collect { total ->
                        binding.pendingTotalTextView.text = String.format("₱ %,.2f", total)
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
