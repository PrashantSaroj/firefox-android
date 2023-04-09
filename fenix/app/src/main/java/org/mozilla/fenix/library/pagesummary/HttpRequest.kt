package org.mozilla.fenix.library.pagesummary


import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * helper class for making http requests.
 */
class HttpRequest {

    /**
     * make get request to [url].
     */
    suspend fun getRequest(url: String): Response {
        val request = Request.Builder().url(url).get().build()
        return client.newCall(request).execute()
    }

    /**
     * make post request to [url].
     */
    suspend fun postRequest(url: String, headers: Map<String, String>, jsonBody: String): Response {
        val requestBody = jsonBody.toRequestBody(defaultMediaType.toMediaType())

        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(headers))
            .post(requestBody)
            .build()

        return client.newCall(request).execute()
    }

    private fun mapToHeaders(headersMap: Map<String, String>): Headers {
        val headersBuilder = Headers.Builder()
        headersMap.forEach { (key, value) ->
            headersBuilder.add(key, value)
        }
        return headersBuilder.build()
    }

    companion object {
        private const val defaultTimeout = 60L

        // singleton for performance reason
        private val client = OkHttpClient().newBuilder()
            .readTimeout(defaultTimeout, TimeUnit.SECONDS)
            .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
            .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
            .build()


        private const val defaultMediaType = "application/json; charset=utf-8"

    }
}
