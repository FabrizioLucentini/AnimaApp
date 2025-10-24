package com.example.anima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.anima.ui.theme.AnimaTheme
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.anima.ui.screens.DailyEntryScreen
import com.example.anima.ui.screens.CalendarScreen
import com.example.anima.ui.screens.SettingsScreen
import com.example.anima.viewmodel.DailyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val vm: DailyViewModel = viewModel()

    Scaffold(modifier = modifier) { padding ->
        // simple top row to switch screens
        Row(modifier = Modifier
            .padding(8.dp)) {
            Button(onClick = { navController.navigate("daily") }) {
                Text("Diario")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("calendar") }) {
                Text("Calendario")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("settings") }) {
                Text("Ajustes")
            }
        }

        NavHost(navController = navController, startDestination = "daily") {
            composable("daily") {
                DailyEntryScreen(viewModel = vm)
            }
            composable("calendar") {
                CalendarScreen(viewModel = vm)
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AnimaTheme {
        Greeting("Android")
    }
}