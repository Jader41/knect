package com.connectfour.common.messages;

import com.connectfour.common.model.GameState;
import com.connectfour.common.model.PlayerColor;

/**
 * Message sent from server to client to notify them that a game is starting.
 */
public class GameStartMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final GameState initialState;
    private final PlayerColor assignedColor;
    private final String opponentUsername;
    
    public GameStartMessage(GameState initialState, PlayerColor assignedColor, String opponentUsername) {
        super(MessageType.GAME_START);
        this.initialState = initialState;
        this.assignedColor = assignedColor;
        this.opponentUsername = opponentUsername;
    }
    
    public GameState getInitialState() {
        return initialState;
    }
    
    public PlayerColor getAssignedColor() {
        return assignedColor;
    }
    
    public String getOpponentUsername() {
        return opponentUsername;
    }
} 