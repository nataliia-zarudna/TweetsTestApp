package com.nzarudna.tweetstestapp.dependency

import com.nzarudna.tweetstestapp.TwitterAuthManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Nataliia on 12.04.2018.
 */
@Module
class AppModule {

    @Provides
    fun provideTwitterAuthManager() : TwitterAuthManager {
        return TwitterAuthManager()
    }
}