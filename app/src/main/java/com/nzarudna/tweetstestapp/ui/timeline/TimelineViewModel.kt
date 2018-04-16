package com.nzarudna.tweetstestapp.ui.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.PagedList
import android.databinding.Observable
import android.databinding.PropertyChangeRegistry
import com.nzarudna.tweetstestapp.model.api.TwitterAuthManager
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import com.nzarudna.tweetstestapp.model.tweet.TweetRepository
import javax.inject.Inject

/**
 * View model for user timeline
 */
class TimelineViewModel : ViewModel(), Observable {

    private val TAG = "TimelineViewModel"

    private val PAGE_SIZE: Int = 20

    @Inject lateinit var mTwitterAuthManager: TwitterAuthManager
    @Inject lateinit var mTwitterRepository: TweetRepository
    private val mRegistry = PropertyChangeRegistry()

    var mObserver: TimelineViewModelObserver? = null
    var mListCount: Int = 0

    fun loadTimeline(): LiveData<PagedList<Tweet>> {

        val userID = mTwitterAuthManager.getUserID()!!
        return mTwitterRepository.getPublicTweets(userID, PAGE_SIZE)
    }

    fun logout() {
        mTwitterAuthManager.mSharedPreferences.edit()
                .remove(TwitterAuthManager.USER_ID)
                .remove(TwitterAuthManager.OAUTH_TOKEN)
                .remove(TwitterAuthManager.OAUTH_TOKEN_SECRET)
                .apply()

        mObserver?.onLogout()
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        mRegistry.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        mRegistry.remove(callback)
    }

    interface TimelineViewModelObserver {
        fun onLogout()
        fun onError(e: Throwable)
    }
}