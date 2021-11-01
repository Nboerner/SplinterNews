package com.example.woven_news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.woven_news.model.Article
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

/**
 * ViewModel class for the Main Activity. Handles interactions between Main Activity view, HTTP
 * requests, and Model class data.  Responsible for completing HTTP requests in coroutines to not
 * block the main UI thread for the Main Activity and notifying the view when it needs to be
 * updated.
 */
@Suppress("BlockingMethodInNonBlockingContext")
class MainViewModel : ViewModel() {

    // Mutable Live Data that is used to monitor currently viewed Articles
    private val _data = MutableLiveData<List<Article>>()

    // create a tangible job to interact with coroutine functions if necessary
    private val viewModelJob = Job()

    // the scope in which the coroutines will run to make HTTP requests
    private val scope = CoroutineScope(viewModelJob + Dispatchers.IO)

    // list of ID's used to make HTTP requests to acquire specific article information
    private var recentStoryIDs = mutableListOf<String>()
    private var bestStoryIDs = mutableListOf<String>()


    // lists of the most recent and highest rated articles to be shown on the main activity
    var recentArticles = mutableListOf<Article>()
    var bestArticles = mutableListOf<Article>()

    // object that represents what the current active story category is that the user is viewing
    var activeStories : MutableList<Article> = recentArticles

    // The public live data object used to signal changes in content to update the RecyclerView
    val storyData : LiveData<List<Article>>
        get() = _data

    /**
     * Init function to launch the coroutines to grab initial data (25 most recent articles)
     * and populates the recycler view with the recent stories by default
     */
    fun init() {
        scope.launch {
            getStoryIDs()
            loadArticles(true) // load initial 25 most recent stories
            loadArticles(false) // load initial 25 best stories
            populateStoryList(recentArticles)
        }
    }

    /**
     * Changes the active Article list to be shown to the user.  If this call was made by scrolling
     * instead of a button press, it loads the next 25 Articles in the current active Article list
     * @param recent boolean value that determines if the current active Article list is "recent"
     * @param buttonPress boolean value that determines if the method was called by a button press
     */
    fun updateView(recent : Boolean, buttonPress : Boolean) {

        // the list of best/recent articles to be passed into populateStoryList() to update UI
        val targetList : MutableList<Article>

        if (recent) {
            targetList = recentArticles
            activeStories = recentArticles
        } else {
            targetList = bestArticles
            activeStories = bestArticles
        }

        scope.launch {
            if(!buttonPress) {
                loadArticles(recent)
            }
            populateStoryList(targetList)
        }
    }

    /**
     * Generates and executes HTTP requests to HackerNews API and populates information structures
     */
    private suspend fun getStoryIDs() {
        // strings representing URLS according to the public API
        val recentStoriesURL =
            "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty"
        val bestStoriesURL =
            "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty"

        // populate the lists of IDs for both recent stories and best stories via HTTP request
        idRequest(recentStoriesURL, true)
        idRequest(bestStoriesURL, false)

    }

