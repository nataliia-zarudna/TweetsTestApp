package com.nzarudna.tweetstestapp

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Singleton
import javax.xml.transform.ErrorListener
import kotlin.collections.HashMap

/**
 * Created by Nataliia on 12.04.2018.
 */
@Singleton
class TwitterAuthManager {

    private val TAG = "TwitterAuthManager"

    private val AUTH_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token"
    private val AUTH_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token"

    private val OAUTH_CALLBACK_HEADER = "oauth_callback"

    fun getRequestToken(context: Context, obtainAuthTokenListener: ObtainAuthTokenListener) {

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders.put(OAUTH_CALLBACK_HEADER, BuildConfig.CALLBACK_URL);

        performAuthRequest(context, AUTH_REQUEST_TOKEN_URL, additionalHeaders, null,
                Response.Listener<String> { response ->

                    val oauthToken = getResponseParamValue(response, "oauth_token")
                    obtainAuthTokenListener.onObtainToken(oauthToken)
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "onErrorResponse " + String(error.networkResponse.data, StandardCharsets.UTF_8), error)

                    obtainAuthTokenListener.onError(error)
                })
    }

    fun getAuthToken(context: Context, oauthToken: String, oauthVerifier: String, obtainAuthTokenListener: ObtainAuthTokenListener) {

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders.put("oauth_token", oauthToken)

        val requestParams = HashMap<String, String>()
        additionalHeaders.put("oauth_verifier", oauthVerifier)

        performAuthRequest(context, AUTH_ACCESS_TOKEN_URL, additionalHeaders, requestParams,
                Response.Listener<String> { response ->

                    val authVerifier = getResponseParamValue(response, "oauth_verifier")
                    obtainAuthTokenListener.onObtainToken(authVerifier)
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "onErrorResponse " + String(error.networkResponse.data, StandardCharsets.UTF_8), error)

                    obtainAuthTokenListener.onError(error)
                })
    }

    private fun performAuthRequest(context: Context, url: String, additionalHeaders: Map<String, String>?,
                           requestParams: Map<String, String>?,
                           listener: Response.Listener<String>, errorListener: Response.ErrorListener
            /*oauthToken: String?, oauthVerifier: String?*/) {

        //val url = if (oauthToken == null) AUTH_REQUEST_TOKEN_URL else AUTH_ACCESS_TOKEN_URL

        val request = object : StringRequest(Request.Method.POST, url, listener, errorListener) {

            override fun getHeaders(): MutableMap<String, String> {

                val oauthHeaders = TreeMap<String, String>()

                oauthHeaders.put("oauth_consumer_key", BuildConfig.CONSUMER_KEY)
                oauthHeaders.put("oauth_signature_method", "HMAC-SHA1")
                oauthHeaders.put("oauth_timestamp", URLEncoder.encode((Date().time / 1000).toString()))

                val nonce = String(Base64.encode(Math.random().toString().toByteArray(), Base64.DEFAULT))
                oauthHeaders.put("oauth_nonce", nonce.trim { it <= ' ' })

                oauthHeaders.put("oauth_version", "1.0")

                /*if (oauthToken != null) {
                    oauthHeaders.put("oauth_token", oauthToken)
                } else {
                    oauthHeaders.put("oauth_callback", BuildConfig.CALLBACK_URL)
                }*/

                var signature = ""
                try {
                    signature = getSignature("POST", url, oauthHeaders, params, BuildConfig.CONSUMER_SECRET_KEY, null)

                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                } catch (e: InvalidKeyException) {
                    e.printStackTrace()
                }

                oauthHeaders.put("oauth_signature", signature.trim { it <= ' ' })

                val encodedHeaders = HashMap<String, String>()
                for (header in oauthHeaders.keys) {
                    val paramValue = oauthHeaders[header]
                    encodedHeaders.put(header, URLEncoder.encode(paramValue))
                }

                val headers = HashMap<String, String>()
                headers.put("Authorization", "OAuth " + glueParams(encodedHeaders))

                return headers
            }

            override fun getParams(): Map<String, String> {

                /*val params = HashMap<String, String>()
                if (oauthVerifier != null) {
                    params.put("oauth_verifier", oauthVerifier)
                }
                return params*/
                if (requestParams != null) {
                    return requestParams
                } else {
                    return super.getParams()
                }
            }
        }

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    private fun getSignature(method: String, baseURL: String, headers: Map<String, String>, params: Map<String, String>, consumerSecret: String, oauthSecret: String?): String {
        var oauthSecret = oauthSecret

        val allParams = TreeMap(headers)
        allParams.putAll(params)

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

        val mac = Mac.getInstance("HMAC-SHA1")

        if (oauthSecret == null) {
            oauthSecret = ""
        }
        val signingKey = consumerSecret + "&" + oauthSecret

        val secretKeySpec = SecretKeySpec(signingKey.toByteArray(), mac.algorithm)
        mac.init(secretKeySpec)

        val digest = mac.doFinal(signatureBaseString.toString().toByteArray())
        val signature = Base64.encode(digest, Base64.DEFAULT)

        return String(signature)
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