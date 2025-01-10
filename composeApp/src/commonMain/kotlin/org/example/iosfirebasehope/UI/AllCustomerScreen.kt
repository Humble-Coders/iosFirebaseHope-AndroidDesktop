package org.example.iosfirebasehope.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.components.AllCustomersScreenComponent
import org.example.iosfirebasehope.navigation.events.AllCustomersScreenEvent

@Composable
fun AllCustomersScreenUI(
    component: AllCustomersScreenComponent,
    db: FirebaseFirestore,
    cylinderDetails: List<Map<String, String>>
) {
    println("AllCustomersScreenUI : ${cylinderDetails}")
    var searchQuery by remember { mutableStateOf("") }
    var customerDetailsList by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var filteredCustomers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // Fetch customer details from Firestore
    LaunchedEffect(Unit) {
        try {
            val document = db.collection("Customers").document("Names").get()
            val customerDetails = document.get("CustomerDetails") as? List<Map<String, String>>
            customerDetailsList = customerDetails ?: emptyList()
            filteredCustomers = customerDetailsList
        } catch (e: Exception) {
            println("Error fetching customer details: ${e.message}")
        }
    }

    // Filter customers based on the search query
    LaunchedEffect(searchQuery) {
        filteredCustomers = if (searchQuery.isEmpty()) {
            customerDetailsList
        } else {
            customerDetailsList.filter { it["Name"]?.contains(searchQuery, true) == true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Customers") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(AllCustomersScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear Text"
                                )
                            }
                        }
                    },
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search by Name")
                    },
                    modifier = Modifier
                        .width(360.dp)
                        .padding(end = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF2f80eb),
                        unfocusedBorderColor = Color(0xFF2f80eb)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
            }

            // Display filtered list of customers
            CustomerList(customerDetailsList = filteredCustomers, modifier = Modifier.padding(innerPadding), component = component,cylinderDetails)
        }
    }
}

@Composable
fun CustomerList(
    customerDetailsList: List<Map<String, String>>,
    modifier: Modifier = Modifier,
    component: AllCustomersScreenComponent,
    cylinderDetail:List<Map<String,String>>
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(customerDetailsList) { customer ->
            CustomerDetailsCard(customer, component = component, cylinderDetail = cylinderDetail)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CustomerDetailsCard(
    customerDetails: Map<String, String>,
    component: AllCustomersScreenComponent,
    cylinderDetail: List<Map<String,String>>
) {
    val customerName = customerDetails["Name"] ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = { component.onEvent(AllCustomersScreenEvent.OnCustomerClick(customerName,cylinderDetail)) }),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb)) // Custom border color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Name: $customerName",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Phone Number: ${customerDetails["Phone Number"] ?: ""}",
                fontSize = 16.sp
            )
        }
    }
}