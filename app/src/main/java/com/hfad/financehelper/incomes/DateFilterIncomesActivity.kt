package com.hfad.financehelper.incomes

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
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class DateFilterIncomesActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var startDate: TextView
    private lateinit var endDate: TextView
    private lateinit var startDateButton: ImageButton
    private lateinit var endDateButton: ImageButton
    private lateinit var filterButton: Button
    private lateinit var expenseList: LinearLayout
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""
    private lateinit var editIncomeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_incomes)

        dbManager = MyDbManager(this)
        dbManager.openDb()

        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        filterButton = findViewById(R.id.filterButton)
        expenseList = findViewById(R.id.incomeList)

        startDateButton.setOnClickListener { openCalendar(true) }
        endDateButton.setOnClickListener { openCalendar(false) }
        filterButton.setOnClickListener {
            val start = startDate.text.toString()
            val end = endDate.text.toString()
            if (start.isNotEmpty() && end.isNotEmpty()) {
                loadIncomesByDateRange(start, end)
            } else {
                Toast.makeText(this, getString(R.string.pleaseSelectDates), Toast.LENGTH_SHORT).show()
            }
        }

        editIncomeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val start = startDate.text.toString()
                val end = endDate.text.toString()
                loadIncomesByDateRange(start, end)
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

    private fun loadIncomesByDateRange(start: String, end: String) {
        expenseList.removeAllViews()
        val incomes = dbManager.readIncomesByDateRange(start, end)

        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val currentCurrency = sharedPreferences.getString("selected_currency", "RUB") ?: "RUB"

        val inflater = layoutInflater
        for (income in incomes) {
            val itemView = inflater.inflate(R.layout.transaction_item, expenseList, false)
            val incomeAmount = itemView.findViewById<TextView>(R.id.expenseAmount)
            val incomeCategory = itemView.findViewById<TextView>(R.id.expenseCategory)
            val incomeDate = itemView.findViewById<TextView>(R.id.expenseDate)
            val incomeComment = itemView.findViewById<TextView>(R.id.expenseComment)

            incomeAmount.text = getString(R.string.sumDb, income.amount, currentCurrency)
            incomeCategory.text = getString(R.string.categoryDb, income.category)
            incomeDate.text = getString(R.string.dateDb, income.date)
            incomeComment.text = getString(R.string.commentDb, income.comment)

            itemView.findViewById<View>(R.id.editButton).setOnClickListener {
                val intent = Intent(this, EditIncomeActivity::class.java)
                intent.putExtra("amount", income.amount)
                intent.putExtra("category", income.category)
                intent.putExtra("date", income.date)
                intent.putExtra("comment", income.comment)
                editIncomeLauncher.launch(intent)
            }
            itemView.findViewById<View>(R.id.deleteButton).setOnClickListener {
                onClickDelete(income.amount, income.category, income.date, income.comment, start, end)
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
        dbManager.deleteFromDbIncomes(amount, category, date, comment)
        loadIncomesByDateRange(start, end)
        Toast.makeText(this, getString(R.string.removed), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}