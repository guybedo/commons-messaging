package com.akalea.commons.messaging.message;

public class Msg {

    private String userId;
    private String address;

    private String recipientId;
    private String content;

    public String getUserId() {
        return userId;
    }

    public Msg setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Msg setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public Msg setRecipientId(String recipientId) {
        this.recipientId = recipientId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Msg setContent(String content) {
        this.content = content;
        return this;
    }

}
