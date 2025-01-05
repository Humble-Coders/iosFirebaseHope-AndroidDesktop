package org.example.iosfirebasehope.navigation.events

interface GasVolumeScreenEvent {
    data class onGasCardClick(val volumeType: String, val cylinderDetailList: List<Map<String, String>>, val gasId: String) : GasVolumeScreenEvent
    data object onBackClick : GasVolumeScreenEvent
    data class OnCylinderClick(val currentCylinderDetails: Map<String, String>) : GasVolumeScreenEvent
}