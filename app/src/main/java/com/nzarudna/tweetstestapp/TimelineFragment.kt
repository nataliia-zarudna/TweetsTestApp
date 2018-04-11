package com.nzarudna.tweetstestapp

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nzarudna.tweetstestapp.databinding.FragmentTimelineBinding

/**
 * Created by Nataliia on 11.04.2018.
 */
class TimelineFragment : Fragment() {

    lateinit var mViewModel : TimelineViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fragmentView: FragmentTimelineBinding
                = DataBindingUtil.inflate(inflater, R.layout.fragment_timeline, container, false)

        mViewModel = TimelineViewModel()
        (activity.application as TweetsTestApplication).appComponent.inject(mViewModel)

        return fragmentView.root
    }

}