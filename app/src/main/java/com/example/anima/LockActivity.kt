package com.example.anima

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.anima.ui.theme.AnimaTheme
import com.example.anima.util.SecurePrefs
import java.util.concurrent.Executor

class LockActivity : FragmentActivity() {
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executor = ContextCompat.getMainExecutor(this)

        val prefs = SecurePrefs(this)
        val canBiometric = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS

        if (canBiometric) {
            val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    runOnUiThread {
                        Toast.makeText(this@LockActivity, "Acceso concedido", Toast.LENGTH_SHORT).show()
                        goToMain()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // fall back to PIN UI
                    runOnUiThread { showPinUI(prefs) }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread { showPinUI(prefs) }
                }
            })

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación requerida")
                .setSubtitle("Por seguridad, inicia sesión con huella o PIN")
                .setNegativeButtonText("Usar PIN")
                .build()

            prompt.authenticate(info)
        } else {
            setContent { AnimaTheme { ShowPinComposable(prefs) } }
        }
    }

    private fun showPinUI(prefs: SecurePrefs) {
        setContent { AnimaTheme { ShowPinComposable(prefs) } }
    }

    @Composable
    fun ShowPinComposable(prefs: SecurePrefs) {
        var pin by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        val lockUntil = prefs.getLockUntil()
        val now = System.currentTimeMillis()

        Surface(modifier = Modifier.fillMaxSize()) {
            if (lockUntil > now) {
                val remaining = (lockUntil - now) / 1000
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("La app está bloqueada. Inténtalo en $remaining segundos")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Introduce tu PIN")
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val stored = prefs.getPin()
                        if (stored == null) {
                            // no PIN set, allow set this as PIN
                            prefs.setPin(pin)
                            Toast.makeText(this@LockActivity, "PIN guardado. Acceso concedido", Toast.LENGTH_SHORT)
                                .show()
                            goToMain()
                        } else if (stored == pin) {
                            prefs.resetFailedAttempts()
                            Toast.makeText(this@LockActivity, "Acceso concedido", Toast.LENGTH_SHORT).show()
                            goToMain()
                        } else {
                            val attempts = prefs.incrementFailedAttempts()
                            if (attempts >= 3) {
                                val lockMs = System.currentTimeMillis() + 60_000L // bloquea 1 minuto
                                prefs.setLockUntil(lockMs)
                                prefs.resetFailedAttempts()
                                message = "PIN incorrecto. Acceso bloqueado 1 minuto"
                            } else {
                                message = "PIN incorrecto"
                            }
                        }
                    }) {
                        Text("Entrar")
                    }
                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(message)
                    }
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}