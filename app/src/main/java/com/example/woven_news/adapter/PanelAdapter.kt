package com.example.woven_news.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.woven_news.R
import com.example.woven_news.model.Article
import java.lang.Exception
import java.net.URL

class PanelAdapter(private var content : List<Article>)
    : RecyclerView.Adapter<PanelAdapter.PanelViewHolder>() {

    /**
     * ViewHolder class to modify and handle the values displayed on story_panels on the Main
     * Activity's recyclerView
     * @param view the view being passed in that will be modified with story details
     */
    class PanelViewHolder(view : View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        // identifies the panel attributes for reassignment in binding
        val storyNumber : TextView = view.findViewById(R.id.number)
        val storyTitle : TextView = view.findViewById(R.id.headline)
        val ratingScore : TextView = view.findViewById(R.id.score)
        val webURL : TextView = view.findViewById(R.id.url)
        val time : TextView = view.findViewById(R.id.timestamp)
        init {
            view.setOnClickListener(this)
        }

        /**
         * Actions to be taken when a story_panel item is clicked
         * @param view the story panel that is clicked
         */
        override fun onClick(view : View?) {
//            Log.d("RecyclerView", "GOTCHA")
            try {
                // try to open the URL associated with the panel
                val targetURL = Intent(Intent.ACTION_VIEW)
                targetURL.data = Uri.parse(webURL.contentDescription.toString())
//                Log.d("URI", targetURL.data.toString())
                view!!.context.startActivity(targetURL)

            } catch (e : Exception) {
                // if URL doesn't work, catch exception and notify user
                Log.d("Exception", e.toString())
                Toast.makeText(view!!.context, "Error Opening URL",
                    Toast.LENGTH_SHORT).show()
            }

        }


    }

    /**
     * Updates the content stored within the recyclerView and notifies the Main Activity
     * to update the UI
     * @param newContent the updated content that needs to be loaded into the Main Activity UI
     */
    @SuppressLint("NotifyDataSetChanged")
    fun update(newContent : List<Article>) {
        content = newContent
        notifyDataSetChanged()
    }

    /**
     * Handles story_panel inflation details and creates the ViewHolder for the story_panel
     * @param parent the parent ViewGroup to where the story_panel will be inflated
     * @param viewType unused - override parameter value
     * @return returns a set up PanelViewHolder class object
     */
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PanelViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_panel, parent, false)

        return PanelViewHolder(adapterLayout)
    }

    /**
     * Assigns the values on a story_panel from the Articles within content
     * @param holder the PanelViewHolder that is getting the modified data
     * @param position the index of the current Article Model Object which is getting read
     */
    @SuppressLint("SetTextI18n") // without active context, couldn't use getString() or alt ?
    override fun onBindViewHolder(holder : PanelViewHolder, position : Int) {
        // current Article we are looking at
        val item = content[position]

        // fill in view information with Article information
        holder.storyNumber.text = (position + 1).toString() + "."
        holder.storyTitle.text = item.title
        holder.ratingScore.text = item.rating

        // the URL content description is used to give the full URL to open a webpage
        holder.webURL.contentDescription = item.URL

        // show website hostname for user convenience
        try {
            holder.webURL.text = "(" + URL(item.URL).host + ")"
        } catch (e : Exception) { // if there is an error getting host, just have blank URL
            Log.d("Exception", e.toString())
            holder.webURL.text = ""
        }
        // calculate how long ago the article was posted in Unix time
        holder.time.text = calculateTime(item.time.toLong())

    }

    /**
     * returns the number of items currently in the Article list
     */
    override fun getItemCount() = content.size

    /**
     * Calculates in Unix time how long ago an article was written in comparison to the current
     * system time.  Handles seconds, minutes, hours and days.
     * @param timestamp the time the Article was posted
     * @return a string that returns a formatted time stamp
     */
    private fun calculateTime(timestamp : Long) : String {

        // Unix timestamp used for calculating how long ago an article was posted
        val currentTime = System.currentTimeMillis() / 1000 // convert to seconds

        val diff = (currentTime - timestamp)

        val calculatedTime : Long

        when {
            diff < 60 -> { // posted within seconds
                calculatedTime = diff
                val second : String = if (calculatedTime == 1.toLong()) {
                    "second"
                } else "seconds"
                return "$calculatedTime $second ago"

            }
            diff < 3600.toLong() -> { // posted within minutes
                calculatedTime = diff.floorDiv(60)
                val minute : String = if (calculatedTime == 1.toLong()) {
                    "minute"
                } else "minutes"
                return "$calculatedTime $minute ago"

            }
            diff < 86400 -> { // posted within hours
                calculatedTime = diff.floorDiv(3600)
                val hour : String = if (calculatedTime == 1.toLong()) {
                    "hour"
                } else "hours"
                return "$calculatedTime $hour ago"

            }
            else -> { // posted within days
                calculatedTime = diff.floorDiv(86400)
                val day : String = if (calculatedTime == 1.toLong()) {
                    "day"
                } else "days"
                return "$calculatedTime $day ago"
            }
        }

    }

}