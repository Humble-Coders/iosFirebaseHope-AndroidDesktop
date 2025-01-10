package org.example.iosfirebasehope.UI

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent
import org.example.iosfirebasehope.navigation.events.CurrentCylinderDetailsScreenEvent

@Composable
fun CurrentCylinderDetailsUI(
    currentCylinderDetails: Map<String, String>, // Generalized type to handle multiple field types
    component: CurrentCylinderDetailsComponent,
    db: FirebaseFirestore // FirebaseFirestore instance for data fetching
) {
    var showEditDialog by remember { mutableStateOf(false) } // State to control the edit dialog
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var price by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) } // State to toggle between text and search bar
    var searchQuery by remember { mutableStateOf("") }

    // State for Currently Issued To and Previous Customers
    var currentlyIssuedTo by remember { mutableStateOf<Map<String, String>?>(null) }
    var previousCustomers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // Fetch Currently Issued To and Previous Customers on launch
    LaunchedEffect(currentCylinderDetails["Serial Number"]) {
        val serialNumber = currentCylinderDetails["Serial Number"] ?: return@LaunchedEffect

        // Fetch Currently Issued To
        val currentlyIssuedToDoc = db.collection("Cylinders")
            .document("Customers")
            .collection(serialNumber)
            .document("Currently Issued To")
            .get()

        if (currentlyIssuedToDoc.exists) {
            currentlyIssuedTo = currentlyIssuedToDoc.data() as? Map<String, String>
        }

        // Fetch Previous Customers
        val previousCustomersDoc = db.collection("Cylinders")
            .document("Customers")
            .collection(serialNumber)
            .document("Previous Customers")
            .get()

        if (previousCustomersDoc.exists) {
            val customersArray = previousCustomersDoc.get("customers") as? List<Map<String, String>>
            previousCustomers = customersArray ?: emptyList()
        }
    }

    // Filter customers based on search query
    val filteredCustomers by derivedStateOf {
        if (searchQuery.isEmpty()) {
            previousCustomers
        } else {
            previousCustomers.filter { customer ->
                customer["name"]?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val keyDisplayNames = mapOf(
        "Batch Number" to "Batch",
        "Status" to "Status",
        "Remarks" to "Remarks"
    )

    LaunchedEffect(currentCylinderDetails["Gas Type"]) {
        try {
            println("Fetching document for gasId: ${currentCylinderDetails["Gas Type"]}")

            val docSnapshot = db.collection("Gases").document(currentCylinderDetails["Gas Type"]!!).get()
            if (docSnapshot.exists) {
                val fetchedVolumesAndSP = docSnapshot.get("VolumesAndSP") as? Map<String, String>
                if (fetchedVolumesAndSP != null) {
                    val volumeType = currentCylinderDetails["Volume Type"] ?: ""
                    price = fetchedVolumesAndSP[volumeType] ?: "Not Found"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cylinder Details") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(CurrentCylinderDetailsScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            // Snackbar host to display messages
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Box displaying current cylinder details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp) // Reduced bottom padding
                    .background(Color(0xFFF3F4F6))
            ) {
                Column {
                    val gasType = currentCylinderDetails["Gas Type"] ?: ""
                    val gasSymbol = getGasSymbol(gasType)
                    val volumeType = currentCylinderDetails["Volume Type"] ?: ""

                    // Row with gas symbol and serial number at the top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp), // Reduced padding
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gas symbol
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp) // Reduced size of gas symbol
                                .background(getGasColor(gasType), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = gasSymbol,
                                color = Color.White,
                                fontSize = 20.sp, // Slightly smaller font size
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp)) // Reduced space between items

                        Column {
                            // Displaying Serial Number, Gas Type, and Volume Type
                            Text(
                                text = "Serial No - ${currentCylinderDetails["Serial Number"] ?: ""}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // Slightly smaller font size
                            )
                            Text(
                                text = gasType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // Slightly smaller font size
                            )
                            Text(
                                text = volumeType.replace(",", "."),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // Slightly smaller font size
                            )
                        }
                    }

                    // Divider under the row
                    Divider(color = Color.Black, thickness = 1.dp)

                    // Remaining details (like Batch Number, Status, Remarks, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp) // Reduced padding
                    ) {
                        // First column: Fixed fields (Batch Number, Status, Remarks)
                        Column(modifier = Modifier.weight(1.2f)) { // Adjust weight to reduce spacing
                            listOf("Batch Number", "Status").forEach { key ->
                                val value = currentCylinderDetails[key]
                                val displayName =
                                    keyDisplayNames[key] ?: key // Use the display name from the map
                                if (!value.isNullOrEmpty()) {
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) { // Reduced vertical padding
                                        Text(
                                            text = "$displayName:", // Display the mapped name
                                            modifier = Modifier.weight(1f), // Adjust weight to shrink the gap
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp // Slightly smaller font size
                                        )
                                        Text(
                                            text = value,
                                            modifier = Modifier.weight(1f), // Equal weight for both columns
                                            fontSize = 14.sp // Slightly smaller font size
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "Price:",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Rs. $price",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Second column: Dynamic fields (any other fields not in the first column)
                        Column(modifier = Modifier.weight(1f)) {
                            listOf("Remarks").forEach { key ->
                                val value = currentCylinderDetails[key]
                                val displayName = keyDisplayNames[key] ?: key // Use the display name from the map
                                if (!value.isNullOrEmpty()) {
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) { // Reduced vertical padding
                                        Text(
                                            text = "$displayName:", // Display the mapped name
                                            modifier = Modifier.weight(1f), // Adjust weight to shrink the gap
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp // Slightly smaller font size
                                        )
                                        Text(
                                            text = value,
                                            modifier = Modifier.weight(1f), // Equal weight for both columns
                                            fontSize = 14.sp // Slightly smaller font size
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { showEditDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 16.dp) // Add some spacing
                                    .height(36.dp), // Small button height
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF2f80eb) // Blue color
                                )
                            ) {
                                Text(
                                    text = "Edit",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            if (showEditDialog) {
                                EditCylinderDetailsDialog(
                                    currentDetails = currentCylinderDetails,
                                    onDismiss = { showEditDialog = false },
                                    onSave = { updatedDetails ->
                                        coroutineScope.launch {
                                            val success = updateCylinderDetails(
                                                db,
                                                currentCylinderDetails["Serial Number"] ?: "",
                                                updatedDetails
                                            )
                                            showEditDialog = false

                                            if (success) {
                                                // Show success Snackbar
                                                snackbarHostState.showSnackbar("Cylinder details updated successfully!")
                                            } else {
                                                // Show failure Snackbar
                                                snackbarHostState.showSnackbar("Failed to update cylinder details.")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            GlowingIconCard(currentlyIssuedTo = currentlyIssuedTo, currentCylinderDetails)

            // Previous Customers Card (always visible)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Header with "Previous Customers" and Search Icon
                    SearchHeader(
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchActiveChange = { isSearchActive = it }
                    )

                    // LazyColumn for previous customers
                    LazyColumn {
                        itemsIndexed(filteredCustomers) { index, customer ->
                            CustomerItem(index, customer)
                        }
                    }

                    // Show "No customers found" if the filtered list is empty
                    if (filteredCustomers.isEmpty()) {
                        Text(
                            text = "No customers found",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp) // Reduced height for the entire header
            .padding(bottom = 4.dp) // Reduced bottom padding
    ) {
        AnimatedVisibility(
            visible = isSearchActive,
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = {
                        onSearchQueryChange("")
                        onSearchActiveChange(false)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                placeholder = { Text("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp), // Reduced height for the search bar
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF2f80eb),
                    unfocusedBorderColor = Color(0xFF2f80eb)
                ),
                singleLine = true,
                visualTransformation = VisualTransformation.None
            )
        }

        AnimatedVisibility(
            visible = !isSearchActive,
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Reduced height for the row
                    .padding(vertical = 4.dp), // Reduced vertical padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previous Customers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp, // Slightly smaller font size
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onSearchActiveChange(true) },
                    modifier = Modifier.size(20.dp) // Smaller icon button size
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                }
            }
        }
    }
}

@Composable
fun GlowingIconCard(currentlyIssuedTo: Map<String, String>?, currentCylinderDetails: Map<String, String>) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(4.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green tint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // "Currently Issued To" Heading
            Text(
                text = "Currently Issued To",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (currentlyIssuedTo != null) {
                Column {
                    val labelWidth = 120.dp // Reduced width for the labels

                    // Display Name
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Name:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = currentlyIssuedTo["name"] ?: "",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    // Display Date
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Issue Date:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = currentlyIssuedTo["date"] ?: "",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Return Date:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = currentCylinderDetails["Return Date"] ?: "Not Available",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    // Display Rate
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Selling Price:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "Rs. ${currentlyIssuedTo["rate"] ?: ""}",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            } else {
                // Fallback message if no details are available
                Text(
                    text = "No data available",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Extracted CustomerItem composable
@Composable
private fun CustomerItem(index: Int, customer: Map<String, String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .clickable { println("Clicked on customer: ${customer["name"]}") }
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${index + 1}.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = customer["name"] ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(0.7f)) {
                    Text("Issue Date:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 22.dp))
                    Text("Selling Price:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(customer["date"] ?: "", fontSize = 14.sp, color = Color.Gray)
                    Text("Rs. ${customer["rate"] ?: ""}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EditCylinderDetailsDialog(
    currentDetails: Map<String, String>, // Current cylinder details
    onDismiss: () -> Unit, // Callback to close the dialog
    onSave: (Map<String, String>) -> Unit // Callback to save the edited details
) {
    var batchNumber by remember { mutableStateOf(currentDetails["Batch Number"] ?: "") }
    var remarks by remember { mutableStateOf(currentDetails["Remarks"] ?: "") }
    var status by remember { mutableStateOf(currentDetails["Status"] ?: "") }

    // Dropdown state
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Full", "Empty", "Repair", "At Plant", "Issued")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cylinder Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Batch Number Field
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Remarks Field
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Dropdown
                Box(modifier = Modifier.fillMaxWidth().clickable { isStatusDropdownExpanded = true }) {
                    // Wrap the OutlinedTextField in a Box and apply clickable to the Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isStatusDropdownExpanded = true } // Open dropdown on click
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {}, // Disable manual input
                            label = { Text("Status") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true // Make the field read-only
                        )
                    }

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = isStatusDropdownExpanded,
                        onDismissRequest = { isStatusDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    status = option // Update the selected status
                                    isStatusDropdownExpanded = false // Close the dropdown
                                }
                            ) {
                                Text(text = option)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Save the edited details
                    val updatedDetails = mapOf(
                        "Batch Number" to batchNumber,
                        "Remarks" to remarks,
                        "Status" to status
                    )
                    onSave(updatedDetails)
                    onDismiss() // Close the dialog
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss // Close the dialog
            ) {
                Text("Cancel", color = Color(0xFF2f80eb))
            }
        }
    )
}

private suspend fun updateCylinderDetails(db: FirebaseFirestore, serialNumber: String, updatedDetails: Map<String, String>): Boolean {
    return try {
        // Fetch the existing document
        val document = db.collection("Cylinders")
            .document("Cylinders")
            .get()

        if (document.exists) {
            // Fetch the existing "CylinderDetails" array
            val existingDetails = document.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

            // Find the current cylinder details and update them
            val updatedCylinderDetails = existingDetails.map { details ->
                if (details["Serial Number"] == serialNumber) {
                    details + updatedDetails // Merge the updated details
                } else {
                    details
                }
            }

            // Save the updated array back to Firestore
            db.collection("Cylinders")
                .document("Cylinders")
                .set(mapOf("CylinderDetails" to updatedCylinderDetails))

            true // Return true on success
        } else {
            false // Return false if the document does not exist
        }
    } catch (e: Exception) {
        println("Error updating cylinder details: ${e.message}")
        false // Return false on failure
    }
}