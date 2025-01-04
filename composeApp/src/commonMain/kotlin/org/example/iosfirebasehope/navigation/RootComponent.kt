package org.example.iosfirebasehope.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.serialization.Serializable
import org.example.iosfirebasehope.navigation.components.AddCylinderScreenComponent
import org.example.iosfirebasehope.navigation.components.AllCylinderDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent
import org.example.iosfirebasehope.navigation.components.CylinderStatusScreenComponent
import org.example.iosfirebasehope.navigation.components.GasVolumeScreenComponent
import org.example.iosfirebasehope.navigation.components.HomeScreenComponent
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
                    }
                )
            )
            is Configuration.GasVolumeScreen -> Child.GasVolumeScreen(
                GasVolumeScreenComponent(
                    gasId = config.gasId,
                    componentContext = context,
                    onGasCardClick = { volumeType, cylinderDetailList ->
                        navigation.pushNew(
                            Configuration.VolumeTypeScreen(
                                cylinderDetailList,
                                volumeType
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
                )
            )
            is Configuration.CurrentCylinderDetails -> Child.CurrentCylinderDetailsScreen(
                CurrentCylinderDetailsComponent(
                    currentCylinderDetails = config.currentCylinderDetails
                )
            )
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
    }

    @Serializable  //converts from json to kotlin object
    sealed class Configuration{
        @Serializable
        data class VolumeTypeScreen(
            val cylinderDetailsList: List<Map<String, String>>,
            val volumeType: String
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
    }
}