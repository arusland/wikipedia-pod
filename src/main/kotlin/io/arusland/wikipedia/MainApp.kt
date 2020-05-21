package io.arusland.wikipedia

import org.apache.commons.lang3.StringUtils.isNotBlank
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Thread.sleep
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


object MainApp {
    private val log = LoggerFactory.getLogger(MainApp::class.java)
    private const val POST_TIME_HOUR = 10

    @JvmStatic
    fun main(args: Array<String>) {
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
        var year = 2015
        var month = 7

        while (true) {
            var now = LocalDateTime.now()
            log.info("Parse new year {}, month: {}", year, month)
            val currentMonth = year >= now.year && month >= now.month.value

            if (currentMonth) {
                val nextTime = now.withHour(POST_TIME_HOUR).withMinute(0)

                if (nextTime > now) {
                    sleepUntil(nextTime)
                }
            }

            val pods = parser.getPods(year, month).let {
                if (currentMonth) {
                    val last = Math.min(now.dayOfMonth, it.size)
                    log.info("subList to {}, list size: {}", last, it.size)
                    it.subList(0, last)
                } else it
            }

            var atLeastOne = false

            pods.forEach { pod ->
                if (!storage.contains(pod.url)) {
                    try {
                        sendImage(tgService, pod, config.channelId)
                        storage.add(pod.url)
                        sleep(config.postSleep)
                        atLeastOne = true
                    } catch (e: Exception) {
                        log.error("Posting failed with error '{}', year: {}, month: {}, pod: {}", e.message, year, month, pod)

                        tgService.sendAlertMessage(makeMarkDownMessage(pod, e))

                        throw e
                    }
                }
            }

            if (currentMonth) {
                now = LocalDateTime.now()
                month = now.month.value
                year = now.year

                if (!atLeastOne) {
                    sleepUntil(now.withHour(POST_TIME_HOUR).withMinute(0).plusDays(1))
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

    private fun sleepUntil(nextTime: LocalDateTime) {
        val now = LocalDateTime.now()
        val diff = ChronoUnit.NANOS.between(now, nextTime)

        if (diff > 0) {
            log.info("Sleep until {}", nextTime)
            sleep(diff)
        }
    }

    private fun makeMarkDownMessage(pod: PodInfo, e: Exception): String {
        return """⛔️ Error sending pod
            |*${e.message}*
            |
            |```$pod```
        """.trimMargin()
    }

    private fun sendImage(tgService: TelegramService, pod: PodInfo, channelId: String) {
        try {
            tgService.sendImageMessage(channelId, pod.url, pod.caption)
        } catch (e: Exception) {
            if (e.message!!.contains("Bad Request:")) {
                log.warn("Try to post thumb version of image: {}", pod.thumbUrl)

                tgService.sendImageMessage(channelId, pod.thumbUrl, pod.caption)
            } else {
                throw e
            }
        }
    }

    private class ProxyAuth(socksUsername: String, socksPassword: String) : Authenticator() {
        private val auth: PasswordAuthentication = PasswordAuthentication(socksUsername, socksPassword.toCharArray())

        override fun getPasswordAuthentication(): PasswordAuthentication {
            return auth
        }
    }
}
