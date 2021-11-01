package com.example.woven_news.model

data class Article (
    val title : String = "(No Title)",
    val rating : String = "-1",
    val URL : String = "",
    val time : String = System.currentTimeMillis().toString(),
        )