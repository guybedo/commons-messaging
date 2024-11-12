package com.akalea.commons.messaging.services.telegram;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgReceipt;
import com.akalea.commons.messaging.message.User;
import com.akalea.commons.messaging.services.ServiceBase;
import com.akalea.sugar.Collections;
import com.google.common.collect.Lists;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

public class TelegramService extends ServiceBase {

    private TelegramBot bot;

    public TelegramService connect() {
        this.bot =
            new TelegramBot.Builder(credentials.getToken()).build();
        this.connected = true;
        return this;
    }

    public TelegramService disconnect() {
        this.bot.shutdown();
        this.bot = null;
        return this;
    }

    @Override
    public MsgReceipt sendMessage(Msg msg) {
        if (!this.connected)
            this.connect();
        SendMessage request =
            new SendMessage(msg.getRecipient().getId(), msg.getContent())
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true);
        Optional
            .ofNullable(msg.getReplyTo())
            .ifPresent(rt -> request.replyToMessageId(Integer.parseInt(rt.getId())));

        SendResponse sendResponse = bot.execute(request);
        boolean ok = sendResponse.isOk();
        Message result = sendResponse.message();
        return new MsgReceipt()
            .setMessage(new Msg().setId(String.valueOf(result.messageId())))
            .setSuccess(ok);
    }

    public List<Msg> getNewMessages() {
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
            if (page == null || page.isEmpty()) {
                break;
            }
            allUpdates.addAll(Collections.notNull(page));
            offset = page.get(page.size() - 1).updateId() + 1;
        }
        return allUpdates
            .stream()
            .map(
                u -> new Msg()
                    .setId(String.valueOf(u.message().messageId()))
                    .setUser(
                        new User()
                            .setId(String.valueOf(u.message().from().id()))
                            .setUuid(u.message().from().username())))
            .collect(Collectors.toList());
    }

}
