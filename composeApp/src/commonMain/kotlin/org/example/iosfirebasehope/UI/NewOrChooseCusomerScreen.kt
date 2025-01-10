package org.example.iosfirebasehope.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import iosfirebasehope.composeapp.generated.resources.Res
import iosfirebasehope.composeapp.generated.resources.baseline_add_box_24
import iosfirebasehope.composeapp.generated.resources.baseline_auto_mode_24
import iosfirebasehope.composeapp.generated.resources.baseline_autorenew_24

import org.example.iosfirebasehope.navigation.components.BillScreenComponent
import org.example.iosfirebasehope.navigation.components.NewOrChooseCustomerScreenComponent
import org.example.iosfirebasehope.navigation.events.BillScreenEvent
import org.example.iosfirebasehope.navigation.events.NewOrChooseCustomerScreenEvent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun NewOrChooseCustomerScreenUI(component: NewOrChooseCustomerScreenComponent, db: FirebaseFirestore) {
    var isVisible by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var isUploading = remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) } // State for upload message
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Auto-dismiss the message after 2 seconds
    LaunchedEffect(uploadMessage) {
        if (uploadMessage != null) {
            delay(2000) // Wait for 2 seconds
            uploadMessage = null // Clear the message
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF2f80eb),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { component.onEvent(NewOrChooseCustomerScreenEvent.OnBackClick) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "BILL",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isVisible && !showAddCustomerDialog,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                ActionCard2(
                    title = "Add New Customer",
                    iconResId = Res.drawable.baseline_add_box_24,
                    onClick = {
                        uploadMessage = null // Clear the message
                        showAddCustomerDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible && !showAddCustomerDialog,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                ActionCard2(
                    title = "Choose Existing Customer",
                    iconResId = Res.drawable.baseline_auto_mode_24,
                    onClick = { /* Handle click */ }
                )
            }

            // Display the upload message below the buttons
            if (uploadMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uploadMessage!!,
                    color = if (uploadMessage!!.startsWith("Customer")) Color.Green else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Add Customer Dialog
        if (showAddCustomerDialog) {
            AddCustomerDialog2(
                onDismiss = { showAddCustomerDialog = false },
                onAddCustomer = { customerDetails ->
                    isUploading.value = true
                    coroutineScope.launch {
                        val success = saveCustomerToFirestore2(db, customerDetails)
                        if (success) {
                            uploadMessage = "Customer added successfully!"
                        } else {
                            uploadMessage = "Failed to add customer. Please try again."
                        }
                        isUploading.value = false
                        showAddCustomerDialog = false
                    }
                },
                isUploading = isUploading
            )
        }
    }
}

@Composable
fun AddCustomerDialog2(
    onDismiss: () -> Unit,
    onAddCustomer: (Map<String, String>) -> Unit,
    isUploading: MutableState<Boolean>
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var referenceName by remember { mutableStateOf("") }
    var referenceMobile by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }

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
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title
                        Text(
                            text = "Add New Customer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Name Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone Number Field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        // Address Field
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // UID Field
                        OutlinedTextField(
                            value = uid,
                            onValueChange = { uid = it },
                            label = { Text("UID") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Reference Name Field
                        OutlinedTextField(
                            value = referenceName,
                            onValueChange = { referenceName = it },
                            label = { Text("Reference Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Reference Mobile Field
                        OutlinedTextField(
                            value = referenceMobile,
                            onValueChange = { referenceMobile = it },
                            label = { Text("Reference Mobile") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        // Deposit Field
                        OutlinedTextField(
                            value = deposit,
                            onValueChange = { deposit = it },
                            label = { Text("Deposit") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number // Ensure numeric input
                            )
                        )

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
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Add Button
                            Button(
                                onClick = {
                                    // Create a map of customer details
                                    val customerDetails = mapOf(
                                        "Name" to name,
                                        "Phone Number" to phoneNumber,
                                        "Address" to address,
                                        "UID" to uid,
                                        "Reference Name" to referenceName,
                                        "Reference Mobile" to referenceMobile,
                                        "Deposit" to deposit
                                    )
                                    onAddCustomer(customerDetails)
                                },
                                enabled = !isUploading.value,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb))
                            ) {
                                Text("Add", color = Color.White)
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


suspend fun saveCustomerToFirestore2(
    db: FirebaseFirestore,
    customerDetails: Map<String, String>
): Boolean {
    val customerName = customerDetails["Name"] ?: return false // Ensure customer name is not null
    val customerPhone = customerDetails["Phone Number"] ?: "" // Default to empty string if Phone Number is missing

    return try {
        val namesDocRef = db.collection("Customers").document("Names")
        val snapshot = namesDocRef.get()

        // Check if a customer with the same name already exists
        if (snapshot.exists) {
            val existingDetails = snapshot.get("CustomerDetails") as? List<Map<String, String>> ?: emptyList()
            if (existingDetails.any { it["Name"] == customerName }) {
                return false
            }
        }

        val detailsMap = customerDetails.filterKeys { it != "Name" }

        // Save to Names collection under Details document
        db.collection("Customers")
            .document("Details")
            .collection("Names")
            .document(customerName)
            .set(mapOf("Details" to detailsMap))

        // Create a document with an empty array of maps in Issued Cylinders collection
        db.collection("Customers")
            .document("Issued Cylinders")
            .collection("Names")
            .document(customerName)
            .set(mapOf("Details" to listOf<Map<String, Any>>()))

        // Create a collection with the current date-time in Transactions
        val transactionsRef = db.collection("Customers")
            .document("Transactions")
            .collection("Names")
            .document(customerName)

        transactionsRef.set(emptyMap<String, Any>())

        // Add new customer entry to the Names document
        val customerEntry = mapOf("Name" to customerName, "Phone Number" to customerPhone)

        if (snapshot.exists) {
            val existingDetails = snapshot.get("CustomerDetails") as? MutableList<Map<String, String>> ?: mutableListOf()
            existingDetails.add(customerEntry)
            namesDocRef.set(mapOf("CustomerDetails" to existingDetails))
        } else {
            namesDocRef.set(mapOf("CustomerDetails" to listOf(customerEntry)))
        }

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


@Composable
fun ActionCard2(
    title: String,
    iconResId: DrawableResource, // Drawable resource ID
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Cards take 80% of the screen width
            .height(150.dp) // Double the height
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(2.dp, Color(0xAA2f80eb)) // Add border with color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // Center content vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
        ) {
            // Text above the icon
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp) // Space between text and icon
            )

            // Icon below the text (using painterResource)
            Icon(
                painter = painterResource(
                    resource = iconResId
                ), // Load drawable resource
                contentDescription = title,
                tint = Color(0xFF2f80eb),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}