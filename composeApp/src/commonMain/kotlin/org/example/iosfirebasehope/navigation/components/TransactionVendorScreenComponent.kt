package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.TransactionVendorScreenEvent

class TransactionVendorScreenComponent(
    val vendorName:String,
    private val onBackClick: () -> Unit,
    val cylinderDetails: List<Map<String, String>>,
    private val onTransactionClick: (String, String) -> Unit
) {
    fun onEvent(event: TransactionVendorScreenEvent){
        when(event){
            is TransactionVendorScreenEvent.OnBackClick -> onBackClick()
            is TransactionVendorScreenEvent.OnTransactionClick->onTransactionClick(event.vendorName,event.dateTime)
        }
    }
}