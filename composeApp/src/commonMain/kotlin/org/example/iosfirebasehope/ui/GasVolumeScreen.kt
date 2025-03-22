package org.example.iosfirebasehope.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.text.input.ImeAction
import org.example.iosfirebasehope.navigation.components.GasVolumeScreenComponent
import org.example.iosfirebasehope.navigation.events.GasVolumeScreenEvent

data class VolumeStatusCounts(
    val volume: String,
    val full: Int,
    val empty: Int,
    val issued: Int,
    val repair: Int,
    val atPlant: Int
)

@Composable
fun GasVolumeScreenUI(
    gasId: String,
    db: FirebaseFirestore,
    cylinderDetailList: List<Map<String, String>>,
    component: GasVolumeScreenComponent
) {
    var volumes by remember { mutableStateOf(emptyList<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredCylinders by remember { mutableStateOf(cylinderDetailList) }
    var selectedStatus = remember { mutableStateOf("None") }
    var volumesAndSP by remember { mutableStateOf<Map<String, String>?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogCustomers by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var totalLpgCount by remember { mutableStateOf(0) }
    var selectedVolumeType by remember { mutableStateOf("") }
    var lpgFullMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var lpgEmptyMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(gasId) {
        try {
            val docSnapshot = db.collection("Gases").document(gasId).get()
            if (docSnapshot.exists) {
                val fetchedVolumesAndSP = docSnapshot.get("VolumesAndSP") as? Map<String, String>
                if (fetchedVolumesAndSP != null) {
                    volumesAndSP = fetchedVolumesAndSP
                    val formattedVolumes = fetchedVolumesAndSP.keys.map { it.replace(",", ".") }
                    volumes = formattedVolumes.toList()
                }
            }
        } catch (e: Exception) {
            println("Error fetching document: ${e.message}")
        }
    }
    LaunchedEffect(gasId) {
        if (gasId == "LPG") {
            try {
                val lpgDocument = db.collection("Cylinders").document("LPG").get()
                lpgFullMap = lpgDocument.get("LPGFull") as? Map<String, Int> ?: emptyMap()
                lpgEmptyMap = lpgDocument.get("LPGEmpty") as? Map<String, Int> ?: emptyMap()
                totalLpgCount = lpgFullMap.values.sum() + lpgEmptyMap.values.sum()
            } catch (e: Exception) {
                println("Error fetching total LPG count: ${e.message}")
            }
        }
    }
    LaunchedEffect(gasId) {
        if (gasId == "LPG") {
            try {
                val customersSnapshot = db.collection("Customers").document("LPG Issued").collection("Names").get()
                var count = 0
                for (customerDoc in customersSnapshot.documents) {
                    val quantities = customerDoc.get("Quantities") as? Map<String, String>
                    quantities?.values?.forEach { quantity ->
                        count += quantity.toIntOrNull() ?: 0
                    }
                }
                totalLpgCount = count
            } catch (e: Exception) {
                println("Error fetching total LPG count: ${e.message}")
            }
        }
    }

    val volumeStatusCounts = volumes.map { volume ->
        var full = 0
        var empty = 0
        var issued = 0
        var repair = 0
        var atPlant = 0

        cylinderDetailList.forEach { cylinderDetail ->
            if (cylinderDetail["Volume Type"] == volume) {
                when (cylinderDetail["Status"]) {
                    "Full" -> full++
                    "Empty" -> empty++
                    "Issued" -> issued++
                    "Repair" -> repair++
                    "At Plant" -> atPlant++
                }
            }
        }

        VolumeStatusCounts(volume, full, empty, issued, repair, atPlant)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { if (gasId=="LPG")
                {
                    Text("LPG Issued Details")}
                else {
                    Text("$gasId")
                }

                },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(GasVolumeScreenEvent.onBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (gasId == "LPG") {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Total LPG Issued: $totalLpgCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                volumesAndSP?.keys?.let { keys ->
                    items(keys.toList()) { volumeType ->
                        LPGVolumeCard(
                            volumeType = volumeType,
                            db = db,
                            lpgFullMap = lpgFullMap,
                            lpgEmptyMap = lpgEmptyMap,
                            onCardClick = { customers ->
                                dialogCustomers = customers
                                showDialog = true
                            },
                            onVolumeSelected = { selectedVolumeType = it }
                        )
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(volumeStatusCounts) { volumeStatus ->
                        VolumeCard(volumeStatus, cylinderDetailList, component, gasId)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    filteredCylinders = cylinderDetailList
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear Text"
                                    )
                                }
                            }
                        },
                        onValueChange = {
                            searchQuery = it
                            filteredCylinders = cylinderDetailList.filter { cylinder ->
                                cylinder["Serial Number"]?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true &&
                                        (selectedStatus.value.isEmpty() || cylinder["Status"] == selectedStatus.value)
                            }
                        },
                        placeholder = {
                            Text("Search by Serial Number")
                        },
                        modifier = Modifier
                            .width(280.dp)
                            .padding(end = 8.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF2f80eb),
                            unfocusedBorderColor = Color(0xFF2f80eb)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    //   var selectedStatus by remember { mutableStateOf("None") }

                    Box(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .width(120.dp).height(55.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedStatus.value.isEmpty()) "Filter" else selectedStatus.value,
                                color = if (selectedStatus.value.isEmpty()) Color.Gray else Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expanded = true }
                            )
                        }
                        LaunchedEffect(Unit) {
                            filteredCylinders = cylinderDetailList.filter { cylinder ->
                                (cylinder["Serial Number"]?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true) &&
                                        (selectedStatus.value.isEmpty() || cylinder["Status"] == selectedStatus.value)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(120.dp).padding(end = 16.dp)
                        ) {
                            val statuses = listOf("None", "Full", "Empty", "Repair", "Issued", "At Plant")
                            statuses.forEach { status ->
                                DropdownMenuItem(onClick = {
                                    selectedStatus.value = status
                                    expanded = false
                                    filteredCylinders = cylinderDetailList.filter { cylinder ->
                                        (cylinder["Serial Number"]?.contains(
                                            searchQuery,
                                            ignoreCase = true
                                        ) == true) &&
                                                (selectedStatus.value.isEmpty() || cylinder["Status"] == selectedStatus.value)
                                    }
                                }) {
                                    Text(text = status)
                                }
                            }
                        }
                    }
                }

                CylinderList(
                    cylinderDetailsList = filteredCylinders,
                    component = component,
                    volumesAndSP = volumesAndSP,
                    status = selectedStatus
                )
            }
        }

        if (showDialog) {
            CustomerDialog(
                customers = dialogCustomers,
                onDismiss = { showDialog = false },
                volumeType = selectedVolumeType
            )
        }
    }
}

