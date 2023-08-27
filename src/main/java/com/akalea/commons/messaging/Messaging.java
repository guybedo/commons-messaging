package com.akalea.commons.messaging;

import java.util.function.Consumer;

import com.akalea.commons.messaging.services.MessagingService;
import com.akalea.commons.messaging.services.MessagingServiceCredentials;
import com.akalea.commons.messaging.services.MessagingServiceId;
import com.akalea.commons.messaging.services.slack.SlackService;
import com.akalea.commons.messaging.services.telegram.TelegramService;

public class Messaging {

    public static MessagingService messageService(
        MessagingServiceId id,
        MessagingServiceCredentials credentials) {
        if (MessagingServiceId.telegram.equals(id))
            return new TelegramService().setCredentials(credentials);
        else if (MessagingServiceId.slack.equals(id))
            return new SlackService().setCredentials(credentials);
        throw new RuntimeException("Not implemented yet");
    };

    public static void transaction(
        MessagingServiceId serviceId,
        MessagingServiceCredentials credentials,
        Consumer<MessagingService> func) {
        MessagingService messageService = messageService(serviceId, credentials);
        messageService.connect();
        try {
            func.accept(messageService);
        } finally {
            messageService.disconnect();
        }
    }
}
