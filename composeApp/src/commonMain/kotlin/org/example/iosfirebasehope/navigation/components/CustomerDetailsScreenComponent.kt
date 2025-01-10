package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.CustomerDetailsScreenEvent

class CustomerDetailsScreenComponent(
    val customerDetails:String,
    private val onBackClick: () -> Unit,
    val cylinderDetailsList: List<Map<String, String>>
) {
    fun onEvent(event: CustomerDetailsScreenEvent){
        when(event){
            is CustomerDetailsScreenEvent.OnBackClick -> onBackClick()
        }
    }
}