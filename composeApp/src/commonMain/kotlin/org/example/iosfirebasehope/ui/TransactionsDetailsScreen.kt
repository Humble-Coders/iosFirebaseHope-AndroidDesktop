package org.example.iosfirebasehope.ui

import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.TransactionDetailsScreenComponent
import org.example.iosfirebasehope.navigation.events.TransactionDetailsScreenEvent

@Composable
fun TransactionDetailsScreen(
    customerName: String,
    dateTime: String,
    component: TransactionDetailsScreenComponent,
    db: FirebaseFirestore
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State to hold transaction details
    var transactionDetails by remember { mutableStateOf<Map<String, Any>?>(null) }

    // State to hold cylinder details
    var cylinderDetails by remember { mutableStateOf<List<Map<String, String>>?>(null) }

    // State to manage pop-up visibility
    var showCylindersIssuedPopup by remember { mutableStateOf(false) }
    var showCylindersReturnedPopup by remember { mutableStateOf(false) }
    var showLPGIssuedPopup by remember { mutableStateOf(false) }
    var showInventoryIssuedPopup by remember { mutableStateOf(false) }

    // Fetch transaction details and cylinder details on launch
    LaunchedEffect(customerName, dateTime) {
        try {
            // Fetch transaction details
            val fetchedDetails = fetchCustomerTransactionDetails(db, customerName, dateTime)
            transactionDetails = fetchedDetails

            // Fetch cylinder details
            val fetchedCylinderDetails = fetchCylinderDetails(db)
            cylinderDetails = fetchedCylinderDetails
        } catch (e: Exception) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Failed to fetch details: ${e.message}")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(TransactionDetailsScreenEvent.OnBackClick) }) {
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
                .fillMaxSize()
        ) {
            if (transactionDetails != null && cylinderDetails != null) {
                // Display transaction details
                val cashAmount = transactionDetails!!["Cash"] as String
                val creditAmount = transactionDetails!!["Credit"] as String
                val cylindersIssued = transactionDetails!!["CylindersIssued"] as List<Map<String, String>>
                val cylindersReturned = transactionDetails!!["CylindersReturned"] as List<Map<String, String>>
                val lpgIssued = transactionDetails!!["LPGIssued"] as List<Map<String, String>>
                val inventoryIssued = transactionDetails!!["InventoryIssued"] as List<Map<String, String>>
                val price=transactionDetails!!["Total Price"] as String
                val CashOut=transactionDetails!!["Cash Out"] as String

                // Cash and Credit Row

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Price: Rs. $price",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Text(
                            text = "Credit: Rs. $creditAmount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cash: Rs. $cashAmount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Text(
                            text = "Cash Out: Rs. $CashOut",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }

                }

                // Cylinders Issued Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showCylindersIssuedPopup = true },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xFFE8F5E9)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Cylinders Issued",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        cylindersIssued.forEach { cylinder ->
                            val serialNumber = cylinder["Serial Number"] ?: "N/A"
                            val cylinderInfo = cylinderDetails!!.find { it["Serial Number"] == serialNumber }
                            val gasType = cylinderInfo?.get("Gas Type") ?: "N/A"
                            val volumeType = cylinderInfo?.get("Volume Type") ?: "N/A"
                            Text(
                                text = "Serial: $serialNumber, Gas: $gasType, Volume: $volumeType",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Cylinders Returned Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showCylindersReturnedPopup = true },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xFFE8F5E9)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Cylinders Returned",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        cylindersReturned.forEach { cylinder ->
                            val serialNumber = cylinder["Serial Number"] ?: "N/A"
                            val cylinderInfo = cylinderDetails!!.find { it["Serial Number"] == serialNumber }
                            val gasType = cylinderInfo?.get("Gas Type") ?: "N/A"
                            val volumeType = cylinderInfo?.get("Volume Type") ?: "N/A"
                            Text(
                                text = "Serial: $serialNumber, Gas: $gasType, Volume: $volumeType",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // LPG Issued Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showLPGIssuedPopup = true },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xFFE8F5E9)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "LPG Issued",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        lpgIssued.forEach { lpg ->
                            Text(
                                text = "Quantity: ${lpg["Quantity"] ?: "N/A"}, Volume Type: ${lpg["Volume Type"] ?: "N/A"}",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Inventory Issued Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showInventoryIssuedPopup = true },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xFFE8F5E9)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Inventory Issued",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        inventoryIssued.forEach { inventory ->
                            Text(
                                text = "Name: ${inventory["Name"] ?: "N/A"}, Quantity: ${inventory["Quantity"] ?: "N/A"}",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Show Pop-ups based on state
                if (showCylindersIssuedPopup) {
                    TransactionDetailsPopup(
                        title = "Cylinders Issued",
                        items = cylindersIssued,
                        cylinderDetails = cylinderDetails!!,
                        onDismiss = { showCylindersIssuedPopup = false }
                    )
                }

                if (showCylindersReturnedPopup) {
                    TransactionDetailsPopup(
                        title = "Cylinders Returned",
                        items = cylindersReturned,
                        cylinderDetails = cylinderDetails!!,
                        onDismiss = { showCylindersReturnedPopup = false }
                    )
                }

                if (showLPGIssuedPopup) {
                    TransactionDetailsPopup(
                        title = "LPG Issued",
                        items = lpgIssued,
                        cylinderDetails = emptyList(), // Not needed for LPG
                        onDismiss = { showLPGIssuedPopup = false }
                    )
                }

                if (showInventoryIssuedPopup) {
                    TransactionDetailsPopup(
                        title = "Inventory Issued",
                        items = inventoryIssued,
                        cylinderDetails = emptyList(), // Not needed for Inventory
                        onDismiss = { showInventoryIssuedPopup = false }
                    )
                }
            } else {
                // Show loading or error message
                Text(
                    text = "Loading transaction details...",
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
private fun TransactionDetailsPopup(
    title: String,
    items: List<Map<String, String>>,
    cylinderDetails: List<Map<String, String>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(items) { item ->
                    when (title) {
                        "Cylinders Issued", "Cylinders Returned" -> {
                            val serialNumber = item["Serial Number"] ?: "N/A"
                            val cylinderInfo = cylinderDetails.find { it["Serial Number"] == serialNumber }
                            val gasType = cylinderInfo?.get("Gas Type") ?: "N/A"
                            val volumeType = cylinderInfo?.get("Volume Type") ?: "N/A"
                            CylinderDetailItem(
                                serialNumber = serialNumber,
                                totalPrice = item["Price"] ?: "N/A",
                                gasType = gasType,
                                volumeType = volumeType
                            )
                        }
                        "LPG Issued" -> {
                            LPGDetailItem(
                                quantity = item["Quantity"] ?: "N/A",
                                price = item["Price"] ?: "N/A",
                                date = item["Date"] ?: "N/A",
                                volumeType = item["Volume Type"] ?: "N/A"
                            )
                        }
                        "Inventory Issued" -> {
                            InventoryDetailItem(
                                name = item["Name"] ?: "N/A",
                                price = item["Price"] ?: "N/A",
                                quantity = item["Quantity"] ?: "N/A"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Fetch cylinder details from the Cylinders collection
private suspend fun fetchCylinderDetails(db: FirebaseFirestore): List<Map<String, String>> {
    val cylinderDoc = db.collection("Cylinders").document("Cylinders").get()
    return cylinderDoc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
}

// Fetch transaction details (unchanged)
private suspend fun fetchCustomerTransactionDetails(
    db: FirebaseFirestore,
    customerName: String,
    dateTime: String
): Map<String, Any> {
    val transactionDetailsCollection = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .document(dateTime)
        .collection("Transaction Details")

    // Fetch transaction detail documents
    val cashDoc = transactionDetailsCollection.document("Cash").get()
    val creditDoc = transactionDetailsCollection.document("Credit").get()
    val cylindersIssuedDoc = transactionDetailsCollection.document("Cylinders Issued").get()
    val cylindersReturnedDoc = transactionDetailsCollection.document("Cylinders Returned").get()
    val lpgIssuedDoc = transactionDetailsCollection.document("LPG Issued").get()
    val inventoryIssuedDoc = transactionDetailsCollection.document("Inventory Issued").get()
    val priceDoc=transactionDetailsCollection.document("Total Price").get()
    val CashOutDoc=transactionDetailsCollection.document("Cash Out").get()

    // Extract transaction details
    val cashAmount = cashDoc.get("Amount") as? String ?: "0"
    val price = priceDoc.get("Amount") as? String ?: "0"
    val CashOut = CashOutDoc.get("Amount") as? String ?: "0"
    val creditAmount = creditDoc.get("Amount") as? String ?: "0"
    val cylindersIssued = cylindersIssuedDoc.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
    val cylindersReturned = cylindersReturnedDoc.get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
    val lpgIssued = lpgIssuedDoc.get("LPGIssued") as? List<Map<String, String>> ?: emptyList()
    val inventoryIssued = inventoryIssuedDoc.get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()

    return mapOf(
        "Cash" to cashAmount,
        "Credit" to creditAmount,
        "CylindersIssued" to cylindersIssued,
        "CylindersReturned" to cylindersReturned,
        "LPGIssued" to lpgIssued,
        "InventoryIssued" to inventoryIssued,
        "Total Price" to price,
        "Cash Out" to CashOut
    )
}

// Composable functions for CylinderDetailItem, LPGDetailItem, and InventoryDetailItem remain unchanged

@Composable
private fun CylinderDetailItem(
    serialNumber: String,
    totalPrice: String,
    gasType: String,
    volumeType: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Serial Number
            Text(
                text = "Serial Number: $serialNumber",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Total Price
            Text(
                text = "Total Price: Rs. $totalPrice",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Gas Type
            Text(
                text = "Gas Type: $gasType",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Volume Type
            Text(
                text = "Volume Type: $volumeType",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )
        }
    }
}

@Composable
private fun LPGDetailItem(
    quantity: String,
    price: String,
    date: String,
    volumeType: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Quantity
            Text(
                text = "Quantity: $quantity",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Price
            Text(
                text = "Price: Rs. $price",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Date
            Text(
                text = "Date: $date",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Volume Type
            Text(
                text = "Volume Type: $volumeType",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )
        }
    }
}

@Composable
private fun InventoryDetailItem(
    name: String,
    price: String,
    quantity: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Name
            Text(
                text = "Name: $name",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Price
            Text(
                text = "Price: Rs. $price",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )

            // Quantity
            Text(
                text = "Quantity: $quantity",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32) // Dark green color
            )
        }
    }
}