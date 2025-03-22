package org.example.iosfirebasehope.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.DailyBookScreenComponent
import org.example.iosfirebasehope.navigation.events.DailyBookScreenEvent
import kotlin.text.toInt


// Data class to hold daily transaction information
data class DailyTransaction(
    val customerName: String,
    val dateTime: String,
    val date: String,
    val time: String,
    val cashAmount: String,
    val creditAmount: String,
    val totalPrice: String,
    val deliveryAmount: String,
    val cashOut: String,
    val cylindersIssuedCount: Int,
    val cylindersReturnedCount: Int,
    val lpgIssuedCount: Int,
    val lpgReturnedCount: Int,
    val inventoryIssuedCount: Int
)



@Composable
fun DailyBookScreenUI(
    component: DailyBookScreenComponent,
    db: FirebaseFirestore
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // Company name
    val COMPANY_NAME = "Gobind Traders"

    // State to hold all transactions
    var allTransactions by remember { mutableStateOf<List<DailyTransaction>>(emptyList()) }

    // State for date selection
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val defaultDateString = "${today.dayOfMonth.toString().padStart(2, '0')}-${today.monthNumber.toString().padStart(2, '0')}-${today.year}"

    var selectedDate by remember { mutableStateOf(defaultDateString) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Loading state
    var isLoading by remember { mutableStateOf(false) }

    // Error state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Function to fetch transactions for a specific date
    fun fetchTransactionsForDate(date: String) {
        isLoading = true
        errorMessage = null

        // Convert date from "DD-MM-YYYY" to "YYYY-MM-DD" for Firebase query
        val parts = date.split("-")
        val queryDate = "${parts[2]}-${parts[1]}-${parts[0]}"

        coroutineScope.launch {
            try {
                // Fetch all customers first
                val customersData = fetchAllCustomers(db)

                // Then fetch transactions for those customers on the selected date
                val transactions = fetchDailyTransactions(db, queryDate, customersData)
                allTransactions = transactions
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Failed to fetch transactions: ${e.message}"
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = errorMessage ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    // Handle date selection from the date picker
    fun handleDateSelection(dateMillis: Long?) {
        if (dateMillis != null) {
            // Convert millis to Instant
            val instant = Instant.fromEpochMilliseconds(dateMillis)

            // Convert to local date time in current time zone
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            // Format as DD-MM-YYYY
            val formattedDate = "${localDateTime.dayOfMonth.toString().padStart(2, '0')}-" +
                    "${localDateTime.monthNumber.toString().padStart(2, '0')}-" +
                    "${localDateTime.year}"

            selectedDate = formattedDate
            fetchTransactionsForDate(formattedDate)
        }
    }

    // Initial fetch using today's date
    LaunchedEffect(Unit) {
        fetchTransactionsForDate(selectedDate)
    }

    // Show date picker dialog if requested
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = {
                handleDateSelection(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Calculate totals
    val totalCash = allTransactions.sumOf { it.cashAmount.toDoubleOrNull() ?: 0.0 }
    val totalCredit = allTransactions.sumOf { it.creditAmount.toDoubleOrNull() ?: 0.0 }
    val totalSales = allTransactions.sumOf { it.totalPrice.toDoubleOrNull() ?: 0.0 }
    val totalCashOut = allTransactions.sumOf { it.cashOut.toDoubleOrNull() ?: 0.0 }
    val totalCylindersIssued = allTransactions.sumOf { it.cylindersIssuedCount }
    val totalCylindersReturned = allTransactions.sumOf { it.cylindersReturnedCount }
    val totalLPGIssued = allTransactions.sumOf { it.lpgIssuedCount }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Daily Book", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(DailyBookScreenEvent.OnBackClick) }) {
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
            // Company header with date selector
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
                    // Date selector button
                    Button(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendar",
                            tint = Color(0xFF2f80eb),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate,
                            color = Color(0xFF2f80eb),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Daily summary card


            Spacer(modifier = Modifier.height(8.dp))

            // Transactions list
            if (isLoading) {
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
                            text = "Loading transactions...",
                            color = Color.Gray
                        )
                    }
                }
            } else if (errorMessage != null) {
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
            } else if (allTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found for ${selectedDate}",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item{
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            elevation = 0.dp,
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color(0xFF2f80eb).copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp)
                            ) {
                                // Title with enhanced style
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFF2f80eb),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Daily Summary (${selectedDate})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Summary grid with better spacing
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SummaryRowEnhanced(label = "Total Sales:", value = "₹ ${totalSales.toInt()}", valueColor = Color(0xFF2E7D32))
                                        SummaryRowEnhanced(label = "Cash In:", value = "₹ ${totalCash.toInt()}", valueColor = Color(0xFF2E7D32))
                                        SummaryRowEnhanced(label = "Cash Out:", value = "₹ ${totalCashOut.toInt()}", valueColor = Color(0xFFD32F2F))
                                    }

                                    // Divider between columns
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(120.dp)
                                            .background(Color(0xFFFFFFFF).copy(alpha = 0.3f))
                                    )

                                    // Right column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SummaryRowEnhanced(label = "Credit:", value = "₹ ${totalCredit.toInt()}", valueColor = Color(0xFFD32F2F))
                                        SummaryRowEnhanced(label = "Cyls Issued:", value = "$totalCylindersIssued", valueColor = Color(0xFF2E7D32))
                                        SummaryRowEnhanced(label = "Cyls Returned:", value = "$totalCylindersReturned", valueColor = Color(0xFFD32F2F))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Transaction count with badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF2f80eb),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${allTransactions.size} Transactions",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2f80eb)
                                    )
                                }
                            }
                        }
                    }
                    items(allTransactions.sortedByDescending { it.dateTime }) { transaction ->
                        DailyTransactionCard(
                            transaction = transaction,
                            onClick = {

                            }
                        )
                    }
                }
            }
        }
    }
}

