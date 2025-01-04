package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.AddCylinderScreenEvent
import org.example.iosfirebasehope.navigation.events.HomeScreenEvent

class AddCylinderScreenComponent (
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit

){
    fun onEvent(event: AddCylinderScreenEvent){
        when(event){
            is AddCylinderScreenEvent.onBackClick -> onBackClick()
        }
    }
}