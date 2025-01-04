import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.UI.getGasColor
import org.example.iosfirebasehope.UI.getGasSymbol
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent

@Composable
fun CurrentCylinderDetailsUI(
    currentCylinderDetails: Map<String, String>, // Generalized type to handle multiple field types
    component: CurrentCylinderDetailsComponent,
    db: FirebaseFirestore // FirebaseFirestore instance for data fetching
) {
    var price by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) } // State to toggle between text and search bar
    var searchQuery by remember { mutableStateOf("") }

    // Parse the "Previous Customers" field manually
    val previousCustomers = currentCylinderDetails["Previous Customers"]?.let {
        parsePreviousCustomersManually(it)
    } ?: emptyList()

    val currentlyIssuedTo = currentCylinderDetails["Currently Issued To"]?.let {
        parseCurrentlyIssuedToManually(it)
    }

    // Filter customers based on search query
    val filteredCustomers by derivedStateOf {
        if (searchQuery.isEmpty()) {
            previousCustomers
        } else {
            previousCustomers.filter { customer ->
                customer.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val keyDisplayNames = mapOf(
        "Batch Number" to "Batch",
        "Status" to "Status",
        "Remarks" to "Remarks"
    )

    LaunchedEffect(currentCylinderDetails["Gas Type"]) {
        try {
            println("Fetching document for gasId: ${currentCylinderDetails["Gas Type"]}")

            val docSnapshot = db.collection("Gases").document(currentCylinderDetails["Gas Type"]!!).get()
            if (docSnapshot.exists) {
                val fetchedVolumesAndSP = docSnapshot.get("VolumesAndSP") as? Map<String, String>
                if (fetchedVolumesAndSP != null) {
                    val volumeType = currentCylinderDetails["Volume Type"] ?: ""
                    price = fetchedVolumesAndSP[volumeType] ?: "Not Found"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cylinder Details") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Box displaying current cylinder details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp) // Reduced bottom padding
                    .background(Color(0xFFF3F4F6))
            ) {
                Column {
                    val gasType = currentCylinderDetails["Gas Type"] ?: ""
                    val gasSymbol = getGasSymbol(gasType)
                    val volumeType = currentCylinderDetails["Volume Type"] ?: ""

                    // Row with gas symbol and serial number at the top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp), // Reduced padding
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gas symbol
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp) // Reduced size of gas symbol
                                .background(getGasColor(gasType), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = gasSymbol,
                                color = Color.White,
                                fontSize = 20.sp, // Slightly smaller font size
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp)) // Reduced space between items

                        Column {
                            // Displaying Serial Number, Gas Type, and Volume Type
                            Text(
                                text = "Serial No - ${currentCylinderDetails["Serial Number"] ?: ""}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // Slightly smaller font size
                            )
                            Text(
                                text = gasType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // Slightly smaller font size
                            )
                            Text(
                                text = volumeType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // Slightly smaller font size
                            )
                        }
                    }

                    // Divider under the row
                    Divider(color = Color.Black, thickness = 1.dp)

                    // Remaining details (like Batch Number, Status, Remarks, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp) // Reduced padding
                    ) {
                        // First column: Fixed fields (Batch Number, Status, Remarks)
                        Column(modifier = Modifier.weight(1.2f)) { // Adjust weight to reduce spacing
                            listOf("Batch Number", "Status", "Remarks").forEach { key ->
                                val value = currentCylinderDetails[key]
                                val displayName = keyDisplayNames[key] ?: key // Use the display name from the map
                                if (!value.isNullOrEmpty()) {
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) { // Reduced vertical padding
                                        Text(
                                            text = "$displayName:", // Display the mapped name
                                            modifier = Modifier.weight(1f), // Adjust weight to shrink the gap
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp // Slightly smaller font size
                                        )
                                        Text(
                                            text = value,
                                            modifier = Modifier.weight(1f), // Equal weight for both columns
                                            fontSize = 14.sp // Slightly smaller font size
                                        )
                                    }
                                }
                            }
                        }

                        // Second column: Dynamic fields (any other fields not in the first column)
                        Column(modifier = Modifier.weight(1f)) { // Adjust weight for better alignment
                            Row(modifier = Modifier.padding(vertical = 2.dp)) { // Reduced vertical padding
                                Text(
                                    text = "Price:",
                                    modifier = Modifier.weight(1f), // Adjust weight to align with the first column
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp // Slightly smaller font size
                                )
                                Text(
                                    text = "Rs. $price",
                                    modifier = Modifier.weight(1f), // Equal weight for consistency
                                    fontSize = 14.sp // Slightly smaller font size
                                )
                            }
                        }
                    }

                }
            }

            GlowingIconCard(currentlyIssuedTo = currentlyIssuedTo, currentCylinderDetails)

//            Spacer(modifier = Modifier.height(12.dp))

            // Previous Customers Card (always visible)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Header with "Previous Customers" and Search Icon
                    SearchHeader(
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchActiveChange = { isSearchActive = it }
                    )



                    // LazyColumn for previous customers
                    LazyColumn {
                        itemsIndexed(filteredCustomers) { index, customer ->
                            CustomerItem(index, customer)
                        }
                    }

                    // Show "No customers found" if the filtered list is empty
                    if (filteredCustomers.isEmpty()) {
                        Text(
                            text = "No customers found",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp) // Reduced height for the entire header
            .padding(bottom = 4.dp) // Reduced bottom padding
    ) {
        AnimatedVisibility(
            visible = isSearchActive,
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = {
                        onSearchQueryChange("")
                        onSearchActiveChange(false)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                placeholder = { Text("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp), // Reduced height for the search bar
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF2f80eb),
                    unfocusedBorderColor = Color(0xFF2f80eb)
                ),
                singleLine = true,
                visualTransformation = VisualTransformation.None
            )
        }

        AnimatedVisibility(
            visible = !isSearchActive,
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Reduced height for the row
                    .padding(vertical = 4.dp), // Reduced vertical padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previous Customers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp, // Slightly smaller font size
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onSearchActiveChange(true) },
                    modifier = Modifier.size(20.dp) // Smaller icon button size
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                }
            }
        }
    }
}



