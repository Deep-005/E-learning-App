package com.example.courses.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.courses.Data.Category
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class CategoryAdapter(private val context: android.content.Context, private val categories: MutableList<Category>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val originalCategories = mutableListOf<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cat_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.cardTextView.text = category.name
        Glide.with(context).load(category.image).into(holder.cardImage)
        holder.cardView.setCardBackgroundColor(Color.parseColor(category.bg_color))
        holder.itemView.setOnClickListener {
            listener.onItemClick(category)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchCategories() {
        firestore.collection("Category").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val categoriesList = task.result.toObjects(Category::class.java)
                originalCategories.clear()
                originalCategories.addAll(categoriesList)
                categories.clear()
                categories.addAll(categoriesList)
                notifyDataSetChanged()
            } else {
                Log.d("Error", "Error fetching categories: ${task.exception}")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(text: String?) {
        categories.clear()
        if (text.isNullOrEmpty()) {
            categories.addAll(originalCategories)
        } else {
            val filteredCategories = originalCategories.filter { it.name.contains(text!!, ignoreCase = true) }
            categories.addAll(filteredCategories)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetFilter() {
        categories.clear()
        categories.addAll(originalCategories)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTextView: TextView = itemView.findViewById(R.id.cardTextView)
        val cardImage: ImageView = itemView.findViewById(R.id.cardImage)
        val cardView: CardView = itemView.findViewById(R.id.card1)
    }

    interface OnItemClickListener {
        fun onItemClick(category: Category)
    }
}