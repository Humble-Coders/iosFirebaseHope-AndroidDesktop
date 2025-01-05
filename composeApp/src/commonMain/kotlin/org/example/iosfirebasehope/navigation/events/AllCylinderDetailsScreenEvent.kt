package org.example.iosfirebasehope.navigation.events

interface AllCylinderDetailsScreenEvent {
    data object OnBackClick : AllCylinderDetailsScreenEvent
    data class OnCylinderClick(val currentCylinderDetails: Map<String, String>) : AllCylinderDetailsScreenEvent
}