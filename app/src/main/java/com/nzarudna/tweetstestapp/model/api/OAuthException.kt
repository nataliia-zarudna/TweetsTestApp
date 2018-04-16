package com.nzarudna.tweetstestapp.model.api

/**
 * Created by Nataliia on 12.04.2018.
 */
class OAuthException: Exception {

    constructor(message: String?) : super(message)
    constructor(message: String?, e: Throwable?) : super(message, e)
}