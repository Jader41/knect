package com.connectfour.server;

import com.connectfour.common.messages.*;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.PlayerColor;
import com.connectfour.common.model.CellState;
import com.connectfour.common.model.GameStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 
// Represents a game session between two players.
public class GameSession {
    private static final Logger logger = LoggerFactory.getLogger(GameSession.class);
    
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final GameServer server;
    
    private final char[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private boolean player1WantsPlayAgain;
    private boolean player2WantsPlayAgain;
    private boolean player1WantsNewGame;
    private boolean player2WantsNewGame;
    
    // 
// Creates a new game session between two players.
// 
// @param player1 The first player
// @param player2 The second player
// @param server The game server
    public GameSession(ClientHandler player1, ClientHandler player2, GameServer server) {
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        
        // Initialize the game board
        this.board = new char[6][7];
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                board[row][col] = ' ';
            }
        }
        
        // Player 1 goes first
        this.currentPlayer = 1;
        this.gameOver = false;
        this.player1WantsPlayAgain = false;
        this.player2WantsPlayAgain = false;
        this.player1WantsNewGame = false;
        this.player2WantsNewGame = false;
        
        // Set the current game for both players
        player1.setCurrentGame(this);
        player2.setCurrentGame(this);
        
        // Create initial game state
        GameState initialState = new GameState(player1.getUsername(), player2.getUsername());
        
        // Send game start messages to both players
        player1.sendMessage(new GameStartMessage(initialState, PlayerColor.RED, player2.getUsername()));
        player2.sendMessage(new GameStartMessage(initialState, PlayerColor.YELLOW, player1.getUsername()));
        
        // Send the initial board state
        sendBoardState();
        
        logger.info("Started new game session between {} and {}", player1.getUsername(), player2.getUsername());
    }
    
    // 
// Starts the game session.
    public void start() {
        logger.info("Starting game session between {} and {}", player1.getUsername(), player2.getUsername());
        
        // Set the current game for both players
        player1.setCurrentGame(this);
        player2.setCurrentGame(this);
        
        // Create initial game state
        GameState initialState = new GameState(player1.getUsername(), player2.getUsername());
        
        // Send game start messages to both players
        player1.sendMessage(new GameStartMessage(initialState, PlayerColor.RED, player2.getUsername()));
        player2.sendMessage(new GameStartMessage(initialState, PlayerColor.YELLOW, player1.getUsername()));
    }
    
    // 
// Handles a move made by a player.
// 
// @param player The player making the move
// @param column The column where the player made the move
    public synchronized void handleMove(ClientHandler player, int column) {
        // Check if it's the player's turn
        boolean isPlayer1Turn = currentPlayer == 1;
        boolean isPlayersTurn = (isPlayer1Turn && player == player1) || (!isPlayer1Turn && player == player2);
        
        logger.info("Move attempted by {} in column {}. Current turn: {}, Player1(RED): {}, Player2(YELLOW): {}, isPlayersTurn: {}",
                player.getUsername(), column, currentPlayer, 
                player1.getUsername(), player2.getUsername(), isPlayersTurn);
        
        if (!isPlayersTurn) {
            logger.warn("Player {} attempted to make a move out of turn", player.getUsername());
            return;
        }
        
        // Make the move
        boolean moveSuccess = makeMove(column);
        
        if (moveSuccess) {
            logger.info("Player {} made successful move in column {}. New turn: {}", 
                    player.getUsername(), column, currentPlayer);
            
            // Log the current board state
            logger.debug("Current board state after move:");
            for (int row = 0; row < 6; row++) {
                StringBuilder rowStr = new StringBuilder();
                for (int col = 0; col < 7; col++) {
                    rowStr.append(board[row][col] == ' ' ? "[ ]" : "[" + board[row][col] + "]");
                }
                logger.debug(rowStr.toString());
            }
            
            // Send updated game state to both players
            broadcastGameState();
        } else {
            logger.warn("Player {} attempted an invalid move in column {}", player.getUsername(), column);
        }
    }
    
    // 
// Broadcasts the current game state to both players.
    private void broadcastGameState() {
        sendBoardState();
        logger.info("Broadcasting game state with current turn: {}", currentPlayer);
    }
    
    // 
// Broadcasts a chat message to both players.
// 
// @param message The chat message to broadcast
    public void broadcastChat(ChatMessage message) {
        player1.sendMessage(message);
        player2.sendMessage(message);
        logger.info("Chat message from {}: {}", message.getSender(), message.getContent());
    }
    
    // 
