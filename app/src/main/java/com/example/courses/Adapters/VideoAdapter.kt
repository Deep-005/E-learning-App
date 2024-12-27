package com.example.courses.Adapters

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.courses.Activities.PlayVideoActivity
import com.example.courses.Data.Videos
import com.example.courses.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class VideoAdapter(
    private val context: Context,
    private val videoList: MutableList<Videos>,
    private val courseName: String,
    private val onAverageProgressCallback: OnAverageProgressCallback
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val videoProgressPercentages: MutableMap<Int, Int> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.videos_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videoList[position]
        holder.bind(video, position)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    private fun getItemAtPosition(position: Int): Videos {
        return videoList[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoName: TextView = itemView.findViewById(R.id.nameVideo)
        private val downloadVideo: CircleImageView = itemView.findViewById(R.id.download_video)
        private val videoProgressNumber: TextView = itemView.findViewById(R.id.videoProgressNumber)
        private val videoProgress: CircularProgressIndicator = itemView.findViewById(R.id.videoProgress)

        @SuppressLint("SetTextI18n")
        fun bind(video: Videos, position: Int) {
            val videoNameText = "video.${position + 1}"
            videoName.text = videoNameText
            itemView.setOnClickListener {
                val videoUrl = video.video
                val intent = Intent(context, PlayVideoActivity::class.java)
                intent.putExtra("videoLink", videoUrl)
                intent.putExtra("videoName", videoNameText)
                intent.putExtra("courseName", courseName)

                val sharedPreferences = context.getSharedPreferences("video_playback", Context.MODE_PRIVATE)
                val key = "${courseName}_${videoNameText}"
                var videoPlaybackPercentage = sharedPreferences.getFloat(key, 0f)
                if (videoPlaybackPercentage >= 100f) {
                    videoPlaybackPercentage = 100f
                }
                intent.putExtra("videoPlaybackPercentage", videoPlaybackPercentage)

                context.startActivity(intent)
            }

            downloadVideo.setOnClickListener {
                downloadVideo(video.video, context)
            }

            val sharedPreferences = context.getSharedPreferences("video_playback", Context.MODE_PRIVATE)
            val key = "${courseName}_${videoNameText}"
            val videoPlaybackPercentage = sharedPreferences.getFloat(key, 0f)
            videoProgressNumber.text = "${videoPlaybackPercentage.toInt()}%"
            videoProgress.setProgressCompat(videoPlaybackPercentage.toInt(), true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchVideos(courseName: String) {
        db.collection("AllCourses").whereEqualTo("name", courseName).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val videoUrls = document["videos"] as? List<String> ?: emptyList()

                    videoList.clear()
                    videoUrls.forEachIndexed { index, videoUrl ->
                        val video = Videos(video = videoUrl)
                        videoList.add(video)
                    }
                    notifyDataSetChanged()

                    storeVideoProgressFromPreferences(courseName)
                } else {
                    Log.e("VideoAdapter", "Course not found: $courseName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("VideoAdapter", "Error fetching videos", exception)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchVideosInternship(internshipName: String) {
        db.collection("InternShips").whereEqualTo("name", internshipName).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val videoUrls = document["videos"] as? List<String> ?: emptyList()

                    videoList.clear()
                    videoUrls.forEachIndexed { index, videoUrl ->
                        val video = Videos(video = videoUrl)
                        videoList.add(video)
                    }
                    notifyDataSetChanged()

                    storeVideoProgressFromPreferences(internshipName)
                } else {
                    Log.e("VideoAdapter", "Module not found: $internshipName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("VideoAdapter", "Error fetching videos", exception)
            }
    }

    private fun downloadVideo(videoUrl: String, context: Context) {
        val videoName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1)
        val request = DownloadManager.Request(Uri.parse(videoUrl))
            .setTitle(videoName)
            .setDescription("Downloading $videoName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun storeVideoProgressFromPreferences(courseName: String) {
        val sharedPreferences = context.getSharedPreferences("video_playback", Context.MODE_PRIVATE)
        val videoCount = itemCount
        var totalProgressSum = 0f
        for (i in 0 until videoCount) {
            val videoItem = getItemAtPosition(i)
            val videoNameText = "video.${i + 1}"
            val key = "${courseName}_${videoNameText}"
            val videoPlaybackPercentage = sharedPreferences.getFloat(key, 0f)
            totalProgressSum += videoPlaybackPercentage
            videoProgressPercentages[i] = videoPlaybackPercentage.toInt()
        }

        val averageProgress = totalProgressSum / videoCount
        onAverageProgressCallback.onAverageProgress(averageProgress)
    }

    interface OnAverageProgressCallback {
        fun onAverageProgress(averageProgress: Float)
    }
}
