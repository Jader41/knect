package com.connectfour.client;

import com.connectfour.client.ui.LoginScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

// 
// Main entry point for the Connect Four client application.
public class ClientApp extends Application {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    
    private String host;
    private int port;
    
    // Add required JavaFX modules for media playback
    static {
        System.setProperty("javafx.verbose", "true");
        System.setProperty("prism.verbose", "true");
        System.setProperty("javafx.sound", "true");
    }
    
    @Override
    public void start(Stage primaryStage) {
        // Set the window title
        primaryStage.setTitle("Connect Four");
        primaryStage.setResizable(false);
        
        // Get the host and port from parameters or use defaults
        Parameters params = getParameters();
        host = params.getNamed().getOrDefault("host", DEFAULT_HOST);
        
        try {
            port = Integer.parseInt(params.getNamed().getOrDefault("port", String.valueOf(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
            System.err.println("Invalid port number, using default: " + DEFAULT_PORT);
        }
        
        // Set up connection listener that will handle reconnection attempts
        GameClient gameClient = new GameClient(host, port);
        
        // Create and show the login screen
        LoginScreen loginScreen = new LoginScreen(primaryStage, gameClient);
        loginScreen.show();
        
        // Add a close handler to clean up resources
        primaryStage.setOnCloseRequest(e -> {
            gameClient.disconnect("User closed application");
            Platform.exit();
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 
