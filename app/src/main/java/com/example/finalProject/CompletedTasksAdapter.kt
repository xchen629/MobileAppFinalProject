package com.example.finalProject

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_item.view.*

class CompletedTasksAdapter(private val tasks: ArrayList<Task>) : RecyclerView.Adapter<CompletedTasksAdapter.MyViewHolder>() {
    private var fireBaseDb: FirebaseFirestore = FirebaseFirestore.getInstance()

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
        Picasso.get().load(currentItem.image.toString()).into(holder.imagename)
        holder.category.text = currentItem.type
        holder.completeDate.text = currentItem.timeEnd
    }

    override fun getItemCount(): Int {
        // Return the size of your dataset (invoked by the layout manager)
        return tasks.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // This class will represent a single row in our recyclerView list
        // This class also allows caching views and reuse them
        // Each MyViewHolder object keeps a reference to 3 view items in our row_item.xml file
        val activityName = itemView.tv_name
        val imagename = itemView.image_profile
        val category = itemView.tv_category
        val completeDate = itemView.tv_date

        // Set onClickListener to show a toast message for the selected row item in the list
        init {
            itemView.setOnClickListener {
                val selectedItem = adapterPosition
                showDescriptionDialog(itemView,selectedItem)
            }

            // Set onLongClickListener to show a toast message and remove the selected row item from the list
            itemView.setOnLongClickListener {
                val selectedItem = adapterPosition //index of selected item
                showDeleteDialog(itemView, selectedItem)
                return@setOnLongClickListener true
            }
        }
    }

    fun showDeleteDialog(view: View, selectedItem:Int){
        val selectedItemValue = tasks[selectedItem].activity
        //val mDialogView = LayoutInflater.from(view.context).inflate(R.layout.customDialog, null)

        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("You Selected: $selectedItemValue")
        builder.setMessage("Are you sure you want to delete this completed task?")
        // Set an icon, optional
        builder.setIcon(android.R.drawable.ic_delete)

        // Set the button actions
        builder.setNegativeButton("DELETE") { dialog, which ->
            deleteTask(tasks[selectedItem].key) //delete from database
            tasks.removeAt(selectedItem)  //remove from the recycler view
            notifyItemRemoved(selectedItem) //update recycler
            Toast.makeText(
                view.context, "Deleting Task: $selectedItemValue",
                Toast.LENGTH_SHORT
            ).show()
        }

        builder.setNeutralButton("Cancel") { dialog, which ->
        }

        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    fun showDescriptionDialog(view: View, selectedItem:Int) {
        val taskDescription = tasks[selectedItem].description
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("Description: ")
        builder.setMessage(taskDescription)
        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }



    fun deleteTask(key: Int) {
        Log.d("adapter", key.toString())
        // To delete the contact based on key, we first execute a query to get a reference to
        // document to be deleted, then loop over matching documents and finally delete each
        // document based on its reference
        fireBaseDb.collection("CompletedTasks")
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

}