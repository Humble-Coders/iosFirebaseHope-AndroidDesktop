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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.NewIssueCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.NewIssueCylinderScreenEvent


@Composable
fun NewIssueCylinderScreenUI(
    component: NewIssueCylinderScreenComponent,
    customerName: String,
    db: FirebaseFirestore
) {



    // Use the platform-specific BackButtonHandler

    // Existing state variables
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
    var currentDateTime by remember {mutableStateOf("")}
    var showTransactionConfirmation by remember { mutableStateOf(false) }
    var isReversingTransaction by remember { mutableStateOf(false) }

    // Calculate the real-time total price
    val totalPrice = remember(issuedCylinders, issuedInventory) {
        val cylinderTotal = issuedCylinders.sumOf { cylinder ->
            if (cylinder.gasType == "LPG") {
                cylinder.quantity * cylinder.totalPrice // Multiply by quantity for LPG
            } else {
                cylinder.totalPrice // For non-LPG cylinders, use the existing totalPrice
            }
        }
        val inventoryTotal = issuedInventory.sumOf { it.price * it.quantity }
        cylinderTotal + inventoryTotal
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
                    onClick = { component.onEvent(NewIssueCylinderScreenEvent.OnBackClick) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Issue New Cylinder",
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
                visible = !showAddCylinderDialog && !showAddInventoryDialog, // Check both dialogs
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
                        // "Add Cylinder" button
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
                            ),
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded borders
                        ) {
                            Text(
                                text = "Add Cylinder",
                                fontSize = 12.sp, // Reduced font size
                                color = Color.White // White text
                            )
                        }

                        // "Add Inventory" button
                        Button(
                            onClick = {
                                showAddInventoryDialog = true
                                showBottomButtons = false // Hide bottom buttons
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                                .height(32.dp), // Reduced button height
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2f80eb) // Use the same color as the top bar
                            ),
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded borders
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

                    // Combined LazyColumn for cylinders and inventory
                    CombinedList(
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
                            alreadySelectedCylinders = alreadySelectedCylinders.filterNot { serialNumber ->
                                deletedCylinders.any { it.serialNumber == serialNumber }
                            }

                            // If the deleted cylinders are LPG, update the alreadySelectedLPGQuantities
                            if (cylinder.gasType == "LPG") {
                                val formattedVolumeType = cylinder.volumeType.replace(".", ",")
                                val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                                alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                                    formattedVolumeType to (alreadySelectedQuantity - cylinder.quantity)
                                )
                            }
                        },
                        onDeleteInventory = { inventoryItem ->
                            // Filter out the deleted inventory items from the issuedInventory list
                            issuedInventory = issuedInventory.filterNot { it.name == inventoryItem.name }

                            // Remove the quantity of the deleted inventory item from alreadySelectedInventoryQuantities
                            val alreadySelectedQuantity = alreadySelectedInventoryQuantities[inventoryItem.name] ?: 0
                            alreadySelectedInventoryQuantities = alreadySelectedInventoryQuantities - inventoryItem.name
                        },
                        alreadySelectedLPGQuantities = alreadySelectedLPGQuantities,
                        onUpdateAlreadySelectedLPGQuantities = { updatedMap ->
                            alreadySelectedLPGQuantities = updatedMap
                        }
                    )
                }
            }
        }

        // Overlay the Add Cylinder dialog box on top of everything (but under the top bar)
        if (showAddCylinderDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // Ensure the dialog is above the main content but under the top bar
            ) {
                AddCylinderDialog2(
                    onDismiss = {
                        isBackButtonEnabled.value = true // Enable the back button
                        showAddCylinderDialog = false
                        showBottomButtons = true // Show bottom buttons
                    },
                    onAddCylinder = { issuedCylinder ->
                        issuedCylinders = issuedCylinders + issuedCylinder
                        // Update alreadySelectedLPGQuantities if the gas type is LPG
                        if (issuedCylinder.gasType == "LPG") {
                            val formattedVolumeType = issuedCylinder.volumeType.replace(".", ",")
                            val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                            alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                                formattedVolumeType to (alreadySelectedQuantity + issuedCylinder.quantity)
                            )
                        }
                    },
                    db = db,
                    alreadySelectedCylinders = alreadySelectedCylinders,
                    onUpdateAlreadySelectedCylinders = { updatedList ->
                        alreadySelectedCylinders = updatedList
                    },
                    isBackButtonEnabled = isBackButtonEnabled
                    ,
                    alreadySelectedLPGQuantities = alreadySelectedLPGQuantities, // Pass the state
                    totalPrice = totalPrice // Pass the totalPrice
                )
            }
        }

        // Overlay the Add Inventory dialog box on top of everything (but under the top bar)
        if (showAddInventoryDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // Ensure the dialog is above the main content but under the top bar
            ) {
                AddInventoryDialog(
                    onDismiss = {
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
                        val errorMessage = validateCheckoutConditions(
                            issuedCylinders,
                            issuedInventory,
                            selectedIssueDate,
                            selectedReturnDate
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

        // Update the checkout dialog call in the NewIssueCylinderScreenUI function
// Find the following section in the code and replace it:

        // Update the CheckoutDialog's onConfirm callback and add a new state variable for transaction confirmation
// Add these state variables below the existing ones in NewIssueCylinderScreenUI:
        var showTransactionConfirmation by remember { mutableStateOf(false) }
        var isReversingTransaction by remember { mutableStateOf(false) }

// Replace the if (showCheckoutDialog) block with this updated version:
        if (showCheckoutDialog) {
            CheckoutDialog(
                totalPrice = totalPrice,
                selectedIssueDate = selectedIssueDate,
                selectedReturnDate = selectedReturnDate,
                onDismiss = { showCheckoutDialog = false },
                onConfirm = { cash, credit, delivery -> // Updated to include delivery
                    // Call the checkoutCylinders function
                    coroutineScope.launch {
                        val issueDateString = selectedIssueDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt()).toString()
                        } ?: ""
                        val returnDateString = selectedReturnDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt()).toString()
                        } ?: ""

                        // Push transaction details to Firestore
                        val success = checkoutCylinders(
                            db = db,
                            customerName = customerName,
                            issuedCylinders = issuedCylinders,
                            issuedInventory = issuedInventory, // Pass issuedInventory
                            issueDate = issueDateString,
                            cash = cash,
                            credit = credit,
                            delivery = delivery, // Added delivery parameter
                            returnDate = returnDateString,
                            onCurrentDateTime = {currentDateTime = it},
                            totalPrice = totalPrice
                        )

                        if (success) {
                            println("Transaction successfully pushed to Firestore!")
                            showCheckoutDialog = false
                            showTransactionConfirmation = true
                        } else {
                            println("Failed to push transaction to Firestore.")
                        }
                    }
                }
            )
        }

