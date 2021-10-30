package com.example.woven_news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.woven_news.model.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

//    private var storyIds : List<String> by mutableStateOf(ListOf())

    private val _data = MutableLiveData<List<String>>()
    // create a tangible job to interact with coroutine functions if necessary
    private val viewModelJob = Job()

    // the scope in which the coroutines will run to make HTTP requests
    val scope = CoroutineScope(viewModelJob + Dispatchers.IO)

    // list of ID's used to make HTTP requests to acquire article information
    var storyIDs = mutableListOf<String>()

    // a list of articles to be shown on the main activity
    var stories = mutableListOf<Article>()

    // The live data object used to detect changes in content to update the RecyclerView
    val storyData : LiveData<List<String>>
        get() = _data

    // init function to launch the coroutines to not block main / UI thread
    fun init() {
        scope.launch {
            getStories()
            populateIDList()
        }
    }

    /**
     * Generates and executes HTTP requests to HackerNews API and populates information structures
     *
     */
    private suspend fun getStories() {

        val topStories = "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty"
        val prefix = "https://hacker-news.firebaseio.com/v0/item/"
        val suffix = ".json?print=pretty"

        val url = URL(topStories)
        val json : InputStream
        val connection : HttpURLConnection = url.openConnection() as HttpURLConnection

        var storyURL : URL
        var storyInfo : String
        var storyConnection : HttpURLConnection


        // make the GET request here
        connection.connect()

        // receive response here

        var parsedData : String = connection.inputStream.bufferedReader().use {it.readText()}
        parsedData = parsedData.trim('[').trim()
        storyIDs = parsedData.split(", ").toMutableList()
        // trim() or removeSurrounding() doesn't work for final val, have to truncate manually (?)
        storyIDs[storyIDs.size - 1] = storyIDs[storyIDs.size - 1].take(
            storyIDs[storyIDs.size - 1].length-2
        )
        Log.d("Tag", storyIDs[0])
        Log.d("Tag", storyIDs[storyIDs.size - 1])
        Log.d("MainActivity","We got there")
        connection.disconnect()

        storyURL = URL(prefix + storyIDs[0] + suffix)
        storyConnection = storyURL.openConnection() as HttpURLConnection

        storyConnection.connect()
        storyInfo = storyConnection.inputStream.bufferedReader().use {it.readText()}
        var storyJSON = JSONObject(storyInfo.substring(
            storyInfo.indexOf("{"), storyInfo.lastIndexOf("}") + 1
        ))
//        val tex = dallas.getJSONArray("by")
        Log.d("general", storyJSON.toString())
        Log.d("specific", storyJSON.get("time").toString())

        Log.d("StoryData", storyInfo)

        storyConnection.disconnect()

        val newArticle = makeArticle(storyJSON)

        Log.d("Article", newArticle.toString())


    }


    suspend private fun populateIDList() {
        Log.d("Proc", storyIDs.toString())
        _data.postValue(storyIDs)

    }


    /**
     * Parses through JSON data of items returned from API to turn into Article model objects
     *
     * @param data passed in json data
     * @return Article returned article generated from json data
     */
    suspend private fun makeArticle (json : JSONObject) : Article {
        val title = json.get("title").toString()
        val rating = json.get("score").toString()
        val URL = json.get("url").toString()
        val time = json.get("time").toString()

        return Article(title, rating, URL, time)
    }


}