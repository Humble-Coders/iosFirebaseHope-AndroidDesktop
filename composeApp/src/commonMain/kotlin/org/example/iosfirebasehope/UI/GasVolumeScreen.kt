import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import org.example.iosfirebasehope.navigation.components.GasVolumeScreenComponent
import org.example.iosfirebasehope.navigation.components.VolumeTypeScreenComponent
import org.example.iosfirebasehope.navigation.events.GasVolumeScreenEvent

data class VolumeStatusCounts(
    val volume: String,
    val full: Int,
    val empty: Int,
    val sold: Int,
    val repair: Int,
    val atPlant: Int
)

@Composable
fun GasVolumeScreenUI(
    gasId: String,
    db: FirebaseFirestore,
    cylinderDetailList: List<Map<String, String>>,
    component: GasVolumeScreenComponent
) {
    var volumes by remember { mutableStateOf(emptyList<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredCylinders by remember { mutableStateOf(cylinderDetailList) }
    var selectedStatus by remember { mutableStateOf("") }


    LaunchedEffect(gasId) {
        // Fetch volumes from Firestore
        val docRef = db.collection("Gases").document(gasId)
        val docSnapshot = docRef.get()
        if (docSnapshot.exists) {
            val volumesList = docSnapshot.get("Volumes") as? List<String>
            volumesList?.let {
                volumes = it
            }
        }
    }

    // Calculate status counts for each volume
    val volumeStatusCounts = volumes.map { volume ->
        var full = 0
        var empty = 0
        var sold = 0
        var repair = 0
        var atPlant = 0

        cylinderDetailList.forEach { cylinderDetail ->
            if (cylinderDetail["Volume Type"] == volume) {
                when (cylinderDetail["Status"]) {
                    "Full" -> full++
                    "Empty" -> empty++
                    "Sold" -> sold++
                    "Repair" -> repair++
                    "At Plant" -> atPlant++
                }
            }
        }

        VolumeStatusCounts(volume, full, empty, sold, repair, atPlant)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gasId) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(GasVolumeScreenEvent.onBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(volumeStatusCounts) { volumeStatus ->
                    VolumeCard(volumeStatus,cylinderDetailList, component )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                filteredCylinders = cylinderDetailList // Reset the list when cleared
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear Text"
                                )
                            }
                        }
                    },
                    onValueChange = {
                        searchQuery = it
                        filteredCylinders = cylinderDetailList.filter { cylinder ->
                            cylinder["Serial Number"]?.contains(searchQuery, ignoreCase = true) == true &&
                                    (selectedStatus.isEmpty() || cylinder["Status"] == selectedStatus)
                        }
                    },
                    placeholder = {
                        Text("Search by Serial Number")
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .padding(end = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF2f80eb),
                        unfocusedBorderColor = Color(0xFF2f80eb)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )

                var expanded by remember { mutableStateOf(false) }
                var selectedStatus by remember { mutableStateOf("") }

                Box(
                    modifier = Modifier

                        .clickable { expanded = true }
                        .width(120.dp).height(55.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,


                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start=16.dp,end=8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedStatus.isEmpty()) "Filter" else selectedStatus,
                            color = if (selectedStatus.isEmpty()) Color.Gray else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expanded = true }
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(120.dp).padding(end=16.dp)
                    ) {
                        val statuses = listOf("", "Full", "Empty", "Repair", "Sold", "At Plant")
                        statuses.forEach { status ->
                            DropdownMenuItem(onClick = {
                                selectedStatus = status
                                expanded = false
                                filteredCylinders = cylinderDetailList.filter { cylinder ->
                                    (cylinder["Serial Number"]?.contains(searchQuery, ignoreCase = true) == true) &&
                                            (selectedStatus.isEmpty() || cylinder["Status"] == selectedStatus)
                                }
                            }) {
                                Text(text = if (status.isEmpty()) "None" else status)
                            }
                        }
                    }
                }



            }



            CylinderList(cylinderDetailsList = filteredCylinders, component = component)
        }
    }
}

