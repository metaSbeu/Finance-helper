package com.hfad.financehelper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetPinActivity : AppCompatActivity() {
    private lateinit var pinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var savePinButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_pin)

        pinEditText = findViewById(R.id.pin_edit_text)
        confirmPinEditText = findViewById(R.id.confirm_pin_edit_text)
        savePinButton = findViewById(R.id.save_pin_button)

        savePinButton.setOnClickListener {
            val pin = pinEditText.text.toString()
            val confirmPin = confirmPinEditText.text.toString()

            if (pin.isNotEmpty() && pin == confirmPin) {
                val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("pin_code", pin)
                    apply()
                }
                Toast.makeText(this, getString(R.string.pinSaved), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.pinNotMatch), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
