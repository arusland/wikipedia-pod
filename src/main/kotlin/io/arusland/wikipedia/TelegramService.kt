package io.arusland.wikipedia

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import org.slf4j.LoggerFactory

class TelegramService(private val botConfig: BotConfig) {
    private val log = LoggerFactory.getLogger(TelegramService::class.java)

    fun sendImageMessage(chatId: String, imgUrl: String, caption: String, disableNotification: Boolean = false) {
        log.info("Send to {}, image {}, caption: {}", chatId, imgUrl, caption)

        val request = SendPhoto(chatId, imgUrl)
        request.caption(caption)
        request.parseMode(ParseMode.HTML)
        request.disableNotification(disableNotification)

        val api = TelegramBot(botConfig.botToken)
        val sendResponse = api.execute(request)

        if (!sendResponse.isOk) {
            throw Exception(sendResponse.description())
        }
    }

    fun sendAlertMessage(message: String) {
        log.warn("Send alert message: {}", message)

        val request = SendMessage(botConfig.alertChannelId, message)
        request.parseMode(ParseMode.Markdown)

        val api = TelegramBot(botConfig.botToken)
        val sendResponse = api.execute(request)

        if (!sendResponse.isOk) {
            log.error("ERROR SENDING ALERT: " + sendResponse.description())
        }
    }
}
