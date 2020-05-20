package io.arusland.wikipedia.io.arusland.wikipedia

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.net.URL

class PageParser {
    private val log = LoggerFactory.getLogger(PageParser::class.java)

    fun parse(year: Int, month: Int): List<String> {
        val pageUrl = URL("https://ru.wikipedia.org/wiki/%D0%A8%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD:Potd/$year-" + monthToString(month))
        log.info("Download page $pageUrl")

        val html = pageUrl.openStream().use { String(it.readBytes()) }
        val doc = Jsoup.parse(html)

        return doc.select(".thumbinner")
                .map { elemToPod(it, pageUrl) }
                .filter { !it.contains(IGNORE_IMAGE, true) }
    }

    private fun elemToPod(elem: Element, pageUrl: URL): String {
        val url = pageUrl.protocol + ":" + elem.select("img.thumbimage")[0].attr("src")

        return url.replace("/thumb/", "/")
                .replace(Regex("/220px.+"), "")
    }

    private fun monthToString(month: Int): String {
        return if (month < 10) "0$month" else "$month"
    }

    companion object {
        const val IGNORE_IMAGE = "ImageNA.svg"
    }
}
