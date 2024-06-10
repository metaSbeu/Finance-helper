package com.hfad.financehelper.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hfad.financehelper.R
import com.hfad.financehelper.db.MyDbManager
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var dbManager: MyDbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppLocale(this)
        setContentView(R.layout.settings_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val changePinButton = findViewById<TextView>(R.id.change_pin_button)
        val deleteDataButton = findViewById<TextView>(R.id.delete_data_button)
        val currencySpinner = findViewById<Spinner>(R.id.currency_spinner)
        val languageSpinner = findViewById<Spinner>(R.id.language_spinner)

        changePinButton.setOnClickListener {
            startActivity(Intent(this, ChangePinActivity::class.java))
        }

        deleteDataButton.setOnClickListener {
            startActivity(Intent(this, DeleteDataActivity::class.java))
        }

        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        val savedCurrency = sharedPreferences.getString("selected_currency", "")

        if (!savedCurrency.isNullOrEmpty()) {
            val currencyPosition = resources.getStringArray(R.array.currencies).indexOf(savedCurrency)
            if (currencyPosition >= 0) {
                currencySpinner.setSelection(currencyPosition)
            }
        }

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCurrency = parent.getItemAtPosition(position).toString()
                with(sharedPreferences.edit()) {
                    putString("selected_currency", selectedCurrency)
                    apply()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dbManager = MyDbManager(this)
        dbManager.openDb()

        setupCurrencySpinner()
        setupLanguageSpinner(languageSpinner, sharedPreferences)
    }

    private fun setupCurrencySpinner() {
        val currencySpinner: Spinner = findViewById(R.id.currency_spinner)
        val currencies = resources.getStringArray(R.array.currencies)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val savedCurrency = sharedPreferences.getString("selected_currency", "RUB")
        if (savedCurrency != null) {
            val currencyPosition = currencies.indexOf(savedCurrency)
            if (currencyPosition >= 0) {
                currencySpinner.setSelection(currencyPosition)
            }
        }

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCurrency = parent.getItemAtPosition(position).toString()
                val previousCurrency = sharedPreferences.getString("selected_currency", "RUB")

                if (selectedCurrency != previousCurrency) {
                    dbManager.convertCurrency(previousCurrency!!, selectedCurrency)
                    sharedPreferences.edit().putString("selected_currency", selectedCurrency).apply()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupLanguageSpinner(languageSpinner: Spinner, sharedPreferences: SharedPreferences) {
        val languages = resources.getStringArray(R.array.languages)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val savedLanguage = sharedPreferences.getString("selected_language", "Русский")
        if (savedLanguage != null) {
            val languagePosition = languages.indexOf(savedLanguage)
            if (languagePosition >= 0) {
                languageSpinner.setSelection(languagePosition)
            }
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = parent.getItemAtPosition(position).toString()
                val previousLanguage = sharedPreferences.getString("selected_language", "Русский")

                if (selectedLanguage != previousLanguage) {
                    changeLanguage(selectedLanguage)
                    sharedPreferences.edit().putString("selected_language", selectedLanguage).apply()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    fun changeLanguage(language: String) {
        val locale = when (language) {
            "English" -> Locale("en")
            "Deutsch" -> Locale("de")
            else -> Locale("ru")
        }
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selected_language", language)
            apply()
        }

        //Toast.makeText(this, "Чтобы изменения полностью вступили в силу требуется перезапуск", Toast.LENGTH_LONG).show()
        val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
        i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    fun setAppLocale(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("selected_language", "Русский")
        val locale = when (savedLanguage) {
            "English" -> Locale("en")
            "Deutsch" -> Locale("de")
            else -> Locale("ru")
        }
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    override fun onDestroy() {
        dbManager.closeDb()
        super.onDestroy()
    }
}
