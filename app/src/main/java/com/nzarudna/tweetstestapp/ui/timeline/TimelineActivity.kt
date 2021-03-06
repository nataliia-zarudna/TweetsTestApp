package com.nzarudna.tweetstestapp.ui.timeline

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import com.nzarudna.tweetstestapp.ui.SingleFragmentActivity

class TimelineActivity : SingleFragmentActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, TimelineActivity::class.java)
        }
    }

    override fun getFragmentInstance(): Fragment {
        return TimelineFragment.newInstance()
    }
}
