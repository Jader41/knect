package com.connectfour.client.ui;

import com.connectfour.client.GameClient;
import com.connectfour.client.ai.AIPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

// 
// The login screen where users enter their username to connect to the server.
public class LoginScreen implements GameClient.ConnectionListener {
    private final Stage stage;
    private final GameClient gameClient;
    private TextField usernameField;
    private Button playOnlineButton;
    private Button playWithComputerButton;
    private Label statusLabel;
    
    public LoginScreen(Stage stage, GameClient gameClient) {
        this.stage = stage;
        this.gameClient = gameClient;
        
        // Add this as a connection listener
        gameClient.addConnectionListener(this);
    }
    
    // 
// Shows the login screen.
    public void show() {
        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();
        
        // Calculate 50% of screen size
        double width = screenWidth * 0.5;
        double height = screenHeight * 0.5;
        
        // Create UI components
        // Stylised game title
        Label titleLabel = new Label("KNECT4");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-family: 'Arial Black'; -fx-text-fill: #444444;");
        
        // Add subtle drop shadow for artistic effect
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setOffsetY(3.0);
        ds.setColor(javafx.scene.paint.Color.rgb(50, 50, 50, 0.3));
        titleLabel.setEffect(ds);
        
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(250);
        
        // Create buttons
        playOnlineButton = new Button("Play Online");
        playOnlineButton.setDefaultButton(true);
        playOnlineButton.setOnAction(e -> connectToServer());
        
        playWithComputerButton = new Button("Play with Computer");
        playWithComputerButton.setOnAction(e -> startLocalGame());
        
        Button leaderboardButton = new Button("Leaderboard");
        leaderboardButton.setOnAction(e -> LeaderboardDialog.show(stage));
        
        // Create button layout
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(playOnlineButton, playWithComputerButton, leaderboardButton);
        
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: red;");
        
        // Create layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #F7F5F2;");
        layout.getChildren().addAll(titleLabel, usernameLabel, usernameField, buttonBox, statusLabel);
        
        // Create scene with 50% of screen size
        Scene scene = new Scene(layout, width, height);
        
        // Set the scene to the stage and center it
        stage.setScene(scene);
        stage.setX((screenWidth - width) / 2);
        stage.setY((screenHeight - height) / 2);
        stage.show();
    }
    
    // 
// Attempts to connect to server for online play.
    private void connectToServer() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return;
        }
        
        if (username.length() > 20) {
            statusLabel.setText("Username must be at most 20 characters");
            return;
        }
        
        // Disable the buttons and update status
        playOnlineButton.setDisable(true);
        playWithComputerButton.setDisable(true);
        statusLabel.setText("Connecting to server...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        
        // Connect to the server
        gameClient.connect(username);
    }
    
    // 
// Start a local game against the computer AI.
    private void startLocalGame() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return;
        }
        
        if (username.length() > 20) {
            statusLabel.setText("Username must be at most 20 characters");
            return;
        }
        
        // Create difficulty selection dialog
        VBox dialogContent = new VBox(10);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(20));
        
        Label difficultyLabel = new Label("Select AI Difficulty:");
        difficultyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create difficulty buttons
        Button easyButton = new Button("Easy");
        Button mediumButton = new Button("Medium");
        Button hardButton = new Button("Hard");
        
        // Style buttons
        String buttonStyle = "-fx-min-width: 100px; -fx-padding: 10px;";
        easyButton.setStyle(buttonStyle);
        mediumButton.setStyle(buttonStyle);
        hardButton.setStyle(buttonStyle);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(easyButton, mediumButton, hardButton);
        
        dialogContent.getChildren().addAll(difficultyLabel, buttonBox);
        
        // Create dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("AI Difficulty");
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        // Set button actions
        easyButton.setOnAction(e -> {
            dialog.setResult("Easy");
            dialog.close();
        });
        
        mediumButton.setOnAction(e -> {
            dialog.setResult("Medium");
            dialog.close();
        });
        
        hardButton.setOnAction(e -> {
            dialog.setResult("Hard");
            dialog.close();
        });
        
        // Show dialog and start game with selected difficulty
        dialog.showAndWait().ifPresent(difficulty -> {
            // Remove this as a connection listener
            gameClient.removeConnectionListener(this);
            
            // Start local game against AI with selected difficulty
            GameScreen gameScreen = new GameScreen(stage, gameClient, username, "Computer (" + difficulty + ")", difficulty);
            gameScreen.show();
        });
    }
    
    @Override
    public void onConnectionEstablished() {
        statusLabel.setText("Connected to server, logging in...");
    }
    
    @Override
    public void onConnectionFailed(String reason) {
        statusLabel.setText("Connection failed: " + reason);
        statusLabel.setStyle("-fx-text-fill: red;");
        playOnlineButton.setDisable(false);
        playWithComputerButton.setDisable(false);
    }
    
    @Override
    public void onDisconnected(String reason) {
        statusLabel.setText("Disconnected: " + reason);
        statusLabel.setStyle("-fx-text-fill: red;");
        playOnlineButton.setDisable(false);
        playWithComputerButton.setDisable(false);
    }
    
    @Override
    public void onLoginSuccessful() {
        // Remove this as a connection listener
        gameClient.removeConnectionListener(this);
        
        // Show the waiting screen
        WaitingScreen waitingScreen = new WaitingScreen(stage, gameClient);
        waitingScreen.show();
    }
    
    @Override
    public void onLoginFailed(String reason) {
        statusLabel.setText("Login failed: " + reason);
        statusLabel.setStyle("-fx-text-fill: red;");
        playOnlineButton.setDisable(false);
        playWithComputerButton.setDisable(false);
    }
} 
