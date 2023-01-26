package com.akalea.commons.messaging;

import com.akalea.commons.messaging.services.MessageService;
import com.akalea.commons.messaging.services.MessageServiceCredentials;
import com.akalea.commons.messaging.services.MessageServiceId;
import com.akalea.commons.messaging.services.telegram.TelegramMessageService;

public class Messaging {

    public static MessageService messageService(
        MessageServiceId id,
        MessageServiceCredentials credentials) {
        if (MessageServiceId.telegram.equals(id))
            return new TelegramMessageService().setCredentials(credentials);
        throw new RuntimeException("Not implemented yet");
    };
}
