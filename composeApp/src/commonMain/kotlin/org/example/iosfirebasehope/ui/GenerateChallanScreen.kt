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
import org.example.iosfirebasehope.navigation.components.GenerateChallanScreenComponent
import org.example.iosfirebasehope.navigation.events.GenerateChallanScreenEvent

@Composable
fun GenerateChallanScreenUI(
    vendorName: String,
    dateTime: String,
    db: FirebaseFirestore,
    component: GenerateChallanScreenComponent
) {
    var transactionDetails by remember { mutableStateOf<TransactionDetailsV?>(null) }
    var vendorDetails by remember { mutableStateOf<Map<String, String>?>(null) }
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
    LaunchedEffect(vendorName, dateTime) {
        transactionDetails = fetchChallanTransactionDetails(db, vendorName, dateTime)

        // Optional: Fetch vendor details for phone number if needed
        try {
            val document = db.collection("Vendors")
                .document("Details")
                .collection("Names")
                .document(vendorName)
                .get()

            vendorDetails = document.get("Details") as? Map<String, String>
        } catch (e: Exception) {
            // Handle case where vendor details might not exist
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challan For Refilling") },
                contentColor = Color.White,
                backgroundColor = Color(0xFF2f80eb),
                navigationIcon = {
                    IconButton(onClick = {  }) {
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
                        text = "CHALLAN",
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

            // Challan details
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
                        // Challan number and date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Issue Date:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formattedDateTime.split(" ").firstOrNull() ?: dateTime,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Challan Number:",
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

                        // Vendor information
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Vendor:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = vendorName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                vendorDetails?.get("Phone Number")?.let { phone ->
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
                        text = "Details",
                        modifier = Modifier.weight(1.6f),
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
                            text = "Cylinders sent for refilling",
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
                                text = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"}",
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
                                text = "S.No: ${cylinder["Serial Number"] ?: "N/A"}",
                                modifier = Modifier.weight(1.6f),
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
                            Text(
                                text = "",
                                modifier = Modifier.weight(1.6f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // Cylinders returned (if available)
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
                                text = "Cylinder",
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
                                text = "S.No: ${cylinder["Serial Number"] ?: "N/A"}",
                                modifier = Modifier.weight(1.6f),
                                textAlign = TextAlign.End,
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }

            // Payment details
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                    // Payment info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cash Paid:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Rs.${transactionDetails?.cash ?: "0"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
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
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Rs.${transactionDetails?.credit ?: "0"}",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
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

            // Button at the bottom
            item {
                Button(
                    onClick = { component.onEvent(GenerateChallanScreenEvent.OnBackClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF2f80eb),
                        contentColor = Color.White
                    )
                ) {
                    Text("Go to HomeScreen")
                }
            }
        }
    }
}

suspend fun fetchChallanTransactionDetails(
    db: FirebaseFirestore,
    vendorName: String,
    dateTime: String
): TransactionDetailsV = coroutineScope {
    // Reference to the transaction document with optimized path
    val transactionRef = db.collection("TransactionVendor")
        .document(vendorName)
        .collection("DateAndTime")
        .document(dateTime)
        .collection("Transaction Details")

    // Launch parallel requests for all documents
    val cashDeferred = async { transactionRef.document("Cash").get() }
    val creditDeferred = async { transactionRef.document("Credit").get() }
    val cylindersIssuedDeferred = async { transactionRef.document("Cylinders Issued").get() }
    val cylindersReturnedDeferred = async { transactionRef.document("Cylinders Returned").get() }
    val lpgIssuedDeferred = async { transactionRef.document("LPG Issued").get() }

    // Fetch cylinders collection in parallel
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
    val lpgIssuedDoc = lpgIssuedDeferred.await()
    val cylindersDetails = cylindersDetailsDeferred.await()

    // Extract data from documents
    val cash = cashDoc.get("Amount") as? String ?: "0"
    val credit = creditDoc.get("Amount") as? String ?: "0"
    val cylindersIssued = cylindersIssuedDoc.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
    val cylindersReturned = cylindersReturnedDoc.get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
    val lpgIssued = lpgIssuedDoc.get("LPGIssued") as? List<Map<String, String>> ?: emptyList()

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

    TransactionDetailsV(
        cash = cash,
        credit = credit,
        cylindersIssued = enrichedCylindersIssued,
        cylindersReturned = cylindersReturned,
        lpgIssued = lpgIssued
    )
}

data class TransactionDetailsV(
    val cash: String,
    val credit: String,
    val cylindersIssued: List<Map<String, String>>,
    val cylindersReturned: List<Map<String, String>>,
    val lpgIssued: List<Map<String, String>>
)