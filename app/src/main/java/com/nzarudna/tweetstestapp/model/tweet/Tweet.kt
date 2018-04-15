package com.nzarudna.tweetstestapp.model.tweet

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Nataliia on 13.04.2018.
 */
data class Tweet(

        @SerializedName("id_str")
        val tweetID: String,

        val text: String,

        @SerializedName("created_at")
        val createdAt: Date
)