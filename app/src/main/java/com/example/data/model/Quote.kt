package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Quote(
    @Json(name = "q") val quoteText: String = "",
    @Json(name = "a") val author: String = "",
    @Json(name = "h") val html: String? = null
)

data class MotivationalQuote(
    val text: String,
    val author: String,
    val category: String = "Coding"
)
