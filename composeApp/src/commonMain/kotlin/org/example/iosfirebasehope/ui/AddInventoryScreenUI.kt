package org.example.iosfirebasehope.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.AddInventoryScreenComponent
import org.example.iosfirebasehope.navigation.events.AddInventoryScreenEvent

@Composable
fun AddInventoryScreenUI(component: AddInventoryScreenComponent, db: FirebaseFirestore) {
    val coroutineScope = rememberCoroutineScope()

    // State for the form fields
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var customUnit by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var cashPaid by remember { mutableStateOf("") }
    var creditAmount by remember { mutableStateOf("") }

    // State for vendor selection
    var vendors by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedVendor by remember { mutableStateOf<String?>(null) }
    var showVendorDialog by remember { mutableStateOf(false) }
    var showAddVendorDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    // State for status messages and loading indicators
    var isUploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Units dropdown
    var unitsDropdownExpanded by remember { mutableStateOf(false) }
    val units = listOf("Kg", "Inch", "Cm", "Other")

    // Auto-dismiss the status message after 2 seconds
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(2000)
            statusMessage = null
        }
    }

    // Fetch vendors when the screen loads
    LaunchedEffect(Unit) {
        vendors = fetchInventoryVendors(db)
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
                        onClick = { component.onEvent(AddInventoryScreenEvent.onBackClick) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Add Inventory",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vendor Selection Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Vendor Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showVendorDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = selectedVendor ?: "Select Vendor",
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Button(
                                onClick = { showAddVendorDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Add New Vendor",
                                    color = Color.White
                                )
                            }
                        }

                        if (selectedVendor != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selected: $selectedVendor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Item Details Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Item Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Item Name
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Item Name*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quantity and Units (side by side)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Quantity*") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Units*", modifier = Modifier.padding(bottom = 8.dp))
                                PrettyDropdownMenu(
                                    selectedItem = selectedUnit ?: "Select Unit",
                                    items = units,
                                    onItemSelected = { selectedUnit = it },
                                    onDropdownExpanded = { expanded -> unitsDropdownExpanded = expanded },
                                    dropdownExpanded = unitsDropdownExpanded
                                )
                            }
                        }

                        // Show custom unit field if "Other" is selected
                        if (selectedUnit == "Other") {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = customUnit,
                                onValueChange = { customUnit = it },
                                label = { Text("Custom Unit*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pricing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = costPrice,
                                onValueChange = { costPrice = it },
                                label = { Text("Cost Price*") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = salePrice,
                                onValueChange = { salePrice = it },
                                label = { Text("Sale Price*") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Add Item Button
            item {
                Button(
                    onClick = {
                        if (validateInputs(itemName, quantity, selectedUnit, customUnit, costPrice, salePrice, selectedVendor)) {
                            // Set the initial cash paid to be equal to the cost price
                            cashPaid = costPrice
                            creditAmount = "0"
                            showPaymentDialog = true
                        } else {
                            statusMessage = "Please fill all required fields."
                        }
                    },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "Add Item",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Status Message
            if (statusMessage != null) {
                item {
                    Surface(
                        color = if (statusMessage!!.startsWith("Failed")) Color(0xFFFFF3F0) else Color(0xFFF0FFF4),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = if (statusMessage!!.startsWith("Failed")) Color.Red else Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

        // Choose Vendor Dialog
        if (showVendorDialog) {
            Dialog(onDismissRequest = { showVendorDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Select Vendor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SearchableDropdownVendor(
                            options = vendors,
                            selectedItem = selectedVendor,
                            onItemSelected = {
                                selectedVendor = it
                            },
                            onClearSelection = {
                                selectedVendor = null
                            },
                            placeholder = "Search Vendor...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {


                            Row {
                                TextButton(
                                    onClick = { showVendorDialog = false }
                                ) {
                                    Text("Cancel", color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { showVendorDialog = false },
                                    enabled = !selectedVendor.isNullOrBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb))
                                ) {
                                    Text("Choose", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Vendor Dialog (improved version)
        if (showAddVendorDialog) {
            var isAddingVendor = remember { mutableStateOf(false) }

            AddInventoryVendorDialog(
                onDismiss = { showAddVendorDialog = false },
                onAddVendor = { vendorDetails ->
                    isAddingVendor.value = true
                    coroutineScope.launch {
                        val success = saveInventoryVendorToFirestore(db, vendorDetails)
                        if (success) {
                            statusMessage = "Vendor added successfully!"
                            vendors = fetchInventoryVendors(db)
                            selectedVendor = vendorDetails["Name"]
                        } else {
                            statusMessage = "Failed to add vendor. Please try again."
                        }
                        isAddingVendor.value = false
                        showAddVendorDialog = false
                    }
                },
                isUploading = isAddingVendor
            )
        }

        // Payment Details Dialog
        if (showPaymentDialog) {
            Dialog(onDismissRequest = { showPaymentDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Payment Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Divider()

                        // Item summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item:", fontWeight = FontWeight.Medium)
                            Text(itemName, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Quantity:", fontWeight = FontWeight.Medium)
                            Text("$quantity ${selectedUnit ?: ""}", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount:", fontWeight = FontWeight.Medium)
                            Text("₹${costPrice}", fontWeight = FontWeight.Bold, color = Color(0xFF2f80eb))
                        }

                        Divider()

                        // Cash Paid Field with auto calculation of credit
                        OutlinedTextField(
                            value = cashPaid,
                            onValueChange = { newValue ->
                                cashPaid = newValue
                                val cost = costPrice.toFloatOrNull() ?: 0f
                                val cash = newValue.toFloatOrNull() ?: 0f
                                creditAmount = (cost - cash).coerceAtLeast(0f).toString()
                            },
                            label = { Text("Cash Paid") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Credit Amount (read-only display)
                        OutlinedTextField(
                            value = creditAmount,
                            onValueChange = { },
                            label = { Text("Credit Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showPaymentDialog = false }
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    isUploading = true
                                    showPaymentDialog = false

                                    // Determine the unit to save
                                    val unitToSave = if (selectedUnit == "Other") customUnit else selectedUnit

                                    // Create the item map with payment details included
                                    val item = mapOf(
                                        "Name" to itemName,
                                        "Quantity" to quantity,
                                        "Units" to unitToSave!!,
                                        "Description" to description,
                                        "CostPrice" to costPrice,
                                        "Price" to salePrice,
                                        "Vendor" to selectedVendor!!,
                                        "CashPaid" to cashPaid,
                                        "CreditAmount" to creditAmount
                                    )

                                    coroutineScope.launch {
                                        val success = saveInventoryToFirestore(db, item)

                                        if (success) {
                                            // Update vendor credit if needed
                                            if (creditAmount.toFloatOrNull() ?: 0f > 0) {
                                                updateVendorCredit(db, selectedVendor!!, creditAmount)
                                            }

                                            statusMessage = "Item added successfully!"
                                            // Reset form fields
                                            resetForm(
                                                { itemName = it },
                                                { quantity = it },
                                                { selectedUnit = it },
                                                { customUnit = it },
                                                { description = it },
                                                { costPrice = it },
                                                { salePrice = it },
                                                { cashPaid = it },
                                                { creditAmount = it }
                                            )
                                        } else {
                                            statusMessage = "Failed to add item. Please try again."
                                        }

                                        isUploading = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb))
                            ) {
                                Text("Confirm", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddInventoryVendorDialog(
    onDismiss: () -> Unit,
    onAddVendor: (Map<String, String>) -> Unit,
    isUploading: MutableState<Boolean>
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {

                    Text(
                        text = "Add New Vendor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Divider()

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vendor Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Phone Number Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(8.dp)
                )

                // Address Field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp)
                )

                // UID Field
                OutlinedTextField(
                    value = uid,
                    onValueChange = { uid = it },
                    label = { Text("UID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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

                    // Add Button
                    Button(
                        onClick = {
                            // Create a map of vendor details
                            val vendorDetails = mapOf(
                                "Name" to name,
                                "Phone Number" to phoneNumber,
                                "Address" to address,
                                "UID" to uid,
                                "Credit" to "0"
                            )
                            onAddVendor(vendorDetails)
                        },
                        enabled = !isUploading.value && name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        if (isUploading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Add Vendor", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Function to validate inputs
private fun validateInputs(
    itemName: String,
    quantity: String,
    selectedUnit: String?,
    customUnit: String,
    costPrice: String,
    salePrice: String,
    selectedVendor: String?
): Boolean {
    return itemName.isNotBlank() &&
            quantity.isNotBlank() &&
            costPrice.isNotBlank() &&
            salePrice.isNotBlank() &&
            selectedVendor != null &&
            (selectedUnit != null && (selectedUnit != "Other" || customUnit.isNotBlank()))
}

// Function to reset form fields
private fun resetForm(
    setItemName: (String) -> Unit,
    setQuantity: (String) -> Unit,
    setSelectedUnit: (String?) -> Unit,
    setCustomUnit: (String) -> Unit,
    setDescription: (String) -> Unit,
    setCostPrice: (String) -> Unit,
    setSalePrice: (String) -> Unit,
    setCashPaid: (String) -> Unit,
    setCreditAmount: (String) -> Unit
) {
    setItemName("")
    setQuantity("")
    setSelectedUnit(null)
    setCustomUnit("")
    setDescription("")
    setCostPrice("")
    setSalePrice("")
    setCashPaid("")
    setCreditAmount("")
}

// Function to save inventory to Firestore
// Function to save inventory to Firestore
suspend fun saveInventoryToFirestore(
    db: FirebaseFirestore,
    itemDetails: Map<String, String>
): Boolean {
    val itemName = itemDetails["Name"] ?: return false

    return try {
        val documentRef = db.collection("Inventory").document("Items")
        val documentSnapshot = documentRef.get()

        if (documentSnapshot.exists) {
            val existingItems = documentSnapshot.get<List<Map<String, String>>>("items") ?: emptyList()

            // Check if an item with the same name already exists
            if (existingItems.any { it["Name"] == itemName }) {
                return false
            }

            // Add the new item to the list
            val updatedItems = existingItems + itemDetails

            // Save the updated list back to Firestore
            documentRef.set(mapOf("items" to updatedItems))
        } else {
            // Create a new document with the item
            documentRef.set(mapOf("items" to listOf(itemDetails)))
        }

        // Format the current date as dd-mm-yyyy
        val currentDate = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        val formattedDate = "${currentDate.dayOfMonth.toString().padStart(2, '0')}-${currentDate.monthNumber.toString().padStart(2, '0')}-${currentDate.year}"

        // Document ID for the transaction (still using the full timestamp for uniqueness)
        val transactionId = kotlinx.datetime.Clock.System.now().toString()

        // Add to vendor inventory transactions if needed
        val vendorName = itemDetails["Vendor"] ?: return true
        val costPrice = itemDetails["CostPrice"] ?: "0"
        val cashPaid = if (itemDetails.containsKey("CashPaid")) itemDetails["CashPaid"] ?: "0" else costPrice
        val creditAmount = if (itemDetails.containsKey("CreditAmount")) itemDetails["CreditAmount"] ?: "0" else "0"

        val transactionDetails = mapOf(
            "ItemName" to itemName,
            "Quantity" to (itemDetails["Quantity"] ?: "0"),
            "Units" to (itemDetails["Units"] ?: ""),
            "CostPrice" to costPrice,
            "Cash" to cashPaid,
            "Credit" to creditAmount,
            "TotalPrice" to costPrice,
            "Date" to formattedDate,
            "Type" to "Purchase"
        )

        db.collection("InventoryVendors")
            .document("Transactions")
            .collection("Names")
            .document(vendorName)
            .collection("History")
            .document(transactionId)  // Using timestamp as document ID for uniqueness
            .set(transactionDetails)

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Function to update vendor credit
// Function to update vendor credit
suspend fun updateVendorCredit(
    db: FirebaseFirestore,
    vendorName: String,
    creditAmount: String
): Boolean {
    return try {
        // Fetch current vendor details from Details/Names collection
        val vendorDetailsRef = db.collection("InventoryVendors")
            .document("Details")
            .collection("Names")
            .document(vendorName)
            .get()

        if (vendorDetailsRef.exists) {
            val detailsMap = vendorDetailsRef.get<Map<String, String>>("Details")
            val currentCredit = detailsMap["Credit"]?.toFloatOrNull() ?: 0f
            val additionalCredit = creditAmount.toFloatOrNull() ?: 0f
            val newCredit = (currentCredit + additionalCredit).toString()

            // Create a new map for the details with the updated credit
            val updatedDetailsMap = detailsMap.toMutableMap()
            updatedDetailsMap["Credit"] = newCredit

            // Update credit in Details/Names collection
            db.collection("InventoryVendors")
                .document("Details")
                .collection("Names")
                .document(vendorName)
                .set(mapOf("Details" to updatedDetailsMap))

            // Also update the credit in the Names document's VendorDetails list for consistency
            val namesDocRef = db.collection("InventoryVendors").document("Names").get()
            if (namesDocRef.exists) {
                val vendorDetailsList = namesDocRef.get<List<Map<String, String>>>("VendorDetails")?.toMutableList() ?: mutableListOf()

                // Find and update this vendor's credit in the list
                val updatedList = vendorDetailsList.map { vendorMap ->
                    if (vendorMap["Name"] == vendorName) {
                        val updatedVendorMap = vendorMap.toMutableMap()
                        updatedVendorMap["Credit"] = newCredit
                        updatedVendorMap
                    } else {
                        vendorMap
                    }
                }

                // Save the updated list back
                db.collection("InventoryVendors")
                    .document("Names")
                    .set(mapOf("VendorDetails" to updatedList))
            }

            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Function to save vendor to Firestore
suspend fun saveInventoryVendorToFirestore(
    db: FirebaseFirestore,
    vendorDetails: Map<String, String>
): Boolean {
    val vendorName = vendorDetails["Name"] ?: return false
    val vendorPhone = vendorDetails["Phone Number"] ?: ""

    return try {
        val namesDocRef = db.collection("InventoryVendors").document("Names")
        val snapshot = namesDocRef.get()

        // Check if a vendor with the same name already exists
        if (snapshot.exists) {
            val existingDetails = snapshot.get<List<Map<String, String>>>("VendorDetails") ?: emptyList()
            if (existingDetails.any { it["Name"] == vendorName }) {
                return false
            }
        }

        val detailsMap = vendorDetails.filterKeys { it != "Name" }

        // Save to Names collection under Details document
        db.collection("InventoryVendors")
            .document("Details")
            .collection("Names")
            .document(vendorName)
            .set(mapOf("Details" to detailsMap))

        // Create a document for transactions
        db.collection("InventoryVendors")
            .document("Transactions")
            .collection("Names")
            .document(vendorName)
            .set(emptyMap<String, Any>())

        // Add new vendor entry to the Names document
        val vendorEntry = mapOf("Name" to vendorName, "Phone Number" to vendorPhone)

        if (snapshot.exists) {
            val existingDetails = snapshot.get<List<Map<String, String>>>("VendorDetails")?.toMutableList() ?: mutableListOf()
            existingDetails.add(vendorEntry)
            namesDocRef.set(mapOf("VendorDetails" to existingDetails))
        } else {
            namesDocRef.set(mapOf("VendorDetails" to listOf(vendorEntry)))
        }

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Function to fetch vendors from Firestore
suspend fun fetchInventoryVendors(db: FirebaseFirestore): List<String> {
    return try {
        val vendorDetailsRef = db.collection("InventoryVendors")
            .document("Names")
            .get()

        val vendorDetails = vendorDetailsRef.get<List<Map<String, String>>>("VendorDetails") ?: emptyList()

        vendorDetails.mapNotNull { it["Name"] }
    } catch (e: Exception) {
        println("Error fetching vendors: ${e.message}")
        emptyList()
    }
}

// These functions are from your original code and are used in this implementation


