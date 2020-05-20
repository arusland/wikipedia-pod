package io.arusland.wikipedia

import org.junit.jupiter.api.Test

class PageParserTest {
    @Test
    fun testGetPods() {
        val images = PageParser().getPods(2012, 2)

        images.forEach {
            println(it.caption)
            println()
        }
    }
}
