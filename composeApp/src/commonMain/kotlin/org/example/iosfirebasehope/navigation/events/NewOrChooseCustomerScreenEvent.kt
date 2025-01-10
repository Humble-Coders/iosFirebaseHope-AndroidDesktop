package org.example.iosfirebasehope.navigation.events

interface NewOrChooseCustomerScreenEvent {
    data object OnBackClick : NewOrChooseCustomerScreenEvent
    data object OnNewCustomerClick : NewOrChooseCustomerScreenEvent
    data object OnChooseCustomerClick : NewOrChooseCustomerScreenEvent
}