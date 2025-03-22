package org.example.iosfirebasehope.navigation.events

interface ReturnCylinderScreenEvent {
    data object OnBackClick : ReturnCylinderScreenEvent
    data object OnReturnCylinderClick : ReturnCylinderScreenEvent
    data class OnChooseCustomerClick(val customerName: String) : ReturnCylinderScreenEvent
    data class OnConfirmClick(val customerName: String, val dateTime: String) : ReturnCylinderScreenEvent
}