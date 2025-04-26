package com.connectfour.client.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Properties;

/**
 * Simple test application for audio playback.
 * Run this class directly to test if audio works on your system.
 */
public class AudioTest extends Application {
    
    private AudioPlayer audioPlayer;
    private Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) {
        // Enable verbose logging for JavaFX
        System.setProperty("javafx.verbose", "true");
        System.setProperty("prism.verbose", "true");
        System.setProperty("javafx.sound", "true");
        
        // Print system information
        printSystemInfo();
        
        // Create UI
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20px;");
        
        Label titleLabel = new Label("Audio Test");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        statusLabel = new Label("Status: Not Started");
        
        Button gameButton = new Button("Play Game Music");
        Button mp3Button = new Button("Play MP3 Sound");
        Button stopButton = new Button("Stop Sound");
        
        // Add volume slider
        Label volumeLabel = new Label("Volume:");
        Slider volumeSlider = new Slider(0, 1.0, 1.0);
        volumeSlider.setPrefWidth(150);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (audioPlayer != null) {
                audioPlayer.setVolume(newVal.doubleValue());
                statusLabel.setText("Volume set to: " + Math.round(newVal.doubleValue() * 100) + "%");
            }
        });
        
        HBox volumeBox = new HBox(10, volumeLabel, volumeSlider);
        
        root.getChildren().addAll(
            titleLabel, 
            statusLabel, 
            gameButton, 
            mp3Button, 
            stopButton,
            volumeBox
        );
        
        // Set up event handlers
        gameButton.setOnAction(event -> {
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.dispose();
            }
            statusLabel.setText("Status: Initializing Game Music...");
            audioPlayer = new AudioPlayer("game_music.mp3");
            audioPlayer.setVolume(volumeSlider.getValue());
            audioPlayer.play();
            statusLabel.setText("Status: Playing Game Music");
        });
        
        mp3Button.setOnAction(event -> {
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.dispose();
            }
            statusLabel.setText("Status: Initializing MP3...");
            audioPlayer = new AudioPlayer("somber_background.mp3");
            audioPlayer.setVolume(volumeSlider.getValue());
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
        Scene scene = new Scene(root, 300, 250);
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
    
    private void printSystemInfo() {
        // Print system properties relevant to audio
        System.out.println("---- SYSTEM INFORMATION ----");
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        
        // Print all JavaFX properties
        System.out.println("---- JAVAFX PROPERTIES ----");
        Properties properties = System.getProperties();
        properties.forEach((key, value) -> {
            if (key.toString().contains("javafx") || key.toString().contains("prism")) {
                System.out.println(key + " = " + value);
            }
        });
        
        System.out.println("-------------------------");
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