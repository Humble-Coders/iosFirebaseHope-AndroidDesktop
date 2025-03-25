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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import org.example.iosfirebasehope.navigation.components.SendForRefillingComponent
import org.example.iosfirebasehope.navigation.events.SendForRefillingEvent
@Composable
fun SendForRefillingScreenUI(
    component: SendForRefillingComponent,
    VendorName: String,
    db: FirebaseFirestore
) {






    // Use the platform-specific BackButtonHandler


    // Existing state variables
    val details = remember { mutableStateOf<Map<String, String>?>(null) }
    val creditValue = remember { mutableStateOf<String?>(null) }
    val phoneNumberValue = remember { mutableStateOf<String?>(null) }
    var showAddVendorDialog by remember { mutableStateOf(false) }
    var showAddCylinderDialog by remember { mutableStateOf(false) }
    var selectedVendor by remember { mutableStateOf<String?>(null) }
    var Vendors by remember { mutableStateOf<List<String>>(emptyList()) }
    var issuedCylinders by remember { mutableStateOf<List<IssuedCylinder>>(emptyList()) }
    var issueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var alreadySelectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var alreadySelectedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }


    var currentDateTime by remember {mutableStateOf("")}






    var isBackButtonEnabled = remember { mutableStateOf(true) }


    // New state variables for date pickers
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var selectedIssueDate by remember { mutableStateOf<Long?>(null) }


    // State variable for bottom buttons visibility
    var showBottomButtons by remember { mutableStateOf(true) }


    // State variable for checkout dialog
    var showCheckoutDialog by remember { mutableStateOf(false) }


    // State variable for validation dialog
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }


    // Calculate the real-time total price




    // Fetch all fields from Firestore
    LaunchedEffect(VendorName) {
        val document = db.collection("Vendors")
            .document("Details")
            .collection("Names")
            .document(VendorName)
            .get()


        // Extract the "Details" map from the document
        details.value = document.get("Details") as? Map<String, String>


        // Extract the "Deposit", "Credit", and "Phone Number" values from the "Details" map
        creditValue.value = details.value?.get("Credit")?.toString()
        phoneNumberValue.value = details.value?.get("Phone Number")?.toString()
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
                    onClick = { component.onEvent(SendForRefillingEvent.OnBackClick) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }


                Text(
                    text = "Send For Refilling",
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
                visible = !showAddCylinderDialog, // Check both dialogs
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
                                    text = VendorName,
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
                                    modifier = Modifier.weight(0.3f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                                ) {
                                    Text(
                                        text = "Phone:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                    Text(
                                        text = "Credit:",
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
                                    creditValue.value?.let { credit ->
                                        Text(
                                            text = credit,
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
                            text = if (issuedCylinders.isEmpty()) "Current Cart: Empty" else "Current Cart",
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




                    CombinedListRef(
                        issuedCylinders = issuedCylinders,
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
                AddCylinderDialogRef(
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
                    alreadySelectedLPGQuantities = alreadySelectedLPGQuantities // Pass the state
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
                }


                // Second Row: Total Button (full width)
                Button(
                    onClick = {
                        // Validate conditions
                        val errorMessage = validateCheckoutConditionsRef(
                            issuedCylinders,
                            selectedIssueDate
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
                        text = "Checkout",
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




        if (showCheckoutDialog) {
            CheckoutDialogRef(


                selectedIssueDate = selectedIssueDate,
                onDismiss = { showCheckoutDialog = false },
                onConfirm = { cash, credit ->
                    // Call the checkoutCylinders function
                    coroutineScope.launch {
                        val issueDateString = selectedIssueDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt()).toString()
                        } ?: ""




                        // Push transaction details to Firestore
                        val success = checkoutCylindersRef(
                            db = db,
                            VendorName = VendorName,
                            issuedCylinders = issuedCylinders,
                            issueDate = issueDateString,
                            cash = cash,
                            credit = credit,
                            onCurrentDateTime = { currentDateTime = it }


                        )


                        if (success) {
                            component.onEvent(SendForRefillingEvent.OnChallanClick(VendorName, currentDateTime))
                            showCheckoutDialog = false // Close the dialog
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
            ValidationDialogRef(
                message = validationMessage,
                onDismiss = { showValidationDialog = false } // Close the dialog
            )
        }
    }
}


@Composable
fun ValidationDialogRef(
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


fun validateCheckoutConditionsRef(
    issuedCylinders: List<IssuedCylinder>,
    selectedIssueDate: Long?
): String? {
    return when {
        issuedCylinders.isEmpty() -> "Nothing added to cart."
        selectedIssueDate == null -> "No issue date selected."
        else -> null // No error
    }
}


@Composable
fun CheckoutDialogRef(
    selectedIssueDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (cash: String, credit: String) -> Unit
) {
    var cash by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
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
            }
        },
        text = {
            Column {
                // Display issue date
                Text(
                    text = "Issue Date: ${selectedIssueDate?.let {
                        LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt())
                    } ?: "Not selected"}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
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
fun CombinedListRef(
    issuedCylinders: List<IssuedCylinder>,
    onDeleteCylinder: (IssuedCylinder) -> Unit,
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
                    IssuedCylinderCardRef(
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




    }
}








@Composable
fun AddCylinderDialogRef(
    onDismiss: () -> Unit,
    onAddCylinder: (IssuedCylinder) -> Unit,
    db: FirebaseFirestore,
    alreadySelectedCylinders: List<String>,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    isBackButtonEnabled: MutableState<Boolean>
) {
    isBackButtonEnabled.value = false // Disable the back button for this dialog
    var gasType by remember { mutableStateOf<String?>(null) }
    var volumeType by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var selectedCylinders by remember { mutableStateOf<List<String?>>(emptyList()) } // Changed to nullable values
    var prices by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf<Double>(0.0) }
    var cylinderOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var volumeOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var localAlreadySelectedCylinders by remember { mutableStateOf(alreadySelectedCylinders) }
    var showValidationMessage by remember { mutableStateOf(false) } // State for validation message
    var availableLPGQuantity by remember { mutableStateOf<Int?>(null) } // State for available LPG quantity
    var quantityError by remember { mutableStateOf<String?>(null) } // State for quantity error message


    // Track currently selected cylinders in this dialog
    var currentlySelectedCylinders by remember { mutableStateOf<Set<String>>(emptySet()) }


    val coroutineScope = rememberCoroutineScope()


    // Function to update available cylinders based on currently selected cylinders
    fun updateAvailableCylinders() {
        if (gasType != null && volumeType != null && gasType != "LPG") {
            coroutineScope.launch {
                // Fetch all available cylinders of this type and status
                val allCylinders = fetchCylindersByStatus(db, gasType!!, volumeType!!, "Empty")


                // Filter out cylinders that are already selected outside this dialog
                availableCylinders = allCylinders.filter {
                    it !in alreadySelectedCylinders && it !in currentlySelectedCylinders
                }


                println("Updated available cylinders: ${availableCylinders.size} cylinders available")
                println("Currently selected in this dialog: ${currentlySelectedCylinders.joinToString()}")
            }
        }
    }


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
        if (gasType == "LPG" && volumeType != null) {
            coroutineScope.launch {
                val lpgDocument = db.collection("Cylinders").document("LPG").get()
                val lpgEmptyMap = lpgDocument.get("LPGEmpty") as? Map<String, Int>
                lpgEmptyMap?.let {
                    // Replace commas in key names with dots
                    val formattedVolumeType = volumeType!!.replace(".", ",")
                    val totalQuantity = it[formattedVolumeType] ?: 0
                    val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                    availableLPGQuantity = totalQuantity - alreadySelectedQuantity
                }
            }
        } else {
            availableCylinders = emptyList()
            availableLPGQuantity = null
        }
    }


    // Update selectedCylinders list when quantity changes
    LaunchedEffect(quantity.toIntOrNull()) {
        if (gasType != "LPG") {
            val quantityInt = quantity.toIntOrNull() ?: 0


            // Collect cylinders that were previously selected but will no longer be tracked
            val previouslySelectedCylinders = selectedCylinders.filterNotNull()


            // Initialize with nulls instead of empty strings
            selectedCylinders = List(quantityInt) { null }


            // Reset the currently selected cylinders
            currentlySelectedCylinders = emptySet()


            // Update available cylinders to reflect that previous selections are now available
            updateAvailableCylinders()
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


    // Initial fetch of available cylinders
    LaunchedEffect(gasType, volumeType) {
        if (gasType != null && volumeType != null && gasType != "LPG") {
            updateAvailableCylinders()
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
                        SearchableDropdownRef(
                            options = cylinderOptions,
                            selectedItem = gasType,
                            onItemSelected = { gasType = it },
                            onClearSelection = { /* Gas type cleared */ },
                            placeholder = "Select Gas Type"
                        )


                        // Volume Type Dropdown
                        Text("Volume Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdownRef(
                            options = volumeOptions,
                            selectedItem = volumeType,
                            onItemSelected = { volumeType = it },
                            onClearSelection = { /* Volume type cleared */ },
                            placeholder = "Select Volume Type"
                        )


                        // Display available LPG quantity if gasType is LPG
                        if (gasType == "LPG" && availableLPGQuantity != null) {
                            Text(
                                text = "Available LPG Quantity: $availableLPGQuantity",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }


                        Text("Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
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


                        // Cylinder Dropdowns (only for non-LPG gas types)
                        if (gasType != "LPG" && quantity.toIntOrNull() != null && quantity.toIntOrNull()!! > 0) {
                            // We need a key to force re-composition when quantity changes
                            key(quantity) {
                                // Keep track of which indexes should be visible
                                var visibleDropdowns by remember { mutableStateOf(List(quantity.toInt()) { true }) }


                                Column {
                                    repeat(quantity.toInt()) { index ->
                                        // Only show this dropdown if it's marked as visible
                                        if (visibleDropdowns[index]) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Cylinder ${index + 1}", fontWeight = FontWeight.Bold)
                                                    SearchableDropdownRef(
                                                        options = availableCylinders,
                                                        selectedItem = selectedCylinders.getOrNull(index),
                                                        onItemSelected = { selectedCylinder ->
                                                            // Update selected cylinders list
                                                            selectedCylinders = selectedCylinders.toMutableList().apply {
                                                                set(index, selectedCylinder)
                                                            }


                                                            // Add to currently selected cylinders set
                                                            currentlySelectedCylinders = currentlySelectedCylinders + selectedCylinder


                                                            // Update available cylinders
                                                            updateAvailableCylinders()
                                                        },
                                                        onClearSelection = {
                                                            // If there was a cylinder selected previously, remove it from currentlySelectedCylinders
                                                            val previousCylinder = selectedCylinders.getOrNull(index)
                                                            if (previousCylinder != null) {
                                                                currentlySelectedCylinders = currentlySelectedCylinders - previousCylinder


                                                                // Update the selected cylinders list
                                                                selectedCylinders = selectedCylinders.toMutableList().apply {
                                                                    set(index, null)
                                                                }


                                                                // Update available cylinders
                                                                updateAvailableCylinders()
                                                            }
                                                        },
                                                        placeholder = "Select Cylinder"
                                                    )
                                                }


                                                // Show remove icon only if the dropdown is empty and it's not the last visible dropdown
                                                if (selectedCylinders.getOrNull(index) == null && visibleDropdowns.count { it } > 1) {
                                                    Column(
                                                        modifier = Modifier.height(80.dp), // Match the approximate height of the dropdown column
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                // Make this dropdown invisible
                                                                visibleDropdowns = visibleDropdowns.toMutableList().apply {
                                                                    set(index, false)
                                                                }
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Remove cylinder",
                                                                tint = Color.Red,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }


                                // Store visible dropdowns state for use in validation
                                LaunchedEffect(visibleDropdowns) {
                                    // Store the current visible dropdowns for validation
                                    val visibleIndices = visibleDropdowns.mapIndexedNotNull { i, visible ->
                                        if (visible) i else null
                                    }


                                    // Update currentlySelectedCylinders based on visible dropdowns only
                                    currentlySelectedCylinders = selectedCylinders
                                        .filterIndexed { index, cylinder ->
                                            visibleDropdowns.getOrNull(index) == true && cylinder != null
                                        }
                                        .filterNotNull()
                                        .toSet()
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
                                    text = "All fields are necessary",
                                    color = Color.Red,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }


                            // Cancel Button
                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }


                            Spacer(modifier = Modifier.width(8.dp))


                            // Add Button
                            Button(
                                onClick = {
                                    // Check if all fields are filled
                                    if (gasType.isNullOrEmpty() || volumeType.isNullOrEmpty() || quantity.isEmpty()) {
                                        showValidationMessage = true
                                        return@Button
                                    }


                                    // For non-LPG, check that we have at least one selected cylinder
                                    if (gasType != "LPG" && selectedCylinders.filterNotNull().isEmpty()) {
                                        showValidationMessage = true
                                        return@Button
                                    } else {
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        if (gasType == "LPG" && availableLPGQuantity != null) {
                                            // Validate quantity for LPG
                                            if (quantityInt <= 0 || quantityInt > availableLPGQuantity!!) {
                                                quantityError = "Quantity must be between 1 and $availableLPGQuantity"
                                                return@Button
                                            }
                                        }
                                        totalPrice = 0.0
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
                                            selectedCylinders.filterNotNull().forEach { serialNumber ->
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
                                        onUpdateAlreadySelectedCylinders(alreadySelectedCylinders + currentlySelectedCylinders.toList())
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
fun SearchableDropdownRef(
    options: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    onClearSelection: () -> Unit = {}, // Added callback for clearing selection
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedItem ?: "") }
    val filteredOptions = options.filter { it.contains(searchQuery, ignoreCase = true) }


    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                value = searchQuery,
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
                modifier = Modifier.fillMaxWidth()
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
                                    text = option,
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


suspend fun checkoutCylindersRef(
    db: FirebaseFirestore,
    VendorName: String?,
    issuedCylinders: List<IssuedCylinder>,
    issueDate: String,
    cash: String,
    credit: String,
    onCurrentDateTime: (String) -> Unit
): Boolean {
    if (VendorName == null || issueDate.isEmpty()) return false


    try {
        // Get the current date and time
        val currentTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
            .toString()


        // Combine issueDate with current time
        val dateTimeString = "${issueDate}_${currentTime}"
            .replace(":", "-")
            .substringBefore(".")


        onCurrentDateTime(dateTimeString)


        // Pre-process data for faster operations
        val nonLpgCylinders = issuedCylinders.filter { it.gasType != "LPG" }
        val lpgCylinders = issuedCylinders.filter { it.gasType == "LPG" }
        val nonLpgCylinderSerialNumbers = nonLpgCylinders.map { it.serialNumber }


        // Prepare transaction data
        val transactionsRef = db.collection("TransactionVendor")
            .document(VendorName)
            .collection("DateAndTime")
            .document(dateTimeString)


        // Prepare cylinders issued data
        val cylindersIssued = nonLpgCylinders.map { cylinder ->
            mapOf(
                "Serial Number" to cylinder.serialNumber,
                "Issue Date" to issueDate
            )
        }


        // Prepare LPG issued data
        val lpgIssued = lpgCylinders.groupBy { it.volumeType }.map { (volumeType, cylinders) ->
            mapOf(
                "Volume Type" to volumeType,
                "Quantity" to cylinders.sumOf { it.quantity },
                "Date" to issueDate
            )
        }


        // Prepare LPG quantity updates by volume type
        val lpgVolumeUpdates = lpgCylinders.groupBy { it.volumeType.replace(".", ",") }
            .mapValues { (_, cylinders) -> cylinders.sumOf { it.quantity } }


        // Parallelize independent operations using coroutineScope
        coroutineScope {
            // Task 1: Create transaction documents in a batch
            val createTransactionTask = async {
                val batch = db.batch()


                // Main transaction document
                batch.set(transactionsRef, mapOf("Date" to dateTimeString))


                // Transaction details collection
                val transactionDetailsRef = transactionsRef.collection("Transaction Details")


                batch.set(transactionDetailsRef.document("Cash"), mapOf("Amount" to cash))
                batch.set(transactionDetailsRef.document("Credit"), mapOf("Amount" to credit))
                batch.set(transactionDetailsRef.document("Cylinders Issued"), mapOf("CylindersIssued" to cylindersIssued))
                batch.set(transactionDetailsRef.document("Cylinders Returned"), mapOf("CylindersReturned" to emptyList<String>()))
                batch.set(transactionDetailsRef.document("LPG Issued"), mapOf("LPGIssued" to lpgIssued))


                // Execute all transaction-related operations as a single batch
                batch.commit()
            }


            // Task 2: Update non-LPG cylinders in Vendors/Issued Cylinders
            val updateIssuedCylindersTask = async {
                if (nonLpgCylinders.isNotEmpty()) {
                    val issuedCylindersRef = db.collection("Vendors")
                        .document("Issued Cylinders")
                        .collection("Names")
                        .document(VendorName)


                    val snapshot = issuedCylindersRef.get()
                    val existingDetails = if (snapshot.exists) {
                        snapshot.get("Details") as? List<String> ?: emptyList()
                    } else {
                        emptyList()
                    }


                    // Add new serial numbers to the existing array
                    val updatedDetails = existingDetails + nonLpgCylinderSerialNumbers


                    // Update the document with the new array
                    issuedCylindersRef.set(mapOf("Details" to updatedDetails))
                }
            }


            // Task 3: Update LPG quantities in Vendors/LPG Issued
            val updateLpgIssuedTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    val lpgIssuedRef = db.collection("Vendors")
                        .document("LPG Issued")
                        .collection("Names")
                        .document(VendorName)


                    val snapshot = lpgIssuedRef.get()
                    val existingLpgQuantities = if (snapshot.exists) {
                        snapshot.get("Quantities") as? Map<String, String> ?: emptyMap()
                    } else {
                        emptyMap()
                    }


                    // Update quantities for each volume type
                    val mergedLpgQuantities = existingLpgQuantities.toMutableMap()


                    lpgVolumeUpdates.forEach { (volumeType, quantity) ->
                        val existingQuantity = existingLpgQuantities[volumeType]?.toIntOrNull() ?: 0
                        val newQuantity = existingQuantity + quantity
                        mergedLpgQuantities[volumeType] = newQuantity.toString()
                    }


                    // Update the document with the merged map
                    lpgIssuedRef.set(mapOf("Quantities" to mergedLpgQuantities))
                }
            }


            // Task 4: Update CylinderDetails status
            val updateCylinderStatusTask = async {
                if (nonLpgCylinderSerialNumbers.isNotEmpty()) {
                    val cylindersRef = db.collection("Cylinders").document("Cylinders")


                    val snapshot = cylindersRef.get()
                    val cylinderDetails = if (snapshot.exists) {
                        snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
                    } else {
                        emptyList()
                    }


                    // Create a lookup set for faster checking
                    val serialNumberSet = nonLpgCylinderSerialNumbers.toSet()


                    // Update the status for cylinders
                    val updatedCylinderDetails = cylinderDetails.map { cylinder ->
                        if (cylinder["Serial Number"] in serialNumberSet) {
                            cylinder.toMutableMap().apply {
                                this["Status"] = "At Plant"
                            }
                        } else {
                            cylinder
                        }
                    }


                    // Save the updated CylinderDetails array back to Firestore
                    cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
                }
            }


            // Task 5: Update Vendor Credit
            val updateVendorCreditTask = async {
                val customerDetailsRef = db.collection("Vendors")
                    .document("Details")
                    .collection("Names")
                    .document(VendorName)


                val customerDetailsSnapshot = customerDetailsRef.get()
                if (customerDetailsSnapshot.exists) {
                    val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()


                    // Increment the "Credit" field
                    val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
                    val newCredit = currentCredit + (credit.toDoubleOrNull() ?: 0.0)


                    // Create the updated map
                    val updatedDetailsMap = detailsMap.toMutableMap().apply {
                        this["Credit"] = newCredit.toString()
                    }


                    // Save the updated map back to Firestore
                    customerDetailsRef.update("Details" to updatedDetailsMap)
                }
            }


            // Task 6: Update LPG Empty quantities
            val updateLpgEmptyTask = async {
                if (lpgCylinders.isNotEmpty()) {
                    val lpgDocumentRef = db.collection("Cylinders").document("LPG")
                    val lpgDocumentSnapshot = lpgDocumentRef.get()


                    if (lpgDocumentSnapshot.exists) {
                        val lpgEmptyMap = lpgDocumentSnapshot.get("LPGEmpty") as? Map<String, Int> ?: emptyMap()
                        val updatedLpgEmptyMap = lpgEmptyMap.toMutableMap()


                        lpgVolumeUpdates.forEach { (volumeType, quantity) ->
                            val currentQuantity = updatedLpgEmptyMap[volumeType] ?: 0
                            updatedLpgEmptyMap[volumeType] = currentQuantity - quantity
                        }


                        lpgDocumentRef.update("LPGEmpty" to updatedLpgEmptyMap)
                    }
                }
            }


            // Await all parallel tasks to complete
            awaitAll(
                createTransactionTask,
                updateIssuedCylindersTask,
                updateLpgIssuedTask,
                updateCylinderStatusTask,
                updateVendorCreditTask,
                updateLpgEmptyTask
            )
        }


        // Create collections for each non-LPG cylinder in Cylinders > Vendors
        // This part can't be easily parallelized due to the iterative nature
        if (nonLpgCylinders.isNotEmpty()) {
            // Process in batches for better performance
            nonLpgCylinders.chunked(10).forEach { cylinderBatch ->
                val batch = db.batch()


                cylinderBatch.forEach { cylinder ->
                    val cylinderRef = db.collection("Cylinders")
                        .document("Vendors")
                        .collection(cylinder.serialNumber)
                        .document("Refill To")


                    batch.set(cylinderRef, mapOf(
                        "name" to VendorName,
                        "date" to issueDate
                    ))
                }


                batch.commit()
            }
        }


        return true
    } catch (e: Exception) {
        println("Error in checkoutCylindersRef: ${e.message}")
        return false
    }
}

@Composable
fun IssuedCylinderCardRef(
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