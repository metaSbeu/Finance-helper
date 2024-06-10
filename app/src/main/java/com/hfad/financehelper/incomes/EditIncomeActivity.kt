package com.hfad.financehelper.incomes

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class EditIncomeActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var editAmount: EditText
    private lateinit var editDate: TextView
    private lateinit var editComment: EditText
    private lateinit var saveButton: Button
    private lateinit var currentValueLabel: TextView
    private lateinit var selectedCategory: String
    private lateinit var radioButtons: List<RadioButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_income)

        dbManager = MyDbManager(this)
        dbManager.openDb()

        editAmount = findViewById(R.id.editIncomeAmount)
        editDate = findViewById(R.id.editIncomeDate)
        editComment = findViewById(R.id.editIncomeComment)
        saveButton = findViewById(R.id.saveIncomeButton)
        currentValueLabel = findViewById(R.id.currentValueLabel)

        radioButtons = listOf(
            findViewById(R.id.radioButtonSalary),
            findViewById(R.id.radioButtonGifts),
            findViewById(R.id.radioButtonInvestments),
            findViewById(R.id.radioButtonOther)
        )

        val oldAmount = intent.getDoubleExtra("amount", 0.0)
        val oldCategory = intent.getStringExtra("category")
        val oldDate = intent.getStringExtra("date")
        val oldComment = intent.getStringExtra("comment")

        currentValueLabel.text =
            getString(R.string.editCurrentValues, oldAmount, oldCategory, oldDate, oldComment)

        editAmount.setText(oldAmount.toString())
        editDate.setText(oldDate)
        editComment.setText(oldComment)

        oldCategory?.let { category ->
            radioButtons.forEach { radioButton ->
                if (radioButton.text.toString() == category) {
                    radioButton.isChecked = true
                    selectedCategory = category
                }
            }
        }

        radioButtons.forEach { radioButton ->
            radioButton.setOnClickListener {
                selectedCategory = radioButton.text.toString()
            }
        }

        saveButton.setOnClickListener {
            val newAmount = editAmount.text.toString().toDoubleOrNull()
            val newDate = editDate.text.toString()
            val newComment = editComment.text.toString()

            if (newAmount == null || selectedCategory.isEmpty() || newDate.isEmpty()) {
                Toast.makeText(this, getString(R.string.pleaseFillAllFields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dbManager.updateIncome(oldAmount, oldCategory!!, oldDate!!, oldComment!!, newAmount, selectedCategory, newDate, newComment)
            setResult(RESULT_OK)
            finish()
        }
        setCurrentDate()
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
                editDate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        editDate.text = currentDate
    }

    fun onClickDatePicker(view: View) {
        openCalendar()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}
