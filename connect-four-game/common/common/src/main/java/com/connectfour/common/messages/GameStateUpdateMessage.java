package com.connectfour.common.messages;

import com.connectfour.common.model.GameState;

/**
 * Message sent from server to client to update the game state.
 */
public class GameStateUpdateMessage extends Message {
    private static final long serialVersionUID = 2L;
    
    private final GameState gameState;
    
    public GameStateUpdateMessage(GameState gameState) {
        super(MessageType.GAME_STATE_UPDATE);
        System.out.println("Creating GameStateUpdateMessage with turn: " + gameState.getCurrentTurn());
        this.gameState = gameState;
    }
    
    public GameState getGameState() {
        System.out.println("Getting GameState from message, turn: " + gameState.getCurrentTurn());
        return gameState;
    }
} 