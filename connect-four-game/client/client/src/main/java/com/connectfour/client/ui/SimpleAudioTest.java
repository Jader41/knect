package com.connectfour.client.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import java.io.File;
import javax.sound.sampled.*;

/**
 * Simple test that generates audio tones directly.
 * This bypasses file loading issues to test system audio.
 */
public class SimpleAudioTest extends Application {
    
    private AudioClip clip;
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting SimpleToneTest...");
        
        // Create UI
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20px;");
        
        Label titleLabel = new Label("Simple Audio Test");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label statusLabel = new Label("Status: Ready");
        
        Button generateToneButton = new Button("Generate Tone (JavaFX)");
        Button playBeepButton = new Button("Play System Beep");
        Button stopButton = new Button("Stop Sound");
        
        root.getChildren().addAll(titleLabel, statusLabel, generateToneButton, playBeepButton, stopButton);
        
        // Set up event handlers
        generateToneButton.setOnAction(event -> {
            statusLabel.setText("Status: Generating tone...");
            try {
                // Create a simple audio clip with a generated tone URL
                String audioFile = SimpleAudioTest.class.getResource("/sounds/somber_background.mp3").toExternalForm();
                System.out.println("Loading audio file: " + audioFile);
                
                if (clip != null) {
                    clip.stop();
                }
                
                clip = new AudioClip(audioFile);
                clip.setVolume(1.0); // Maximum volume
                clip.setCycleCount(AudioClip.INDEFINITE); // Loop indefinitely
                clip.play();
                
                statusLabel.setText("Status: Playing tone (check volume)");
            } catch (Exception e) {
                System.err.println("Error generating tone: " + e.getMessage());
                e.printStackTrace();
                statusLabel.setText("Status: Error - " + e.getMessage());
            }
        });
        
        playBeepButton.setOnAction(event -> {
            statusLabel.setText("Status: Playing system beep...");
            try {
                // Play system beep using Java Sound API
                Toolkit.getDefaultToolkit().beep();
                
                // Create a sine wave tone
                generateAndPlayTone();
                
                statusLabel.setText("Status: Played system beep");
            } catch (Exception e) {
                System.err.println("Error playing beep: " + e.getMessage());
                e.printStackTrace();
                statusLabel.setText("Status: Error - " + e.getMessage());
            }
        });
        
        stopButton.setOnAction(event -> {
            if (clip != null) {
                clip.stop();
            }
            statusLabel.setText("Status: Stopped");
        });
        
        // Set up scene
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("Simple Audio Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void generateAndPlayTone() {
        try {
            System.out.println("Generating direct tone with Java Sound API...");
            
            // Audio format parameters
            float sampleRate = 44100.0f;
            int sampleSizeInBits = 16;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = false;
            
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            // Create a data line
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported!");
                return;
            }
            
            // Get and open the line
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            
            // Duration and frequency
            int durationMs = 1000; // 1 second
            float frequency = 440.0f; // A4 note
            
            // Generate the tone
            int bufferSize = (int) sampleRate * durationMs / 1000;
            byte[] buffer = new byte[bufferSize];
            
            for (int i = 0; i < bufferSize; i++) {
                double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                buffer[i] = (byte) (Math.sin(angle) * 100); // Amplitude
            }
            
            // Play the tone
            line.write(buffer, 0, buffer.length);
            
            // Wait for the buffer to finish playing
            line.drain();
            line.close();
            
            System.out.println("Tone generation completed");
        } catch (Exception e) {
            System.err.println("Error generating tone: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static class Toolkit {
        public static Toolkit getDefaultToolkit() {
            return new Toolkit();
        }
        
        public void beep() {
            System.out.println("System beep requested");
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
                System.out.println("System beep executed");
            } catch (Exception e) {
                System.err.println("Error playing system beep: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 