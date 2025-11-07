package com.example.anima.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.app.TimePickerDialog
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
import android.provider.Settings
import androidx.core.net.toUri

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = SecurePrefs(context)

    var reminderEnabled by remember { mutableStateOf(prefs.isReminderEnabled()) }
    var hour by remember { mutableStateOf(prefs.getReminderHour()) }
    var minute by remember { mutableStateOf(prefs.getReminderMinute()) }
    var message by remember { mutableStateOf(TextFieldValue(prefs.getReminderMessage())) }
    var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
    var newPin by remember { mutableStateOf("") }
    var newPinConfirm by remember { mutableStateOf("") }
    // UI state to show explanatory dialogs for permissions/settings
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showExactAlarmsDialog by remember { mutableStateOf(false) }
    // helper to open notification settings for older devices
    val openNotificationSettings = {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Throwable) {
            // silent fail: avoid debug logs in production UI
            Toast.makeText(context, "No se pudo abrir ajustes de notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // register permission launcher after state so callback can access current values
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (!granted) {
            Toast.makeText(context, "Permiso de notificaciones denegado. No se programará el recordatorio.", Toast.LENGTH_SHORT).show()
        } else {
            // If the user granted permission and the user currently wants reminders enabled,
            // schedule the reminder now.
            if (reminderEnabled) {
                try {
                    AlarmHelper.scheduleDailyReminder(context, hour, minute, message.text)
                    Toast.makeText(context, "Recordatorio programado", Toast.LENGTH_SHORT).show()
                } catch (t: Throwable) {
                    // ignore detailed debug log
                    Toast.makeText(context, "No se pudo programar el recordatorio: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // diagnostics removed for production
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
            try {
                prefs.setReminderEnabled(reminderEnabled)
                prefs.setReminderHour(hour)
                prefs.setReminderMinute(minute)
                prefs.setReminderMessage(message.text)

                // On Android 13+ request POST_NOTIFICATIONS permission before scheduling
                if (reminderEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            // show a dialog explaining why we need notification permission; launcher will be invoked if user accepts
                            showNotificationPermissionDialog = true
                            // user will decide in the dialog; don't schedule until permission result or they re-save
                        } else {
                            try {
                                AlarmHelper.scheduleDailyReminder(context, hour, minute, message.text)
                                Toast.makeText(context, "Recordatorio programado", Toast.LENGTH_SHORT).show()
                                // (test notification removed to avoid instant notification on save)
                            } catch (t: Throwable) {
                                // ignore detailed debug log
                                Toast.makeText(context, "No se pudo programar el recordatorio: ${t.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        try {
                            AlarmHelper.scheduleDailyReminder(context, hour, minute, message.text)
                            Toast.makeText(context, "Recordatorio programado", Toast.LENGTH_SHORT).show()
                            // (test notification removed to avoid instant notification on save)
                        } catch (t: Throwable) {
                            // ignore detailed debug log
                            Toast.makeText(context, "No se pudo programar el recordatorio: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    try {
                        AlarmHelper.cancelReminder(context)
                    } catch (t: Throwable) {
                        // ignore detailed debug log
                        Toast.makeText(context, "No se pudo cancelar el recordatorio: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }

                Toast.makeText(context, "Recordatorio configurado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Toast.makeText(context, "Error al guardar recordatorio: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }) {
            Text("Guardar recordatorio")
        }

        // Notification permission explanatory dialog
        if (showNotificationPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationPermissionDialog = false },
                title = { Text("Permisos para recordatorios y notificaciones") },
                text = { Text("La aplicación necesita permiso para mostrar notificaciones (para recordatorios) y, según la versión de Android, permiso para programar alarmas exactas. ¿Desea conceder estos permisos ahora?") },
                confirmButton = {
                    TextButton(onClick = {
                        showNotificationPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openNotificationSettings()
                        }
                    }) { Text("Abrir") }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationPermissionDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // Exact alarms explanatory dialog
        if (showExactAlarmsDialog) {
            AlertDialog(
                onDismissRequest = { showExactAlarmsDialog = false },
                title = { Text("Permiso para alarmas (recordatorios exactos)") },
                text = { Text("La aplicación necesita permiso para programar alarmas exactas para que los recordatorios se disparen a la hora correcta. ¿Desea abrir los ajustes para permitirlo?") },
                confirmButton = {
                    TextButton(onClick = {
                        showExactAlarmsDialog = false
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intentSettings = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(intentSettings)
                            } else {
                                // fallback: open app details settings so user can inspect permissions
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = "package:${context.packageName}".toUri()
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                         } catch (ex: Throwable) {
                             // ignore detailed debug log
                             Toast.makeText(context, "No se pudo abrir ajustes de alarmas exactas: ${ex.message}", Toast.LENGTH_LONG).show()
                         }
                    }) { Text("Abrir ajustes") }
                },
                dismissButton = {
                    TextButton(onClick = { showExactAlarmsDialog = false }) { Text("Cancelar") }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
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
        HorizontalDivider()
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
