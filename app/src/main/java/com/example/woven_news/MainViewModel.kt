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
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<List<Article>>()
    // create a tangible job to interact with coroutine functions if necessary
    private val viewModelJob = Job()

    // the scope in which the coroutines will run to make HTTP requests
    private val scope = CoroutineScope(viewModelJob + Dispatchers.IO)

    // list of ID's used to make HTTP requests to acquire specific article information
    private var recentStoryIDs = mutableListOf<String>()
    private var bestStoryIDs = mutableListOf<String>()


    // a list of the most recent or highest rated articles to be shown on the main activity
    var recentStories = mutableListOf<Article>()
    var bestStories = mutableListOf<Article>()

    //
    var activeStories : MutableList<Article> = recentStories

    // The live data object used to detect changes in content to update the RecyclerView
    val storyData : LiveData<List<Article>>
        get() = _data

    /**
     * Init function to launch the coroutines to grab initial data (25 most recent articles)
     */
    fun init() {
        scope.launch {
            getStoryIDs(true)
            loadArticles(true)
            populateStoryList(recentStories)
        }
    }

    fun updateView(recent : Boolean) {
        val targetList : MutableList<Article>
        if (recent) {
            targetList = recentStories
            activeStories = recentStories
        } else {
            targetList = bestStories
            activeStories = bestStories
        }

        scope.launch {
            loadArticles(recent)
            populateStoryList(targetList)
        }
    }

    fun loadMore() {
        scope.launch {
//            loadArticles(recent)
        }
    }

    /**
     * Generates and executes HTTP requests to HackerNews API and populates information structures
     * @param recent Boolean to determine whether to pull recent article data vs best article data
     */
    private suspend fun getStoryIDs(recent : Boolean) {

        val newStoriesURL = "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty"

        val bestStoriesURL = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty"

        idRequest(newStoriesURL, true)
        idRequest(bestStoriesURL, false)

    }

    /**
     * Loads 25 articles into active stories list
     * @param recent Boolean value determining whether we are loading into recent or best stories
     */
    private suspend fun loadArticles(recent : Boolean) {
        val prefix = "https://hacker-news.firebaseio.com/v0/item/"
        val suffix = ".json?print=pretty"

        val storyIDs : MutableList<String>
        val articleList : MutableList<Article>

        if (recent) {
            storyIDs = recentStoryIDs
            articleList = recentStories
        } else {
            storyIDs = bestStoryIDs
            articleList = bestStories
            Log.d("ProcCheck", "Loading Articles for Best Stories")
        }

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

            storyURL = URL(prefix + storyIDs[0] + suffix)
            storyIDs.removeFirst()
            Log.d("URL", storyURL.toString())
            storyConnection = storyURL.openConnection() as HttpURLConnection

            storyConnection.connect()
            storyInfo = storyConnection.inputStream.bufferedReader().use {it.readText()}
            Log.d("Data", storyInfo)
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

                articleList.add(newArticle)

                Log.d("Article", newArticle.toString())

                i++

            } catch (e : Exception) {
                Log.d("Exception", e.toString())
            }

        }

    }

    /**
     * Updates the live data object with the passed in list of articles to notify adapter of update
     * @param stories list of articles to be displayed on the MainActivity
     */
    private suspend fun populateStoryList(stories : MutableList<Article>) {
        Log.d("BeforeMeteor", _data.toString())
        _data.postValue(stories)
        Log.d("AfterMeteor", _data.toString())

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

    /**
     * makes the initial HTTP request to receive either 500 best or recent story IDs
     * @param url target URL for API call to receive best or recent story IDs
     * @param recent whether or not we want best or recent story IDs
     */
    private suspend fun idRequest(url : String, recent : Boolean) {

        var idList : MutableList<String>

        val url = URL(url)

        val connection : HttpURLConnection = url.openConnection() as HttpURLConnection


        // make the GET request here
        connection.connect()

        // receive response here & decipher into readable Array<String>

        var parsedData : String = connection.inputStream.bufferedReader().use {it.readText()}
            .trim('[').trim()
        idList = parsedData.split(", ").toMutableList()

        // trim() or removeSurrounding() doesn't work for final val, have to truncate manually (?)
        idList[idList.size - 1] = idList[idList.size - 1].take(
            idList[idList.size - 1].length-2
        )

        Log.d("Tag", idList[0])
        Log.d("Tag", idList[idList.size - 1])
        if (recent) {
            recentStoryIDs = idList
        } else {
            bestStoryIDs = idList
        }
        Log.d("MainActivity","We got there")
        connection.disconnect()

    }


}