package com.nzarudna.tweetstestapp.model.tweet

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.ItemKeyedDataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.util.Log
import com.nzarudna.tweetstestapp.model.api.TwitterApi
import com.nzarudna.tweetstestapp.model.api.TwitterAuthManager
import retrofit2.Response
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

    fun getPublicTweets(userID: String, pageSize: Int): LiveData<PagedList<Tweet>> {

        val dataSourceFactory = ApiTweetsFactory(userID, mTwitterApi, mTwitterAuthManager)
        val config: PagedList.Config = PagedList.Config.Builder()
                .setPageSize(pageSize)
                .setEnablePlaceholders(false)
                .build()

        val livePagedList: LiveData<PagedList<Tweet>> =
                LivePagedListBuilder(dataSourceFactory, config)
                        .setFetchExecutor(Executors.newSingleThreadExecutor())
                        .setBoundaryCallback(object : PagedList.BoundaryCallback<Tweet>() {
                            override fun onItemAtFrontLoaded(itemAtFront: Tweet) {
                                super.onItemAtFrontLoaded(itemAtFront)
                            }
                        })
                        .build()

        return livePagedList
    }

    class ApiTweetsFactory(val userID: String,
                           val mTwitterApi: TwitterApi,
                           val mTwitterAuthManager: TwitterAuthManager) : DataSource.Factory<String, Tweet>() {

        override fun create(): DataSource<String, Tweet> {
            return ApiTweetsDataSource(userID, mTwitterApi, mTwitterAuthManager)
        }
    }

    class ApiTweetsDataSource
    @Inject constructor(val userID: String,
                        val mTwitterApi: TwitterApi,
                        val mTwitterAuthManager: TwitterAuthManager) : ItemKeyedDataSource<String, Tweet>() {

        private val TAG = "ApiTweetsDataSource"

        private val USER_TIMELINE_URL = "https://api.twitter.com/1.1/statuses/user_timeline.json"

        override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Tweet>) {

            val requestParams = HashMap<String, String>()
            requestParams.put(TwitterAuthManager.USER_ID, userID)
            requestParams.put("count", params.requestedLoadSize.toString())

            val authHeader: String = mTwitterAuthManager.getOAuthHeader(TwitterAuthManager.GET, USER_TIMELINE_URL,
                    null, requestParams)
            val response: Response<List<Tweet>> = mTwitterApi
                    .getTimeline(authHeader, userID, params.requestedLoadSize)
                    .execute()
            processResponse(response, callback)
        }

        override fun getKey(item: Tweet): String {
            return item.tweetID
        }

        override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Tweet>) {

            val requestParams = HashMap<String, String>()
            requestParams.put(TwitterAuthManager.USER_ID, userID)
            requestParams.put("count", params.requestedLoadSize.toString())
            requestParams.put("max_id", params.key)

            val authHeader: String = mTwitterAuthManager.getOAuthHeader(TwitterAuthManager.GET, USER_TIMELINE_URL,
                    null, requestParams)
            val response: Response<List<Tweet>> = mTwitterApi
                    .getTimelineAfter(authHeader, userID, params.requestedLoadSize, params.key)
                    .execute()
            processResponse(response, object: LoadCallback<Tweet>() {
                override fun onResult(tweetsList: MutableList<Tweet>) {
                    tweetsList.removeAt(0)
                    callback.onResult(tweetsList)
                }
            })
        }

        override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Tweet>) {

            val requestParams = HashMap<String, String>()
            requestParams.put(TwitterAuthManager.USER_ID, userID)
            requestParams.put("count", params.requestedLoadSize.toString())
            requestParams.put("since_id", params.key)

            val authHeader: String = mTwitterAuthManager.getOAuthHeader(TwitterAuthManager.GET, USER_TIMELINE_URL,
                    null, requestParams)
            val response: Response<List<Tweet>> = mTwitterApi
                    .getTimelineBefore(authHeader, userID, params.requestedLoadSize, params.key)
                    .execute()
            processResponse(response, callback)
        }

        private fun processResponse(response: Response<List<Tweet>>, callback: LoadCallback<Tweet>) {
            if (response.isSuccessful && response.body() != null) {

                val tweetsList: List<Tweet> = response.body()!!
                callback.onResult(tweetsList)

            } else {
                Log.e(TAG, "Fail to load timeline: " + response.errorBody()?.string())
            }
        }
    }
}