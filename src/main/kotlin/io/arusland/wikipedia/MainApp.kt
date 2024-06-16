package io.arusland.wikipedia

import org.apache.commons.lang3.StringUtils.isNotBlank
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Thread.sleep
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.min

object MainApp {
    private val log = LoggerFactory.getLogger(MainApp::class.java)
    private const val POST_TIME_HOUR = 10
    private const val RETRY_TIMEOUT: Long = 60 * 1000
    private const val RETRY_ERROR = "Too Many Requests: retry after"
    private val TIME_ZONE = TimeZone.getTimeZone("GMT+3:00")
    private val exitOnDone = System.getProperty("exitOnDone", "true").toBoolean()

    @JvmStatic
    fun main(args: Array<String>) {
        log.info("Start application")
        val socksUsername = System.getProperty("java.net.socks.username")
        val socksPassword = System.getProperty("java.net.socks.password")

        if (isNotBlank(socksUsername) && isNotBlank(socksPassword)) {
            log.warn("using SOCKS: socksUsername: $socksUsername")
            Authenticator.setDefault(ProxyAuth(socksUsername, socksPassword))
        }

        val config = BotConfig.load("application.properties")
        val tgService = TelegramService(config)
        val parser = PageParser()
        val storage = UrlStorage(File("already_posted.txt")).load()
        var year = config.startYear
        var month = config.startMonth
        var sentImagesCount = 0

        while (true) {
            val now = getNow()
            year = min(year, now.year)

            if (year == now.year && month > now.monthValue) {
                month = now.monthValue
            }

            log.info("Parse new year {}, month: {}", year, month)

            val currentMonth = year == now.year && month == now.month.value

            if (currentMonth) {
                val nextTime = adjustPostTime(now)

                if (nextTime > now) {
                    sleepUntil(nextTime)
                }
            }

            val pods = parser.getPods(year, month).let {
                if (currentMonth) {
                    val last = min(now.dayOfMonth, it.size)
                    log.info("subList to {}, list size: {}", last, it.size)
                    it.subList(0, last)
                } else it
            }

            log.info("Found {} pods", pods.size)

            pods.forEach { pod ->
                if (!storage.contains(pod.url)) {
                    try {
                        sendImage(tgService, pod, config.channelId)
                        storage.add(pod.url)
                        sleep(config.postSleep)
                        sentImagesCount++
                    } catch (e: Exception) {
                        log.error("Posting failed with error '{}', year: {}, month: {}, pod: {}", e.message, year, month, pod, e)

                        tgService.sendAlertMessage(makeMarkDownMessage(pod, e))

                        throw e
                    }
                }
            }

            if (currentMonth) {
                if (exitOnDone) {
                    if (sentImagesCount == 0) {
                        log.warn("No images sent")
                        tgService.sendAlertMessage("Wikipedia pod: No images sent")
                    }
                    log.info("Exit program")
                    return
                }

                sleepUntil(adjustPostTime(getNow()).plusDays(1))

                getNow().let {
                    year = it.year
                    month = it.monthValue
                }
            } else {
                month++

                if (month > 12) {
                    year++
                    month = 1
                }
            }
        }
    }

    private fun adjustPostTime(now: OffsetDateTime): OffsetDateTime {
        return now.withHour(POST_TIME_HOUR)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
    }

    private fun sleepUntil(nextTime: OffsetDateTime) {
        val now = getNow()
        val diff = ChronoUnit.MILLIS.between(now, nextTime)

        if (diff > 0) {
            log.info("Sleep until {}, duration: {}", nextTime, Duration.ofMillis(diff))
            sleep(diff)
        }
    }

    private fun getNow() = OffsetDateTime.now(TIME_ZONE.toZoneId())

    private fun makeMarkDownMessage(pod: PodInfo, e: Exception): String {
        return """⛔️ Error sending pod
            |*${e.message}*
            |
            |```$pod```
        """.trimMargin()
    }

    private fun sendImage(tgService: TelegramService, pod: PodInfo, channelId: String, inRetry: Boolean = false, disableNotification: Boolean = false) {
        try {
            tgService.sendImageMessage(channelId, pod.url, pod.caption, disableNotification = disableNotification)
        } catch (e: Exception) {
            log.error("Error sending main image: {}", pod.url, e)
            if (!retryIf(inRetry, e, tgService, pod, channelId)) {
                if (e.message!!.contains("Bad Request:")) {
                    val errors = mutableListOf<Exception>()
                    listOf(pod.thumbUrl, pod.originalImageUrl).forEach { backupUrl ->
                        sleep(1000)
                        log.warn("Try to send backup version of image: {}", backupUrl)
                        try {
                            tgService.sendImageMessage(channelId, backupUrl, pod.caption, disableNotification = true)
                            errors.clear()
                        } catch (e2: Exception) {
                            errors.add(e2)
                            log.error("Error sending backup image: {}", backupUrl, e2)
                        }
                    }
                    errors.lastOrNull()?.let { throw it }
                } else {
                    throw e
                }
            }
        }
    }

    private fun retryIf(inRetry: Boolean,
                        error: Exception,
                        tgService: TelegramService,
                        pod: PodInfo,
                        channelId: String): Boolean {
        if (!inRetry && error.message!!.contains(RETRY_ERROR)) {
            log.warn("Retry after exception", error)
            tgService.sendAlertMessage("Wait before retry after error: " + error.message)
            sleep(RETRY_TIMEOUT)
            sendImage(tgService, pod, channelId, inRetry = true)

            return true
        }

        return false
    }

    private class ProxyAuth(socksUsername: String, socksPassword: String) : Authenticator() {
        private val auth: PasswordAuthentication = PasswordAuthentication(socksUsername, socksPassword.toCharArray())

        override fun getPasswordAuthentication(): PasswordAuthentication {
            return auth
        }
    }
}
