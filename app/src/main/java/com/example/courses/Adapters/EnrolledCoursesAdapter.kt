package com.example.courses.Adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Activities.NotesAndVideoActivity
import com.example.courses.Data.AllCourses
import com.example.courses.R
import java.util.Locale

class EnrolledCoursesAdapter(private val allCourses: List<AllCourses>, private val courseNames: List<String>) : RecyclerView.Adapter<EnrolledCoursesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_courses_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = allCourses.find { it.name.lowercase(Locale.ROOT) == courseNames[position].lowercase(Locale.ROOT) }
        if (course != null) {
            holder.bind(course)
        }
    }

    override fun getItemCount(): Int {
        return courseNames.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.imageCourse)
        private val name: TextView = itemView.findViewById(R.id.nameCourse)
        private val brief: TextView = itemView.findViewById(R.id.briefCourse)
        private val time: TextView = itemView.findViewById(R.id.timeCourse)

        @SuppressLint("DiscouragedApi")
        fun bind(course: AllCourses) {
            Glide.with(itemView.context)
                .load(course.image)
                .into(image)
            name.text = course.name
            brief.text = course.brief
            time.text = course.time

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, NotesAndVideoActivity::class.java)
                intent.putExtra("course_name", courseNames[position])
                itemView.context.startActivity(intent)
            }
        }
    }
}