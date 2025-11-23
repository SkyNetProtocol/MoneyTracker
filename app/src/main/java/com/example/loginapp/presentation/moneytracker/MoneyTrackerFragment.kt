package com.example.loginapp.presentation.moneytracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginapp.R
import com.example.loginapp.databinding.FragmentMoneyTrackerBinding
import com.example.loginapp.domain.model.MoneyTransaction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoneyTrackerFragment : Fragment() {

    private var _binding: FragmentMoneyTrackerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoneyTrackerViewModel by viewModels()
    private val categoryViewModel: com.example.loginapp.presentation.category.CategoryViewModel by viewModels()
    private lateinit var adapter: MoneyTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoneyTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeTransactions()

        // Get userId and category from arguments
        val userId = arguments?.getInt("USER_ID", -1) ?: -1
        val category = arguments?.getString("CATEGORY", "PERSONAL") ?: "PERSONAL"
        if (userId == -1) {
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.loadTransactions(userId, category)
    }

    private fun setupRecyclerView() {
        adapter = MoneyTransactionAdapter(
            onEditClick = { transaction ->
                showEditTransactionDialog(transaction)
            },
            onDeleteClick = { transaction ->
                showDeleteConfirmationDialog(transaction)
            }
        )
        binding.transactionsRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.layoutManager = layoutManager

        binding.transactionsRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun setupFab() {
        binding.addTransactionFab.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val typeRadioGroup = dialogView.findViewById<android.widget.RadioGroup>(R.id.typeRadioGroup)
        val incomeRadioButton = dialogView.findViewById<android.widget.RadioButton>(R.id.incomeRadioButton)
        val expenseRadioButton = dialogView.findViewById<android.widget.RadioButton>(R.id.expenseRadioButton)

        // Set default selection
        expenseRadioButton.isChecked = true

        // Category spinner
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val categoryAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Load expense categories by default
        var selectedCategoryId: Int? = null
        categoryViewModel.loadCategoriesByType("EXPENSE")
        
        // Observe expense categories
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.expenseCategories.collect { categories ->
                categoryAdapter.clear()
                categoryAdapter.addAll(categories.map { "${it.icon} ${it.name}" })
                categoryAdapter.notifyDataSetChanged()
            }
        }

        // Update categories when type changes
        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.incomeRadioButton -> "INCOME"
                R.id.expenseRadioButton -> "EXPENSE"
                else -> "EXPENSE"
            }
            categoryViewModel.loadCategoriesByType(type)
            
            viewLifecycleOwner.lifecycleScope.launch {
                val categories = if (type == "INCOME") {
                    categoryViewModel.incomeCategories.value
                } else {
                    categoryViewModel.expenseCategories.value
                }
                categoryAdapter.clear()
                categoryAdapter.addAll(categories.map { "${it.icon} ${it.name}" })
                categoryAdapter.notifyDataSetChanged()
            }
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialog)
            .setTitle("Add Transaction")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val title = titleEditText.text.toString()
                val amountStr = amountEditText.text.toString()
                val type = when (typeRadioGroup.checkedRadioButtonId) {
                    R.id.incomeRadioButton -> "INCOME"
                    R.id.expenseRadioButton -> "EXPENSE"
                    else -> "INCOME"
                }

                if (title.isNotBlank() && amountStr.isNotBlank()) {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null) {
                        // Get selected category ID
                        val selectedPosition = categorySpinner.selectedItemPosition
                        val categories = if (type == "INCOME") {
                            categoryViewModel.incomeCategories.value
                        } else {
                            categoryViewModel.expenseCategories.value
                        }
                        selectedCategoryId = if (selectedPosition >= 0 && selectedPosition < categories.size) {
                            categories[selectedPosition].id
                        } else null
                        
                        dialog.dismiss() // Dismiss BEFORE operation
                        viewModel.addTransaction(title, amount, type, selectedCategoryId)
                    } else {
                        Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transactions.collect { transactions ->
                        adapter.submitList(transactions)
                    }
                }
                launch {
                    viewModel.operationState.collect { state ->
                        handleOperationState(state)
                    }
                }
            }
        }
    }

    private var loadingDialog: androidx.appcompat.app.AlertDialog? = null

    private fun handleOperationState(state: OperationState) {
        when (state) {
            is OperationState.Idle -> {
                loadingDialog?.dismiss()
            }
            is OperationState.Loading -> {
                loadingDialog?.dismiss() // Dismiss existing before creating new
                loadingDialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialog)
                    .setMessage("Processing...")
                    .setCancelable(false)
                    .create()
                loadingDialog?.show()
            }
            is OperationState.Success -> {
                loadingDialog?.dismiss()
                MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialog)
                    .setTitle("Success")
                    .setMessage(state.message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.resetOperationState()
                    }
                    .show()
            }
            is OperationState.Error -> {
                loadingDialog?.dismiss()
                MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialog)
                    .setTitle("Error")
                    .setMessage(state.message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.resetOperationState()
                    }
                    .show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(transaction: MoneyTransaction) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialog)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTransactionDialog(transaction: MoneyTransaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.typeRadioGroup)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

        // Pre-fill with existing data
        titleEditText.setText(transaction.title)
        amountEditText.setText(transaction.amount.toString())
        
        when (transaction.type) {
            "INCOME" -> typeRadioGroup.check(R.id.incomeRadioButton)
            "EXPENSE" -> typeRadioGroup.check(R.id.expenseRadioButton)
        }

        val categoryAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Load categories for the current type
        categoryViewModel.loadCategoriesByType(transaction.type)
        viewLifecycleOwner.lifecycleScope.launch {
            val categories = if (transaction.type == "INCOME") {
                categoryViewModel.incomeCategories.value
            } else {
                categoryViewModel.expenseCategories.value
            }
            categoryAdapter.clear()
            categoryAdapter.addAll(categories.map { "${it.icon} ${it.name}" })
            categoryAdapter.notifyDataSetChanged()
            
            // Set current category
            val currentCategoryIndex = categories.indexOfFirst { it.name == transaction.category }
            if (currentCategoryIndex >= 0) {
                categorySpinner.setSelection(currentCategoryIndex)
            }
        }

        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.incomeRadioButton -> "INCOME"
                R.id.expenseRadioButton -> "EXPENSE"
                else -> "EXPENSE"
            }
            categoryViewModel.loadCategoriesByType(type)
            
            viewLifecycleOwner.lifecycleScope.launch {
                val categories = if (type == "INCOME") {
                    categoryViewModel.incomeCategories.value
                } else {
                    categoryViewModel.expenseCategories.value
                }
                categoryAdapter.clear()
                categoryAdapter.addAll(categories.map { "${it.icon} ${it.name}" })
                categoryAdapter.notifyDataSetChanged()
            }
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialog)
            .setTitle("Edit Transaction")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val title = titleEditText.text.toString()
                val amountStr = amountEditText.text.toString()
                val type = when (typeRadioGroup.checkedRadioButtonId) {
                    R.id.incomeRadioButton -> "INCOME"
                    R.id.expenseRadioButton -> "EXPENSE"
                    else -> transaction.type
                }

                val selectedCategoryText = categorySpinner.selectedItem?.toString() ?: ""
                val categoryName = selectedCategoryText.substringAfter(" ").trim()

                if (title.isNotEmpty() && amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val updatedTransaction = transaction.copy(
                        title = title,
                        amount = amount,
                        type = type,
                        category = categoryName
                    )
                    dialog.dismiss() // Dismiss BEFORE operation
                    viewModel.updateTransaction(updatedTransaction)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        _binding = null
    }
}
