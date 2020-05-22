package io.arusland.wikipedia

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import org.slf4j.LoggerFactory
import java.io.*
import java.time.LocalDate
import java.util.*

/**
 * Simple bot configuration
 */
class BotConfig private constructor(prop: Properties) {
    private val log = LoggerFactory.getLogger(BotConfig::class.java)
    private val props: Properties = Validate.notNull(prop, "props")

    val botName: String
        get() = getProperty("bot.name")

    val botToken: String
        get() = getProperty("bot.token")

    val postSleep: Long
        get() = getProperty("post.sleep", "1000").toLong()

    val channelId: String
        get() = getProperty("channel.chatId")

    val alertChannelId: String
        get() = getProperty("alert.chatId")

    val startYear: Int
        get() = getProperty("start.year", LocalDate.now().year.toString()).toInt()

    val startMonth: Int
        get() = getProperty("start.month", LocalDate.now().monthValue.toString()).toInt()

    private fun getProperty(key: String): String {
        return Validate.notNull(props.getProperty(key),
                "Configuration not found for key: $key")
    }

    private fun getProperty(key: String, defValue: String): String {
        val value = props.getProperty(key)

        return StringUtils.defaultString(value, defValue)
    }

    private fun setLongList(propName: String, list: List<Long>) {
        props.setProperty(propName, list.joinToString(","))
    }

    private fun getLongList(propName: String): List<Long> {
        return getProperty(propName, "")
                .split(",".toRegex())
                .filter { it.isNotBlank() }
                .map { it.trim().toLong() }
                .toList()
    }

    fun save(fileName: String) {
        FileOutputStream(fileName).use { output -> props.store(output, "Wikipeda Pod") }
    }

    companion object {
        fun load(fileName: String, throwOnError: Boolean = true): BotConfig {
            val props = Properties()

            try {
                val file = File(fileName).canonicalFile
                FileInputStream(file).use { input -> props.load(input) }
            } catch (e: Exception) {
                if (throwOnError) {
                    throw RuntimeException(e)
                }
            }

            return BotConfig(props)
        }
    }
}
