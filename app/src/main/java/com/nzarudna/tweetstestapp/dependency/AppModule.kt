package com.nzarudna.tweetstestapp.dependency

import android.content.Context
import android.content.SharedPreferences
import com.nzarudna.tweetstestapp.R
import com.nzarudna.tweetstestapp.TwitterApi
import com.nzarudna.tweetstestapp.TwitterAuthManager
import dagger.Module
import dagger.Provides

/**
 * Created by Nataliia on 12.04.2018.
 */
@Module
class AppModule(var mContext: Context) {

    @Provides
    fun provideTwitterAuthManager() : TwitterAuthManager {
        return TwitterAuthManager(provideSharedPreferences(), mContext)
    }

    @Provides
    fun provideSharedPreferences() : SharedPreferences {
        val preferenceFile = mContext.getString(R.string.tweets_preferences_file)
        return mContext.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideTwitterApi() : TwitterApi {
        return TwitterApi.Factory.create()
    }
}