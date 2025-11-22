package com.example.loginapp.presentation.summary

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loginapp.databinding.FragmentDateSummaryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DateSummaryFragment : Fragment() {

    private var _binding: FragmentDateSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountSummaryViewModel by viewModels()
    private var userId: Int = -1
    private var date: String = ""

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_DATE = "date"

        fun newInstance(userId: Int, date: String): DateSummaryFragment {
            val fragment = DateSummaryFragment()
            val args = Bundle()
            args.putInt(ARG_USER_ID, userId)
            args.putString(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_USER_ID)
            date = it.getString(ARG_DATE) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDateSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dateTitleTextView.text = date

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDateSummary.collect { summary ->
                summary?.let {
                    binding.totalIncomeTextView.text = "PHP ${it.totalIncome}"
                    binding.totalExpenseTextView.text = "PHP ${it.totalExpense}"
                    binding.remainingTextView.text = "PHP ${it.remaining}"

                    if (it.remaining >= 0) {
                        binding.remainingTextView.setTextColor(Color.GREEN)
                    } else {
                        binding.remainingTextView.setTextColor(Color.RED)
                    }
                }
            }
        }

        if (userId != -1 && date.isNotEmpty()) {
            val category = arguments?.getString("CATEGORY", "PERSONAL") ?: "PERSONAL"
            viewModel.loadDateSummary(userId, category, date)
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            // Navigate to HomeFragment (Money Tracker tab)
            // We need to clear the back stack to avoid weird navigation states
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            
            val homeFragment = com.example.loginapp.presentation.home.HomeFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", userId)
                }
            }
            
            parentFragmentManager.beginTransaction()
                .replace(com.example.loginapp.R.id.fragment_container_view, homeFragment)
                .commit()
            
            // Note: HomeFragment defaults to the first tab (Money Tracker), which is what we want.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