@Composable
fun VolumeCard(volumeStatus: VolumeStatusCounts,cylinderDetailsList: List<Map<String, String>>,component: GasVolumeScreenComponent, gasId: String = "") {

    val filteredList= cylinderDetailsList.filter { cylinder ->
        cylinder["Volume Type"] == volumeStatus.volume
    }

    Card(
        modifier = Modifier
            .height(155.dp)
            .width(100.dp)
            .padding(end = 4.dp, start = 4.dp)
            .clickable(onClick = {
                // Handle click
                component.onEvent(GasVolumeScreenEvent.onGasCardClick(volumeStatus.volume, filteredList, gasId))
            }),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb)) // Custom border color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // Ensure items are centered
            // Add vertical padding to the entire column
        ) {
            Text(
                text = volumeStatus.volume.replace(",", "."),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(text = "Full: ${volumeStatus.full}", fontSize = 12.sp)
            Text(text = "Empty: ${volumeStatus.empty}", fontSize = 12.sp)
            Text(text = "Issued: ${volumeStatus.issued}", fontSize = 12.sp)
            Text(text = "Repair: ${volumeStatus.repair}", fontSize = 12.sp)
            Text(text = "At Plant: ${volumeStatus.atPlant}", fontSize = 12.sp)
        }
    }
}


