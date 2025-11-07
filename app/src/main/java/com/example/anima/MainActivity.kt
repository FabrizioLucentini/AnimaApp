package com.example.anima

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anima.ui.screens.CalendarScreen
import com.example.anima.ui.screens.DailyEntryScreen
import com.example.anima.ui.screens.SettingsScreen
import com.example.anima.ui.theme.AnimaTheme
import com.example.anima.viewmodel.DailyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // register permission launcher before setContent
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
            }
        }

        super.onCreate(savedInstanceState)

        // On Android 13+ request POST_NOTIFICATIONS if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // simple top row to switch screens
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(onClick = { navController.navigate("daily") }) {
                Text("Diario")
            }
            Spacer(modifier = Modifier.width(18.dp))
            Button(onClick = { navController.navigate("calendar") }) {
                Text("Calendario")
            }
            Spacer(modifier = Modifier.width(18.dp))
            Button(onClick = { navController.navigate("settings") }) {
                Text("Ajustes")
            }
        }

        NavHost(navController = navController, startDestination = "daily") {
            composable("daily") {
                DailyEntryScreen(viewModel = vm)
            }
            composable("calendar") {
                // Pass an onEdit lambda that waits for loadEntryForDate to complete before navigating
                CalendarScreen(viewModel = vm, onEdit = { epochDay ->
                    coroutineScope.launch {
                        // suspend until the entry is loaded/created
                        vm.loadEntryForDate(epochDay)
                        // navigate to diary after entry is ready
                        navController.navigate("daily")
                    }
                })
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
