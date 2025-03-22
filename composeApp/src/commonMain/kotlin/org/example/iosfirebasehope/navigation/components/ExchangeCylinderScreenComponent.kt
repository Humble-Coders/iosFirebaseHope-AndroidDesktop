package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.ExchangeCylinderScreenEvent

class ExchangeCylinderScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    val customerName: String,
    private val onBillClick: (String, String) -> Unit
) {
    fun onEvent(event: ExchangeCylinderScreenEvent) {
        when (event) {
            is ExchangeCylinderScreenEvent.OnBackClick -> onBackClick()
            is ExchangeCylinderScreenEvent.OnBillClick -> onBillClick(event.customerName, event.dateTime)
        }
    }
}