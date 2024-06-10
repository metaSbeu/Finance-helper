package com.hfad.financehelper.incomes

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

class AddIncomeActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var editTextSum: EditText
    private lateinit var editTextComment: EditText
    private lateinit var selectedCategory: String
    private lateinit var dateTextView: TextView
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_income)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbManager = MyDbManager(this)
        dbManager.openDb()

        editTextSum = findViewById(R.id.editTextSum)
        editTextComment = findViewById(R.id.transactionComment)
        dateTextView = findViewById(R.id.dateTextView)

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        dateTextView.text = currentDate
        selectedDate = currentDate

        val radioButtonSalary = findViewById<RadioButton>(R.id.radioButtonSalary)
        radioButtonSalary.isChecked = true
        selectedCategory = radioButtonSalary.text.toString()

        val radioButtons = listOf(
            radioButtonSalary,
            findViewById<RadioButton>(R.id.radioButtonGifts),
            findViewById<RadioButton>(R.id.radioButtonInvestments),
            findViewById<RadioButton>(R.id.radioButtonOther)
        )

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
    }

    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                    GregorianCalendar(selectedYear, selectedMonth, selectedDay).time
                )
                dateTextView.text = selectedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    fun onClickCalendar(view: View) {
        openCalendar()
    }

    private fun saveData() {
        val amount = editTextSum.text.toString().toDoubleOrNull()
        val comment = editTextComment.text.toString()
        val dateText = dateTextView.text.toString()

        if (amount == null || selectedCategory.isEmpty() || dateText.isEmpty()) {
            Toast.makeText(this, getString(R.string.pleaseFillAllFields), Toast.LENGTH_SHORT).show()
            return
        }

        dbManager.insertToDb(true, amount, selectedCategory, dateText, comment)
        Toast.makeText(this, getString(R.string.transactionSaved), Toast.LENGTH_SHORT).show()

        setResult(RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}
