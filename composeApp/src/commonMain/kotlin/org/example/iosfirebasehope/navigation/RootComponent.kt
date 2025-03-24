package org.example.iosfirebasehope.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import kotlinx.serialization.Serializable
import org.example.iosfirebasehope.navigation.components.AddCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.AddInventoryScreenComponent
import org.example.iosfirebasehope.navigation.components.AllCustomersScreenComponent
import org.example.iosfirebasehope.navigation.components.AllCylinderDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.AllVendorsScreenComponent
import org.example.iosfirebasehope.navigation.components.BillScreenComponent
import org.example.iosfirebasehope.navigation.components.CreditListScreenComponent
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent
import org.example.iosfirebasehope.navigation.components.CurrentlyIssuedScreenComponent
import org.example.iosfirebasehope.navigation.components.CustomerDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.CylinderStatusScreenComponent
import org.example.iosfirebasehope.navigation.components.DailyBookScreenComponent
import org.example.iosfirebasehope.navigation.components.ExchangeCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.GasVolumeScreenComponent
import org.example.iosfirebasehope.navigation.components.GenerateBillScreenComponent
import org.example.iosfirebasehope.navigation.components.GenerateChallanScreenComponent
import org.example.iosfirebasehope.navigation.components.HomeScreenComponent
import org.example.iosfirebasehope.navigation.components.InventoryScreenComponent
import org.example.iosfirebasehope.navigation.components.InventoryVendorDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.InventoryVendorsScreenComponent
import org.example.iosfirebasehope.navigation.components.IssueCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.NewIssueCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.NewOrChooseCustomerScreenComponent
import org.example.iosfirebasehope.navigation.components.NewOrChooseVendorScreenComponent
import org.example.iosfirebasehope.navigation.components.NotificationScreenComponent
import org.example.iosfirebasehope.navigation.components.ReceiveCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.ReturnCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.SendForRefillingComponent
import org.example.iosfirebasehope.navigation.components.TransactionDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.TransactionScreenComponent
import org.example.iosfirebasehope.navigation.components.TransactionVendorDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.TransactionVendorScreenComponent
import org.example.iosfirebasehope.navigation.components.VendorDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.VolumeTypeScreenComponent

