package org.example.iosfirebasehope

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.GenerateBillScreenComponent
import org.example.iosfirebasehope.navigation.events.GenerateBillScreenEvent
import org.example.iosfirebasehope.ui.TransactionDetails
import java.io.File
import java.io.FileOutputStream


class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()


@Composable
actual fun GenerateBillScreenUI(
    customerName: String,
    dateTime: String,
    db: FirebaseFirestore,
    component: GenerateBillScreenComponent
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var transactionDetails by remember { mutableStateOf<TransactionDetails?>(null) }
    var customerDetails by remember { mutableStateOf<Map<String, String>?>(null) }
    var formattedDateTime by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isPdfGenerating by remember { mutableStateOf(false) }

    // For PDF sharing
    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

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
        isLoading = true
        try {
            transactionDetails = fetchTransactionDetails(db, customerName, dateTime)

            // Fetch customer details for phone number
            val document = db.collection("Customers")
                .document("Details")
                .collection("Names")
                .document(customerName)
                .get()

            customerDetails = document.get("Details") as? Map<String, String>
        } finally {
            isLoading = false
        }
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
                },
                actions = {
                    // Add a "View Bill" button that generates PDF
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isPdfGenerating = true
                                val fileUri = generatePdf(
                                    context = context,
                                    customerName = customerName,
                                    formattedDateTime = formattedDateTime,
                                    dateTime = dateTime,
                                    transactionDetails = transactionDetails,
                                    customerDetails = customerDetails,
                                    subtotal = subtotal,
                                    deliveryAmount = deliveryAmount,
                                    grandTotal = grandTotal
                                )
                                pdfUri = fileUri
                                isPdfGenerating = false

                                // Open the PDF viewer directly
                                openPdf(context, fileUri, shareLauncher)
                            }
                        },
                        enabled = !isLoading && !isPdfGenerating
                    ) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "View Bill",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            // Show loading indicator while data is being fetched
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2f80eb))
            }
        } else if (isPdfGenerating) {
            // Show loading indicator for PDF generation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF2f80eb))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating PDF...", color = Color(0xFF2f80eb))
                }
            }
        } else {
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

                                    // Updated payment mode logic
                                    val cashAmount = transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
                                    val creditAmount = transactionDetails?.credit?.toDoubleOrNull() ?: 0.0

                                    val paymentMode = when {
                                        cashAmount > 0 && creditAmount > 0 -> "Cash + Credit"
                                        cashAmount > 0 -> "Cash"
                                        creditAmount > 0 -> "Credit"
                                        else -> "N/A"
                                    }

                                    Text(
                                        text = paymentMode,
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

                // LPG Issued
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

                        itemsIndexed(lpgItems) { _, lpgItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LPG ${lpgItem["Volume Type"] ?: "Unknown"}",
                                    modifier = Modifier.weight(2f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = lpgItem["Quantity"] ?: "1",
                                    modifier = Modifier.weight(0.5f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Rs.${lpgItem["Price"] ?: "0"}",
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp
                                )

                                // Calculate total for this item
                                val quantity = lpgItem["Quantity"]?.toIntOrNull() ?: 1
                                val price = lpgItem["Price"]?.toDoubleOrNull() ?: 0.0
                                val total = quantity * price

                                Text(
                                    text = "Rs.${total}",
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp
                                )
                            }
                            Divider(color = Color.LightGray)
                        }
                    }
                }

// LPG Returned section
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

                        itemsIndexed(lpgItems) { _, lpgItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LPG ${lpgItem["Volume Type"] ?: "Unknown"}",
                                    modifier = Modifier.weight(2f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = lpgItem["Quantity"] ?: "1",
                                    modifier = Modifier.weight(0.5f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "-",
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "-",
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp
                                )
                            }
                            Divider(color = Color.LightGray)
                        }
                    }
                }

// Cylinders Returned
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
                                    text = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"} (S.No: ${cylinder["Serial Number"] ?: "N/A"}) (Returned)",
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
                                    text = "-",
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "-",
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp
                                )
                            }
                            Divider(color = Color.LightGray)
                        }
                    }
                }

