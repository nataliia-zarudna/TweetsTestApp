package com.nzarudna.tweetstestapp.ui.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.nzarudna.tweetstestapp.BR
import com.nzarudna.tweetstestapp.R
import com.nzarudna.tweetstestapp.TweetsTestApplication
import com.nzarudna.tweetstestapp.databinding.FragmentTimelineBinding
import com.nzarudna.tweetstestapp.databinding.ListItemTweetBinding
import com.nzarudna.tweetstestapp.model.tweet.Tweet
import com.nzarudna.tweetstestapp.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_timeline.view.*

/**
 * Fragment with twitter timeline
 */
class TimelineFragment : Fragment(), TimelineViewModel.TimelineViewModelObserver {

    lateinit var mViewModel: TimelineViewModel
    lateinit var mTweetAdapter: TweetAdapter

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fragmentViewBinding: FragmentTimelineBinding
                = DataBindingUtil.inflate(inflater, R.layout.fragment_timeline, container, false)
        val fragmentView = fragmentViewBinding.root

        mViewModel = ViewModelProviders.of(this).get(TimelineViewModel::class.java)
        (activity?.application as TweetsTestApplication).mAppComponent.inject(mViewModel)
        mViewModel.mObserver = this
        fragmentViewBinding.setVariable(BR.viewModel, mViewModel)

        mTweetAdapter = TweetAdapter(activity!!, object : DiffUtil.ItemCallback<Tweet>() {

            override fun areContentsTheSame(oldItem: Tweet?, newItem: Tweet?): Boolean {
                return oldItem?.text?.equals(newItem?.text) == true
            }

            override fun areItemsTheSame(oldItem: Tweet?, newItem: Tweet?): Boolean {
                return oldItem?.equals(newItem) == true
            }
        })

        fragmentView.tweetsSwipeRefreshLayout.setOnRefreshListener {

            fragmentView.tweetsSwipeRefreshLayout.isRefreshing = true
            loadTimeline()
        }
        fragmentView.tweetsRecyclerView.layoutManager = LinearLayoutManager(activity)
        fragmentView.tweetsRecyclerView.adapter = mTweetAdapter

        loadTimeline()

        return fragmentView
    }

    /**
     * Load current user's timeline
     */
    fun loadTimeline() {
        val liveDataList: LiveData<PagedList<Tweet>> = mViewModel.loadTimeline()
        liveDataList.observe(this, object : Observer<PagedList<Tweet>> {
            override fun onChanged(pagedList: PagedList<Tweet>?) {

                view?.tweetsSwipeRefreshLayout?.isRefreshing = false
                //mViewModel.onTimelineLoaded()

                mViewModel.mListCount = pagedList?.size ?: 0
                mTweetAdapter.submitList(pagedList)
            }
        })
    }

    override fun onLogout() {
        if (activity != null) {
            startActivity(LoginActivity.newIntent(activity!!))
            activity?.finish()
        }
    }

    override fun onError(e: Throwable) {
        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_timeline, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout_menu -> {
                mViewModel.logout()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    class TweetViewHolder(val mDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(mDataBinding.root) {

        var mViewModel = TweetItemViewModel()

        init {
            mDataBinding.setVariable(BR.viewModel, mViewModel)
        }

        fun bind(tweet: Tweet?) {
            mViewModel.mTweet = tweet!!
            mDataBinding.executePendingBindings()
        }
    }

    class TweetAdapter(val mContext: Context, diffCallback: DiffUtil.ItemCallback<Tweet>) : PagedListAdapter<Tweet, TweetViewHolder>(diffCallback) {

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