class RootComponent(
    componentContext: ComponentContext,
): ComponentContext by componentContext {

    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        serializer = Configuration.serializer(),
        initialConfiguration = Configuration.HomeScreen,
        handleBackButton = true,
        childFactory = ::createChild
    )

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createChild(
        config: Configuration,
        context: ComponentContext,
    ): Child{
        return when(config){
            is Configuration.HomeScreen -> Child.HomeScreen(
                HomeScreenComponent(
                    componentContext = context,
                    onGasCardClick = { gasId, cylinderDetailList ->
                        navigation.pushNew(Configuration.GasVolumeScreen(gasId, cylinderDetailList))
                    },
                    onAddCylinderClick = {cylinderDetailList ->
                        navigation.pushNew(Configuration.AddCylinderScreen(cylinderDetailList))
                    },
                    onStatusClick = { cylinderDetailsList, status, gasList ->
                        navigation.pushNew(
                            Configuration.CylinderStatusScreen(
                                cylinderDetailsList,
                                status,
                                gasList
                            )
                        )
                    },
                    onAllCylinderDetailsClick = { cylinderDetailList ->
                        navigation.pushNew(Configuration.AllCylinderDetailsScreen(cylinderDetailList))
                    },
                    onBillClick = {
                        navigation.pushNew(Configuration.BillScreen)
                    },
                    onInventoryClick = {
                        navigation.pushNew(Configuration.InventoryScreen)
                    },
                    onAllCustomerClick = { cylinderDetailList, gasList ->
                        navigation.pushNew(Configuration.AllCustomerScreen(cylinderDetailList, gasList))
                    },
                    onNotificationClick = {
                        navigation.pushNew(Configuration.NotificationScreen(it))
                    },
                    onRefillClick = {
                        navigation.pushNew(Configuration.NewOrChooseVendorScreen)
                    },
                    onAllVendorClick = { cylinderDetailList, gasList ->
                        navigation.pushNew(Configuration.AllVendorScreen(cylinderDetailList, gasList))
                    },
                    onCreditListClick = {
                        navigation.pushNew(Configuration.CreditListScreen)
                    },
                    onCurrentlyIssuedClick = {
                        navigation.pushNew(Configuration.CurrentlyIssuedScreen(it))
                    },
                    onDailyBookClick = {
                        navigation.pushNew(Configuration.DailyBookScreen)
                    },
                    onAddInventoryClick = {
                        navigation.pushNew(Configuration.AddInventoryScreen)
                    },
                    onInventoryVendorClick = {
                        navigation.pushNew(Configuration.InventoryVendorScreen)
                    }
                )
            )
            is Configuration.GasVolumeScreen -> Child.GasVolumeScreen(
                GasVolumeScreenComponent(
                    gasId = config.gasId,
                    componentContext = context,
                    onGasCardClick = { volumeType, cylinderDetailList, gasId ->
                        navigation.pushNew(
                            Configuration.VolumeTypeScreen(
                                cylinderDetailList,
                                volumeType,
                                gasId = config.gasId
                            )
                        )
                    },
                    cylinderDetailList = config.cylinderDetailList,
                    onBackClick = {
                        navigation.pop()
                    },
                    onCylinderClick = {
                            cylinderDetails ->
                        navigation.pushNew(
                            Configuration.CurrentCylinderDetails(
                                currentCylinderDetails = cylinderDetails
                            )
                        )
                    }
                )
            )
            is Configuration.AddCylinderScreen -> Child.AddCylinderScreen(
                AddCylinderScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    cylinderDetailList =config.cylinderDetails
                )
            )
            is Configuration.CylinderStatusScreen -> Child.CylinderStatusScreen(
                CylinderStatusScreenComponent(
                    componentContext = context,
                    status = config.status,
                    cylinderDetailsList = config.cylinderDetailsList,
                    gasList = config.gasList,
                    onBackClick = {
                        navigation.pop()
                    },
                    onCylinderClick = { currentCylinderDetails ->
                        navigation.pushNew(
                            Configuration.CurrentCylinderDetails(
                                currentCylinderDetails = currentCylinderDetails
                            )
                        )
                    }
                )
            )
            is Configuration.VolumeTypeScreen -> Child.VolumeTypeScreen(
                VolumeTypeScreenComponent(
                    componentContext = context,
                    cylinderDetailList = config.cylinderDetailsList,
                    volumeType = config.volumeType,
                    onBackClick = {
                        navigation.pop()
                    },
                    gasId = config.gasId,
                    onCylinderClick = { currentCylinderDetails ->
                        navigation.pushNew(
                            Configuration.CurrentCylinderDetails(
                                currentCylinderDetails = currentCylinderDetails
                            )
                        )
                    }
                )
            )
            is Configuration.AllCylinderDetailsScreen -> Child.AllCylinderDetailsScreen(
                AllCylinderDetailsScreenComponent(
                    componentContext = context,
                    cylinderDetailsList = config.cylinderDetailList,
                    onBackClick ={
                        navigation.pop()
                    },
                    onCylinderClick = { currentCylinderDetails ->
                        navigation.pushNew(
                            Configuration.CurrentCylinderDetails(
                                currentCylinderDetails = currentCylinderDetails
                            )
                        )
                    }
                )
            )
            is Configuration.CurrentCylinderDetails -> Child.CurrentCylinderDetailsScreen(
                CurrentCylinderDetailsComponent(
                    currentCylinderDetails = config.currentCylinderDetails,
                    onBackClick = {
                        navigation.pop()
                    },
                    onGoToHomeClick = {
                        // Replace the entire stack with just HomeScreen
                        navigation.replaceAll(Configuration.HomeScreen)
                    }
                )
            )
            is Configuration.BillScreen -> Child.BillScreen(
                BillScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onIssueCylinderClick = {
                        navigation.pushNew(Configuration.NewOrChooseCustomerScreen)
                    },
                    onChooseCustomerClick = { customerName ->
                        navigation.pushNew(
                            Configuration.ReturnCylinderScreen(
                                customerName = customerName
                            )
                        )
                    },
                    onExchangeCylinderClick = { customerName ->
                        navigation.pushNew(
                            Configuration.ExchangeCylinderScreen(
                                customerName = customerName
                            )
                        )
                    }
                )
            )
