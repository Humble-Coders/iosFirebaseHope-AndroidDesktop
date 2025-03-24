package org.example.iosfirebasehope.navigation.events

interface DailyBookScreenEvent {
    data object OnBackClick : DailyBookScreenEvent
    data class OnTransactionClick(val customerName: String, val dateTime: String) : DailyBookScreenEvent

}