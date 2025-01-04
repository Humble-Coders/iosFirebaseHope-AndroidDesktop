package org.example.iosfirebasehope.UI


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore

import iosfirebasehope.composeapp.generated.resources.Res
import iosfirebasehope.composeapp.generated.resources.at_plant
import iosfirebasehope.composeapp.generated.resources.empty
import iosfirebasehope.composeapp.generated.resources.full
import iosfirebasehope.composeapp.generated.resources.issued
import iosfirebasehope.composeapp.generated.resources.repair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.iosfirebasehope.navigation.components.HomeScreenComponent
import org.example.iosfirebasehope.navigation.events.HomeScreenEvent
import org.jetbrains.compose.resources.DrawableResource


@Composable
fun HomeScreenUI(component: HomeScreenComponent, db: FirebaseFirestore) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var cylinderDetailsList: List<Map<String, String>> by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var cylinderList: List<Cylinder> by remember { mutableStateOf(listOf<Cylinder>()) }
    var gasDocuments: List<DocumentSnapshot> by remember { mutableStateOf(listOf<DocumentSnapshot>()) }
    var gasList:List<String> by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        cylinderDetailsList= allCylinderDetails(db)
        gasDocuments= gasDocuments(db)
         cylinderList = SortCylindersData(cylinderDetailsList,gasDocuments)
        isLoading=false

    }
    //map gasDocuments to gasList
    gasList = gasDocuments.map { it.id }

    MaterialTheme {
        ModalDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    DrawerHeader(drawerState, scope)
                    DrawerItem(
                        icon = Icons.Default.Info,
                        text = "All Cylinder Details",
                        onClick = {

                            component.onEvent(HomeScreenEvent.OnAllCylinderDetailsClick(cylinderDetailsList))
                            scope.launch { drawerState.close() }
                        }
                    )
                    Divider(color = Color.Gray, thickness = 1.dp)
                    DrawerItem(
                        icon = Icons.Default.Person,
                        text = "Customer Details",
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    Divider(color = Color.Gray, thickness = 1.dp)
                    DrawerItem(
                        icon = Icons.Default.AddCircle,
                        text = "Add Cylinder",
                        onClick = {
                            component.onEvent(HomeScreenEvent.OnAddCylinderClick)
                            scope.launch { drawerState.close() }
                        }
                    )
                    Divider(color = Color.Gray, thickness = 1.dp)
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        modifier = Modifier.height(64.dp),
                        title = { Text("Cylinder Management") },
                        backgroundColor = Color(0xFF2f80eb),
                        contentColor = Color.White,
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* TODO: Handle notification icon click */ }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavigation(
                        backgroundColor = Color(0xFF2f80eb),
                        contentColor = Color.White
                    ) {
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.DateRange, contentDescription = "Bill", tint = Color.White) },
                            label = { Text("Bill") },
                            selected = false,
                            onClick = {component.onEvent(HomeScreenEvent.OnBillClick)}
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Refilling", tint = Color.White) },
                            label = { Text("Refilling") },
                            selected = false,
                            onClick = { /* Handle Refilling click */ }
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.List, contentDescription = "Repair", tint = Color.White) },
                            label = { Text("Inventory") },
                            selected = false,
                            onClick = { /* Handle Repair click */ }
                        )
                    }
                }
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(bottom = 48.dp)) {
                        StatusRow(component = component, cylinderDetailsList = cylinderDetailsList, isLoading = isLoading, gasList = gasList)
                        CylinderList(component = component, cylinders = cylinderList, cylinderDetailsList = cylinderDetailsList)
                    }
                }

            }
        }
    }
}

@Composable
fun DrawerHeader(drawerState: DrawerState, scope: CoroutineScope = rememberCoroutineScope()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF2f80eb))
            .padding(start = 0.dp, end = 16.dp, bottom = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {scope.launch { drawerState.close()}}) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Header Icon",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Cylinder Management",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}




@Composable
fun StatusRow(component: HomeScreenComponent, cylinderDetailsList: List<Map<String, String>>, isLoading: Boolean, gasList:List<String>) {
    val statuses = listOf(
        Triple("Full", Color.White, Res.drawable.full),
        Triple("Empty", Color.White, Res.drawable.empty),
        Triple("Issued", Color.White, Res.drawable.issued),
        Triple("Repair", Color.White, Res.drawable.repair),
        Triple("At Plant", Color.White, Res.drawable.at_plant)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(statuses) { status ->
            Box(
                modifier = Modifier.clickable(enabled = !isLoading) {
                    component.onEvent(HomeScreenEvent.OnStatusClick(cylinderDetailsList, status.first,gasList))
                    // Handle click event here
                    println("Clicked on ${status.first}")
                }
            ) {
                StatusIcon(status.first, status.second, status.third)
            }
        }
    }
}


