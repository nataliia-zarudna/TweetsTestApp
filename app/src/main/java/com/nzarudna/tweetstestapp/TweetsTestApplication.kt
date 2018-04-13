package com.nzarudna.tweetstestapp

import android.app.Application
import com.nzarudna.tweetstestapp.dependency.AppComponent
import com.nzarudna.tweetstestapp.dependency.AppModule
import com.nzarudna.tweetstestapp.dependency.DaggerAppComponent

/**
 * Created by Nataliia on 12.04.2018.
 */
class TweetsTestApplication : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
    }
}