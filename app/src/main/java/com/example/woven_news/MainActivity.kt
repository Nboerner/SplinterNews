package com.example.woven_news

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.woven_news.adapter.PanelAdapter

class MainActivity : AppCompatActivity() {

    // A basic linear layout manager used to present the articles in an ordered fashion
    private lateinit var linearLayoutManager : LinearLayoutManager

    // adapter used by the RecyclerView to handle population of articles from HTTP request
    private lateinit var adapter : PanelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        // ensures the device is connected to the internet prior to HTTP requests
        if (checkInternet()) Toast.makeText(this, "Connection is Clear",
            Toast.LENGTH_SHORT).show() else {
                // The Device is not connected and application does nothing
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("No Internet Connection Detected.  " +
                            "Please connect to the network then restart the application.")
                    .setCancelable(false)
                    .setNeutralButton("Okay") { dialog, _ ->
                        dialog.cancel()
                    }
            val alert = dialogBuilder.create()
                alert.setTitle("No Internet Connection Detected")
                alert.show()
                return
        }
//            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()

        // initialize our linearLayoutManager
        linearLayoutManager = LinearLayoutManager(this)

        // TODO make on scroll listener for dynamic loading

        // initialize a viewModel class for the page
        val viewModel = MainViewModel()

        // create a recyclerview to prepare to present data
        val recyclerView = findViewById<RecyclerView>(R.id.newsList)

        // attach the recyclerview to custom class PanelAdapter to manage data
        adapter = PanelAdapter(this, viewModel.activeStories)
        recyclerView.adapter = adapter

        // recyclerView does not have to worry about resizing due to content
        recyclerView.setHasFixedSize(true)

        // connect the recyclerView with the linear layout manager
        recyclerView.layoutManager = linearLayoutManager

        // waits for changes to occur in our liveData object from ViewModel to signal UI update
        viewModel.storyData.observe(
            this,
            {
                adapter.update(viewModel.activeStories)
            },
        )




        // begins HTTP requests to grab data from ViewModel class
        viewModel.init()

        findViewById<ImageButton>(R.id.recentButton).setOnClickListener {
            if (viewModel.activeStories == viewModel.bestStories) {
                Toast.makeText(this, "Switching to Recent Stories",
                    Toast.LENGTH_SHORT).show()
            }
            viewModel.updateView(true)
            findViewById<TextView>(R.id.storyType).text = getString(R.string.recent)
        }

        findViewById<ImageButton>(R.id.bestButton).setOnClickListener {
            if (viewModel.activeStories == viewModel.recentStories) {
                Toast.makeText(this, "Switching to Best Stories",
                    Toast.LENGTH_SHORT).show()
            }
            viewModel.updateView(false)
            findViewById<TextView>(R.id.storyType).text = getString(R.string.best)
        }

    }


    /**
     * Checks if the device is currently connected to an active internet connection
     * @return whether or not the device has an active and valid connection
    */
    private fun checkInternet() : Boolean {
        val communicationManager : ConnectivityManager = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        // checks to see if the Android Version is modern or old (deprecated)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // a representation of the device's network object
            val networkListener = communicationManager.activeNetwork ?: return false

            // this will provide information about the method the device uses to connect
            val networkFunctions = communicationManager.getNetworkCapabilities(networkListener)
                ?: return false

            return when {
                // network is wifi
                networkFunctions.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // network is cell
                networkFunctions.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // if neither, return false
                else -> false
            }

        } else {
            // If device is an older Android Version, uses deprecated methods for compatibility
            @Suppress("DEPRECATION")
            val networkListener = communicationManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkListener.isConnected
        }
    }

}