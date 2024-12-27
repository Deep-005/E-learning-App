package com.example.courses.Fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Adapters.CategoryAdapter
import com.example.courses.Data.Category
import com.example.courses.R

class MoreFragment : Fragment() {

    private lateinit var adapter: CategoryAdapter
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var heading: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)
        recyclerView = view.findViewById(R.id.categoryRV)
        searchView = view.findViewById(R.id.search_icon)
        heading = view.findViewById(R.id.head)

        // Setting up the adapter and recyclerView
        adapter = CategoryAdapter(requireContext(), mutableListOf(), object : CategoryAdapter.OnItemClickListener {
            override fun onItemClick(category: Category) {
                val bundle = Bundle()
                bundle.putString("categoryName", category.name)
                Log.d("category", bundle.toString())
                // Navigating with NavController and passing the bundle
                findNavController().navigate(R.id.action_moreFragment_to_categorizedFragment, bundle)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns

        adapter.fetchCategories()

        // SearchView setup
        searchView.isFocusable = true
        searchView.isFocusableInTouchMode = true
        searchView.requestFocus()
        searchView.isIconified = true

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })

        // Show all card views when search view is not in focus
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (::adapter.isInitialized) {
                    adapter.resetFilter()
                }
                heading.visibility = View.VISIBLE
                hideKeyboard()
            } else {
                heading.visibility = View.GONE
            }
        }

        // Back button setup
        val back = view.findViewById<ImageButton>(R.id.backToBack)
        back.setOnClickListener {
            findNavController().popBackStack()  // Use this for simple back navigation
        }

        return view
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }
}


