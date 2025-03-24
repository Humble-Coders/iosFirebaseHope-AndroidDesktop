package org.example.iosfirebasehope.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.InventoryVendorDetailsScreenComponent
import org.example.iosfirebasehope.navigation.events.InventoryVendorDetailsScreenEvent

@Composable
fun InventoryVendorDetailsScreenUI(
    vendorName: String,
    component: InventoryVendorDetailsScreenComponent,
    db: FirebaseFirestore
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State for vendor details
    var vendorDetails by remember { mutableStateOf<Map<String, String>?>(null) }

    // State for transactions
    var transactions by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // State for UI controls
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCashDialogActive by remember { mutableStateOf(false) }
    var isUploading = remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Map<String, String>?>(null) }
    var showTransactionDetails by remember { mutableStateOf(false) }

    // Fetch vendor details and transactions on launch
    LaunchedEffect(vendorName) {
        try {
            // Fetch vendor details
            val vendorDoc = db.collection("InventoryVendors")
                .document("Details")
                .collection("Names")
                .document(vendorName)
                .get()

            if (vendorDoc.exists) {
                vendorDetails = vendorDoc.get("Details") as? Map<String, String>
            } else {
                errorMessage = "Vendor details not found."
            }

            // Fetch transactions
            val transactionsCollection = db.collection("InventoryVendors").document("Transactions").collection("Names")
                .document(vendorName)
                .collection("History")
                .get()

            val transactionsList = mutableListOf<Map<String, String>>()
            transactionsCollection.documents.forEach { doc ->
                val docId = doc.id
                val data = doc.data<Map<String, String>>()
                if (data != null) {
                    val transactionMap = mutableMapOf<String, String>()
                    transactionMap["id"] = docId
                    transactionMap["date"] = docId

                    // Add all entries from data to the transaction map
                    data.forEach { (key, value) ->
                        transactionMap[key] = value
                    }

                    transactionsList.add(transactionMap)
                }
            }

            transactions = transactionsList.sortedByDescending { it["date"] as String }

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to fetch data: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Inventory Vendor Details", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(InventoryVendorDetailsScreenEvent.onBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading vendor details...")
            }
        } else if (errorMessage != null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "An error occurred",
                    color = Color.Red
                )
            }
        } else {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Vendor Details Card
                if (vendorDetails != null) {
                    SwipeableInventoryVendorDetailsCard(
                        vendorName = vendorName,
                        vendorDetails = vendorDetails!!,
                        coroutineScope = coroutineScope
                    )
                }

                // Cash Transaction Button
                Button(
                    onClick = { isCashDialogActive = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                ) {
                    Text(
                        text = "Cash Payment",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // Transactions Section Header
                Text(
                    text = "Transaction History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                // Transactions List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    if (transactions.isEmpty()) {
                        item {
                            Text(
                                text = "No transactions found",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        items(transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                onClick = {
                                    selectedTransaction = transaction
                                    showTransactionDetails = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // View All Transactions Button (if needed for future expansion)

            }

            // Cash Payment Dialog
            if (isCashDialogActive) {
                CashPaymentDialog(
                    onDismiss = { isCashDialogActive = false },
                    isUploading = isUploading,
                    vendorName = vendorName,
                    db = db,
                    onUpdateSuccess = {
                        // Refresh the vendor details
                        coroutineScope.launch {
                            val vendorDoc = db.collection("InventoryVendors")
                                .document("Details")
                                .collection("Names")
                                .document(vendorName)
                                .get()

                            if (vendorDoc.exists) {
                                vendorDetails = vendorDoc.get("Details") as? Map<String, String>

                                // Also refresh transactions
                                val transactionsCollection = db.collection("InventoryVendorTransactions")
                                    .document(vendorName)
                                    .collection("DateAndTime")
                                    .get()

                                val transactionsList = mutableListOf<Map<String, String>>()
                                transactionsCollection.documents.forEach { doc ->
                                    val docId = doc.id
                                    val data = doc.data<Map<String, String>>()
                                    if (data != null) {
                                        val transactionMap = mutableMapOf<String, String>()
                                        transactionMap["id"] = docId
                                        transactionMap["date"] = docId

                                        // Add all entries from data to the transaction map
                                        data.forEach { (key, value) ->
                                            transactionMap[key] = value.toString()
                                        }

                                        transactionsList.add(transactionMap)
                                    }
                                }

                                transactions = transactionsList.sortedByDescending { it["date"] as String }
                            } else {
                                scaffoldState.snackbarHostState.showSnackbar("Vendor details not found.")
                            }
                        }
                    },
                    coroutineScope = coroutineScope
                )
            }

            // Transaction Details Dialog
            if (showTransactionDetails && selectedTransaction != null) {
                TransactionDetailsDialog(
                    transaction = selectedTransaction!!,
                    onDismiss = { showTransactionDetails = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableInventoryVendorDetailsCard(
    vendorName: String,
    vendorDetails: Map<String, String>,
    coroutineScope: CoroutineScope
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = vendorName,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2f80eb)
            )

            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> InventoryVendorDetailsPage(
                        listOf(
                            "Phone Number" to (vendorDetails["Phone Number"] ?: "Not Available"),
                            "Credit" to "₹${vendorDetails["Credit"] ?: "0"}",
                            "UID" to (vendorDetails["UID"] ?: "Not Available"),
                        )
                    )
                    1 -> InventoryVendorDetailsPage(
                        listOf(
                            "Address" to (vendorDetails["Address"] ?: "Not Available")
                        )
                    )
                }
            }

            // Page Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color(0xFF2f80eb) else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(8.dp)
                            .background(color, RoundedCornerShape(4.dp))
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(iteration)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryVendorDetailsPage(details: List<Pair<String, String>>) {
    Column(modifier = Modifier.padding(8.dp)) {
        details.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(1f),
                    color = if (label == "Credit" && value != "₹0" && value != "₹Not Available") Color.Red else Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Map<String, String>,
    onClick: () -> Unit
) {
    val date = transaction["Date"] as? String ?: (transaction["date"] as? String ?: "")
    val formattedDate = if (date.contains("_")) {
        val parts = date.split("_")
        "${parts[0]} ${parts[1].replace("-", ":")}"
    } else {
        date
    }

    val itemName = transaction["ItemName"] as? String ?: ""
    val quantity = transaction["Quantity"] as? String ?: ""
    val cash = transaction["Cash"] as? String ?: "0"
    val credit = transaction["Credit"] as? String ?: "0"
    val totalPrice = transaction["TotalPrice"] as? String ?: transaction["CostPrice"] as? String ?: "0"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = formattedDate,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Item details
            if (itemName.isNotEmpty()) {
                Text(
                    text = "Item: $itemName",
                    fontWeight = FontWeight.Medium
                )

                if (quantity.isNotEmpty()) {
                    Text(
                        text = "Quantity: $quantity",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Financial details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cash: ₹$cash",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50) // Green for cash
                    )

                    Text(
                        text = "Credit: ₹$credit",
                        fontWeight = FontWeight.Medium,
                        color = if (credit.toFloatOrNull() ?: 0f > 0) Color(0xFFFF5722) else Color.Gray // Orange-red for credit
                    )
                }

                Text(
                    text = "Total: ₹$totalPrice",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
@Composable
fun TransactionDetailsDialog(
    transaction: Map<String, String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transaction Details",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2f80eb)
            )
        },
        text = {
            Column {
                // Format and display the date better
                val date = transaction["Date"] as? String ?: (transaction["date"] as? String ?: "")
                val formattedDate = if (date.contains("_")) {
                    val parts = date.split("_")
                    "${parts[0]} ${parts[1].replace("-", ":")}"
                } else {
                    date
                }

                Text(
                    text = "Date: $formattedDate",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Highlight financial details
                val cash = transaction["Cash"] as? String ?: "0"
                val credit = transaction["Credit"] as? String ?: "0"
                val totalPrice = transaction["TotalPrice"] as? String ?: transaction["CostPrice"] as? String ?: "0"

                Text(
                    text = "Financial Summary:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Cash Payment:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(120.dp)
                    )

                    Text(
                        text = "₹$cash",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Credit Amount:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(120.dp)
                    )

                    Text(
                        text = "₹$credit",
                        modifier = Modifier.weight(1f),
                        color = if (credit.toFloatOrNull() ?: 0f > 0) Color(0xFFFF5722) else Color.Gray
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Total Price:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(120.dp)
                    )

                    Text(
                        text = "₹$totalPrice",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Display all other transaction details
                transaction.entries.filter { entry ->
                    val key = entry.key.lowercase()
                    key != "date" && key != "id" &&
                            key != "cash" && key != "credit" && key != "totalprice" && key != "costprice" &&
                            key != "type" && key != "amount" && key != "date"
                }.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${key.replaceFirstChar { it.uppercase() }}:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(120.dp)
                        )

                        Text(
                            text = value.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
            ) {
                Text("Close", color = Color.White)
            }
        },
        backgroundColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CashPaymentDialog(
    onDismiss: () -> Unit,
    isUploading: MutableState<Boolean>,
    vendorName: String,
    db: FirebaseFirestore,
    onUpdateSuccess: () -> Unit,
    coroutineScope: CoroutineScope
) {
    var cashPayment by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedDate = "${currentDate.dayOfMonth.toString().padStart(2, '0')}-${currentDate.monthNumber.toString().padStart(2, '0')}-${currentDate.year}"

// Use the timestamp as document ID for uniqueness
    val transactionId = Clock.System.now().toString()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top transparent spacer
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Dialog box content
        item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title
                        Text(
                            text = "Cash Payment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF2f80eb)
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Cash Payment Field
                        OutlinedTextField(
                            value = cashPayment,
                            onValueChange = { cashPayment = it },
                            label = { Text("Payment Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )

                        // Error Message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancel Button
                            TextButton(
                                onClick = onDismiss,
                                enabled = !isUploading.value
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Confirm Button
                            Button(
                                onClick = {
                                    val paymentAmount = cashPayment.toFloatOrNull()
                                    if (paymentAmount == null || paymentAmount <= 0) {
                                        errorMessage = "Please enter a valid amount."
                                        return@Button
                                    }

                                    isUploading.value = true
                                    errorMessage = null

                                    // Launch a coroutine to handle Firestore operations
                                    coroutineScope.launch {
                                        try {
                                            // Fetch the current credit value
                                            val document = db.collection("InventoryVendors")
                                                .document("Details")
                                                .collection("Names")
                                                .document(vendorName)
                                                .get()

                                            // Get the current date and time in the format "yyyy-MM-dd_HH:mm:ss"
                                            val currentDateTime = Clock.System.now()
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .toString()
                                                .replace("T", "_")
                                                .replace(":", "-")
                                                .substringBefore(".")

                                            // Reference to the Transactions collection
                                            val transactionsRef = db.collection("InventoryVendorTransactions")
                                                .document(vendorName)
                                                .collection("DateAndTime")
                                                .document(currentDateTime)

                                            // Create transaction record
                                            transactionsRef.set(
                                                mapOf(
                                                    "Type" to "Payment",
                                                    "Cash" to paymentAmount.toString(),
                                                    "Credit" to "0",
                                                    "TotalPrice" to paymentAmount.toString(),
                                                    "Date" to formattedDate
                                                )
                                            )

                                            if (document.exists) {
                                                val details = document.get("Details") as? Map<String, String>
                                                val currentCredit = details?.get("Credit")?.toFloatOrNull() ?: 0f

                                                // Calculate new credit value
                                                val updatedCredit = (currentCredit - paymentAmount).coerceAtLeast(0f)

                                                // Update the credit value
                                                val updatedDetails = details?.toMutableMap()?.apply {
                                                    put("Credit", updatedCredit.toString())
                                                    put("Last Transaction", currentDateTime)
                                                }

                                                // Update Firestore
                                                db.collection("InventoryVendors")
                                                    .document("Details")
                                                    .collection("Names")
                                                    .document(vendorName)
                                                    .update("Details" to updatedDetails)

                                                // Notify success
                                                isUploading.value = false
                                                onUpdateSuccess() // Notify parent about successful update
                                                onDismiss() // Close the dialog
                                            } else {
                                                isUploading.value = false
                                                errorMessage = "Vendor details not found."
                                            }
                                        } catch (e: Exception) {
                                            isUploading.value = false
                                            errorMessage = "Failed to update credit: ${e.message}"
                                        }
                                    }
                                },
                                enabled = !isUploading.value,
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                if (isUploading.value) {
                                    Text("Processing...", color = Color.White)
                                } else {
                                    Text("Confirm Payment", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom transparent spacer
        item {
            Spacer(modifier = Modifier.height(500.dp))
        }
    }
}