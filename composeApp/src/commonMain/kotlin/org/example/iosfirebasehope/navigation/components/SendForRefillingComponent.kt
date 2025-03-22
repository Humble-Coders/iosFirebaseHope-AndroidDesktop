package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.SendForRefillingEvent

class SendForRefillingComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    val VendorName: String,
    private val onChallanClick: (String, String) -> Unit
) {
    fun onEvent(event: SendForRefillingEvent) {
        when (event) {
            is SendForRefillingEvent.OnBackClick -> onBackClick()
            is SendForRefillingEvent.OnChallanClick -> onChallanClick(event.VendorName, event.dateTime)
        }
    }
}