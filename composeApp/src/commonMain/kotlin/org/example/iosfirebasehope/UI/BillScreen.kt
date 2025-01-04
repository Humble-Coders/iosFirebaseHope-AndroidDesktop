package org.example.iosfirebasehope.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import iosfirebasehope.composeapp.generated.resources.Res
import iosfirebasehope.composeapp.generated.resources.baseline_add_box_24
import iosfirebasehope.composeapp.generated.resources.baseline_auto_mode_24
import iosfirebasehope.composeapp.generated.resources.baseline_autorenew_24
import org.example.iosfirebasehope.navigation.components.BillScreenComponent
import org.example.iosfirebasehope.navigation.events.BillScreenEvent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun BillScreenUI(component: BillScreenComponent,
                 db: FirebaseFirestore) {
    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF2f80eb), // TopAppBar background color
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Back Button
                    IconButton(
                        onClick = { component.onEvent(BillScreenEvent.OnBackClick) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Title
                    Text(
                        text = "BILL",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center, // Center the cards vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center the cards horizontally
        ) {
            // Card 1: Issue New Cylinder
            ActionCard(
                title = "Issue New Cylinder",
                iconResId = Res.drawable.baseline_add_box_24, // Replace with your drawable resource
                onClick = { component.onEvent(BillScreenEvent.OnIssueCylinderClick) }
            )

            Spacer(modifier = Modifier.height(16.dp)) // Add space between cards

            // Card 2: Exchange Cylinder
            ActionCard(
                title = "Exchange Cylinder",
                iconResId = Res.drawable.baseline_auto_mode_24, // Replace with your drawable resource
                onClick = { /* Handle click */ }
            )

            Spacer(modifier = Modifier.height(16.dp)) // Add space between cards

            // Card 3: Return a Cylinder
            ActionCard(
                title = "Return a Cylinder",
                iconResId = Res.drawable.baseline_autorenew_24, // Replace with your drawable resource
                onClick = { /* Handle click */ }
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    iconResId: DrawableResource, // Drawable resource ID
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Cards take 80% of the screen width
            .height(150.dp) // Double the height
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(2.dp, Color(0xFF2f80eb)) // Add border with color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // Center content vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
        ) {
            // Text above the icon
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp) // Space between text and icon
            )

            // Icon below the text (using painterResource)
            Icon(
                painter = painterResource(
                    resource = iconResId
                ), // Load drawable resource
                contentDescription = title,
                tint = Color(0xFF2f80eb),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}