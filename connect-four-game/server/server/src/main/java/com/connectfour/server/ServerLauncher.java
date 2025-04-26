package com.connectfour.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main entry point for the Connect Four server application.
 * This launcher allows the server to be started as a standalone application.
 */
public class ServerLauncher extends Application {
    
    private TextArea logArea;
    private GameServer server;
    
    /**
     * Main method to launch the server GUI application.
     * 
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        // Set up the window
        stage.setTitle("Connect Four Server");
        
        // Create a simple TextArea for logs
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        
        BorderPane root = new BorderPane();
        root.setCenter(logArea);
        
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Redirect console output to GUI
        redirectConsoleOutput();
        
        // Display header
        logArea.appendText("CONNECT FOUR SERVER\n");
        logArea.appendText("==================\n\n");
        
        // Start the server
        Platform.runLater(() -> {
            startServer();
        });
        
        // Handle window close
        stage.setOnCloseRequest(e -> {
            if (server != null) {
                server.stop();
            }
            Platform.exit();
        });
    }
    
    private void startServer() {
        try {
            server = new GameServer(8080);
            logMessage("Starting server on port 8080...");
            
            // Start the server in a separate thread
            new Thread(() -> {
                server.start();
            }).start();
            
            logMessage("Server started successfully");
        } catch (Exception e) {
            logMessage("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void redirectConsoleOutput() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        OutputStream out = new OutputStream() {
            private StringBuilder buffer = new StringBuilder();
            
            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                buffer.append(c);
                
                if (c == '\n') {
                    String text = buffer.toString();
                    buffer = new StringBuilder();
                    
                    // Output to original console as well
                    originalOut.print(text);
                    
                    // Add to log TextArea
                    final String line = text;
                    Platform.runLater(() -> {
                        logArea.appendText(line);
                        logArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll
                    });
                }
            }
        };
        
        // Replace System.out and System.err
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
        
        System.out.println("Console output redirected to GUI");
    }
    
    private void logMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Platform.runLater(() -> {
            logArea.appendText("[" + timestamp + "] " + message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll
        });
    }
} 