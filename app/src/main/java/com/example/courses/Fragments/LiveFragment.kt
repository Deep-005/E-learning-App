package com.example.courses.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Activities.LiveSessionActivity
import com.example.courses.Adapters.LiveClassAdapter
import com.example.courses.Data.LiveClasses
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore

class LiveFragment : Fragment() {

    private lateinit var liveClassAdapter: LiveClassAdapter
    private lateinit var recyclerView: RecyclerView
    private val liveClassList = mutableListOf<LiveClasses>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_live, container, false)
        recyclerView = view.findViewById(R.id.liveRV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with a click listener
        liveClassAdapter = LiveClassAdapter(requireContext(), liveClassList) { link ->
            // Open LiveSessionActivity with the link
            val intent = Intent(requireContext(), LiveSessionActivity::class.java)
            intent.putExtra("link", link)
            startActivity(intent)
        }
        recyclerView.adapter = liveClassAdapter

        fetchLiveClasses()

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchLiveClasses() {
        val db = FirebaseFirestore.getInstance()
        db.collection("LiveClass")
            .get()
            .addOnSuccessListener { result ->
                liveClassList.clear()
                for (document in result) {
                    val liveClass = document.toObject(LiveClasses::class.java)
                    liveClassList.add(liveClass)
                }

                if (liveClassList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.noLiveClasses)?.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    view?.findViewById<TextView>(R.id.noLiveClasses)?.visibility = View.GONE
                }

                liveClassAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("LiveFragment", "Error fetching live classes", exception)
                recyclerView.visibility = View.GONE
                view?.findViewById<TextView>(R.id.noLiveClasses)?.visibility = View.VISIBLE
            }
    }

}

