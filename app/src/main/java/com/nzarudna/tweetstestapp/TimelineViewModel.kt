package com.nzarudna.tweetstestapp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.PagedList
import android.databinding.Bindable
import android.databinding.Observable
import android.databinding.PropertyChangeRegistry
import android.util.Log
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import com.nzarudna.tweetstestapp.model.tweet.TweetRepository
import javax.inject.Inject

/**
 * Created by Nataliia on 12.04.2018.
 */
class TimelineViewModel : ViewModel(), Observable {

    private val TAG = "TimelineViewModel"

    private val PAGE_SIZE: Int = 20

    @Inject lateinit var mTwitterAuthManager: TwitterAuthManager
    @Inject lateinit var mTwitterRepository: TweetRepository
    private val mRegistry = PropertyChangeRegistry()

    var mObserver: TimelineViewModelObserver? = null
    var mListCount: Int = 0

    @Bindable
    var isAuthenticating = false
        get() {
            Log.d(TAG, "Set isAuthenticating GET " + field)
            return field
        }
        set(value) {
            field = value
            Log.d(TAG, "Set isAuthenticating " + value)
            mRegistry.notifyChange(this, BR._all)
        }

    @Bindable
    var isAuthorized: Boolean = false
        get() {
            val value = mTwitterAuthManager.isAuthorized()
            Log.d(TAG, "Set isAuthenticating GET " + value)
            return value
        }
        set(value) {
            field = value
            Log.d(TAG, "Set isAuthorized " + value)
            mRegistry.notifyChange(this, BR._all)
        }

    fun loadTimeline(): LiveData<PagedList<Tweet>> {

        val userID = mTwitterAuthManager.getUserID()!!
        return mTwitterRepository.getPublicTweets(userID, PAGE_SIZE)
    }

    fun authorize() {
        isAuthenticating = true

        mTwitterAuthManager.getRequestToken(object : TwitterAuthManager.ObtainAuthTokenListener {

            override fun onObtainToken(authToken: String?) {
                if (authToken == null) {
                    mObserver?.onError(OAuthException("OAuth token is empty"))
                    return
                }

                val authURL: String = mTwitterAuthManager.getAuthenticateURL(authToken)
                mObserver?.loadURL(authURL)
            }

            override fun onError(e: Throwable) {
                isAuthenticating = false
                mObserver?.onError(e)
            }
        })
    }

    fun onWebPageFinished(url: String, observer: TimelineViewModelObserver?) {

        if (url.startsWith(BuildConfig.CALLBACK_URL)) {
            mTwitterAuthManager.getAuthToken(url, object : TwitterAuthManager.ObtainAuthTokenListener {

                override fun onObtainToken(authToken: String?) {
                    isAuthenticating = false
                    observer?.onAuthorized()
                }

                override fun onError(e: Throwable) {
                    isAuthenticating = false
                    observer?.onError(e)
                }
            })
        }
    }

    fun invalidateOauthToken() {
        mTwitterAuthManager.mSharedPreferences.edit()
                .remove(TwitterAuthManager.USER_ID)
                .remove(TwitterAuthManager.OAUTH_TOKEN)
                .remove(TwitterAuthManager.OAUTH_TOKEN_SECRET)
                .apply()
        isAuthorized = false
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        mRegistry.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        mRegistry.remove(callback)
    }

    interface TimelineViewModelObserver {
        fun loadURL(url: String)
        fun onAuthorized()
        fun onError(e: Throwable)
    }
}