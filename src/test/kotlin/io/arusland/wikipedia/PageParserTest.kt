package io.arusland.wikipedia

import io.arusland.wikipedia.io.arusland.wikipedia.PageParser
import org.junit.jupiter.api.Test

class PageParserTest {
    @Test
    fun testParser() {
        val images = PageParser().parse(2005, 3)

        images.forEach { println(it) }
    }
}