//            is Configuration.IssueNewCylinderScreen -> Child.IssueNewCylinderScreen(
//                IssueCylinderScreenComponent(
//                    componentContext = context,
//                    onBackClick = {
//                        navigation.pop()
//                    }
//                )
//            )
            is Configuration.NewOrChooseCustomerScreen -> Child.NewOrChooseCustomerScreen(
                NewOrChooseCustomerScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onChooseCustomerClick = { customerName ->
                        navigation.pushNew(
                            Configuration.NewIssueCylinderScreen(
                                customerName = customerName
                            )
                        )
                    }
                )
            )
            is Configuration.InventoryScreen -> Child.InventoryScreen(
                InventoryScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    }
                )
            )
            is Configuration.AllCustomerScreen -> Child.AllCustomerScreen(
                AllCustomersScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onCustomerClick = { customerDetails ,cylinderDetailList,gasList->
                        navigation.pushNew(
                            Configuration.CustomerDetailsScreen(
                                customerDetails = customerDetails,
                                cylinderDetailList = cylinderDetailList,
                                gasList = gasList

                            )
                        )
                    },
                    gasList = config.gasList,
                    cylinderDetailsList = config.cylinderDetailList
                )
            )
            is Configuration.CustomerDetailsScreen -> Child.CustomerDetailsScreen(
                CustomerDetailsScreenComponent(
                    customerDetails = config.customerDetails,
                    onBackClick = {
                        navigation.pop()
                    },
                    gasList = config.gasList,
                    cylinderDetailsList = config.cylinderDetailList,
                    onTransactionClick = { customerName, cylinderDetails ->
                        navigation.pushNew(
                            Configuration.TransactionScreen(
                                customerName = customerName,
                                cylinderDetails = cylinderDetails
                            )
                        )
                    }
                )
            )
            is Configuration.NewIssueCylinderScreen -> Child.NewIssueCylinderScreen(
                NewIssueCylinderScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    customerName = config.customerName,
                    onBillClick = { customerName, dateTime ->
                        navigation.pushNew(
                            Configuration.GenerateBillScreen(
                                customerName = customerName,
                                dateTime = dateTime
                            )
                        )
                    },
                )
            )
            is Configuration.NotificationScreen -> Child.NotificationScreen(
                NotificationScreenComponent(
                    cylinderDetails = config.cylinderDetail,
                    onBackClick = {
                        navigation.pop()
                    }
                )
            )
            is Configuration.TransactionScreen -> Child.TransactionScreen(
                TransactionScreenComponent(
                    customerName = config.customerName,
                    cylinderDetails = config.cylinderDetails,
                    onBackClick = {
                        navigation.pop()
                    },
                    onTransactionClick = { customerName, dateTime ->
                        navigation.pushNew(
                            Configuration.TransactionDetailsScreen(
                                customerName = customerName,
                                dateTime = dateTime
                            )
                        )
                    }
                )
            )
            is Configuration.ReturnCylinderScreen -> Child.ReturnCylinderScreen(
                ReturnCylinderScreenComponent(
                    onBackClick = {
                        navigation.pop()
                    },
                    onReturnCylinderClick = {
                        navigation.pushNew(Configuration.NewOrChooseCustomerScreen)
                    },
                    onChooseCustomerClick = { customerName ->
                        navigation.pushNew(
                            Configuration.NewIssueCylinderScreen(
                                customerName = customerName
                            )
                        )
                    },
                    customerName = config.customerName,
                    onConfirmClick = { customerName, dateTime ->
                        navigation.pushNew(
                            Configuration.GenerateBillScreen(
                                customerName = customerName,
                                dateTime = dateTime
                            )
                        )
                    }
                )
            )
            is Configuration.ExchangeCylinderScreen -> Child.ExchangeCylinderScreen(
                ExchangeCylinderScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    customerName = config.customerName,
                    onBillClick = { customerName, dateTime ->
                        navigation.pushNew(
                            Configuration.GenerateBillScreen(
                                customerName = customerName,
                                dateTime = dateTime
                            )
                        )
                    }
                )
            )
            is Configuration.NewOrChooseVendorScreen -> Child.NewOrChooseVendorScreen(
                NewOrChooseVendorScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onChooseVendorClick = { vendorName ->
                        navigation.pushNew(
                            Configuration.SendForRefillingScreen(
                                vendorName = vendorName
                            )
                        )
                    },
                    onRecieveClick = { vendorName ->
                        navigation.pushNew(
                            Configuration.ReceiveCylinderScreen(
                                vendorName = vendorName
                            )
                        )
                    }
                )
            )
            is Configuration.SendForRefillingScreen -> Child.SendForRefillingScreen(
                SendForRefillingComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    VendorName = config.vendorName,
                    onChallanClick = { vendorName, dateTime ->
                        navigation.pushNew(
                            Configuration.GenerateChallanScreen(
                                vendorName = vendorName,
                                dateTime = dateTime
                            )
                        )
                    }
                )
            )
            is Configuration.ReceiveCylinderScreen -> Child.ReceiveCylinderScreen(
                ReceiveCylinderScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    VendorName = config.vendorName,
                    onConfirmClick = {
                        navigation.replaceAll(Configuration.HomeScreen)
                    }
                )
            )
            is Configuration.GenerateBillScreen -> Child.GenerateBillScreen(
                GenerateBillScreenComponent(
                    componentContext = context,
                    customerName = config.customerName,
                    dateTime = config.dateTime,
                    onBackClick = {
                        navigation.replaceAll(Configuration.HomeScreen)
                    }
                )
            )
            is Configuration.TransactionDetailsScreen -> Child.TransactionDetailsScreen(
                TransactionDetailsScreenComponent(
                    onBackClick = {
                        navigation.pop()
                    },
                    customerName = config.customerName,
                    dateTime = config.dateTime,
                    onBillClick = {
                        navigation.pushNew(Configuration.GenerateBillScreen(config.customerName, config.dateTime))
                    }
                )
            )
            is Configuration.AllVendorScreen -> Child.AllVendorScreen(
                AllVendorsScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onVendorClick = { vendorDetails, cylinderDetails,gasList ->
                        navigation.pushNew(
                            Configuration.VendorDetailsScreen(
                                vendorDetails = vendorDetails,
                                cylinderDetailList = cylinderDetails,
                                gasList = gasList

                            )
                        )
                    },
                    cylinderDetailsList = config.cylinderDetailList,
                    gasList = config.gasList
                )
            )
            is Configuration.VendorDetailsScreen -> Child.VendorDetailsScreen(
                VendorDetailsScreenComponent(
                    vendorDetails = config.vendorDetails,
                    onBackClick = {
                        navigation.pop()
                    },
                    cylinderDetailsList = config.cylinderDetailList,
                    onTransactionClick = { vendorName, cylinderDetails ->
                        navigation.pushNew(
                            Configuration.TransactionVendorScreen(
                                vendorName = vendorName,
                                cylinderDetails = cylinderDetails
                            ),

                            )
                    },
                    gasList = config.gasList
                )
            )
            is Configuration.TransactionVendorScreen -> Child.TransactionVendorScreen(
                TransactionVendorScreenComponent(
                    vendorName = config.vendorName,
                    cylinderDetails = config.cylinderDetails,
                    onBackClick = {
                        navigation.pop()
                    },
                    onTransactionClick = { vendorName, dateTime ->
                        navigation.pushNew(
                            Configuration.TransactionVendorDetailsScreen(
                                vendorName = vendorName,
                                dateTime = dateTime
                            )
                        )
                    }
                )
            )
            is Configuration.TransactionVendorDetailsScreen -> Child.TransactionVendorDetailsScreen(
                TransactionVendorDetailsScreenComponent(
                    vendorName = config.vendorName,
                    dateTime = config.dateTime,
                    onBackClick = {
                        navigation.pop()
                    }
                )
            )
            is Configuration.GenerateChallanScreen -> Child.GenerateChallanScreen(
                GenerateChallanScreenComponent(
                    VendorName = config.vendorName,
                    dateTime = config.dateTime,
                    onBackClick = {
                        navigation.pop()
                    },
                    componentContext = context
                )
            )
            is Configuration.CreditListScreen -> Child.CreditListScreen(
                CreditListScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    }
                )
            )

            is Configuration.CurrentlyIssuedScreen -> Child.CurrentlyIssuedScreen(
                CurrentlyIssuedScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    cylinderDetailList = config.CylinderdetailsList
                )
            )
            is Configuration.DailyBookScreen -> Child.DailyBookScreen(
                DailyBookScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onTransactionClick = { customerName, dateTime ->
                        navigation.pushNew(
                            Configuration.TransactionDetailsScreen(
                                customerName = customerName,
                                dateTime = dateTime
                            )
                        )
                    }

                )
            )
            is Configuration.AddInventoryScreen -> Child.AddInventoryScreen(
                AddInventoryScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    }
                )
            )
            is Configuration.InventoryVendorScreen -> Child.InventoryVendorScreen(
                InventoryVendorsScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    onVendorClick = { vendorName ->
                        navigation.pushNew(
                            Configuration.InventoryVendorDetailsScreen(
                                vendorName = vendorName
                            ))
                    }
                )
            )
            is Configuration.InventoryVendorDetailsScreen -> Child.InventoryVendorDetailsScreen(
                InventoryVendorDetailsScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    },
                    vendorDetails = config.vendorName
                )
            )
        }
    }
}




