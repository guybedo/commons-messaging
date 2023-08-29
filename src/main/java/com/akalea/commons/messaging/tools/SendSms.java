package com.akalea.commons.messaging.tools;

import java.io.IOException;
import java.net.MalformedURLException;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.User;
import com.akalea.commons.messaging.services.MessagingServiceCredentials;
import com.akalea.commons.messaging.services.twilio.TwilioService;

public class SendSms {

    public static void main(String[] args) throws MalformedURLException, IOException {
        new TwilioService()
            .setCredentials(
                new MessagingServiceCredentials()
                    .setUuid("xxx")
                    .setToken("xxx"))
            .sendMessage(
                new Msg()
                    .setUser(new User().setUuid("+16184378358"))
                    .setRecipient(new User().setUuid("+68987760041"))
                    .setContent("test"));
    }
}
