package com.hfad.financehelper.db

import android.provider.BaseColumns

object MyDbNameClass : BaseColumns {
    const val TABLE_NAME_INCOMES = "incomes"
    const val TABLE_NAME_EXPENSES = "expenses"
    const val COLUMN_NAME_AMOUNT = "amount"
    const val COLUMN_NAME_CATEGORY = "category"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_COMMENT = "comment"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "FinanceHelper.db"

    const val SQL_CREATE_ENTRIES_INCOMES = """
        CREATE TABLE $TABLE_NAME_INCOMES (
            ${BaseColumns._ID} INTEGER PRIMARY KEY,
            $COLUMN_NAME_AMOUNT TEXT,
            $COLUMN_NAME_CATEGORY TEXT,
            $COLUMN_NAME_DATE TEXT,
            $COLUMN_NAME_COMMENT TEXT
        )
    """

    const val SQL_CREATE_ENTRIES_EXPENSES = """
        CREATE TABLE $TABLE_NAME_EXPENSES (
            ${BaseColumns._ID} INTEGER PRIMARY KEY,
            $COLUMN_NAME_AMOUNT TEXT,
            $COLUMN_NAME_CATEGORY TEXT,
            $COLUMN_NAME_DATE TEXT,
            $COLUMN_NAME_COMMENT TEXT
        )
    """

    const val SQL_DELETE_ENTRIES_INCOMES = "DROP TABLE IF EXISTS $TABLE_NAME_INCOMES"
    const val SQL_DELETE_ENTRIES_EXPENSES = "DROP TABLE IF EXISTS $TABLE_NAME_EXPENSES"
}