// Add the TransactionConfirmationDialog right after the CheckoutDialog
        if (showTransactionConfirmation) {
            TransactionConfirmationDialog(
                onViewBill = {
                    // Navigate to generate bill screen
                    showTransactionConfirmation = false
                    component.onEvent(NewIssueCylinderScreenEvent.OnBillClick(customerName, currentDateTime))
                },
                onReverseTransaction = {
                    // Reverse the transaction
                    isReversingTransaction = true
                    coroutineScope.launch {
                        val reversalSuccess = reverseTransaction(
                            db = db,
                            customerName = customerName,
                            dateTimeString = currentDateTime,
                            issuedCylinders = issuedCylinders,
                            issuedInventory = issuedInventory,
                            credit = issuedInventory.sumOf { it.price * it.quantity }.toString()
                        )

                        isReversingTransaction = false
                        showTransactionConfirmation = false

                        if (reversalSuccess) {
                            // Reset the cart after successful reversal
                            issuedCylinders = emptyList()
                            issuedInventory = emptyList()
                            alreadySelectedCylinders = emptyList()
                            alreadySelectedLPGQuantities = emptyMap()
                            alreadySelectedInventory = emptyList()
                            alreadySelectedInventoryQuantities = emptyMap()

                            // Show success message
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Transaction successfully reversed!")
                            }
                        } else {
                            // Show error message
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to reverse transaction. Please try again.")
                            }
                        }
                    }
                },
                onDismiss = {
                    showTransactionConfirmation = false
                }
            )
        }

// Show a loading dialog when reversing the transaction
        if (isReversingTransaction) {
            AlertDialog(
                onDismissRequest = { /* Do nothing - user cannot dismiss while processing */ },
                title = { Text("Reversing Transaction") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2f80eb),
                            modifier = Modifier.padding(16.dp)
                        )
                        Text("Please wait while we reverse the transaction...")
                    }
                },
                confirmButton = { /* No buttons while processing */ }
            )
        }

        // Validation dialog
        if (showValidationDialog) {
            ValidationDialog(
                message = validationMessage,
                onDismiss = { showValidationDialog = false } // Close the dialog
            )
        }
    }
}

