package com.hfad.financehelper.expenses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class DateFilterExpensesActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var startDate: TextView
    private lateinit var endDate: TextView
    private lateinit var startDateButton: ImageButton
    private lateinit var endDateButton: ImageButton
    private lateinit var filterButton: Button
    private lateinit var expenseList: LinearLayout
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""
    private lateinit var editExpenseLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentCurrency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_expenses)

        dbManager = MyDbManager(this)
        dbManager.openDb()
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        currentCurrency = sharedPreferences.getString("selected_currency", "RUB") ?: "RUB"
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        filterButton = findViewById(R.id.filterButton)
        expenseList = findViewById(R.id.expenseList)

        startDateButton.setOnClickListener { openCalendar(true) }
        endDateButton.setOnClickListener { openCalendar(false) }
        filterButton.setOnClickListener {
            val start = startDate.text.toString()
            val end = endDate.text.toString()
            if (start.isNotEmpty() && end.isNotEmpty()) {
                loadExpensesByDateRange(start, end)
            } else {
                Toast.makeText(this, getString(R.string.pleaseSelectDates), Toast.LENGTH_SHORT).show()
            }
        }

        editExpenseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val start = startDate.text.toString()
                val end = endDate.text.toString()
                if (start.isNotEmpty() && end.isNotEmpty()) {
                    loadExpensesByDateRange(start, end)
                }
            }
        }
    }

    private fun openCalendar(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    GregorianCalendar(selectedYear, selectedMonth, selectedDay).time
                )
                if (isStartDate) {
                    selectedStartDate = selectedDate
                    startDate.setText(selectedDate)
                } else {
                    selectedEndDate = selectedDate
                    endDate.setText(selectedDate)
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun loadExpensesByDateRange(start: String, end: String) {
        expenseList.removeAllViews()
        val expenses = dbManager.readExpensesByDateRange(start, end)

        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val currentCurrency = sharedPreferences.getString("selected_currency", "RUB") ?: "RUB"

        val inflater = layoutInflater
        for (expense in expenses) {
            val itemView = inflater.inflate(R.layout.transaction_item, expenseList, false)
            val expenseAmount = itemView.findViewById<TextView>(R.id.expenseAmount)
            val expenseCategory = itemView.findViewById<TextView>(R.id.expenseCategory)
            val expenseDate = itemView.findViewById<TextView>(R.id.expenseDate)
            val expenseComment = itemView.findViewById<TextView>(R.id.expenseComment)


            expenseAmount.text = getString(R.string.sumDb, expense.amount, currentCurrency)
            expenseCategory.text = getString(R.string.categoryDb, expense.category)
            expenseDate.text = getString(R.string.dateDb, expense.date)
            expenseComment.text = getString(R.string.commentDb, expense.comment)

            itemView.findViewById<View>(R.id.editButton).setOnClickListener {
                val intent = Intent(this, EditExpenseActivity::class.java)
                intent.putExtra("amount", expense.amount)
                intent.putExtra("category", expense.category)
                intent.putExtra("date", expense.date)
                intent.putExtra("comment", expense.comment)
                editExpenseLauncher.launch(intent)
            }

            itemView.findViewById<View>(R.id.deleteButton).setOnClickListener {
                onClickDelete(expense.amount, expense.category, expense.date, expense.comment, start, end)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            itemView.layoutParams = params

            expenseList.addView(itemView)
        }
    }

    private fun onClickDelete(amount: Double, category: String, date: String, comment: String, start: String, end: String) {
        dbManager.deleteFromDbExpenses(amount, category, date, comment)
        loadExpensesByDateRange(start, end)
        Toast.makeText(this, getString(R.string.removed), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}