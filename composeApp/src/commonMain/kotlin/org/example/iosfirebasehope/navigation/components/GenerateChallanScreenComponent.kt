package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.GenerateChallanScreenEvent

class GenerateChallanScreenComponent(
    componentContext: ComponentContext,
    val VendorName: String,
    val dateTime: String,
    private val onBackClick: () -> Unit
)
{
    fun onEvent(event: GenerateChallanScreenEvent){
        when(event){
            is GenerateChallanScreenEvent.OnBackClick -> onBackClick()
        }
    }
}