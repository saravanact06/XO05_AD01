package com.thambithappu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ThambiThappuApp()
                }
            }
        }
    }
}

private enum class Screen { Home, Demo }

@Composable
fun ThambiThappuApp() {
    var screen by remember { mutableStateOf(Screen.Home) }
    var settings by remember { mutableStateOf(ProtectionSettings()) }

    when (screen) {
        Screen.Home -> HomeScreen(
            settings = settings,
            onSettingsChange = { settings = it },
            onOpenDemo = { screen = Screen.Demo },
            onSettingsClick = { /* full settings screen not built yet - hook up later */ }
        )
        Screen.Demo -> ProtectedDemoScreen(
            settings = settings,
            onExit = { screen = Screen.Home }
        )
    }
}
