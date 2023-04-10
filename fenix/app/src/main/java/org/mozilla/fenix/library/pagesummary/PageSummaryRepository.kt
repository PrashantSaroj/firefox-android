package org.mozilla.fenix.library.pagesummary

import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * repository class for getting data from openai apis.
 */
class PageSummaryRepository {
    private val apiKey = ""
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val httpRequest = HttpRequest()
    private val gson = Gson()

    // strategies for parsing html
    private val chunkingStrategy = ChunkingStrategy()
    private val headingParagraphStrategy = HeadingParagraphStrategy()

    private val headers = mapOf(
        "Content-Type" to "application/json",
        "Authorization" to "Bearer $apiKey",
    )

    /**
     * retrieve page summary using gpt-3.5.
     */
    suspend fun getPageSummary(summaryPageUrl: String): String? {
        return getPageContent(summaryPageUrl)?.let {
            val responseBody =
                httpRequest.postRequest(apiUrl, headers, createRequestBody(it)).body?.string()
            gson.fromJson(responseBody, GptResponse::class.java).choices[0].message.content
        }
    }

    private suspend fun getPageContent(summaryPageUrl: String): String? {
        return httpRequest.getRequest(summaryPageUrl).body?.string()
    }

    private fun createRequestBody(summaryPageHtml: String): String {
        val parsedHtml = parseHtml(summaryPageHtml)
        val message = JSONObject().apply {
            put("role", "user")
            put("content", "$PROMPT $parsedHtml")
        }
        val messageArray = JSONArray().apply {
            put(0, message)
        }
        return JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("temperature", 0.5)
            put("messages", messageArray)
        }.toString()
    }

    private fun parseHtml(htmlResponse: String): String {
        val primaryParsed = headingParagraphStrategy.parseHtml(htmlResponse)
        return if (primaryParsed.length < HeadingParagraphStrategy.MIN_THRESHOLD) {
            chunkingStrategy.parseHtml(htmlResponse)
        } else {
            primaryParsed
        }
    }


    companion object {
        private const val PROMPT =
            "Ignoring any Javascript or CSS code, give me detailed summary of this webpage: "
    }
}
