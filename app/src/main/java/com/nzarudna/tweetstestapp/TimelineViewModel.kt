package com.nzarudna.tweetstestapp

import android.arch.lifecycle.ViewModel
import android.content.Context
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

    fun loadTimeline() {



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
                    val d = 1
                }

                override fun onError(e: Throwable) {
                    observer?.onError(e)
                }

            })
        }
    }

    interface TimelineViewModelObserver {
        fun loadURL(url: String)
        fun onError(e: Throwable)
    }
}