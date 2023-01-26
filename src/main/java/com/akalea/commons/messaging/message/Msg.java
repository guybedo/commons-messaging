package com.akalea.commons.messaging.message;

public class Msg {

    private User   user;
    private String chatId;

    private User   recipient;
    private String content;

    public User getUser() {
        return user;
    }

    public Msg setUser(User userId) {
        this.user = userId;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public Msg setChatId(String address) {
        this.chatId = address;
        return this;
    }

    public User getRecipient() {
        return recipient;
    }

    public Msg setRecipient(User recipientId) {
        this.recipient = recipientId;
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
