package com.connectfour.client.ui;

import com.connectfour.client.GameClient;
import com.connectfour.client.ai.AIPlayer;
import com.connectfour.common.messages.ChatMessage;
import com.connectfour.common.model.CellState;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.GameStatus;
import com.connectfour.common.model.PlayerColor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * The main game screen where the Connect Four game is played.
 */
public class GameScreen implements GameClient.GameStateListener, GameClient.ConnectionListener, GameClient.ChatMessageListener {
    private final Stage stage;
    private final GameClient gameClient;
    
    // UI components
    private BorderPane rootLayout;
    private GridPane boardGrid;
    private Label turnLabel;
    private Label statusLabel;
    private TextFlow chatFlow;
    private TextField chatInput;
    private Circle[][] boardCells;
    
    // Game state
    private GameState gameState;
    private PlayerColor playerColor;
    private String opponentUsername;
    
    // AI player
    private AIPlayer aiPlayer;
    private String aiDifficulty;
    private boolean isAIGame;
    private boolean isAITurn;
    
    public GameScreen(Stage stage, GameClient gameClient) {
        this.stage = stage;
        this.gameClient = gameClient;
        this.isAIGame = false;
        
        // Register listeners
        gameClient.addGameStateListener(this);
        gameClient.addConnectionListener(this);
        gameClient.addChatMessageListener(this);
        
        // Get initial game state
        this.gameState = gameClient.getCurrentGameState();
        this.playerColor = gameClient.getAssignedColor();
        this.opponentUsername = gameClient.getOpponentUsername();
    }
    
    /**
     * Constructor for AI game.
     */
    public GameScreen(Stage stage, GameClient gameClient, GameState gameState, PlayerColor playerColor, String aiDifficulty) {
        this.stage = stage;
        this.gameClient = gameClient;
        this.gameState = gameState;
        this.playerColor = playerColor;
        this.opponentUsername = "Computer (" + aiDifficulty + ")";
        this.isAIGame = true;
        this.aiDifficulty = aiDifficulty;
        
        // Create AI player
        PlayerColor aiColor = (playerColor == PlayerColor.RED) ? PlayerColor.YELLOW : PlayerColor.RED;
        this.aiPlayer = new AIPlayer(aiDifficulty, aiColor);
        
        // Determine if AI goes first (if AI is RED)
        this.isAITurn = (aiColor == PlayerColor.RED);
    }
    
    /**
     * Shows the game screen.
     */
    public void show() {
        // Create the root layout
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: white;");
        rootLayout.setPadding(new Insets(0, 30, 0, 30)); // Add left and right margin
        
        // Create the game board
        createGameBoard();
        
        // Create the info panel
        VBox infoPanel = createInfoPanel();
        rootLayout.setTop(infoPanel);
        
        // Create the chat panel
        VBox chatPanel = createChatPanel();
        rootLayout.setBottom(chatPanel);
        
        // Create scene
        Scene scene = new Scene(rootLayout, 800, 600);
        
        // Set the scene to the stage
        stage.setScene(scene);
        stage.setTitle("Connect Four");
        
        // Update the board with the current game state
        updateBoard();
        updateGameStatus();
        
        // If it's an AI game and AI goes first, make AI move
        if (isAIGame && isAITurn) {
            makeAIMove();
        }
    }
    
    /**
     * Creates the game board UI.
     */
    private void createGameBoard() {
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        boardGrid.setStyle("-fx-background-color: #CCCCCC;");
        
        // Wrap the board in a container with padding
        BorderPane boardContainer = new BorderPane();
        boardContainer.setCenter(boardGrid);
        boardContainer.setPadding(new Insets(0, 20, 0, 20)); // Add left and right padding
        
        boardCells = new Circle[GameState.ROWS][GameState.COLUMNS];
        
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(70, 70);
                
                Circle circle = new Circle(30);
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.LIGHTGRAY);
                circle.setStrokeWidth(1);
                
                boardCells[row][col] = circle;
                
