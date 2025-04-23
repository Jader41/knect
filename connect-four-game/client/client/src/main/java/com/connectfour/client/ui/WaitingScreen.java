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
        
        // Create layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, statusLabel, cancelButton);
        
        // Create scene
        Scene scene = new Scene(layout, 400, 300);
        
        // Set the scene to the stage
        stage.setScene(scene);
        
        // Start waiting animation
        startWaitingAnimation();
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