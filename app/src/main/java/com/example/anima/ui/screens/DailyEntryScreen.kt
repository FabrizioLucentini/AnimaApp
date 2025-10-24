package com.example.anima.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anima.viewmodel.DailyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyEntryScreen(viewModel: DailyViewModel) {
    val currentEntry by viewModel.currentEntry.collectAsState()
    var mood by remember { mutableStateOf(currentEntry?.mood ?: 5) }
    var note by remember { mutableStateOf(currentEntry?.note ?: "") }

    // Keep local state in sync when ViewModel updates
    LaunchedEffect(currentEntry) {
        mood = currentEntry?.mood ?: 5
        note = currentEntry?.note ?: ""
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Registro diario")
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Estado de ánimo: $mood")
        Slider(
            value = mood.toFloat(),
            onValueChange = { mood = it.toInt() },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Nota breve") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.updateMoodAndNote(mood, note)
        }) {
            Text(text = "Guardar")
        }
    }
}
