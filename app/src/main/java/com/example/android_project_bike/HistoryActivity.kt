package com.example.android_project_bike

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class HistoryActivity : AppCompatActivity() {

    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView = findViewById<ListView>(R.id.history_list)
        val listItems = listOf(
            "04.03.2020, 12:00-14:12",
            "05.03.2020, 21:33-23:45"
            )
        adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_list_item_1,
            listItems
        )
        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}
