package org.example.iosfirebasehope.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.TransactionVendorScreenComponent
import org.example.iosfirebasehope.navigation.events.TransactionVendorScreenEvent


@Composable
fun TransactionVendorScreenUI(
    vendorName: String,
    component: TransactionVendorScreenComponent,
    db: FirebaseFirestore // Callback for transaction click
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State to hold transactions grouped by date and time
    var transactions by remember { mutableStateOf<Map<String, Map<String, Any>>?>(null) }

    // Fetch transactions on launch
    LaunchedEffect(vendorName) {
        try {
            // Call the suspend function to fetch transactions
            val fetchedTransactions = fetchTransactions(db, vendorName)
            transactions = fetchedTransactions
        } catch (e: Exception) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Failed to fetch transactions: ${e.message}")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Transactions for $vendorName", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(TransactionVendorScreenEvent.OnBackClick) }) {
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
            if (transactions != null) {
                // Display transactions grouped by date and time
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(transactions!!.entries.toList()) { (dateTime, transactionDetails) ->
                        // Split date and time
                        val (date, time) = dateTime.split("_")

                        // Calculate counts
                        val cylindersIssuedCount = countSerialNumbers(transactionDetails["CylindersIssued"] as List<Map<String, String>>)
                        val cylindersReturnedCount = countSerialNumbers(transactionDetails["CylindersReturned"] as List<Map<String, String>>)
                        val lpgIssuedCount = sumQuantities(transactionDetails["LPGIssued"] as List<Map<String, String>>)
                        val inventoryIssuedCount = sumQuantities(transactionDetails["InventoryIssued"] as List<Map<String, String>>)

                        TransactionCard(
                            date = date,
                            time = time,
                            cashAmount = transactionDetails["Cash"] as String,
                            creditAmount = transactionDetails["Credit"] as String,
                            cylindersIssuedCount = cylindersIssuedCount,
                            cylindersReturnedCount = cylindersReturnedCount,
                            lpgIssuedCount = lpgIssuedCount,
                            inventoryIssuedCount = inventoryIssuedCount,
                            onClick = {
                                // Handle transaction click
                                component.onEvent(TransactionVendorScreenEvent.OnTransactionClick(vendorName, dateTime))
                            }
                        )
                    }
                }
            } else {
                // Show loading or error message
                Text(
                    text = "Loading transactions...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// Function to count serial numbers in a list of cylinders
private fun countSerialNumbers(cylinders: List<Map<String, String>>): Int {
    var count = 0
    for (cylinder in cylinders) {
        if (cylinder["Serial Number"] != null) {
            count++
        }
    }
    return count
}

// Function to sum quantities in a list of items
private fun sumQuantities(items: List<Map<String, String>>): Int {
    var sum = 0
    for (item in items) {
        val quantity = item["Quantity"]?.toIntOrNull() ?: 0
        sum += quantity
    }
    return sum
}

@Composable
private fun TransactionCard(
    date: String,
    time: String,
    cashAmount: String,
    creditAmount: String,
    cylindersIssuedCount: Int,
    cylindersReturnedCount: Int,
    lpgIssuedCount: Int,
    inventoryIssuedCount: Int,
    onClick: () -> Unit // Callback for card click
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }, // Make the card clickable
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date and Time Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date on the left
                Text(
                    text = date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Time on the right
                Text(
                    text = time,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Divider
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Cash and Credit Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cash
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cash:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "Rs. $cashAmount",
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Credit
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Credit:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "Rs. $creditAmount",
                        fontSize = 16.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // Cylinders Issued and Cylinders Returned Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cylinders Issued
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Issued:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$cylindersIssuedCount",
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Cylinders Returned
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Returned:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$cylindersReturnedCount",
                        fontSize = 16.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // LPG Issued and Inventory Issued Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LPG Issued
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LPG Issued:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$lpgIssuedCount",
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Inventory Issued
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventory Issued:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$inventoryIssuedCount",
                        fontSize = 16.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

// Suspend function to fetch all transactions (unchanged)
private suspend fun fetchTransactions(
    db: FirebaseFirestore,
    vendorName: String
): Map<String, Map<String, Any>> {
    val transactionMap = mutableMapOf<String, Map<String, Any>>()

    // Fetch the `DateAndTime` collection for the given vendor
    val transactionsSnapshot = db.collection("TransactionVendor")
        .document(vendorName)
        .collection("DateAndTime")
        .get()

    val transactionDocuments = transactionsSnapshot.documents
    if (transactionDocuments.isEmpty()) {
        throw Exception("No transactions found for this vendor.")
    }

    // Iterate through each `DateAndTime` document
    for (dateTimeDoc in transactionsSnapshot.documents) {
        val dateTime = dateTimeDoc.id // The document ID as the date and time
        val transactionDetailsCollection = dateTimeDoc.reference.collection("Transaction Details")

        // Fetch transaction detail documents
        val cashDoc = transactionDetailsCollection.document("Cash").get()
        val creditDoc = transactionDetailsCollection.document("Credit").get()
        val cylindersIssuedDoc = transactionDetailsCollection.document("Cylinders Issued").get()
        val cylindersReturnedDoc = transactionDetailsCollection.document("Cylinders Returned").get()
        val lpgIssuedDoc = transactionDetailsCollection.document("LPG Issued").get()
        val inventoryIssuedDoc = transactionDetailsCollection.document("Inventory Issued").get()

        // Extract transaction details
        val cashAmount = cashDoc.get("Amount") as? String ?: "0"
        val creditAmount = creditDoc.get("Amount") as? String ?: "0"
        val cylindersIssued = cylindersIssuedDoc.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
        val cylindersReturned = cylindersReturnedDoc.get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
        val lpgIssued = lpgIssuedDoc.get("LPGIssued") as? List<Map<String, String>> ?: emptyList()
        val inventoryIssued = inventoryIssuedDoc.get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()

        // Store the transaction details for the current date and time
        transactionMap[dateTime] = mapOf(
            "Cash" to cashAmount,
            "Credit" to creditAmount,
            "CylindersIssued" to cylindersIssued,
            "CylindersReturned" to cylindersReturned,
            "LPGIssued" to lpgIssued,
            "InventoryIssued" to inventoryIssued
        )
    }

    return transactionMap
}