package com.nzarudna.tweetstestapp

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.nzarudna.tweetstestapp.databinding.FragmentTimelineBinding
import kotlinx.android.synthetic.main.fragment_timeline.*

/**
 * Created by Nataliia on 11.04.2018.
 */
class TimelineFragment : Fragment(), TimelineViewModel.TimelineViewModelObserver {

    lateinit var mViewModel: TimelineViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fragmentView: FragmentTimelineBinding
                = DataBindingUtil.inflate(inflater, R.layout.fragment_timeline, container, false)

        mViewModel = TimelineViewModel()
        (activity?.application as TweetsTestApplication).appComponent.inject(mViewModel)

        //mViewModel.mTwitterAuthManager.mSharedPreferences.edit().remove("user_id").commit()
        //mViewModel.mTwitterAuthManager.mSharedPreferences.edit().remove("oauth_token").commit()
        //mViewModel.mTwitterAuthManager.mSharedPreferences.edit().remove("oauth_token_secret").commit()

        if (mViewModel.isAuthorized()) {
            mViewModel.loadTimeline()
        } else {
            mViewModel.authorize(this)
        }

        return fragmentView.root
    }

    override fun loadURL(url: String) {

        val webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {

                if (activity != null) {
                    mViewModel.onWebPageFinished(url, this@TimelineFragment)
                }

                super.onPageFinished(view, url)
            }
        }
        loginWebView.setWebViewClient(webViewClient)
        loginWebView.loadUrl(url)
    }

    override fun onAuthorized() {
        mViewModel.loadTimeline()
    }

    override fun onError(e: Throwable) {
        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
    }

}