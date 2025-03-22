package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.AllVendorsScreenEvent

class AllVendorsScreenComponent(
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    private val onVendorClick: (String, List<Map<String, String>>, List<String>) -> Unit,
    val cylinderDetailsList: List<Map<String, String>>,
    val gasList: List<String>
) {
    fun onEvent(event: AllVendorsScreenEvent){
        when(event){
            is AllVendorsScreenEvent.OnBackClick -> onBackClick()
            is AllVendorsScreenEvent.OnVendorClick -> onVendorClick(event.vendorDetails, event.cylinderDetails,event.gasList)
        }
    }
}