package com.connectfour.common.messages;

/**
 * Message sent by a client to indicate they want to cancel matchmaking.
 */
public class CancelMatchmakingMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public CancelMatchmakingMessage() {
        super(MessageType.CANCEL_MATCHMAKING);
    }
} 