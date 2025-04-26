package com.connectfour.client.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Simple test application for audio playback.
 * Run this class directly to test if audio works on your system.
 */
public class AudioTest extends Application {
    
    private AudioPlayer audioPlayer;
    private Label volumeLabel;
    
    @Override
    public void start(Stage primaryStage) {
        // Enable verbose logging for JavaFX
        System.setProperty("javafx.verbose", "true");
        System.setProperty("prism.verbose", "true");
        System.setProperty("javafx.sound", "true");
        
        // Create UI
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20px;");
        
        Label titleLabel = new Label("Audio Test");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label statusLabel = new Label("Status: Not Started");
        
        Button wavButton = new Button("Play WAV Sound");
        Button mp3Button = new Button("Play MP3 Sound");
        Button stopButton = new Button("Stop Sound");
        
        // Add volume controls
        Label volumeTextLabel = new Label("Volume:");
        volumeLabel = new Label("1.0");
        Slider volumeSlider = new Slider(0, 10, 1);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(1);
        volumeSlider.setMinorTickCount(0);
        volumeSlider.setSnapToTicks(true);
        
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue();
            volumeLabel.setText(String.format("%.1f", volume));
            if (audioPlayer != null) {
                audioPlayer.setVolume(volume);
            }
        });
        
        HBox volumeBox = new HBox(10, volumeTextLabel, volumeSlider, volumeLabel);
        
        root.getChildren().addAll(titleLabel, statusLabel, wavButton, mp3Button, stopButton, volumeBox);
        
        // Set up event handlers
        wavButton.setOnAction(event -> {
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.dispose();
            }
            statusLabel.setText("Status: Initializing WAV...");
            audioPlayer = new AudioPlayer("background_music.wav");
            double volume = volumeSlider.getValue();
            audioPlayer.setVolume(volume);
            audioPlayer.play();
            statusLabel.setText("Status: Playing WAV");
        });
        
        mp3Button.setOnAction(event -> {
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.dispose();
            }
            statusLabel.setText("Status: Initializing MP3...");
            audioPlayer = new AudioPlayer("somber_background.mp3");
            double volume = volumeSlider.getValue();
            audioPlayer.setVolume(volume);
            audioPlayer.play();
            statusLabel.setText("Status: Playing MP3");
        });
        
        stopButton.setOnAction(event -> {
            if (audioPlayer != null) {
                audioPlayer.stop();
                statusLabel.setText("Status: Stopped");
            }
        });
        
        // Set up scene
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Audio Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Clean up on window close
        primaryStage.setOnCloseRequest(event -> {
            if (audioPlayer != null) {
                audioPlayer.dispose();
            }
        });
    }
    
    @Override
    public void stop() {
        if (audioPlayer != null) {
            audioPlayer.dispose();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 