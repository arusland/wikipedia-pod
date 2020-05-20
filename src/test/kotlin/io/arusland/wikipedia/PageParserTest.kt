package io.arusland.wikipedia

import org.junit.jupiter.api.Test

class PageParserTest {
    @Test
    fun testGetPods() {
        val images = PageParser().getPods(2005, 8)

        images.forEach {
            println(it.url)
            println(it.thumbUrl)
            println(it.caption)
            println()
        }
    }
}
