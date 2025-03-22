package org.example.iosfirebasehope.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.TransactionScreenComponent
import org.example.iosfirebasehope.navigation.events.TransactionScreenEvent


@Composable
fun TransactionScreenUI(
    customerName: String,
    component: TransactionScreenComponent,
    db: FirebaseFirestore // Callback for transaction click
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State to hold transactions grouped by date and time
    var transactions by remember { mutableStateOf<Map<String, Map<String, Any>>?>(null) }

    // Filter state
    var filterType by remember { mutableStateOf("None") }
    var dateFilterExpanded by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    // Get unique dates for date filter
    val allDates = transactions?.entries?.map { entry ->
        val (date, _) = entry.key.split("_")
        val dateList = date.split("-")
        "${dateList[2]}-${dateList[1]}-${dateList[0]}"
    }?.distinct()?.sorted() ?: emptyList()

    // Fetch transactions on launch
    LaunchedEffect(customerName) {
        try {
            // Call the suspend function to fetch transactions
            val fetchedTransactions = fetchTransactions(db, customerName)
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
                title = { Text("Transactions for $customerName", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(TransactionScreenEvent.OnBackClick) }) {
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
            // Filter dropdowns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Primary filter dropdown
                Box {
                    Button(
                        onClick = { filterExpanded = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(filterType, color = Color.White)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            filterType = "None"
                            selectedDate = ""
                            filterExpanded = false
                        }) {
                            Text("None")
                        }
                        DropdownMenuItem(onClick = {
                            filterType = "Date"
                            filterExpanded = false
                            dateFilterExpanded = true
                        }) {
                            Text("Date")
                        }
                        DropdownMenuItem(onClick = {
                            filterType = "Credit"
                            selectedDate = ""
                            filterExpanded = false
                        }) {
                            Text("Credit")
                        }
                    }
                }

                // Date filter dropdown (shows only when Date filter is selected)
                if (filterType == "Date") {
                    Box {
                        Button(
                            onClick = { dateFilterExpanded = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Text(if (selectedDate.isEmpty()) "Select Date" else selectedDate, color = Color.White)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = dateFilterExpanded,
                            onDismissRequest = { dateFilterExpanded = false }
                        ) {
                            allDates.forEach { date ->
                                DropdownMenuItem(onClick = {
                                    selectedDate = date
                                    dateFilterExpanded = false
                                }) {
                                    Text(date)
                                }
                            }
                        }
                    }
                }
            }

            if (transactions != null) {
                // Apply filters
                val filteredTransactions = when (filterType) {
                    "Date" -> {
                        if (selectedDate.isEmpty()) {
                            transactions
                        } else {
                            // Convert selected date back to original format for filtering
                            val dateParts = selectedDate.split("-")
                            val originalFormat = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
                            transactions?.filter { entry ->
                                entry.key.startsWith(originalFormat)
                            }
                        }
                    }
                    "Credit" -> {
                        transactions?.filter { (_, details) ->
                            val creditAmount = details["Credit"] as String
                            creditAmount != "0"
                        }
                    }
                    else -> transactions
                }

                // Display filtered transactions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredTransactions!!.entries.sortedByDescending { it.key }) { (dateTime, transactionDetails) ->
                        // Split date and time
                        val (date, time) = dateTime.split("_")
                        // Format date
                        val dateList = date.split("-")
                        val newDate = "${dateList[2]}-${dateList[1]}-${dateList[0]}"

                        // Calculate counts
                        val cylindersIssuedCount = countSerialNumbers(transactionDetails["CylindersIssued"] as List<Map<String, String>>)
                        val cylindersReturnedCount = countSerialNumbers(transactionDetails["CylindersReturned"] as List<Map<String, String>>)
                        val lpgIssuedCount = sumQuantities(transactionDetails["LPGIssued"] as List<Map<String, String>>)
                        val lpgReturnedCount = sumQuantitiesInt(transactionDetails["LPGReturned"] as? Map<String, Int> ?: emptyMap())
                        val inventoryIssuedCount = sumQuantities(transactionDetails["InventoryIssued"] as List<Map<String, String>>)
                        val deliveryAmount = transactionDetails["Delivery"] as? String ?: "0"

                        TransactionCard(
                            date = newDate,
                            price = transactionDetails["Total Price"] as String,
                            cashAmount = transactionDetails["Cash"] as String,
                            creditAmount = transactionDetails["Credit"] as String,
                            deliveryAmount = deliveryAmount,
                            cylindersIssuedCount = cylindersIssuedCount,
                            cylindersReturnedCount = cylindersReturnedCount,
                            lpgIssuedCount = lpgIssuedCount,
                            lpgReturnedCount = lpgReturnedCount,
                            inventoryIssuedCount = inventoryIssuedCount,
                            cashOut = transactionDetails["Cash Out"] as String,
                            onClick = {
                                // Handle transaction click
                                component.onEvent(TransactionScreenEvent.OnTransactionClick(customerName, dateTime))
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

private fun sumQuantitiesInt(items: Map<String, Int>): Int {
    var sum = 0
    for (item in items) {
        // add the values for each key and store in sum
        sum += item.value
    }
    return sum
}

@Composable
private fun TransactionCard(
    date: String,
    price: String,
    cashAmount: String,
    creditAmount: String,
    deliveryAmount: String,
    cylindersIssuedCount: Int,
    cylindersReturnedCount: Int,
    lpgIssuedCount: Int,
    lpgReturnedCount: Int,
    inventoryIssuedCount: Int,
    onClick: () -> Unit, // Callback for card click
    cashOut: String
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
            // Date and Total Price Row
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

                // Price on the right
                Text(
                    text = "Price: Rs. $price",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Divider
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Main content - 4 fields on left, 4 fields on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column - 4 fields
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Cash
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cash:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = "Rs. $cashAmount",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    // 2. Cylinders Issued
                    if (cashOut == "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Inventory:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "$inventoryIssuedCount",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }

                    }

                    // 3. LPG Issued
                    if (cashOut == "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cyls Issued:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "$cylindersIssuedCount",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }

                    }

                    // 4. Inventory
                    if (cashOut == "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LPG Issued:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "$lpgIssuedCount",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    // Cash Out (only shown if cashOut is not "0")
                    if (cashOut != "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cash Out:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "Rs. $cashOut",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                // Right column - 4 fields
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Credit
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Credit:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "Rs. $creditAmount",
                            fontSize = 14.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }

                    // 2. Delivery
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Delivery:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = if (deliveryAmount != "0") "Rs. $deliveryAmount" else "0",
                            fontSize = 14.sp,
                            color = if (deliveryAmount != "0") Color(0xFF1976D2) else Color.Gray
                        )
                    }

                    // 3. Cylinders Returned
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cyls Returned:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "$cylindersReturnedCount",
                            fontSize = 14.sp,
                            color = if (cylindersReturnedCount > 0) Color(0xFFD32F2F) else Color.Gray
                        )
                    }

                    // 4. LPG Returned
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LPG Returned:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "$lpgReturnedCount",
                            fontSize = 14.sp,
                            color = if (lpgReturnedCount > 0) Color(0xFFD32F2F) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Suspend function to fetch all transactions (unchanged from original)
private suspend fun fetchTransactions(
    db: FirebaseFirestore,
    customerName: String
): Map<String, Map<String, Any>> = coroutineScope {
    val transactionMap = mutableMapOf<String, Map<String, Any>>()

    // First fetch all DateAndTime documents
    val transactionsSnapshot = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .get()

    // Then parallelize the fetching of details for each date
    val deferredResults = transactionsSnapshot.documents.map { dateTimeDoc ->
        async {
            val dateTime = dateTimeDoc.id
            val transactionDetailsCollection = dateTimeDoc.reference.collection("Transaction Details")

            // Fetch all documents in parallel
            val results = awaitAll(
                async { transactionDetailsCollection.document("Cash").get() },
                async { transactionDetailsCollection.document("Credit").get() },
                async { transactionDetailsCollection.document("Cylinders Issued").get() },
                async { transactionDetailsCollection.document("Cylinders Returned").get() },
                async { transactionDetailsCollection.document("LPG Issued").get() },
                async { transactionDetailsCollection.document("Inventory Issued").get() },
                async { transactionDetailsCollection.document("Total Price").get() },
                async { transactionDetailsCollection.document("Cash Out").get() },
                async { transactionDetailsCollection.document("LPG Returned").get() },
                async { transactionDetailsCollection.document("Delivery").get() }
            )

            // Map results to their respective values
            dateTime to mapOf(
                "Cash" to (results[0].get("Amount") as? String ?: "0"),
                "Credit" to (results[1].get("Amount") as? String ?: "0"),
                "CylindersIssued" to (results[2].get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()),
                "CylindersReturned" to (results[3].get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()),
                "LPGIssued" to (results[4].get("LPGIssued") as? List<Map<String, String>> ?: emptyList()),
                "InventoryIssued" to (results[5].get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()),
                "Total Price" to (results[6].get("Amount") as? String ?: "0"),
                "Cash Out" to (results[7].get("Amount") as? String ?: "0"),
                "LPGReturned" to (results[8].get("LPGReturned") as? Map<String, Int> ?: emptyMap()),
                "Delivery" to (results[9].get("Amount") as? String ?: "0")
            )
        }
    }

    // Collect all results
    deferredResults.awaitAll().forEach { (dateTime, data) ->
        transactionMap[dateTime] = data
    }

    transactionMap
}