package com.example.woven_news.adapter

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
        val storyTitle : TextView = view.findViewById(R.id.panel)
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

    override fun onBindViewHolder(holder : PanelViewHolder, position : Int) {
        val item = content[position]
        holder.storyTitle.text = item
    }

    override fun getItemCount() = content.size

}