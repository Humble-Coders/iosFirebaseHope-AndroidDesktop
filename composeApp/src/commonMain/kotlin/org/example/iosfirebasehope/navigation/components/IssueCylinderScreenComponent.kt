package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.IssueCylinderScreenEvent

class IssueCylinderScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit
) {
    fun onEvent(event: IssueCylinderScreenEvent) {
        when (event) {
            is IssueCylinderScreenEvent.OnBackClick -> onBackClick()
        }
    }
}