package com.connectfour.client.ai;

import com.connectfour.common.model.CellState;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.PlayerColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 
// AI player for Connect Four with three difficulty levels.
public class AIPlayer {
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    private final Difficulty difficulty;
    private final Random random;
    private final PlayerColor aiColor;
    private final PlayerColor playerColor;
    
    // 
// Creates a new AI player with the specified difficulty.
// 
// @param difficultyStr The difficulty as a string
// @param aiColor The color assigned to the AI
    public AIPlayer(String difficultyStr, PlayerColor aiColor) {
        this.difficulty = getDifficultyFromString(difficultyStr);
        this.aiColor = aiColor;
        this.playerColor = (aiColor == PlayerColor.RED) ? PlayerColor.YELLOW : PlayerColor.RED;
        this.random = new Random();
    }
    
    // 
// Converts a string difficulty to the corresponding enum value.
// 
// @param difficultyStr The difficulty as a string
// @return The difficulty enum value
    private Difficulty getDifficultyFromString(String difficultyStr) {
        if ("Medium".equalsIgnoreCase(difficultyStr)) {
            return Difficulty.MEDIUM;
        } else if ("Hard".equalsIgnoreCase(difficultyStr)) {
            return Difficulty.HARD;
        } else {
            return Difficulty.EASY;
        }
    }
    
    // 
// Gets the best move for the AI based on the current game state.
// 
// @param gameState The current game state
// @return The column to make a move in (0-based index)
    public int getBestMove(GameState gameState) {
        switch (difficulty) {
            case MEDIUM:
                return getMediumMove(gameState);
            case HARD:
                return getHardMove(gameState);
            case EASY:
            default:
                return getEasyMove(gameState);
        }
    }
    
    // 
// Gets a random valid move (Easy difficulty).
// 
// @param gameState The current game state
// @return The column to make a move in
    private int getEasyMove(GameState gameState) {
        List<Integer> validMoves = getValidMoves(gameState);
        return validMoves.get(random.nextInt(validMoves.size()));
    }
    
