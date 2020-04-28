package com.example.finalProject

import com.google.gson.annotations.SerializedName
/*
data class TaskData(
    val results: List<Task>
)
*/
data class Task(
    val activity: String,
    val accessibility: Double,
    val type: String,
    val participants: Int,
    val price: Double,
    val key: Int
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
