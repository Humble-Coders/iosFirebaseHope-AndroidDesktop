package org.example.iosfirebasehope.navigation.events

interface ExchangeCylinderScreenEvent {
    data object OnBackClick : ExchangeCylinderScreenEvent
    data class OnBillClick (val customerName: String,val dateTime: String) : ExchangeCylinderScreenEvent
}