package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.AllCustomersScreenEvent

class AllCustomersScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    private val onCustomerClick: (String, List<Map<String, String>>, List<String>) -> Unit,
    val cylinderDetailsList: List<Map<String, String>>,
    val gasList: List<String>
) {
    fun onEvent(event: AllCustomersScreenEvent){
        when(event){
            is AllCustomersScreenEvent.OnBackClick -> onBackClick()
            is AllCustomersScreenEvent.OnCustomerClick -> onCustomerClick(event.customerDetails, event.cylinderDetails, event.gasList)
        }
    }
}