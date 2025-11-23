package com.example.loginapp.presentation.moneytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.R
import com.example.loginapp.domain.model.MoneyTransaction

class MoneyTransactionAdapter(
    private val onEditClick: (MoneyTransaction) -> Unit,
    private val onDeleteClick: (MoneyTransaction) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(TransactionDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.DateHeader -> VIEW_TYPE_HEADER
            is TransactionListItem.TransactionItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item.date)
            is TransactionListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.transaction, onEditClick, onDeleteClick)
        }
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(date: String) {
            dateTextView.text = date
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        private val editButton: MaterialButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)

        fun bind(
            transaction: MoneyTransaction,
            onEditClick: (MoneyTransaction) -> Unit,
            onDeleteClick: (MoneyTransaction) -> Unit
        ) {
            titleTextView.text = transaction.title
            amountTextView.text = "PHP ${transaction.amount}"
            typeTextView.text = transaction.type

            editButton.setOnClickListener {
                onEditClick(transaction)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(transaction)
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
        override fun areItemsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
            return when {
                oldItem is TransactionListItem.DateHeader && newItem is TransactionListItem.DateHeader ->
                    oldItem.date == newItem.date
                oldItem is TransactionListItem.TransactionItem && newItem is TransactionListItem.TransactionItem ->
                    oldItem.transaction.id == newItem.transaction.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
            return oldItem == newItem
        }
    }
}
