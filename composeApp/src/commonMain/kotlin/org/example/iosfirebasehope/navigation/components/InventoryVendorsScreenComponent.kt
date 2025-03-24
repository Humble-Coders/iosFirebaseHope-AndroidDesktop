package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.AllVendorsScreenEvent
import org.example.iosfirebasehope.navigation.events.InventoryVendorsScreenEvent

class InventoryVendorsScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    private val onVendorClick: (String) -> Unit
) {
    fun onEvent(event: InventoryVendorsScreenEvent){
        when(event){
            is InventoryVendorsScreenEvent.OnBackClick -> onBackClick()
            is InventoryVendorsScreenEvent.OnVendorClick -> onVendorClick(event.vendorDetails)
        }
    }
}