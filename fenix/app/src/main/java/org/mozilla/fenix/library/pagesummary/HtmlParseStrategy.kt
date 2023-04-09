package org.mozilla.fenix.library.pagesummary

import androidx.core.text.HtmlCompat
import org.jsoup.Jsoup
import org.mozilla.fenix.library.pagesummary.HtmlParseStrategy.Companion.MAX_TOKENS

/**
 * strategy for parsing html.
 */
interface HtmlParseStrategy {
    fun parseHtml(htmlResponse: String): String

    companion object {
        /**
         * limit the number of tokens sent to api.
         */
        const val MAX_TOKENS = 4000
    }
}

/**
 * extract contents of html page by content of <p> and <h1> tags.
 */
class HeadingParagraphStrategy : HtmlParseStrategy {
    override fun parseHtml(htmlResponse: String): String {
        val document = Jsoup.parse(htmlResponse)
        val sb = StringBuilder()

        document.select("h1, p").forEach {
            sb.append(it.text())
        }
        return sb.toString().let {
            if (it.length > MAX_TOKENS) {
                ChunkingStrategy.createAndSelectChunks(it)
            } else {
                it
            }
        }
    }

    companion object {
        /**
         * if the parsed response is less than this threshold then don't
         * use this strategy.
         */
        const val MIN_THRESHOLD = 100
    }
}

/**
 * remove all html tags and pick some chunks.
 */
class ChunkingStrategy : HtmlParseStrategy {
    override fun parseHtml(htmlResponse: String): String {
        return HtmlCompat.fromHtml(htmlResponse, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .let { createAndSelectChunks(it) }
    }

    companion object {

        fun createAndSelectChunks(str: String) =
            if (str.length < MAX_TOKENS) {
                str
            } else {
                // since lots of html and js content will be at top of page
                // break page into parts and pick non adjacent 4 parts

                // this list will have at least 5 elements
                val chunkedParts = str.chunked(MAX_TOKENS / 4)

                listOf(
                    chunkedParts.first(),
                    chunkedParts[chunkedParts.size / 4],
                    chunkedParts[3 * chunkedParts.size / 4],
                    chunkedParts.last(),
                ).joinToString(" ")
            }
    }

}
