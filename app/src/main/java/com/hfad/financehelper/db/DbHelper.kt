package com.hfad.financehelper.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DbNameClass.DATABASE_NAME, null, DbNameClass.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DbNameClass.SQL_CREATE_ENTRIES_INCOMES)
        db.execSQL(DbNameClass.SQL_CREATE_ENTRIES_EXPENSES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DbNameClass.SQL_DELETE_ENTRIES_INCOMES)
        db.execSQL(DbNameClass.SQL_DELETE_ENTRIES_EXPENSES)
        onCreate(db)
    }
}