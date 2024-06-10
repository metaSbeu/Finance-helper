package com.hfad.financehelper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hfad.financehelper.db.MyDbManager
import com.hfad.financehelper.expenses.AddExpenseActivity
import com.hfad.financehelper.expenses.CheckExpenseActivity
import com.hfad.financehelper.expenses.DateFilterExpensesActivity
import com.hfad.financehelper.incomes.AddIncomeActivity
import com.hfad.financehelper.incomes.CheckIncomeActivity
import com.hfad.financehelper.incomes.DateFilterIncomesActivity
import com.hfad.financehelper.settings.SettingsActivity
import java.text.DecimalFormat

class SecretActivity : AppCompatActivity() {

    private lateinit var dbManager: MyDbManager
    private lateinit var balanceTextView: TextView
    private var balance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        balanceTextView = findViewById(R.id.balanceTextView)
        dbManager = MyDbManager(this)
        dbManager.openDb()
        updateBalance()
    }

    private fun updateBalance() {
        val incomes = dbManager.getTotalIncomes()
        val expenses = dbManager.getTotalExpenses()

        balance = incomes - expenses

        // Use DecimalFormat to format the balance with two decimal places
        val formatter = DecimalFormat("#,##0.00")
        balanceTextView.text = getString(R.string.balanceMain, formatter.format(balance))
    }

    fun onClickIncome(view: View) {
        startActivity(Intent(this, AddIncomeActivity::class.java))
    }

    fun onClickCheckIncomes(view: View) {
        startActivity(Intent(this, CheckIncomeActivity::class.java))
    }

    fun onClickExpense(view: View) {
        startActivity(Intent(this, AddExpenseActivity::class.java))
    }

    fun onClickCheckExpenses(view: View) {
        startActivity(Intent(this, CheckExpenseActivity::class.java))
    }


    fun onClickFilterExpenses(view: View){
        startActivity(Intent(this, DateFilterExpensesActivity::class.java))
    }
    fun onClickFilterIncomes(view: View){
        startActivity(Intent(this, DateFilterIncomesActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        updateBalance()
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }

    fun onClickSettings(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onBackPressed() {}

}
