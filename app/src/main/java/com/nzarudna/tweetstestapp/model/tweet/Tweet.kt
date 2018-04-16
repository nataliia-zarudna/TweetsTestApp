package com.nzarudna.tweetstestapp.model.tweet

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Tweet model
 */
data class Tweet(

        @SerializedName("id_str")
        val tweetID: String,

        val text: String,

        @SerializedName("created_at")
        val createdAt: Date
)