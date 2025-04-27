package com.connectfour.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

// 
// JavaFX GUI application for the Connect Four server.
public class ServerAppGUI extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ServerAppGUI.class);
    private static final int DEFAULT_PORT = 8080;
    
    private GameServer server;
    private TextArea logArea;
    private Label statusLabel;
    private Label connectionCountLabel;
    private Label uptimeLabel;
    private Button startStopButton;
    private AtomicInteger connectionCount = new AtomicInteger(0);
    private LocalDateTime startTime;
    private boolean serverRunning = false;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Four Server Console");
        
        // Create UI components
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Header with server status
        VBox headerBox = new VBox(5);
        HBox statusBox = new HBox(10);
        Label statusTextLabel = new Label("Server Status:");
        statusTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        statusLabel = new Label("STARTING...");
        statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-font-size: 14px;");
        statusBox.getChildren().addAll(statusTextLabel, statusLabel);
        
        HBox connectionBox = new HBox(10);
        Label connectionTextLabel = new Label("Connected Clients:");
        connectionTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        connectionCountLabel = new Label("0");
        connectionCountLabel.setStyle("-fx-font-size: 14px;");
        connectionBox.getChildren().addAll(connectionTextLabel, connectionCountLabel);
        
        HBox uptimeBox = new HBox(10);
        Label uptimeTextLabel = new Label("Uptime:");
        uptimeTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        uptimeLabel = new Label("00:00:00");
        uptimeLabel.setStyle("-fx-font-size: 14px;");
        uptimeBox.getChildren().addAll(uptimeTextLabel, uptimeLabel);
        
        headerBox.getChildren().addAll(statusBox, connectionBox, uptimeBox);
        headerBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        root.setTop(headerBox);
        
        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        root.setCenter(logArea);
        
        // Control buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        startStopButton = new Button("Stop Server");
        startStopButton.setPrefWidth(120);
        startStopButton.setStyle("-fx-font-weight: bold;");
        startStopButton.setOnAction(e -> toggleServer());
        
        Button clearLogButton = new Button("Clear Log");
        clearLogButton.setPrefWidth(120);
        clearLogButton.setOnAction(e -> logArea.clear());
        
        buttonBox.getChildren().addAll(startStopButton, clearLogButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        root.setBottom(buttonBox);
        
        // Set up the scene
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Position the window in the center of the screen
        primaryStage.centerOnScreen();
        
        // Add initial message to log area
        logArea.appendText("Connect Four Server Console\n");
        logArea.appendText("-------------------------\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        logArea.appendText("[" + formatter.format(LocalDateTime.now()) + "] Server GUI started\n");
        
        // Set up logging to text area
        redirectSystemOut();
        
        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            if (server != null) {
                server.stop();
            }
            Platform.exit();
        });
        
        // Start a thread for updating the uptime
        Thread uptimeThread = new Thread(() -> {
            while (true) {
                if (serverRunning && startTime != null) {
                    updateUptime();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        uptimeThread.setDaemon(true);
        uptimeThread.start();
        
        // Auto-start the server when the GUI launches
        Platform.runLater(() -> {
            startServer();
            logArea.appendText("[" + formatter.format(LocalDateTime.now()) + "] Server auto-started on port " + DEFAULT_PORT + "\n");
        });
    }
    
    private void toggleServer() {
        if (serverRunning) {
            stopServer();
        } else {
            startServer();
        }
    }
    
    private void startServer() {
        try {
            server = new GameServer(DEFAULT_PORT) {
                // Custom implementation to track client connections
                public void clientConnected(ClientHandler client) {
                    // Update connection count
                    updateConnectionCount(1);
                }
                
                public void clientDisconnected(ClientHandler client) {
                    // Update connection count
                    updateConnectionCount(-1);
                }
            };
            
            server.start();
            serverRunning = true;
            startTime = LocalDateTime.now();
            
            // Update UI
            Platform.runLater(() -> {
                statusLabel.setText("RUNNING");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                startStopButton.setText("Stop Server");
            });
            
            logger.info("Server started on port {}", DEFAULT_PORT);
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            Platform.runLater(() -> {
                logArea.appendText("ERROR: Failed to start server: " + e.getMessage() + "\n");
            });
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop();
            serverRunning = false;
            
            // Update UI
            Platform.runLater(() -> {
                statusLabel.setText("STOPPED");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                startStopButton.setText("Start Server");
                connectionCountLabel.setText("0");
            });
            
            logger.info("Server stopped");
        }
    }
    
    private void updateConnectionCount(int delta) {
        int count = connectionCount.addAndGet(delta);
        Platform.runLater(() -> {
            connectionCountLabel.setText(String.valueOf(count));
        });
    }
    
    private void updateUptime() {
        if (startTime != null && serverRunning) {
            LocalDateTime now = LocalDateTime.now();
            long seconds = java.time.Duration.between(startTime, now).getSeconds();
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            
            String uptimeStr = String.format("%02d:%02d:%02d", hours, minutes, secs);
            Platform.runLater(() -> {
                uptimeLabel.setText(uptimeStr);
            });
        }
    }
    
    private void redirectSystemOut() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        // Create a custom OutputStream that writes to the TextArea
        OutputStream out = new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();
            
            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                buffer.append(c);
                
                if (c == '\n') {
                    String text = buffer.toString();
                    buffer.setLength(0);
                    
                    // Write to original console as well
                    originalOut.print(text);
                    
                    // Add timestamp to log message
                    String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
                    String logMessage = "[" + timestamp + "] " + text;
                    
                    // Update the UI on the JavaFX thread
                    Platform.runLater(() -> {
                        logArea.appendText(logMessage);
                        
                        // Auto-scroll to bottom
                        logArea.setScrollTop(Double.MAX_VALUE);
                    });
                }
            }
        };
        
        // Redirect both standard output and error output
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
        
        // Add a test log message
        System.out.println("System output redirected to GUI console");
    }
} 
