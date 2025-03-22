package org.example.iosfirebasehope.navigation.events

interface NewOrChooseVendorScreenEvent {
    data object OnBackClick : NewOrChooseVendorScreenEvent
    data class OnChooseVendorClick(val VendorName: String) : NewOrChooseVendorScreenEvent
    data class OnRecieveClick(val VendorName: String) : NewOrChooseVendorScreenEvent
}