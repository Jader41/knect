package com.connectfour.common.messages;

// 
// Message sent from client to server when a player wants to play again.
public class PlayAgainRequestMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final boolean wantsToPlayAgain;
    
    public PlayAgainRequestMessage(boolean wantsToPlayAgain) {
        super(MessageType.PLAY_AGAIN_REQUEST);
        this.wantsToPlayAgain = wantsToPlayAgain;
    }
    
    public boolean wantsToPlayAgain() {
        return wantsToPlayAgain;
    }
} 
