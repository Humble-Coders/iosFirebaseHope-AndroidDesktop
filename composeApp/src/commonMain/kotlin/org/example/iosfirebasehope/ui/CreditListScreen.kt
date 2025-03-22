package org.example.iosfirebasehope.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.CreditListScreenComponent
import org.example.iosfirebasehope.navigation.events.CreditListScreenEvent

data class CustomerCredit(
    val name: String,
    val credit: Double,
    val phoneNumber: String? = null
)

fun Double.round(decimals: Int): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val rounded = kotlin.math.round(this * multiplier) / multiplier

    // Convert to string with proper decimal places
    val result = rounded.toString()
    val parts = result.split('.')

    return if (parts.size == 1) {
        // No decimal part, add zeros
        "$result.${"0".repeat(decimals)}"
    } else {
        // Has decimal part, pad with zeros if needed
        val decimalPart = parts[1].take(decimals).padEnd(decimals, '0')
        "${parts[0]}.$decimalPart"
    }
}

@Composable
fun CreditListScreen(
    component : CreditListScreenComponent,
    db: FirebaseFirestore,

) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State to hold the list of customers with credit
    var customers by remember { mutableStateOf<List<CustomerCredit>>(emptyList()) }

    // Loading state
    var isLoading by remember { mutableStateOf(true) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Error state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Filtered customers based on search
    val filteredCustomers by derivedStateOf {
        if (searchQuery.isEmpty()) {
            customers
        } else {
            customers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phoneNumber?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // Sort by credit amount (highest first)
    val sortedCustomers by derivedStateOf {
        filteredCustomers.sortedByDescending { it.credit }
    }

    // Calculate total credit
    val totalCredit by derivedStateOf {
        customers.sumOf { it.credit }
    }

    // Fetch customers with credit from Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        try {
            // Get reference to the collection
            val customersCollectionRef = db.collection("Customers")
                .document("Details")
                .collection("Names")

            // Get all customers
            val customersSnapshot = customersCollectionRef.get()

            // Process each customer to extract credit info
            val customersWithCredit = customersSnapshot.documents.mapNotNull { document ->
                val customerName = document.id
                val detailsMap = document.get("Details") as? Map<String, String>

                // Get credit value
                val creditString = detailsMap?.get("Credit") as? String
                val credit = creditString?.toDoubleOrNull() ?: 0.0

                // Get phone number if available
                val phoneNumber = detailsMap?.get("Phone Number") as? String

                // Only include customers with credit > 0
                if (credit > 0) {
                    CustomerCredit(customerName, credit, phoneNumber)
                } else {
                    null
                }
            }

            customers = customersWithCredit
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
                title = { Text("Credit List", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = {component.onEvent(CreditListScreenEvent.OnBackClick)}) {
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by customer name or phone") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF2f80eb)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF2f80eb),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Credit summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = 4.dp,
                backgroundColor = Color(0xFF2f80eb)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Outstanding Credit",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "₹ ${totalCredit.round(2)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "${customers.size} Customer${if (customers.size != 1) "s" else ""}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            // Customers list header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Credit Amount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(120.dp)
                )
            }

            if (isLoading) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2f80eb))
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
                        text = if (searchQuery.isEmpty()) {
                            "No customers with outstanding credit"
                        } else {
                            "No matching customers found"
                        },
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Customer list
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedCustomers) { customer ->
                        CustomerCreditItem(
                            customer = customer,
                            onClick = {  }
                        )
                        Divider(
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCreditItem(
    customer: CustomerCredit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Customer details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = customer.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (customer.phoneNumber != null) {
                Text(
                    text = customer.phoneNumber,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Credit amount
        Text(
            text = "₹ ${customer.credit.round(2)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF2f80eb),
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp)
        )

        // Arrow icon

    }
}