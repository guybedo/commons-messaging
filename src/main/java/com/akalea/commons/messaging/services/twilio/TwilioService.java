package com.akalea.commons.messaging.services.twilio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akalea.commons.messaging.message.Msg;
import com.akalea.commons.messaging.message.MsgReceipt;
import com.akalea.commons.messaging.services.ServiceBase;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class TwilioService extends ServiceBase {
    private final static Logger logger = LoggerFactory.getLogger(TwilioService.class);

    public TwilioService connect() {
        Twilio.init(this.credentials.getUuid(), this.credentials.getToken());
        this.connected = true;
        return this;
    }

    public TwilioService disconnect() {
        return this;
    }

    @Override
    public MsgReceipt sendMessage(Msg msg) {
        if (!this.connected)
            this.connect();
        Message message =
            Message
                .creator(
                    new com.twilio.type.PhoneNumber(msg.getRecipient().getUuid()),
                    new com.twilio.type.PhoneNumber(msg.getUser().getUuid()),
                    msg.getContent())
                .create();
        return new MsgReceipt()
            .setMessage(new Msg().setContent(message.getStatus().toString()))
            .setSuccess(true);

    }

    public List<Msg> getNewMessages() {
        throw new RuntimeException("Not implemented");
    }

}
