package com.akalea.commons.messaging;

import com.akalea.commons.messaging.services.MessagingService;
import com.akalea.commons.messaging.services.MessagingServiceCredentials;
import com.akalea.commons.messaging.services.MessagingServiceId;
import com.akalea.commons.messaging.services.telegram.TelegramService;

public class Messaging {

    public static MessagingService messageService(
        MessagingServiceId id,
        MessagingServiceCredentials credentials) {
        if (MessagingServiceId.telegram.equals(id))
            return new TelegramService().setCredentials(credentials);
        throw new RuntimeException("Not implemented yet");
    };
}