// Function to fetch all customers from Firebase
private suspend fun fetchAllCustomers(db: FirebaseFirestore): List<String> = kotlinx.coroutines.coroutineScope {
    try {
        // Access the correct collections for customer data
        val customerDoc = db.collection("Customers")
            .document("Names")
            .get()

        // Extract customer names from the CustomerDetails field
        val customerDetails = customerDoc.get("CustomerDetails") as? List<Map<String, String>> ?: emptyList()

        // Extract customer names from each map
        customerDetails.mapNotNull { it["Name"] }
    } catch (e: Exception) {
        println("Error fetching customers: ${e.message}")
        emptyList()
    }
}

// Efficient function to fetch all transactions for a specific date
private suspend fun fetchDailyTransactions(
    db: FirebaseFirestore,
    datePrefix: String,
    customerNames: List<String>
): List<DailyTransaction> = kotlinx.coroutines.coroutineScope {
    val allTransactions = mutableListOf<DailyTransaction>()

    // For each customer, fetch transactions for the specified date
    val customerJobs = customerNames.map { customerName ->
        async {
            val customerTransactions = mutableListOf<DailyTransaction>()

            try {
                // Query transactions for the specific date
                val transactionsQuery = db.collection("Transactions")
                    .document(customerName)
                    .collection("DateAndTime")
                    .get()

                val dateTransactions = transactionsQuery.documents
                    .filter { it.id.startsWith(datePrefix) }

                // Process each matching transaction
                for (dateTimeDoc in dateTransactions) {
                    val dateTime = dateTimeDoc.id
                    val dateParts = dateTime.split("_")

                    // Only process if it's properly formatted
                    if (dateParts.size == 2) {
                        val date = dateParts[0]
                        val time = dateParts[1]

                        // Format date for display
                        val dateComponents = date.split("-")
                        val formattedDate = "${dateComponents[2]}-${dateComponents[1]}-${dateComponents[0]}"

                        // Fetch all transaction details in parallel
                        val transactionDetailsCollection = dateTimeDoc.reference.collection("Transaction Details")

                        // Use structured concurrency to fetch all documents at once
                        val detailsResults = awaitAll(
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

                        // Extract relevant data
                        val cashAmount = detailsResults[0].get("Amount") as? String ?: "0"
                        val creditAmount = detailsResults[1].get("Amount") as? String ?: "0"
                        val cylindersIssued = detailsResults[2].get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
                        val cylindersReturned = detailsResults[3].get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
                        val lpgIssued = detailsResults[4].get("LPGIssued") as? List<Map<String, String>> ?: emptyList()
                        val inventoryIssued = detailsResults[5].get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()
                        val totalPrice = detailsResults[6].get("Amount") as? String ?: "0"
                        val cashOut = detailsResults[7].get("Amount") as? String ?: "0"
                        val lpgReturned = detailsResults[8].get("LPGReturned") as? Map<String, Int> ?: emptyMap()
                        val deliveryAmount = detailsResults[9].get("Amount") as? String ?: "0"

                        // Count items
                        val cylindersIssuedCount = countSerialNumbers(cylindersIssued)
                        val cylindersReturnedCount = countSerialNumbers(cylindersReturned)
                        val lpgIssuedCount = sumQuantities(lpgIssued)
                        val lpgReturnedCount = sumQuantitiesInt(lpgReturned)
                        val inventoryIssuedCount = sumQuantities(inventoryIssued)

                        // Create transaction object
                        customerTransactions.add(
                            DailyTransaction(
                                customerName = customerName,
                                dateTime = dateTime,
                                date = formattedDate,
                                time = time,
                                cashAmount = cashAmount,
                                creditAmount = creditAmount,
                                totalPrice = totalPrice,
                                deliveryAmount = deliveryAmount,
                                cashOut = cashOut,
                                cylindersIssuedCount = cylindersIssuedCount,
                                cylindersReturnedCount = cylindersReturnedCount,
                                lpgIssuedCount = lpgIssuedCount,
                                lpgReturnedCount = lpgReturnedCount,
                                inventoryIssuedCount = inventoryIssuedCount
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error fetching transactions for customer $customerName: ${e.message}")
            }

            customerTransactions
        }
    }

    // Collect all results
    customerJobs.awaitAll().forEach { transactions ->
        allTransactions.addAll(transactions)
    }

    allTransactions
}

// Helper functions for counting items
private fun countSerialNumbers(cylinders: List<Map<String, String>>): Int {
    var count = 0
    for (cylinder in cylinders) {
        if (cylinder["Serial Number"] != null) {
            count++
        }
    }
    return count
}

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
        sum += item.value
    }
    return sum
}



@Composable
fun DailyTransactionCard(
    transaction: DailyTransaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Customer name, total price and time row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer name
                Text(
                    text = transaction.customerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Total price
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "₹ ${transaction.totalPrice}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.totalPrice != "0") Color(0xFF2E7D32) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Time in 12-hour format
                val formattedTime = formatTo12HourTime(transaction.time)
                Text(
                    text = formattedTime,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Main content - 4 fields on left, 4 fields on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cash
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cash:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = "₹ ${transaction.cashAmount}",
                            fontSize = 14.sp,
                            color = if (transaction.cashAmount != "0") Color(0xFF2E7D32) else Color.Gray
                        )
                    }

                    // Inventory (only shown if cashOut is "0")
                    if (transaction.cashOut == "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Inventory:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "${transaction.inventoryIssuedCount}",
                                fontSize = 14.sp,
                                color = if (transaction.inventoryIssuedCount > 0) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }

                    // Cylinders Issued (only shown if cashOut is "0")
                    if (transaction.cashOut == "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cyls Issued:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "${transaction.cylindersIssuedCount}",
                                fontSize = 14.sp,
                                color = if (transaction.cylindersIssuedCount > 0) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }

                    // LPG Issued (only shown if cashOut is "0")
                    if (transaction.cashOut == "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LPG Issued:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "${transaction.lpgIssuedCount}",
                                fontSize = 14.sp,
                                color = if (transaction.lpgIssuedCount > 0) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }

                    // Cash Out (only shown if cashOut is not "0")
                    if (transaction.cashOut != "0") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cash Out:",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "₹ ${transaction.cashOut}",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Credit
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Credit:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "₹ ${transaction.creditAmount}",
                            fontSize = 14.sp,
                            color = if (transaction.creditAmount != "0") Color(0xFFD32F2F) else Color.Gray
                        )
                    }

                    // Delivery
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Delivery:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = if (transaction.deliveryAmount != "0") "₹ ${transaction.deliveryAmount}" else "0",
                            fontSize = 14.sp,
                            color = if (transaction.deliveryAmount != "0") Color(0xFF1976D2) else Color.Gray
                        )
                    }

                    // Cylinders Returned
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cyls Returned:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "${transaction.cylindersReturnedCount}",
                            fontSize = 14.sp,
                            color = if (transaction.cylindersReturnedCount > 0) Color(0xFFD32F2F) else Color.Gray
                        )
                    }

                    // LPG Returned - Added as requested
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LPG Returned:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "${transaction.lpgReturnedCount}",
                            fontSize = 14.sp,
                            color = if (transaction.lpgReturnedCount > 0) Color(0xFFD32F2F) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Function to convert 24-hour format (HH-MM-SS) to 12-hour format (hh:mm AM/PM)
// Using only Kotlin, no Java dependencies
fun formatTo12HourTime(time24: String): String {
    // Parse the input format (e.g., "09-52-24")
    val parts = time24.split("-")
    if (parts.size != 3) return time24.replace("-", ":")

    val hour = parts[0].toIntOrNull() ?: return time24.replace("-", ":")
    val minute = parts[1]

    // Convert to 12-hour format
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    val amPm = if (hour < 12) "AM" else "PM"

    // Format as "h:mm AM/PM"
    return "$hour12:$minute $amPm"
}




// Improved Daily Summary Card


// Enhanced version of the SummaryRow with better styling
@Composable
fun SummaryRowEnhanced(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )

        Box(
            modifier = Modifier
                .background(
                    color = valueColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}