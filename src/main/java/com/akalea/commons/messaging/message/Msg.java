package com.akalea.commons.messaging.message;

public class Msg {

    private String id;

    private User user;
    private User recipient;

    private Msg replyTo;

    private String content;

    public String getId() {
        return id;
    }

    public Msg setId(String id) {
        this.id = id;
        return this;
    }

    public Msg getReplyTo() {
        return replyTo;
    }

    public Msg setReplyTo(Msg replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Msg setUser(User userId) {
        this.user = userId;
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