sealed class Child{
    data class HomeScreen(val component: HomeScreenComponent): Child()
    data class GasVolumeScreen(val component: GasVolumeScreenComponent): Child()
    data class AddCylinderScreen(val component: AddCylinderScreenComponent): Child()
    data class CylinderStatusScreen(val component: CylinderStatusScreenComponent): Child()
    data class VolumeTypeScreen(val component: VolumeTypeScreenComponent): Child()
    data class AllCylinderDetailsScreen(val component: AllCylinderDetailsScreenComponent): Child()
    data class CurrentCylinderDetailsScreen(val component: CurrentCylinderDetailsComponent): Child()
    data class BillScreen(val component: BillScreenComponent): Child()
    //data class IssueNewCylinderScreen(val component: IssueCylinderScreenComponent): Child()
    data class NewOrChooseCustomerScreen(val component: NewOrChooseCustomerScreenComponent): Child()
    data class InventoryScreen(val component: InventoryScreenComponent): Child()
    data class AllCustomerScreen(val component: AllCustomersScreenComponent): Child()
    data class CustomerDetailsScreen(val component: CustomerDetailsScreenComponent): Child()
    data class NewIssueCylinderScreen(val component: NewIssueCylinderScreenComponent): Child()
    data class TransactionScreen(val component: TransactionScreenComponent): Child()
    data class NotificationScreen(val component: NotificationScreenComponent): Child()
    data class ReturnCylinderScreen(val component: ReturnCylinderScreenComponent): Child()
    data class ExchangeCylinderScreen(val component: ExchangeCylinderScreenComponent): Child()
    data class NewOrChooseVendorScreen(val component: NewOrChooseVendorScreenComponent): Child()
    data class SendForRefillingScreen(val component: SendForRefillingComponent): Child()
    data class ReceiveCylinderScreen(val component: ReceiveCylinderScreenComponent): Child()
    data class GenerateBillScreen(val component: GenerateBillScreenComponent): Child()
    data class TransactionDetailsScreen(val component: TransactionDetailsScreenComponent): Child()
    data class AllVendorScreen(val component: AllVendorsScreenComponent): Child()
    data class VendorDetailsScreen(val component: VendorDetailsScreenComponent): Child()
    data class TransactionVendorScreen(val component: TransactionVendorScreenComponent): Child()
    data class TransactionVendorDetailsScreen(val component: TransactionVendorDetailsScreenComponent): Child()
    data class GenerateChallanScreen(val component: GenerateChallanScreenComponent): Child()
    data class CreditListScreen(val component: CreditListScreenComponent): Child()
    data class CurrentlyIssuedScreen(val component: CurrentlyIssuedScreenComponent): Child()
    data class DailyBookScreen ( val component: DailyBookScreenComponent): Child()
    data class AddInventoryScreen ( val component: AddInventoryScreenComponent): Child()
    data class InventoryVendorScreen ( val component: InventoryVendorsScreenComponent): Child()
    data class InventoryVendorDetailsScreen ( val component: InventoryVendorDetailsScreenComponent): Child()
}

