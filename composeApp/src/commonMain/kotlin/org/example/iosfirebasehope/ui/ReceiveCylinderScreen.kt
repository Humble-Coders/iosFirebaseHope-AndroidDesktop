package org.example.iosfirebasehope.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.ReceiveCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.ReceiveCylinderScreenEvent
import kotlin.collections.plus


@Composable
fun ReceiveCylinderScreenUI(
    VendorName: String,
    db: FirebaseFirestore,
    component: ReceiveCylinderScreenComponent
) {
    // State variables
    val details = remember { mutableStateOf<Map<String, String>?>(null) }
    var creditValue by remember { mutableStateOf("0") } // Changed to var and initial value "0"
    val phoneNumberValue = remember { mutableStateOf<String?>(null) }
    var showReceiveCylinderDialog by remember { mutableStateOf(false) }
    var showReceiveLPGDialog by remember { mutableStateOf(false) }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var issuedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var issuedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var cylinderDetailsList by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // State for animation
    var showMainContent by remember { mutableStateOf(true) }

    // State for ReceiveDialog
    var showReceiveDialog by remember { mutableStateOf(false) }

    // Fetch Vendor details
    LaunchedEffect(VendorName) {
        val document = db.collection("Vendors")
            .document("Details")
            .collection("Names")
            .document(VendorName)
            .get()

        details.value = document.get("Details") as? Map<String, String>

        // Properly set credit value from details
        val vendorCredit = details.value?.get("Credit")
        creditValue = if (vendorCredit.isNullOrEmpty()) "0" else vendorCredit

        phoneNumberValue.value = details.value?.get("Phone Number")?.toString()

        // Fetch issued cylinders
        val issuedCylindersDoc = db.collection("Vendors")
            .document("Issued Cylinders")
            .collection("Names")
            .document(VendorName)
            .get()
        issuedCylinders = issuedCylindersDoc.get("Details") as? List<String> ?: emptyList()

        // Fetch issued LPG quantities
        val issuedLPGDoc = db.collection("Vendors")
            .document("LPG Issued")
            .collection("Names")
            .document(VendorName)
            .get()
        issuedLPGQuantities = issuedLPGDoc.get("Quantities") as? Map<String, Int> ?: emptyMap()
    }

    // Fetch cylinder details for selected serial numbers
    LaunchedEffect(selectedCylinders) {
        if (selectedCylinders.isNotEmpty()) {
            val cylindersDoc = db.collection("Cylinders").document("Cylinders").get()
            val cylinderDetails = cylindersDoc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
            cylinderDetailsList = cylinderDetails.filter { it["Serial Number"] in selectedCylinders }
        }
    }

    // Function to handle deletion of non-LPG cylinders
    val onDeleteNonLPG = { cylinderDetails: Map<String, String> ->
        // Remove the cylinder from the selectedCylinders list
        selectedCylinders = selectedCylinders.filter { it != cylinderDetails["Serial Number"] }
        cylinderDetailsList = cylinderDetailsList.filter { it["Serial Number"] != cylinderDetails["Serial Number"] }
    }

    // Function to handle deletion of LPG entries
    val onDeleteLPG = { volumeType: String ->
        // Remove the LPG entry from the selectedLPGQuantities map
        selectedLPGQuantities = selectedLPGQuantities - volumeType
    }

    // Function to handle the "Return LPG" button click
    val onReturnLPGClick = {
        showReceiveLPGDialog = true
    }

    // Combine LPG and non-LPG items into a single list
    val CombinedItemReceives = remember(cylinderDetailsList, selectedLPGQuantities) {
        val nonLPGItems = cylinderDetailsList.map { cylinderDetails ->
            CombinedItemReceive.NonLPG(cylinderDetails)
        }
        val lpgItems = selectedLPGQuantities.entries.map { (volumeType, quantity) ->
            CombinedItemReceive.LPG(volumeType, quantity)
        }
        nonLPGItems + lpgItems
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Surface(
            color = Color(0xFF2f80eb),
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { component.onEvent(ReceiveCylinderScreenEvent.OnBackClick) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Receive Cylinders",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Main content
        Scaffold(
            topBar = { Box {} }
        ) { innerPadding ->
            // Wrap the entire main content in AnimatedVisibility
            AnimatedVisibility(
                visible = showMainContent,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // Vendor details box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, top = 64.dp) // Reduced bottom padding
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = VendorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            Divider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(0.3f)) {
                                    Text(text = "Phone:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Credit:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    phoneNumberValue.value?.let { Text(text = it, fontSize = 14.sp) }
                                    Text(text = creditValue, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // Row for "Return Cylinders" and "Return LPG" buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // "Return Cylinders" button
                        Button(
                            onClick = {
                                showMainContent = false
                                showReceiveCylinderDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text(text = "Receive Cylinders", fontSize = 12.sp, color = Color.White)
                        }

                        // "Return LPG" button
                        Button(
                            onClick = {
                                showMainContent = false
                                onReturnLPGClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text(text = "Receive LPG", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    // "Cylinders for Return" text and divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cylinders for Receiving",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Divider(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp),
                            color = Color.Gray
                        )
                    }

                    // Show selected cylinders count separately in a new row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Selected Cylinders: ${selectedCylinders.size}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Combined LazyColumn for LPG and non-LPG items
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Ensure the Box takes remaining space
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(CombinedItemReceives) { item ->
                                when (item) {
                                    is CombinedItemReceive.NonLPG -> {
                                        NonLPGReturnCardReceiveReceive(
                                            cylinderDetails = item.cylinderDetails,
                                            onDelete = { onDeleteNonLPG(item.cylinderDetails) }
                                        )
                                    }
                                    is CombinedItemReceive.LPG -> {
                                        LPGReturnCardReceive(
                                            volumeType = item.volumeType,
                                            quantity = item.quantity,
                                            onDelete = { onDeleteLPG(item.volumeType) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Animate the "Return" button at the bottom
                    AnimatedVisibility(
                        visible = showMainContent,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Button(
                            onClick = {
                                showReceiveDialog = true // Show the Return dialog
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                        ) {
                            Text(text = "Receive", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Return Cylinder Dialog (outside AnimatedVisibility)
        if (showReceiveCylinderDialog) {
            ReceiveCylinderDialog(
                onDismiss = {
                    showReceiveCylinderDialog = false
                    showMainContent = true // Show main content with animation
                },
                onDone = { cylinders ->
                    // Update selectedCylinders
                    selectedCylinders = (selectedCylinders + cylinders).distinct()
                    showMainContent = true // Show main content with animation
                },
                db = db,
                VendorName = VendorName,
                alreadySelectedCylinders = selectedCylinders
            )
        }

        // Return LPG Dialog (outside AnimatedVisibility)
        if (showReceiveLPGDialog) {
            if (issuedLPGQuantities.isEmpty()) {
                // Show dialog if no LPG is issued
                AlertDialog(
                    onDismissRequest = {
                        showReceiveLPGDialog = false
                        showMainContent = true // Show main content with animation
                    },
                    title = { Text("Return LPG") },
                    text = { Text("No LPG issued to $VendorName") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showReceiveLPGDialog = false
                                showMainContent = true // Show main content with animation
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text("OK", color = Color.White)
                        }
                    }
                )
            } else {
                // Show the Return LPG dialog
                ReceiveLPGDialog(
                    onDismiss = {
                        showReceiveLPGDialog = false
                        showMainContent = true
                    },
                    onDone = { volumeType, quantity ->
                        // Update selectedLPGQuantities by incrementing the existing quantity
                        val currentQuantity = selectedLPGQuantities[volumeType] ?: 0
                        selectedLPGQuantities = selectedLPGQuantities + mapOf(volumeType to (currentQuantity + quantity))
                    },
                    quantities = issuedLPGQuantities,
                    VendorName = VendorName,
                    alreadySelectedLPGQuantities = selectedLPGQuantities // Pass the already selected LPG quantities
                )
            }
        }

        // Return Dialog (outside AnimatedVisibility)
        if (showReceiveDialog) {
            ReceiveDialog(
                VendorName = VendorName,
                credit = creditValue,
                selectedCylinders = cylinderDetailsList + selectedLPGQuantities.map { (volumeType, quantity) ->
                    mapOf("Volume Type" to volumeType, "Quantity" to quantity.toString())
                },
                onDismiss = { showReceiveDialog = false },
                onReturn = {
                    // Handle any additional logic after successful return
                },
                db = db,
                component = component
            )
        }
    }
}


// Sealed class to represent combined items
sealed class CombinedItemReceive {
    data class NonLPG(val cylinderDetails: Map<String, String>) : CombinedItemReceive()
    data class LPG(val volumeType: String, val quantity: Int) : CombinedItemReceive()
}




@Composable
fun NonLPGReturnCardReceiveReceive(
    cylinderDetails: Map<String, String>,
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // State for delete confirmation dialog


    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Cylinder") },
            text = { Text("Are you sure you want to delete this cylinder?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete() // Trigger the delete action
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display gas symbol and color
            val gasName = cylinderDetails["Gas Type"] ?: ""
            val gasSymbol = getGasSymbol1(gasName)
            val gasColor = getGasColor1(gasName)


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp) // Smaller size for the gas symbol box
                        .background(gasColor, shape = MaterialTheme.shapes.medium)
                ) {
                    Text(
                        text = gasSymbol,
                        color = Color.White,
                        fontSize = 20.sp, // Smaller font size for the gas symbol
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp)) // Reduced spacing


                // Column for cylinder details
                Column(
                    modifier = Modifier
                        .weight(1f) // Take remaining space
                        .padding(end = 8.dp) // Add padding to separate from the bin icon
                ) {
                    // Define the keys to display in the desired order
                    val orderedKeys = listOf("Serial Number", "Volume Type", "Issue Date", "Issued At Price")


                    // Iterate through the ordered keys and display their values
                    orderedKeys.forEach { key ->
                        val value = cylinderDetails[key] // Get the value for the current key
                        if (!value.isNullOrEmpty()) { // Check if the value is not null or empty
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "$key: ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp, // Smaller font size for labels
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = value.replace(",", "."),
                                    fontSize = 12.sp, // Smaller font size for values
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }


                // Bin icon (right side)
                IconButton(
                    onClick = { showDeleteConfirmation = true }, // Show delete confirmation dialog
                    modifier = Modifier.size(40.dp) // Smaller size for the bin icon
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}


@Composable
fun LPGReturnCardReceive(
    volumeType: String,
    quantity: Int,
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // State for delete confirmation dialog


    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete LPG") },
            text = { Text("Are you sure you want to delete this LPG entry?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete() // Trigger the delete action
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display LPG symbol and color
            val gasName = "LPG"
            val gasSymbol = getGasSymbol1(gasName)
            val gasColor = getGasColor1(gasName)


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp) // Smaller size for the gas symbol box
                        .background(gasColor, shape = MaterialTheme.shapes.medium)
                ) {
                    Text(
                        text = gasSymbol,
                        color = Color.White,
                        fontSize = 20.sp, // Smaller font size for the gas symbol
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp)) // Reduced spacing


                // Column for LPG details
                Column(
                    modifier = Modifier
                        .weight(1f) // Take remaining space
                        .padding(end = 8.dp) // Add padding to separate from the bin icon
                ) {
                    Text(
                        text = "LPG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp // Smaller font size for the title
                    )
                    Text(
                        text = "Volume Type: $volumeType",
                        fontSize = 12.sp // Smaller font size for details
                    )
                    Text(
                        text = "Quantity: $quantity",
                        fontSize = 12.sp // Smaller font size for details
                    )
                }


                // Bin icon (right side)
                IconButton(
                    onClick = { showDeleteConfirmation = true }, // Show delete confirmation dialog
                    modifier = Modifier.size(40.dp) // Smaller size for the bin icon
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}




@Composable
fun ReceiveCylinderDialog(
    onDismiss: () -> Unit,
    onDone: (List<String>) -> Unit,
    db: FirebaseFirestore,
    VendorName: String,
    alreadySelectedCylinders: List<String>
) {
    var selectedCylinders by remember { mutableStateOf(mutableListOf("")) }
    var cylinderOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showValidationMessage by remember { mutableStateOf(false) }
    var allCylinders by remember { mutableStateOf<List<String>>(emptyList()) } // Store all available cylinders


    // Fetch cylinder options
    LaunchedEffect(Unit) {
        val issuedCylindersDoc = db.collection("Vendors")
            .document("Issued Cylinders")
            .collection("Names")
            .document(VendorName)
            .get()
        allCylinders = issuedCylindersDoc.get("Details") as? List<String> ?: emptyList()
        cylinderOptions = allCylinders
    }


    // Filter out already selected cylinders from the options
    val availableCylinderOptions = remember(cylinderOptions, selectedCylinders, alreadySelectedCylinders) {
        cylinderOptions.filter {
            it !in alreadySelectedCylinders &&
                    it !in selectedCylinders.filter { cylinder -> cylinder.isNotEmpty() }
        }
    }


    // Hide validation message after 3 seconds
    LaunchedEffect(showValidationMessage) {
        if (showValidationMessage) {
            delay(3000)
            showValidationMessage = false
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top transparent spacer
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }


        // Dialog box content
        item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colors.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Return Cylinders", fontWeight = FontWeight.Bold, fontSize = 20.sp)


                        // NEW: Receive All button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    // Filter out already selected cylinders
                                    val availableToSelect = allCylinders.filter {
                                        it !in alreadySelectedCylinders
                                    }


                                    if (availableToSelect.isNotEmpty()) {
                                        // Directly call onDone with all available cylinders
                                        onDone(availableToSelect)
                                        onDismiss()
                                    } else {
                                        showValidationMessage = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF4CAF50) // Green color
                                ),
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Receive All",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Receive All (${
                                            allCylinders.filter {
                                                it !in alreadySelectedCylinders
                                            }.size
                                        })",
                                        color = Color.White
                                    )
                                }
                            }
                        }


                        Divider(modifier = Modifier.padding(vertical = 8.dp))


                        // Cylinders Selection Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Select Individual Cylinders", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    selectedCylinders = selectedCylinders.toMutableList().apply {
                                        add("")
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Cylinder",
                                    tint = Color(0xFF2f80eb)
                                )
                            }
                        }


                        // Dynamically generated cylinder selection fields
                        selectedCylinders.forEachIndexed { index, _ ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SearchableDropdown31(
                                    modifier = Modifier.weight(1f),
                                    options = availableCylinderOptions,
                                    selectedItem = selectedCylinders[index],
                                    onItemSelected = { selectedItem ->
                                        selectedCylinders = selectedCylinders.toMutableList().apply {
                                            this[index] = selectedItem
                                        }
                                    },
                                    placeholder = "Select Cylinder",
                                    keyboardType = KeyboardType.Number
                                )


                                // Delete icon for each cylinder selection
                                if (selectedCylinders.size > 1) {
                                    IconButton(
                                        onClick = {
                                            selectedCylinders = selectedCylinders.toMutableList().apply {
                                                removeAt(index)
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Cylinder",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }


                        // Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Validation message
                            if (showValidationMessage) {
                                Text(
                                    text = if (allCylinders.filter { it !in alreadySelectedCylinders }.isEmpty())
                                        "No cylinders available to receive"
                                    else
                                        "All fields are necessary",
                                    color = Color.Red,
                                    modifier = Modifier.padding(end = 8.dp),
                                    fontSize = 12.sp
                                )
                            }


                            // Cancel Button
                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }


                            Spacer(modifier = Modifier.width(8.dp))


                            // Done Button
                            Button(
                                onClick = {
                                    // Check if all fields are filled
                                    if (selectedCylinders.any { it.isEmpty() }) {
                                        showValidationMessage = true
                                    } else {
                                        // Filter out empty selections
                                        val nonEmptyCylinders = selectedCylinders.filter { it.isNotEmpty() }
                                        onDone(nonEmptyCylinders)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Done", color = Color.White)
                            }
                        }
                    }
                }
            }
        }


        // Bottom transparent spacer
        item {
            Spacer(modifier = Modifier.height(500.dp))
        }
    }
}


@Composable
fun ReceiveLPGDialog(
    onDismiss: () -> Unit,
    onDone: (volumeType: String, quantity: Int) -> Unit,
    quantities: Map<String, Int>,
    VendorName: String,
    alreadySelectedLPGQuantities: Map<String, Int> // Pass the already selected LPG quantities
) {
    var volumeType by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var showValidationMessage by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf<String?>(null) }


    // Auto-dismiss the quantity error message after 4 seconds
    LaunchedEffect(quantityError) {
        if (quantityError != null) {
            delay(4000) // 4 seconds
            quantityError = null
        }
    }


    // Auto-dismiss the validation message after 4 seconds
    LaunchedEffect(showValidationMessage) {
        if (showValidationMessage) {
            delay(4000) // 4 seconds
            showValidationMessage = false
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top transparent spacer
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }


        // Dialog box content
        item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colors.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Return LPG", fontWeight = FontWeight.Bold, fontSize = 20.sp)


                        // Display all quantities in the format "key: value cylinders issued"
                        quantities.forEach { (key, value) ->
                            Text(
                                text = "$key: $value cylinders issued",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }


                        Spacer(modifier = Modifier.height(16.dp))


                        // Volume Type Dropdown
                        Text("Volume Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdown3(
                            options = quantities.keys.toList(),
                            selectedItem = volumeType,
                            onItemSelected = { volumeType = it },
                            placeholder = "Select Volume Type",
                            keyboardType = KeyboardType.Number
                        )


                        // Quantity Input
                        Text("Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )


                        // Display quantity error message in two lines
                        if (quantityError != null) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = quantityError!!.split("\n")[0], // First line
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = quantityError!!.split("\n")[1], // Second line
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }


                        // Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Validation message
                            if (showValidationMessage) {
                                Text(
                                    text = "All fields are required",
                                    color = Color.Red,
                                    modifier = Modifier.padding(end = 8.dp),
                                    fontSize = 12.sp
                                )
                            }


                            // Cancel Button
                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }


                            Spacer(modifier = Modifier.width(8.dp))


                            // Done Button
                            Button(
                                onClick = {
                                    // Validate inputs
                                    if (volumeType.isNullOrEmpty() || quantity.isEmpty()) {
                                        showValidationMessage = true // Show validation message if fields are empty
                                    } else {
                                        val enteredQuantity = quantity.toIntOrNull() ?: 0
                                        val availableQuantity = quantities[volumeType!!] ?: 0
                                        val alreadySelectedQuantity = alreadySelectedLPGQuantities[volumeType!!] ?: 0


                                        if (enteredQuantity + alreadySelectedQuantity > availableQuantity) {
                                            // Set the error message in two lines
                                            quantityError = "Quantity cannot exceed $availableQuantity\nCylinders already in cart: $alreadySelectedQuantity"
                                        } else {
                                            onDone(volumeType!!, enteredQuantity)
                                            onDismiss()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Done", color = Color.White)
                            }
                        }
                    }
                }
            }
        }


        // Bottom transparent spacer
        item {
            Spacer(modifier = Modifier.height(500.dp))
        }
    }
}




@Composable
fun ReceiveDialog(
    VendorName: String,
    credit: String?,
    selectedCylinders: List<Map<String, String>>,
    onDismiss: () -> Unit,
    onReturn: (updatedDeposit: String) -> Unit,
    db: FirebaseFirestore,
    component: ReceiveCylinderScreenComponent
) {
    var cashOut by remember { mutableStateOf("") }
    var creditInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Parse current credit to double, defaulting to 0.0 if null or not a number
    val currentCredit = credit?.toDoubleOrNull() ?: 0.0

    // Calculate credit after cashOut deduction
    val creditAfterCashOut = remember(currentCredit, cashOut) {
        val cashOutValue = cashOut.toDoubleOrNull() ?: 0.0
        maxOf(0.0, currentCredit - cashOutValue)
    }

    // Calculate final credit after adding creditInput
    val finalCredit = remember(creditAfterCashOut, creditInput) {
        val creditInputValue = creditInput.toDoubleOrNull() ?: 0.0
        creditAfterCashOut + creditInputValue
    }

    AlertDialog(
        onDismissRequest = { if(isLoading) {} else onDismiss },
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(VendorName, fontSize = 20.sp)
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF2f80eb)
                    )
                }
            }
        },
        text = {
            Column {
                // Current credit display
                Text(
                    text = "Current Credit: ${credit ?: "0"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Cash out field - deducts from credit
                OutlinedTextField(
                    value = cashOut,
                    onValueChange = { cashOut = it },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Cash out",
                                tint = Color(0xAAD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash out (deducts from credit)", color = Color(0xAAD32F2F))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color(0xFFD32F2F).copy(alpha = 0.5f)
                    )
                )

                // Show credit after cash out deduction if cash out value is entered
                if (cashOut.isNotEmpty() && cashOut != "0") {
                    Text(
                        text = "Credit after cash out: $creditAfterCashOut",
                        fontSize = 12.sp,
                        color = Color(0xAAD32F2F),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                }

                // Add credit field
                OutlinedTextField(
                    value = creditInput,
                    onValueChange = { creditInput = it },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Credit",
                                tint = Color(0xFF388E3C),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Credit", color = Color(0xFF388E3C))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF388E3C),
                        unfocusedBorderColor = Color(0xFF388E3C).copy(alpha = 0.5f)
                    )
                )

                // Final summary box showing the resulting credit balance
                if ((cashOut.isNotEmpty() && cashOut != "0") || (creditInput.isNotEmpty() && creditInput != "0")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        backgroundColor = Color(0xFFF1F8E9)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Summary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Starting Credit: $currentCredit",
                                fontSize = 12.sp
                            )
                            if (cashOut.isNotEmpty() && cashOut != "0") {
                                Text(
                                    text = "Cash Out: -${cashOut.toDoubleOrNull() ?: 0.0}",
                                    fontSize = 12.sp,
                                    color = Color(0xAAD32F2F)
                                )
                            }
                            if (creditInput.isNotEmpty() && creditInput != "0") {
                                Text(
                                    text = "Credit Added: +${creditInput.toDoubleOrNull() ?: 0.0}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF388E3C)
                                )
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Final Credit: $finalCredit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    // Launch a coroutine to handle the Firestore updates
                    CoroutineScope(Dispatchers.IO).launch {
                        CustomCheckboxReceive(
                            db = db,
                            selectedCylinders = selectedCylinders,
                            VendorName = VendorName,
                            cashOut = cashOut,
                            creditInput = creditInput,
                            onSuccess = {
                                // Call the original onReturn callback
                                component.onEvent(ReceiveCylinderScreenEvent.OnConfirmClick)
                            },
                            onFailure = { e ->
                                println("Failed to update Firestore: $e")
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                Text("Receive", color = Color.White, fontSize = 14.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text("Cancel", color = Color.White, fontSize = 14.sp)
            }
        }
    )
}


suspend fun CustomCheckboxReceive(
    db: FirebaseFirestore,
    selectedCylinders: List<Map<String, String>>,
    VendorName: String,
    cashOut: String,
    creditInput: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    try {
        // Pre-process data for efficiency
        val nonLpgCylinders = selectedCylinders.filter { it.containsKey("Gas Type") }
        val lpgCylinders = selectedCylinders.filter { !it.containsKey("Gas Type") }
        println("Non-LPG Cylinders: $nonLpgCylinders")
        println("LPG Cylinders: $lpgCylinders")

        // Get current date and time information once
        val currentDateTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
            .replace("T", "_")
            .replace(":", "-")
            .substringBefore(".")

        val currentDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()

        // Extract serialNumbers and prepare LPG data for batch operations
        val serialNumbers = nonLpgCylinders.mapNotNull { it["Serial Number"] }
        val lpgVolumeUpdates = lpgCylinders.groupBy {
            (it["Volume Type"] ?: "").replace(".", ",")
        }.mapValues { (_, cylinders) ->
            cylinders.sumOf { it["Quantity"]?.toIntOrNull() ?: 0 }
        }

        // Prepare cylinders returned data
        val cylindersReturned = nonLpgCylinders.map { cylinder ->
            mapOf(
                "Serial Number" to cylinder["Serial Number"],
                "Return Date" to currentDate
            )
        }

        // Execute parallel operations with coroutineScope
        coroutineScope {
            // TASK 1: Create transaction record with all details in a batch
            val createTransactionTask = async {
                val transactionsRef = db.collection("TransactionVendor")
                    .document(VendorName)
                    .collection("DateAndTime")
                    .document(currentDateTime)

                val batch = db.batch()

                // Main transaction document
                batch.set(transactionsRef, mapOf("Date" to currentDate))

                // Transaction details
                val transactionDetailsRef = transactionsRef.collection("Transaction Details")

                batch.set(transactionDetailsRef.document("Cash Out"), mapOf("Amount" to cashOut))
                batch.set(transactionDetailsRef.document("Credit"), mapOf("Amount" to creditInput))
                batch.set(transactionDetailsRef.document("Cylinders Issued"),
                    mapOf("CylindersIssued" to emptyList<Map<String, String>>()))
                batch.set(transactionDetailsRef.document("Cylinders Returned"),
                    mapOf("CylindersReturned" to cylindersReturned))
                batch.set(transactionDetailsRef.document("Inventory Issued"),
                    mapOf("InventoryIssued" to emptyList<Map<String, String>>()))
                batch.set(transactionDetailsRef.document("LPG Issued"),
                    mapOf("LPGIssued" to emptyList<Map<String, String>>()))
                batch.set(transactionDetailsRef.document("LPG Returned"),
                    mapOf("LPGReturned" to lpgVolumeUpdates))

                batch.commit()
            }

            // TASK 2: Update CylinderDetails array for all non-LPG cylinders
            val updateCylinderDetailsTask = async {
                if (serialNumbers.isNotEmpty()) {
                    val cylindersRef = db.collection("Cylinders").document("Cylinders")
                    val snapshot = cylindersRef.get()

                    if (snapshot.exists) {
                        val cylinderDetails = snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

                        // Create a set for faster lookups
                        val serialNumberSet = serialNumbers.toSet()

                        // Update the fields for matching cylinders
                        val updatedCylinderDetails = cylinderDetails.map { details ->
                            if (details["Serial Number"] in serialNumberSet) {
                                details.toMutableMap().apply {
                                    this["Status"] = "Full"
                                    this["Issue Date"] = ""
                                    this["Issued At Price"] = ""
                                    this["Notifications Date"] = ""
                                    this["Rent"] = "0"
                                }
                            } else {
                                details
                            }
                        }

                        // Update in a single operation
                        cylindersRef.update("CylinderDetails" to updatedCylinderDetails)
                    }
                }
            }

            // TASK 3: Update Issued Cylinders array for vendor
            val updateIssuedCylindersTask = async {
                if (serialNumbers.isNotEmpty()) {
                    val issuedCylindersRef = db.collection("Vendors")
                        .document("Issued Cylinders")
                        .collection("Names")
                        .document(VendorName)

                    val issuedCylindersSnapshot = issuedCylindersRef.get()

                    if (issuedCylindersSnapshot.exists) {
                        val detailsArray = issuedCylindersSnapshot.get("Details") as? List<String> ?: emptyList()

                        // Remove all serial numbers in one operation
                        val updatedDetailsArray = detailsArray.filter { it !in serialNumbers }

                        // Update in a single operation
                        issuedCylindersRef.update("Details" to updatedDetailsArray)
                    }
                }
            }

            // TASK 4: Update LPG quantities
            val updateLpgTask = async {
                if (lpgVolumeUpdates.isNotEmpty()) {
                    // Task 4.1: Update LPG Full map
                    val lpgFullTask = async {
                        val lpgRef = db.collection("Cylinders").document("LPG")
                        val lpgSnapshot = lpgRef.get()

                        if (lpgSnapshot.exists) {
                            val lpgFullMap = lpgSnapshot.get("LPGFull") as? Map<String, Int> ?: emptyMap()
                            val updatedLpgFullMap = lpgFullMap.toMutableMap()

                            // Update all volume types at once
                            lpgVolumeUpdates.forEach { (volumeTypeKey, quantity) ->
                                updatedLpgFullMap[volumeTypeKey] = (updatedLpgFullMap[volumeTypeKey] ?: 0) + quantity
                            }

                            lpgRef.update("LPGFull" to updatedLpgFullMap)
                        }
                    }

                    // Task 4.2: Update vendor's LPG Quantities
                    val vendorLpgTask = async {
                        val lpgIssuedRef = db.collection("Vendors")
                            .document("LPG Issued")
                            .collection("Names")
                            .document(VendorName)

                        val lpgIssuedSnapshot = lpgIssuedRef.get()

                        if (lpgIssuedSnapshot.exists) {
                            val quantitiesMap = lpgIssuedSnapshot.get("Quantities") as? Map<String, Int> ?: emptyMap()
                            val updatedQuantitiesMap = quantitiesMap.toMutableMap()

                            // Update all volume types at once
                            lpgVolumeUpdates.forEach { (volumeTypeKey, quantity) ->
                                val currentQty = updatedQuantitiesMap[volumeTypeKey] ?: 0
                                updatedQuantitiesMap[volumeTypeKey] = maxOf(0, currentQty - quantity)
                            }

                            lpgIssuedRef.update("Quantities" to updatedQuantitiesMap)
                        }
                    }

                    // Wait for both LPG tasks to complete
                    awaitAll(lpgFullTask, vendorLpgTask)
                }
            }

            // TASK 5: Update vendor credit - MODIFIED THIS TASK
            val updateVendorCreditTask = async {
                val vendorDetailsRef = db.collection("Vendors")
                    .document("Details")
                    .collection("Names")
                    .document(VendorName)

                val vendorDetailsSnapshot = vendorDetailsRef.get()

                if (vendorDetailsSnapshot.exists) {
                    val detailsMap = vendorDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

                    // Parse all values to ensure we handle them as numbers
                    val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
                    val cashOutValue = cashOut.toDoubleOrNull() ?: 0.0
                    val creditInputValue = creditInput.toDoubleOrNull() ?: 0.0

                    // Calculate new credit: (current credit - cash out) + new credit input
                    // This deducts cash out and adds the credit input
                    val newCredit = (currentCredit - cashOutValue) + creditInputValue

                    // Ensure credit never goes below 0
                    val finalCredit = maxOf(0.0, newCredit)

                    // Update in a single operation
                    val updatedDetailsMap = detailsMap.toMutableMap().apply {
                        this["Credit"] = finalCredit.toString()
                    }

                    vendorDetailsRef.update("Details" to updatedDetailsMap)
                }
            }

            // Wait for all tasks to complete
            awaitAll(
                createTransactionTask,
                updateCylinderDetailsTask,
                updateIssuedCylindersTask,
                updateLpgTask,
                updateVendorCreditTask
            )
        }

        // Call onSuccess after all operations complete
        onSuccess()
    } catch (e: Exception) {
        println("Error in CustomCheckboxReceive: ${e.message}")
        onFailure(e)
    }
}


//suspend fun CustomCheckboxReceive(
//    db: FirebaseFirestore,
//    selectedCylinders: List<Map<String, String>>,
//    VendorName: String,
//    cashOut: String,
//    creditInput: String,// New parameter for the updated deposit value
//    onSuccess: () -> Unit,
//    onFailure: (Exception) -> Unit
//) {
//    // Filter out non-LPG cylinders
//    val nonLpgCylinders = selectedCylinders.filter { cylinder ->
//        cylinder.containsKey("Gas Type") // Non-LPG cylinders have the "Gas Type" field
//    }
//    println("Non-LPG Cylinders: $nonLpgCylinders")
//
//    // Filter LPG cylinders
//    val lpgCylinders = selectedCylinders.filter { cylinder ->
//        !cylinder.containsKey("Gas Type") // LPG cylinders do not have the "Gas Type" field
//    }
//    println("LPG Cylinders: $lpgCylinders")
//
//    try {
//        // Get the current date and time in the format "yyyy-MM-dd_HH:mm:ss"
//        val currentDateTime = Clock.System.now()
//            .toLocalDateTime(TimeZone.currentSystemDefault())
//            .toString()
//            .replace("T", "_")
//            .replace(":", "-")
//            .substringBefore(".")
//
//        // Get only the date in the format "yyyy-MM-dd"
//        val currentDate = Clock.System.now()
//            .toLocalDateTime(TimeZone.currentSystemDefault())
//            .date
//            .toString()
//
//        // Reference to the Transactions collection
//        val transactionsRef = db.collection("TransactionVendor")
//            .document(VendorName)
//            .collection("DateAndTime")
//            .document(currentDateTime)
//
//        transactionsRef.set(mapOf("Date" to currentDate))
//
//
//        // Create the "Transaction Details" collection
//        val transactionDetailsRef = transactionsRef.collection("Transaction Details")
//
//        // Push Cash In document with the user-provided value
//
//        // Push Cash Out document with the user-provided value
//        transactionDetailsRef.document("Cash Out").set(mapOf("Amount" to cashOut))
//
//        // Push Credit document with the user-provided value
//        transactionDetailsRef.document("Credit").set(mapOf("Amount" to creditInput))
//
//        // Push Cylinders Issued document (empty array)
//        transactionDetailsRef.document("Cylinders Issued").set(mapOf("CylindersIssued" to emptyList<Map<String, String>>()))
//
//        // Push Cylinders Returned document
//        val cylindersReturned = nonLpgCylinders.map { cylinder ->
//            mapOf(
//                "Serial Number" to cylinder["Serial Number"],
//                "Return Date" to currentDate // Use only the date here
//            )
//        }
//        transactionDetailsRef.document("Cylinders Returned").set(mapOf("CylindersReturned" to cylindersReturned))
//
//        // Push Inventory Issued document (empty array)
//        transactionDetailsRef.document("Inventory Issued").set(mapOf("InventoryIssued" to emptyList<Map<String, String>>()))
//
//        // Push LPG Issued document (empty array)
//        transactionDetailsRef.document("LPG Issued").set(mapOf("LPGIssued" to emptyList<Map<String, String>>()))
//
//        // Create a map for LPG Returned
//        val lpgReturnedMap = mutableMapOf<String, Int>()
//        for (cylinder in lpgCylinders) {
//            val volumeType = cylinder["Volume Type"] ?: continue
//            val quantity = cylinder["Quantity"]?.toIntOrNull() ?: continue
//
//            // Replace "." with "," in the volume type key
//            val volumeTypeKey = volumeType.replace(".", ",")
//
//            // Add the quantity to the map
//            lpgReturnedMap[volumeTypeKey] = (lpgReturnedMap[volumeTypeKey] ?: 0) + quantity
//        }
//
//        // Push LPG Returned document with the map
//        transactionDetailsRef.document("LPG Returned").set(mapOf("LPGReturned" to lpgReturnedMap))
//
//        // Perform updates for each non-LPG cylinder
//        for (cylinder in nonLpgCylinders) {
//            val serialNumber = cylinder["Serial Number"] ?: continue
//
//
//
//            // Step 2: Update the "CylinderDetails" array in the "Cylinders" document
//            val cylindersRef = db.collection("Cylinders").document("Cylinders")
//
//            // Fetch existing CylinderDetails array
//            val snapshot = cylindersRef.get()
//            if (snapshot.exists) {
//                val cylinderDetails = snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
//
//                // Update the fields for the matching cylinder
//                val updatedCylinderDetails = cylinderDetails.map { details ->
//                    if (details["Serial Number"] == serialNumber) {
//                        // Create a mutable copy of the map and update fields
//                        val updatedDetails = details.toMutableMap().apply {
//                            this["Status"] = "Full"
//                            this["Issue Date"] = ""
//                            this["Issued At Price"] = ""
//                            this["Notifications Date"] = ""
//                            this["Rent"] = "0"
//                        }
//                        updatedDetails
//                    } else {
//                        details
//                    }
//                }
//
//                // Save the updated array back to Firestore
//                cylindersRef.update("CylinderDetails" to updatedCylinderDetails)
//                println("Successfully updated 'CylinderDetails' for cylinder $serialNumber")
//            } else {
//                throw Exception("Document 'Cylinders' does not exist")
//            }
//
//            // Step 3: Remove the serial number from the "Details" array in the "Issued Cylinders" document
//            val issuedCylindersRef = db.collection("Vendors")
//                .document("Issued Cylinders")
//                .collection("Names")
//                .document(VendorName)
//
//            // Fetch the existing "Details" array
//            val issuedCylindersSnapshot = issuedCylindersRef.get()
//            if (issuedCylindersSnapshot.exists) {
//                val detailsArray = issuedCylindersSnapshot.get("Details") as? List<String> ?: emptyList()
//
//                // Remove the serial number from the array
//                val updatedDetailsArray = detailsArray.filter { it != serialNumber }
//
//                // Update the document with the new array
//                issuedCylindersRef.update("Details" to updatedDetailsArray)
//                println("Successfully removed $serialNumber from 'Details' array for Vendor $VendorName")
//            } else {
//                throw Exception("Document 'Issued Cylinders > Names > $VendorName' does not exist")
//            }
//        }
//
//        // Perform updates for each LPG cylinder
//        for (cylinder in lpgCylinders) {
//            val volumeType = cylinder["Volume Type"] ?: continue
//            val quantity = cylinder["Quantity"]?.toIntOrNull() ?: continue
//            println("Volume Type: $volumeType, Quantity: $quantity")
//
//            // Replace "." with "," in the volume type key
//            val volumeTypeKey = volumeType.replace(".", ",")
//            println("Volume Type Key: $volumeTypeKey")
//
//            // Step 4: Update the "LPGEmpty" map in the "Cylinders > LPG" document
//            val lpgRef = db.collection("Cylinders").document("LPG")
//
//            // Fetch the existing "LPGEmpty" map
//            val lpgSnapshot = lpgRef.get()
//            if (lpgSnapshot.exists) {
//                val lpgFullMap = lpgSnapshot.get("LPGFull") as? Map<String, Int> ?: emptyMap()
//
//                // Increment the value for the volume type key
//                val updatedLpgFullMap = lpgFullMap.toMutableMap().apply {
//                    this[volumeTypeKey] = (this[volumeTypeKey] ?: 0) + quantity
//                }
//                println("Updated LPGEmpty Map: $updatedLpgFullMap")
//
//                // Save the updated map back to Firestore
//                lpgRef.update("LPGFull" to updatedLpgFullMap)
//                println("Successfully updated 'LPGEmpty' map for volume type $volumeTypeKey")
//            } else {
//                throw Exception("Document 'Cylinders > LPG' does not exist")
//            }
//
//            // Step 5: Update the "Quantities" map in the "Vendors > LPG Issued > Names > VendorName" document
//            val lpgIssuedRef = db.collection("Vendors")
//                .document("LPG Issued")
//                .collection("Names")
//                .document(VendorName)
//
//            // Fetch the existing "Quantities" map
//            val lpgIssuedSnapshot = lpgIssuedRef.get()
//            if (lpgIssuedSnapshot.exists) {
//                val quantitiesMap = lpgIssuedSnapshot.get("Quantities") as? Map<String, Int> ?: emptyMap()
//
//                // Decrement the value for the volume type key
//                val updatedQuantitiesMap = quantitiesMap.toMutableMap().apply {
//                    this[volumeTypeKey] = (this[volumeTypeKey] ?: 0) - quantity
//                }
//
//                // Save the updated map back to Firestore
//                lpgIssuedRef.update("Quantities" to updatedQuantitiesMap)
//                println("Successfully updated 'Quantities' map for volume type $volumeTypeKey")
//            } else {
//                throw Exception("Document 'Vendors > LPG Issued > Names > $VendorName' does not exist")
//            }
//        }
//
//        // Step 6: Update the "Credit" and "Deposit" fields in the "Vendors > Details > Names > VendorName" document
//        val VendorDetailsRef = db.collection("Vendors")
//            .document("Details")
//            .collection("Names")
//            .document(VendorName)
//
//        // Fetch the existing "Details" map
//        val VendorDetailsSnapshot = VendorDetailsRef.get()
//        if (VendorDetailsSnapshot.exists) {
//            val detailsMap = VendorDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()
//
//            // Increment the "Credit" field
//            val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
//            val newCredit = currentCredit + (creditInput.toDoubleOrNull() ?: 0.0)
//
//            // Update the "Deposit" field with the updatedDeposit value
//
//
//            // Create the updated map
//            val updatedDetailsMap = detailsMap.toMutableMap().apply {
//                this["Credit"] = newCredit.toString()
//            }
//
//            // Save the updated map back to Firestore
//            VendorDetailsRef.update("Details" to updatedDetailsMap)
//            println("Successfully updated 'Credit' and 'Deposit' fields for Vendor $VendorName")
//        } else {
//            throw Exception("Document 'Vendors > Details > Names > $VendorName' does not exist")
//        }
//
//        // Call onSuccess if all updates are successful
//        onSuccess()
//    } catch (e: Exception) {
//        // Call onFailure if any update fails
//        onFailure(e)
//    }
//}

