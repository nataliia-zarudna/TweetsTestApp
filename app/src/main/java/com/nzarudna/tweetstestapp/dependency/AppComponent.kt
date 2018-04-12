package com.nzarudna.tweetstestapp.dependency

import com.nzarudna.tweetstestapp.TimelineViewModel
import dagger.Component

/**
 * Created by Nataliia on 12.04.2018.
 */
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(viewModel: TimelineViewModel)

}