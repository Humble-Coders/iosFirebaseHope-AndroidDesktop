package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.AddInventoryScreenEvent
import org.example.iosfirebasehope.navigation.events.CreditListScreenEvent

class AddInventoryScreenComponent(
    componentContext: ComponentContext,
     private val onBackClick: () -> Unit)
{
fun onEvent(event: AddInventoryScreenEvent) {
        when (event) {
            is AddInventoryScreenEvent.onBackClick -> onBackClick()
        }
    }

}
