package com.nzarudna.tweetstestapp

import retrofit2.Call
import retrofit2.Retrofit
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
    fun getAuthToken(@Header("Authorization") authorizationHeader: String, @Field("oauth_verifier") oauthVerifier: String): Call<String>

    companion object Factory {
        fun create(): TwitterApi {
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl("https://api.twitter.com/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
            return retrofit.create(TwitterApi::class.java)
        }
    }
}