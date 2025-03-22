package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.CurrentCylinderDetailsScreenEvent

class CurrentCylinderDetailsComponent (
    val currentCylinderDetails: Map<String, String>,
    private val onBackClick: () -> Unit,
    private val onGoToHomeClick: () -> Unit
){
    fun onEvent(event: CurrentCylinderDetailsScreenEvent){
        when(event){
            is CurrentCylinderDetailsScreenEvent.OnBackClick -> onBackClick()
            is CurrentCylinderDetailsScreenEvent.OnGoToHomeClick -> onGoToHomeClick()
        }
    }
}