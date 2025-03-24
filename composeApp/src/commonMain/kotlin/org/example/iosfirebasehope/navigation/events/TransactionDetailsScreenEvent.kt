package org.example.iosfirebasehope.navigation.events

interface TransactionDetailsScreenEvent {
    data object OnBackClick : TransactionDetailsScreenEvent
    data class OnBillClick(val customerName: String,val dateTime: String) : TransactionDetailsScreenEvent

}