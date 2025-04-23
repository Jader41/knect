package com.connectfour.common.messages;

/**
 * Message sent to notify about a disconnect.
 */
public class DisconnectMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final String reason;
    
    public DisconnectMessage(String reason) {
        super(MessageType.DISCONNECT);
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
} 