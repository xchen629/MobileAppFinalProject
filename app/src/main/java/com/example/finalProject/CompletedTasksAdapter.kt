package com.example.finalProject

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.row_item.view.*

class CompletedTasksAdapter(private val tasks: ArrayList<Task>) : RecyclerView.Adapter<CompletedTasksAdapter.MyViewHolder>() {

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
        holder.img.setImageResource(R.drawable.ic_contacts_black_24dp)

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
        val img = itemView.image_profile

        // Set onClickListener to show a toast message for the selected row item in the list
        init {
            itemView.setOnClickListener{
                val selectedItem = adapterPosition
                Toast.makeText(itemView.context, "You clicked on $selectedItem",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}