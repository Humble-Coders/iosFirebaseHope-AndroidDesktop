package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.InventoryScreenEvent

class InventoryScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit
) {
    fun onEvent(event: InventoryScreenEvent) {
        when (event) {
            is InventoryScreenEvent.onBackClick -> onBackClick()
        }
    }
}