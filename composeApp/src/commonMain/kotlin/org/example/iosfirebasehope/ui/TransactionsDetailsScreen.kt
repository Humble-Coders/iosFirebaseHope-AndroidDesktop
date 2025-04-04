package org.example.iosfirebasehope.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.TransactionDetailsScreenComponent
import org.example.iosfirebasehope.navigation.events.TransactionDetailsScreenEvent


// First, add state variables for dialog visibility and text fields
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

    // State for popups
    var showCylindersIssuedPopup by remember { mutableStateOf(false) }
    var showCylindersReturnedPopup by remember { mutableStateOf(false) }
    var showLPGIssuedPopup by remember { mutableStateOf(false) }
    var showLPGReturnedPopup by remember { mutableStateOf(false) }
    var showInventoryIssuedPopup by remember { mutableStateOf(false) }

    // New state for edit payment dialog
    var showEditPaymentDialog by remember { mutableStateOf(false) }
    var cashAmountText by remember { mutableStateOf("") }
    var creditAmountText by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    // Fetch transaction details and cylinder details on launch
    LaunchedEffect(customerName, dateTime) {
        try {
            // Fetch transaction details
            val fetchedDetails = fetchCustomerTransactionDetails(db, customerName, dateTime)
            transactionDetails = fetchedDetails

            // Initialize text fields with current values
            cashAmountText = (fetchedDetails["Cash"] as String).trim()
            creditAmountText = (fetchedDetails["Credit"] as String).trim()

            // Fetch cylinder details
            val fetchedCylinderDetails = fetchCylinderDetails(db)
            cylinderDetails = fetchedCylinderDetails
        } catch (e: Exception) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Failed to fetch details: ${e.message}")
            }
        }
    }

    // Edit payment dialog
    if (showEditPaymentDialog && transactionDetails != null) {
        val totalPrice = (transactionDetails!!["Total Price"] as String).trim()

        EditPaymentDialog(
            totalPrice = totalPrice,
            cashAmount = cashAmountText,
            isUpdating = isUpdating,
            onCashChange = { cashAmountText = it },
            onDismiss = { showEditPaymentDialog = false },
            onSave = {
                coroutineScope.launch {
                    try {
                        isUpdating = true

                        val totalPriceValue = totalPrice.toDoubleOrNull() ?: 0.0
                        val newCashValue = cashAmountText.toDoubleOrNull() ?: 0.0
                        val newCreditValue = maxOf(0.0, totalPriceValue - newCashValue)

                        // Use the enhanced function to update payment details and customer credit
                        val updatedDetails = updatePaymentDetailsWithCredit(
                            db = db,
                            customerName = customerName,
                            dateTime = dateTime,
                            totalPrice = totalPriceValue,
                            oldCash = (transactionDetails!!["Cash"] as String).trim().toDoubleOrNull() ?: 0.0,
                            oldCredit = (transactionDetails!!["Credit"] as String).trim().toDoubleOrNull() ?: 0.0,
                            newCash = newCashValue
                        )

                        // Update the UI with new details
                        transactionDetails = updatedDetails

                        scaffoldState.snackbarHostState.showSnackbar("Payment details updated successfully")
                        isUpdating = false
                        showEditPaymentDialog = false
                    } catch (e: Exception) {
                        scaffoldState.snackbarHostState.showSnackbar("Failed to update: ${e.message}")
                        isUpdating = false
                    }
                }
            }
        )
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
        // Removed View Bill button from here - it will be placed below the transaction summary card

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
            val lpgReturned = transactionDetails!!["LPGReturned"] as List<Map<String, String>>

            // Use LazyColumn as the root container for scrollable content
            LazyColumn(
                modifier = Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                    )
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

                // Add View Bill Button below the summary card (now in blue)
                item {
                    Button(
                        onClick = { component.onEvent(TransactionDetailsScreenEvent.OnBillClick(customerName, dateTime)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)), // Blue color
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "View Bill",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Add Edit Payment Button
                item {
                    Button(
                        onClick = { showEditPaymentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2)), // Darker blue
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Edit Payment Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Transaction Type Cards
                item {
                    TransactionTypeCard(
                        title = "Cyls Issued",
                        count = cylindersIssued.size,
                        onClick = { showCylindersIssuedPopup = true }
                    )
                }

                item {
                    TransactionTypeCard(
                        title = "Cyls Returned",
                        count = cylindersReturned.size,
                        onClick = { showCylindersReturnedPopup = true }
                    )
                }

                item {
                    // For LPG, show the total quantity instead of list size
                    val totalLpgQuantity = lpgIssued.sumOf {
                        (it["Quantity"] ?: "0").toIntOrNull() ?: 0
                    }
                    TransactionTypeCard(
                        title = "LPG Issued",
                        count = totalLpgQuantity,
                        onClick = { showLPGIssuedPopup = true }
                    )
                }

                item {
                    // For LPG Returned, calculate the total from the returned list
                    val totalLpgReturnedQuantity = lpgReturned.sumOf {
                        (it["Quantity"] ?: "0").toIntOrNull() ?: 0
                    }
                    TransactionTypeCard(
                        title = "LPG Returned",
                        count = totalLpgReturnedQuantity,
                        onClick = { showLPGReturnedPopup = true }
                    )
                }

                item {
                    // For Inventory, show the list size as before
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

            if (showLPGReturnedPopup) {
                TransactionDetailsPopup(
                    title = "LPG Returned",
                    items = lpgReturned,
                    cylinderDetails = emptyList(), // Not needed for LPG
                    onDismiss = { showLPGReturnedPopup = false }
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

// New dedicated function to update payment details
suspend fun updatePaymentDetailsWithCredit(
    db: FirebaseFirestore,
    customerName: String,
    dateTime: String,
    totalPrice: Double,
    oldCash: Double,
    oldCredit: Double,
    newCash: Double
): Map<String, Any> = coroutineScope {
    // Calculate new credit amount based on cash payment
    val newCredit = maxOf(0.0, totalPrice - newCash)

    // Calculate change in credit to update customer's total credit
    val creditDifference = newCredit - oldCredit

    // Path to transaction details collection
    val transactionsPath = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .document(dateTime)
        .collection("Transaction Details")

    // Path to customer details document
    val customerDetailsPath = db.collection("Customers")
        .document("Details")
        .collection("Names")
        .document(customerName)

    // First, get current customer details to extract existing credit
    val customerDetailsDeferred = async {
        customerDetailsPath.get()
    }

    val customerDoc = customerDetailsDeferred.await()
    val customerDetails = customerDoc.get("Details") as? Map<String, String> ?: mapOf()
    val currentCustomerCredit = customerDetails["Credit"]?.toDoubleOrNull() ?: 0.0

    // Calculate new customer credit balance
    val newCustomerCredit = currentCustomerCredit + creditDifference

    // Prepare update batch to ensure atomic updates
    val updateCustomerDetailsDeferred = async <Unit>{
        // Create updated details map with new credit
        val updatedDetails = customerDetails.toMutableMap().apply {
            put("Credit", newCustomerCredit.toString())
        }

        // Update customer details document
        customerDetailsPath.update("Details" to updatedDetails)
    }

    // Update transaction cash amount
    val updateCashDeferred = async {
        transactionsPath.document("Cash").update(mapOf("Amount" to newCash.toString()))
    }

    // Update transaction credit amount
    val updateCreditDeferred = async {
        transactionsPath.document("Credit").update(mapOf("Amount" to newCredit.toString()))
    }

    // Wait for all updates to complete
    updateCustomerDetailsDeferred.await()
    updateCashDeferred.await()
    updateCreditDeferred.await()

    // Return updated transaction details
    return@coroutineScope fetchCustomerTransactionDetails(db, customerName, dateTime)
}

// Add the new EditPaymentDialog composable
@Composable
fun EditPaymentDialog(
    totalPrice: String,
    cashAmount: String,
    isUpdating: Boolean,
    onCashChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    // Calculate credit amount based on total price and cash
    val totalPriceValue = totalPrice.toDoubleOrNull() ?: 0.0
    val cashValue = cashAmount.toDoubleOrNull() ?: 0.0
    val calculatedCredit = totalPriceValue - cashValue
    val creditDisplay = if (calculatedCredit < 0) "0" else calculatedCredit.toString()

    Dialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isUpdating,
            dismissOnClickOutside = !isUpdating
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Dialog title
                Text(
                    text = "Edit Payment Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2f80eb),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Total price display (read-only)
                OutlinedTextField(
                    value = totalPrice,
                    onValueChange = { },
                    label = { Text("Total Price") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = false,
                    readOnly = true
                )

                // Cash amount input
                OutlinedTextField(
                    value = cashAmount,
                    onValueChange = onCashChange,
                    label = { Text("Cash Amount") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    enabled = !isUpdating
                )

                // Credit amount (calculated, read-only)
                OutlinedTextField(
                    value = creditDisplay,
                    onValueChange = { },
                    label = { Text("Credit Amount (Calculated)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = false,
                    readOnly = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = if (calculatedCredit > 0) Color(0xFFD32F2F) else Color.Gray,
                        disabledLabelColor = Color.Gray
                    )
                )

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        enabled = !isUpdating
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Save Changes")
                    }
                }
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
                                        volumeType = item["Volume Type"] ?: "N/A",
                                        isReturned = false
                                    )
                                }
                                "LPG Returned" -> {
                                    LPGDetailItem(
                                        quantity = item["Quantity"] ?: "N/A",
                                        price = item["Price"] ?: "N/A",
                                        date = item["Date"] ?: "N/A",
                                        volumeType = item["Volume Type"] ?: "N/A",
                                        isReturned = true  // Set to true for LPG Returned
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
    volumeType: String,
    isReturned: Boolean = false // Add new parameter with default value
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

                // Only show date if not an LPG returned item
                if (!isReturned) {
                    Text(
                        text = "Date: $date",
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Only show price if not an LPG returned item
            if (!isReturned) {
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
): Map<String, Any> = coroutineScope {
    val transactionDetailsCollection = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .document(dateTime)
        .collection("Transaction Details")

    // Launch parallel requests using async
    val cashDocDeferred = async { transactionDetailsCollection.document("Cash").get() }
    val creditDocDeferred = async { transactionDetailsCollection.document("Credit").get() }
    val cylindersIssuedDocDeferred = async { transactionDetailsCollection.document("Cylinders Issued").get() }
    val cylindersReturnedDocDeferred = async { transactionDetailsCollection.document("Cylinders Returned").get() }
    val lpgIssuedDocDeferred = async { transactionDetailsCollection.document("LPG Issued").get() }
    val lpgReturnedDocDeferred = async { transactionDetailsCollection.document("LPG Returned").get() }
    val inventoryIssuedDocDeferred = async { transactionDetailsCollection.document("Inventory Issued").get() }
    val priceDocDeferred = async { transactionDetailsCollection.document("Total Price").get() }
    val CashOutDocDeferred = async { transactionDetailsCollection.document("Cash Out").get() }

    // Await all results (this happens in parallel)
    val cashDoc = cashDocDeferred.await()
    val creditDoc = creditDocDeferred.await()
    val cylindersIssuedDoc = cylindersIssuedDocDeferred.await()
    val cylindersReturnedDoc = cylindersReturnedDocDeferred.await()
    val lpgIssuedDoc = lpgIssuedDocDeferred.await()
    val lpgReturnedDoc = lpgReturnedDocDeferred.await()
    val inventoryIssuedDoc = inventoryIssuedDocDeferred.await()
    val priceDoc = priceDocDeferred.await()
    val CashOutDoc = CashOutDocDeferred.await()

    // Extract transaction details
    val cashAmount = cashDoc.get("Amount") as? String ?: "0"
    val price = priceDoc.get("Amount") as? String ?: "0"
    val CashOut = CashOutDoc.get("Amount") as? String ?: "0"
    val creditAmount = creditDoc.get("Amount") as? String ?: "0"
    val cylindersIssued = cylindersIssuedDoc.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
    val cylindersReturned = cylindersReturnedDoc.get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
    val lpgIssued = lpgIssuedDoc.get("LPGIssued") as? List<Map<String, String>> ?: emptyList()
    val lpgReturnedMap = lpgReturnedDoc.get("LPGReturned") as? Map<String, Int> ?: emptyMap()
    val inventoryIssued = inventoryIssuedDoc.get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()

    val lpgReturned = lpgReturnedMap.map { (volumeType, quantity) ->
        mapOf(
            "Volume Type" to volumeType,
            "Quantity" to quantity.toString(),
        )
    }

    mapOf(
        "Cash" to cashAmount,
        "Credit" to creditAmount,
        "CylindersIssued" to cylindersIssued,
        "CylindersReturned" to cylindersReturned,
        "LPGIssued" to lpgIssued,
        "InventoryIssued" to inventoryIssued,
        "Total Price" to price,
        "Cash Out" to CashOut,
        "LPGReturned" to lpgReturned
    )
}