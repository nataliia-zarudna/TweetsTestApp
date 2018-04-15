package com.nzarudna.tweetstestapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import java.net.URLEncoder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap

/**
 * Created by Nataliia on 12.04.2018.
 */
@Singleton
class TwitterAuthManager @Inject constructor(val mSharedPreferences: SharedPreferences,
                                             val mContext: Context,
                                             val mTwitterApi: TwitterApi) {

    private val TAG = "TwitterAuthManager"

    companion object {
        private const val AUTH_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token"
        private const val AUTH_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token"
        private const val AUTHENTICATE_URL = "https://api.twitter.com/oauth/authenticate"

        const val POST = "POST"
        const val GET = "GET"

        private const val OAUTH_CALLBACK_HEADER = "oauth_callback"
        private const val OAUTH_TOKEN_HEADER = "oauth_token"
        private const val OAUTH_VERIFIER_HEADER = "oauth_verifier"
        private const val OAUTH_CONSUMER_KEY_HEADER = "oauth_consumer_key"
        private const val OAUTH_SIGNATURE_HEADER = "oauth_signature"
        private const val OAUTH_SIGNATURE_METHOD_HEADER = "oauth_signature_method"
        private const val OAUTH_SIGNATURE_METHOD_HMAC_SHA1 = "HMAC-SHA1"
        private const val OAUTH_TIMESTAMP_HEADER = "oauth_timestamp"
        private const val OAUTH_NONCE_HEADER = "oauth_nonce"
        private const val OAUTH_VERSION_HEADER = "oauth_version"
        private const val OAUTH_VERSION_VALUE = "1.0"

        const val OAUTH_TOKEN = "oauth_token"
        const val OAUTH_TOKEN_SECRET = "oauth_token_secret"
        const val USER_ID: String = "user_id"
        private const val SCREEN_NAME = "screen_name"
    }

    fun isAuthorized(): Boolean {
        return mSharedPreferences.contains(OAUTH_TOKEN)
                && mSharedPreferences.contains(OAUTH_TOKEN_SECRET)
                && mSharedPreferences.contains(USER_ID)
    }

    fun getUserID(): String? {
        if (isAuthorized()) {
            return mSharedPreferences.getString(USER_ID, "")
        } else {
            return null
        }
    }

    fun getRequestToken(obtainAuthTokenListener: ObtainAuthTokenListener) {

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders.put(OAUTH_CALLBACK_HEADER, BuildConfig.CALLBACK_URL)
        val oauthHeaders: String = getOAuthHeader(POST, AUTH_REQUEST_TOKEN_URL, additionalHeaders, null)

        Log.d(TAG, "Oaut header: " + oauthHeaders)

        mTwitterApi
                .getRequestToken(oauthHeaders)
                .enqueue(object : Callback<String> {

                    override fun onResponse(call: Call<String>?, response: retrofit2.Response<String>?) {
                        if (response?.isSuccessful == true && response.body() != null) {
                            val oauthToken = getResponseParamValue(response.body()!!, OAUTH_TOKEN_HEADER)
                            obtainAuthTokenListener.onObtainToken(oauthToken)
                        } else {
                            Log.e(TAG, response?.errorBody()?.string())
                            obtainAuthTokenListener.onError(OAuthException("Empty request token"))
                        }
                    }

                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        obtainAuthTokenListener.onError(OAuthException("Cannot obtain request token", t))
                    }

                })
    }

    fun getAuthToken(callbackResultURL: String, obtainAuthTokenListener: ObtainAuthTokenListener) {

        val urlParams = callbackResultURL.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

        val oauthToken: String? = getResponseParamValue(urlParams, OAUTH_TOKEN_HEADER)
        val oauthVerifier: String? = getResponseParamValue(urlParams, OAUTH_VERIFIER_HEADER)
        if (oauthToken == null || oauthVerifier == null) {
            obtainAuthTokenListener.onError(OAuthException("$OAUTH_TOKEN_HEADER or $OAUTH_VERIFIER_HEADER is empty"))
            return
        }

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders.put(OAUTH_TOKEN_HEADER, oauthToken)
        val oauthHeaders: String = getOAuthHeader(POST, AUTH_REQUEST_TOKEN_URL, additionalHeaders, null)

        mTwitterApi
                .getAuthToken(oauthHeaders, oauthVerifier)
                .enqueue(object : Callback<String> {

                    override fun onResponse(call: Call<String>?, response: retrofit2.Response<String>?) {
                        if (response?.isSuccessful == true && response.body() != null) {

                            val responseBody: String = response.body()!!
                            val userToken = getResponseParamValue(responseBody, OAUTH_TOKEN)
                            val userTokenSecret = getResponseParamValue(responseBody, OAUTH_TOKEN_SECRET)

                            mSharedPreferences
                                    .edit()
                                    .putString(OAUTH_TOKEN, userToken)
                                    .putString(OAUTH_TOKEN_SECRET, userTokenSecret)
                                    .putString(USER_ID, getResponseParamValue(responseBody, USER_ID))
                                    .putString(SCREEN_NAME, getResponseParamValue(responseBody, SCREEN_NAME))
                                    .apply()

                            obtainAuthTokenListener.onObtainToken(oauthVerifier)
                        } else {
                            Log.e(TAG, response?.errorBody()?.string())
                            obtainAuthTokenListener.onError(OAuthException("Empty request token"))
                        }
                    }

                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        obtainAuthTokenListener.onError(OAuthException("Cannot obtain oauth token", t))
                    }
                })
    }

    fun getOAuthHeader(method: String, url: String, additionalHeaders: Map<String, String>?,
                       requestParams: Map<String, String>?): String {

        val oauthHeaders = TreeMap<String, String>()

        oauthHeaders.put(OAUTH_CONSUMER_KEY_HEADER, BuildConfig.CONSUMER_KEY)
        oauthHeaders.put(OAUTH_SIGNATURE_METHOD_HEADER, OAUTH_SIGNATURE_METHOD_HMAC_SHA1)

        val timestamp = URLEncoder.encode((Date().time / 1000).toString())
        Log.d(TAG, "timestamp " + timestamp)
        oauthHeaders.put(OAUTH_TIMESTAMP_HEADER, timestamp)

        //val nonce = String(Base64.encode(Math.random().toString().toByteArray(), Base64.DEFAULT))
        val nonce = URLEncoder.encode((Date().time / 1000 * 2).toString())
        Log.d(TAG, "nonce " + nonce)
        oauthHeaders.put(OAUTH_NONCE_HEADER, nonce.trim())

        oauthHeaders.put(OAUTH_VERSION_HEADER, OAUTH_VERSION_VALUE)

        if (mSharedPreferences.contains(OAUTH_TOKEN)) {
            val oauthToken: String = mSharedPreferences.getString(OAUTH_TOKEN, "")
            oauthHeaders.put(OAUTH_TOKEN_HEADER, oauthToken)
        }

        if (additionalHeaders != null) {
            oauthHeaders.putAll(additionalHeaders);
        }

        val cloneRequestParams = HashMap<String, String>()
        if (requestParams != null) {
            requestParams.forEach({ (key, value) ->
                cloneRequestParams.put(key, URLEncoder.encode(value))
            })
        }

        var signature = ""
        try {
            signature = getSignature(method, url, oauthHeaders, cloneRequestParams)

        } catch (e: NoSuchAlgorithmException) {
            throw OAuthException("Cannot create oauth signature", e)
        } catch (e: InvalidKeyException) {
            throw OAuthException("Cannot create oauth signature", e)
        }

        oauthHeaders.put(OAUTH_SIGNATURE_HEADER, signature.trim())

        val encodedHeaders = HashMap<String, String>()
        for (header in oauthHeaders.keys) {
            val paramValue = oauthHeaders[header]
            encodedHeaders.put(header, URLEncoder.encode(paramValue))
        }

        return "OAuth " + glueParams(encodedHeaders)
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    private fun getSignature(method: String, baseURL: String,
                             headers: Map<String, String>,
                             params: Map<String, String>?): String {

        val allParams = TreeMap(headers)
        if (params != null) {
            allParams.putAll(params)
        }

        val signatureBaseString = StringBuilder()

        signatureBaseString
                .append(method.toUpperCase())
                .append("&")
                .append(URLEncoder.encode(baseURL))
                .append('&')

        val paramsSignaturePart = StringBuilder()
        for (key in allParams.keys) {
            paramsSignaturePart
                    .append(key)
                    .append('=')

            val value = if (OAUTH_CALLBACK_HEADER == key) URLEncoder.encode(allParams[key]) else allParams[key]
            paramsSignaturePart.append(value)
                    .append('&')
        }
        paramsSignaturePart.deleteCharAt(paramsSignaturePart.length - 1)

        val encodedParamsSignaturePart = URLEncoder.encode(paramsSignaturePart.toString())
        signatureBaseString.append(encodedParamsSignaturePart)

        val mac = Mac.getInstance(OAUTH_SIGNATURE_METHOD_HMAC_SHA1)

        val oauthSecret: String = mSharedPreferences.getString(OAUTH_TOKEN_SECRET, "")
        val signingKey = BuildConfig.CONSUMER_SECRET_KEY + "&" + oauthSecret

        val secretKeySpec = SecretKeySpec(signingKey.toByteArray(), mac.algorithm)
        mac.init(secretKeySpec)

        val digest = mac.doFinal(signatureBaseString.toString().toByteArray())
        val signature = Base64.encode(digest, Base64.DEFAULT)

        return String(signature)
    }

    fun getAuthenticateURL(oauthToken: String): String {
        return "$AUTHENTICATE_URL?$OAUTH_TOKEN_HEADER=$oauthToken"
    }

    private fun glueParams(params: Map<String, String>): String {
        val builder = StringBuilder()

        for (key in params.keys) {
            builder
                    .append(key)
                    .append('=')
                    .append('"')
                    .append(params[key])
                    .append('"')
                    .append(',')
        }
        builder.deleteCharAt(builder.length - 1)

        return builder.toString()
    }

    fun getResponseParamValue(response: String, param: String): String? {
        val params = response.split("\\&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in params.indices) {
            val paramTokens = params[i].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (param == paramTokens[0]) {
                return paramTokens[1]
            }
        }
        return null
    }

    interface ObtainAuthTokenListener {

        fun onObtainToken(authToken: String?)

        fun onError(e: Throwable)
    }
}