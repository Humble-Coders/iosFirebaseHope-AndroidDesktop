package org.example.iosfirebasehope.navigation.events

import dev.gitlive.firebase.firestore.DocumentSnapshot

interface HomeScreenEvent {
    data class OnGasCardClick(val gasId: String, val cylinderDetailList: List<Map<String, String>>) : HomeScreenEvent
    data object OnAddCylinderClick : HomeScreenEvent
    data class OnStatusClick(val cylinderDetailList: List<Map<String, String>>,val status: String, val gasList: List<String>) : HomeScreenEvent
    data class OnAllCylinderDetailsClick(val cylinderDetailList: List<Map<String, String>>) : HomeScreenEvent
}