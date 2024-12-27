package com.example.courses.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Activities.NotesAndVideoActivity
import com.example.courses.Data.Internship
import com.example.courses.R
import java.util.Locale

class InternshipEnrolledAdapter(
    private val internships: List<Internship>,
    private val enrolledInternshipNames: List<String>
) : RecyclerView.Adapter<InternshipEnrolledAdapter.InternshipViewHolder>() {

    // ViewHolder class for the Internship
    class InternshipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCourse: ImageView = view.findViewById(R.id.imageCourse)
        val nameCourse: TextView = view.findViewById(R.id.nameCourse)
        val briefCourse: TextView = view.findViewById(R.id.briefCourse)
        val instructorCourse: TextView = view.findViewById(R.id.instructorCourse)
        val timeCourse: TextView = view.findViewById(R.id.timeCourse)
        val priceCourse: TextView = view.findViewById(R.id.priceCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternshipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.internship_card_layout, parent, false)
        return InternshipViewHolder(view)
    }

    override fun onBindViewHolder(holder: InternshipViewHolder, position: Int) {
        val internship = internships.find {
            it.name.lowercase(Locale.ROOT) == enrolledInternshipNames[position].lowercase(Locale.ROOT)
        }
        if (internship != null) {
            holder.bind(internship)
        }
    }

    override fun getItemCount(): Int {
        return enrolledInternshipNames.size
    }

    // Bind the internship data to the ViewHolder
    private fun InternshipViewHolder.bind(internship: Internship) {
        Glide.with(itemView.context)
            .load(internship.image)
            .into(imageCourse)

        nameCourse.text = internship.name
        briefCourse.text = internship.brief
        instructorCourse.text = internship.instructor
        timeCourse.text = internship.time
        priceCourse.text = internship.price

        // Handle item click event
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, NotesAndVideoActivity::class.java)
            intent.putExtra("internshipName", internship.name)
            itemView.context.startActivity(intent)
        }
    }
}
