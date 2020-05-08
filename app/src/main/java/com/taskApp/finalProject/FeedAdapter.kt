package com.taskApp.finalProject

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.row_item.view.*

class FeedAdapter(private val tasks: ArrayList<Task>) : RecyclerView.Adapter<FeedAdapter.MyViewHolder>() {
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

        Glide.with(holder.itemView.context).load(currentItem.image.toString()).apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(20))).into(holder.imagename)
        holder.name.text = (currentItem.name.toString())
        holder.completeDate.text = currentItem.dateEnd

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
        val name = itemView.tv_category
        val completeDate = itemView.tv_date

        // Set onClickListener to show a toast message for the selected row item in the list
        init {
           itemView.setOnClickListener {
                val selectedItem = adapterPosition
                showDescriptionDialog(itemView,selectedItem)
            }
        }
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

}