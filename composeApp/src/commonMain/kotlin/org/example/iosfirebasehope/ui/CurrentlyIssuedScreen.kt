package org.example.iosfirebasehope.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.CurrentlyIssuedScreenComponent
import org.example.iosfirebasehope.navigation.events.CurrentlyIssuedScreenEvent

// Data class to hold customer cylinder information
data class CustomerCylinders(
    val customerName: String,
    val cylinderSummary: List<CylinderGroup>,
    val totalCylinders: Int
)

data class CylinderGroup(
    val gasType: String,
    val volumeType: String,
    val quantity: Int
)

@Composable
fun CurrentlyIssuedScreenUI(
    component: CurrentlyIssuedScreenComponent,
    db: FirebaseFirestore,
    cylinderDetails: List<Map<String, String>>
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // Company name
    val COMPANY_NAME = "Gobind Traders"

    // State to hold all customers' cylinder data
    var customersCylinders by remember { mutableStateOf<List<CustomerCylinders>>(emptyList()) }

    // Loading state
    var isLoading by remember { mutableStateOf(true) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Error state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Filtered customers based on search
    val filteredCustomers by derivedStateOf {
        if (searchQuery.isEmpty()) {
            customersCylinders
        } else {
            customersCylinders.filter {
                it.customerName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Sort by total cylinders count (highest first)
    val sortedCustomers by derivedStateOf {
        filteredCustomers.sortedByDescending { it.totalCylinders }
    }

    // Calculate total cylinders across all customers
    val totalCylindersCount by derivedStateOf {
        customersCylinders.sumOf { it.totalCylinders }
    }

    // Optimized data fetching from Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        try {
            // OPTIMIZATION 1: Fetch all issued cylinders data in a single batch
            val allIssuedCylindersRef = db.collection("Customers")
                .document("Issued Cylinders")
                .collection("Names")
                .get()

            val allIssuedCylindersData = allIssuedCylindersRef.documents.associate { doc ->
                val customerName = doc.id
                val issuedCylinders = doc.get("Details") as? List<String> ?: emptyList()
                customerName to issuedCylinders
            }

            // Process all customers with non-empty cylinder lists
            val allCustomerCylinders = allIssuedCylindersData
                .filter { (_, cylinders) -> cylinders.isNotEmpty() }
                .map { (customerName, issuedCylinders) ->
                    // Group cylinders by gas type and volume type
                    val groupedCylinders = issuedCylinders.groupBy { serialNumber ->
                        val cylinder = cylinderDetails.find { it["Serial Number"] == serialNumber }
                        Pair(
                            cylinder?.get("Gas Type") ?: "Unknown",
                            cylinder?.get("Volume Type") ?: "Unknown"
                        )
                    }

                    // Convert to our data structure
                    val cylinderGroups = groupedCylinders.map { (groupKey, cylinders) ->
                        CylinderGroup(
                            gasType = groupKey.first,
                            volumeType = groupKey.second,
                            quantity = cylinders.size
                        )
                    }

                    CustomerCylinders(
                        customerName = customerName,
                        cylinderSummary = cylinderGroups,
                        totalCylinders = issuedCylinders.size
                    )
                }

            customersCylinders = allCustomerCylinders
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Failed to load customers: ${e.message}"
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = errorMessage ?: "An unknown error occurred"
                )
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("All Customers Cylinders", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(CurrentlyIssuedScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Company header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                backgroundColor = Color(0xFF2f80eb),
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = COMPANY_NAME,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Currently Issued Cylinders",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$totalCylindersCount",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Total Cylinders with ${customersCylinders.size} Customers",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by customer name") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF2f80eb)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Clear",
                                tint = Color(0xFF2f80eb)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF2f80eb),
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (isLoading) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2f80eb))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading customer data...",
                            color = Color.Gray
                        )
                    }
                }
            } else if (errorMessage != null) {
                // Error message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (sortedCustomers.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No customers have issued cylinders" else "No matching customers found",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Customers list
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedCustomers) { customer ->
                        CustomerCylindersCard(
                            customer = customer,
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCylindersCard(
    customer: CustomerCylinders,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Customer name and total cylinders
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = customer.customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2f80eb), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${customer.totalCylinders}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Divider(
                color = Color.LightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Cylinder groups
            customer.cylinderSummary.forEach { cylinderGroup ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gas type and volume info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = cylinderGroup.gasType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = cylinderGroup.volumeType,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    // Quantity in circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${cylinderGroup.quantity}",
                            color = Color(0xFF2f80eb),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (cylinderGroup != customer.cylinderSummary.last()) {
                    Divider(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(start = 8.dp, end = 8.dp)
                    )
                }
            }

            // View details button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF2f80eb)
                    )
                ) {
                    Text("View Details")
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "View Details",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}