package org.example.iosfirebasehope.UI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.CustomerDetailsScreenComponent
import org.example.iosfirebasehope.navigation.events.CustomerDetailsScreenEvent

@Composable
fun CustomerDetailsScreenUI(
    customerName: String, // Customer name passed as a parameter
    component: CustomerDetailsScreenComponent,
    cylinderDetail: List<Map<String, String>>, // List of cylinder details passed as a parameter
    db: FirebaseFirestore // FirebaseFirestore instance for data fetching
) {
    println("CustomerDetailsScreenUI : $cylinderDetail")
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State for customer details
    var customerDetails by remember { mutableStateOf<Map<String, String>?>(null) }

    // State for issued cylinders
    var issuedCylinders by remember { mutableStateOf<List<String>?>(null) }

    // State for search functionality
    var isSearchActive by remember { mutableStateOf(false) } // State to toggle between text and search bar
    var searchQuery by remember { mutableStateOf("") } // State to hold the search query

    // Fetch customer details and issued cylinders on launch
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
        } catch (e: Exception) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Failed to fetch data: ${e.message}")
            }
        }
    }

    // Filter issued cylinders based on search query
    val filteredIssuedCylinders by derivedStateOf {
        if (searchQuery.isEmpty()) {
            issuedCylinders
        } else {
            issuedCylinders?.filter { serialNumber ->
                serialNumber.contains(searchQuery, ignoreCase = true)
            }
        }
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
            // Add a "Transactions" button at the bottom of the screen
            Button(
                onClick = { //component.onEvent(CustomerDetailsScreenEvent.OnTransactionsClick)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text(
                    text = "View All Transactions",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (customerDetails != null) {
                // Display customer details in a card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Customer Name
                        Text(
                            text = customerName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Divider
                        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        // Address
                        DetailRow(label = "Address", value = customerDetails!!["Address"] ?: "Not Available")

                        // Deposit
                        DetailRow(label = "Deposit", value = customerDetails!!["Deposit"] ?: "Not Available")

                        // Phone Number
                        DetailRow(label = "Phone Number", value = customerDetails!!["Phone Number"] ?: "Not Available")

                        // Reference Mobile
                        DetailRow(label = "Reference Mobile", value = customerDetails!!["Reference Mobile"] ?: "Not Available")

                        // Reference Name
                        DetailRow(label = "Reference Name", value = customerDetails!!["Reference Name"] ?: "Not Available")

                        // UID
                        DetailRow(label = "UID", value = customerDetails!!["UID"] ?: "Not Available")
                    }
                }
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

            // Search Header for Issued Cylinders
            SearchHeader(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchActiveChange = { isSearchActive = it }
            )

            // Display issued cylinders in a LazyColumn
            if (filteredIssuedCylinders != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredIssuedCylinders!!) { serialNumber ->
                        // Find the cylinder details from the passed list
                        val cylinder = cylinderDetail.find { it["Serial Number"] == serialNumber }

                        if (cylinder != null) {
                            IssuedCylinderItem(
                                serialNumber = serialNumber,
                                volumeType = cylinder["Volume Type"] ?: "Not Available",
                                gasType = cylinder["Gas Type"] ?: "Not Available"
                            )
                        }
                    }
                }
            } else {
                // Show loading or error message
                Text(
                    text = "Loading issued cylinders...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
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
                        .padding(horizontal = 16.dp) // Horizontal padding of 16.dp
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

@Composable
private fun IssuedCylinderItem(serialNumber: String, volumeType: String, gasType: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp), // Proper padding for the card
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Set the card color to 0xFFE8F5E9
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Serial Number
            Text(
                text = "Serial Number: $serialNumber",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Volume Type
            Text(
                text = "Volume Type: $volumeType",
                fontSize = 14.sp,
                color = Color.Gray
            )

            // Gas Type
            Text(
                text = "Gas Type: $gasType",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}