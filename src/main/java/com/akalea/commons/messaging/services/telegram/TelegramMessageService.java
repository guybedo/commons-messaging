package com.akalea.commons.messaging.services.telegram;

import java.util.List;
import java.util.stream.Collectors;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgResult;
import com.akalea.commons.messaging.services.ServiceBase;
import com.google.common.collect.Lists;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

public class TelegramMessageService extends ServiceBase {

    private TelegramBot bot;

    public void connect() {
        this.bot =
            new TelegramBot.Builder(credentials.getToken()).build();
    }

    @Override
    public MsgResult sendMessage(Msg msg) {
        SendMessage request =
            new SendMessage(msg.getRecipientId(), msg.getContent())
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyToMessageId(1)
                .replyMarkup(new ForceReply());

        SendResponse sendResponse = bot.execute(request);
        boolean ok = sendResponse.isOk();
        Message result = sendResponse.message();
        return new MsgResult()
            .setMessageId(String.valueOf(result.messageId()))
            .setSuccess(ok);
    }

    public List<Msg> getNewMessages(boolean confirm) {
        int pageSize = 100;
        int offset = 0;
        List<Update> allUpdates = Lists.newArrayList();
        while (true) {
            GetUpdates getUpdates =
                new GetUpdates()
                    .limit(pageSize)
                    .offset(offset)
                    .timeout(0);
            GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
            List<Update> page = updatesResponse.updates();
            allUpdates.addAll(page);
            if (page.isEmpty() || page.size() < pageSize) {
                break;
            }
        }
        return allUpdates
            .stream()
            .map(
                u -> new Msg()
                    .setUserId(String.valueOf(u.message().from().id()))
                    .setAddress(String.valueOf(u.message().chat().id())))
            .collect(Collectors.toList());

    }

}
