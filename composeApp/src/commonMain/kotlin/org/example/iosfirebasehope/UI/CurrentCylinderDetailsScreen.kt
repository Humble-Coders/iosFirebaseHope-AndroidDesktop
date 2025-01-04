import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.components.CurrentCylinderDetailsComponent

@Composable
fun CurrentCylinderDetailsUI(
    currentCylinderDetails: Map<String, String>, // Generalized type to handle multiple field types
    component: CurrentCylinderDetailsComponent,
    db: FirebaseFirestore // FirebaseFirestore instance for data fetching
) {
    // Map of database keys to display names
    val keyDisplayNames = mapOf(
        "Batch Number" to "Batch",
        "Status" to "Status",
        "Remarks" to "Remarks",
        "Volume" to "Capacity",
        // Add more keys as needed
    )

    // Get the serial number from currentCylinderDetails
    val serialNumber = currentCylinderDetails["Serial Number"] ?: ""
    val abcd = currentCylinderDetails["Previous Customers"]
    println("abcd $currentCylinderDetails")
    // Parse the "Previous Customers" field manually
    val previousCustomers = currentCylinderDetails["Previous Customers"]?.let { customersString ->
        parsePreviousCustomersManually(customersString)
    } ?: emptyList()

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
                    .padding(bottom = 16.dp)
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
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gas symbol
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(63.dp)
                                .background(getGasColor(gasType), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = gasSymbol,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            // Displaying Serial Number, Gas Type, and Volume Type
                            Text(
                                text = "Serial No - $serialNumber",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = gasType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = volumeType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Divider under the row
                    Divider(color = Color.Black, thickness = 1.dp)

                    // Remaining details (like Batch Number, Status, Remarks, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // First column
                        Column(modifier = Modifier.weight(1f)) {
                            listOf("Batch Number", "Status", "Remarks").forEach { key ->
                                val value = currentCylinderDetails[key]
                                if (!value.isNullOrEmpty()) {
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "${keyDisplayNames[key] ?: key}:",
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = value,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Second column
                        Column(modifier = Modifier.weight(1f)) {
                            currentCylinderDetails.filterKeys { key ->
                                key !in listOf(
                                    "Previous Customers",
                                    "Batch Number",
                                    "Status",
                                    "Remarks",
                                    "Gas Type",
                                    "Serial Number",
                                    "Volume Type"
                                )
                            }.forEach { (key, value) ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = "${keyDisplayNames[key] ?: key}:",
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = value,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card to display previous customers
            if (previousCustomers.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Previous Customers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        previousCustomers.forEach { customer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Name: ${customer.name}", fontWeight = FontWeight.Bold)
                                Text(text = "Date: ${customer.date}")
                                Text(text = "Rate: ${customer.rate}")
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            } else {
                Text(
                    text = "No previous customers found",
                    modifier = Modifier.padding(16.dp)
                )
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

