package com.nzarudna.tweetstestapp.dependency

import com.nzarudna.tweetstestapp.ui.login.LoginViewModel
import com.nzarudna.tweetstestapp.ui.timeline.TimelineViewModel
import dagger.Component

/**
 * Dagger application component
 */
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(viewModel: TimelineViewModel)

    fun inject(viewModel: LoginViewModel)

}