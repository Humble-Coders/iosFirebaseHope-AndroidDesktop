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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import org.example.iosfirebasehope.navigation.components.CylinderStatusScreenComponent
import org.example.iosfirebasehope.navigation.events.CylinderStatusScreenEvent

@Composable
fun CylinderStatusScreenUI(
    component: CylinderStatusScreenComponent,
    cylinderDetailsList: List<Map<String, String>>,
    status: String,
    gasList: List<String>
) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredCylinders by remember { mutableStateOf(cylinderDetailsList) }
    var selectedGases by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Filtered cylinders based on the search query and selected gases
    LaunchedEffect(searchQuery, selectedGases) {
        filteredCylinders = cylinderDetailsList.filter { cylinder ->
            val matchesSearchQuery = cylinder["Serial Number"]?.contains(searchQuery, ignoreCase = true) == true
            val matchesStatus = cylinder["Status"] == status
            val matchesGases = selectedGases.isEmpty() || cylinder["Gas Type"] == selectedGases
            matchesSearchQuery && matchesStatus && matchesGases
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status : $status") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(CylinderStatusScreenEvent.onBackClick) }) {
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
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear Text"
                                )
                            }
                        }
                    },
                    onValueChange = { searchQuery = it },
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

                Box(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .width(120.dp)
                        .height(55.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedGases.isEmpty()) "Filter" else selectedGases,
                            color = if (selectedGases.isEmpty()) Color.Gray else Color.Black
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
                        modifier = Modifier.width(120.dp).padding(end = 16.dp)
                    ) {
                        DropdownMenuItem(onClick = {
                            selectedGases = ""
                            expanded = false
                        }) {
                            Text(text = "None")
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        gasList.forEach { gas ->
                            DropdownMenuItem(onClick = {
                                selectedGases = gas
                                expanded = false
                            }) {
                                Text(text = gas)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

            }

            // Display filtered list of cylinders
            CylinderList(status = status, cylinderDetailsList = filteredCylinders, modifier = Modifier.padding(innerPadding), component = component)
        }
    }
}

@Composable
fun CylinderList(status: String, cylinderDetailsList: List<Map<String, String>>, modifier: Modifier = Modifier, component: CylinderStatusScreenComponent) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(cylinderDetailsList) { cylinder ->
            CylinderDetailsCard(listOf(cylinder), component = component)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CylinderDetailsCard(cylinderDetailsList: List<Map<String, String>>, component: CylinderStatusScreenComponent) {

    val currentCylinderDetails = cylinderDetailsList.firstOrNull() ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {component.onEvent(CylinderStatusScreenEvent.OnCylinderClick(currentCylinderDetails))}),
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

                    val orderedKeys = listOf(
                        "Serial Number",
                        "Batch Number",
                        "Gas Type",
                        "Volume Type",
                        "Remarks"
                    )

                    Column {
                        orderedKeys.forEach { key ->
                            details[key]?.let { value ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        text = "$key: ",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = value.replace(",", "."), // Replace commas with dots
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
}