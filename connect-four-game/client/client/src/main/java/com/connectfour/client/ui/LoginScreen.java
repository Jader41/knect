package com.connectfour.client.ui;

import com.connectfour.client.GameClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The login screen where users enter their username to connect to the server.
 */
public class LoginScreen implements GameClient.ConnectionListener {
    private final Stage stage;
    private final GameClient gameClient;
    private TextField usernameField;
    private Button loginButton;
    private Label statusLabel;
    
    public LoginScreen(Stage stage, GameClient gameClient) {
        this.stage = stage;
        this.gameClient = gameClient;
        
        // Add this as a connection listener
        gameClient.addConnectionListener(this);
    }
    
    /**
     * Shows the login screen.
     */
    public void show() {
        // Create UI components
        Label titleLabel = new Label("Connect Four");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(200);
        
        loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> login());
        
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: red;");
        
        // Create layout
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, usernameLabel, usernameField, loginButton, statusLabel);
        
        // Create scene
        Scene scene = new Scene(layout, 400, 300);
        
        // Set the scene to the stage
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Attempts to log in with the username entered in the field.
     */
    private void login() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return;
        }
        
        if (username.length() > 20) {
            statusLabel.setText("Username must be at most 20 characters");
            return;
        }
        
        // Disable the login button and update status
        loginButton.setDisable(true);
        statusLabel.setText("Connecting...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        
        // Connect to the server
        gameClient.connect(username);
    }
    
    @Override
    public void onConnectionEstablished() {
        statusLabel.setText("Connected to server, logging in...");
    }
    
    @Override
    public void onConnectionFailed(String reason) {
        statusLabel.setText("Connection failed: " + reason);
        statusLabel.setStyle("-fx-text-fill: red;");
        loginButton.setDisable(false);
    }
    
    @Override
    public void onDisconnected(String reason) {
        statusLabel.setText("Disconnected: " + reason);
        statusLabel.setStyle("-fx-text-fill: red;");
        loginButton.setDisable(false);
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
        loginButton.setDisable(false);
    }
} 