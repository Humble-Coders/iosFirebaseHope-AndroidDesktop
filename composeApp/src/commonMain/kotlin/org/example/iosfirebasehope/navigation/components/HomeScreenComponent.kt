package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import dev.gitlive.firebase.firestore.DocumentSnapshot
import org.example.iosfirebasehope.navigation.events.HomeScreenEvent

class HomeScreenComponent(
    componentContext: ComponentContext,
    private val onStatusClick: (List<Map<String, String>>, String, List<String>) -> Unit,
    private val onAddCylinderClick: (List<Map<String,String>>) -> Unit,
    private val onNotificationClick:(List<Map<String,String>>)->Unit,
    private val onInventoryClick: () -> Unit,
    private val onAllCustomerClick: (List<Map<String, String>>,List<String>) -> Unit,
    private val onBillClick: () -> Unit,
    private val onAllCylinderDetailsClick: (List<Map<String, String>>) -> Unit,
    private val onRefillClick:()->Unit,
    private val onAllVendorClick: (List<Map<String, String>>, List<String>) -> Unit,
    private val onGasCardClick: (String, List<Map<String, String>>) -> Unit): ComponentContext by componentContext {
    fun onEvent(event: HomeScreenEvent){
        when(event){
            is HomeScreenEvent.OnGasCardClick -> onGasCardClick(event.gasId, event.cylinderDetailList)
            is HomeScreenEvent.OnAddCylinderClick -> onAddCylinderClick(event.cylinderDetailList)
            is HomeScreenEvent.OnStatusClick -> onStatusClick(event.cylinderDetailList, event.status,event.gasList)
            is HomeScreenEvent.OnAllCylinderDetailsClick -> onAllCylinderDetailsClick(event.cylinderDetailList)
            is HomeScreenEvent.OnBillClick -> onBillClick()
            is HomeScreenEvent.onInventoryClick -> onInventoryClick()
            is HomeScreenEvent.OnAllCustomerClick -> onAllCustomerClick(event.cylinderDetailList, event.gasList)
            is HomeScreenEvent.OnNotificationClick -> onNotificationClick(event.cylinderDetail)
            is HomeScreenEvent.OnRefillClick -> onRefillClick()
            is HomeScreenEvent.OnAllVendorClick -> onAllVendorClick(event.cylinderDetailList, event.gasList)
        }
    }
}