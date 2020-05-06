package com.example.finalProject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.koushikdutta.ion.Ion
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.completeTaskBtn
import kotlinx.android.synthetic.main.content_main.categorySpinner
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    var currentTask = Task("","",0,null,"","","","",0)
    var emptyTask = Task("","",0,null,"","","","",0)
    private lateinit var fireBaseDb: FirebaseFirestore
    private val CAMERA_REQUEST = 1000
    private val PERMISSION_PICK_IMAGE = 1001
    internal var filePath: Uri? = null


    var imageID: String? = null
    //Dialog
    lateinit var dialog: AlertDialog

    //Firebase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    var playSound : MediaPlayer? = null

    //bored api for generating tasks
    private val BASE_URL = "https://www.boredapi.com/api/activity/"
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val randomTaskAPI = retrofit.create(RandomTaskService::class.java)

    //Cat pic api
    private val CAT_URL = "https://api.thecatapi.com/v1/images/search?mime_types=jpg,png"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // #### Authentication using FirebaseAuth #####
        // If currentUser is null, open the RegisterActivity
        if (FirebaseAuth.getInstance().currentUser == null){
            startRegisterActivity()
        }
        else{
            storage = FirebaseStorage.getInstance()
            storageReference = storage.reference
            dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build()

            val clickable_imageview = findViewById<ImageView>(R.id.image_view)

            findViewById<View>(R.id.contentLayout).setOnTouchListener { v, event ->
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                true
            }
            clickable_imageview.setOnClickListener{
                showImageDialog()
            }

            completeTaskBtn.setOnClickListener {
                if(currentTask == emptyTask){
                    createDialog("You don't have a task!","Press the button above to generate a new task.")
                }
                else{
                    if(currentTask.image == null || currentTask.image.equals("null")){
                        createDialog("Image Required","Tap on the image to choose a picture to go along with your completed task before uploading.")
                    }
                    else{
                        makePublicDialog()
                    }

                }
            }

            Thread{
                while(true){
                    Thread.sleep(1000)
                    if(currentTask.timeStart != ""){
                        displayElapsedTime()
                    }
                }
            }.start()
            setSupportActionBar(toolbar)

            // Get a Cloud Firestore instance
            fireBaseDb = FirebaseFirestore.getInstance()
            loadUserData()
            Log.d(TAG,FirebaseAuth.getInstance().currentUser!!.uid)//gets user id
        }
    }


    @SuppressLint("NewApi")
    fun displayElapsedTime(){
        val currentTimeMs = System.currentTimeMillis()

        Log.d("time",currentTimeMs.toString())

        val taskTimeMs = currentTask.timeStart.toLong()
        Log.d("time",taskTimeMs.toString())

        var elapsedTimeMs = (currentTimeMs - taskTimeMs)
        Log.d("time","elapsed: $elapsedTimeMs")

        //displays elapsed time in days, hours, minutes, seconds
        val test = String.format("%02d:%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toDays(elapsedTimeMs),
            TimeUnit.MILLISECONDS.toHours(elapsedTimeMs) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(elapsedTimeMs)),
            TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMs)- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeMs)),
            TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMs)))
        val elapsedTimeDisplay = "Elapsed Time: $test"

        time_tv.text = elapsedTimeDisplay
    }

    @SuppressLint("NewApi")
    fun addTask(view:View){
        deleteTask() //remove previous task

        val category = categorySpinner.selectedItem.toString().toLowerCase()
        if(category == "random"){
            randomTaskAPI.getTask().enqueue(object : Callback<Task> {
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

                    task_tv.text = body.activity
                    currentTask = body
                    currentTask.timeStart = System.currentTimeMillis().toString()
                    currentTask.uid = FirebaseAuth.getInstance().currentUser!!.uid
                    addTaskToDatabase(currentTask)
                    description_tv.text.clear()
                    image_view.setImageResource(R.drawable.add_image)
                }
            })
        }
        else{
            // Using enqueue method allows to make asynchronous call without blocking/freezing main thread
            randomTaskAPI.getTaskOfType(category).enqueue(object : Callback<Task> {
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

                    task_tv.text = body.activity
                    currentTask = body
                    currentTask.timeStart = System.currentTimeMillis().toString()
                    currentTask.uid = FirebaseAuth.getInstance().currentUser!!.uid
                    addTaskToDatabase(currentTask)
                    description_tv.text.clear()
                    image_view.setImageResource(R.drawable.add_image)
                }
            })
        }
    }

    fun deleteTask() {
        fireBaseDb.collection("Tasks")
            .whereEqualTo("uid",FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("activity",task_tv.text.toString())
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    if (document != null) {
                        // delete the document
                        document.reference.delete()

                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
            }
    }

    @SuppressLint("NewApi")
    fun saveCompletedTask(task:Task, uri: Uri){
        // Get an instance of our collection=
        val savedCTasks = fireBaseDb.collection("CompletedTasks")

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm MMMM dd, yyyy")
        val formatted = current.format(formatter)

        // Custom class is used to represent your document
        val newTask = Task(
            task.activity,
            task.type,
            task.key,
            uri.toString(),
            FirebaseAuth.getInstance().currentUser!!.uid,
            description_tv.text.toString(),
            task.timeStart,
            formatted,
            task.public
        )
        // Get an auto generated id for a document that you want to insert
        val id = savedCTasks.document().id

        // Add data
        savedCTasks.document(id).set(newTask)
        deleteTask()
        currentTask = emptyTask
        val newText = "Press button to get new task"
        task_tv.text = newText
        description_tv.text.clear()
        image_view.setImageResource(R.drawable.add_image)
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

        // Get an auto generated id for a document that you want to insert
        val id = savedTasks.document().id

        // Add data
        savedTasks.document(id).set(task)
    }

    //this function loads all of the active tasks for the logged in user
    fun loadUserData() {
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
                            document.get("type").toString(),
                            document.get("key").toString().toInt(),
                            document.get("image").toString(),
                            document.get("uid").toString(),
                            document.get("description").toString(),
                            document.get("timeStart").toString(),
                            document.get("timeEnd").toString(),
                            document.get("public").toString().toInt()
                        )
                        task_tv.text = newTask.activity
                        currentTask = newTask
                        displayElapsedTime()
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }

    // An helper function to start our RegisterActivity
    private fun startRegisterActivity(){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        // Make sure to call finish(), otherwise the user would be able to go back to the MainActivity
        finish()
    }

    //Adds item to the menu bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //THis logs user out/action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_logout -> {
                // User chose the "logout" item, logout the user then
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            // After logout, start the RegisterActivity again
                            startRegisterActivity()
                        }
                        else{
                            Log.e(TAG, "Task is not successful:${task.exception} ")
                        }
                    }
                true
            }
            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }
    }
    //Upload image to the firebase storage
    private fun uploadImage() {
        if (filePath != null)
        {
            Log.d("filepath",filePath.toString())
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            imageID = UUID.randomUUID().toString()
            val imageRef = storageReference!!.child("images/" + imageID)

            imageRef.putFile(filePath!!)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "File Uploaded", Toast.LENGTH_SHORT).show()
                    getImageUrl(imageRef)
                }
        }
    }

    //Choose image from the gallery
    private fun chooseImage() {
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object:
            MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), PERMISSION_PICK_IMAGE)
                }
                else{
                    Toast.makeText(this@MainActivity, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest()
            }
        }).check()
    }

    //Opens the camera
    private fun openCamera() {
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object:
            MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                    filePath = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                }
                else{
                    Toast.makeText(this@MainActivity, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest()
            }
        }).check()
    }


    //this sets the image to the image view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PERMISSION_PICK_IMAGE) {
                if (data != null) {
                    if (data.data != null) {
                        filePath = data.data
                        try {
                            val bitmap =
                                MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                            image_view.setImageBitmap(bitmap)
                            currentTask.image=filePath.toString()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                    image_view.setImageBitmap(bitmap)
                    currentTask.image=filePath.toString()
                }
                catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    //Shows dialog when use press on the imageview
    private fun showImageDialog() {
        // Create an alertdialog builder object,
        // then set attributes that you want the dialog to have
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("What would you like to do?")
        builder.setMessage("Choose an option below to complete with the task!")

        // Open the camera if user choose this option
        builder.setPositiveButton("Picture from Camera"){ dialog, which ->
            openCamera()
        }
        //Open up the gallery if user choose this option
        builder.setNegativeButton("Upload from gallery"){dialog, which ->
            chooseImage()
        }
        //Play the sound if user choose this button
        builder.setNeutralButton("Random Cat Picture"){dialog, which ->
            //set default pic here
            downloadCatImage()

        }
        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    fun downloadCatImage() {
        // Download the data from the specified URL
        Ion.with(this)
            .load(CAT_URL)
            .asString()
            .setCallback { e, result ->
                Log.d(TAG, "The received data : $result")
                // Helper function to parse/process data
                parseCatImageData(result)
            }
    }

    private fun parseCatImageData(result: String){
        // Extract the information from JSON data
        //[{"breeds":[],"categories":[{"id":1,"name":"hats"}],"id":"7fq","url":"https://cdn2.thecatapi.com/images/7fq.jpg","width":1024,"height":576}]
        val data = JSONObject(result.removePrefix("[").removeSuffix("]"))
        val img = data.getString("url")
        Log.d(TAG, "The received data : $img")
        currentTask.image = img
        filePath = null

        // Display the image using Ion library, alternatively picasso library can be used
        Ion.with(image_view)
            .load(img)
    }

    //function that plays the sound
    private fun playSound() {
        if (playSound == null){
            playSound = MediaPlayer.create(this, R.raw.song)
        }
        playSound?.start()
    }

    fun getImageUrl(imageRef: StorageReference){
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Log.d("uri", uri.toString())
            saveCompletedTask(currentTask, uri)
        }
    }

    fun createDialog(title:String, message:String){
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    fun makePublicDialog(){
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Would you like to make your task public?")
        builder.setMessage("This will display your task, picture, and description in a feed displayed to all users.")

        builder.setPositiveButton("Yes"){ dialog, which ->
            currentTask.public = 1
            taskComplete()
        }
        builder.setNegativeButton("No"){dialog, which ->
            currentTask.public = 0
            taskComplete()
        }

        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    fun taskComplete(){
        if (filePath != null){
            uploadImage() //camera or gallery
        }
        else{
            saveCompletedTask(currentTask, Uri.parse(currentTask.image)) //cat pic
        }
        playSound()
    }
}
