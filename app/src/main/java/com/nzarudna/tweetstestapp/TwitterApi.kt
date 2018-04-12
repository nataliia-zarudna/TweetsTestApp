package com.nzarudna.tweetstestapp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by Nataliia on 13.04.2018.
 */
public interface TwitterApi {

    @POST("oauth/request_token")
    fun getRequestToken(@Header("Authorization") authorizationHeader: String): Call<String>

    companion object Factory {
        fun create(): TwitterApi {
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://api.twitter.com/")
                    .build()
            return retrofit.create(TwitterApi::class.java)
        }
    }
}