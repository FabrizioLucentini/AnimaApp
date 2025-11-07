package com.example.anima.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anima.data.model.DailyEntry
import com.example.anima.util.DateUtils
import com.example.anima.util.MoodColor
import com.example.anima.viewmodel.DailyViewModel
import java.util.Calendar

@Composable
fun CalendarScreen(viewModel: DailyViewModel, onEdit: (Long) -> Unit = {}) {
    var entries by remember { mutableStateOf<List<DailyEntry>>(emptyList()) }
    val todayEpoch = DateUtils.todayEpochDay()
    val todayCal = DateUtils.calendarForEpochDay(todayEpoch)
    var displayedYear by remember { mutableStateOf(todayCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableStateOf(todayCal.get(Calendar.MONTH) + 1) } // 1..12
    var selectedEpochDay by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(displayedYear, displayedMonth) {
        entries = viewModel.getEntriesForMonth(displayedYear, displayedMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                // go to previous month
                if (displayedMonth == 1) {
                    displayedMonth = 12
                    displayedYear -= 1
                } else displayedMonth -= 1
            }) { Text("<") }

            Spacer(modifier = Modifier.width(8.dp))
            val headerLabel = DateUtils.formatMonthYear(displayedYear, displayedMonth)
            Text(
                text = headerLabel,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                // next month
                if (displayedMonth == 12) {
                    displayedMonth = 1
                    displayedYear += 1
                } else displayedMonth += 1
            }) { Text(">") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val moodMap = remember(entries) {
            entries.filter { it.mood != null }.associateBy { it.date }
        }

        val (firstDay, lastDay) = DateUtils.epochDayRangeForMonth(displayedYear, displayedMonth)
        val daysInMonth = (lastDay - firstDay + 1).toInt().coerceAtLeast(1)

        // Render a simple 7-column grid (Sun..Sat)
        Column {
            val startWeekday = DateUtils.calendarForEpochDay(firstDay).get(Calendar.DAY_OF_WEEK) // 1=Sun..7=Sat
            // number of leading blanks before day 1
            val leadingBlanks = (startWeekday - Calendar.SUNDAY)

            val totalCells = leadingBlanks + daysInMonth
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - leadingBlanks + 1
                        if (cellIndex < leadingBlanks || dayNumber > daysInMonth) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            )
                        } else {
                            val epochDay = firstDay + (dayNumber - 1)
                            val entry = moodMap[epochDay]
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MoodColor.colorFor(entry?.mood))
                                .clickable {
                                    // Select the day to show its note in a popup. Editing is done via the "Editar" button.
                                    selectedEpochDay = epochDay
                                }, contentAlignment = Alignment.Center) {
                                Text(text = dayNumber.toString(), fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Show popup card for the selected day (if any)
        if (selectedEpochDay != null) {
            val sel = selectedEpochDay!!
            val selectedEntry = entries.firstOrNull { it.date == sel }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nota del ${DateUtils.formatter.format(DateUtils.calendarForEpochDay(sel).time)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = selectedEntry?.note ?: "No hay nota para este día.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { selectedEpochDay = null }) {
                            Text("Cerrar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            // Delegate edit action to the caller; the caller can load/create the entry and navigate.
                            onEdit(sel)
                            selectedEpochDay = null
                        }) {
                            Text("Editar")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Draw a simple line chart for mood over the month
        val moodEntries = entries.filter { it.mood != null }.sortedBy { it.date }
        if (moodEntries.size < 2) {
            Text(text = "No hay suficientes datos para graficar")
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)) {
                    val left = 20f
                    val right = size.width - 8f
                    val top = 8f
                    val bottom = size.height - 20f
                    val usableW = right - left
                    val usableH = bottom - top

                    // build path
                    val path = Path()
                    moodEntries.forEachIndexed { idx, entry ->
                        val xRatio =
                            (entry.date - firstDay).toFloat() / (daysInMonth - 1).coerceAtLeast(1).toFloat()
                        val x = left + xRatio * usableW
                        val yRatio = (entry.mood!! - 1f) / 9f // 0..1
                        val y = top + (1f - yRatio) * usableH
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    // draw line
                    drawPath(path = path, color = androidx.compose.ui.graphics.Color(0xFF4CAF50), style = Stroke(width = 4f))

                    // draw points
                    moodEntries.forEach { entry ->
                        val xRatio =
                            (entry.date - firstDay).toFloat() / (daysInMonth - 1).coerceAtLeast(1).toFloat()
                        val x = left + xRatio * usableW
                        val yRatio = (entry.mood!! - 1f) / 9f
                        val y = top + (1f - yRatio) * usableH
                        drawCircle(color = MoodColor.colorFor(entry.mood), radius = 6f, center = Offset(x, y))
                    }
                }
            }

            val avg = moodEntries.map { it.mood!! }.average()
            Text(text = "Promedio del mes: ${"%.1f".format(avg)}")
        }
    }
}