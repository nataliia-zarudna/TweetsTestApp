package com.nzarudna.tweetstestapp.model.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

/**
 * Created by Nataliia on 13.04.2018.
 */
public interface TwitterApi {

    @POST("oauth/request_token")
    fun getRequestToken(@Header("Authorization") authorizationHeader: String): Call<String>

    @FormUrlEncoded
    @POST("oauth/access_token")
    fun getAuthToken(@Header("Authorization") authorizationHeader: String,
                     @Field("oauth_verifier") oauthVerifier: String): Call<String>

    @GET("1.1/statuses/user_timeline.json")
    fun getTimeline(@Header("Authorization") authorizationHeader: String,
                    @Query("user_id") userID: String,
                    @Query("count") count: Int): Call<List<Tweet>>

    @GET("1.1/statuses/user_timeline.json")
    fun getTimelineBefore(@Header("Authorization") authorizationHeader: String,
                          @Query("user_id") userID: String,
                          @Query("count") count: Int,
                          @Query("since_id") sinceID: String): Call<List<Tweet>>

    @GET("1.1/statuses/user_timeline.json")
    fun getTimelineAfter(@Header("Authorization") authorizationHeader: String,
                         @Query("user_id") userID: String,
                         @Query("count") count: Int,
                         @Query("max_id") maxID: String): Call<List<Tweet>>

    companion object Factory {
        fun create(): TwitterApi {

            val gson: Gson = GsonBuilder()
                    .setDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
                    .create()

            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://api.twitter.com/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            return retrofit.create(TwitterApi::class.java)
        }
    }
}