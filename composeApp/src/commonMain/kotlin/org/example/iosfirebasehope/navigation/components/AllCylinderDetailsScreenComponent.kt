package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext

import org.example.iosfirebasehope.navigation.events.AllCylinderDetailsScreenEvent


class AllCylinderDetailsScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    val cylinderDetailsList: List<Map<String, String>>
) {

    fun onEvent(event: AllCylinderDetailsScreenEvent){
        when(event){
            is AllCylinderDetailsScreenEvent.OnBackClick -> onBackClick()
        }
    }
}