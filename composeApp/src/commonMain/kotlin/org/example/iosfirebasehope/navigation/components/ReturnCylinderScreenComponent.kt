package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.ReturnCylinderScreenEvent

class ReturnCylinderScreenComponent (
    private val onBackClick: () -> Unit,
    private val onReturnCylinderClick: () -> Unit,
    private val onChooseCustomerClick: (String) -> Unit,
    val customerName: String,
    private val onConfirmClick: (String, String) -> Unit
){
    fun onEvent(event: ReturnCylinderScreenEvent) {
        when (event) {
            is ReturnCylinderScreenEvent.OnBackClick -> onBackClick()
            is ReturnCylinderScreenEvent.OnReturnCylinderClick -> onReturnCylinderClick()
            is ReturnCylinderScreenEvent.OnChooseCustomerClick -> onChooseCustomerClick(event.customerName)
            is ReturnCylinderScreenEvent.OnConfirmClick -> onConfirmClick(event.customerName, event.dateTime)
        }
    }
}