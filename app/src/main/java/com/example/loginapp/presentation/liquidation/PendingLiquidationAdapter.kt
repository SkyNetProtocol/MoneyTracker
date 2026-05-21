package com.example.loginapp.presentation.liquidation

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PendingLiquidationAdapter(
    private val onLiquidateClick: (MoneyTransaction) -> Unit
) : ListAdapter<MoneyTransaction, PendingLiquidationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_liquidation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onLiquidateClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val categoryAndDateTextView: TextView = itemView.findViewById(R.id.categoryAndDateTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val liquidateButton: MaterialButton = itemView.findViewById(R.id.liquidateButton)

        fun bind(
            transaction: MoneyTransaction,
            onLiquidateClick: (MoneyTransaction) -> Unit
        ) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date(transaction.timestamp))

            titleTextView.text = transaction.title
            categoryAndDateTextView.text = "${transaction.category} • ${dateStr}"
            amountTextView.text = "PHP ${transaction.amount}"

            liquidateButton.setOnClickListener {
                onLiquidateClick(transaction)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MoneyTransaction>() {
        override fun areItemsTheSame(oldItem: MoneyTransaction, newItem: MoneyTransaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoneyTransaction, newItem: MoneyTransaction): Boolean {
            return oldItem == newItem
        }
    }
}
