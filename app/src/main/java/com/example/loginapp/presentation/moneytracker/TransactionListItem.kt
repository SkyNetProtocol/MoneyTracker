package com.example.loginapp.presentation.moneytracker

import com.example.loginapp.domain.model.MoneyTransaction

sealed class TransactionListItem {
    data class DateHeader(val date: String) : TransactionListItem()
    data class TransactionItem(val transaction: MoneyTransaction) : TransactionListItem()
}
