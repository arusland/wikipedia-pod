package io.arusland.wikipedia

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Thread.sleep
import java.net.Authenticator
import java.net.PasswordAuthentication

object MainApp {
    private val log = LoggerFactory.getLogger(MainApp::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val socksUsername = System.getProperty("java.net.socks.username")
        val socksPassword = System.getProperty("java.net.socks.password")

        if (StringUtils.isNotBlank(socksUsername) && StringUtils.isNotBlank(socksPassword)) {
            log.warn("using SOCKS: socksUsername: $socksUsername")
            Authenticator.setDefault(ProxyAuth(socksUsername, socksPassword))
        }

        val config = BotConfig.load("application.properties")
        val tgService = TelegramService(config)
        val parser = PageParser()
        val pods = parser.getPods(2005, 9)
        val storage = UrlStorage(File("already_posted.txt")).load()

        pods.forEach { pod ->
            if (!storage.contains(pod.url)) {
                try {
                    sendImage(tgService, pod, config.channelId)
                    storage.add(pod.url)
                    sleep(config.postSleep)
                } catch (e: Exception) {
                    log.error("Error '{}' of posting pod: {}", e.message, pod)
                    throw e
                }
            }
        }
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
