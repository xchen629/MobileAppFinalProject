package com.example.finalProject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_task.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class taskActivity : AppCompatActivity() {

    private val BASE_URL = "https://www.boredapi.com/api/activity/"
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        // Define an array to store a list of users
        val taskList = ArrayList<Task>()

        // specify a viewAdapter for the dataset
        val adapter = TasksAdapter(taskList)
        task_recycler_view.adapter = adapter

        // use a linear layout manager
        task_recycler_view.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val randomTaskAPI = retrofit.create(RandomTaskService::class.java)

        // Using enqueue method allows to make asynchronous call without blocking/freezing main thread
        randomTaskAPI.getTaskType("recreational").enqueue(object : Callback<Task>{
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.d(TAG, "onFailure : $t")
            }

            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                Log.d(TAG, "onResponse: $response")
                Log.d(TAG, "onResponse: ${response.body()?.activity}")
                val body = response.body()

                if (body == null){
                    Log.w(TAG, "Valid response was not received")
                    return
                }

                //Log.d(TAG, ": ${body.results.get(0).activity}")

                // Update the adapter with the new data
                taskList.add(body)
                adapter.notifyDataSetChanged()
            }

        })

    }
}
