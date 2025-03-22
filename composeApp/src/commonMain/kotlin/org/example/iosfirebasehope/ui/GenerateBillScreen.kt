package org.example.iosfirebasehope.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
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
import kotlinx.coroutines.coroutineScope
import org.example.iosfirebasehope.navigation.components.GenerateBillScreenComponent
import org.example.iosfirebasehope.navigation.events.GenerateBillScreenEvent

@Composable
fun GenerateBillScreenUI(
    customerName: String,
    dateTime: String,
    db: FirebaseFirestore,
    component: GenerateBillScreenComponent
) {
    var transactionDetails by remember { mutableStateOf<TransactionDetails?>(null) }
    var customerDetails by remember { mutableStateOf<Map<String, String>?>(null) }
    var formattedDateTime by remember { mutableStateOf("") }

    // Format date and time
    LaunchedEffect(dateTime) {
        formattedDateTime = if (dateTime.contains("_")) {
            val parts = dateTime.split("_")
            "${parts[0]} ${parts[1].replace("-", ":")}"
        } else {
            dateTime
        }
    }

    // Fetch transaction details
    LaunchedEffect(customerName, dateTime) {
        transactionDetails = fetchTransactionDetails(db, customerName, dateTime)

        // Fetch customer details for phone number
        val document = db.collection("Customers")
            .document("Details")
            .collection("Names")
            .document(customerName)
            .get()

        customerDetails = document.get("Details") as? Map<String, String>
    }

    // Calculate subtotal, delivery amount and grand total
    val subtotal = transactionDetails?.price?.toDoubleOrNull() ?: 0.0
    val deliveryAmount = transactionDetails?.delivery?.toDoubleOrNull() ?: 0.0
    val grandTotal = subtotal + deliveryAmount

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice") },
                contentColor = Color.White,
                backgroundColor = Color(0xFF2f80eb),
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(GenerateBillScreenEvent.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INVOICE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Gobind Traders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = "Gas & Cylinder Suppliers",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Phone",
                            tint = Color(0xFF2f80eb),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+91 8194963318",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Invoice details
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 2.dp,
                    backgroundColor = Color(0xFFF5F5F5)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Invoice number and date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Invoice Date:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formattedDateTime.split(" ")[0],
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Invoice Number:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = dateTime.replace(":", "-").take(15),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Customer information
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Customer:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = customerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                customerDetails?.get("Phone Number")?.let { phone ->
                                    Text(
                                        text = phone,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Payment Method:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if ((transactionDetails?.credit?.toDoubleOrNull() ?: 0.0) > 0) {
                                        "Cash + Credit"
                                    } else {
                                        "Cash"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Items header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2f80eb), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "ITEMS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Column headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Description",
                        modifier = Modifier.weight(2f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Qty",
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Rate",
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Amount",
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Cylinders issued
            transactionDetails?.cylindersIssued?.let { cylinders ->
                if (cylinders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Cylinders Issued",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0))
                                .padding(8.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    itemsIndexed(cylinders) { _, cylinder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"} (S.No: ${cylinder["Serial Number"] ?: "N/A"})",
                                modifier = Modifier.weight(2f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "1",
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Rs.${cylinder["Price"] ?: "0"}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Rs.${cylinder["Price"] ?: "0"}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // LPG issued
            transactionDetails?.lpgIssued?.let { lpgItems ->
                if (lpgItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "LPG Issued",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0))
                                .padding(8.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    itemsIndexed(lpgItems) { _, lpg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LPG ${lpg["Volume Type"] ?: "Unknown"}",
                                modifier = Modifier.weight(2f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${lpg["Quantity"] ?: "0"}",
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )

                            val quantity = lpg["Quantity"]?.toDoubleOrNull() ?: 0.0
                            val price = lpg["Price"]?.toDoubleOrNull() ?: 0.0
                            val rate = if (quantity > 0) price / quantity else 0.0

                            Text(
                                text = "Rs.${kotlin.math.round(subtotal * 100) / 100.0}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Rs.${lpg["Price"] ?: "0"}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // Inventory issued
            transactionDetails?.inventoryIssued?.let { inventoryItems ->
                if (inventoryItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Inventory Items",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0))
                                .padding(8.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    itemsIndexed(inventoryItems) { _, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item["Name"] ?: "Unknown Item",
                                modifier = Modifier.weight(2f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = item["Quantity"] ?: "0",
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )

                            val quantity = item["Quantity"]?.toDoubleOrNull() ?: 0.0
                            val price = item["Price"]?.toDoubleOrNull() ?: 0.0
                            val rate = if (quantity > 0) price / quantity else 0.0

                            Text(
                                text = "Rs.${kotlin.math.round(rate * 100) / 100.0}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Rs.${item["Price"] ?: "0"}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // Cylinders returned
            transactionDetails?.cylindersReturned?.let { cylinders ->
                if (cylinders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Cylinders Returned",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0))
                                .padding(8.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    itemsIndexed(cylinders) { _, cylinder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cylinder (S.No: ${cylinder["Serial Number"] ?: "N/A"})",
                                modifier = Modifier.weight(3.3f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "1",
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "",
                                modifier = Modifier.weight(0.8f),
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // LPG returned
            transactionDetails?.lpgReturned?.let { lpgItems ->
                if (lpgItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "LPG Returned",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0))
                                .padding(8.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    itemsIndexed(lpgItems) { _, lpg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LPG ${lpg["Volume Type"] ?: "Unknown"}",
                                modifier = Modifier.weight(2f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${lpg["Quantity"] ?: "0"}",
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "",
                                modifier = Modifier.weight(1.6f),
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // Totals
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Subtotal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Rs.${(subtotal * 100).toInt() / 100.0}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    // Delivery
                    if (deliveryAmount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Delivery:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Rs.${(deliveryAmount * 100).toInt() / 100.0}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                    // Grand Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Grand Total:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Rs.${kotlin.math.round(grandTotal * 100) / 100.0}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Payment info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cash Paid:",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Rs.${transactionDetails?.cash ?: "0"}",
                            fontSize = 14.sp
                        )
                    }

                    if ((transactionDetails?.credit?.toDoubleOrNull() ?: 0.0) > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Credit Amount:",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "Rs.${transactionDetails?.credit ?: "0"}",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }

                    if ((transactionDetails?.CashOut?.toDoubleOrNull() ?: 0.0) > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cash Out:",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "Rs.${transactionDetails?.CashOut ?: "0"}",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            // Thank you note
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Thank you for your business!",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "For any queries, please contact us.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Update the TransactionDetails data class to include delivery and LPG returned
data class TransactionDetails(
    val cash: String,
    val credit: String,
    val cylindersIssued: List<Map<String, String>>,
    val cylindersReturned: List<Map<String, String>>,
    val inventoryIssued: List<Map<String, String>>,
    val lpgIssued: List<Map<String, String>>,
    val lpgReturned: List<Map<String, String>> = emptyList(), // Added LPG returned
    val price: String,
    val CashOut: String,
    val delivery: String = "0" // Added delivery
)

// Update the fetchTransactionDetails function to fetch delivery and LPG returned
suspend fun fetchTransactionDetails(
    db: FirebaseFirestore,
    customerName: String,
    dateTime: String
): TransactionDetails = coroutineScope {
    // Build base reference path to avoid repetition
    val transactionRef = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .document(dateTime)
        .collection("Transaction Details")

    // Launch parallel requests for all documents
    val cashDeferred = async { transactionRef.document("Cash").get() }
    val creditDeferred = async { transactionRef.document("Credit").get() }
    val cylindersIssuedDeferred = async { transactionRef.document("Cylinders Issued").get() }
    val cylindersReturnedDeferred = async { transactionRef.document("Cylinders Returned").get() }
    val inventoryIssuedDeferred = async { transactionRef.document("Inventory Issued").get() }
    val lpgIssuedDeferred = async { transactionRef.document("LPG Issued").get() }
    val priceDeferred = async { transactionRef.document("Total Price").get() }
    val cashOutDeferred = async { transactionRef.document("Cash Out").get() }
    val lpgReturnedDeferred = async { transactionRef.document("LPG Returned").get() }
    val deliveryDeferred = async { transactionRef.document("Delivery").get() }

    // Fetch cylinders collection in parallel with other requests
    val cylindersDetailsDeferred = async {
        db.collection("Cylinders").get().documents.flatMap { doc ->
            doc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
        }
    }

    // Await all results
    val cashDoc = cashDeferred.await()
    val creditDoc = creditDeferred.await()
    val cylindersIssuedDoc = cylindersIssuedDeferred.await()
    val cylindersReturnedDoc = cylindersReturnedDeferred.await()
    val inventoryIssuedDoc = inventoryIssuedDeferred.await()
    val lpgIssuedDoc = lpgIssuedDeferred.await()
    val priceDoc = priceDeferred.await()
    val cashOutDoc = cashOutDeferred.await()
    val lpgReturnedDoc = lpgReturnedDeferred.await()
    val deliveryDoc = deliveryDeferred.await()
    val cylindersDetails = cylindersDetailsDeferred.await()

    // Extract data from documents (avoiding multiple await calls)
    val price = priceDoc.get("Amount") as? String ?: "0"
    val cashOut = cashOutDoc.get("Amount") as? String ?: "0"
    val cash = cashDoc.get("Amount") as? String ?: "0"
    val credit = creditDoc.get("Amount") as? String ?: "0"
    val delivery = deliveryDoc.get("Amount") as? String ?: "0"

    // Use type-safe casting with safe fallbacks for collections
    val cylindersIssued = cylindersIssuedDoc.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
    val cylindersReturned = cylindersReturnedDoc.get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
    val inventoryIssued = inventoryIssuedDoc.get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()
    val lpgIssued = lpgIssuedDoc.get("LPGIssued") as? List<Map<String, String>> ?: emptyList()
    val lpgReturned = lpgReturnedDoc.get("LPGReturned") as? List<Map<String, String>> ?: emptyList()

    // Create a lookup map for cylinder details to avoid repeated searches
    val cylinderDetailsMap = cylindersDetails.associateBy { it["Serial Number"] ?: "" }

    // Enrich cylindersIssued with Gas Type and Volume Type from lookup map
    val enrichedCylindersIssued = cylindersIssued.map { cylinder ->
        val serialNumber = cylinder["Serial Number"] ?: ""
        val cylinderDetail = cylinderDetailsMap[serialNumber] ?: emptyMap()
        cylinder.toMutableMap().apply {
            put("Gas Type", cylinderDetail["Gas Type"] ?: "Unknown")
            put("Volume Type", cylinderDetail["Volume Type"] ?: "Unknown")
        }
    }

    TransactionDetails(
        cash = cash,
        credit = credit,
        cylindersIssued = enrichedCylindersIssued,
        cylindersReturned = cylindersReturned,
        inventoryIssued = inventoryIssued,
        lpgIssued = lpgIssued,
        lpgReturned = lpgReturned,
        price = price,
        CashOut = cashOut,
        delivery = delivery
    )
}