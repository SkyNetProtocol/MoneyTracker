package com.example.loginapp.presentation.calendar

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
import com.example.loginapp.databinding.FragmentCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginapp.presentation.moneytracker.MoneyTransactionAdapter

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var adapter: MoneyTransactionAdapter

    private val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt("USER_ID", -1) ?: -1
        val category = arguments?.getString("CATEGORY", "PERSONAL") ?: "PERSONAL"

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.initialize(userId, category)
        setupRecyclerView()
        setupCalendar()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MoneyTransactionAdapter(
            onEditClick = {
                Toast.makeText(requireContext(), "Manage modifying operations (edit/delete) in the Money Tracker tab", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = {
                Toast.makeText(requireContext(), "Manage modifying operations (edit/delete) in the Money Tracker tab", Toast.LENGTH_SHORT).show()
            },
            onPendingClick = { transaction, isChecked ->
                viewModel.togglePendingLiquidation(transaction, isChecked)
            }
        )
        binding.transactionsRecyclerView.adapter = adapter
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupCalendar() {
        // Set today as selected by default
        binding.calendarView.selectedDate = CalendarDay.today()
        updateSelectedDateText(System.currentTimeMillis())
        
        // Load data for today on initial setup
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        viewModel.selectDate(calendar.timeInMillis)

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val calendar = Calendar.getInstance()
            // MaterialCalendarView uses actual year, not year offset
            calendar.set(Calendar.YEAR, date.year)
            calendar.set(Calendar.MONTH, date.month - 1) // MaterialCalendarView months are 1-based
            calendar.set(Calendar.DAY_OF_MONTH, date.day)
            // Set time to start of day (midnight)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val selectedDateMillis = calendar.timeInMillis
            viewModel.selectDate(selectedDateMillis)
            updateSelectedDateText(selectedDateMillis)
        }
    }

    private fun updateSelectedDateText(dateMillis: Long) {
        binding.selectedDateText.text = dateFormat.format(Date(dateMillis))
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.totalIncome.collect { income ->
                        binding.totalIncomeText.text = String.format("PHP %.2f", income)
                    }
                }
                launch {
                    viewModel.totalExpense.collect { expense ->
                        binding.totalExpenseText.text = String.format("PHP %.2f", expense)
                    }
                }
                launch {
                    viewModel.netBalance.collect { balance ->
                        binding.netBalanceText.text = String.format("PHP %.2f", balance)
                    }
                }
                launch {
                    viewModel.transactions.collect { transactions ->
                        val listItems = mutableListOf<com.example.loginapp.presentation.moneytracker.TransactionListItem>()
                        if (transactions.isNotEmpty()) {
                            val formattedDate = dateFormat.format(Date(viewModel.selectedDate.value))
                            listItems.add(com.example.loginapp.presentation.moneytracker.TransactionListItem.DateHeader(formattedDate))
                            listItems.addAll(transactions.map { 
                                com.example.loginapp.presentation.moneytracker.TransactionListItem.TransactionItem(it) 
                            })
                        }
                        adapter.submitList(listItems)
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
