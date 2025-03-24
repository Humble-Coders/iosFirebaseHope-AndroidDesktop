package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.CreditListScreenEvent
import org.example.iosfirebasehope.navigation.events.DailyBookScreenEvent

class DailyBookScreenComponent(
    componentContext: ComponentContext,
     private val onBackClick: () -> Unit,
    private val onTransactionClick: (String, String) -> Unit


)

{
fun onEvent(event: DailyBookScreenEvent) {
        when (event) {
            is DailyBookScreenEvent.OnBackClick -> onBackClick()
            is DailyBookScreenEvent.OnTransactionClick -> onTransactionClick(event.customerName, event.dateTime)
        }
    }

}
