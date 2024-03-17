package io.arusland.wikipedia

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URL

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

    @Test
    fun testCase20240314() {
        val html = javaClass.getResource("/pages/page202403.html").readText()
        val pods = PageParser().getPods(
            html,
            URL("https://ru.wikipedia.org/wiki/%D0%A8%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD:Potd/2024-%D0%BC%D0%B0%D1%80%D1%82")
        )

        assertEquals(31, pods.size)
        assertEquals(
            "<a href=\"https://ru.wikipedia.org/wiki/%D0%A4%D0%BE%D1%81%D1%81%D0%B8%D0%BB%D0%B8%D0%B8\">Окаменелость</a> <a href=\"https://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D0%BB%D0%BB%D1%8E%D1%81%D0%BA%D0%B8\">моллюска</a> <i>Gryphaea arcuata</i>, известного как «ноготь дьявола», с разных ракурсов, <a href=\"https://ru.wikipedia.org/wiki/%D0%AE%D1%80%D1%81%D0%BA%D0%B8%D0%B9_%D0%BF%D0%B5%D1%80%D0%B8%D0%BE%D0%B4\">юрский период</a>, <a href=\"https://ru.wikipedia.org/wiki/%D0%93%D0%B5%D1%80%D0%BC%D0%B0%D0%BD%D0%B8%D1%8F\">Германия</a>",
            pods[13].caption
        )

        assertEquals(
            "Внутренний фасад одного из двориков <a href=\"https://en.wikipedia.org/wiki/Borujerdi_House\">дома Боруджерди</a>, построенного по проекту архитектора <a href=\"https://en.wikipedia.org/wiki/Ustad_Ali_Maryam\">Устада Али Марьяма</a> в 1857 году. <a href=\"https://ru.wikipedia.org/wiki/%D0%9A%D0%B0%D1%88%D0%B0%D0%BD_(%D0%98%D1%80%D0%B0%D0%BD)\">Кашан</a>, <a href=\"https://ru.wikipedia.org/wiki/%D0%98%D1%81%D1%84%D0%B0%D1%85%D0%B0%D0%BD_(%D0%BE%D1%81%D1%82%D0%B0%D0%BD)\">Исфахан</a>, <a href=\"https://ru.wikipedia.org/wiki/%D0%98%D1%80%D0%B0%D0%BD\">Иран</a>",
            pods[14].caption
        )

        assertEquals(
            "<a href=\"https://ru.wikipedia.org/wiki/%D0%A5%D0%BE%D1%80_(%D0%B0%D1%80%D1%85%D0%B8%D1%82%D0%B5%D0%BA%D1%82%D1%83%D1%80%D0%B0)\">Хор</a> и <a href=\"https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D1%82%D0%B0%D1%80%D1%8C\">алтарь</a> в <a href=\"https://de.wikipedia.org/wiki/St._Jakobus_der_%C3%84ltere_(D%C3%BClmen)\">церкви Святого Иакова</a>. <a href=\"https://de.wikipedia.org/wiki/Bauerschaft\">Сельское поселение</a> Веддерн, <a href=\"https://de.wikipedia.org/wiki/Kirchspiel_(D%C3%BClmen)\">Кирхшпиль</a>, <a href=\"https://ru.wikipedia.org/wiki/%D0%94%D1%8E%D0%BB%D1%8C%D0%BC%D0%B5%D0%BD\">Дюльмен</a>, <a href=\"https://ru.wikipedia.org/wiki/%D0%A1%D0%B5%D0%B2%D0%B5%D1%80%D0%BD%D1%8B%D0%B9_%D0%A0%D0%B5%D0%B9%D0%BD-%D0%92%D0%B5%D1%81%D1%82%D1%84%D0%B0%D0%BB%D0%B8%D1%8F\">Северный Рейн-Вестфалия</a>",
            pods[15].caption
        )

        assertEquals(
            "Часть витража в церкви <a href=\"https://ru.wikipedia.org/wiki/%D0%A1%D0%B2%D1%8F%D1%82%D0%BE%D0%B9_%D0%9F%D0%B0%D1%82%D1%80%D0%B8%D0%BA\">святого Патрика</a> в Джанкшен-Сити, <a href=\"https://ru.wikipedia.org/wiki/%D0%9E%D0%B3%D0%B0%D0%B9%D0%BE\">Огайо</a>",
            pods[16].caption
        )

        assertEquals("Цветы <i>Neillia affinis</i>", pods[19].caption)

        assertEquals(
            "«Перец № 30» — чёрно-белая фотография зелёного стручка <a href=\"https://ru.wikipedia.org/wiki/%D0%9F%D0%B5%D1%80%D0%B5%D1%86_%D1%81%D0%BB%D0%B0%D0%B4%D0%BA%D0%B8%D0%B9\">сладкого перца</a> работы <a href=\"https://ru.wikipedia.org/wiki/%D0%A3%D1%8D%D1%81%D1%82%D0%BE%D0%BD,_%D0%AD%D0%B4%D0%B2%D0%B0%D1%80%D0%B4\">Эдварда Уэстона</a>",
            pods[23].caption
        )
    }
}
