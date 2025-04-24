package com.connectfour.server;

import com.connectfour.common.messages.*;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.PlayerColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a game session between two players.
 */
public class GameSession {
    private static final Logger logger = LoggerFactory.getLogger(GameSession.class);
    
    private final ClientHandler player1;
    private final ClientHandler player2;
    private GameState gameState;
    private boolean player1WantsRematch;
    private boolean player2WantsRematch;
    
    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameState = new GameState(player1.getUsername(), player2.getUsername());
        this.player1WantsRematch = false;
        this.player2WantsRematch = false;
    }
    
    /**
     * Starts the game session.
     */
    public void start() {
        logger.info("Starting game session between {} and {}", player1.getUsername(), player2.getUsername());
        
        // Set the current game for both players
        player1.setCurrentGame(this);
        player2.setCurrentGame(this);
        
        // Send game start messages to both players
        player1.sendMessage(new GameStartMessage(gameState, PlayerColor.RED, player2.getUsername()));
        player2.sendMessage(new GameStartMessage(gameState, PlayerColor.YELLOW, player1.getUsername()));
    }
    
    /**
     * Handles a move made by a player.
     * 
     * @param player The player making the move
     * @param column The column where the player made the move
     */
    public synchronized void handleMove(ClientHandler player, int column) {
        // Check if it's the player's turn
        boolean isPlayer1Turn = gameState.getCurrentTurn() == PlayerColor.RED;
        boolean isPlayersTurn = (isPlayer1Turn && player == player1) || (!isPlayer1Turn && player == player2);
        
        logger.info("Move attempted by {} in column {}. Current turn: {}, Player1(RED): {}, Player2(YELLOW): {}, isPlayersTurn: {}",
                player.getUsername(), column, gameState.getCurrentTurn(), 
                player1.getUsername(), player2.getUsername(), isPlayersTurn);
        
        if (!isPlayersTurn) {
            logger.warn("Player {} attempted to make a move out of turn", player.getUsername());
            return;
        }
        
        // Make the move
        boolean moveSuccess = gameState.makeMove(column);
        
        if (moveSuccess) {
            logger.info("Player {} made successful move in column {}. New turn: {}", 
                    player.getUsername(), column, gameState.getCurrentTurn());
            
            // Log the current board state
            logger.debug("Current board state after move:");
            for (int row = 0; row < GameState.ROWS; row++) {
                StringBuilder rowStr = new StringBuilder();
                for (int col = 0; col < GameState.COLUMNS; col++) {
                    switch(gameState.getCellState(row, col)) {
                        case EMPTY: rowStr.append("[ ]"); break;
                        case RED: rowStr.append("[R]"); break;
                        case YELLOW: rowStr.append("[Y]"); break;
                    }
                }
                logger.debug(rowStr.toString());
            }
            
            // Send updated game state to both players
            broadcastGameState();
        } else {
            logger.warn("Player {} attempted an invalid move in column {}", player.getUsername(), column);
        }
    }
    
    /**
     * Broadcasts the current game state to both players.
     */
    private void broadcastGameState() {
        // Create a copy of the game state to ensure immutability
        GameState stateCopy = gameState.copy();
        logger.info("Broadcasting game state with current turn: " + stateCopy.getCurrentTurn());
        
        GameStateUpdateMessage message = new GameStateUpdateMessage(stateCopy);
        player1.sendMessage(message);
        player2.sendMessage(message);
    }
    
    /**
     * Broadcasts a chat message to both players.
     * 
     * @param message The chat message to broadcast
     */
    public void broadcastChat(ChatMessage message) {
        player1.sendMessage(message);
        player2.sendMessage(message);
        logger.info("Chat message from {}: {}", message.getSender(), message.getContent());
    }
    
    /**
     * Handles a player's request to play again.
     * 
     * @param player The player making the request
     * @param wantsToPlayAgain Whether the player wants to play again
     */
    public synchronized void handlePlayAgainRequest(ClientHandler player, boolean wantsToPlayAgain) {
        if (player == player1) {
            player1WantsRematch = wantsToPlayAgain;
        } else if (player == player2) {
            player2WantsRematch = wantsToPlayAgain;
        } else {
            logger.warn("Unknown player requested to play again: {}", player.getUsername());
            return;
        }
        
        logger.info("Player {} wants to play again: {}", player.getUsername(), wantsToPlayAgain);
        
        // If both players want to play again, start a new game
        if (player1WantsRematch && player2WantsRematch) {
            resetGame();
        }
        
        // If either player doesn't want to play again, notify both players
        else if (!wantsToPlayAgain) {
            PlayAgainResponseMessage response = new PlayAgainResponseMessage(false, false);
            player1.sendMessage(response);
            player2.sendMessage(response);
            
            // Clear the game session for both players
            player1.clearCurrentGame();
            player2.clearCurrentGame();
        }
    }
    
    /**
     * Handles a player disconnecting from the game.
     * 
     * @param player The player who disconnected
     */
    public void handlePlayerDisconnect(ClientHandler player) {
        logger.info("Player {} disconnected from game session", player.getUsername());
        
        // Notify the other player
        ClientHandler otherPlayer = (player == player1) ? player2 : player1;
        
        if (otherPlayer.isConnected()) {
            PlayAgainResponseMessage response = new PlayAgainResponseMessage(false, true);
            otherPlayer.sendMessage(response);
            otherPlayer.clearCurrentGame();
        }
    }
    
    /**
     * Resets the game for a rematch.
     */
    private void resetGame() {
        logger.info("Resetting game session between {} and {}", player1.getUsername(), player2.getUsername());
        
        // Create a new game state
        gameState = new GameState(player1.getUsername(), player2.getUsername());
        
        // Reset rematch flags
        player1WantsRematch = false;
        player2WantsRematch = false;
        
        // Send game start messages to both players
        player1.sendMessage(new GameStartMessage(gameState, PlayerColor.RED, player2.getUsername()));
        player2.sendMessage(new GameStartMessage(gameState, PlayerColor.YELLOW, player1.getUsername()));
    }
} 