@Serializable  //converts from json to kotlin object
sealed class Configuration{
    @Serializable
    data class VolumeTypeScreen(
        val cylinderDetailsList: List<Map<String, String>>,
        val volumeType: String,
        val gasId: String
    ): Configuration()

    @Serializable
    data class GasVolumeScreen(
        val gasId: String,
        val cylinderDetailList: List<Map<String, String>>
    ): Configuration()

    @Serializable
    data object HomeScreen: Configuration()

    @Serializable
    data class AddCylinderScreen (val cylinderDetails: List<Map<String, String>>): Configuration()

    @Serializable
    data class AllCylinderDetailsScreen(val cylinderDetailList: List<Map<String, String>>): Configuration()

    @Serializable
    data class CylinderStatusScreen(val cylinderDetailsList: List<Map<String, String>>, val status: String,val gasList: List<String>): Configuration()

    @Serializable
    data class CurrentCylinderDetails(val currentCylinderDetails: Map<String, String>): Configuration()



    @Serializable
    data object BillScreen: Configuration()

//    @Serializable
//    data object IssueNewCylinderScreen: Configuration()
//    companion object

    @Serializable
    data object NewOrChooseCustomerScreen: Configuration()

    @Serializable
    data object InventoryScreen: Configuration()

    @Serializable
    data class AllCustomerScreen(val cylinderDetailList: List<Map<String, String>>,val gasList: List<String>): Configuration()