@Composable
fun StatusIcon(name: String, color: Color, imageRes: DrawableResource) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF2f80eb))
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                colorFilter = tint(color),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CylinderList(component: HomeScreenComponent, cylinders: List<Cylinder>, cylinderDetailsList: List<Map<String, String>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(cylinders) { cylinder ->
            CylinderCard(cylinder, component, cylinderDetailsList)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun getGasColor(gasName: String): Color {
    return when (gasName) {
        "Oxygen" -> Color(0xFF2196F3) // Blue
        "Carbon Dioxide" -> Color(0xFFFF9800) // Orange
        "Ammonia" -> Color(0xFF795548) // Brown
        "Argon" -> Color(0xFF9C27B0) // Purple
        "Nitrogen" -> Color(0xFF4CAF50) // Green
        "Hydrogen" -> Color(0xFFFF5722) // Deep Orange
        "Helium" -> Color(0xFF3F51B5) // Indigo
        "LPG" -> Color(0xFF009688) // Teal
        "Argon Specto" -> Color(0xFF673AB7) // Deep Purple
        "Zero Air" -> Color(0xFFE91E63) // Pink
        else -> Color.Gray // Default color for any other gases
    }
}

@Composable
fun CylinderCard(cylinder: Cylinder, component: HomeScreenComponent, cylinderDetailsList: List<Map<String, String>>) {


    val gasSortedList: List<Map<String, String>> = cylinderDetailsList.filter { it["Gas Type"] == cylinder.name }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { component.onEvent(HomeScreenEvent.OnGasCardClick(cylinder.name, gasSortedList)) }),
        elevation = 4.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(getGasColor(cylinder.name), shape = MaterialTheme.shapes.medium)
            ) {
                Text(
                    text = cylinder.symbol,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = cylinder.name, fontWeight = FontWeight.Bold)
                Text(text = "Full: ${cylinder.full}")
                Text(text = "Empty: ${cylinder.empty}")
                Text(text = "Sold: ${cylinder.sold}")
                Text(text = "Repair: ${cylinder.repair}")
                Text(text = "At Plant: ${cylinder.atPlant}")
            }
        }
    }
}


// Function to fetch cylinder data from Firestore


fun SortCylindersData(cylinderDetailsList: List<Map<String, String>>,gasDocuments: List<DocumentSnapshot>): List<Cylinder> {
    val cylindersList = mutableListOf<Cylinder>()


    // Parse the cylinder details into CylinderDetail objects
    val cylinderDetails = cylinderDetailsList.mapNotNull { map ->
        val gasName = map["Gas Type"]
        val status = map["Status"]
        if (gasName != null && status != null) {
            CylinderDetail(gasName, status)
        } else {
            null
        }
    }

    // Iterate over each gas and count the status
    gasDocuments.forEach { gasDocument ->
        val gasName = gasDocument.id
        var full = 0
        var empty = 0
        var sold = 0
        var repair = 0
        var atPlant = 0

        // Count the status for this gas from the cylinderDetails array
        cylinderDetails.forEach { cylinderDetail ->
            if (cylinderDetail.gasName == gasName) {
                when (cylinderDetail.status) {
                    "Full" -> full++
                    "Empty" -> empty++
                    "Sold" -> sold++
                    "Repair" -> repair++
                    "At Plant" -> atPlant++
                }
            }
        }

        // Add the cylinder to the list
        cylindersList.add(
            Cylinder(
                name = gasName,
                symbol = getGasSymbol(gasName),
                full = full,
                empty = empty,
                sold = sold,
                repair = repair,
                atPlant = atPlant
            )
        )
    }

    return cylindersList
}

fun getGasSymbol(gasName: String): String {
    // Return the appropriate symbol based on the gas name
    return when (gasName) {
        "Oxygen" -> "O₂"
        "Carbon Dioxide" -> "CO₂"
        "Ammonia" -> "NH₃"
        "Argon" -> "Ar"
        "Nitrogen" -> "N₂"
        "Hydrogen" -> "H"
        "Helium" -> "He"
        "LPG" -> "LPG"
        "Argon Specto" -> "AS"
        "Zero Air" -> "Z"
        else -> ""
    }
}


// Function to return the gas color (could be improved with a map or lookup)

@Serializable
data class Cylinder(
    val name: String,
    val symbol: String,
    val full: Int,
    val empty: Int,
    val sold: Int,
    val repair: Int,
    val atPlant: Int
)
@Serializable
data class CylinderDetail(
    val gasName: String,
    val status: String
)

suspend fun allCylinderDetails(db: FirebaseFirestore): List<Map<String, String>> {
    val cylindersList = mutableListOf<Cylinder>()

    // Fetch all gas names from the "Gases" collection
    val gasDocuments = db.collection("Gases").get().documents

    // Fetch the cylinder details from the "Cylinders" collection
    val cylinderDocument = db.collection("Cylinders").document("Cylinders").get()

    // Safely cast the CylinderDetails field to a list of maps
    val cylinderDetailsList =
        cylinderDocument.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
    println("CylinderDetails List: $cylinderDetailsList")

    return cylinderDetailsList
}

suspend fun gasDocuments(db: FirebaseFirestore): List<DocumentSnapshot> {
    // Fetch all gas names from the "Gases" collection
    val gasDocuments = db.collection("Gases").get().documents
    return gasDocuments
}

data class CylinderDetails(
    val BatchNumber: String,
    val GasType: String,
    val PreviousCustomers: List<Map<String, String>>? = emptyList(),
    val Remarks: String? = "NA",
    val SerialNumber: String,
    val Status: String,
    val VolumeType: String
)