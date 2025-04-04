package org.example.iosfirebasehope.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.ReturnCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.ReturnCylinderScreenEvent

@Composable
fun ReturnCylinderScreenUI(
    customerName: String,
    db: FirebaseFirestore,
    component: ReturnCylinderScreenComponent
) {
    // State variables
    var currentDateTime by remember { mutableStateOf("") }
    val details = remember { mutableStateOf<Map<String, String>?>(null) }
    val depositValue = remember { mutableStateOf<String?>(null) }
    val creditValue = remember { mutableStateOf<String?>(null) }
    val phoneNumberValue = remember { mutableStateOf<String?>(null) }
    var showReturnCylinderDialog by remember { mutableStateOf(false) }
    var showReturnLPGDialog by remember { mutableStateOf(false) }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var issuedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var issuedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var cylinderDetailsList by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var selectedReturnDate by remember { mutableStateOf<Long?>(null) }
    var showReturnDatePicker by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    // State for animation
    var showMainContent by remember { mutableStateOf(true) }

    // State for ReturnDialog
    var showReturnDialog by remember { mutableStateOf(false) }

    // Fetch customer details
    LaunchedEffect(customerName) {
        val document = db.collection("Customers")
            .document("Details")
            .collection("Names")
            .document(customerName)
            .get()

        details.value = document.get("Details") as? Map<String, String>
        depositValue.value = details.value?.get("Deposit")?.toString()
        creditValue.value = details.value?.get("Credit")?.toString()
        phoneNumberValue.value = details.value?.get("Phone Number")?.toString()

        // Fetch issued cylinders
        val issuedCylindersDoc = db.collection("Customers")
            .document("Issued Cylinders")
            .collection("Names")
            .document(customerName)
            .get()
        issuedCylinders = issuedCylindersDoc.get("Details") as? List<String> ?: emptyList()

        // Fetch issued LPG quantities
        val issuedLPGDoc = db.collection("Customers")
            .document("LPG Issued")
            .collection("Names")
            .document(customerName)
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
        showReturnLPGDialog = true
    }

    // Combine LPG and non-LPG items into a single list
    val combinedItems = remember(cylinderDetailsList, selectedLPGQuantities) {
        val nonLPGItems = cylinderDetailsList.map { cylinderDetails ->
            CombinedItem.NonLPG(cylinderDetails)
        }
        val lpgItems = selectedLPGQuantities.entries.map { (volumeType, quantity) ->
            CombinedItem.LPG(volumeType, quantity)
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
                    onClick = { component.onEvent(ReturnCylinderScreenEvent.OnBackClick) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Return Cylinders",
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
                    // Customer details box
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
                                    text = customerName,
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
                                    Text(text = "Deposit:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Credit:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    phoneNumberValue.value?.let { Text(text = it, fontSize = 14.sp) }
                                    depositValue.value?.let { Text(text = it, fontSize = 14.sp) }
                                    creditValue.value?.let { Text(text = it, fontSize = 14.sp) }
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
                                showReturnCylinderDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text(text = "Return Cylinders", fontSize = 12.sp, color = Color.White)
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
                            Text(text = "Return LPG", fontSize = 12.sp, color = Color.White)
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
                            text = "Cylinders for Return",
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
                            items(combinedItems) { item ->
                                when (item) {
                                    is CombinedItem.NonLPG -> {
                                        NonLPGReturnCard(
                                            cylinderDetails = item.cylinderDetails,
                                            onDelete = { onDeleteNonLPG(item.cylinderDetails) }
                                        )
                                    }
                                    is CombinedItem.LPG -> {
                                        LPGReturnCard(
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between buttons
                        ) {
                            // Return Date Button
                            Button(
                                onClick = { showReturnDatePicker = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp), // Thin button
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp) // Add horizontal padding
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Return Date",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp) // Adjust icon size
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    androidx.compose.material3.Text(
                                        text = if (selectedReturnDate != null) {
                                            "Return: ${LocalDate.fromEpochDays((selectedReturnDate!! / (1000 * 60 * 60 * 24)).toInt())}"
                                        } else {
                                            "Return Date"
                                        },
                                        fontSize = 12.sp, // Smaller font size
                                        color = Color.White,
                                        maxLines = 1, // Prevent text wrapping
                                        overflow = TextOverflow.Ellipsis // Add ellipsis if text overflows
                                    )
                                }
                            }
                            // Return Button
                            Button(
                                onClick = {
                                    // Validate the checkout conditions
                                    val validationError = validateCheckoutConditions2(selectedCylinders, selectedLPGQuantities, selectedReturnDate)
                                    if (validationError != null) {
                                        showValidationDialog = true
                                        validationMessage = validationError
                                    } else {
                                        showReturnDialog = true // Show the Return dialog
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                            ) {
                                Text(text = "Return", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        if (showValidationDialog) {
            ValidationDialog(
                message = validationMessage,
                onDismiss = { showValidationDialog = false } // Close the dialog
            )
        }

        // Return Cylinder Dialog (outside AnimatedVisibility)
        if (showReturnCylinderDialog) {
            ReturnCylinderDialog(
                onDismiss = {
                    showReturnCylinderDialog = false
                    showMainContent = true // Show main content with animation
                },
                onDone = { cylinders ->
                    // Update selectedCylinders
                    selectedCylinders = (selectedCylinders + cylinders).distinct()
                    showMainContent = true // Show main content with animation
                },
                db = db,
                customerName = customerName,
                alreadySelectedCylinders = selectedCylinders
            )
        }

        // Return LPG Dialog (outside AnimatedVisibility)
        if (showReturnLPGDialog) {
            if (issuedLPGQuantities.isEmpty()) {
                // Show dialog if no LPG is issued
                AlertDialog(
                    onDismissRequest = {
                        showReturnLPGDialog = false
                        showMainContent = true // Show main content with animation
                    },
                    title = { Text("Return LPG") },
                    text = { Text("No LPG issued to $customerName") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showReturnLPGDialog = false
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
                ReturnLPGDialog(
                    onDismiss = {
                        showReturnLPGDialog = false
                        showMainContent = true
                    },
                    onDone = { volumeType, quantity ->
                        // Update selectedLPGQuantities by incrementing the existing quantity
                        val currentQuantity = selectedLPGQuantities[volumeType] ?: 0
                        selectedLPGQuantities = selectedLPGQuantities + mapOf(volumeType to (currentQuantity + quantity))
                    },
                    quantities = issuedLPGQuantities,
                    customerName = customerName,
                    alreadySelectedLPGQuantities = selectedLPGQuantities, // Pass the already selected LPG quantities
                    selectedReturnDate = selectedReturnDate
                )
            }
        }

        // Return Dialog (outside AnimatedVisibility)
        if (showReturnDialog) {
            ReturnDialog(
                customerName = customerName,
                deposit = depositValue.value,
                credit = creditValue.value,
                selectedCylinders = cylinderDetailsList + selectedLPGQuantities.map { (volumeType, quantity) ->
                    mapOf("Volume Type" to volumeType, "Quantity" to quantity.toString())
                },
                onDismiss = { showReturnDialog = false },
                onReturn = {
                    // Handle any additional logic after successful return
                },
                db = db,
                selectedReturnDate = selectedReturnDate,
                component = component
            )
        }
        if (showReturnDatePicker) {
            DatePickerModal(
                onDateSelected = { dateMillis ->
                    selectedReturnDate = dateMillis
                    showReturnDatePicker = false
                },
                onDismiss = { showReturnDatePicker = false }
            )
        }
    }
}

// Sealed class to represent combined items
sealed class CombinedItem {
    data class NonLPG(val cylinderDetails: Map<String, String>) : CombinedItem()
    data class LPG(val volumeType: String, val quantity: Int) : CombinedItem()
}

fun validateCheckoutConditions2(
    selectedCylinders: List<String>,
    selectedLPGQuantities: Map<String, Int>,
    selectedReturnDate: Long?
): String? {
    return when {
        selectedCylinders.isEmpty() && selectedLPGQuantities.isEmpty() -> "No Cylinders Selected"
        selectedReturnDate == null -> "No return date selected."
        else -> null // No error
    }
}


// 1. First, modify the NonLPGReturnCard to show days held for each cylinder

@Composable
fun NonLPGReturnCard(
    cylinderDetails: Map<String, String>,
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // State for delete confirmation dialog

    // Calculate days held for this cylinder
    val daysHeld = remember(cylinderDetails) {
        val issueDateString = cylinderDetails["Issue Date"] ?: ""
        try {
            val issueDate = LocalDate.parse(issueDateString)
            val currentDate = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            issueDate.daysUntil(currentDate)
        } catch (e: Exception) {
            // If date parsing fails, return 0 or some placeholder value
            0
        }
    }

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

                    // Add days held information
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "Days Held: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = daysHeld.toString(),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
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

// 2. Now, modify the ReturnDialog to display total days held

@Composable
fun ReturnDialog(
    customerName: String,
    deposit: String?,
    credit: String?,
    selectedCylinders: List<Map<String, String>>,
    onDismiss: () -> Unit,
    onReturn: (updatedDeposit: String) -> Unit,
    db: FirebaseFirestore,
    selectedReturnDate: Long?,
    component: ReturnCylinderScreenComponent
) {
    var cashIn by remember { mutableStateOf("") }
    var currentDateTime by remember { mutableStateOf("") }
    var cashOut by remember { mutableStateOf("") }
    var creditInput by remember { mutableStateOf("") }
    var deductRentFromDeposit by remember { mutableStateOf(false) }
    var rentFactor by remember { mutableStateOf("0") }
    var averageDays = 0
    var totalDays = 0 // Track total days
    var isLoading by remember { mutableStateOf(false) }

    // Calculate the total rent
    val totalRent = remember(selectedCylinders, rentFactor) {
        val currentDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        totalDays = 0 // Reset total days

        // Filter out LPG cylinders
        val nonLpgCylinders = selectedCylinders.filter { cylinder ->
            cylinder.containsKey("Gas Type") // LPG cylinders do not have the "Gas Type" field
        }
        println("nonLpgCylindersOnReturnScreen: $nonLpgCylinders")

        var cylinderCount = 0
        nonLpgCylinders.forEach { cylinder ->
            val issueDateString = cylinder["Issue Date"] ?: ""
            val issueDate = try {
                LocalDate.parse(issueDateString)
            } catch (e: Exception) {
                println("Invalid issue date format: $issueDateString")
                return@forEach
            }

            val daysBetween = issueDate.daysUntil(
                LocalDate.fromEpochDays((selectedReturnDate!! / (1000 * 60 * 60 * 24)).toInt())
            )

            totalDays += daysBetween

            // Add the value of cylinder["Rent"] (now representing previous days) to totalDays
            val previousDays = cylinder["Rent"]?.toIntOrNull() ?: 0
            totalDays += previousDays

            cylinderCount++
        }

        // Calculate average after the forEach loop
        averageDays = if (cylinderCount > 0) totalDays / cylinderCount else 0
        println("totalDays: $totalDays")
        println("cylinderCount: $cylinderCount")
        println("averageDays: $averageDays")

        val factor = rentFactor.toDoubleOrNull() ?: 0.0
        totalDays * factor // Multiply totalDays by the factor to get the total rent
    }

    val updatedDeposit = remember(deductRentFromDeposit, totalRent, deposit) {
        if (deductRentFromDeposit) {
            val depositValue = deposit?.toDoubleOrNull() ?: 0.0
            (depositValue - totalRent).coerceAtLeast(0.0).toString()
        } else {
            deposit ?: "N/A"
        }
    }

    // Calculate updated credit value based on cashIn
    val updatedCredit = remember(credit, cashIn) {
        val creditValue = credit?.toDoubleOrNull() ?: 0.0
        val cashInValue = cashIn.toDoubleOrNull() ?: 0.0
        (creditValue - cashInValue).coerceAtLeast(0.0).toString()
    }

    // Calculate final deposit after cashOut
    val finalDeposit = remember(updatedDeposit, cashOut) {
        val depositValue = updatedDeposit.toDoubleOrNull() ?: 0.0
        val cashOutValue = cashOut.toDoubleOrNull() ?: 0.0
        (depositValue - cashOutValue).coerceAtLeast(0.0).toString()
    }

    AlertDialog(
        onDismissRequest = {if(isLoading) {} else onDismiss},
        title = {Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(customerName, fontSize = 20.sp)
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF2f80eb)
                )
            }
        }},
        text = {
            Column {
                Text(
                    text = "Deposit: $updatedDeposit",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Credit: ${credit ?: "N/A"}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = rentFactor,
                        onValueChange = { rentFactor = it },
                        label = { Text("Rent") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .height(57.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        singleLine = true
                    )
                    Text(
                        text = "/day",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add total days held information
                Text(
                    text = "Total Days Held: $totalDays",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Total Rent: ₹${totalRent}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    CustomCheckbox(
                        isChecked = deductRentFromDeposit,
                        onCheckedChange = { deductRentFromDeposit = it }
                    )
                    Text(
                        text = "Deduct rent from Deposit",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cash in field - deduct from credit
                OutlinedTextField(
                    value = cashIn,
                    onValueChange = { cashIn = it },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Cash in",
                                tint = Color(0xFF388E3C),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash in (deducts from credit)", color = Color(0xFF388E3C))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF388E3C),
                        unfocusedBorderColor = Color(0xFF388E3C).copy(alpha = 0.5f)
                    )
                )

                // Display updated credit amount
                Text(
                    text = "Updated Credit: ${updatedCredit}",
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                // Cash out field - deduct from deposit
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
                            Text("Cash out (deducts from deposit)", color = Color(0xAAD32F2F))
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

                // Display final deposit amount
                Text(
                    text = "Final Deposit: ${finalDeposit}",
                    fontSize = 12.sp,
                    color = Color(0xAAD32F2F),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                // Credit field - adds to credit
                OutlinedTextField(
                    value = creditInput,
                    onValueChange = { creditInput = it },
                    label = { Text("Add Credit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Launch a coroutine to handle the Firestore updates
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        updateCylindersOnReturn(
                            db = db,
                            selectedCylinders = selectedCylinders,
                            customerName = customerName,
                            cashIn = cashIn,
                            cashOut = cashOut,
                            creditInput = creditInput,
                            updatedDeposit = finalDeposit, // Pass the final deposit value
                            updatedCredit = updatedCredit, // Pass the updated credit value
                            onSuccess = {
                                // Call the original onReturn callback
                                onReturn(finalDeposit)
                                component.onEvent(ReturnCylinderScreenEvent.OnConfirmClick(customerName, currentDateTime))
                            },
                            onFailure = { e ->
                                println("Failed to update Firestore: $e")
                            },
                            selectedReturnDate = selectedReturnDate ?: 0L,
                            averageDays = averageDays,
                            totalRent = totalRent,
                            onCurrentDateTime = {currentDateTime = it}
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                Text("Return", color = Color.White, fontSize = 14.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color.White, fontSize = 14.sp)
            }
        }
    )
}

@Composable
fun LPGReturnCard(
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

// Helper functions for gas symbol and color
fun getGasSymbol1(gasName: String): String {
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

fun getGasColor1(gasName: String): Color {
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
fun ReturnCylinderDialog(
    onDismiss: () -> Unit,
    onDone: (List<String>) -> Unit,
    db: FirebaseFirestore,
    customerName: String,
    alreadySelectedCylinders: List<String>
) {
    var quantity by remember { mutableStateOf("") }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var cylinderOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showValidationMessage by remember { mutableStateOf(false) } // State for validation message
    var quantityError by remember { mutableStateOf<String?>(null) } // State for quantity error message

    // Fetch cylinder options
    LaunchedEffect(Unit) {
        val issuedCylindersDoc = db.collection("Customers")
            .document("Issued Cylinders")
            .collection("Names")
            .document(customerName)
            .get()
        cylinderOptions = issuedCylindersDoc.get("Details") as? List<String> ?: emptyList()
    }

    // Update selectedCylinders list when quantity changes
    LaunchedEffect(quantity) {
        val quantityInt = quantity.toIntOrNull() ?: 0
        selectedCylinders = List(quantityInt) { "" } // Initialize the list with empty strings
    }

    // Filter out already selected cylinders from the options
    val availableCylinderOptions = cylinderOptions.filter { it !in alreadySelectedCylinders }

    // Hide validation message after 3 seconds
    LaunchedEffect(showValidationMessage) {
        if (showValidationMessage) {
            delay(3000) // 3 seconds
            showValidationMessage = false
        }
    }

    // Hide quantity error message after 3 seconds
    LaunchedEffect(quantityError) {
        if (quantityError != null) {
            delay(3000) // 3 seconds
            quantityError = null
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

                        // Quantity Input
                        Text("Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Display quantity error message
                        if (quantityError != null) {
                            Text(
                                text = quantityError!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Show dropdowns based on quantity
                        val quantityInt = quantity.toIntOrNull() ?: 0
                        repeat(quantityInt) { index ->
                            Text("Cylinder ${index + 1}", fontWeight = FontWeight.Bold)
                            SearchableDropdown3(
                                options = availableCylinderOptions.filter { it !in selectedCylinders },
                                selectedItem = selectedCylinders.getOrNull(index),
                                onItemSelected = { selectedItem ->
                                    selectedCylinders = selectedCylinders.toMutableList().apply {
                                        // Ensure the list has enough elements before setting the value
                                        if (index < size) {
                                            set(index, selectedItem)
                                        }
                                    }
                                },
                                placeholder = "Select Cylinder",
                                keyboardType = KeyboardType.Number
                            )
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
                                    text = "All fields are necessary",
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
                                    if (quantity.isEmpty() || selectedCylinders.any { it.isEmpty() }) {
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
fun ReturnLPGDialog(
    onDismiss: () -> Unit,
    onDone: (String, Int) -> Unit,
    quantities: Map<String, Int>,
    customerName: String,
    alreadySelectedLPGQuantities: Map<String, Int>, // Pass the already selected LPG quantities
    selectedReturnDate: Long?
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


//@Composable
//fun ReturnDialog(
//    customerName: String,
//    deposit: String?,
//    credit: String?,
//    selectedCylinders: List<Map<String, String>>,
//    onDismiss: () -> Unit,
//    onReturn: (updatedDeposit: String) -> Unit, // Add updatedDeposit as a parameter
//    db: FirebaseFirestore,
//    selectedReturnDate: Long?,
//    component: ReturnCylinderScreenComponent
//) {
//    var cashIn by remember { mutableStateOf("") }
//    var currentDateTime by remember { mutableStateOf("") }
//    var cashOut by remember { mutableStateOf("") }
//    var creditInput by remember { mutableStateOf("") }
//    var deductRentFromDeposit by remember { mutableStateOf(false) }
//    var rentFactor by remember { mutableStateOf("30") }
//    var averageDays = 0
//    var isLoading by remember { mutableStateOf(false) }
//
//    val totalRent = remember(selectedCylinders, rentFactor) {
//        val currentDate = Clock.System.now()
//            .toLocalDateTime(TimeZone.currentSystemDefault())
//            .date
//        var totalDays = 0
//
//        // Filter out LPG cylinders
//        println("selectedCylindersOnReturnScreen: $selectedCylinders")
//        val nonLpgCylinders = selectedCylinders.filter { cylinder ->
//            cylinder.containsKey("Gas Type") // LPG cylinders do not have the "Gas Type" field
//        }
//        println("nonLpgCylindersOnReturnScreen: $nonLpgCylinders")
//
//        var cylinderCount = 0  // Add this before the forEach loop
//        nonLpgCylinders.forEach { cylinder ->
//            val issueDateString = cylinder["Issue Date"] ?: ""
//            val issueDate = try {
//                LocalDate.parse(issueDateString)
//            } catch (e: Exception) {
//                println("Invalid issue date format: $issueDateString")
//                return@forEach
//            }
//
//            val daysBetween = issueDate.daysUntil(
//                LocalDate.fromEpochDays((selectedReturnDate!! / (1000 * 60 * 60 * 24)).toInt())
//            )
//
//            totalDays += daysBetween
//
//            // Add the value of cylinder["Rent"] (now representing previous days) to totalDays
//            val previousDays = cylinder["Rent"]?.toIntOrNull() ?: 0
//            totalDays += previousDays
//
//            cylinderCount++  // Increment counter for valid cylinders
//        }
//
//        // Calculate average after the forEach loop
//        averageDays = if (cylinderCount > 0) totalDays / cylinderCount else 0
//        println("totalDays: $totalDays")
//        println("cylinderCount: $cylinderCount")
//        println("averageDays: $averageDays")
//
//        val factor = rentFactor.toDoubleOrNull() ?: 30.0
//        totalDays * factor // Multiply totalDays by the factor to get the total rent
//    }
//
//    val updatedDeposit = remember(deductRentFromDeposit, totalRent, deposit) {
//        if (deductRentFromDeposit) {
//            val depositValue = deposit?.toDoubleOrNull() ?: 0.0
//            (depositValue - totalRent).coerceAtLeast(0.0).toString()
//        } else {
//            deposit ?: "N/A"
//        }
//    }
//
//    AlertDialog(
//        onDismissRequest = {if(isLoading) {} else onDismiss},
//        title = {Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(customerName, fontSize = 20.sp)
//            if (isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(24.dp),
//                    color = Color(0xFF2f80eb)
//                )
//            }
//        }},
//        text = {
//            Column {
//                Text(
//                    text = "Deposit: $updatedDeposit",
//                    fontSize = 14.sp,
//                    modifier = Modifier.padding(bottom = 4.dp)
//                )
//                Text(
//                    text = "Credit: ${credit ?: "N/A"}",
//                    fontSize = 14.sp,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//                Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(bottom = 4.dp)
//                ) {
//                    OutlinedTextField(
//                        value = rentFactor,
//                        onValueChange = { rentFactor = it },
//                        label = { Text("Rent") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier
//                            .width(80.dp)
//                            .height(57.dp),
//                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
//                        singleLine = true
//                    )
//                    Text(
//                        text = "/day",
//                        fontSize = 14.sp,
//                        modifier = Modifier.padding(start = 4.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = "Total Rent: ₹${totalRent}",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(bottom = 4.dp)
//                )
//                Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
//                ) {
//                    CustomCheckbox(
//                        isChecked = deductRentFromDeposit,
//                        onCheckedChange = { deductRentFromDeposit = it }
//                    )
//                    Text(
//                        text = "Deduct rent from Deposit",
//                        fontSize = 14.sp,
//                        modifier = Modifier.padding(start = 8.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Cash in field
//                OutlinedTextField(
//                    value = cashIn,
//                    onValueChange = { cashIn = it },
//                    label = {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Cash in",
//                                tint = Color(0xFF388E3C),
//                                modifier = Modifier.size(16.dp)
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text("Cash in", color = Color(0xFF388E3C))
//                        }
//                    },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 4.dp),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = Color(0xFF388E3C),
//                        unfocusedBorderColor = Color(0xFF388E3C).copy(alpha = 0.5f)
//                    )
//                )
//
//                // Cash out field
//                OutlinedTextField(
//                    value = cashOut,
//                    onValueChange = { cashOut = it },
//                    label = {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowForward,
//                                contentDescription = "Cash out",
//                                tint = Color(0xAAD32F2F),
//                                modifier = Modifier.size(16.dp)
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text("Cash out", color = Color(0xAAD32F2F))
//                        }
//                    },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 4.dp),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = Color(0xFFD32F2F),
//                        unfocusedBorderColor = Color(0xFFD32F2F).copy(alpha = 0.5f)
//                    )
//                )
//
//                OutlinedTextField(
//                    value = creditInput,
//                    onValueChange = { creditInput = it },
//                    label = { Text("Credit") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    // Launch a coroutine to handle the Firestore updates
//                    isLoading = true
//                    CoroutineScope(Dispatchers.IO).launch {
//                        updateCylindersOnReturn(
//                            db = db,
//                            selectedCylinders = selectedCylinders,
//                            customerName = customerName,
//                            cashIn = cashIn,
//                            cashOut = cashOut,
//                            creditInput = creditInput,
//                            updatedDeposit = updatedDeposit, // Pass the updatedDeposit value
//                            onSuccess = {
//                                // Call the original onReturn callback
//                                onReturn(updatedDeposit)
//                                component.onEvent(ReturnCylinderScreenEvent.OnConfirmClick(customerName, currentDateTime))
//                            },
//                            onFailure = { e ->
//                                println("Failed to update Firestore: $e")
//                            },
//                            selectedReturnDate = selectedReturnDate ?: 0L,
//                            averageDays = averageDays,
//                            totalRent = totalRent,
//                            onCurrentDateTime = {currentDateTime = it}
//                        )
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
//                enabled = !isLoading
//            ) {
//                Text("Return", color = Color.White, fontSize = 14.sp)
//            }
//        },
//        dismissButton = {
//            Button(
//                onClick = onDismiss,
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
//                enabled = !isLoading
//            ) {
//                Text("Cancel", color = Color.White, fontSize = 14.sp)
//            }
//        }
//    )
//}

@Composable
fun CustomCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val borderColor = if (isChecked) Color(0xFF4CAF50) else Color.Gray
    val fillColor = if (isChecked) Color(0xFF4CAF50) else Color.Transparent

    Box(
        modifier = Modifier
            .size(18.dp)
            .border(2.dp, borderColor, shape = RoundedCornerShape(3.dp))
            .clickable { onCheckedChange(!isChecked) }
            .background(fillColor),
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

suspend fun updateCylindersOnReturn(
    db: FirebaseFirestore,
    selectedCylinders: List<Map<String, String>>,
    customerName: String,
    cashIn: String,
    cashOut: String,
    creditInput: String,
    updatedDeposit: String,
    updatedCredit: String, // Added this parameter to receive the updated credit value
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit,
    selectedReturnDate: Long,
    averageDays: Int,
    totalRent: Double,
    onCurrentDateTime: (String) -> Unit
) {
    // Pre-process and filter data once at the beginning
    val nonLpgCylinders = selectedCylinders.filter { it.containsKey("Gas Type") }
    val lpgCylinders = selectedCylinders.filter { !it.containsKey("Gas Type") }
    println("Non-LPG Cylinders: $nonLpgCylinders")
    println("LPG Cylinders: $lpgCylinders")

    try {
        // Get the current date and time
        val currentTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
            .toString()

        val formattedReturnDate = LocalDate.fromEpochDays((selectedReturnDate / (1000 * 60 * 60 * 24)).toInt()).toString()
        val dateTimeString = "${formattedReturnDate}_${currentTime}"
            .replace(":", "-")
            .substringBefore(".")

        onCurrentDateTime(dateTimeString)

        // Prepare LPG data for batch operations
        val lpgReturnedMap = mutableMapOf<String, Int>()
        val lpgVolumeTypeQuantities = mutableMapOf<String, Int>()

        for (cylinder in lpgCylinders) {
            val volumeType = cylinder["Volume Type"] ?: continue
            val quantity = cylinder["Quantity"]?.toIntOrNull() ?: continue
            val volumeTypeKey = volumeType.replace(".", ",")

            lpgReturnedMap[volumeTypeKey] = (lpgReturnedMap[volumeTypeKey] ?: 0) + quantity
            lpgVolumeTypeQuantities[volumeTypeKey] = (lpgVolumeTypeQuantities[volumeTypeKey] ?: 0) + quantity
        }

        // Use coroutineScope for parallel operations
        coroutineScope {
            // TASK 1: Create transaction record and details (can be done in a batch)
            val createTransactionTask = async {
                val transactionsRef = db.collection("Transactions")
                    .document(customerName)
                    .collection("DateAndTime")
                    .document(dateTimeString)

                val transactionDetailsRef = transactionsRef.collection("Transaction Details")

                // Create a batch for all transaction-related operations
                val batch = db.batch()

                // Main transaction document
                batch.set(transactionsRef, mapOf("Date" to dateTimeString))

                // Transaction details
                batch.set(transactionDetailsRef.document("Cash"), mapOf("Amount" to cashIn))
                batch.set(transactionDetailsRef.document("Cash Out"), mapOf("Amount" to cashOut))
                batch.set(transactionDetailsRef.document("Credit"), mapOf("Amount" to creditInput))
                batch.set(transactionDetailsRef.document("Total Price"), mapOf("Amount" to totalRent.toString()))
                batch.set(transactionDetailsRef.document("Cylinders Issued"), mapOf("CylindersIssued" to emptyList<Map<String, String>>()))
                batch.set(transactionDetailsRef.document("Inventory Issued"), mapOf("InventoryIssued" to emptyList<Map<String, String>>()))
                batch.set(transactionDetailsRef.document("LPG Issued"), mapOf("LPGIssued" to emptyList<Map<String, String>>()))

                // Cylinders returned
                val cylindersReturned = nonLpgCylinders.map { cylinder ->
                    mapOf(
                        "Serial Number" to cylinder["Serial Number"],
                        "Return Date" to LocalDate.fromEpochDays((selectedReturnDate / (1000 * 60 * 60 * 24)).toInt()).toString()
                    )
                }
                batch.set(transactionDetailsRef.document("Cylinders Returned"), mapOf("CylindersReturned" to cylindersReturned))

                // LPG returned
                batch.set(transactionDetailsRef.document("LPG Returned"), mapOf("LPGReturned" to lpgReturnedMap))

                // Execute all transaction operations as a single batch
                batch.commit()
            }

            // TASK 2: Update non-LPG cylinder information
            val updateNonLpgTask = async {
                if (nonLpgCylinders.isNotEmpty()) {
                    // Preload data for faster updates
                    val serialNumbers = nonLpgCylinders.mapNotNull { it["Serial Number"] }

                    // Get all cylinder details at once
                    val cylindersRef = db.collection("Cylinders").document("Cylinders")
                    val cylinderSnapshot = cylindersRef.get()
                    val cylinderDetails = cylinderSnapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

                    // Get customer's issued cylinders
                    val issuedCylindersRef = db.collection("Customers")
                        .document("Issued Cylinders")
                        .collection("Names")
                        .document(customerName)
                    val issuedCylindersSnapshot = issuedCylindersRef.get()
                    val detailsArray = issuedCylindersSnapshot.get("Details") as? List<String> ?: emptyList()

                    // Update cylinder details
                    val updatedCylinderDetails = cylinderDetails.map { details ->
                        if (details["Serial Number"] in serialNumbers) {
                            details.toMutableMap().apply {
                                this["Status"] = "Empty"
                                this["Issue Date"] = ""
                                this["Issued At Price"] = ""
                                this["Notifications Date"] = ""
                                this["Rent"] = "0"
                            }
                        } else {
                            details
                        }
                    }

                    // Update issued cylinders list
                    val updatedDetailsArray = detailsArray.filter { it !in serialNumbers }

                    // Process updates in batches for efficiency
                    // Update cylinder details
                    cylindersRef.update("CylinderDetails" to updatedCylinderDetails)

                    // Update customer's issued cylinders
                    issuedCylindersRef.update("Details" to updatedDetailsArray)

                    // Update "Currently Issued To" for all cylinders in parallel
                    serialNumbers.map { serialNumber ->
                        async {
                            val currentlyIssuedToRef = db.collection("Cylinders")
                                .document("Customers")
                                .collection(serialNumber)
                                .document("Currently Issued To")

                            currentlyIssuedToRef.set(
                                mapOf(
                                    "name" to "",
                                    "date" to "",
                                    "price" to ""
                                )
                            )
                        }
                    }.awaitAll()
                }
            }

            // TASK 3: Update LPG information
            val updateLpgTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    // Update LPG Empty quantities
                    val lpgRef = db.collection("Cylinders").document("LPG")
                    val lpgSnapshot = lpgRef.get()
                    val lpgEmptyMap = lpgSnapshot.get("LPGEmpty") as? Map<String, Int> ?: emptyMap()

                    // Calculate new values for LPG Empty map
                    val updatedLpgEmptyMap = lpgEmptyMap.toMutableMap()
                    lpgVolumeTypeQuantities.forEach { (volumeTypeKey, quantity) ->
                        updatedLpgEmptyMap[volumeTypeKey] = (updatedLpgEmptyMap[volumeTypeKey] ?: 0) + quantity
                    }

                    // Update LPG Empty in Firestore
                    lpgRef.update("LPGEmpty" to updatedLpgEmptyMap)

                    // Update customer's LPG issued quantities
                    val lpgIssuedRef = db.collection("Customers")
                        .document("LPG Issued")
                        .collection("Names")
                        .document(customerName)

                    val lpgIssuedSnapshot = lpgIssuedRef.get()
                    if (lpgIssuedSnapshot.exists) {
                        val quantitiesMap = lpgIssuedSnapshot.get("Quantities") as? Map<String, Int> ?: emptyMap()

                        // Calculate new quantities
                        val updatedQuantitiesMap = quantitiesMap.toMutableMap()
                        lpgVolumeTypeQuantities.forEach { (volumeTypeKey, quantity) ->
                            val currentQuantity = updatedQuantitiesMap[volumeTypeKey] ?: 0
                            updatedQuantitiesMap[volumeTypeKey] = maxOf(0, currentQuantity - quantity)
                        }

                        // Update quantities in Firestore
                        lpgIssuedRef.update("Quantities" to updatedQuantitiesMap)
                    }
                }
            }

            // TASK 4: Update customer details (credit, deposit, average days)
            val updateCustomerDetailsTask = async {
                val customerDetailsRef = db.collection("Customers")
                    .document("Details")
                    .collection("Names")
                    .document(customerName)

                val customerDetailsSnapshot = customerDetailsRef.get()
                if (customerDetailsSnapshot.exists) {
                    val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

                    // Use the passed values for credit and deposit
                    val newCredit = updatedCredit.toDoubleOrNull() ?: 0.0
                    val additionalCredit = creditInput.toDoubleOrNull() ?: 0.0
                    val finalCredit = newCredit + additionalCredit

                    val newDeposit = updatedDeposit.toDoubleOrNull() ?: 0.0

                    // Calculate new average days
                    val existingAverageDays = detailsMap["Average Days"]?.toIntOrNull() ?: 0
                    val newAverageDays = if (existingAverageDays == 0) {
                        averageDays
                    } else {
                        (existingAverageDays + averageDays) / 2
                    }

                    // Update customer details
                    val updatedDetailsMap = detailsMap.toMutableMap().apply {
                        this["Credit"] = finalCredit.toString()
                        this["Deposit"] = newDeposit.toString()
                        this["Average Days"] = newAverageDays.toString()
                    }

                    customerDetailsRef.update("Details" to updatedDetailsMap)
                }
            }

            // Await completion of all tasks
            awaitAll(createTransactionTask, updateNonLpgTask, updateLpgTask, updateCustomerDetailsTask)
        }

        // Call onSuccess if all updates complete successfully
        onSuccess()
    } catch (e: Exception) {
        println("Error in updateCylindersOnReturn: ${e.message}")
        // Call onFailure if any update fails
        onFailure(e)
    }
}


//suspend fun updateCylindersOnReturn(
//    db: FirebaseFirestore,
//    selectedCylinders: List<Map<String, String>>,
//    customerName: String,
//    cashIn: String,
//    cashOut: String,
//    creditInput: String,
//    updatedDeposit: String, // New parameter for the updated deposit value
//    onSuccess: () -> Unit,
//    onFailure: (Exception) -> Unit,
//    selectedReturnDate: Long,
//    averageDays: Int,
//    totalRent: Double,
//    onCurrentDateTime: (String) -> Unit
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
//        val currentTime = Clock.System.now()
//            .toLocalDateTime(TimeZone.currentSystemDefault())
//            .time
//            .toString()
//
//        val formattedReturnDate = LocalDate.fromEpochDays((selectedReturnDate!! / (1000 * 60 * 60 * 24)).toInt()).toString()
//        // Combine issueDate with current time
//        val dateTimeString = "${formattedReturnDate}_${currentTime}"
//            .replace(":", "-")
//            .substringBefore(".")
//
//
//        // Reference to the Transactions collection
//        val transactionsRef = db.collection("Transactions")
//            .document(customerName)
//            .collection("DateAndTime")
//            .document(dateTimeString)
//
//        onCurrentDateTime(dateTimeString)
//
//        transactionsRef.set(mapOf("Date" to dateTimeString))
//
//        // Create the "Transaction Details" collection
//        val transactionDetailsRef = transactionsRef.collection("Transaction Details")
//
//        // Push Cash In document with the user-provided value
//        transactionDetailsRef.document("Cash").set(mapOf("Amount" to cashIn))
//
//        // Push Cash Out document with the user-provided value
//        transactionDetailsRef.document("Cash Out").set(mapOf("Amount" to cashOut))
//
//        // Push Credit document with the user-provided value
//        transactionDetailsRef.document("Credit").set(mapOf("Amount" to creditInput))
//
//        transactionDetailsRef.document("Total Price").set(mapOf("Amount" to totalRent.toString()))
//
//        // Push Cylinders Issued document (empty array)
//        transactionDetailsRef.document("Cylinders Issued").set(mapOf("CylindersIssued" to emptyList<Map<String, String>>()))
//
//        // Push Cylinders Returned document
//        val cylindersReturned = nonLpgCylinders.map { cylinder ->
//            mapOf(
//                "Serial Number" to cylinder["Serial Number"],
//                "Return Date" to LocalDate.fromEpochDays((selectedReturnDate / (1000 * 60 * 60 * 24)).toInt()).toString() // Use only the date here
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
//            // Step 1: Update the "Currently Issued To" document
//            val currentlyIssuedToRef = db.collection("Cylinders")
//                .document("Customers")
//                .collection(serialNumber)
//                .document("Currently Issued To")
//
//            // Set all fields to empty strings
//            currentlyIssuedToRef.set(
//                mapOf(
//                    "name" to "",
//                    "date" to "",
//                    "price" to ""
//                )
//            )
//
//            println("Successfully updated 'Currently Issued To' for cylinder $serialNumber")
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
//                            this["Status"] = "Empty"
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
//            val issuedCylindersRef = db.collection("Customers")
//                .document("Issued Cylinders")
//                .collection("Names")
//                .document(customerName)
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
//                println("Successfully removed $serialNumber from 'Details' array for customer $customerName")
//            } else {
//                throw Exception("Document 'Issued Cylinders > Names > $customerName' does not exist")
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
//                val lpgEmptyMap = lpgSnapshot.get("LPGEmpty") as? Map<String, Int> ?: emptyMap()
//
//                // Increment the value for the volume type key
//                val updatedLpgEmptyMap = lpgEmptyMap.toMutableMap().apply {
//                    this[volumeTypeKey] = (this[volumeTypeKey] ?: 0) + quantity
//                }
//                println("Updated LPGEmpty Map: $updatedLpgEmptyMap")
//
//                // Save the updated map back to Firestore
//                lpgRef.update("LPGEmpty" to updatedLpgEmptyMap)
//                println("Successfully updated 'LPGEmpty' map for volume type $volumeTypeKey")
//            } else {
//                throw Exception("Document 'Cylinders > LPG' does not exist")
//            }
//
//            // Step 5: Update the "Quantities" map in the "Customers > LPG Issued > Names > customerName" document
//            val lpgIssuedRef = db.collection("Customers")
//                .document("LPG Issued")
//                .collection("Names")
//                .document(customerName)
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
//                throw Exception("Document 'Customers > LPG Issued > Names > $customerName' does not exist")
//            }
//        }
//
//        // Step 6: Update the "Credit" and "Deposit" fields in the "Customers > Details > Names > customerName" document
//        val customerDetailsRef = db.collection("Customers")
//            .document("Details")
//            .collection("Names")
//            .document(customerName)
//
//        // Fetch the existing "Details" map
//        val customerDetailsSnapshot = customerDetailsRef.get()
//        if (customerDetailsSnapshot.exists) {
//            val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()
//
//            // Increment the "Credit" field
//            val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
//            val newCredit = currentCredit + (creditInput.toDoubleOrNull() ?: 0.0)
//
//            // Update the "Deposit" field with the updatedDeposit value
//            val newDeposit = updatedDeposit.toDoubleOrNull() ?: 0.0
//
//            // Get the existing Average Days and calculate new average
//            val existingAverageDays = detailsMap["Average Days"]?.toIntOrNull() ?: 0
//            val newAverageDays: Int
//            if (existingAverageDays == 0) {
//                // If the existing average is 0, set the new average to the current average
//                newAverageDays = averageDays
//            }
//            else {
//                // Calculate the new average days
//                newAverageDays = (existingAverageDays + averageDays) / 2
//            }
//
//            // Create the updated map
//            val updatedDetailsMap = detailsMap.toMutableMap().apply {
//                this["Credit"] = newCredit.toString()
//                this["Deposit"] = newDeposit.toString()
//                this["Average Days"] = newAverageDays.toString()
//            }
//
//            // Save the updated map back to Firestore
//            customerDetailsRef.update("Details" to updatedDetailsMap)
//            println("Successfully updated 'Credit' and 'Deposit' fields for customer $customerName")
//
//        } else {
//            throw Exception("Document 'Customers > Details > Names > $customerName' does not exist")
//        }
//
//        // Call onSuccess if all updates are successful
//        onSuccess()
//    } catch (e: Exception) {
//        // Call onFailure if any update fails
//        onFailure(e)
//    }
//}





