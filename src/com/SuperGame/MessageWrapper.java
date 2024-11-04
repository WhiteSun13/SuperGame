package com.SuperGame;

import java.io.Serializable;

public class MessageWrapper implements Serializable {
    private String type;
    private Object payload;

    public MessageWrapper(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}