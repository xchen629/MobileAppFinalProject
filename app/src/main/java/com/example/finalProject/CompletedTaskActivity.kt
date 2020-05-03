package com.example.finalProject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_completed_task.*
import kotlinx.android.synthetic.main.activity_task.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CompletedTaskActivity : AppCompatActivity() {

    private val TAG = "CompletedTasksActivity"
    private lateinit var fireBaseDb: FirebaseFirestore

    // Define an array to store a list of users
    private val taskList = ArrayList<Task>()
    // specify a viewAdapter for the dataset
    val adapter = CompletedTasksAdapter(taskList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_task)
        fireBaseDb = FirebaseFirestore.getInstance()
        loadCompletedTasksData()
    }

    //this function loads all of the active tasks for the logged in user
    fun loadCompletedTasksData() {
        completedTasks_rv.adapter = adapter
        completedTasks_rv.layoutManager = LinearLayoutManager(this)

        // Get data using addOnSuccessListener
        fireBaseDb.collection("CompletedTasks")
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
