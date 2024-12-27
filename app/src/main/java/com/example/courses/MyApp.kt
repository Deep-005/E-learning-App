package com.example.courses

import android.app.Application
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import com.example.courses.Activities.DashActivity
import com.example.courses.Activities.OfflineActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApp : Application() {

    var isConnected: Boolean = true // Tracks network state
    var firebaseAnalytics: FirebaseAnalytics? = null
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Initialize App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Register the Network Callback
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager?.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Internet is available, return to the previous activity if needed
                isConnected = true
                Intent(this@MyApp, DashActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(it)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Internet is lost, move to OfflineActivity
                isConnected = false
                Intent(this@MyApp, OfflineActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(it)
                }
            }
        })
    }
}

