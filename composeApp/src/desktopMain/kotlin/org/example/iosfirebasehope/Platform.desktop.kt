package org.example.iosfirebasehope

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.example.iosfirebasehope.navigation.components.GenerateBillScreenComponent
import org.example.iosfirebasehope.navigation.events.GenerateBillScreenEvent
import org.example.iosfirebasehope.ui.TransactionDetails
import org.example.iosfirebasehope.ui.fetchTransactionDetails
import java.awt.Desktop
import java.io.File
import java.nio.file.Paths

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun GenerateBillScreenUI(
    customerName: String,
    dateTime: String,
    db: FirebaseFirestore,
    component: GenerateBillScreenComponent
) {
    val coroutineScope = rememberCoroutineScope()

    var transactionDetails by remember { mutableStateOf<TransactionDetails?>(null) }
    var customerDetails by remember { mutableStateOf<Map<String, String>?>(null) }
    var formattedDateTime by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isPdfGenerating by remember { mutableStateOf(false) }

    // For PDF access - using desktop URI representation
    var pdfPath by remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(GenerateBillScreenEvent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // "View Bill" button that generates PDF
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isPdfGenerating = true
                                val filePath = generatePdf(
                                    customerName = customerName,
                                    formattedDateTime = formattedDateTime,
                                    dateTime = dateTime,
                                    transactionDetails = transactionDetails,
                                    customerDetails = customerDetails,
                                    subtotal = subtotal,
                                    deliveryAmount = deliveryAmount,
                                    grandTotal = grandTotal
                                )
                                pdfPath = filePath
                                isPdfGenerating = false

                                // Open the PDF viewer using desktop APIs
                                openPdf(filePath)
                            }
                        },
                        enabled = !isLoading && !isPdfGenerating
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "View Bill",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White
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
                        backgroundColor = Color(0xFFF5F5F5),
                        elevation = 2.dp
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
                                    val cashAmount =
                                        transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
                                    val creditAmount =
                                        transactionDetails?.credit?.toDoubleOrNull() ?: 0.0

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
                            .background(
                                Color(0xFF2f80eb),
                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
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
                                val unitPrice =
                                    if (quantity > 0) totalPrice / quantity else totalPrice

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
                                    val filePath = generatePdf(
                                        customerName = customerName,
                                        formattedDateTime = formattedDateTime,
                                        dateTime = dateTime,
                                        transactionDetails = transactionDetails,
                                        customerDetails = customerDetails,
                                        subtotal = subtotal,
                                        deliveryAmount = deliveryAmount,
                                        grandTotal = grandTotal
                                    )
                                    pdfPath = filePath
                                    isPdfGenerating = false

                                    // Open the PDF directly
                                    openPdf(filePath)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Download Bill",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "View Bill",
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

suspend fun generatePdf(
        customerName: String,
        formattedDateTime: String,
        dateTime: String,
        transactionDetails: TransactionDetails?,
        customerDetails: Map<String, String>?,
        subtotal: Double,
        deliveryAmount: Double,
        grandTotal: Double
    ): String = withContext(Dispatchers.IO) {
        // Create a PDDocument instance
        val document = PDDocument()
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height

        // Set up variables for positioning
        var yPos = pageHeight - 50f
        val leftMargin = 40f
        val centerX = pageWidth / 2f
        var currentPage = 1
        var page = PDPage(PDRectangle.A4)
        document.addPage(page)
        var contentStream = PDPageContentStream(document, page)

        // Colors
    // Colors
    val blueColor = PDColor(floatArrayOf(47f/255f, 128f/255f, 235f/255f), PDDeviceRGB.INSTANCE) // #2f80eb
    val grayColor = PDColor(floatArrayOf(128f/255f, 128f/255f, 128f/255f), PDDeviceRGB.INSTANCE)
    val lightGrayColor = PDColor(floatArrayOf(224f/255f, 224f/255f, 224f/255f), PDDeviceRGB.INSTANCE)
    val redColor = PDColor(floatArrayOf(211f/255f, 47f/255f, 47f/255f), PDDeviceRGB.INSTANCE) // #D32F2F
    val whiteColor = PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)
    val blackColor = PDColor(floatArrayOf(0f, 0f, 0f), PDDeviceRGB.INSTANCE)

        // Helper function to close current stream and create a new page
        fun createNewPage(): PDPageContentStream {
            contentStream.close()
            page = PDPage(PDRectangle.A4)
            document.addPage(page)
            currentPage++
            yPos = pageHeight - 50f
            return PDPageContentStream(document, page)
        }

        // Helper function to draw text with specified font
        fun drawText(
            text: String,
            x: Float,
            y: Float,
            font: PDType1Font = PDType1Font.HELVETICA,
            fontSize: Float = 10f,
            color: PDColor = blackColor
        ) {
            contentStream.setFont(font, fontSize)
            contentStream.setNonStrokingColor(color)
            contentStream.beginText()
            contentStream.newLineAtOffset(x, y)
            contentStream.showText(text)
            contentStream.endText()
        }

        // Helper function to draw rectangle with color
        fun drawRect(x: Float, y: Float, width: Float, height: Float, color: PDColor) {
            contentStream.setNonStrokingColor(color)
            contentStream.addRect(x, y, width, height)
            contentStream.fill()
        }

        // Helper function to draw line
        fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, color: PDColor) {
            contentStream.setStrokingColor(color)
            contentStream.moveTo(x1, y1)
            contentStream.lineTo(x2, y2)
            contentStream.stroke()
        }

        // Draw header
        drawText("INVOICE", centerX - 25f, yPos, PDType1Font.HELVETICA_BOLD, 14f)

        yPos -= 20

        // Draw company name
        drawText("Gobind Traders", centerX - 50f, yPos, PDType1Font.HELVETICA_BOLD, 12f)

        yPos -= 15

        // Draw company subtitle
        drawText("Gas & Cylinder Suppliers", centerX - 65f, yPos, PDType1Font.HELVETICA, 10f, grayColor)

        yPos -= 15

        // Draw phone number
        drawText("+91 8194963318", centerX - 45f, yPos, PDType1Font.HELVETICA, 10f, grayColor)

        yPos -= 20

        // Draw horizontal line
        drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, grayColor)

        yPos -= 25

        // Draw invoice details
        drawText("Invoice Date:", leftMargin, yPos, PDType1Font.HELVETICA, 10f)
        drawText("Invoice Number:", pageWidth - leftMargin - 150, yPos, PDType1Font.HELVETICA, 10f)

        yPos -= 20

        drawText(formattedDateTime.split(" ")[0], leftMargin, yPos, PDType1Font.HELVETICA_BOLD, 10f)
        drawText(dateTime.replace(":", "-").take(15), pageWidth - leftMargin - 150, yPos, PDType1Font.HELVETICA_BOLD, 10f)

        yPos -= 30

        // Draw customer information
        drawText("Customer:", leftMargin, yPos, PDType1Font.HELVETICA, 10f)
        drawText("Payment Method:", pageWidth - leftMargin - 150, yPos, PDType1Font.HELVETICA, 10f)

        yPos -= 20

        drawText(customerName, leftMargin, yPos, PDType1Font.HELVETICA_BOLD, 10f)

        // Payment mode logic
        val cashAmount = transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
        val creditAmount = transactionDetails?.credit?.toDoubleOrNull() ?: 0.0

        val paymentMode = when {
            cashAmount > 0 && creditAmount > 0 -> "Cash + Credit"
            cashAmount > 0 -> "Cash"
            creditAmount > 0 -> "Credit"
            else -> "N/A"
        }

        drawText(paymentMode, pageWidth - leftMargin - 150, yPos, PDType1Font.HELVETICA_BOLD, 10f)

        yPos -= 20

        // Draw customer phone if available
        customerDetails?.get("Phone Number")?.let { phone ->
            drawText(phone, leftMargin, yPos, PDType1Font.HELVETICA, 10f)
            yPos -= 20
        }

        // Check if we need to start a new page for items section
        if (yPos < 400) { // Need enough space for headers and first few items
            contentStream = createNewPage()
            yPos = pageHeight - 50f
        }

        // Draw items header - blue background
        drawRect(leftMargin, yPos - 30, pageWidth - (2 * leftMargin), 30f, blueColor)
        drawText("ITEMS", leftMargin + 10, yPos - 15, PDType1Font.HELVETICA_BOLD, 12f, whiteColor)
        yPos -= 30

        // Draw column headers - light gray background
        drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f, lightGrayColor)

        // Column header texts
        drawText("Description", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)
        drawText("Qty", leftMargin + 250, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)
        drawText("Rate", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)
        drawText("Amount", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

        yPos -= 25

        // Function to check and create a new page if needed
        fun checkNewPage(requiredSpace: Float = 25f): Boolean {
            if (yPos < (requiredSpace + 50)) {
                contentStream = createNewPage()

                // Add "continued" indicator
                drawText("(continued from previous page)", centerX - 80, yPos, PDType1Font.HELVETICA_BOLD, 10f)
                yPos -= 20

                // Redraw column headers
                drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f, lightGrayColor)
                drawText("Description", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)
                drawText("Qty", leftMargin + 250, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)
                drawText("Rate", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)
                drawText("Amount", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

                yPos -= 25
                return true
            }
            return false
        }

        // Draw cylinders issued
        transactionDetails?.cylindersIssued?.let { cylinders ->
            if (cylinders.isNotEmpty()) {
                checkNewPage()

                // Section header
                // Section header
                drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f,
                    PDColor(floatArrayOf(240f/255f, 240f/255f, 240f/255f), PDDeviceRGB.INSTANCE))
                drawText("Cylinders Issued", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

                yPos -= 25

                for (cylinder in cylinders) {
                    checkNewPage()

                    val description = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"} (S.No: ${cylinder["Serial Number"] ?: "N/A"})"
                    val price = cylinder["Price"] ?: "0"

                    drawText(description, leftMargin + 10, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("1", leftMargin + 250, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("Rs.$price", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("Rs.$price", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA, 10f)

                    yPos -= 25

                    // Draw a light gray line
                    drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, lightGrayColor)
                }
            }
        }

        // Draw LPG Issued
        transactionDetails?.lpgIssued?.let { lpgItems ->
            if (lpgItems.isNotEmpty()) {
                checkNewPage()

                // Section header
                // Section header
                drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f,
                    PDColor(floatArrayOf(240f/255f, 240f/255f, 240f/255f), PDDeviceRGB.INSTANCE))
                drawText("LPG Issued", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

                yPos -= 25

                for (lpgItem in lpgItems) {
                    checkNewPage()

                    val description = "LPG ${lpgItem["Volume Type"] ?: "Unknown"}"
                    val quantity = lpgItem["Quantity"] ?: "1"
                    val price = lpgItem["Price"] ?: "0"
                    val total = (quantity.toIntOrNull() ?: 1) * (price.toDoubleOrNull() ?: 0.0)

                    drawText(description, leftMargin + 10, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText(quantity, leftMargin + 250, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("Rs.$price", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("Rs.$total", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA, 10f)

                    yPos -= 25

                    // Draw a light gray line
                    drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, lightGrayColor)
                }
            }
        }

        // Draw LPG Returned
        transactionDetails?.lpgReturned?.let { lpgItems ->
            if (lpgItems.isNotEmpty()) {
                checkNewPage()

                // Section header
                drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f,
                    PDColor(floatArrayOf(240f/255f, 240f/255f, 240f/255f), PDDeviceRGB.INSTANCE))
                drawText("LPG Returned", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

                yPos -= 25

                for (lpgItem in lpgItems) {
                    checkNewPage()

                    val description = "LPG ${lpgItem["Volume Type"] ?: "Unknown"} (Returned)"
                    val quantity = lpgItem["Quantity"] ?: "1"

                    drawText(description, leftMargin + 10, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText(quantity, leftMargin + 250, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("-", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("-", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA, 10f)

                    yPos -= 25

                    // Draw a light gray line
                    drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, lightGrayColor)
                }
            }
        }

        // Draw Cylinders Returned
        transactionDetails?.cylindersReturned?.let { cylinders ->
            if (cylinders.isNotEmpty()) {
                checkNewPage()

                // Section header

                drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f,
                    PDColor(floatArrayOf(240f/255f, 240f/255f, 240f/255f), PDDeviceRGB.INSTANCE))
                drawText("Cylinders Returned", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

                yPos -= 25

                for (cylinder in cylinders) {
                    checkNewPage()

                    val description = "${cylinder["Gas Type"] ?: "Unknown"} ${cylinder["Volume Type"] ?: "Unknown"} (S.No: ${cylinder["Serial Number"] ?: "N/A"}) (Returned)"

                    drawText(description, leftMargin + 10, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("1", leftMargin + 250, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("-", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("-", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA, 10f)

                    yPos -= 25

                    // Draw a light gray line
                    drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, lightGrayColor)
                }
            }
        }

        // Draw Inventory Items
        transactionDetails?.inventoryIssued?.let { items ->
            if (items.isNotEmpty()) {
                checkNewPage()

                // Section header
                drawRect(leftMargin, yPos - 25, pageWidth - (2 * leftMargin), 25f,
                    PDColor(floatArrayOf(240f/255f, 240f/255f, 240f/255f), PDDeviceRGB.INSTANCE))
                drawText("Inventory Items", leftMargin + 10, yPos - 17, PDType1Font.HELVETICA_BOLD, 10f)

                yPos -= 25

                for (item in items) {
                    checkNewPage()

                    val name = item["Name"] ?: "Unknown Item"
                    val quantity = item["Quantity"] ?: "1"
                    val totalPrice = item["Price"]?.toDoubleOrNull() ?: 0.0
                    val quantityNum = quantity.toIntOrNull() ?: 1
                    val unitPrice = if (quantityNum > 0) totalPrice / quantityNum else totalPrice

                    drawText(name, leftMargin + 10, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText(quantity, leftMargin + 250, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("Rs.${unitPrice}", leftMargin + 300, yPos - 17, PDType1Font.HELVETICA, 10f)
                    drawText("Rs.${totalPrice}", leftMargin + 370, yPos - 17, PDType1Font.HELVETICA, 10f)

                    yPos -= 25

                    // Draw a light gray line
                    drawLine(leftMargin, yPos, pageWidth - leftMargin, yPos, lightGrayColor)
                }
            }
        }

        // Skip some space before totals
        yPos -= 15

        // Check if we need a new page for totals section
        if (yPos < 200) { // We need enough space for all the totals, payment info, and footer
            contentStream = createNewPage()
            yPos -= 30
        }

        // Draw totals
        drawText("Subtotal:", leftMargin + 300, yPos, PDType1Font.HELVETICA, 10f)
        drawText("Rs.${(subtotal * 100).toInt() / 100.0}", leftMargin + 370, yPos, PDType1Font.HELVETICA, 10f)

        yPos -= 25

        // Draw delivery if applicable
        if (deliveryAmount > 0) {
            drawText("Delivery:", leftMargin + 300, yPos, PDType1Font.HELVETICA, 10f)
            drawText("Rs.${(deliveryAmount * 100).toInt() / 100.0}", leftMargin + 370, yPos, PDType1Font.HELVETICA, 10f)
            yPos -= 25
        }

        // Draw a divider line
        drawLine(leftMargin + 300, yPos, pageWidth - leftMargin, yPos, blackColor)

        yPos -= 25

        // Draw grand total
        drawText("Grand Total:", leftMargin + 300, yPos, PDType1Font.HELVETICA_BOLD, 10f)
        drawText("Rs.${kotlin.math.round(grandTotal * 100) / 100.0}", leftMargin + 370, yPos, PDType1Font.HELVETICA_BOLD, 10f)

        yPos -= 30

        // Draw payment details
        val cashPaid = transactionDetails?.cash?.toDoubleOrNull() ?: 0.0
        if (cashPaid > 0) {
            drawText("Cash Paid:", leftMargin + 300, yPos, PDType1Font.HELVETICA, 10f)
            drawText("Rs.${transactionDetails?.cash ?: "0"}", leftMargin + 370, yPos, PDType1Font.HELVETICA, 10f)
            yPos -= 20
        }

        val creditAmount2 = transactionDetails?.credit?.toDoubleOrNull() ?: 0.0
        if (creditAmount2 > 0) {
            drawText("Credit Amount:", leftMargin + 300, yPos, PDType1Font.HELVETICA, 10f, redColor)
            drawText("Rs.${transactionDetails?.credit ?: "0"}", leftMargin + 370, yPos, PDType1Font.HELVETICA, 10f, redColor)
            yPos -= 20
        }

        val cashOut = transactionDetails?.CashOut?.toDoubleOrNull() ?: 0.0
        if (cashOut > 0) {
            drawText("Cash Out:", leftMargin + 300, yPos, PDType1Font.HELVETICA, 10f, redColor)
            drawText("Rs.${transactionDetails?.CashOut ?: "0"}", leftMargin + 370, yPos, PDType1Font.HELVETICA, 10f, redColor)
            yPos -= 20
        }

        // Draw thank you note at the bottom
        yPos = 100f
        drawText("Thank you for your business!", centerX - 65f, yPos, PDType1Font.HELVETICA_BOLD, 10f)
        yPos -= 20
        drawText("For any queries, please contact us.", centerX - 85f, yPos, PDType1Font.HELVETICA, 10f, grayColor)

        // Add page numbers to all pages
        for (i in 0 until document.numberOfPages) {
            val tempPage = document.getPage(i)
            val tempStream = PDPageContentStream(document, tempPage, PDPageContentStream.AppendMode.APPEND, false)

            tempStream.setFont(PDType1Font.HELVETICA, 10f)
            val pageText = "Page ${i + 1} of ${document.numberOfPages}"

            tempStream.beginText()
            tempStream.newLineAtOffset(pageWidth - leftMargin - 60, 30f)
            tempStream.showText(pageText)
            tempStream.endText()

            tempStream.close()
        }

        // Close the main content stream
        contentStream.close()

        // Create directory for output if it doesn't exist
        val downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads").toFile()
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        // Save the PDF to the Downloads directory
        val fileName = "Invoice_${customerName.replace(" ", "_")}_${dateTime.replace(":", "-").take(15)}.pdf"
        val file = File(downloadsDir, fileName)
        document.save(file)
        document.close()

        // Return the path to the generated PDF
        file.absolutePath
    }

        /**
         * Open the generated PDF file using the Desktop API
         */
        fun openPdf(filePath: String?) {
            if (filePath == null) return

            try {
                val file = File(filePath)
                if (file.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                }
            } catch (e: Exception) {
                println("Error opening PDF: ${e.message}")
                e.printStackTrace()
            }
        }


/**
 * Fetch transaction details from Firestore for desktop platforms
 */
