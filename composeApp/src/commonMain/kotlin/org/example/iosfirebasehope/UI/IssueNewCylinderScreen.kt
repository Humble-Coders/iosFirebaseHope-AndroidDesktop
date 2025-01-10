package org.example.iosfirebasehope.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import dev.gitlive.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.example.iosfirebasehope.navigation.components.IssueCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.IssueCylinderScreenEvent
import androidx.compose.material3.TextButton
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import dev.gitlive.firebase.firestore.FieldValue
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun IssueNewCylinderScreenUI(component: IssueCylinderScreenComponent, db: FirebaseFirestore) {
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddCylinderDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<String?>(null) }
    var customers by remember { mutableStateOf<List<String>>(emptyList()) }
    var issuedCylinders by remember { mutableStateOf<List<IssuedCylinder>>(emptyList()) }
    var issueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var returnDays by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var alreadySelectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) } // State for already selected cylinders

    // Fetch customers from Firestore
    LaunchedEffect(Unit) {
        customers = fetchCustomers(db)
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF2f80eb), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { component.onEvent(IssueCylinderScreenEvent.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                onClick = { showAddCustomerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add New Customer", color = Color.White, fontSize = 16.sp)
            }

            // Customer Dropdown
            Text("Select Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp))
            SearchableDropdown(
                options = customers,
                selectedItem = selectedCustomer,
                onItemSelected = { selectedCustomer = it },
                placeholder = "Search customer..."
            )

            // Add Cylinder Button
            Button(
                onClick = { showAddCylinderDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb)),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Add Cylinder", color = Color.White, fontSize = 16.sp)
            }

            // Display Issued Cylinders
            LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
                items(issuedCylinders.size) { index ->
                    val cylinder = issuedCylinders[index]
                    IssuedCylinderCard(cylinder)
                }
            }

            // Issue Date and Return Days
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Issue Date:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb))
                ) {
                    Text(issueDate?.toString() ?: "Select Date")
                }
            }

            // Custom Date Picker Dialog
            if (showDatePicker) {
                CustomDatePicker(
                    onDateSelected = { selectedDate ->
                        issueDate = selectedDate
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            // Return Days Input
            OutlinedTextField(
                value = returnDays,
                onValueChange = { returnDays = it },
                label = { Text("Return Days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            // Checkout Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = checkoutCylinders(db, selectedCustomer, issuedCylinders,
                            issueDate.toString(), returnDays.toIntOrNull() ?: 0)
                        if (success) {
                            snackbarHostState.showSnackbar("Cylinders issued successfully!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to issue cylinders.")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb)),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Checkout (Total: ${issuedCylinders.sumOf { it.totalPrice }})", color = Color.White, fontSize = 16.sp)
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onAddCustomer = { customerDetails ->
                coroutineScope.launch {
                    val success = saveCustomerToFirestore(
                        db, customerDetails
                    )
                    if (success) {
                        snackbarHostState.showSnackbar("Customer added successfully!")
                        customers = fetchCustomers(db)
                    } else {
                        snackbarHostState.showSnackbar("Failed to add customer.")
                    }
                }
            }
        )
    }

    // Add Cylinder Dialog
    if (showAddCylinderDialog) {
        AddCylinderDialog(
            onDismiss = { showAddCylinderDialog = false },
            onAddCylinder = { issuedCylinder ->
                issuedCylinders = issuedCylinders + issuedCylinder
            },
            db = db,
            alreadySelectedCylinders = alreadySelectedCylinders,
            onUpdateAlreadySelectedCylinders = { updatedList ->
                alreadySelectedCylinders = updatedList
            }
        )
    }
}

@Composable
fun CustomDatePicker(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate(2023, 1, 1)) } // Default date

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                // Year Picker
                Text("Year: ${selectedDate.year}")
                Row {
                    Button(onClick = { selectedDate = selectedDate.minus(1, DateTimeUnit.YEAR) }) {
                        Text("-")
                    }
                    Button(onClick = { selectedDate = selectedDate.plus(1, DateTimeUnit.YEAR) }) {
                        Text("+")
                    }
                }

                // Month Picker
                Text("Month: ${selectedDate.monthNumber}")
                Row {
                    Button(onClick = { selectedDate = selectedDate.minus(1, DateTimeUnit.MONTH) }) {
                        Text("-")
                    }
                    Button(onClick = { selectedDate = selectedDate.plus(1, DateTimeUnit.MONTH) }) {
                        Text("+")
                    }
                }

                // Day Picker
                Text("Day: ${selectedDate.dayOfMonth}")
                Row {
                    Button(onClick = { selectedDate = selectedDate.minus(1, DateTimeUnit.DAY) }) {
                        Text("-")
                    }
                    Button(onClick = { selectedDate = selectedDate.plus(1, DateTimeUnit.DAY) }) {
                        Text("+")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDateSelected(selectedDate) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCylinderDialog(
    onDismiss: () -> Unit,
    onAddCylinder: (IssuedCylinder) -> Unit,
    db: FirebaseFirestore,
    alreadySelectedCylinders: List<String>,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit
) {
    var gasType by remember { mutableStateOf<String?>(null) }
    var volumeType by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var prices by remember { mutableStateOf<List<Double>>(emptyList()) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var cylinderOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var volumeOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var localAlreadySelectedCylinders by remember { mutableStateOf(alreadySelectedCylinders) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch gas types on launch
    LaunchedEffect(Unit) {
        val gases = db.collection("Gases").get().documents
        cylinderOptions = gases.map { it.id }
    }

    // Fetch volume types when gasType changes
    LaunchedEffect(gasType) {
        if (gasType != null) {
            val document = db.collection("Gases").document(gasType!!).get()
            val volumesAndSP = document.get("VolumesAndSP") as? Map<String, Int>
            volumesAndSP?.let {
                volumeOptions = it.keys.map { volume -> volume.replace(",", ".") }
            }
        } else {
            volumeOptions = emptyList()
        }
    }

    // Fetch available cylinders when gasType or volumeType changes
    LaunchedEffect(gasType, volumeType) {
        if (gasType != null && volumeType != null) {
            coroutineScope.launch {
                val allCylinders = fetchCylindersByStatus(db, gasType!!, volumeType!!, "Full")
                availableCylinders = allCylinders.filter { it !in localAlreadySelectedCylinders }
            }
        } else {
            availableCylinders = emptyList()
        }
    }

    // Initialize selectedCylinders and prices lists when quantity changes
    LaunchedEffect(quantity.toIntOrNull()) {
        val quantityInt = quantity.toIntOrNull() ?: 0
        selectedCylinders = List(quantityInt) { "" }
        prices = List(quantityInt) { 0.0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Cylinder", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Gas Type Dropdown
                Text("Gas Type", fontWeight = FontWeight.Bold)
                SearchableDropdown(
                    options = cylinderOptions,
                    selectedItem = gasType,
                    onItemSelected = { gasType = it },
                    placeholder = "Select Gas Type"
                )

                // Volume Type Dropdown
                Text("Volume Type", fontWeight = FontWeight.Bold)
                SearchableDropdown(
                    options = volumeOptions,
                    selectedItem = volumeType,
                    onItemSelected = { volumeType = it },
                    placeholder = "Select Volume Type"
                )

                // Quantity Input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Cylinder Dropdowns
                if (quantity.toIntOrNull() != null) {
                    // Reset localAlreadySelectedCylinders and selectedCylinders when quantity changes
                    LaunchedEffect(quantity.toIntOrNull()) {
                        localAlreadySelectedCylinders = emptyList()
                        selectedCylinders = List(quantity.toInt()) { "" }
//                        availableCylinders = availableCylinders.filter { it !in alreadySelectedCylinders }
//                         Re-fetch availableCylinders and exclude any cylinders that are still selected
                        if (gasType != null && volumeType != null) {
                            coroutineScope.launch {
                                val allCylinders = fetchCylindersByStatus(db, gasType!!, volumeType!!, "Full")
                                availableCylinders = allCylinders.filter { it !in alreadySelectedCylinders }
                            }
                        }
                    }

                    repeat(quantity.toInt()) { index ->
                        Text("Cylinder ${index + 1}", fontWeight = FontWeight.Bold)
                        SearchableDropdown(
                            options = availableCylinders,
                            selectedItem = selectedCylinders.getOrNull(index),
                            onItemSelected = { selectedCylinder ->
                                selectedCylinders = selectedCylinders.toMutableList().apply { set(index, selectedCylinder) }
                                localAlreadySelectedCylinders = localAlreadySelectedCylinders + selectedCylinder + alreadySelectedCylinders
                                availableCylinders = availableCylinders.filter { it != localAlreadySelectedCylinders[index] }
                            },
                            placeholder = "Select Cylinder",
                        )
                        OutlinedTextField(
                            value = prices.getOrNull(index)?.toString() ?: "",
                            onValueChange = { newValue ->
                                prices = prices.toMutableList().apply {
                                    set(index, newValue.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCylinders.isNotEmpty()) {
                        totalPrice = prices.sum()
                        selectedCylinders.forEach { serialNumber ->
                            onAddCylinder(
                                IssuedCylinder(
                                    serialNumber = serialNumber,
                                    gasType = gasType ?: "",
                                    volumeType = volumeType ?: "",
                                    quantity = quantity.toInt(),
                                    totalPrice = totalPrice
                                )
                            )
                        }
                        onUpdateAlreadySelectedCylinders(localAlreadySelectedCylinders)
                        onDismiss()
                    } else {
                        coroutineScope.launch {
                            SnackbarHostState().showSnackbar("Please select at least one cylinder.")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2f80eb))
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF2f80eb))
            }
        }
    )
}

// Firestore Function to Fetch Cylinders by Gas Type, Volume Type, and Status
suspend fun fetchCylindersByStatus(db: FirebaseFirestore, gasType: String, volumeType: String, status: String): List<String> {
    val cylinders = db.collection("Cylinders").document("Cylinders").get()
    val cylinderDetails = cylinders.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
    return cylinderDetails
        .filter { it["Gas Type"] == gasType && it["Volume Type"] == volumeType && it["Status"] == status }
        .map { it["Serial Number"] ?: "" } // Extract serial numbers
}

// Data Classes
data class IssuedCylinder(
    val serialNumber: String, // Added serialNumber property
    val gasType: String,
    val volumeType: String,
    val quantity: Int,
    val totalPrice: Double
)

// Firestore Functions
suspend fun fetchCustomers(db: FirebaseFirestore): List<String> {
    return try {
        // Navigate to the target document
        val customerDetailsRef = db.collection("Customers")
            .document("Names")
            .get()

        // Extract the CustomerDetails array from the document
        val customerDetails = customerDetailsRef.get("CustomerDetails") as? List<Map<String, String>> ?: emptyList()

        // Map the "Name" fields from each map in the array
        customerDetails.mapNotNull { it["Name"] }
    } catch (e: Exception) {
        println("Error fetching customers: ${e.message}")
        emptyList()
    }
}



suspend fun checkoutCylinders(
    db: FirebaseFirestore,
    customerName: String?,
    issuedCylinders: List<IssuedCylinder>,
    issueDate: String?,
    returnDays: Int
): Boolean {
    println("checkoutCylinders: $customerName, $issueDate, $returnDays, $issuedCylinders, $db")

    // Validate inputs
    if (customerName == null || issueDate == null) return false

    try {
        // Part 1: Update Details array
        val customerRef = db.collection("Customers")
            .document("Issued Cylinders")
            .collection("Names")
            .document(customerName)

        val snapshot = customerRef.get()
        val existingDetails = if (snapshot.exists) {
            snapshot.get("Details") as? List<String> ?: emptyList()
        } else {
            emptyList()
        }

        val newSerialNumbers = issuedCylinders.map { it.serialNumber }
        val updatedDetails = existingDetails + newSerialNumbers
        customerRef.set(mapOf("Details" to updatedDetails))

        // Part 2: Add transaction data
        val transactionsRef = db.collection("Customers")
            .document("Transactions")
            .collection("Names")
            .document(customerName)

        // Create the document if it doesn't exist
        if (!transactionsRef.get().exists) {
            transactionsRef.set(emptyMap<String, Any>())
        }

        // Get the current date
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

        // Reference the transactions collection with today's date
        val transactionCollectionRef = transactionsRef.collection(currentDate)

        // Check if the collection exists by trying to retrieve a document in it
        val cashDocRef = transactionCollectionRef.document("Cash")
        val collectionExists = cashDocRef.get().exists

        if (!collectionExists) {
            // Add the required documents if the collection does not exist
            transactionCollectionRef.document("Cash").set(mapOf("Amount" to ""))
            transactionCollectionRef.document("Credit").set(mapOf("Amount" to ""))
            transactionCollectionRef.document("Cylinders Returned").set(mapOf("CylindersReturned" to listOf<String>()))
        }

        // Retrieve existing CylindersIssued array if present
        val cylindersIssuedRef = transactionCollectionRef.document("Cylinders Issued")
        val cylindersSnapshot = cylindersIssuedRef.get()
        val existingCylindersIssued = if (cylindersSnapshot.exists) {
            cylindersSnapshot.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
        } else {
            emptyList()
        }

        // Prepare new CylindersIssued entries
        val newCylindersIssued = issuedCylinders.map {
            mapOf(
                "Serial Number" to it.serialNumber,
                "Total Price" to it.totalPrice
            )
        }

        // Combine existing and new CylindersIssued
        val updatedCylindersIssued = existingCylindersIssued + newCylindersIssued

        // Upload the updated CylindersIssued array
        cylindersIssuedRef.set(mapOf("CylindersIssued" to updatedCylindersIssued))


        // Part 3: Update Status in Cylinders > Cylinders > CylinderDetails
        val cylindersRef = db.collection("Cylinders").document("Cylinders")
        val cylindersSnapshot2 = cylindersRef.get()

        if (cylindersSnapshot2.exists) {
            val cylinderDetails = cylindersSnapshot2.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()

            // Convert the list to a mutable list for modification
            val updatedCylinderDetails = cylinderDetails.map { map ->
                if (map["Serial Number"] in issuedCylinders.map { it.serialNumber }) {
                    map.toMutableMap().apply {
                        this["Status"] = "Issued"
                    }
                } else {
                    map
                }
            }

            // Upload the updated array back to Firestore
            cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
        }


        return true
    } catch (e: Exception) {
        println("Error in checkoutCylinders: ${e.message}")
        return false
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
    var deposit by remember { mutableStateOf("") } // New state for deposit

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
            }
        },
        confirmButton = {
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
                        "Deposit" to deposit // Add deposit to the details
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

suspend fun saveCustomerToFirestore(
    db: FirebaseFirestore,
    customerDetails: Map<String, String>,
    //showSnackbar: (String) -> Unit
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
                //showSnackbar("Customer with name $customerName already exists.")
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
//        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
//        val dateTimeString = "${currentDateTime.date}_${currentDateTime.hour}:${currentDateTime.minute}:${currentDateTime.second}"
//
        val transactionsRef = db.collection("Customers")
            .document("Transactions")
            .collection("Names")
            .document(customerName)
//            .collection(dateTimeString)

        // Create an empty document for the customerName
        transactionsRef.set(emptyMap<String, Any>())
//
//        transactionsRef.document("Cylinders Issued")
//            .set(mapOf("Serial Numbers" to listOf<String>()))
//
//
//        transactionsRef.document("Cylinders Returned")
//            .set(mapOf("Serial Numbers" to listOf<String>()))
//
//
//        transactionsRef.document("Cash")
//            .set(mapOf("Amount" to ""))
//
//
//        transactionsRef.document("Credit")
//            .set(mapOf("Amount" to ""))


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
        //showSnackbar("Failed to add customer: ${e.message}")
        false
    }
}



@Composable
fun SearchableDropdown(
    options: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
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
                        .heightIn(max = 200.dp) // Set a maximum height for the dropdown
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (filteredOptions.isEmpty()) {
                            item {
                                Text(
                                    text = "No options found",
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
fun IssuedCylinderCard(cylinder: IssuedCylinder) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Gas Type: ${cylinder.gasType}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Volume Type: ${cylinder.volumeType}",
                fontSize = 14.sp
            )
            Text(
                text = "Quantity: ${cylinder.quantity}",
                fontSize = 14.sp
            )
            Text(
                text = "Total Price: ${cylinder.totalPrice}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}