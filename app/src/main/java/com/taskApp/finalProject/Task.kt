package com.taskApp.finalProject

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
    var timeEnd: String,
    var dateEnd:String,
    var public: Int,
    var name: String?
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