@Composable
fun GlowingIconCard(currentlyIssuedTo: IssuedToDetails?, currentCylinderDetails: Map<String, String>) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(4.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green tint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // "Currently Issued To" Heading
            Text(
                text = "Currently Issued To",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (currentlyIssuedTo != null) {
                Column {
                    val labelWidth = 120.dp // Reduced width for the labels

                    // Display Name
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Name:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = currentlyIssuedTo.name,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    // Display Date
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Issue Date:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = currentlyIssuedTo.date,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Return Date:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = currentCylinderDetails["Return Date"] ?: "Not Available",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    // Display Rate
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Box(
                            modifier = Modifier.width(labelWidth)
                        ) {
                            Text(
                                text = "Selling Price:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "Rs. ${currentlyIssuedTo.rate}",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            } else {
                // Fallback message if no details are available
                Text(
                    text = "No data available",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }



}


// Extracted CustomerItem composable
@Composable
private fun CustomerItem(index: Int, customer: CustomerDetails) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .clickable { println("Clicked on customer: ${customer.name}") }
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${index + 1}.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(0.7f)) {
                    Text("Issue Date:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 22.dp))
                    Text("Selling Price:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(customer.date, fontSize = 14.sp, color = Color.Gray )
                    Text("Rs. ${customer.rate}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

// Data class to hold customer details
data class CustomerDetails(val name: String, val date: String, val rate: String)

// Function to manually parse the "Previous Customers" string
fun parsePreviousCustomersManually(customersString: String): List<CustomerDetails> {
    val customers = mutableListOf<CustomerDetails>()

    // Remove the outermost square brackets
    val trimmedString = customersString.removePrefix("[").removeSuffix("]")

    // Split into individual customer entries using "}, {" as the delimiter
    val customerEntries = trimmedString.split("}, {")

    for (entry in customerEntries) {
        // Remove any remaining curly braces
        val cleanedEntry = entry.removePrefix("{").removeSuffix("}")

        // Split into fields
        val fields = cleanedEntry.split(", ")

        var name = ""
        var date = ""
        var rate = ""

        for (field in fields) {
            // Split into key and value
            val keyValue = field.split("=")
            if (keyValue.size == 2) {
                val key = keyValue[0].trim()
                val value = keyValue[1].trim()

                when (key) {
                    "Name" -> name = value
                    "Date" -> date = value
                    "Rate" -> rate = value
                }
            }
        }

        // Only add the customer if all fields are non-empty
        if (name.isNotEmpty() && date.isNotEmpty() && rate.isNotEmpty()) {
            customers.add(CustomerDetails(name, date, rate))
        }
    }

    return customers
}


// Data class to hold issued-to details
data class IssuedToDetails(val name: String, val date: String, val rate: String)

// Function to manually parse the "Currently Issued To" string
fun parseCurrentlyIssuedToManually(issuedToString: String): IssuedToDetails? {
    // Remove the outermost curly braces
    val cleanedString = issuedToString.removePrefix("{").removeSuffix("}")

    // Split into fields
    val fields = cleanedString.split(", ")

    var name = ""
    var date = ""
    var rate = ""

    for (field in fields) {
        // Split into key and value
        val keyValue = field.split("=")
        if (keyValue.size == 2) {
            val key = keyValue[0].trim()
            val value = keyValue[1].trim()

            when (key) {
                "Name" -> name = value
                "Date" -> date = value
                "Rate" -> rate = value
            }
        }
    }

    // Return the parsed details only if all fields are non-empty
    return if (name.isNotEmpty() && date.isNotEmpty() && rate.isNotEmpty()) {
        IssuedToDetails(name, date, rate)
    } else {
        null // Return null if any field is missing
    }
}
