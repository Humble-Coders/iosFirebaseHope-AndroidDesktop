package org.example.iosfirebasehope.navigation.events

interface CustomerDetailsScreenEvent {
    data object OnBackClick : CustomerDetailsScreenEvent
    data class OnTransactionClick(val customerName: String,val cylinderDetails : List<Map<String, String>>) : CustomerDetailsScreenEvent
}