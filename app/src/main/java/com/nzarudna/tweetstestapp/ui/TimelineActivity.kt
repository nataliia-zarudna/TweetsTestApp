package com.nzarudna.tweetstestapp.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nzarudna.tweetstestapp.R

class TimelineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            var timelineFragment = TimelineFragment()
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, timelineFragment)
                    .commit()
        }
    }
}
