package com.nzarudna.tweetstestapp

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.nzarudna.tweetstestapp.databinding.FragmentTimelineBinding
import com.nzarudna.tweetstestapp.databinding.ListItemTweetBinding
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import kotlinx.android.synthetic.main.fragment_timeline.*

/**
 * Created by Nataliia on 11.04.2018.
 */
class TimelineFragment : Fragment(), TimelineViewModel.TimelineViewModelObserver {

    lateinit var mViewModel: TimelineViewModel
    lateinit var mTweetAdapter: TweetAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fragmentView: FragmentTimelineBinding
                = DataBindingUtil.inflate(inflater, R.layout.fragment_timeline, container, false)

        mViewModel = TimelineViewModel()
        (activity?.application as TweetsTestApplication).appComponent.inject(mViewModel)

        //mViewModel.mTwitterAuthManager.mSharedPreferences.edit().remove("user_id").commit()
        //mViewModel.mTwitterAuthManager.mSharedPreferences.edit().remove("oauth_token").commit()
        //mViewModel.mTwitterAuthManager.mSharedPreferences.edit().remove("oauth_token_secret").commit()

        mTweetAdapter = TweetAdapter(activity!!, object: DiffUtil.ItemCallback<Tweet>() {

            override fun areContentsTheSame(oldItem: Tweet?, newItem: Tweet?): Boolean {
                return oldItem?.text?.equals(newItem?.text) == true
            }

            override fun areItemsTheSame(oldItem: Tweet?, newItem: Tweet?): Boolean {
                return oldItem?.equals(newItem) == true
            }
        })

        tweetsRecyclerView.adapter = mTweetAdapter

        if (mViewModel.isAuthorized()) {
            loadTimeline()
        } else {
            mViewModel.authorize(this)
        }

        return fragmentView.root
    }

    fun loadTimeline() {
        val list: PagedList<Tweet> = mViewModel.loadTimeline()
        mTweetAdapter.submitList(list)
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

    override fun onAuthorized() = loadTimeline()

    override fun onError(e: Throwable) {
        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
    }

    class TweetViewHolder(val mDataBinding: ViewDataBinding): RecyclerView.ViewHolder(mDataBinding.root) {

        var mViewModel = TweetItemViewModel()

        init {
            mDataBinding.setVariable(BR.viewModel, mViewModel)
        }

        fun bind(tweet: Tweet?) {
            mViewModel.tweet = tweet!!
        }
    }

    class TweetAdapter(val mContext: Context, diffCallback: DiffUtil.ItemCallback<Tweet>): PagedListAdapter<Tweet, TweetViewHolder>(diffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {

            val dataBinding = DataBindingUtil.inflate<ListItemTweetBinding>(LayoutInflater.from(mContext), R.layout.list_item_tweet, parent, false)
            return TweetViewHolder(dataBinding)
        }

        override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
            val tweet: Tweet? = getItem(position)
            holder.bind(tweet)
        }

    }
}