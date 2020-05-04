package com.example.finalProject

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.lang.ref.Reference

/*
data class TaskData(
    val results: List<Task>
)
*/
data class Task(
    val activity: String,
    val type: String,
    val key: Int,
    var image: String? = null,
    var uid: String? = null,
    var description: String,
    var timeStart: String,
    var timeEnd: String
)


// Copied and pasted a sample response below to make it easier to write data class above
/*
{
  "activity": "Create and follow a savings plan",
  "accessibility": 0.2,
  "type": "busywork",
  "participants": 1,
  "price": "",
  "link": "",
  "key": "9366464"
}
*/
