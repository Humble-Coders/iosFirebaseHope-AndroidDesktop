package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.AddInventoryScreenEvent
import org.example.iosfirebasehope.navigation.events.CreditListScreenEvent
import org.example.iosfirebasehope.navigation.events.InventoryVendorDetailsScreenEvent

class InventoryVendorDetailsScreenComponent(
    componentContext: ComponentContext,
    val vendorDetails: String,
     private val onBackClick: () -> Unit)
{
fun onEvent(event: InventoryVendorDetailsScreenEvent) {
        when (event) {
            is InventoryVendorDetailsScreenEvent.onBackClick -> onBackClick()
        }
    }

}
