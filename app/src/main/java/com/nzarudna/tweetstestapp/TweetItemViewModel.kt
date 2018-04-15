package com.nzarudna.tweetstestapp

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.PropertyChangeRegistry
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import java.text.SimpleDateFormat

/**
 * Created by nsirobaba on 4/13/18.
 */
class TweetItemViewModel : BaseObservable() {

    val mRegistry: PropertyChangeRegistry = PropertyChangeRegistry()

    @Bindable
    lateinit var tweet: Tweet
    /*set(value) {
        mRegistry.notifyChange(this, BR._all)
    }*/

    var text: String = ""
        get() = tweet.text

    var createdAt: String = ""
        get() = SimpleDateFormat.getDateTimeInstance().format(tweet.createdAt)
}