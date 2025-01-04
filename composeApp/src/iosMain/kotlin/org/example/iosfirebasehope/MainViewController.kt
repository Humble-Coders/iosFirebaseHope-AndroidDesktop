package org.example.iosfirebasehope

import androidx.compose.ui.window.ComposeUIViewController
import org.example.iosfirebasehope.navigation.RootComponent
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

fun MainViewController() = ComposeUIViewController {
    val root = remember {
        RootComponent(DefaultComponentContext(lifecycle = LifecycleRegistry()))
    }
    val db = Firebase.firestore
    App(rootComponent = root, db = db)
}