                cell.getChildren().add(circle);
                
                final int column = col;
                cell.setOnMouseClicked(e -> makeMove(column));
                
                boardGrid.add(cell, col, row);
            }
        }
        
        rootLayout.setCenter(boardContainer);
    }
    
    /**
     * Creates the info panel showing game status and turn information.
     * 
     * @return The info panel
     */
    private VBox createInfoPanel() {
        VBox infoPanel = new VBox(10);
        infoPanel.setPadding(new Insets(10));
        infoPanel.setAlignment(Pos.TOP_CENTER);
        infoPanel.setMinWidth(200);
        infoPanel.setStyle("-fx-background-color: white;");
        
        // Player info
        Label playerLabel = new Label(gameClient.getUsername());
        playerLabel.setStyle("-fx-font-weight: bold;");
        
        Circle playerCircle = new Circle(15);
        playerCircle.setFill(Color.BLACK);
        playerCircle.setStroke(Color.BLACK);
        playerCircle.setStrokeWidth(1);
        
        HBox playerInfo = new HBox(10);
        playerInfo.setAlignment(Pos.CENTER_LEFT);
        playerInfo.getChildren().addAll(playerCircle, playerLabel);
        
        // Opponent info
        Label opponentLabel = new Label(opponentUsername);
        opponentLabel.setStyle("-fx-font-weight: bold;");
        
        Circle opponentCircle = new Circle(15);
        opponentCircle.setFill(Color.rgb(65, 105, 225));
        opponentCircle.setStroke(Color.BLACK);
        opponentCircle.setStrokeWidth(1);
        
        HBox opponentInfo = new HBox(10);
        opponentInfo.setAlignment(Pos.CENTER_LEFT);
        opponentInfo.getChildren().addAll(opponentCircle, opponentLabel);
        
        // Turn and status labels
        turnLabel = new Label();
        turnLabel.setStyle("-fx-font-size: 14px;");
        
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Center the player info and status info
        HBox playerStatusBox = new HBox(30);
        playerStatusBox.setAlignment(Pos.CENTER);
        playerStatusBox.getChildren().addAll(playerInfo, statusLabel, opponentInfo);
        
        // Turn info container - moved from above to be more visible
        HBox turnInfoBox = new HBox();
        turnInfoBox.setAlignment(Pos.CENTER);
        turnInfoBox.getChildren().add(turnLabel);
        turnInfoBox.setPadding(new Insets(10, 0, 10, 0));
        
        // New Game button - will be shown/hidden based on game state
        Button newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC;");
        newGameButton.setOnAction(e -> gameClient.requestPlayAgain(true));
        newGameButton.setVisible(false); // Initially hidden during gameplay
        newGameButton.setManaged(false); // Don't take up space when hidden
        
        VBox centerContent = new VBox(10);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(playerStatusBox, turnInfoBox, newGameButton);
        
        infoPanel.getChildren().add(centerContent);
        
        return infoPanel;
    }
    
    /**
     * Creates the chat panel.
     * 
     * @return The chat panel
     */
    private VBox createChatPanel() {
        VBox chatPanel = new VBox(5);
        chatPanel.setPadding(new Insets(10));
        chatPanel.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-width: 1 0 0 0;");
        
        Label chatLabel = new Label("Chat");
        chatLabel.setStyle("-fx-font-weight: bold;");
        
        chatFlow = new TextFlow();
        chatFlow.setPrefHeight(100);
        
        ScrollPane chatScroll = new ScrollPane(chatFlow);
        chatScroll.setFitToWidth(true);
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        chatInput = new TextField();
        chatInput.setPromptText("Type a message...");
        chatInput.setOnAction(e -> sendChatMessage());
        
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC;");
        sendButton.setOnAction(e -> sendChatMessage());
        
        HBox inputBox = new HBox(5, chatInput, sendButton);
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        
        chatPanel.getChildren().addAll(chatLabel, chatScroll, inputBox);
        
        return chatPanel;
    }
    
    /**
     * Makes a move in the specified column.
     * 
     * @param column The column in which to make the move
     */
    private void makeMove(int column) {
        // Check if it's the player's turn
        boolean isPlayerTurn = (!isAIGame && gameState.getCurrentTurn() == playerColor) || 
                               (isAIGame && !isAITurn);
        
        if (gameState.getStatus() != GameStatus.IN_PROGRESS || !isPlayerTurn) {
            return;
        }
        
        if (isAIGame) {
            // Make move locally
            boolean moveSuccess = gameState.makeMove(column);
            if (moveSuccess) {
                updateBoard();
                updateGameStatus();
                
                // If game is still in progress, let AI make its move
                if (gameState.getStatus() == GameStatus.IN_PROGRESS) {
                    isAITurn = true;
                    // Add a small delay before AI moves
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            Platform.runLater(this::makeAIMove);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            }
        } else {
            // Send move to server for online game
            gameClient.makeMove(column);
        }
    }
    
    /**
     * Makes a move for the AI player.
     */
    private void makeAIMove() {
        if (gameState.getStatus() == GameStatus.IN_PROGRESS && isAITurn) {
            int column = aiPlayer.getBestMove(gameState);
            boolean moveSuccess = gameState.makeMove(column);
            
            if (moveSuccess) {
                updateBoard();
                updateGameStatus();
                isAITurn = false;
            } else {
                // If AI couldn't make a valid move (shouldn't happen with proper implementation),
                // try another column
                makeAIMove();
            }
        }
    }
    
    /**
     * Sends a chat message.
     */
    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        
        if (!message.isEmpty()) {
            gameClient.sendChatMessage(message);
            chatInput.clear();
        }
    }
    
    /**
     * Updates the game board to reflect the current game state.
     */
    private void updateBoard() {
        System.out.println("Updating board UI from game state...");
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                CellState cellState = gameState.getCellState(row, col);
                
                switch (cellState) {
                    case EMPTY:
                        boardCells[row][col].setFill(Color.WHITE);
                        break;
                    case RED:
                        boardCells[row][col].setFill(Color.BLACK);
                        System.out.println("Setting cell [" + row + "," + col + "] to RED");
                        break;
                    case YELLOW:
                        boardCells[row][col].setFill(Color.rgb(65, 105, 225));
                        System.out.println("Setting cell [" + row + "," + col + "] to YELLOW");
                        break;
                }
            }
        }
        // Force a layout refresh
        boardGrid.requestLayout();
    }
    
    /**
     * Updates the game status and turn information.
     */
    private void updateGameStatus() {
        boolean isPlayerTurn = (!isAIGame && gameState.getCurrentTurn() == playerColor) || 
                               (isAIGame && !isAITurn);
        
        if (gameState.getStatus() == GameStatus.IN_PROGRESS) {
            if (isPlayerTurn) {
                turnLabel.setText("Your turn");
                statusLabel.setText("Click on a column to place your piece");
            } else {
                turnLabel.setText(isAIGame ? "Computer's turn" : "Opponent's turn");
                statusLabel.setText("Waiting for " + (isAIGame ? "computer" : "opponent") + " to make a move");
            }
        } else {
            switch (gameState.getStatus()) {
                case RED_WINS:
                    if (playerColor == PlayerColor.RED || (isAIGame && playerColor != PlayerColor.RED)) {
                        turnLabel.setText("You win!");
                    } else {
                        turnLabel.setText(isAIGame ? "Computer wins!" : "Opponent wins!");
                    }
                    statusLabel.setText("Game Over");
                    break;
                case YELLOW_WINS:
                    if (playerColor == PlayerColor.YELLOW || (isAIGame && playerColor != PlayerColor.YELLOW)) {
                        turnLabel.setText("You win!");
                    } else {
                        turnLabel.setText(isAIGame ? "Computer wins!" : "Opponent wins!");
                    }
                    statusLabel.setText("Game Over");
                    break;
                case DRAW:
                    turnLabel.setText("Game ended in a draw");
                    statusLabel.setText("Game Over");
                    break;
                default:
                    break;
            }
            
            // Show play again buttons
            showPlayAgainButtons();
        }
    }
    
    /**
     * Shows the play again buttons.
     */
    private void showPlayAgainButtons() {
        // For AI games, we'll just enable the "Return to Lobby" button
        // For online games, we'll implement this in the handlers
    }
    
    /**
     * Requests to play again or decline a rematch.
     * 
     * @param wantToPlayAgain Whether the player wants to play again
     */
    private void requestPlayAgain(boolean wantToPlayAgain) {
        if (isAIGame) {
            if (wantToPlayAgain) {
                // Create a new game state for the AI game
                gameState = new GameState(gameClient.getUsername(), opponentUsername);
                
                // Reset the AI game
                isAITurn = (playerColor != PlayerColor.RED);
                
                // Update the UI
                updateBoard();
                updateGameStatus();
                
                // If AI goes first, make AI move
                if (isAITurn) {
                    makeAIMove();
                }
            } else {
                returnToLobby();
            }
        } else {
            // For online games, send the request to the server
            gameClient.requestPlayAgain(wantToPlayAgain);
        }
    }
    
    /**
     * Returns to the lobby/waiting screen.
     */
    private void returnToLobby() {
        // Clear game state
        gameState = null;
        
        // Show waiting screen
        WaitingScreen waitingScreen = new WaitingScreen(stage, gameClient);
        waitingScreen.show();
    }
    
    @Override
    public void onGameStarted(GameState gameState, PlayerColor assignedColor, String opponentUsername) {
        this.gameState = gameState;
        this.playerColor = assignedColor;
        this.opponentUsername = opponentUsername;
        
        updateBoard();
        updateGameStatus();
        
        // Clear the chat
        chatFlow.getChildren().clear();
    }
    
    @Override
    public void onGameStateUpdated(GameState gameState) {
        // Debug logging
        System.out.println("Game state updated. Current turn: " + gameState.getCurrentTurn() + 
                           ", Player color: " + playerColor + 
                           ", Is my turn: " + (gameState.getCurrentTurn() == playerColor));
        
        this.gameState = gameState;
        
        updateBoard();
        updateGameStatus();
    }
    
    @Override
    public void onOpponentDisconnected() {
        statusLabel.setText("Opponent disconnected");
        
        // Return to waiting screen
        WaitingScreen waitingScreen = new WaitingScreen(stage, gameClient);
        waitingScreen.show();
    }
    
    @Override
    public void onOpponentDeclinedRematch() {
        statusLabel.setText("Opponent declined rematch");
        
        // Return to waiting screen
        WaitingScreen waitingScreen = new WaitingScreen(stage, gameClient);
        waitingScreen.show();
    }
    
    @Override
    public void onConnectionEstablished() {
        // Not used
    }
    
    @Override
    public void onConnectionFailed(String reason) {
        // Not used
    }
    
    @Override
    public void onDisconnected(String reason) {
        // Show a message
        statusLabel.setText("Disconnected: " + reason);
        
        if (isAIGame) {
            // For AI games, just return to the waiting screen
            returnToLobby();
        } else {
            // For online games, return to login screen
            LoginScreen loginScreen = new LoginScreen(stage, gameClient);
            loginScreen.show();
        }
    }
    
    @Override
    public void onLoginSuccessful() {
        // Not used
    }
    
    @Override
    public void onLoginFailed(String reason) {
        // Not used
    }
    
    @Override
    public void onChatMessageReceived(ChatMessage message) {
        Platform.runLater(() -> {
            Text senderText = new Text(message.getSender() + ": ");
            senderText.setStyle("-fx-font-weight: bold;");
            
            Text messageText = new Text(message.getContent() + "\n");
            
            chatFlow.getChildren().addAll(senderText, messageText);
            
            // Auto-scroll to the bottom
            ((ScrollPane) chatFlow.getParent()).setVvalue(1.0);
        });
    }
} 