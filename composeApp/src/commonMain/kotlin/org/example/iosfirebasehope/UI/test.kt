import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.components.IssueCylinderScreenComponent

@Composable
fun IssueNewCylinderScreenUI(component: IssueCylinderScreenComponent, db: FirebaseFirestore) {
    val cylindersRef = db.collection("Cylinders").document("Cylinders")
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    // New fields for return date and body count
    var returnDate by remember { mutableStateOf("") }
    var bodyCount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button to create initial structure
        Button(
            onClick = {
                scope.launch {
                    try {
                        // Creating the initial structure in Firestore
                        val initialData = mapOf(
                            "Array" to listOf(
                                mapOf(
                                    "Array2" to emptyList<Map<String, String>>(), // Empty Array2
                                    "Return Date" to "", // Empty return date
                                    "Body Count" to "" // Empty body count
                                )
                            )
                        )
                        cylindersRef.set(initialData)
                        statusMessage = "Initial structure created in Firestore."
                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Create Initial Structure")
        }

        // TextFields to collect user input for Array2 fields
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = rate,
            onValueChange = { rate = it },
            label = { Text("Rate") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // TextFields to collect user input for the new fields
        TextField(
            value = returnDate,
            onValueChange = { returnDate = it },
            label = { Text("Return Date") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = bodyCount,
            onValueChange = { bodyCount = it },
            label = { Text("Body Count") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Button to add Return Date to Firestore
        Button(
            onClick = {
                if (returnDate.isNotEmpty()) {
                    scope.launch {
                        try {
                            val snapshot = cylindersRef.get()
                            if (snapshot.exists) {
                                val currentData = snapshot.data<Cylinders>() ?: Cylinders()
                                val updatedArray = currentData.Array.toMutableList()

                                if (updatedArray.isNotEmpty()) {
                                    updatedArray[0] = updatedArray[0].copy(returnDate = returnDate)
                                }

                                cylindersRef.set(Cylinders(Array = updatedArray))
                                statusMessage = "Return Date updated."
                            } else {
                                statusMessage = "Document does not exist."
                            }
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.message}"
                        }
                    }
                } else {
                    statusMessage = "Please enter Return Date."
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Update Return Date")
        }

        // Button to add Body Count to Firestore
        Button(
            onClick = {
                if (bodyCount.isNotEmpty()) {
                    scope.launch {
                        try {
                            val snapshot = cylindersRef.get()
                            if (snapshot.exists) {
                                val currentData = snapshot.data<Cylinders>() ?: Cylinders()
                                val updatedArray = currentData.Array.toMutableList()

                                if (updatedArray.isNotEmpty()) {
                                    updatedArray[0] = updatedArray[0].copy(bodyCount = bodyCount)
                                }

                                cylindersRef.set(Cylinders(Array = updatedArray))
                                statusMessage = "Body Count updated."
                            } else {
                                statusMessage = "Document does not exist."
                            }
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.message}"
                        }
                    }
                } else {
                    statusMessage = "Please enter Body Count."
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Update Body Count")
        }

        // Button to add to Array2
        Button(
            onClick = {
                if (name.isNotEmpty() && date.isNotEmpty() && rate.isNotEmpty()) {
                    scope.launch {
                        try {
                            val snapshot = cylindersRef.get()
                            if (snapshot.exists) {
                                val currentData = snapshot.data<Cylinders>() ?: Cylinders()
                                val updatedArray = currentData.Array.toMutableList()

                                if (updatedArray.isNotEmpty()) {
                                    val firstElement = updatedArray[0]
                                    val updatedArray2 = firstElement.Array2.toMutableList()

                                    updatedArray2.add(Array2Item(Name = name, Date = date, Rate = rate))
                                    updatedArray[0] = ArrayElement(Array2 = updatedArray2)

                                    cylindersRef.set(Cylinders(Array = updatedArray))
                                    statusMessage = "Element added to Array2."
                                }
                            } else {
                                statusMessage = "Document does not exist."
                            }
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.message}"
                        }
                    }
                } else {
                    statusMessage = "Please fill in all Array2 fields."
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Add Element to Array2")
        }

        // Button to add similar element to Array
        Button(
            onClick = {
                scope.launch {
                    try {
                        val snapshot = cylindersRef.get()
                        if (snapshot.exists) {
                            val currentData = snapshot.data<Cylinders>() ?: Cylinders()
                            val updatedArray = currentData.Array.toMutableList()

                            if (updatedArray.isNotEmpty()) {
                                val firstElement = updatedArray[0]
                                val updatedArray2 = firstElement.Array2.toMutableList()

                                updatedArray2.add(Array2Item(Name = name, Date = date, Rate = rate))
                                updatedArray.add(ArrayElement(Array2 = updatedArray2))

                                cylindersRef.set(Cylinders(Array = updatedArray))
                                statusMessage = "Similar element added to Array."
                            }
                        } else {
                            statusMessage = "Document does not exist."
                        }
                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Add Similar Element to Array")
        }

        Text(statusMessage)
    }
}

@kotlinx.serialization.Serializable
data class Array2Item(
    val Name: String = "",
    val Date: String = "",
    val Rate: String = ""
)

@kotlinx.serialization.Serializable
data class ArrayElement(
    val Array2: List<Array2Item> = emptyList(),
    val returnDate: String = "",
    val bodyCount: String = ""
)

@kotlinx.serialization.Serializable
data class Cylinders(
    val Array: List<ArrayElement> = emptyList()
)