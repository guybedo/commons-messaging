package com.akalea.commons.messaging.services;

import java.util.List;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgReceipt;

public interface MessagingService {
    
    MessagingService connect();

    MsgReceipt sendMessage(Msg message);

    List<Msg> getNewMessages();
}
