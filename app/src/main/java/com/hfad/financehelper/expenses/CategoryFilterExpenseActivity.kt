package com.hfad.financehelper.expenses

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager

class CategoryFilterExpenseActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var radioGroupTransactionCategory: GridLayout
    private lateinit var filterButton: Button
    private lateinit var expenseList: LinearLayout
    private lateinit var editExpenseLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentCurrency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_filter_expense)

        dbManager = MyDbManager(this)
        dbManager.openDb()

        radioGroupTransactionCategory = findViewById(R.id.radioGroupTransactionCategory)
        filterButton = findViewById(R.id.filterButton)
        expenseList = findViewById(R.id.expenseList)
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        currentCurrency = sharedPreferences.getString("selected_currency", "RUB") ?: "RUB"

        for (i in 0 until radioGroupTransactionCategory.childCount) {
            val view = radioGroupTransactionCategory.getChildAt(i)
            if (view is RadioButton) {
                view.setOnClickListener {
                    clearRadioButtonSelection(view.id)
                    view.isChecked = true
                }
            }
        }

        filterButton.setOnClickListener {
            val category = getSelectedCategory()
            if (category != null) {
                loadExpensesByCategory(category)
            } else {
                Toast.makeText(this, getString(R.string.pleaseTypeCategory), Toast.LENGTH_SHORT).show()
            }
        }

        editExpenseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val category = getSelectedCategory()
                if (category != null) {
                    loadExpensesByCategory(category)
                }
            }
        }
    }

    private fun clearRadioButtonSelection(excludeId: Int) {
        for (i in 0 until radioGroupTransactionCategory.childCount) {
            val view = radioGroupTransactionCategory.getChildAt(i)
            if (view is RadioButton && view.id != excludeId) {
                view.isChecked = false
            }
        }
    }

    private fun getSelectedCategory(): String? {
        for (i in 0 until radioGroupTransactionCategory.childCount) {
            val view = radioGroupTransactionCategory.getChildAt(i)
            if (view is RadioButton && view.isChecked) {
                return view.text.toString()
            }
        }
        return null
    }

    private fun loadExpensesByCategory(category: String) {
        expenseList.removeAllViews()
        val expenses = dbManager.readExpensesByCategory(category)

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
                onClickDelete(expense.amount, expense.category, expense.date, expense.comment)
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
        dbManager.deleteFromDbExpenses(amount, category, date, comment)
        val currentCategory = getSelectedCategory()
        if (currentCategory != null) {
            loadExpensesByCategory(currentCategory)
        }
        Toast.makeText(this, getString(R.string.removed), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}
