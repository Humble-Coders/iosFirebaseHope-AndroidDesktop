package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.NewIssueCylinderScreenEvent

class NewIssueCylinderScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    val customerName: String,
    private val onBillClick: (String, String) -> Unit
) {
    fun onEvent(event: NewIssueCylinderScreenEvent) {
        when (event) {
            is NewIssueCylinderScreenEvent.OnBackClick -> onBackClick()
            is NewIssueCylinderScreenEvent.OnBillClick -> onBillClick(event.customerName, event.dateTime)
        }
    }
}