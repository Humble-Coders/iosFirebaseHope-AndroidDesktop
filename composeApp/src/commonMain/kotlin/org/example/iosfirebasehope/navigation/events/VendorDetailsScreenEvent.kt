package org.example.iosfirebasehope.navigation.events

interface VendorDetailsScreenEvent {
    data object OnBackClick : VendorDetailsScreenEvent
    data class OnTransactionClick(val vendorName: String,val cylinderDetails : List<Map<String, String>>) : VendorDetailsScreenEvent
}