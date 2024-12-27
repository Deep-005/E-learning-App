package com.example.courses.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Data.LiveClasses
import com.example.courses.R
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class LiveClassAdapter(
    private val context: Context,
    private val liveClasses: List<LiveClasses>,
    private val onLinkClick: (String) -> Unit
) : RecyclerView.Adapter<LiveClassAdapter.LiveClassViewHolder>() {

    inner class LiveClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.live_course)
        val dateTime: TextView = view.findViewById(R.id.date_time)
        val instructor: TextView = view.findViewById(R.id.live_tutor)
        val link: TextView = view.findViewById(R.id.link)
        val imageView: ImageView = view.findViewById(R.id.liveImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveClassViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.live_cards, parent, false)
        return LiveClassViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LiveClassViewHolder, position: Int) {
        val liveClass = liveClasses[position]
        holder.courseName.text = liveClass.courseName
        holder.dateTime.text = "${liveClass.date} | ${liveClass.time}"
        holder.instructor.text = liveClass.instructor

        // Check if the link is empty
        if (liveClass.link.isNullOrEmpty()) {
            holder.link.text = "Link will be provided right before the time of the live class to start."
            holder.link.setTextColor(ContextCompat.getColor(context, R.color.black))
            holder.link.setOnClickListener(null) // Disable click listener
        } else {
            holder.link.text = liveClass.link
            holder.link.setOnClickListener {
                handleLinkClick(liveClass)
            }
        }

        // Load image using Glide or Picasso
        Glide.with(holder.imageView.context)
            .load(liveClass.image)
            .into(holder.imageView)
    }


    override fun getItemCount(): Int = liveClasses.size

    @SuppressLint("SimpleDateFormat")
    private fun handleLinkClick(liveClass: LiveClasses) {
        try {
            // Parse the date and time from the liveClass object
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val liveDateTime = sdf.parse("${liveClass.date} ${liveClass.time}")

            // Get the current time
            val currentDateTime = Date()

            if (liveDateTime != null && liveDateTime <= currentDateTime) {
                // If live session time has arrived or passed, open the link
                onLinkClick(liveClass.link)
            } else if (liveDateTime != null) {
                // If live session time is in the future, calculate the difference
                val difference = liveDateTime.time - currentDateTime.time
                val hours = difference / (1000 * 60 * 60)
                val minutes = (difference % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (difference % (1000 * 60)) / 1000

                // Show toast message with remaining time
                Toast.makeText(
                    context,
                    "Time remaining: ${hours}h ${minutes}m ${seconds}s",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error parsing date or time", Toast.LENGTH_SHORT).show()
        }
    }
}

