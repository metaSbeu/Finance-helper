package com.hfad.financehelper.incomes

import android.annotation.SuppressLint
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

class CheckIncomeActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager
    private lateinit var incomeList: LinearLayout
    private lateinit var editIncomeLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentCurrency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check_income)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbManager = MyDbManager(this)
        dbManager.openDb()

        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        currentCurrency = sharedPreferences.getString("selected_currency", "RUB") ?: "RUB"

        incomeList = findViewById(R.id.incomeList)
        loadIncomes()

        editIncomeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadIncomes()
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun loadIncomes() {
        incomeList.removeAllViews()
        val incomes = dbManager.readIncomes()
        val inflater = LayoutInflater.from(this)

        for (income in incomes) {
            val itemView = inflater.inflate(R.layout.transaction_item, incomeList, false)
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
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            itemView.layoutParams = params


            incomeList.addView(itemView)
        }
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }

    fun onClickFilterIncomes(view: View) {
        startActivity(Intent(this, DateFilterIncomesActivity::class.java))
    }

    fun onClickDelete(view: View) {
        val parent = view.parent.parent as? LinearLayout
        if (parent != null) {
            val incomeAmountView = parent.findViewById<TextView>(R.id.expenseAmount)
            val incomeCategoryView = parent.findViewById<TextView>(R.id.expenseCategory)
            val incomeDateView = parent.findViewById<TextView>(R.id.expenseDate)
            val incomeCommentView = parent.findViewById<TextView>(R.id.expenseComment)

            if (incomeAmountView != null && incomeCategoryView != null && incomeDateView != null && incomeCommentView != null) {
                val incomeAmount = incomeAmountView.text.toString().removePrefix(getString(R.string.deleteSum)).removeSuffix(" $currentCurrency").toDouble()
                val incomeCategory = incomeCategoryView.text.toString().removePrefix(getString(R.string.deleteCategory))
                val incomeDate = incomeDateView.text.toString().removePrefix(getString(R.string.deleteDate))
                val incomeComment = incomeCommentView.text.toString().removePrefix(getString(R.string.deleteComment))

                dbManager.deleteFromDbIncomes(incomeAmount, incomeCategory, incomeDate, incomeComment)
                incomeList.removeView(parent)
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
        loadIncomes()
    }

    fun onClickCategoryFilterIncomes(view: View) {
        startActivity(Intent(this, CategoryFilterIncomeActivity::class.java))
    }
}
