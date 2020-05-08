package com.taskApp.finalProject

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity() {
    private val TAG = "FeedActivity"
    private lateinit var fireBaseDb: FirebaseFirestore

    // Define an array to store a list of users
    private var taskList = ArrayList<Task>()
    // specify a viewAdapter for the dataset
    val adapter = FeedAdapter(taskList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        fireBaseDb = FirebaseFirestore.getInstance()
        loadPublicTasksData()

        // Setup refresh listener which triggers new data loading
        swipe_refresh_layout.setOnRefreshListener {
            // Update the adapter with new data.
            loadPublicTasksData()

            // hide the refreshing animation
            swipe_refresh_layout.isRefreshing = false
        }

        // set a different color for the refreshing animation -- Optional
        swipe_refresh_layout.setColorSchemeColors(Color.BLUE)


    }

    //this function loads all of the active tasks for the logged in user
    fun loadPublicTasksData(){
        taskList.clear() //make sure taskList is empty
        feed_rv.adapter = adapter
        feed_rv.layoutManager = LinearLayoutManager(this)

        // Get data using addOnSuccessListener
        fireBaseDb.collection("CompletedTasks")
            .orderBy("timeEnd") //displays tasks by newest on top
            .get()
            .addOnSuccessListener { documents ->
                // The result (documents) contains all the records in db, each of them is a document
                for (document in documents) {
                    if(document.get("public").toString() == "1") //check if logged in user id matches the uid of task
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


