package com.nzarudna.tweetstestapp.ui.login

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.nzarudna.tweetstestapp.BR
import com.nzarudna.tweetstestapp.R
import com.nzarudna.tweetstestapp.TweetsTestApplication
import com.nzarudna.tweetstestapp.databinding.FragmentLoginBinding
import com.nzarudna.tweetstestapp.ui.timeline.TimelineActivity
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * Fragment for user login
 */
class LoginFragment : Fragment(), LoginViewModel.LoginViewModelObserver {

    lateinit var mViewModel: LoginViewModel

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        (activity?.application as TweetsTestApplication).mAppComponent.inject(mViewModel)
        mViewModel.mObserver = this

        if (mViewModel.isAuthorized) {
            startTimelineActivity()
            activity?.finish()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fragmentViewBinding: FragmentLoginBinding
                = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        val fragmentView = fragmentViewBinding.root
        fragmentViewBinding.setVariable(BR.viewModel, mViewModel)

        return fragmentView
    }

    private fun startTimelineActivity() {
        if (activity != null) {
            val intent = TimelineActivity.newIntent(activity!!)
            startActivity(intent)
            activity?.finish()
        }
    }

    /**
     * Load url in WebView
     */
    override fun loadURL(url: String) {

        val webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {

                if (activity != null) {
                    mViewModel.onWebPageFinished(url)
                }

                super.onPageFinished(view, url)
            }
        }
        loginWebView.setWebViewClient(webViewClient)
        loginWebView.loadUrl(url)
    }

    override fun onAuthorized() = startTimelineActivity()

    override fun onError(e: Throwable) {
        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
    }
}