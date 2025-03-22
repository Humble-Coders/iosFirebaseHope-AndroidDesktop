package org.example.iosfirebasehope.navigation.events

interface TransactionScreenEvent {
    data object OnBackClick : TransactionScreenEvent
    data class OnTransactionClick(val customerName: String, val dateTime: String) : TransactionScreenEvent
}
