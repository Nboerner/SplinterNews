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
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

//    private var storyIds : List<String> by mutableStateOf(ListOf())

    private val _data = MutableLiveData<List<Article>>()
    // create a tangible job to interact with coroutine functions if necessary
    private val viewModelJob = Job()

    // the scope in which the coroutines will run to make HTTP requests
    val scope = CoroutineScope(viewModelJob + Dispatchers.IO)

    // list of ID's used to make HTTP requests to acquire article information
    var storyIDs = mutableListOf<String>()

    // a list of articles to be shown on the main activity
    var stories = mutableListOf<Article>()

    // The live data object used to detect changes in content to update the RecyclerView
    val storyData : LiveData<List<Article>>
        get() = _data

    // init function to launch the coroutines to not block main / UI thread
    fun init() {
        scope.launch {
            getStories()
            populateStoryList()
        }
    }

    fun loadMore() {
        scope.launch {
            loadArticles()
        }
    }

    /**
     * Generates and executes HTTP requests to HackerNews API and populates information structures
     *
     */
    private suspend fun getStories() {

        val topStories = "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty"


        val url = URL(topStories)
        val connection : HttpURLConnection = url.openConnection() as HttpURLConnection


        // make the GET request here
        connection.connect()

        // receive response here & decipher into readable Array<String>

        var parsedData : String = connection.inputStream.bufferedReader().use {it.readText()}
            .trim('[').trim()
        storyIDs = parsedData.split(", ").toMutableList()

        // trim() or removeSurrounding() doesn't work for final val, have to truncate manually (?)
        storyIDs[storyIDs.size - 1] = storyIDs[storyIDs.size - 1].take(
            storyIDs[storyIDs.size - 1].length-2
        )

        Log.d("Tag", storyIDs[0])
        Log.d("Tag", storyIDs[storyIDs.size - 1])
        Log.d("MainActivity","We got there")
        connection.disconnect()

        loadArticles()

    }

    /**
     * Loads 25 articles into active stories list
     */
    private suspend fun loadArticles() {
        val prefix = "https://hacker-news.firebaseio.com/v0/item/"
        val suffix = ".json?print=pretty"

        var storyURL : URL
        var storyInfo : String
        var storyConnection : HttpURLConnection

        // load 25 articles
        var i = 0
        while (i < 25) {


            // base case
            if (storyIDs.isEmpty()) {
                Log.d("Debug", "No More Stories to Load")
                return
            }

            storyURL = URL(prefix + storyIDs[i] + suffix)
            storyIDs.removeAt(i)
            Log.d("URL", storyURL.toString())
            storyConnection = storyURL.openConnection() as HttpURLConnection

            storyConnection.connect()
            storyInfo = storyConnection.inputStream.bufferedReader().use {it.readText()}
            Log.d("Debug ugh", storyInfo)
            var storyJSON = JSONObject()
            try {
                storyJSON = JSONObject(storyInfo.substring(
                    storyInfo.indexOf("{"), storyInfo.lastIndexOf("}") + 1
                ))
            } catch (e : Exception) {
                Log.d("Exception", e.toString())
                continue
            }

            storyConnection.disconnect()

            try {
                storyJSON.get("url").toString()

                val newArticle = makeArticle(storyJSON)

                stories.add(newArticle)

                Log.d("Article", newArticle.toString())

                i++

            } catch (e : Exception) {
                Log.d("Exception", e.toString())
            }



        }

    }

    private suspend fun populateStoryList() {
        Log.d("Proc", stories.toString())
        _data.postValue(stories)

    }


    /**
     * Parses through JSON data of items returned from API to turn into Article model objects
     *
     * @param data passed in json data
     * @return Article returned article generated from json data
     */
    private suspend fun makeArticle (json : JSONObject) : Article {
        Log.d("debug", json.toString())
        val title = json.get("title").toString()
        val rating = json.get("score").toString()
        val URL = json.get("url").toString()
        val time = json.get("time").toString()

        return Article(title, rating, URL, time)
    }


}