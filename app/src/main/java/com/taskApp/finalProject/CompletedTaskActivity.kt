package com.taskApp.finalProject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_completed_task.*

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

        val goToTask = findViewById<Button>(R.id.returnToTasks)
        goToTask.setOnClickListener{
            finish()
        }

        val goToFeed = findViewById<Button>(R.id.goToFeed)
        goToFeed.setOnClickListener{
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }


    }

    //this function loads all of the active tasks for the logged in user
    fun loadCompletedTasksData() {
        completedTasks_rv.adapter = adapter
        completedTasks_rv.layoutManager = LinearLayoutManager(this)

        // Get data using addOnSuccessListener
        fireBaseDb.collection("CompletedTasks")
            .orderBy("timeEnd") //displays tasks by newest on top
            .get()
            .addOnSuccessListener { documents ->
                // The result (documents) contains all the records in db, each of them is a document
                for (document in documents) {
                    if(FirebaseAuth.getInstance().currentUser!!.uid == document.get("uid").toString()) //check if logged in user id matches the uid of task
                    {
                        val newTask = Task(
                            document.get("activity").toString(),
                            document.get("type").toString(),
                            document.get("key").toString().toInt(),
                            document.get("image").toString(),
                            document.get("uid").toString(),
                            document.get("description").toString(),
                            document.get("timeStart").toString(),
                            document.get("timeEnd").toString(),
                            document.get("dateEnd").toString(),
                            document.get("public").toString().toInt(),
                            document.get("name").toString()
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