// Inventory Issued
                transactionDetails?.inventoryIssued?.let { items ->
                    if (items.isNotEmpty()) {
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

                        itemsIndexed(items) { _, item ->
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
                                    text = item["Quantity"] ?: "1",
                                    modifier = Modifier.weight(0.5f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )

                                // Calculate unit price (if possible)
                                val quantity = item["Quantity"]?.toIntOrNull() ?: 1
                                val totalPrice = item["Price"]?.toDoubleOrNull() ?: 0.0
                                val unitPrice = if (quantity > 0) totalPrice / quantity else totalPrice

                                Text(
                                    text = "Rs.${unitPrice}",
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
                        val cashAmount = transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
                        if (cashAmount > 0) {
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
                        // Add "View Bill" button at the bottom for easy access
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isPdfGenerating = true
                                    val fileUri = generatePdf(
                                        context = context,
                                        customerName = customerName,
                                        formattedDateTime = formattedDateTime,
                                        dateTime = dateTime,
                                        transactionDetails = transactionDetails,
                                        customerDetails = customerDetails,
                                        subtotal = subtotal,
                                        deliveryAmount = deliveryAmount,
                                        grandTotal = grandTotal
                                    )
                                    pdfUri = fileUri
                                    isPdfGenerating = false

                                    // Open the PDF directly - this is the primary action
                                    openPdf(context, fileUri, shareLauncher)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Download Bill",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Download Bill",
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

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
}

/**
 * Generate PDF file for the invoice
 */
/**
 * Generate PDF file for the invoice
 */
private suspend fun generatePdf(
    context: Context,
    customerName: String,
    formattedDateTime: String,
    dateTime: String,
    transactionDetails: TransactionDetails?,
    customerDetails: Map<String, String>?,
    subtotal: Double,
    deliveryAmount: Double,
    grandTotal: Double
): Uri {
    // Create a PdfDocument instance
    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 width in points
    val pageHeight = 842 // A4 height in points

    // Set up variables for positioning
    var yPos = 50f
    val leftMargin = 40f
    val centerX = pageWidth / 2f
    var currentPage = 1
    var page = createNewPage(pdfDocument, pageWidth, pageHeight, currentPage)
    var canvas = page.canvas
    var paint = android.graphics.Paint()

    // Draw header (make header more compact)
    paint.textSize = 14f
    paint.isFakeBoldText = true
    val headerText = "INVOICE"
    val headerWidth = paint.measureText(headerText)
    canvas.drawText(headerText, centerX - headerWidth / 2, yPos, paint)

    yPos += 15 // Reduced from 20

    // Draw company name
    paint.textSize = 12f
    val companyName = "Gobind Traders"
    val companyNameWidth = paint.measureText(companyName)
    canvas.drawText(companyName, centerX - companyNameWidth / 2, yPos, paint)

    yPos += 12 // Reduced from 15

    // Draw company subtitle
    paint.textSize = 9f // Reduced from 10
    paint.isFakeBoldText = false
    val subtitle = "Gas & Cylinder Suppliers"
    val subtitleWidth = paint.measureText(subtitle)
    canvas.drawText(subtitle, centerX - subtitleWidth / 2, yPos, paint)

    yPos += 12 // Reduced from 15

    // Draw phone number
    val phoneNumber = "+91 8194963318"
    val phoneWidth = paint.measureText(phoneNumber)
    canvas.drawText(phoneNumber, centerX - phoneWidth / 2, yPos, paint)

    yPos += 15 // Reduced from 20

    // Draw horizontal line
    canvas.drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, paint)

    yPos += 15 // Reduced from 25

    // Draw invoice details (make more compact)
    paint.textSize = 9f // Reduced from 10
    canvas.drawText("Invoice Date:", leftMargin, yPos, paint)
    canvas.drawText("Invoice Number:", pageWidth - leftMargin - 150, yPos, paint)

    yPos += 15 // Reduced from 20

    paint.isFakeBoldText = true
    canvas.drawText(formattedDateTime.split(" ")[0], leftMargin, yPos, paint)
    canvas.drawText(dateTime.replace(":", "-").take(15), pageWidth - leftMargin - 150, yPos, paint)

    yPos += 18 // Reduced from 30

    // Draw customer information
    paint.isFakeBoldText = false
    canvas.drawText("Customer:", leftMargin, yPos, paint)
    canvas.drawText("Payment Method:", pageWidth - leftMargin - 150, yPos, paint)

    yPos += 15 // Reduced from 20

    paint.isFakeBoldText = true
    canvas.drawText(customerName, leftMargin, yPos, paint)

    // Payment mode logic
    val cashAmount = transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
    val creditAmount = transactionDetails?.credit?.toDoubleOrNull() ?: 0.0

    val paymentMode = when {
        cashAmount > 0 && creditAmount > 0 -> "Cash + Credit"
        cashAmount > 0 -> "Cash"
        creditAmount > 0 -> "Credit"
        else -> "N/A"
    }

    canvas.drawText(paymentMode, pageWidth - leftMargin - 150, yPos, paint)

    yPos += 15 // Reduced from 20

    // Draw customer phone if available
    customerDetails?.get("Phone Number")?.let { phone ->
        paint.isFakeBoldText = false
        canvas.drawText(phone, leftMargin, yPos, paint)
        yPos += 15 // Add this to provide space after phone
    }

    // Check if we need to start a new page for items section (reduced threshold)
    if (yPos + 60 > pageHeight - 50) { // Reduced from 80
        pdfDocument.finishPage(page)
        currentPage++
        page = createNewPage(pdfDocument, pageWidth, pageHeight, currentPage)
        canvas = page.canvas
        paint = android.graphics.Paint()
        yPos = 40f // Start a bit higher on continuation pages
    }

    // Draw items header
    paint.color = android.graphics.Color.rgb(47, 128, 235) // #2f80eb
    canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 25, paint) // Reduced from 30

    paint.color = android.graphics.Color.WHITE
    paint.isFakeBoldText = true
    paint.textSize = 11f // Reduced from 12
    canvas.drawText("ITEMS", leftMargin + 10, yPos + 17, paint)

    yPos += 25 // Reduced from 30

    // Draw column headers (make more compact)
    paint.color = android.graphics.Color.rgb(224, 224, 224) // #E0E0E0
    canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint) // Reduced from 25

    paint.color = android.graphics.Color.BLACK
    paint.textSize = 9f // Reduced from 10
    canvas.drawText("Description", leftMargin + 10, yPos + 14, paint) // Adjusted y-position
    canvas.drawText("Qty", leftMargin + 250, yPos + 14, paint)
    canvas.drawText("Rate", leftMargin + 300, yPos + 14, paint)
    canvas.drawText("Amount", leftMargin + 370, yPos + 14, paint)

    yPos += 20 // Reduced from 25

    // Function to check and create new page if needed
    fun checkAndCreateNewPageIfNeeded(heightNeeded: Float): Boolean {
        if (yPos + heightNeeded > pageHeight - 50) {
            pdfDocument.finishPage(page)
            currentPage++
            page = createNewPage(pdfDocument, pageWidth, pageHeight, currentPage)
            canvas = page.canvas
            paint = android.graphics.Paint()

            // Add "continued" indicator
            paint.textSize = 9f // Reduced from 10
            paint.isFakeBoldText = true
            canvas.drawText("(continued from previous page)", centerX - 80, 30f, paint) // Move higher
            yPos = 40f // Start higher on continuation pages

            // Redraw column headers
            paint.color = android.graphics.Color.rgb(224, 224, 224)
            canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint)

            paint.color = android.graphics.Color.BLACK
            paint.textSize = 9f
            canvas.drawText("Description", leftMargin + 10, yPos + 14, paint)
            canvas.drawText("Qty", leftMargin + 250, yPos + 14, paint)
            canvas.drawText("Rate", leftMargin + 300, yPos + 14, paint)
            canvas.drawText("Amount", leftMargin + 370, yPos + 14, paint)

            yPos += 20
            return true
        }
        return false
    }

    // Draw cylinders issued with more compact layout
    transactionDetails?.cylindersIssued?.let { cylinders ->
        if (cylinders.isNotEmpty()) {
            checkAndCreateNewPageIfNeeded(25f + (cylinders.size * 20f)) // Estimate space needed

            paint.color = android.graphics.Color.rgb(240, 240, 240) // #F0F0F0
            canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint) // Reduced from 25

            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("Cylinders Issued", leftMargin + 10, yPos + 14, paint) // Adjusted y-position

            yPos += 20 // Reduced from 25

            paint.isFakeBoldText = false
            for (cylinder in cylinders) {
                if (checkAndCreateNewPageIfNeeded(20f)) {
                    // If a new page was created, we don't need to add extra spacing
                    // as the headers were already redrawn
                } else {
                    // Only check for new page if we're not at the start of a page
                }

                val description = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"} (S.No: ${cylinder["Serial Number"] ?: "N/A"})"
                val price = cylinder["Price"] ?: "0"

                canvas.drawText(description, leftMargin + 10, yPos + 14, paint)
                canvas.drawText("1", leftMargin + 250, yPos + 14, paint)
                canvas.drawText("Rs.$price", leftMargin + 300, yPos + 14, paint)
                canvas.drawText("Rs.$price", leftMargin + 370, yPos + 14, paint)

                yPos += 20 // Reduced from 25

                // Draw a light gray line
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, paint)

                paint.color = android.graphics.Color.BLACK
            }
        }
    }

    // Draw LPG Issued with more compact layout
    transactionDetails?.lpgIssued?.let { lpgItems ->
        if (lpgItems.isNotEmpty()) {
            checkAndCreateNewPageIfNeeded(25f + (lpgItems.size * 20f)) // Estimate space needed

            paint.color = android.graphics.Color.rgb(240, 240, 240)
            canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint) // Reduced from 25

            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("LPG Issued", leftMargin + 10, yPos + 14, paint) // Adjusted y-position

            yPos += 20 // Reduced from 25

            paint.isFakeBoldText = false
            for (lpgItem in lpgItems) {
                val description = "LPG ${lpgItem["Volume Type"] ?: "Unknown"}"
                val quantity = lpgItem["Quantity"] ?: "1"
                val price = lpgItem["Price"] ?: "0"
                val total = (quantity.toIntOrNull() ?: 1) * (price.toDoubleOrNull() ?: 0.0)

                canvas.drawText(description, leftMargin + 10, yPos + 14, paint)
                canvas.drawText(quantity, leftMargin + 250, yPos + 14, paint)
                canvas.drawText("Rs.$price", leftMargin + 300, yPos + 14, paint)
                canvas.drawText("Rs.$total", leftMargin + 370, yPos + 14, paint)

                yPos += 20 // Reduced from 25

                // Draw a light gray line
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, paint)

                paint.color = android.graphics.Color.BLACK
            }
        }
    }

    // Draw LPG Returned with more compact layout
    transactionDetails?.lpgReturned?.let { lpgItems ->
        if (lpgItems.isNotEmpty()) {
            checkAndCreateNewPageIfNeeded(25f + (lpgItems.size * 20f)) // Estimate space needed

            paint.color = android.graphics.Color.rgb(240, 240, 240)
            canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint) // Reduced from 25

            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("LPG Returned", leftMargin + 10, yPos + 14, paint) // Adjusted y-position

            yPos += 20 // Reduced from 25

            paint.isFakeBoldText = false
            for (lpgItem in lpgItems) {
                val description = "LPG ${lpgItem["Volume Type"] ?: "Unknown"} (Returned)"
                val quantity = lpgItem["Quantity"] ?: "1"

                canvas.drawText(description, leftMargin + 10, yPos + 14, paint)
                canvas.drawText(quantity, leftMargin + 250, yPos + 14, paint)
                canvas.drawText("-", leftMargin + 300, yPos + 14, paint)
                canvas.drawText("-", leftMargin + 370, yPos + 14, paint)

                yPos += 20 // Reduced from 25

                // Draw a light gray line
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, paint)

                paint.color = android.graphics.Color.BLACK
            }
        }
    }

    // Draw Cylinders Returned with more compact layout
    transactionDetails?.cylindersReturned?.let { cylinders ->
        if (cylinders.isNotEmpty()) {
            checkAndCreateNewPageIfNeeded(25f + (cylinders.size * 20f)) // Estimate space needed

            paint.color = android.graphics.Color.rgb(240, 240, 240)
            canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint) // Reduced from 25

            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("Cylinders Returned", leftMargin + 10, yPos + 14, paint) // Adjusted y-position

            yPos += 20 // Reduced from 25

            paint.isFakeBoldText = false
            for (cylinder in cylinders) {
                val description = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"} (S.No: ${cylinder["Serial Number"] ?: "N/A"}) (Returned)"

                canvas.drawText(description, leftMargin + 10, yPos + 14, paint)
                canvas.drawText("1", leftMargin + 250, yPos + 14, paint)
                canvas.drawText("-", leftMargin + 300, yPos + 14, paint)
                canvas.drawText("-", leftMargin + 370, yPos + 14, paint)

                yPos += 20 // Reduced from 25

                // Draw a light gray line
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, paint)

                paint.color = android.graphics.Color.BLACK
            }
        }
    }

    // Draw Inventory Items with more compact layout
    transactionDetails?.inventoryIssued?.let { items ->
        if (items.isNotEmpty()) {
            checkAndCreateNewPageIfNeeded(25f + (items.size * 20f)) // Estimate space needed

            paint.color = android.graphics.Color.rgb(240, 240, 240)
            canvas.drawRect(leftMargin, yPos, pageWidth - leftMargin, yPos + 20, paint) // Reduced from 25

            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("Inventory Items", leftMargin + 10, yPos + 14, paint) // Adjusted y-position

            yPos += 20 // Reduced from 25

            paint.isFakeBoldText = false
            for (item in items) {
                val name = item["Name"] ?: "Unknown Item"
                val quantity = item["Quantity"] ?: "1"
                val totalPrice = item["Price"]?.toDoubleOrNull() ?: 0.0
                val quantityNum = quantity.toIntOrNull() ?: 1
                val unitPrice = if (quantityNum > 0) totalPrice / quantityNum else totalPrice

                canvas.drawText(name, leftMargin + 10, yPos + 14, paint)
                canvas.drawText(quantity, leftMargin + 250, yPos + 14, paint)
                canvas.drawText("Rs.${unitPrice}", leftMargin + 300, yPos + 14, paint)
                canvas.drawText("Rs.${totalPrice}", leftMargin + 370, yPos + 14, paint)

                yPos += 20 // Reduced from 25

                // Draw a light gray line
                paint.color = android.graphics.Color.LTGRAY
                canvas.drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, paint)

                paint.color = android.graphics.Color.BLACK
            }
        }
    }

    // Check if we need a new page for totals section (with reduced space requirement)
    if (checkAndCreateNewPageIfNeeded(150f)) { // Reduced from 200
        // New page was created, don't need extra spacing
    } else {
        // Add some extra spacing before totals
        yPos += 10 // Reduced from 15
    }

    // Draw totals with more compact layout
    paint.isFakeBoldText = false
    canvas.drawText("Subtotal:", leftMargin + 300, yPos, paint)
    canvas.drawText("Rs.${(subtotal * 100).toInt() / 100.0}", leftMargin + 370, yPos, paint)

    yPos += 20 // Reduced from 25

    // Draw delivery if applicable
    if (deliveryAmount > 0) {
        canvas.drawText("Delivery:", leftMargin + 300, yPos, paint)
        canvas.drawText("Rs.${(deliveryAmount * 100).toInt() / 100.0}", leftMargin + 370, yPos, paint)
        yPos += 20 // Reduced from 25
    }

    // Draw a divider line
    canvas.drawLine(leftMargin + 300, yPos, pageWidth - leftMargin, yPos, paint)

    yPos += 20 // Reduced from 25

    // Draw grand total
    paint.isFakeBoldText = true
    paint.textSize = 10f
    canvas.drawText("Total:", leftMargin + 300, yPos, paint)
    canvas.drawText("Rs.${kotlin.math.round(grandTotal * 100) / 100.0}", leftMargin + 370, yPos, paint)

    yPos += 25 // Reduced from 30

    // Draw payment details
    paint.isFakeBoldText = false
    paint.textSize = 9f // Reduced from 10

    val cashPaid = transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
    if (cashPaid > 0) {
        canvas.drawText("Cash Paid:", leftMargin + 300, yPos, paint)
        canvas.drawText("Rs.${transactionDetails?.cash ?: "0"}", leftMargin + 370, yPos, paint)
        yPos += 15 // Reduced from 20
    }

    val creditAmount2 = transactionDetails?.credit?.toDoubleOrNull() ?: 0.0
    if (creditAmount2 > 0) {
        paint.color = android.graphics.Color.rgb(211, 47, 47) // #D32F2F (Red color)
        canvas.drawText("Credit:", leftMargin + 300, yPos, paint)
        canvas.drawText("Rs.${transactionDetails?.credit ?: "0"}", leftMargin + 370, yPos, paint)
        paint.color = android.graphics.Color.BLACK
        yPos += 15 // Reduced from 20
    }

    val cashOut = transactionDetails?.CashOut?.toDoubleOrNull() ?: 0.0
    if (cashOut > 0) {
        paint.color = android.graphics.Color.rgb(211, 47, 47) // #D32F2F (Red color)
        canvas.drawText("Cash Out:", leftMargin + 300, yPos, paint)
        canvas.drawText("Rs.${transactionDetails?.CashOut ?: "0"}", leftMargin + 370, yPos, paint)
        paint.color = android.graphics.Color.BLACK
        yPos += 15 // Reduced from 20
    }

    // Add thank you note
    canvas.drawText("Thank you for your business!", centerX - 70, pageHeight - 40f, paint)

    // Add page number at the bottom of the page
    paint.textSize = 8f
    val pageText = "Page $currentPage of $currentPage"
    val pageTextWidth = paint.measureText(pageText)
    canvas.drawText(pageText, pageWidth - leftMargin - pageTextWidth, pageHeight - 20f, paint)

    // Finish current page
    pdfDocument.finishPage(page)

    // Create file in Downloads directory
    val fileName = "Invoice_${customerName.replace(" ", "_")}_${dateTime.replace(":", "-").take(15)}.pdf"
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, fileName)

    try {
        // Write the document to the file
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pdfDocument.close()
    }

    // Get content URI using FileProvider
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

