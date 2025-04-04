package org.example.iosfirebasehope

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.components.GenerateBillScreenComponent

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun GenerateBillScreenUI(
    customerName: String,
    dateTime: String,
   db: FirebaseFirestore,
   component: GenerateBillScreenComponent
)


