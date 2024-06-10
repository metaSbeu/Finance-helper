package com.hfad.financehelper.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hfad.financehelper.R

class ChangePinActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentPinEditText: EditText
    private lateinit var newPinEditText: EditText
    private lateinit var savePinButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)

        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        currentPinEditText = findViewById(R.id.current_pin)
        newPinEditText = findViewById(R.id.new_pin)
        savePinButton = findViewById(R.id.save_pin_button)

        savePinButton.setOnClickListener {
            val currentPin = currentPinEditText.text.toString()
            val newPin = newPinEditText.text.toString()

            val savedPin = sharedPreferences.getString("pin_code", "0000")
            if (currentPin == savedPin) {
                if (newPin.length == 4) {
                    sharedPreferences.edit().putString("pin_code", newPin).apply()
                    Toast.makeText(this, "PIN-код изменен успешно", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Новый PIN-код должен быть 4 цифры", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Текущий PIN-код неверен", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
