package com.connectfour.client.ui;

import com.connectfour.client.GameClient;
import com.connectfour.common.messages.ChatMessage;
import com.connectfour.common.model.CellState;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.GameStatus;
import com.connectfour.common.model.PlayerColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
    
    public GameScreen(Stage stage, GameClient gameClient) {
        this.stage = stage;
        this.gameClient = gameClient;
        
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
     * Shows the game screen.
     */
    public void show() {
        // Create the root layout
        rootLayout = new BorderPane();
        
        // Create the game board
        createGameBoard();
        
        // Create the info panel
        VBox infoPanel = createInfoPanel();
        rootLayout.setRight(infoPanel);
        
        // Create the chat panel
        VBox chatPanel = createChatPanel();
        rootLayout.setBottom(chatPanel);
        
        // Create scene
        Scene scene = new Scene(rootLayout, 800, 600);
        
        // Set the scene to the stage
        stage.setScene(scene);
        
        // Update the board with the current game state
        updateBoard();
        updateGameStatus();
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
        boardGrid.setStyle("-fx-background-color: blue;");
        
        boardCells = new Circle[GameState.ROWS][GameState.COLUMNS];
        
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(70, 70);
                
                Circle circle = new Circle(30);
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
                
                boardCells[row][col] = circle;
                
                cell.getChildren().add(circle);
                
                final int column = col;
                cell.setOnMouseClicked(e -> makeMove(column));
                
                boardGrid.add(cell, col, row);
            }
        }
        
        rootLayout.setCenter(boardGrid);
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
        infoPanel.setStyle("-fx-background-color: #f0f0f0;");
        
        // Player info
        Label playerLabel = new Label("You: " + gameClient.getUsername());
        playerLabel.setStyle("-fx-font-weight: bold;");
        
        Circle playerCircle = new Circle(15);
        playerCircle.setFill(playerColor == PlayerColor.RED ? Color.RED : Color.YELLOW);
        playerCircle.setStroke(Color.BLACK);
        playerCircle.setStrokeWidth(1);
        
        HBox playerInfo = new HBox(10);
        playerInfo.setAlignment(Pos.CENTER_LEFT);
        playerInfo.getChildren().addAll(playerCircle, playerLabel);
        
        // Opponent info
        Label opponentLabel = new Label("Opponent: " + opponentUsername);
        opponentLabel.setStyle("-fx-font-weight: bold;");
        
        Circle opponentCircle = new Circle(15);
        opponentCircle.setFill(playerColor == PlayerColor.RED ? Color.YELLOW : Color.RED);
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
        
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        
        infoPanel.getChildren().addAll(playerInfo, opponentInfo, separator, turnLabel, statusLabel);
        
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
        chatPanel.setStyle("-fx-background-color: #f0f0f0;");
        
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
        boolean isPlayerTurn = (gameState.getCurrentTurn() == playerColor);
        
        if (isPlayerTurn && gameState.getStatus() == GameStatus.IN_PROGRESS) {
            gameClient.makeMove(column);
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
        for (int row = 0; row < GameState.ROWS; row++) {
            for (int col = 0; col < GameState.COLUMNS; col++) {
                CellState cellState = gameState.getCellState(row, col);
                
                switch (cellState) {
                    case EMPTY:
                        boardCells[row][col].setFill(Color.WHITE);
                        break;
                    case RED:
                        boardCells[row][col].setFill(Color.RED);
                        break;
                    case YELLOW:
                        boardCells[row][col].setFill(Color.YELLOW);
                        break;
                }
            }
        }
    }
    
    /**
     * Updates the game status and turn information.
     */
    private void updateGameStatus() {
        // Update turn label
        boolean isPlayerTurn = (gameState.getCurrentTurn() == playerColor);
        
        if (gameState.getStatus() == GameStatus.IN_PROGRESS) {
            turnLabel.setText(isPlayerTurn ? "Your turn" : "Opponent's turn");
            turnLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isPlayerTurn ? "green" : "black") + ";");
        } else {
            turnLabel.setText("Game over");
        }
        
        // Update status label
        switch (gameState.getStatus()) {
            case IN_PROGRESS:
                statusLabel.setText("");
                break;
            case RED_WINS:
                boolean playerWon = (playerColor == PlayerColor.RED);
                statusLabel.setText(playerWon ? "You won!" : "Opponent won!");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (playerWon ? "green" : "red") + ";");
                showGameOverDialog(playerWon);
                break;
            case YELLOW_WINS:
                playerWon = (playerColor == PlayerColor.YELLOW);
                statusLabel.setText(playerWon ? "You won!" : "Opponent won!");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (playerWon ? "green" : "red") + ";");
                showGameOverDialog(playerWon);
                break;
            case DRAW:
                statusLabel.setText("Game ended in a draw!");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: blue;");
                showGameOverDialog(false);
                break;
        }
    }
    
    /**
     * Shows a dialog when the game is over, asking if the player wants to play again.
     * 
     * @param playerWon Whether the player won the game
     */
    private void showGameOverDialog(boolean playerWon) {
        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        
        // Set the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER);
        
        Label resultLabel = new Label(playerWon ? "You won!" : 
                                      (gameState.getStatus() == GameStatus.DRAW ? "It's a draw!" : "You lost!"));
        resultLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label questionLabel = new Label("Would you like to play again?");
        
        content.getChildren().addAll(resultLabel, questionLabel);
        
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType playAgainButton = new ButtonType("Play Again", ButtonBar.ButtonData.YES);
        ButtonType quitButton = new ButtonType("Quit", ButtonBar.ButtonData.NO);
        
        dialog.getDialogPane().getButtonTypes().addAll(playAgainButton, quitButton);
        
        // Show the dialog
        dialog.showAndWait().ifPresent(response -> {
            if (response == playAgainButton) {
                gameClient.requestPlayAgain(true);
            } else {
                gameClient.requestPlayAgain(false);
                
                // Go back to login screen
                gameClient.removeGameStateListener(this);
                gameClient.removeConnectionListener(this);
                gameClient.removeChatMessageListener(this);
                
                LoginScreen loginScreen = new LoginScreen(stage, gameClient);
                loginScreen.show();
            }
        });
    }
    
    /**
     * Shows a dialog when the opponent disconnects.
     */
    private void showOpponentDisconnectedDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Opponent Disconnected");
        alert.setHeaderText("Your opponent has disconnected");
        alert.setContentText("You will be returned to the login screen.");
        
        alert.showAndWait();
        
        // Go back to login screen
        gameClient.removeGameStateListener(this);
        gameClient.removeConnectionListener(this);
        gameClient.removeChatMessageListener(this);
        
        LoginScreen loginScreen = new LoginScreen(stage, gameClient);
        loginScreen.show();
    }
    
    /**
     * Shows a dialog when the opponent declines a rematch.
     */
    private void showOpponentDeclinedRematchDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Rematch");
        alert.setHeaderText("Your opponent declined to play again");
        alert.setContentText("You will be returned to the login screen.");
        
        alert.showAndWait();
        
        // Go back to login screen
        gameClient.removeGameStateListener(this);
        gameClient.removeConnectionListener(this);
        gameClient.removeChatMessageListener(this);
        
        LoginScreen loginScreen = new LoginScreen(stage, gameClient);
        loginScreen.show();
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
        this.gameState = gameState;
        
        updateBoard();
        updateGameStatus();
    }
    
    @Override
    public void onOpponentDisconnected() {
        showOpponentDisconnectedDialog();
    }
    
    @Override
    public void onOpponentDeclinedRematch() {
        showOpponentDeclinedRematchDialog();
    }
    
    @Override
    public void onConnectionEstablished() {
        // Not used in game screen
    }
    
    @Override
    public void onConnectionFailed(String reason) {
        // Not used in game screen
    }
    
    @Override
    public void onDisconnected(String reason) {
        // Show login screen again
        gameClient.removeGameStateListener(this);
        gameClient.removeConnectionListener(this);
        gameClient.removeChatMessageListener(this);
        
        LoginScreen loginScreen = new LoginScreen(stage, gameClient);
        loginScreen.show();
    }
    
    @Override
    public void onLoginSuccessful() {
        // Not used in game screen
    }
    
    @Override
    public void onLoginFailed(String reason) {
        // Not used in game screen
    }
    
    @Override
    public void onChatMessageReceived(ChatMessage message) {
        Text messageSender = new Text(message.getSender() + ": ");
        messageSender.setStyle("-fx-font-weight: bold;");
        
        Text messageContent = new Text(message.getContent() + "\n");
        
        chatFlow.getChildren().addAll(messageSender, messageContent);
        
        // Scroll to the bottom
        chatFlow.layout();
    }
} 