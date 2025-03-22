package org.example.iosfirebasehope.navigation.events

interface BillScreenEvent {
    data object OnBackClick : BillScreenEvent
    data object OnIssueCylinderClick : BillScreenEvent
    data class OnChooseCustomerClick (val customerName: String) : BillScreenEvent
    data class OnExchangeCylinderClick (val customerName: String) : BillScreenEvent
}