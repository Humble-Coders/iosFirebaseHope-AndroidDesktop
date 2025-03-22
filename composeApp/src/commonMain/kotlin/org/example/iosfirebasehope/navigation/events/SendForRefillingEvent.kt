package org.example.iosfirebasehope.navigation.events

interface SendForRefillingEvent {
    data object OnBackClick : SendForRefillingEvent
    data class OnChallanClick(val VendorName: String,val dateTime: String) : SendForRefillingEvent

}