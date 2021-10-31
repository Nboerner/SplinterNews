package com.example.woven_news.adapter

import android.annotation.SuppressLint
import android.content.Context
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

class PanelAdapter(private val context : Context, private var content : List<Article>)
    : RecyclerView.Adapter<PanelAdapter.PanelViewHolder>() {


    class PanelViewHolder(context : Context, view : View) : RecyclerView.ViewHolder(view),
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

        override fun onClick(view : View?) {
            Log.d("RecyclerView", "GOTCHA")
            try {
                val targetURL = Intent(Intent.ACTION_VIEW)
                targetURL.data = Uri.parse(webURL.contentDescription.toString())
                Log.d("URI", targetURL.data.toString())
                view!!.context.startActivity(targetURL)

            } catch (e : Exception) {
                Log.d("Exception", e.toString())
                Toast.makeText(view!!.context, "Error Opening URL",
                    Toast.LENGTH_SHORT).show()
            }

        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newContent : List<Article>) {
        content = newContent
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PanelViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_panel, parent, false)

        return PanelViewHolder(context, adapterLayout)
    }

    @SuppressLint("SetTextI18n") // without active context, couldn't use getString() or alt ?
    override fun onBindViewHolder(holder : PanelViewHolder, position : Int) {
        val item = content[position]
        holder.storyNumber.text = (position + 1).toString() + "."
        holder.storyTitle.text = item.title
        holder.ratingScore.text = item.rating
        // the URL content description is used to give the full URL to open a webpage
        holder.webURL.contentDescription = item.URL
        try {
            holder.webURL.text = "(" + URL(item.URL).host + ")"
        } catch (e : Exception) { // if there is an error getting host, just have blank URL
            Log.d("Debug", e.toString())
            holder.webURL.text = ""
        }
        holder.time.text = calculateTime(item.time.toLong())

    }

    override fun getItemCount() = content.size

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