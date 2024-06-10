package com.hfad.financehelper

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var cancellationSignal: CancellationSignal? = null
    private lateinit var pinText: EditText

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setAppLocale(this)

        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        if (isFirstRun(sharedPreferences)) {
            startActivity(Intent(this, SetPinActivity::class.java))
            finish()
            return
        }

        checkBiometricSupport()

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pinText = findViewById(R.id.pin_text)
        fun checkPinAndNavigate(pin: String) {
            val savedPin = sharedPreferences.getString("pin_code", "0000")
            if (pin == savedPin) {
                pinText.setText("")
                startActivity(Intent(this, SecretActivity::class.java))
                notifyUser(getString(R.string.authSuccess))
            } else {
                if (pinText.length() >= 4) {
                    notifyUser(getString(R.string.incorrectCode))
                }
            }
        }

        val pinButtons = arrayOfNulls<Button>(10)
        for (i in 0..9) {
            val resID = resources.getIdentifier("pin_button$i", "id", packageName)
            pinButtons[i] = findViewById(resID)
            pinButtons[i]?.setOnClickListener {
                val digit = (it as Button).text.toString()
                pinText.append(digit)
                checkPinAndNavigate(pinText.text.toString())
            }
        }

        val btnErase: View = findViewById<Button>(R.id.btn_erase)
        btnErase.setOnClickListener {
            val currentText = pinText.text.toString()
            if (currentText.isNotEmpty()) {
                pinText.setText(currentText.substring(0, currentText.length - 1))
            }
        }

        val btnAuthenticate: View = findViewById(R.id.btn_fingerprint)
        btnAuthenticate.setOnClickListener {
            fingerprintAuthentication()
        }
    }

    private fun isFirstRun(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getString("pin_code", null) == null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun fingerprintAuthentication() {
        val biometricPrompt = BiometricPrompt.Builder(this)
            .setTitle(getString(R.string.userAuthentication))
            .setSubtitle(getString(R.string.enterApp))
            .setNegativeButton(getString(R.string.cancel), this.mainExecutor, DialogInterface.OnClickListener { dialog, which ->
            }).build()
        biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure) {
            notifyUser(getString(R.string.fingerPrintAuthNotEnabled))
            return false
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser(getString(R.string.fingerprintNoPermission))
            return false
        }

        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser(getString(R.string.authCancelled))
        }
        return cancellationSignal as CancellationSignal
    }

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser(getString(R.string.authError, errString))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser(getString(R.string.authSuccess))
                    startActivity(Intent(this@MainActivity, SecretActivity::class.java))
                }
            }

    private fun setAppLocale(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("selected_language", "Русский")
        val locale = when (savedLanguage) {
            "English" -> Locale("en")
            "Deutsch" -> Locale("de")
            else -> Locale("ru")
        }
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
