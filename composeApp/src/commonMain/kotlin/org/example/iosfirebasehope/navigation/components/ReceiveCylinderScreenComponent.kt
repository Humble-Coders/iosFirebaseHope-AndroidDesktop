package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.ReceiveCylinderScreenEvent

class ReceiveCylinderScreenComponent(
    componentContext: ComponentContext,
    val VendorName:String,
    private val onBackClick: () -> Unit,
    private val onConfirmClick: () -> Unit
){
    fun onEvent(event: ReceiveCylinderScreenEvent) {
        when (event) {
            is ReceiveCylinderScreenEvent.OnBackClick -> onBackClick()
            is ReceiveCylinderScreenEvent.OnConfirmClick -> onConfirmClick()
        }
    }

}