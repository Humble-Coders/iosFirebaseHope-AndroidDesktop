package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.CreditListScreenEvent

class CreditListScreenComponent(
    componentContext: ComponentContext,
     private val onBackClick: () -> Unit)
{
fun onEvent(event: CreditListScreenEvent) {
        when (event) {
            is CreditListScreenEvent.OnBackClick -> onBackClick()
        }
    }

}
