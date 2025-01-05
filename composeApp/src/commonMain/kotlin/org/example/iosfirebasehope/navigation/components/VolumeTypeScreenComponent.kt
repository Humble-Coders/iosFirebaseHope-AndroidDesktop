package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.VolumeTypeScreenEvent

class VolumeTypeScreenComponent(
    val volumeType: String,
    val cylinderDetailList: List<Map<String, String>>,
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit,
    val gasId: String,
    private val onCylinderClick: (Map<String, String>) -> Unit
) {

        fun onEvent(event: VolumeTypeScreenEvent) {
            when (event) {
                is VolumeTypeScreenEvent.onBackClick -> onBackClick()
                is VolumeTypeScreenEvent.OnCylinderClick -> onCylinderClick(event.currentCylinderDetails)
            }

        }
}