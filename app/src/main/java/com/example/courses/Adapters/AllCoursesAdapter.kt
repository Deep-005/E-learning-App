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

class AllCoursesAdapter(
    private val context: Context,
    private val allCoursesList: List<AllCourses>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AllCoursesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.all_courses_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, brief, price, image, time) = allCoursesList[position]
        holder.nameCourse.text = name
        holder.briefCourse.text = brief
        holder.timeCourse.text = time
        holder.priceCourse.text = price

        // Load the image using Glide or another image loading library
        Glide.with(context)
            .load(image)
            .into(holder.imageCourse)
    }

    override fun getItemCount(): Int {
        return allCoursesList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameCourse: TextView
        var briefCourse: TextView
        var timeCourse: TextView
        var priceCourse: TextView
        var imageCourse: ImageView

        init {
            nameCourse = itemView.findViewById(R.id.nameCourse)
            briefCourse = itemView.findViewById(R.id.briefCourse)
            timeCourse = itemView.findViewById(R.id.timeCourse)
            priceCourse = itemView.findViewById(R.id.priceCourse)
            imageCourse = itemView.findViewById(R.id.imageCourse)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(allCoursesList[position])
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(course: AllCourses)
    }

}