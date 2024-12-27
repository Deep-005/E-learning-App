package com.example.courses.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Data.Internship
import com.example.courses.R

class DashInternshipAdapter(
    private val internshipList: List<Internship>,
) : RecyclerView.Adapter<DashInternshipAdapter.InternshipViewHolder>() {

    private lateinit var listener: OnItemClickListener

    class InternshipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCourse: ImageView = view.findViewById(R.id.imageInternship)
        val nameCourse: TextView = view.findViewById(R.id.nameInternship)
        val briefCourse: TextView = view.findViewById(R.id.briefInternship)
        val timeCourse: TextView = view.findViewById(R.id.timeInternship)
        val priceCourse: TextView = view.findViewById(R.id.priceInternship)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternshipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.internship_dash_card, parent, false)
        return InternshipViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: InternshipViewHolder, position: Int) {
        val internship = internshipList[position]

        holder.nameCourse.text = internship.name
        holder.briefCourse.text = internship.brief
        holder.timeCourse.text = internship.time
        holder.priceCourse.text = internship.price

        // Use Glide or similar library to load image
        Glide.with(holder.itemView.context)
            .load(internship.image)
            .placeholder(R.drawable.live_class) // Replace with your placeholder image
            .into(holder.imageCourse)

        // Set click listener on the item view
        holder.itemView.setOnClickListener {
            listener.onItemClick(internship) // Use the listener to handle the click
        }
    }

    override fun getItemCount(): Int = internshipList.size

    interface OnItemClickListener {
        fun onItemClick(course: Internship)
    }

}