suspend fun reverseTransaction(
    db: FirebaseFirestore,
    customerName: String,
    dateTimeString: String,
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    credit: String
): Boolean {
    try {
        // Pre-process data collections for better efficiency
        val nonLpgCylinders = issuedCylinders.filter { it.gasType != "LPG" }
        val lpgCylinders = issuedCylinders.filter { it.gasType == "LPG" }
        val nonLpgCylinderSerialNumbers = nonLpgCylinders.map { it.serialNumber }

        // Process operations in parallel
        coroutineScope {
            // Task 1: Delete the transaction document
            val deleteTransactionTask = async {
                val transactionsRef = db.collection("Transactions")
                    .document(customerName)
                    .collection("DateAndTime")
                    .document(dateTimeString)

                // Delete all documents in the "Transaction Details" subcollection
                val transactionDetailsRef = transactionsRef.collection("Transaction Details")
                val documents = transactionDetailsRef.get().documents

                // Use batch to delete all documents in Transaction Details
                val batch = db.batch()
                documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()

                // Delete the main transaction document
                transactionsRef.delete()
            }

            // Task 2: Remove non-LPG cylinders from Customers > Issued Cylinders > Names > CustomerName > Details
            val updateIssuedCylindersTask = async {
                if (nonLpgCylinders.isNotEmpty()) {
                    val issuedCylindersRef = db.collection("Customers")
                        .document("Issued Cylinders")
                        .collection("Names")
                        .document(customerName)

                    val snapshot = issuedCylindersRef.get()
                    if (snapshot.exists) {
                        val existingDetails = snapshot.get("Details") as? List<String> ?: emptyList()

                        // Remove the serial numbers from the list
                        val updatedDetails = existingDetails.filter { it !in nonLpgCylinderSerialNumbers }

                        // Update the document with the filtered list
                        issuedCylindersRef.set(mapOf("Details" to updatedDetails))
                    }
                }
            }

            // Task 3: Reset LPG quantities in Customers > LPG Issued > Names > CustomerName
            val updateLpgQuantitiesTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    val lpgIssuedRef = db.collection("Customers")
                        .document("LPG Issued")
                        .collection("Names")
                        .document(customerName)

                    val snapshot = lpgIssuedRef.get()
                    if (snapshot.exists) {
                        val existingLpgQuantities = snapshot.get("Quantities") as? Map<String, String> ?: emptyMap()

                        // Process all volume types in one go
                        val volumeTypeQuantities = lpgCylinders.groupBy { it.volumeType.replace(".", ",") }
                        val updatedLpgQuantities = existingLpgQuantities.toMutableMap()

                        volumeTypeQuantities.forEach { (volumeType, cylinders) ->
                            val existingQuantity = existingLpgQuantities[volumeType]?.toIntOrNull() ?: 0
                            val newQuantity = existingQuantity - cylinders.sumOf { it.quantity }
                            // Ensure we don't set negative quantities
                            updatedLpgQuantities[volumeType] = (if (newQuantity < 0) 0 else newQuantity).toString()
                        }

                        lpgIssuedRef.set(mapOf("Quantities" to updatedLpgQuantities))
                    }
                }
            }

            // Task 4: Reset cylinder status in Cylinders > Cylinders > CylinderDetails
            val updateCylinderDetailsTask = async {
                if (nonLpgCylinderSerialNumbers.isNotEmpty()) {
                    val cylindersRef = db.collection("Cylinders").document("Cylinders")
                    val snapshot = cylindersRef.get()
                    if (snapshot.exists) {
                        val cylinderDetails = snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

                        // Reset the status for the issued cylinders
                        val updatedCylinderDetails = cylinderDetails.map { cylinder ->
                            if (cylinder["Serial Number"] in nonLpgCylinderSerialNumbers) {
                                cylinder.toMutableMap().apply {
                                    this["Status"] = "Full"  // Reset to Full status
                                    this.remove("Issue Date")
                                    this.remove("Issued At Price")
                                    // Leave Notifications Date untouched
                                }
                            } else {
                                cylinder
                            }
                        }

                        cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
                    }
                }
            }

            // Task 5: Restore inventory quantities
            val updateInventoryTask = async {
                if (issuedInventory.isNotEmpty()) {
                    val inventoryRef = db.collection("Inventory").document("Items")
                    val inventorySnapshot = inventoryRef.get()

                    if (inventorySnapshot.exists) {
                        val existingItems = inventorySnapshot.get("items") as? List<Map<String, String>> ?: emptyList()

                        // Create a map for faster lookups of inventory quantities
                        val inventoryQuantityMap = issuedInventory.associate { it.name to it.quantity }

                        val updatedItems = existingItems.map { item ->
                            val issuedQuantity = inventoryQuantityMap[item["Name"]]
                            if (issuedQuantity != null) {
                                val currentQuantity = item["Quantity"]?.toIntOrNull() ?: 0
                                val newQuantity = currentQuantity + issuedQuantity
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
            }

            // Task 6: Restore LPGFull quantities
            val updateLpgFullTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    val lpgDocumentRef = db.collection("Cylinders").document("LPG")
                    val lpgDocumentSnapshot = lpgDocumentRef.get()

                    if (lpgDocumentSnapshot.exists) {
                        val lpgFullMap = lpgDocumentSnapshot.get("LPGFull") as? Map<String, Int> ?: emptyMap()

                        // Calculate changes to quantities by volume type in one go
                        val volumeChanges = lpgCylinders
                            .groupBy { it.volumeType.replace(".", ",") }
                            .mapValues { (_, cylinders) -> cylinders.sumOf { it.quantity } }

                        val updatedLpgFullMap = lpgFullMap.toMutableMap()
                        volumeChanges.forEach { (volumeType, quantity) ->
                            val currentQuantity = updatedLpgFullMap[volumeType] ?: 0
                            updatedLpgFullMap[volumeType] = currentQuantity + quantity
                        }

                        lpgDocumentRef.update("LPGFull" to updatedLpgFullMap)
                    }
                }
            }

            // Task 7: Reset customer credit
            val updateCustomerCreditTask = async {
                val creditValue = credit.toDoubleOrNull() ?: 0.0
                if (creditValue > 0) {
                    val customerDetailsRef = db.collection("Customers")
                        .document("Details")
                        .collection("Names")
                        .document(customerName)

                    val customerDetailsSnapshot = customerDetailsRef.get()
                    if (customerDetailsSnapshot.exists) {
                        val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

                        val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
                        val newCredit = currentCredit - creditValue

                        val updatedDetailsMap = detailsMap.toMutableMap().apply {
                            this["Credit"] = (if (newCredit < 0) 0.0 else newCredit).toString()
                        }

                        customerDetailsRef.update("Details" to updatedDetailsMap)
                    }
                }
            }

            // Task 8: Delete cylinder customer references
            val deleteCylinderCustomerRefsTask = async {
                // Process in batches for better performance
                nonLpgCylinders.forEach { cylinder ->
                    val cylinderRef = db.collection("Cylinders")
                        .document("Customers")
                        .collection(cylinder.serialNumber)

                    // Delete "Currently Issued To" document
                    cylinderRef.document("Currently Issued To").delete()

                    // Update Previous Customers document (remove the last entry)
                    val previousCustomersRef = cylinderRef.document("Previous Customers")
                    val snapshot = previousCustomersRef.get()
                    if (snapshot.exists) {
                        val existingCustomers = snapshot.get("customers") as? List<Map<String, String>> ?: emptyList()

                        // Remove the last entry if it matches our customer
                        val updatedCustomers = if (existingCustomers.isNotEmpty() &&
                            existingCustomers.last()["name"] == customerName) {
                            existingCustomers.dropLast(1)
                        } else {
                            existingCustomers
                        }

                        previousCustomersRef.set(mapOf("customers" to updatedCustomers))
                    }
                }
            }

            // Wait for all tasks to complete
            awaitAll(
                deleteTransactionTask,
                updateIssuedCylindersTask,
                updateLpgQuantitiesTask,
                updateCylinderDetailsTask,
                updateInventoryTask,
                updateLpgFullTask,
                updateCustomerCreditTask,
                deleteCylinderCustomerRefsTask
            )
        }

        return true
    } catch (e: Exception) {
        println("Error in reverseTransaction: ${e.message}")
        return false
    }
}

