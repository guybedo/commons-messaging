package com.akalea.commons.messaging.message;

public class MsgResult {

    private String  messageId;
    private boolean success;

    public String getMessageId() {
        return messageId;
    }

    public MsgResult setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public MsgResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

}
