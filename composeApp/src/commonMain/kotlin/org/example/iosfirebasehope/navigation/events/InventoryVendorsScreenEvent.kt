package org.example.iosfirebasehope.navigation.events

interface InventoryVendorsScreenEvent {
    data object OnBackClick : InventoryVendorsScreenEvent
    data class OnVendorClick(val vendorDetails: String) : InventoryVendorsScreenEvent
}