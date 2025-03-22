package org.example.iosfirebasehope.navigation.events

interface TransactionVendorScreenEvent {
    data object OnBackClick : TransactionVendorScreenEvent
    data class OnTransactionClick(val vendorName: String,val dateTime: String) : TransactionVendorScreenEvent
}