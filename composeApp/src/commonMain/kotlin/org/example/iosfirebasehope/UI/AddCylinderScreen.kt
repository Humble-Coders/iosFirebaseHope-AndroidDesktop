package org.example.iosfirebasehope.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.components.AddCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.AddCylinderScreenEvent

@Composable
fun AddCylinderScreenUI(component: AddCylinderScreenComponent, db: FirebaseFirestore) {
//    val db = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()

    var serialNumber by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var selectedGasType by remember { mutableStateOf<String?>(null) }
    var selectedVolumeType by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var remarks by remember { mutableStateOf("") }

    val gasTypes = remember { mutableStateListOf<String>() }
    val volumeTypes = remember { mutableStateListOf<String>() }

    var gasTypeDropdownExpanded by remember { mutableStateOf(false) }
    var volumeTypeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Scaffold state for Snackbar
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(Unit) {
        val gases = db.collection("Gases").get().documents
        gasTypes.clear()
        gasTypes.addAll(gases.map { it.id })
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        scaffoldState = scaffoldState,  // Set scaffoldState
        topBar = {
            TopAppBar(
                title = { Text("Add Cylinder", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(AddCylinderScreenEvent.onBackClick)}) {
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
            // Dismissing the keyboard when tapping anywhere outside the text fields
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                keyboardController?.hide()  // Hide keyboard when tapped outside
                            }
                        )
                    }
            ) {
                Column {
                    Text("Serial Number", color = Color(0xFF2f80eb))

                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color(0xFF2f80eb)
                        ),
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done  // Done button on the keyboard
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )

                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Batch Number", color = Color(0xFF2f80eb))
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color(0xFF2f80eb)
                        ),
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done  // Done button on the keyboard
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )

                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Gas Type", color = Color(0xFF2f80eb))
                    PrettyDropdownMenu(
                        selectedItem = selectedGasType ?: "Select Gas Type",
                        items = gasTypes,
                        onItemSelected = { selectedGasType = it },
                        onDropdownExpanded = { expanded -> gasTypeDropdownExpanded = expanded },
                        dropdownExpanded = gasTypeDropdownExpanded,
                        fetchDataOnItemClick = { gas ->
                            coroutineScope.launch {
                                val volumes = db.collection("Gases").document(gas).get().get("Volumes") as List<String>
                                volumeTypes.clear()
                                volumeTypes.addAll(volumes)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Volume Type", color = Color(0xFF2f80eb))
                    PrettyDropdownMenu(
                        selectedItem = selectedVolumeType ?: "Select Volume Type",
                        items = volumeTypes,
                        onItemSelected = { selectedVolumeType = it },
                        onDropdownExpanded = { expanded -> volumeTypeDropdownExpanded = expanded },
                        dropdownExpanded = volumeTypeDropdownExpanded,
                        // Check if gas type is selected before allowing volume type selection
                        fetchDataOnItemClick = {
                            if (selectedGasType == null) {
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        "Please select a gas type first"
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Status", color = Color(0xFF2f80eb))
                    PrettyDropdownMenu(
                        selectedItem = selectedStatus ?: "Select Status",
                        items = listOf("Full", "Empty"),
                        onItemSelected = { selectedStatus = it },
                        onDropdownExpanded = { expanded -> statusDropdownExpanded = expanded },
                        dropdownExpanded = statusDropdownExpanded
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Remarks", color = Color(0xFF2f80eb))
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color(0xFF2f80eb)
                        ),
                        value = remarks,
                        onValueChange = { remarks = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done  // Done button on the keyboard
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val cylinderDetails = hashMapOf(
                                    "Serial Number" to serialNumber,
                                    "Batch Number" to batchNumber,
                                    "Gas Type" to selectedGasType,
                                    "Volume Type" to selectedVolumeType,
                                    "Status" to selectedStatus,
                                    "Remarks" to remarks,
                                    "Previous Customers" to listOf(
                                        // Array of 2 maps, each with Name, Date, and Rate set to null
                                        mapOf("Name" to null, "Date" to null, "Rate" to null),
                                        mapOf("Name" to null, "Date" to null, "Rate" to null)
                                    )
                                )
                                val documentRef = db.collection("Cylinders").document("Cylinders")
                                val documentSnapshot = documentRef.get()

                                if (documentSnapshot.exists) {
                                    val existingCylinderDetails = documentSnapshot.get("CylinderDetails") as? List<Map<String, String>>
                                    val serialNumberExists = existingCylinderDetails?.any { it["SerialNumber"] == serialNumber } ?: false

                                    if (!serialNumberExists) {
                                        documentRef.update(
                                            mapOf("CylinderDetails" to FieldValue.arrayUnion(cylinderDetails))
                                        )

                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                "Cylinder Successfully Added."
                                            )
                                        }
                                    } else {
                                        // Handle the case where the serial number already exists (e.g., show a toast message)
                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar(
                                                "Cylinder with SerialNumber $serialNumber already exists."
                                            )
                                        }
                                    }
                                } else {
                                    documentRef.set(
                                        mapOf("CylinderDetails" to listOf(cylinderDetails))
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Cylinder", color = Color.White)
                    }

                }
            }
        }
    )
}


@Composable
fun PrettyDropdownMenu(
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onDropdownExpanded: (Boolean) -> Unit,
    dropdownExpanded: Boolean,
    fetchDataOnItemClick: ((String) -> Unit)? = null
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
                onDismissRequest = { onDropdownExpanded(false)

                }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onItemSelected(item)
                            onDropdownExpanded(false)
                            fetchDataOnItemClick?.invoke(item)
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
