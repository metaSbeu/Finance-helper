package com.hfad.financehelper.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context) : SQLiteOpenHelper(context, MyDbNameClass.DATABASE_NAME, null, MyDbNameClass.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(MyDbNameClass.SQL_CREATE_ENTRIES_INCOMES)
        db.execSQL(MyDbNameClass.SQL_CREATE_ENTRIES_EXPENSES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(MyDbNameClass.SQL_DELETE_ENTRIES_INCOMES)
        db.execSQL(MyDbNameClass.SQL_DELETE_ENTRIES_EXPENSES)
        onCreate(db)
    }
}