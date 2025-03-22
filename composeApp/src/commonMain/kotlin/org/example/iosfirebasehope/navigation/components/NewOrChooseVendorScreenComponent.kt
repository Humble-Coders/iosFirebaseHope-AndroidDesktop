package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.NewOrChooseVendorScreenEvent

class NewOrChooseVendorScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    private val onChooseVendorClick: (String) -> Unit,
    private val onRecieveClick: (String) -> Unit
) {
    fun onEvent(event: NewOrChooseVendorScreenEvent) {
        when (event) {
            is NewOrChooseVendorScreenEvent.OnBackClick -> onBackClick()
            is NewOrChooseVendorScreenEvent.OnChooseVendorClick -> onChooseVendorClick(event.VendorName)
            is NewOrChooseVendorScreenEvent.OnRecieveClick -> onRecieveClick(event.VendorName)
        }
    }
}