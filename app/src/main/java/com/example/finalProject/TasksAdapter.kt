package com.example.finalProject

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.ContentResolver
import android.os.Bundle
import androidx.core.app.ActivityCompat.startActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import io.opencensus.metrics.LongGauge
import kotlinx.android.synthetic.main.activity_button_and_photo.*
import kotlinx.android.synthetic.main.row_item.view.*
import java.io.IOException
import java.security.AccessController.getContext
import java.util.*
import kotlin.collections.ArrayList


class TasksAdapter(private val tasks: ArrayList<Task>) : RecyclerView.Adapter<TasksAdapter.MyViewHolder>(){
    private var fireBaseDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    var playSound : MediaPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val currentItem = tasks[position]
        holder.activityName.text = currentItem.activity

    }

    override fun getItemCount(): Int {
        // Return the size of your dataset (invoked by the layout manager)
        return tasks.size
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    inner class MyViewHolder (itemView: View): RecyclerView.ViewHolder (itemView) {
        // This class will represent a single row in our recyclerView list
        // This class also allows caching views and reuse them
        // Each MyViewHolder object keeps a reference to 3 view items in our row_item.xml file
        val activityName = itemView.tv_name

        // Set onClickListener to show a toast message for the selected row item in the list
        init {
            /*itemView.setOnClickListener{
                val selectedItem = adapterPosition
                Toast.makeText(itemView.context, "You clicked on $selectedItem",
                    Toast.LENGTH_SHORT).show()
            }
            */
            // Set onLongClickListener to show a toast message and remove the selected row item from the list
            itemView.setOnLongClickListener {
                val selectedItem = adapterPosition //index of selected item
                showDeleteOrSaveDialog(itemView,selectedItem)

                return@setOnLongClickListener true
            }
        }


        fun showDeleteOrSaveDialog(view: View, selectedItem:Int){
            val selectedItemValue = tasks[selectedItem].activity
            //val mDialogView = LayoutInflater.from(view.context).inflate(R.layout.customDialog, null)

            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("You Selected: $selectedItemValue")
            builder.setMessage("What would you like to do with this task?")
            // Set an icon, optional
            //builder.setIcon(android.R.drawable.ic_delete)

            // Set the button actions
            builder.setPositiveButton("DELETE") { dialog, which ->
                deleteTask(tasks[selectedItem].key) //delete from database
                tasks.removeAt(selectedItem)  //remove from the recycler view
                notifyItemRemoved(selectedItem) //update recycler
                Toast.makeText(
                    itemView.context, "Deleting Task: $selectedItemValue",
                    Toast.LENGTH_SHORT
                ).show()
            }

            builder.setNegativeButton("COMPLETE TASK") { dialog, which ->
                saveCompletedTask(tasks[selectedItem])
                showPhotoOptionsDialog(itemView) //ask user if they want to take pic, upload pic, or just use default image
                deleteTask(tasks[selectedItem].key) //delete from database
                tasks.removeAt(selectedItem)  //remove from the recycler view
                notifyItemRemoved(selectedItem) //update recycler
                Toast.makeText(
                    itemView.context, "Task Completed: $selectedItemValue",
                    Toast.LENGTH_SHORT
                ).show()
                //play sound from media player
                playSound(itemView)
            }

            builder.setNeutralButton("Cancel") { dialog, which ->
            }

            // create the dialog and show it
            val dialog = builder.create()
            dialog.show()
        }

    }

    fun deleteTask(key: Int) {
        Log.d("adapter", key.toString())
        // To delete the contact based on key, we first execute a query to get a reference to
        // document to be deleted, then loop over matching documents and finally delete each
        // document based on its reference
        fireBaseDb.collection("Tasks")
            .whereEqualTo("key", key)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    if (document != null) {
                        Log.d("adapter", "${document.id} => ${document.data}")
                        // delete the document
                        document.reference.delete()
                        // Assuming there is only one document we want to delete so break the loop
                        break
                    } else {
                        Log.d("adapter", "No such document")
                    }
                }
            }
    }

    fun saveCompletedTask(task:Task){
        // Get an instance of our collection
        val savedCTasks = fireBaseDb.collection("CompletedTasks")

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
        val id = savedCTasks.document().id

        // Add data
        savedCTasks.document(id).set(newTask)
    }

    //function that plays the sound
    private fun playSound(view: View) {
        if (playSound == null){
            playSound = MediaPlayer.create(view.context, R.raw.song)
        }
        playSound?.start()
    }


    private fun showPhotoOptionsDialog(view: View) {
        // Create an alertdialog builder object,
        // then set attributes that you want the dialog to have
        val builder = androidx.appcompat.app.AlertDialog.Builder(view.context)
        builder.setTitle("What would you like to do?")
        builder.setMessage("Choose an option below to complete with the task!")

        // Open the camera if user choose this option
        builder.setPositiveButton("Picture from Camera"){ dialog, which ->
            //openCamera()
        }
        //Open up the gallery if user choose this option
        builder.setNegativeButton("Upload from gallery"){dialog, which ->
            //chooseImage()
        }
        //Play the sound if user choose this button
        builder.setNeutralButton("No picture"){dialog, which ->
            playSound(view)
        }
        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }
}
