package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.CustomerDetailsScreenEvent

class CustomerDetailsScreenComponent(
    val customerDetails:String,
    private val onBackClick: () -> Unit,
    val gasList: List<String>,
    val cylinderDetailsList: List<Map<String, String>>,
    private val onTransactionClick: (String,List<Map<String, String>>) -> Unit
) {
    fun onEvent(event: CustomerDetailsScreenEvent){
        when(event){
            is CustomerDetailsScreenEvent.OnBackClick -> onBackClick()
            is CustomerDetailsScreenEvent.OnTransactionClick -> onTransactionClick(event.customerName, event.cylinderDetails)
        }
    }
}