package com.example.woven_news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

//    private var storyIds : List<String> by mutableStateOf(ListOf())

    private val _data = MutableLiveData<List<String>>()
    // create a tangible job to interact with coroutine functions if necessary
    private val viewModelJob = Job()

    // the scope in which the coroutines will run
    val scope = CoroutineScope(viewModelJob + Dispatchers.IO)

    var stories = emptyList<String>()

    val storyData : LiveData<List<String>>
        get() = _data

    // init function to launch the coroutines to not block main / UI thread
    fun init() {
        scope.launch {
            getStories()
            populateList()
        }
    }

//        var panels

    private suspend fun getStories() {
        val topStories = "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty"
        val url = URL(topStories)
        val json : InputStream
        val connection : HttpURLConnection = url.openConnection() as HttpURLConnection

        // make the GET request here
        connection.connect()
        // receive response here
        json = connection.inputStream // ?
        var parsedData : String = json.bufferedReader().use {it.readText()}
        parsedData = parsedData.trim('[').trim()
        stories = parsedData.split(", ")
        Log.d("Tag", stories[0])
        Log.d("MainActivity","We got there")
        connection.disconnect()

    }

//        suspend fun getNewStories() {
//        }

    private fun populateList() {
        Log.d("Proc", stories.toString())
        _data.postValue(stories)

    }

}