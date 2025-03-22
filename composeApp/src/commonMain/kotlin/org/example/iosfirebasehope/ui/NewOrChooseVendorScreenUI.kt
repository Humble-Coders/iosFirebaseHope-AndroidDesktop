package org.example.iosfirebasehope.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.gitlive.firebase.firestore.FirebaseFirestore
import iosfirebasehope.composeapp.generated.resources.Res
import iosfirebasehope.composeapp.generated.resources.baseline_add_box_24
import iosfirebasehope.composeapp.generated.resources.baseline_auto_mode_24
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.NewOrChooseVendorScreenComponent
import org.example.iosfirebasehope.navigation.events.NewOrChooseVendorScreenEvent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun NewOrChooseVendorScreenUI(component: NewOrChooseVendorScreenComponent, db: FirebaseFirestore) {
    var isVisible by remember { mutableStateOf(false) }
    var showAddVendorDialog by remember { mutableStateOf(false) }
    var sendForRefillingVendorDialog by remember { mutableStateOf(false) } // State for choose Vendor dialog
    var recieveRefillingVendorDialog by remember { mutableStateOf(false) } // State for choose Vendor dialog
    var isUploading = remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var Vendors by remember { mutableStateOf<List<String>>(listOf("Vendor 1", "Vendor 2", "Vendor 3")) }
    var selectedVendor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isVisible = true
    }
    LaunchedEffect(Unit) {
        Vendors = fetchVendors(db)
    }

    // Auto-dismiss the message after 2 seconds
    LaunchedEffect(uploadMessage) {
        if (uploadMessage != null) {
            Vendors= fetchVendors(db)
            delay(2000)
            uploadMessage = null
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { component.onEvent(NewOrChooseVendorScreenEvent.OnBackClick) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Refill",
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
                visible = isVisible && !showAddVendorDialog,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                ActionCardVendor(
                    title = "Add New Vendor",
                    iconResId = Res.drawable.baseline_add_box_24,
                    onClick = {
                        uploadMessage = null
                        showAddVendorDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible && !showAddVendorDialog,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                ActionCardVendor(
                    title = "Send For Refilling",
                    iconResId = Res.drawable.baseline_auto_mode_24,
                    onClick = { sendForRefillingVendorDialog = true } // Show dialog
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible && !showAddVendorDialog,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                ActionCardVendor(
                    title = "Receive Refilling",
                    iconResId = Res.drawable.baseline_auto_mode_24,
                    onClick = { recieveRefillingVendorDialog = true } // Show dialog
                )
            }

            if (uploadMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uploadMessage!!,
                    color = if (uploadMessage!!.startsWith("Vendor")) Color.Green else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        // Add Vendor Dialog
        if (showAddVendorDialog) {
            AddVendorDialog(
                onDismiss = { showAddVendorDialog = false },
                onAddVendor = { VendorDetails ->
                    isUploading.value = true
                    coroutineScope.launch {
                        val success = saveVendorToFirestore(db, VendorDetails)
                        if (success) {
                            uploadMessage = "Vendor added successfully!"
                        } else {
                            uploadMessage = "Failed to add Vendor. Please try again."
                        }
                        isUploading.value = false
                        showAddVendorDialog = false
                    }
                },
                isUploading = isUploading,
                component = component
            )
        }

        // Dialog for choosing a Vendor
        if (sendForRefillingVendorDialog) {
            Dialog(onDismissRequest = { sendForRefillingVendorDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Select Vendor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SearchableDropdownVendor(
                            options = Vendors,
                            selectedItem = selectedVendor,
                            onItemSelected = {
                                selectedVendor = it
                            },
                            onClearSelection = {
                                selectedVendor = null // Reset when input is cleared
                            },
                            placeholder = "Search Vendor...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { sendForRefillingVendorDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(
                                onClick = {
                                    component.onEvent(NewOrChooseVendorScreenEvent.OnChooseVendorClick(selectedVendor!!))
                                },
                                enabled = !selectedVendor.isNullOrBlank() // Disable button if input is cleared
                            ) {
                                Text(
                                    "Choose",
                                    color = if (!selectedVendor.isNullOrBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        if (recieveRefillingVendorDialog) {
            Dialog(onDismissRequest = { recieveRefillingVendorDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Select Vendor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SearchableDropdownVendor(
                            options = Vendors,
                            selectedItem = selectedVendor,
                            onItemSelected = {
                                selectedVendor = it
                            },
                            onClearSelection = {
                                selectedVendor = null // Reset when input is cleared
                            },
                            placeholder = "Search Vendor...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { recieveRefillingVendorDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(
                                onClick = {
                                    component.onEvent(NewOrChooseVendorScreenEvent.OnRecieveClick(selectedVendor!!))
                                },
                                enabled = !selectedVendor.isNullOrBlank() // Disable button if input is cleared
                            ) {
                                Text(
                                    "Choose",
                                    color = if (!selectedVendor.isNullOrBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun SearchableDropdownVendor(
    options: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    onClearSelection: () -> Unit = {}, // Added callback for clearing selection
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedItem ?: "") }
    val filteredOptions = options.filter { it.contains(searchQuery, ignoreCase = true) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expanded = it.isNotEmpty()
                    if (it.isEmpty()) {
                        onClearSelection() // Notify parent when input is cleared
                    }
                },
                label = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface)
                        .heightIn(max = 200.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        if (filteredOptions.isEmpty()) {
                            item {
                                Text(
                                    "No options found",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(filteredOptions.size) { index ->
                                val option = filteredOptions[index]
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = option
                                            onItemSelected(option)
                                            expanded = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AddVendorDialog(
    onDismiss: () -> Unit,
    onAddVendor: (Map<String, String>) -> Unit,
    isUploading: MutableState<Boolean>,
    component: NewOrChooseVendorScreenComponent
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }

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
                            text = "Add New Vendor",
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


                        // Reference Mobile Field



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

                                    // Create a map of Vendor details
                                    val VendorDetails = mapOf(
                                        "Name" to name,
                                        "Phone Number" to phoneNumber,
                                        "Address" to address,
                                        "UID" to uid,
                                        "Credit" to ""
                                    )
                                    onAddVendor(VendorDetails)

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


suspend fun saveVendorToFirestore(
    db: FirebaseFirestore,
    VendorDetails: Map<String, String>
): Boolean {
    val VendorName = VendorDetails["Name"] ?: return false // Ensure Vendor name is not null
    val VendorPhone = VendorDetails["Phone Number"] ?: "" // Default to empty string if Phone Number is missing

    return try {
        val namesDocRef = db.collection("Vendors").document("Names")
        val snapshot = namesDocRef.get()

        // Check if a Vendor with the same name already exists
        if (snapshot.exists) {
            val existingDetails = snapshot.get("VendorDetails") as? List<Map<String, String>> ?: emptyList()
            if (existingDetails.any { it["Name"] == VendorName }) {
                return false
            }
        }

        val detailsMap = VendorDetails.filterKeys { it != "Name" }

        // Save to Names collection under Details document
        db.collection("Vendors")
            .document("Details")
            .collection("Names")
            .document(VendorName)
            .set(mapOf("Details" to detailsMap))

        // Create a document with an empty array of maps in Issued Cylinders collection
        db.collection("Vendors")
            .document("Issued Cylinders")
            .collection("Names")
            .document(VendorName)
            .set(mapOf("Details" to listOf<Map<String, String>>()))

        val transactionsRef = db.collection("Vendors")
            .document("Transactions")
            .collection("Names")
            .document(VendorName)

        transactionsRef.set(emptyMap<String, Any>())

        // Add new Vendor entry to the Names document
        val VendorEntry = mapOf("Name" to VendorName, "Phone Number" to VendorPhone)

        if (snapshot.exists) {
            val existingDetails = snapshot.get("VendorDetails") as? MutableList<Map<String, String>> ?: mutableListOf()
            existingDetails.add(VendorEntry)
            namesDocRef.set(mapOf("VendorDetails" to existingDetails))
        } else {
            namesDocRef.set(mapOf("VendorDetails" to listOf(VendorEntry)))
        }

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


@Composable
fun ActionCardVendor(
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


suspend fun fetchVendors(db: FirebaseFirestore): List<String> {
    return try {
        // Navigate to the target document
        val VendorDetailsRef = db.collection("Vendors")
            .document("Names")
            .get()

        // Extract the VendorDetails array from the document
        val VendorDetails = VendorDetailsRef.get("VendorDetails") as? List<Map<String, String>> ?: emptyList()

        // Map the "Name" fields from each map in the array
        VendorDetails.mapNotNull { it["Name"] }
    } catch (e: Exception) {
        println("Error fetching Vendors: ${e.message}")
        emptyList()
    }
}