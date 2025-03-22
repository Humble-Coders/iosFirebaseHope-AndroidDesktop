package org.example.iosfirebasehope.navigation.components

import org.example.iosfirebasehope.navigation.events.VendorDetailsScreenEvent

class VendorDetailsScreenComponent(
    val vendorDetails:String,
    private val onBackClick: () -> Unit,
    val cylinderDetailsList: List<Map<String, String>>,
    private val onTransactionClick: (String,List<Map<String, String>>) -> Unit,
    val gasList: List<String>
) {
    fun onEvent(event: VendorDetailsScreenEvent){
        when(event){
            is VendorDetailsScreenEvent.OnBackClick -> onBackClick()
            is VendorDetailsScreenEvent.OnTransactionClick -> onTransactionClick(event.vendorName, event.cylinderDetails)
        }
    }
}