package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.CreditListScreenEvent
import org.example.iosfirebasehope.navigation.events.CurrentlyIssuedScreenEvent

class CurrentlyIssuedScreenComponent(
    componentContext: ComponentContext,
     private val onBackClick: () -> Unit,
    val cylinderDetailList : List<Map<String,String>>
)
{
fun onEvent(event: CurrentlyIssuedScreenEvent) {
        when (event) {
            is CurrentlyIssuedScreenEvent.OnBackClick -> onBackClick()
        }
    }

}
