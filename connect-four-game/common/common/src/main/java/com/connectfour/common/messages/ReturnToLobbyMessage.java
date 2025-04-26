package com.connectfour.common.messages;

/**
 * Message sent from the client to the server to indicate the player wants to return to the lobby.
 */
public class ReturnToLobbyMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public ReturnToLobbyMessage() {
        super(MessageType.RETURN_TO_LOBBY);
    }
} 