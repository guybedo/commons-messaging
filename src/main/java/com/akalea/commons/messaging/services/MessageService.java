package com.akalea.commons.messaging.services;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgResult;

public interface MessageService {

    MsgResult sendMessage(Msg message);
}
