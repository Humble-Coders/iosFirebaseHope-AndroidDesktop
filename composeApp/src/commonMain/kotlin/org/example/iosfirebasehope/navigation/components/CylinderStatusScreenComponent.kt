package org.example.iosfirebasehope.navigation.components

import com.arkivanov.decompose.ComponentContext
import org.example.iosfirebasehope.navigation.events.CylinderStatusScreenEvent


class CylinderStatusScreenComponent (
    val status: String,
    val cylinderDetailsList:List<Map<String,String>>,
    val gasList: List<String>,
    componentContext: ComponentContext,
    private val onBackClick: () -> Unit
){
    fun onEvent(event: CylinderStatusScreenEvent ){
        when(event){
            is CylinderStatusScreenEvent.onBackClick -> onBackClick()
        }
}}