    @Serializable
    data class CustomerDetailsScreen(val gasList: List<String>, val customerDetails: String, val cylinderDetailList: List<Map<String, String>>): Configuration()

    @Serializable
    data class NewIssueCylinderScreen(val customerName: String): Configuration()

    @Serializable
    data class TransactionScreen(val customerName: String,val cylinderDetails: List<Map<String, String>>): Configuration()

    @Serializable
    data class NotificationScreen(val cylinderDetail: List<Map<String, String>>): Configuration()

    @Serializable
    data class ReturnCylinderScreen(val customerName: String): Configuration()

    @Serializable
    data class ExchangeCylinderScreen(val customerName: String): Configuration()

    @Serializable
    data object NewOrChooseVendorScreen: Configuration()

    @Serializable
    data class SendForRefillingScreen(val vendorName: String): Configuration()

    @Serializable
    data class ReceiveCylinderScreen(val vendorName: String): Configuration()

    @Serializable
    data class GenerateBillScreen(val customerName: String,val dateTime: String): Configuration()

    @Serializable
    data class TransactionDetailsScreen(val customerName: String,val dateTime: String): Configuration()

    @Serializable
    data class AllVendorScreen(val cylinderDetailList: List<Map<String, String>>,val gasList: List<String>): Configuration()

    @Serializable
    data class VendorDetailsScreen(val vendorDetails: String,val cylinderDetailList: List<Map<String, String>>,val gasList: List<String>): Configuration()

    @Serializable
    data class TransactionVendorScreen(val vendorName: String,val cylinderDetails: List<Map<String, String>>): Configuration()

    @Serializable
    data class TransactionVendorDetailsScreen(val vendorName: String,val dateTime: String): Configuration()

    @Serializable
    data class GenerateChallanScreen(val vendorName: String,val dateTime: String): Configuration()

    @Serializable
    data object CreditListScreen: Configuration()

    @Serializable
    data class CurrentlyIssuedScreen (val CylinderdetailsList :List<Map<String,String>>): Configuration()

    @Serializable
    data object DailyBookScreen: Configuration()

    @Serializable
    data object AddInventoryScreen: Configuration()

    @Serializable
    data object InventoryVendorScreen: Configuration()

    @Serializable
    data class InventoryVendorDetailsScreen(val vendorName:String) : Configuration()

}