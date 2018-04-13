package com.nzarudna.tweetstestapp.model.tweet

import android.arch.paging.PositionalDataSource
import android.content.Context
import com.nzarudna.tweetstestapp.TwitterAuthManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Nataliia on 13.04.2018.
 */
@Singleton
class TweetRepository @Inject constructor(val mTwitterAuthManager: TwitterAuthManager) {


    fun getPublicTweets(userID: Int) {


    }

    class ApiTweetsDataSource
    @Inject constructor(val mTwitterAuthManager: TwitterAuthManager) : PositionalDataSource<Tweet>() {

        private val USER_TIMELINE_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json"

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Tweet>) {

            //mTwitterAuthManager.performSignedRequest()

        }

        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Tweet>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}