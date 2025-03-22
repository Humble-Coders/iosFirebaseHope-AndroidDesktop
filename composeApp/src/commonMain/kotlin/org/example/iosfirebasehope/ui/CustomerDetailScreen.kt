package org.example.iosfirebasehope.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.CustomerDetailsScreenComponent
import org.example.iosfirebasehope.navigation.events.CustomerDetailsScreenEvent


@Composable
fun CustomerDetailsScreenUI(
    customerName: String, // Customer name passed as a parameter
    component: CustomerDetailsScreenComponent,
    cylinderDetail: List<Map<String, String>>, // List of cylinder details passed as a parameter
    db: FirebaseFirestore, // FirebaseFirestore instance for data fetching,
    gasList: List<String>
) {
    println("CustomerDetailsScreenUI : $cylinderDetail")
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<Pair<String?, String?>?>(null) }

    // State for customer details
    var customerDetails by remember { mutableStateOf<Map<String, String>?>(null) }

    // State for issued cylinders
    var issuedCylinders by remember { mutableStateOf<List<String>?>(null) }

    // State for LPG Issued
    var lpgIssued by remember { mutableStateOf<Map<String, String>?>(null) }

    // State for search functionality
    var isSearchActive by remember { mutableStateOf(false) } // State to toggle between text and search bar
    var searchQuery by remember { mutableStateOf("") } // State to hold the search query
    var isUploading = remember { mutableStateOf(false) }
    var isCashDialogActive by remember { mutableStateOf(false) }
    var selectedGases by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Fetch customer details, issued cylinders, and LPG Issued on launch
    LaunchedEffect(customerName) {
        try {
            // Fetch customer details
            val customerDoc = db.collection("Customers")
                .document("Details")
                .collection("Names")
                .document(customerName)
                .get()

            if (customerDoc.exists) {
                customerDetails = customerDoc.get("Details") as? Map<String, String>
            } else {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Customer details not found.")
                }
            }

            // Fetch issued cylinders
            val issuedCylindersDoc = db.collection("Customers")
                .document("Issued Cylinders")
                .collection("Names")
                .document(customerName)
                .get()

            if (issuedCylindersDoc.exists) {
                issuedCylinders = issuedCylindersDoc.get("Details") as? List<String>
            } else {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("No issued cylinders found.")
                }
            }

            // Fetch LPG Issued
            val lpgIssuedDoc = db.collection("Customers")
                .document("LPG Issued")
                .collection("Names")
                .document(customerName)
                .get()

            if (lpgIssuedDoc.exists) {
                lpgIssued = lpgIssuedDoc.get("Quantities") as? Map<String, String>
            } else {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("No LPG Issued found.")
                }
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Failed to fetch data: ${e.message}")
            }
        }
    }

    // Filter issued cylinders based on search query
    val filteredIssuedCylinders by derivedStateOf {
        if (searchQuery.isEmpty() && selectedGases.isEmpty()) {
            issuedCylinders ?: emptyList()
        } else {
            issuedCylinders?.filter { serialNumber ->
                val cylinder = cylinderDetail.find { it["Serial Number"] == serialNumber }
                val matchesSearchQuery = serialNumber.contains(searchQuery, ignoreCase = true)
                val matchesGases = selectedGases.isEmpty() || cylinder?.get("Gas Type") == selectedGases
                matchesSearchQuery && matchesGases
            } ?: emptyList()
        }
    }

    val groupedCylinders = filteredIssuedCylinders.groupBy { serialNumber ->
        val cylinder = cylinderDetail.find { it["Serial Number"] == serialNumber }
        cylinder?.get("Gas Type") to cylinder?.get("Volume Type")
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Customer Details", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(CustomerDetailsScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (customerDetails != null) {
                SwipeableCustomerDetailsCard(
                    customerName = customerName,
                    customerDetails = customerDetails!!,
                    coroutineScope = coroutineScope
                )
            } else {
                // Show loading or error message
                Text(
                    text = "Loading customer details...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Button(
                onClick = { isCashDialogActive = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text(
                    text = "Cash Transaction",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            if (isCashDialogActive) {
                CashTransactionDialog(
                    onDismiss = { isCashDialogActive = false },
                    isUploading = isUploading,
                    customerName = customerName,
                    db = db,
                    onUpdateSuccess = {
                        // Refresh the customer details
                        coroutineScope.launch {
                            val customerDoc = db.collection("Customers")
                                .document("Details")
                                .collection("Names")
                                .document(customerName)
                                .get()

                            if (customerDoc.exists) {
                                customerDetails = customerDoc.get("Details") as? Map<String, String>
                            } else {
                                scaffoldState.snackbarHostState.showSnackbar("Customer details not found.")
                            }
                        }
                    },
                    coroutineScope = coroutineScope
                )
            }

            // Search Header for Issued Cylinders
//            SearchHeader(
//                isSearchActive = isSearchActive,
//                searchQuery = searchQuery,
//                onSearchQueryChange = { searchQuery = it },
//                onSearchActiveChange = { isSearchActive = it }
//            )

            // Fixed space for Currently Issued Cylinders
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Currently Issued Cylinders:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Box(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .width(64.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedGases.isEmpty()) "Filter" else selectedGases,
                                color = if (selectedGases.isEmpty()) Color.Gray else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expanded = true }
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                selectedGases = ""
                                expanded = false
                            }) {
                                Text(text = "None")
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            gasList.forEach { gas ->
                                DropdownMenuItem(onClick = {
                                    selectedGases = gas
                                    expanded = false
                                }) {
                                    Text(text = gas)
                                }
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }}
            Box(
                modifier = Modifier
                    .fillMaxWidth().weight(3f)
            ) {


                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    groupedCylinders.forEach { (group, cylinders) ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedGroup = group
                                        isDialogOpen = true
                                    },
                                elevation = 2.dp,
                                shape = RoundedCornerShape(8.dp),
                                backgroundColor = Color(0xFFE8F5E9)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "Gas Type: ${group.first}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Volume Type: ${group.second}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Quantity: ${cylinders.size}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Fixed space for LPG Issued
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Fixed height for the section
            ) {
                if (lpgIssued != null) {
                    Text(
                        text = "LPG Issued",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                    LpgIssuedSection(lpgIssued = lpgIssued!!)
                } else {
                    // Show loading or error message
                    Text(
                        text = "No LPG Cylinders Issued",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)

                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (isDialogOpen && selectedGroup != null) {
                val serialNumbers = groupedCylinders[selectedGroup]?.map { it } ?: emptyList()
                AlertDialog(
                    onDismissRequest = { isDialogOpen = false },
                    title = {
                        Text(
                            text = "Serial Numbers",
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
                            items(serialNumbers) { serialNumber ->
                                Text(
                                    text = serialNumber,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { isDialogOpen = false },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Button(
                onClick = { component.onEvent(CustomerDetailsScreenEvent.OnTransactionClick(customerName, cylinderDetail))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text(
                    text = "View All Transactions",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CompactDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp) // Reduced vertical padding
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp, // Reduced font size
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp, // Reduced font size
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LpgIssuedSection(lpgIssued: Map<String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp), // Proper padding for the car
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Set the card color to 0xFFE8F5E9
    ) {
        Column { // Column to display LPG Issued details
            Text(
                text = "LPG Issued Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                lpgIssued.forEach { (key, value) ->
                    val newKey = key.replace(",", ".")
                    Text(
                        text = "$newKey: $value",
                        fontSize = 12.sp, // Reduced font size
                        modifier = Modifier.padding(horizontal = 16.dp), // Reduced padding
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Rest of the code remains unchanged...

// Rest of the code remains unchanged...
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
            .padding(horizontal = 16.dp, vertical = 8.dp) // Horizontal padding of 16.dp
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
                placeholder = { Text("Search issued cylinders...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // Fixed height for the search bar
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
                    .height(52.dp) // Fixed height for the row
                    .padding(vertical = 4.dp), // Reduced vertical padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Currently Issued Cylinders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, // Slightly larger font size
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp) // Horizontal padding of 16.dp
                )
                IconButton(
                    onClick = { onSearchActiveChange(true) },
                    modifier = Modifier.size(24.dp) // Slightly larger icon button size
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableCustomerDetailsCard(
    customerName: String,
    customerDetails: Map<String, String>,
    coroutineScope: CoroutineScope
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp), // ✅ FIXED
        elevation = 4.dp, // ✅ FIXED
        shape = RoundedCornerShape(8.dp)
    )
    {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = customerName,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                fontWeight = FontWeight.Bold
            )

            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // **KMP-Compatible Pager**
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> CustomerDetailsPage(
                        listOf(
                            "Phone Number" to (customerDetails["Phone Number"] ?: "Not Available"),
                            "Deposit" to (customerDetails["Deposit"] ?: "Not Available"),
                            "Credit" to (customerDetails["Credit"] ?: "Not Available"),
                            "Avg Rotation" to ((customerDetails["Average Days"]?.let { "$it days" }) ?: "Not Available")
                        )
                    )
                    1 -> CustomerDetailsPage(
                        listOf(
                            "Address" to (customerDetails["Address"] ?: "Not Available"),
                            "Reference Name" to (customerDetails["Reference Name"] ?: "Not Available"),
                            "Reference Mobile" to (customerDetails["Reference Mobile"] ?: "Not Available"),
                            "UID" to (customerDetails["UID"] ?: "Not Available")
                        )
                    )
                }
            }


            // **Page Indicators**
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(8.dp)
                            .background(color)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(iteration)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDetailsPage(details: List<Pair<String, String>>) {
    Column(modifier = Modifier.padding(8.dp)) {
        details.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp) // Add spacing between label and value
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CashTransactionDialog(
    onDismiss: () -> Unit,
    isUploading: MutableState<Boolean>,
    customerName: String,
    db: FirebaseFirestore,
    onUpdateSuccess: () -> Unit, // Callback to notify the parent about successful update
    coroutineScope: CoroutineScope // Pass the coroutine scope from the parent
) {
    var cashIn by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title
                        Text(
                            text = "Make a Cash Transaction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Cash In Field
                        OutlinedTextField(
                            value = cashIn,
                            onValueChange = { cashIn = it },
                            label = { Text("Cash In") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )

                        // Error Message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                modifier = Modifier.padding(vertical = 8.dp)
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
                            // Cancel Button
                            TextButton(
                                onClick = onDismiss,
                                enabled = !isUploading.value
                            ) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Confirm Button
                            Button(
                                onClick = {
                                    val cashInAmount = cashIn.toDoubleOrNull()
                                    if (cashInAmount == null || cashInAmount <= 0) {
                                        errorMessage = "Please enter a valid amount."
                                        return@Button
                                    }

                                    isUploading.value = true
                                    errorMessage = null

                                    // Launch a coroutine to handle Firestore operations
                                    coroutineScope.launch {
                                        try {
                                            // Fetch the current credit value
                                            val document = db.collection("Customers")
                                                .document("Details")
                                                .collection("Names")
                                                .document(customerName)
                                                .get()

                                            // Get the current date and time in the format "yyyy-MM-dd_HH:mm:ss"
                                            val currentDateTime = Clock.System.now()
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .toString()
                                                .replace("T", "_")
                                                .replace(":", "-")
                                                .substringBefore(".")

                                            // Reference to the Transactions collection
                                            val transactionsRef = db.collection("Transactions")
                                                .document(customerName)
                                                .collection("DateAndTime")
                                                .document(currentDateTime)

                                            // Create the "Transaction Details" collection
                                            transactionsRef.set(mapOf("Date" to currentDateTime))

                                            val transactionDetailsRef = transactionsRef.collection("Transaction Details")

                                            // Push Cash document
                                            transactionDetailsRef.document("Cash").set(mapOf("Amount" to cashInAmount))

                                            // Push Credit document
                                            transactionDetailsRef.document("Credit").set(mapOf("Amount" to ""))

                                            transactionDetailsRef.document("Cylinders Issued").set(emptyMap<String, String>())


                                            // Push Cylinders Returned document (empty for now)
                                            transactionDetailsRef.document("Cylinders Returned").set(mapOf("CylindersReturned" to emptyList<String>()))

                                            transactionDetailsRef.document("LPG Issued").set(mapOf("LPGIssued" to ""))

                                            transactionDetailsRef.document("Inventory Issued").set(mapOf("InventoryIssued" to ""))

                                            // Push the new credit value

                                            if (document.exists) {
                                                val details = document.get("Details") as? Map<String, String>
                                                val currentCredit = details?.get("Credit")?.toDoubleOrNull() ?: 0.0
                                                val updatedCredit: Double

                                                if (currentCredit < cashInAmount) {
                                                    updatedCredit = 0.0
                                                }
                                                else{
                                                    updatedCredit = currentCredit - cashInAmount
                                                }

                                                // Update the credit value

                                                val updatedDetails = details?.toMutableMap()?.apply {
                                                    put("Credit", updatedCredit.toString())
                                                }


                                                // Update Firestore
                                                db.collection("Customers")
                                                    .document("Details")
                                                    .collection("Names")
                                                    .document(customerName)
                                                    .update("Details" to updatedDetails)

                                                // Notify success
                                                isUploading.value = false
                                                onUpdateSuccess() // Notify parent about successful update
                                                onDismiss() // Close the dialog
                                            } else {
                                                isUploading.value = false
                                                errorMessage = "Customer details not found."
                                            }
                                        } catch (e: Exception) {
                                            isUploading.value = false
                                            errorMessage = "Failed to update credit: ${e.message}"
                                        }
                                    }
                                },
                                enabled = !isUploading.value,
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Confirm", color = Color.White)
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