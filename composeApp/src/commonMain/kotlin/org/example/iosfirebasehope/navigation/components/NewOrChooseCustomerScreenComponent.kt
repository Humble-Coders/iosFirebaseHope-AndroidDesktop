package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.NewOrChooseCustomerScreenEvent

class NewOrChooseCustomerScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
) {
    fun onEvent(event: NewOrChooseCustomerScreenEvent) {
        when (event) {
            is NewOrChooseCustomerScreenEvent.OnBackClick -> onBackClick()
        }
    }
}