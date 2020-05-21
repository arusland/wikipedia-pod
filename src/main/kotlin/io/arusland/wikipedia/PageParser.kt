package io.arusland.wikipedia

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.net.URL

class PageParser {
    private val log = LoggerFactory.getLogger(PageParser::class.java)

    fun getPods(year: Int, month: Int): List<PodInfo> {
        try {
            val pageUrl = URL("https://ru.wikipedia.org/wiki/%D0%A8%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD:Potd/$year-" + monthToString(month))
            log.info("Download page {}", pageUrl)

            val html = pageUrl.openStream().use { String(it.readBytes()) }
            val doc = Jsoup.parse(html)

            return doc.select(".thumbinner")
                    .map { elemToPod(it, pageUrl) }
                    .filterNotNull()
        } catch (e: FileNotFoundException) {
            return emptyList()
        }
    }

    private fun elemToPod(elem: Element, pageUrl: URL): PodInfo? {
        val url = pageUrl.protocol + ":" + elem.select("img.thumbimage")[0].attr("src")
        val thumbUrl = url.replace(THUMB_URL_PATTERN, "/1280px$1")
        val imageUrl = url.replace("/thumb/", "/").replace(CLEAN_URL_PATTERN, "")
        val imageNA = imageUrl.contains(IGNORE_IMAGE, true)

        if (!imageNA) {
            val caption = cleanCaption(elem.select(".thumbcaption")[0], pageUrl)

            return PodInfo(url = imageUrl, thumbUrl = thumbUrl, caption = caption)
        }

        return null
    }

    private fun cleanCaption(elem: Element, pageUrl: URL): String {
        var html = elem.html()
        html = html.replace(DIV_PATTERN, "")
        html = html.replace(TITLE_PATTERN, ">")
        html = html.replace(CLASS_PATTERN, ">")
        html = html.replace("\"/wiki/", '"' + pageUrl.toFullHost() + "/wiki/")
        html = html.replace(SPAN_PATTERN, "")
        html = html.replace(SPAN2_PATTERN, "")
        html = html.replace(SMALL_PATTERN, "")
        html = html.replace(NE_LINK, "$1")
        html = html.replace(SUP_PATTERN, "$1")
        html = html.replace("\n", "")
        html = html.replace(BR_PATTERN, "\n")
        html = html.replace("&nbsp;", " ")

        return html
    }

    private fun monthToString(month: Int): String {
        return if (month < 10) "0$month" else "$month"
    }

    companion object {
        const val IGNORE_IMAGE = "ImageNA.svg"
        val CLEAN_URL_PATTERN = Regex("/\\d+px[^/]+$")
        val THUMB_URL_PATTERN = Regex("/\\d+px([^/]+)$")
        val DIV_PATTERN = Regex("<div .+?</div>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
        val SPAN_PATTERN = Regex("<span .+?>")
        val SPAN2_PATTERN = Regex("</*span>")
        val SMALL_PATTERN = Regex("</*small>")
        val BR_PATTERN = Regex("<br>")
        val SUP_PATTERN = Regex("<sup>(.+)</sup>")
        val TITLE_PATTERN = Regex(" title=.+?>")
        val CLASS_PATTERN = Regex(" class=.+?>")
        val NE_LINK = Regex("<a .+?redlink=1\">(.+?)</a>")
    }
}
