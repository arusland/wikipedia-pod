package io.arusland.wikipedia

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendPhoto
import org.slf4j.LoggerFactory

class TelegramService(val botConfig: BotConfig) {
    private val log = LoggerFactory.getLogger(TelegramService::class.java)

    fun sendImageMessage(chatId: String, imgUrl: String, caption: String) {
        log.info("Send to {}, image {}, caption: {}", chatId, imgUrl, caption)

        val request = SendPhoto(chatId, imgUrl)
        request.caption(caption)
        request.parseMode(ParseMode.HTML)

        val api = TelegramBot(botConfig.botToken)
        val sendResponse = api.execute(request)

        if (!sendResponse.isOk) {
            throw Exception(sendResponse.description())
        }
    }
}
