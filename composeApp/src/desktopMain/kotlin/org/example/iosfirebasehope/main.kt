package org.example.iosfirebasehope

import android.app.Application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import org.example.iosfirebasehope.navigation.RootComponent

fun main() = application {
    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {

        val storage = mutableMapOf<String, String>()
        override fun clear(key: String) {
            storage.remove(key)
        }

        override fun log(msg: String) = println(msg)

        override fun retrieve(key: String) = storage[key]

        override fun store(key: String, value: String) = storage.set(key, value)

    })

    val options = FirebaseOptions(
        projectId = "cylinder-management-24e4c",
        applicationId = "1:738035583514:web:16287d2bf644a445bd1fa3",
        apiKey = "AIzaSyBtxQ_xs4tS6WlhyAhCdVu80UrG7GbPJGY"
    )

    Firebase.initialize(Application(), options)

    Window(
        onCloseRequest = ::exitApplication,
        title = "iosFirebaseHope",
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            width = 1280.dp,
            height = 720.dp
        )
    )  {
        val root = remember {
            RootComponent(DefaultComponentContext(lifecycle = LifecycleRegistry()))
        }
        val db = Firebase.firestore
        App(rootComponent = root, db = db)
    }
}