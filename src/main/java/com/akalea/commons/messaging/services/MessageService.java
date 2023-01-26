package com.akalea.commons.messaging.services;

import java.util.List;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgResult;

public interface MessageService {
    
    MessageService connect();

    MsgResult sendMessage(Msg message);

    List<Msg> getNewMessages();
}
