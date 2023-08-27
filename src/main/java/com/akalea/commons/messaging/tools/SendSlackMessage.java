package com.akalea.commons.messaging.tools;

import java.io.IOException;
import java.net.MalformedURLException;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.User;
import com.akalea.commons.messaging.services.slack.SlackService;

public class SendSlackMessage {

    public static void main(String[] args) throws MalformedURLException, IOException {
        SlackService service = (SlackService) new SlackService().connect();

        service.sendMessage(
            new Msg()
                .setRecipient(
                    new User().setUuid(
                        "https://hooks.slack.com/services/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"))
                .setContent("test"));
    }
}
