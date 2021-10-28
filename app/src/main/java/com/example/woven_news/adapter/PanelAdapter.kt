package com.example.woven_news.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.woven_news.R

class PanelAdapter(private val context : Context, private val content : List<String>)
    : RecyclerView.Adapter<PanelAdapter.PanelViewHolder>() {

    class PanelViewHolder(private val view : View) : RecyclerView.ViewHolder(view) {
        val storyTitle : TextView = view.findViewById(R.id.panel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PanelViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_panel, parent, false)

        return PanelViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: PanelViewHolder, position: Int) {
        val item = content[position]
        // TODO set the textview's name to be the title of the story
//        holder.storyTitle.text = context.resources.item.getText()
    }

    override fun getItemCount() = content.size

}