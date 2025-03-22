package org.example.iosfirebasehope.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.ExchangeCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.ExchangeCylinderScreenEvent


@Composable
fun ExchangeCylinderScreenUI(
    component: ExchangeCylinderScreenComponent,
    customerName: String,
    db: FirebaseFirestore
) {
    // Existing state variables
    var exchangePairs by remember { mutableStateOf<List<Pair<Map<String, String>, Map<String, String>>>>(emptyList()) }
    val details = remember { mutableStateOf<Map<String, String>?>(null) }
    val depositValue = remember { mutableStateOf<String?>(null) }
    val creditValue = remember { mutableStateOf<String?>(null) }
    val phoneNumberValue = remember { mutableStateOf<String?>(null) }
    val rotationPeriod = remember { mutableStateOf<String?>(null) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddCylinderDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<String?>(null) }
    var customers by remember { mutableStateOf<List<String>>(emptyList()) }
    var issuedCylinders by remember { mutableStateOf<List<IssuedCylinder>>(emptyList()) }
    var issueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var returnDays by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var alreadySelectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var alreadySelectedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var issuedInventory by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }
    var alreadySelectedInventory by remember { mutableStateOf<List<String>>(emptyList()) }
    var alreadySelectedInventoryQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var isBackButtonEnabled = remember { mutableStateOf(true) }

    // New state variables for date pickers
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showReturnDatePicker by remember { mutableStateOf(false) }
    var selectedIssueDate by remember { mutableStateOf<Long?>(null) }
    var selectedReturnDate by remember { mutableStateOf<Long?>(null) }

    // State variable for bottom buttons visibility
    var showBottomButtons by remember { mutableStateOf(true) }

    // State variable for checkout dialog
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // State variable for validation dialog
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }
    var showMainContent by remember { mutableStateOf(true) }
    var showExchangeDialog by remember { mutableStateOf(false) }

    var currentDateTime by remember { mutableStateOf("") }

    var issuedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var lpgExchangePairs by remember { mutableStateOf<List<Pair<Map<String, String>, Map<String, String>>>>(emptyList()) }

    // Callback for deleting an LPG exchange pair
    val onDeleteLPGExchange: (Pair<Map<String, String>, Map<String, String>>) -> Unit = { pair ->
        lpgExchangePairs = lpgExchangePairs.filterNot { it == pair }
    }

    // Calculate the real-time total price
    // Calculate the real-time total price
    val totalPrice = remember(issuedCylinders, issuedInventory, exchangePairs, lpgExchangePairs) {
        // Sum of prices from issued cylinders (Add Cylinder button)
        val cylinderTotal = issuedCylinders.sumOf { cylinder ->
            if (cylinder.gasType == "LPG") {
                cylinder.quantity * cylinder.totalPrice // Multiply by quantity for LPG
            } else {
                cylinder.totalPrice // For non-LPG cylinders, use the existing totalPrice
            }
        }

        // Sum of prices from issued inventory (Add Cylinder button)
        val inventoryTotal = issuedInventory.sumOf { it.price * it.quantity }

        // Sum of prices from exchangePairs (non-LPG exchanges)
        val exchangeTotal = exchangePairs.sumOf { pair ->
            pair.second["Price"]?.toDoubleOrNull() ?: 0.0
        }

        // Sum of prices from lpgExchangePairs (LPG exchanges)
        val lpgExchangeTotal = lpgExchangePairs.sumOf { pair ->
            val quantity = pair.second["Quantity"]?.toIntOrNull() ?: 1
            val price = pair.second["Price"]?.toDoubleOrNull() ?: 0.0
            quantity * price
        }

        // Total sum of all prices
        cylinderTotal + inventoryTotal + exchangeTotal + lpgExchangeTotal
    }

    // Fetch all fields from Firestore
    LaunchedEffect(customerName) {
        val document = db.collection("Customers")
            .document("Details")
            .collection("Names")
            .document(customerName)
            .get()

        // Extract the "Details" map from the document
        details.value = document.get("Details") as? Map<String, String>

        // Extract the "Deposit", "Credit", and "Phone Number" values from the "Details" map
        depositValue.value = details.value?.get("Deposit")?.toString()
        creditValue.value = details.value?.get("Credit")?.toString()
        phoneNumberValue.value = details.value?.get("Phone Number")?.toString()
        rotationPeriod.value = details.value?.get("Average Days")?.toString()

        // Fetch issued LPG quantities
        val issuedLPGDoc = db.collection("Customers")
            .document("LPG Issued")
            .collection("Names")
            .document(customerName)
            .get()
        issuedLPGQuantities = issuedLPGDoc.get("Quantities") as? Map<String, Int> ?: emptyMap()
    }



    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar with higher zIndex to ensure it stays on top
        Surface(
            color = Color(0xFF2f80eb),
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f) // Higher zIndex to ensure it stays on top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = isBackButtonEnabled.value,
                    onClick = { component.onEvent(ExchangeCylinderScreenEvent.OnBackClick) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Exchange Cylinders",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Main content (below the top bar)
        Scaffold(
            topBar = {
                // Empty top bar (since we're manually placing the top bar above)
                Box {}
            }
        ) { innerPadding ->
            // Animate the main content sliding out when the dialog is shown
            AnimatedVisibility(
                visible = !showAddCylinderDialog && !showExchangeDialog && !showAddInventoryDialog, // Check both dialogs
                enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(durationMillis = 300))
            ) {
                Column(modifier = Modifier.padding(innerPadding)) {
                    // Box displaying current cylinder details
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, top = 64.dp) // Increased top and bottom padding
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp), // Increased padding
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp // Increased font size
                                )
                            }
                            Divider()

                            // Display "Phone", "Deposit", and "Credit" in a single row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Column for keys
                                Column(
                                    modifier = Modifier.weight(0.4f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                                ) {
                                    Text(
                                        text = "Phone:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                    Text(
                                        text = "Deposit:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                    Text(
                                        text = "Credit:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                    Text(
                                        text = "Avg Rotation:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                }

                                // Column for values
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                                ) {
                                    phoneNumberValue.value?.let { phoneNumber ->
                                        Text(
                                            text = phoneNumber,
                                            fontSize = 14.sp // Increased font size
                                        )
                                    }
                                    depositValue.value?.let { deposit ->
                                        Text(
                                            text = deposit,
                                            fontSize = 14.sp // Increased font size
                                        )
                                    }
                                    creditValue.value?.let { credit ->
                                        Text(
                                            text = credit,
                                            fontSize = 14.sp // Increased font size
                                        )
                                    }
                                    rotationPeriod.value?.let { rotation ->
                                        Text(
                                            text = "$rotation days",
                                            fontSize = 14.sp // Increased font size
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add buttons below the grey box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // State variable for showing the ExchangeDialog


                        // Exchange Cylinder button
                        Button(
                            onClick = {
                                showBottomButtons = false // Hide bottom buttons
                                showExchangeDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text(
                                text = "Exchange",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }


                        // "Add Cylinder" button (moved to the right)
                        Button(
                            onClick = {
                                showAddCylinderDialog = true
                                showBottomButtons = false // Hide bottom buttons
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .height(32.dp), // Reduced button height
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2f80eb) // Use the same color as the top bar
                            )
                        ) {
                            Text(
                                text = "Add Cylinder",
                                fontSize = 12.sp, // Reduced font size
                                color = Color.White // White text
                            )
                        }
                        Button(
                            onClick = {
                                showAddInventoryDialog = true
                                showBottomButtons = false // Hide bottom buttons
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp), // Reduced button height
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2f80eb) // Use the same color as the top bar
                            )
                        ) {
                            Text(
                                text = "Add Inventory",
                                fontSize = 12.sp, // Reduced font size
                                color = Color.White // White text
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // "Current Cart" text with a Divider in the same row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (issuedCylinders.isEmpty() && issuedInventory.isEmpty()) "Current Cart: Empty" else "Current Cart",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp) // Add padding to separate text and divider
                        )
                        Divider(
                            modifier = Modifier
                                .weight(1f) // Take remaining space
                                .height(1.dp), // Thin divider
                            color = Color.Gray
                        )
                    }

                    val onUpdateAlreadySelectedCylinders: (List<String>) -> Unit = { updatedList ->
                        alreadySelectedCylinders = updatedList
                    }

                    // Combined LazyColumn for cylinders and inventory
                    CombinedList9(
                        issuedCylinders = issuedCylinders,
                        issuedInventory = issuedInventory,
                        onDeleteCylinder = { cylinder ->
                            // Filter out the deleted cylinders from the issuedCylinders list
                            val deletedCylinders = issuedCylinders.filter {
                                it.gasType == cylinder.gasType && it.volumeType == cylinder.volumeType
                            }

                            // Update the issuedCylinders list by removing the deleted cylinders
                            issuedCylinders = issuedCylinders.filterNot {
                                it.gasType == cylinder.gasType && it.volumeType == cylinder.volumeType
                            }

                            // Remove the serial numbers of the deleted cylinders from alreadySelectedCylinders
                            alreadySelectedCylinders =
                                alreadySelectedCylinders.filterNot { serialNumber ->
                                    deletedCylinders.any { it.serialNumber == serialNumber }
                                }

                            // If the deleted cylinders are LPG, update the alreadySelectedLPGQuantities
                            if (cylinder.gasType == "LPG") {
                                val formattedVolumeType = cylinder.volumeType.replace(".", ",")
                                val alreadySelectedQuantity =
                                    alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                                alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                                    formattedVolumeType to (alreadySelectedQuantity - cylinder.quantity)
                                )
                            }
                        },
                        onDeleteInventory = { inventoryItem ->
                            // Filter out the deleted inventory items from the issuedInventory list
                            issuedInventory =
                                issuedInventory.filterNot { it.name == inventoryItem.name }

                            // Remove the quantity of the deleted inventory item from alreadySelectedInventoryQuantities
                            val alreadySelectedQuantity =
                                alreadySelectedInventoryQuantities[inventoryItem.name] ?: 0
                            alreadySelectedInventoryQuantities =
                                alreadySelectedInventoryQuantities - inventoryItem.name
                        },
                        alreadySelectedLPGQuantities = alreadySelectedLPGQuantities,
                        onUpdateAlreadySelectedLPGQuantities = { updatedMap ->
                            alreadySelectedLPGQuantities = updatedMap
                        },
                        exchangePairs = exchangePairs,
                        onDeleteExchange = { pair ->
                            // Remove the pair from the list
                            exchangePairs = exchangePairs.filterNot { it == pair }
                        },
                        onUpdateAlreadySelectedCylinders = onUpdateAlreadySelectedCylinders,
                        alreadySelectedCylinders = alreadySelectedCylinders,
                        lpgExchangePairs = lpgExchangePairs,
                        onDeleteLPGExchange = onDeleteLPGExchange, // Pass the callback
                    )
                }
            }
        }

        val onUpdateAlreadySelectedCylinders: (List<String>) -> Unit = { updatedList ->
            alreadySelectedCylinders = updatedList
        }



        // Show the ExchangeDialog when showExchangeDialog is true
        if (showExchangeDialog) {
            ExchangeDialog(
                customerName = customerName,
                onDismiss = {
                    showBottomButtons = true // Show bottom buttons
                    showExchangeDialog = false
                },
                db = db,
                onDone = { newExchangePairs ->
                    // Update the exchangePairs state with the selected pairs
                    exchangePairs = exchangePairs + newExchangePairs
                    showExchangeDialog = false
                },
                alreadySelectedCylinders = alreadySelectedCylinders,
                alreadySelectedLPGQuantities = alreadySelectedLPGQuantities,
                quantities = issuedLPGQuantities,
                onUpdateAlreadySelectedCylinders = onUpdateAlreadySelectedCylinders,
                onDoneLPG = { lpgExchangePair ->
                    lpgExchangePairs = lpgExchangePairs + lpgExchangePair
                    showExchangeDialog = false
                },
                lpgExchangePairs = lpgExchangePairs,
                issuedCylinders = issuedCylinders
            )
        }