// Handles a player's request to play again.
// 
// @param player The player making the request
// @param wantsToPlayAgain Whether the player wants to play again
    public synchronized void handlePlayAgainRequest(ClientHandler player, boolean wantsToPlayAgain) {
        if (player == player1) {
            player1WantsPlayAgain = wantsToPlayAgain;
        } else if (player == player2) {
            player2WantsPlayAgain = wantsToPlayAgain;
        } else {
            logger.warn("Unknown player requested to play again: {}", player.getUsername());
            return;
        }
        
        logger.info("Player {} wants to play again: {}", player.getUsername(), wantsToPlayAgain);
        
        // If both players want to play again, start a new game
        if (player1WantsPlayAgain && player2WantsPlayAgain) {
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
    
    // 
// Handles a player's request to start a new game.
// 
// @param player The player making the request
    public void handleNewGameRequest(ClientHandler player) {
        if (!gameOver) {
            // Can't request a new game while the current game is in progress
            DisconnectMessage errorMsg = new DisconnectMessage("Cannot start a new game while the current game is in progress");
            player.sendMessage(errorMsg);
            return;
        }
        
        if (player == player1) {
            player1WantsNewGame = true;
            
            // Notify player2 about the new game request
            // Since we don't have NewGameRequestMessage, use ChatMessage instead
            ChatMessage reqMsg = new ChatMessage(player1.getUsername(), "Would you like to start a new game?");
            player2.sendMessage(reqMsg);
        } else if (player == player2) {
            player2WantsNewGame = true;
            
            // Notify player1 about the new game request
            ChatMessage reqMsg = new ChatMessage(player2.getUsername(), "Would you like to start a new game?");
            player1.sendMessage(reqMsg);
        }
        
        // Check if both players want a new game
        if (player1WantsNewGame && player2WantsNewGame) {
            resetGame();
        }
    }
    
    // 
// Handles a player's response to a new game request.
// 
// @param player The player responding to the request
// @param accepted Whether the player accepted the request
    public void handleNewGameResponse(ClientHandler player, boolean accepted) {
        if (!gameOver) {
            // Can't respond to a new game request while the current game is in progress
            DisconnectMessage errorMsg = new DisconnectMessage("Cannot respond to new game request while the current game is in progress");
            player.sendMessage(errorMsg);
            return;
        }
        
        if (player == player1) {
            player1WantsNewGame = accepted;
            
            if (!accepted) {
                // Notify player2 that player1 declined the new game
                DisconnectMessage errorMsg = new DisconnectMessage(player1.getUsername() + " declined the new game request");
                player2.sendMessage(errorMsg);
                player2WantsNewGame = false;
            }
        } else if (player == player2) {
            player2WantsNewGame = accepted;
            
            if (!accepted) {
                // Notify player1 that player2 declined the new game
                DisconnectMessage errorMsg = new DisconnectMessage(player2.getUsername() + " declined the new game request");
                player1.sendMessage(errorMsg);
                player1WantsNewGame = false;
            }
        }
        
        // Check if both players want a new game
        if (player1WantsNewGame && player2WantsNewGame) {
            resetGame();
        }
    }
    
    // 
// Handles a player's request to return to the lobby.
// 
// @param player The player requesting to return to the lobby
    public void handleReturnToLobbyRequest(ClientHandler player) {
        ClientHandler otherPlayer = (player == player1) ? player2 : player1;
        
        // Notify the other player
        DisconnectMessage errorMsg = new DisconnectMessage(player.getUsername() + " has returned to the lobby");
        otherPlayer.sendMessage(errorMsg);
        
        // Clear the game session for both players
        player1.clearCurrentGame();
        player2.clearCurrentGame();
        
        // Add the player back to matchmaking
        server.addToMatchmaking(player);
        
        // Add the other player back to matchmaking if they're still connected
        if (otherPlayer.isConnected()) {
            server.addToMatchmaking(otherPlayer);
        }
        
        // End this game session
        server.endGameSession(this);
        
        logger.info("Player {} returned to lobby, ending game session with {}", 
                player.getUsername(), otherPlayer.getUsername());
    }
    
    // 
// Handles a player disconnecting from the game.
// 
// @param player The player who disconnected
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
    
    // 
// Resets the game for a rematch.
    private void resetGame() {
        logger.info("Resetting game session between {} and {}", player1.getUsername(), player2.getUsername());
        
        // Reset the board
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                board[row][col] = ' ';
            }
        }
        
        // Reset game state
        currentPlayer = 1;
        gameOver = false;
        player1WantsPlayAgain = false;
        player2WantsPlayAgain = false;
        player1WantsNewGame = false;
        player2WantsNewGame = false;
        
        // Create new game state
        GameState initialState = new GameState(player1.getUsername(), player2.getUsername());
        
        // Send game start messages to both players
        player1.sendMessage(new GameStartMessage(initialState, PlayerColor.RED, player2.getUsername()));
        player2.sendMessage(new GameStartMessage(initialState, PlayerColor.YELLOW, player1.getUsername()));
        
        // Send the initial board state
        sendBoardState();
        
        logger.info("Started new game between {} and {}", player1.getUsername(), player2.getUsername());
    }
    
    // 
