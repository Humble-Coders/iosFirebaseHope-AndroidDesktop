package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import dev.gitlive.firebase.firestore.DocumentSnapshot
import org.example.iosfirebasehope.navigation.events.HomeScreenEvent

class HomeScreenComponent(
    componentContext: ComponentContext,
    private val onStatusClick: (List<Map<String, String>>, String, List<String>) -> Unit,
    private val onAddCylinderClick: () -> Unit,
    private val onInventoryClick: () -> Unit,
    private val onAllCustomerClick: (List<Map<String, String>>) -> Unit,
    private val onBillClick: () -> Unit,
    private val onAllCylinderDetailsClick: (List<Map<String, String>>) -> Unit,
    private val onGasCardClick: (String, List<Map<String, String>>) -> Unit): ComponentContext by componentContext {
    fun onEvent(event: HomeScreenEvent){
        when(event){
            is HomeScreenEvent.OnGasCardClick -> onGasCardClick(event.gasId, event.cylinderDetailList)
            is HomeScreenEvent.OnAddCylinderClick -> onAddCylinderClick()
            is HomeScreenEvent.OnStatusClick -> onStatusClick(event.cylinderDetailList, event.status,event.gasList)
            is HomeScreenEvent.OnAllCylinderDetailsClick -> onAllCylinderDetailsClick(event.cylinderDetailList)
            is HomeScreenEvent.OnBillClick -> onBillClick()
            is HomeScreenEvent.onInventoryClick -> onInventoryClick()
            is HomeScreenEvent.OnAllCustomerClick -> onAllCustomerClick(event.cylinderDetailList)
        }
    }
}