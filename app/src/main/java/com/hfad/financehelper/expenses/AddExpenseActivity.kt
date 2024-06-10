package com.hfad.financehelper.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var editTextSum: EditText
    private lateinit var editTextComment: EditText
    private lateinit var textViewDate: TextView
    private lateinit var selectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbManager = MyDbManager(this)
        dbManager.openDb()
        editTextSum = findViewById(R.id.editTextSum)
        editTextComment = findViewById(R.id.transactionComment)
        textViewDate = findViewById(R.id.editTextText)

        val radioButtons = listOf<RadioButton>(
            findViewById(R.id.radioButtonHealth),
            findViewById(R.id.radioButtonLeisure),
            findViewById(R.id.radioButtonHouse),
            findViewById(R.id.radioButtonCafe),
            findViewById(R.id.radioButtonEducation),
            findViewById(R.id.radioButtonGifts),
            findViewById(R.id.radioButtonFood),
            findViewById(R.id.radioButtonOther)
        )

        radioButtons[0].isChecked = true
        selectedCategory = radioButtons[0].text.toString()

        for (radioButton in radioButtons) {
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategory = radioButton.text.toString()
                    for (otherButton in radioButtons) {
                        if (otherButton != radioButton) {
                            otherButton.isChecked = false
                        }
                    }
                }
            }
        }

        findViewById<Button>(R.id.addTransactionButton).setOnClickListener {
            saveData()
        }

        textViewDate.setOnClickListener {
            openCalendar()
        }

        setCurrentDate()
    }

    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        textViewDate.text = currentDate
    }

    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                    GregorianCalendar(selectedYear, selectedMonth, selectedDay).time
                )
                textViewDate.text = selectedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun saveData() {
        val amount = editTextSum.text.toString().toDoubleOrNull()
        val comment = editTextComment.text.toString()
        val date = textViewDate.text.toString()

        if (amount == null || selectedCategory.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, getString(R.string.pleaseFillAllFields), Toast.LENGTH_SHORT).show()
            return
        }

        dbManager.insertToDb(false, amount, selectedCategory, date, comment)
        Toast.makeText(this, getString(R.string.transactionSaved), Toast.LENGTH_SHORT).show()

        setResult(RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }

    fun onClickDatePicker(view: View) {
        openCalendar()
    }
}