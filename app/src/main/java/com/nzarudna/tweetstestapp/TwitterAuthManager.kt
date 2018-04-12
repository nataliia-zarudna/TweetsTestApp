package com.nzarudna.tweetstestapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import retrofit2.Call
import retrofit2.Callback
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

    private val AUTH_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token"
    private val AUTH_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token"
    private val AUTHENTICATE_URL = "https://api.twitter.com/oauth/authenticate"

    private val OAUTH_CALLBACK_HEADER = "oauth_callback"
    private val OAUTH_TOKEN_HEADER = "oauth_token"
    private val OAUTH_VERIFIER_HEADER = "oauth_verifier"
    private val OAUTH_CONSUMER_KEY_HEADER = "oauth_consumer_key"
    private val OAUTH_SIGNATURE_HEADER = "oauth_signature"
    private val OAUTH_SIGNATURE_METHOD_HEADER = "oauth_signature_method"
    private val OAUTH_SIGNATURE_METHOD_HMAC_SHA1 = "HMAC-SHA1"
    private val OAUTH_TIMESTAMP_HEADER = "oauth_timestamp"
    private val OAUTH_NONCE_HEADER = "oauth_nonce"
    private val OAUTH_VERSION_HEADER = "oauth_version"
    private val OAUTH_VERSION_VALUE = "1.0"

    private val OAUTH_TOKEN_PARAM = "oauth_token"
    private val OAUTH_TOKEN_SECRET_PARAM = "oauth_token_secret"
    private val USER_ID_PARAM = "user_id"
    private val SCREEN_NAME_PARAM = "screen_name"

    fun getRequestToken(obtainAuthTokenListener: ObtainAuthTokenListener) {

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders.put(OAUTH_CALLBACK_HEADER, BuildConfig.CALLBACK_URL)

        val oauthHeaders: String = getOAuthHeader(AUTH_REQUEST_TOKEN_URL, additionalHeaders, null)
        val requestToken: Call<String> = mTwitterApi.getRequestToken(oauthHeaders)
        requestToken.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>?, response: retrofit2.Response<String>?) {
                val oauthToken = getResponseParamValue(response?.body(), OAUTH_TOKEN_HEADER)
                obtainAuthTokenListener.onObtainToken(oauthToken)
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        /*performSignedRequest(AUTH_REQUEST_TOKEN_URL, additionalHeaders, null,
                Response.Listener<String> { response ->

                    val oauthToken = getResponseParamValue(response, OAUTH_TOKEN_HEADER)
                    obtainAuthTokenListener.onObtainToken(oauthToken)
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "onErrorResponse " + String(error.networkResponse.data, StandardCharsets.UTF_8), error)

                    obtainAuthTokenListener.onError(error)
                })*/
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

        val requestParams = HashMap<String, String>()
        additionalHeaders.put(OAUTH_VERIFIER_HEADER, oauthVerifier)

        performSignedRequest(AUTH_ACCESS_TOKEN_URL, additionalHeaders, requestParams,
                Response.Listener<String> { response ->

                    val authVerifier = getResponseParamValue(response, OAUTH_TOKEN_SECRET_PARAM)
                    obtainAuthTokenListener.onObtainToken(authVerifier)

                    mSharedPreferences
                            .edit()
                            .putString(OAUTH_TOKEN_PARAM, oauthToken)
                            .putString(OAUTH_TOKEN_SECRET_PARAM, oauthVerifier)
                            .putString(USER_ID_PARAM, getResponseParamValue(response, USER_ID_PARAM))
                            .putString(SCREEN_NAME_PARAM, getResponseParamValue(response, SCREEN_NAME_PARAM))
                            .apply()
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "onErrorResponse " + String(error.networkResponse.data, StandardCharsets.UTF_8), error)

                    obtainAuthTokenListener.onError(error)
                })
    }

    fun performSignedRequest(url: String, additionalHeaders: Map<String, String>?,
                             requestParams: Map<String, String>?,
                             listener: Response.Listener<String>, errorListener: Response.ErrorListener) {

        val request = object : StringRequest(Request.Method.POST, url, listener, errorListener) {

            override fun getHeaders(): MutableMap<String, String> {

                val authHeader = getOAuthHeader(url, additionalHeaders, requestParams)
                val headers = HashMap<String, String>()
                headers.put("Authorization", authHeader)
                return headers
            }

            override fun getParams(): Map<String, String> {
                if (requestParams != null) {
                    return requestParams
                } else {
                    return HashMap<String, String>()
                }
            }
        }

        val requestQueue = Volley.newRequestQueue(mContext)
        requestQueue.add(request)
    }

    private fun getOAuthHeader(url: String, additionalHeaders: Map<String, String>?,
                               requestParams: Map<String, String>?): String {

        val oauthHeaders = TreeMap<String, String>()

        oauthHeaders.put(OAUTH_CONSUMER_KEY_HEADER, BuildConfig.CONSUMER_KEY)
        oauthHeaders.put(OAUTH_SIGNATURE_METHOD_HEADER, OAUTH_SIGNATURE_METHOD_HMAC_SHA1)
        oauthHeaders.put(OAUTH_TIMESTAMP_HEADER, URLEncoder.encode((Date().time / 1000).toString()))

        val nonce = String(Base64.encode(Math.random().toString().toByteArray(), Base64.DEFAULT))
        oauthHeaders.put(OAUTH_NONCE_HEADER, nonce.trim { it <= ' ' })

        oauthHeaders.put(OAUTH_VERSION_HEADER, OAUTH_VERSION_VALUE)

        if (additionalHeaders != null) {
            oauthHeaders.putAll(additionalHeaders);
        }

        var signature = ""
        try {
            signature = getSignature("POST", url, oauthHeaders, requestParams)

        } catch (e: NoSuchAlgorithmException) {
            throw OAuthException("Cannot create oauth signature", e)
        } catch (e: InvalidKeyException) {
            throw OAuthException("Cannot create oauth signature", e)
        }

        oauthHeaders.put(OAUTH_SIGNATURE_HEADER, signature.trim { it <= ' ' })

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

        val oauthSecret: String = mSharedPreferences.getString(OAUTH_TOKEN_SECRET_PARAM, "")
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