package com.connectfour.common.model;

import java.io.Serializable;

/**
 * Represents the state of a Connect Four game.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    
    private final CellState[][] board;
    private PlayerColor currentTurn;
    private GameStatus status;
    private final String player1Username;
    private final String player2Username;
    
    public GameState(String player1Username, String player2Username) {
        this.board = new CellState[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                this.board[row][col] = CellState.EMPTY;
            }
        }
        this.currentTurn = PlayerColor.RED; // Red always goes first
        this.status = GameStatus.IN_PROGRESS;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
    }
    
    public CellState getCellState(int row, int col) {
        return board[row][col];
    }
    
    public PlayerColor getCurrentTurn() {
        return currentTurn;
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public String getPlayer1Username() {
        return player1Username;
    }
    
    public String getPlayer2Username() {
        return player2Username;
    }
    
    /**
     * Makes a move in the specified column.
     * 
     * @param column The column in which to drop the piece (0-based index)
     * @return true if the move was successful, false otherwise
     */
    public boolean makeMove(int column) {
        if (status != GameStatus.IN_PROGRESS || column < 0 || column >= COLUMNS) {
            return false;
        }
        
        // Find the lowest empty row in the selected column
        int row = -1;
        for (int r = ROWS - 1; r >= 0; r--) {
            if (board[r][column] == CellState.EMPTY) {
                row = r;
                break;
            }
        }
        
        if (row == -1) {
            // Column is full
            return false;
        }
        
        // Place the piece
        board[row][column] = (currentTurn == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
        
        // Check for win or draw
        if (checkForWin(row, column)) {
            status = (currentTurn == PlayerColor.RED) ? GameStatus.RED_WINS : GameStatus.YELLOW_WINS;
        } else if (isBoardFull()) {
            status = GameStatus.DRAW;
        } else {
            // Switch turns
            currentTurn = (currentTurn == PlayerColor.RED) ? PlayerColor.YELLOW : PlayerColor.RED;
        }
        
        return true;
    }
    
    private boolean isBoardFull() {
        for (int col = 0; col < COLUMNS; col++) {
            if (board[0][col] == CellState.EMPTY) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkForWin(int row, int col) {
        CellState playerPiece = board[row][col];
        
        // Check horizontal
        int count = 0;
        for (int c = 0; c < COLUMNS; c++) {
            if (board[row][c] == playerPiece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        // Check vertical
        count = 0;
        for (int r = 0; r < ROWS; r++) {
            if (board[r][col] == playerPiece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (positive slope)
        count = 0;
        int startRow = row + Math.min(col, row);
        int startCol = col - Math.min(col, row);
        
        for (int r = startRow, c = startCol; r >= 0 && c < COLUMNS; r--, c++) {
            if (board[r][c] == playerPiece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (negative slope)
        count = 0;
        startRow = row - Math.min(col, ROWS - 1 - row);
        startCol = col - Math.min(col, ROWS - 1 - row);
        
        for (int r = startRow, c = startCol; r < ROWS && c < COLUMNS; r++, c++) {
            if (board[r][c] == playerPiece) {
                count++;
                if (count >= 4) return true;
            } else {
                count = 0;
            }
        }
        
        return false;
    }
    
    /**
     * Creates a deep copy of the current game state.
     * 
     * @return A new GameState instance with the same data
     */
    public GameState copy() {
        GameState copy = new GameState(this.player1Username, this.player2Username);
        
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                copy.board[row][col] = this.board[row][col];
            }
        }
        
        copy.currentTurn = this.currentTurn;
        copy.status = this.status;
        
        return copy;
    }
} 