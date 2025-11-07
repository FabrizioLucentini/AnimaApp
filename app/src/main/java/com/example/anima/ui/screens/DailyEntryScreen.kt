package com.example.anima.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anima.viewmodel.DailyViewModel
import com.example.anima.util.DateUtils

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

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            // Reverted to the original simple title (no date) as requested
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
                label = { Text("Qué Hiciste Hoy?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // If the entry being edited is not today's entry, show "Editar" else "Guardar"
                val isEditingPast = currentEntry?.date?.let { it != DateUtils.todayEpochDay() } ?: false
                Button(
                    onClick = {
                        // Pass an onSaved callback that snaps the UI back to today's entry after saving
                        viewModel.updateMoodAndNote(mood, note) {
                            viewModel.loadEntryForDate(DateUtils.todayEpochDay())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.32f)
                ) {
                    Text(text = if (isEditingPast) "Editar" else "Guardar")
                }
            }
        }
    }
}