// Makes a move for the current player.
// 
// @param column The column to place the piece in
// @return true if the move was successful, false otherwise
    private boolean makeMove(int column) {
        // Check if the column is valid
        if (column < 0 || column >= 7) {
            logger.warn("Invalid column: {}", column);
            return false;
        }
        
        // Check if the column is full
        if (board[0][column] != ' ') {
            logger.warn("Column {} is full", column);
            return false;
        }
        
        // Find the next available row in the column
        int row = 5;
        while (row >= 0 && board[row][column] != ' ') {
            row--;
        }
        
        // Place the piece
        char piece = (currentPlayer == 1) ? 'R' : 'Y';
        board[row][column] = piece;
        
        logger.info("Player {} placed {} piece at row {}, column {}", 
            (currentPlayer == 1) ? player1.getUsername() : player2.getUsername(), 
            (currentPlayer == 1) ? "RED" : "YELLOW", row, column);
            
        // Check for win conditions
        if (checkForWin(row, column)) {
            gameOver = true;
            logger.info("Player {} wins!", (currentPlayer == 1) ? player1.getUsername() : player2.getUsername());
            // We could add code here to notify clients of the win
        }
        
        // Switch players
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        
        return true;
    }
    
    // 
// Checks if the last move resulted in a win.
// 
// @param row The row of the last move
// @param column The column of the last move
// @return true if the move resulted in a win, false otherwise
    private boolean checkForWin(int row, int column) {
        char piece = board[row][column];
        
        // Check horizontal
        int count = 0;
        for (int c = 0; c < 7; c++) {
            if (board[row][c] == piece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        // Check vertical
        count = 0;
        for (int r = 0; r < 6; r++) {
            if (board[r][column] == piece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (top-left to bottom-right)
        count = 0;
        int startRow = row - Math.min(row, column);
        int startCol = column - Math.min(row, column);
        for (int i = 0; i < 6 && startRow + i < 6 && startCol + i < 7; i++) {
            if (board[startRow + i][startCol + i] == piece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (top-right to bottom-left)
        count = 0;
        startRow = row - Math.min(row, 6 - column - 1);
        startCol = column + Math.min(row, 6 - column - 1);
        for (int i = 0; i < 6 && startRow + i < 6 && startCol - i >= 0; i++) {
            if (board[startRow + i][startCol - i] == piece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        return false;
    }

    // 
// Sends the current board state to both players.
    private void sendBoardState() {
        // Create a new GameState
        GameState gameState = new GameState(player1.getUsername(), player2.getUsername());
        
        // Copy the board state from our internal representation to the GameState
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] == 'R') {
                    gameState.setCellState(row, col, CellState.RED);
                } else if (board[row][col] == 'Y') {
                    gameState.setCellState(row, col, CellState.YELLOW);
                } else {
                    gameState.setCellState(row, col, CellState.EMPTY);
                }
            }
        }
        
        // Set the current turn in the GameState
        PlayerColor currentTurnColor = (currentPlayer == 1) ? PlayerColor.RED : PlayerColor.YELLOW;
        gameState.setCurrentTurn(currentTurnColor);
        
        // Set game status if the game is over
        if (gameOver) {
            // Check which player won or if it's a draw
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 7; col++) {
                    if (board[row][col] != ' ' && checkForWin(row, col)) {
                        if (board[row][col] == 'R') {
                            gameState.setGameStatus(GameStatus.RED_WINS);
                        } else {
                            gameState.setGameStatus(GameStatus.YELLOW_WINS);
                        }
                        break;
                    }
                }
            }
            
            // If no winner found but game is over, it's a draw
            if (gameState.getStatus() == GameStatus.IN_PROGRESS) {
                gameState.setGameStatus(GameStatus.DRAW);
            }
        }
        
        // Create a message with the game state
        GameStateUpdateMessage message = new GameStateUpdateMessage(gameState);
        logger.info("Sending board state with current turn: {}", currentPlayer == 1 ? "RED" : "YELLOW");
        
        player1.sendMessage(message);
        player2.sendMessage(message);
    }
} 
