package com.hfad.financehelper.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlin.math.round

data class Income(val amount: Double, val category: String, val date: String, val comment: String)
data class Expense(val amount: Double, val category: String, val date: String, val comment: String)

class MyDbManager(context: Context) {
    private val dbHelper = MyDbHelper(context)
    private var db: SQLiteDatabase? = null

    fun openDb() {
        db = dbHelper.writableDatabase
    }

    fun insertToDb(isIncome: Boolean, amount: Double, category: String, date: String, comment: String) {
        val tableName = if (isIncome) MyDbNameClass.TABLE_NAME_INCOMES else MyDbNameClass.TABLE_NAME_EXPENSES
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_NAME_AMOUNT, amount)
            put(MyDbNameClass.COLUMN_NAME_CATEGORY, category)
            put(MyDbNameClass.COLUMN_NAME_DATE, date)
            put(MyDbNameClass.COLUMN_NAME_COMMENT, comment)
        }
        db?.insert(tableName, null, values)
    }

    fun readIncomes(): List<Income> {
        val incomes = mutableListOf<Income>()
        val cursor = db?.query(MyDbNameClass.TABLE_NAME_INCOMES, null, null, null, null, null, null)
        with(cursor) {
            while (this?.moveToNext() == true) {
                val amount = getDouble(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_AMOUNT))
                val category = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_CATEGORY))
                val date = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_DATE))
                val comment = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_COMMENT))
                incomes.add(Income(amount, category, date, comment))
            }
        }
        cursor?.close()
        return incomes
    }

    fun readExpenses(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val cursor = db?.query(MyDbNameClass.TABLE_NAME_EXPENSES, null, null, null, null, null, null)
        with(cursor) {
            while (this?.moveToNext() == true) {
                val amount = getDouble(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_AMOUNT))
                val category = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_CATEGORY))
                val date = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_DATE))
                val comment = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_COMMENT))
                expenses.add(Expense(amount, category, date, comment))
            }
        }
        cursor?.close()
        return expenses
    }

    fun getTotalIncomes(): Double {
        val cursor = db?.rawQuery("SELECT SUM(REPLACE(${MyDbNameClass.COLUMN_NAME_AMOUNT}, ',', '.')) FROM ${MyDbNameClass.TABLE_NAME_INCOMES}", null)
        var totalIncome = 0.0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalIncome = cursor.getDouble(0)
            }
            cursor.close()
        }
        return totalIncome
    }

    fun getTotalExpenses(): Double {
        val cursor = db?.rawQuery("SELECT SUM(REPLACE(${MyDbNameClass.COLUMN_NAME_AMOUNT}, ',', '.')) FROM ${MyDbNameClass.TABLE_NAME_EXPENSES}", null)
        var totalExpenses = 0.0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalExpenses = cursor.getDouble(0)
            }
            cursor.close()
        }
        return totalExpenses
    }

    fun deleteFromDbIncomes(amount: Double, category: String, date: String, comment: String) {
        val tableName = MyDbNameClass.TABLE_NAME_INCOMES
        db?.delete(
            tableName,
            "${MyDbNameClass.COLUMN_NAME_AMOUNT} = ? AND ${MyDbNameClass.COLUMN_NAME_CATEGORY} = ? AND ${MyDbNameClass.COLUMN_NAME_DATE} = ? AND ${MyDbNameClass.COLUMN_NAME_COMMENT} = ?",
            arrayOf(amount.toString(), category, date, comment)
        )
    }

    fun deleteFromDbExpenses(amount: Double, category: String, date: String, comment: String) {
        val tableName = MyDbNameClass.TABLE_NAME_EXPENSES
        db?.delete(
            tableName,
            "${MyDbNameClass.COLUMN_NAME_AMOUNT} = ? AND ${MyDbNameClass.COLUMN_NAME_CATEGORY} = ? AND ${MyDbNameClass.COLUMN_NAME_DATE} = ? AND ${MyDbNameClass.COLUMN_NAME_COMMENT} = ?",
            arrayOf(amount.toString(), category, date, comment)
        )
    }

    fun updateIncome(oldAmount: Double, oldCategory: String, oldDate: String, oldComment: String,
                     newAmount: Double, newCategory: String, newDate: String, newComment: String) {
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_NAME_AMOUNT, newAmount)
            put(MyDbNameClass.COLUMN_NAME_CATEGORY, newCategory)
            put(MyDbNameClass.COLUMN_NAME_DATE, newDate)
            put(MyDbNameClass.COLUMN_NAME_COMMENT, newComment)
        }
        db?.update(
            MyDbNameClass.TABLE_NAME_INCOMES,
            values,
            "${MyDbNameClass.COLUMN_NAME_AMOUNT} = ? AND ${MyDbNameClass.COLUMN_NAME_CATEGORY} = ? AND ${MyDbNameClass.COLUMN_NAME_DATE} = ? AND ${MyDbNameClass.COLUMN_NAME_COMMENT} = ?",
            arrayOf(oldAmount.toString(), oldCategory, oldDate, oldComment)
        )
    }


    fun updateExpense(oldAmount: Double, oldCategory: String, oldDate: String, oldComment: String,
                      newAmount: Double, newCategory: String, newDate: String, newComment: String) {
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_NAME_AMOUNT, newAmount)
            put(MyDbNameClass.COLUMN_NAME_CATEGORY, newCategory)
            put(MyDbNameClass.COLUMN_NAME_DATE, newDate)
            put(MyDbNameClass.COLUMN_NAME_COMMENT, newComment)
        }
        db?.update(
            MyDbNameClass.TABLE_NAME_EXPENSES,
            values,
            "${MyDbNameClass.COLUMN_NAME_AMOUNT} = ? AND ${MyDbNameClass.COLUMN_NAME_CATEGORY} = ? AND ${MyDbNameClass.COLUMN_NAME_DATE} = ? AND ${MyDbNameClass.COLUMN_NAME_COMMENT} = ?",
            arrayOf(oldAmount.toString(), oldCategory, oldDate, oldComment)
        )
    }

    fun readExpensesByDateRange(startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val query = """
        SELECT * FROM ${MyDbNameClass.TABLE_NAME_EXPENSES} 
        WHERE strftime('%Y-%m-%d', substr(${MyDbNameClass.COLUMN_NAME_DATE}, 7, 4) || '-' || substr(${MyDbNameClass.COLUMN_NAME_DATE}, 4, 2) || '-' || substr(${MyDbNameClass.COLUMN_NAME_DATE}, 1, 2)) 
        BETWEEN ? AND ?
    """
        val selectionArgs = arrayOf(
            "${startDate.substring(6, 10)}-${startDate.substring(3, 5)}-${startDate.substring(0, 2)}",
            "${endDate.substring(6, 10)}-${endDate.substring(3, 5)}-${endDate.substring(0, 2)}"
        )

        val cursor = db?.rawQuery(query, selectionArgs)

        with(cursor) {
            while (this?.moveToNext() == true) {
                val amount = getDouble(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_AMOUNT))
                val category = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_CATEGORY))
                val date = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_DATE))
                val comment = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_COMMENT))
                expenses.add(Expense(amount, category, date, comment))
            }
        }
        cursor?.close()
        return expenses
    }

    fun readIncomesByDateRange(startDate: String, endDate: String): List<Expense> {
        val  incomes = mutableListOf<Expense>()
        val query = """
        SELECT * FROM ${MyDbNameClass.TABLE_NAME_INCOMES} 
        WHERE strftime('%Y-%m-%d', substr(${MyDbNameClass.COLUMN_NAME_DATE}, 7, 4) || '-' || substr(${MyDbNameClass.COLUMN_NAME_DATE}, 4, 2) || '-' || substr(${MyDbNameClass.COLUMN_NAME_DATE}, 1, 2)) 
        BETWEEN ? AND ?
    """
        val selectionArgs = arrayOf(
            "${startDate.substring(6, 10)}-${startDate.substring(3, 5)}-${startDate.substring(0, 2)}",
            "${endDate.substring(6, 10)}-${endDate.substring(3, 5)}-${endDate.substring(0, 2)}"
        )

        val cursor = db?.rawQuery(query, selectionArgs)

        with(cursor) {
            while (this?.moveToNext() == true) {
                val amount = getDouble(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_AMOUNT))
                val category = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_CATEGORY))
                val date = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_DATE))
                val comment = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_COMMENT))
                incomes.add(Expense(amount, category, date, comment))
            }
        }
        cursor?.close()
        return incomes
    }

    fun readExpensesByCategory(category: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val cursor = db?.query(
            MyDbNameClass.TABLE_NAME_EXPENSES, null,
            "${MyDbNameClass.COLUMN_NAME_CATEGORY} = ?", arrayOf(category),
            null, null, null
        )
        with(cursor) {
            while (this?.moveToNext() == true) {
                val amount = getDouble(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_AMOUNT))
                val date = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_DATE))
                val comment = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_COMMENT))
                expenses.add(Expense(amount, category, date, comment))
            }
        }
        cursor?.close()
        return expenses
    }

    fun readIncomesByCategory(category: String): List<Income> {
        val incomes = mutableListOf<Income>()
        val cursor = db?.query(
            MyDbNameClass.TABLE_NAME_INCOMES, null,
            "${MyDbNameClass.COLUMN_NAME_CATEGORY} = ?", arrayOf(category),
            null, null, null
        )
        with(cursor) {
            while (this?.moveToNext() == true) {
                val amount = getDouble(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_AMOUNT))
                val date = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_DATE))
                val comment = getString(getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_COMMENT))
                incomes.add(Income(amount, category, date, comment))
            }
        }
        cursor?.close()
        return incomes
    }

    fun convertCurrency(fromCurrency: String, toCurrency: String) {
        val fromRate = getCurrencyRate(fromCurrency)
        val toRate = getCurrencyRate(toCurrency)

        // Пересчет доходов
        val incomes = readIncomes()
        incomes.forEach { income ->
            val newAmount = round(income.amount * fromRate / toRate)
            updateIncome(income.amount, income.category, income.date, income.comment, newAmount, income.category, income.date, income.comment)
        }

        // Пересчет расходов
        val expenses = readExpenses()
        expenses.forEach { expense ->
            val newAmount = round(expense.amount * fromRate / toRate)
            updateExpense(expense.amount, expense.category, expense.date, expense.comment, newAmount, expense.category, expense.date, expense.comment)
        }
    }

    private fun getCurrencyRate(currency: String): Double {
        return when (currency) {
            "USD" -> CurrencyRates.USD_TO_RUB
            "EUR" -> CurrencyRates.EUR_TO_RUB
            "RUB" -> CurrencyRates.RUB
            else -> CurrencyRates.RUB
        }
    }

    fun clearTables() {
        db?.execSQL("DELETE FROM ${MyDbNameClass.TABLE_NAME_INCOMES}")
        db?.execSQL("DELETE FROM ${MyDbNameClass.TABLE_NAME_EXPENSES}")
    }

    object CurrencyRates {
        const val USD_TO_RUB = 90.0
        const val EUR_TO_RUB = 100.0
        const val RUB = 1.0
    }

    fun closeDb() {
        dbHelper.close()
    }
}