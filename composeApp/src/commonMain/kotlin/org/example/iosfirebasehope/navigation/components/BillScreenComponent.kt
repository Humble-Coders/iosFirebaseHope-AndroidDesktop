package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.BillScreenEvent

class BillScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    private val onIssueCylinderClick: () -> Unit,
    private val onChooseCustomerClick: (String) -> Unit,
    private val onExchangeCylinderClick: (String) -> Unit
) {
    fun onEvent(event: BillScreenEvent) {
        when (event) {
            is BillScreenEvent.OnBackClick -> onBackClick()
            is BillScreenEvent.OnIssueCylinderClick -> onIssueCylinderClick()
            is BillScreenEvent.OnChooseCustomerClick -> onChooseCustomerClick(event.customerName)
            is BillScreenEvent.OnExchangeCylinderClick -> onExchangeCylinderClick(event.customerName)
        }
    }
}