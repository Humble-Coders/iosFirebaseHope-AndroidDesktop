package org.example.iosfirebasehope.UI

import dev.gitlive.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.IssueCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.IssueCylinderScreenEvent

@Composable
fun IssueNewCylinderScreenUI(component: IssueCylinderScreenComponent, db: FirebaseFirestore) {
    var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility
    val coroutineScope = rememberCoroutineScope()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF2f80eb), // TopAppBar background color
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Back Button
                    IconButton(
                        onClick = { component.onEvent(IssueCylinderScreenEvent.OnBackClick) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Title
                    Text(
                        text = "Issue New Cylinder",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        snackbarHost = {
            // Snackbar host to display messages
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add New Customer Button
            Button(
                onClick = { showDialog = true }, // Show dialog when clicked
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add New Customer",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            // Dialog Box for Adding New Customer
            if (showDialog) {
                AddCustomerDialog(
                    onDismiss = { showDialog = false }, // Hide dialog on dismiss
                    onAddCustomer = { customerDetails ->
                        coroutineScope.launch {
                            val success = saveCustomerToFirestore(db, customerDetails) // Save customer details to Firestore
                            if (success) {
                                // Show Snackbar on success
                                snackbarHostState.showSnackbar("Customer added successfully!")
                            } else {
                                // Show Snackbar on failure
                                snackbarHostState.showSnackbar("Failed to add customer.")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAddCustomer: (Map<String, String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var referenceName by remember { mutableStateOf("") }
    var referenceMobile by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Create a map of customer details
                    val customerDetails = mapOf(
                        "Name" to name,
                        "PhoneNumber" to phoneNumber,
                        "Address" to address,
                        "UID" to uid,
                        "ReferenceName" to referenceName,
                        "ReferenceMobile" to referenceMobile
                    )
                    onAddCustomer(customerDetails) // Pass details to the callback
                    onDismiss() // Close the dialog
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb))
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss // Close the dialog
            ) {
                Text("Cancel", color = Color(0xFF2f80eb))
            }
        }
    )
}

private suspend fun saveCustomerToFirestore(db: FirebaseFirestore, customerDetails: Map<String, String>): Boolean {
    val customerName = customerDetails["Name"] ?: return false // Ensure customer name is not null

    return try {
        // Fetch the existing document
        val document = db.collection("Customers")
            .document(customerName)
            .get()

        if (document.exists) {
            // If the customer exists, fetch the existing "CustomerDetails" array
            val existingDetails = document.get("CustomerDetails") as? List<Map<String, String>> ?: emptyList()

            // Add the new customer details to the array
            val updatedDetails = existingDetails + customerDetails

            // Update the document with the new array
            db.collection("Customers")
                .document(customerName)
                .set(mapOf("CustomerDetails" to updatedDetails))
            true // Return true on success
        } else {
            // If the customer does not exist, create a new document with the "CustomerDetails" array
            db.collection("Customers")
                .document(customerName)
                .set(mapOf("CustomerDetails" to listOf(customerDetails)))
            true // Return true on success
        }
    } catch (e: Exception) {
        // Handle failure
        println("Error saving customer: ${e.message}")
        false // Return false on failure
    }
}