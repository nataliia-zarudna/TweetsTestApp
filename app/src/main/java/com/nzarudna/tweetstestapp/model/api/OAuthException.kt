package com.nzarudna.tweetstestapp.model.api

/**
 * Exception during authenticating
 */
class OAuthException: Exception {

    constructor(message: String?) : super(message)

    constructor(message: String?, e: Throwable?) : super(message, e)
}