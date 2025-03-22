package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.NotificationScreenEvent

class NotificationScreenComponent(
    private val onBackClick: () -> Unit,
    val cylinderDetails: List<Map<String, String>>
) {
    fun onEvent(event: NotificationScreenEvent){
        when(event){
            is NotificationScreenEvent.OnBackClick -> onBackClick()
        }
    }
}