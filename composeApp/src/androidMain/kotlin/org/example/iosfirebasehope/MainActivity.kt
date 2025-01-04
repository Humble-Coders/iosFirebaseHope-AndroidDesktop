package org.example.iosfirebasehope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.example.iosfirebasehope.UI.AddCylinderScreenUI


import org.example.iosfirebasehope.navigation.RootComponent


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = retainedComponent(){ //helps with phone rotations
            RootComponent(it)
        }

        FirebaseApp.initializeApp(this)
        val db = Firebase.firestore
        setContent {
            App(rootComponent = root, db)
            //AddCylinderScreenUI()
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}