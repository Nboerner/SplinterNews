package com.example.woven_news.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.woven_news.R

class PanelAdapter(private val context : Context, private var content : List<String>)
    : RecyclerView.Adapter<PanelAdapter.PanelViewHolder>() {

    class PanelViewHolder(private val view : View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        // identifyies the panel attributes for reassignment in binding
        val storyNumber : TextView = view.findViewById(R.id.number)
        val storyTitle : TextView = view.findViewById(R.id.headline)
        val ratingScore : TextView = view.findViewById(R.id.score)
        val webURL : TextView = view.findViewById(R.id.url)
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view : View?) {
            Log.d("RecyclerView", "GOTCHA")
        }
    }

    fun update(newContent : List<String>) {
        content = newContent
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PanelViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_panel, parent, false)

        return PanelViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n") // without active context, couldn't use getString() or alt ?
    override fun onBindViewHolder(holder : PanelViewHolder, position : Int) {
        val item = content[position]
        holder.storyNumber.text = (position + 1).toString() + "."
        holder.storyTitle.text = item
//        holder.ratingScore.text = SCORE
//        holder.webURL.text = URL
    }

    override fun getItemCount() = content.size

}