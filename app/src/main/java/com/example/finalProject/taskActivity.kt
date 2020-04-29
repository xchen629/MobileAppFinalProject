package com.example.finalProject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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

    // Define an array to store a list of users
    private val taskList = ArrayList<Task>()

    // specify a viewAdapter for the dataset
    val adapter = TasksAdapter(taskList)
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val randomTaskAPI = retrofit.create(RandomTaskService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
    }

    fun addRandTask(view:View){
        task_recycler_view.adapter = adapter
        task_recycler_view.layoutManager = LinearLayoutManager(this)

        // Using enqueue method allows to make asynchronous call without blocking/freezing main thread
        randomTaskAPI.getTask().enqueue(object : Callback<Task>{
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

                // Update the adapter with the new data
                taskList.add(0,body)
                adapter.notifyDataSetChanged()
            }
        })
    }

    fun addTypeTask(view:View){
        task_recycler_view.adapter = adapter
        task_recycler_view.layoutManager = LinearLayoutManager(this)
        val category = categorySpinner.selectedItem.toString().toLowerCase()

        // Using enqueue method allows to make asynchronous call without blocking/freezing main thread
        randomTaskAPI.getTaskOfType(category).enqueue(object : Callback<Task>{
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

                // Update the adapter with the new data
                taskList.add(0,body)
                adapter.notifyDataSetChanged()
            }
        })
    }


}