//        CombinedList9(
//            issuedCylinders = issuedCylinders,
//            issuedInventory = issuedInventory,
//            exchangePairs = exchangePairs, // Pass the exchangePairs state
//            onDeleteCylinder = { cylinder ->
//                // Handle cylinder deletion
//            },
//            onDeleteInventory = { inventoryItem ->
//                // Handle inventory deletion
//            },
//            onDeleteExchange = { pair ->
//                // Remove the pair from the list
//                exchangePairs = exchangePairs.filterNot { it == pair }
//            },
//            alreadySelectedLPGQuantities = alreadySelectedLPGQuantities,
//            onUpdateAlreadySelectedLPGQuantities = { updatedMap ->
//                alreadySelectedLPGQuantities = updatedMap
//            }
//        )
        // Update alreadySelectedCylinders when a cylinder is added
        val onAddCylinder: (IssuedCylinder) -> Unit = { issuedCylinder ->
            issuedCylinders = issuedCylinders + issuedCylinder
            if (issuedCylinder.gasType != "LPG") {
                alreadySelectedCylinders = alreadySelectedCylinders + issuedCylinder.serialNumber
            }
            if (issuedCylinder.gasType == "LPG") {
                val formattedVolumeType = issuedCylinder.volumeType.replace(".", ",")
                val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                    formattedVolumeType to (alreadySelectedQuantity + issuedCylinder.quantity)
                )
            }
        }

        // Update alreadySelectedCylinders when a cylinder is deleted
        val onDeleteCylinder: (IssuedCylinder) -> Unit = { cylinder ->
            issuedCylinders = issuedCylinders.filterNot { it.serialNumber == cylinder.serialNumber }
            if (cylinder.gasType != "LPG") {
                alreadySelectedCylinders = alreadySelectedCylinders.filterNot { it == cylinder.serialNumber }
            }
            if (cylinder.gasType == "LPG") {
                val formattedVolumeType = cylinder.volumeType.replace(".", ",")
                val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                    formattedVolumeType to (alreadySelectedQuantity - cylinder.quantity)
                )
            }
        }

        // Overlay the Add Cylinder dialog box on top of everything (but under the top bar)
        if (showAddCylinderDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // Ensure the dialog is above the main content but under the top bar
            ) {
                AddCylinderDialog9(
                    onDismiss = {
                        isBackButtonEnabled.value = true // Enable the back button
                        showAddCylinderDialog = false
                        showBottomButtons = true // Show bottom buttons
                    },
                    onAddCylinder = onAddCylinder,
                    db = db,
                    alreadySelectedCylinders = alreadySelectedCylinders,
                    onUpdateAlreadySelectedCylinders = { updatedList ->
                        alreadySelectedCylinders = updatedList
                    },
                    isBackButtonEnabled = isBackButtonEnabled,
                    alreadySelectedLPGQuantities = alreadySelectedLPGQuantities,
                    issuedCylinders = issuedCylinders,
                    lpgExchangePairs = lpgExchangePairs// Pass the state
                )
            }
        }
        if (showAddInventoryDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // Ensure the dialog is above the main content but under the top bar
            ) {
                AddInventoryDialog2(
                    onDismiss = {
                        isBackButtonEnabled.value = true
                        showAddInventoryDialog = false
                        showBottomButtons = true // Show bottom buttons
                    },
                    onAddInventory = { inventoryItem ->
                        issuedInventory = issuedInventory + inventoryItem
                        val alreadySelectedQuantity = alreadySelectedInventoryQuantities[inventoryItem.name] ?: 0
                        alreadySelectedInventoryQuantities = alreadySelectedInventoryQuantities + mapOf(
                            inventoryItem.name to (alreadySelectedQuantity + inventoryItem.quantity)
                        )
                    },
                    db = db,
                    alreadySelectedInventory = alreadySelectedInventory,
                    onUpdateAlreadySelectedInventory = { updatedList ->
                        alreadySelectedInventory = updatedList
                    },
                    alreadySelectedInventoryQuantities = alreadySelectedInventoryQuantities,
                    isBackButtonEnabled = isBackButtonEnabled
                )
            }
        }



        // Bottom buttons with animation
        AnimatedVisibility(
            visible = showBottomButtons,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
            modifier = Modifier
                .align(Alignment.BottomCenter) // Align to the bottom of the screen
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Add some padding around the buttons
            ) {
                // First Row: Issue Date and Return Date Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp), // Add small bottom padding to separate rows
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Issue Date Button
                    Button(
                        onClick = { showIssueDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
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
                                contentDescription = "Issue Date",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp) // Adjust icon size
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedIssueDate != null) {
                                    "Issue: ${LocalDate.fromEpochDays((selectedIssueDate!! / (1000 * 60 * 60 * 24)).toInt())}"
                                } else {
                                    "Issue Date"
                                },
                                fontSize = 12.sp, // Smaller font size
                                color = Color.White,
                                maxLines = 1, // Prevent text wrapping
                                overflow = TextOverflow.Ellipsis // Add ellipsis if text overflows
                            )
                        }
                    }

                    // Return Date Button
                    Button(
                        onClick = { showReturnDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
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
                            Text(
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
                }

                // Second Row: Total Button (full width)
                Button(
                    onClick = {
                        // Validate conditions
                        val errorMessage = validateCheckoutConditions9(
                            issuedCylinders,
                            issuedInventory,
                            selectedIssueDate,
                            selectedReturnDate,
                            exchangePairs,
                            lpgExchangePairs
                        )
                        if (errorMessage == null) {
                            showCheckoutDialog = true // Show checkout dialog if validation passes
                        } else {
                            // Show validation dialog with the relevant message
                            validationMessage = errorMessage
                            showValidationDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp), // Thin button
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)) // Green color
                ) {
                    Text(
                        text = "Checkout: $${totalPrice}",
                        fontSize = 14.sp, // Slightly larger font size for emphasis
                        color = Color.White,
                        maxLines = 1, // Prevent text wrapping
                        overflow = TextOverflow.Ellipsis // Add ellipsis if text overflows
                    )
                }
            }
        }

        // Date picker modals
        if (showIssueDatePicker) {
            DatePickerModal(
                onDateSelected = { dateMillis ->
                    selectedIssueDate = dateMillis
                    showIssueDatePicker = false
                },
                onDismiss = { showIssueDatePicker = false }
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

        if (showCheckoutDialog) {
            CheckoutDialog9(
                totalPrice = totalPrice,
                selectedIssueDate = selectedIssueDate,
                selectedReturnDate = selectedReturnDate,
                onDismiss = { showCheckoutDialog = false },
                onConfirm = { cash, credit ->
                    // Call the checkoutCylinders function
                    coroutineScope.launch {
                        val issueDateString = selectedIssueDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt()).toString()
                        } ?: ""
                        val returnDateString = selectedReturnDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt()).toString()
                        } ?: ""

                        // Push transaction details to Firestore
                        val success = checkoutCylinders9(
                            db = db,
                            customerName = customerName,
                            issuedCylinders = issuedCylinders,
                            issuedInventory = issuedInventory, // Pass issuedInventory
                            issueDate = issueDateString,
                            cash = cash,
                            credit = credit,
                            returnDate = returnDateString,
                            exchangePairs = exchangePairs,
                            lpgExchangePairs = lpgExchangePairs,
                            totalPrice = totalPrice,
                            onCurrentDateTime = { currentDateTime = it }
                        )

                        if (success) {
                            component.onEvent(ExchangeCylinderScreenEvent.OnBillClick(
                                customerName = customerName,
                                dateTime = currentDateTime
                            ))
                            println("Transaction successfully pushed to Firestore!")
                        } else {
                            println("Failed to push transaction to Firestore.")
                        }
                    }
                }
            )
        }

        // Validation dialog
        if (showValidationDialog) {
            ValidationDialog9(
                message = validationMessage,
                onDismiss = { showValidationDialog = false } // Close the dialog
            )
        }
    }
}

@Composable
fun ValidationDialog9(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)) // Blue color
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}

fun validateCheckoutConditions9(
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    selectedIssueDate: Long?,
    selectedReturnDate: Long?,
    exchangePairs: List<Pair<Map<String, String>, Map<String, String>>>, // Explicitly typed
    lpgExchangePairs: List<Pair<Map<String, String>, Map<String, String>>> // Explicitly typed
): String? {
    return when {
        issuedCylinders.isEmpty() && issuedInventory.isEmpty() && exchangePairs.isEmpty() && lpgExchangePairs.isEmpty() -> "Nothing added to cart."
        selectedIssueDate == null -> "No issue date selected."
        selectedReturnDate == null -> "No return date selected."
        else -> null // No error
    }
}

