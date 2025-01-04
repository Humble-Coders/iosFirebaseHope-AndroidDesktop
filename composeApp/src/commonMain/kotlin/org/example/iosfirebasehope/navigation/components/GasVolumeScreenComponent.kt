package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.GasVolumeScreenEvent
import org.example.iosfirebasehope.navigation.events.HomeScreenEvent

class GasVolumeScreenComponent(
    val gasId: String,
    val cylinderDetailList: List<Map<String, String>>,
    componentContext: ComponentContext,
    private val onGasCardClick: (String, List<Map<String, String>>) -> Unit,
    private val onBackClick: () -> Unit,
    private val onCylinderClick: (Map<String, String>) -> Unit
) {

    fun onEvent(event: GasVolumeScreenEvent) {
        when (event) {
            is GasVolumeScreenEvent.onGasCardClick -> onGasCardClick(
                event.volumeType,
                event.cylinderDetailList
            )
            is GasVolumeScreenEvent.onBackClick -> onBackClick()
            is GasVolumeScreenEvent.OnCylinderClick -> onCylinderClick(event.currentCylinderDetails)
        }

    }
}