@Composable
fun TransactionConfirmationDialog(
    onViewBill: () -> Unit,
    onReverseTransaction: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Transaction Complete",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                "Your transaction has been successfully processed. What would you like to do next?",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onViewBill,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text("View Bill", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onReverseTransaction,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336)) // Red color
            ) {
                Text("Reverse Transaction", color = Color.White)
            }
        }
    )
}
@Composable
fun ValidationDialog(
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

fun validateCheckoutConditions(
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    selectedIssueDate: Long?,
    selectedReturnDate: Long?
): String? {
    return when {
        issuedCylinders.isEmpty() && issuedInventory.isEmpty() -> "Nothing added to cart."
        selectedIssueDate == null -> "No issue date selected."
        selectedReturnDate == null -> "No return date selected."
        else -> null // No error
    }
}

@Composable
fun CheckoutDialog(
    totalPrice: Double,
    selectedIssueDate: Long?,
    selectedReturnDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (cash: String, credit: String, delivery: String) -> Unit
) {
    var deliveryStr by remember { mutableStateOf("0") } // Default to 0
    var cash by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Calculate total including delivery
    val delivery = deliveryStr.toDoubleOrNull() ?: 0.0
    val total = totalPrice + delivery

    // Initialize cash to total amount and update credit accordingly
    LaunchedEffect(total, deliveryStr) {
        cash = total.toString()
        credit = "0"
    }

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
                    // Display base price
                    Text(
                        text = "Base Price: $${totalPrice}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
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

                    // Delivery input - Moved to the top of the payment fields
                    OutlinedTextField(
                        value = deliveryStr,
                        onValueChange = {
                            deliveryStr = it
                            // Recalculate total when delivery changes
                            val newDelivery = it.toDoubleOrNull() ?: 0.0
                            val newTotal = totalPrice + newDelivery
                            cash = newTotal.toString()
                            credit = "0"
                        },
                        label = { Text("Delivery Charge") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Display total price after delivery
                    Text(
                        text = "Total Price: Rs.${(total * 100).toInt() / 100.0}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Cash input
                    OutlinedTextField(
                        value = cash,
                        onValueChange = { newCash ->
                            cash = newCash
                            // Auto-calculate credit based on cash input
                            val cashValue = newCash.toDoubleOrNull() ?: 0.0
                            val creditValue = total - cashValue
                            credit = if (creditValue > 0) ((creditValue * 100).toInt() / 100.0).toString() else "0"
                        },
                        label = { Text("Cash$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Credit input (calculated automatically)
                    OutlinedTextField(
                        value = credit,
                        onValueChange = { newCredit ->
                            credit = newCredit
                            // If user changes credit, adjust cash accordingly
                            val creditValue = newCredit.toDoubleOrNull() ?: 0.0
                            val cashValue = total - creditValue
                            cash = if (cashValue > 0) ((cashValue * 100).toInt() / 100.0).toString() else "0"
                        },
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
                    onConfirm(cash, credit, deliveryStr)
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
fun CombinedList(
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    onDeleteCylinder: (IssuedCylinder) -> Unit,
    onDeleteInventory: (InventoryItem) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    onUpdateAlreadySelectedLPGQuantities: (Map<String, Int>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 100.dp) // Add padding to avoid overlapping with bottom buttons
    ) {
        // Add cylinders to the list
        issuedCylinders
            .groupBy { Pair(it.gasType, it.volumeType) } // Group cylinders by gasType and volumeType
            .forEach { (_, groupCylinders) ->
                // Calculate total quantity and total price for the group
                val totalQuantity = groupCylinders.sumOf { it.quantity }
                val totalPrice = if (groupCylinders.first().gasType == "LPG") {
                    // For LPG cylinders, calculate total price as quantity * price per unit
                    groupCylinders.sumOf { it.quantity * it.totalPrice }
                } else {
                    // For non-LPG cylinders, use the existing totalPrice logic
                    groupCylinders.sumOf { it.totalPrice }
                }

                // Create a grouped cylinder for display
                val groupedCylinder = IssuedCylinder(
                    serialNumber = "", // Not needed for grouped card
                    gasType = groupCylinders.first().gasType,
                    volumeType = groupCylinders.first().volumeType,
                    quantity = totalQuantity,
                    totalPrice = totalPrice
                )

                item {
                    IssuedCylinderCard3(
                        cylinder = groupedCylinder,
                        onDelete = {
                            // Delete all cylinders in this group
                            groupCylinders.forEach { onDeleteCylinder(it) }

                            // If the group is LPG, update the alreadySelectedLPGQuantities
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
                    IssuedInventoryCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}


@Composable
fun AddCylinderDialog2(
    onDismiss: () -> Unit,
    onAddCylinder: (IssuedCylinder) -> Unit,
    db: FirebaseFirestore,
    alreadySelectedCylinders: List<String>,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    isBackButtonEnabled: MutableState<Boolean>,
    totalPrice: Double
) {
    isBackButtonEnabled.value = false
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
    var showValidationMessage by remember { mutableStateOf(false) }
    var availableLPGQuantity by remember { mutableStateOf<Int?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch gas types
    LaunchedEffect(Unit) {
        val gases = db.collection("Gases").get().documents
        cylinderOptions = gases.map { it.id }
    }

    // Fetch volume types and set default price when gasType changes
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
        availableLPGQuantity = null
    }

    // Set default price when both gasType and volumeType are selected
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
            coroutineScope.launch {
                val lpgDocument = db.collection("Cylinders").document("LPG").get()
                val lpgFullMap = lpgDocument.get("LPGFull") as? Map<String, Int>
                lpgFullMap?.let {
                    val formattedVolumeType = volumeType!!.replace(".", ",")
                    val totalQuantity = it[formattedVolumeType] ?: 0
                    val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                    availableLPGQuantity = totalQuantity - alreadySelectedQuantity

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

    // Rest of your existing LaunchedEffect blocks...
    LaunchedEffect(quantity.toIntOrNull()) {
        if (gasType != "LPG") {
            val quantityInt = quantity.toIntOrNull() ?: 0
            selectedCylinders = List(quantityInt) { "" }
        }
    }

    LaunchedEffect(showValidationMessage) {
        if (showValidationMessage) {
            delay(3000)
            showValidationMessage = false
        }
    }

    LaunchedEffect(quantityError) {
        if (quantityError != null) {
            delay(3000)
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
                                            if (quantityInt <= 0 || quantityInt > availableLPGQuantity!!) {
                                                quantityError = "Quantity must be between 1 and $availableLPGQuantity"
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
fun SearchableDropdown3(
    options: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    onClearSelection: () -> Unit = {}, // Added callback for clearing selection
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedItem ?: "") }
    val filteredOptions = options.filter { it.contains(searchQuery, ignoreCase = true) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = searchQuery.replace(",", "."), // Replace commas with dots for display,
                onValueChange = {
                    searchQuery = it
                    expanded = it.isNotEmpty()
                    if (it.isEmpty()) {
                        onClearSelection() // Notify parent when input is cleared
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
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
                                    text = option.replace(",", "."),
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

//suspend fun checkoutCylinders(
//    db: FirebaseFirestore,
//    customerName: String?,
//    issuedCylinders: List<IssuedCylinder>,
//    issuedInventory: List<InventoryItem>,
//    issueDate: String,
//    cash: String,
//    credit: String,
//    delivery: String, // Added delivery parameter
//    returnDate: String,
//    onCurrentDateTime: (String) -> Unit,
//    totalPrice: Double
//): Boolean {
//    if (customerName == null || issueDate.isEmpty()) return false
//
//    try {
//        // Get the current date and time in the format "yyyy-MM-dd_HH:mm:ss"
//        // Get current time
//        val currentTime = Clock.System.now()
//            .toLocalDateTime(TimeZone.currentSystemDefault())
//            .time
//            .toString()
//
//        // Combine issueDate with current time
//        val dateTimeString = "${issueDate}_${currentTime}"
//            .replace(":", "-")
//            .substringBefore(".")
//
//        onCurrentDateTime(dateTimeString)
//
//        // Reference to the Transactions collection
//        val transactionsRef = db.collection("Transactions")
//            .document(customerName)
//            .collection("DateAndTime")
//            .document(dateTimeString)
//
//        transactionsRef.set(mapOf("Date" to dateTimeString))
//
//        // Create the "Transaction Details" collection
//        val transactionDetailsRef = transactionsRef.collection("Transaction Details")
//
//        transactionDetailsRef.document("Total Price").set(mapOf("Amount" to totalPrice.toString()))
//
//        // Push Cash document
//        transactionDetailsRef.document("Cash").set(mapOf("Amount" to cash))
//
//        // Push Cash Out document
//        transactionDetailsRef.document("Cash Out").set(mapOf("Amount" to "0"))
//
//        // Push Credit document
//        transactionDetailsRef.document("Credit").set(mapOf("Amount" to credit))
//
//        // Push Delivery document - Added new document
//        transactionDetailsRef.document("Delivery").set(mapOf("Amount" to delivery))
//
//        // Push Cylinders Issued document
//        val cylindersIssued = issuedCylinders.filter { it.gasType != "LPG" }.map { cylinder ->
//            mapOf(
//                "Serial Number" to cylinder.serialNumber,
//                "Price" to cylinder.totalPrice,
//                "Issue Date" to issueDate
//            )
//        }
//        transactionDetailsRef.document("Cylinders Issued").set(mapOf("CylindersIssued" to cylindersIssued))
//
//        // Push Cylinders Returned document (empty for now)
//        transactionDetailsRef.document("Cylinders Returned").set(mapOf("CylindersReturned" to emptyList<String>()))
//
//        // Push LPG Issued document
//        val lpgIssued = issuedCylinders.filter { it.gasType == "LPG" }.groupBy { it.volumeType }.map { (volumeType, cylinders) ->
//            mapOf(
//                "Volume Type" to volumeType,
//                "Price" to cylinders.sumOf { it.totalPrice },
//                "Quantity" to cylinders.sumOf { it.quantity },
//                "Date" to issueDate
//            )
//        }
//        transactionDetailsRef.document("LPG Issued").set(mapOf("LPGIssued" to lpgIssued))
//
//        // Push Inventory Issued document
//        val inventoryIssued = issuedInventory.map { inventoryItem ->
//            mapOf(
//                "Name" to inventoryItem.name.toString(),
//                "Quantity" to inventoryItem.quantity.toString(),
//                "Price" to inventoryItem.price.toString()
//            )
//        }
//        transactionDetailsRef.document("Inventory Issued").set(mapOf("InventoryIssued" to inventoryIssued))
//
//        // Update non-LPG cylinders in Customers > Issued Cylinders > Names > CustomerName > Details
//        val nonLpgCylinders = issuedCylinders.filter { it.gasType != "LPG" }
//        if (nonLpgCylinders.isNotEmpty()) {
//            val issuedCylindersRef = db.collection("Customers")
//                .document("Issued Cylinders")
//                .collection("Names")
//                .document(customerName)
//
//            // Fetch existing Details array
//            val snapshot = issuedCylindersRef.get()
//            val existingDetails = if (snapshot.exists) {
//                snapshot.get("Details") as? List<String> ?: emptyList()
//            } else {
//                emptyList()
//            }
//
//            // Add new serial numbers to the existing array
//            val newSerialNumbers = nonLpgCylinders.map { it.serialNumber }
//            val updatedDetails = existingDetails + newSerialNumbers
//
//            // Update the document with the new array
//            issuedCylindersRef.set(mapOf("Details" to updatedDetails))
//        }
//
//        // Update LPG quantities in Customers > LPG Issued > Names > CustomerName
//        val lpgCylinders = issuedCylinders.filter { it.gasType == "LPG" }
//        if (lpgCylinders.isNotEmpty()) {
//            val lpgIssuedRef = db.collection("Customers")
//                .document("LPG Issued")
//                .collection("Names")
//                .document(customerName)
//
//            // Fetch existing LPG quantities map
//            val snapshot = lpgIssuedRef.get()
//            val existingLpgQuantities = if (snapshot.exists) {
//                snapshot.get("Quantities") as? Map<String, String> ?: emptyMap()
//            } else {
//                emptyMap()
//            }
//
//            // Update quantities for each volume type
//            val updatedLpgQuantities = lpgCylinders.groupBy { it.volumeType.replace(".",",") }.mapValues { (volumeType, cylinders) ->
//                val existingQuantity = existingLpgQuantities[volumeType]?.toIntOrNull() ?: 0
//                val newQuantity = existingQuantity + cylinders.sumOf { it.quantity }
//                newQuantity.toString() // Store as string
//            }
//
//            // Merge the existing quantities with the updated quantities
//            val mergedLpgQuantities = existingLpgQuantities.toMutableMap().apply {
//                updatedLpgQuantities.forEach { (volumeType, quantity) ->
//                    this[volumeType.replace(".",",")] = quantity
//                }
//            }
//
//            // Update the document with the merged map
//            lpgIssuedRef.set(mapOf("Quantities" to mergedLpgQuantities))
//        }
//
//        // Update CylinderDetails in Cylinders > Cylinders > CylinderDetails
//        val nonLpgCylinderSerialNumbers = nonLpgCylinders.map { it.serialNumber }
//        if (nonLpgCylinderSerialNumbers.isNotEmpty()) {
//            val cylindersRef = db.collection("Cylinders").document("Cylinders")
//
//            // Fetch existing CylinderDetails array
//            val snapshot = cylindersRef.get()
//            val cylinderDetails = if (snapshot.exists) {
//                snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
//            } else {
//                emptyList()
//            }
//
//            // Update the status and Notifications Date for issued cylinders
//            val updatedCylinderDetails = cylinderDetails.map { cylinder ->
//                if (cylinder["Serial Number"] in nonLpgCylinderSerialNumbers) {
//                    cylinder.toMutableMap().apply {
//                        this["Status"] = "Issued"
//                        this["Notifications Date"] = returnDate // Use issueDate as the Notifications Date
//                        this["Issue Date"] = issueDate
//                        this["Issued At Price"] = nonLpgCylinders.find { it.serialNumber == this["Serial Number"] }?.totalPrice.toString()
//                    }
//                } else {
//                    cylinder
//                }
//            }
//
//            // Save the updated CylinderDetails array back to Firestore
//            cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
//        }
//
//        // Create collections for each non-LPG cylinder in Cylinders > Customers
//        for (cylinder in nonLpgCylinders) {
//            val cylinderRef = db.collection("Cylinders")
//                .document("Customers")
//                .collection(cylinder.serialNumber)
//
//            // Create Currently Issued To document
//            cylinderRef.document("Currently Issued To").set(
//                mapOf(
//                    "name" to customerName,
//                    "date" to issueDate,
//                    "price" to cylinder.totalPrice.toString()
//                )
//            )
//
//            // Create or update Previous Customers document
//            val previousCustomersRef = cylinderRef.document("Previous Customers")
//            val snapshot = previousCustomersRef.get()
//            val existingCustomers = if (snapshot.exists) {
//                snapshot.get("customers") as? List<Map<String, String>> ?: emptyList()
//            } else {
//                emptyList()
//            }
//
//            // Add the current customer details to the existing array
//            val newCustomerDetails = mapOf(
//                "name" to customerName,
//                "date" to issueDate,
//                "price" to cylinder.totalPrice.toString()
//            )
//            val updatedCustomers = existingCustomers + newCustomerDetails
//
//            // Update the Previous Customers document
//            previousCustomersRef.set(mapOf("customers" to updatedCustomers))
//        }
//
//        // Update LPGFull map in Cylinders > LPG document
//        val lpgDocumentRef = db.collection("Cylinders").document("LPG")
//        val lpgDocumentSnapshot = lpgDocumentRef.get()
//        if (lpgDocumentSnapshot.exists) {
//            val lpgFullMap = lpgDocumentSnapshot.get("LPGFull") as? Map<String, Int> ?: emptyMap()
//            val updatedLpgFullMap = lpgCylinders.groupBy { it.volumeType.replace(".",",") }.entries.fold(lpgFullMap.toMutableMap()) { acc, entry ->
//                val (volumeType, cylinders) = entry
//                val issuedQuantity = cylinders.sumOf { it.quantity }
//                val currentQuantity = acc[volumeType] ?: 0
//                acc[volumeType] = currentQuantity - issuedQuantity
//                acc
//            }
//            lpgDocumentRef.update("LPGFull" to updatedLpgFullMap)
//        }
//
//        if (issuedInventory.isNotEmpty()) {
//            val inventoryRef = db.collection("Inventory").document("Items")
//            val inventorySnapshot = inventoryRef.get()
//
//            if (inventorySnapshot.exists) {
//                val existingItems = inventorySnapshot.get("items") as? List<Map<String, String>> ?: emptyList()
//
//                val updatedItems = existingItems.map { item ->
//                    val issuedItem = issuedInventory.find { it.name == item["Name"] }
//                    if (issuedItem != null) {
//                        val currentQuantity = item["Quantity"]?.toIntOrNull() ?: 0
//                        val newQuantity = currentQuantity - issuedItem.quantity
//                        item.toMutableMap().apply {
//                            this["Quantity"] = newQuantity.toString()
//                        }
//                    } else {
//                        item
//                    }
//                }
//
//                inventoryRef.set(mapOf("items" to updatedItems))
//            }
//        }
//
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
//            val newCredit = currentCredit + (credit.toDoubleOrNull() ?: 0.0)
//
//            // Create the updated map
//            val updatedDetailsMap = detailsMap.toMutableMap().apply {
//                this["Credit"] = newCredit.toString()
//            }
//
//            // Save the updated map back to Firestore
//            customerDetailsRef.update("Details" to updatedDetailsMap)
//
//            println("Successfully updated 'Credit' field for customer $customerName")
//
//        } else {
//            throw Exception("Document 'Customers > Details > Names > $customerName' does not exist")
//        }
//
//        return true
//    } catch (e: Exception) {
//        println("Error in checkoutCylinders: ${e.message}")
//        return false
//    }
//}




suspend fun checkoutCylinders(
    db: FirebaseFirestore,
    customerName: String?,
    issuedCylinders: List<IssuedCylinder>,
    issuedInventory: List<InventoryItem>,
    issueDate: String,
    cash: String,
    credit: String,
    delivery: String,
    returnDate: String,
    onCurrentDateTime: (String) -> Unit,
    totalPrice: Double
): Boolean {
    if (customerName == null || issueDate.isEmpty()) return false

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

        onCurrentDateTime(dateTimeString)

        // Reference to the Transactions collection
        val transactionsRef = db.collection("Transactions")
            .document(customerName)
            .collection("DateAndTime")
            .document(dateTimeString)

        // Pre-process data collections for better efficiency
        val nonLpgCylinders = issuedCylinders.filter { it.gasType != "LPG" }
        val lpgCylinders = issuedCylinders.filter { it.gasType == "LPG" }
        val nonLpgCylinderSerialNumbers = nonLpgCylinders.map { it.serialNumber }

        // Prepare all transaction detail documents in advance
        val transactionBatch = db.batch()

        // Main transaction document
        transactionBatch.set(transactionsRef, mapOf("Date" to dateTimeString))

        // Transaction details collection documents
        val transactionDetailsRef = transactionsRef.collection("Transaction Details")

        transactionBatch.set(transactionDetailsRef.document("Total Price"), mapOf("Amount" to totalPrice.toString()))
        transactionBatch.set(transactionDetailsRef.document("Cash"), mapOf("Amount" to cash))
        transactionBatch.set(transactionDetailsRef.document("Cash Out"), mapOf("Amount" to "0"))
        transactionBatch.set(transactionDetailsRef.document("Credit"), mapOf("Amount" to credit))
        transactionBatch.set(transactionDetailsRef.document("Delivery"), mapOf("Amount" to delivery))

        // Prepare cylinders issued data
        val cylindersIssued = nonLpgCylinders.map { cylinder ->
            mapOf(
                "Serial Number" to cylinder.serialNumber,
                "Price" to cylinder.totalPrice,
                "Issue Date" to issueDate
            )
        }
        transactionBatch.set(
            transactionDetailsRef.document("Cylinders Issued"),
            mapOf("CylindersIssued" to cylindersIssued)
        )

        // Empty cylinders returned for now
        transactionBatch.set(
            transactionDetailsRef.document("Cylinders Returned"),
            mapOf("CylindersReturned" to emptyList<String>())
        )

        // Prepare LPG issued data
        val lpgIssued = lpgCylinders
            .groupBy { it.volumeType }
            .map { (volumeType, cylinders) ->
                mapOf(
                    "Volume Type" to volumeType,
                    "Price" to cylinders.sumOf { it.totalPrice },
                    "Quantity" to cylinders.sumOf { it.quantity },
                    "Date" to issueDate
                )
            }
        transactionBatch.set(
            transactionDetailsRef.document("LPG Issued"),
            mapOf("LPGIssued" to lpgIssued)
        )

        // Prepare inventory issued data
        val inventoryIssued = issuedInventory.map { inventoryItem ->
            mapOf(
                "Name" to inventoryItem.name.toString(),
                "Quantity" to inventoryItem.quantity.toString(),
                "Price" to inventoryItem.price.toString()
            )
        }
        transactionBatch.set(
            transactionDetailsRef.document("Inventory Issued"),
            mapOf("InventoryIssued" to inventoryIssued)
        )

        // Execute the transaction batch
        transactionBatch.commit()

        // Now handle the more complex operations that can't all be batched together

        // Start parallel async tasks for operations that don't depend on each other
       coroutineScope {
            // Task 1: Update non-LPG cylinders in Customers > Issued Cylinders > Names > CustomerName > Details
            val updateIssuedCylindersTask = async {
                if (nonLpgCylinders.isNotEmpty()) {
                    val issuedCylindersRef = db.collection("Customers")
                        .document("Issued Cylinders")
                        .collection("Names")
                        .document(customerName)

                    val snapshot = issuedCylindersRef.get()
                    val existingDetails = if (snapshot.exists) {
                        snapshot.get("Details") as? List<String> ?: emptyList()
                    } else {
                        emptyList()
                    }

                    val updatedDetails = existingDetails + nonLpgCylinderSerialNumbers
                    issuedCylindersRef.set(mapOf("Details" to updatedDetails))
                }
            }

            // Task 2: Update LPG quantities in Customers > LPG Issued > Names > CustomerName
            val updateLpgQuantitiesTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    val lpgIssuedRef = db.collection("Customers")
                        .document("LPG Issued")
                        .collection("Names")
                        .document(customerName)

                    val snapshot = lpgIssuedRef.get()
                    val existingLpgQuantities = if (snapshot.exists) {
                        snapshot.get("Quantities") as? Map<String, String> ?: emptyMap()
                    } else {
                        emptyMap()
                    }

                    // Process all volume types in one go
                    val volumeTypeQuantities = lpgCylinders.groupBy { it.volumeType.replace(".", ",") }
                    val mergedLpgQuantities = existingLpgQuantities.toMutableMap()

                    volumeTypeQuantities.forEach { (volumeType, cylinders) ->
                        val existingQuantity = existingLpgQuantities[volumeType]?.toIntOrNull() ?: 0
                        val newQuantity = existingQuantity + cylinders.sumOf { it.quantity }
                        mergedLpgQuantities[volumeType] = newQuantity.toString()
                    }

                    lpgIssuedRef.set(mapOf("Quantities" to mergedLpgQuantities))
                }
            }

            // Task 3: Update CylinderDetails in Cylinders > Cylinders > CylinderDetails
            val updateCylinderDetailsTask = async {
                if (nonLpgCylinderSerialNumbers.isNotEmpty()) {
                    val cylindersRef = db.collection("Cylinders").document("Cylinders")
                    val snapshot = cylindersRef.get()
                    val cylinderDetails = if (snapshot.exists) {
                        snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
                    } else {
                        emptyList()
                    }

                    // Create a map for faster lookups of cylinder price by serial number
                    val cylinderPriceMap = nonLpgCylinders.associate { it.serialNumber to it.totalPrice.toString() }

                    // Update the status and Notifications Date for issued cylinders
                    val updatedCylinderDetails = cylinderDetails.map { cylinder ->
                        if (cylinder["Serial Number"] in nonLpgCylinderSerialNumbers) {
                            cylinder.toMutableMap().apply {
                                this["Status"] = "Issued"
                                this["Notifications Date"] = returnDate
                                this["Issue Date"] = issueDate
                                this["Issued At Price"] = cylinderPriceMap[this["Serial Number"]].toString()
                            }
                        } else {
                            cylinder
                        }
                    }

                    cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
                }
            }

            // Task 4: Update inventory quantities
            val updateInventoryTask = async {
                if (issuedInventory.isNotEmpty()) {
                    val inventoryRef = db.collection("Inventory").document("Items")
                    val inventorySnapshot = inventoryRef.get()

                    if (inventorySnapshot.exists) {
                        val existingItems = inventorySnapshot.get("items") as? List<Map<String, String>> ?: emptyList()

                        // Create a map for faster lookups of inventory quantities
                        val inventoryQuantityMap = issuedInventory.associate { it.name to it.quantity }

                        val updatedItems = existingItems.map { item ->
                            val issuedQuantity = inventoryQuantityMap[item["Name"]]
                            if (issuedQuantity != null) {
                                val currentQuantity = item["Quantity"]?.toIntOrNull() ?: 0
                                val newQuantity = currentQuantity - issuedQuantity
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
            }

            // Task 5: Update LPGFull map in Cylinders > LPG document
            val updateLpgFullTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    val lpgDocumentRef = db.collection("Cylinders").document("LPG")
                    val lpgDocumentSnapshot = lpgDocumentRef.get()

                    if (lpgDocumentSnapshot.exists) {
                        val lpgFullMap = lpgDocumentSnapshot.get("LPGFull") as? Map<String, Int> ?: emptyMap()

                        // Calculate changes to quantities by volume type in one go
                        val volumeChanges = lpgCylinders
                            .groupBy { it.volumeType.replace(".", ",") }
                            .mapValues { (_, cylinders) -> cylinders.sumOf { it.quantity } }

                        val updatedLpgFullMap = lpgFullMap.toMutableMap()
                        volumeChanges.forEach { (volumeType, quantity) ->
                            val currentQuantity = updatedLpgFullMap[volumeType] ?: 0
                            updatedLpgFullMap[volumeType] = currentQuantity - quantity
                        }

                        lpgDocumentRef.update("LPGFull" to updatedLpgFullMap)
                    }
                }
            }

            // Task 6: Update customer credit
            val updateCustomerCreditTask = async {
                val customerDetailsRef = db.collection("Customers")
                    .document("Details")
                    .collection("Names")
                    .document(customerName)

                val customerDetailsSnapshot = customerDetailsRef.get()
                if (customerDetailsSnapshot.exists) {
                    val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

                    val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
                    val newCredit = currentCredit + (credit.toDoubleOrNull() ?: 0.0)

                    val updatedDetailsMap = detailsMap.toMutableMap().apply {
                        this["Credit"] = newCredit.toString()
                    }

                    customerDetailsRef.update("Details" to updatedDetailsMap)
                }
            }

            // Wait for all tasks to complete
            kotlinx.coroutines.awaitAll(
                updateIssuedCylindersTask,
                updateLpgQuantitiesTask,
                updateCylinderDetailsTask,
                updateInventoryTask,
                updateLpgFullTask,
                updateCustomerCreditTask
            )
        }

        // Create collections for each non-LPG cylinder in Cylinders > Customers
        // This part can't be easily parallelized due to iterative nature and potential serial number conflicts
        if (nonLpgCylinders.isNotEmpty()) {
            // Process in batches of 10 for better performance
            nonLpgCylinders.chunked(10).forEach { cylinderBatch ->
                val cylinderBatchOperations = db.batch()

                cylinderBatch.forEach { cylinder ->
                    val cylinderRef = db.collection("Cylinders")
                        .document("Customers")
                        .collection(cylinder.serialNumber)

                    // Currently Issued To document
                    cylinderBatchOperations.set(
                        cylinderRef.document("Currently Issued To"),
                        mapOf(
                            "name" to customerName,
                            "date" to issueDate,
                            "price" to cylinder.totalPrice.toString()
                        )
                    )
                }

                cylinderBatchOperations.commit()
            }

            // Handle Previous Customers updates
            nonLpgCylinders.forEach { cylinder ->
                val cylinderRef = db.collection("Cylinders")
                    .document("Customers")
                    .collection(cylinder.serialNumber)

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
        }

        return true
    } catch (e: Exception) {
        println("Error in checkoutCylinders: ${e.message}")
        return false
    }
}

@Composable
fun IssuedCylinderCard3(
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


data class InventoryItem(
    val name: String,
    val quantity: Int,
    val price: Double
)


@Composable
fun AddInventoryDialog(
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


@Composable
fun IssuedInventoryCard(
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

// added later

// Firestore Function to Fetch Cylinders by Gas Type, Volume Type, and Status
suspend fun fetchCylindersByStatus(db: FirebaseFirestore, gasType: String, volumeType: String, status: String): List<String> {
    val cylinders = db.collection("Cylinders").document("Cylinders").get()
    val cylinderDetails = cylinders.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
    return cylinderDetails
        .filter { it["Gas Type"] == gasType && it["Volume Type"] == volumeType && it["Status"] == status }
        .map { it["Serial Number"] ?: "" } // Extract serial numbers
}

// Data Classes
data class IssuedCylinder(
    val serialNumber: String, // Added serialNumber property
    val gasType: String,
    val volumeType: String,
    val quantity: Int,
    val totalPrice: Double
)

// Firestore Functions
suspend fun fetchCustomers(db: FirebaseFirestore): List<String> {
    return try {
        // Navigate to the target document
        val customerDetailsRef = db.collection("Customers")
            .document("Names")
            .get()

        // Extract the CustomerDetails array from the document
        val customerDetails = customerDetailsRef.get("CustomerDetails") as? List<Map<String, String>> ?: emptyList()

        // Map the "Name" fields from each map in the array
        customerDetails.mapNotNull { it["Name"] }
    } catch (e: Exception) {
        println("Error fetching customers: ${e.message}")
        emptyList()
    }
}