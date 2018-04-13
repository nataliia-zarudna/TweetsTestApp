package com.nzarudna.tweetstestapp

import android.arch.lifecycle.ViewModel
import android.arch.paging.PagedList
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import com.nzarudna.tweetstestapp.model.tweet.TweetRepository
import javax.inject.Inject

/**
 * Created by Nataliia on 12.04.2018.
 */
class TimelineViewModel : ViewModel() {

    @Inject lateinit var mTwitterAuthManager : TwitterAuthManager
    @Inject lateinit var mTwitterRepository: TweetRepository

    fun isAuthorized(): Boolean {
        return mTwitterAuthManager.isAuthorized()
    }

    fun loadTimeline(): PagedList<Tweet> {

        val userID = mTwitterAuthManager.getUserID()!!
        //val userID = "44196397"
        return mTwitterRepository.getPublicTweets(userID, 20)
    }

    fun authorize(observer: TimelineViewModelObserver?) {
        mTwitterAuthManager.getRequestToken(object: TwitterAuthManager.ObtainAuthTokenListener {

            override fun onObtainToken(authToken: String?) {
                if (authToken == null) {
                    observer?.onError(OAuthException("OAuth token is empty"))
                    return
                }

                val authURL: String = mTwitterAuthManager.getAuthenticateURL(authToken)
                observer?.loadURL(authURL)
            }

            override fun onError(e: Throwable) {
                observer?.onError(e)
            }
        })
    }

    fun onWebPageFinished(url: String, observer: TimelineViewModelObserver?) {

        if (url.startsWith(BuildConfig.CALLBACK_URL)) {
            mTwitterAuthManager.getAuthToken(url, object: TwitterAuthManager.ObtainAuthTokenListener {

                override fun onObtainToken(authToken: String?) {
                    observer?.onAuthorized()
                }

                override fun onError(e: Throwable) {
                    observer?.onError(e)
                }

            })
        }
    }

    interface TimelineViewModelObserver {
        fun loadURL(url: String)
        fun onAuthorized()
        fun onError(e: Throwable)
    }
}