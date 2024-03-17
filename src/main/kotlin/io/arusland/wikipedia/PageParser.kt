package io.arusland.wikipedia

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.net.URL

class PageParser {
    private val log = LoggerFactory.getLogger(PageParser::class.java)

    fun getPods(year: Int, month: Int): List<PodInfo> {
        try {
            val pageUrl = URL(
                "https://ru.wikipedia.org/wiki/%D0%A8%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD:Potd/$year-" + monthToString(month)
            )
            log.info("Download page {}", pageUrl)
            val html = pageUrl.openStream().use { String(it.readBytes()) }

            return getPods(html, pageUrl)
        } catch (e: FileNotFoundException) {
            return emptyList()
        }
    }

    fun getPods(html: String, pageUrl: URL): List<PodInfo> {
        val doc = Jsoup.parse(html)

        return doc.select("figure")
            .map { elemToPod(it, pageUrl) }
            .filterNotNull()
    }

    private fun elemToPod(elem: Element, pageUrl: URL): PodInfo? {
        val url = pageUrl.protocol + ":" + elem.select("img")[0].attr("src")
        val thumbUrl = url.replace(THUMB_URL_PATTERN, "/1280px$1")
        val imageUrl = url.replace("/thumb/", "/").replace(CLEAN_URL_PATTERN, "")
        val imageNA = imageUrl.contains(IGNORE_IMAGE, true)

        if (!imageNA) {
            val caption = cleanCaption(elem.select("figcaption")[0], pageUrl)

            return PodInfo(url = imageUrl, thumbUrl = thumbUrl, caption = caption)
        }

        return null
    }

    private fun cleanCaption(elem: Element, pageUrl: URL): String {
        var html = elementAsHtml(elem)
        html = html.replace(DIV_PATTERN, "")
        html = html.replace(TITLE_PATTERN, ">")
        html = html.replace(CLASS_PATTERN, ">")
        html = html.replace("\"/wiki/", '"' + pageUrl.toFullHost() + "/wiki/")
        html = html.replace(SPAN_PATTERN, "")
        html = html.replace(SPAN2_PATTERN, "")
        html = html.replace(SMALL_PATTERN, "")
        html = html.replace(SUP_PATTERN_MAIN, "")
        html = html.replace(SUP_PATTERN, "")
        html = html.replace(NE_LINK, "")
        html = html.replace("\n", "")
        html = html.replace(BR_PATTERN, "\n")
        html = html.replace("&nbsp;", " ")

        return html
    }

    private fun elementAsHtml(elem: Element): String {
        // remain only text and links
        return cleanNodes(elem).html()
    }

    private fun allowedNodes(node: Node?) =
        node is Element && (node.tagName() == "a" || node.tagName() == "i") || node is TextNode

    private fun <T> cleanNodes(elem: T): T where T : Node {
        // remove edit links
        // <i><a href="/w/index.php?title=Gryphaea_arcuata&amp;action=edit&amp;redlink=1">Gryphaea arcuata</a></> => <i>Gryphaea arcuata</i>
        elem.childNodes().filter { child -> isActionEditLinkElem(child) || isSpanElem(child) }
            .forEach { child -> child.replaceWith(getChildIf(child)) }

        elem.childNodes()
            .map { cleanNodes(it) }
            .filter { !allowedNodes(it) }
            .toList()
            .forEach { node -> node.remove() }

        return elem
    }

    private fun getChildIf(node: Node): Node =
        if (isActionEditLinkElem(node) || isSpanElem(node))
            getChildIf(node.childNode(0))
        else
            node

    /**
     * Return true when child is a link to edit page
     *
     * Example: <a href="/w/index.php?title=Gryphaea_arcuata&amp;action=edit&amp;redlink=1" class="new">Gryphaea arcuata</a>
     */
    private fun isActionEditLinkElem(node: Node?) =
        node is Element && node.tagName() == "a"
                && node.attr("href").contains("action=edit")
                && node.childNodeSize() == 1

    private fun isSpanElem(child: Node?): Boolean =
        child is Element && child.tagName() == "span" && child.childNodeSize() == 1

    private fun monthToString(month: Int): String {
        return if (month < 10) "0$month" else "$month"
    }

    private companion object {
        const val IGNORE_IMAGE = "ImageNA.svg"
        val CLEAN_URL_PATTERN = Regex("/\\d+px[^/]+$")
        val THUMB_URL_PATTERN = Regex("/\\d+px([^/]+)$")
        val DIV_PATTERN = Regex("<div .+?</div>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
        val SPAN_PATTERN = Regex("<span .+?>")
        val SPAN2_PATTERN = Regex("</*span>")
        val SMALL_PATTERN = Regex("</*small>")
        val BR_PATTERN = Regex("<br>")
        val SUP_PATTERN = Regex("</*su(p|b)>")
        val SUP_PATTERN_MAIN = Regex("<su(p|b) .+?>")
        val TITLE_PATTERN = Regex(" title=.+?>")
        val CLASS_PATTERN = Regex(" class=.+?>")
        val NE_LINK = Regex("<a .+?redlink=1\"(.+?)</a>")
    }
}
