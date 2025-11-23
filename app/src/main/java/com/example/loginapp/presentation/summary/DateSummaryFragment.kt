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
    private var category: String = "PERSONAL"

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_DATE = "date"
        private const val ARG_CATEGORY = "category"

        fun newInstance(userId: Int, date: String, category: String = "PERSONAL"): DateSummaryFragment {
            val fragment = DateSummaryFragment()
            val args = Bundle()
            args.putInt(ARG_USER_ID, userId)
            args.putString(ARG_DATE, date)
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_USER_ID)
            date = it.getString(ARG_DATE) ?: ""
            category = it.getString(ARG_CATEGORY) ?: "PERSONAL"
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
            viewModel.loadDateSummary(userId, category, date)
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            // Navigate to HomeFragment with the correct category
            requireActivity().supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            
            val currentCategory = category
            val homeFragment = com.example.loginapp.presentation.home.HomeFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", userId)
                    putString("CATEGORY", currentCategory)
                }
            }
            
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.example.loginapp.R.id.fragment_container_view, homeFragment)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
