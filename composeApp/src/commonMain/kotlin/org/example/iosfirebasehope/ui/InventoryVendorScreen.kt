package org.example.iosfirebasehope.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.components.InventoryVendorsScreenComponent
import org.example.iosfirebasehope.navigation.events.InventoryVendorsScreenEvent

@Composable
fun InventoryVendorsScreenUI(
    component: InventoryVendorsScreenComponent,
    db: FirebaseFirestore
) {
    var searchQuery by remember { mutableStateOf("") }
    var vendorDetailsList by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var filteredVendors by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch vendor details from Firestore
    LaunchedEffect(Unit) {
        try {
            val document = db.collection("InventoryVendors").document("Names").get()
            val vendorDetails = document.get<List<Map<String, String>>>("VendorDetails")
            vendorDetailsList = vendorDetails ?: emptyList()
            filteredVendors = vendorDetailsList
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error fetching vendors: ${e.message}"
            isLoading = false
            println("Error fetching inventory vendor details: ${e.message}")
        }
    }

    // Filter vendors based on the search query
    LaunchedEffect(searchQuery, vendorDetailsList) {
        filteredVendors = if (searchQuery.isEmpty()) {
            vendorDetailsList
        } else {
            vendorDetailsList.filter { vendor ->
                vendor["Name"]?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Vendors") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(InventoryVendorsScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
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
                        Text("Search by Vendor Name")
                    },
                    modifier = Modifier
                        .width(360.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF2f80eb),
                        unfocusedBorderColor = Color(0xFF2f80eb)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
                )
            }

            // Display loading, error or vendor list
            when {
                isLoading -> {
                    Text(
                        text = "Loading vendors...",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red
                    )
                }
                filteredVendors.isEmpty() -> {
                    Text(
                        text = if (searchQuery.isEmpty()) "No vendors found" else "No vendors match your search",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
                else -> {
                    InventoryVendorList(
                        vendorDetailsList = filteredVendors,
                        onVendorClick = { vendor ->
                            component.onEvent(InventoryVendorsScreenEvent.OnVendorClick(vendor))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryVendorList(
    vendorDetailsList: List<Map<String, String>>,
    onVendorClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(vendorDetailsList) { vendor ->
            InventoryVendorCard(vendor = vendor, onVendorClick = onVendorClick)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun InventoryVendorCard(
    vendor: Map<String, String>,
    onVendorClick: (String) -> Unit
) {
    val vendorName = vendor["Name"] ?: ""
    val phoneNumber = vendor["Phone Number"] ?: ""
    val credit = vendor["Credit"] ?: "0"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onVendorClick(vendorName) },
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vendorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2f80eb)
                )

                if (credit.toFloatOrNull() ?: 0f > 0) {
                    Text(
                        text = "Credit: ₹$credit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Phone: $phoneNumber",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }
    }
}

