package com.example.anima.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.anima.util.AlarmHelper
import com.example.anima.util.SecurePrefs
import java.util.Calendar

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = SecurePrefs(context)

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (!granted) {
            Toast.makeText(context, "Permiso de notificaciones denegado. No se programará el recordatorio.", Toast.LENGTH_SHORT).show()
        }
    }

    var reminderEnabled by remember { mutableStateOf(prefs.isReminderEnabled()) }
    var hour by remember { mutableStateOf(prefs.getReminderHour()) }
    var minute by remember { mutableStateOf(prefs.getReminderMinute()) }
    var message by remember { mutableStateOf(TextFieldValue(prefs.getReminderMessage())) }
    var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
    var newPin by remember { mutableStateOf("") }
    var newPinConfirm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Recordatorio", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Activar")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            // show TimePicker
            val tp = TimePickerDialog(context, { _, h, m ->
                hour = h
                minute = m
            }, hour, minute, true)
            tp.show()
        }) {
            Text(text = "Hora: %02d:%02d".format(hour, minute))
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensaje del recordatorio") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            prefs.setReminderEnabled(reminderEnabled)
            prefs.setReminderHour(hour)
            prefs.setReminderMinute(minute)
            prefs.setReminderMessage(message.text)

            // On Android 13+ request POST_NOTIFICATIONS permission before scheduling
            if (reminderEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        // user will decide; don't schedule until permission result or they re-save
                        Toast.makeText(context, "Solicitando permiso de notificaciones... Vuelva a guardar para programar si concede permiso.", Toast.LENGTH_LONG).show()
                    } else {
                        AlarmHelper.scheduleDailyReminder(context, hour, minute, message.text)
                    }
                } else {
                    AlarmHelper.scheduleDailyReminder(context, hour, minute, message.text)
                }
            } else {
                AlarmHelper.cancelReminder(context)
            }

            if (reminderEnabled) {
                // if already scheduled above it was scheduled; otherwise a toast informs the user
            }
            Toast.makeText(context, "Recordatorio configurado correctamente", Toast.LENGTH_SHORT).show()
        }) {
            Text("Guardar recordatorio")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Seguridad", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = newPin, onValueChange = { newPin = it }, label = { Text("Nuevo PIN") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = newPinConfirm, onValueChange = { newPinConfirm = it }, label = { Text("Confirmar PIN") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (newPin.isBlank()) { Toast.makeText(context, "PIN no puede estar vacío", Toast.LENGTH_SHORT).show(); return@Button }
            if (newPin != newPinConfirm) { Toast.makeText(context, "PIN y confirmación no coinciden", Toast.LENGTH_SHORT).show(); return@Button }
            prefs.setPin(newPin)
            Toast.makeText(context, "PIN guardado correctamente", Toast.LENGTH_SHORT).show()
            newPin = ""
            newPinConfirm = ""
        }) {
            Text("Establecer PIN")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Apariencia", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { themeMode = 0 }) { Text("Seguir sistema") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { themeMode = 1 }) { Text("Claro") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { themeMode = 2 }) { Text("Oscuro") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            prefs.setThemeMode(themeMode)
            Toast.makeText(context, "Modo oscuro activado correctamente", Toast.LENGTH_SHORT).show()
        }) {
            Text("Guardar apariencia")
        }
    }
}
