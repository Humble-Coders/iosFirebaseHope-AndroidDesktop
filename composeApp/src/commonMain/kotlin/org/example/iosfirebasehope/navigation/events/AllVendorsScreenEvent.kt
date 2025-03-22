package org.example.iosfirebasehope.navigation.events

interface AllVendorsScreenEvent {
    data object OnBackClick : AllVendorsScreenEvent
    data class OnVendorClick(val vendorDetails: String,val cylinderDetails : List<Map<String, String>>,val gasList: List<String>) : AllVendorsScreenEvent
}