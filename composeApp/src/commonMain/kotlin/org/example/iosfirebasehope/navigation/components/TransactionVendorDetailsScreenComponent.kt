package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.TransactionVendorDetailsScreenEvent

class TransactionVendorDetailsScreenComponent(
    private val onBackClick: () -> Unit,
    val vendorName: String,
    val dateTime: String
) {
    fun onEvent(event: TransactionVendorDetailsScreenEvent){
        when(event){
            is TransactionVendorDetailsScreenEvent.OnBackClick -> onBackClick()
        }
    }
}