package org.example.iosfirebasehope.navigation.events

interface CylinderStatusScreenEvent {

    data object onBackClick : CylinderStatusScreenEvent
    data class OnCylinderClick(val currentCylinderDetails: Map<String, String>) : CylinderStatusScreenEvent
}