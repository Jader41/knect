package com.connectfour.common.model;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Represents the state of a Connect Four game.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 2L; // Updated version
    
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    
    private final CellState[][] board;
    private PlayerColor currentTurn; // Ensure not transient
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
    
    // Add custom serialization methods to ensure proper serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(currentTurn); // Explicitly write currentTurn
        System.out.println("Serializing GameState, currentTurn=" + currentTurn);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        currentTurn = (PlayerColor) in.readObject(); // Explicitly read currentTurn
        System.out.println("Deserializing GameState, currentTurn=" + currentTurn);
    }
    
    public CellState getCellState(int row, int col) {
        return board[row][col];
    }
    
    public PlayerColor getCurrentTurn() {
        System.out.println("Getting current turn: " + currentTurn); // Debug
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
        System.out.println("makeMove called for column " + column + ", current turn: " + currentTurn);
        
        if (status != GameStatus.IN_PROGRESS || column < 0 || column >= COLUMNS) {
            System.out.println("Invalid move: game not in progress or column out of bounds");
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
            System.out.println("Invalid move: column " + column + " is full");
            return false;
        }
        
        // Place the piece
        CellState newPiece = (currentTurn == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
        System.out.println("Placing " + newPiece + " piece at row " + row + ", column " + column);
        board[row][column] = newPiece;
        
        // Check for win or draw
        if (checkForWin(row, column)) {
            status = (currentTurn == PlayerColor.RED) ? GameStatus.RED_WINS : GameStatus.YELLOW_WINS;
            System.out.println("Win detected! Status set to: " + status);
        } else if (isBoardFull()) {
            status = GameStatus.DRAW;
            System.out.println("Board is full. Status set to DRAW");
        } else {
            // Switch turns
            PlayerColor oldTurn = currentTurn;
            currentTurn = (currentTurn == PlayerColor.RED) ? PlayerColor.YELLOW : PlayerColor.RED;
            System.out.println("Turn switched from " + oldTurn + " to " + currentTurn);
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
        System.out.println("Checking for win at row=" + row + ", col=" + col + " for piece " + playerPiece);
        
        // Check horizontal
        int count = 0;
        for (int c = 0; c < COLUMNS; c++) {
            if (board[row][c] == playerPiece) {
                count++;
                if (count >= 4) {
                    System.out.println("HORIZONTAL WIN found");
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        // Check vertical
        count = 0;
        for (int r = 0; r < ROWS; r++) {
            if (board[r][col] == playerPiece) {
                count++;
                if (count >= 4) {
                    System.out.println("VERTICAL WIN found");
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (positive slope)
        count = 0;
        // Calculate starting positions for diagonal check
        int startRow = row;
        int startCol = col;
        
        // Move to the bottom-left of the diagonal
        while (startRow < ROWS - 1 && startCol > 0) {
            startRow++;
            startCol--;
        }
        
        System.out.println("Positive diagonal starting at row=" + startRow + ", col=" + startCol);
        
        // Now check the diagonal going up-right
        for (int r = startRow, c = startCol; r >= 0 && c < COLUMNS; r--, c++) {
            if (r < ROWS && c >= 0 && c < COLUMNS && board[r][c] == playerPiece) {
                count++;
                if (count >= 4) {
                    System.out.println("POSITIVE DIAGONAL WIN found");
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (negative slope)
        count = 0;
        // Calculate starting positions for anti-diagonal check
        startRow = row;
        startCol = col;
        
        // Move to the top-left of the diagonal
        while (startRow > 0 && startCol > 0) {
            startRow--;
            startCol--;
        }
        
        System.out.println("Negative diagonal starting at row=" + startRow + ", col=" + startCol);
        
        // Now check the diagonal going down-right
        for (int r = startRow, c = startCol; r < ROWS && c < COLUMNS; r++, c++) {
            if (r >= 0 && c >= 0 && board[r][c] == playerPiece) {
                count++;
                if (count >= 4) {
                    System.out.println("NEGATIVE DIAGONAL WIN found");
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        System.out.println("No win found");
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
        System.out.println("Copying GameState, currentTurn from " + this.currentTurn + " to " + copy.currentTurn);
        copy.status = this.status;
        
        return copy;
    }
    
    /**
     * Sets the state of a specific cell directly.
     * This method is for server use only, for reconstructing game state.
     * 
     * @param row The row index
     * @param col The column index
     * @param state The cell state to set
     */
    public void setCellState(int row, int col, CellState state) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS) {
            board[row][col] = state;
        }
    }
    
    /**
     * Sets the current turn directly.
     * This method is for server use only, for reconstructing game state.
     * 
     * @param color The player color whose turn it is
     */
    public void setCurrentTurn(PlayerColor color) {
        this.currentTurn = color;
    }
    
    /**
     * Sets the game status directly.
     * This method is for server use only, for reconstructing game state.
     * 
     * @param status The game status to set
     */
    public void setGameStatus(GameStatus status) {
        this.status = status;
    }
} 