@Composable
fun VolumeCard(volumeStatus: VolumeStatusCounts,cylinderDetailsList: List<Map<String, String>>,component: GasVolumeScreenComponent) {

    val filteredList= cylinderDetailsList.filter { cylinder ->
        cylinder["Volume Type"] == volumeStatus.volume
    }
    Card(
        modifier = Modifier
            .height(155.dp)
            .width(100.dp)
            .padding(end = 4.dp, start = 4.dp)
            .clickable(onClick = {
                // Handle click
                component.onEvent(GasVolumeScreenEvent.onGasCardClick(volumeStatus.volume, filteredList))
            }),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb)) // Custom border color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // Ensure items are centered
             // Add vertical padding to the entire column
        ) {
            Text(
                text = volumeStatus.volume,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(text = "Full: ${volumeStatus.full}", fontSize = 12.sp)
            Text(text = "Empty: ${volumeStatus.empty}", fontSize = 12.sp)
            Text(text = "Sold: ${volumeStatus.sold}", fontSize = 12.sp)
            Text(text = "Repair: ${volumeStatus.repair}", fontSize = 12.sp)
            Text(text = "At Plant: ${volumeStatus.atPlant}", fontSize = 12.sp)
        }
    }
}


@Composable
fun CylinderList(cylinderDetailsList: List<Map<String, String>>, modifier: Modifier = Modifier, component: GasVolumeScreenComponent) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(cylinderDetailsList) { cylinder ->
            CylinderDetailsCard2(listOf(cylinder), component = component)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CylinderDetailsCard2(cylinderDetailsList: List<Map<String, String>>, component: GasVolumeScreenComponent) {
    val currentCylinderDetails = cylinderDetailsList.firstOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {
                // Handle click
                component.onEvent(GasVolumeScreenEvent.OnCylinderClick(currentCylinderDetails = currentCylinderDetails!!))
            }),
        elevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF2f80eb)) // Custom border color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            cylinderDetailsList.forEach { details ->
                val gasName = details["Gas Type"] ?: ""

                val gasSymbol = getGasSymbol(gasName)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(getGasColor(gasName), shape = MaterialTheme.shapes.medium)
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
                        details.filterKeys { key ->
                            key != "Previous Customers" && key != "Gas Type" && key != "Remarks" && key != "Batch Number"
                        }.forEach { (key, value) ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "$key: ",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
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
    }
}

fun getGasSymbol(gasName: String): String {
    // Return the appropriate symbol based on the gas name
    return when (gasName) {
        "Oxygen" -> "O₂"
        "Carbon Dioxide" -> "CO₂"
        "Ammonia" -> "NH₃"
        "Argon" -> "Ar"
        "Nitrogen" -> "N₂"
        "Hydrogen" -> "H"
        "Helium" -> "He"
        "LPG" -> "LPG"
        "Argon Specto" -> "AS"
        "Zero Air" -> "Z"
        else -> ""
    }
}

fun getGasColor(gasName: String): Color {
    return when (gasName) {
        "Oxygen" -> Color(0xFF2196F3) // Blue
        "Carbon Dioxide" -> Color(0xFFFF9800) // Orange
        "Ammonia" -> Color(0xFF795548) // Brown
        "Argon" -> Color(0xFF9C27B0) // Purple
        "Nitrogen" -> Color(0xFF4CAF50) // Green
        "Hydrogen" -> Color(0xFFFF5722) // Deep Orange
        "Helium" -> Color(0xFF3F51B5) // Indigo
        "LPG" -> Color(0xFF009688) // Teal
        "Argon Specto" -> Color(0xFF673AB7) // Deep Purple
        "Zero Air" -> Color(0xFFE91E63) // Pink
        else -> Color.Gray // Default color for any other gases
    }
}
