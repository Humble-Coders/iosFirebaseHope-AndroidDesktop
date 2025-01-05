package org.example.iosfirebasehope.navigation.events

interface VolumeTypeScreenEvent {
    data object onBackClick : VolumeTypeScreenEvent
    data class OnCylinderClick(val currentCylinderDetails: Map<String, String>) : VolumeTypeScreenEvent
}