package com.example.courses.Adapters

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Activities.ViewPDFActivity
import com.example.courses.Data.Notes
import com.example.courses.R
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class NotesAdapter(private val context: Context, private val notesList: MutableList<Notes>) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notes = notesList[position]
        holder.bind(notes, position)
        holder.itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notesName: TextView = itemView.findViewById(R.id.name_PDF)
        private val downloadPdf: CircleImageView = itemView.findViewById(R.id.download_PDF)

        @SuppressLint("SetTextI18n")
        fun bind(notes: Notes, position: Int) {
            notesName.text = "pdf.${position + 1}"

            // Set the click listener to open ViewPDFActivity with the PDF URL
            itemView.setOnClickListener {
                val pdfUrl = notes.pdf
                val intent = Intent(context, ViewPDFActivity::class.java)
                intent.putExtra("pdf_url", pdfUrl)
                intent.putExtra("pdf_name", notesName.text)
                context.startActivity(intent)
            }

            downloadPdf.setOnClickListener {
                downloadPdf(notes.pdf, context)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchNotes(courseName: String) {
        db.collection("AllCourses").whereEqualTo("name", courseName).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val pdfUrls = document["pdf"] as? List<String> ?: emptyList()

                    notesList.clear()
                    pdfUrls.forEachIndexed { index, pdfUrl ->
                        val note = Notes(name = "PDF ${index + 1}", pdf = pdfUrl)
                        notesList.add(note)
                    }
                    notifyDataSetChanged()
                    Log.d("NotesAdapter", "Notes list size: ${notesList.size}")
                } else {
                    Log.e("NotesAdapter", "Course not found: $courseName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NotesAdapter", "Error fetching notes", exception)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchNotesInternship(internshipName: String) {
        db.collection("InternShips").whereEqualTo("name", internshipName).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val pdfUrls = document["pdf"] as? List<String> ?: emptyList()

                    notesList.clear()
                    pdfUrls.forEachIndexed { index, pdfUrl ->
                        val note = Notes(name = "PDF ${index + 1}", pdf = pdfUrl)
                        notesList.add(note)
                    }
                    notifyDataSetChanged()
                    Log.d("NotesAdapter", "Notes list size: ${notesList.size}")
                } else {
                    Log.e("NotesAdapter", "Module not found: $internshipName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NotesAdapter", "Error fetching notes", exception)
            }
    }

    private fun downloadPdf(pdfUrl: String, context: Context) {
        val pdfName = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1)
        val request = DownloadManager.Request(Uri.parse(pdfUrl))
            .setTitle(pdfName)
            .setDescription("Downloading $pdfName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pdfName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}

