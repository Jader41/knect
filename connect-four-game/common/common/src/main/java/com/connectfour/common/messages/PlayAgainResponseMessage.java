package com.connectfour.common.messages;

/**
 * Message sent from server to client in response to a play again request.
 */
public class PlayAgainResponseMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final boolean bothWantToPlayAgain;
    private final boolean opponentDisconnected;
    
    public PlayAgainResponseMessage(boolean bothWantToPlayAgain, boolean opponentDisconnected) {
        super(MessageType.PLAY_AGAIN_RESPONSE);
        this.bothWantToPlayAgain = bothWantToPlayAgain;
        this.opponentDisconnected = opponentDisconnected;
    }
    
    public boolean bothWantToPlayAgain() {
        return bothWantToPlayAgain;
    }
    
    public boolean isOpponentDisconnected() {
        return opponentDisconnected;
    }
} 