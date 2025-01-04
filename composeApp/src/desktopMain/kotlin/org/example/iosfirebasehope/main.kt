package org.example.iosfirebasehope

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.example.iosfirebasehope.navigation.RootComponent

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "iosFirebaseHope",
    ) {
        val root = remember {
            RootComponent(DefaultComponentContext(lifecycle = LifecycleRegistry()))
        }
        val db = Firebase.firestore
        App(rootComponent = root, db = db)
    }
}