@Composable
fun CheckoutDialog9(
    totalPrice: Double,
    selectedIssueDate: Long?,
    selectedReturnDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (cash: String, credit: String) -> Unit
) {
    var cash by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if(isLoading){} else onDismiss() },
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Checkout")
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF2f80eb)
                    )
                }
            }},
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    // Display total price
                    Text(
                        text = "Total Price: $${totalPrice}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display issue date
                    Text(
                        text = "Issue Date: ${selectedIssueDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt())
                        } ?: "Not selected"}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display return date
                    Text(
                        text = "Return Date: ${selectedReturnDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt())
                        } ?: "Not selected"}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Cash input
                    OutlinedTextField(
                        value = cash,
                        onValueChange = { cash = it },
                        label = { Text("Cash") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    // Credit input
                    OutlinedTextField(
                        value = credit,
                        onValueChange = { credit = it },
                        label = { Text("Credit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }


            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    onConfirm(cash, credit)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun CombinedList9(
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    exchangePairs: List<Pair<Map<String, String>, Map<String, String>>>,
    onDeleteCylinder: (IssuedCylinder) -> Unit,
    onDeleteInventory: (InventoryItem) -> Unit,
    onDeleteExchange: (Pair<Map<String, String>, Map<String, String>>) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    onUpdateAlreadySelectedLPGQuantities: (Map<String, Int>) -> Unit,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit,
    alreadySelectedCylinders: List<String>, // Add this parameter
    onDeleteLPGExchange: (Pair<Map<String, String>, Map<String, String>>) -> Unit, // Callback for deleting LPG exchange pairs
    lpgExchangePairs: List<Pair<Map<String, String>, Map<String, String>>>, // LPG exchange pairs
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 100.dp)
    ) {
        // Display LPG exchange pairs
        items(lpgExchangePairs.size) { index ->
            val pair = lpgExchangePairs[index]
            LPGExchangeCard9(
                returnPair = pair.first,
                issuePair = pair.second,
                onDelete = { onDeleteLPGExchange(pair) }
            )
        }

        items(exchangePairs.size) { index ->
            val pair = exchangePairs[index]
            ExchangeCylinderCard9(
                oldCylinder = pair.first,
                newCylinder = pair.second,
                onDelete = {
                    onDeleteExchange(pair)
                    // Restore the exact pair's serial numbers
                    val oldSerialNumber = pair.first["Serial Number"] ?: ""
                    val newSerialNumber = pair.second["Serial Number"] ?: ""
                    onUpdateAlreadySelectedCylinders(
                        alreadySelectedCylinders.filterNot { it == oldSerialNumber || it == newSerialNumber }
                    )
                }
            )
        }

        issuedCylinders
            .groupBy { Pair(it.gasType, it.volumeType) }
            .forEach { (_, groupCylinders) ->
                val totalQuantity = groupCylinders.sumOf { it.quantity }
                val totalPrice = if (groupCylinders.first().gasType == "LPG") {
                    groupCylinders.sumOf { it.quantity * it.totalPrice }
                } else {
                    groupCylinders.sumOf { it.totalPrice }
                }
                val groupedCylinder = IssuedCylinder(
                    serialNumber = "",
                    gasType = groupCylinders.first().gasType,
                    volumeType = groupCylinders.first().volumeType,
                    quantity = totalQuantity,
                    totalPrice = totalPrice
                )
                item {
                    IssuedCylinderCard9(
                        cylinder = groupedCylinder,
                        onDelete = {
                            groupCylinders.forEach { onDeleteCylinder(it) }
                            if (groupedCylinder.gasType == "LPG") {
                                val formattedVolumeType = groupedCylinder.volumeType.replace(".", ",")
                                val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                                onUpdateAlreadySelectedLPGQuantities(
                                    alreadySelectedLPGQuantities + mapOf(
                                        formattedVolumeType to (alreadySelectedQuantity - groupedCylinder.quantity)
                                    )
                                )
                            }
                        }
                    )
                }

            }

        // Add inventory to the list
        issuedInventory
            .groupBy { it.name } // Group inventory items by name
            .forEach { (_, groupItems) ->
                // Calculate total quantity and total price for the group
                val totalQuantity = groupItems.sumOf { it.quantity }
                val totalPrice = groupItems.sumOf { it.price * it.quantity }

                // Create a grouped inventory item for display
                val groupedInventoryItem = InventoryItem(
                    name = groupItems.first().name,
                    quantity = totalQuantity,
                    price = totalPrice
                )

                item {
                    IssuedInventoryCard2(
                        inventoryItem = groupedInventoryItem,
                        onDelete = {
                            // Delete all items in this group
                            groupItems.forEach { onDeleteInventory(it) }
                        }
                    )
                }
            }
    }
}
@Composable
fun IssuedInventoryCard2(
    inventoryItem: InventoryItem,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Inventory") },
            text = { Text("Are you sure you want to delete this inventory item?") },
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
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Card content (left side)
            Column {
                Text(
                    text = inventoryItem.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Total Quantity: ${inventoryItem.quantity}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Total Price: ${inventoryItem.price}",
                    fontSize = 14.sp
                )
            }

            // Bin icon (right side)
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(48.dp)
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




@Composable
fun AddCylinderDialog9(
    onDismiss: () -> Unit,
    onAddCylinder: (IssuedCylinder) -> Unit,
    db: FirebaseFirestore,
    alreadySelectedCylinders: List<String>,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    isBackButtonEnabled: MutableState<Boolean>,
    issuedCylinders: List<IssuedCylinder>, // Pass issued cylinders from the main screen
    lpgExchangePairs: List<Pair<Map<String, String>, Map<String, String>>>, // Pass LPG exchange pairs
) {
    isBackButtonEnabled.value = false // Disable the back button for this dialog
    var gasType by remember { mutableStateOf<String?>(null) }
    var volumeType by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var prices by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf<Double>(0.0) }
    var cylinderOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var volumeOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var localAlreadySelectedCylinders by remember { mutableStateOf(alreadySelectedCylinders) }
    var showValidationMessage by remember { mutableStateOf(false) } // State for validation message
    var availableLPGQuantity by remember { mutableStateOf<Int?>(null) } // State for available LPG quantity
    var quantityError by remember { mutableStateOf<String?>(null) } // State for quantity error message

    val coroutineScope = rememberCoroutineScope()

    // Fetch gas types
    LaunchedEffect(Unit) {
        val gases = db.collection("Gases").get().documents
        cylinderOptions = gases.map { it.id }
    }

    // Fetch volume types when gasType changes
    LaunchedEffect(gasType) {
        if (gasType != null) {
            val document = db.collection("Gases").document(gasType!!).get()
            val volumesAndSP = document.get("VolumesAndSP") as? Map<String, Int>
            volumesAndSP?.let {
                volumeOptions = it.keys.map { volume -> volume.replace(",", ".") }
            }
        } else {
            volumeOptions = emptyList()
        }
        // Clear available LPG quantity when gas type changes
        availableLPGQuantity = null
    }

    // Fetch available cylinders when gasType or volumeType changes (only for non-LPG gas types)
    LaunchedEffect(gasType, volumeType) {
        if (gasType != null && volumeType != null) {
            coroutineScope.launch {
                val gasDoc = db.collection("Gases").document(gasType!!).get()
                val volumesAndSP = gasDoc.get("VolumesAndSP") as? Map<String, String>
                val formattedVolumeType = volumeType!!.replace(".", ",")
                val defaultPrice = volumesAndSP?.get(formattedVolumeType)?.toString() ?: ""
                prices = defaultPrice
            }
        }
        if (gasType == "LPG" && volumeType != null) {
            println("volumeType: $volumeType")
            coroutineScope.launch {
                val lpgDocument = db.collection("Cylinders").document("LPG").get()
                val lpgFullMap = lpgDocument.get("LPGFull") as? Map<String, Int>
                lpgFullMap?.let {
                    // Replace commas in key names with dots
                    val formattedVolumeType = volumeType!!.replace(".", ",")
                    println("formattedVolumeType: $formattedVolumeType")
                    val totalQuantity = it[formattedVolumeType] ?: 0
                    println("totalQuantity: $totalQuantity")
                    val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                    println("alreadySelectedQuantity: $alreadySelectedQuantity")
                    val issuedQuantity = issuedCylinders
                        .filter { it.gasType == "LPG" && it.volumeType == volumeType }
                        .sumOf { it.quantity }
                    println("issuedQuantity: $issuedQuantity")
                    availableLPGQuantity = totalQuantity
                    println("availableLPGQuantity: $availableLPGQuantity")

                    val gasDoc = db.collection("Gases").document("LPG").get()
                    val volumesAndSP = gasDoc.get("VolumesAndSP") as? Map<String, String>
                    val defaultPrice = volumesAndSP?.get(formattedVolumeType)?.toString() ?: ""
                    prices = defaultPrice
                }
            }
        } else {
            availableCylinders = emptyList()
            availableLPGQuantity = null
        }
    }

    // Calculate the total issued quantity for the selected volume type
    val totalIssuedQuantityForVolumeType = remember(lpgExchangePairs, volumeType?.replace(",", "."), issuedCylinders) {
        println("lpgExchangePairs here: $lpgExchangePairs")
        println("volumeType here: $volumeType")
        println("issuedCylinders here: $issuedCylinders")

        val exchangeQuantity = lpgExchangePairs
            .filter { it.second["Volume Type"] == volumeType?.replace(".",",") }
            .sumOf { it.second["Quantity"]?.toIntOrNull() ?: 0 }
        println("exchangeQuantity here: $exchangeQuantity")

        val addCylinderQuantity = issuedCylinders
            .filter { it.volumeType == volumeType?.replace(",", ".") && it.gasType == "LPG" }
            .sumOf { it.quantity }
        println("addCylinderQuantity here: $addCylinderQuantity")

        exchangeQuantity + addCylinderQuantity
    }

    // Initialize selectedCylinders when quantity changes (only for non-LPG gas types)
    LaunchedEffect(quantity.toIntOrNull()) {
        if (gasType != "LPG") {
            val quantityInt = quantity.toIntOrNull() ?: 0
            selectedCylinders = List(quantityInt) { "" }
        }
    }

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
                        Text("Add Cylinder", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                        // Gas Type Dropdown
                        Text("Gas Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdown3(
                            options = cylinderOptions,
                            selectedItem = gasType,
                            onItemSelected = { gasType = it },
                            placeholder = "Select Gas Type",
                            keyboardType = KeyboardType.Text
                        )

                        // Volume Type Dropdown
                        Text("Volume Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdown3(
                            options = volumeOptions,
                            selectedItem = volumeType,
                            onItemSelected = { volumeType = it },
                            placeholder = "Select Volume Type",
                            keyboardType = KeyboardType.Number
                        )

                        // Display available LPG quantity if gasType is LPG
                        if (gasType == "LPG" && availableLPGQuantity != null) {
                            Text(
                                text = "Available LPG Quantity: $availableLPGQuantity",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (gasType != "LPG" && gasType != null && volumeType != null) {
                            Text(
                                text = "Available Cylinders: ${availableCylinders.size}",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Text("Quantity & Price", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        // Quantity Input
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
                        OutlinedTextField(
                            value = prices,
                            onValueChange = { prices = it },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if(gasType != "LPG" && gasType != null && volumeType != null) {
                            LaunchedEffect(gasType, volumeType) {
                                coroutineScope.launch {
                                    val allCylinders = fetchCylindersByStatus(db, gasType!!, volumeType!!, "Full")
                                    availableCylinders = allCylinders.filter { it !in alreadySelectedCylinders }
                                }
                            }
                        }

                        // Cylinder Dropdowns (only for non-LPG gas types)
                        if (gasType != "LPG" && quantity.toIntOrNull() != null) {
                            LaunchedEffect(quantity.toIntOrNull()) {
                                localAlreadySelectedCylinders = emptyList()
                                selectedCylinders = List(quantity.toInt()) { "" }
                                if (gasType != null && volumeType != null) {
                                    coroutineScope.launch {
                                        val allCylinders = fetchCylindersByStatus(db, gasType!!, volumeType!!, "Full")
                                        availableCylinders = allCylinders.filter { it !in alreadySelectedCylinders }
                                    }
                                }
                            }

                            repeat(quantity.toInt()) { index ->
                                Text("Cylinder ${index + 1}", fontWeight = FontWeight.Bold)
                                SearchableDropdown3(
                                    options = availableCylinders,
                                    selectedItem = selectedCylinders.getOrNull(index),
                                    onItemSelected = { selectedCylinder ->
                                        selectedCylinders = selectedCylinders.toMutableList().apply { set(index, selectedCylinder) }
                                        localAlreadySelectedCylinders = localAlreadySelectedCylinders + selectedCylinder + alreadySelectedCylinders
                                        availableCylinders = availableCylinders.filter { it != localAlreadySelectedCylinders[index] }
                                    },
                                    placeholder = "Select Cylinder",
                                    keyboardType = KeyboardType.Number
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
                                    text = "All fields are necessary",
                                    color = Color.Red,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            // Cancel Button
                            TextButton(onClick = onDismiss
                            ) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Add Button
                            Button(
                                onClick = {
                                    // Check if all fields are filled
                                    if (gasType.isNullOrEmpty() || volumeType.isNullOrEmpty() || quantity.isEmpty() || prices.isEmpty() || (gasType != "LPG" && selectedCylinders.any { it.isEmpty() })) {
                                        showValidationMessage = true
                                    } else {
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        if (gasType == "LPG" && availableLPGQuantity != null) {
                                            // Validate quantity for LPG
                                            val totalQuantity = totalIssuedQuantityForVolumeType + quantityInt
                                            if (totalQuantity > availableLPGQuantity!!) {
                                                quantityError = "Total quantity ($totalQuantity) cannot exceed available quantity ($availableLPGQuantity)"
                                                return@Button
                                            }
                                        }
                                        totalPrice = prices.toDoubleOrNull() ?: 0.0
                                        if (gasType == "LPG") {
                                            // For LPG, create an IssuedCylinder without serial numbers
                                            onAddCylinder(
                                                IssuedCylinder(
                                                    serialNumber = "", // Leave empty for LPG
                                                    gasType = gasType ?: "",
                                                    volumeType = volumeType ?: "",
                                                    quantity = quantityInt,
                                                    totalPrice = totalPrice
                                                )
                                            )
                                        } else {
                                            // For non-LPG gas types, create IssuedCylinder for each selected cylinder
                                            selectedCylinders.forEach { serialNumber ->
                                                onAddCylinder(
                                                    IssuedCylinder(
                                                        serialNumber = serialNumber,
                                                        gasType = gasType ?: "",
                                                        volumeType = volumeType ?: "",
                                                        quantity = 1,
                                                        totalPrice = totalPrice
                                                    )
                                                )
                                            }
                                        }
                                        onUpdateAlreadySelectedCylinders(localAlreadySelectedCylinders)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Add", color = Color.White)
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
fun SearchableDropdown9(
    options: List<Map<String, String>>, // Accept cylinder details as a list of maps
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    onClearSelection: () -> Unit = {},
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedItem ?: "") }
    val filteredOptions = options.filter {
        it["Serial Number"]?.contains(searchQuery, ignoreCase = true) == true
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = searchQuery.replace(",", "."),
                onValueChange = {
                    searchQuery = it
                    expanded = it.isNotEmpty()
                    if (it.isEmpty()) {
                        onClearSelection()
                    }
                },
                label = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                        .border(1.dp, MaterialTheme.colors.onSurface)
                        .heightIn(max = 200.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        if (filteredOptions.isEmpty()) {
                            item {
                                Text(
                                    "No options found",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        } else {
                            items(filteredOptions.size) { index ->
                                val option = filteredOptions[index]
                                val serialNumber = option["Serial Number"] ?: ""
                                val gasType = option["Gas Type"] ?: ""
                                val volumeType = option["Volume Type"] ?: ""

                                // Display Serial Number, Gas Type, and Volume Type in the dropdown item
                                Text(
                                    text = "$serialNumber - $gasType ($volumeType)".replace(",", "."),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = serialNumber
                                            onItemSelected(serialNumber)
                                            expanded = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun checkoutCylinders9(
    db: FirebaseFirestore,
    customerName: String?,
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    issueDate: String,
    cash: String,
    credit: String,
    returnDate: String,
    exchangePairs: List<Pair<Map<String, String>, Map<String, String>>>, // Explicitly typed
    lpgExchangePairs: List<Pair<Map<String, String>, Map<String, String>>>, // Explicitly typed
    totalPrice: Double,
    onCurrentDateTime: (String) -> Unit
): Boolean {

    var averageDays = 0

    if (customerName == null || issueDate.isEmpty()) return false
    // Fetch CylinderDetails array once at the beginning for all operations
    val cylindersRef = db.collection("Cylinders").document("Cylinders")
    val cylinderDetailsSnapshot = cylindersRef.get()
    var cylinderDetails = if (cylinderDetailsSnapshot.exists) {
        cylinderDetailsSnapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
    } else {
        emptyList()
    }


    try {
        // Get the current date and time in the format "yyyy-MM-dd_HH:mm:ss"
        val currentTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
            .toString()

        // Combine issueDate with current time
        val dateTimeString = "${issueDate}_${currentTime}"
            .replace(":", "-")
            .substringBefore(".")

        // Reference to the Transactions collection
        val transactionsRef = db.collection("Transactions")
            .document(customerName)
            .collection("DateAndTime")
            .document(dateTimeString)

        onCurrentDateTime(dateTimeString)

        transactionsRef.set(mapOf("Date" to dateTimeString))

        // Create the "Transaction Details" collection
        val transactionDetailsRef = transactionsRef.collection("Transaction Details")

        transactionDetailsRef.document("Total Price").set(mapOf("Amount" to totalPrice.toString()))

        // Push Cash document
        transactionDetailsRef.document("Cash").set(mapOf("Amount" to cash))

        transactionDetailsRef.document("Cash Out").set(mapOf("Amount" to "0"))

        // Push Credit document
        transactionDetailsRef.document("Credit").set(mapOf("Amount" to credit))

        // Push Cylinders Issued document
        val cylindersIssued = issuedCylinders.filter { it.gasType != "LPG" }.map { cylinder ->
            mapOf(
                "Serial Number" to cylinder.serialNumber,
                "Price" to cylinder.totalPrice,
                "Issue Date" to issueDate
            )
        }

        // Add cylinders from non-LPG exchange pairs (New column)
        val exchangeCylindersIssued = exchangePairs.map { pair: Pair<Map<String, String>, Map<String, String>> ->
            mapOf(
                "Serial Number" to pair.second["Serial Number"], // New cylinder
                "Price" to pair.second["Price"]?.toDoubleOrNull(),
                "Issue Date" to issueDate
            )
        }

        // Combine issued cylinders and exchange cylinders
        val allCylindersIssued = cylindersIssued + exchangeCylindersIssued

        transactionDetailsRef.document("Cylinders Issued").set(mapOf("CylindersIssued" to allCylindersIssued))

        // Push Cylinders Returned document
        val cylindersReturned = exchangePairs.map { pair: Pair<Map<String, String>, Map<String, String>> ->
            mapOf(
                "Serial Number" to pair.first["Serial Number"], // Old cylinder being returned
                "Return Date" to issueDate
            )
        }
        transactionDetailsRef.document("Cylinders Returned").set(mapOf("CylindersReturned" to cylindersReturned))

        // Push LPG Issued document
        val lpgIssued = issuedCylinders.filter { it.gasType == "LPG" }.groupBy { it.volumeType }.map { (volumeType, cylinders) ->
            mapOf(
                "Volume Type" to volumeType,
                "Price" to cylinders.sumOf { it.totalPrice },
                "Quantity" to cylinders.sumOf { it.quantity },
                "Date" to issueDate
            )
        }

        // Add LPG cylinders from LPG exchange pairs (Issued column)
        val lpgExchangeIssued = lpgExchangePairs.map { pair: Pair<Map<String, String>, Map<String, String>> ->
            mapOf(
                "Volume Type" to pair.second["Volume Type"], // Issued cylinder
                "Price" to pair.second["Price"]?.toDoubleOrNull(),
                "Quantity" to pair.second["Quantity"]?.toIntOrNull(),
                "Date" to issueDate
            )
        }

        // Combine issued LPG cylinders and exchange LPG cylinders
        val allLpgIssued = lpgIssued + lpgExchangeIssued

        transactionDetailsRef.document("LPG Issued").set(mapOf("LPGIssued" to allLpgIssued))

        // Push LPG Returned document
        val lpgReturnedMap = mutableMapOf<String, Int>()
        for (pair in lpgExchangePairs) {
            val volumeType = pair.first["Volume Type"] ?: continue
            val quantity = pair.first["Quantity"]?.toIntOrNull() ?: continue
            val volumeTypeKey = volumeType.replace(".", ",")
            lpgReturnedMap[volumeTypeKey] = (lpgReturnedMap[volumeTypeKey] ?: 0) + quantity
        }
        transactionDetailsRef.document("LPG Returned").set(mapOf("LPGReturned" to lpgReturnedMap))

        // Push Inventory Issued document
        val inventoryIssued = issuedInventory.map { inventoryItem ->
            mapOf(
                "Name" to inventoryItem.name.toString(),
                "Quantity" to inventoryItem.quantity.toString(),
                "Price" to inventoryItem.price.toString()
            )
        }
        transactionDetailsRef.document("Inventory Issued").set(mapOf("InventoryIssued" to inventoryIssued))

        // Update non-LPG cylinders in Customers > Issued Cylinders > Names > CustomerName > Details
        val nonLpgCylinders = issuedCylinders.filter { it.gasType != "LPG" }
        val exchangeNonLpgCylinders = exchangePairs.map { pair: Pair<Map<String, String>, Map<String, String>> ->
            IssuedCylinder(
                serialNumber = pair.second["Serial Number"] ?: "", // New cylinder
                gasType = pair.second["Gas Type"] ?: "",
                volumeType = pair.second["Volume Type"] ?: "",
                quantity = 1,
                totalPrice = pair.second["Price"]?.toDoubleOrNull() ?: 0.0
            )
        }

        val allNonLpgCylinders = nonLpgCylinders + exchangeNonLpgCylinders

        if (allNonLpgCylinders.isNotEmpty()) {
            val issuedCylindersRef = db.collection("Customers")
                .document("Issued Cylinders")
                .collection("Names")
                .document(customerName)

            // Fetch existing Details array
            val snapshot = issuedCylindersRef.get()
            val existingDetails = if (snapshot.exists) {
                snapshot.get("Details") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }

            // Add new serial numbers to the existing array
            val newSerialNumbers = allNonLpgCylinders.map { it.serialNumber }
            val updatedDetails = existingDetails + newSerialNumbers

            // Update the document with the new array
            issuedCylindersRef.set(mapOf("Details" to updatedDetails))
        }

        // Update LPG quantities in Customers > LPG Issued > Names > CustomerName
        val lpgCylinders = issuedCylinders.filter { it.gasType == "LPG" }
        val exchangeLpgCylinders = lpgExchangePairs.map { pair: Pair<Map<String, String>, Map<String, String>> ->
            IssuedCylinder(
                serialNumber = "", // LPG doesn't have serial numbers
                gasType = "LPG",
                volumeType = pair.second["Volume Type"] ?: "",
                quantity = pair.second["Quantity"]?.toIntOrNull() ?: 0,
                totalPrice = pair.second["Price"]?.toDoubleOrNull() ?: 0.0
            )
        }

        val allLpgCylinders = lpgCylinders + exchangeLpgCylinders


        if (allLpgCylinders.isNotEmpty()) {
            val lpgIssuedRef = db.collection("Customers")
                .document("LPG Issued")
                .collection("Names")
                .document(customerName)

            // Fetch existing LPG quantities map
            val snapshot = lpgIssuedRef.get()
            val existingLpgQuantities = if (snapshot.exists) {
                snapshot.get("Quantities") as? Map<String, String> ?: emptyMap()
            } else {
                emptyMap()
            }

            // Update quantities for each volume type
            val updatedLpgQuantities = allLpgCylinders.groupBy { it.volumeType.replace(".", ",") }.mapValues { (volumeType, cylinders) ->
                val existingQuantity = existingLpgQuantities[volumeType]?.toIntOrNull() ?: 0
                val newQuantity = existingQuantity + cylinders.sumOf { it.quantity }
                newQuantity.toString() // Store as string
            }

            // Merge the existing quantities with the updated quantities
            val mergedLpgQuantities = existingLpgQuantities.toMutableMap().apply {
                updatedLpgQuantities.forEach { (volumeType, quantity) ->
                    this[volumeType.replace(".", ",")] = quantity
                }
            }

            // Update the document with the merged map
            lpgIssuedRef.set(mapOf("Quantities" to mergedLpgQuantities))
        }

        // Fetch the CylinderDetails array from the Cylinders document
        val cylindersRef = db.collection("Cylinders").document("Cylinders")
        val cylinderDetailsSnapshot = cylindersRef.get()
        var cylinderDetails = if (cylinderDetailsSnapshot.exists) {
            cylinderDetailsSnapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
        } else {
            emptyList()
        }

        // Update CylinderDetails in Cylinders > Cylinders > CylinderDetails
        val nonLpgCylinderSerialNumbers = allNonLpgCylinders.map { it.serialNumber }

        if (nonLpgCylinderSerialNumbers.isNotEmpty()) {
            var totalDaysBetween = 0
            var daysForAverage = 0
            var cylinderCount = 0

            // Calculate rent for old cylinders and update the new cylinders
            val updatedCylinderDetails = cylinderDetails.map { cylinder ->
                if (cylinder["Serial Number"] in nonLpgCylinderSerialNumbers) {
                    // Find the old cylinder paired with this new cylinder
                    val oldCylinderPair = exchangePairs.find { it.second["Serial Number"] == cylinder["Serial Number"] }
                    if (oldCylinderPair != null) {
                        val oldCylinderSerialNumber = oldCylinderPair.first["Serial Number"]
                        val oldCylinder = cylinderDetails.find { it["Serial Number"] == oldCylinderSerialNumber }

                        if (oldCylinder != null) {
                            // Calculate the rent
                            val oldIssueDateString = oldCylinder["Issue Date"]
                            val rent = if (oldIssueDateString != null) {
                                try {
                                    // Parse the old issue date
                                    val oldIssueDate = LocalDate.parse(oldIssueDateString)
                                    println("Old issue date: $oldIssueDate")
                                    // Parse the current issue date (selected by the customer)
                                    val currentIssueDate = LocalDate.parse(issueDate)
                                    // Calculate the number of days between the two dates
                                    var daysBetween = oldIssueDate.daysUntil(currentIssueDate)

                                    daysForAverage = daysBetween.toInt()
                                    daysBetween = (daysBetween.toInt() + oldCylinder["Rent"]!!.toInt())

                                    // Accumulate days between and count of cylinders
                                    totalDaysBetween += daysForAverage
                                    cylinderCount++

                                    // Return the string value
                                    daysBetween.toString()
                                } catch (e: Exception) {
                                    println("Invalid date format: $oldIssueDateString or $issueDate")
                                    "0" // Default to 0 if there's an error
                                }
                            } else {
                                "0" // Default to 0 if oldIssueDateString is null
                            }

                            println("Rent: $rent")

                            // Update the new cylinder's map with the rent
                            cylinder.toMutableMap().apply {
                                this["Status"] = "Issued"
                                this["Notifications Date"] = returnDate
                                this["Issue Date"] = issueDate
                                this["Issued At Price"] = allNonLpgCylinders.find { it.serialNumber == this["Serial Number"] }?.totalPrice.toString()
                                this["Rent"] = rent.toString() // Add the rent field
                            }
                        } else {
                            cylinder // Return the cylinder unchanged if oldCylinder is null
                        }
                    } else {
                        cylinder
                    }
                } else {
                    cylinder
                }
            }

            if (allNonLpgCylinders.isNotEmpty()) {
                var totalDaysBetween = 0
                var cylinderCount = 0

                // First update: Update status and notifications for newly issued cylinders
                cylinderDetails = cylinderDetails.map { cylinder ->
                    if (cylinder["Serial Number"] in allNonLpgCylinders.map { it.serialNumber }) {
                        cylinder.toMutableMap().apply {
                            this["Status"] = "Issued"
                            this["Notifications Date"] = returnDate
                            this["Issue Date"] = issueDate
                            this["Issued At Price"] = allNonLpgCylinders.find { it.serialNumber == this["Serial Number"] }?.totalPrice.toString()
                        }
                    } else {
                        cylinder
                    }
                }

                // Second update: Calculate rent for exchanged cylinders
                cylinderDetails = cylinderDetails.map { cylinder ->
                    if (cylinder["Serial Number"] in allNonLpgCylinders.map { it.serialNumber }) {
                        val oldCylinderPair = exchangePairs.find { it.second["Serial Number"] == cylinder["Serial Number"] }
                        if (oldCylinderPair != null) {
                            val oldCylinderSerialNumber = oldCylinderPair.first["Serial Number"]
                            val oldCylinder = cylinderDetails.find { it["Serial Number"] == oldCylinderSerialNumber }

                            if (oldCylinder != null) {
                                val oldIssueDateString = oldCylinder["Issue Date"]
                                val rent = if (oldIssueDateString != null) {
                                    try {
                                        val oldIssueDate = LocalDate.parse(oldIssueDateString)
                                        val currentIssueDate = LocalDate.parse(issueDate)
                                        var daysBetween = oldIssueDate.daysUntil(currentIssueDate)

                                        totalDaysBetween += daysBetween.toInt()
                                        cylinderCount++

                                        daysBetween = (daysBetween.toInt() + oldCylinder["Rent"]!!.toInt())
                                        daysBetween.toString()
                                    } catch (e: Exception) {
                                        "0"
                                    }
                                } else {
                                    "0"
                                }

                                cylinder.toMutableMap().apply {
                                    this["Rent"] = rent
                                }
                            } else {
                                cylinder
                            }
                        } else {
                            cylinder
                        }
                    } else {
                        cylinder
                    }
                }

                // Third update: Handle returned cylinders
                cylinderDetails = cylinderDetails.map { details ->
                    if (details["Serial Number"] in exchangePairs.map { it.first["Serial Number"] }) {
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

                // Save the final updated CylinderDetails array to Firestore (done only once)
                cylindersRef.set(mapOf("CylinderDetails" to cylinderDetails))
            }

            // Calculate average days
            val averageDays = if (cylinderCount > 0) totalDaysBetween / cylinderCount else 0

            val customerDetailsRef = db.collection("Customers")
                .document("Details")
                .collection("Names")
                .document(customerName)

            // Fetch the existing "Details" map
            val customerDetailsSnapshot = customerDetailsRef.get()
            if (customerDetailsSnapshot.exists) {
                val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

                // Get the existing Average Days and calculate new average
                val existingAverageDays = detailsMap["Average Days"]?.toIntOrNull() ?: 0

                val newAverageDays: Int
                if (existingAverageDays == 0) {
                    // If the existing average is 0, set the new average to the current average
                    newAverageDays = averageDays
                }
                else {
                    // Calculate the new average days
                    newAverageDays = (existingAverageDays + averageDays) / 2
                }

                // Create the updated map
                val updatedDetailsMap = detailsMap.toMutableMap().apply {
                    this["Average Days"] = newAverageDays.toString()
                }

                // Save the updated map back to Firestore
                customerDetailsRef.update("Details" to updatedDetailsMap)
                println("Successfully updated 'Credit' and 'Deposit' fields for customer $customerName")

            } else {
                throw Exception("Document 'Customers > Details > Names > $customerName' does not exist")
            }

            // Save the updated CylinderDetails array back to Firestore
            // cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
        }

        // Create collections for each non-LPG cylinder in Cylinders > Customers
        for (cylinder in allNonLpgCylinders) {
            val cylinderRef = db.collection("Cylinders")
                .document("Customers")
                .collection(cylinder.serialNumber)

            // Create Currently Issued To document
            cylinderRef.document("Currently Issued To").set(
                mapOf(
                    "name" to customerName,
                    "date" to issueDate,
                    "price" to cylinder.totalPrice.toString()
                )
            )

            // Create or update Previous Customers document
            val previousCustomersRef = cylinderRef.document("Previous Customers")
            val snapshot = previousCustomersRef.get()
            val existingCustomers = if (snapshot.exists) {
                snapshot.get("customers") as? List<Map<String, String>> ?: emptyList()
            } else {
                emptyList()
            }

            // Add the current customer details to the existing array
            val newCustomerDetails = mapOf(
                "name" to customerName,
                "date" to issueDate,
                "price" to cylinder.totalPrice.toString()
            )
            val updatedCustomers = existingCustomers + newCustomerDetails

            // Update the Previous Customers document
            previousCustomersRef.set(mapOf("customers" to updatedCustomers))
        }

        // Update LPGFull map in Cylinders > LPG document
        val lpgDocumentRef = db.collection("Cylinders").document("LPG")
        val lpgDocumentSnapshot = lpgDocumentRef.get()
        if (lpgDocumentSnapshot.exists) {
            val lpgFullMap = lpgDocumentSnapshot.get("LPGFull") as? Map<String, Int> ?: emptyMap()
            val updatedLpgFullMap = allLpgCylinders.groupBy { it.volumeType.replace(".", ",") }.entries.fold(lpgFullMap.toMutableMap()) { acc, entry ->
                val (volumeType, cylinders) = entry
                val issuedQuantity = cylinders.sumOf { it.quantity }
                val currentQuantity = acc[volumeType] ?: 0
                acc[volumeType] = currentQuantity - issuedQuantity
                acc
            }
            lpgDocumentRef.update("LPGFull" to updatedLpgFullMap)
        }

        // Handle returned cylinders (non-LPG and LPG)
        // Non-LPG cylinders (Old column)
        val oldCylinders = exchangePairs.map { it.first } // Old cylinders being returned
        for (cylinder in oldCylinders) {
            val serialNumber = cylinder["Serial Number"] ?: continue

            // Step 1: Update the "Currently Issued To" document
            val currentlyIssuedToRef = db.collection("Cylinders")
                .document("Customers")
                .collection(serialNumber)
                .document("Currently Issued To")

            // Set all fields to empty strings
            currentlyIssuedToRef.set(
                mapOf(
                    "name" to "",
                    "date" to "",
                    "price" to ""
                )
            )

            // Step 2: Update the "CylinderDetails" array in the "Cylinders" document


            // Step 3: Remove the serial number from the "Details" array in the "Issued Cylinders" document
            val issuedCylindersRef = db.collection("Customers")
                .document("Issued Cylinders")
                .collection("Names")
                .document(customerName)

            // Fetch the existing "Details" array
            val issuedCylindersSnapshot = issuedCylindersRef.get()
            if (issuedCylindersSnapshot.exists) {
                val detailsArray = issuedCylindersSnapshot.get("Details") as? List<String> ?: emptyList()

                // Remove the serial number from the array
                val updatedDetailsArray = detailsArray.filter { it != serialNumber }

                // Update the document with the new array
                issuedCylindersRef.update("Details" to updatedDetailsArray)
            } else {
                throw Exception("Document 'Issued Cylinders > Names > $customerName' does not exist")
            }
        }

        // LPG cylinders (Return column)
        val lpgReturnCylinders = lpgExchangePairs.map { it.first } // LPG cylinders being returned
        for (cylinder in lpgReturnCylinders) {
            val volumeType = cylinder["Volume Type"] ?: continue
            val quantity = cylinder["Quantity"]?.toIntOrNull() ?: continue

            // Replace "." with "," in the volume type key
            val volumeTypeKey = volumeType.replace(".", ",")

            // Step 4: Update the "LPGEmpty" map in the "Cylinders > LPG" document
            val lpgRef = db.collection("Cylinders").document("LPG")

            // Fetch the existing "LPGEmpty" map
            val lpgSnapshot = lpgRef.get()
            if (lpgSnapshot.exists) {
                val lpgEmptyMap = lpgSnapshot.get("LPGEmpty") as? Map<String, Int> ?: emptyMap()

                // Increment the value for the volume type key
                val updatedLpgEmptyMap = lpgEmptyMap.toMutableMap().apply {
                    this[volumeTypeKey] = (this[volumeTypeKey] ?: 0) + quantity
                }

                // Save the updated map back to Firestore
                lpgRef.update("LPGEmpty" to updatedLpgEmptyMap)
            } else {
                throw Exception("Document 'Cylinders > LPG' does not exist")
            }

            // Step 5: Update the "Quantities" map in the "Customers > LPG Issued > Names > customerName" document
            val lpgIssuedRef = db.collection("Customers")
                .document("LPG Issued")
                .collection("Names")
                .document(customerName)

            // Fetch the existing "Quantities" map
            val lpgIssuedSnapshot = lpgIssuedRef.get()
            if (lpgIssuedSnapshot.exists) {
                val quantitiesMap = lpgIssuedSnapshot.get("Quantities") as? Map<String, Int> ?: emptyMap()

                // Decrement the value for the volume type key
                val updatedQuantitiesMap = quantitiesMap.toMutableMap().apply {
                    this[volumeTypeKey] = (this[volumeTypeKey] ?: 0) - quantity
                }

                // Save the updated map back to Firestore
                lpgIssuedRef.update("Quantities" to updatedQuantitiesMap)
            } else {
                throw Exception("Document 'Customers > LPG Issued > Names > $customerName' does not exist")
            }
        }

        // Step 6: Increment the "Credit" field in the "Customers > Details > Names > customerName > Details" map
        val customerDetailsRef = db.collection("Customers")
            .document("Details")
            .collection("Names")
            .document(customerName)

        // Fetch the existing "Details" map
        val customerDetailsSnapshot = customerDetailsRef.get()
        if (customerDetailsSnapshot.exists) {
            val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

            // Increment the "Credit" field
            val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
            val newCredit = currentCredit + (credit.toDoubleOrNull() ?: 0.0)

            // Update the "Details" map with the new credit value
            val updatedDetailsMap = detailsMap.toMutableMap().apply {
                this["Credit"] = newCredit.toString()
            }

            // Save the updated map back to Firestore
            customerDetailsRef.update("Details" to updatedDetailsMap)
        } else {
            throw Exception("Document 'Customers > Details > Names > $customerName' does not exist")
        }

        if (issuedInventory.isNotEmpty()) {
            val inventoryRef = db.collection("Inventory").document("Items")
            val inventorySnapshot = inventoryRef.get()

            if (inventorySnapshot.exists) {
                val existingItems = inventorySnapshot.get("items") as? List<Map<String, String>> ?: emptyList()

                val updatedItems = existingItems.map { item ->
                    val issuedItem = issuedInventory.find { it.name == item["Name"] }
                    if (issuedItem != null) {
                        val currentQuantity = item["Quantity"]?.toIntOrNull() ?: 0
                        val newQuantity = currentQuantity - issuedItem.quantity
                        item.toMutableMap().apply {
                            this["Quantity"] = newQuantity.toString()
                        }
                    } else {
                        item
                    }
                }

                inventoryRef.set(mapOf("items" to updatedItems))
            }
        }

        return true
    } catch (e: Exception) {
        println("Error in checkoutCylinders: ${e.message}")
        return false
    }
}

@Composable
fun IssuedCylinderCard9(
    cylinder: IssuedCylinder,
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // State for delete confirmation dialog

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete this group?") },
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
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Card content (left side)
            Column {
                Text(
                    text = cylinder.gasType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Volume Type: ${cylinder.volumeType}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Total Quantity: ${cylinder.quantity}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Total Price: ${cylinder.totalPrice}", // Display the calculated total price
                    fontSize = 14.sp
                )
            }

            // Bin icon (right side)
            IconButton(
                onClick = { showDeleteConfirmation = true }, // Show delete confirmation dialog
                modifier = Modifier.size(48.dp)
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


@Composable
fun ExchangeDialog(
    customerName: String,
    onDismiss: () -> Unit,
    db: FirebaseFirestore,
    onDone: (List<Pair<Map<String, String>, Map<String, String>>>) -> Unit,
    alreadySelectedCylinders: List<String>,
    alreadySelectedLPGQuantities: Map<String, Int>,
    quantities: Map<String, Int>,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit,
    onDoneLPG: (Pair<Map<String, String>, Map<String, String>>) -> Unit,
    lpgExchangePairs: List<Pair<Map<String, String>, Map<String, String>>>,
    issuedCylinders: List<IssuedCylinder>,
) {
    var quantity by remember { mutableStateOf("") }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedNewCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var cylinderOptions by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var fullCylinderDetails by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var showValidationMessage by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var isLPGSelected by remember { mutableStateOf(false) }
    var volumeType by remember { mutableStateOf<String?>(null) }
    var quantityReturn by remember { mutableStateOf("") }
    var lpgFullQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var localAlreadySelectedCylinders by remember { mutableStateOf(alreadySelectedCylinders) }
    var exchangeVolumeType by remember { mutableStateOf<String?>(null) }
    var exchangePrice by remember { mutableStateOf("") }

    // Single price for all cylinders
    var commonPrice by remember { mutableStateOf("") }

    // New state variables for validation errors
    var quantityExceedsAvailableError by remember { mutableStateOf<String?>(null) }
    var quantityReturnExceedsIssuedError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch cylinder options and full CylinderDetails
    LaunchedEffect(Unit) {
        val issuedCylindersDoc = db.collection("Customers")
            .document("Issued Cylinders")
            .collection("Names")
            .document(customerName)
            .get()
        val issuedCylinders = issuedCylindersDoc.get("Details") as? List<String> ?: emptyList()

        val cylindersDoc = db.collection("Cylinders").document("Cylinders").get()
        val cylinderDetails = cylindersDoc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

        cylinderOptions = cylinderDetails.filter { it["Serial Number"] in issuedCylinders && it["Serial Number"] !in localAlreadySelectedCylinders }
        fullCylinderDetails = cylinderDetails

        val lpgDoc = db.collection("Cylinders").document("LPG").get()
        lpgFullQuantities = lpgDoc.get("LPGFull") as? Map<String, Int> ?: emptyMap()
    }

    LaunchedEffect(quantity) {
        val quantityInt = quantity.toIntOrNull() ?: 0
        selectedCylinders = List(quantityInt) { "" }
        selectedNewCylinders = List(quantityInt) { "" }
    }

    LaunchedEffect(exchangeVolumeType) {
        if (isLPGSelected && exchangeVolumeType != null) {
            coroutineScope.launch {
                val gasDoc = db.collection("Gases").document("LPG").get()
                val volumesAndSP = gasDoc.get("VolumesAndSP") as? Map<String, Int>
                val price = volumesAndSP?.get(exchangeVolumeType)?.toString() ?: ""
                exchangePrice = price
            }
        }
    }

    // Update commonPrice when a new cylinder is selected for non-LPG case
    LaunchedEffect(selectedNewCylinders) {
        if (!isLPGSelected && selectedNewCylinders.isNotEmpty() && selectedNewCylinders.any { it.isNotEmpty() }) {
            // Get the first valid selected new cylinder
            val firstValidCylinder = selectedNewCylinders.firstOrNull { it.isNotEmpty() }
            if (firstValidCylinder != null) {
                val cylinder = fullCylinderDetails.find { it["Serial Number"] == firstValidCylinder }
                val gasType = cylinder?.get("Gas Type")
                val volumeType = cylinder?.get("Volume Type")

                if (gasType != null && volumeType != null) {
                    coroutineScope.launch {
                        val gasDoc = db.collection("Gases").document(gasType).get()
                        val volumesAndSP = gasDoc.get("VolumesAndSP") as? Map<String, Int>
                        val price = volumesAndSP?.get(volumeType.replace(".",","))?.toString() ?: ""
                        commonPrice = price
                    }
                }
            }
        }
    }

    val availableCylinderOptions = remember(selectedCylinders, localAlreadySelectedCylinders) {
        cylinderOptions.filter { cylinder ->
            cylinder["Serial Number"] !in selectedCylinders && cylinder["Serial Number"] !in localAlreadySelectedCylinders
        }
    }

    LaunchedEffect(quantityError) {
        if (quantityError != null) {
            delay(4000)
            quantityError = null
        }
    }

    LaunchedEffect(showValidationMessage) {
        if (showValidationMessage) {
            delay(4000)
            showValidationMessage = false
        }
    }

    // Calculate the sum of quantities for the selected volume type in the LazyColumn
    val totalIssuedQuantityForVolumeType = remember(lpgExchangePairs,
        exchangeVolumeType?.replace(",","."), issuedCylinders) {
        val exchangeQuantity = lpgExchangePairs
            .filter { it.second["Volume Type"] == exchangeVolumeType }
            .sumOf { it.second["Quantity"]?.toIntOrNull() ?: 0 }

        val addCylinderQuantity = issuedCylinders
            .filter { it.volumeType == exchangeVolumeType?.replace(",",".") && it.gasType == "LPG" }
            .sumOf { it.quantity }

        exchangeQuantity + addCylinderQuantity
    }

    val totalReturnedQuantityForVolumeType = remember(lpgExchangePairs, volumeType) {
        lpgExchangePairs
            .filter { it.first["Volume Type"] == volumeType?.replace(",",".") }
            .sumOf { it.first["Quantity"]?.toIntOrNull() ?: 0 }
    }

    // Validate quantities when "Done" is clicked
    val validateQuantities: () -> Boolean = {
        if (isLPGSelected) {
            val quantityInt = quantity.toIntOrNull() ?: 0
            val quantityReturnInt = quantityReturn.toIntOrNull() ?: 0

            // Check if the sum of the input quantity and existing quantities exceeds available quantity
            val availableQuantity = exchangeVolumeType?.let { lpgFullQuantities[it] } ?: 0
            if (quantityInt + totalIssuedQuantityForVolumeType > availableQuantity) {
                quantityExceedsAvailableError = "Exceeds available quantity ($availableQuantity)"
                coroutineScope.launch {
                    delay(4000)
                    quantityExceedsAvailableError = null
                }
                false
            } else if (quantityReturnInt + totalReturnedQuantityForVolumeType > (volumeType?.let { quantities[it] } ?: 0)) {
                quantityReturnExceedsIssuedError = "Exceeds issued quantity (${quantities[volumeType]})"
                coroutineScope.launch {
                    delay(4000)
                    quantityReturnExceedsIssuedError = null
                }
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Return Cylinders", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CustomCheckbox9(
                                isChecked = isLPGSelected,
                                onCheckedChange = { isLPGSelected = it }
                            )
                            Text("LPG", fontSize = 20.sp)
                        }

                        if (quantityError != null) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = quantityError!!.split("\n")[0],
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = quantityError!!.split("\n")[1],
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (isLPGSelected) {
                            // Return Section
                            Text("Return", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                            Text("Volume Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            SearchableDropdown3(
                                options = quantities.keys.toList(),
                                selectedItem = volumeType,
                                onItemSelected = { volumeType = it },
                                placeholder = "Select Volume Type",
                                displayText = { volumeType ->
                                    val issuedCount = quantities[volumeType] ?: 0
                                    "$volumeType (Issued: $issuedCount)"
                                },
                                keyboardType = KeyboardType.Number
                            )

                            Text("Quantity Return", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            OutlinedTextField(
                                value = quantityReturn,
                                onValueChange = { newValue ->
                                    quantityReturn = newValue
                                    val quantityReturnInt = newValue.toIntOrNull() ?: 0
                                    val issuedQuantity = volumeType?.let { quantities[it] } ?: 0
                                    if (quantityReturnInt <= 0 || quantityReturnInt > issuedQuantity) {
                                        quantityReturnExceedsIssuedError = "Quantity must be less than $issuedQuantity"
                                    } else {
                                        quantityReturnExceedsIssuedError = null
                                    }
                                },
                                label = { Text("Quantity Return") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (quantityReturnExceedsIssuedError != null) {
                                Text(
                                    text = quantityReturnExceedsIssuedError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Issue Section
                            Text("Issue", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
                            Text("Volume Type for Exchange", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            SearchableDropdown3(
                                options = lpgFullQuantities.keys.toList(),
                                selectedItem = exchangeVolumeType,
                                onItemSelected = { exchangeVolumeType = it },
                                placeholder = "Select Volume Type for Exchange",
                                displayText = { volumeType ->
                                    val availableQuantity = lpgFullQuantities[volumeType] ?: 0
                                    "$volumeType (Available: $availableQuantity)"
                                },
                                keyboardType = KeyboardType.Number
                            )

                            Text("Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { newValue ->
                                    quantity = newValue
                                    val quantityInt = newValue.toIntOrNull() ?: 0
                                    val availableQuantity = exchangeVolumeType?.let { lpgFullQuantities[it] } ?: 0
                                    if (quantityInt <= 0 || quantityInt > availableQuantity) {
                                        quantityExceedsAvailableError = "Quantity must be less than $availableQuantity"
                                    } else {
                                        quantityExceedsAvailableError = null
                                    }
                                },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (quantityExceedsAvailableError != null) {
                                Text(
                                    text = quantityExceedsAvailableError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Text("Price", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            OutlinedTextField(
                                value = exchangePrice,
                                onValueChange = { exchangePrice = it },
                                label = { Text("Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Non-LPG Section
                            Text("Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Common price field for all cylinders
                            Text("Price (for all cylinders)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            OutlinedTextField(
                                value = commonPrice,
                                onValueChange = { commonPrice = it },
                                label = { Text("Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            val quantityInt = quantity.toIntOrNull() ?: 0
                            repeat(quantityInt) { index ->
                                Text("Cylinder ${index + 1}", fontWeight = FontWeight.Bold)
                                SearchableDropdown9(
                                    options = availableCylinderOptions,
                                    selectedItem = selectedCylinders.getOrNull(index),
                                    onItemSelected = { selectedItem ->
                                        selectedCylinders = selectedCylinders.toMutableList().apply {
                                            if (index < size) {
                                                set(index, selectedItem)
                                            }
                                        }
                                    },
                                    placeholder = "Select Cylinder",
                                )

                                if (selectedCylinders.getOrNull(index)?.isNotEmpty() == true) {
                                    val selectedCylinder = cylinderOptions.find { it["Serial Number"] == selectedCylinders[index] }
                                    val gasType = selectedCylinder?.get("Gas Type")

                                    Text("Select New Cylinder", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                    SearchableDropdown9(
                                        options = fullCylinderDetails.filter {
                                            it["Gas Type"] == gasType &&
                                                    it["Status"] == "Full" &&
                                                    it["Serial Number"] !in selectedNewCylinders &&
                                                    it["Serial Number"] !in localAlreadySelectedCylinders
                                        },
                                        selectedItem = selectedNewCylinders.getOrNull(index),
                                        onItemSelected = { selectedItem ->
                                            selectedNewCylinders = selectedNewCylinders.toMutableList().apply {
                                                if (index < size) {
                                                    set(index, selectedItem)
                                                }
                                            }

                                            // Fetch the default price for the selected new cylinder
                                            // and update the common price if it's still empty
                                            if (commonPrice.isEmpty()) {
                                                val newCylinder = fullCylinderDetails.find { it["Serial Number"] == selectedItem }
                                                val volumeType = newCylinder?.get("Volume Type")
                                                if (gasType != null && volumeType != null) {
                                                    coroutineScope.launch {
                                                        val gasDoc = db.collection("Gases").document(gasType).get()
                                                        val volumesAndSP = gasDoc.get("VolumesAndSP") as? Map<String, Int>
                                                        val price = volumesAndSP?.get(volumeType.replace(".",","))?.toString() ?: ""
                                                        commonPrice = price
                                                    }
                                                }
                                            }
                                        },
                                        placeholder = "Select New Cylinder",
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showValidationMessage) {
                                Text(
                                    text = "All fields are necessary",
                                    color = Color.Red,
                                    modifier = Modifier.padding(end = 8.dp),
                                    fontSize = 12.sp
                                )
                            }

                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (validateQuantities()) {
                                        if (isLPGSelected) {
                                            // Handle LPG case
                                            if (volumeType.isNullOrEmpty() || quantityReturn.isEmpty() || exchangeVolumeType.isNullOrEmpty() || quantity.isEmpty() || exchangePrice.isEmpty()) {
                                                showValidationMessage = true
                                            } else {
                                                // Create a pair for LPG exchange
                                                val returnPair = mapOf(
                                                    "Volume Type" to volumeType!!,
                                                    "Quantity" to quantityReturn
                                                )
                                                val issuePair = mapOf(
                                                    "Volume Type" to exchangeVolumeType!!,
                                                    "Quantity" to quantity,
                                                    "Price" to exchangePrice
                                                )
                                                val lpgExchangePair = Pair(returnPair, issuePair)

                                                // Pass the LPG exchange pair to the parent
                                                onDoneLPG(lpgExchangePair)
                                                onDismiss()
                                            }
                                        } else {
                                            // Handle non-LPG case with common price
                                            if (commonPrice.isEmpty()) {
                                                showValidationMessage = true
                                            } else {
                                                val newExchangePairs = selectedCylinders.mapIndexed { index, oldSerialNumber ->
                                                    val oldCylinder = cylinderOptions.find { it["Serial Number"] == oldSerialNumber }
                                                    val newCylinder = fullCylinderDetails.find { it["Serial Number"] == selectedNewCylinders[index] }
                                                    if (oldCylinder != null && newCylinder != null) {
                                                        // Use the common price for all cylinders
                                                        Pair(oldCylinder, newCylinder + ("Price" to commonPrice))
                                                    } else {
                                                        null
                                                    }
                                                }.filterNotNull()

                                                val newSelectedCylinders = selectedCylinders + selectedNewCylinders
                                                onUpdateAlreadySelectedCylinders(localAlreadySelectedCylinders + newSelectedCylinders)

                                                onDone(newExchangePairs)
                                                onDismiss()
                                            }
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

        item {
            Spacer(modifier = Modifier.height(500.dp))
        }
    }
}

@Composable
fun ExchangeCylinderCard9(
    oldCylinder: Map<String, String>,
    newCylinder: Map<String, String>,
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Exchange") },
            text = { Text("Are you sure you want to delete this exchange?") },
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
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Gas Symbol
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(getGasColor1(oldCylinder["Gas Type"] ?: ""), shape = CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getGasSymbol1(oldCylinder["Gas Type"] ?: ""),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Left Column: Old Cylinder
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Old",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Serial: ${oldCylinder["Serial Number"]}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Gas: ${oldCylinder["Gas Type"]}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Volume: ${oldCylinder["Volume Type"]}",
                    fontSize = 14.sp
                )
            }

            // Vertical Divider
            Divider(
                modifier = Modifier
                    .height(80.dp)
                    .width(1.dp)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: New Cylinder
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "New",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Serial: ${newCylinder["Serial Number"]}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Gas: ${newCylinder["Gas Type"]}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Volume: ${newCylinder["Volume Type"]}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Price: ${newCylinder["Price"]}",
                    fontSize = 14.sp
                )
            }

            // Delete Button
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(48.dp)
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

@Composable
fun SearchableDropdown3(
    options: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    placeholder: String,
    displayText: (String) -> String = { it },
    keyboardType: KeyboardType// Default to display the item as is
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedItem ?: "") }
    val filteredOptions = options.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = searchQuery.replace(",", "."),
                onValueChange = {
                    searchQuery = it
                    expanded = it.isNotEmpty()
                },
                label = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                        .border(1.dp, MaterialTheme.colors.onSurface)
                        .heightIn(max = 200.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        if (filteredOptions.isEmpty()) {
                            item {
                                Text(
                                    "No options found",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        } else {
                            items(filteredOptions.size) { index ->
                                val option = filteredOptions[index]
                                Text(
                                    text = displayText(option).replace(",", "."), // Use the custom display text
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = option
                                            onItemSelected(option)
                                            expanded = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomCheckbox9(
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
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LPGExchangeCard9(
    returnPair: Map<String, String>, // Contains Volume Type and Quantity for Return
    issuePair: Map<String, String>, // Contains Volume Type, Quantity, and Price for Issue
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Calculate total price: Price * Quantity
    val totalPrice = remember(issuePair) {
        val price = issuePair["Price"]?.toDoubleOrNull() ?: 0.0
        val quantity = issuePair["Quantity"]?.toIntOrNull() ?: 1
        price * quantity
    }

    // Format the total price with 2 decimal places without using .format
    val formattedTotalPrice = if (totalPrice == totalPrice.toInt().toDouble()) {
        // If the price is a whole number, display it without decimals
        totalPrice.toInt().toString()
    } else {
        // Otherwise, display it with 2 decimal places
        val integerPart = totalPrice.toInt()
        val fractionalPart = ((totalPrice - integerPart) * 100).toInt()
        "$integerPart.${fractionalPart.toString().padStart(2, '0')}"
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Exchange") },
            text = { Text("Are you sure you want to delete this exchange?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
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
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Gas Symbol
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(getGasColor1("LPG"), shape = CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getGasSymbol1("LPG"),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Left Column: Return
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Return",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Volume: ${returnPair["Volume Type"]?.replace(",",".")}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Quantity: ${returnPair["Quantity"]}",
                    fontSize = 14.sp
                )
            }

            // Vertical Divider
            Divider(
                modifier = Modifier
                    .height(80.dp)
                    .width(1.dp)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Issued
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Issued",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Volume: ${issuePair["Volume Type"]?.replace(",",".")}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Quantity: ${issuePair["Quantity"]}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Price: $$formattedTotalPrice", // Use formattedTotalPrice
                    fontSize = 14.sp
                )
            }

            // Delete Button
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(48.dp)
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

@Composable
fun AddInventoryDialog2(
    onDismiss: () -> Unit,
    onAddInventory: (InventoryItem) -> Unit,
    db: FirebaseFirestore,
    alreadySelectedInventory: List<String>,
    onUpdateAlreadySelectedInventory: (List<String>) -> Unit,
    alreadySelectedInventoryQuantities: Map<String, Int>,
    isBackButtonEnabled: MutableState<Boolean>
) {
    isBackButtonEnabled.value = false // Disable the back button for this dialog
    var inventoryName by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var inventoryOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showValidationMessage by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var availableInventoryQuantity by remember { mutableStateOf<Int?>(null) }

    // Fetch inventory items from Firestore
    LaunchedEffect(Unit) {
        val inventoryDocument = db.collection("Inventory").document("Items").get()
        val inventoryItems = inventoryDocument.get("items") as? List<Map<String, String>> ?: emptyList()
        inventoryOptions = inventoryItems.map { it["Name"] as String }
    }

    // Fetch available quantity when inventoryName changes
    LaunchedEffect(inventoryName) {
        if (inventoryName != null) {
            val inventoryDocument = db.collection("Inventory").document("Items").get()
            val inventoryItems = inventoryDocument.get("items") as? List<Map<String, String>> ?: emptyList()
            val selectedItem = inventoryItems.find { it["Name"] == inventoryName }
            selectedItem?.let {
                val totalQuantity = it["Quantity"]?.toIntOrNull() ?: 0
                val alreadySelectedQuantity = alreadySelectedInventoryQuantities[inventoryName!!] ?: 0
                availableInventoryQuantity = totalQuantity - alreadySelectedQuantity
                price = it["Price"] ?: ""
            }
        } else {
            availableInventoryQuantity = null
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
                        Text("Add Inventory", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                        // Inventory Name Dropdown
                        Text("Inventory Name", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdown3(
                            options = inventoryOptions,
                            selectedItem = inventoryName,
                            onItemSelected = { inventoryName = it },
                            placeholder = "Select Inventory Name",
                            keyboardType = KeyboardType.Text
                        )

                        // Display available inventory quantity
                        if (inventoryName != null && availableInventoryQuantity != null) {
                            Text(
                                text = "Available Quantity: $availableInventoryQuantity",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

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

                        // Price Input
                        Text("Price", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

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
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            // Cancel Button
                            TextButton(onClick = { onDismiss()
                               isBackButtonEnabled.value=true // Enable the back button
                            }) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Add Button
                            Button(
                                onClick = {
                                    // Check if all fields are filled
                                    if (inventoryName.isNullOrEmpty() || quantity.isEmpty() || price.isEmpty()) {
                                        showValidationMessage = true
                                    } else {
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        if (availableInventoryQuantity != null && quantityInt > availableInventoryQuantity!!) {
                                            quantityError = "Quantity must be less than or equal to $availableInventoryQuantity"
                                            return@Button
                                        }
                                        val totalPrice = price.toDoubleOrNull() ?: 0.0
                                        onAddInventory(
                                            InventoryItem(
                                                name = inventoryName!!,
                                                quantity = quantityInt,
                                                price = totalPrice
                                            )
                                        )
                                        onUpdateAlreadySelectedInventory(alreadySelectedInventory + inventoryName!!)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Add", color = Color.White)
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