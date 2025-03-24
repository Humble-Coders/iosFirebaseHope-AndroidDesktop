package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.TransactionDetailsScreenEvent

class TransactionDetailsScreenComponent(
    private val onBackClick: () -> Unit,
    val customerName: String,
    val dateTime: String,
    private val onBillClick: () -> Unit
) {
    fun onEvent(event: TransactionDetailsScreenEvent){
        when(event){
            is TransactionDetailsScreenEvent.OnBackClick -> onBackClick()
            is TransactionDetailsScreenEvent.OnBillClick -> onBillClick()
        }
    }
}