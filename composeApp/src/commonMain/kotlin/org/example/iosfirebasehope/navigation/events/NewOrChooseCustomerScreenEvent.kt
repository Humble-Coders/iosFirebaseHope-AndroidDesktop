package org.example.iosfirebasehope.navigation.events

interface NewOrChooseCustomerScreenEvent {
    data object OnBackClick : NewOrChooseCustomerScreenEvent
    data class OnChooseCustomerClick(val customerName: String) : NewOrChooseCustomerScreenEvent
}