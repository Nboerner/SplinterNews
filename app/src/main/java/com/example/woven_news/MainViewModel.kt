package com.example.woven_news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<List<String>>()
    val storyData : LiveData<List<String>>
        get() = _data

    // init function to launch the coroutines to not block main / UI thread
    fun init() {
        scope.launch {
            getStories()
        }
        populateList()
    }

    // the scope in which the coroutines will run
    val scope = CoroutineScope(Job())

    var stories = emptyList<String>()

//        var panels

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
        connection.disconnect()

    }

//        suspend fun getNewStories() {
//        }

    fun populateList() {
        _data.value = stories

    }
}