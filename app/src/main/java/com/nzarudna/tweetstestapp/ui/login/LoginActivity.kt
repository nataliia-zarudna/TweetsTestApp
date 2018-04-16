package com.nzarudna.tweetstestapp.ui.login

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import com.nzarudna.tweetstestapp.ui.SingleFragmentActivity

/**
 * Login activity
 */
class LoginActivity : SingleFragmentActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    override fun getFragmentInstance(): Fragment {
        return LoginFragment.newInstance()
    }
}