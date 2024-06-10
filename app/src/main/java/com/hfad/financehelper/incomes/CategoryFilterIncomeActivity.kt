package com.hfad.financehelper.incomes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager

class CategoryFilterIncomeActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var radioGroupTransactionCategory: RadioGroup
    private lateinit var filterButton: Button
    private lateinit var expenseList: LinearLayout
    private lateinit var editIncomeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_filter_income)

        dbManager = MyDbManager(this)
        dbManager.openDb()

        radioGroupTransactionCategory = findViewById(R.id.radioGroupTransactionCategory)
        filterButton = findViewById(R.id.filterButton)
        expenseList = findViewById(R.id.expenseList)

        filterButton.setOnClickListener {
            val selectedCategory = getSelectedCategory()
            if (selectedCategory != null) {
                loadIncomesByCategory(selectedCategory)
            } else {
                Toast.makeText(this, getString(R.string.pleaseTypeCategory), Toast.LENGTH_SHORT).show()
            }
        }

        editIncomeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedCategory = getSelectedCategory()
                if (selectedCategory != null) {
                    loadIncomesByCategory(selectedCategory)
                }
            }
        }
    }

    private fun getSelectedCategory(): String? {
        val selectedRadioButtonId = radioGroupTransactionCategory.checkedRadioButtonId
        if (selectedRadioButtonId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            return selectedRadioButton.text.toString()
        }
        return null
    }

    private fun loadIncomesByCategory(category: String) {
        expenseList.removeAllViews()
        val incomes = dbManager.readIncomesByCategory(category)

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
                onClickDelete(income.amount, income.category, income.date, income.comment)
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

    private fun onClickDelete(amount: Double, category: String, date: String, comment: String) {
        dbManager.deleteFromDbIncomes(amount, category, date, comment)
        val selectedCategory = getSelectedCategory()
        if (selectedCategory != null) {
            loadIncomesByCategory(selectedCategory)
        }
        Toast.makeText(this, getString(R.string.removed), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}
