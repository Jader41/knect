package com.connectfour.client.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Simple test application for the emergency tone generator.
 */
public class EmergencyAudioTest extends Application {
    
    private EmergencyTonePlayer tonePlayer;
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting EmergencyAudioTest...");
        
        // Create UI
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20px;");
        
        Label titleLabel = new Label("Emergency Audio Test");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label statusLabel = new Label("Status: Not Started");
        
        Button playButton = new Button("Play Tone");
        Button stopButton = new Button("Stop Tone");
        
        root.getChildren().addAll(titleLabel, statusLabel, playButton, stopButton);
        
        // Initialize tone player
        tonePlayer = new EmergencyTonePlayer();
        
        // Set up event handlers
        playButton.setOnAction(event -> {
            statusLabel.setText("Status: Playing tone...");
            tonePlayer.start();
        });
        
        stopButton.setOnAction(event -> {
            statusLabel.setText("Status: Stopped");
            tonePlayer.stop();
        });
        
        // Set up scene
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("Emergency Audio Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Clean up on window close
        primaryStage.setOnCloseRequest(event -> {
            if (tonePlayer != null) {
                tonePlayer.stop();
            }
        });
    }
    
    @Override
    public void stop() {
        if (tonePlayer != null) {
            tonePlayer.stop();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 