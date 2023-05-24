package com.akalea.commons.messaging.message;

public class MsgMedia {

    public static enum MediaType {
            img, gif, video
    };

    private MediaType type;
    private String    filename;
    private byte[]    data;

    public String getFilename() {
        return filename;
    }

    public MsgMedia setFilename(String name) {
        this.filename = name;
        return this;
    }

    public MediaType getType() {
        return type;
    }

    public MsgMedia setType(MediaType type) {
        this.type = type;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public MsgMedia setData(byte[] data) {
        this.data = data;
        return this;
    }

}
