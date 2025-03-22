package org.example.iosfirebasehope.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.example.iosfirebasehope.navigation.components.VolumeTypeScreenComponent
import org.example.iosfirebasehope.navigation.events.VolumeTypeScreenEvent


@Composable
fun VolumeTypeScreenUI(
    component: VolumeTypeScreenComponent,
    VolumeType: String,
    cylinderDetailList: List<Map<String, String>>,
    gasId: String
) {

    var searchQuery by remember { mutableStateOf("") }
    var filteredCylinders by remember { mutableStateOf(cylinderDetailList) }
    var selectedStatus by remember { mutableStateOf("") }

    val toShowGasName = cylinderDetailList.firstOrNull()?.get("Gas Type") ?: ""

    val volumeType = VolumeType.replace(",", ".")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$gasId: $volumeType") },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(VolumeTypeScreenEvent.onBackClick) }) {
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
fun CylinderList(cylinderDetailsList: List<Map<String, String>>, modifier: Modifier = Modifier, component: VolumeTypeScreenComponent) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(cylinderDetailsList) { cylinder ->
            CylinderDetailsCard4(
                listOf(cylinder),
                component = component
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CylinderDetailsCard4(cylinderDetailsList: List<Map<String, String>>, component: VolumeTypeScreenComponent) {

    val currentCylinderDetails = cylinderDetailsList.firstOrNull() ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {component.onEvent(VolumeTypeScreenEvent.OnCylinderClick(currentCylinderDetails))}),
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
                        "Volume Type",
                        "Status",
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

