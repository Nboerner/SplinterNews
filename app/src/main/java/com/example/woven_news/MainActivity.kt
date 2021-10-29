package com.example.woven_news

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.woven_news.adapter.PanelAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager : LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linearLayoutManager = LinearLayoutManager(this)


        // initialize a viewModel class for the page
        val viewModel = MainViewModel()

        // create a recyclerview to prepare to present data
        val recyclerView = findViewById<RecyclerView>(R.id.newsList)
        // attach the recyclerview to custom class PanelAdapter to manage data
        recyclerView.adapter = PanelAdapter(this, viewModel.stories)
        // recyclerView does not have to worry about resizing due to content
        recyclerView.setHasFixedSize(true)
        //
        recyclerView.layoutManager = linearLayoutManager

        viewModel.storyData.observe(
            this,
            Observer { examinedData ->
//                recyclerView.adapter.update(viewModel.stories)
//                recyclerView.adapter?.notifyDataSetChanged()
                Log.d("ugh", viewModel.storyData.toString())
            },
        )


        // ensures the device is connected to the internet prior to HTTP requests
        if (checkInternet()) Toast.makeText(this, "Connection is Clear",
            Toast.LENGTH_SHORT).show() else
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()

        // begins HTTP requests to grab data
        viewModel.init()
    }

    /*
 Checks if the device is currently connected to an active internet connection
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