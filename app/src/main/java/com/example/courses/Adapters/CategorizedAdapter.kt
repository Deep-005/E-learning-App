package com.example.courses.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Data.AllCourses
import com.example.courses.R

class CategorizedAdapter(
    private val context: Context,
    val allCourses: MutableList<AllCourses>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CategorizedAdapter.ViewHolder>() {

    private var filteredallCourses = mutableListOf<AllCourses>()

    init {
        filteredallCourses = allCourses
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.all_courses_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = filteredallCourses[position]

        // Set the course details to the TextViews
        holder.nameCourseCategory.text = course.name
        holder.briefCourseCategory.text = course.brief
        holder.timeCourseCategory.text = course.time
        holder.priceCourseCategory.text = course.price

        // Load the image using Glide
        Glide.with(context)
            .load(course.image)
            .placeholder(R.drawable.live_class)
            .error(R.drawable.live_class)
            .into(holder.imageCourseCategory)

        // Set the onClickListener to handle the click event
        holder.itemView.setOnClickListener {
            onItemClick(course.name)  // Pass the course name to the activity via callback
        }
    }

    override fun getItemCount(): Int {
        return filteredallCourses.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameCourseCategory: TextView = itemView.findViewById(R.id.nameCourse)
        val briefCourseCategory: TextView = itemView.findViewById(R.id.briefCourse)
        val timeCourseCategory: TextView = itemView.findViewById(R.id.timeCourse)
        val priceCourseCategory: TextView = itemView.findViewById(R.id.priceCourse)
        val imageCourseCategory: ImageView = itemView.findViewById(R.id.imageCourse)
    }
}




