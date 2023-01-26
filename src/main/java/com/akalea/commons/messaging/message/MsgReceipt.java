package com.akalea.commons.messaging.message;

public class MsgReceipt {

    private Msg     message;
    private boolean success;

    public Msg getMessage() {
        return message;
    }

    public MsgReceipt setMessage(Msg messageId) {
        this.message = messageId;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public MsgReceipt setSuccess(boolean success) {
        this.success = success;
        return this;
    }

}
