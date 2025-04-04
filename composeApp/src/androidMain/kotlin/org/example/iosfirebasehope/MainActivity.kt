package org.example.iosfirebasehope

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore


import org.example.iosfirebasehope.navigation.RootComponent


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = retainedComponent(){ //helps with phone rotations
            RootComponent(it)
        }

        // Check internet connectivity first
        if (isNetworkAvailable()) {
            FirebaseApp.initializeApp(this)
            val db = Firebase.firestore
            setContent {
                App(rootComponent = root, db)
            }
        } else {
            // Handle offline state
            Toast.makeText(this, "No internet connection. Some features may not work.", Toast.LENGTH_LONG).show()

            // Initialize Firebase anyway - it will work in offline mode if configured properly
            FirebaseApp.initializeApp(this)
            val db = Firebase.firestore
            setContent {
                App(rootComponent = root, db)
            }
        }
    }

    // Simple method to check network availability
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            // For older Android versions
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}