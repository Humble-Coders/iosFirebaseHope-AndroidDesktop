package org.example.iosfirebasehope.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.NotificationScreenComponent
import org.example.iosfirebasehope.navigation.events.NotificationScreenEvent

// Data class for better type safety
data class CylinderNotification(
    val serialNumber: String,
    val gasType: String,
    val volumeType: String,
    val notificationDate: String,
    val currentlyIssuedTo: String,
    val issueDate: String,
    val sellingPrice: String,
    val daysRemaining: String
)

@Composable
fun NotificationScreenUI(
    component: NotificationScreenComponent,
    db: FirebaseFirestore
) {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var notifications by remember { mutableStateOf<List<CylinderNotification>>(emptyList()) }
    var filteredNotifications by remember { mutableStateOf<List<CylinderNotification>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedSerialNumber by remember { mutableStateOf("") }
    var daysToExtend by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Function to load data efficiently with async
    fun loadNotifications() {
        isLoading = true
        coroutineScope.launch {
            try {
                val loadedNotifications = withContext(Dispatchers.IO) {
                    fetchOverdueCylindersAsync(db, currentDate)
                }
                notifications = loadedNotifications
                // Initialize filtered notifications with all notifications
                filteredNotifications = loadedNotifications
            } catch (e: Exception) {
                println("Error loading notifications: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadNotifications()
    }

    // Filter notifications based on search query
    LaunchedEffect(searchQuery, notifications) {
        if (searchQuery.isEmpty()) {
            filteredNotifications = notifications
        } else {
            filteredNotifications = notifications.filter { notification ->
                notification.serialNumber.contains(searchQuery, ignoreCase = true) ||
                        notification.currentlyIssuedTo.contains(searchQuery, ignoreCase = true) ||
                        notification.gasType.contains(searchQuery, ignoreCase = true) ||
                        notification.volumeType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(NotificationScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by serial number, customer, gas type...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF2f80eb)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF2f80eb)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF2f80eb),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF2f80eb)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp),
                        color = Color(0xFF2f80eb)
                    )
                } else {
                    if (filteredNotifications.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(filteredNotifications) { notification ->
                                NotificationCylinderCard(
                                    currentlyIssuedTo = notification.currentlyIssuedTo,
                                    issueDate = notification.issueDate,
                                    sellingPrice = notification.sellingPrice,
                                    volumeType = notification.volumeType,
                                    gasType = notification.gasType,
                                    serialNumber = notification.serialNumber,
                                    daysRemaining = notification.daysRemaining,
                                    onExtendDueDays = {
                                        selectedSerialNumber = notification.serialNumber
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (searchQuery.isEmpty()) "No notifications found."
                            else "No matches found for \"$searchQuery\"",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    "Extend Due Days",
                    color = Color(0xFF2E7D32)
                )
            },
            text = {
                Column {
                    Text(
                        "Enter the number of days to extend:",
                        color = Color(0xFF2E7D32)
                    )
                    TextField(
                        value = daysToExtend,
                        onValueChange = { daysToExtend = it },
                        label = { Text("Days", color = Color(0xFF2E7D32)) },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.Black,
                            backgroundColor = Color.White,
                            cursorColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color(0xFF2E7D32)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = daysToExtend.toIntOrNull() ?: 0
                        if (days > 0) {
                            coroutineScope.launch {
                                try {
                                    extendNotificationDate(db, selectedSerialNumber, currentDate, days)
                                    loadNotifications() // Reload data after update
                                } catch (e: Exception) {
                                    println("Error updating notification date: ${e.message}")
                                }
                            }
                            showDialog = false
                            daysToExtend = "" // Reset the input field
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Extend")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        daysToExtend = "" // Reset the input field
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Cancel")
                }
            },
            backgroundColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF2E7D32)
        )
    }
}

// Async implementation for fetching cylinders
private suspend fun fetchOverdueCylindersAsync(
    db: FirebaseFirestore,
    currentDate: LocalDate
): List<CylinderNotification> = withContext(Dispatchers.IO) {
    try {
        // First, get the main cylinder document - this needs to be sequential
        val documentRef = db.collection("Cylinders").document("Cylinders")
        val documentSnapshot = documentRef.get()

        if (!documentSnapshot.exists) return@withContext emptyList()

        val cylinderDetails = documentSnapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

        // Filter to only process cylinders with overdue notifications
        val overdueSerialNumbers = cylinderDetails.mapNotNull { cylinder ->
            val notificationDate = cylinder["Notifications Date"] ?: return@mapNotNull null
            try {
                val parsedNotificationDate = LocalDate.parse(notificationDate)

                // Only process overdue cylinders
                if (parsedNotificationDate <= currentDate) {
                    Pair(
                        cylinder["Serial Number"] ?: return@mapNotNull null,
                        CylinderInfo(
                            gasType = cylinder["Gas Type"] ?: "N/A",
                            volumeType = cylinder["Volume Type"] ?: "N/A",
                            notificationDate = notificationDate,
                            daysOverdue = parsedNotificationDate.daysUntil(currentDate).toString()
                        )
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

        // Process customer info in parallel
        if (overdueSerialNumbers.isEmpty()) return@withContext emptyList()

        // Use async to fetch customer info for all cylinders in parallel
        val deferredResults = overdueSerialNumbers.map { (serialNumber, cylinderInfo) ->
            async {
                try {
                    val currentlyIssuedToDoc = db.collection("Cylinders")
                        .document("Customers")
                        .collection(serialNumber)
                        .document("Currently Issued To")
                        .get()

                    if (currentlyIssuedToDoc.exists) {
                        val name = currentlyIssuedToDoc.get("name") as? String ?: "N/A"
                        val date = currentlyIssuedToDoc.get("date") as? String ?: "N/A"
                        val price = currentlyIssuedToDoc.get("price") as? String ?: "N/A"

                        CylinderNotification(
                            serialNumber = serialNumber,
                            gasType = cylinderInfo.gasType,
                            volumeType = cylinderInfo.volumeType,
                            notificationDate = cylinderInfo.notificationDate,
                            currentlyIssuedTo = name,
                            issueDate = date,
                            sellingPrice = price,
                            daysRemaining = cylinderInfo.daysOverdue
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }

        // Wait for all parallel operations to complete and filter out nulls
        val results = deferredResults.awaitAll().filterNotNull()

        // Sort by days overdue (descending)
        results.sortedByDescending { it.daysRemaining.toIntOrNull() ?: 0 }
    } catch (e: Exception) {
        println("Error fetching cylinder details: ${e.message}")
        emptyList()
    }
}

// Helper class for intermediate cylinder data
private data class CylinderInfo(
    val gasType: String,
    val volumeType: String,
    val notificationDate: String,
    val daysOverdue: String
)

// Function to extend notification date
private suspend fun extendNotificationDate(
    db: FirebaseFirestore,
    serialNumber: String,
    currentDate: LocalDate,
    days: Int
) = withContext(Dispatchers.IO) {
    try {
        val documentRef = db.collection("Cylinders").document("Cylinders")
        val documentSnapshot = documentRef.get()

        if (!documentSnapshot.exists) return@withContext

        val cylinderDetails = documentSnapshot.get("CylinderDetails") as? List<Map<String, String>> ?: return@withContext
        val newNotificationDate = currentDate.plus(DatePeriod(days = days))

        val updatedCylinderDetails = cylinderDetails.map { cylinder ->
            if (cylinder["Serial Number"] == serialNumber) {
                val mutableCylinder = cylinder.toMutableMap()
                mutableCylinder["Notifications Date"] = newNotificationDate.toString()
                mutableCylinder
            } else {
                cylinder
            }
        }

        documentRef.update(mapOf("CylinderDetails" to updatedCylinderDetails))
    } catch (e: Exception) {
        println("Error extending notification date: ${e.message}")
    }
}

@Composable
private fun NotificationCylinderCard(
    currentlyIssuedTo: String,
    issueDate: String,
    sellingPrice: String,
    volumeType: String,
    gasType: String,
    serialNumber: String,
    daysRemaining: String,
    onExtendDueDays: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFEEEEEE)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Cylinder Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Serial Number: $serialNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Currently Issued To: $currentlyIssuedTo",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Issue Date: $issueDate",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Selling Price: Rs. $sellingPrice",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Volume Type: $volumeType",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            // Right Side: Due Days and Extend Button as a Card
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .padding(start = 16.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                backgroundColor = Color(0xFFfa3737)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Due By:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "$daysRemaining day(s)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = Color(0xFFFFFFFF),
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = onExtendDueDays,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Text("Extend Days")
                    }
                }
            }
        }
    }
}