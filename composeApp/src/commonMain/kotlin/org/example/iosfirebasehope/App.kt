package org.example.iosfirebasehope


import GasVolumeScreenUI

import androidx.compose.material.*
import androidx.compose.runtime.*
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.UI.AddCylinderScreenUI
import org.example.iosfirebasehope.UI.AllCustomersScreenUI
import org.example.iosfirebasehope.UI.AllCylinderDetailsScreenUI
import org.example.iosfirebasehope.UI.BillScreenUI
import org.example.iosfirebasehope.UI.CurrentCylinderDetailsUI
import org.example.iosfirebasehope.UI.CustomerDetailsScreenUI
import org.example.iosfirebasehope.UI.CylinderStatusScreenUI
import org.example.iosfirebasehope.UI.HomeScreenUI
import org.example.iosfirebasehope.UI.InventoryScreenUI
import org.example.iosfirebasehope.UI.IssueNewCylinderScreenUI
import org.example.iosfirebasehope.UI.NewOrChooseCustomerScreenUI
import org.example.iosfirebasehope.UI.VolumeTypeScreenUI
import org.example.iosfirebasehope.navigation.Child.AddCylinderScreen
import org.example.iosfirebasehope.navigation.Child.AllCustomerScreen
import org.example.iosfirebasehope.navigation.Child.AllCylinderDetailsScreen
import org.example.iosfirebasehope.navigation.Child.BillScreen
import org.example.iosfirebasehope.navigation.Child.CurrentCylinderDetailsScreen
import org.example.iosfirebasehope.navigation.Child.CustomerDetailsScreen
import org.example.iosfirebasehope.navigation.Child.CylinderStatusScreen
import org.example.iosfirebasehope.navigation.Child.GasVolumeScreen
import org.example.iosfirebasehope.navigation.Child.HomeScreen
import org.example.iosfirebasehope.navigation.Child.InventoryScreen
import org.example.iosfirebasehope.navigation.Child.IssueNewCylinderScreen
import org.example.iosfirebasehope.navigation.Child.NewOrChooseCustomerScreen
import org.example.iosfirebasehope.navigation.Child.VolumeTypeScreen


import org.example.iosfirebasehope.navigation.RootComponent




@Composable
fun App(rootComponent: RootComponent, db: FirebaseFirestore){
    MaterialTheme{

        val childStack by rootComponent.childStack.subscribeAsState()
        Children(
            stack = childStack,
            animation = stackAnimation(slide())
        ){
                child ->
            when(val instance = child.instance){

                is HomeScreen -> HomeScreenUI(instance.component,db)

                is GasVolumeScreen -> GasVolumeScreenUI(
                    component =  instance.component,
                    gasId = instance.component.gasId, db = db,
                    cylinderDetailList = instance.component.cylinderDetailList
                )

                is AddCylinderScreen -> AddCylinderScreenUI(instance.component, db)

                is CylinderStatusScreen -> CylinderStatusScreenUI(instance.component,
                    cylinderDetailsList = instance.component.cylinderDetailsList,
                    status = instance.component.status, gasList = instance.component.gasList)

                is VolumeTypeScreen -> VolumeTypeScreenUI(instance.component, cylinderDetailList = instance.component.cylinderDetailList,VolumeType=instance.component.volumeType, gasId = instance.component.gasId)

                is AllCylinderDetailsScreen -> AllCylinderDetailsScreenUI(instance.component, cylinderDetailsList = instance.component.cylinderDetailsList)
                is CurrentCylinderDetailsScreen -> CurrentCylinderDetailsUI(component = instance.component, currentCylinderDetails = instance.component.currentCylinderDetails, db = db)
                is BillScreen -> BillScreenUI(instance.component, db)
                is IssueNewCylinderScreen -> IssueNewCylinderScreenUI(instance.component, db)
                is NewOrChooseCustomerScreen -> NewOrChooseCustomerScreenUI(instance.component, db)
                is InventoryScreen -> InventoryScreenUI(instance.component, db)
                is AllCustomerScreen -> AllCustomersScreenUI(instance.component, db, instance.component.cylinderDetailsList)
                is CustomerDetailsScreen -> CustomerDetailsScreenUI(instance.component.customerDetails,instance.component, instance.component.cylinderDetailsList,db)
            }
        }
    }
}


































//@Composable
//fun App() {
//    var text by remember { mutableStateOf("") }
//    val db = Firebase.firestore
//    val coroutineScope = rememberCoroutineScope()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        TextField(
//            value = text,
//            onValueChange = { text = it },
//            label = { Text("Enter text") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(
//            onClick = {
//                coroutineScope.launch {
//                    val data = hashMapOf("text" to text)
//                    try {
//                        db.collection("testCollection").add(data)
//                        // Handle success
//                    } catch (e: Exception) {
//                        // Handle failure
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Upload to Firestore")
//        }
//    }
//}