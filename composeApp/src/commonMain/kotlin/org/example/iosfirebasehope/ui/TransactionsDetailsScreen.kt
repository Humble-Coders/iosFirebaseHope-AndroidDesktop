package org.example.iosfirebasehope.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
                },
                elevation = 8.dp
            )
        }
    ) { innerPadding ->
        // View Bill Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Button(
                onClick = { component.onEvent(TransactionDetailsScreenEvent.OnBillClick(customerName,dateTime)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "View Bill",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (transactionDetails != null && cylinderDetails != null) {
            // Extract transaction details
            val cashAmount = transactionDetails!!["Cash"] as String
            val creditAmount = transactionDetails!!["Credit"] as String
            val cylindersIssued = transactionDetails!!["CylindersIssued"] as List<Map<String, String>>
            val cylindersReturned = transactionDetails!!["CylindersReturned"] as List<Map<String, String>>
            val lpgIssued = transactionDetails!!["LPGIssued"] as List<Map<String, String>>
            val inventoryIssued = transactionDetails!!["InventoryIssued"] as List<Map<String, String>>
            val price = transactionDetails!!["Total Price"] as String
            val cashOut = transactionDetails!!["Cash Out"] as String

            // Use LazyColumn as the root container for scrollable content
            LazyColumn(
                modifier = Modifier
                    .padding(top = 60.dp + innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr))
                    .fillMaxSize()
            ) {
                // Summary Cards
                item {
                    TransactionSummaryCard(
                        price = price,
                        cashAmount = cashAmount,
                        creditAmount = creditAmount,
                        cashOut = cashOut
                    )
                }

                // Transaction Type Cards
                item {
                    TransactionTypeCard(
                        title = "Cylinders Issued",
                        count = cylindersIssued.size,
                        onClick = { showCylindersIssuedPopup = true }
                    )
                }

                item {
                    TransactionTypeCard(
                        title = "Cylinders Returned",
                        count = cylindersReturned.size,
                        onClick = { showCylindersReturnedPopup = true }
                    )
                }

                item {
                    TransactionTypeCard(
                        title = "LPG Issued",
                        count = lpgIssued.size,
                        onClick = { showLPGIssuedPopup = true }
                    )
                }

                item {
                    TransactionTypeCard(
                        title = "Inventory Issued",
                        count = inventoryIssued.size,
                        onClick = { showInventoryIssuedPopup = true }
                    )
                }

                // Add some spacing at the bottom
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
            // Loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun TransactionSummaryCard(
    price: String,
    cashAmount: String,
    creditAmount: String,
    cashOut: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transaction Summary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2f80eb),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Total Price",
                    value = "Rs. $price",
                    color = Color(0xFF2E7D32)
                )

                SummaryItem(
                    title = "Credit",
                    value = "Rs. $creditAmount",
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Cash",
                    value = "Rs. $cashAmount",
                    color = Color(0xFF2E7D32)
                )

                SummaryItem(
                    title = "Cash Out",
                    value = "Rs. $cashOut",
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TransactionTypeCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0xFF2E7D32)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                // Title with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2f80eb)
                    )

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF2f80eb),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Close")
                    }
                }

                // Divider
                Divider(color = Color.LightGray, thickness = 1.dp)

                // Content
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items to display",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CylinderDetailItem(
    serialNumber: String,
    totalPrice: String,
    gasType: String,
    volumeType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Serial Number: $serialNumber",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gas: $gasType",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )

                Text(
                    text = "Volume: $volumeType",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Price: Rs. $totalPrice",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
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
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Volume: $volumeType",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quantity: $quantity",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )

                Text(
                    text = "Date: $date",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Price: Rs. $price",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
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
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Name: $name",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quantity: $quantity",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )

                Text(
                    text = "Price: Rs. $price",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

// These two functions remain unchanged
private suspend fun fetchCylinderDetails(db: FirebaseFirestore): List<Map<String, String>> {
    val cylinderDoc = db.collection("Cylinders").document("Cylinders").get()
    return cylinderDoc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
}

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
    val priceDoc = transactionDetailsCollection.document("Total Price").get()
    val CashOutDoc = transactionDetailsCollection.document("Cash Out").get()

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