package com.nzarudna.tweetstestapp.ui.timeline

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.PropertyChangeRegistry
import com.nzarudna.tweetstestapp.BR
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import java.text.SimpleDateFormat

/**
 * View model for one tweet item in list
 */
class TweetItemViewModel : BaseObservable() {

    val mRegistry: PropertyChangeRegistry = PropertyChangeRegistry()

    @Bindable
    var mTweet: Tweet? = null
        set(value) {
            field = value
            mRegistry.notifyChange(this, BR.viewModel)
        }

    var text: String? = null
        get() = mTweet?.text ?: ""

    var createdAt: String? = null
        get() {
            if (mTweet?.createdAt != null) {
                return SimpleDateFormat.getDateTimeInstance().format(mTweet!!.createdAt)
            } else {
                return null
            }
        }
}