package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext

import org.example.iosfirebasehope.navigation.events.AllCylinderDetailsScreenEvent
import org.example.iosfirebasehope.navigation.events.GasVolumeScreenEvent


class AllCylinderDetailsScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    val cylinderDetailsList: List<Map<String, String>>,
    private val onCylinderClick: (Map<String, String>) -> Unit
) {

    fun onEvent(event: AllCylinderDetailsScreenEvent){
        when(event){
            is AllCylinderDetailsScreenEvent.OnBackClick -> onBackClick()
            is AllCylinderDetailsScreenEvent.OnCylinderClick -> onCylinderClick(event.currentCylinderDetails)
        }
    }
}