@Composable
fun CylinderList(
    cylinderDetailsList: List<Map<String, String>>,
    modifier: Modifier = Modifier,
    component: GasVolumeScreenComponent,
    volumesAndSP: Map<String, Any>?,
    status: MutableState<String>
) {
    if (cylinderDetailsList.isEmpty()&&status.value!="None") {
        Text("No cylinders found.", modifier = Modifier.padding(16.dp))
    }
    else if(cylinderDetailsList.isEmpty()&&status.value=="None")
    {
        Text("Please Select a Filter.", modifier = Modifier.padding(16.dp))
    }
    else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(cylinderDetailsList) { cylinder ->
                CylinderDetailsCard2(listOf(cylinder), component = component, price = "", volumesAndSP = volumesAndSP)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CylinderDetailsCard2(
    cylinderDetailsList: List<Map<String, String>>,
    component: GasVolumeScreenComponent,
    price: String = "",
    volumesAndSP: Map<String, Any>?
) {
    val currentCylinderDetails = cylinderDetailsList.firstOrNull() ?: return // Early return if null


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {
                // Handle click
                println("Cylinder clicked: $currentCylinderDetails")
                component.onEvent(GasVolumeScreenEvent.OnCylinderClick(currentCylinderDetails = currentCylinderDetails))
            }),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb)) // Custom border color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val gasName = currentCylinderDetails["Gas Type"] ?: ""
            val gasSymbol = getGasSymbol(gasName)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .background(getGasColor(gasName), shape = MaterialTheme.shapes.medium)
                ) {
                    Text(
                        text = gasSymbol,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Define the keys to display in the desired order
                    val orderedKeys = listOf("Serial Number", "Volume Type", "Status")

                    // Iterate through the ordered keys and display their values
                    orderedKeys.forEach { key ->
                        val value = currentCylinderDetails[key] // Get the value for the current key
                        if (!value.isNullOrEmpty()) { // Check if the value is not null or empty
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "$key: ",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = value.replace(",", "."),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Display the Price field below Status
//                    val volumeType = currentCylinderDetails["Volume Type"] // Get the Volume Type from details
//                    if (volumesAndSP != null && volumeType != null) {
//                        val price = volumesAndSP[volumeType] // Get the price corresponding to the Volume Type
//                        if (price != null) {
//                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
//                                Text(
//                                    text = "Price: ",
//                                    fontWeight = FontWeight.Bold,
//                                    modifier = Modifier.weight(1f)
//                                )
//                                Text(
//                                    text = price.toString(), // Convert price to string
//                                    modifier = Modifier.weight(1f)
//                                )
//                            }
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun LPGVolumeCard(
    volumeType: String,
    db: FirebaseFirestore,
    lpgFullMap: Map<String, Int>,
    lpgEmptyMap: Map<String, Int>,
    onCardClick: (List<Pair<String, String>>) -> Unit,
    onVolumeSelected: (String) -> Unit
) {
    var customers by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0) }
    val fullCount = lpgFullMap[volumeType] ?: 0
    val emptyCount = lpgEmptyMap[volumeType] ?: 0

    LaunchedEffect(volumeType) {
        try {
            val customersList = mutableListOf<Pair<String, String>>()
            val customersSnapshot = db.collection("Customers").document("LPG Issued").collection("Names").get()
            var count = 0
            for (customerDoc in customersSnapshot.documents) {
                val quantities = customerDoc.get("Quantities") as? Map<String, String>
                quantities?.forEach { (volume, quantity) ->
                    if (volume == volumeType) {
                        customersList.add(customerDoc.id to quantity)
                        count += quantity.toIntOrNull() ?: 0
                    }
                }
            }
            customers = customersList
            totalCount = count
        } catch (e: Exception) {
            println("Error fetching customers: ${e.message}")
        }
    }

    Card(
        modifier = Modifier
            .height(170.dp)
            .width(170.dp)
            .padding(16.dp)
            .clickable(onClick = { onCardClick(customers); onVolumeSelected(volumeType) }),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = volumeType.replace(",", "."),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Full: $fullCount",
                color = Color.Black,
                fontSize = 14.sp
            )
            Text(
                text = "Empty: $emptyCount",
                color = Color.Black,
                fontSize = 14.sp
            )
            Text(
                text = "Issued: $totalCount",
                color = Color.Black,
                fontSize = 14.sp
            )

        }
    }
}

@Composable
fun CustomerDialog(
    customers: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    volumeType: String
) {
    val volumeTypeFormatted = volumeType.replace(",", ".")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Customers of $volumeTypeFormatted LPG",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2f80eb)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(customers) { (customerName, quantity) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = customerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = quantity,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}