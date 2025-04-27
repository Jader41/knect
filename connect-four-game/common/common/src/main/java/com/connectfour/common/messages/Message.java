package com.connectfour.common.messages;

import java.io.Serializable;

// 
// Base class for all messages exchanged between client and server.
public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final MessageType type;
    
    protected Message(MessageType type) {
        this.type = type;
    }
    
    public MessageType getType() {
        return type;
    }
} 
