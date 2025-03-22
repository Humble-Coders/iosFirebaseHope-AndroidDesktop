package org.example.iosfirebasehope.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.InventoryScreenComponent
import org.example.iosfirebasehope.navigation.events.InventoryScreenEvent

@Composable
fun InventoryScreenUI(component: InventoryScreenComponent, db: FirebaseFirestore) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State for the dialog box
    var showDialog by remember { mutableStateOf(false) }

    // State for the form fields
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var customUnit by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") } // New state for Price

    // State for the units dropdown
    var unitsDropdownExpanded by remember { mutableStateOf(false) }
    val units = listOf("Kg", "Inch", "Cm", "Other")

    // State for the inventory items
    var inventoryItems by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // State for editing an item
    var itemToEdit by remember { mutableStateOf<Map<String, String>?>(null) }

    // State for delete confirmation dialog


    var itemToDelete by remember { mutableStateOf<Map<String, String>?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var isVerifyingPin by remember { mutableStateOf(false) }
    var showIncorrectPinDialog by remember { mutableStateOf(false) }


    // Fetch inventory items on launch
    LaunchedEffect(Unit) {
        fetchInventoryItems(db, scaffoldState) { items ->
            inventoryItems = items
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Inventory", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(InventoryScreenEvent.onBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color(0xFF2f80eb)
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (showPinDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showPinDialog = false
                            enteredPin = ""
                        },
                        title = { Text("Enter PIN to Delete", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = { enteredPin = it },
                                    label = { Text("PIN") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFF2f80eb),
                                        focusedLabelColor = Color(0xFF2f80eb)
                                    ),
                                    enabled = !isVerifyingPin
                                )
                                if (isVerifyingPin) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        color = Color(0xFF2f80eb)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    isVerifyingPin = true
                                    coroutineScope.launch {
                                        val passwordDoc = db.collection("Passwords")
                                            .document("Password")
                                            .get()

                                        val storedPin = passwordDoc.get("Pin") as? String

                                        if (enteredPin == storedPin) {
                                            showPinDialog = false
                                            showDeleteConfirmationDialog = true
                                        } else {
                                            showIncorrectPinDialog = true
                                        }
                                        enteredPin = ""
                                        isVerifyingPin = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                                enabled = !isVerifyingPin && enteredPin.isNotEmpty()
                            ) {
                                Text("Verify", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showPinDialog = false
                                    enteredPin = ""
                                },
                                enabled = !isVerifyingPin
                            ) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }
                        }
                    )
                }

                // Incorrect PIN Dialog
                if (showIncorrectPinDialog) {
                    AlertDialog(
                        onDismissRequest = { showIncorrectPinDialog = false },
                        title = { Text("Error", fontWeight = FontWeight.Bold, color = Color.Red) },
                        text = { Text("Incorrect PIN") },
                        confirmButton = {
                            Button(
                                onClick = { showIncorrectPinDialog = false },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    )
                }

                 //Delete Confirmation Dialog
                if (showDeleteConfirmationDialog && itemToDelete != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteConfirmationDialog = false
                            itemToDelete = null
                        },
                        title = { Text("Delete Item") },
                        text = { Text("Are you sure you want to delete this item?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        deleteItem(db, scaffoldState, itemToDelete!!, inventoryItems) { updatedItems ->
                                            inventoryItems = updatedItems
                                        }
                                        showDeleteConfirmationDialog = false
                                        itemToDelete = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showDeleteConfirmationDialog = false
                                    itemToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                            ) {
                                Text("Cancel", color = Color.Black)
                            }
                        }
                    )
                }
                // Add Item Button
                Button(
                    onClick = {
                        // Reset fields when opening the dialog for adding a new item
                        itemName = ""
                        quantity = ""
                        selectedUnit = null
                        customUnit = ""
                        description = ""
                        price = "" // Reset the price field
                        itemToEdit = null
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Item", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display Inventory Items in Cards
                LazyColumn {
                    items(inventoryItems) { item ->
                        InventoryItemCard(
                            item = item,
                            onEditClick = {
                                // Pre-fill fields when opening the dialog for editing
                                itemToEdit = item
                                itemName = item["Name"] ?: ""
                                quantity = item["Quantity"] ?: ""
                                selectedUnit = item["Units"]
                                description = item["Description"] ?: ""
                                price = item["Price"] ?: "" // Pre-fill the price field
                                showDialog = true
                            },
                            onDeleteClick = {
                                itemToDelete = item
                                showPinDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Dialog Box for Adding/Editing Item
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(if (itemToEdit == null) "Add New Item" else "Edit Item") },
                        text = {
                            Column {
                                // Item Name (disabled for editing)
                                TextField(
                                    value = itemName,
                                    onValueChange = { itemName = it },
                                    label = { Text("Item Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = itemToEdit == null // Disable editing for name
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quantity
                                TextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    label = { Text("Quantity") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Price
                                TextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = { Text("Price") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Units Dropdown
                                Text("Units")
                                PrettyDropdownMenu(
                                    selectedItem = selectedUnit ?: "Select Unit",
                                    items = units,
                                    onItemSelected = { selectedUnit = it },
                                    onDropdownExpanded = { expanded -> unitsDropdownExpanded = expanded },
                                    dropdownExpanded = unitsDropdownExpanded
                                )

                                // Custom Unit Input (if "Other" is selected)
                                if (selectedUnit == "Other") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextField(
                                        value = customUnit,
                                        onValueChange = { customUnit = it },
                                        label = { Text("Enter Custom Unit") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Description
                                TextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    // Validate inputs
                                    if (itemName.isBlank() || quantity.isBlank() || selectedUnit == null || price.isBlank()) {
                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                "Please fill all required fields.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        return@Button
                                    }

                                    // Check if the item name already exists (only for adding new items)
                                    if (itemToEdit == null && inventoryItems.any { it["Name"] == itemName }) {
                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                "Item with this name already exists.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        return@Button
                                    }

                                    // Close the dialog immediately after clicking "Save"
                                    showDialog = false

                                    // Determine the unit to save
                                    val unitToSave = if (selectedUnit == "Other") customUnit else selectedUnit

                                    // Create the item map
                                    val item = mapOf(
                                        "Name" to itemName,
                                        "Quantity" to quantity,
                                        "Units" to unitToSave!!,
                                        "Description" to description,
                                        "Price" to price // Add the price field
                                    )

                                    // Fetch the existing items, update the array, and save it back
                                    coroutineScope.launch {
                                        try {
                                            val documentRef = db.collection("Inventory").document("Items")
                                            val documentSnapshot = documentRef.get()

                                            val existingItems = if (documentSnapshot.exists) {
                                                documentSnapshot.get("items") as? List<Map<String, String>> ?: emptyList()
                                            } else {
                                                emptyList()
                                            }

                                            // Add or update the item in the list
                                            val updatedItems = if (itemToEdit == null) {
                                                existingItems + item
                                            } else {
                                                existingItems.map { if (it["Name"] == itemToEdit!!["Name"]) item else it }
                                            }

                                            // Save the updated list back to Firestore
                                            documentRef.set(mapOf("items" to updatedItems))

                                            // Update the local state to reflect the new item
                                            inventoryItems = updatedItems

                                            // Show success message
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                if (itemToEdit == null) "Item added successfully." else "Item updated successfully.",
                                                duration = SnackbarDuration.Short
                                            )

                                            // Reset form fields and editing state
                                            itemName = ""
                                            quantity = ""
                                            selectedUnit = null
                                            customUnit = ""
                                            description = ""
                                            price = "" // Reset the price field
                                            itemToEdit = null
                                        } catch (e: Exception) {
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                "Failed to save item: ${e.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Save", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showDialog = false
                                    itemToEdit = null
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                            ) {
                                Text("Cancel", color = Color.Black)
                            }
                        }
                    )
                }

                // Delete Confirmation Dialog
//                if (itemToDelete != null) {
//                    AlertDialog(
//                        onDismissRequest = { itemToDelete = null },
//                        title = { Text("Delete Item") },
//                        text = { Text("Are you sure you want to delete this item?") },
//                        confirmButton = {
//                            Button(
//                                onClick = {
//                                    coroutineScope.launch {
//                                        deleteItem(db, scaffoldState, itemToDelete!!, inventoryItems) { updatedItems ->
//                                            inventoryItems = updatedItems
//                                        }
//                                        itemToDelete = null
//                                    }
//                                },
//                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
//                            ) {
//                                Text("Delete", color = Color.White)
//                            }
//                        },
//                        dismissButton = {
//                            Button(
//                                onClick = { itemToDelete = null },
//                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
//                            ) {
//                                Text("Cancel", color = Color.Black)
//                            }
//                        }
//                    )
//                }
            }
        }
    )
}

@Composable
fun InventoryItemCard(
    item: Map<String, String>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF2f80eb)) // Border color matching top bar
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            // Edit Button (Top-Right)
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFF2f80eb)
                )
            }

            // Item Details
            Column {
                Text(
                    text = item["Name"] ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quantity: ${item["Quantity"]} ${item["Units"]}",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price: ${item["Price"]}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Description: ${item["Description"]}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Delete Button (Bottom-Right)
            IconButton(
                onClick = {
                    onDeleteClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun PrettyDropdownMenu(
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onDropdownExpanded: (Boolean) -> Unit,
    dropdownExpanded: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp))
            .clickable { onDropdownExpanded(!dropdownExpanded) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedItem,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Drop Down Arrow",
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                modifier = Modifier.width(300.dp),
                onDismissRequest = { onDropdownExpanded(false) }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onItemSelected(item)
                            onDropdownExpanded(false)
                        }
                    ) {
                        Text(
                            text = item,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// Function to fetch inventory items from Firestore
private suspend fun fetchInventoryItems(
    db: FirebaseFirestore,
    scaffoldState: ScaffoldState,
    onItemsFetched: (List<Map<String, String>>) -> Unit
) {
    try {
        val documentRef = db.collection("Inventory").document("Items")
        val documentSnapshot = documentRef.get()

        if (documentSnapshot.exists) {
            val items = documentSnapshot.get("items") as? List<Map<String, String>> ?: emptyList()
            onItemsFetched(items)
        } else {
            scaffoldState.snackbarHostState.showSnackbar("No items found.")
        }
    } catch (e: Exception) {
        scaffoldState.snackbarHostState.showSnackbar("Failed to fetch items: ${e.message}")
    }
}

// Function to delete an item from Firestore
private suspend fun deleteItem(
    db: FirebaseFirestore,
    scaffoldState: ScaffoldState,
    item: Map<String, String>,
    currentItems: List<Map<String, String>>,
    onItemsUpdated: (List<Map<String, String>>) -> Unit
) {
    try {
        val documentRef = db.collection("Inventory").document("Items")
        val updatedItems = currentItems.filterNot { it["Name"] == item["Name"] }

        // Save the updated list back to Firestore
        documentRef.set(mapOf("items" to updatedItems))

        // Update the local state
        onItemsUpdated(updatedItems)

        // Show success message
        scaffoldState.snackbarHostState.showSnackbar("Item deleted successfully.")
    } catch (e: Exception) {
        scaffoldState.snackbarHostState.showSnackbar("Failed to delete item: ${e.message}")
    }
}