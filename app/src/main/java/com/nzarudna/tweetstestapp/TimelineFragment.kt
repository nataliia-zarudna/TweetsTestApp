package com.nzarudna.tweetstestapp

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.android.volley.Response
import com.nzarudna.tweetstestapp.databinding.FragmentTimelineBinding
import kotlinx.android.synthetic.main.fragment_timeline.*

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

        val webView = login_view

        //FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
        //mAuth = FirebaseAuth.getInstance();

        TwitterAuthManager.getRequestToken(this, Response.Listener<String> { response ->
            //Log.d(TAG, "response " + response)

            val oauthToken = TwitterAuthManager.getResponseParamValue(response, "oauth_token")

            val webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageFinished(view: WebView, url: String) {

                    if (url.startsWith(BuildConfig.CALLBACK_URL)) {
                        val urlParams = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                        val authVerifier = TwitterAuthManager.getResponseParamValue(urlParams, "oauth_verifier")

                        TwitterAuthManager.getRequestToken(this@MainActivity, Response.Listener<String> { response ->
                            Log.d(TAG, response)

                            val authVerifier = TwitterAuthManager.getResponseParamValue(urlParams, "oauth_verifier")
                        }, oauthToken, authVerifier)
                    }

                    super.onPageFinished(view, url)
                }
            }
            webView.setWebViewClient(webViewClient)
            webView.loadUrl("https://api.twitter.com/oauth/authenticate?oauth_token=" + oauthToken)
        }, null, null)

        return fragmentView.root
    }

}