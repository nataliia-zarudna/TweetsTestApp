package com.nzarudna.tweetstestapp

import android.arch.lifecycle.ViewModel
import javax.inject.Inject

/**
 * Created by Nataliia on 12.04.2018.
 */
class TimelineViewModel : ViewModel() {

    @Inject lateinit var twitterAuthManager : TwitterAuthManager

}