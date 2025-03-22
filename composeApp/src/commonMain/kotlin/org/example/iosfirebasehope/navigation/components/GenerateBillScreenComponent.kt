package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.GenerateBillScreenEvent

class GenerateBillScreenComponent(
    componentContext: ComponentContext,
    val customerName: String,
    val dateTime: String,
    private val onBackClick: () -> Unit
)
{
    fun onEvent(event: GenerateBillScreenEvent){
        when(event){
            is GenerateBillScreenEvent.OnBackClick -> onBackClick()
        }
    }
}