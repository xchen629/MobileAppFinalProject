package com.example.finalProject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.content_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class taskActivity  : AppCompatActivity() {

    private val BASE_URL = "https://www.boredapi.com/api/activity/"
    private val TAG = "TaskActivity"
    private lateinit var fireBaseDb: FirebaseFirestore

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
        fireBaseDb = FirebaseFirestore.getInstance()
        loadUserData()
    }


    fun addTask(view:View){
        task_recycler_view.adapter = adapter
        task_recycler_view.layoutManager = LinearLayoutManager(this)
        val category = categorySpinner.selectedItem.toString().toLowerCase()

        if(category == "random"){
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
                    addTaskToDatabase(body)
                    adapter.notifyDataSetChanged()
                }
            })
        }
        else{
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
                    addTaskToDatabase(body)
                    adapter.notifyDataSetChanged()
                }
            })
        }
    }

    fun launchCompletedTaskActivity(view: View) {
        // Launch the second activity
        val intent = Intent(this, CompletedTaskActivity::class.java)
        Log.d(TAG, "Launching the third activity")
        startActivity(intent)
    }

    // Add  a record to db
    fun addTaskToDatabase(task: Task) {
        // Get an instance of our collection
        val savedTasks = fireBaseDb.collection("Tasks")

        // Custom class is used to represent your document
        val newTask = Task(
            task.activity,
            task.accessibility,
            task.type,
            task.participants,
            task.price,
            task.link,
            task.key,
            " ",
            FirebaseAuth.getInstance().currentUser!!.uid
        )

        // Get an auto generated id for a document that you want to insert
        val id = savedTasks.document().id

        // Add data
        savedTasks.document(id).set(newTask)
    }

    //this function loads all of the active tasks for the logged in user
    fun loadUserData() {
        task_recycler_view.adapter = adapter
        task_recycler_view.layoutManager = LinearLayoutManager(this)

        // Get data using addOnSuccessListener
        fireBaseDb.collection("Tasks")
            //.orderBy("id") //use this later to order the tasks by new to old
            //.orderBy("id", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // The result (documents) contains all the records in db, each of them is a document
                for (document in documents) {
                    if(FirebaseAuth.getInstance().currentUser!!.uid == document.get("uid").toString()) //check if logged in user id matches the uid of task
                    {
                        val newTask = Task(
                            document.get("activity").toString(),
                            document.get("accessibility").toString(),
                            document.get("type").toString(),
                            document.get("participants").toString(),
                            document.get("price").toString(),
                            document.get("link").toString(),
                            document.get("key").toString().toInt(),
                            document.get("image").toString(),
                            document.get("uid").toString()
                        )
                        // Update the adapter with the new data
                        taskList.add(0,newTask)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }
}
