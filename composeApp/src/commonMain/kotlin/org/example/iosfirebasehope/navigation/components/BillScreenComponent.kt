package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.BillScreenEvent

class BillScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    private val onIssueCylinderClick: () -> Unit
) {
    fun onEvent(event: BillScreenEvent) {
        when (event) {
            is BillScreenEvent.OnBackClick -> onBackClick()
            is BillScreenEvent.OnIssueCylinderClick -> onIssueCylinderClick()
        }
    }
}