// Helper function to create a new page
private fun createNewPage(pdfDocument: PdfDocument, pageWidth: Int, pageHeight: Int, pageNumber: Int): PdfDocument.Page {
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    return pdfDocument.startPage(pageInfo)
}

/**
 * Open the generated PDF file
 */
private fun openPdf(
    context: Context,
    fileUri: Uri,
    launcher: ActivityResultLauncher<Intent>
) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
        addCategory(Intent.CATEGORY_DEFAULT)
    }

    try {
        // Try to directly start the activity
        context.startActivity(intent)
    } catch (e: Exception) {
        // If direct start fails, try using the launcher
        try {
            launcher.launch(intent)
        } catch (e2: Exception) {
            // If all attempts to open fail, show a toast with instructions
            Toast.makeText(
                context,
                "Could not open PDF viewer. File saved to Downloads folder.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}




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
    val lpgReturnedMap = lpgReturnedDoc.get("LPGReturned") as? Map<String, Int>?: emptyMap()

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
    val enrichedCylindersReturned = cylindersReturned.map { cylinder ->
        val serialNumber = cylinder["Serial Number"] ?: ""
        val cylinderDetail = cylinderDetailsMap[serialNumber] ?: emptyMap()
        cylinder.toMutableMap().apply {
            put("Gas Type", cylinderDetail["Gas Type"] ?: "Unknown")
            put("Volume Type", cylinderDetail["Volume Type"] ?: "Unknown")
        }
    }

    val lpgReturned = lpgReturnedMap.map { (volumeType, quantity) ->
        mapOf(
            "Volume Type" to volumeType,
            "Quantity" to quantity.toString(),
            // If you don't have price data for returned LPG, use "N/A" or "0
        )
    }


    TransactionDetails(
        cash = cash,
        credit = credit,
        cylindersIssued = enrichedCylindersIssued,
        cylindersReturned = enrichedCylindersReturned,
        inventoryIssued = inventoryIssued,
        lpgIssued = lpgIssued,
        lpgReturned = lpgReturned,
        price = price,
        CashOut = cashOut,
        delivery = delivery
    )
}