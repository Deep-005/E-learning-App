@file:Suppress("UNCHECKED_CAST")

package com.example.courses.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Data.AllCourses
import com.example.courses.R
import java.util.Locale

class CourseAdapter(private val context: Context, private val courses: MutableList<AllCourses>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>(), Filterable {

    private var filteredCourses: MutableList<AllCourses> = courses.toMutableList()
    private var courseFilter: CourseFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_courses_card, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = filteredCourses[position]
        holder.bindCourse(course)
        holder.itemView.setOnClickListener {
            listener.onItemClick(course)
        }
    }

    override fun getItemCount(): Int {
        return filteredCourses.size
    }

    override fun getFilter(): Filter {
        if (courseFilter == null) {
            courseFilter = CourseFilter(this, courses)
        }
        return courseFilter!!
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetFilter() {
        filteredCourses = courses.toMutableList()
        notifyDataSetChanged()
    }

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameCourse)
        private val briefTextView: TextView = itemView.findViewById(R.id.briefCourse)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceCourse)
        private val imageView: ImageView = itemView.findViewById(R.id.imageCourse)

        fun bindCourse(course: AllCourses) {
            nameTextView.text = course.name
            briefTextView.text = course.brief
            priceTextView.text = course.price

            Glide.with(context)
                .load(course.image)
                .placeholder(R.drawable.live_class)
                .into(imageView)
        }
    }

    private class CourseFilter(
        private val adapter: CourseAdapter,
        private val originalList: MutableList<AllCourses>
    ) : Filter() {

        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            val filteredList: MutableList<AllCourses> = mutableListOf()

            if (constraint.isBlank()) {
                filteredList.addAll(originalList)
            } else {
                for (course in originalList) {
                    if (course.name.lowercase(Locale.ROOT).contains(constraint.toString().lowercase(Locale.ROOT))) {
                        filteredList.add(course)
                    }
                }
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            adapter.filteredCourses.clear()
            adapter.filteredCourses.addAll(results.values as MutableList<AllCourses>)
            adapter.notifyDataSetChanged()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(course: AllCourses)
    }
}