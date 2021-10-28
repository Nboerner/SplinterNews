package com.example.woven_news

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = MainViewModel()
        viewModel.init()
        if (checkInternet()) Toast.makeText(this, "Connection is Clear",
            Toast.LENGTH_SHORT).show() else
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
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



    class MainViewModel : ViewModel() {

        // init function to launch the coroutines to not block main / UI thread
        fun init() {
            scope.launch {
                getStories()
                populateList()
            }
        }

        // the scope in which the coroutines will run
        val scope = CoroutineScope(Job())

        var stories = emptyList<String>()

        suspend fun getStories() {
            val topStories = "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty"
            val url : URL = URL(topStories)
            val json : InputStream
            val connection : HttpURLConnection = url.openConnection() as HttpURLConnection

            // make the GET request here
            connection.connect()
            // TODO Deal with Timeouts?
            // receive response here
            json = connection.inputStream // ?
            val parsedData : String = json.bufferedReader().use {it.readText()}
            stories = parsedData.split(", ")
            Log.d("Tag", stories[0].trim('[').trim())
            Log.d("MainActivity","We got there")

        }

//        suspend fun getNewStories() {
//            val newStories =
//        }

        suspend fun populateList() {

        }
    }
}