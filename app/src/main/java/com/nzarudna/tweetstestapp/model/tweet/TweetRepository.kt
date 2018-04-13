package com.nzarudna.tweetstestapp.model.tweet

import android.arch.paging.ItemKeyedDataSource
import android.arch.paging.PagedList
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.nzarudna.tweetstestapp.TwitterApi
import com.nzarudna.tweetstestapp.TwitterAuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Nataliia on 13.04.2018.
 */
@Singleton
class TweetRepository @Inject constructor(val mTwitterAuthManager: TwitterAuthManager,
                                          val mTwitterApi: TwitterApi) {

    private val TAG = "TweetRepository"

    private val USER_TIMELINE_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json"

    fun getPublicTweets(userID: String, pageSize: Int): PagedList<Tweet> {

        val apiTweetsDataSource: ApiTweetsDataSource = ApiTweetsDataSource(userID, mTwitterApi, mTwitterAuthManager)
        val config: PagedList.Config = PagedList.Config.Builder().setPageSize(pageSize).build()
        val pagedList: PagedList<Tweet> =
                PagedList.Builder(apiTweetsDataSource, config)
                        .setFetchExecutor(Executors.newSingleThreadExecutor())
                        .setNotifyExecutor(object: Executor {

                            private val handler: Handler = Handler(Looper.getMainLooper())

                            override fun execute(r: Runnable?) {
                                handler.post(r)
                            }
                        })
                        .build()

        return pagedList
    }

    class ApiTweetsDataSource
    @Inject constructor(val userID: String, val mTwitterApi: TwitterApi, val mTwitterAuthManager: TwitterAuthManager) : ItemKeyedDataSource<String, Tweet>() {

        private val TAG = "ApiTweetsDataSource"

        override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Tweet>) {

            val requestParams = HashMap<String, String>()
            requestParams.put(TwitterAuthManager.USER_ID, userID)
            requestParams.put("count", params.requestedLoadSize.toString())
            //requestParams.put("since_id", params.requestedLoadSize.toString())
            //requestParams.put("max_id", params.requestedLoadSize.toString())

            val authHeader: String = mTwitterAuthManager.getOAuthHeader(TwitterAuthManager.GET, "https://api.twitter.com/1.1/statuses/user_timeline.json",
                    null, requestParams)
            mTwitterApi
                    .getTimeline(authHeader, userID, params.requestedLoadSize)
                    .enqueue(object: Callback<List<Tweet>> {

                        override fun onResponse(call: Call<List<Tweet>>?, response: Response<List<Tweet>>?) {
                            if (response?.isSuccessful == true && response.body() != null) {

                                var tweetsList: List<Tweet> = response.body()!!;
                                callback.onResult(tweetsList)

                            } else {
                                Log.e(TAG, "Fail to load timeline")
                            }
                        }

                        override fun onFailure(call: Call<List<Tweet>>?, t: Throwable?) {
                            Log.e(TAG, "Fail to load timeline", t)
                        }

                    })
        }

        override fun getKey(item: Tweet): String {
            return item.tweetID
        }

        override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Tweet>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Tweet>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}