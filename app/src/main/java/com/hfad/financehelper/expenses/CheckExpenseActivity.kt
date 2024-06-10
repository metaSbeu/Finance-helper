package com.hfad.financehelper.expenses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager

class CheckExpenseActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var expenseList: LinearLayout
    private lateinit var editExpenseLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentCurrency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbManager = MyDbManager(this)
        dbManager.openDb()

        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        currentCurrency = sharedPreferences.getString("selected_currency", "RUB") ?: "RUB"

        expenseList = findViewById(R.id.expenseList)
        loadExpenses()

        editExpenseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadExpenses()
            }
        }
    }

    private fun loadExpenses() {
        expenseList.removeAllViews()
        val expenses = dbManager.readExpenses()
        val inflater = LayoutInflater.from(this)

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

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16) // Отступ снизу между элементами
            itemView.layoutParams = params

            expenseList.addView(itemView)
        }
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }

    fun onClickFilterExpenses(view: View) {
        startActivity(Intent(this, DateFilterExpensesActivity::class.java))
    }

    fun onClickDelete(view: View) {
        val parent = view.parent.parent as? LinearLayout
        if (parent != null) {
            val expenseAmountView = parent.findViewById<TextView>(R.id.expenseAmount)
            val expenseCategoryView = parent.findViewById<TextView>(R.id.expenseCategory)
            val expenseDateView = parent.findViewById<TextView>(R.id.expenseDate)
            val expenseCommentView = parent.findViewById<TextView>(R.id.expenseComment)

            if (expenseAmountView != null && expenseCategoryView != null && expenseDateView != null && expenseCommentView != null) {
                val expenseAmount = expenseAmountView.text.toString().removePrefix(getString(R.string.deleteSum)).removeSuffix(" $currentCurrency").toDouble()
                val expenseCategory = expenseCategoryView.text.toString().removePrefix(getString(R.string.deleteCategory))
                val expenseDate = expenseDateView.text.toString().removePrefix(getString(R.string.deleteDate))
                val expenseComment = expenseCommentView.text.toString().removePrefix(getString(R.string.deleteComment))

                dbManager.deleteFromDbExpenses(expenseAmount, expenseCategory, expenseDate, expenseComment)
                expenseList.removeView(parent)
                Toast.makeText(this, getString(R.string.removed), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.cannotFindElement), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.cannotFindElement), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    fun onClickCategoryFilterExpenses(view: View) {
        startActivity(Intent(this, CategoryFilterExpenseActivity::class.java))
    }
}
