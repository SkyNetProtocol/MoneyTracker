package com.example.loginapp.presentation.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.R
import com.example.loginapp.domain.model.MoneyTransaction
import com.google.android.material.button.MaterialButton

class CalendarTransactionAdapter(
    private val onEditClick: (MoneyTransaction) -> Unit,
    private val onDeleteClick: (MoneyTransaction) -> Unit
) : ListAdapter<MoneyTransaction, CalendarTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position), onEditClick, onDeleteClick)
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

    class TransactionDiffCallback : DiffUtil.ItemCallback<MoneyTransaction>() {
        override fun areItemsTheSame(oldItem: MoneyTransaction, newItem: MoneyTransaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoneyTransaction, newItem: MoneyTransaction): Boolean {
            return oldItem == newItem
        }
    }
}
