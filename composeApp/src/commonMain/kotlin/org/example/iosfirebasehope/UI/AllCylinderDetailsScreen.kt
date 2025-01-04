package org.example.iosfirebasehope.UI

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
import org.example.iosfirebasehope.navigation.components.AllCylinderDetailsScreenComponent
import org.example.iosfirebasehope.navigation.components.CylinderStatusScreenComponent
import org.example.iosfirebasehope.navigation.events.AllCylinderDetailsScreenEvent
import org.example.iosfirebasehope.navigation.events.CylinderStatusScreenEvent

@Composable
fun AllCylinderDetailsScreenUI(
    component: AllCylinderDetailsScreenComponent,
    cylinderDetailsList: List<Map<String, String>>
) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredCylinders by remember { mutableStateOf(cylinderDetailsList) }
    var expanded by remember { mutableStateOf(false) }

    // Filtered cylinders based on the search query and selected gases
    LaunchedEffect(searchQuery) {
        filteredCylinders = if (searchQuery.isEmpty()) {
            cylinderDetailsList
        } else {
            cylinderDetailsList.filter { it["Serial Number"]?.contains(searchQuery, true) == true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Cylinder Details") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(AllCylinderDetailsScreenEvent.OnBackClick) }) {
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
                        .width(360.dp)
                        .padding(end = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF2f80eb),
                        unfocusedBorderColor = Color(0xFF2f80eb)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )



            }

            // Display filtered list of cylinders
            CylinderList2( cylinderDetailsList = filteredCylinders, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun CylinderList2( cylinderDetailsList: List<Map<String, String>>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(cylinderDetailsList) { cylinder ->
            CylinderDetailsCard3(listOf(cylinder))
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CylinderDetailsCard3(cylinderDetailsList: List<Map<String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = { /* Handle click */ }),
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
                            key != "Previous Customers"
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
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}