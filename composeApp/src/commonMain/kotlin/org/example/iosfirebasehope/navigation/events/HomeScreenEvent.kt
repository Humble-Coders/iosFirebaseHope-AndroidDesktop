package org.example.iosfirebasehope.navigation.events



interface HomeScreenEvent {
    data class OnGasCardClick(val gasId: String, val cylinderDetailList: List<Map<String, String>>) : HomeScreenEvent
    data class OnAddCylinderClick (val cylinderDetailList: List<Map<String, String>>) : HomeScreenEvent
    data class OnStatusClick(val cylinderDetailList: List<Map<String, String>>,val status: String, val gasList: List<String>) : HomeScreenEvent
    data class OnAllCylinderDetailsClick(val cylinderDetailList: List<Map<String, String>>) : HomeScreenEvent
    data object OnBillClick : HomeScreenEvent
    data object onInventoryClick : HomeScreenEvent
    data class OnAllCustomerClick(val cylinderDetailList: List<Map<String, String>>, val gasList: List<String> ) : HomeScreenEvent
    data class OnNotificationClick(val cylinderDetail: List<Map<String, String>>) : HomeScreenEvent
    data object OnRefillClick : HomeScreenEvent
    data class OnAllVendorClick(val cylinderDetailList: List<Map<String, String>> ,val gasList: List<String>) : HomeScreenEvent
    data object onCreditListClick : HomeScreenEvent
    data class onCurrentlyIssuedClick (val cylinderDetailsList : List<Map<String, String>> )    : HomeScreenEvent
    data object OnDailyBookClick : HomeScreenEvent
}