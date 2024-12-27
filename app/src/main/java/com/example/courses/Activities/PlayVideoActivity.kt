package com.example.courses.Activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.courses.Adapters.VideoAdapter
import com.example.courses.R
import com.example.courses.databinding.ActivityPlayVideoBinding

class PlayVideoActivity : AppCompatActivity(), MediaPlayer.OnBufferingUpdateListener, View.OnClickListener {

    private lateinit var binding: ActivityPlayVideoBinding
    private lateinit var videoView: VideoView
    private lateinit var seekBar: SeekBar
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var playPauseButton: ImageView
    private lateinit var backButton: ImageView

    private lateinit var videoUrl: String
    private lateinit var videoName: String
    private var videoPlayedPercentage = 0f
    private var isPlaying = false
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isShowing = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the animation view
        binding.loadingAnimation.visibility = View.GONE

        videoView = binding.videoView
        seekBar = binding.seekbar
        playPauseButton = binding.playPauseBtn
        backButton = binding.backToNotesAndVideos

        Handler(Looper.getMainLooper()).postDelayed({
            hideViews(binding.topBar, seekBar, playPauseButton)
        }, 3000)
        // Show the views when the screen is touched
        binding.videoView.setOnTouchListener { _, _ ->
            if (!isShowing) {
                showViews(binding.topBar, seekBar, playPauseButton)
                Handler(Looper.getMainLooper()).postDelayed({
                    hideViews(binding.topBar, seekBar, playPauseButton)
                }, 3000)
            }
            true
        }

        // Set up the video's URI
        videoUrl = intent.getStringExtra("videoLink").toString()
        videoName = intent.getStringExtra("videoName").toString()
        binding.NowPlaying.text = videoName
        // Set up the video source
        videoView.setVideoURI(Uri.parse(videoUrl))

        // Set up the video preparation listener
        videoView.setOnPreparedListener {
            mediaPlayer = it

            // Show the loading animation
            binding.loadingAnimation.post {
                binding.loadingAnimation.visibility = View.VISIBLE
            }

            // Set up the seek bar's max value
            seekBar.max = it.duration

            // Get the video playback percentage from SharedPreferences
            val sharedPreferences = getSharedPreferences("video_playback", MODE_PRIVATE)
            val courseName = intent.getStringExtra("courseName").toString()
            val key = "${courseName}_${videoName}"
            val videoPlaybackPercentage = sharedPreferences.getFloat(key, 0f)
            var seekPosition = (it.duration * videoPlaybackPercentage) / 100

            // Fix the progress number to 100 if it's greater than 100
            if (videoPlaybackPercentage >= 100f) {
                seekPosition = 0F
            }

            it.seekTo(seekPosition.toInt())
            seekBar.progress = seekPosition.toInt()

            // Start the video playback
            it.start()
            isPlaying = true

            // Delay hiding the loading animation
            Handler().postDelayed({
                binding.loadingAnimation.visibility = View.GONE
            }, 1500)

            // Update the seek bar progress periodically
            handler = Handler()
            runnable = object : Runnable {
                override fun run() {
                    if (isPlaying) {
                        seekBar.progress = videoView.currentPosition
                        handler?.postDelayed(this, 1000)
                    }
                }
            }
            handler?.postDelayed(runnable!!, 1000)
        }

        // Set up the seek bar's progress
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                if (p2) {
                    videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        // Update the video played percentage
        videoView.setOnCompletionListener {
            videoPlayedPercentage = 100f
            Handler().postDelayed({
                playPauseButton.setImageResource(R.drawable.play_btn)
                recordVideoPlayback(videoName, 100f) // Set the progress to 100% when the video is completed
                seekBar.progress = 0 // Reset the seekbar progress to 0
                videoView.seekTo(0) // Seek the video to the beginning
                finish()
            }, 2000) // delay for 2 second
        }

        // Set up the audio manager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        // Set up the play/pause button
        playPauseButton.setOnClickListener(this)
        // Set up the back button
        backButton.setOnClickListener(this)
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        val videoPlaybackPercentage = percent.toFloat()
        recordVideoPlayback(videoName, videoPlaybackPercentage)

        if (videoPlaybackPercentage < 100f) { // Only record the progress if it's less than 100%
            recordVideoPlayback(videoName, videoPlaybackPercentage)
            seekBar.progress = (videoView.duration * videoPlaybackPercentage / 100).toInt() // Update the seekbar progress
        }

        binding.loadingAnimation.visibility = View.VISIBLE
        Handler().postDelayed({
            if (percent == 100) {
                binding.loadingAnimation.visibility = View.GONE
            }
        }, 1000)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.playPauseBtn -> {
                if (isPlaying) {
                    mediaPlayer.pause()
                    playPauseButton.setImageResource(R.drawable.play_btn)
                    isPlaying = false
                    handler?.removeCallbacks(runnable!!)
                } else {
                    mediaPlayer.start()
                    playPauseButton.setImageResource(R.drawable.pause_btn)
                    isPlaying = true
                    handler?.postDelayed(runnable!!, 1000)
                }
            }
            R.id.backToNotesAndVideos -> {
                // Handle back button click
                val videoPlaybackPercentage = (videoView.currentPosition / videoView.duration.toFloat()) * 100
                recordVideoPlayback(videoName, videoPlaybackPercentage)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(runnable!!)
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        super.onBackPressed()
        val videoPlaybackPercentage = (videoView.currentPosition / videoView.duration.toFloat()) * 100
        recordVideoPlayback(videoName, videoPlaybackPercentage)
        finish()
    }

    private fun recordVideoPlayback(videoName: String, videoPlaybackPercentage: Float) {
        val sharedPreferences = getSharedPreferences("video_playback", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val courseName = intent.getStringExtra("courseName").toString()
        val key = "${courseName}_${videoName}"
        // Check if the progress number is already 100
        if (sharedPreferences.getFloat(key, 0f) < 100f) {
            editor.putFloat(key, videoPlaybackPercentage)
        }

        editor.apply()
    }

    private fun hideViews(vararg views: View) {
        views.forEach { view ->
            when (view.id) {
                R.id.topBar -> {
                    ObjectAnimator.ofFloat(view, "translationY", 0f, -view.height.toFloat()).apply {
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                    view.postDelayed({
                        view.visibility = View.GONE
                    }, 500)
                }
                R.id.seekbar -> {
                    ObjectAnimator.ofFloat(view, "translationY", 0f, view.height.toFloat()).apply {
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                    view.postDelayed({
                        view.visibility = View.GONE
                    }, 500)
                }
                else -> {
                    view.visibility = View.GONE
                }
            }
        }
        isShowing = false
    }

    private fun showViews(vararg views: View) {
        views.forEach { view ->
            when (view.id) {
                R.id.topBar -> {
                    view.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(view, "translationY", -view.height.toFloat(), 0f).apply {
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                }
                R.id.seekbar -> {
                    view.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat(), 0f).apply {
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                }
                else -> {
                    view.visibility = View.VISIBLE
                }
            }
        }
        isShowing = true
    }

}