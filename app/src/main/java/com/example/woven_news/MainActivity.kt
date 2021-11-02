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

    // Adapter used by the RecyclerView to handle population of articles from HTTP request
    private lateinit var adapter : PanelAdapter

    // Listener used to detect when the bottom of a list has been reached by the user
    private lateinit var scrollListener : RecyclerView.OnScrollListener

    // Flag to indicate onScrollListener to stop sending toast messages
    private var internetFlag : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        // ensures the device is connected to the internet prior to HTTP requests
        if (checkInternet()) {
            Log.d("Network", "Internet connection is good")
            internetFlag = true
        } else {
                // The Device is not connected and application does nothing beyond this statement
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

        // initialize a viewModel class for the page
        val viewModel = MainViewModel()

        // initialize linearLayoutManager
        linearLayoutManager = LinearLayoutManager(this)

        // create a recyclerview to prepare to present data & attach to adapter to manage data
        val recyclerView = findViewById<RecyclerView>(R.id.newsList)
        adapter = PanelAdapter(viewModel.activeStories)
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
        // sets the listener to detect scrolling to the end of the recyclerView
        setRecyclerScrollListener(recyclerView, viewModel)



        Toast.makeText(this, "Loading News Articles from Hacker News...",
            Toast.LENGTH_LONG).show()

        // begins HTTP requests to grab data from ViewModel class
        viewModel.init()

        // set functionality for the "Recent" button on UI
        findViewById<ImageButton>(R.id.recentButton).setOnClickListener {
            if (viewModel.activeStories == viewModel.bestArticles) {
                Toast.makeText(this, "Switching to Recent Stories",
                    Toast.LENGTH_SHORT).show()
            }
            // requests view to show Recent Articles, but should not load new Articles on press
            viewModel.updateView(recent = true, buttonPress = true)
            findViewById<TextView>(R.id.storyType).text = getString(R.string.recent)
        }

        // set functionality for the "Best" button on UI
        findViewById<ImageButton>(R.id.bestButton).setOnClickListener {
            if (viewModel.activeStories == viewModel.recentArticles) {
                Toast.makeText(this, "Switching to Best Stories",
                    Toast.LENGTH_SHORT).show()
            }
            // requests view to show Best Articles, but should not load new Articles on press
            viewModel.updateView(recent = false, buttonPress = true)
            findViewById<TextView>(R.id.storyType).text = getString(R.string.best)
        }

    }


    /**
     * Creates a custom onScrollListener for the passed in RecyclerView object to load new Articles
     * once the RecyclerView has been scrolled to the bottom
     * @param recycler the RecyclerView to attach the listener to
     * @param viewModel the ViewModel responsible for updating the RecyclerView
     */
    private fun setRecyclerScrollListener(recycler : RecyclerView, viewModel : MainViewModel) {

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // check if active internet connection exists
                if (!checkInternet()) {
                    if(internetFlag) // notify user
                    Toast.makeText(applicationContext, "No Internet Connection Detected, " +
                            "Cannot Fetch New Articles",
                        Toast.LENGTH_LONG).show()
                    internetFlag = false // set to no internet
                    return

                }

                internetFlag = true

                // number of articles currently loaded into the view
                val totalArticles = recyclerView.layoutManager!!.itemCount
                if (totalArticles == linearLayoutManager.findLastVisibleItemPosition() + 1) {
                    // user has scrolled to the bottom of the view
                    Log.d("RecyclerView", "Loading new Content on scroll")
                    if (viewModel.activeStories == viewModel.recentArticles) {
                        Log.d("RecyclerView", "Loading new recent articles")
                        viewModel.updateView(recent = true, buttonPress = false)
                    } else {
                        Log.d("RecyclerView", "Loading new best articles")
                        viewModel.updateView(recent = false, buttonPress = false)
                    }
                    // temporarily removing listener prevents unnecessary calls during load
                    recyclerView.removeOnScrollListener(scrollListener)
                    // set up listener again
                    setRecyclerScrollListener(recyclerView, viewModel)
                }


            }
        }
        // assign listener to RecyclerView again
        recycler.addOnScrollListener(scrollListener)
//        Log.d("RecyclerView", "Setting new listener")

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