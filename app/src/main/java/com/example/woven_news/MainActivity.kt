package com.example.woven_news

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
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

        // initialize our linearLayoutManager
        linearLayoutManager = LinearLayoutManager(this)

        // TODO make on scroll listener for dynamic loading

        // initialize a viewModel class for the page
        val viewModel = MainViewModel()

        // create a recyclerview to prepare to present data
        val recyclerView = findViewById<RecyclerView>(R.id.newsList)

        // attach the recyclerview to custom class PanelAdapter to manage data
        adapter = PanelAdapter(this, viewModel.stories)
        recyclerView.adapter = adapter

        // recyclerView does not have to worry about resizing due to content
        recyclerView.setHasFixedSize(true)

        // connect the recyclerView with the linear layout manager
        recyclerView.layoutManager = linearLayoutManager

        // waits for changes to occur in our liveData object from ViewModel to signal UI update
        viewModel.storyData.observe(
            this,
            Observer { examinedData ->
                adapter.update(viewModel.stories)
                Log.d("ugh", viewModel.storyData.toString())
                Log.d("ugh", viewModel.storyData.value.toString())
            },
        )


        // ensures the device is connected to the internet prior to HTTP requests
        if (checkInternet()) Toast.makeText(this, "Connection is Clear",
            Toast.LENGTH_SHORT).show() else
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()

        // begins HTTP requests to grab data from ViewModel class
        viewModel.init()
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