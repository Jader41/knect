package com.connectfour.common.messages;

import com.connectfour.common.model.GameState;

/**
 * Message sent from server to client to update the game state.
 */
public class GameStateUpdateMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final GameState gameState;
    
    public GameStateUpdateMessage(GameState gameState) {
        super(MessageType.GAME_STATE_UPDATE);
        this.gameState = gameState;
    }
    
    public GameState getGameState() {
        return gameState;
    }
} 