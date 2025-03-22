package org.example.iosfirebasehope.navigation.events

interface NewIssueCylinderScreenEvent {
    data object OnBackClick : NewIssueCylinderScreenEvent
    data class OnBillClick(val customerName: String,val dateTime: String) : NewIssueCylinderScreenEvent
}