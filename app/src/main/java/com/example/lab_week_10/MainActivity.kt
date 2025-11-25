package com.example.lab_week_10

import android.os.Bundle
import android.widget.Button
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import android.widget.Toast
import java.util.Date
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.lab_week_10.viewmodels.TotalViewModel
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[TotalViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Initialize the value of the total from the database
        initializeValueFromDatabase()
        prepareViewModel()
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text = getString(R.string.text_total, total)
    }

    private fun prepareViewModel(){
        // Observe the LiveData object
        viewModel.total.observe(this) {
            // Whenever the value of the LiveData object changes
            updateText(it)
        }

        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    // Create an instance of the TotalDatabase
    // by lazy is used to create the database only when it's needed
    private val db by lazy { prepareDatabase() }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        ).allowMainThreadQueries().build()
    }

    private fun initializeValueFromDatabase() {
        val total = db.totalDao().getTotal(ID)
        if (total.isEmpty()) {
            val now = Date().toString()
            db.totalDao().insert(Total(id = ID, totalObject = TotalObject(value = 0, date = now)))
            viewModel.setTotal(0)
        } else {
            viewModel.setTotal(total.first().totalObject.value)
        }
    }

    override fun onPause() {
        super.onPause()
        val now = Date().toString()
        db.totalDao().update(Total(ID, TotalObject(viewModel.total.value ?: 0, now)))
    }

    override fun onStart() {
        super.onStart()
        // Show the last saved date for the total value
        val total = db.totalDao().getTotal(ID)
        if (total.isNotEmpty()) {
            val lastDate = total.first().totalObject.date
            Toast.makeText(this, "Last updated: $lastDate", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // For simplicity, only one Total object is stored in the database
        const val ID: Long = 1
    }
}