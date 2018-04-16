package com.nzarudna.tweetstestapp.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.nzarudna.tweetstestapp.R

/**
 * Activity that holds one fragment
 */
abstract class SingleFragmentActivity : AppCompatActivity() {

    abstract fun getFragmentInstance(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_fragemnt)

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            val timelineFragment = getFragmentInstance()
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, timelineFragment)
                    .commit()
        }
    }
}