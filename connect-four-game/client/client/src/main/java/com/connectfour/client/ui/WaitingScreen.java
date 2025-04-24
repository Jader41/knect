package com.connectfour.client.ui;

import com.connectfour.client.GameClient;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.PlayerColor;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Screen shown while waiting for an opponent to join the game.
 */
public class WaitingScreen implements GameClient.ConnectionListener, GameClient.GameStateListener {
    private final Stage stage;
    private final GameClient gameClient;
    private Label statusLabel;
    private Timeline waitingAnimation;
    private int dots = 0;
    
    public WaitingScreen(Stage stage, GameClient gameClient) {
        this.stage = stage;
        this.gameClient = gameClient;
        
        // Add listeners
        gameClient.addConnectionListener(this);
        gameClient.addGameStateListener(this);
    }
    
    /**
     * Shows the waiting screen.
     */
    public void show() {
        // Create UI components
        Label titleLabel = new Label("Connect Four");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        statusLabel = new Label("Waiting for opponent");
        statusLabel.setStyle("-fx-font-size: 16px;");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            gameClient.disconnect("User cancelled matchmaking");
            
            // Show the login screen again
            LoginScreen loginScreen = new LoginScreen(stage, gameClient);
            loginScreen.show();
        });
        
        Button playVsComputerButton = new Button("Play vs Computer");
        playVsComputerButton.setOnAction(e -> {
            stopWaitingAnimation();
            showAIDifficultySelection();
        });
        
        // Create button layout
        HBox buttonLayout = new HBox(10);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(cancelButton, playVsComputerButton);
        
        // Create layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, statusLabel, buttonLayout);
        
        // Create scene
        Scene scene = new Scene(layout, 400, 300);
        
        // Set the scene to the stage
        stage.setScene(scene);
        
        // Start waiting animation
        startWaitingAnimation();
    }
    
    /**
     * Shows the AI difficulty selection screen.
     */
    private void showAIDifficultySelection() {
        // Remove from matchmaking queue if we're in it
        gameClient.cancelMatchmaking();
        
        // Create UI components
        Label titleLabel = new Label("Select AI Difficulty");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label promptLabel = new Label("Choose computer difficulty level:");
        promptLabel.setStyle("-fx-font-size: 16px;");
        
        Button easyButton = new Button("Easy");
        easyButton.setPrefWidth(120);
        easyButton.setOnAction(e -> startAIGame("Easy"));
        
        Button mediumButton = new Button("Medium");
        mediumButton.setPrefWidth(120);
        mediumButton.setOnAction(e -> startAIGame("Medium"));
        
        Button hardButton = new Button("Hard");
        hardButton.setPrefWidth(120);
        hardButton.setOnAction(e -> startAIGame("Hard"));
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show()); // Go back to waiting screen
        
        // Create difficulty buttons layout
        VBox difficultyButtons = new VBox(10);
        difficultyButtons.setAlignment(Pos.CENTER);
        difficultyButtons.getChildren().addAll(easyButton, mediumButton, hardButton);
        
        // Create layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, promptLabel, difficultyButtons, backButton);
        
        // Create scene
        Scene scene = new Scene(layout, 400, 300);
        
        // Set the scene to the stage
        stage.setScene(scene);
    }
    
    /**
     * Starts a game against the AI with the specified difficulty.
     * 
     * @param difficulty The AI difficulty level
     */
    private void startAIGame(String difficulty) {
        // Remove from matchmaking queue if we're in it
        gameClient.cancelMatchmaking();
        
        // Remove listeners before starting AI game to avoid unnecessary callbacks
        gameClient.removeConnectionListener(this);
        gameClient.removeGameStateListener(this);
        
        // Create a local game state
        GameState gameState = new GameState(gameClient.getUsername(), "Computer (" + difficulty + ")");
        
        // Show the game screen with AI opponent
        GameScreen gameScreen = new GameScreen(stage, gameClient, gameState, PlayerColor.RED, difficulty);
        gameScreen.show();
    }
    
    /**
     * Starts the waiting animation (dots).
     */
    private void startWaitingAnimation() {
        waitingAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0.5), e -> {
                dots = (dots + 1) % 4;
                StringBuilder dotsString = new StringBuilder();
                for (int i = 0; i < dots; i++) {
                    dotsString.append(".");
                }
                statusLabel.setText("Waiting for opponent" + dotsString);
            })
        );
        waitingAnimation.setCycleCount(Animation.INDEFINITE);
        waitingAnimation.play();
    }
    
    /**
     * Stops the waiting animation.
     */
    private void stopWaitingAnimation() {
        if (waitingAnimation != null) {
            waitingAnimation.stop();
        }
    }
    
    @Override
    public void onConnectionEstablished() {
        // Not used
    }
    
    @Override
    public void onConnectionFailed(String reason) {
        // Not used - already handled by LoginScreen
    }
    
    @Override
    public void onDisconnected(String reason) {
        stopWaitingAnimation();
        
        // Show login screen again
        LoginScreen loginScreen = new LoginScreen(stage, gameClient);
        loginScreen.show();
    }
    
    @Override
    public void onLoginSuccessful() {
        // Not used - already handled by LoginScreen
    }
    
    @Override
    public void onLoginFailed(String reason) {
        // Not used - already handled by LoginScreen
    }
    
    @Override
    public void onGameStarted(GameState gameState, PlayerColor assignedColor, String opponentUsername) {
        // Game is starting, stop the waiting animation
        stopWaitingAnimation();
        
        // Remove listeners
        gameClient.removeConnectionListener(this);
        gameClient.removeGameStateListener(this);
        
        // Show the game screen
        GameScreen gameScreen = new GameScreen(stage, gameClient);
        gameScreen.show();
    }
    
    @Override
    public void onGameStateUpdated(GameState gameState) {
        // Not used in waiting screen
    }
    
    @Override
    public void onOpponentDisconnected() {
        // Not used in waiting screen
    }
    
    @Override
    public void onOpponentDeclinedRematch() {
        // Not used in waiting screen
    }
} 