    /**
     * Loads 25 articles into active stories list
     * @param recent Boolean value determining whether we are loading into recent or best stories
     */
    private suspend fun loadArticles(recent : Boolean) {
        // API URL components
        val prefix = "https://hacker-news.firebaseio.com/v0/item/"
        val suffix = ".json?print=pretty"

        // Lists of Articles and IDs used to load more Articles from HTTP request
        val storyIDs : MutableList<String>
        val articleList : MutableList<Article>

        if (recent) { // Loading Recent Articles
            storyIDs = recentStoryIDs
            articleList = recentArticles
            Log.d("ProcCheck", "Loading Articles for Recent Stories")
        } else { // Loading Best Articles
            storyIDs = bestStoryIDs
            articleList = bestArticles
            Log.d("ProcCheck", "Loading Articles for Best Stories")
        }

        var storyURL : URL // URL to be constructed from prefix + ID + suffix
        var storyInfo : String // String representing the inputStream from HTTP request
        var storyConnection : HttpURLConnection // The HTTP connection object

        withContext(Dispatchers.IO) {
            // load 25 articles
            var i = 0
            while (i < 25) {


                // base case
                if (storyIDs.isEmpty()) {
                    Log.d("Debug", "No More Stories to Load")
                    return@withContext
                }

                storyURL = URL(prefix + storyIDs[0] + suffix)

                // remove loaded Article ID from list
                storyIDs.removeFirst()
//                Log.d("URL", storyURL.toString())
                storyConnection = storyURL.openConnection() as HttpURLConnection

                storyConnection.connect()
                storyInfo = storyConnection.inputStream.bufferedReader().use {it.readText()}
//                Log.d("Data", storyInfo)
                // JSONObject holding the raw information of the article
                var storyJSON : JSONObject

                // try to parse the JSON object
                try {
                    // parse JSON object
                    storyJSON = JSONObject(storyInfo.substring(
                        storyInfo.indexOf("{"), storyInfo.lastIndexOf("}") + 1
                    ))
                } catch (e : Exception) {
                    // error parsing JSON
                    Log.d("Exception", e.toString())
                    continue
                }

                storyConnection.disconnect()

                // try to populate Article details with parsed JSON info
                try {
                    storyJSON.get("url").toString()

                    // get a new Article object by passing parsed JSON object into makeArticle()
                    val newArticle = makeArticle(storyJSON)

                    // add the returned Article to the current Article list
                    articleList.add(newArticle)

//                    Log.d("Article", newArticle.toString())

                    // i is only incremented if an article is successfully loaded
                    i++

                } catch (e : Exception) {
                    Log.d("Exception", e.toString())
                }

            }

        }



    }

    /**
     * Updates the live data object in a thread-safe way with the passed in list of articles to
     * notify adapter of update
     * @param stories list of articles to be displayed on the MainActivity
     */
    private suspend fun populateStoryList(stories : MutableList<Article>) {
        withContext(Dispatchers.Default) {
            _data.postValue(stories)
        }
    }


    /**
     * Parses through JSON data of items returned from API to turn into Article model objects
     * @param json passed in json object to populate Article Model information
     * @return Article returned article generated from json data
     */
    private fun makeArticle (json : JSONObject) : Article {
//        Log.d("JSON", json.toString())
        val title = json.get("title").toString()
        val rating = json.get("score").toString()
        val url = json.get("url").toString()
        val time = json.get("time").toString()

        return Article(title, rating, url, time)
    }

    /**
     * makes the initial HTTP request to receive either 500 best or recent story IDs
     * @param url target URL for API call to receive best or recent story IDs
     * @param recent whether or not we want best or recent story IDs
     */
    private suspend fun idRequest(url : String, recent : Boolean) {

        withContext(Dispatchers.IO) {

            val idList : MutableList<String>

            val targetURL = URL(url)

            val connection : HttpURLConnection = targetURL.openConnection() as HttpURLConnection


            // make the GET request here
            try{
                connection.connect()
            } catch (e : Exception) { // should never be an error here as long as API is consistent
                Log.d("Exception", e.toString())
                return@withContext
            }

            // receive response here & decipher into readable Array<String>
            val parsedData : String = connection.inputStream.bufferedReader().use {it.readText()}
                .trim('[').trim()
            idList = parsedData.split(", ").toMutableList()

            /* trim() or removeSurrounding() doesn't work for final val, have to */
            /* truncate manually (?) */
            idList[idList.size - 1] = idList[idList.size - 1].take(
                idList[idList.size - 1].length-2
            )

            // uncomment to see first and last ID in logcat
//            Log.d("IDs", idList[0]) // debug -> first ID in the list
//            Log.d("IDs", idList[idList.size - 1]) // debug -> last ID in the list
            if (recent) {
                recentStoryIDs = idList
            } else {
                bestStoryIDs = idList
            }
            Log.d("MainActivity","ID HTTP requests successful")
            connection.disconnect()

        }



    }


}