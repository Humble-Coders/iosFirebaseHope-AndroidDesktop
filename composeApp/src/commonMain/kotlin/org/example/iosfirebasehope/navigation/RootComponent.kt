package org.example.iosfirebasehope.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.serialization.Serializable
import org.example.iosfirebasehope.navigation.components.AddCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.AllCustomersScreenComponent
import org.example.iosfirebasehope.navigation.components.AllCylinderDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.BillScreenComponent
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent
import org.example.iosfirebasehope.navigation.components.CustomerDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.CylinderStatusScreenComponent
import org.example.iosfirebasehope.navigation.components.GasVolumeScreenComponent
import org.example.iosfirebasehope.navigation.components.HomeScreenComponent
import org.example.iosfirebasehope.navigation.components.InventoryScreenComponent
import org.example.iosfirebasehope.navigation.components.IssueCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.NewOrChooseCustomerScreenComponent
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
                    onAddCylinderClick = {
                        navigation.pushNew(Configuration.AddCylinderScreen)
                    },
                    onStatusClick = { cylinderDetailsList, status,gasList ->
                        navigation.pushNew(Configuration.CylinderStatusScreen(cylinderDetailsList, status,gasList))
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
                    onAllCustomerClick = {
                        navigation.pushNew(Configuration.AllCustomerScreen(it))
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
                    }
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
                    }
                )
            )
            is Configuration.IssueNewCylinderScreen -> Child.IssueNewCylinderScreen(
                IssueCylinderScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
                    }
                )
            )
            is Configuration.NewOrChooseCustomerScreen -> Child.NewOrChooseCustomerScreen(
                NewOrChooseCustomerScreenComponent(
                    componentContext = context,
                    onBackClick = {
                        navigation.pop()
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
                    onCustomerClick = { customerDetails ,cylinderDetailList->
                        navigation.pushNew(
                            Configuration.CustomerDetailsScreen(
                                customerDetails = customerDetails,
                                cylinderDetailList = cylinderDetailList

                            )
                        )
                    },
                    cylinderDetailsList = config.cylinderDetailList
                )
            )
            is Configuration.CustomerDetailsScreen -> Child.CustomerDetailsScreen(
                CustomerDetailsScreenComponent(
                    customerDetails = config.customerDetails,
                    onBackClick = {
                        navigation.pop()
                    },
                    cylinderDetailsList = config.cylinderDetailList
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
        data class IssueNewCylinderScreen(val component: IssueCylinderScreenComponent): Child()
        data class NewOrChooseCustomerScreen(val component: NewOrChooseCustomerScreenComponent): Child()
        data class InventoryScreen(val component: InventoryScreenComponent): Child()
        data class AllCustomerScreen(val component: AllCustomersScreenComponent): Child()
        data class CustomerDetailsScreen(val component: CustomerDetailsScreenComponent): Child()
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
        data object AddCylinderScreen: Configuration()

        @Serializable
        data class AllCylinderDetailsScreen(val cylinderDetailList: List<Map<String, String>>): Configuration()

        @Serializable
        data class CylinderStatusScreen(val cylinderDetailsList: List<Map<String, String>>, val status: String,val gasList: List<String>): Configuration()

        @Serializable
        data class CurrentCylinderDetails(val currentCylinderDetails: Map<String, String>): Configuration()

        @Serializable
        data object BillScreen: Configuration()

        @Serializable
        data object IssueNewCylinderScreen: Configuration()
        companion object

        @Serializable
        data object NewOrChooseCustomerScreen: Configuration()

        @Serializable
        data object InventoryScreen: Configuration()

        @Serializable
        data class AllCustomerScreen(val cylinderDetailList: List<Map<String, String>>): Configuration()

        @Serializable
        data class CustomerDetailsScreen(val customerDetails: String,val cylinderDetailList: List<Map<String, String>>): Configuration()
    }