    // 
// Gets a semi-intelligent move (Medium difficulty).
// Plays a winning move if possible, or blocks the opponent from winning.
// Otherwise, plays a random move.
// 
// @param gameState The current game state
// @return The column to make a move in
    private int getMediumMove(GameState gameState) {
        // Check for winning moves
        List<Integer> validMoves = getValidMoves(gameState);
        
        // Try each valid move to see if it results in a win
        for (int column : validMoves) {
            GameState tempState = gameState.copy();
            tempState.makeMove(column);
            
            // If this move wins, choose it
            if (checkForWin(tempState, column, aiColor)) {
                return column;
            }
        }
        
        // Check for opponent winning moves to block
        for (int column : validMoves) {
            GameState tempState = gameState.copy();
            
            // Temporarily assume it's the opponent's turn
            // We need to manually determine where the piece would land
            int row = getRowForMove(gameState, column);
            if (row != -1) {
                CellState[][] board = getBoardFromGameState(tempState);
                CellState playerPiece = (playerColor == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
                board[row][column] = playerPiece;
                
                // Check if the opponent would win with this move
                if (checkForWinAtPosition(board, row, column, playerPiece)) {
                    return column;
                }
            }
        }
        
        // If no winning or blocking move, choose a random move
        return validMoves.get(random.nextInt(validMoves.size()));
    }
    
    // 
// Gets the optimal move using a more advanced strategy (Hard difficulty).
// Uses a simple minimax algorithm with a limited depth.
// 
// @param gameState The current game state
// @return The column to make a move in
    private int getHardMove(GameState gameState) {
        List<Integer> validMoves = getValidMoves(gameState);
        
        // If the first move, choose a move in the middle columns for better strategy
        if (isBoardEmpty(gameState)) {
            int middleColumn = GameState.COLUMNS / 2;
            return middleColumn;
        }
        
        // Check for immediate winning moves
        for (int column : validMoves) {
            GameState tempState = gameState.copy();
            tempState.makeMove(column);
            
            // If this move wins, choose it
            if (checkForWin(tempState, column, aiColor)) {
                return column;
            }
        }
        
        // Check for opponent winning moves to block
        for (int column : validMoves) {
            GameState tempState = gameState.copy();
            
            // Temporarily determine where the piece would land
            int row = getRowForMove(gameState, column);
            if (row != -1) {
                CellState[][] board = getBoardFromGameState(tempState);
                CellState playerPiece = (playerColor == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
                board[row][column] = playerPiece;
                
                // Check if the opponent would win with this move
                if (checkForWinAtPosition(board, row, column, playerPiece)) {
                    return column;
                }
            }
        }
        
        // Look for moves that create a threat (3 in a row)
        for (int column : validMoves) {
            int row = getRowForMove(gameState, column);
            if (row != -1) {
                GameState tempState = gameState.copy();
                tempState.makeMove(column);
                
                if (hasThreeInARow(tempState, aiColor)) {
                    return column;
                }
            }
        }
        
        // Prefer center columns for better strategic position
        List<Integer> prioritizedMoves = new ArrayList<>(validMoves);
        prioritizedMoves.sort((a, b) -> {
            int distanceFromCenterA = Math.abs(a - GameState.COLUMNS / 2);
            int distanceFromCenterB = Math.abs(b - GameState.COLUMNS / 2);
            return Integer.compare(distanceFromCenterA, distanceFromCenterB);
        });
        
        if (!prioritizedMoves.isEmpty()) {
            return prioritizedMoves.get(0);
        }
        
        // Fallback to random move
        return validMoves.get(random.nextInt(validMoves.size()));
    }
    
    // 
// Gets a list of valid moves (columns where a piece can be placed).
// 
// @param gameState The current game state
// @return List of valid column indexes
    private List<Integer> getValidMoves(GameState gameState) {
        List<Integer> validMoves = new ArrayList<>();
        
        for (int col = 0; col < GameState.COLUMNS; col++) {
            if (gameState.getCellState(0, col) == CellState.EMPTY) {
                validMoves.add(col);
            }
        }
        
        return validMoves;
    }
    
    // 
// Checks if a move would result in a win based on the game state.
// 
// @param gameState The game state
// @param column The column of the last move
// @param color The player color to check for win
// @return true if the move resulted in a win, false otherwise
    private boolean checkForWin(GameState gameState, int column, PlayerColor color) {
        // Find the row of the last move
        int row = -1;
        for (int r = 0; r < GameState.ROWS; r++) {
            CellState cellState = (color == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
            if (gameState.getCellState(r, column) == cellState) {
                row = r;
                break;
            }
        }
        
        if (row == -1) {
            return false;
        }
        
        CellState[][] board = getBoardFromGameState(gameState);
        CellState piece = (color == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
        
        return checkForWinAtPosition(board, row, column, piece);
    }
    
    // 
// Checks if there's a win at a specific position.
// 
// @param board The game board
// @param row The row to check
// @param col The column to check
// @param playerPiece The player's piece type
// @return true if there's a win, false otherwise
    private boolean checkForWinAtPosition(CellState[][] board, int row, int col, CellState playerPiece) {
        // Check horizontal
        int count = 0;
        for (int c = 0; c < GameState.COLUMNS; c++) {
            if (board[row][c] == playerPiece) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        // Check vertical
        count = 0;
        for (int r = 0; r < GameState.ROWS; r++) {
            if (board[r][col] == playerPiece) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (positive slope)
        count = 0;
        int startRow = row;
        int startCol = col;
        
        // Move to the bottom-left of the diagonal
        while (startRow < GameState.ROWS - 1 && startCol > 0) {
            startRow++;
            startCol--;
        }
        
        // Now check the diagonal going up-right
        for (int r = startRow, c = startCol; r >= 0 && c < GameState.COLUMNS; r--, c++) {
            if (r < GameState.ROWS && c >= 0 && c < GameState.COLUMNS && board[r][c] == playerPiece) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (negative slope)
        count = 0;
        startRow = row;
        startCol = col;
        
        // Move to the top-left of the diagonal
        while (startRow > 0 && startCol > 0) {
            startRow--;
            startCol--;
        }
        
        // Now check the diagonal going down-right
        for (int r = startRow, c = startCol; r < GameState.ROWS && c < GameState.COLUMNS; r++, c++) {
            if (r >= 0 && c >= 0 && board[r][c] == playerPiece) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        
        return false;
    }
    
    // 
// Gets the row where a piece would land if dropped in the specified column.
// 
// @param gameState The game state
// @param column The column to check
// @return The row where the piece would land, or -1 if the column is full
    private int getRowForMove(GameState gameState, int column) {
        for (int row = GameState.ROWS - 1; row >= 0; row--) {
            if (gameState.getCellState(row, column) == CellState.EMPTY) {
                return row;
            }
        }
        return -1; // Column is full
    }
    
    // 
// Checks if the board is empty (first move of the game).
// 
// @param gameState The game state
// @return true if the board is empty, false otherwise
    private boolean isBoardEmpty(GameState gameState) {
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                if (gameState.getCellState(row, col) != CellState.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // 
// Checks if a player has three pieces in a row (used for threat detection).
// 
// @param gameState The game state
// @param color The player color to check
// @return true if there are three pieces in a row, false otherwise
    private boolean hasThreeInARow(GameState gameState, PlayerColor color) {
        CellState piece = (color == PlayerColor.RED) ? CellState.RED : CellState.YELLOW;
        CellState[][] board = getBoardFromGameState(gameState);
        
        // Check horizontally
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col <= GameState.COLUMNS - 3; col++) {
                int count = 0;
                for (int k = 0; k < 3; k++) {
                    if (board[row][col + k] == piece) {
                        count++;
                    }
                }
                if (count == 3) {
                    return true;
                }
            }
        }
        
        // Check vertically
        for (int row = 0; row <= GameState.ROWS - 3; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                int count = 0;
                for (int k = 0; k < 3; k++) {
                    if (board[row + k][col] == piece) {
                        count++;
                    }
                }
                if (count == 3) {
                    return true;
                }
            }
        }
        
        // Check diagonal (positive slope)
        for (int row = 3; row < GameState.ROWS; row++) {
            for (int col = 0; col <= GameState.COLUMNS - 3; col++) {
                int count = 0;
                for (int k = 0; k < 3; k++) {
                    if (board[row - k][col + k] == piece) {
                        count++;
                    }
                }
                if (count == 3) {
                    return true;
                }
            }
        }
        
        // Check diagonal (negative slope)
        for (int row = 0; row <= GameState.ROWS - 3; row++) {
            for (int col = 0; col <= GameState.COLUMNS - 3; col++) {
                int count = 0;
                for (int k = 0; k < 3; k++) {
                    if (board[row + k][col + k] == piece) {
                        count++;
                    }
                }
                if (count == 3) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // 
// Creates a 2D array representing the board from the game state.
// 
// @param gameState The game state
// @return 2D array representing the board
    private CellState[][] getBoardFromGameState(GameState gameState) {
        CellState[][] board = new CellState[GameState.ROWS][GameState.COLUMNS];
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                board[row][col] = gameState.getCellState(row, col);
            }
        }
        return board;
    }
} 
