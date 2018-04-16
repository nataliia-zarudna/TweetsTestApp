package com.nzarudna.tweetstestapp.dependency

import android.content.Context
import android.content.SharedPreferences
import com.nzarudna.tweetstestapp.R
import com.nzarudna.tweetstestapp.model.api.TwitterApi
import com.nzarudna.tweetstestapp.model.api.TwitterAuthManager
import com.nzarudna.tweetstestapp.model.tweet.TweetRepository
import dagger.Module
import dagger.Provides

/**
 * Created by Nataliia on 12.04.2018.
 */
@Module
class AppModule(var mContext: Context) {

    @Provides
    fun provideTwitterAuthManager() : TwitterAuthManager {
        return TwitterAuthManager(provideSharedPreferences(), mContext, provideTwitterApi())
    }

    @Provides
    fun provideTweetRepository() : TweetRepository {
        return TweetRepository(provideTwitterAuthManager(), provideTwitterApi())
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