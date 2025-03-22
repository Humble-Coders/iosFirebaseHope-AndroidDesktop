package org.example.iosfirebasehope.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent
import org.example.iosfirebasehope.navigation.events.CurrentCylinderDetailsScreenEvent

@Composable
fun CurrentCylinderDetailsUI(
    initialCylinderDetails: Map<String, String>, // Initial data passed as parameter
    component: CurrentCylinderDetailsComponent,
    db: FirebaseFirestore
) {
    var currentCylinderDetails by remember { mutableStateOf(initialCylinderDetails.toMutableMap()) }
    var showEditDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mutable state variables for Batch, Status, and Remarks
    var batchNumber by remember { mutableStateOf(currentCylinderDetails["Batch Number"] ?: "") }
    var status by remember { mutableStateOf(currentCylinderDetails["Status"] ?: "") }
    var remarks by remember { mutableStateOf(currentCylinderDetails["Remarks"] ?: "") }

    var price by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var currentlyIssuedTo by remember { mutableStateOf<Map<String, String>?>(null) }
    var previousCustomers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var showStatusErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentCylinderDetails["Serial Number"]) {
        val serialNumber = currentCylinderDetails["Serial Number"] ?: return@LaunchedEffect

        val currentlyIssuedToDoc = db.collection("Cylinders")
            .document("Customers")
            .collection(serialNumber)
            .document("Currently Issued To")
            .get()

        if (currentlyIssuedToDoc.exists) {
            currentlyIssuedTo = currentlyIssuedToDoc.data() as? Map<String, String>
        }

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

    val filteredCustomers by derivedStateOf {
        if (searchQuery.isEmpty()) {
            previousCustomers
        } else {
            previousCustomers.filter { customer ->
                customer["name"]?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

   

    LaunchedEffect(currentCylinderDetails["Gas Type"]) {
        try {
            println("Fetching document for gasId: ${currentCylinderDetails["Gas Type"]}")

            val docSnapshot = db.collection("Gases").document(currentCylinderDetails["Gas Type"]!!).get()
            if (docSnapshot.exists) {
                val fetchedVolumesAndSP = docSnapshot.get("VolumesAndSP") as? Map<String, String>
                if (fetchedVolumesAndSP != null) {
                    val volumeType = currentCylinderDetails["Volume Type"] ?: ""
                    price = fetchedVolumesAndSP[volumeType.replace(".", ",")] ?: "Not Found"
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Color(0xFFF3F4F6))
            ) {
                Column {
                    val gasType = currentCylinderDetails["Gas Type"] ?: ""
                    val gasSymbol = getGasSymbol(gasType)
                    val volumeType = currentCylinderDetails["Volume Type"] ?: ""

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(getGasColor(gasType), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = gasSymbol,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Serial No - ${currentCylinderDetails["Serial Number"] ?: ""}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = gasType,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = volumeType.replace(",", "."),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = { showEditDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF2f80eb)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (status == "Full" || status == "Empty") {
                                        showPinDialog = true
                                    } else {
                                        showStatusErrorDialog = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = if (status == "Full" || status == "Empty") Color(0xFFDC3545) else Color.Gray
                                )
                            }
                        }
                    }


                    Divider(color = Color.Black, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            // Display Batch Number
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "Batch:",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = batchNumber,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp
                                )
                            }

                            // Display Status
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "Status:",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = status,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp
                                )
                            }

                            // Display Price
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

                        Column(modifier = Modifier.weight(1f)) {
                            // Display Remarks
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "Remarks:",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = remarks,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp
                                )
                            }

//                            Button(
//                                onClick = { showEditDialog = true },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(top = 8.dp, bottom = 16.dp)
//                                    .height(36.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    backgroundColor = Color(0xFF2f80eb)
//                                )
//                            ) {
//                                Text(
//                                    text = "Edit",
//                                    color = Color.White,
//                                    fontSize = 14.sp
//                                )
//                            }

                            // Add these colors at the top of your file
                            val blueColor = Color(0xFF2f80eb)
                            val redColor = Color(0xFFDC3545)

//                            Button(
//                                onClick = { showPinDialog = true },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(top = 8.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    backgroundColor = redColor
//                                )
//                            ) {
//                                Text("Delete Cylinder", color = Color.White)
//                            }
                            var showSuccessDialog by remember { mutableStateOf(false) }
                            var showErrorDialog by remember { mutableStateOf(false) }
                            var showIncorrectPinDialog by remember { mutableStateOf(false) }

                            // Add these state variables
                            var isVerifyingPin by remember { mutableStateOf(false) }
                            var isDeletingCylinder by remember { mutableStateOf(false) }

                            if (showPinDialog) {
                                AlertDialog(
                                    onDismissRequest = { showPinDialog = false },
                                    title = { Text("Enter PIN to Delete", fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column {
                                            OutlinedTextField(
                                                value = enteredPin,
                                                onValueChange = { enteredPin = it },
                                                label = { Text("PIN") },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Done
                                                ),
                                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                                    focusedBorderColor = blueColor,
                                                    focusedLabelColor = blueColor
                                                ),
                                                enabled = !isVerifyingPin
                                            )
                                            if (isVerifyingPin) {
                                                LinearProgressIndicator(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp),
                                                    color = blueColor
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                isVerifyingPin = true
                                                coroutineScope.launch {
                                                    val passwordDoc = db.collection("Passwords")
                                                        .document("Password")
                                                        .get()

                                                    val storedPin = passwordDoc.get("Pin") as? String

                                                    if (enteredPin == storedPin) {
                                                        showPinDialog = false
                                                        showDeleteConfirmationDialog = true
                                                    } else {
                                                        showIncorrectPinDialog = true
                                                    }
                                                    enteredPin = ""
                                                    isVerifyingPin = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = blueColor),
                                            enabled = !isVerifyingPin && enteredPin.isNotEmpty()
                                        ) {
                                            Text("Verify", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showPinDialog = false },
                                            enabled = !isVerifyingPin
                                        ) {
                                            Text("Cancel", color = blueColor)
                                        }
                                    }
                                )
                            }

                            if (showStatusErrorDialog) {
                                AlertDialog(
                                    onDismissRequest = { showStatusErrorDialog = false },
                                    title = { Text("Cannot Delete", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545)) },
                                    text = { Text("Cylinder can only be deleted when its status is either Full or Empty.") },
                                    confirmButton = {
                                        Button(
                                            onClick = { showStatusErrorDialog = false },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                                        ) {
                                            Text("Close", color = Color.White)
                                        }
                                    }
                                )
                            }

                            if (showDeleteConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmationDialog = false },
                                    title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column {
                                            Text("Are you sure you want to delete this cylinder?")
                                            if (isDeletingCylinder) {
                                                LinearProgressIndicator(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp),
                                                    color = redColor
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                isDeletingCylinder = true
                                                coroutineScope.launch {
                                                    val success = deleteCylinder(
                                                        db,
                                                        currentCylinderDetails["Serial Number"] ?: ""
                                                    )
                                                    if (success) {
                                                        showSuccessDialog = true
                                                    } else {
                                                        showErrorDialog = true
                                                    }
                                                    showDeleteConfirmationDialog = false
                                                    isDeletingCylinder = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = redColor),
                                            enabled = !isDeletingCylinder
                                        ) {
                                            Text("Delete", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showDeleteConfirmationDialog = false },
                                            enabled = !isDeletingCylinder
                                        ) {
                                            Text("Cancel", color = blueColor)
                                        }
                                    }
                                )
                            }

                            if (showSuccessDialog) {
                                AlertDialog(
                                    onDismissRequest = {},
                                    title = { Text("Success", fontWeight = FontWeight.Bold, color = Color.Green) },
                                    text = { Text("Cylinder deleted successfully") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                component.onEvent(CurrentCylinderDetailsScreenEvent.OnGoToHomeClick)
                                            },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = blueColor)
                                        ) {
                                            Text("Back to Home", color = Color.White)
                                        }
                                    }
                                )
                            }

                            if (showErrorDialog) {
                                AlertDialog(
                                    onDismissRequest = {},
                                    title = { Text("Error", fontWeight = FontWeight.Bold, color = redColor) },
                                    text = { Text("Failed to delete cylinder") },
                                    confirmButton = {
                                        Button(
                                            onClick = { showErrorDialog = false },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = blueColor)
                                        ) {
                                            Text("Close", color = Color.White)
                                        }
                                    }
                                )
                            }

                            if (showIncorrectPinDialog) {
                                AlertDialog(
                                    onDismissRequest = {},
                                    title = { Text("Error", fontWeight = FontWeight.Bold, color = redColor) },
                                    text = { Text("Incorrect PIN") },
                                    confirmButton = {
                                        Button(
                                            onClick = { showIncorrectPinDialog = false },
                                            colors = ButtonDefaults.buttonColors(backgroundColor = blueColor)
                                        ) {
                                            Text("Close", color = Color.White)
                                        }
                                    }
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
                                            if (success) {
                                                // Update the mutable state variables
                                                batchNumber = updatedDetails["Batch Number"] ?: ""
                                                status = updatedDetails["Status"] ?: ""
                                                remarks = updatedDetails["Remarks"] ?: ""

                                                // Update the currentCylinderDetails map
                                                currentCylinderDetails.putAll(updatedDetails)

                                                snackbarHostState.showSnackbar("Cylinder details updated successfully!")
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to update cylinder details.")
                                            }
                                            showEditDialog = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            GlowingIconCard(currentlyIssuedTo = currentlyIssuedTo, currentCylinderDetails)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SearchHeader(
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchActiveChange = { isSearchActive = it }
                    )

                    LazyColumn {
                        itemsIndexed(filteredCustomers) { index, customer ->
                            CustomerItem(index, customer)
                        }
                    }

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
  

    // Calculate days held for this cylinder
    val daysHeld = remember(currentlyIssuedTo) {
        val issueDateString = currentlyIssuedTo?.get("date") ?: ""
        try {
            val issueDate = LocalDate.parse(issueDateString)
            val currentDate = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            issueDate.daysUntil(currentDate)
        } catch (e: Exception) {
            println(e)
            // If date parsing fails, return 0 or some placeholder value
            0
        }
    }

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

            if (currentlyIssuedTo != null && currentlyIssuedTo["name"].isNullOrEmpty()) {
                // Display "Not Issued Currently" if the name is empty
                Text(
                    text = "Not Issued Currently",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (currentlyIssuedTo != null) {
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

                    // Display Days Held
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Days Held:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = daysHeld.toString(),
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
                            text = currentCylinderDetails["Notifications Date"] ?: "Not Available",
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
                            text = "Rs. ${currentlyIssuedTo["price"] ?: ""}",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            } else {
                // Fallback message if no details are available
                Text(
                    text = "Not Issued Currently",
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
                    Text("Rs. ${customer["price"] ?: ""}", fontSize = 14.sp, color = Color.Gray)
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
    var isEnabled by remember { mutableStateOf(true) }
    // Dropdown state
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Full", "Empty", "Repair")

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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Remarks Field
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    if(status=="Issued" || status=="At Plant"){
                        isEnabled=false
                    }
                    OutlinedTextField(
                        enabled = isEnabled,
                        value = status,
                        onValueChange = {}, // Disable manual input
                        label = { Text("Status") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if(isEnabled) isStatusDropdownExpanded = true }, // Open dropdown on click
                        readOnly = true, // Make the field read-only
                        trailingIcon = {
                            IconButton(onClick = { if (isEnabled) isStatusDropdownExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Open Dropdown"
                                )
                            }
                        }
                    )

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = isStatusDropdownExpanded,
                        onDismissRequest = { isStatusDropdownExpanded = false },
                        modifier = Modifier.width(200.dp)
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

private suspend fun deleteCylinder(
    db: FirebaseFirestore,
    serialNumber: String
): Boolean {
    return try {
        // Fetch the password document to verify
        val passwordDoc = db.collection("Passwords")
            .document("Password")
            .get()

        // Fetch the existing cylinder details document
        val cylinderDoc = db.collection("Cylinders")
            .document("Cylinders")
            .get()

        if (passwordDoc.exists && cylinderDoc.exists) {
            // Get the stored PIN

            // If pin verification is successful, proceed with deletion
            val existingDetails = cylinderDoc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

            val updatedCylinderDetails = existingDetails.filter {
                it["Serial Number"] != serialNumber
            }

            // Save the updated array back to Firestore
            db.collection("Cylinders")
                .document("Cylinders")
                .set(mapOf("CylinderDetails" to updatedCylinderDetails))

            true
        } else {
            false
        }
    } catch (e: Exception) {
        println("Error deleting cylinder: ${e.message}")
        false
    }
}