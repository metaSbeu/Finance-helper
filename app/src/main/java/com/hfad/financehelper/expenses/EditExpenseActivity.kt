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

class EditExpenseActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_edit_expense)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbManager = MyDbManager(this)
        dbManager.openDb()

        editAmount = findViewById(R.id.editExpenseAmount)
        editDate = findViewById(R.id.editExpenseDate)
        editComment = findViewById(R.id.editExpenseComment)
        saveButton = findViewById(R.id.saveExpenseButton)
        currentValueLabel = findViewById(R.id.currentValueLabel)

        radioButtons = listOf(
            findViewById(R.id.radioButtonHealth),
            findViewById(R.id.radioButtonLeisure),
            findViewById(R.id.radioButtonHouse),
            findViewById(R.id.radioButtonCafe),
            findViewById(R.id.radioButtonEducation),
            findViewById(R.id.radioButtonGifts),
            findViewById(R.id.radioButtonFood),
            findViewById(R.id.radioButtonOther)
        )

        val oldAmount = intent.getDoubleExtra("amount", 0.0)
        val oldCategory = intent.getStringExtra("category")
        val oldDate = intent.getStringExtra("date")
        val oldComment = intent.getStringExtra("comment")

        currentValueLabel.text =
            getString(R.string.editCurrentValues, oldAmount, oldCategory, oldDate, oldComment)

        editAmount.setText(oldAmount.toString())
        editDate.text = oldDate
        editComment.setText(oldComment)
        selectedCategory = oldCategory ?: ""

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

            if (radioButton.text.toString() == oldCategory) {
                radioButton.isChecked = true
            }
        }

        saveButton.setOnClickListener {
            val newAmount = editAmount.text.toString().toDoubleOrNull()
            val newCategory = selectedCategory
            val newDate = editDate.text.toString()
            val newComment = editComment.text.toString()

            if (newAmount == null || newCategory.isEmpty() || newDate.isEmpty()) {
                Toast.makeText(this, getString(R.string.pleaseFillAllFields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dbManager.updateExpense(
                oldAmount, oldCategory!!, oldDate!!, oldComment!!,
                newAmount, newCategory, newDate, newComment
            )
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
                editDate.text = selectedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    fun onClickDatePicker(view: View) {
        openCalendar()
    }

    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        editDate.text = currentDate
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}
