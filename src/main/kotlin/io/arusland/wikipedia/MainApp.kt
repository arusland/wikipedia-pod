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
        val pods = parser.getPods(2005, 6)
        val storage = UrlStorage(File("already_posted.txt")).load()

        pods.forEach { pod ->
            if (!storage.contains(pod.url)) {
                try {
                    tgService.sendImageMessage(config.channelId, pod.url, pod.caption)
                    storage.add(pod.url)
                    sleep(config.postSleep)
                } catch (e: Exception) {
                    log.error("Error '{}' of posting pod: {}", e.message, pod)
                    throw e
                }
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
