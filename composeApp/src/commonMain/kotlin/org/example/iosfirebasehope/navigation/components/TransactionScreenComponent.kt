package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.TransactionScreenEvent

class TransactionScreenComponent(
    val customerName:String,
    private val onBackClick: () -> Unit,
    val cylinderDetails: List<Map<String, String>>,
    private val onTransactionClick: (String, String) -> Unit
) {
    fun onEvent(event: TransactionScreenEvent){
        when(event){
            is TransactionScreenEvent.OnBackClick -> onBackClick()
            is TransactionScreenEvent.OnTransactionClick->onTransactionClick(event.customerName,event.dateTime)
        }
    }
}