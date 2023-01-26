package com.akalea.commons.messaging;

import com.akalea.commons.messaging.services.MessageService;
import com.akalea.commons.messaging.services.MessageServiceCredentials;
import com.akalea.commons.messaging.services.MessageServiceId;
import com.akalea.commons.messaging.services.telegram.TelegramMessageService;

public class Messaging {

    public MessageService messageService(
        MessageServiceId id,
        MessageServiceCredentials crendetials) {
        if (MessageServiceId.telegram.equals(id))
            return new TelegramMessageService();
        throw new RuntimeException("Not implemented yet");
    };
}
